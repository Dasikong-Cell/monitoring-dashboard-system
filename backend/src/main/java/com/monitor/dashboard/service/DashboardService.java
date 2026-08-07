package com.monitor.dashboard.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monitor.dashboard.entity.AccessLog;
import com.monitor.dashboard.entity.Alert;
import com.monitor.dashboard.entity.Device;
import com.monitor.dashboard.entity.DeviceMetric;
import com.monitor.dashboard.mapper.AccessLogMapper;
import com.monitor.dashboard.mapper.AlertMapper;
import com.monitor.dashboard.mapper.DeviceMapper;
import com.monitor.dashboard.mapper.DeviceMetricMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardService {

    private final DeviceMapper deviceMapper;
    private final DeviceMetricMapper metricMapper;
    private final AlertMapper alertMapper;
    private final AccessLogMapper accessLogMapper;

    public DashboardService(DeviceMapper deviceMapper,
                            DeviceMetricMapper metricMapper,
                            AlertMapper alertMapper,
                            AccessLogMapper accessLogMapper) {
        this.deviceMapper = deviceMapper;
        this.metricMapper = metricMapper;
        this.alertMapper = alertMapper;
        this.accessLogMapper = accessLogMapper;
    }

    // ========= 带 TTL 的缓存 =========
    private static class Cached<T> {
        final T value;
        final long expireAt;
        Cached(T value, long ttlMs) {
            this.value = value;
            this.expireAt = System.currentTimeMillis() + ttlMs;
        }
        boolean isExpired() { return System.currentTimeMillis() > expireAt; }
    }

    private final Map<String, Cached<?>> cache = new ConcurrentHashMap<>();

    private static final long TTL_FAST  = 2_000L;
    private static final long TTL_SLOW  = 5_000L;
    private static final long TTL_MEDIUM = 3_000L;

    private <T> T getOrCompute(String key, long ttlMs, java.util.function.Supplier<T> fn) {
        Cached<?> c = cache.get(key);
        if (c != null && !c.isExpired()) {
            hitCounter.incrementAndGet();
            @SuppressWarnings("unchecked")
            T casted = (T) c.value;
            return casted;
        }
        missCounter.incrementAndGet();
        long t0 = System.nanoTime();
        T v = fn.get();
        long cost = System.nanoTime() - t0;
        queryTimeSum.addAndGet(cost);
        queryCount.incrementAndGet();
        cache.put(key, new Cached<>(v, ttlMs));
        return v;
    }

    // ========= 性能指标 =========
    private final AtomicLong hitCounter = new AtomicLong();
    private final AtomicLong missCounter = new AtomicLong();
    private final AtomicLong queryTimeSum = new AtomicLong();
    private final AtomicLong queryCount = new AtomicLong();
    private final AtomicLong wsBroadcastCount = new AtomicLong();
    private volatile long lastTickTs = 0;
    private volatile long dbRowsRead = 0;

    // ========= API 方法 =========

    public Map<String, Object> summary() {
        return getOrCompute("summary", TTL_SLOW, () -> {
            List<Device> all = deviceMapper.selectList(null);
            dbRowsRead += all.size();
            long total = all.size();
            long online = all.stream().filter(d -> "online".equals(d.getStatus())).count();
            long fault = all.stream().filter(d -> "fault".equals(d.getStatus())).count();
            long offline = all.stream().filter(d -> "offline".equals(d.getStatus())).count();

            LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
            long todayAlerts = alertMapper.selectCount(new LambdaQueryWrapper<Alert>().ge(Alert::getCreatedAt, todayStart));
            long unacked = alertMapper.selectCount(new LambdaQueryWrapper<Alert>().eq(Alert::getAck, 0));

            List<AccessLog> todayLogs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                    .ge(AccessLog::getCreatedAt, todayStart));
            dbRowsRead += todayLogs.size();
            long todayFlow = todayLogs.stream().mapToLong(AccessLog::getBytes).sum();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("deviceTotal", total);
            map.put("deviceOnline", online);
            map.put("deviceFault", fault);
            map.put("deviceOffline", offline);
            map.put("todayAlerts", todayAlerts);
            map.put("unacked", unacked);
            map.put("todayFlowMB", new BigDecimal(todayFlow).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP));
            return map;
        });
    }

    public Map<String, Object> categoryPie() {
        return getOrCompute("category", TTL_MEDIUM, () -> {
            List<Device> all = deviceMapper.selectList(null);
            dbRowsRead += all.size();
            Map<String, Long> g = all.stream().collect(Collectors.groupingBy(Device::getCategory, Collectors.counting()));
            List<String> names = new ArrayList<>();
            List<Long> values = new ArrayList<>();
            String[] zh = {"摄像头", "门禁", "传感器", "网络设备", "服务器"};
            String[] en = {"camera", "door", "sensor", "router", "server"};
            for (int i = 0; i < en.length; i++) {
                names.add(zh[i]);
                values.add(g.getOrDefault(en[i], 0L));
            }
            return Map.of("names", names, "values", values);
        });
    }

    public Map<String, Object> alertTrend() {
        return getOrCompute("trend", TTL_SLOW, () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startWindow = now.minusHours(23).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endWindow = now;
            List<Alert> windowAlerts = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                    .ge(Alert::getCreatedAt, startWindow).lt(Alert::getCreatedAt, endWindow));
            dbRowsRead += windowAlerts.size();

            Map<Integer, Long> hourCount = windowAlerts.stream()
                    .collect(Collectors.groupingBy(a -> a.getCreatedAt().getHour(), Collectors.counting()));

            List<String> hours = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            for (int i = 23; i >= 0; i--) {
                LocalDateTime h = now.minusHours(i);
                hours.add(String.format("%02d:00", h.getHour()));
                counts.add(hourCount.getOrDefault(h.getHour(), 0L).intValue());
            }
            return Map.of("hours", hours, "counts", counts);
        });
    }

    public Map<String, Object> alertLevel() {
        return getOrCompute("level", TTL_MEDIUM, () -> {
            List<Alert> all = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                    .ge(Alert::getCreatedAt, LocalDateTime.now().minusDays(30)));
            dbRowsRead += all.size();
            long critical = all.stream().filter(a -> "critical".equals(a.getLevel())).count();
            long warning  = all.stream().filter(a -> "warning".equals(a.getLevel())).count();
            long info     = all.stream().filter(a -> "info".equals(a.getLevel())).count();
            return Map.of("names", List.of("紧急告警", "普通告警", "提示"),
                          "values", List.of(critical, warning, info));
        });
    }

    public List<Map<String, Object>> latestAlerts() {
        return getOrCompute("alerts", TTL_FAST, () -> {
            List<Alert> list = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                    .orderByDesc(Alert::getCreatedAt).last("LIMIT 20"));
            dbRowsRead += list.size();
            return list.stream().map(a -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("level", a.getLevel());
                m.put("levelText", switch (a.getLevel()) {
                    case "critical" -> "紧急";
                    case "warning"  -> "普通";
                    default         -> "提示";
                });
                m.put("deviceName", a.getDeviceName());
                m.put("message", a.getMessage());
                m.put("ack", a.getAck() == 1);
                m.put("createdAt", a.getCreatedAt().toString().replace("T", " "));
                return m;
            }).toList();
        });
    }

    public Map<String, Object> flowTargets() {
        return getOrCompute("flow", TTL_MEDIUM, () -> {
            List<AccessLog> logs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                    .ge(AccessLog::getCreatedAt, LocalDateTime.now().minusHours(6)));
            dbRowsRead += logs.size();
            Map<String, Long> g = logs.stream().collect(Collectors.groupingBy(AccessLog::getTarget,
                    Collectors.summingLong(AccessLog::getBytes)));
            List<Map.Entry<String, Long>> top = g.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .limit(10).toList();

            List<String> names = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();
            for (Map.Entry<String, Long> e : top) {
                names.add(e.getKey());
                values.add(new BigDecimal(e.getValue()).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP));
            }
            return Map.of("names", names, "values", values);
        });
    }

    public Map<String, Object> flowTrend() {
        return getOrCompute("flowTrend", TTL_SLOW, () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startWindow = now.minusHours(11).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endWindow = now;
            List<AccessLog> logs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                    .ge(AccessLog::getCreatedAt, startWindow).lt(AccessLog::getCreatedAt, endWindow));
            dbRowsRead += logs.size();

            Map<Integer, Map<String, Long>> byHour = logs.stream().collect(Collectors.groupingBy(
                    l -> l.getCreatedAt().getHour(),
                    Collectors.groupingBy(AccessLog::getSource, Collectors.summingLong(AccessLog::getBytes))
            ));

            List<String> labels = new ArrayList<>();
            List<BigDecimal> inbound = new ArrayList<>();
            List<BigDecimal> outbound = new ArrayList<>();
            for (int i = 11; i >= 0; i--) {
                LocalDateTime h = now.minusHours(i).withMinute(0).withSecond(0).withNano(0);
                labels.add(String.format("%02d:00", h.getHour()));
                Map<String, Long> src = byHour.getOrDefault(h.getHour(), Map.of());
                long in = src.getOrDefault("内网", 0L);
                long out = src.getOrDefault("外网", 0L);
                inbound.add(new BigDecimal(in).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP));
                outbound.add(new BigDecimal(out).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP));
            }
            return Map.of("labels", labels, "inbound", inbound, "outbound", outbound);
        });
    }

    public List<Map<String, Object>> deviceMetrics() {
        return getOrCompute("metrics", TTL_FAST, () -> {
            List<DeviceMetric> metrics = metricMapper.selectList(new LambdaQueryWrapper<DeviceMetric>()
                    .orderByDesc(DeviceMetric::getTs).last("LIMIT 60"));
            dbRowsRead += metrics.size();

            List<Long> devIds = metrics.stream().map(DeviceMetric::getDeviceId).distinct().toList();
            Map<Long, Device> devMap = devIds.isEmpty() ? Map.of() :
                    deviceMapper.selectBatchIds(devIds).stream().collect(Collectors.toMap(Device::getId, d -> d));
            dbRowsRead += devMap.size();

            Set<Long> seen = new HashSet<>();
            return metrics.stream().filter(m -> seen.add(m.getDeviceId())).map(m -> {
                Device dev = devMap.get(m.getDeviceId());
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("deviceId", m.getDeviceId());
                r.put("deviceName", dev != null ? dev.getName() : "未知");
                r.put("category", dev != null ? dev.getCategory() : "");
                r.put("cpu", m.getCpu());
                r.put("memory", m.getMemory());
                r.put("temperature", m.getTemperature());
                r.put("bandwidth", m.getBandwidth());
                r.put("online", m.getOnline() == 1);
                return r;
            }).toList();
        });
    }

    // ========= 健康检查 =========
    public Map<String, Object> health() {
        Map<String, Object> h = new LinkedHashMap<>();
        h.put("status", "UP");
        h.put("timestamp", System.currentTimeMillis());
        try {
            long c = deviceMapper.selectCount(null);
            h.put("db", "OK");
            h.put("deviceCount", c);
        } catch (Exception e) {
            h.put("db", "FAIL: " + e.getMessage());
            h.put("status", "DEGRADED");
        }
        h.put("wsSessions", com.monitor.dashboard.ws.DashboardWebSocket.size());
        return h;
    }

    // ========= 运行时性能指标 =========
    public Map<String, Object> runtimeMetrics() {
        long hits = hitCounter.get();
        long misses = missCounter.get();
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
        double avgQueryMs = queryCount.get() > 0
                ? queryTimeSum.get() / queryCount.get() / 1_000_000.0 : 0;

        Runtime rt = Runtime.getRuntime();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cacheHitRate", String.format("%.1f%%", hitRate));
        m.put("cacheHits", hits);
        m.put("cacheMisses", misses);
        m.put("avgQueryMs", String.format("%.2f", avgQueryMs));
        m.put("totalQueries", queryCount.get());
        m.put("wsBroadcasts", wsBroadcastCount.get());
        m.put("wsSessions", com.monitor.dashboard.ws.DashboardWebSocket.size());
        m.put("lastTickAgoMs", lastTickTs > 0 ? System.currentTimeMillis() - lastTickTs : -1);
        m.put("gcCountCleanup", cache.size());
        m.put("jvmUsedMB", (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024);
        m.put("jvmMaxMB", rt.maxMemory() / 1024 / 1024);
        return m;
    }

    // ========= 定时任务 =========
    @Scheduled(fixedDelay = 3000)
    public void tick() {
        lastTickTs = System.currentTimeMillis();

        // 预刷新所有缓存
        try { summary(); categoryPie(); alertTrend(); alertLevel(); latestAlerts(); flowTargets(); flowTrend(); deviceMetrics(); }
        catch (Exception e) { log.warn("tick prewarm error: {}", e.getMessage()); }

        List<Device> online = deviceMapper.selectList(new LambdaQueryWrapper<Device>().eq(Device::getStatus, "online"));
        for (Device d : online) {
            DeviceMetric m = new DeviceMetric();
            m.setDeviceId(d.getId());
            m.setCpu(RandomUtil.randomBigDecimal(new BigDecimal(5), new BigDecimal(95)));
            m.setMemory(RandomUtil.randomBigDecimal(new BigDecimal(10), new BigDecimal(85)));
            m.setTemperature(RandomUtil.randomBigDecimal(new BigDecimal(18), new BigDecimal(38)));
            m.setBandwidth(RandomUtil.randomBigDecimal(new BigDecimal(20), new BigDecimal(800)));
            m.setOnline(1);
            m.setTs(LocalDateTime.now());
            metricMapper.insert(m);
        }

        metricMapper.delete(new LambdaQueryWrapper<DeviceMetric>()
                .lt(DeviceMetric::getTs, LocalDateTime.now().minusHours(1)));

        if (RandomUtil.randomDouble() < 0.015 && !online.isEmpty()) {
            int idx = RandomUtil.randomInt(0, online.size());
            Device d = online.get(idx);
            Alert a = new Alert();
            a.setLevel(RandomUtil.randomEle(new String[]{"critical", "warning", "info", "warning", "info"}));
            a.setDeviceId(d.getId());
            a.setDeviceName(d.getName());
            a.setMessage(RandomUtil.randomEle(new String[]{
                    "CPU 使用率超过 85%", "响应延迟 > 300ms", "温度过高告警",
                    "网络丢包率异常", "磁盘 I/O 突发", "登录失败多次"
            }));
            a.setAck(0);
            alertMapper.insert(a);
            cache.remove("alerts");
        }

        String[] sources = {"内网", "内网", "内网", "外网", "外网"};
        String[] targets = {"Web服务器", "数据库", "OA服务器", "文件服务器", "邮件服务器", "防火墙"};
        String[] visitors = {"user-01", "user-12", "user-07", "user-21", "anonymous", "job-night"};
        AccessLog logObj = new AccessLog();
        logObj.setSource(RandomUtil.randomEle(sources));
        logObj.setTarget(RandomUtil.randomEle(targets));
        logObj.setBytes(RandomUtil.randomLong(500_000, 40_000_000));
        logObj.setVisitor(RandomUtil.randomEle(visitors));
        accessLogMapper.insert(logObj);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", summary());
        payload.put("alerts", latestAlerts().subList(0, Math.min(5, latestAlerts().size())));
        payload.put("metrics", deviceMetrics().subList(0, Math.min(3, deviceMetrics().size())));
        payload.put("tick", System.currentTimeMillis());
        wsBroadcastCount.incrementAndGet();
        com.monitor.dashboard.ws.DashboardWebSocket.broadcast(payload);
    }
}

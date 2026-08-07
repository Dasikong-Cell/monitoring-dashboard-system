package com.monitor.dashboard.service;

import cn.hutool.core.date.DateUtil;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DeviceMapper deviceMapper;
    private final DeviceMetricMapper metricMapper;
    private final AlertMapper alertMapper;
    private final AccessLogMapper accessLogMapper;
    private final RedisTemplate<String, Object> redis;

    private static final String CACHE_SUMMARY   = "dash:summary";
    private static final String CACHE_CATEGORY  = "dash:category";
    private static final String CACHE_TREND     = "dash:trend";
    private static final String CACHE_LEVEL     = "dash:level";
    private static final String CACHE_ALERTS    = "dash:alerts";
    private static final String CACHE_FLOW      = "dash:flow";

    // ========= 汇总卡片 =========
    public Map<String, Object> summary() {
        Object cached = redis.opsForValue().get(CACHE_SUMMARY);
        if (cached != null) return (Map<String, Object>) cached;

        List<Device> all = deviceMapper.selectList(null);
        long total = all.size();
        long online = all.stream().filter(d -> "online".equals(d.getStatus())).count();
        long fault = all.stream().filter(d -> "fault".equals(d.getStatus())).count();
        long offline = all.stream().filter(d -> "offline".equals(d.getStatus())).count();

        long todayAlerts = alertMapper.selectCount(new LambdaQueryWrapper<Alert>()
                .ge(Alert::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay()));
        long unacked = alertMapper.selectCount(new LambdaQueryWrapper<Alert>().eq(Alert::getAck, 0));

        long todayFlow = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                        .ge(AccessLog::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay()))
                .stream().mapToLong(AccessLog::getBytes).sum();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("deviceTotal", total);
        map.put("deviceOnline", online);
        map.put("deviceFault", fault);
        map.put("deviceOffline", offline);
        map.put("todayAlerts", todayAlerts);
        map.put("unacked", unacked);
        map.put("todayFlowMB", new BigDecimal(todayFlow).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP));

        redis.opsForValue().set(CACHE_SUMMARY, map, 5, java.util.concurrent.TimeUnit.SECONDS);
        return map;
    }

    // ========= 设备类别分布（饼图） =========
    public Map<String, Object> categoryPie() {
        Object cached = redis.opsForValue().get(CACHE_CATEGORY);
        if (cached != null) return (Map<String, Object>) cached;

        List<Device> all = deviceMapper.selectList(null);
        Map<String, Long> g = all.stream().collect(Collectors.groupingBy(Device::getCategory, Collectors.counting()));
        List<String> names = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        String[] zh = {"摄像头", "门禁", "传感器", "网络设备", "服务器"};
        String[] en = {"camera", "door", "sensor", "router", "server"};
        for (int i = 0; i < en.length; i++) {
            names.add(zh[i]);
            values.add(g.getOrDefault(en[i], 0L));
        }
        Map<String, Object> map = Map.of("names", names, "values", values);
        redis.opsForValue().set(CACHE_CATEGORY, map, 10, java.util.concurrent.TimeUnit.SECONDS);
        return map;
    }

    // ========= 近 24h 告警趋势（折线） =========
    public Map<String, Object> alertTrend() {
        Object cached = redis.opsForValue().get(CACHE_TREND);
        if (cached != null) return (Map<String, Object>) cached;

        LocalDateTime now = LocalDateTime.now();
        List<String> hours = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            LocalDateTime h = now.minusHours(i);
            hours.add(String.format("%02d:00", h.getHour()));
            LocalDateTime start = h.withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusHours(1);
            Integer c = alertMapper.selectCount(new LambdaQueryWrapper<Alert>()
                    .ge(Alert::getCreatedAt, start)
                    .lt(Alert::getCreatedAt, end)).intValue();
            counts.add(c);
        }
        Map<String, Object> map = Map.of("hours", hours, "counts", counts);
        redis.opsForValue().set(CACHE_TREND, map, 15, java.util.concurrent.TimeUnit.SECONDS);
        return map;
    }

    // ========= 告警级别分布（横向柱状） =========
    public Map<String, Object> alertLevel() {
        Object cached = redis.opsForValue().get(CACHE_LEVEL);
        if (cached != null) return (Map<String, Object>) cached;

        List<Alert> all = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                .ge(Alert::getCreatedAt, LocalDateTime.now().minusDays(30)));
        long critical = all.stream().filter(a -> "critical".equals(a.getLevel())).count();
        long warning  = all.stream().filter(a -> "warning".equals(a.getLevel())).count();
        long info     = all.stream().filter(a -> "info".equals(a.getLevel())).count();

        Map<String, Object> map = Map.of(
                "names", List.of("紧急告警", "普通告警", "提示"),
                "values", List.of(critical, warning, info)
        );
        redis.opsForValue().set(CACHE_LEVEL, map, 15, java.util.concurrent.TimeUnit.SECONDS);
        return map;
    }

    // ========= 最新告警列表（滚动） =========
    public List<Map<String, Object>> latestAlerts() {
        Object cached = redis.opsForValue().get(CACHE_ALERTS);
        if (cached != null) return (List<Map<String, Object>>) cached;

        List<Alert> list = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                .orderByDesc(Alert::getCreatedAt).last("LIMIT 20"));
        List<Map<String, Object>> result = list.stream().map(a -> {
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

        redis.opsForValue().set(CACHE_ALERTS, result, 3, java.util.concurrent.TimeUnit.SECONDS);
        return result;
    }

    // ========= 访问流量 TOP10 目标（横向柱状） =========
    public Map<String, Object> flowTargets() {
        Object cached = redis.opsForValue().get(CACHE_FLOW);
        if (cached != null) return (Map<String, Object>) cached;

        List<AccessLog> logs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                .ge(AccessLog::getCreatedAt, LocalDateTime.now().minusHours(6)));
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
        Map<String, Object> map = Map.of("names", names, "values", values);
        redis.opsForValue().set(CACHE_FLOW, map, 10, java.util.concurrent.TimeUnit.SECONDS);
        return map;
    }

    // ========= 设备实时指标（仪表 / 实时折线） =========
    public List<Map<String, Object>> deviceMetrics() {
        return metricMapper.selectList(new LambdaQueryWrapper<DeviceMetric>()
                        .orderByDesc(DeviceMetric::getTs).last("LIMIT 30"))
                .stream().map(m -> {
                    Device dev = deviceMapper.selectById(m.getDeviceId());
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
    }

    // ========= 近 12 小时流量趋势 =========
    public Map<String, Object> flowTrend() {
        LocalDateTime now = LocalDateTime.now();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> inbound = new ArrayList<>();
        List<BigDecimal> outbound = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime h = now.minusHours(i).withMinute(0).withSecond(0).withNano(0);
            labels.add(String.format("%02d:00", h.getHour()));
            LocalDateTime end = h.plusHours(1);
            List<AccessLog> hourLogs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                    .ge(AccessLog::getCreatedAt, h).lt(AccessLog::getCreatedAt, end));
            long in = hourLogs.stream().filter(l -> "内网".equals(l.getSource())).mapToLong(AccessLog::getBytes).sum();
            long out = hourLogs.stream().filter(l -> "外网".equals(l.getSource())).mapToLong(AccessLog::getBytes).sum();
            inbound.add(new BigDecimal(in).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP));
            outbound.add(new BigDecimal(out).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP));
        }
        return Map.of("labels", labels, "inbound", inbound, "outbound", outbound);
    }

    // ========= 定时：模拟实时数据推送 =========
    @Scheduled(fixedDelay = 3000)
    public void tick() {
        // 刷新所有缓存
        redis.delete(Arrays.asList(CACHE_SUMMARY, CACHE_CATEGORY, CACHE_TREND, CACHE_LEVEL, CACHE_ALERTS, CACHE_FLOW));
        summary(); categoryPie(); alertTrend(); alertLevel(); latestAlerts(); flowTargets();

        // 每 3 秒生成一批新的 device_metric 快照
        List<Device> online = deviceMapper.selectList(new LambdaQueryWrapper<Device>()
                .eq(Device::getStatus, "online"));
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

        // 清理老旧快照（只保留近 1 小时）
        metricMapper.delete(new LambdaQueryWrapper<DeviceMetric>()
                .lt(DeviceMetric::getTs, LocalDateTime.now().minusHours(1)));

        // 1% 概率产生一条新告警
        if (RandomUtil.randomDouble() < 0.015) {
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
        }

        // 随机写入 access_log
        String[] sources = {"内网", "内网", "内网", "外网", "外网"};
        String[] targets = {"Web服务器", "数据库", "OA服务器", "文件服务器", "邮件服务器", "防火墙"};
        String[] visitors = {"user-01", "user-12", "user-07", "user-21", "anonymous", "job-night"};
        AccessLog logObj = new AccessLog();
        logObj.setSource(RandomUtil.randomEle(sources));
        logObj.setTarget(RandomUtil.randomEle(targets));
        logObj.setBytes(RandomUtil.randomLong(500_000, 40_000_000));
        logObj.setVisitor(RandomUtil.randomEle(visitors));
        accessLogMapper.insert(logObj);

        // WebSocket 推一份精简数据
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", summary());
        payload.put("alerts", latestAlerts().subList(0, Math.min(5, latestAlerts().size())));
        payload.put("tick", System.currentTimeMillis());
        com.monitor.dashboard.ws.DashboardWebSocket.broadcast(payload);
    }
}

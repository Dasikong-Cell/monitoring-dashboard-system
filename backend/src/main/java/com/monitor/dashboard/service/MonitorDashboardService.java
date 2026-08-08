package com.monitor.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monitor.dashboard.entity.AccessLog;
import com.monitor.dashboard.entity.Alert;
import com.monitor.dashboard.entity.Device;
import com.monitor.dashboard.local.CacheManager;
import com.monitor.dashboard.local.RuntimeMetricsCollector;
import com.monitor.dashboard.mapper.AccessLogMapper;
import com.monitor.dashboard.mapper.AlertMapper;
import com.monitor.dashboard.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitorDashboardService {

    private final DeviceMapper deviceMapper;
    private final AlertMapper alertMapper;
    private final AccessLogMapper accessLogMapper;
    private final CacheManager cacheManager;

    public MonitorDashboardService(DeviceMapper deviceMapper,
                            AlertMapper alertMapper,
                            AccessLogMapper accessLogMapper,
                            CacheManager cacheManager) {
        this.deviceMapper = deviceMapper;
        this.alertMapper = alertMapper;
        this.accessLogMapper = accessLogMapper;
        this.cacheManager = cacheManager;
    }

    public Map<String, Object> summary() {
        return cacheManager.getOrCompute("summary", CacheManager.TTL_SLOW, () -> {
            List<Device> all = deviceMapper.selectList(null);
            RuntimeMetricsCollector.INSTANCE.recordDbRead(all.size());
            long total = all.size();
            long online = all.stream().filter(d -> "online".equals(d.getStatus())).count();
            long fault = all.stream().filter(d -> "fault".equals(d.getStatus())).count();
            long offline = all.stream().filter(d -> "offline".equals(d.getStatus())).count();

            LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
            long todayAlerts = alertMapper.selectCount(new LambdaQueryWrapper<Alert>().ge(Alert::getCreatedAt, todayStart));
            long unacked = alertMapper.selectCount(new LambdaQueryWrapper<Alert>().eq(Alert::getAck, 0));

            List<AccessLog> todayLogs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                    .ge(AccessLog::getCreatedAt, todayStart));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(todayLogs.size());
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
        return cacheManager.getOrCompute("category", CacheManager.TTL_MEDIUM, () -> {
            List<Device> all = deviceMapper.selectList(null);
            RuntimeMetricsCollector.INSTANCE.recordDbRead(all.size());
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
        return cacheManager.getOrCompute("trend", CacheManager.TTL_SLOW, () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startWindow = now.minusHours(23).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endWindow = now;
            List<Alert> windowAlerts = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                    .ge(Alert::getCreatedAt, startWindow).lt(Alert::getCreatedAt, endWindow));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(windowAlerts.size());

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
        return cacheManager.getOrCompute("level", CacheManager.TTL_MEDIUM, () -> {
            List<Alert> all = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                    .ge(Alert::getCreatedAt, LocalDateTime.now().minusDays(30)));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(all.size());
            long critical = all.stream().filter(a -> "critical".equals(a.getLevel())).count();
            long warning  = all.stream().filter(a -> "warning".equals(a.getLevel())).count();
            long info     = all.stream().filter(a -> "info".equals(a.getLevel())).count();
            return Map.of("names", List.of("紧急告警", "普通告警", "提示"),
                          "values", List.of(critical, warning, info));
        });
    }

    public Map<String, Object> flowTargets() {
        return cacheManager.getOrCompute("flow", CacheManager.TTL_MEDIUM, () -> {
            List<AccessLog> logs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                    .ge(AccessLog::getCreatedAt, LocalDateTime.now().minusHours(6)));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(logs.size());
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
        return cacheManager.getOrCompute("flowTrend", CacheManager.TTL_SLOW, () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startWindow = now.minusHours(11).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endWindow = now;
            List<AccessLog> logs = accessLogMapper.selectList(new LambdaQueryWrapper<AccessLog>()
                    .ge(AccessLog::getCreatedAt, startWindow).lt(AccessLog::getCreatedAt, endWindow));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(logs.size());

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
}

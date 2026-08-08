package com.monitor.dashboard.service;

import com.monitor.dashboard.local.CacheManager;
import com.monitor.dashboard.local.RuntimeMetricsCollector;
import com.monitor.dashboard.mapper.DeviceMapper;
import com.monitor.dashboard.ws.DashboardWebSocket;
import org.springframework.stereotype.Service;

import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MonitorSystemService {

    private final DeviceMapper deviceMapper;
    private final CacheManager cacheManager;

    public MonitorSystemService(DeviceMapper deviceMapper, CacheManager cacheManager) {
        this.deviceMapper = deviceMapper;
        this.cacheManager = cacheManager;
    }

    public Map<String, Object> health() {
        Map<String, Object> h = new LinkedHashMap<>();
        h.put("status", "UP");
        h.put("timestamp", System.currentTimeMillis());
        try {
            long c = deviceMapper.selectCount(null);
            RuntimeMetricsCollector.INSTANCE.recordDbRead(1);
            h.put("db", "OK");
            h.put("deviceCount", c);
        } catch (Exception e) {
            h.put("db", "FAIL: " + e.getMessage());
            h.put("status", "DEGRADED");
        }
        h.put("wsSessions", DashboardWebSocket.size());
        return h;
    }

    public Map<String, Object> runtimeMetrics() {
        RuntimeMetricsCollector rm = RuntimeMetricsCollector.INSTANCE;
        double hitRate = rm.getCacheHitRate();
        double avgQueryMs = rm.getAvgQueryMs();

        Runtime rt = Runtime.getRuntime();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cacheHitRate", String.format("%.1f%%", hitRate));
        m.put("cacheHits", rm.getCacheHits());
        m.put("cacheMisses", rm.getCacheMisses());
        m.put("avgQueryMs", String.format("%.2f", avgQueryMs));
        m.put("totalQueries", rm.getTotalQueries());
        m.put("wsBroadcasts", rm.getWsBroadcastCount());
        m.put("wsSessions", DashboardWebSocket.size());
        m.put("lastTickAgoMs", rm.getLastTickTs() > 0 ? System.currentTimeMillis() - rm.getLastTickTs() : -1);
        m.put("gcCountCleanup", cacheManager.size());
        m.put("jvmUsedMB", (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024);
        m.put("jvmMaxMB", rt.maxMemory() / 1024 / 1024);
        return m;
    }
}

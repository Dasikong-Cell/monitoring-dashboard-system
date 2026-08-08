package com.monitor.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monitor.dashboard.entity.Device;
import com.monitor.dashboard.entity.DeviceMetric;
import com.monitor.dashboard.local.CacheManager;
import com.monitor.dashboard.local.RuntimeMetricsCollector;
import com.monitor.dashboard.mapper.DeviceMapper;
import com.monitor.dashboard.mapper.DeviceMetricMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final DeviceMetricMapper metricMapper;
    private final CacheManager cacheManager;

    public DeviceService(DeviceMapper deviceMapper,
                         DeviceMetricMapper metricMapper,
                         CacheManager cacheManager) {
        this.deviceMapper = deviceMapper;
        this.metricMapper = metricMapper;
        this.cacheManager = cacheManager;
    }

    public List<Map<String, Object>> deviceMetrics() {
        return cacheManager.getOrCompute("metrics", CacheManager.TTL_FAST, () -> {
            List<DeviceMetric> metrics = metricMapper.selectList(new LambdaQueryWrapper<DeviceMetric>()
                    .orderByDesc(DeviceMetric::getTs).last("LIMIT 60"));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(metrics.size());

            List<Long> devIds = metrics.stream().map(DeviceMetric::getDeviceId).distinct().toList();
            Map<Long, Device> devMap = devIds.isEmpty() ? Map.of() :
                    deviceMapper.selectBatchIds(devIds).stream().collect(Collectors.toMap(Device::getId, d -> d));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(devMap.size());

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

    public List<Device> listDevices() {
        List<Device> list = deviceMapper.selectList(null);
        RuntimeMetricsCollector.INSTANCE.recordDbRead(list.size());
        return list;
    }

    public Device getDeviceById(Long id) {
        Device d = deviceMapper.selectById(id);
        RuntimeMetricsCollector.INSTANCE.recordDbRead(d != null ? 1 : 0);
        return d;
    }
}

package com.monitor.dashboard.service;

import com.monitor.dashboard.entity.Device;
import java.util.List;
import java.util.Map;

public interface MonitorDeviceService {
    List<Map<String, Object>> deviceMetrics();
    List<Device> listDevices();
    Device getDeviceById(Long id);
}

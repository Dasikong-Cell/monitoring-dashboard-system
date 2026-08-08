package com.monitor.dashboard.service;

import java.util.Map;

public interface MonitorSystemService {
    Map<String, Object> health();
    Map<String, Object> runtimeMetrics();
}

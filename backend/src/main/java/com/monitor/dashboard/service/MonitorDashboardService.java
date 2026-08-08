package com.monitor.dashboard.service;

import java.util.Map;

public interface MonitorDashboardService {
    Map<String, Object> summary();
    Map<String, Object> categoryPie();
    Map<String, Object> alertTrend();
    Map<String, Object> alertLevel();
    Map<String, Object> flowTargets();
    Map<String, Object> flowTrend();
}

package com.monitor.dashboard.service;

import java.util.List;
import java.util.Map;

public interface MonitorAlertService {
    List<Map<String, Object>> latestAlerts();
    void ackAlert(Long id);
}

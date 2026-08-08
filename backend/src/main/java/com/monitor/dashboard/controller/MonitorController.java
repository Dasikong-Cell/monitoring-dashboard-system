package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.service.MonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        return R.ok(monitorService.health());
    }

    @GetMapping("/runtime")
    public R<Map<String, Object>> runtime() {
        return R.ok(monitorService.runtimeMetrics());
    }
}

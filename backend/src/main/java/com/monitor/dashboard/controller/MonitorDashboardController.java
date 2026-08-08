package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.service.MonitorDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class MonitorDashboardController {

    private final MonitorDashboardService svc;

    public MonitorDashboardController(MonitorDashboardService svc) {
        this.svc = svc;
    }

    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        return R.ok(svc.summary());
    }

    @GetMapping("/category")
    public R<Map<String, Object>> category() {
        return R.ok(svc.categoryPie());
    }

    @GetMapping("/alert-trend")
    public R<Map<String, Object>> alertTrend() {
        return R.ok(svc.alertTrend());
    }

    @GetMapping("/alert-level")
    public R<Map<String, Object>> alertLevel() {
        return R.ok(svc.alertLevel());
    }

    @GetMapping("/flow")
    public R<Map<String, Object>> flow() {
        return R.ok(svc.flowTargets());
    }

    @GetMapping("/flow-trend")
    public R<Map<String, Object>> flowTrend() {
        return R.ok(svc.flowTrend());
    }
}

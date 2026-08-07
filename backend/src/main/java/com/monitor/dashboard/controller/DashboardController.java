package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService svc;

    public DashboardController(DashboardService svc) {
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

    @GetMapping("/alerts")
    public R<List<Map<String, Object>>> alerts() {
        return R.ok(svc.latestAlerts());
    }

    @GetMapping("/flow")
    public R<Map<String, Object>> flow() {
        return R.ok(svc.flowTargets());
    }

    @GetMapping("/flow-trend")
    public R<Map<String, Object>> flowTrend() {
        return R.ok(svc.flowTrend());
    }

    @GetMapping("/metrics")
    public R<List<Map<String, Object>>> metrics() {
        return R.ok(svc.deviceMetrics());
    }

    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        return R.ok(svc.health());
    }

    @GetMapping("/runtime")
    public R<Map<String, Object>> runtime() {
        return R.ok(svc.runtimeMetrics());
    }
}

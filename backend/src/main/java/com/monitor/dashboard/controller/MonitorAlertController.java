package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.service.MonitorAlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class MonitorAlertController {

    private final MonitorAlertService alertService;

    public MonitorAlertController(MonitorAlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public R<List<Map<String, Object>>> latest() {
        return R.ok(alertService.latestAlerts());
    }

    @PostMapping("/{id}/ack")
    public R<Void> ack(@PathVariable Long id) {
        alertService.ackAlert(id);
        return R.ok();
    }
}

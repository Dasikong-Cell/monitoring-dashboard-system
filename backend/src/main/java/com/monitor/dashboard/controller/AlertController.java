package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
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

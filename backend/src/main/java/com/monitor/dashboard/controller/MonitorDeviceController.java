package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.entity.Device;
import com.monitor.dashboard.service.MonitorDeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/devices")
public class MonitorDeviceController {

    private final MonitorDeviceService deviceService;

    public MonitorDeviceController(MonitorDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public R<List<Device>> list() {
        return R.ok(deviceService.listDevices());
    }

    @GetMapping("/{id}")
    public R<Device> getById(@PathVariable Long id) {
        return R.ok(deviceService.getDeviceById(id));
    }

    @GetMapping("/metrics")
    public R<List<Map<String, Object>>> metrics() {
        return R.ok(deviceService.deviceMetrics());
    }
}

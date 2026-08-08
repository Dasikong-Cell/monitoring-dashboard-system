package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.service.MonitorRealtimeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/realtime")
public class MonitorRealtimeController {

    private final MonitorRealtimeService realtimeDataService;

    public MonitorRealtimeController(MonitorRealtimeService realtimeDataService) {
        this.realtimeDataService = realtimeDataService;
    }

    @PostMapping("/tick")
    public R<Void> tick() throws Exception {
        Method m = MonitorRealtimeService.class.getDeclaredMethod("tick");
        m.setAccessible(true);
        m.invoke(realtimeDataService);
        return R.ok();
    }
}

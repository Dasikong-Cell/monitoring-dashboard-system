package com.monitor.dashboard.controller;

import com.monitor.dashboard.common.R;
import com.monitor.dashboard.service.RealtimeDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/realtime")
public class RealtimeController {

    private final RealtimeDataService realtimeDataService;

    public RealtimeController(RealtimeDataService realtimeDataService) {
        this.realtimeDataService = realtimeDataService;
    }

    @PostMapping("/tick")
    public R<Void> tick() throws Exception {
        Method m = RealtimeDataService.class.getDeclaredMethod("tick");
        m.setAccessible(true);
        m.invoke(realtimeDataService);
        return R.ok();
    }
}

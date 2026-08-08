package com.monitor.dashboard.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monitor.dashboard.entity.AccessLog;
import com.monitor.dashboard.entity.Alert;
import com.monitor.dashboard.entity.Device;
import com.monitor.dashboard.entity.DeviceMetric;
import com.monitor.dashboard.local.CacheManager;
import com.monitor.dashboard.local.RuntimeMetricsCollector;
import com.monitor.dashboard.mapper.AccessLogMapper;
import com.monitor.dashboard.mapper.AlertMapper;
import com.monitor.dashboard.mapper.DeviceMapper;
import com.monitor.dashboard.mapper.DeviceMetricMapper;
import com.monitor.dashboard.ws.DashboardWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RealtimeDataService {

    private final DeviceMapper deviceMapper;
    private final DeviceMetricMapper metricMapper;
    private final AlertMapper alertMapper;
    private final AccessLogMapper accessLogMapper;
    private final CacheManager cacheManager;
    private final DashboardService dashboardService;
    private final AlertService alertService;
    private final DeviceService deviceService;

    public RealtimeDataService(DeviceMapper deviceMapper,
                               DeviceMetricMapper metricMapper,
                               AlertMapper alertMapper,
                               AccessLogMapper accessLogMapper,
                               CacheManager cacheManager,
                               DashboardService dashboardService,
                               AlertService alertService,
                               DeviceService deviceService) {
        this.deviceMapper = deviceMapper;
        this.metricMapper = metricMapper;
        this.alertMapper = alertMapper;
        this.accessLogMapper = accessLogMapper;
        this.cacheManager = cacheManager;
        this.dashboardService = dashboardService;
        this.alertService = alertService;
        this.deviceService = deviceService;
    }

    @Scheduled(fixedDelay = 3000)
    public void tick() {
        RuntimeMetricsCollector.INSTANCE.tickNow();

        try {
            dashboardService.summary();
            dashboardService.categoryPie();
            dashboardService.alertTrend();
            dashboardService.alertLevel();
            alertService.latestAlerts();
            dashboardService.flowTargets();
            dashboardService.flowTrend();
            deviceService.deviceMetrics();
        } catch (Exception e) {
            log.warn("tick prewarm error: {}", e.getMessage());
        }

        List<Device> online = deviceMapper.selectList(new LambdaQueryWrapper<Device>().eq(Device::getStatus, "online"));
        for (Device d : online) {
            DeviceMetric m = new DeviceMetric();
            m.setDeviceId(d.getId());
            m.setCpu(RandomUtil.randomBigDecimal(new BigDecimal(5), new BigDecimal(95)));
            m.setMemory(RandomUtil.randomBigDecimal(new BigDecimal(10), new BigDecimal(85)));
            m.setTemperature(RandomUtil.randomBigDecimal(new BigDecimal(18), new BigDecimal(38)));
            m.setBandwidth(RandomUtil.randomBigDecimal(new BigDecimal(20), new BigDecimal(800)));
            m.setOnline(1);
            m.setTs(LocalDateTime.now());
            metricMapper.insert(m);
        }

        metricMapper.delete(new LambdaQueryWrapper<DeviceMetric>()
                .lt(DeviceMetric::getTs, LocalDateTime.now().minusHours(1)));

        if (RandomUtil.randomDouble() < 0.015 && !online.isEmpty()) {
            int idx = RandomUtil.randomInt(0, online.size());
            Device d = online.get(idx);
            Alert a = new Alert();
            a.setLevel(RandomUtil.randomEle(new String[]{"critical", "warning", "info", "warning", "info"}));
            a.setDeviceId(d.getId());
            a.setDeviceName(d.getName());
            a.setMessage(RandomUtil.randomEle(new String[]{
                    "CPU 使用率超过 85%", "响应延迟 > 300ms", "温度过高告警",
                    "网络丢包率异常", "磁盘 I/O 突发", "登录失败多次"
            }));
            a.setAck(0);
            alertMapper.insert(a);
            cacheManager.invalidate("alerts");
        }

        String[] sources = {"内网", "内网", "内网", "外网", "外网"};
        String[] targets = {"Web服务器", "数据库", "OA服务器", "文件服务器", "邮件服务器", "防火墙"};
        String[] visitors = {"user-01", "user-12", "user-07", "user-21", "anonymous", "job-night"};
        AccessLog logObj = new AccessLog();
        logObj.setSource(RandomUtil.randomEle(sources));
        logObj.setTarget(RandomUtil.randomEle(targets));
        logObj.setBytes(RandomUtil.randomLong(500_000, 40_000_000));
        logObj.setVisitor(RandomUtil.randomEle(visitors));
        accessLogMapper.insert(logObj);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", dashboardService.summary());
        var alertsList = alertService.latestAlerts();
        payload.put("alerts", alertsList.subList(0, Math.min(5, alertsList.size())));
        var metricsList = deviceService.deviceMetrics();
        payload.put("metrics", metricsList.subList(0, Math.min(3, metricsList.size())));
        payload.put("tick", System.currentTimeMillis());
        RuntimeMetricsCollector.INSTANCE.recordWsBroadcast();
        DashboardWebSocket.broadcast(payload);
    }
}

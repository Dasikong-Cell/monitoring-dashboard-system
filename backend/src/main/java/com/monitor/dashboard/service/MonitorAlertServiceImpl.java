package com.monitor.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monitor.dashboard.common.BusinessException;
import com.monitor.dashboard.common.ErrorCode;
import com.monitor.dashboard.entity.Alert;
import com.monitor.dashboard.local.CacheManager;
import com.monitor.dashboard.local.RuntimeMetricsCollector;
import com.monitor.dashboard.mapper.AlertMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitorAlertServiceImpl implements MonitorAlertService {

    private final AlertMapper alertMapper;
    private final CacheManager cacheManager;

    public MonitorAlertServiceImpl(AlertMapper alertMapper, CacheManager cacheManager) {
        this.alertMapper = alertMapper;
        this.cacheManager = cacheManager;
    }

    public List<Map<String, Object>> latestAlerts() {
        return cacheManager.getOrCompute("alerts", CacheManager.TTL_FAST, () -> {
            List<Alert> list = alertMapper.selectList(new LambdaQueryWrapper<Alert>()
                    .orderByDesc(Alert::getCreatedAt).last("LIMIT 20"));
            RuntimeMetricsCollector.INSTANCE.recordDbRead(list.size());
            return list.stream().map(a -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("level", a.getLevel());
                m.put("levelText", switch (a.getLevel()) {
                    case "critical" -> "紧急";
                    case "warning"  -> "普通";
                    default         -> "提示";
                });
                m.put("deviceName", a.getDeviceName());
                m.put("message", a.getMessage());
                m.put("ack", a.getAck() == 1);
                m.put("createdAt", a.getCreatedAt().toString().replace("T", " "));
                return m;
            }).toList();
        });
    }

    public void ackAlert(Long id) {
        Alert a = alertMapper.selectById(id);
        RuntimeMetricsCollector.INSTANCE.recordDbRead(a != null ? 1 : 0);
        if (a == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        a.setAck(1);
        alertMapper.updateById(a);
        cacheManager.invalidate("alerts");
    }
}

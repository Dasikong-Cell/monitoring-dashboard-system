package com.monitor.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceMetricVO {
    private Long deviceId;
    private String deviceName;
    private String category;
    private BigDecimal cpu;
    private BigDecimal memory;
    private BigDecimal temperature;
    private BigDecimal bandwidth;
    private Boolean online;
}

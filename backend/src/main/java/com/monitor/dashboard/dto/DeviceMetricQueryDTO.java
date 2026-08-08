package com.monitor.dashboard.dto;

import lombok.Data;

@Data
public class DeviceMetricQueryDTO {
    private Long deviceId;
    private Integer limit;
}

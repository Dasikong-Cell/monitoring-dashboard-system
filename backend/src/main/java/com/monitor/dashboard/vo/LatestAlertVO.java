package com.monitor.dashboard.vo;

import lombok.Data;

@Data
public class LatestAlertVO {
    private Long id;
    private String level;
    private String levelText;
    private String deviceName;
    private String message;
    private Boolean ack;
    private String createdAt;
}

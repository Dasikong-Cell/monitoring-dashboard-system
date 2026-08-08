package com.monitor.dashboard.dto;

import lombok.Data;

@Data
public class AlertQueryDTO {
    private Integer level;
    private Integer ack;
    private Integer page;
    private Integer size;
}

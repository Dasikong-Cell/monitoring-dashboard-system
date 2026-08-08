package com.monitor.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SummaryVO {
    private long deviceTotal;
    private long deviceOnline;
    private long deviceFault;
    private long deviceOffline;
    private long todayAlerts;
    private long unacked;
    private BigDecimal todayFlowMB;
}

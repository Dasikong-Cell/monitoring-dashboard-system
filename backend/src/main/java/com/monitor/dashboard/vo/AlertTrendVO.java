package com.monitor.dashboard.vo;

import lombok.Data;

import java.util.List;

@Data
public class AlertTrendVO {
    private List<String> hours;
    private List<Integer> counts;
}

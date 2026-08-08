package com.monitor.dashboard.vo;

import lombok.Data;

import java.util.List;

@Data
public class AlertLevelVO {
    private List<String> names;
    private List<Long> values;
}

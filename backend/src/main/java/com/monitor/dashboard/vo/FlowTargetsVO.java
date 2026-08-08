package com.monitor.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FlowTargetsVO {
    private List<String> names;
    private List<BigDecimal> values;
}

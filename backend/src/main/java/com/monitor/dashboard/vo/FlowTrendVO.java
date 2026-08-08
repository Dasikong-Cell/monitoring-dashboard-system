package com.monitor.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FlowTrendVO {
    private List<String> labels;
    private List<BigDecimal> inbound;
    private List<BigDecimal> outbound;
}

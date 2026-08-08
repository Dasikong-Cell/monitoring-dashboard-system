package com.monitor.dashboard.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryPieVO {
    private List<String> names;
    private List<Long> values;
}

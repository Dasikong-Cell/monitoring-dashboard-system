package com.monitor.dashboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_metric")
public class DeviceMetric {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private BigDecimal cpu;
    private BigDecimal memory;
    private BigDecimal temperature;
    private BigDecimal bandwidth;
    private Integer online;
    private LocalDateTime ts;
}

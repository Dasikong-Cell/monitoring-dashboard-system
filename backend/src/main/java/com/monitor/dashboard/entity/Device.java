package com.monitor.dashboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String category;
    private String location;
    private String ip;
    private String status;
    @TableField("last_online_at")
    private LocalDateTime lastOnlineAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

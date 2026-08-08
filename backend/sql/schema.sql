-- 设备表
CREATE TABLE IF NOT EXISTS device (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL COMMENT '设备名称',
    category   VARCHAR(50)  NOT NULL COMMENT '分类: camera/door/sensor/router/server',
    ip         VARCHAR(50)  COMMENT 'IP地址',
    location   VARCHAR(200) COMMENT '位置',
    status     VARCHAR(20)  NOT NULL DEFAULT 'online' COMMENT 'online/offline/fault',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 设备指标表
CREATE TABLE IF NOT EXISTS device_metric (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id   BIGINT         NOT NULL COMMENT '设备ID',
    cpu         DECIMAL(5,2)   COMMENT 'CPU使用率%',
    memory      DECIMAL(5,2)   COMMENT '内存使用率%',
    temperature DECIMAL(5,2)   COMMENT '温度℃',
    bandwidth   DECIMAL(10,2)  COMMENT '带宽Mbps',
    online      TINYINT(1)     NOT NULL DEFAULT 1,
    ts          DATETIME       NOT NULL COMMENT '采集时间',
    INDEX idx_device_ts (device_id, ts)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备指标表';

-- 告警表
CREATE TABLE IF NOT EXISTS alert (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    level      VARCHAR(20)  NOT NULL COMMENT 'critical/warning/info',
    device_id  BIGINT       NOT NULL,
    device_name VARCHAR(100),
    message    VARCHAR(500),
    ack        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0未处理/1已处理',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_level_time (level, created_at),
    INDEX idx_ack (ack)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警表';

-- 访问日志表
CREATE TABLE IF NOT EXISTS access_log (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    source     VARCHAR(50),
    target     VARCHAR(100),
    bytes      BIGINT       NOT NULL DEFAULT 0,
    visitor    VARCHAR(100),
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问日志表';

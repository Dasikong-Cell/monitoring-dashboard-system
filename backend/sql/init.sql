-- ============================================================
-- 智慧园区设备运维监控大屏 —— 数据库初始化脚本
-- MySQL 8.0+
-- ============================================================
DROP DATABASE IF EXISTS monitor_dashboard;
CREATE DATABASE monitor_dashboard DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE monitor_dashboard;

-- 设备表
CREATE TABLE device (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(64)  NOT NULL COMMENT '设备名称',
    code            VARCHAR(32)  NOT NULL UNIQUE COMMENT '设备编号',
    category        VARCHAR(32)  NOT NULL COMMENT '类别: camera门禁 sensor router server',
    location        VARCHAR(64)  NOT NULL COMMENT '安装位置',
    ip              VARCHAR(64)  COMMENT 'IP 地址',
    status          VARCHAR(16)  NOT NULL DEFAULT 'offline' COMMENT 'online offline fault maintenance',
    last_online_at  DATETIME,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB COMMENT='设备表';

-- 设备实时指标快照（每 30s 一条）
CREATE TABLE device_metric (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id   BIGINT       NOT NULL,
    cpu         DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'CPU %',
    memory      DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '内存 %',
    temperature DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '温度 ℃',
    bandwidth   DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '带宽 Mbps',
    online      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '在线状态',
    ts          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_ts (device_id, ts)
) ENGINE=InnoDB COMMENT='设备实时指标快照';

-- 告警表
CREATE TABLE alert (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    level       VARCHAR(16)  NOT NULL COMMENT 'critical warning info',
    device_id   BIGINT       NOT NULL,
    device_name VARCHAR(64)  COMMENT '冗余设备名',
    message     VARCHAR(255) NOT NULL COMMENT '告警描述',
    ack         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '已处理',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_level (level),
    INDEX idx_created (created_at)
) ENGINE=InnoDB COMMENT='告警表';

-- 访问流量日志
CREATE TABLE access_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    source      VARCHAR(64)  COMMENT '来源区域',
    target      VARCHAR(64)  COMMENT '目标设备/区域',
    bytes       BIGINT       NOT NULL DEFAULT 0 COMMENT '流量字节',
    visitor     VARCHAR(64)  COMMENT '访问者标识',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created (created_at)
) ENGINE=InnoDB COMMENT='访问流量日志';

-- 初始化 30 台设备
INSERT INTO device (name, code, category, location, ip, status, last_online_at) VALUES
('园区东门摄像头',   'CAM-001', 'camera', '东门入口',   '10.0.1.11', 'online',   NOW()),
('园区西门摄像头',   'CAM-002', 'camera', '西门入口',   '10.0.1.12', 'online',   NOW()),
('1号楼大厅摄像头',  'CAM-003', 'camera', '1号楼大厅',  '10.0.1.13', 'online',   NOW()),
('2号楼大厅摄像头',  'CAM-004', 'camera', '2号楼大厅',  '10.0.1.14', 'fault',    DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
('地下车库摄像头',   'CAM-005', 'camera', '地下车库',   '10.0.1.15', 'online',   NOW()),
('食堂入口摄像头',   'CAM-006', 'camera', '食堂',       '10.0.1.16', 'offline',  DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('机房摄像头',      'CAM-007', 'camera', '机房',       '10.0.1.17', 'online',   NOW()),
('北门门禁',        'ACS-001', 'door',   '北门',       '10.0.2.11', 'online',   NOW()),
('南门门禁',        'ACS-002', 'door',   '南门',       '10.0.2.12', 'online',   NOW()),
('1号楼门禁',       'ACS-003', 'door',   '1号楼',      '10.0.2.13', 'maintenance', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('2号楼门禁',       'ACS-004', 'door',   '2号楼',      '10.0.2.14', 'online',   NOW()),
('烟感传感器1',      'SEN-001', 'sensor', '机房A区',    '10.0.3.11', 'online',   NOW()),
('烟感传感器2',      'SEN-002', 'sensor', '机房B区',    '10.0.3.12', 'online',   NOW()),
('温湿度传感器',     'SEN-003', 'sensor', '机房中央',   '10.0.3.13', 'online',   NOW()),
('水浸传感器',      'SEN-004', 'sensor', '机房下层',   '10.0.3.14', 'online',   NOW()),
('空调传感器',      'SEN-005', 'sensor', '机房空调',   '10.0.3.15', 'fault',    DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
('核心交换机',      'NET-001', 'router', '机房核心',   '10.0.4.11', 'online',   NOW()),
('汇聚交换机1',      'NET-002', 'router', '1号楼机房',  '10.0.4.12', 'online',   NOW()),
('汇聚交换机2',      'NET-003', 'router', '2号楼机房',  '10.0.4.13', 'online',   NOW()),
('防火墙',          'NET-004', 'router', '边界',       '10.0.4.14', 'online',   NOW()),
('无线控制器',      'NET-005', 'router', '机房',       '10.0.4.15', 'online',   NOW()),
('Web服务器',       'SRV-001', 'server', '机房服务器1','10.0.5.11', 'online',   NOW()),
('数据库服务器',     'SRV-002', 'server', '机房服务器2','10.0.5.12', 'online',   NOW()),
('文件服务器',      'SRV-003', 'server', '机房服务器3','10.0.5.13', 'online',   NOW()),
('备份服务器',      'SRV-004', 'server', '机房服务器4','10.0.5.14', 'offline',  DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('堡垒机',          'SRV-005', 'server', '边界',       '10.0.5.15', 'online',   NOW()),
('OA服务器',        'SRV-006', 'server', '机房',       '10.0.5.16', 'online',   NOW()),
('邮件服务器',      'SRV-007', 'server', '机房',       '10.0.5.17', 'online',   NOW()),
('监控存储服务器',   'SRV-008', 'server', '机房',       '10.0.5.18', 'online',   NOW()),
('DNS服务器',       'SRV-009', 'server', '边界',       '10.0.5.19', 'online',   NOW());

-- 初始实时指标（为所有在线设备生成一条）
INSERT INTO device_metric (device_id, cpu, memory, temperature, bandwidth, online, ts)
SELECT id,
       ROUND(RAND() * 80 + 5, 2),
       ROUND(RAND() * 70 + 10, 2),
       ROUND(RAND() * 25 + 20, 2),
       ROUND(RAND() * 500 + 50, 2),
       IF(status = 'online', 1, 0),
       NOW()
FROM device;

-- 初始告警数据
INSERT INTO alert (level, device_id, device_name, message, ack, created_at) VALUES
('critical', 4,  '2号楼大厅摄像头',  '视频信号中断',               0, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
('warning',  16, '空调传感器',       '温度超过 32℃',              0, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
('warning',  30, 'DNS服务器',        '响应延迟 280ms',             1, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
('info',     9,  '北门门禁',        '连续识别失败 5 次',          0, DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
('critical', 6,  '食堂入口摄像头',    '设备离线超过 2 小时',        0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('warning',  10, '1号楼门禁',       '维护中，建议绕行南门',        0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('info',     12, '烟感传感器1',      '自检正常',                  1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('warning',  24, '备份服务器',       '磁盘空间 92%',              0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 初始访问流量
INSERT INTO access_log (source, target, bytes, visitor, created_at) VALUES
('外网', '防火墙',    5242880,   'anonymous', DATE_SUB(NOW(), INTERVAL 30 SECOND)),
('内网', 'Web服务器', 10485760,  'user-01',   DATE_SUB(NOW(), INTERVAL 45 SECOND)),
('内网', '数据库',    2097152,   'job-night', DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('外网', '邮件服务器', 7340032,   'user-07',   DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
('内网', 'OA服务器',   15728640,  'user-12',   DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
('外网', 'Web服务器', 4194304,   'anonymous', DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
('内网', '文件服务器', 6291456,   'user-21',   DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
('内网', '备份服务器', 31457280,  'backup-job',DATE_SUB(NOW(), INTERVAL 6 MINUTE)),
('外网', '防火墙',    8388608,   'anonymous', DATE_SUB(NOW(), INTERVAL 7 MINUTE)),
('内网', '数据库',    10485760,  'job-etl',   DATE_SUB(NOW(), INTERVAL 8 MINUTE));

# API 文档

## Dashboard 概览
- `GET /dashboard/summary` - 核心指标概览
- `GET /dashboard/category` - 设备分类饼图
- `GET /dashboard/alert-trend` - 24小时告警趋势
- `GET /dashboard/alert-level` - 告警级别分布
- `GET /dashboard/flow` - 流量 TOP10 目标
- `GET /dashboard/flow-trend` - 12小时流量趋势

## 设备管理
- `GET /devices/` - 设备列表
- `GET /devices/{id}` - 设备详情
- `GET /devices/metrics` - 实时设备指标

## 告警管理
- `GET /alerts/` - 最新告警列表
- `POST /alerts/{id}/ack` - 确认告警

## 系统监控
- `GET /monitor/health` - 健康检查
- `GET /monitor/runtime` - 运行时性能指标

## 实时数据
- `POST /realtime/tick` - 手动触发数据推送
- `WS /ws/dashboard` - WebSocket 实时推送通道

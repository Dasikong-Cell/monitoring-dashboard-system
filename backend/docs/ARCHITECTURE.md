# 架构设计

## 分层架构
```
┌─────────────────────────────────┐
│     Controller (6 个)            │  ← HTTP 接口层
├─────────────────────────────────┤
│     Service (5 个)               │  ← 业务逻辑层
├─────────────────────────────────┤
│     Mapper (4 个)                │  ← 数据访问层 (MyBatis-Plus)
├─────────────────────────────────┤
│     Entity (4 个)                │  ← 数据库实体
└─────────────────────────────────┘
          ↑
┌─────────────────────────────────┐
│  CacheManager + RuntimeMetrics   │  ← 本地组件
└─────────────────────────────────┘
```

## 包结构
- `common/` - R、异常、全局处理
- `config/` - WebMvc、Async、MyBatisPlus、WebSocket
- `controller/` - Dashboard、Device、Alert、Monitor、Realtime
- `dto/` - 请求参数对象
- `vo/` - 视图对象
- `local/` - CacheManager（TTL缓存）、RuntimeMetricsCollector（性能计数器）
- `ws/` - WebSocket 实时推送

## 缓存策略
- TTL_FAST: 2s（实时指标）
- TTL_MEDIUM: 3s（聚合数据）
- TTL_SLOW: 5s（趋势数据）

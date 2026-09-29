# 智慧园区设备运维监控大屏系统

> Smart Park OPS Monitoring Dashboard —— 面向园区场景的实时监控可视化大屏

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-orange.svg)](https://vuejs.org/)
[![ECharts](https://img.shields.io/badge/ECharts-5.5-teal.svg)](https://echarts.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://dev.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

---

## 项目简介

本系统为智慧园区运维场景提供实时监控大屏，覆盖设备状态、告警、流量三大核心指标，数据由后端实时推送，大屏即时响应。

### 核心功能

| 模块 | 技术实现 |
|------|----------|
| 核心指标卡片 | 汇总设备总数、在线/故障/离线、今日告警、出口流量 —— **Redis 缓存** |
| 设备类别分布 | 环形饼图 —— **ECharts** |
| 近 24h 告警趋势 | 平滑折线 + 渐变面积 —— **ECharts** |
| 告警级别分布 | 横向三色柱状（紧急/普通/提示） —— **ECharts** |
| 近 12h 流量趋势 | 双面积图（内网 vs 外网） —— **ECharts** |
| 流量 TOP10 | 横向渐变柱 —— **ECharts** |
| 设备健康度 | 3 个仪表盘（在线率/故障率/处理率） —— **ECharts Gauge** |
| 实时负载 TOP | 列表 + 动态进度条 —— **实时刷新** |
| 最新告警列表 | 自动滚动列表 + 级别色条 —— **WebSocket 推送** |
| 实时数据推送 | 后端定时模拟 + `@Scheduled` + **WebSocket 广播** |
| 大屏自适应 | 1920×1080 设计稿 + **scale 等比缩放**，适配所有分辨率 |
| 大屏边框装饰 | **DataV-Vue3** 边框组件 |

### 数据流

```
MySQL（原始数据） ──► 聚合统计 ──► Redis（缓存）
                          │
                          ▼
                   SpringBoot REST API ◄── 前端 Axios 定时轮询（5s）
                          │
                          ▼
                 WebSocket /ws/dashboard ◄── 前端 WS 实时订阅（3s 推一次）
```

---

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + Hutool 5.8.26 + FastJSON2 |
| 数据库 | MySQL 8.0 + Redis 7 |
| 前端 | Vue 3.5 + ECharts 5.5 + DataV-Vue3 + Axios + Vite 5 |
| 实时通信 | Spring WebSocket (JSR-356) |

---

## 数据库设计

### device 设备表
| 字段 | 说明 |
|------|------|
| id | 主键 |
| name | 设备名称 |
| code | 设备编号（唯一） |
| category | camera / door / sensor / router / server |
| location | 安装位置 |
| ip | IP 地址 |
| status | online / offline / fault / maintenance |

### device_metric 实时指标快照（每 3s 一条）
| 字段 | 说明 |
|------|------|
| device_id | 关联设备 |
| cpu / memory / temperature / bandwidth | 实时指标 |
| online | 在线状态 |
| ts | 时间戳 |

### alert 告警表
| 字段 | 说明 |
|------|------|
| level | critical / warning / info |
| device_id / device_name | 关联设备 |
| message | 告警描述 |
| ack | 是否处理 |

### access_log 访问流量日志
| 字段 | 说明 |
|------|------|
| source / target | 来源区域 / 目标设备 |
| bytes | 流量字节数 |

---

## 快速开始

### 1. 启动 MySQL + Redis

```bash
# MySQL（确保端口 3306）
# Redis（确保端口 6379）
redis-server
```

### 2. 初始化数据库

```bash
mysql -u root -p123456 < backend/sql/init.sql
```

脚本会自动：
- 创建 `monitor_dashboard` 数据库
- 建 4 张表
- 写入 **30 台设备**（摄像头、门禁、传感器、网络设备、服务器）
- 初始实时指标、告警、流量数据

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
# 启动在 http://localhost:8089/api
```

配置文件：`backend/src/main/resources/application.yml`
- 数据库：`monitor_dashboard` / `root` / `123456`
- Redis：`localhost:6379`

### 4. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
# 访问 http://localhost:5174
```

Vite 已配置 `/api` → `http://localhost:8089` 代理。

### 5. 查看效果

打开浏览器访问 `http://localhost:5174`，看到：
- 深蓝科技感大屏（1920×1080 设计稿，自动缩放适配）
- 标题栏显示 WebSocket 连接状态、时钟
- 左侧：4 张汇总卡片 + 告警趋势 + 流量趋势
- 中间：设备类别饼图 + 3 个健康度仪表盘 + 实时负载 TOP
- 右侧：告警级别柱图 + 流量 TOP10 + 最新告警滚动列表

后端会自动：
- **每 3 秒**模拟新的设备指标快照、随机产生告警、写入访问日志
- **每 3 秒**通过 WebSocket 广播 summary + 最新告警

---

## 项目结构

```
monitor-dashboard-system/
├── backend/                                  # SpringBoot 后端
│   ├── pom.xml
│   ├── sql/init.sql                          # 建库 + 初始化数据
│   └── src/main/java/com/monitor/dashboard/
│       ├── DashboardApplication.java         # 启动类（@EnableScheduling）
│       ├── common/R.java                     # 统一响应
│       ├── config/
│       │   ├── AppConfig.java                # MyBatis-Plus + CORS
│       │   ├── RedisConfig.java              # Redis 序列化
│       │   └── WebSocketConfig.java          # WebSocket 导出器
│       ├── controller/DashboardController.java   # 8 个统计 API
│       ├── entity/                           # Device / DeviceMetric / Alert / AccessLog
│       ├── mapper/                           # MyBatis-Plus Mapper
│       ├── service/DashboardService.java      # 核心聚合 + @Scheduled tick
│       └── ws/DashboardWebSocket.java        # WebSocket endpoint /ws/dashboard
├── frontend/                                 # Vue3 前端
│   ├── index.html
│   ├── vite.config.js                        # /api → 8089 代理
│   ├── package.json
│   └── src/
│       ├── main.js                           # 引入 DataVVue3
│       ├── App.vue                           # scale 自适应
│       ├── style.css                         # 全局暗黑样式
│       ├── api/index.js                      # Axios 封装
│       ├── utils/websocket.js                # WS 自动重连
│       └── views/Dashboard.vue               # 大屏主页面
├── .gitignore
├── LICENSE
└── README.md
```

---

## REST API

| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/dashboard/summary` | GET | 核心指标汇总（带 Redis 5s 缓存） |
| `/api/dashboard/category` | GET | 设备类别分布 |
| `/api/dashboard/alert-trend` | GET | 近 24 小时告警趋势 |
| `/api/dashboard/alert-level` | GET | 近 30 日告警级别分布 |
| `/api/dashboard/alerts` | GET | 最新告警列表（最多 20 条） |
| `/api/dashboard/flow` | GET | 近 6 小时流量 TOP10 |
| `/api/dashboard/flow-trend` | GET | 近 12 小时流量趋势（内网/外网） |
| `/api/dashboard/metrics` | GET | 设备实时指标 TOP30 |

### WebSocket

**URL**: `ws://localhost:8089/api/ws/dashboard`

后端每 3 秒广播：
```json
{
  "ts": 1720454000000,
  "data": {
    "summary": { /* 同 /summary */ },
    "alerts": [ /* 最新 5 条 */ ]
  }
}
```

---

## 核心实现亮点

### 1. 自适应缩放（零依赖）
```js
// App.vue —— 根据视口与设计稿(1920x1080)的比例，取 min(sx, sy) 整体缩放
scale = Math.min(window.innerWidth/1920, window.innerHeight/1080)
transform: scale(${scale})
```
任何分辨率都能铺满、不拉伸、不溢出。

### 2. WebSocket 自动重连
```js
// utils/websocket.js
connect() → onclose → 3s 后重连 → 成功则清定时器
closed 标记保证页面销毁后不再重连
```

### 3. Redis 热数据缓存 + 定时失效
```java
// 每个接口先查缓存，hit 直接返回；miss 查 DB 并写入 TTL=3~15s
redis.opsForValue().set("dash:summary", data, 5, TimeUnit.SECONDS);
// tick() 定时先 delete 所有 key，再调用所有接口填充新值
redis.delete(Arrays.asList(CACHE_SUMMARY, ...));
```

### 4. 模拟实时数据（可替换为真实采集）
```java
@Scheduled(fixedDelay = 3000)
public void tick() {
    // 1. 生成新的 device_metric 快照（CPU/内存/温度/带宽 随机波动）
    // 2. 1.5% 概率产生新告警
    // 3. 随机写入 access_log
    // 4. 清理 1 小时前的快照
    // 5. WebSocket 广播 summary + alerts
}
```

### 5. 大屏装饰（DataV-Vue3）
```vue
<dv-decoration-3 style="width:100%;height:100%;" />
```
无需手写 SVG 边框，一行搞定。

---

## 生产部署

- **后端打包**：`mvn clean package -DskipTests` → `monitor-dashboard.jar`
- **前端打包**：`pnpm build` → `dist/`，Nginx 托管 + `/api` 反代
- **Redis 可切换为 Lettuce 集群 / Sentinel**
- **真实告警**：将 `@Scheduled tick()` 替换为 Prometheus AlertManager Webhook 或自建 Agent 上报

---

## 安全与部署

本仓库已落实以下生产就绪与安全加固：

- **密钥外置**：`DB_PASSWORD`、微信密钥等均通过环境变量注入（见 `.env.example`），生产务必覆盖默认值。
- **Actuator 健康端点**：引入 `spring-boot-starter-actuator`，仅暴露 `health`、`info`；`health` 的详细信息 `show-details: when_authorized`（携带有效凭证才展示），且已排除在鉴权拦截之外。
- **接口鉴权总开关**：新增 `AuthInterceptor`，由环境变量 `REQUIRE_AUTH` 控制（默认 `false`，保持与原行为兼容）。置为 `true` 后，非 GET 的 `/api/**` 必须携带 `Bearer Token`；只读 GET 与 `/api/actuator/**` 始终放行。当前服务未签发 JWT，开启后仅做「是否携带 Bearer」准入校验，如需严格验签请补充 JWT 逻辑。
- **CORS 必须配置**：生产环境请设置环境变量 `CORS_ALLOWED_ORIGINS`（逗号分隔的允许来源），不再使用通配符配合凭证。`backend/src/main/resources/application.yml` 中默认仅放开本地前端来源。
- **不回显内部异常**：全局异常处理器对未捕获异常统一返回「服务器内部错误」，完整堆栈仅记录在服务端日志。
- **结构化日志**：`backend/src/main/resources/logback-spring.xml` 输出控制台 + 滚动文件，应用包 `INFO`、MyBatis/SQL 日志降为 `WARN`（已关闭 `StdOutImpl`，避免完整 SQL 与参数打印到 stdout）。
- **Docker 镜像对齐**：`Dockerfile` 基础镜像升级为 `eclipse-temurin:21-jre-alpine`，容器端口固定 `8080`；`docker-compose.yml` 已通过 `SPRING_DATASOURCE_*` 将后端数据源接通到 `mysql` 服务（库名 `monitor_dashboard`），不再依赖硬编码的 `localhost`。
- **CI**：`.github/workflows/ci.yml` 使用 JDK 21 + `mvn -B test` 进行构建与测试。

---

## 许可证

[MIT License](./LICENSE) © 2025

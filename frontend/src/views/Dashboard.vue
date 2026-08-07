<template>
  <div class="dashboard">

    <!-- ========= 顶部标题栏 ========= -->
    <header class="header">
      <div class="header-side left-side">
        <span class="dot ws-dot" :class="{ alive: wsAlive }"></span>
        <span class="ws-label">{{ wsAlive ? '数据实时推送中' : '等待连接...' }}</span>
        <span class="refresh-info">最近刷新 {{ refreshLabel }}</span>
      </div>

      <div class="header-title">
        <span class="title-main">智慧园区设备运维监控中心</span>
        <span class="title-sub">SMART PARK OPS MONITORING</span>
      </div>

      <div class="header-side right-side">
        <span class="weather-label">🌤 多云 24℃</span>
        <span class="date-label">{{ dateText }}</span>
        <span class="time-label">{{ timeText }}</span>
      </div>
    </header>

    <!-- ========= 主体 三栏 ========= -->
    <main class="body">

      <!-- ---- 左栏 ---- -->
      <section class="col col-left">

        <!-- 汇总卡片 -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">核心指标概览</div>
          <div class="cards">
            <div class="card">
              <div class="card-icon" style="color:#2196ff">🖥</div>
              <div class="card-body">
                <div class="card-value">{{ summary.deviceTotal ?? 0 }}</div>
                <div class="card-label">设备总数</div>
              </div>
            </div>
            <div class="card">
              <div class="card-icon" style="color:#00d9a7">✓</div>
              <div class="card-body">
                <div class="card-value">{{ summary.deviceOnline ?? 0 }}</div>
                <div class="card-label">在线运行</div>
              </div>
            </div>
            <div class="card">
              <div class="card-icon" style="color:#ff4d4f">⚠</div>
              <div class="card-body">
                <div class="card-value">{{ summary.deviceFault ?? 0 }}</div>
                <div class="card-label">故障设备</div>
              </div>
            </div>
            <div class="card">
              <div class="card-icon" style="color:#faad14">⚡</div>
              <div class="card-body">
                <div class="card-value">{{ summary.todayAlerts ?? 0 }}</div>
                <div class="card-label">今日告警</div>
              </div>
            </div>
          </div>
          <div class="extra-row">
            <div class="extra-item">
              <span class="extra-val">{{ summary.todayFlowMB ?? 0 }} <small>MB</small></span>
              <span class="extra-lbl">今日出口流量</span>
            </div>
            <div class="extra-item">
              <span class="extra-val">{{ summary.unacked ?? 0 }} <small>条</small></span>
              <span class="extra-lbl">未处理告警</span>
            </div>
            <div class="extra-item">
              <span class="extra-val">{{ summary.deviceOffline ?? 0 }} <small>台</small></span>
              <span class="extra-lbl">离线设备</span>
            </div>
          </div>
        </div>

        <!-- 近 24h 告警趋势 -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">近 24 小时告警趋势</div>
          <div ref="chartAlertRef" class="chart-box"></div>
        </div>

        <!-- 近 12h 流量趋势 -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">近 12 小时网络流量（MB）</div>
          <div ref="chartFlowTrendRef" class="chart-box"></div>
        </div>
      </section>

      <!-- ---- 中间栏 ---- -->
      <section class="col col-center">

        <!-- 设备类别分布 -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">设备类别分布</div>
          <div ref="chartCatRef" class="chart-box"></div>
        </div>

        <!-- 设备状态仪表盘 -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">设备健康度</div>
          <div class="gauge-wrap">
            <div class="gauge-item">
              <div ref="gaugeOnlineRef" class="gauge"></div>
              <div class="gauge-label">在线率</div>
            </div>
            <div class="gauge-item">
              <div ref="gaugeFaultRef" class="gauge"></div>
              <div class="gauge-label">故障率</div>
            </div>
            <div class="gauge-item">
              <div ref="gaugeAlertsRef" class="gauge"></div>
              <div class="gauge-label">告警处理率</div>
            </div>
          </div>
        </div>

        <!-- 实时指标 TOP -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">实时负载 TOP 设备</div>
          <div class="metric-list">
            <div v-for="(m, i) in topMetrics" :key="m.deviceId" class="metric-row">
              <span class="rank" :class="['r' + (i+1)]">{{ i + 1 }}</span>
              <span class="m-name" :title="m.deviceName">{{ m.deviceName }}</span>
              <div class="bar-wrap">
                <div class="bar bar-cpu" :style="{width: m.cpu + '%'}"></div>
              </div>
              <span class="m-val">{{ m.cpu }}%</span>
            </div>
            <div v-if="!topMetrics.length" class="empty-tip">暂无实时数据…</div>
          </div>
        </div>
      </section>

      <!-- ---- 右栏 ---- -->
      <section class="col col-right">

        <!-- 告警级别 -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">近 30 日告警级别分布</div>
          <div ref="chartLevelRef" class="chart-box"></div>
        </div>

        <!-- 流量 TOP10 -->
        <div class="panel">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">近 6 小时流量 TOP10（MB）</div>
          <div ref="chartFlowRef" class="chart-box"></div>
        </div>

        <!-- 最新告警列表 -->
        <div class="panel panel-alerts">
          <dv-decoration-3 style="width:100%;height:100%;" />
          <div class="panel-title">
            <span>最新告警事件</span>
            <span class="pulse-tip">
              <span class="pulse-dot"></span>
              <span>实时刷新</span>
            </span>
          </div>
          <div ref="alertScrollBox" class="alert-scroll">
            <div ref="alertScrollTrack" class="alert-track">
              <div v-for="a in alerts" :key="a.id + '-' + a.createdAt"
                   class="alert-item" :class="'lv-' + a.level">
                <span class="lv-badge">{{ a.levelText }}</span>
                <span class="a-device" :title="a.deviceName">{{ a.deviceName }}</span>
                <span class="a-msg" :title="a.message">{{ a.message }}</span>
                <span class="a-time">{{ a.createdAt.substring(11) }}</span>
                <span class="a-status">{{ a.ack ? '已处理' : '待处理' }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { api } from '../api/index.js'
import { createWS } from '../utils/websocket.js'

// ============ 状态 ============
const wsAlive = ref(false)
const refreshLabel = ref('--:--:--')
const dateText = ref('')
const timeText = ref('')

const summary = ref({})
const alerts = ref([])
const topMetrics = ref([])

// 图表 ref
const chartAlertRef = ref()
const chartCatRef = ref()
const chartLevelRef = ref()
const chartFlowRef = ref()
const chartFlowTrendRef = ref()
const gaugeOnlineRef = ref()
const gaugeFaultRef = ref()
const gaugeAlertsRef = ref()

let charts = []
let wsInstance = null
let timers = []

// ============ 时钟 ============
function clockTick() {
  const now = dayjs()
  dateText.value = now.format('YYYY年MM月DD日 dddd').replace(/Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday/,
    d => ({Sunday:'周日',Monday:'周一',Tuesday:'周二',Wednesday:'周三',Thursday:'周四',Friday:'周五',Saturday:'周六'}[d]))
  timeText.value = now.format('HH:mm:ss')
}
timers.push(setInterval(clockTick, 1000))

// ============ 工具：初始化图表 ============
function initChart(ref, option) {
  const el = typeof ref === 'string' ? document.querySelector(ref) : ref.value
  if (!el) return null
  const c = echarts.init(el, 'dark')
  c.setOption(option)
  charts.push(c)
  return c
}

// 通用配色
const palette = ['#00e5ff', '#2196ff', '#7c4dff', '#9c4dff', '#00d9a7', '#ffb020', '#ff4d4f', '#ff80ab']
const axisColor = 'rgba(120,180,255,0.35)'
const axisText  = 'rgba(200,220,255,0.65)'

// ============ 告警趋势 ============
function initAlertTrend() {
  return initChart(chartAlertRef, {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,30,60,0.9)', borderColor: '#2196ff', textStyle: { color: '#cfe3ff' } },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: [], axisLine: { lineStyle: { color: axisColor } }, axisLabel: { color: axisText } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(60,100,160,0.15)' } }, axisLabel: { color: axisText } },
    series: [{
      data: [], type: 'line', smooth: true, symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#ff4d4f', width: 2 },
      itemStyle: { color: '#ff4d4f' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0,0,0,1,[
          {offset:0, color:'rgba(255,77,79,0.6)'},
          {offset:1, color:'rgba(255,77,79,0.02)'}
        ])
      }
    }]
  })
}

// ============ 流量趋势 ============
function initFlowTrend() {
  return initChart(chartFlowTrendRef, {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,30,60,0.9)', borderColor: '#2196ff', textStyle: { color: '#cfe3ff' } },
    legend: { textStyle: { color: axisText }, top: 0, right: 10 },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: [], boundaryGap: false, axisLine: { lineStyle: { color: axisColor } }, axisLabel: { color: axisText } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(60,100,160,0.15)' } }, axisLabel: { color: axisText } },
    series: [
      { name: '内网', data: [], type: 'line', smooth: true, symbol: 'none',
        lineStyle: { color: '#00e5ff', width: 2 },
        areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(0,229,255,0.45)'},{offset:1,color:'rgba(0,229,255,0)'}]) } },
      { name: '外网', data: [], type: 'line', smooth: true, symbol: 'none',
        lineStyle: { color: '#9c4dff', width: 2 },
        areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(156,77,255,0.45)'},{offset:1,color:'rgba(156,77,255,0)'}]) } }
    ]
  })
}

// ============ 设备类别饼 ============
function initCat() {
  return initChart(chartCatRef, {
    tooltip: { trigger: 'item', backgroundColor: 'rgba(10,30,60,0.9)', borderColor: '#2196ff', textStyle: { color: '#cfe3ff' } },
    legend: { textStyle: { color: axisText }, bottom: 0 },
    color: palette,
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
      label: { color: axisText, formatter: '{b}\n{d}%' },
      labelLine: { lineStyle: { color: axisColor } },
      itemStyle: { borderColor: '#050d1f', borderWidth: 2 },
      data: []
    }]
  })
}

// ============ 告警级别横向柱 ============
function initLevel() {
  return initChart(chartLevelRef, {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,30,60,0.9)', borderColor: '#2196ff', textStyle: { color: '#cfe3ff' } },
    grid: { left: 100, right: 30, top: 20, bottom: 20 },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(60,100,160,0.15)' } }, axisLabel: { color: axisText } },
    yAxis: { type: 'category', data: ['提示', '普通告警', '紧急告警'], inverse: true,
      axisLine: { lineStyle: { color: axisColor } }, axisLabel: { color: axisText } },
    series: [{
      type: 'bar', data: [],
      itemStyle: {
        color: (p) => ['#00d9a7', '#faad14', '#ff4d4f'][p.dataIndex],
        borderRadius: [0, 4, 4, 0]
      },
      barWidth: 16,
      label: { show: true, position: 'right', color: axisText }
    }]
  })
}

// ============ 流量 TOP10 ============
function initFlow() {
  return initChart(chartFlowRef, {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,30,60,0.9)', borderColor: '#2196ff', textStyle: { color: '#cfe3ff' } },
    grid: { left: 110, right: 30, top: 10, bottom: 10 },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(60,100,160,0.15)' } }, axisLabel: { color: axisText } },
    yAxis: { type: 'category', data: [], inverse: true,
      axisLine: { lineStyle: { color: axisColor } }, axisLabel: { color: axisText } },
    series: [{
      type: 'bar', data: [], barWidth: 14,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0,0,1,0,[
          {offset:0, color:'#00e5ff'},{offset:1, color:'#2196ff'}]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', color: axisText, formatter: '{c} MB' }
    }]
  })
}

// ============ 仪表盘 ============
function initGauge(ref, min, max, color) {
  return initChart(ref, {
    series: [{
      type: 'gauge', radius: '100%', center: ['50%', '55%'],
      startAngle: 200, endAngle: -20,
      min, max,
      splitNumber: 5,
      progress: { show: true, width: 8, itemStyle: { color } },
      axisLine: { lineStyle: { width: 8, color: [[1, 'rgba(60,100,160,0.18)']] } },
      axisTick: { distance: -12, length: 4, lineStyle: { color: axisColor } },
      splitLine: { distance: -16, length: 8, lineStyle: { color: axisColor } },
      axisLabel: { distance: -20, color: axisText, fontSize: 10 },
      anchor: { show: false },
      pointer: { show: false },
      title: { show: false },
      detail: { offsetCenter: [0, '15%'], color: color, fontSize: 20, fontWeight: 'bold', formatter: '{value}%' },
      data: [{ value: 0 }]
    }]
  })
}

// ============ 刷新所有图表 ============
async function refreshAll() {
  try {
    summary.value   = await api.summary()     || {}
    refreshLabel.value = dayjs().format('HH:mm:ss')

    const [cat, at, lv, fl, ft, mt] = await Promise.all([
      api.category(), api.alertTrend(), api.alertLevel(),
      api.flow(), api.flowTrend(), api.metrics()
    ])

    // 类别饼
    cat?.names && charts._cat?.setOption({ series: [{ data: cat.names.map((n,i)=>({name:n,value:cat.values[i]})) }] })
    // 告警趋势
    at?.hours && charts._alert?.setOption({ xAxis: { data: at.hours }, series: [{ data: at.counts }] })
    // 告警级别
    lv?.values && charts._level?.setOption({ series: [{ data: lv.values }] })
    // 流量 TOP
    fl?.names && charts._flow?.setOption({ yAxis: { data: fl.names }, series: [{ data: fl.values }] })
    // 流量趋势
    ft?.labels && charts._flowTrend?.setOption({
      xAxis: { data: ft.labels },
      series: [{ data: ft.inbound }, { data: ft.outbound }]
    })
    // TOP 负载设备（CPU 排序，取前 6）
    topMetrics.value = (mt || []).sort((a,b) => Number(b.cpu) - Number(a.cpu)).slice(0, 6)

    // 仪表盘
    const s = summary.value
    const total = s.deviceTotal || 1
    const onlineRate = Math.round((s.deviceOnline || 0) / total * 100)
    const faultRate  = Math.round((s.deviceFault || 0) / total * 100)
    const ackRate    = Math.round(((s.todayAlerts || 0) - (s.unacked || 0)) / Math.max(1, s.todayAlerts || 1) * 100)
    charts._gOnline?.setOption({ series: [{ data: [{ value: onlineRate }] }] })
    charts._gFault?.setOption({ series: [{ data: [{ value: faultRate }] }] })
    charts._gAck?.setOption({ series: [{ data: [{ value: ackRate }] }] })

    // 告警列表
    alerts.value = await api.alerts() || []
  } catch (e) { console.error('refreshAll fail', e) }
}

// ============ 滚动 ============
function setupAlertScroll() {
  const box = document.querySelector('.alert-scroll')
  const track = document.querySelector('.alert-track')
  if (!box || !track) return
  let raf
  let offset = 0
  function scroll() {
    if (!track || !box) return
    const single = 48
    const totalH = track.scrollHeight
    if (totalH <= box.clientHeight) return
    offset += 0.5
    if (offset > totalH / 2) offset = 0
    track.style.transform = `translateY(-${offset}px)`
    raf = requestAnimationFrame(scroll)
  }
  raf = requestAnimationFrame(scroll)
  timers.push({ cancel: () => raf && cancelAnimationFrame(raf) })
}

// ============ 初始化 ============
onMounted(async () => {
  clockTick()

  await nextTick()

  charts._cat      = initCat()
  charts._alert    = initAlertTrend()
  charts._flowTrend= initFlowTrend()
  charts._level    = initLevel()
  charts._flow     = initFlow()
  charts._gOnline  = initGauge(gaugeOnlineRef, 0, 100, '#00d9a7')
  charts._gFault   = initGauge(gaugeFaultRef, 0, 100, '#ff4d4f')
  charts._gAck     = initGauge(gaugeAlertsRef, 0, 100, '#2196ff')

  await refreshAll()
  setupAlertScroll()

  // 每 5 秒轮询全量
  timers.push(setInterval(refreshAll, 5000))

  // WebSocket 实时增量
  const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
  wsInstance = createWS(`${proto}//${location.host}/api/ws/dashboard`, (msg) => {
    wsAlive.value = true
    if (msg?.data?.summary) {
      summary.value = msg.data.summary
      refreshLabel.value = dayjs().format('HH:mm:ss')
    }
    if (msg?.data?.alerts) {
      alerts.value = [...msg.data.alerts, ...alerts.value].slice(0, 20)
    }
  })
})

onBeforeUnmount(() => {
  charts.forEach(c => c.dispose?.())
  timers.forEach(t => typeof t === 'number' ? clearInterval(t) : t.cancel?.())
  wsInstance?.close()
  window.removeEventListener('resize', onResize)
})

function onResize() { charts.forEach(c => c.resize?.()) }
window.addEventListener('resize', onResize)
</script>

<style scoped>
.dashboard {
  width: 1920px; height: 1080px;
  display: flex; flex-direction: column;
  background:
    radial-gradient(ellipse at top, rgba(33,150,255,0.12), transparent 60%),
    radial-gradient(ellipse at bottom, rgba(156,77,255,0.10), transparent 60%),
    #050d1f;
  padding: 12px 18px;
  gap: 12px;
  box-sizing: border-box;
  position: relative;
}

/* ======= 顶部 ======= */
.header {
  height: 72px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid rgba(33,150,255,0.35);
  position: relative;
}
.header::before, .header::after {
  content: ''; position: absolute; bottom: -1px;
  width: 120px; height: 2px;
  background: linear-gradient(90deg, transparent, #2196ff, transparent);
}
.header::before { left: 10%; }
.header::after  { right: 10%; }

.header-title { text-align: center; line-height: 1.1; }
.title-main {
  display: block;
  font-size: 32px; font-weight: 800; letter-spacing: 4px;
  background: linear-gradient(180deg, #ffffff, #00e5ff);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  text-shadow: 0 0 24px rgba(0,229,255,0.5);
}
.title-sub { font-size: 12px; color: rgba(0,229,255,0.6); letter-spacing: 6px; }

.header-side { display: flex; align-items: center; gap: 16px; font-size: 14px; }
.ws-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: #ff4d4f; box-shadow: 0 0 8px #ff4d4f;
  transition: all .3s;
}
.ws-dot.alive { background: #00d9a7; box-shadow: 0 0 12px #00d9a7; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100%{ opacity:1 } 50%{ opacity:.3 } }
.ws-label { color: #00d9a7; }
.refresh-info { color: rgba(200,220,255,0.5); }
.weather-label { color: #ffb020; }
.date-label { color: rgba(200,220,255,0.6); }
.time-label { color: #00e5ff; font-size: 20px; font-weight: bold; font-family: 'Consolas', monospace; }

/* ======= 三栏主体 ======= */
.body { flex: 1; display: grid; grid-template-columns: 480px 1fr 520px; gap: 12px; overflow: hidden; }
.col { display: flex; flex-direction: column; gap: 12px; min-height: 0; }

/* ======= 面板通用 ======= */
.panel {
  position: relative; padding: 10px 14px;
  background: linear-gradient(135deg, rgba(10,30,60,0.6), rgba(5,20,45,0.4));
  border: 1px solid rgba(33,150,255,0.25);
  border-radius: 4px;
  overflow: hidden;
  display: flex; flex-direction: column;
}
.panel-title {
  font-size: 15px; font-weight: bold; color: #00e5ff;
  padding-bottom: 6px; margin-bottom: 8px;
  border-bottom: 1px solid rgba(33,150,255,0.25);
  display: flex; justify-content: space-between; align-items: center;
  letter-spacing: 1px;
}
.chart-box { flex: 1; min-height: 140px; }

/* ======= 汇总卡片 ======= */
.cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-bottom: 10px; }
.card {
  background: rgba(0, 229, 255, 0.05);
  border: 1px solid rgba(0, 229, 255, 0.25);
  border-radius: 4px;
  padding: 10px 12px; display: flex; align-items: center; gap: 12px;
}
.card-icon { font-size: 28px; width: 40px; text-align: center; }
.card-value { font-size: 26px; font-weight: bold; color: #fff; font-family: 'Consolas', monospace; }
.card-label { font-size: 12px; color: rgba(200,220,255,0.6); margin-top: 2px; }
.extra-row { display: flex; gap: 10px; }
.extra-item {
  flex: 1; background: rgba(156,77,255,0.08);
  border: 1px solid rgba(156,77,255,0.25); border-radius: 4px;
  padding: 8px 10px; text-align: center;
}
.extra-val { font-size: 20px; font-weight: bold; color: #9c4dff; font-family: 'Consolas', monospace; }
.extra-val small { font-size: 12px; color: rgba(200,220,255,0.5); margin-left: 2px; }
.extra-lbl { font-size: 11px; color: rgba(200,220,255,0.55); display: block; margin-top: 2px; }

/* ======= 仪表盘 ======= */
.gauge-wrap { display: flex; justify-content: space-around; align-items: center; flex: 1; min-height: 140px; }
.gauge-item { display: flex; flex-direction: column; align-items: center; flex: 1; }
.gauge { width: 150px; height: 140px; }
.gauge-label { font-size: 13px; color: rgba(200,220,255,0.7); margin-top: -8px; }

/* ======= TOP 设备 ======= */
.metric-list { flex: 1; overflow: hidden; display: flex; flex-direction: column; gap: 8px; }
.metric-row {
  display: grid; grid-template-columns: 30px 140px 1fr 70px; align-items: center; gap: 8px;
  font-size: 13px; padding: 6px 8px; background: rgba(33,150,255,0.06);
  border: 1px solid rgba(33,150,255,0.15); border-radius: 3px;
}
.rank {
  width: 22px; height: 22px; line-height: 22px; text-align: center;
  border-radius: 3px; font-weight: bold; font-family: 'Consolas', monospace;
  background: rgba(33,150,255,0.2); color: #00e5ff; font-size: 12px;
}
.rank.r1 { background: linear-gradient(135deg,#ff4d4f,#faad14); color:#fff; }
.rank.r2 { background: linear-gradient(135deg,#faad14,#ffb020); color:#fff; }
.rank.r3 { background: linear-gradient(135deg,#ffb020,#ff80ab); color:#fff; }
.m-name { color: #cfe3ff; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bar-wrap { height: 10px; background: rgba(33,150,255,0.1); border-radius: 3px; overflow: hidden; }
.bar { height: 100%; border-radius: 3px; transition: width .6s ease; }
.bar-cpu { background: linear-gradient(90deg, #00e5ff, #2196ff); }
.m-val { color: #00e5ff; font-family: 'Consolas', monospace; text-align: right; }
.empty-tip { text-align: center; color: rgba(200,220,255,0.4); padding: 40px 0; }

/* ======= 告警滚动 ======= */
.panel-alerts { flex: 1; }
.pulse-tip { font-size: 12px; display: flex; align-items: center; gap: 6px; }
.pulse-dot { width: 8px; height: 8px; background: #ff4d4f; border-radius: 50%; animation: pulse 1.5s infinite; }
.alert-scroll { flex: 1; overflow: hidden; position: relative; height: 100%; }
.alert-track { display: flex; flex-direction: column; gap: 6px; will-change: transform; }
.alert-item {
  display: grid; grid-template-columns: 56px 140px 1fr 70px 60px;
  gap: 8px; align-items: center;
  padding: 8px 10px; border-radius: 3px; font-size: 13px;
  background: rgba(33,150,255,0.05);
  border-left: 3px solid #2196ff;
}
.alert-item.lv-critical { border-left-color: #ff4d4f; background: rgba(255,77,79,0.08); }
.alert-item.lv-warning  { border-left-color: #faad14; background: rgba(250,173,20,0.08); }
.alert-item.lv-info     { border-left-color: #00d9a7; background: rgba(0,217,167,0.06); }
.lv-badge {
  font-size: 11px; padding: 2px 6px; border-radius: 3px; text-align: center;
  font-weight: bold;
  background: rgba(33,150,255,0.2); color: #00e5ff;
}
.lv-critical .lv-badge { background: rgba(255,77,79,0.25); color: #ff4d4f; }
.lv-warning  .lv-badge { background: rgba(250,173,20,0.25); color: #faad14; }
.lv-info     .lv-badge { background: rgba(0,217,167,0.2); color: #00d9a7; }
.a-device { color: #cfe3ff; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.a-msg    { color: rgba(200,220,255,0.85); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 12px; }
.a-time   { color: rgba(200,220,255,0.55); font-family: 'Consolas', monospace; font-size: 12px; }
.a-status { text-align: center; font-size: 11px; padding: 2px 0; border-radius: 3px; }
.alert-item.lv-critical .a-status:not(.ack) { background: rgba(255,77,79,0.25); color: #ff4d4f; }
.alert-item.lv-warning  .a-status:not(.ack) { background: rgba(250,173,20,0.25); color: #faad14; }
.alert-item.lv-info     .a-status:not(.ack) { background: rgba(0,217,167,0.2); color: #00d9a7; }
.alert-item.lv-critical .a-status.ack,
.alert-item.lv-warning  .a-status.ack,
.alert-item.lv-info     .a-status.ack { background: rgba(200,220,255,0.15); color: rgba(200,220,255,0.55); }
</style>

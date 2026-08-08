<template>
  <div class="system-page">
    <h2>系统监控</h2>
    <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:16px">
      <div class="card">
        <h3>健康检查</h3>
        <div>状态: <b style="color:#2ed573">{{ health.status }}</b></div>
        <div>数据库: {{ health.db }}</div>
        <div>设备数: {{ health.deviceCount }}</div>
        <div>WebSocket 会话: {{ health.wsSessions }}</div>
      </div>
      <div class="card">
        <h3>运行时指标</h3>
        <div>缓存命中率: {{ runtime.cacheHitRate }}</div>
        <div>平均查询: {{ runtime.avgQueryMs }} ms</div>
        <div>广播次数: {{ runtime.wsBroadcasts }}</div>
        <div>JVM 已用: {{ runtime.jvmUsedMB }} MB / {{ runtime.jvmMaxMB }} MB</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { monitorApi } from '../api/monitorApi'

const health = ref({})
const runtime = ref({})
let timer

async function load() {
  try {
    [health.value, runtime.value] = await Promise.all([monitorApi.health(), monitorApi.runtime()])
  } catch(e) { console.warn(e) }
}

onMounted(() => { load(); timer = setInterval(load, 5000) })
onUnmounted(() => clearInterval(timer))
</script>

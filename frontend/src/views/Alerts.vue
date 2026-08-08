<template>
  <div class="alerts-page">
    <h2>告警中心</h2>
    <div class="card">
      <table style="width:100%;border-collapse:collapse">
        <thead>
          <tr style="background:#fafafa;text-align:left">
            <th style="padding:8px">级别</th>
            <th style="padding:8px">设备</th>
            <th style="padding:8px">消息</th>
            <th style="padding:8px">时间</th>
            <th style="padding:8px">状态</th>
            <th style="padding:8px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in alerts" :key="a.id" style="border-bottom:1px solid #f0f0f0">
            <td style="padding:8px"><AlertBadge :level="a.level" /></td>
            <td style="padding:8px">{{ a.deviceName }}</td>
            <td style="padding:8px">{{ a.message }}</td>
            <td style="padding:8px">{{ a.createdAt }}</td>
            <td style="padding:8px">{{ a.ack ? '已处理' : '未处理' }}</td>
            <td style="padding:8px">
              <button v-if="!a.ack" @click="handleAck(a.id)" style="padding:4px 12px;background:#4facfe;color:#fff;border:none;border-radius:4px;cursor:pointer">确认</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AlertBadge from '../components/AlertBadge.vue'
import { alertApi } from '../api/alertApi'

const alerts = ref([])
onMounted(load)
async function load() { try { alerts.value = await alertApi.list() } catch(e) { console.warn(e) } }
async function handleAck(id) { await alertApi.ack(id); await load() }
</script>

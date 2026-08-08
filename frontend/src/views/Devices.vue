<template>
  <div class="devices-page">
    <h2>设备管理</h2>
    <div class="card">
      <table style="width:100%;border-collapse:collapse">
        <thead>
          <tr style="background:#fafafa;text-align:left">
            <th style="padding:8px">ID</th>
            <th style="padding:8px">名称</th>
            <th style="padding:8px">分类</th>
            <th style="padding:8px">IP</th>
            <th style="padding:8px">位置</th>
            <th style="padding:8px">状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in devices" :key="d.id" style="border-bottom:1px solid #f0f0f0">
            <td style="padding:8px">{{ d.id }}</td>
            <td style="padding:8px;font-weight:500">{{ d.name }}</td>
            <td style="padding:8px">{{ deviceCategoryZH[d.category] || d.category }}</td>
            <td style="padding:8px">{{ d.ip || '-' }}</td>
            <td style="padding:8px">{{ d.location || '-' }}</td>
            <td style="padding:8px"><StatusTag :status="d.status" /></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import StatusTag from '../components/StatusTag.vue'
import { deviceApi } from '../api/deviceApi'
import { DEVICE_CATEGORY_ZH } from '../utils/constants'

const devices = ref([])
onMounted(async () => {
  try { devices.value = await deviceApi.list() } catch(e) { console.warn(e) }
})
</script>

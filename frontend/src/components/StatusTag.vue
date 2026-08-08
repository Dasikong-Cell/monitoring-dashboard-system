<template>
  <span :style="{ background: map[status].bg, color: map[status].fg, padding: '2px 10px', borderRadius: 4, fontSize: 12 }">
    {{ map[status].label }}
  </span>
</template>

<script setup>
const props = defineProps({ status: { type: String, default: 'online' } })
const map = {
  online:  { label: '在线', bg: '#e8f5e9', fg: '#2e7d32' },
  offline: { label: '离线', bg: '#f5f5f5', fg: '#757575' },
  fault:   { label: '故障', bg: '#ffebee', fg: '#c62828' }
}
</script>

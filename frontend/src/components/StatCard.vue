<template>
  <div class="stat-card">
    <div class="stat-icon" :style="{ background: color }">{{ icon }}</div>
    <div class="stat-body">
      <div class="stat-value">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  icon: { type: String, default: '📊' },
  color: { type: String, default: '#4facfe' }
})
</script>

<style scoped>
.stat-card {
  background: #fff; border-radius: 8px; padding: 16px 20px;
  display: flex; align-items: center; gap: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff;
}
.stat-value { font-size: 24px; font-weight: bold; color: #222; }
.stat-label { font-size: 12px; color: #888; margin-top: 2px; }
</style>

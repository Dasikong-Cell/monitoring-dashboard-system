<template>
  <span :style="{ background: levelCfg.color, color: '#fff', padding: '2px 8px', borderRadius: 10, fontSize: 12 }">
    {{ levelCfg.label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { ALERT_LEVEL } from '../utils/constants'
const props = defineProps({ level: { type: String, default: 'info' } })
const levelCfg = computed(() => ALERT_LEVEL[props.level] || ALERT_LEVEL.info)
</script>

<template>
  <div id="scaler" ref="scalerRef" :style="scaleStyle">
    <Dashboard />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Dashboard from './views/Dashboard.vue'

// 设计稿基准 1920x1080
const W = 1920, H = 1080
const scale = ref(1)

function updateScale() {
  const sx = window.innerWidth / W
  const sy = window.innerHeight / H
  scale.value = Math.min(sx, sy)
}

onMounted(() => {
  updateScale()
  window.addEventListener('resize', updateScale)
})
onBeforeUnmount(() => window.removeEventListener('resize', updateScale))

const scaleStyle = computed(() => ({
  width: `${W}px`,
  height: `${H}px`,
  transform: `scale(${scale.value})`,
  transformOrigin: 'top left',
  position: 'absolute',
  top: '0',
  left: `max(0px, calc((100vw - ${W * scale.value}px) / 2))`,
  background: 'transparent'
}))
</script>

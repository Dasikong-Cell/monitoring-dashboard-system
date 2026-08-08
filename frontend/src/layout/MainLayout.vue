<template>
  <div class="main-layout">
    <header class="header">
      <div class="logo">🖥️ 智慧园区监控大屏</div>
      <nav class="nav">
        <router-link to="/">大屏</router-link>
        <router-link to="/devices">设备</router-link>
        <router-link to="/alerts">告警</router-link>
        <router-link to="/system">系统</router-link>
      </nav>
      <div class="status">
        <span :class="['dot', store.connected ? 'ok' : 'err']"></span>
        {{ store.connected ? '已连接' : '离线' }}
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <span>© 2026 智慧园区监控系统</span>
      <span>Java 17 + Spring Boot 3 + Vue 3 + ECharts</span>
    </footer>
  </div>
</template>

<script setup>
import { store } from '../store'
</script>

<style scoped>
.main-layout { min-height: 100vh; display: flex; flex-direction: column; }
.header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 24px; background: linear-gradient(90deg, #1a1a2e, #16213e);
  color: #fff;
}
.logo { font-size: 18px; font-weight: bold; }
.nav a { color: #9ab; margin: 0 12px; text-decoration: none; }
.nav a.router-link-active { color: #4facfe; }
.status { display: flex; align-items: center; gap: 6px; font-size: 13px; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot.ok { background: #2ed573; }
.dot.err { background: #ff4757; }
.main { flex: 1; padding: 20px; }
.footer { text-align: center; padding: 12px; color: #666; font-size: 12px; }
.footer span { margin: 0 8px; }
</style>

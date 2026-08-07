import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5175,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8089',
        changeOrigin: true,
        ws: true
      }
    }
  },
  build: {
    target: 'es2020',
    minify: 'esbuild',
    cssMinify: true,
    chunkSizeWarningLimit: 500,
    rollupOptions: {
      output: {
        manualChunks: {
          'echarts': ['echarts'],
          'datav': ['@kjgl77/datav-vue3'],
          'vue-vendor': ['vue', 'vue-router', 'pinia']
        }
      }
    }
  }
})

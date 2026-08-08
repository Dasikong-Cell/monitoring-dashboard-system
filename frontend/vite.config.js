import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@styles': resolve(__dirname, 'src/styles')
    }
  },
  css: {
    preprocessorOptions: {},
    resolve: {
      alias: {
        '@styles': resolve(__dirname, 'src/styles')
      }
    }
  },
  server: {
    port: 5175,
    host: true,
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8089',
        changeOrigin: true,
        ws: true
      }
    }
  },
  preview: {
    historyApiFallback: true
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

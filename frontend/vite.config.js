import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    allowedHosts: ['124ni4zy06929.vicp.fun'],
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})

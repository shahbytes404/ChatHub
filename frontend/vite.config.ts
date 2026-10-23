import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  server:{
    port: 4173,
    proxy: {
      '/api' : {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws':{
        target: 'ws://localhost:8080',
        ws:true,
        changeOrigin: true
      }
    }
  },
  preview:{
    port: 4173
  }
})

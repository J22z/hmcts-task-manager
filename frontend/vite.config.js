import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  server: {
    // Forwards API requests to the Spring Boot backend running on port 8080.
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
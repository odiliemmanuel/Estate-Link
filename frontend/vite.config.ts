import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev-server proxy so the browser only ever talks to this origin (/api/...).
// Each backend microservice gets a route prefix here. When the api-gateway is
// built, collapse these into a single target, e.g. '/api': 'http://localhost:9090'.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // user-service (auth) — port 8081
      '/api/v1/auth': 'http://localhost:8081',
      '/api/v1/users': 'http://localhost:8081',
      // property-service — port 8082
      '/api/v1/properties': 'http://localhost:8082',
      '/api/v1/listings': 'http://localhost:8082',
      '/api/v1/uploads': 'http://localhost:8082',
      // inspection-service — port 8083
      '/api/v1/inspection-slots': 'http://localhost:8083',
      '/api/v1/inspection-requests': 'http://localhost:8083',
      // offer-service — port 8084
      '/api/v1/offers': 'http://localhost:8084',
      // analytics-service — port 8086
      '/api/v1/analytics': 'http://localhost:8086',
    },
  },
})

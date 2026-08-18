/// <reference types="vite/client" />

interface ImportMetaEnv {
  // Optional override for the backend base URL. When unset, the app talks to
  // same-origin /api/* routes that Vite proxies to the microservices.
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

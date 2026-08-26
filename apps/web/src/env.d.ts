/// <reference types="vite/client" />

import 'vue-router'

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_AMAP_KEY?: string
  readonly VITE_AMAP_SECURITY_JS_CODE?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
  }
}

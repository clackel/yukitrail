import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import type { Pinia } from 'pinia'
import type { Router } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { apiClient } from './httpClients'

type RetriableRequest = InternalAxiosRequestConfig & { _retry?: boolean }

export const installApiInterceptors = (pinia: Pinia, router: Router) => {
  const auth = useAuthStore(pinia)

  const requestInterceptor = apiClient.interceptors.request.use((config) => {
    if (auth.accessToken) {
      config.headers.Authorization = `Bearer ${auth.accessToken}`
    }
    return config
  })

  const responseInterceptor = apiClient.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const request = error.config as RetriableRequest | undefined
      const isUnauthorized = error.response?.status === 401
      const isAuthRequest = request?.url?.includes('/auth/') ?? false

      if (!request || !isUnauthorized || isAuthRequest || request._retry) {
        return Promise.reject(error)
      }

      request._retry = true
      // 多个并发 401 会在 store 中共享同一个刷新任务。
      if (await auth.refresh()) {
        request.headers.Authorization = `Bearer ${auth.accessToken}`
        return apiClient.request(request)
      }

      const currentRoute = router.currentRoute.value
      if (currentRoute.name !== 'login') {
        await router.replace({
          name: 'login',
          query: { redirect: currentRoute.fullPath },
        })
      }
      return Promise.reject(error)
    },
  )

  return () => {
    // 测试和热重载场景可以主动卸载，避免重复注册拦截器。
    apiClient.interceptors.request.eject(requestInterceptor)
    apiClient.interceptors.response.eject(responseInterceptor)
  }
}

import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { apiClient, authClient } from '@/services/httpClients'
import type { ApiEnvelope, AuthSessionData, UserData } from '@/types/api'

interface RegisterPayload {
  email: string
  password: string
  nickname: string
}

interface LoginPayload {
  email: string
  password: string
}

// 模块级 Promise 保证并发请求只发起一次刷新。
let refreshPromise: Promise<boolean> | null = null

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const user = ref<UserData | null>(null)
  const initialized = ref(false)
  const isAuthenticated = computed(() => accessToken.value !== null && user.value !== null)

  const applySession = (session: AuthSessionData) => {
    // 访问令牌只保存在内存，不写入任何浏览器持久化存储。
    accessToken.value = session.accessToken
    user.value = session.user
  }

  const clearSession = () => {
    accessToken.value = null
    user.value = null
  }

  const performRefresh = async () => {
    try {
      const response = await authClient.post<ApiEnvelope<AuthSessionData>>('/auth/refresh')
      applySession(response.data.data)
      return true
    } catch {
      clearSession()
      return false
    } finally {
      initialized.value = true
    }
  }

  const refresh = () => {
    if (!refreshPromise) {
      refreshPromise = performRefresh().finally(() => {
        refreshPromise = null
      })
    }
    return refreshPromise
  }

  const initialize = async () => {
    if (initialized.value) return isAuthenticated.value
    return refresh()
  }

  const login = async (payload: LoginPayload) => {
    const response = await authClient.post<ApiEnvelope<AuthSessionData>>('/auth/login', payload)
    applySession(response.data.data)
    initialized.value = true
  }

  const register = async (payload: RegisterPayload) => {
    const response = await authClient.post<ApiEnvelope<AuthSessionData>>('/auth/register', payload)
    applySession(response.data.data)
    initialized.value = true
  }

  const loadCurrentUser = async () => {
    // 个人信息以受保护的 auth/me 接口为准，同时验证当前访问令牌仍然有效。
    const response = await apiClient.get<ApiEnvelope<UserData>>('/auth/me')
    user.value = response.data.data
    return response.data.data
  }

  const logout = async () => {
    try {
      await authClient.post('/auth/logout')
    } catch {
      // 即使后端暂时不可达，也要完成本地退出。
    } finally {
      clearSession()
      initialized.value = true
    }
  }

  return {
    accessToken,
    user,
    initialized,
    isAuthenticated,
    initialize,
    refresh,
    login,
    register,
    loadCurrentUser,
    logout,
    clearSession,
  }
})

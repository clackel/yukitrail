import type { AxiosAdapter, InternalAxiosRequestConfig } from 'axios'
import { createPinia, setActivePinia } from 'pinia'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Router } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import type { ApiEnvelope, AuthSessionData } from '@/types/api'
import { apiClient, authClient } from './httpClients'
import { installApiInterceptors } from './installApiInterceptors'

type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean }

const session: ApiEnvelope<AuthSessionData> = {
  code: 'OK',
  message: '请求成功',
  data: {
    accessToken: 'fresh-access-token',
    tokenType: 'Bearer',
    expiresIn: 900,
    user: { id: 7, email: 'traveler@example.com', nickname: '雪路' },
  },
  traceId: 'trace-id',
}

describe('API authentication interceptors', () => {
  const originalApiAdapter = apiClient.defaults.adapter
  const originalAuthAdapter = authClient.defaults.adapter
  let uninstall: (() => void) | undefined

  afterEach(() => {
    uninstall?.()
    uninstall = undefined
    apiClient.defaults.adapter = originalApiAdapter
    authClient.defaults.adapter = originalAuthAdapter
  })

  it('shares one refresh and retries concurrent 401 requests with the new token', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const auth = useAuthStore(pinia)
    auth.$patch({
      accessToken: 'expired-access-token',
      user: session.data.user,
      initialized: true,
    })

    let refreshCalls = 0
    const authAdapter: AxiosAdapter = async (config) => {
      refreshCalls += 1
      return { data: session, status: 200, statusText: 'OK', headers: {}, config }
    }
    authClient.defaults.adapter = authAdapter

    let apiCalls = 0
    const retryTokens: string[] = []
    const apiAdapter: AxiosAdapter = async (config) => {
      apiCalls += 1
      const request = config as RetriableConfig
      if (!request._retry) {
        return Promise.reject({
          config,
          response: { status: 401 },
          isAxiosError: true,
        })
      }

      retryTokens.push(String(config.headers.Authorization))
      return {
        data: { ok: true },
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      }
    }
    apiClient.defaults.adapter = apiAdapter

    const replace = vi.fn()
    const router = {
      currentRoute: { value: { name: 'home', fullPath: '/' } },
      replace,
    } as unknown as Router
    uninstall = installApiInterceptors(pinia, router)

    const responses = await Promise.all([
      apiClient.get('/trips'),
      apiClient.get('/trips?view=compact'),
    ])

    expect(responses.map((response) => response.status)).toEqual([200, 200])
    expect(refreshCalls).toBe(1)
    expect(apiCalls).toBe(4)
    expect(retryTokens).toEqual(['Bearer fresh-access-token', 'Bearer fresh-access-token'])
    expect(replace).not.toHaveBeenCalled()
  })
})

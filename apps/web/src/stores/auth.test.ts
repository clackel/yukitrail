import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient, authClient } from '@/services/httpClients'
import type { ApiEnvelope, AuthSessionData, UserData } from '@/types/api'
import { useAuthStore } from './auth'

vi.mock('@/services/httpClients', () => ({
  authClient: {
    post: vi.fn(),
  },
  apiClient: {
    get: vi.fn(),
  },
}))

const session: ApiEnvelope<AuthSessionData> = {
  code: 'OK',
  message: '请求成功',
  data: {
    accessToken: 'access-token',
    tokenType: 'Bearer',
    expiresIn: 900,
    user: { id: 7, email: 'traveler@example.com', nickname: '雪路' },
  },
  traceId: 'trace-id',
}

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(authClient.post).mockReset()
    vi.mocked(apiClient.get).mockReset()
    localStorage.clear()
    sessionStorage.clear()
  })

  it('registers and keeps the access token only in memory', async () => {
    vi.mocked(authClient.post).mockResolvedValue({ data: session })
    const store = useAuthStore()

    await store.register({
      email: 'traveler@example.com',
      password: 'correct-horse',
      nickname: '雪路',
    })

    expect(store.accessToken).toBe('access-token')
    expect(store.user?.nickname).toBe('雪路')
    expect(localStorage.length).toBe(0)
    expect(sessionStorage.length).toBe(0)
  })

  it('shares one refresh request between concurrent callers', async () => {
    let resolveRefresh: ((value: { data: ApiEnvelope<AuthSessionData> }) => void) | undefined
    vi.mocked(authClient.post).mockReturnValue(new Promise((resolve) => {
      resolveRefresh = resolve
    }))
    const store = useAuthStore()

    const first = store.refresh()
    const second = store.refresh()
    expect(authClient.post).toHaveBeenCalledTimes(1)

    resolveRefresh?.({ data: session })
    await expect(Promise.all([first, second])).resolves.toEqual([true, true])
    expect(store.accessToken).toBe('access-token')
  })

  it('loads the protected current-user profile into the session', async () => {
    const userEnvelope: ApiEnvelope<UserData> = {
      code: 'OK',
      message: '请求成功',
      data: session.data.user,
      traceId: 'trace-id',
    }
    vi.mocked(apiClient.get).mockResolvedValue({ data: userEnvelope })
    const store = useAuthStore()

    await expect(store.loadCurrentUser()).resolves.toEqual(session.data.user)

    expect(apiClient.get).toHaveBeenCalledWith('/auth/me')
    expect(store.user?.email).toBe('traveler@example.com')
  })

  it('clears memory even when the logout request fails', async () => {
    vi.mocked(authClient.post)
      .mockResolvedValueOnce({ data: session })
      .mockRejectedValueOnce(new Error('offline'))
    const store = useAuthStore()
    await store.login({ email: 'traveler@example.com', password: 'correct-horse' })

    await expect(store.logout()).resolves.toBeUndefined()
    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
  })
})

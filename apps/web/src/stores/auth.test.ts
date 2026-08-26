import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { authClient } from '@/services/httpClients'
import type { ApiEnvelope, AuthSessionData } from '@/types/api'
import { useAuthStore } from './auth'

vi.mock('@/services/httpClients', () => ({
  authClient: {
    post: vi.fn(),
  },
}))

const session: ApiEnvelope<AuthSessionData> = {
  code: 'OK',
<<<<<<< HEAD
  message: '请求成功',
=======
  message: 'success',
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
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

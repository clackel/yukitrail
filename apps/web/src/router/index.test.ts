import { createPinia, setActivePinia } from 'pinia'
import { describe, expect, it, vi } from 'vitest'

import { authClient } from '@/services/httpClients'
import router, { installAuthGuards } from './index'

vi.mock('@/services/httpClients', () => ({
  authClient: {
    post: vi.fn(),
  },
  apiClient: {},
}))

describe('auth route guards', () => {
  it('redirects an anonymous visitor to login with the requested path', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    vi.mocked(authClient.post).mockReset()
    vi.mocked(authClient.post).mockRejectedValue(new Error('no refresh cookie'))
    await router.replace('/login')
    installAuthGuards(pinia)

    await router.push('/')

    expect(router.currentRoute.value.name).toBe('login')
    expect(router.currentRoute.value.query.redirect).toBe('/')
  })
})

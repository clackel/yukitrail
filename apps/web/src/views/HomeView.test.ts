import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient } from '@/services/apiClient'
import { useAuthStore } from '@/stores/auth'
import HomeView from './HomeView.vue'

const replace = vi.fn()
const logout = vi.fn()

vi.mock('@/services/apiClient', () => ({
  apiClient: {
    get: vi.fn(),
  },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ replace }),
}))

describe('HomeView', () => {
  beforeEach(() => {
    vi.mocked(apiClient.get).mockReset()
    replace.mockReset()
    logout.mockReset()
    vi.mocked(useAuthStore).mockReturnValue({
      user: { id: 1, email: 'traveler@example.com', nickname: '雪路' },
      logout,
    } as unknown as ReturnType<typeof useAuthStore>)
  })

  it('shows the project identity and a healthy API state', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        code: 'OK',
        message: '请求成功',
        data: { service: 'yukitrail-api', status: 'UP' },
        traceId: 'test-trace-id',
      },
    })

    const wrapper = mount(HomeView, {
      global: { stubs: { RouterLink: { template: '<a><slot /></a>' } } },
    })
    await flushPromises()

    expect(wrapper.get('h1').text()).toContain('把旅程变成')
    expect(wrapper.text()).toContain('traveler@example.com')
    expect(wrapper.text()).toContain('雪路')
    expect(wrapper.text()).toContain('API 已连接')
    expect(apiClient.get).toHaveBeenCalledWith('/health')
  })

  it('keeps the page usable when the API is unavailable', async () => {
    vi.mocked(apiClient.get).mockRejectedValue(new Error('offline'))

    const wrapper = mount(HomeView, {
      global: { stubs: { RouterLink: { template: '<a><slot /></a>' } } },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('API 尚未连接')
    expect(wrapper.findAll('button').some((button) => button.text() === '重新检查')).toBe(true)
  })
})

import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient } from '@/services/apiClient'
import HomeView from './HomeView.vue'

vi.mock('@/services/apiClient', () => ({
  apiClient: {
    get: vi.fn(),
  },
}))

describe('HomeView', () => {
  beforeEach(() => {
    vi.mocked(apiClient.get).mockReset()
  })

  it('shows the project identity and a healthy API state', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        code: 'OK',
        message: 'success',
        data: { service: 'yukitrail-api', status: 'UP' },
        traceId: 'test-trace-id',
      },
    })

    const wrapper = mount(HomeView)
    await flushPromises()

    expect(wrapper.get('h1').text()).toContain('把旅程变成')
    expect(wrapper.text()).toContain('API 已连接')
    expect(apiClient.get).toHaveBeenCalledWith('/health')
  })

  it('keeps the page usable when the API is unavailable', async () => {
    vi.mocked(apiClient.get).mockRejectedValue(new Error('offline'))

    const wrapper = mount(HomeView)
    await flushPromises()

    expect(wrapper.text()).toContain('API 尚未连接')
    expect(wrapper.get('button').text()).toBe('重新检查')
  })
})


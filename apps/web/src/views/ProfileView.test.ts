import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/stores/auth'
import ProfileView from './ProfileView.vue'

const loadCurrentUser = vi.fn()
const logout = vi.fn()
const replace = vi.fn()
const user = { id: 7, email: 'traveler@example.com', nickname: '雪路' }

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ replace }),
}))

const mountProfile = () => mount(ProfileView, {
  global: {
    plugins: [ElementPlus],
    stubs: {
      RouterLink: { template: '<a><slot /></a>' },
    },
  },
})

describe('ProfileView', () => {
  beforeEach(() => {
    loadCurrentUser.mockReset()
    logout.mockReset()
    replace.mockReset()
    vi.mocked(useAuthStore).mockReturnValue({
      user,
      loadCurrentUser,
      logout,
    } as unknown as ReturnType<typeof useAuthStore>)
  })

  it('loads and displays the current user information', async () => {
    loadCurrentUser.mockResolvedValue(user)

    const wrapper = mountProfile()
    await flushPromises()

    expect(loadCurrentUser).toHaveBeenCalledOnce()
    expect(wrapper.get('h1').text()).toBe('个人信息')
    expect(wrapper.text()).toContain('雪路')
    expect(wrapper.text()).toContain('traveler@example.com')
    expect(wrapper.text()).toContain('7')
    expect(wrapper.text()).toContain('登录会话有效')
  })

  it('finishes local logout and returns to the login page', async () => {
    loadCurrentUser.mockResolvedValue(user)
    logout.mockResolvedValue(undefined)
    const wrapper = mountProfile()
    await flushPromises()

    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(logout).toHaveBeenCalledOnce()
    expect(replace).toHaveBeenCalledWith('/login')
  })
})

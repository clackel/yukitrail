import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/stores/auth'
import LoginView from './LoginView.vue'
import RegisterView from './RegisterView.vue'

const login = vi.fn()
const register = vi.fn()
const replace = vi.fn()
const route = { query: {} as Record<string, string> }

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ replace }),
}))

const mountAuthView = (component: typeof LoginView | typeof RegisterView) => mount(component, {
  global: {
    plugins: [ElementPlus],
    stubs: {
      RouterLink: { template: '<a><slot /></a>' },
    },
  },
})

describe('authentication forms', () => {
  beforeEach(() => {
    login.mockReset()
    register.mockReset()
    replace.mockReset()
    route.query = {}
    vi.mocked(useAuthStore).mockReturnValue({ login, register } as unknown as ReturnType<typeof useAuthStore>)
  })

  it('submits a normalized login and follows the protected redirect', async () => {
    route.query = { redirect: '/?from=login' }
    login.mockResolvedValue(undefined)
    const wrapper = mountAuthView(LoginView)
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue(' traveler@example.com ')
    await inputs[1].setValue('correct-horse')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(login).toHaveBeenCalledWith({
      email: 'traveler@example.com',
      password: 'correct-horse',
    })
    expect(replace).toHaveBeenCalledWith('/?from=login')
  })

  it('submits registration and enters the protected home page', async () => {
    register.mockResolvedValue(undefined)
    const wrapper = mountAuthView(RegisterView)
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue(' 雪路 ')
    await inputs[1].setValue(' traveler@example.com ')
    await inputs[2].setValue('correct-horse')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(register).toHaveBeenCalledWith({
      nickname: '雪路',
      email: 'traveler@example.com',
      password: 'correct-horse',
    })
    expect(replace).toHaveBeenCalledWith('/')
  })
})

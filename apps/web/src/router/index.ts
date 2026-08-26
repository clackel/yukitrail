import { createRouter, createWebHistory } from 'vue-router'
import type { Pinia } from 'pinia'

import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true },
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

export const installAuthGuards = (pinia: Pinia) => {
  router.beforeEach(async (to) => {
    const auth = useAuthStore(pinia)
    // 首次导航先尝试通过 HttpOnly Cookie 恢复登录状态。
    await auth.initialize()

    if (to.meta.requiresAuth && !auth.isAuthenticated) {
      return {
        name: 'login',
        query: { redirect: to.fullPath },
      }
    }

    if (to.meta.guestOnly && auth.isAuthenticated) {
      return { name: 'home' }
    }

    return true
  })
}

export default router

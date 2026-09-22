<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(true)
const loadError = ref('')

const avatarText = computed(() => auth.user?.nickname.trim().charAt(0).toUpperCase() || '旅')

const loadProfile = async () => {
  loadError.value = ''
  loading.value = true
  try {
    await auth.loadCurrentUser()
  } catch {
    loadError.value = '暂时无法加载最新个人信息，请稍后重试。'
  } finally {
    loading.value = false
  }
}

const logout = async () => {
  await auth.logout()
  await router.replace('/login')
}

onMounted(loadProfile)
</script>

<template>
  <main class="site-shell profile-shell">
    <nav class="topbar" aria-label="个人中心导航">
      <router-link class="brand" to="/" aria-label="YukiTrail 首页">
        <span class="brand-mark" aria-hidden="true">Y</span>
        <span>YukiTrail</span>
      </router-link>
      <div class="profile-actions">
        <router-link to="/">返回首页</router-link>
        <button type="button" @click="logout">退出登录</button>
      </div>
    </nav>

    <section class="profile-page" aria-labelledby="profile-title">
      <header class="profile-heading">
        <p class="eyebrow">个人中心</p>
        <h1 id="profile-title">个人信息</h1>
        <p>这里展示当前登录账户的信息，数据来自受保护的用户接口。</p>
      </header>

      <el-alert
        v-if="loadError"
        :title="loadError"
        type="error"
        :closable="false"
        show-icon
      >
        <template #default>
          <button class="profile-retry" type="button" @click="loadProfile">重新加载</button>
        </template>
      </el-alert>

      <section v-if="auth.user" class="profile-card" aria-label="当前用户资料">
        <div class="profile-identity">
          <span class="profile-avatar" aria-hidden="true">{{ avatarText }}</span>
          <div>
            <p>当前登录用户</p>
            <h2>{{ auth.user.nickname }}</h2>
          </div>
          <span class="account-status">账户正常</span>
        </div>

        <dl class="profile-details">
          <div>
            <dt>昵称</dt>
            <dd>{{ auth.user.nickname }}</dd>
          </div>
          <div>
            <dt>邮箱</dt>
            <dd>{{ auth.user.email }}</dd>
          </div>
          <div>
            <dt>用户编号</dt>
            <dd>{{ auth.user.id }}</dd>
          </div>
        </dl>

        <footer class="profile-session">
          <span class="status-dot" aria-hidden="true"></span>
          <div>
            <strong>登录会话有效</strong>
            <small>访问令牌仅保存在当前页面内存中</small>
          </div>
        </footer>
      </section>

      <p v-else-if="loading" class="profile-loading" role="status">正在加载个人信息…</p>
    </section>
  </main>
</template>

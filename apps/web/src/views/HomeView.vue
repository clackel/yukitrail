<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { apiClient } from '@/services/apiClient'
import { useAuthStore } from '@/stores/auth'
import type { ApiEnvelope, HealthData } from '@/types/api'

type ApiState = 'checking' | 'ready' | 'unavailable'

const apiState = ref<ApiState>('checking')
const auth = useAuthStore()
const router = useRouter()

const statusLabel = computed(() => {
  if (apiState.value === 'ready') return 'API 已连接'
  if (apiState.value === 'unavailable') return 'API 尚未连接'
  return '正在检查 API'
})

const checkApi = async () => {
  // 健康检查只影响状态卡片，不阻塞首页其余内容。
  apiState.value = 'checking'

  try {
    const response = await apiClient.get<ApiEnvelope<HealthData>>('/health')
    apiState.value = response.data.data.status === 'UP' ? 'ready' : 'unavailable'
  } catch {
    apiState.value = 'unavailable'
  }
}

const logout = async () => {
  await auth.logout()
  await router.replace('/login')
}

onMounted(checkApi)
</script>

<template>
  <main class="site-shell">
    <nav class="topbar" aria-label="主导航">
      <a class="brand" href="/" aria-label="YukiTrail 首页">
        <span class="brand-mark" aria-hidden="true">Y</span>
        <span>YukiTrail</span>
      </a>
      <div class="user-menu">
        <router-link class="user-profile-link" to="/profile" aria-label="查看个人信息">
          <strong>{{ auth.user?.nickname }}</strong>
          <small>{{ auth.user?.email }}</small>
        </router-link>
        <button type="button" @click="logout">退出</button>
      </div>
    </nav>

    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">规划旅程，留下足迹。</p>
        <h1>把旅程变成<br />清晰的每一天</h1>
        <p class="intro">
          从日期、地点到每日安排，YukiTrail 帮你把零散灵感整理成一份随时可以重新打开的旅行计划。
        </p>

        <div class="status-card" :data-state="apiState">
          <span class="status-dot" aria-hidden="true"></span>
          <div>
            <strong>{{ statusLabel }}</strong>
            <span>
              {{ apiState === 'ready' ? '前后端基础链路工作正常' : '页面仍可独立使用，启动后端后可重试' }}
            </span>
          </div>
          <button v-if="apiState === 'unavailable'" type="button" @click="checkApi">重新检查</button>
        </div>
      </div>

      <div class="route-preview" aria-label="旅行计划预览插画">
        <div class="sun"></div>
        <div class="mountain mountain-back"></div>
        <div class="mountain mountain-front"></div>
        <div class="trail-line"></div>
        <span class="map-pin pin-one" aria-hidden="true"><b>1</b></span>
        <span class="map-pin pin-two" aria-hidden="true"><b>2</b></span>
        <div class="preview-caption">
          <span>杭州 · 三日计划</span>
          <strong>下一站，清晰可见</strong>
        </div>
      </div>
    </section>

    <section class="foundations" aria-labelledby="foundation-title">
      <div>
        <p class="section-kicker">首版闭环</p>
        <h2 id="foundation-title">先把一次旅行规划完整做好</h2>
      </div>
      <ol class="feature-list">
        <li>
          <span>01</span>
          <div><strong>创建计划</strong><small>明确目的地与日期范围</small></div>
        </li>
        <li>
          <span>02</span>
          <div><strong>编排每天</strong><small>添加地点、时间和备注</small></div>
        </li>
        <li>
          <span>03</span>
          <div><strong>地图查看</strong><small>按顺序理解地点分布</small></div>
        </li>
      </ol>
    </section>
  </main>
</template>

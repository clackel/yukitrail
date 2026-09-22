<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'

import { presentApiError } from '@/services/apiErrors'
import { useAuthStore } from '@/stores/auth'

interface LoginForm {
  email: string
  password: string
}

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const serverError = ref('')
const fieldErrors = ref<Record<string, string>>({})
const form = reactive<LoginForm>({ email: '', password: '' })

const rules: FormRules<LoginForm> = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效邮箱', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 72, message: '密码长度应为 8–72 个字符', trigger: 'blur' },
  ],
}

const submit = async () => {
  serverError.value = ''
  fieldErrors.value = {}

  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await auth.login({ email: form.email.trim(), password: form.password })
    // 仅接受站内绝对路径，避免把登录成功用户跳转到外部站点。
    const redirect = typeof route.query.redirect === 'string'
      && route.query.redirect.startsWith('/')
      && !route.query.redirect.startsWith('//')
      ? route.query.redirect
      : '/'
    await router.replace(redirect)
  } catch (error) {
    const presented = presentApiError(error)
    serverError.value = presented.message
    fieldErrors.value = presented.fields
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="auth-shell">
    <section class="auth-story" aria-labelledby="login-story-title">
      <a class="brand auth-brand" href="/" aria-label="YukiTrail">
        <span class="brand-mark" aria-hidden="true">Y</span>
        <span>YukiTrail</span>
      </a>
      <div>
        <p class="eyebrow">欢迎回来</p>
        <h1 id="login-story-title">继续整理<br />你的下一段旅程</h1>
        <p class="intro">登录后，你的旅行计划会一直保存在自己的私有空间中。</p>
      </div>
      <div class="auth-landscape" aria-hidden="true">
        <span></span><span></span><span></span>
      </div>
    </section>

    <section class="auth-panel" aria-labelledby="login-title">
      <div class="auth-card">
        <p class="section-kicker">账户登录</p>
        <h2 id="login-title">回到 YukiTrail</h2>
        <p class="auth-hint">使用注册时的邮箱和密码登录。</p>

        <el-alert v-if="serverError" :title="serverError" type="error" :closable="false" show-icon />

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
          <el-form-item label="邮箱" prop="email" :error="fieldErrors.email">
            <el-input v-model="form.email" type="email" autocomplete="email" placeholder="name@example.com" />
          </el-form-item>
          <el-form-item label="密码" prop="password" :error="fieldErrors.password">
            <el-input
              v-model="form.password"
              type="password"
              autocomplete="current-password"
              placeholder="输入密码"
              show-password
            />
          </el-form-item>
          <el-button class="auth-submit" type="primary" native-type="submit" :loading="submitting">
            登录并继续
          </el-button>
        </el-form>

        <p class="auth-switch">还没有账户？<router-link to="/register">创建账户</router-link></p>
      </div>
    </section>
  </main>
</template>

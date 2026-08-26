<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'

import { presentApiError } from '@/services/apiErrors'
import { useAuthStore } from '@/stores/auth'

interface RegisterForm {
  nickname: string
  email: string
  password: string
}

const auth = useAuthStore()
const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const serverError = ref('')
const fieldErrors = ref<Record<string, string>>({})
const form = reactive<RegisterForm>({ nickname: '', email: '', password: '' })

const rules: FormRules<RegisterForm> = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 40, message: '昵称长度应为 2–40 个字符', trigger: 'blur' },
  ],
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
    await auth.register({
      nickname: form.nickname.trim(),
      email: form.email.trim(),
      password: form.password,
    })
    await router.replace('/')
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
    <section class="auth-story" aria-labelledby="register-story-title">
      <a class="brand auth-brand" href="/" aria-label="YukiTrail">
        <span class="brand-mark" aria-hidden="true">Y</span>
        <span>YukiTrail</span>
      </a>
      <div>
        <p class="eyebrow">开始规划</p>
        <h1 id="register-story-title">为旅程留出<br />一片清晰空间</h1>
        <p class="intro">创建账户后即可进入自己的私有规划空间。</p>
      </div>
      <div class="auth-landscape" aria-hidden="true">
        <span></span><span></span><span></span>
      </div>
    </section>

    <section class="auth-panel" aria-labelledby="register-title">
      <div class="auth-card">
        <p class="section-kicker">创建账户</p>
        <h2 id="register-title">加入 YukiTrail</h2>
        <p class="auth-hint">注册成功后会自动登录。</p>

        <el-alert v-if="serverError" :title="serverError" type="error" :closable="false" show-icon />

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
          <el-form-item label="昵称" prop="nickname" :error="fieldErrors.nickname">
            <el-input v-model="form.nickname" autocomplete="nickname" placeholder="怎么称呼你" maxlength="40" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email" :error="fieldErrors.email">
            <el-input v-model="form.email" type="email" autocomplete="email" placeholder="name@example.com" />
          </el-form-item>
          <el-form-item label="密码" prop="password" :error="fieldErrors.password">
            <el-input
              v-model="form.password"
              type="password"
              autocomplete="new-password"
              placeholder="至少 8 个字符"
              show-password
              @keyup.enter="submit"
            />
          </el-form-item>
          <el-button class="auth-submit" type="primary" native-type="submit" :loading="submitting">
            创建并登录
          </el-button>
        </el-form>

        <p class="auth-switch">已有账户？<router-link to="/login">返回登录</router-link></p>
      </div>
    </section>
  </main>
</template>

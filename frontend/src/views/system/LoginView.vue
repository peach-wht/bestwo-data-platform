<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

import { useUserStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    await userStore.loginWithPassword(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect =
      typeof route.query.redirect === 'string' &&
      route.query.redirect.startsWith('/') &&
      !route.query.redirect.startsWith('//')
        ? route.query.redirect
        : '/dashboard'
    await router.push(redirect)
  } catch (error) {
    const message = axios.isAxiosError(error)
      ? (error.response?.data?.message as string | undefined) || '登录失败'
      : '登录失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <div class="login-heading">
        <h1>Bestwo 管理后台</h1>
        <p>微信订单数据接入与数仓分析平台</p>
      </div>

      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-alert
          class="login-tip"
          type="info"
          :closable="false"
          title="初始管理员账号：admin / Bestwo@2026!"
        />
        <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

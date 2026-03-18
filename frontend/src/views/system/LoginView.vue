<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: 'admin',
  password: ''
})

const handleLogin = () => {
  userStore.setUser('demo-token', form.username || 'admin')
  ElMessage.success('已进入管理后台骨架')
  router.push('/dashboard')
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
        <el-button type="primary" class="login-button" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

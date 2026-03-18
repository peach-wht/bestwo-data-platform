<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="status-page">
    <el-card class="status-card">
      <h1>404</h1>
      <p>页面不存在，请检查访问路径。</p>
      <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
    </el-card>
  </div>
</template>

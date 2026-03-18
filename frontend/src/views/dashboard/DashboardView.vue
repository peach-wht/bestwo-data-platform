<template>
  <PageContainer title="仪表盘" description="查看平台整体运行概览与关键指标占位信息。">
    <div class="dashboard-grid">
      <el-card shadow="hover">
        <template #header>订单总数</template>
        <div class="metric-value">12,680</div>
        <div class="metric-note">微信订单接入总量占位</div>
      </el-card>

      <el-card shadow="hover">
        <template #header>今日支付金额</template>
        <div class="metric-value">¥ 86,420</div>
        <div class="metric-note">支付金额统计占位</div>
      </el-card>

      <el-card shadow="hover">
        <template #header>任务运行数</template>
        <div class="metric-value">18</div>
        <div class="metric-note">调度任务执行数量占位</div>
      </el-card>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
</script>

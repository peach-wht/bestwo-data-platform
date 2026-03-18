<template>
  <PageContainer title="数仓管理" description="用于后续接入 Doris、指标模型与分析查询能力。">
    <el-card>
      <p>当前仅提供数仓管理页面占位，后续可扩展主题域、报表与数据集成配置。</p>
    </el-card>
    <el-card class="placeholder-card">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="数据源">Doris / PostgreSQL</el-descriptions-item>
        <el-descriptions-item label="分析场景">订单明细、支付汇总、任务监控</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
</script>

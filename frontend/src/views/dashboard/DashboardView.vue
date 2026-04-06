<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import PageContainer from '@/components/PageContainer.vue'
import {
  getAlertRecords,
  getJobLogs,
  getPayOverview,
  getPayTrend,
  type AlertRecordItem,
  type JobExecutionLogItem,
  type PayOverviewItem,
  type PayTrendItem
} from '@/api/warehouse-analysis'
import { ensureApiSuccess, formatAmount, formatCount, formatDateTime, formatPercent, resolveErrorMessage } from '@/utils/ui'

const loading = ref(false)
const errorMessage = ref('')
const overview = ref<PayOverviewItem | null>(null)
const trendList = ref<PayTrendItem[]>([])
const jobLogs = ref<JobExecutionLogItem[]>([])
const alerts = ref<AlertRecordItem[]>([])

const loadDashboard = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const { startDate, endDate } = getDefaultDateRange()
    const [overviewResponse, trendResponse, logResponse, alertResponse] = await Promise.all([
      getPayOverview({ startDate, endDate }),
      getPayTrend({ startDate, endDate }),
      getJobLogs({ limit: 6 }),
      getAlertRecords({ alertStatus: 'OPEN', limit: 6 })
    ])

    overview.value = ensureApiSuccess(overviewResponse, '加载支付概览失败')
    trendList.value = ensureApiSuccess(trendResponse, '加载支付趋势失败') ?? []
    jobLogs.value = ensureApiSuccess(logResponse, '加载任务日志失败') ?? []
    alerts.value = ensureApiSuccess(alertResponse, '加载告警记录失败') ?? []
  } catch (error) {
    const message = resolveErrorMessage(error, '仪表盘加载失败，请检查网关和数仓服务状态')
    errorMessage.value = message
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const getStatusTagType = (status?: string | null) => {
  switch (status) {
    case 'SUCCESS':
    case 'PASSED':
      return 'success'
    case 'FAILED':
    case 'ERROR':
      return 'danger'
    case 'RUNNING':
    case 'PROCESSING':
      return 'warning'
    default:
      return 'info'
  }
}

const getAlertTagType = (level?: string | null) => {
  switch (level) {
    case 'HIGH':
      return 'danger'
    case 'MEDIUM':
      return 'warning'
    default:
      return 'info'
  }
}

function getDefaultDateRange() {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 6)
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate)
  }
}

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(async () => {
  await loadDashboard()
})
</script>

<template>
  <PageContainer title="仪表盘" description="展示微信订单支付概览、趋势、最近任务日志和治理告警。">
    <div class="dashboard-page">
      <div class="page-actions">
        <el-button type="primary" :loading="loading" @click="loadDashboard">刷新数据</el-button>
      </div>

      <el-alert
        v-if="errorMessage"
        :closable="false"
        :title="errorMessage"
        type="error"
        show-icon
      />

      <div class="metric-grid">
        <el-card shadow="hover">
          <template #header>最近 7 天订单数</template>
          <div class="metric-value">{{ formatCount(overview?.order_count) }}</div>
          <div class="metric-note">来自 ADS/DWS 聚合结果</div>
        </el-card>

        <el-card shadow="hover">
          <template #header>最近 7 天支付金额</template>
          <div class="metric-value">{{ formatAmount(overview?.paid_amount) }}</div>
          <div class="metric-note">支付成功金额汇总</div>
        </el-card>

        <el-card shadow="hover">
          <template #header>最近 7 天退款金额</template>
          <div class="metric-value">{{ formatAmount(overview?.refund_amount) }}</div>
          <div class="metric-note">退款金额趋势可用于观察异常波动</div>
        </el-card>

        <el-card shadow="hover">
          <template #header>支付成功率</template>
          <div class="metric-value">{{ formatPercent(overview?.pay_success_rate) }}</div>
          <div class="metric-note">最新统计日期：{{ overview?.latest_stat_date || '--' }}</div>
        </el-card>
      </div>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>支付趋势</h3>
            <span>最近 7 天</span>
          </div>
        </template>

        <el-table v-loading="loading" :data="trendList" border stripe empty-text="暂无支付趋势数据">
          <el-table-column prop="stat_date" label="统计日期" min-width="120" />
          <el-table-column prop="order_count" label="订单数" min-width="110">
            <template #default="{ row }">
              {{ formatCount(row.order_count) }}
            </template>
          </el-table-column>
          <el-table-column prop="paid_order_count" label="已支付订单" min-width="130">
            <template #default="{ row }">
              {{ formatCount(row.paid_order_count) }}
            </template>
          </el-table-column>
          <el-table-column prop="paid_amount" label="支付金额" min-width="140">
            <template #default="{ row }">
              {{ formatAmount(row.paid_amount) }}
            </template>
          </el-table-column>
          <el-table-column prop="refund_amount" label="退款金额" min-width="140">
            <template #default="{ row }">
              {{ formatAmount(row.refund_amount) }}
            </template>
          </el-table-column>
          <el-table-column prop="pay_success_rate" label="成功率" min-width="120">
            <template #default="{ row }">
              {{ formatPercent(row.pay_success_rate) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <div class="dashboard-bottom">
        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>最近任务</h3>
              <span>来自 dw_job_log</span>
            </div>
          </template>

          <el-table v-loading="loading" :data="jobLogs" border stripe empty-text="暂无任务日志">
            <el-table-column prop="job_name" label="任务名称" min-width="180" />
            <el-table-column prop="run_status" label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.run_status)">{{ row.run_status || '--' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="started_at" label="开始时间" min-width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.started_at) }}
              </template>
            </el-table-column>
            <el-table-column prop="message" label="结果说明" min-width="260" show-overflow-tooltip />
          </el-table>
        </el-card>

        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>开放告警</h3>
              <span>来自 dw_alert_record</span>
            </div>
          </template>

          <el-table v-loading="loading" :data="alerts" border stripe empty-text="当前没有开放告警">
            <el-table-column prop="alert_level" label="级别" min-width="100">
              <template #default="{ row }">
                <el-tag :type="getAlertTagType(row.alert_level)">{{ row.alert_level || '--' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alert_title" label="告警标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="source_name" label="来源" min-width="150" show-overflow-tooltip />
            <el-table-column prop="fired_at" label="触发时间" min-width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.fired_at) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </PageContainer>
</template>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 16px;
}

.page-actions {
  display: flex;
  justify-content: flex-end;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-value {
  font-size: 30px;
  font-weight: 700;
  color: #0f172a;
}

.metric-note {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
}

.section-header span {
  color: #64748b;
  font-size: 13px;
}

.dashboard-bottom {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1200px) {
  .metric-grid,
  .dashboard-bottom {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .metric-grid,
  .dashboard-bottom {
    grid-template-columns: 1fr;
  }

  .page-actions {
    justify-content: stretch;
  }

  .page-actions .el-button {
    width: 100%;
  }
}
</style>

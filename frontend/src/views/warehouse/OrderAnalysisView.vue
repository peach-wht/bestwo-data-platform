<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import PageContainer from '@/components/PageContainer.vue'
import {
  getAlertRecords,
  getLineageRelations,
  getOrderDaySummary,
  getOrderPage,
  getPayOverview,
  getPayTrend,
  type AlertRecordItem,
  type LineageRelationItem,
  type OrderDaySummaryItem,
  type OrderPageData,
  type OrderQueryParams,
  type PayOverviewItem,
  type PayTrendItem
} from '@/api/warehouse-analysis'
import { ensureApiSuccess, formatAmount, formatCount, formatDateTime, formatPercent, resolveErrorMessage } from '@/utils/ui'

type DateRangeValue = [string, string]

const form = reactive({
  dateRange: getDefaultDateRange(),
  keyword: ''
})

const overviewLoading = ref(false)
const trendLoading = ref(false)
const summaryLoading = ref(false)
const ordersLoading = ref(false)
const governanceLoading = ref(false)

const analysisError = ref('')
const governanceError = ref('')

const overview = ref<PayOverviewItem | null>(null)
const trendList = ref<PayTrendItem[]>([])
const summaryList = ref<OrderDaySummaryItem[]>([])
const orderPage = ref<OrderPageData>({
  list: [],
  pageNum: 1,
  pageSize: 20,
  total: 0
})
const lineageList = ref<LineageRelationItem[]>([])
const alertList = ref<AlertRecordItem[]>([])

const searchLoading = computed(
  () => overviewLoading.value || trendLoading.value || summaryLoading.value || ordersLoading.value
)

const handleSearch = async () => {
  if (!isDateRangeValid(form.dateRange)) {
    analysisError.value = '请选择日期范围'
    ElMessage.warning('请选择日期范围')
    return
  }

  analysisError.value = ''
  orderPage.value.pageNum = 1

  try {
    await Promise.all([
      loadOverviewAndTrend(),
      loadSummary(),
      loadOrders(1, orderPage.value.pageSize),
      loadGovernance()
    ])
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '订单分析加载失败'))
  }
}

const handleReset = async () => {
  form.dateRange = getDefaultDateRange()
  form.keyword = ''
  analysisError.value = ''
  governanceError.value = ''
  orderPage.value = {
    list: [],
    pageNum: 1,
    pageSize: 20,
    total: 0
  }
  await handleSearch()
}

const handleCurrentChange = async (pageNum: number) => {
  try {
    await loadOrders(pageNum, orderPage.value.pageSize)
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '加载订单明细失败'))
  }
}

const handleSizeChange = async (pageSize: number) => {
  try {
    await loadOrders(1, pageSize)
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '加载订单明细失败'))
  }
}

const loadOverviewAndTrend = async () => {
  overviewLoading.value = true
  trendLoading.value = true

  try {
    const params = buildBaseParams()
    const [overviewResponse, trendResponse] = await Promise.all([
      getPayOverview(params),
      getPayTrend(params)
    ])
    overview.value = ensureApiSuccess(overviewResponse, '加载支付概览失败')
    trendList.value = ensureApiSuccess(trendResponse, '加载支付趋势失败') ?? []
  } catch (error) {
    analysisError.value = resolveErrorMessage(error, '加载支付分析失败')
    throw error
  } finally {
    overviewLoading.value = false
    trendLoading.value = false
  }
}

const loadSummary = async () => {
  summaryLoading.value = true

  try {
    const response = await getOrderDaySummary(buildBaseParams())
    summaryList.value = ensureApiSuccess(response, '加载日汇总失败') ?? []
  } catch (error) {
    analysisError.value = resolveErrorMessage(error, '加载日汇总失败')
    summaryList.value = []
    throw error
  } finally {
    summaryLoading.value = false
  }
}

const loadOrders = async (pageNum: number, pageSize: number) => {
  ordersLoading.value = true

  try {
    const params: OrderQueryParams = {
      ...buildBaseParams(),
      pageNum,
      pageSize
    }
    const response = await getOrderPage(params)
    orderPage.value = ensureApiSuccess(response, '加载订单明细失败') ?? {
      list: [],
      pageNum,
      pageSize,
      total: 0
    }
  } catch (error) {
    analysisError.value = resolveErrorMessage(error, '加载订单明细失败')
    orderPage.value = { list: [], pageNum, pageSize, total: 0 }
    throw error
  } finally {
    ordersLoading.value = false
  }
}

const loadGovernance = async () => {
  governanceLoading.value = true

  try {
    const [lineageResponse, alertResponse] = await Promise.all([
      getLineageRelations({ tableCode: 'dwd_wx_order_detail', limit: 20 }),
      getAlertRecords({ alertStatus: 'OPEN', limit: 10 })
    ])
    lineageList.value = ensureApiSuccess(lineageResponse, '加载血缘关系失败') ?? []
    alertList.value = ensureApiSuccess(alertResponse, '加载告警记录失败') ?? []
  } catch (error) {
    governanceError.value = resolveErrorMessage(error, '加载治理视图失败')
  } finally {
    governanceLoading.value = false
  }
}

const buildBaseParams = () => {
  const [startDate, endDate] = form.dateRange
  const keyword = form.keyword.trim()

  return {
    startDate,
    endDate,
    keyword: keyword || undefined
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

const isDateRangeValid = (dateRange: string[]) => dateRange.length === 2 && !!dateRange[0] && !!dateRange[1]

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getDefaultDateRange(): DateRangeValue {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 6)
  return [formatDate(startDate), formatDate(endDate)]
}

onMounted(async () => {
  await handleSearch()
})
</script>

<template>
  <PageContainer title="订单分析" description="展示支付概览、趋势、订单明细，以及当前数仓链路的最小血缘与告警视图。">
    <div class="analysis-page">
      <el-card shadow="hover">
        <el-form label-width="88px">
          <div class="analysis-form-grid">
            <el-form-item label="日期范围" required>
              <el-date-picker
                v-model="form.dateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                class="date-range-picker"
              />
            </el-form-item>
            <el-form-item label="订单号">
              <el-input
                v-model="form.keyword"
                clearable
                placeholder="输入订单号关键字"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item class="analysis-actions">
              <el-button type="primary" :loading="searchLoading" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-card>

      <el-alert
        v-if="analysisError"
        :closable="false"
        :title="analysisError"
        type="error"
        show-icon
      />

      <div class="metric-grid">
        <el-card shadow="hover">
          <template #header>区间订单数</template>
          <div class="metric-value">{{ formatCount(overview?.order_count) }}</div>
          <div class="metric-note">按查询日期范围汇总</div>
        </el-card>
        <el-card shadow="hover">
          <template #header>支付订单数</template>
          <div class="metric-value">{{ formatCount(overview?.paid_order_count) }}</div>
          <div class="metric-note">已支付订单总量</div>
        </el-card>
        <el-card shadow="hover">
          <template #header>支付金额</template>
          <div class="metric-value">{{ formatAmount(overview?.paid_amount) }}</div>
          <div class="metric-note">来自 DWS/ADS 聚合结果</div>
        </el-card>
        <el-card shadow="hover">
          <template #header>支付成功率</template>
          <div class="metric-value">{{ formatPercent(overview?.pay_success_rate) }}</div>
          <div class="metric-note">最近统计日：{{ overview?.latest_stat_date || '--' }}</div>
        </el-card>
      </div>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>支付趋势</h3>
            <span>来自 /api/warehouse/pay/trend</span>
          </div>
        </template>

        <el-table v-loading="trendLoading" :data="trendList" border stripe empty-text="暂无支付趋势数据">
          <el-table-column prop="stat_date" label="统计日期" min-width="120" />
          <el-table-column prop="order_count" label="订单数" min-width="100">
            <template #default="{ row }">
              {{ formatCount(row.order_count) }}
            </template>
          </el-table-column>
          <el-table-column prop="paid_order_count" label="支付订单数" min-width="120">
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

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>日汇总</h3>
            <span>来自 /api/warehouse/summary/day</span>
          </div>
        </template>

        <el-table v-loading="summaryLoading" :data="summaryList" border stripe empty-text="暂无日汇总数据">
          <el-table-column prop="stat_date" label="stat_date" min-width="120" />
          <el-table-column prop="order_count" label="order_count" min-width="120" />
          <el-table-column prop="paid_order_count" label="paid_order_count" min-width="140" />
          <el-table-column prop="total_amount" label="total_amount" min-width="140" />
          <el-table-column prop="paid_amount" label="paid_amount" min-width="140" />
          <el-table-column prop="refund_amount" label="refund_amount" min-width="140" />
        </el-table>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>订单明细</h3>
            <span>来自 /api/warehouse/orders</span>
          </div>
        </template>

        <el-table v-loading="ordersLoading" :data="orderPage.list" border stripe empty-text="暂无订单明细数据">
          <el-table-column prop="order_id" label="order_id" min-width="140" />
          <el-table-column prop="order_no" label="order_no" min-width="180" />
          <el-table-column prop="order_status" label="order_status" min-width="120" />
          <el-table-column prop="pay_status" label="pay_status" min-width="120" />
          <el-table-column prop="order_time" label="order_time" min-width="180" />
          <el-table-column prop="pay_time" label="pay_time" min-width="180" />
          <el-table-column prop="total_amount" label="total_amount" min-width="120" />
          <el-table-column prop="buyer_nickname" label="buyer_nickname" min-width="140" />
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="orderPage.pageNum"
            v-model:page-size="orderPage.pageSize"
            background
            layout="total, sizes, prev, pager, next"
            :page-sizes="[10, 20, 50, 100]"
            :total="orderPage.total"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </el-card>

      <el-alert
        v-if="governanceError"
        :closable="false"
        :title="governanceError"
        type="warning"
        show-icon
      />

      <div class="governance-grid">
        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>表级血缘</h3>
              <span>dwd_wx_order_detail 上下游</span>
            </div>
          </template>

          <el-table v-loading="governanceLoading" :data="lineageList" border stripe empty-text="暂无血缘关系">
            <el-table-column prop="upstream_table_code" label="上游表" min-width="180" />
            <el-table-column prop="downstream_table_code" label="下游表" min-width="180" />
            <el-table-column prop="transform_name" label="转换任务" min-width="180" />
          </el-table>
        </el-card>

        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>开放告警</h3>
              <span>来自 dw_alert_record</span>
            </div>
          </template>

          <el-table v-loading="governanceLoading" :data="alertList" border stripe empty-text="当前没有开放告警">
            <el-table-column prop="alert_level" label="级别" min-width="90">
              <template #default="{ row }">
                <el-tag :type="getAlertTagType(row.alert_level)">{{ row.alert_level || '--' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alert_title" label="告警标题" min-width="220" show-overflow-tooltip />
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
.analysis-page {
  display: grid;
  gap: 16px;
}

.analysis-form-grid {
  display: grid;
  grid-template-columns: minmax(280px, 2fr) minmax(220px, 1fr) auto;
  gap: 16px;
}

.analysis-actions {
  margin-left: auto;
}

.date-range-picker {
  width: 100%;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.metric-note {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.governance-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1200px) {
  .metric-grid,
  .governance-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .analysis-form-grid,
  .metric-grid,
  .governance-grid {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    justify-content: flex-start;
  }
}
</style>

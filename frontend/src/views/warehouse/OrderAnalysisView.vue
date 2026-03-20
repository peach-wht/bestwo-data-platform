<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import PageContainer from '@/components/PageContainer.vue'
import {
  getOrderDaySummary,
  getOrderPage,
  type ApiResponse,
  type OrderDaySummaryItem,
  type OrderPageData,
  type OrderQueryParams
} from '@/api/warehouse-analysis'

type DateRangeValue = [string, string]

const text = {
  title: '\u8ba2\u5355\u5206\u6790',
  description:
    '\u901a\u8fc7\u0020\u0067\u0061\u0074\u0065\u0077\u0061\u0079\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u67e5\u8be2\u0020\u0077\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u4e2d\u7684\u0020\u0044\u006f\u0072\u0069\u0073\u0020\u8ba2\u5355\u660e\u7ec6\u4e0e\u65e5\u6c47\u603b\u6570\u636e\u3002',
  summaryTitle: '\u65e5\u6c47\u603b',
  ordersTitle: '\u8ba2\u5355\u660e\u7ec6',
  dateRangeLabel: '\u65e5\u671f\u8303\u56f4',
  keywordLabel: '\u8ba2\u5355\u53f7',
  searchButton: '\u67e5\u8be2',
  resetButton: '\u91cd\u7f6e',
  dateRequired: '\u8bf7\u9009\u62e9\u65e5\u671f\u8303\u56f4',
  keywordPlaceholder: '\u8bf7\u8f93\u5165\u8ba2\u5355\u53f7\u5173\u952e\u5b57',
  startPlaceholder: '\u5f00\u59cb\u65e5\u671f',
  endPlaceholder: '\u7ed3\u675f\u65e5\u671f',
  rangeSeparator: '\u81f3',
  searchFailed:
    '\u67e5\u8be2\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u0020\u0067\u0061\u0074\u0065\u0077\u0061\u0079\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u548c\u0020\u0077\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u72b6\u6001',
  emptySummary: '\u6682\u65e0\u65e5\u6c47\u603b\u6570\u636e',
  emptyOrders: '\u6682\u65e0\u8ba2\u5355\u660e\u7ec6\u6570\u636e'
} as const

const form = reactive({
  dateRange: getDefaultDateRange(),
  keyword: ''
})

const summaryLoading = ref(false)
const ordersLoading = ref(false)
const summaryError = ref('')
const ordersError = ref('')

const summaryList = ref<OrderDaySummaryItem[]>([])
const orderPage = ref<OrderPageData>({
  list: [],
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const searchLoading = computed(() => summaryLoading.value || ordersLoading.value)

const handleSearch = async () => {
  if (!isDateRangeValid(form.dateRange)) {
    ElMessage.warning(text.dateRequired)
    summaryError.value = text.dateRequired
    ordersError.value = text.dateRequired
    return
  }

  summaryError.value = ''
  ordersError.value = ''
  orderPage.value.pageNum = 1

  try {
    await loadSummary()
    await loadOrders(1, orderPage.value.pageSize)
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  }
}

const handleReset = async () => {
  form.dateRange = getDefaultDateRange()
  form.keyword = ''
  summaryList.value = []
  summaryError.value = ''
  ordersError.value = ''
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
    ElMessage.error(resolveErrorMessage(error))
  }
}

const handleSizeChange = async (pageSize: number) => {
  try {
    await loadOrders(1, pageSize)
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error))
  }
}

const loadSummary = async () => {
  summaryLoading.value = true
  summaryError.value = ''

  try {
    const params = buildBaseParams()
    const response = await getOrderDaySummary(params)
    ensureSuccess(response)
    summaryList.value = response.data ?? []
  } catch (error) {
    summaryList.value = []
    summaryError.value = resolveErrorMessage(error)
    throw error
  } finally {
    summaryLoading.value = false
  }
}

const loadOrders = async (pageNum: number, pageSize: number) => {
  ordersLoading.value = true
  ordersError.value = ''

  try {
    const params: OrderQueryParams = {
      ...buildBaseParams(),
      pageNum,
      pageSize
    }
    const response = await getOrderPage(params)
    ensureSuccess(response)
    orderPage.value = response.data ?? {
      list: [],
      pageNum,
      pageSize,
      total: 0
    }
  } catch (error) {
    orderPage.value = {
      list: [],
      pageNum,
      pageSize,
      total: 0
    }
    ordersError.value = resolveErrorMessage(error)
    throw error
  } finally {
    ordersLoading.value = false
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

const ensureSuccess = <T>(response: ApiResponse<T>) => {
  if (response.code !== 0) {
    throw new Error(response.message || response.msg || text.searchFailed)
  }
}

const resolveErrorMessage = (error: unknown) => {
  if (typeof error === 'object' && error !== null) {
    if ('response' in error && error.response && typeof error.response === 'object') {
      const responseData = (error.response as { data?: ApiResponse<unknown> }).data
      if (responseData) {
        return responseData.message || responseData.msg || text.searchFailed
      }
    }

    if ('message' in error && typeof error.message === 'string') {
      return error.message
    }
  }

  return text.searchFailed
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
  <PageContainer :title="text.title" :description="text.description">
    <el-card shadow="hover">
      <el-form label-width="88px" class="analysis-form">
        <div class="analysis-form-grid">
          <el-form-item :label="text.dateRangeLabel" required class="analysis-form-item">
            <el-date-picker
              v-model="form.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              :range-separator="text.rangeSeparator"
              :start-placeholder="text.startPlaceholder"
              :end-placeholder="text.endPlaceholder"
              class="date-range-picker"
            />
          </el-form-item>

          <el-form-item :label="text.keywordLabel" class="analysis-form-item">
            <el-input
              v-model="form.keyword"
              clearable
              :placeholder="text.keywordPlaceholder"
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <el-form-item class="analysis-actions">
            <el-button type="primary" :loading="searchLoading" @click="handleSearch">
              {{ text.searchButton }}
            </el-button>
            <el-button @click="handleReset">{{ text.resetButton }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="section-header">
          <h3>{{ text.summaryTitle }}</h3>
        </div>
      </template>

      <el-alert
        v-if="summaryError"
        :closable="false"
        :title="summaryError"
        type="error"
        show-icon
        class="section-alert"
      />

      <el-table
        v-loading="summaryLoading"
        :data="summaryList"
        border
        stripe
        :empty-text="text.emptySummary"
      >
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
          <h3>{{ text.ordersTitle }}</h3>
        </div>
      </template>

      <el-alert
        v-if="ordersError"
        :closable="false"
        :title="ordersError"
        type="error"
        show-icon
        class="section-alert"
      />

      <el-table
        v-loading="ordersLoading"
        :data="orderPage.list"
        border
        stripe
        :empty-text="text.emptyOrders"
      >
        <el-table-column prop="order_id" label="order_id" min-width="120" />
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
  </PageContainer>
</template>

<style scoped>
.analysis-form-grid {
  display: grid;
  grid-template-columns: minmax(280px, 2fr) minmax(220px, 1fr) auto;
  gap: 16px;
}

.analysis-form-item {
  margin-bottom: 0;
}

.analysis-actions {
  margin-bottom: 0;
  align-self: end;
}

.date-range-picker {
  width: 100%;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
}

.section-alert {
  margin-bottom: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 960px) {
  .analysis-form-grid {
    grid-template-columns: 1fr;
  }

  .analysis-actions {
    justify-self: start;
  }

  .pagination-wrap {
    justify-content: flex-start;
  }
}
</style>

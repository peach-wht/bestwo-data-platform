<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import PageContainer from '@/components/PageContainer.vue'
import {
  createOrder,
  getMockPayment,
  getOrderDetail,
  getOrders,
  mockPaymentFail,
  mockPaymentSuccess,
  prepayOrder,
  type CreateOrderRequest,
  type MockPaymentQueryResponse,
  type OrderDetail,
  type OrderListItem,
  type OrderPageData,
  type OrderPrepayResponse
} from '@/api/orders'
import { ensureApiSuccess, formatDateTime, formatFenToYuan, resolveErrorMessage } from '@/utils/ui'

interface CreateOrderForm {
  orderTitle: string
  orderDescription: string
  buyerId: string
  buyerNickname: string
  totalAmountYuan: number
  remark: string
  preferredTradeType: string
}

const createFormRef = ref<FormInstance>()
const createLoading = ref(false)
const listLoading = ref(false)
const detailLoading = ref(false)
const mockPaymentLoading = ref(false)
const mockActionLoading = ref(false)
const payingOrderId = ref('')
const detailVisible = ref(false)
const payDialogVisible = ref(false)
const errorMessage = ref('')

const createForm = reactive<CreateOrderForm>({
  orderTitle: '微信支付测试订单',
  orderDescription: '来自前端订单中心的测试订单',
  buyerId: 'demo-user-001',
  buyerNickname: '演示用户',
  totalAmountYuan: 199,
  remark: '',
  preferredTradeType: 'NATIVE'
})

const queryForm = reactive({
  keyword: '',
  orderStatus: '',
  payStatus: '',
  pageNum: 1,
  pageSize: 10
})

const pageData = ref<OrderPageData>({
  list: [],
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const currentDetail = ref<OrderDetail | null>(null)
const currentPrepay = ref<OrderPrepayResponse | null>(null)
const currentMockPayment = ref<MockPaymentQueryResponse | null>(null)

const createRules: FormRules<CreateOrderForm> = {
  orderTitle: [{ required: true, message: '请输入订单标题', trigger: 'blur' }],
  totalAmountYuan: [{ required: true, message: '请输入订单金额', trigger: 'change' }]
}

const orderStatusOptions = ['CREATED', 'WAIT_PAY', 'PAID', 'CLOSED', 'CANCELLED', 'REFUNDED']
const payStatusOptions = ['UNPAID', 'PAYING', 'PAID', 'FAILED', 'CLOSED', 'REFUNDED']

const loadOrders = async (pageNum = queryForm.pageNum, pageSize = queryForm.pageSize) => {
  listLoading.value = true
  errorMessage.value = ''

  try {
    const response = await getOrders({
      keyword: queryForm.keyword.trim() || undefined,
      orderStatus: queryForm.orderStatus || undefined,
      payStatus: queryForm.payStatus || undefined,
      pageNum,
      pageSize
    })
    const data = ensureApiSuccess(response, '加载订单列表失败')
    pageData.value = data ?? { list: [], pageNum, pageSize, total: 0 }
    queryForm.pageNum = Number(pageData.value.pageNum || pageNum)
    queryForm.pageSize = Number(pageData.value.pageSize || pageSize)
  } catch (error) {
    const message = resolveErrorMessage(error, '加载订单列表失败，请检查 order-service 和 gateway-service')
    errorMessage.value = message
    ElMessage.error(message)
  } finally {
    listLoading.value = false
  }
}

const loadOrderDetail = async (orderId: string, silent = false) => {
  if (!silent) {
    detailLoading.value = true
  }

  try {
    const response = await getOrderDetail(orderId)
    currentDetail.value = ensureApiSuccess(response, '加载订单详情失败')
  } catch (error) {
    currentDetail.value = null
    if (!silent) {
      ElMessage.error(resolveErrorMessage(error, '加载订单详情失败'))
    }
  } finally {
    if (!silent) {
      detailLoading.value = false
    }
  }
}

const refreshCurrentOrderViews = async () => {
  await loadOrders(queryForm.pageNum, queryForm.pageSize)

  if (detailVisible.value && currentDetail.value?.orderId) {
    await loadOrderDetail(currentDetail.value.orderId, true)
  }
}

const syncMockPaymentFromPrepay = (prepay: OrderPrepayResponse | null) => {
  if (!prepay?.mockMode) {
    currentMockPayment.value = null
    return
  }

  currentMockPayment.value = {
    paymentOrderNo: prepay.paymentOrderNo,
    orderNo: prepay.orderNo,
    status: prepay.status,
    platform: prepay.platform,
    tradeType: prepay.tradeType,
    paymentProvider: prepay.paymentProvider,
    mockMode: prepay.mockMode,
    mockPayToken: prepay.mockPayToken || prepay.channelPrepayId,
    mockPayUrl: prepay.mockPayUrl || prepay.codeUrl,
    channelOrderNo: prepay.channelOrderNo
  }
}

const syncPrepayWithMockPayment = (mockPayment: MockPaymentQueryResponse) => {
  if (!currentPrepay.value) {
    return
  }

  currentPrepay.value = {
    ...currentPrepay.value,
    status: mockPayment.status,
    channelOrderNo: mockPayment.channelOrderNo,
    channelPrepayId: mockPayment.mockPayToken || currentPrepay.value.channelPrepayId,
    paymentProvider: mockPayment.paymentProvider || currentPrepay.value.paymentProvider,
    mockMode: mockPayment.mockMode ?? currentPrepay.value.mockMode,
    mockPayToken: mockPayment.mockPayToken || currentPrepay.value.mockPayToken,
    mockPayUrl: mockPayment.mockPayUrl || currentPrepay.value.mockPayUrl,
    codeUrl: mockPayment.mockPayUrl || currentPrepay.value.codeUrl
  }
}

const handleCreateOrder = async () => {
  if (!createFormRef.value) {
    return
  }

  try {
    await createFormRef.value.validate()
  } catch {
    return
  }

  createLoading.value = true
  errorMessage.value = ''

  try {
    const payload: CreateOrderRequest = {
      orderTitle: createForm.orderTitle.trim(),
      orderDescription: createForm.orderDescription.trim() || undefined,
      buyerId: createForm.buyerId.trim() || undefined,
      buyerNickname: createForm.buyerNickname.trim() || undefined,
      totalAmountFen: toFen(createForm.totalAmountYuan),
      payableAmountFen: toFen(createForm.totalAmountYuan),
      preferredPayPlatform: 'WECHAT_PAY',
      preferredTradeType: createForm.preferredTradeType,
      remark: createForm.remark.trim() || undefined,
      createdBy: 'frontend'
    }

    const response = await createOrder(payload)
    const data = ensureApiSuccess(response, '创建订单失败')
    ElMessage.success(`订单创建成功：${data.orderNo}`)
    queryForm.pageNum = 1
    await loadOrders(1, queryForm.pageSize)
  } catch (error) {
    const message = resolveErrorMessage(error, '创建订单失败，请检查 order-service 状态')
    errorMessage.value = message
    ElMessage.error(message)
  } finally {
    createLoading.value = false
  }
}

const handleSearch = async () => {
  queryForm.pageNum = 1
  await loadOrders(1, queryForm.pageSize)
}

const handleReset = async () => {
  queryForm.keyword = ''
  queryForm.orderStatus = ''
  queryForm.payStatus = ''
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  await loadOrders(1, 10)
}

const handleViewDetail = async (row: OrderListItem) => {
  detailVisible.value = true
  await loadOrderDetail(row.orderId)
}

const handlePrepay = async (row: OrderListItem) => {
  payingOrderId.value = row.orderId

  try {
    const response = await prepayOrder(row.orderId)
    currentPrepay.value = ensureApiSuccess(response, '发起支付失败')
    syncMockPaymentFromPrepay(currentPrepay.value)
    payDialogVisible.value = true
    ElMessage.success(currentPrepay.value.mockMode ? '已生成 Mock 支付单，可直接在弹窗中模拟收银台操作' : '已生成微信支付预下单参数')
    await refreshCurrentOrderViews()
  } catch (error) {
    const message = resolveErrorMessage(error, '发起支付失败，请检查支付服务状态')
    ElMessage.error(message)
  } finally {
    payingOrderId.value = ''
  }
}

const handleCurrentChange = async (pageNum: number) => {
  queryForm.pageNum = pageNum
  await loadOrders(pageNum, queryForm.pageSize)
}

const handleSizeChange = async (pageSize: number) => {
  queryForm.pageSize = pageSize
  queryForm.pageNum = 1
  await loadOrders(1, pageSize)
}

const handleRefreshMockPayment = async (showMessage = false) => {
  if (!currentPrepay.value?.paymentOrderNo) {
    return
  }

  mockPaymentLoading.value = true

  try {
    const response = await getMockPayment(currentPrepay.value.paymentOrderNo)
    const data = ensureApiSuccess(response, '查询模拟支付结果失败')
    currentMockPayment.value = data
    syncPrepayWithMockPayment(data)
    if (showMessage) {
      ElMessage.success(`当前支付状态：${data.status || '--'}`)
    }
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '查询模拟支付结果失败'))
  } finally {
    mockPaymentLoading.value = false
  }
}

const handleMockSuccess = async () => {
  if (!currentPrepay.value?.paymentOrderNo) {
    return
  }

  mockActionLoading.value = true

  try {
    const response = await mockPaymentSuccess(currentPrepay.value.paymentOrderNo, {
      operator: 'frontend-mock-user'
    })
    const data = ensureApiSuccess(response, '模拟支付成功失败')
    currentMockPayment.value = data
    syncPrepayWithMockPayment(data)
    ElMessage.success('已模拟支付成功，并走完统一回调处理逻辑')
    await refreshCurrentOrderViews()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '模拟支付成功失败'))
  } finally {
    mockActionLoading.value = false
  }
}

const handleMockFail = async () => {
  if (!currentPrepay.value?.paymentOrderNo) {
    return
  }

  mockActionLoading.value = true

  try {
    const response = await mockPaymentFail(currentPrepay.value.paymentOrderNo, {
      operator: 'frontend-mock-user',
      reason: 'USER_CANCEL'
    })
    const data = ensureApiSuccess(response, '模拟支付失败失败')
    currentMockPayment.value = data
    syncPrepayWithMockPayment(data)
    ElMessage.success('已模拟支付失败，并走完统一回调处理逻辑')
    await refreshCurrentOrderViews()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '模拟支付失败失败'))
  } finally {
    mockActionLoading.value = false
  }
}

const copyCodeUrl = async () => {
  const target = currentPrepay.value?.mockPayUrl || currentPrepay.value?.codeUrl
  if (!target) {
    return
  }

  try {
    await navigator.clipboard.writeText(target)
    ElMessage.success('支付链接已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动复制支付链接')
  }
}

const getStatusTagType = (status?: string | null) => {
  switch (status) {
    case 'PAID':
    case 'SUCCESS':
      return 'success'
    case 'WAIT_PAY':
    case 'PAYING':
    case 'PREPAYING':
      return 'warning'
    case 'FAILED':
      return 'danger'
    case 'CLOSED':
    case 'CANCELLED':
      return 'info'
    default:
      return ''
  }
}

function toFen(value: number) {
  return Math.round((value || 0) * 100)
}

onMounted(async () => {
  await loadOrders()
})
</script>

<template>
  <PageContainer
    title="订单中心"
    description="用于创建测试订单、查看订单列表，并通过网关发起支付。Mock 模式下可直接在弹窗中模拟收银台成功 / 失败回调。"
  >
    <div class="order-page">
      <el-alert
        v-if="errorMessage"
        :closable="false"
        :title="errorMessage"
        type="error"
        show-icon
      />

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>创建订单</h3>
            <span>直接调用 /api/orders</span>
          </div>
        </template>

        <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
          <div class="form-grid">
            <el-form-item label="订单标题" prop="orderTitle">
              <el-input v-model="createForm.orderTitle" maxlength="128" show-word-limit />
            </el-form-item>
            <el-form-item label="购买人 ID">
              <el-input v-model="createForm.buyerId" maxlength="64" />
            </el-form-item>
            <el-form-item label="购买人昵称">
              <el-input v-model="createForm.buyerNickname" maxlength="128" />
            </el-form-item>
            <el-form-item label="订单金额" prop="totalAmountYuan">
              <el-input-number v-model="createForm.totalAmountYuan" :min="0.01" :precision="2" :step="1" />
            </el-form-item>
            <el-form-item label="支付方式">
              <el-select v-model="createForm.preferredTradeType">
                <el-option label="微信 Native" value="NATIVE" />
                <el-option label="微信 JSAPI" value="JSAPI" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="createForm.remark" maxlength="255" />
            </el-form-item>
            <el-form-item label="订单描述" class="full-width">
              <el-input v-model="createForm.orderDescription" type="textarea" :rows="2" maxlength="255" show-word-limit />
            </el-form-item>
          </div>

          <div class="actions">
            <el-button type="primary" :loading="createLoading" @click="handleCreateOrder">创建订单</el-button>
          </div>
        </el-form>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>订单列表</h3>
            <span>支持分页、状态筛选、支付和 Mock 收银台联调</span>
          </div>
        </template>

        <el-form inline class="query-form">
          <el-form-item label="关键字">
            <el-input
              v-model="queryForm.keyword"
              clearable
              placeholder="订单号 / 标题 / 购买人"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="queryForm.orderStatus" clearable placeholder="全部状态">
              <el-option v-for="item in orderStatusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="支付状态">
            <el-select v-model="queryForm.payStatus" clearable placeholder="全部状态">
              <el-option v-for="item in payStatusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="listLoading" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="listLoading" :data="pageData.list" border stripe empty-text="暂无订单数据">
          <el-table-column prop="orderNo" label="订单号" min-width="190" />
          <el-table-column prop="orderTitle" label="订单标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="buyerNickname" label="购买人" min-width="120" />
          <el-table-column prop="totalAmountFen" label="订单金额" min-width="120">
            <template #default="{ row }">
              {{ formatFenToYuan(row.totalAmountFen) }}
            </template>
          </el-table-column>
          <el-table-column prop="orderStatus" label="订单状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.orderStatus)">{{ row.orderStatus || '--' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="payStatus" label="支付状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.payStatus)">{{ row.payStatus || '--' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="180" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
                <el-button
                  link
                  type="success"
                  :loading="payingOrderId === row.orderId"
                  :disabled="row.payStatus === 'PAID'"
                  @click="handlePrepay(row)"
                >
                  支付
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="queryForm.pageNum"
            v-model:page-size="queryForm.pageSize"
            background
            layout="total, sizes, prev, pager, next"
            :page-sizes="[10, 20, 50, 100]"
            :total="pageData.total"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </el-card>

      <el-drawer v-model="detailVisible" title="订单详情" size="55%">
        <div v-loading="detailLoading">
          <el-descriptions v-if="currentDetail" :column="2" border>
            <el-descriptions-item label="订单号">{{ currentDetail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="外部单号">{{ currentDetail.externalOrderNo || '--' }}</el-descriptions-item>
            <el-descriptions-item label="订单标题">{{ currentDetail.orderTitle || '--' }}</el-descriptions-item>
            <el-descriptions-item label="购买人">{{ currentDetail.buyerNickname || currentDetail.buyerId || '--' }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">{{ currentDetail.orderStatus || '--' }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ currentDetail.payStatus || '--' }}</el-descriptions-item>
            <el-descriptions-item label="总金额">{{ formatFenToYuan(currentDetail.totalAmountFen) }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">{{ formatFenToYuan(currentDetail.paidAmountFen) }}</el-descriptions-item>
            <el-descriptions-item label="支付单号">{{ currentDetail.latestPaymentOrderNo || '--' }}</el-descriptions-item>
            <el-descriptions-item label="渠道单号">{{ currentDetail.latestChannelOrderNo || '--' }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatDateTime(currentDetail.paidTime) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(currentDetail.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="订单描述" :span="2">
              {{ currentDetail.orderDescription || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">
              {{ currentDetail.remark || '--' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-drawer>

      <el-dialog v-model="payDialogVisible" title="支付预下单结果" width="760px">
        <div class="pay-dialog-body">
          <el-descriptions v-if="currentPrepay" :column="2" border>
            <el-descriptions-item label="订单号">{{ currentPrepay.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="支付单号">{{ currentPrepay.paymentOrderNo }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ currentPrepay.tradeType || '--' }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="getStatusTagType(currentPrepay.status)">{{ currentPrepay.status || '--' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付提供方">{{ currentPrepay.paymentProvider || 'WECHAT' }}</el-descriptions-item>
            <el-descriptions-item label="模拟模式">{{ currentPrepay.mockMode ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ formatDateTime(currentPrepay.expireAt) }}</el-descriptions-item>
            <el-descriptions-item label="渠道单号">{{ currentPrepay.channelOrderNo || '--' }}</el-descriptions-item>
            <el-descriptions-item label="支付链接 / code_url" :span="2">
              <div class="code-url-wrap">
                <el-input :model-value="currentPrepay.mockPayUrl || currentPrepay.codeUrl || '--'" readonly />
                <el-button type="primary" @click="copyCodeUrl">复制</el-button>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <el-card v-if="currentPrepay?.mockMode" shadow="never" class="mock-pay-card">
            <template #header>
              <div class="section-header">
                <h3>模拟收银台</h3>
                <span>直接调用 /api/pay/mock-payments/** 和统一 mock 回调链路</span>
              </div>
            </template>

            <el-alert
              :closable="false"
              title="当前是 Mock 支付模式。点击下方按钮会生成模拟回调，并复用现有支付状态流转与入仓链路。"
              type="info"
              show-icon
            />

            <div class="mock-pay-actions">
              <el-button :loading="mockPaymentLoading" @click="handleRefreshMockPayment(true)">查询支付结果</el-button>
              <el-button type="success" :loading="mockActionLoading" @click="handleMockSuccess">支付成功</el-button>
              <el-button type="danger" plain :loading="mockActionLoading" @click="handleMockFail">支付失败</el-button>
            </div>

            <el-descriptions v-if="currentMockPayment" :column="2" border class="mock-pay-result">
              <el-descriptions-item label="支付单号">{{ currentMockPayment.paymentOrderNo }}</el-descriptions-item>
              <el-descriptions-item label="订单号">{{ currentMockPayment.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getStatusTagType(currentMockPayment.status)">{{ currentMockPayment.status || '--' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Mock Token">{{ currentMockPayment.mockPayToken || '--' }}</el-descriptions-item>
              <el-descriptions-item label="渠道单号">{{ currentMockPayment.channelOrderNo || '--' }}</el-descriptions-item>
              <el-descriptions-item label="成功时间">{{ formatDateTime(currentMockPayment.successTime) }}</el-descriptions-item>
              <el-descriptions-item label="失败原因" :span="2">
                {{ currentMockPayment.failMessage || '--' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
      </el-dialog>
    </div>
  </PageContainer>
</template>

<style scoped>
.order-page {
  display: grid;
  gap: 16px;
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.full-width {
  grid-column: 1 / -1;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.query-form {
  margin-bottom: 8px;
}

.row-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.pay-dialog-body {
  display: grid;
  gap: 16px;
}

.code-url-wrap {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.mock-pay-card {
  border-style: dashed;
}

.mock-pay-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.mock-pay-result {
  margin-top: 16px;
}

@media (max-width: 960px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    justify-content: flex-start;
  }

  .code-url-wrap {
    grid-template-columns: 1fr;
  }

  .mock-pay-actions {
    flex-direction: column;
  }
}
</style>

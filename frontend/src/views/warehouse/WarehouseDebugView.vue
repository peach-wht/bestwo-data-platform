<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

import PageContainer from '@/components/PageContainer.vue'
import {
  getHealth,
  getWarehouseOrdersTest,
  getWarehousePing,
  type ApiResponse,
  type WarehouseOrderRow,
  type WarehousePingResult
} from '@/api/warehouse'

const text = {
  pageTitle: '\u0057\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u0020\u8054\u8c03\u6d4b\u8bd5',
  pageDescription:
    '\u5f53\u524d\u9875\u9762\u901a\u8fc7\u0020\u0067\u0061\u0074\u0065\u0077\u0061\u0079\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u8c03\u7528\u0020\u0077\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\uff0c\u7528\u4e8e\u68c0\u67e5\u5065\u5eb7\u72b6\u6001\u3001\u0044\u006f\u0072\u0069\u0073\u0020\u8fde\u901a\u6027\u548c\u6d4b\u8bd5\u8ba2\u5355\u67e5\u8be2\u3002',
  healthTitle: '\u5065\u5eb7\u68c0\u67e5',
  healthDescription:
    '\u8c03\u7528\u0020\u0067\u0061\u0074\u0065\u0077\u0061\u0079\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u7684\u0020\u002f\u0068\u0065\u0061\u006c\u0074\u0068\u0020\u63a5\u53e3\u3002',
  healthButton: '\u8c03\u7528\u0020\u002f\u0068\u0065\u0061\u006c\u0074\u0068',
  pingTitle: '\u0044\u006f\u0072\u0069\u0073\u0020\u8fde\u901a\u68c0\u67e5',
  pingDescription:
    '\u901a\u8fc7\u0020\u0067\u0061\u0074\u0065\u0077\u0061\u0079\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u8f6c\u53d1\u8c03\u7528\u0020\u002f\u0077\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u002f\u0070\u0069\u006e\u0067\u3002',
  pingButton: '\u8c03\u7528\u0020\u002f\u0077\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u002f\u0070\u0069\u006e\u0067',
  ordersTitle: '\u8ba2\u5355\u6d4b\u8bd5\u67e5\u8be2',
  ordersDescription:
    '\u8c03\u7528\u0020\u002f\u0077\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u002f\u006f\u0072\u0064\u0065\u0072\u0073\u002f\u0074\u0065\u0073\u0074\u0020\u5e76\u5c55\u793a\u8fd4\u56de\u7684\u8ba2\u5355\u5217\u8868\u548c\u539f\u59cb\u0020\u004a\u0053\u004f\u004e\u3002',
  ordersButton: '\u67e5\u8be2\u6700\u65b0\u8ba2\u5355',
  rawJsonTitle: '\u539f\u59cb\u0020\u004a\u0053\u004f\u004e\u0020\u8fd4\u56de',
  emptyJson: '\u70b9\u51fb\u6309\u94ae\u5f00\u59cb\u8054\u8c03',
  requestFailed:
    '\u8bf7\u6c42\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u0020\u0067\u0061\u0074\u0065\u0077\u0061\u0079\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u4e0e\u0020\u0077\u0061\u0072\u0065\u0068\u006f\u0075\u0073\u0065\u002d\u0073\u0065\u0072\u0076\u0069\u0063\u0065\u0020\u662f\u5426\u5df2\u542f\u52a8'
} as const

const healthLoading = ref(false)
const pingLoading = ref(false)
const ordersLoading = ref(false)

const healthResponse = ref<ApiResponse<string> | null>(null)
const pingResponse = ref<ApiResponse<WarehousePingResult> | null>(null)
const ordersResponse = ref<ApiResponse<WarehouseOrderRow[]> | null>(null)

const healthError = ref('')
const pingError = ref('')
const ordersError = ref('')

const orderRows = computed(() => ordersResponse.value?.data ?? [])

const formatJson = (value: unknown) => {
  if (!value) {
    return text.emptyJson
  }

  return JSON.stringify(value, null, 2)
}

const resolveErrorMessage = (error: unknown) => {
  if (
    typeof error === 'object' &&
    error !== null &&
    'response' in error &&
    typeof error.response === 'object' &&
    error.response !== null &&
    'data' in error.response &&
    typeof error.response.data === 'object' &&
    error.response.data !== null &&
    'message' in error.response.data
  ) {
    return String(error.response.data.message)
  }

  if (typeof error === 'object' && error !== null && 'message' in error) {
    return String(error.message)
  }

  return text.requestFailed
}

const handleHealth = async () => {
  healthLoading.value = true
  healthError.value = ''

  try {
    healthResponse.value = await getHealth()
  } catch (error) {
    const message = resolveErrorMessage(error)
    healthError.value = message
    ElMessage.error(message)
  } finally {
    healthLoading.value = false
  }
}

const handlePing = async () => {
  pingLoading.value = true
  pingError.value = ''

  try {
    pingResponse.value = await getWarehousePing()
  } catch (error) {
    const message = resolveErrorMessage(error)
    pingError.value = message
    ElMessage.error(message)
  } finally {
    pingLoading.value = false
  }
}

const handleQueryOrders = async () => {
  ordersLoading.value = true
  ordersError.value = ''

  try {
    ordersResponse.value = await getWarehouseOrdersTest()
  } catch (error) {
    const message = resolveErrorMessage(error)
    ordersError.value = message
    ElMessage.error(message)
  } finally {
    ordersLoading.value = false
  }
}
</script>

<template>
  <PageContainer :title="text.pageTitle" :description="text.pageDescription">
    <div class="warehouse-debug">
      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <div>
              <h3>{{ text.healthTitle }}</h3>
              <p>{{ text.healthDescription }}</p>
            </div>
            <el-button type="primary" :loading="healthLoading" @click="handleHealth">
              {{ text.healthButton }}
            </el-button>
          </div>
        </template>

        <el-alert
          v-if="healthError"
          :closable="false"
          :title="healthError"
          type="error"
          show-icon
          class="section-alert"
        />

        <pre class="json-block">{{ formatJson(healthResponse) }}</pre>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <div>
              <h3>{{ text.pingTitle }}</h3>
              <p>{{ text.pingDescription }}</p>
            </div>
            <el-button type="primary" :loading="pingLoading" @click="handlePing">
              {{ text.pingButton }}
            </el-button>
          </div>
        </template>

        <el-alert
          v-if="pingError"
          :closable="false"
          :title="pingError"
          type="error"
          show-icon
          class="section-alert"
        />

        <pre class="json-block">{{ formatJson(pingResponse) }}</pre>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <div>
              <h3>{{ text.ordersTitle }}</h3>
              <p>{{ text.ordersDescription }}</p>
            </div>
            <el-button type="primary" :loading="ordersLoading" @click="handleQueryOrders">
              {{ text.ordersButton }}
            </el-button>
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

        <el-table v-loading="ordersLoading" :data="orderRows" border stripe class="orders-table">
          <el-table-column prop="order_id" label="order_id" min-width="140" />
          <el-table-column prop="order_no" label="order_no" min-width="180" />
          <el-table-column prop="order_status" label="order_status" min-width="140" />
          <el-table-column prop="pay_status" label="pay_status" min-width="140" />
          <el-table-column prop="order_time" label="order_time" min-width="180" />
        </el-table>

        <div class="json-panel">
          <h4>{{ text.rawJsonTitle }}</h4>
          <pre class="json-block">{{ formatJson(ordersResponse) }}</pre>
        </div>
      </el-card>
    </div>
  </PageContainer>
</template>

<style scoped>
.warehouse-debug {
  display: grid;
  gap: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
}

.section-header p {
  margin: 8px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}

.section-alert {
  margin-bottom: 12px;
}

.orders-table {
  width: 100%;
}

.json-panel {
  margin-top: 16px;
}

.json-panel h4 {
  margin: 0 0 12px;
  font-size: 14px;
}

.json-block {
  margin: 0;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: #0f172a;
  color: #dbeafe;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .section-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .section-header .el-button {
    width: 100%;
  }
}
</style>

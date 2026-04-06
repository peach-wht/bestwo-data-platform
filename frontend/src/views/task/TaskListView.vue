<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import PageContainer from '@/components/PageContainer.vue'
import {
  getAlertRecords,
  getJobDefinitions,
  getJobLogs,
  getQualityRules,
  initWarehouseMetadata,
  runBuildAdsJob,
  runBuildDwdJob,
  runQualityJob,
  runSyncOrderJob,
  type AlertRecordItem,
  type JobDefinitionItem,
  type JobExecutionLogItem,
  type MetadataInitResult,
  type QualityRuleItem
} from '@/api/warehouse-analysis'
import { ensureApiSuccess, formatDateTime, resolveErrorMessage } from '@/utils/ui'

type TaskAction = 'init-meta' | 'sync-order' | 'build-dwd' | 'build-ads' | 'quality'

const pageLoading = ref(false)
const actionLoading = ref<TaskAction | ''>('')
const errorMessage = ref('')
const lastActionMessage = ref('')

const jobDefinitions = ref<JobDefinitionItem[]>([])
const jobLogs = ref<JobExecutionLogItem[]>([])
const qualityRules = ref<QualityRuleItem[]>([])
const alerts = ref<AlertRecordItem[]>([])

const loadTaskData = async () => {
  pageLoading.value = true
  errorMessage.value = ''

  try {
    const [jobResponse, logResponse, ruleResponse, alertResponse] = await Promise.all([
      getJobDefinitions({ limit: 20 }),
      getJobLogs({ limit: 20 }),
      getQualityRules({ limit: 20 }),
      getAlertRecords({ alertStatus: 'OPEN', limit: 20 })
    ])

    jobDefinitions.value = ensureApiSuccess(jobResponse, '加载任务定义失败') ?? []
    jobLogs.value = ensureApiSuccess(logResponse, '加载任务日志失败') ?? []
    qualityRules.value = ensureApiSuccess(ruleResponse, '加载质量规则失败') ?? []
    alerts.value = ensureApiSuccess(alertResponse, '加载开放告警失败') ?? []
  } catch (error) {
    const message = resolveErrorMessage(error, '加载任务中心失败，请检查 gateway-service 和 warehouse-service')
    errorMessage.value = message
    ElMessage.error(message)
  } finally {
    pageLoading.value = false
  }
}

const executeAction = async (action: TaskAction) => {
  actionLoading.value = action
  lastActionMessage.value = ''

  try {
    switch (action) {
      case 'init-meta': {
        const response = await initWarehouseMetadata()
        const data = ensureApiSuccess<MetadataInitResult>(response, '初始化元数据失败')
        lastActionMessage.value = `元数据初始化完成，执行了 ${data.executedResources?.length ?? 0} 个脚本`
        break
      }
      case 'sync-order': {
        const response = await runSyncOrderJob()
        const data = ensureApiSuccess(response, '同步 ODS 失败')
        lastActionMessage.value = String(data.message || 'ODS 同步成功')
        break
      }
      case 'build-dwd': {
        const response = await runBuildDwdJob()
        const data = ensureApiSuccess(response, '构建 DWD 失败')
        lastActionMessage.value = String(data.message || 'DWD 构建成功')
        break
      }
      case 'build-ads': {
        const response = await runBuildAdsJob()
        const data = ensureApiSuccess(response, '构建 ADS 失败')
        lastActionMessage.value = String(data.message || 'ADS 构建成功')
        break
      }
      case 'quality': {
        const response = await runQualityJob()
        const data = ensureApiSuccess(response, '执行质量检查失败')
        lastActionMessage.value = String(data.message || '质量检查任务已完成')
        break
      }
    }

    ElMessage.success(lastActionMessage.value || '任务执行成功')
    await loadTaskData()
  } catch (error) {
    const message = resolveErrorMessage(error, '任务执行失败')
    lastActionMessage.value = message
    ElMessage.error(message)
  } finally {
    actionLoading.value = ''
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

onMounted(async () => {
  await loadTaskData()
})
</script>

<template>
  <PageContainer title="任务中心" description="执行数仓初始化、同步、构建和质量检查，并查看统一任务日志与治理告警。">
    <div class="task-page">
      <el-alert
        v-if="errorMessage"
        :closable="false"
        :title="errorMessage"
        type="error"
        show-icon
      />

      <el-alert
        v-if="lastActionMessage"
        :closable="false"
        :title="lastActionMessage"
        type="success"
        show-icon
      />

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>任务操作</h3>
            <span>按当前 MVP 链路顺序执行</span>
          </div>
        </template>

        <div class="action-grid">
          <el-button type="primary" :loading="actionLoading === 'init-meta'" @click="executeAction('init-meta')">
            初始化元数据
          </el-button>
          <el-button type="primary" :loading="actionLoading === 'sync-order'" @click="executeAction('sync-order')">
            同步 ODS
          </el-button>
          <el-button type="primary" :loading="actionLoading === 'build-dwd'" @click="executeAction('build-dwd')">
            构建 DWD
          </el-button>
          <el-button type="primary" :loading="actionLoading === 'build-ads'" @click="executeAction('build-ads')">
            构建 ADS
          </el-button>
          <el-button type="warning" :loading="actionLoading === 'quality'" @click="executeAction('quality')">
            运行质量检查
          </el-button>
          <el-button :loading="pageLoading" @click="loadTaskData">刷新任务看板</el-button>
        </div>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>任务定义</h3>
            <span>来自 dw_sync_job</span>
          </div>
        </template>

        <el-table v-loading="pageLoading" :data="jobDefinitions" border stripe empty-text="暂无任务定义">
          <el-table-column prop="job_code" label="任务编码" min-width="180" />
          <el-table-column prop="job_name" label="任务名称" min-width="220" />
          <el-table-column prop="source_tables" label="来源表" min-width="220" show-overflow-tooltip />
          <el-table-column prop="target_tables" label="目标表" min-width="220" show-overflow-tooltip />
          <el-table-column prop="last_run_status" label="最近状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.last_run_status)">{{ row.last_run_status || '--' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="last_run_at" label="最近运行时间" min-width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.last_run_at) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <div class="two-column-grid">
        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>最近任务日志</h3>
              <span>来自 dw_job_log</span>
            </div>
          </template>

          <el-table v-loading="pageLoading" :data="jobLogs" border stripe empty-text="暂无任务日志">
            <el-table-column prop="job_name" label="任务名称" min-width="180" />
            <el-table-column prop="run_status" label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.run_status)">{{ row.run_status || '--' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="started_at" label="开始时间" min-width="165">
              <template #default="{ row }">
                {{ formatDateTime(row.started_at) }}
              </template>
            </el-table-column>
            <el-table-column prop="message" label="说明" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-card>

        <el-card shadow="hover">
          <template #header>
            <div class="section-header">
              <h3>质量规则</h3>
              <span>来自 dw_quality_rule</span>
            </div>
          </template>

          <el-table v-loading="pageLoading" :data="qualityRules" border stripe empty-text="暂无质量规则">
            <el-table-column prop="rule_code" label="规则编码" min-width="110" />
            <el-table-column prop="rule_name" label="规则名称" min-width="200" />
            <el-table-column prop="table_name" label="表名" min-width="160" />
            <el-table-column prop="rule_level" label="级别" min-width="90">
              <template #default="{ row }">
                <el-tag :type="getAlertTagType(row.rule_level)">{{ row.rule_level || '--' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <el-card shadow="hover">
        <template #header>
          <div class="section-header">
            <h3>开放告警</h3>
            <span>来自 dw_alert_record</span>
          </div>
        </template>

        <el-table v-loading="pageLoading" :data="alerts" border stripe empty-text="当前没有开放告警">
          <el-table-column prop="alert_level" label="级别" min-width="100">
            <template #default="{ row }">
              <el-tag :type="getAlertTagType(row.alert_level)">{{ row.alert_level || '--' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alert_type" label="类型" min-width="120" />
          <el-table-column prop="alert_title" label="告警标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="source_name" label="来源" min-width="180" show-overflow-tooltip />
          <el-table-column prop="fired_at" label="触发时间" min-width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.fired_at) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </PageContainer>
</template>

<style scoped>
.task-page {
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

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.two-column-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 960px) {
  .action-grid,
  .two-column-grid {
    grid-template-columns: 1fr;
  }
}
</style>

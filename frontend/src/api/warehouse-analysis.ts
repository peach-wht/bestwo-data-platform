import http from '@/api/http'

export interface ApiResponse<T> {
  code: number
  message?: string
  msg?: string
  data: T
}

export interface OrderQueryParams {
  startDate: string
  endDate: string
  keyword?: string
  pageNum?: number
  pageSize?: number
}

export interface OrderPageItem {
  order_id?: string | number | null
  order_no?: string | null
  order_status?: string | number | null
  pay_status?: string | number | null
  order_time?: string | null
  pay_time?: string | null
  total_amount?: string | number | null
  buyer_nickname?: string | null
}

export interface OrderPageData {
  list: OrderPageItem[]
  pageNum: number
  pageSize: number
  total: number
}

export interface OrderDaySummaryItem {
  stat_date?: string | null
  order_count?: string | number | null
  paid_order_count?: string | number | null
  total_amount?: string | number | null
  paid_amount?: string | number | null
  refund_amount?: string | number | null
}

export interface PayOverviewItem {
  latest_stat_date?: string | null
  order_count?: number | null
  paid_order_count?: number | null
  unpaid_order_count?: number | null
  closed_order_count?: number | null
  total_amount?: string | number | null
  paid_amount?: string | number | null
  refund_amount?: string | number | null
  pay_success_rate?: string | number | null
}

export interface PayTrendItem {
  stat_date?: string | null
  order_count?: number | null
  paid_order_count?: number | null
  unpaid_order_count?: number | null
  closed_order_count?: number | null
  total_amount?: string | number | null
  paid_amount?: string | number | null
  refund_amount?: string | number | null
  pay_success_rate?: string | number | null
}

export interface JobDefinitionItem {
  job_code?: string | null
  job_name?: string | null
  source_type?: string | null
  source_tables?: string | null
  target_tables?: string | null
  enabled?: number | null
  remark?: string | null
  last_run_status?: string | null
  last_run_message?: string | null
  last_run_at?: string | null
}

export interface JobExecutionLogItem {
  log_id?: string | null
  job_code?: string | null
  job_name?: string | null
  job_type?: string | null
  source_type?: string | null
  source_tables?: string | null
  target_tables?: string | null
  run_status?: string | null
  message?: string | null
  metric_one_label?: string | null
  metric_one_value?: number | null
  metric_two_label?: string | null
  metric_two_value?: number | null
  metric_three_label?: string | null
  metric_three_value?: number | null
  started_at?: string | null
  finished_at?: string | null
  duration_ms?: number | null
}

export interface QualityRuleItem {
  rule_code?: string | null
  rule_name?: string | null
  table_name?: string | null
  rule_level?: string | null
  rule_type?: string | null
  threshold_value?: number | null
  enabled?: number | null
  rule_order?: number | null
  description?: string | null
}

export interface QualityResultItem {
  rule_code?: string | null
  rule_name?: string | null
  table_name?: string | null
  result_status?: string | null
  result_level?: string | null
  failed_count?: number | null
  total_count?: number | null
  message?: string | null
  checked_at?: string | null
}

export interface LineageRelationItem {
  relation_id?: string | null
  relation_type?: string | null
  upstream_datasource_code?: string | null
  upstream_table_code?: string | null
  downstream_datasource_code?: string | null
  downstream_table_code?: string | null
  transform_name?: string | null
  enabled?: number | null
}

export interface AlertRecordItem {
  alert_id?: string | null
  alert_type?: string | null
  alert_level?: string | null
  alert_source?: string | null
  source_code?: string | null
  source_name?: string | null
  alert_status?: string | null
  alert_title?: string | null
  alert_message?: string | null
  fired_at?: string | null
  resolved_at?: string | null
}

export interface MetadataInitResult {
  database?: string | null
  datasourceCount?: number | null
  tableCount?: number | null
  columnCount?: number | null
  executedResources?: string[]
}

export interface JobRunResult {
  logId?: string | null
  jobCode?: string | null
  runStatus?: string | null
  message?: string | null
  startedAt?: string | null
  finishedAt?: string | null
  durationMs?: number | null
  [key: string]: unknown
}

export const getOrderPage = (params: OrderQueryParams) =>
  http.get<ApiResponse<OrderPageData>>('/warehouse/orders', { params })

export const getOrderDaySummary = (params: Pick<OrderQueryParams, 'startDate' | 'endDate'>) =>
  http.get<ApiResponse<OrderDaySummaryItem[]>>('/warehouse/summary/day', { params })

export const getPayOverview = (params?: Pick<OrderQueryParams, 'startDate' | 'endDate'>) =>
  http.get<ApiResponse<PayOverviewItem>>('/warehouse/pay/overview', { params })

export const getPayTrend = (params: Pick<OrderQueryParams, 'startDate' | 'endDate'>) =>
  http.get<ApiResponse<PayTrendItem[]>>('/warehouse/pay/trend', { params })

export const getJobDefinitions = (params?: { limit?: number }) =>
  http.get<ApiResponse<JobDefinitionItem[]>>('/warehouse/meta/jobs', { params })

export const getJobLogs = (params?: { jobCode?: string; limit?: number }) =>
  http.get<ApiResponse<JobExecutionLogItem[]>>('/warehouse/meta/job-logs', { params })

export const getQualityRules = (params?: { limit?: number }) =>
  http.get<ApiResponse<QualityRuleItem[]>>('/warehouse/quality/rules', { params })

export const getQualityResults = (params?: { limit?: number }) =>
  http.get<ApiResponse<QualityResultItem[]>>('/warehouse/quality/results', { params })

export const getLineageRelations = (params?: { tableCode?: string; limit?: number }) =>
  http.get<ApiResponse<LineageRelationItem[]>>('/warehouse/lineage', { params })

export const getAlertRecords = (params?: { alertStatus?: string; limit?: number }) =>
  http.get<ApiResponse<AlertRecordItem[]>>('/warehouse/alerts', { params })

export const initWarehouseMetadata = () =>
  http.post<ApiResponse<MetadataInitResult>>('/warehouse/meta/init')

export const runSyncOrderJob = () =>
  http.post<ApiResponse<JobRunResult>>('/dw/jobs/sync-order/run')

export const runBuildDwdJob = () =>
  http.post<ApiResponse<JobRunResult>>('/dw/jobs/build-dwd/run')

export const runBuildAdsJob = () =>
  http.post<ApiResponse<JobRunResult>>('/dw/jobs/build-ads/run')

export const runQualityJob = () =>
  http.post<ApiResponse<JobRunResult>>('/dw/jobs/quality/run')

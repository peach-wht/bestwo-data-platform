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

export const getOrderPage = (params: OrderQueryParams) =>
  http.get<ApiResponse<OrderPageData>>('/warehouse/orders', { params })

export const getOrderDaySummary = (params: Pick<OrderQueryParams, 'startDate' | 'endDate'>) =>
  http.get<ApiResponse<OrderDaySummaryItem[]>>('/warehouse/summary/day', { params })

import http from '@/api/http'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface WarehousePingResult {
  result: number
  database: string
}

export interface WarehouseOrderRow {
  order_id?: string | number | null
  order_no?: string | null
  order_status?: string | number | null
  pay_status?: string | number | null
  order_time?: string | null
  [key: string]: unknown
}

export const getHealth = () => http.get<ApiResponse<string>>('/health')

export const getWarehousePing = () => http.get<ApiResponse<WarehousePingResult>>('/warehouse/ping')

export const getWarehouseOrdersTest = () =>
  http.get<ApiResponse<WarehouseOrderRow[]>>('/warehouse/orders/test')

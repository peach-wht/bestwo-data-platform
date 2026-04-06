import http from '@/api/http'
import type { ApiResponse } from '@/api/warehouse-analysis'

export interface CreateOrderRequest {
  externalOrderNo?: string
  orderTitle: string
  orderDescription?: string
  buyerId?: string
  buyerNickname?: string
  totalAmountFen: number
  payableAmountFen: number
  preferredPayPlatform?: string
  preferredTradeType?: string
  remark?: string
  extJson?: string
  createdBy?: string
}

export interface CreateOrderResponse {
  orderId: string
  orderNo: string
  orderStatus: string
  payStatus: string
  totalAmountFen: number
  payableAmountFen: number
  preferredPayPlatform?: string
  preferredTradeType?: string
  createdAt?: string
}

export interface OrderListQueryParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  orderStatus?: string
  payStatus?: string
}

export interface OrderListItem {
  orderId: string
  orderNo: string
  orderTitle?: string
  buyerId?: string
  buyerNickname?: string
  totalAmountFen?: number
  payableAmountFen?: number
  paidAmountFen?: number
  orderStatus?: string
  payStatus?: string
  preferredPayPlatform?: string
  preferredTradeType?: string
  latestPaymentOrderNo?: string
  paidTime?: string
  createdAt?: string
}

export interface OrderPageData {
  list: OrderListItem[]
  pageNum: number
  pageSize: number
  total: number
}

export interface OrderItem {
  itemId?: string
  skuCode?: string
  skuName?: string
  quantity?: number
  salePriceFen?: number
  totalAmountFen?: number
}

export interface OrderDetail {
  orderId: string
  orderNo: string
  externalOrderNo?: string
  bizType?: string
  orderSource?: string
  orderTitle?: string
  orderDescription?: string
  buyerId?: string
  buyerNickname?: string
  currency?: string
  totalAmountFen?: number
  payableAmountFen?: number
  paidAmountFen?: number
  refundedAmountFen?: number
  orderStatus?: string
  payStatus?: string
  preferredPayPlatform?: string
  preferredTradeType?: string
  successPayPlatform?: string
  latestPaymentOrderNo?: string
  latestChannelOrderNo?: string
  paidTime?: string
  expiredTime?: string
  closedTime?: string
  cancelledTime?: string
  remark?: string
  extJson?: string
  createdAt?: string
  updatedAt?: string
  items?: OrderItem[]
}

export interface OrderPrepayResponse {
  orderId: string
  orderNo: string
  paymentOrderId: string
  paymentOrderNo: string
  platform?: string
  tradeType?: string
  status?: string
  codeUrl?: string
  paymentProvider?: string
  mockMode?: boolean
  mockPayToken?: string
  mockPayUrl?: string
  channelOrderNo?: string
  channelPrepayId?: string
  expireAt?: string
  createdAt?: string
}

export interface MockPaymentActionRequest {
  operator?: string
  reason?: string
}

export interface MockPaymentQueryResponse {
  paymentOrderNo: string
  orderNo: string
  status?: string
  platform?: string
  tradeType?: string
  paymentProvider?: string
  mockMode?: boolean
  mockPayToken?: string
  mockPayUrl?: string
  channelOrderNo?: string
  successTime?: string
  failMessage?: string
}

export const createOrder = (data: CreateOrderRequest) =>
  http.post<ApiResponse<CreateOrderResponse>>('/orders', data)

export const getOrders = (params: OrderListQueryParams) =>
  http.get<ApiResponse<OrderPageData>>('/orders', { params })

export const getOrderDetail = (id: string) =>
  http.get<ApiResponse<OrderDetail>>(`/orders/${id}`)

export const prepayOrder = (id: string) =>
  http.post<ApiResponse<OrderPrepayResponse>>(`/orders/${id}/pay`)

export const getMockPayment = (paymentOrderNo: string) =>
  http.get<ApiResponse<MockPaymentQueryResponse>>(`/pay/mock-payments/${paymentOrderNo}`)

export const mockPaymentSuccess = (paymentOrderNo: string, data?: MockPaymentActionRequest) =>
  http.post<ApiResponse<MockPaymentQueryResponse>>(`/pay/mock-payments/${paymentOrderNo}/success`, data ?? {})

export const mockPaymentFail = (paymentOrderNo: string, data?: MockPaymentActionRequest) =>
  http.post<ApiResponse<MockPaymentQueryResponse>>(`/pay/mock-payments/${paymentOrderNo}/fail`, data ?? {})

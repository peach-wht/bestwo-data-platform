export interface ApiLikeResponse<T> {
  code: number
  message?: string
  msg?: string
  data: T
}

export const ensureApiSuccess = <T>(response: ApiLikeResponse<T>, fallbackMessage: string) => {
  if (response.code !== 0) {
    throw new Error(response.message || response.msg || fallbackMessage)
  }

  return response.data
}

export const resolveErrorMessage = (error: unknown, fallbackMessage = '请求失败，请稍后重试') => {
  if (typeof error === 'object' && error !== null) {
    if ('response' in error && error.response && typeof error.response === 'object') {
      const response = error.response as { data?: ApiLikeResponse<unknown> }
      if (response.data) {
        return response.data.message || response.data.msg || fallbackMessage
      }
    }

    if ('message' in error && typeof error.message === 'string' && error.message) {
      return error.message
    }
  }

  return fallbackMessage
}

export const formatDateTime = (value?: string | null) => {
  if (!value) {
    return '--'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

export const formatFenToYuan = (value?: string | number | null) => {
  if (value === null || value === undefined || value === '') {
    return '--'
  }

  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return String(value)
  }

  return `¥ ${((amount || 0) / 100).toFixed(2)}`
}

export const formatAmount = (value?: string | number | null) => {
  if (value === null || value === undefined || value === '') {
    return '--'
  }

  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return String(value)
  }

  return `¥ ${amount.toFixed(2)}`
}

export const formatPercent = (value?: string | number | null) => {
  if (value === null || value === undefined || value === '') {
    return '--'
  }

  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return String(value)
  }

  return `${(amount * 100).toFixed(2)}%`
}

export const formatCount = (value?: string | number | null) => {
  if (value === null || value === undefined || value === '') {
    return '--'
  }

  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return String(value)
  }

  return amount.toLocaleString('zh-CN')
}

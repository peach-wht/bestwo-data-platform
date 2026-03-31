import axios, { type AxiosRequestConfig } from 'axios'

const apiBaseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const service = axios.create({
  baseURL: apiBaseURL,
  timeout: 10000
})

service.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error)
)

const http = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return service.get<T>(url, config).then((response) => response.data)
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return service.post<T>(url, data, config).then((response) => response.data)
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return service.put<T>(url, data, config).then((response) => response.data)
  },
  delete<T>(url: string, config?: AxiosRequestConfig) {
    return service.delete<T>(url, config).then((response) => response.data)
  }
}

export default http

import http from '@/api/http'

export interface AuthUser {
  username: string
  roles: string[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface LoginPayload {
  username: string
  password: string
}

export const login = (payload: LoginPayload) => http.post<ApiResponse<AuthUser>>('/auth/login', payload)

export const logout = () => http.post<ApiResponse<null>>('/auth/logout')

export const fetchCurrentUser = () => http.get<ApiResponse<AuthUser>>('/auth/me')

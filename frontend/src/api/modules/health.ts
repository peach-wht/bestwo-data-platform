import http from '@/api/http'

export const fetchGatewayHealth = () => http.get('/health')

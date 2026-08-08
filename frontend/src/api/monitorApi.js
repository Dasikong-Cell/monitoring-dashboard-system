import axios from 'axios'
import { withRetry } from './request'

const monitorRequest = axios.create({ baseURL: '/api/monitor', timeout: 5000 })
monitorRequest.interceptors.response.use(r => r.data?.data ?? r.data)

export const monitorApi = {
  health:  () => withRetry(() => monitorRequest.get('/health')),
  runtime: () => withRetry(() => monitorRequest.get('/runtime'))
}

import { request, withRetry } from './request'
import { cachedOrRequest } from './cache'

const deviceRequest = axios.create({ baseURL: '/api/devices', timeout: 5000 })
deviceRequest.interceptors.response.use(r => r.data?.data ?? r.data)

import axios from 'axios'

export const deviceApi = {
  list:    () => withRetry(() => deviceRequest.get('/')),
  get:     (id) => withRetry(() => deviceRequest.get(`/${id}`)),
  metrics: () => cachedOrRequest('metrics', () => withRetry(() => axios.get('/api/devices/metrics').then(r => r.data?.data ?? r.data)))
}

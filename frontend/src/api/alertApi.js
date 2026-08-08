import axios from 'axios'
import { withRetry } from './request'
import { cachedOrRequest, invalidateKey } from './cache'

const alertRequest = axios.create({ baseURL: '/api/alerts', timeout: 5000 })
alertRequest.interceptors.response.use(r => r.data?.data ?? r.data)

export const alertApi = {
  list: () => cachedOrRequest('alerts', () => withRetry(() => alertRequest.get('/'))),
  ack:  (id) => {
    invalidateKey('alerts')
    return withRetry(() => alertRequest.post(`/${id}/ack`))
  }
}

import { request, withRetry } from './request'
import { cachedOrRequest } from './cache'

export const dashboardApi = {
  summary:    () => cachedOrRequest('summary',    () => withRetry(() => request.get('/summary'))),
  category:   () => cachedOrRequest('category',   () => withRetry(() => request.get('/category'))),
  alertTrend: () => cachedOrRequest('alertTrend', () => withRetry(() => request.get('/alert-trend'))),
  alertLevel: () => cachedOrRequest('alertLevel', () => withRetry(() => request.get('/alert-level'))),
  flow:       () => cachedOrRequest('flow',       () => withRetry(() => request.get('/flow'))),
  flowTrend:  () => cachedOrRequest('flowTrend',  () => withRetry(() => request.get('/flow-trend')))
}

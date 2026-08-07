import axios from 'axios'

const request = axios.create({
  baseURL: '/api/dashboard',
  timeout: 5000
})

// ========= 客户端 TTL 缓存 =========
const CACHE_TTL = 3_000
const clientCache = new Map()
const pendingReqs = new Map()

function cachedOrRequest(key, fn) {
  const hit = clientCache.get(key)
  if (hit && hit.expireAt > Date.now()) {
    return Promise.resolve(hit.value)
  }
  const pending = pendingReqs.get(key)
  if (pending) return pending

  const p = fn().then(v => {
    clientCache.set(key, { value: v, expireAt: Date.now() + CACHE_TTL })
    pendingReqs.delete(key)
    return v
  }).catch(err => {
    pendingReqs.delete(key)
    throw err
  })
  pendingReqs.set(key, p)
  return p
}

function withRetry(fn, retries = 1) {
  return fn().catch(err => {
    if (retries > 0 && (!err.response || err.code === 'ERR_NETWORK' || err.code === 'ECONNABORTED')) {
      return withRetry(fn, retries - 1)
    }
    throw err
  })
}

request.interceptors.response.use(
  r => r.data?.data ?? r.data,
  err => {
    console.warn('[api]', err.message)
    return Promise.reject(err)
  }
)

export const api = {
  summary: ()    => cachedOrRequest('summary',    () => withRetry(() => request.get('/summary'))),
  category: ()   => cachedOrRequest('category',   () => withRetry(() => request.get('/category'))),
  alertTrend: () => cachedOrRequest('alertTrend', () => withRetry(() => request.get('/alert-trend'))),
  alertLevel: () => cachedOrRequest('alertLevel', () => withRetry(() => request.get('/alert-level'))),
  alerts: ()     => cachedOrRequest('alerts',     () => withRetry(() => request.get('/alerts'))),
  flow: ()       => cachedOrRequest('flow',       () => withRetry(() => request.get('/flow'))),
  flowTrend: ()  => cachedOrRequest('flowTrend',  () => withRetry(() => request.get('/flow-trend'))),
  metrics: ()    => cachedOrRequest('metrics',    () => withRetry(() => request.get('/metrics')))
}

export function clearCache() {
  clientCache.clear()
}

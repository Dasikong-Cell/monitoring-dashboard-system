import axios from 'axios'

export const request = axios.create({
  baseURL: '/api/dashboard',
  timeout: 5000
})

export function withRetry(fn, retries = 1) {
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

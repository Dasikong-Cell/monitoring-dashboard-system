import axios from 'axios'

const request = axios.create({
  baseURL: '/api/dashboard',
  timeout: 8000
})

request.interceptors.response.use(
  r => r.data?.data ?? r.data,
  err => {
    console.error('[api]', err.message)
    return Promise.reject(err)
  }
)

export const api = {
  summary: ()       => request.get('/summary'),
  category: ()      => request.get('/category'),
  alertTrend: ()    => request.get('/alert-trend'),
  alertLevel: ()    => request.get('/alert-level'),
  alerts: ()        => request.get('/alerts'),
  flow: ()          => request.get('/flow'),
  flowTrend: ()     => request.get('/flow-trend'),
  metrics: ()       => request.get('/metrics')
}

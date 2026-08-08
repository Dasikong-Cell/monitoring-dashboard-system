export { dashboardApi } from './dashboardApi'
export { deviceApi } from './deviceApi'
export { alertApi } from './alertApi'
export { monitorApi } from './monitorApi'
export { clearCache } from './cache'

import { dashboardApi } from './dashboardApi'

export const api = {
  summary:    dashboardApi.summary,
  category:   dashboardApi.category,
  alertTrend: dashboardApi.alertTrend,
  alertLevel: dashboardApi.alertLevel,
  alerts:     () => import('./alertApi').then(m => m.alertApi.list()),
  flow:       dashboardApi.flow,
  flowTrend:  dashboardApi.flowTrend,
  metrics:    () => import('./deviceApi').then(m => m.deviceApi.metrics())
}

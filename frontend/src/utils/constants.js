export const ALERT_LEVEL = {
  critical: { label: '紧急', color: '#ff4757' },
  warning:  { label: '普通', color: '#ffa502' },
  info:     { label: '提示', color: '#2ed573' }
}

export const DEVICE_CATEGORY_ZH = {
  camera: '摄像头',
  door: '门禁',
  sensor: '传感器',
  router: '网络设备',
  server: '服务器'
}

export const CHART_COLORS = ['#4facfe', '#00f2fe', '#43e97b', '#fa709a', '#fee140', '#a18cd1']

export const WS_ENDPOINT = (loc) => {
  const proto = loc.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${loc.host}/ws/dashboard`
}

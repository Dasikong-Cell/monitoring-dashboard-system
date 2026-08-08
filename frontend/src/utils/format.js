export function formatBytes(bytes) {
  if (bytes == null) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  let v = bytes
  while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }
  return `${v.toFixed(2)} ${units[i]}`
}

export function formatPercent(n, digits = 1) {
  return `${(n * 100).toFixed(digits)}%`
}

export function formatTime(ts) {
  if (!ts) return '-'
  const d = new Date(typeof ts === 'number' ? ts : ts.replace(' ', 'T'))
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

export function formatDateTime(ts) {
  if (!ts) return '-'
  return String(ts).replace('T', ' ')
}

export function truncate(s, n = 20) {
  if (!s) return ''
  return s.length > n ? s.slice(0, n) + '…' : s
}

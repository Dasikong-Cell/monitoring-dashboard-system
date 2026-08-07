import { api } from '../api/index.js'

export function createWS(url, onMessage) {
  let ws = null
  let closed = false
  let reconnectDelay = 1000
  let heartbeatTimer = null
  let reconnectTimer = null
  let heartbeatMissed = 0

  function startHeartbeat() {
    if (heartbeatTimer) clearInterval(heartbeatTimer)
    heartbeatMissed = 0
    heartbeatTimer = setInterval(() => {
      if (ws?.readyState === WebSocket.OPEN) {
        try { ws.send(JSON.stringify({ type: 'ping', ts: Date.now() })) } catch {}
        heartbeatMissed++
        if (heartbeatMissed > 3) {
          console.warn('[ws] heartbeat missed 3x, forcing reconnect')
          ws.close()
        }
      }
    }, 10000)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) { clearInterval(heartbeatTimer); heartbeatTimer = null }
  }

  function connect() {
    if (closed) return
    try {
      ws = new WebSocket(url)
    } catch (e) {
      fallbackPolling()
      return
    }

    ws.onopen = () => {
      console.log('[ws] connected')
      reconnectDelay = 1000
      if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
      startHeartbeat()
      onMessage({ type: 'status', status: 'connected' })
    }

    ws.onmessage = (e) => {
      heartbeatMissed = 0
      try {
        const data = JSON.parse(e.data)
        if (data.type === 'pong') return
        onMessage(data)
      } catch (err) { console.error('[ws] parse fail', err) }
    }

    ws.onclose = (e) => {
      console.warn('[ws] closed (code=' + e.code + '), reconnect in ' + reconnectDelay + 'ms')
      stopHeartbeat()
      if (!closed) {
        reconnectTimer = setTimeout(connect, reconnectDelay)
        reconnectDelay = Math.min(reconnectDelay * 2, 15_000)
        if (reconnectDelay >= 15_000) {
          console.warn('[ws] too many reconnects, falling back to polling')
          fallbackPolling()
        }
      }
    }

    ws.onerror = () => {
      try { ws?.close() } catch {}
    }
  }

  // 降级：轮询模式
  let pollTimer = null
  let polling = false
  function fallbackPolling() {
    if (polling) return
    polling = true
    onMessage({ type: 'status', status: 'polling' })
    pollTimer = setInterval(async () => {
      try {
        const [s, a, m] = await Promise.all([api.summary(), api.alerts(), api.metrics()])
        onMessage({ summary: s, alerts: a.slice(0, 5), metrics: m.slice(0, 3), tick: Date.now() })
      } catch {}
    }, 5000)
  }

  connect()

  return {
    close() {
      closed = true
      polling = false
      stopHeartbeat()
      if (reconnectTimer) clearTimeout(reconnectTimer)
      if (pollTimer) clearInterval(pollTimer)
      ws?.close()
    },
    forceReconnect() {
      reconnectDelay = 1000
      ws?.close()
    }
  }
}

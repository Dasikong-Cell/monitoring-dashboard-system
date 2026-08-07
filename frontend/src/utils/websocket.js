// WebSocket 自动重连封装
export function createWS(url, onMessage) {
  let ws = null
  let timer = null
  let closed = false

  function connect() {
    if (closed) return
    ws = new WebSocket(url)
    ws.onopen = () => {
      console.log('[ws] connected')
      if (timer) { clearTimeout(timer); timer = null }
    }
    ws.onmessage = (e) => {
      try { onMessage(JSON.parse(e.data)) }
      catch (err) { console.error('[ws] parse fail', err) }
    }
    ws.onclose = () => {
      console.log('[ws] closed, reconnect in 3s...')
      if (!closed) timer = setTimeout(connect, 3000)
    }
    ws.onerror = () => ws?.close()
  }
  connect()

  return {
    close() { closed = true; ws?.close() }
  }
}

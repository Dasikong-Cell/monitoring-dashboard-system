import { reactive } from 'vue'

export const store = reactive({
  connected: false,
  lastTick: null,
  wsError: null,
  deviceCount: 0,
  alertUnacked: 0,

  setConnected(v) { this.connected = v },
  setLastTick(ts) { this.lastTick = ts },
  setWsError(e) { this.wsError = e },
  setDeviceCount(n) { this.deviceCount = n },
  setAlertUnacked(n) { this.alertUnacked = n }
})

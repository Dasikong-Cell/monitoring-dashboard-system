export const CACHE_TTL = 3_000

const clientCache = new Map()
const pendingReqs = new Map()

export function cachedOrRequest(key, fn) {
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

export function clearCache() {
  clientCache.clear()
}

export function invalidateKey(key) {
  clientCache.delete(key)
}

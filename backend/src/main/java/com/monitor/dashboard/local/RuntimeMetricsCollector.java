package com.monitor.dashboard.local;

import java.util.concurrent.atomic.AtomicLong;

public enum RuntimeMetricsCollector {
    INSTANCE;

    private final AtomicLong hitCounter = new AtomicLong();
    private final AtomicLong missCounter = new AtomicLong();
    private final AtomicLong queryTimeSum = new AtomicLong();
    private final AtomicLong queryCount = new AtomicLong();
    private final AtomicLong wsBroadcastCount = new AtomicLong();
    private volatile long lastTickTs = 0;
    private volatile long dbRowsRead = 0;

    public void recordHit() { hitCounter.incrementAndGet(); }
    public void recordMiss() { missCounter.incrementAndGet(); }
    public void recordQuery(long nanos) {
        queryTimeSum.addAndGet(nanos);
        queryCount.incrementAndGet();
    }
    public void recordWsBroadcast() { wsBroadcastCount.incrementAndGet(); }
    public void recordDbRead(long n) { dbRowsRead += n; }
    public void tickNow() { lastTickTs = System.currentTimeMillis(); }

    public long getCacheHits() { return hitCounter.get(); }
    public long getCacheMisses() { return missCounter.get(); }
    public long getTotalQueries() { return queryCount.get(); }
    public long getQueryTimeSumNanos() { return queryTimeSum.get(); }
    public long getWsBroadcastCount() { return wsBroadcastCount.get(); }
    public long getLastTickTs() { return lastTickTs; }
    public long getDbRowsRead() { return dbRowsRead; }

    public double getCacheHitRate() {
        long hits = hitCounter.get();
        long misses = missCounter.get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total * 100 : 0;
    }

    public double getAvgQueryMs() {
        long count = queryCount.get();
        return count > 0 ? queryTimeSum.get() / count / 1_000_000.0 : 0;
    }
}

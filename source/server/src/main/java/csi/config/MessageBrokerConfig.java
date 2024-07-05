package csi.config;

import java.util.concurrent.TimeUnit;

public class MessageBrokerConfig {
    private int pollTimeoutDuration = 120000;
    private TimeUnit pollTimeoutUnit = TimeUnit.MILLISECONDS;
    private long maxCacheSize = 1000;
    private long maxIdleTimeDurationForQueue = 1;
    private TimeUnit maxIdleTimeUnitForQueue = TimeUnit.HOURS;
    private int initialQueueSize = 5000;

    public int getPollTimeoutDuration() {
        return pollTimeoutDuration;
    }

    public void setPollTimeoutDuration(int pollTimeoutDuration) {
        this.pollTimeoutDuration = pollTimeoutDuration;
    }

    public TimeUnit getPollTimeoutUnit() {
        return pollTimeoutUnit;
    }

    public void setPollTimeoutUnit(TimeUnit pollTimeoutUnit) {
        this.pollTimeoutUnit = pollTimeoutUnit;
    }

    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public long getMaxIdleTimeDurationForQueue() {
        return maxIdleTimeDurationForQueue;
    }

    public void setMaxIdleTimeDurationForQueue(long maxIdleTimeDurationForQueue) {
        this.maxIdleTimeDurationForQueue = maxIdleTimeDurationForQueue;
    }

    public TimeUnit getMaxIdleTimeUnitForQueue() {
        return maxIdleTimeUnitForQueue;
    }

    public void setMaxIdleTimeUnitForQueue(TimeUnit maxIdleTimeUnitForQueue) {
        this.maxIdleTimeUnitForQueue = maxIdleTimeUnitForQueue;
    }

    public int getInitialQueueSize() {
        return initialQueueSize;
    }

    public void setInitialQueueSize(int initialQueueSize) {
        this.initialQueueSize = initialQueueSize;
    }
}

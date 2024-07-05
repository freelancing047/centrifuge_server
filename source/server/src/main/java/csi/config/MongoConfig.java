package csi.config;

public class MongoConfig
        extends AbstractConfigurationSettings {

    static final int DEFAULT_START_ATTEMPTS = 3;
    static final int DEFAULT_PING_ATTEMPTS = 5;
    static final int DEFAULT_PING_WAIT_TIME = 2000;

    private String mongoUrl;
    private boolean shutdown = true;
    private int startAttempts;
    private int pingAttempts;
    private long pingWaitTime;

    public String getMongoUrl() {
        return mongoUrl;
    }

    public Boolean getShutdown() {
        return shutdown;
    }

    /**
     * @return the startAttempts
     */
    public int getStartAttempts() {
        return (startAttempts > 0) ? startAttempts : DEFAULT_START_ATTEMPTS;
    }

    /**
     * @return the pingAttempts
     */
    public int getPingAttempts() {
        return (pingAttempts > 0) ? pingAttempts : DEFAULT_PING_ATTEMPTS;
    }

    /**
     * @return the pingWaitTime
     */
    public long getPingWaitTime() {
        return (pingWaitTime > 0) ? pingWaitTime : DEFAULT_PING_WAIT_TIME;
    }

    public void setMongoUrl(String mongoUrl) {
        this.mongoUrl = mongoUrl;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public void setStartAttempts(int startAttempts) {
        this.startAttempts = startAttempts;
    }

    public void setPingAttempts(int pingAttempts) {
        this.pingAttempts = pingAttempts;
    }

    public void setPingWaitTime(long pingWaitTime) {
        this.pingWaitTime = pingWaitTime;
    }
}

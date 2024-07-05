package csi.server.business.cachedb;

public interface CacheDataProducer {

    public abstract long getRowCount();
    public abstract Exception getException();

    // From Thread

    public abstract void run();
    public boolean isAlive();
    public void join() throws InterruptedException;
    public void join(long millisecondsIn) throws InterruptedException;
    public void handleCancel(boolean flagIn);
}
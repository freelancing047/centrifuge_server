package csi.config;

public class TaskManagerConfig
    extends AbstractConfigurationSettings
{
    private int minimumThreads = 1;
    private int maximumThreads = 5;

    public int getMinimumThreads() {
        return minimumThreads;
    }

    public void setMinimumThreads(int minimumThreads) {
        this.minimumThreads = minimumThreads;
    }

    public int getMaximumThreads() {
        return maximumThreads;
    }

    public void setMaximumThreads(int maximumThreads) {
        this.maximumThreads = maximumThreads;
    }
}

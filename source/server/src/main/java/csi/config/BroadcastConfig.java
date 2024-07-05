package csi.config;

public class BroadcastConfig
    extends AbstractConfigurationSettings
{
    private boolean listenByDefault = true;

    public boolean isListenByDefault() {
        return listenByDefault;
    }

    public void setListenByDefault(boolean listenByDefault) {
        this.listenByDefault = listenByDefault;
    }
}

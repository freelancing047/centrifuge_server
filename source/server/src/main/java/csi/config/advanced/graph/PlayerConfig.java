package csi.config.advanced.graph;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by Patrick on 4/25/2014.
 */
public class PlayerConfig implements IsSerializable {
    private ControlBarConfig controlBarConfig;

    public ControlBarConfig getControlBarConfig() {
        return controlBarConfig;
    }

    public void setControlBarConfig(ControlBarConfig controlBarConfig) {
        this.controlBarConfig = controlBarConfig;
    }
}

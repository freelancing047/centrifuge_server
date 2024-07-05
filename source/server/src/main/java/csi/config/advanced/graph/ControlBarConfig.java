package csi.config.advanced.graph;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by Patrick on 4/25/2014.
 */
public class ControlBarConfig implements IsSerializable{
    private String startColor;
    private String endColor;
    private String viewColor;

    public String getStartColor() {
        return startColor;
    }

    public void setStartColor(String startColor) {
        this.startColor = startColor;
    }

    public void setEndColor(String endColor) {
        this.endColor = endColor;
    }

    public String getEndColor() {
        return endColor;
    }

    public void setPositionColor(String positionColor) {
        this.viewColor = positionColor;
    }

    public String getPositionColor() {
        return viewColor;
    }
}

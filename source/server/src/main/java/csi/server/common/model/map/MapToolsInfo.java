package csi.server.common.model.map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MapToolsInfo implements IsSerializable {
    private boolean legendEnabled;
    private boolean legendShown;
    private boolean multitypeDecoratorEnabled;
    private boolean multitypeDecoratorShown;
    private boolean linkupDecoratorEnabled;

    public MapToolsInfo() {
    }

    public boolean isLegendEnabled() {
        return legendEnabled;
    }

    public void setLegendEnabled(boolean legendEnabled) {
        this.legendEnabled = legendEnabled;
    }

    public boolean isLegendShown() {
        return legendShown;
    }

    public void setLegendShown(boolean legendShown) {
        this.legendShown = legendShown;
    }

    public boolean isMultitypeDecoratorEnabled() {
        return multitypeDecoratorEnabled;
    }

    public void setMultitypeDecoratorEnabled(boolean multitypeDecoratorEnabled) {
        this.multitypeDecoratorEnabled = multitypeDecoratorEnabled;
    }

    public boolean isMultitypeDecoratorShown() {
        return multitypeDecoratorShown;
    }

    public void setMultitypeDecoratorShown(boolean multitypeDecoratorShown) {
        this.multitypeDecoratorShown = multitypeDecoratorShown;
    }

    public boolean isLinkupDecoratorEnabled() {
        return linkupDecoratorEnabled;
    }

    public void setLinkupDecoratorEnabled(boolean linkupDecoratorEnabled) {
        this.linkupDecoratorEnabled = linkupDecoratorEnabled;
    }
}

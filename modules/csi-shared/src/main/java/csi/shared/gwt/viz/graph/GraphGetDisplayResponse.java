package csi.shared.gwt.viz.graph;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GraphGetDisplayResponse implements IsSerializable {
    String encodedDisplay;
    Boolean hiddenItems;

    public GraphGetDisplayResponse() {
    }

    public String getEncodedDisplay() {
        return encodedDisplay;
    }

    public void setEncodedDisplay(String encodedDisplay) {
        this.encodedDisplay = encodedDisplay;
    }

    public Boolean getHiddenItems() {
        return hiddenItems;
    }

    public void setHiddenItems(Boolean hiddenItems) {
        this.hiddenItems = hiddenItems;
    }

    public GraphGetDisplayResponse(String encodedDisplay, Boolean hiddenItems) {
        this.encodedDisplay = encodedDisplay;
        this.hiddenItems = hiddenItems;
    }
}

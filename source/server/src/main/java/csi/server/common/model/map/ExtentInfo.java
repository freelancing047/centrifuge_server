package csi.server.common.model.map;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class ExtentInfo implements IsSerializable, Serializable {
    private Extent initialExtent;
    private Extent extent;
    private boolean newExtent = true;
    private boolean placeTypeLimitReached = false;
    private boolean trackTypeLimitReached = false;

    public ExtentInfo() {
    }

    public Extent getInitialExtent() {
        return initialExtent;
    }

    public void setInitialExtent(Extent initialExtent) {
        this.initialExtent = initialExtent;
    }

    public Extent getExtent() {
        return extent;
    }

    public void setExtent(Extent extent) {
        this.extent = extent;
    }

    public boolean isNewExtent() {
        return newExtent;
    }

    public void setNewExtent(boolean newExtent) {
        this.newExtent = newExtent;
    }

    public boolean isPlaceTypeLimitReached() {
        return placeTypeLimitReached;
    }

    public void setPlaceTypeLimitReached(boolean placeTypeLimitReached) {
        this.placeTypeLimitReached = placeTypeLimitReached;
    }

    public boolean isTrackTypeLimitReached() {
        return trackTypeLimitReached;
    }

    public void setTrackTypeLimitReached(boolean trackTypeLimitReached) {
        this.trackTypeLimitReached = trackTypeLimitReached;
    }
}

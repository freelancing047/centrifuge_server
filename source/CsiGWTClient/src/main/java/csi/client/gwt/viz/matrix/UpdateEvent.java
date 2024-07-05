package csi.client.gwt.viz.matrix;

import com.google.gwt.event.shared.GwtEvent;

public class UpdateEvent extends GwtEvent<MetricsNeedUpdateHandler> {
    String visUuid;
    boolean force = false;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public UpdateEvent(String vizUUid) {
        visUuid = vizUUid;
    }

    public UpdateEvent(String vizUUid, boolean force) {
        visUuid = vizUUid;
    }

    public String getVisUuid() {
        return visUuid;
    }

    public void setVisUuid(String visUuid) {
        this.visUuid = visUuid;
    }

    public static Type<MetricsNeedUpdateHandler> TYPE = new Type<MetricsNeedUpdateHandler>();

    @Override
    public Type<MetricsNeedUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MetricsNeedUpdateHandler handler) {
        handler.onMetricsNeedsUpdate(this);
    }
}

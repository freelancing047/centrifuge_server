package csi.client.gwt.viz.matrix;

import com.google.gwt.event.shared.GwtEvent;

public class ExpireMetrics extends GwtEvent<ExpireMetricsHandler> {
    String visUuid;
    public static Type<ExpireMetricsHandler> TYPE = new Type<ExpireMetricsHandler>();
    public ExpireMetrics(String vizUUid) {
        visUuid = vizUUid;
    }
    @Override
    public Type<ExpireMetricsHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ExpireMetricsHandler handler) {
        handler.onExpireMetrics(this);
    }

    public String getVisUuid() {
        return visUuid;
    }

    public void setVisUuid(String visUuid) {
        this.visUuid = visUuid;
    }
}

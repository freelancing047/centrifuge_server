package csi.client.gwt.viz.matrix;

import com.google.gwt.event.shared.GwtEvent;

public class HideMetricsEvent extends GwtEvent<MetricsHideHandler> {
    String visUuid;

    public HideMetricsEvent(String vizUUid) {
        visUuid = vizUUid;
    }


    public String getVisUuid() {
        return visUuid;
    }

    public void setVisUuid(String visUuid) {
        this.visUuid = visUuid;
    }

    public static Type<MetricsHideHandler> TYPE = new Type<MetricsHideHandler>();

    @Override
    public Type<MetricsHideHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MetricsHideHandler handler) {
        handler.onHideMetrics(this);
    }
}

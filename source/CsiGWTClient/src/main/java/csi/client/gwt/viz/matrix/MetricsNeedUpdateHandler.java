package csi.client.gwt.viz.matrix;

import com.google.gwt.event.shared.EventHandler;

public interface MetricsNeedUpdateHandler extends EventHandler {
    void onMetricsNeedsUpdate(UpdateEvent event);
}

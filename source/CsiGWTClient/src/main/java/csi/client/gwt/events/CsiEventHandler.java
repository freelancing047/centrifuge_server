package csi.client.gwt.events;

import com.google.gwt.event.shared.EventHandler;

public interface CsiEventHandler extends EventHandler {

    public void onCsiEvent(CsiEvent event);
}

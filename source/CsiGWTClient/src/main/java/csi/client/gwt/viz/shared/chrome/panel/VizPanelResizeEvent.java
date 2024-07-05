package csi.client.gwt.viz.shared.chrome.panel;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class VizPanelResizeEvent extends BaseCsiEvent<VizPanelResizeEventHandler>{

	public static final GwtEvent.Type<VizPanelResizeEventHandler> type = new GwtEvent.Type<VizPanelResizeEventHandler>();

    @Override
	public com.google.gwt.event.shared.GwtEvent.Type<VizPanelResizeEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(VizPanelResizeEventHandler handler) {
		handler.onResize(this);
	}

}

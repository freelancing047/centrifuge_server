package csi.client.gwt.viz.chart.overview.view.content;

import com.google.gwt.event.dom.client.DomEvent.Type;

import csi.client.gwt.etc.BaseCsiEventHandler;

public class PostRenderingHandler extends BaseCsiEventHandler {
    /**
     * Event type for mouse down events. Represents the meta-data associated with
     * this event.
     */
    private static final Type<PostRenderingHandler> TYPE = new Type<PostRenderingHandler>(null, null);

    /**
     * Gets the event type associated with mouse down events.
     * 
     * @return the handler type
     */
    public static Type<PostRenderingHandler> getType() {
      return TYPE;
    }
}

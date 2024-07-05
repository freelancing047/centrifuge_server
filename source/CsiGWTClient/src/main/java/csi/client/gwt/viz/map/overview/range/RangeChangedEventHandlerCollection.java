package csi.client.gwt.viz.map.overview.range;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RangeChangedEventHandlerCollection {

    private final List<RangeChangedEventHandler> handlers = new ArrayList<RangeChangedEventHandler>();

    public void addHandler(RangeChangedEventHandler handler){
        handlers.add(handler);
    }

    public void clear(){
        handlers.clear();
    }

    public void fireEvent(RangeChangedEvent event){
        for (RangeChangedEventHandler handler : handlers) {
            handler.onRangeChanged(event);
        }
    }
}

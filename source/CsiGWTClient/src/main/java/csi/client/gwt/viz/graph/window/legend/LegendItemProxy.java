package csi.client.gwt.viz.graph.window.legend;

import com.google.gwt.user.client.ui.IsWidget;

public interface LegendItemProxy extends IsWidget{
    String getKey();
    String getType();
    String getImageUrl();
}

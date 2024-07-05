package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;

public interface VizButtonHandler {

    void bind(Button button);

    String getTooltipText();
}

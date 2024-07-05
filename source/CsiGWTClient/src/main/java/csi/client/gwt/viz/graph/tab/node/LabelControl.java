package csi.client.gwt.viz.graph.tab.node;

import com.github.gwtbootstrap.client.ui.Controls;

public class LabelControl extends Controls {

    private Filters filter;

    public LabelControl(Filters filterType) {
        this.filter = filterType;
    }

    public Filters getFilter() {
        return filter;
    }
}

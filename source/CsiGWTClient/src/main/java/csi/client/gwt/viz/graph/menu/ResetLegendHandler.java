package csi.client.gwt.viz.graph.menu;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;

public class ResetLegendHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public ResetLegendHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }



    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        Graph graph = getPresenter();
        graph.showLegend();
        graph.getGraphLegend().show();
        ContentPanel legendAsWindow = graph.getGraphLegend().getLegendAsWindow();
        Widget parent = legendAsWindow.getParent();
        if(parent != null)
            ((AbsolutePanel) parent).setWidgetPosition(legendAsWindow, parent.getElement().getOffsetWidth() - 64- 25-legendAsWindow.getOffsetWidth(), 25);

    }

}

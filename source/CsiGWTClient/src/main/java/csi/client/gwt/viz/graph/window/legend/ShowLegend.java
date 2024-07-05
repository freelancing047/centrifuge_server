package csi.client.gwt.viz.graph.window.legend;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sencha.gxt.widget.core.client.ContentPanel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

class ShowLegend implements GraphLegend.Presenter {

    private GraphLegend graphLegend;

    public ShowLegend(GraphLegend graphLegend) {
        this.graphLegend = graphLegend;
    }


    @Override
    public String mayStop() {
        return null;
    }


    @Override
    public void onCancel() {
    }


    @Override
    public void onStop() {
    }


    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        if (panel instanceof ContentPanel) {
            ((ContentPanel) panel).setHeading(CentrifugeConstantsLocator.get().menuKeyConstants_show_legend());
        }
    }
}

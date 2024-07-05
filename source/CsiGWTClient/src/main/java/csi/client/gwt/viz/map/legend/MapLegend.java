package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.ContentPanel;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.client.gwt.viz.map.presenter.MapPresenter;

import java.util.Map;

public interface MapLegend extends IsWidget {
    MapLegend getLegend();

    void setSequenceNumber(Integer sequenceNumber);

    void load();

    void hide();

    void show();

    void clear();

    boolean isVisible();

    ContentPanel getLegendAsWindow();

    MapPresenter getMapPresenter();

    void showAndPositionLegend();

    void positionLegend();

    void setLegendPositionAnchored(boolean value);

    int[] getLegendPosition();

    Map<String, String> getVisItems();

    void updateCombinedPlaceIconStatus(boolean isVisible);

    interface View extends IsWidget {
        void clear();

        void showLinkLimitReachedMessage(boolean isLinkLimitReached);

        void addLegendItem(LegendItemProxy item, boolean draggable);

        void setScrollParent(XElement body);

        FluidContainer getFluidContainer();
    }

    interface Presenter extends Activity {
    }
}

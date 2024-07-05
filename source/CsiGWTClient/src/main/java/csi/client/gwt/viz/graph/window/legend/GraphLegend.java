package csi.client.gwt.viz.graph.window.legend;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.ContentPanel;

import csi.client.gwt.viz.graph.Graph;

public interface GraphLegend extends IsWidget {

    void setRightOffset(int i);

    int getRightOffset();

    interface View extends IsWidget {

        void addLegendItem(LegendItemProxy item);

        void clear();

        FluidContainer getFluidContainer();

        void updateOrder(List<String> itemOrderList);

        void setParent(XElement body);

        List<LegendItemProxy> getNodeLegendItems(Set<String> types);
        List<LegendItemProxy> getLinkLegendItems(Set<String> types);

    }

    interface Presenter extends Activity {

    }

    interface Model {

    }

    GraphLegend getLegend();

    void load();

    Map<String, String> getVisItems();

    ContentPanel getLegendAsWindow();

    void hide();

    void show();

    boolean isHidden();

    Graph getGraph();

    boolean hasMultiType();

    List<LegendItemProxy> getMatchingItems(Set<String> types, boolean b);

    void addCommonItem();

    void addNewlyAdded();
}

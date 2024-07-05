package csi.client.gwt.viz.graph.surface.multitypes;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.graph.surface.GraphSurface.View;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.shared.gwt.viz.graph.MultiTypeInfo;

public class MultiTypePresenter {
    
    private MultiTypeView view;

    public void showTypes(View graphSurface, MultiTypeInfo result, List<LegendItemProxy> items) {
        
        view = new MultiTypeView();
        
        view.create(items);
        view.setPosition(result.getX(), result.getY());
        
        
    }

    public Widget getView() {
        
        return view.asWidget();
    }

    
    
}

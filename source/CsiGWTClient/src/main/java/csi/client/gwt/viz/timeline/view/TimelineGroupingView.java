package csi.client.gwt.viz.timeline.view;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.viz.timeline.model.GroupingItem;
import csi.client.gwt.viz.timeline.presenters.legend.TimelineGroupLegendPresenter;
import csi.client.gwt.viz.timeline.view.drawing.TimelineTrackRenderable;

public class TimelineGroupingView extends Composite implements IsWidget {
    
    private FluidContainer fluidContainer;
    
    private TimelineGroupLegendPresenter presenter;
    
    private boolean hasItems = false;
    
    public TimelineGroupingView(TimelineGroupLegendPresenter presenter){
        this.presenter = presenter;
        fluidContainer = new FluidContainer();
        fluidContainer.addStyleName("legend-container");//NON-NLS
        initWidget(fluidContainer);
        
        
    }
    
    public void addItem(IsWidget widget){
        hasItems = true;
        String label = ((GroupingItem)widget).getText();
        
        if(label.isEmpty()){
            ((GroupingItem)widget).italicize(TimelineTrackRenderable.NO_VALUE);
        }
        
        widget.asWidget().addDomHandler(new GroupItemClickHandler(widget, presenter, label),
                ClickEvent.getType());
        fluidContainer.add(widget.asWidget());
    }

    public void clear() {
        hasItems = false;
        fluidContainer.clear();
    }

    public boolean hasItems() {
        return hasItems;
    }
}

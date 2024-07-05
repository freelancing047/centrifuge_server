package csi.client.gwt.viz.timeline.view;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.core.client.dom.XElement;

import csi.client.gwt.viz.graph.window.legend.DndFluidContainer;
import csi.client.gwt.viz.graph.window.legend.DropCompleteEvent;
import csi.client.gwt.viz.graph.window.legend.DropCompleteEventHandler;
import csi.client.gwt.viz.shared.ExportableLegend;
import csi.client.gwt.viz.timeline.model.LegendItem;
import csi.client.gwt.viz.timeline.presenters.legend.TimelineColorLegendPresenter;

public class TimelineLegendView extends ExportableLegend implements IsWidget {
    private TimelineColorLegendPresenter presenter;
    
    private boolean hasItems = false;
    
    public TimelineLegendView(TimelineColorLegendPresenter presenter, boolean readOnly){
        super();

        this.presenter = presenter;

        
        if(readOnly){
            fluidContainer = new FluidContainer();
            fluidContainer.addStyleName("legend-container");//NON-NLS
            // TODO: need to set min-width to 120px
            initWidget(fluidContainer);

        }else {
            fluidContainer = new DndFluidContainer();
            
            fluidContainer.addStyleName("legend-container");//NON-NLS
            // TODO: need to set min-width to 120px
            initWidget(fluidContainer);
            ((DndFluidContainer) fluidContainer).addDropHandler(new DropCompleteEventHandler() {

                @Override
                public void onDropComplete(DropCompleteEvent event) {
                    LegendItem proxy = (LegendItem) event.getWidget();
                    if (proxy != null) {
                        presenter.updateLegendModel(event.getPosition(), proxy.getText());
                    }

                }
            });
        }
        
    }

    public void setParent(XElement display) {

        if(fluidContainer instanceof DndFluidContainer)
            ((DndFluidContainer) fluidContainer).setScrollParent(display);
    }
    
    public void addLegendItem(IsWidget widget){
        hasItems = true;
        widget.asWidget().setTitle(((LegendItem)widget).getText());
        widget.asWidget().addDomHandler(new LegendItemClickHandler(presenter, ((LegendItem)widget).getText()), ClickEvent.getType());

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

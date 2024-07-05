package csi.client.gwt.viz.graph.window.legend;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;

import csi.client.gwt.events.HoverEvent;
import csi.client.gwt.events.HoverEventHandler;
import csi.server.business.visualization.graph.base.ObjectAttributes;

public class GraphLegendViewImpl extends Composite implements GraphLegend.View {

    @UiField
    FluidContainer fluidContainer;

    private GraphLegendImpl graphLegend;

    private boolean isReadOnly = false;

    public GraphLegendViewImpl(GraphLegendImpl graphLegend, boolean readOnly) {
        this.graphLegend = graphLegend;
        this.isReadOnly = readOnly;
        if(readOnly){
            this.graphLegend = graphLegend;
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
                    LegendItemProxy proxy = (LegendItemProxy) event.getWidget();
                    if (proxy != null) {
                        GraphLegendViewImpl.this.graphLegend.updateLegendModel(event.getPosition(), proxy.getKey());
                    }

                }
            });
        }

    }


    public FluidContainer getFluidContainer() {
        return fluidContainer;
    }

    @Override
    public void addLegendItem(final LegendItemProxy item) {
        if ((item instanceof NodeLegendItemProxy) ||
                (item instanceof InCommonLegendItem) ||
                (item instanceof NewlyAddedLegendItem) ||
                (item instanceof MultiTypeLegendItem)) {

        item.asWidget().addDomHandler(new NodeLegendItemClickHandler(graphLegend.getGraph(), item.getKey().replaceAll(ObjectAttributes.NODES_OBJECT_TYPE, "")),
                ClickEvent.getType());
        };

        item.asWidget().setTitle(item.getType());

        if (item instanceof LinkLegendItemProxy ||
                item instanceof MultiTypeLinkLegendItem ||
                (item instanceof InCommonLinkLegendItem) ||
                (item instanceof NewlyAddedLinkLegendItem)) {
            item.asWidget().addDomHandler(new LinkLegendItemClickHandler(graphLegend.getGraph(), item.getKey().replaceAll(ObjectAttributes.EDGES_OBJECT_TYPE, "")),
                    ClickEvent.getType());
        }
        item.asWidget().addStyleName("legend-item");
        if(!isReadOnly) {
            ((DndFluidContainer) fluidContainer).add(item.asWidget());
        }else{
            fluidContainer.add(item.asWidget());
        }
        
    }

    @Override
    public void clear() {
        fluidContainer.clear();

    }


    @Override
    public void updateOrder(List<String> itemOrderList) {

        if(itemOrderList == null || itemOrderList.isEmpty() || isReadOnly){
            
        } else {
        
            int widgetCount = fluidContainer.getWidgetCount();
            List<Widget> orderedWidgets = new ArrayList<Widget>();
            for(String item: itemOrderList){
                for(int ii=widgetCount-1; ii>=0; ii--){
                    Widget widget = fluidContainer.getWidget(ii);
                    if(widget instanceof LegendItemProxy){
                        
                        String key = ((LegendItemProxy)widget).getKey();
                        if(key.equals(item)){
                            orderedWidgets.add(widget);
                            break;
                        }
                    }
                }
            }
            
            clear();
            for(Widget widget: orderedWidgets){
                ((DndFluidContainer)fluidContainer).addNoHandler(widget);
            }
        }
        
    }




    @Override
    public void setParent(XElement display) {

        if(fluidContainer instanceof DndFluidContainer)
            ((DndFluidContainer) fluidContainer).setScrollParent(display);
    }




    @Override
    public List<LegendItemProxy> getNodeLegendItems(Set<String> types) {
        
        int widgetCount = fluidContainer.getWidgetCount();
        List<LegendItemProxy> matchedValues = new ArrayList<LegendItemProxy>();
        for(String type: types){
            String typeKey = ObjectAttributes.NODES_OBJECT_TYPE + type;
            for(int ii=widgetCount-1; ii>=0; ii--){
                Widget widget = fluidContainer.getWidget(ii);
                if(widget instanceof LegendItemProxy){
                    
                    LegendItemProxy legendItemProxy = (LegendItemProxy)widget;
                    String key = legendItemProxy.getKey();
                    if(key.equals(typeKey)){
                        matchedValues.add(legendItemProxy);
                        break;
                    }
                }
            }
        }
        
        return matchedValues;
    }
    
    @Override
    public List<LegendItemProxy> getLinkLegendItems(Set<String> types) {
        
        int widgetCount = fluidContainer.getWidgetCount();
        List<LegendItemProxy> matchedValues = new ArrayList<LegendItemProxy>();
        for(String type: types){
            String typeKey = ObjectAttributes.EDGES_OBJECT_TYPE+ type;
            for(int ii=widgetCount-1; ii>=0; ii--){
                Widget widget = fluidContainer.getWidget(ii);
                if(widget instanceof LegendItemProxy){

                    LegendItemProxy legendItemProxy = (LegendItemProxy)widget;
                    String key = legendItemProxy.getKey();
                    if(key.equals(typeKey)){
                        matchedValues.add(legendItemProxy);
                        break;
                    }
                }
            }
        }
        
        return matchedValues;
    }
}

package csi.client.gwt.viz.graph.surface;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.shared.gwt.viz.graph.MultiTypeInfo;

class ExpandItem extends AbstractGraphSurfaceActivity {

    private int mouseX;
    private int mouseY;

    public ExpandItem(GraphSurface graphSurface) {
        super(graphSurface);
    }

    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
    //        //FIXME: Perhaps I do not need to refresh every time?
    //        graphSurface.refresh(selectPointRequest);
        
        if(graphSurface.getGraph().getLegend() != null && graphSurface.getGraph().getLegend().hasMultiType()){
            
            VortexFuture<MultiTypeInfo> itemInfo = graphSurface.getGraph().getModel()
                    .findItemTypes(mouseX, mouseY);
            
            itemInfo.addEventHandler(new AbstractVortexEventHandler<MultiTypeInfo>(){

                @Override
                public void onSuccess(MultiTypeInfo result) {
                    //result.getTooltips();
                    if(result != null){
                        graphSurface.showMultiTypes(result);
                    }
                }});
            
        }
    }
}

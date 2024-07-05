package csi.client.gwt.viz.graph.surface;

import java.util.List;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.viz.graph.surface.GraphSurface.View;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.DragStartDTO;
import csi.server.common.dto.graph.gwt.NodeMapDTO;

class DragItems extends AbstractGraphSurfaceActivity {

    protected boolean dragImageReady = false;
    private int dragX;
    private int dragY;
    private int startY;
    private int startX;

    public DragItems(GraphSurface graphSurface) {
        super(graphSurface);
    }

    @Override
    public void drag(int deltaX, int deltaY) {
        Model model = graphSurface.getModel();
        View view = graphSurface.getView();
        dragX += deltaX;
        dragY += deltaY;
        if (dragImageReady) {
            if (model.getDragStartDTO() != null) {
                int x = model.getDragStartDTO().imageX;
                int y = model.getDragStartDTO().imageY;
                // view.removeFromGraph(x, y, dragImage);
                List<NodeMapDTO> dragNodes = model.getDragStartDTO().getDragNodes();
                view.drawDragImage(dragX + x, dragY + y, model.getDragImage(), dragNodes);
            }
            // TODO: on the next pass i should translate(x,y) the drag items rather than redraw.
        }
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        int mouseX = mouseHandler.getMouseX();
        int mouseY = mouseHandler.getMouseY();
        startDrag(mouseX, mouseY);
        // TODO:change to hide. will may need to update location (stick not string)
        graphSurface.getToolTipManager().removeAllToolTips();
    }

    @Override
    public void startDrag(int mouseX, int mouseY) {
        startX = mouseX;
        startY = mouseY;
        Model model = graphSurface.getModel();
        VortexFuture<DragStartDTO> dragStartFuture = model.dragStart(mouseX, mouseY);
        dragStartFuture.addEventHandler(new AbstractVortexEventHandler<DragStartDTO>() {

            @Override
            public void onSuccess(DragStartDTO result) {
                if (result.getDragNodes().isEmpty()) {
                    graphSurface.refresh();
                    return;
                }
                if (!dragImageReady) {
                    graphSurface.getModel().dragImage().addLoadHandler(new LoadHandler() {

                        @SuppressWarnings("unused")
                        @Override
                        public void onLoad(LoadEvent event) {
                            dragImageReady = true;
                        }
                    });
                }

            }
        });
    }

    private boolean dragRefresh = true;
    @Override
    public void stopDrag(int mouseX, int mouseY) {
        int x;
        int y;
        dragRefresh = true;
        Model model = graphSurface.getModel();
        DragStartDTO dragStartDTO = model.getDragStartDTO();
        if (dragStartDTO != null) {
            x = dragStartDTO.getImageX();
            y = dragStartDTO.getImageY();
            model.dragEnd(x + (mouseX - startX), y + (mouseY - startY), new AbstractVortexEventHandler<String>(){

                @Override
                public void onSuccess(String result) {
                    graphSurface.refresh();
                    dragRefresh = false;
                } 
            
            @Override
            public boolean onError(Throwable t) {
                return false;
            }}
            );
        }
            
//        if(dragRefresh)
//            graphSurface.refresh();
        
        
    }
}
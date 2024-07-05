package csi.client.gwt.viz.graph.surface;

import java.io.IOException;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.DragStartDTO;
import csi.server.common.dto.graph.gwt.PlunkLinkDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.shared.gwt.viz.graph.GraphGetDisplayResponse;

class DisplayProxy implements GraphSurface.Model {

    protected CssColor backgroundColor;
    private Image dragImage;
    private GraphSurface graphSurface;
    private int width;
    private int height;
    private Image display;
    private int xOffset = 0;
    private int yOffset = 0;
    private double zoom = 1;
    protected DragStartDTO dragStartDTO;
    private VortexEventHandler<GraphGetDisplayResponse> getDisplayCallback = new AbstractVortexEventHandler<GraphGetDisplayResponse>() {

        @Override
        public void onSuccess(GraphGetDisplayResponse result) {
            String encodedDisplay = result.getEncodedDisplay();
            successHandler.onLoad(null);
            if (display != null) {
                RootPanel.get().add(display);
                if (encodedDisplay != null) {
                    display.setUrl(encodedDisplay);
                    // NOTE:I would like to call this, but it breaks IE9/IE10
                    // display.setVisible(false);
                    display.getElement().getStyle().setPosition(Position.ABSOLUTE);
                    display.getElement().getStyle().setTop(-10000, Unit.PX);
                    display.getElement().getStyle().setLeft(-10000, Unit.PX);
                }
            }
            graphSurface.getGraph().setHiddenItemIndicator(result.getHiddenItems());
        }
        
        @Override
        public boolean onError(Throwable t){
            errorHandler.onLoad(null);
            return false;
            
        }
    };
    private LoadHandler errorHandler;
    private LoadHandler successHandler;

    @Override
    public DragStartDTO getDragStartDTO() {
        return dragStartDTO;
    }

    DisplayProxy(GraphSurface graphSurface, int width, int height) {
        this.graphSurface = graphSurface;
        this.width = width;
        this.height = height;
        display = new Image();
        
                
        try {
            VortexFuture<GraphGetDisplayResponse> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(getDisplayCallback);
            future.execute(GraphActionServiceProtocol.class).getDisplay(graphSurface.getVizUuid(), "" + width, "" + height);
        } catch (Exception exception) {
        }
    }

    @Override
    public void dragEnd(int x, int y, VortexEventHandler<String> handler) {
        String endx = "" + x;
        String endy = "" + y;
        try {
            VortexFuture<String> future = WebMain.injector.getVortex().createFuture();

            future.addEventHandler(handler);
            future.execute(GraphActionServiceProtocol.class).dragEnd(endx, endy, graphSurface.getVizUuid());
//            WebMain.injector.getVortex().execute(GraphActionServiceProtocol.class)
//                    .dragEnd(endx, endy, graphSurface.getVizUuid());
        } catch (CentrifugeException ignored) {

        }
        if (dragImage != null) {
            RootPanel.get().remove(dragImage.asWidget());
        }
    }

    @Override
    public Image dragImage() {
        dragImage = new Image();
        try {
            WebMain.injector.getVortex().execute(new Callback<String>() {

                @Override
                public void onSuccess(String result) {
                    dragImage.setUrl(result);
                    RootPanel.get().add(dragImage);
                    // move off screen
                    dragImage.getElement().getStyle().setPosition(Position.ABSOLUTE);
                    dragImage.getElement().getStyle().setTop(-10000, Unit.PX);
                    dragImage.getElement().getStyle().setLeft(-10000, Unit.PX);
                    // dragImage.setVisible(false);
                }
            }, GraphActionServiceProtocol.class).getDragImage(graphSurface.getVizUuid());
        } catch (CentrifugeException ignored) {
        } catch (IOException ignored) {
        }
        return dragImage;
    }

    @Override
    public VortexFuture<DragStartDTO> dragStart(int x, int y) {
        dragStartDTO = null;
        String startx = "" + x;
        String starty = "" + y;
        VortexFuture<DragStartDTO> vortexFuture = WebMain.injector.getVortex().createFuture();
        try{
        vortexFuture.execute(GraphActionServiceProtocol.class).gwtDragStart(startx, starty, graphSurface.getVizUuid());
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<DragStartDTO>() {

            @Override
            public void onSuccess(DragStartDTO result) {
                dragStartDTO = result;
            }
        });
        } catch(Exception exception){
            //ignored
        }
        return vortexFuture;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Image getImage() {
        return display;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public double getxOffset() {
        return xOffset;
    }

    @Override
    public double getyOffset() {
        return yOffset;
    }

    @Override
    public double getZoom() {
        return zoom;
    }

    @Override
    public VortexFuture<Boolean> shouldFitToSizeBasedOnBounds(int offsetWidth, int offsetHeight) {
        final VortexFuture<Boolean> vortexFuture = WebMain.injector.getVortex().createFuture();
        try{
        vortexFuture.execute(GraphActionServiceProtocol.class).ensureViewport(graphSurface.getVizUuid(), offsetWidth, offsetHeight);
        } catch(Exception exception){
            //ignore
        }
        return vortexFuture;
    }

    @Override
    public VortexFuture<Void> pan(int deltaX, int deltaY) {
        final VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        xOffset += deltaX;
        yOffset += deltaY;
        try{
            vortexFuture.execute(GraphActionServiceProtocol.class).panTo(xOffset, yOffset, graphSurface.getVizUuid());
        } catch(Exception exception){
            //ignored
        }
        return vortexFuture;
    }

    @Override
    public void setxOffset(double xOffset) {
        this.xOffset = (int) xOffset;
    }

    @Override
    public void setyOffset(double yOffset) {
        this.yOffset = (int) yOffset;
    }

    @Override
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    @Override
    public VortexFuture<Void> zoomPercent(double zoom) {
        this.zoom *= zoom;
        double zoomThatMakeSenseToServer = 0;
        if (this.zoom > 1) {
            zoomThatMakeSenseToServer = this.zoom - 1;
        } else if (this.zoom < 1) {
            zoomThatMakeSenseToServer = (-1D / (this.zoom)) + 1D;
        }
        final VortexFuture<Void> futureTask = WebMain.injector.getVortex().createFuture();
        try {
            futureTask.execute(GraphActionServiceProtocol.class).zoomPercent("" + zoomThatMakeSenseToServer,
                    graphSurface.getVizUuid());
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        return futureTask;
    }

    @Override
    public VortexFuture<Void> zoomToFit() {
        IsWidget view = graphSurface.getView();
        this.height = view.asWidget().getOffsetHeight();
        this.width =  view.asWidget().getOffsetWidth();

        final VortexFuture<Void> futureTask = WebMain.injector.getVortex().createFuture();
        try {
            futureTask.execute(GraphActionServiceProtocol.class).fitToSize(graphSurface.getVizUuid(), width, height);
        } catch (CentrifugeException ignored) {
        }
        return futureTask;
    }

    @Override
    public VortexFuture<Void> zoomToRegion(int x1, int y1, int x2, int y2) {
        final VortexFuture<Void> futureTask = WebMain.injector.getVortex().createFuture();
        try {
            futureTask.execute(GraphActionServiceProtocol.class).zoomToRegion(graphSurface.getVizUuid(),
                    graphSurface.getGraph().getDataviewUuid(), (double) x1, (double) x2, (double) y1, (double) y2);
        } catch (CentrifugeException ignored) {
        }
        return futureTask;
    }

    @Override
    public VortexFuture<PlunkedLink> plunkLink(PlunkLinkDTO plunkLinkDTO) {
        VortexFuture<PlunkedLink> addLinkFuture = WebMain.injector.getVortex().createFuture();
        try{
            addLinkFuture.execute(GraphActionServiceProtocol.class).plunkLink(plunkLinkDTO);
        } catch(Exception exception){
            //ignore
        }
        return addLinkFuture;
    }

    @Override
    public Image getDragImage() {
        return dragImage;
    }

    public void addErrorHandler(LoadHandler loadHandler) {
        this.errorHandler = loadHandler;
    }
    

    public void addSuccessHandler(LoadHandler loadHandler) {
        this.successHandler = loadHandler;
    }

}
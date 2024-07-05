package csi.client.gwt.viz.graph.surface;

import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.vortex.VortexFuture;

class PanGraph extends AbstractGraphSurfaceActivity {

    public PanGraph(GraphSurface graphSurface) {
        super(graphSurface);
    }

    @Override
    public void drag(int deltaX, int deltaY) {
        Model model = graphSurface.getModel();
        model.setxOffset(model.getxOffset() + deltaX);
        model.setyOffset(model.getyOffset() + deltaY);
        graphSurface.getView().moveAllToolTips(deltaX, deltaY);
        graphSurface.getView().draw(model.getImage(), model.getxOffset(), model.getyOffset(), model.getZoom());
    }

    @Override
    public void stopDrag(int x, int y) {
        Model model = graphSurface.getModel();
        // We pan 0,0 because this call is really just to update the server. We have already panned the local model in drag(int,int)
        try{
            VortexFuture<Void> panTo = model.pan(0, 0);
            graphSurface.refresh(panTo);
        } catch(Exception exception){
            //ignore
        }
    }
}

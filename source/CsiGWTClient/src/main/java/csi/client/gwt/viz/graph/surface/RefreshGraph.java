package csi.client.gwt.viz.graph.surface;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.viz.graph.surface.GraphSurface.View;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;

class RefreshGraph extends AbstractGraphSurfaceActivity {

    private static final int MIN_HEIGHT = 400;
    private static final int MIN_WIDTH = 400;
    private DisplayProxy model;
    private boolean again;
    private boolean running = false;
    
    public RefreshGraph(final GraphSurface graphSurface) {
        super(graphSurface);
        running = true;
        if (graphSurface.getGraph().isLoadedOnce()) {
            // clean up DOM
            Model oldModel = graphSurface.getModel();
            if (oldModel != null) {
                RootPanel.get().remove(oldModel.getImage());
            }
            
            model = createDisplayProxy(graphSurface);
            graphSurface.setModel(model);

            model.getImage().addLoadHandler(new LoadHandler() {

                @SuppressWarnings("unused")
                @Override
                public void onLoad(LoadEvent event) {
                    if(again){
                        graphSurface.getActivityManager().setActivity(new RefreshGraph(graphSurface));
                    }
                    else {
                        
                        graphSurface.show();
                    }
                }
            });
            
            model.addErrorHandler(new LoadHandler() {

                @SuppressWarnings("unused")
                @Override
                public void onLoad(LoadEvent event) {
                    running = false;
                    if(again){
                        again = false;
                        graphSurface.getActivityManager().setActivity(new RefreshGraph(graphSurface));
                    } else {
                        graphSurface.show();
                    }
                }
            });
            
            model.addSuccessHandler(new LoadHandler() {

                @Override
                public void onLoad(LoadEvent event) {
                    running = false;
                }});
        }
    }

    private static DisplayProxy createDisplayProxy(final GraphSurface graphSurface) {

        IsWidget view = graphSurface.getView();
        int lastWidth = MIN_WIDTH;
        int lastHeight = MIN_HEIGHT;
        if(graphSurface.getModel() != null) {
            lastWidth = graphSurface.getModel().getWidth();
            lastHeight = graphSurface.getModel().getHeight();
        }
        int width = view.asWidget().getOffsetWidth();
        int height = view.asWidget().getOffsetHeight();
        
        //This is in case there is an offscreen draw happening, like broadcast
        if(width < VizPanel.DEFAULT_WINDOW_SIZE) {
            width = lastWidth;
        }
        if(height < VizPanel.DEFAULT_WINDOW_SIZE) {
            height = lastHeight;
        }

        return new DisplayProxy(graphSurface, width, height);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        View view = graphSurface.getView();
        view.setCursor(Cursor.WAIT);
    }

    @Override
    public void onStop() {
        super.onStop();
        View view = graphSurface.getView();
        view.setCursor(Cursor.AUTO);
    }

    public void again() {
        again = true;
        //We have a completed graph refresh, but it won't trigger "again" so need a new activity
        if(!running) {
            graphSurface.getActivityManager().setActivity(new RefreshGraph(graphSurface));
        }
    }
}

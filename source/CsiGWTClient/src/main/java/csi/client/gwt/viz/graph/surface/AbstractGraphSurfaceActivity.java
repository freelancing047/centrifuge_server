package csi.client.gwt.viz.graph.surface;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.util.CSIActivityManager;

public abstract class AbstractGraphSurfaceActivity implements Activity, GraphSurface.Presenter {

    protected GraphSurface graphSurface;

    public AbstractGraphSurfaceActivity(GraphSurface graphSurface) {
        this.graphSurface = checkNotNull(graphSurface);
    }

    public AbstractGraphSurfaceActivity() {
        // TODO:Needed for mouseHandler. Should be deleted and I should do something smarter.
    }

    @Override
    public void hoverHere(int mouseX, int mouseY) {
        //No-op
    }

    @Override
    public void clickHere(int x, int y) {
        if (graphSurface == null) {
            return;
        }
        CSIActivityManager manager = graphSurface.getActivityManager();
        if (manager == null) {
            return;
        }
        if (!isBackground(x, y)|| itemsNear(x,y)) {
            if (graphSurface.getClickMode() == GraphSurface.ClickMode.SELECT) {
                SelectItem selectActivity = new SelectItem(graphSurface);
                selectActivity.setMousePosition(x, y);
                manager.setActivity(selectActivity);
            /*} else if(graphSurface.getClickMode() == GraphSurface.ClickMode.NONE){
                ExpandItem selectActivity = new ExpandItem(graphSurface);
                selectActivity.setMousePosition(x, y);
                manager.setActivity(selectActivity);*/
            }
        }
    }

    private boolean itemsNear(int x, int y) {
        int slop = WebMain.getClientStartupInfo().getGraphAdvConfig().getLinkTargetingHelp();
        ImageData imageData = graphSurface.getView().getImageDataAt(x, y, slop * 2 + 1, slop * 2 + 1);
        x -= slop;
        y -= slop;
        for (int i = 0; i < 2*slop+1; i++) {
            for (int j = 0; j < 2*slop+1; j++) {
                int backgroundColorInt = graphSurface.getBackgroundColorInt();
                int pixelRed = imageData.getRedAt(i, j);
                int pixelBlue = imageData.getBlueAt(i, j);
                int pixelGreen = imageData.getGreenAt(i, j);
                int backgroundRed = (backgroundColorInt >> 16) % 256;
                boolean isRedMatch = pixelRed == backgroundRed;
                int backgroundGreen = (backgroundColorInt >> 8) % 256;
                boolean isGreenMatch = pixelGreen == backgroundGreen;
                int backgroundBlue = (backgroundColorInt) % 256;
                boolean isBlueMatch = pixelBlue == backgroundBlue;
                if (isRedMatch && isGreenMatch && isBlueMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void doubleClickHere(int x, int y) {
    }

    @Override
    public void drag(int deltaX, int deltaY) {
    }

    @Override
    public void draw() {
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        graphSurface.setPresenter(this);
    }

    @Override
    public void startDrag(int x, int y) {
    }

    @Override
    public void stopDrag(int x, int y) {
    }

    @Override
    public void zoomIn() {
    }

    @Override
    public void zoomOut() {
    }

    protected boolean isBackground(int x, int y) {
        if(graphSurface==null || graphSurface.getView() == null){
            return false;
        }
        ImageData imageData = graphSurface.getView().getImageDataAt(x, y);
        if (imageData.getAlphaAt(0, 0) == 0) {
            return true;
        }
        int backgroundColorInt = graphSurface.getBackgroundColorInt();
        int pixelRed = imageData.getRedAt(0, 0);
        int pixelBlue = imageData.getBlueAt(0, 0);
        int pixelGreen = imageData.getGreenAt(0, 0);
        int backgroundRed = (backgroundColorInt >> 16) % 256;
        boolean isRedMatch = pixelRed == backgroundRed;
        int backgroundGreen = (backgroundColorInt >> 8) % 256;
        boolean isGreenMatch = pixelGreen == backgroundGreen;
        int backgroundBlue = (backgroundColorInt) % 256;
        boolean isBlueMatch = pixelBlue == backgroundBlue;
        return isRedMatch && isGreenMatch && isBlueMatch;
    }

    @Override
    public void updateCursor() {
        // empty
    }
}

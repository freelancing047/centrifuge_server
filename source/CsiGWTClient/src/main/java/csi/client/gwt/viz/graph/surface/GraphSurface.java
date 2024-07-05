package csi.client.gwt.viz.graph.surface;

import java.util.List;
import java.util.Set;

import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.surface.menu.ContextMenuPresenter;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTipManager;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.DragStartDTO;
import csi.server.common.dto.graph.gwt.NodeMapDTO;
import csi.server.common.dto.graph.gwt.PlunkLinkDTO;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.shared.core.visualization.graph.GraphLayout;
import csi.shared.gwt.viz.graph.MultiTypeInfo;

public interface GraphSurface extends IsWidget {

    enum ClickMode {
        NONE, SELECT;
    }

    enum DragMode {
        PAN, SELECT, ZOOM, DEFAULT, DESELECT;
    }

    interface Model {

        Image dragImage();

        VortexFuture<DragStartDTO> dragStart(int x, int y);

        int getHeight();

        Image getImage();

        int getWidth();

        double getxOffset();

        double getyOffset();

        double getZoom();

        VortexFuture<Void> pan(int deltaX, int deltaY);

        void setxOffset(double xOffset);

        void setyOffset(double yOffset);

        void setZoom(double zoom);

        VortexFuture<Void> zoomPercent(double d);

        VortexFuture<Void> zoomToFit();

        VortexFuture<Void> zoomToRegion(int x1, int y1, int x2, int y2);

        VortexFuture<PlunkedLink> plunkLink(PlunkLinkDTO plunkLinkDTO);

        DragStartDTO getDragStartDTO();

        Image getDragImage();

        VortexFuture<Boolean> shouldFitToSizeBasedOnBounds(int offsetWidth, int offsetHeight);

        void dragEnd(int x, int y, VortexEventHandler<String> handler);
    }

    interface Presenter {

        void clickHere(int x, int y);

        void doubleClickHere(int x, int y);

        void drag(int deltaX, int deltaY);

        void draw();

        void startDrag(int mouseX, int mouseY);

        void stopDrag(int mouseX, int mouseY);

        void zoomIn();

        void zoomOut();

        void updateCursor();

        void hoverHere(int mouseX, int mouseY);
    }

    interface View extends IsWidget {

        void draw(Image image, double x, double y, double zoom);

        void drawDragImage(int i, int j, Image dragImage, List<NodeMapDTO> dragNodes);

        void drawRect(int x1, int y1, int x2, int y2, String fillStyleColor);

        ImageData getImageDataAt(int x, int y);

        void setBackgroundFill(FillStrokeStyle backgroundFill);

        // TODO: make more generic
        void setCursor(Cursor cursor);

        void drawTowards(Image image, int startX, int startY, double zoom);

        ImageData getImageDataAt(int x, int y, int w, int h);

        void removeFromGraph(int x, int y, Image dragImage);

        void addTooltip(Widget toolTip, int x, int y);
        
        void addMultiTypePreview(Widget view, int x, int y);

        void moveAllToolTips(int deltaX, int deltaY);

        void clearForeground();

        void drawToolTipLines();

        void drawLineWithCircleIndicatingStart(double startX, double startY, double endX, double endY);
    }

    void zoomToFit();

    void refresh();

    int getBackgroundColorInt();

    MouseHandler getMouseHandler();

    Model getModel();

    @SuppressWarnings("rawtypes")
    void refresh(VortexFuture vortexFuture);

    @SuppressWarnings("rawtypes")
    void refreshWithNewLayout(VortexFuture vortexFuture, GraphLayout oldLayout);

    String getVizUuid();

    ClickMode getClickMode();

    CSIActivityManager getActivityManager();

    View getView();

    DragMode getDragMode(boolean useExplicit);

    DragMode getExplicitDragMode();

    FillStrokeStyle getBackgroundColor();

    Graph getGraph();

    KeyHandler getKeyHandler();

    void setPresenter(Presenter presenter);

    void setModel(Model model);

    void show();

    void setClickMode(ClickMode select);

    Presenter getPresenter();

    void setDragMode(DragMode mode);

    void setExplicitDragMode(DragMode mode);

    ContextMenuPresenter getContextMenuPresenter();

    ToolTipManager getToolTipManager();

    public void setReadOnly();

    void showMultiTypes(MultiTypeInfo result);
}

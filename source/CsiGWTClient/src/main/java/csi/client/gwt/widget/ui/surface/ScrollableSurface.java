/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.widget.ui.surface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.aria.client.OrientationValue;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Rectangle;
import csi.client.gwt.util.MouseUtil;
import csi.client.gwt.viz.matrix.*;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.RectangularDragger;
import csi.client.gwt.widget.ui.RectangularDragger.MouseMoveData;
import csi.client.gwt.widget.ui.RectangularDragger.RectangularDraggerCallback;
import csi.client.gwt.widget.ui.Tooltip;
import csi.client.gwt.widget.ui.surface.ScrollableSurfaceRenderable.BBox;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.util.HasLabel;
import csi.shared.core.util.Native;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixSelectionRequest;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ScrollableSurface extends ResizeComposite {
/*

    private static final String IMAGE_PNG = "image/png";
    private static final String GRAY = "gray";
    private static final String NONE = "none";
    private static final String POINTER_EVENTS = "pointerEvents";
    private static final String AXIS_Y = "axisY";
    private static final String AXIS_X = "axisX";
    private static final int WIDTH_LABEL = 75;
    private static final int WIDTH_OVERVIEW = 100;
    private static final int DIM_HIT_GRID = 50;
    private final MatrixModel model;

    private TitleDefinition mainTitleDefinition = new TitleDefinition();
    private OrdinalAxisExtent lastExtentX, lastExtentY;
    private int canvasHeight, canvasWidth;
    private boolean gridLineInCenter;

    // Highlights the horizontal or vertical grid line if this is not < 0
    private double gridHighlightX = -1, gridHighlightY = -1;
    private double tooltipX = -1, tooltipY = -1;

    private ZoomTimer zoomTimer;
    private boolean dragging;
    private boolean dragSelection;
    private boolean dragZoom;
    private boolean dragDeselect;

    private ScrollableSurfaceRenderable highlightedSSR;
    private int axisResizeState;
    private boolean axisDisplayed;

    private DataRequestCallback dataRequestCallback;
    private TooltipCallback<? extends HasLabel> tooltipCallback;
    private List<? extends ScrollableSurfaceRenderable> renderables = new ArrayList<>();

    // These are used for hit-testing
    private Map<SurfaceLocation, ScrollableSurfaceRenderable> renderablesByLocation = new HashMap<>(10000);

    private Map<Integer, List<ScrollableSurfaceRenderable>> renderablesByGrid = new HashMap<Integer, List<ScrollableSurfaceRenderable>>(10000);

    private boolean overlappingRenderables;

    // Number of hit-grids going out horizontally.
    private int horizontalHitGridCount;

    @UiField
    FullSizeLayoutPanel surfaceContainer;

    @UiField
    Tooltip tooltip;

    @UiField(provided = true)
    Double dimLabel = new Double(WIDTH_LABEL);

    @UiField(provided = true)
    Canvas gridCanvas = Canvas.createIfSupported(),
            mainCanvas = Canvas.createIfSupported(),
            highlightCanvas = Canvas.createIfSupported();

    @UiField(provided = true)
    OrdinalAxisCanvas axisX;

    @UiField(provided = true)
    OrdinalAxisCanvas axisY;

    MatrixPresenter presenter;

    interface SpecificUiBinder extends UiBinder<Widget, ScrollableSurface> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ScrollableSurface(MatrixModel model, MatrixPresenter presenter) {
        // i added the presenter here, probably should decouple
        this.model = model;
        this.presenter = presenter;

        axisX = new OrdinalAxisCanvas(OrientationValue.HORIZONTAL);
        axisY = new OrdinalAxisCanvas(OrientationValue.VERTICAL);

        // handlers.
        addAxisResizeHandlers();
        addMainCanvasRectangularDragHandler();

        initWidget(uiBinder.createAndBindUi(this));

        setStyle();
    }

    private void setStyle() {
        Style style = highlightCanvas.getElement().getStyle();
        style.setZIndex(2);
        style.setProperty(POINTER_EVENTS, NONE);
        style = highlightCanvas.getElement().getParentElement().getStyle();
        style.setProperty(POINTER_EVENTS, NONE);
    }

    public OrdinalAxisCanvas getAxisX() {
        return axisX;
    }

    public OrdinalAxisCanvas getAxisY() {
        return axisY;
    }

    public class ScrollableSurfaceTooltipTimer extends Timer {
        private int x, y;

        private ScrollableSurfaceRenderable renderable;

        public void setData(int x, int y, ScrollableSurfaceRenderable renderable) {
            this.x = x;
            this.y = y;
            this.renderable = renderable;
        }

        @Override
        public void run() {
            ScrollableSurface.this.displayTooltip(x, y, renderable);
        }

    }

    private ScrollableSurfaceTooltipTimer tooltipTimer = new ScrollableSurfaceTooltipTimer();

    public void setMaxExtents() {
        axisX.setMaxIndex(model.getMatrixDataResponse().getMaxX());
        axisY.setMaxIndex(model.getMatrixDataResponse().getMaxY());
    }

    private void addAxisResizeHandlers() {

        ResizeHandler axisResizeHandler = event -> {
            axisResizeState |= 0x10;  // 16
            if (axisResizeState == 0x11) { // 17
                axisResizeState = 0;
                processExtents();
            }
        };

        axisX.addResizeHandler(axisResizeHandler);
        axisY.addResizeHandler(axisResizeHandler);
    }

    public void setGridLineInCenter(boolean gridLineInCenter) {
        this.gridLineInCenter = gridLineInCenter;
    }

    public TitleDefinition getAxisXTitle() {
        return axisX.getAxisTitle();
    }

    public TitleDefinition getAxisYTitle() {
        return axisY.getAxisTitle();
    }

    public TitleDefinition getMainTitle() {
        return mainTitleDefinition;
    }

    public void updateAxisTitles() {
//        // Force a set to cause title to update.
        axisX.setAxisTitle(axisX.getAxisTitle());
        axisY.setAxisTitle(axisY.getAxisTitle());
    }

    public void setDataRequestCallback(DataRequestCallback dataRequestCallback) {
        this.dataRequestCallback = dataRequestCallback;
    }

    public <T extends HasLabel> void setTooltipCallback(final TooltipCallback<T> tooltipCallback) {
        this.tooltipCallback = tooltipCallback;
        axisX.setAxisHighlightCallback(new AxisHighlightCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public <E extends HasLabel> void highlight(int clientX, int clientY, E value, int i, double location,
                                                       boolean enable) {
                setTooltipHidden();
                if (enable) {
                    //Hide last tooltip first, otherwise you could orphan the tooltip
                    SafeHtml innerHTML = tooltipCallback.getAxisTooltipContent(OrientationValue.HORIZONTAL, i, (T) value);
                    int x = MouseUtil.getRelativeX(clientX, tooltip.getElement());
                    int y = MouseUtil.getRelativeY(clientY, tooltip.getElement());
                    tooltip.display(innerHTML, x, y);
                    gridHighlightX = location;
                    axisX.highlight(i, true);
                } else {
                    gridHighlightX = -1;
                    axisX.highlight(i, false);
                }
                refreshGrid();
                displayHighlights();
            }
        });

        // create two call backs , one that selects everything on x axis, another one on y axis given the coord of click
        axisX.setAxisClickCallback(new AxisClickCallback() {
            @Override
            public <T extends HasLabel> void onClickLabel(int clientX, int clientY, T value, int index, double location, boolean enable) {
                String val = value.toString();
                MatrixSelectionRequest req = new MatrixSelectionRequest(presenter.getUuid(), presenter.getDataViewUuid(), MatrixSelectionRequest.Axis.X, value.toString());
                Cell oneCell = null;

                //fixme

                */
/*for (Cell cell : model.getMatrixDataResponse().getCells()) {
                    if(cell.getX() == index ){
                        oneCell = cell;
                        break;
                    }
                }

                // these toggles the summary or not cat select
                if (oneCell != null) {
                    req.setSummaryStartEnd(oneCell.getStartX(), oneCell.getEndX());
                }
*//*

                presenter.doCategorySelection(req);

                for (Cell a : model.getMatrixDataResponse().getCells()) {
                    if (a.getX() == index) {
                        model.modifySelection(a, enable);
                    }
                }
                refreshMainCanvas();
            }
        });

        axisY.setAxisClickCallback(new AxisClickCallback() {
            @Override
            public <T extends HasLabel> void onClickLabel(int clientX, int clientY, T value, int index, double location, boolean enable) {
                String val = value.toString();

                MatrixSelectionRequest req = new MatrixSelectionRequest(presenter.getUuid(), presenter.getDataViewUuid(), MatrixSelectionRequest.Axis.Y, value.toString());
                Cell oneCell = null;

                //fixme
                */
/*for (Cell cell : model.getMatrixDataResponse().getCells()) {
                    if(cell.getCategoryY().equals(val)){
                        oneCell = cell;
                        break;
                    }
                }
                if (oneCell != null) {
                    req.setSummaryStartEnd(oneCell.getStartY(), oneCell.getEndY());
                }*//*

                presenter.doCategorySelection(req);


                // TODO: we don't need to do this anymore


                for (Cell a : model.getMatrixDataResponse().getCells()) {
                    if (a.getY()== index) {
                        model.modifySelection(a, enable);
                    }
                }
            }
        });


        axisY.setAxisHighlightCallback(new AxisHighlightCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public <E extends HasLabel> void highlight(int clientX, int clientY, E value, int i, double location,
                                                       boolean enable) {
                setTooltipHidden();
                if (enable) {
                    SafeHtml innerHTML = tooltipCallback.getAxisTooltipContent(OrientationValue.VERTICAL, i, (T) value);
                    int x = MouseUtil.getRelativeX(clientX, tooltip.getElement());
                    int y = MouseUtil.getRelativeY(clientY, tooltip.getElement());
                    tooltip.display(innerHTML, x, y);
                    gridHighlightY = location;
                    axisY.highlight(i, true);
                } else {
                    gridHighlightY = -1;
                    axisY.highlight(i, false);
                }
                refreshGrid();
                displayHighlights();
            }
        });
    }

    private ScrollableSurfaceRenderable getRenderableAt(MouseEvent<?> event) {
        int x = event.getRelativeX(mainCanvas.getElement());
        int y = event.getRelativeY(mainCanvas.getElement());
        ScrollableSurfaceRenderable ssr = null;
        if (mainCanvas.getContext2d().getImageData(x, y, 1, 1).getAlphaAt(0, 0) > 0) {
            ssr = getClosestAt(x, y);
        }
        return ssr;
    }

    private ScrollableSurfaceRenderable getClosestAt(int mx, int my) {
        if (overlappingRenderables) {
            int gridNumber = (my / DIM_HIT_GRID) * horizontalHitGridCount + mx / DIM_HIT_GRID + 1;
            List<ScrollableSurfaceRenderable> renderables = renderablesByGrid.get(gridNumber);
            if (renderables != null) {
                // Renderables are in the list in largest to smallest order. We therefore traverse from smallest to largest.
                for (int i = renderables.size() - 1; i >= 0; i--) {
                    ScrollableSurfaceRenderable renderable = renderables.get(i);
                    if (renderable.isHitTest(mx, my)) {
                        return renderable;
                    }
                }
            }
            return null;
        } else {
            int indexX = axisX.getClosestIndex(mx);
            int indexY = axisY.getClosestIndex(canvasHeight - my);
            return renderablesByLocation.get(new SurfaceLocation(indexX, indexY));
        }
    }

    @UiHandler("mainCanvas")
    protected void handleMouseZoom(MouseWheelEvent event) {
        int mx = event.getRelativeX(mainCanvas.getElement());
        int my = canvasHeight - event.getRelativeY(mainCanvas.getElement());

        if (zoomTimer != null) {
            // The user is continuously rolling the mouse wheel. Cancel the previous zoom request and add to it.
            zoomTimer.cancel();
        } else {
            zoomTimer = new ZoomTimer() {
                @Override
                public void run() {
                    if (getZoom() != 0) {
                        applyZoom(getMx(), getMy(), getZoom());
                        zoomTimer = null;
                    }
                }
            };
        }

        int deltaY = event.getDeltaY();
        // Workaround for bug in GWT.2.5.1
        if (deltaY == 0) {
            deltaY = getDeltaY(event.getNativeEvent());
        }

        int z = zoomTimer.getZoom() + ((deltaY > 0) ? -1 : 1);
        zoomTimer.setMx(mx);
        zoomTimer.setMy(my);
        zoomTimer.setZoom(z);
        zoomTimer.schedule(100);
    }

    */
/**
     * Workaround for bug in GWT.2.5.1
     *
     * @param event
     * @return
     *//*

    private static native int getDeltaY(NativeEvent event)*/
/*-{
        return event.wheelDelta;
    }-*//*
;

    */
/**
     * @param zoomLevel Zooms in by the given percentage. For each level, will zoom by 10% So 1 will cause it to
     *                  zoom in by 10% 2 by 10% of 10% etc. Negative zoom values will cause zoom-out.
     *                  <p>
     *                  Zooms to center.
     *//*

    public void zoom(int zoomLevel) {
        presenter.preserveSelection();
        if(this.presenter.getModel().isSummary()){
            applyZoom(canvasWidth / 2, canvasHeight / 2, zoomLevel);
        }else{
            applyZoom(canvasWidth / 2, canvasHeight / 2, zoomLevel/2);
        }
    }

    // so we can test if are displaying all cells, or if we are displayiing all categories, and then we can get other stuff if needed
    private void applyZoom(int mx, int my, int zoomLevel) {
        boolean isFullyZoomedX = model.getCategoryResponse().getCategoryX().size() -1 == axisX.getCurrentExtent().getEndIndex();
        boolean isFullyZoomedY = model.getCategoryResponse().getCategoryY().size() -1 == axisY.getCurrentExtent().getEndIndex();

        if ((model.isSummary() || zoomLevel < 0) && (isFullyZoomedX && isFullyZoomedY)) {
            if(!model.getMatrixDataResponse().isFullMatrix() || zoomLevel > 0) {

                int zoomAmountX = (int)(((double)axisX.getMaxIndex() / 100) * zoomLevel);
                int zoomAmountY = (int)(((double)axisY.getMaxIndex() / 100) * zoomLevel);

                zoomAmountX = zoomAmountX == 0 ? 2* zoomLevel : zoomAmountX;
                zoomAmountY = zoomAmountY == 0 ? 2* zoomLevel : zoomAmountY;

                int x1;
                int x2;
                int y1;
                int y2;

                if(presenter.updater != null){
                    x1 = presenter.updater.getReq().getStartX() + zoomAmountX;
                    x2 = presenter.updater.getReq().getEndX() - zoomAmountX;
                    y1 = presenter.updater.getReq().getStartY() + zoomAmountY;
                    y2 = presenter.updater.getReq().getEndY() - zoomAmountY;

                }else {
                    x1 = axisX.getDataStart() + zoomAmountX;
                    x2 = axisX.getDataEnd() - zoomAmountX;
                    y1 = axisY.getDataStart() + zoomAmountY;
                    y2 = axisY.getDataEnd() - zoomAmountY;
                }
                // make sure we are not requesting fake stuff
                x1 = x1 >= 0 ? x1 : 0;
                x2 = x2 <= axisX.getMaxIndex() ? x2 : axisX.getMaxIndex();
                y1 = y1 >= 0 ? y1 : 0;
                y2 = y2 <= axisY.getMaxIndex() ? y2 : axisY.getMaxIndex();

                MatrixDataRequest req = new MatrixDataRequest();
                req.setExtent(x1, x2, y1, y2);

                presenter.requestUpdate(req);
            }
        } else {
            if (zoomLevel < 0 || (getCurrentExtents().getWidth() > 0 && getCurrentExtents().getHeight() > 2) ||
                    (getCurrentExtents().getWidth() > 2 && getCurrentExtents().getHeight() > 0)) {
                axisX.zoom(mx, zoomLevel);
                axisY.zoom(my, zoomLevel);

                processExtents();
            }
        }

    }

    @UiHandler("mainCanvas")
    protected void handleMouseMove(MouseMoveEvent event) {
        event.preventDefault();
        try {
            if (!dragging) {
                int x = event.getRelativeX(mainCanvas.getElement());
                int y = event.getRelativeY(mainCanvas.getElement());

                ScrollableSurfaceRenderable ssr = getRenderableAt(event);
                if (ssr != null & (highlightedSSR == null || highlightedSSR != ssr)) {
                    hideTooltip();

                    tooltipTimer.cancel();
                    tooltipTimer.setData(x, y, ssr);
                    tooltipTimer.schedule(400);
                }

                if (ssr == null) {
                    hideTooltip();
                    tooltipTimer.cancel();
                }
            }
        }catch (Exception e){

        }
    }

    private void displayTooltip(int x, int y, ScrollableSurfaceRenderable ssr) {
       try {
           setTooltipHidden();

           axisX.highlight(ssr.getX(), true);
           axisY.highlight(ssr.getY(), true);
           highlightedSSR = ssr;
           SafeHtml innerHTML = tooltipCallback.getItemTooltipContent(ssr.getX(), ssr.getY(), ssr);
           tooltip.display(innerHTML, x + WIDTH_LABEL, y);
           tooltipX = axisX.getLocation(ssr.getX());
           tooltipY = axisY.getLocation(ssr.getY());
           refreshGrid();
           displayHighlights();
       }catch (Exception E){

       }
    }

    private void hideTooltip() {

        if (highlightedSSR != null) {
            setTooltipHidden();
            axisX.highlight(highlightedSSR.getX(), false);
            axisY.highlight(highlightedSSR.getY(), false);
            highlightedSSR = null;

            tooltipX = -1;
            tooltipY = -1;
            refreshGrid();
            displayHighlights();
        }
    }

    protected void setTooltipHidden() {

        if (tooltip != null) {
            if (tooltip.isVisible()) {
                try {
                    tooltip.hide();
                } catch (Exception e) {
                }
            }
        }
    }

    @UiHandler("mainCanvas")
    protected void handleMouseOut(MouseOutEvent event) {
        setTooltipHidden();
        clearHighlights();
        refreshGrid();
        dragZoom = false;
        dragSelection = false;
        dragging = false;
    }

    private boolean alreadyPanning = false;

    public void setAlreadyPanning(Boolean val) {
        alreadyPanning = val;
    }


    public double getCoordinateWidth() {
        return gridCanvas.getCoordinateSpaceWidth();
    }

    public double getCoordinateHeight() {
        return gridCanvas.getCoordinateSpaceHeight();
    }

    protected void addMainCanvasRectangularDragHandler() {
        new RectangularDragger(mainCanvas, new RectangularDraggerCallback() {

            @Override
            public void onMouseDown(MouseDownEvent event, int mx, int my) {
                if(model.getMatrixDataResponse() == null) return;
                dragging = true;
                dragSelection = event.isControlKeyDown() && !event.isShiftKeyDown();
                dragDeselect = event.isControlKeyDown() && event.isShiftKeyDown();
                dragZoom = !event.isControlKeyDown() && event.isShiftKeyDown();
            }

            @Override
            public void onMouseMove(MouseMoveData data) {
                if(model.getMatrixDataResponse() == null) return;
                setTooltipHidden();
                clearHighlights();
                if (dragSelection || dragDeselect) {
                    refreshGrid();
                    drawSelectionBox(data.getStartX(), data.getStartY(), data.getDeltaFromStartX(), data.getDeltaFromStartY());
                } else if (dragZoom) { // true/false based on the shift key
                    refreshGrid();
                    drawZoomBox(data.getStartX(), data.getStartY(), data.getDeltaFromStartX(), data.getDeltaFromStartY());
                } else {
                    axisX.pan(data.getDeltaFromLastX());
                    axisY.pan(-data.getDeltaFromLastY());
//                    Scheduler.get().scheduleDeferred(() -> presenter.isUpdate(data.getStartX(), data.getStopX(), data.getStartY(), data.getStopY()));
                }
            }

            @Override
            public void onMouseUp(boolean click, MouseMoveData data) {
                if(model.getMatrixDataResponse() == null) return;
                // if its just a little jitter, don't update
                if(data.getDeltaFromStartX() < 15 && data.getDeltaFromStartY() < 15 && data.getDeltaFromStartX() > -15 && data.getDeltaFromStartY() > -15){
                    refreshGrid();
                    processExtents();
                    return;
                }


                presenter.preserveSelection();
                dragging = false;
                refreshGrid();
                if (dragSelection) {
                    displayHighlights();
                    if (!click) {
                        selectRenderablesInRegion(data.getStartX(), data.getStartY(), data.getDeltaFromStartX(),
                                data.getDeltaFromStartY());
                    }
                } else if (dragZoom) {
                    if (!click) {

                        int x1 = axisX.getClosestIndexCeiling(data.getStartX());
                        int y1 = axisY.getClosestIndex(gridCanvas.getCoordinateSpaceHeight() - data.getStartY());
                        int x2 = axisX.getClosestIndexCeiling(data.getStopX());
                        int y2 = axisY.getClosestIndex(gridCanvas.getCoordinateSpaceHeight() - data.getStopY());

                        int displayX = Math(x1, x2);
                        int displayY = Math.min(y1, y2);
                        int displayHeight = displayY + Math.abs(y1 - y2);
                        int displayWidth = displayX + Math.abs(x1 - x2);


                        if (model.isSummary()) {
                            if (model.getMatrixDataResponse().getCells().size() != 0) {
                                double sX = model.getMatrixDataResponse().getStartX();
                                double eX = model.getMatrixDataResponse().getEndX();
                                double sY = model.getMatrixDataResponse().getStartY();
                                double eY = model.getMatrixDataResponse().getEndY();


                                int newStartX = (int)sX + (displayX * axisX.getNumberOfCategories((int)(eX - sX)));
                                int newStartY = (int)sY + (displayY * axisY.getNumberOfCategories((int)(eY - sY)));
                                int newEndX = (int)sX + (displayWidth * axisX.getNumberOfCategories((int)(eX - sX)));
                                int newEndY = (int)sY + (displayHeight  * axisY.getNumberOfCategories((int)(eY - sY)));


                                // this might b a hack but it should work
                                if(getAxisX().getDataStart() == getAxisX().getDataEnd()){
                                    newStartX = getAxisX().getDataStart();
                                    newEndX   = getAxisX().getDataEnd();
                                }

                                if(getAxisY().getDataStart() == getAxisY().getDataEnd()){
                                    newStartY = getAxisY().getDataStart();
                                    newEndY   = getAxisY().getDataEnd();
                                }

                                // so this should give us.... exactly what we need.

                                presenter.showInfoForRegion(newStartX, newStartY, newEndX, newEndY);

                            }
                        } else {

                            display(displayX, displayY, displayWidth, displayHeight);
                        }
                    }
                } else if (dragDeselect) {
                    displayHighlights();
                    if (!click) {
                        deselectRenderablesInRegion(data.getStartX(), data.getStartY(), data.getDeltaFromStartX(), data.getDeltaFromStartY());
                    }
                } else {
                    processExtents();
                    presenter.isUpdate();

                }
//
            }

        });
    }

    protected List<ScrollableSurfaceRenderable> isInRegion(int x, int y, int dx, int dy){
        if (dx < 0) {
            x += dx;
            dx = -dx;
        }
        if (dy < 0) {
            y += dy;
            dy = -dy;
        }

        List<ScrollableSurfaceRenderable> rends = new ArrayList<>();

        for (ScrollableSurfaceRenderable renderable : renderables) {
            if(renderable instanceof AbstractScrollableSurfaceRenderable){
                AbstractScrollableSurfaceRenderable rend = (AbstractScrollableSurfaceRenderable) renderable;
                if(rend.isInRect(x, y, dx, dy)){
                    rends.add(renderable);
                }

            }
        }

        return rends;
    }

    protected void selectRenderablesInRegion(int x, int y, int dx, int dy) {
        if (dx < 0) {
            x += dx;
            dx = -dx;
        }
        if (dy < 0) {
            y += dy;
            dy = -dy;
        }

        for (ScrollableSurfaceRenderable renderable : renderables) {
            renderable.selectionRectangle(x, y, dx, dy);
        }

        refreshMainCanvas();
    }

    protected void deselectRenderablesInRegion(int x, int y, int dx, int dy) {
        if (dx < 0) {
            x += dx;
            dx = -dx;
        }
        if (dy < 0) {
            y += dy;
            dy = -dy;
        }

        for (ScrollableSurfaceRenderable renderable : renderables) {
            renderable.deselectionRectangle(x, y, dx, dy);
        }

        refreshMainCanvas();
    }

    protected void drawSelectionBox(int x, int y, int dx, int dy) {
        if (dx < 0) {
            x += dx;
            dx = -dx;
        }
        if (dy < 0) {
            y += dy;
            dy = -dy;
        }

        Context2d ctx = gridCanvas.getContext2d();
        ctx.beginPath();
        ctx.setStrokeStyle(CssColor.make(GRAY)); //$NON-NLS-1$
        ctx.rect(x, y, dx, dy);
        ctx.stroke();
    }

    protected void drawZoomBox(int x, int y, int dx, int dy) {

        if (dx < 0) {
            x += dx;
            dx = -dx;
        }
        if (dy < 0) {
            y += dy;
            dy = -dy;
        }

        Context2d ctx = gridCanvas.getContext2d();
        ctx.beginPath();

        int red = 255;
        int green = 0;
        int blue = 0;
        double alpha = .2;

        FillStrokeStyle f = CssColor.make("rgba(" + red + ", " + green + "," + blue + ", " + alpha + ")");

        ctx.setFillStyle(f);
        ctx.setStrokeStyle(CssColor.make(255, 0, 0));
        ctx.rect(x, y, dx, dy);
        ctx.stroke();
        ctx.fill();
        ctx.setGlobalAlpha(1.0);
    }

    @UiHandler("mainCanvas")
    protected void handleMouseClick(ClickEvent event) {
        if (dragZoom) {
            dragZoom = false;
            dragSelection = false;
            return;
        }
        ScrollableSurfaceRenderable ssr = getRenderableAt(event);
        if (ssr != null) {
            ssr.onClick(event);
            refreshMainCanvas();
        }
    }

    public void processExtents() {

        OrdinalAxisExtent xExtent = axisX.getCurrentExtent();
        OrdinalAxisExtent yExtent = axisY.getCurrentExtent();

        if (xExtent != null && yExtent != null) {
            if (!xExtent.equals(lastExtentX) || !yExtent.equals(lastExtentY)) {
                dataRequestCallback.getData(xExtent.getStartIndex(), xExtent.getEndIndex(), yExtent.getStartIndex(), yExtent.getEndIndex());

                lastExtentX = xExtent;
                lastExtentY = yExtent;

            } else {
                refresh();

            }
        }
    }

    public void refresh() {
        refreshGrid();
        refreshMainCanvas();
        displayHighlights();
    }

    @Override
    public void onResize() {
        canvasHeight = getOffsetHeight();
        canvasWidth = getOffsetWidth();

        if (axisDisplayed) {
            canvasHeight -= WIDTH_LABEL;
            canvasWidth -= WIDTH_LABEL;
        }

        if (canvasHasSize()) {
            gridCanvas.setCoordinateSpaceWidth(canvasWidth);
            gridCanvas.setCoordinateSpaceHeight(canvasHeight);
            mainCanvas.setCoordinateSpaceWidth(canvasWidth);
            mainCanvas.setCoordinateSpaceHeight(canvasHeight);
            highlightCanvas.setCoordinateSpaceWidth(canvasWidth);
            highlightCanvas.setCoordinateSpaceHeight(canvasHeight);

            horizontalHitGridCount = canvasWidth / DIM_HIT_GRID + 1;
        }

        Scheduler.get().scheduleFixedDelay(() -> {
            if (canvasHasSize() && axisHasExtent()) {
                display(axisX.getCurrentExtent().getStartIndex(), axisY.getCurrentExtent().getStartIndex(),
                        axisX.getCurrentExtent().getEndIndex(), axisY.getCurrentExtent().getEndIndex());
            } else if (canvasHasSize() && model != null && model.getCategoryResponse() != null) {
                display(0, 0, model.getCategoryX().size() - 1, model.getCategoryY().size() - 1);
            }
            return false;
        }, 500);

        super.onResize();
    }

    private boolean axisHasExtent() {
        OrdinalAxisExtent xExtent = axisX.getCurrentExtent();
        OrdinalAxisExtent yExtent = axisY.getCurrentExtent();
        if (yExtent == null || xExtent == null) {
            return false;
        }

        return xExtent.getStartIndex() != 0 || xExtent.getEndIndex() != 0 || yExtent.getStartIndex() != 0 || yExtent.getEndIndex() != 0;
    }

    private boolean canvasHasSize() {
        return canvasHeight > 0 && canvasWidth > 0;
    }

    */
/**
     * Sets the domain values on the axes.
     *
     * @param valuesX
     * @param valuesY
     *//*

    public void displayCategories(List<? extends HasLabel> valuesX, List<? extends HasLabel> valuesY) {
        // Null out last extents. Otherwise an attempt to display new categories will not cause screen to be updated.
        lastExtentX = null;
        lastExtentY = null;

        axisX.setDomainValues(valuesX);
        axisY.setDomainValues(valuesY);
    }

    */
/**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     *//*



    public void display(int x1, int y1, int x2, int y2) {
        axisX.setExtents(x1, x2);
        axisY.setExtents(y1, y2);
        processExtents();
    }

    public boolean isRegionDisplayed(int x1, int y1, int x2, int y2) {

        int xSta = axisX.getCurrentExtent().getStartIndex(), xEnd = axisX.getCurrentExtent().getEndIndex();
        int ySta = axisY.getCurrentExtent().getStartIndex(), yEnd = axisY.getCurrentExtent().getEndIndex();
        if(xSta == x1 && xEnd == x2 && ySta == y1 & yEnd == y2){
            return true;
        }
        return false;
    }


    private void refreshGrid() {
        if (canBeRefreshed()) {
            _displayGrid();
        }
    }

    private boolean canBeRefreshed() {
        if (getOffsetWidth() <= 0 || getOffsetHeight() <= 0)
            return false;
        if (axisX.getSegmentSize() == 0 || axisY.getSegmentSize() == 0)
            return false;
        if (axisX.getCurrentExtent() == null || axisY.getCurrentExtent() == null)
            return false;

        return true;
    }

    private void _displayGrid() {
        Context2d ctx = gridCanvas.getContext2d();
        ctx.clearRect(0, 0, canvasWidth, canvasHeight);
        ctx.save();

        ctx.setStrokeStyle(CssColor.make(220, 220, 220));
        ctx.setLineWidth(1.0);

        double z = getZoomRatio();
        double alpha = 0.3 + z * 0.7;
        if (alpha > 1.0) {
            alpha = 1.0;
        }

        ctx.setGlobalAlpha(alpha);
        ctx.beginPath();

        // X axis
        {
            double d = 10 / axisX.getSegmentSize();
            // no less than one
            int delta = d < 1 ? 1 : (int) d;

            int i = axisX.getStartIndex();
            double x = axisX.getLocation(i);

            if (!gridLineInCenter) {
                x = x - axisX.getSegmentSize() / 2.0;
            }

            while (x < canvasWidth) {
                ctx.moveTo((int) x, 0);
                ctx.lineTo((int) x, canvasHeight);
                i += delta;
                x = axisX.getLocation(i);
                if (!gridLineInCenter) {
                    x = x - axisX.getSegmentSize() / 2.0;
                }
            }
        }

        // Y axis
        {
            double d = 10 / axisY.getSegmentSize();
            int delta = d < 1 ? 1 : (int) d;

            int i = axisY.getStartIndex();
            double y = axisY.getLocation(i);

            if (!gridLineInCenter) {
                y = y - axisY.getSegmentSize() / 2.0;
            }
            while (y > 0) {
                ctx.moveTo(0, (int) y);
                ctx.lineTo(canvasWidth, (int) y);
                i += delta;
                y = axisY.getLocation(i);
                if (!gridLineInCenter) {
                    y = y - axisY.getSegmentSize() / 2.0;
                }
            }
        }


        ctx.stroke();
        ctx.restore();
    }

    public void clearHighlights() {
        Context2d ctx = highlightCanvas.getContext2d();

        ctx.clearRect(0, 0, canvasWidth, canvasHeight);
        ctx.save();
        highlightCanvas.getElement().getStyle().setOpacity(0);
        highlightCanvas.setVisible(false);
    }

    public void displayHighlights() {
        if (!dragging) {

            highlightCanvas.getElement().getStyle().setOpacity(100);
            highlightCanvas.setVisible(true);
            Context2d ctx = highlightCanvas.getContext2d();

            ctx.clearRect(0, 0, canvasWidth, canvasHeight);
            ctx.save();
            ctx.setStrokeStyle(CssColor.make(220, 220, 220));
            ctx.setLineWidth(1.0);

            double z = getZoomRatio();
            double alpha = 0.3 + z * 0.7;
            if (alpha > 1.0) {
                alpha = 1.0;
            }
            ctx.setGlobalAlpha(alpha);

            ctx.beginPath();
            if (gridHighlightX >= 0) {
                ctx.setStrokeStyle(CssColor.make(GRAY)); //$NON-NLS-1$
                ctx.beginPath();
                ctx.moveTo(gridHighlightX, 0);
                ctx.lineTo(gridHighlightX, canvasHeight);
                ctx.stroke();
            } else if (gridHighlightY >= 0) {
                ctx.setStrokeStyle(CssColor.make(0, 0, 0));
                ctx.beginPath();
                ctx.moveTo(0, gridHighlightY);
                ctx.lineTo(canvasWidth, gridHighlightY);
                ctx.stroke();
            } else if (tooltipX > 0) {
                ctx.setStrokeStyle(CssColor.make(0, 0, 0));
                ctx.beginPath();
                ctx.moveTo(0, tooltipY);
                ctx.lineTo(tooltipX, tooltipY);
                ctx.lineTo(tooltipX, canvasHeight);
                ctx.stroke();
            } else {
                clearHighlights();
            }

            ctx.restore();
        }
    }

    public List<? extends ScrollableSurfaceRenderable> getRenderables() {
        return renderables;
    }

    */
/**
     * Displays the renderables passed in.
     * @param renderables
     *//*

    public void display(List<? extends ScrollableSurfaceRenderable> renderables) {
        this.renderables = renderables;

        renderablesByLocation.clear();

        for (ScrollableSurfaceRenderable ssr : renderables) {
            SurfaceLocation location = new SurfaceLocation(ssr.getX(), ssr.getY());
            renderablesByLocation.put(location, ssr);
        }

        refresh();
    }

    private void refreshMainCanvas() {
        Context2d ctx = mainCanvas.getContext2d();
        ctx.clearRect(0, 0, canvasWidth, canvasHeight);
        ctx.save();

        DrawContext dc = new DrawContext();

        dc.setCategoryWidth(axisX.getSegmentSize());
        dc.setCategoryHeight(axisY.getSegmentSize());
        dc.setTotalElements(renderables.size());
        // Compute axis zoom levels
        dc.setZoomLevel(getZoomRatio());

        if (renderables.size() > 0) {
            ScrollableSurfaceRenderable renderable = renderables.get(0);
            renderable.prepareContext(ctx, dc);
            overlappingRenderables = renderable.isOverlappingVisual();
        }

        renderablesByGrid.clear();

        int startX = axisX.getCurrentExtent().getStartIndex() - 1;
        int endX = axisX.getCurrentExtent().getEndIndex() + 1;

        int startY = axisY.getCurrentExtent().getStartIndex() - 1;
        int endY = axisY.getCurrentExtent().getEndIndex() + 1;

        for (ScrollableSurfaceRenderable renderable : renderables) {
            double x = axisX.getLocation(renderable.getX());
            double y = axisY.getLocation(renderable.getY());
            if (renderable.getX() >= startX && renderable.getX() <= endX &&
                    renderable.getY() >= startY && renderable.getY() <= endY) {
                ctx.save();
                BBox box = renderable.render(x, y, ctx, dc);
                ctx.restore();
                if (overlappingRenderables) {
                    addGridIntersections(box, renderable);
                }
            }
        }

        ctx.restore();

        ctx.setStrokeStyle(CssColor.make(GRAY)); //$NON-NLS-1$
        ctx.beginPath();
        ctx.setLineWidth(2.0);
        ctx.moveTo(0, 0);
        ctx.lineTo(0, canvasHeight);
        ctx.lineTo(canvasWidth, canvasHeight);
        ctx.stroke();
        updatePositionIndicator();

        displayTitle();
    }

    private void displayTitle() {
        Context2d ctx = mainCanvas.getContext2d();
        ctx.save();
        ctx.setGlobalAlpha(0.5);
        ctx.setFillStyle(mainTitleDefinition.getFontColor());
        ctx.setFont(mainTitleDefinition.getFontSize() + "px Helvetica"); //$NON-NLS-1$
        ctx.setTextAlign(TextAlign.CENTER);
        ctx.setTextBaseline(TextBaseline.BOTTOM);
        ctx.fillText(mainTitleDefinition.getText(), mainCanvas.getOffsetWidth() / 2.0, 20 + mainTitleDefinition.getFontSize());
        ctx.restore();
    }

    */
/**
     * //TODO: make these actual controls.
     * Position Sliders on matrix that will tell you where you are in the big picture.
     *//*

    private void updatePositionIndicator() {

//        Native.log("Max X " + axisX.getMaxIndex() );
//        Native.log("Max Y " + axisY.getMaxIndex() );

        Context2d ctx = mainCanvas.getContext2d();
        ctx.save();
        ctx.setGlobalAlpha(.8);
        ctx.setFillStyle(GRAY);

        Context2d grid = gridCanvas.getContext2d();

        ctx.clearRect(0, 0, canvasWidth, 10);
        grid.clearRect(0,0,canvasWidth, 10);


        ctx.clearRect(canvasWidth-10, 0,10, canvasHeight);
        grid.clearRect(canvasWidth-10, 0,10, canvasHeight);

        ctx.save();
        ctx.setGlobalAlpha(1);
        ctx.setFillStyle("black");
        ctx.fillRect(0, 10, canvasWidth-10, 1);
        ctx.fillRect(canvasWidth - 10, 10, 1, canvasHeight);

        ctx.restore();
//        ctx.fillRect(canvasWidth-10,  10, canvasWidth, 1 );

        // Horizontal position
        {
            if (model.isSummary()) {
                double multiplier = (double) canvasWidth / model.getMatrixDataResponse().getMaxX();
                double end = axisX.getDataEnd() - axisX.getDataStart();
                double summaryS = axisX.getDataStart() * multiplier;
                double summaryE = end * multiplier;
                ctx.fillRect(summaryS, 0, summaryE, 10);
            } else {
                int xOffset = 0;
                if(!model.getMatrixDataResponse().isFullMatrix()){
                    Native.log("are you the issue here?");
                    double multiplier = (double) canvasWidth / model.getMatrixDataResponse().getMaxX();
                    double end = axisX.getDataEnd() - axisX.getDataStart();
                    double summaryS = axisX.getDataStart() * multiplier;
                    xOffset = (int)summaryS;
                }
                int x = (int) (axisX.getCurrentExtent().getStartIndex() / (double) axisX.getMaxIndex() * canvasWidth);
                int w = (int) ((axisX.getCurrentExtent().getEndIndex() - axisX.getCurrentExtent().getStartIndex() + 1) / (double) axisX.getMaxIndex() * canvasWidth);
                if(w < 5){
                    w = 5;
                }

//                new BBox(x + xOffset , 0, w, 10)
                ctx.fillRect(x + xOffset , 0, w, 10);
            }
        }

        // Vertical position
        {
            if (model.isSummary()) {

                double multiplier = (double)canvasHeight / model.getMatrixDataResponse().getMaxY();
                double end = axisY.getDataEnd() - axisY.getDataStart();
                double summaryS = axisY.getDataStart() * multiplier;
                double summaryE = end * multiplier;
//                Native.log("Y start: " + summaryS + " end: " + summaryE);
                summaryS = canvasHeight - summaryS - summaryE;
                ctx.fillRect(canvasWidth - 10, summaryS, 10, summaryE);

            } else {
                int yOffset = 0;
                if(!model.getMatrixDataResponse().isFullMatrix()){
                    double multiplier = (double)canvasHeight / model.getMatrixDataResponse().getMaxY();
                    double end = axisY.getDataEnd() - axisY.getDataStart();
                    double summaryS = axisY.getDataStart() * multiplier;
                    yOffset = (int)summaryS;
                }

                int y = (int) (axisY.getCurrentExtent().getStartIndex() / (double) axisY.getMaxIndex() * canvasHeight);
                int h = (int) ((axisY.getCurrentExtent().getEndIndex() - axisY.getCurrentExtent().getStartIndex() + 1) / (double) axisY.getMaxIndex() * canvasHeight);
                y = y+yOffset;
                y = canvasHeight - y - h;
                if(h < 5){
                    h = 5;
                }

                ctx.fillRect(canvasWidth - 10, y, 10, h);
            }
        }
        ctx.restore();
    }

    private void addGridIntersections(BBox box, ScrollableSurfaceRenderable renderable) {
        for (int x = box.x1; x <= box.x2 + DIM_HIT_GRID; x += DIM_HIT_GRID) {
            for (int y = box.y1; y <= box.y2 + DIM_HIT_GRID; y += DIM_HIT_GRID) {
                int gridNumber = (y / DIM_HIT_GRID) * horizontalHitGridCount + x / DIM_HIT_GRID + 1;
                List<ScrollableSurfaceRenderable> list = renderablesByGrid.get(gridNumber);
                if (list == null) {
                    list = new ArrayList<ScrollableSurfaceRenderable>(300);
                    renderablesByGrid.put(gridNumber, list);
                }
                list.add(renderable);
            }
        }
    }

    private double getZoomRatio() {
        double ratioX = axisX.getOffsetDimension() / axisX.getDomainValuesSize();
        double normX = Math.max(50, ratioX);
        double zoomX = axisX.getSegmentSize() / normX;

        double ratioY = axisY.getOffsetDimension() / axisY.getDomainValuesSize();
        double normY = Math.max(50, ratioY);
        double zoomY = axisY.getSegmentSize() / normY;

        return Math.min(zoomX, zoomY);
    }


    public ImagingRequest getImagingRequest(String overview) {
        ImagingRequest request = new ImagingRequest();
        request.setWidth(getOffsetWidth() + WIDTH_OVERVIEW); // add the extra space needed for the overview and the stats.
        request.setHeight(getOffsetHeight());

        PNGImageComponent gridPNG = new PNGImageComponent();
        gridPNG.setData(gridCanvas.toDataUrl(IMAGE_PNG)); //$NON-NLS-1$
        gridPNG.setX(WIDTH_LABEL);
        gridPNG.setY(0);
        request.addComponent(gridPNG);

        PNGImageComponent mainPNG = new PNGImageComponent();
        mainPNG.setData(mainCanvas.toDataUrl(IMAGE_PNG)); //$NON-NLS-1$
        mainPNG.setX(WIDTH_LABEL);
        mainPNG.setY(0);
        request.addComponent(mainPNG);

        PNGImageComponent xAxisPNG = new PNGImageComponent();
        xAxisPNG.setData(axisX.toDataUrl(IMAGE_PNG)); //$NON-NLS-1$
        xAxisPNG.setX(WIDTH_LABEL);
        xAxisPNG.setY(gridCanvas.getOffsetHeight());
        request.addComponent(xAxisPNG);

        PNGImageComponent yAxisPNG = new PNGImageComponent();
        yAxisPNG.setData(axisY.toDataUrl(IMAGE_PNG)); //$NON-NLS-1$
        request.addComponent(yAxisPNG);
        // This will add the color bar legend and the statistics to the export.
        PNGImageComponent legend = new PNGImageComponent();
        legend.setX(mainCanvas.getOffsetWidth() + WIDTH_LABEL);
        legend.setY(0);
        legend.setData(overview);
        request.addComponent(legend);

        return request;
    }

    public Rectangle getCurrentExtents() {
        if (lastExtentX != null && lastExtentY != null) {
            return new Rectangle(lastExtentX.getStartIndex(), lastExtentY.getStartIndex(), lastExtentX.getEndIndex()
                    - lastExtentX.getStartIndex(), lastExtentY.getEndIndex() - lastExtentY.getStartIndex());
        } else {
            return null;
        }
    }

    public void displayAxis(boolean display) {
        if (display) {
            axisDisplayed = true;
            surfaceContainer.setWidgetLeftRight(gridCanvas, dimLabel, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetTopBottom(gridCanvas, 0, Unit.PX, dimLabel, Unit.PX);
            surfaceContainer.setWidgetLeftRight(highlightCanvas, dimLabel, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetTopBottom(highlightCanvas, 0, Unit.PX, dimLabel, Unit.PX);
            surfaceContainer.setWidgetLeftRight(mainCanvas, dimLabel, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetTopBottom(mainCanvas, 0, Unit.PX, dimLabel, Unit.PX);

            surfaceContainer.setWidgetLeftRight(axisX, dimLabel, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetBottomHeight(axisX, 0, Unit.PX, dimLabel, Unit.PX);

            surfaceContainer.setWidgetLeftWidth(axisY, 0, Unit.PX, dimLabel, Unit.PX);
            surfaceContainer.setWidgetTopBottom(axisY, 0, Unit.PX, dimLabel, Unit.PX);
        } else {
            axisDisplayed = false;
            surfaceContainer.setWidgetLeftRight(gridCanvas, 0, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetTopBottom(gridCanvas, 0, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetLeftRight(highlightCanvas, 0, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetTopBottom(highlightCanvas, 0, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetLeftRight(mainCanvas, 0, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetTopBottom(mainCanvas, 0, Unit.PX, 0, Unit.PX);

            surfaceContainer.setWidgetLeftRight(axisX, dimLabel, Unit.PX, 0, Unit.PX);
            surfaceContainer.setWidgetBottomHeight(axisX, -dimLabel, Unit.PX, dimLabel, Unit.PX);

            surfaceContainer.setWidgetLeftWidth(axisY, -dimLabel, Unit.PX, dimLabel, Unit.PX);
            surfaceContainer.setWidgetTopBottom(axisY, 0, Unit.PX, dimLabel, Unit.PX);
        }
    }

    public void clear() {
        dragging = false;
        dragSelection = false;
        dragZoom = false;
        dragDeselect = false;
        renderables = new ArrayList();
        renderablesByLocation = new HashMap();
        renderablesByGrid = new HashMap();

    }
*/
}

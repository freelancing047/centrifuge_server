package csi.client.gwt.widget.ui.surface;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import csi.client.gwt.viz.matrix.MatrixMetricsView;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.server.common.model.visualization.matrix.MatrixType;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ColorUtil;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixWrapper;

import java.util.*;

public class MatrixMainCanvas extends BaseRenderable {
    private static final int RADI_SELECTION_RATIO = 4;//FIXME: magic numbers
    private static double MIN_SELECTION_STROKE_WIDTH = 2; //FIXME: magic numbers

    private static CssColor SELECTION_COLOR = CssColor.make(255, 133, 10);
    private static final double TWO_PI = Math.PI * 2;
    private static NumberFormat valueFormat = NumberFormat.getDecimalFormat();

    private final HashSet<Cell> cells = Sets.newHashSet();
    private MatrixView matrixView;
    private Layer layer;
    private boolean forceRender = true;
    private double oldX = -1, oldY = -1, oldWidth = -1, oldHeight = -1;
    private Canvas canvas;
    boolean isPan = false;
    double panX = 0.0;
    double panY = 0.0;
    UpdateTimer zoomUpdater;
    UpdateTimer regionZoomUpdater;
    UpdateTimer panUpdater;

    public class UpdateTimer extends Timer {
        @Override
        public void run() {
            matrixView.fetchData();
        }
    }

    public MatrixMainCanvas(MatrixView matrixView) {
        this.matrixView = matrixView;
        addHandlers();
    }

    public void addHandlers() {
        addSelectHandler();
        addZoomHandler();
        addPanHandler();
        addMouseMoveHandler();
        addMouseOutHandler();
    }

    public void addMouseOutHandler() {
        addMouseOutHandler(event -> {
            if (!matrixView.isLoaded()) {
                return;
            }
            matrixView.clearHoverCell();
            layer.getDrawingPanel().render();
        });

    }

    public void addMouseMoveHandler() {

        addMouseMoveHandler(new MouseMoveHandler() {
            private boolean isSearching = false;

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (!matrixView.isLoaded() || isPan || isSearching) {
                    return;
                }

                isSearching = true;

                MatrixModel model = matrixView.getModel();
                matrixView.clearHoverCell();
                final CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform();
                final double x = event.getX();
                final double y = event.getY();

                // TODO need to clean this up..
                if (x > 83 && y < layer.getHeight() - 83) {
                    try {
                        if (model != null && model.getSettings().getMatrixType() == MatrixType.HEAT_MAP && !matrixView.getModel().isSummary()) {
                            CSIContext2d.CanvasTransform t2 = t.copy();
                            t2.invert();

                            int i = (int) (t2.getX(x, y) + .5);
                            i = i - (i % model.getBinCountForAxis((int) model.getWidth()));
                            int i1 = (int) (t2.getY(x, y) + .5);
                            i1 = i1 - (i1 % model.getBinCountForAxis((int) model.getHeight()));

                            List<Cell> l = Lists.newArrayList();

                            for (Cell cell : matrixView.getModel().getMatrixDataResponse().getCells()) {
                                if (cell.getX() == i && cell.getY() == i1) {
                                    l.add(cell);
                                }
                            }

                            if (!l.isEmpty()) {
                                matrixView.setHoverCell(l.get(0).getX(), l.get(0).getY());
                            }

                        } else {
                            try {
                                List<Cell> l = Lists.newArrayList();
                                for (Cell cell : matrixView.getModel().getMatrixDataResponse().getCells()) {
                                    double x1 = t.getX(cell.getX(), cell.getY());
                                    double y1 = t.getY(cell.getX(), cell.getY());

                                    if (Math.sqrt(Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2)) < model.getScaledBubbleRadius(cell.getValue().doubleValue())) {
                                        l.add(cell);
                                    }
                                }
                                Collections.sort(l, new Comparator<Cell>() {

                                    @Override
                                    public int compare(Cell cell, Cell o2) {

                                        double x1 = t.getX(cell.getX(), cell.getY());
                                        double y1 = t.getY(cell.getX(), cell.getY());
                                        double x2 = t.getX(o2.getX(), o2.getY());
                                        double y2 = t.getY(o2.getX(), o2.getY());

                                        double v = Math.sqrt(Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2)) - Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
                                        if (v > 0) {
                                            return 1;
                                        }
                                        if (v < 0) {
                                            return -1;
                                        }
                                        return 0;

                                    }
                                });
                                if (!l.isEmpty()) {
                                    matrixView.setHoverCell(l.get(0).getX(), l.get(0).getY());
                                }

                            } catch (Exception ignored) {
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                try {
                    CSIContext2d.CanvasTransform t2 = t.copy();
                    t2.invert();
//                    closest x/y

                    int binCountForAxisY = model.getBinCountForAxis((int) model.getHeight());
                    int binCountForAxisX = model.getBinCountForAxis((int) model.getWidth());

                    int minX = (int) matrixView.getModel().getX();
                    int minY = (int) matrixView.getModel().getY();

                    int i = (int) (t2.getX(x, y) + .5);
                    int xMod = (int) ((i - minX) % binCountForAxisX);
                    i = (i - xMod) + binCountForAxisX / 2;

                    int i1 = (int) (t2.getY(x, y) + .5);
                    int yMod = ((i1 - minY) % binCountForAxisY);
                    i1 = (i1 - yMod) + binCountForAxisY / 2;

                    matrixView.setMouseXY(i, i1);

                } catch (Exception ignored) {

                }
                layer.getDrawingPanel().render();
                isSearching = false;
            }

        });

    }

    private void addPanHandler() {
        HandlesAllMouseEvents panHandler = new HandlesAllMouseEvents() {
            private Scheduler.RepeatingCommand fetch;
            private double epsilon = 8;
            private int y;
            private int x;
            boolean down = false;

            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (!matrixView.isLoaded()) {
                    return;
                }
                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                    if (!event.getNativeEvent().getCtrlKey() && !event.getNativeEvent().getShiftKey()) {
                        x = event.getX();
                        y = event.getY();
                        down = true;
                    }
                }
                event.preventDefault();

            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                //User must move at least epsilon before panning, small drags don't count
                if (down && Math.sqrt(Math.pow(x - event.getX(), 2) + Math.pow(y - event.getY(), 2)) > epsilon) {
                    isPan = true;
                }
                if (down && isPan) {
                    CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
                    t.invert();
                    double ex = t.getX(event.getX(), event.getY());
                    double ey = t.getY(event.getX(), event.getY());
                    double cx = t.getX(this.x, y);
                    double cy = t.getY(this.x, y);
                    MatrixModel model = matrixView.getModel();
                    model.setCurrentView(model.getX() - ex + cx, model.getY() - ey + cy, model.getWidth(), model.getHeight());


                    panY += event.getY() - y;
                    panX += event.getX() - x;
                    x = event.getX();
                    y = event.getY();
                    layer.getDrawingPanel().render();
                }
            }

            @Override
            public void onMouseOut(MouseOutEvent event) {
            }

            @Override
            public void onMouseOver(MouseOverEvent event) {

            }

            @Override
            public void onMouseUp(MouseUpEvent event) {
                if (down && isPan) {
                    isPan = false;
                    down = false;
                    panX = 0;
                    panY = 0;

                    if (panUpdater == null) {
                        panUpdater = new UpdateTimer();
                    } else {
                        panUpdater.cancel();
                    }
                    layer.getDrawingPanel().render();
                    panUpdater.schedule(500);
                } else {
                    down = false;
                }
            }

            @Override
            public void onMouseWheel(MouseWheelEvent event) {
            }
        };
        addMouseDownHandler(panHandler);
        addMouseUpHandler(panHandler);
        addMouseMoveHandler(panHandler);
        addMouseWheelHandler(panHandler);
    }

    private void addZoomHandler() {
        HandlesAllMouseEvents zoomHandler = new HandlesAllMouseEvents() {
            private Scheduler.RepeatingCommand fetch;
            private Rectangle renderable;
            private double epsilon = 2;
            private int y;
            private int x;
            boolean down = false;
            boolean isMove = false;

            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (!matrixView.isLoaded()) {
                    return;
                }
                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                    if (!event.getNativeEvent().getCtrlKey() && event.getNativeEvent().getShiftKey()) {
                        this.x = event.getX();
                        this.y = event.getY();
                        down = true;
                    }
                }

            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (down && Math.sqrt(Math.pow(x - event.getX(), 2) + Math.pow(y - event.getY(), 2)) > epsilon) {
                    isMove = true;
                    matrixView.clearHoverCell();
                    matrixView.setMouseXY(-1, -1);

                    layer.getDrawingPanel().render();
                    event.preventDefault();
                }

                if (down && isMove) {
                    if (renderable == null) {
                        renderable = new Rectangle() {
                            @Override
                            public boolean hitTest(double x, double y) {
                                return false;
                            }
                        };
                        renderable.setStrokeStyle(CssColor.make("black"));
                        int red = 255;
                        int green = 0;
                        int blue = 0;
                        double alpha = .2;

                        FillStrokeStyle f = CssColor.make("rgba(" + red + ", " + green + "," + blue + ", " + alpha + ")");
                        renderable.setFillStyle(f);
                        layer.addItem(renderable);
                    }
                    renderable.setX(this.x);
                    renderable.setY(this.y);
                    renderable.setW(event.getX() - this.x);
                    renderable.setH(event.getY() - this.y);
                    layer.getDrawingPanel().render();
                }
            }

            @Override
            public void onMouseOut(MouseOutEvent event) {
            }

            @Override
            public void onMouseOver(MouseOverEvent event) {

            }

            @Override
            public void onMouseUp(MouseUpEvent event) {
                if (down && isMove) {
                    if (renderable != null) {
                        layer.remove(renderable);
                        renderable = null;
                    }
                    MatrixModel model = matrixView.getModel();
                    CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
                    t.invert();
                    double ex = t.getX(event.getX(), event.getY());
                    double ey = t.getY(event.getX(), event.getY());
                    double cx = t.getX(this.x, this.y);
                    double cy = t.getY(this.x, this.y);

                    model.setCurrentView(Math.min(ex, cx), Math.min(ey, cy), Math.abs(ex - cx) + 1, Math.abs(ey - cy) + 1);

                    isMove = false;
                    down = false;
                    layer.getDrawingPanel().render();

                    if (matrixView.getModel().isSummary()) {
                        if (regionZoomUpdater == null) {
                            regionZoomUpdater = new UpdateTimer();
                        } else {
                            regionZoomUpdater.cancel();
                        }

                        regionZoomUpdater.schedule(300);
                    } else {
                        MatrixMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(model.getVisualizationUuid()));
                    }
                } else {
                    down = false;
                    isMove = false;
                }

            }

            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                if (!matrixView.isLoaded()) {
                    return;
                }
                MatrixModel model = matrixView.getModel();

                double pow;
                pow = Math.pow(2, .25);


                CSIContext2d.CanvasTransform d = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
                double mX =  d.getX(matrixView.getMouseX(), matrixView.getMouseY());
//                double mX = 267.6987732
                ;
                double mY =  d.getY(matrixView.getMouseX(), matrixView.getMouseY());
//                double mY = 1498.10839
                ;
                double anchorPx;
                double anchorCx;
                double anchorPy;
                double anchorCy;
                double y = 0;
                double x = 0;
                double y2 = 0;
                double x2 = 0;
                {
                    CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
                    y = t.getY(0, model.getY() + model.getHeight());
                    y2 = t.getY(0, model.getY());

                    x = t.getX(model.getX() + model.getWidth(), 0);
                    x2 = t.getX(model.getX(), 0);

                    t.invert();
                    anchorCx = t.getX(mX, mY);
                    anchorCy = t.getY(mX, mY);
                }
                {
                    CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
                    anchorPx = t.getX(anchorCx, anchorCy);
                    anchorPy = t.getY(anchorCx, anchorCy);
                }
                CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
                if (event.getDeltaY() > 0) {
                    t.scale(1 / pow, 1 / pow);
                } else {
                    t.scale(pow, pow);
                }
                t.invert();
                double sX = t.getX(anchorPx, anchorPy);
                double sY = t.getY(anchorPx, anchorPy);
                //t.translate(sX-anchorPx,sY-anchorPy);

                t.m4 = t.m4 - (sX - anchorCx);
                t.m5 = t.m5 - (sY - anchorCy);
                /*                double sX = t.getX(anchorCx, anchorCy);
                double sY = t.getY(anchorCx, anchorCy);
                //t.translate(sX-anchorPx,sY-anchorPy);

                t.m4 = t.m4 - (sX - anchorPx);
                t.m5 = t.m5 - (sY - anchorPy);
                t.invert();*/
                double aax = t.getX(mX, mY);//should equal mx,my
                double aay = t.getY(mX, mY);

                double minX = t.getX(x2, 0);
                double maxX = t.getX(x, 0);

                double maxY = t.getY(0, y);
                double minY = t.getY(0, y2);

                double width = maxX - minX;
                double height = maxY - minY;
                model.setCurrentView(minX, minY, width, height);


                layer.getDrawingPanel().render();

                if (zoomUpdater == null) {
                    zoomUpdater = new UpdateTimer();
                    zoomUpdater.schedule(200);
                } else {
                    zoomUpdater.cancel();
                    zoomUpdater = new UpdateTimer();
                    zoomUpdater.schedule(200);
                }
            }

        };
        addMouseDownHandler(zoomHandler);
        addMouseUpHandler(zoomHandler);
        addMouseMoveHandler(zoomHandler);
        addMouseWheelHandler(zoomHandler);
    }

    boolean shift = false;

    private void addSelectHandler() {
        HandlesAllMouseEvents selectHandler = new HandlesAllMouseEvents() {
            private Rectangle renderable;
            private double epsilon = 5;
            private int y;
            private int x;
            boolean down = false;

            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (!matrixView.isLoaded()) {
                    return;
                }

                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                    if (event.getNativeEvent().getCtrlKey()) {
                        down = true;

                        this.x = event.getX();
                        this.y = event.getY();

                        event.preventDefault();
                    }
                }

            }

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (down && Math.sqrt(Math.pow(x - event.getX(), 2) + Math.pow(y - event.getY(), 2)) > epsilon) {
                    isPan = true;

                    matrixView.clearHoverCell();
                    matrixView.setMouseXY(-1, -1);
                    layer.getDrawingPanel().render();
                }

                if (down && isPan) {
                    if (renderable == null) {
                        renderable = new Rectangle() {
                            @Override
                            public boolean hitTest(double x, double y) {
                                return false;
                            }
                        };
                        renderable.setStrokeStyle(CssColor.make("black"));
                        int red = 0;
                        int green = 0;
                        int blue = 255;
                        double alpha = .2;
                        FillStrokeStyle f = CssColor.make("rgba(" + red + ", " + green + "," + blue + ", " + alpha + ")");
                        renderable.setFillStyle(f);
                        layer.addItem(renderable);
                    }

                    renderable.setX(this.x);
                    renderable.setY(this.y);
                    renderable.setW(event.getX() - this.x);
                    renderable.setH(event.getY() - this.y);
                    layer.getDrawingPanel().render();
                }
            }

            @Override
            public void onMouseOut(MouseOutEvent event) {
            }

            @Override
            public void onMouseOver(MouseOverEvent event) {
            }

            @Override
            public void onMouseUp(MouseUpEvent event) {
                MatrixModel model = matrixView.getModel();
                CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
                t.invert();
                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                    if (down && isPan) {
                        layer.remove(renderable);
                        if (renderable != null) {
                            renderable = null;
                        }

                        double ex = t.getX(event.getX(), event.getY());
                        double ey = t.getY(event.getX(), event.getY());

                        double cx = t.getX(this.x, this.y);
                        double cy = t.getY(this.x, this.y);

                        double minX = Math.min(ex, cx);
                        double minY = Math.min(ey, cy);
                        double maxX = Math.max(ex, cx);
                        double maxY = Math.max(ey, cy);

                        if (model.isSummary()) {

                            MatrixDataRequest dr = new MatrixDataRequest();
                            dr.setVizUuid(matrixView.presenter.getVisualizationDef().getUuid());
                            dr.setDvUuid(matrixView.presenter.getDataViewUuid());
                            dr.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.DISALLOW_SUMMARY);
                            dr.setExtent((int) (minX + .5), (int) (maxX + .5), (int) (minY + .5), (int) (maxY + .5));

                            List<Cell> currentCells = new ArrayList<Cell>();
                            model.getMatrixDataResponse().getCells().forEach(c -> {
                                if (c.getX() >= minX && c.getX() <= maxX && c.getY() >= minY && c.getY() <= maxY) {
                                    currentCells.add(c);
                                }
                            });


                            shift = event.isShiftKeyDown();
                            if (shift) {
                                model.getSelectedCells().removeIf(c -> c.getX() >= minX && c.getX() <= maxX && c.getY() >= minY && c.getY() <= maxY);
                                matrixView.presenter.getView().refresh();
                            } else {
                                matrixView.setLoadingIndicator(true);
                                selectSummaryCells(currentCells);
//                                Vortex vortex = matrixView.presenter.getVortex();
//                                VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();
//                                vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
//                                    @Override
//                                    public void onSuccess(MatrixWrapper result) {
//                                        result.getData().getCells().forEach(cell -> {
//                                            matrixView.presenter.getModel().selectCell(cell);
//                                        });
//                                        shift = false;
//                                        matrixView.setLoadingIndicator(false);
//                                        matrixView.presenter.getView().refresh();
//                                    }
//                                });
//
//                                vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(dr);
//                                matrixView.setLoadingIndicator(true);
                            }

                        } else {
                            for (Cell cell : cells) {
                                if (isInViewport(cell)) {
                                    if (isInRange(cell, minX, minY, maxX, maxY)) {
                                        if (event.isShiftKeyDown()) {
                                            model.deselectCell(cell);
                                        } else {
                                            model.selectCell(cell);
                                        }
                                    }
                                }
                            }
                        }

                        forceRender = true;
                        isPan = false;
                        down = false;
                        layer.getDrawingPanel().render();
                    } else if (!isPan && down) {
                        Cell hoverCell = matrixView.getHoverCell();
                        down = false;
                        shift = event.isShiftKeyDown();
                        if (hoverCell != null && isInViewport(hoverCell)) {
                            matrixView.presenter.selectCell(hoverCell, shift);
                        }
                    }
                }
            }

            @Override
            public void onMouseWheel(MouseWheelEvent event) {

            }
        };

        addMouseDownHandler(selectHandler);
        addMouseUpHandler(selectHandler);
        addMouseMoveHandler(selectHandler);
        addMouseWheelHandler(selectHandler);


    }

    private void selectSummaryCells(List<Cell> cells) {
        MatrixModel model = matrixView.getModel();
        int bucketX = model.getBinCountForAxis((int) model.getWidth());
        int bucketY = model.getBinCountForAxis((int) model.getHeight());

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Cell c : cells) {
            // min xy
            int sX = c.getX() - bucketX / 2;
            int sY = c.getY() - bucketY / 2;

            // max xy
            int eX = sX + bucketX - 1;
            int eY = sY + bucketY - 1;

            minX = Math.min(sX, minX);
            minY = Math.min(sY, minY);

            maxX = Math.max(eX, maxX);
            maxY = Math.max(eY, maxY);
        }

        MatrixDataRequest dr = new MatrixDataRequest();
        dr.setVizUuid(matrixView.presenter.getVisualizationDef().getUuid());
        dr.setDvUuid(matrixView.presenter.getDataViewUuid());
        dr.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.DISALLOW_SUMMARY);
        dr.setExtent(minX, maxX, minY, maxY);
        Vortex vortex = matrixView.presenter.getVortex();
        VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {
                result.getData().getCells().forEach(cell -> {
                    matrixView.presenter.getModel().selectCell(cell);
                });
                shift = false;
                matrixView.setLoadingIndicator(false);
                matrixView.presenter.getView().refresh();
            }
        });
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(dr);
    }

    private void selectByCell(Cell cell) {
        MatrixModel model = matrixView.getModel();

        int bucketX = model.getBinCountForAxis((int) model.getWidth());
        int bucketY = model.getBinCountForAxis((int) model.getHeight());

        int sX = cell.getX() - bucketX / 2;
        int sY = cell.getY() - bucketY / 2;

        int eX = sX + bucketX - 1;
        int eY = sY + bucketY - 1;

        MatrixDataRequest dr = new MatrixDataRequest();
        dr.setVizUuid(matrixView.presenter.getVisualizationDef().getUuid());
        dr.setDvUuid(matrixView.presenter.getDataViewUuid());
        dr.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.DISALLOW_SUMMARY);
        dr.setExtent(sX, eX, sY, eY);
        Vortex vortex = matrixView.presenter.getVortex();
        VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {
                result.getData().getCells().forEach(cell -> {
                    matrixView.presenter.getModel().selectCell(cell);
                });
                shift = false;
                matrixView.setLoadingIndicator(false);
                matrixView.presenter.getView().refresh();
            }
        });
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(dr);
    }

    @Override
    public void render(Context2d ctx2) {
        if (!matrixView.isLoaded()) {
            return;
        }
        ctx2.beginPath();

        //FIXME: might need more to handle low category counts.
        MatrixModel model = matrixView.getModel();
        CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform();
        if ((model.getX() != oldX) || (model.getY() != oldY) || (model.getWidth() != oldWidth) || (model.getHeight() != oldHeight) || (forceRender)) {
            if (!isPan) {
                oldX = model.getX();
                oldY = model.getY();
                oldWidth = model.getWidth();
                oldHeight = model.getHeight();
                forceRender = false;
                canvas = Canvas.createIfSupported();
                Context2d ctx = canvas.getContext2d();
                canvas.setCoordinateSpaceHeight(layer.getHeight());
                canvas.setCoordinateSpaceWidth(layer.getWidth());

                model.setupSummarySelection();

                HashSet<Cell> cells = this.cells;
                List<Cell> ce = Lists.newArrayList(cells);
                ce.sort(new Comparator<Cell>() {

                    @Override
                    public int compare(Cell o1, Cell o2) {
//                    return ComparisonChain.start().compareFalseFirst(o1 == matrixView.getHoverCell(), o2 == matrixView.getHoverCell()).compare(-o1.getValue().doubleValue(), -o2.getValue().doubleValue()).result();
                        return ComparisonChain.start().compare(-o1.getValue().doubleValue(), -o2.getValue().doubleValue()).result();
                    }
                });

                for (Cell cell : ce) {
                    if (!isInViewport(cell)) {
                        continue;
                    }
                    ctx.save();
                    t.moveOrigin(ctx, cell.getX(), cell.getY());

                    double cellValue = cell.getValue().doubleValue();
                    double currentRadius = model.getScaledBubbleRadius(cellValue);

                    String color = model.getColor(cellValue);
                    switch (matrixView.getModel().getSettings().getMatrixType()) {

                        case BUBBLE:
                            if (matrixView.getModel().isSummary()) {
                                renderSummaryBubble(ctx, currentRadius, color);
                            } else {
                                renderBubble(ctx, currentRadius, color);
                            }
                            if (model.isShowValue()) {
                                renderValue(ctx, cellValue, currentRadius, color);
                            }
                            break;
                        case HEAT_MAP:
                            if (matrixView.getModel().isSummary()) {
                                renderSummaryHeat(ctx, currentRadius, color);
                            } else {

                                renderHeat(ctx, currentRadius, color);
                            }
                            if (model.isShowValue()) {
                                renderValue(ctx, cellValue, currentRadius, color);
                            }
                            break;
                        case CO_OCCURRENCE:
                            break;
                        case CO_OCCURRENCE_DIR:
                            break;
                    }

                    if (model.isSelected(cell)) {
                        renderSelectionDecoration(ctx, currentRadius);
                    }
                    ctx.restore();
                }
            } else {
                t = new CSIContext2d.CanvasTransform();
                t.multiply(((MatrixMainLayer) layer).getAxisMask());
                double sx = t.getX(0, 0);
                double sy = t.getY(0, 0);
                double width = t.getX(layer.getWidth(), layer.getHeight()) - t.getX(0, 0);
                double height = t.getY(layer.getWidth(), layer.getHeight()) - t.getY(0, 0);
                ctx2.drawImage(canvas.getCanvasElement(), sx - panX, sy - panY, width, height, sx, sy, width, height);
                return;
            }
        }
        t = new CSIContext2d.CanvasTransform();
        t.multiply(((MatrixMainLayer) layer).getAxisMask());
        double sx = t.getX(0, 0);
        double sy = t.getY(0, 0);
        double width = t.getX(layer.getWidth(), layer.getHeight()) - t.getX(0, 0);
        double height = t.getY(layer.getWidth(), layer.getHeight()) - t.getY(0, 0);
        ctx2.drawImage(canvas.getCanvasElement(), sx, sy, width, height, sx, sy, width, height);
    }

    private void renderSummaryHeat(Context2d ctx, double currentRadius, String color) {
        CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform();
//        double dx = t.getX(2, 1) - t.getX(1, 1);
//        double dy = t.getY(1, 2) - t.getY(1, 1);
        double dx = currentRadius;
        double dy = currentRadius;
        ctx.setStrokeStyle(color);
        ctx.setGlobalAlpha(.75);

        ctx.beginPath();
        ctx.moveTo(-.5 * dx, -.5 * dy);
        ctx.lineTo(-.5 * dx, .5 * dy);
        ctx.lineTo(.5 * dx, .5 * dy);
        ctx.lineTo(.5 * dx, -.5 * dy);
        ctx.closePath();
        ctx.stroke();
        ClientColorHelper.Color color1 = ClientColorHelper.get().makeFromHex(color.substring(1, color.length()));
        int red = color1.getRed();
        int blue = color1.getBlue();
        int green = color1.getGreen();
        red = (int) Math.sqrt(Math.pow(red, 2) * .25);
        blue = (int) Math.sqrt(Math.pow(blue, 2) * .25);
        green = (int) Math.sqrt(Math.pow(green, 2) * .25);

        ctx.setStrokeStyle(CssColor.make(ClientColorHelper.get().makeFromRGB(red, green, blue).toString()));
        ctx.setLineWidth(2);
        ctx.stroke();
        ctx.beginPath();
        ctx.closePath();
    }

    private void renderHeat(Context2d ctx, double currentRadius, String color) {
        CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform();
        double dx = t.getX(2, 1) - t.getX(1, 1);
        double dy = t.getY(1, 2) - t.getY(1, 1);
        ctx.setFillStyle(color);
        ctx.setGlobalAlpha(.75);

        ctx.beginPath();
        ctx.moveTo(-.5 * dx, -.5 * dy);
        ctx.lineTo(-.5 * dx, .5 * dy);
        ctx.lineTo(.5 * dx, .5 * dy);
        ctx.lineTo(.5 * dx, -.5 * dy);

        ctx.closePath();
        ctx.fill();
        ClientColorHelper.Color color1 = ClientColorHelper.get().makeFromHex(color.substring(1, color.length()));
        int red = color1.getRed();
        int blue = color1.getBlue();
        int green = color1.getGreen();
        red = (int) Math.sqrt(Math.pow(red, 2) * .25);
        blue = (int) Math.sqrt(Math.pow(blue, 2) * .25);
        green = (int) Math.sqrt(Math.pow(green, 2) * .25);

        ctx.setStrokeStyle(CssColor.make(ClientColorHelper.get().makeFromRGB(red, green, blue).toString()));
        ctx.setLineWidth(0.5);
        ctx.stroke();
        ctx.closePath();

    }

    public boolean isInViewport(Cell cell) {
        MatrixModel model = matrixView.getModel();
        double minX = model.getX();
        double minY = model.getY();
        double maxX = model.getX() + model.getWidth() - 1;
        double maxY = model.getY() + model.getHeight() - 1;

        return isInRange(cell, minX, minY, maxX, maxY);
    }

    public boolean isInRange(Cell cell, double minX, double minY, double maxX, double maxY) {
        return ((!(cell.getX() < minX)) && (!(cell.getY() < minY)) && (!(cell.getX() > maxX)) && (!(cell.getY() > maxY)));
    }

    private void renderHover(Context2d ctx, double currentRadius, String color) {
        ctx.setFillStyle(color);
        ctx.setGlobalAlpha(.75);

        ctx.beginPath();
        ctx.arc(0, 0, Math.abs(currentRadius), 0, TWO_PI, true);
        ctx.closePath();
        ctx.fill();
        ctx.setLineWidth(1);
        ctx.stroke();
        currentRadius += Math.min(currentRadius, 15);
/*
        ctx.beginPath();
        ctx.arc(0, 0, Math.abs(currentRadius), 0, TWO_PI, true);
        ctx.closePath();
        ctx.setLineWidth(2);
        ctx.stroke();
        ctx.setLineWidth(.5);*/

    }

    private void renderBubble(Context2d ctx, double currentRadius, String color) {

        ctx.setFillStyle(color);
        ctx.setGlobalAlpha(.75);

        ctx.beginPath();
        ctx.arc(0, 0, Math.abs(currentRadius), 0, TWO_PI, true);
        ctx.closePath();
        ctx.fill();
        ClientColorHelper.Color color1 = ClientColorHelper.get().makeFromHex(color.substring(1, color.length()));
        int red = color1.getRed();
        int blue = color1.getBlue();
        int green = color1.getGreen();
        red = (int) Math.sqrt(Math.pow(red, 2) * .25);
        blue = (int) Math.sqrt(Math.pow(blue, 2) * .25);
        green = (int) Math.sqrt(Math.pow(green, 2) * .25);

        ctx.setStrokeStyle(CssColor.make(ClientColorHelper.get().makeFromRGB(red, green, blue).toString()));
        ctx.setLineWidth(.5);
        ctx.stroke();
        ctx.beginPath();
        ctx.closePath();

    }

    private void renderSummaryBubble(Context2d ctx, double currentRadius, String color) {
        ctx.setGlobalAlpha(.75);

        ctx.setStrokeStyle(color);
        ctx.setLineWidth(2);
        ctx.beginPath();
        ctx.arc(0, 0, Math.abs(currentRadius), 0, TWO_PI, true);
        ctx.closePath();
        ctx.stroke();


        ctx.setStrokeStyle("black");
        ctx.setLineWidth(.5);
        ctx.beginPath();
        ctx.arc(0, 0, Math.abs(currentRadius), 0, TWO_PI, true);
        ctx.closePath();
        ctx.stroke();

    }

    private void renderValue(Context2d ctx, double cellValue, double currentRadius, String color) {
        ColorUtil.HSL hsl = ColorUtil.toHSL(color);
        hsl = ColorUtil.getContrastingGrayScale(hsl);
        ctx.setFillStyle(CssColor.make(ColorUtil.toColorString(hsl)));
        ctx.setFont(12 + "px Arial");
        String val = valueFormat.format(cellValue);
        int i = (int) ctx.measureText(val).getWidth() / 2;


        boolean isHeatmap = matrixView.getModel().getSettings().getMatrixType() == MatrixType.HEAT_MAP;

        if (currentRadius < 12 & !isHeatmap) {
            ctx.fillText(val, -i, -currentRadius - 1);
        } else {
            ctx.fillText(val, -i, 6);
        }

    }

    private void renderSelectionDecoration(Context2d ctx, double currentRadius) {
        ctx.setStrokeStyle(SELECTION_COLOR);
        ctx.setFillStyle(SELECTION_COLOR);

        ctx.setGlobalAlpha(1);

        if (matrixView.getModel().getSettings().getMatrixType() == MatrixType.HEAT_MAP) {
            ctx.save();
            double dx, dy;
            if (matrixView.getModel().isSummary()) {
                dx = currentRadius;
                dy = currentRadius;

                double lineWidth = currentRadius / RADI_SELECTION_RATIO;
                if (Double.compare(lineWidth, MIN_SELECTION_STROKE_WIDTH) < 0) {
                    lineWidth = MIN_SELECTION_STROKE_WIDTH;
                }

                ctx.beginPath();
                ctx.moveTo(-.5 * dx + lineWidth / 2.0, -.5 * dy + lineWidth / 2.0);
                ctx.lineTo(-.5 * dx + lineWidth / 2.0, .5 * dy - lineWidth / 2.0);
                ctx.lineTo(.5 * dx - lineWidth / 2.0, .5 * dy - lineWidth / 2.0);
                ctx.lineTo(.5 * dx - lineWidth / 2.0, -.5 * dy + lineWidth / 2.0);
                ctx.closePath();
                ctx.setLineWidth(lineWidth);
                ctx.stroke();

            } else {

                CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform();
                dx = t.getX(2, 1) - t.getX(1, 1);
                dy = t.getY(1, 2) - t.getY(1, 1);

                double min = Math.min(Math.abs(dx), Math.abs(dy)) * .1;
                double lineWidth = Math.min(min, 8) + 2;
                ctx.beginPath();
                ctx.moveTo(-.5 * dx + lineWidth / 2.0, -.5 * dy - lineWidth / 2.0);

                ctx.lineTo(-.5 * dx + lineWidth / 2.0, .5 * dy + lineWidth / 2.0);
                ctx.lineTo(.5 * dx - lineWidth / 2.0, .5 * dy + lineWidth / 2.0);
                ctx.lineTo(.5 * dx - lineWidth / 2.0, -.5 * dy - lineWidth / 2.0);

                ctx.closePath();

                ctx.setLineWidth(lineWidth);

                ctx.stroke();

            }


            ctx.setLineWidth(MIN_SELECTION_STROKE_WIDTH);
            ctx.beginPath();
            ctx.closePath();


            ctx.restore();
        } else {
            double lineWidth = currentRadius / RADI_SELECTION_RATIO;
            if (Double.compare(lineWidth, MIN_SELECTION_STROKE_WIDTH) < 0) {
                lineWidth = MIN_SELECTION_STROKE_WIDTH;
            }

            Double radius = Math.abs(currentRadius - lineWidth / 2);
            if (Double.compare(radius, MIN_SELECTION_STROKE_WIDTH) < 0) {
                radius = MIN_SELECTION_STROKE_WIDTH / 2;
            }

            ctx.arc(0, 0, radius, 0, TWO_PI, true);
            ctx.setLineWidth(lineWidth);
        }

        ctx.stroke();
        ctx.setGlobalAlpha(1.0);
    }

    @Override
    public boolean hitTest(double x, double y) {
        CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getAxisMask();
//        t.invert();

        MatrixModel model = matrixView.getModel();
        if (model != null && t != null) {
            double xMin = t.getX(0, 0);
            double yMax = t.getY(0, 0);
            double xMax = t.getX(layer.getWidth(), layer.getHeight());
            double yMin = t.getY(layer.getWidth(), layer.getHeight());
            if (x >= xMin && x <= xMax && y <= yMax && y >= yMin) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    public void setCells(Collection<Cell> _cells) {
        cells.clear();
        cells.addAll(_cells);
    }

    public void forceRender() {
        forceRender = true;
    }


    // need to split for the summary/not
    public int getCategoryX(int clientX, int clientY) {
        CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
        t.invert();
        double cx = t.getX(clientX, clientY);
        return (int) cx;
    }

    public int getCategoryY(int clientX, int clientY) {
        CSIContext2d.CanvasTransform t = ((MatrixMainLayer) layer).getMainCanvasTransform().copy();
        t.invert();
        double cy = t.getY(clientX, clientY);
        return (int) cy;
    }


}
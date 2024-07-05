package csi.client.gwt.viz.graph.surface;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.HandlesAllMouseEvents;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.CancellableScheduleCommand;
import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTip;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTipManager;
import csi.client.gwt.viz.shared.BroadcastAlert;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.NeighborPropDTO;
import csi.server.common.dto.graph.gwt.NodeMapDTO;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.shared.core.color.ClientColorHelper;

public class ViewImpl extends Composite implements GraphSurface.View, RequiresResize {
   private static final Logger LOG = Logger.getLogger(GraphSurface.View.class.getName());
   
    private static final double DEFAULT_ZOOM_LEVEL = .411;
    private static final int MAX_LINKS_DRAWN_ON_DRAG_IMAGE = 50000;

    private static ImageElement dashedImage = null;

    private Canvas backgroundCanvas;
    private FillStrokeStyle backgroundFill;
    private Canvas mainCanvas;
    private Cursor cursor = Cursor.AUTO; // for memoizing
    private Canvas foregroundCanvas;
    private GraphSurface graphSurface;
    private FillStrokeStyle lastFilledBackgroundWith;
    private AbsolutePanel absolutePanel;
    private int lastHeight = -1;
    private int lastWidth = -1;
    private CancellableScheduleCommand resizeCommand = null;

    public ViewImpl(GraphSurface graphSurface) {
        this.graphSurface = graphSurface;
        mainCanvas = Canvas.createIfSupported();
        // TODO: How should I fail if canvas is not supported
        checkNotNull(mainCanvas);
        setCanvasWidthHeight(mainCanvas);
        // TODO:remove later. this was here because we wanted to set use it for requesting full screen
        // canvas.getCanvasElement().setId("canvas-"+graphSurfaceFactory.getVizUuid());

        backgroundCanvas = Canvas.createIfSupported();
        checkNotNull(backgroundCanvas);
        setCanvasWidthHeight(backgroundCanvas);

        foregroundCanvas = Canvas.createIfSupported();
        checkNotNull(foregroundCanvas);
        setCanvasWidthHeight(foregroundCanvas);

        absolutePanel = new AbsolutePanel();
        absolutePanel.add(backgroundCanvas, 0, 0);
        absolutePanel.add(mainCanvas, 0, 0);
        absolutePanel.add(foregroundCanvas, 0, 0);
        initWidget(absolutePanel);
        addhandlers();
    }

    private void addhandlers() {
        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        if (mouseHandler != null) {
            HandlesAllMouseEvents.handle(foregroundCanvas, mouseHandler);
            foregroundCanvas.addClickHandler(mouseHandler);
            foregroundCanvas.addDoubleClickHandler(mouseHandler);
        }

        KeyHandler keyHandler = graphSurface.getKeyHandler();
        if (keyHandler != null) {
            // Need to add handler to root so I don't miss any.
            RootPanel.get().addDomHandler(keyHandler, KeyDownEvent.getType());
            RootPanel.get().addDomHandler(keyHandler, KeyUpEvent.getType());
            RootPanel.get().addDomHandler(keyHandler, KeyPressEvent.getType());
        }

        ContextMenuHandler contextMenuHandler = new GraphSurfaceContextMenuHandler(graphSurface);
        if (contextMenuHandler != null) {
            foregroundCanvas.addDomHandler(contextMenuHandler, ContextMenuEvent.getType());
        }        
    }

    private void setCanvasWidthHeight(Canvas canvas) {
        canvas.setWidth("100%");
        canvas.setHeight("100%");
    }

    @Override
    public void draw(Image image, double xOffset, double yOffset, double zoom) {
        if (image == null) {
            return;
        }
        // checkNotNull(image);
        drawBackground();
        clearForeground();
        double width = image.getWidth();
        double height = image.getHeight();
        resetCoordinateSpace(height / width);
        Context2d context = mainCanvas.getContext2d();
        blur(mainCanvas.getCanvasElement());
        context.setTransform(1D, 0D, 0D, 1D, 0D, 0D);
        context.clearRect(0D, 0D, getCanvasWidth(), getCanvasHeight());
        // double dx = (getCanvasWidth() / 2D) + xOffset;
        // double dy = (getCanvasHeight() / 2D) + yOffset;
        context.setTransform(zoom, 0D, 0D, zoom, xOffset, yOffset);
        // context.scale(zoom, zoom);
        ImageElement imageElement = ImageElement.as(image.getElement());
        try {
            context.drawImage(imageElement, 0, 0, width, height, 0, 0, width, height);
        } catch (Exception e) {
            imageElement.setSrc(imageElement.getSrc());
            graphSurface.getPresenter().draw();
        }
        drawToolTipLines();
    }

    public static native void blur(Element canvas) /*-{
    var ctx = canvas.getContext('2d');


    }-*/;
//ctx.filter = 'saturate(30%)';
    @Override
    public void drawDragImage(int x, int y, Image dragImage, List<NodeMapDTO> dragNodes) {
        // TODO: param should probably not be a list of DTOs, but some inner PointToPoint class
        checkNotNull(dragImage);
        int linksDrawn = 0;
        foregroundCanvas.setCoordinateSpaceHeight(getCanvasHeight());
        foregroundCanvas.setCoordinateSpaceWidth(getCanvasWidth());
        Context2d context = setupDrawingContext();
        context.beginPath();
        // FIXME: This could use some clean up. But works for now.
        for (NodeMapDTO nodeMapDTO : dragNodes) {
            List<NeighborPropDTO> neighbors = nodeMapDTO.neighbors;
            for (NeighborPropDTO neighborPropDTO : neighbors) {
                context.moveTo(x + nodeMapDTO.relativeX, y + nodeMapDTO.relativeY);
                if ((neighborPropDTO.isInSelection != null) && neighborPropDTO.isInSelection) {
                    context.lineTo(x + neighborPropDTO.relativeX, y + neighborPropDTO.relativeY);
                } else {
                    context.lineTo(neighborPropDTO.displayX, neighborPropDTO.displayY);
                }
                // Give up if there are many lines.
                if (linksDrawn++ > MAX_LINKS_DRAWN_ON_DRAG_IMAGE) {
                    LOG.info("Too many lines to draw for this drag image.");
                    break;
                }
            }
        }
        if (linksDrawn < MAX_LINKS_DRAWN_ON_DRAG_IMAGE) {
            context.stroke();
        }
        ImageElement ie = ImageElement.as(dragImage.getElement());
        context.drawImage(ie, x, y);//, dragImage.getWidth(), dragImage.getHeight());
    }

    @Override
    public void drawRect(int startX, int startY, int mouseX, int mouseY, String label) {
        Context2d context = mainCanvas.getContext2d();
        context.setTransform(1D, 0D, 0D, 1D, 0D, 0D);
        // FIXME: Style the overlay box here
        int red = 255;
        int green = 0;
        int blue = 0;
        double alpha = .2;
        FillStrokeStyle f = CssColor.make("rgba(" + red + ", " + green + "," + blue + ", " + alpha + ")");
        context.setFillStyle(f);
        context.setStrokeStyle(CssColor.make(255, 0, 0));
        double x = startX;
        double y = startY;
        double w = mouseX - startX;
        double h = mouseY - startY;
        context.fillRect(x, y, w, h);
        context.strokeRect(x, y, w, h);
        context.setFont("bold 12px sans-serif");
        context.setFillStyle(CssColor.make(0, 0, 0));
        context.fillText(label, startX, startY - 2, 120);
    }

    @Override
    public void drawTowards(Image image, int startX, int startY, double zoom) {
        checkNotNull(image);
        drawBackground();
        double width = image.getWidth();
        double height = image.getHeight();
        resetCoordinateSpace(height / width);
        Context2d context = mainCanvas.getContext2d();
        context.setTransform(1D, 0D, 0D, 1D, 0D, 0D);
        context.clearRect(0D, 0D, getCanvasWidth(), getCanvasHeight());
        context.translate(startX, startY);
        context.scale(zoom, zoom);
        context.translate(-startX, -startY);
        ImageElement imageElement = ImageElement.as(image.getElement());
        context.drawImage(imageElement, 0, 0, width, height);
    }

    @Override
    public ImageData getImageDataAt(int x, int y) {
        Context2d context2d = mainCanvas.getContext2d();
        return context2d.getImageData(x, y, 1D, 1D);
    }

    @Override
    public ImageData getImageDataAt(int x, int y, int w, int h) {
        Context2d context2d = mainCanvas.getContext2d();
        return context2d.getImageData(x, y, w, h);
    }
    @Override
    public void onResize() {
        
        if(resizeCommand != null) {
            resizeCommand.cancel();
        }
        
        this.resizeCommand = new CancellableScheduleCommand() {
            
            private boolean cancel = false;

            @Override
            public void execute() {
                if(cancel) {
                    return;
                }
                int offsetHeight = getOffsetHeight();
                int offsetWidth = getOffsetWidth();
                
                //Height and width invalid, return
                if(offsetHeight == 0 && offsetWidth == 0) {
                    return;
                }
                

                //if neither height nor width have changed we do not need to resize.
                if ((lastHeight != offsetHeight || lastWidth != offsetWidth) && graphSurface.getPresenter() != null && graphSurface.asWidget().isAttached()) {
                    lastHeight = offsetHeight;
                    lastWidth = offsetWidth;
                    IsWidget view = graphSurface.getView();
                    VortexFuture<Boolean> zoomScale;
                    Model model = graphSurface.getModel();
                    
                    zoomScale = model.shouldFitToSizeBasedOnBounds(view.asWidget().getOffsetWidth(), view.asWidget().getOffsetHeight());
                    
                    zoomScale.addEventHandler(new AbstractVortexEventHandler<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if(result)
                                graphSurface.zoomToFit();
                            else {
                                graphSurface.refresh();
                            }
                        }
                    });
                } 
                else if(graphSurface.getPresenter() != null && graphSurface.asWidget().isAttached()){
                    graphSurface.refresh();
                }
            }
            
            @Override
            public void cancel() {
                this.cancel  = true;
            }
        };
        Scheduler.get().scheduleDeferred(resizeCommand);
        
    }

    @Override
    public void removeFromGraph(int x, int y, Image dragImage) {
        checkNotNull(dragImage);
        Context2d context = mainCanvas.getContext2d();
        context.setTransform(1D, 0D, 0D, 1D, 0D, 0D);
        String oldComposite = context.getGlobalCompositeOperation();
        double oldAlpha = context.getGlobalAlpha();
        context.setGlobalCompositeOperation(com.google.gwt.canvas.dom.client.Context2d.Composite.DESTINATION_OUT);
        ImageElement ie = ImageElement.as(dragImage.getElement());
        context.drawImage(ie, x, y, dragImage.getWidth(), dragImage.getHeight());
        context.setGlobalCompositeOperation(com.google.gwt.canvas.dom.client.Context2d.Composite.SOURCE_OVER);
        context.setGlobalAlpha(.2);
        context.drawImage(ie, x, y, dragImage.getWidth(), dragImage.getHeight());
        // reset globals to previous values
        context.setGlobalCompositeOperation(oldComposite);
        context.setGlobalAlpha(oldAlpha);
    }

    @Override
    public void setBackgroundFill(FillStrokeStyle backgroundFill) {
        this.backgroundFill = backgroundFill;
    }

    @Override
    public void setCursor(Cursor cursor) {
        if (this.cursor != cursor) {
            this.cursor = cursor;
            foregroundCanvas.getCanvasElement().getStyle().setCursor(cursor);
        }
    }

    public void drawToolTipLines() {
        clearForeground();
        foregroundCanvas.setCoordinateSpaceHeight(getCanvasHeight());
        foregroundCanvas.setCoordinateSpaceWidth(getCanvasWidth());
        Context2d context2d = foregroundCanvas.getContext2d();
        ToolTipManager ttm = graphSurface.getToolTipManager();
        List<ToolTip> toolTips = ttm.getToolTips();
        for (ToolTip toolTip : toolTips) {
            if (graphSurface.getToolTipManager().showOnHoverOnly()) {
                if (toolTip.isMouseOver()) {
                    drawToolTipLine(context2d, toolTip);
                }
            } else { // always draw
                drawToolTipLine(context2d, toolTip);
            }
        }
    }

    @Override
    public void drawLineWithCircleIndicatingStart(double startX, double startY, double endX, double endY) {
        clearForeground();
        foregroundCanvas.setCoordinateSpaceHeight(getCanvasHeight());
        foregroundCanvas.setCoordinateSpaceWidth(getCanvasWidth());

        Context2d context2d = setupDrawingContext();

        drawCircleAtStart(context2d, startX, startY);
        drawLine(context2d, startX, startY, endX, endY);

    }

    private void drawCircleAtStart(Context2d context2d, double startX, double startY) {
        context2d.beginPath();
        context2d.arc(startX, startY, 2, 0, 10);
        context2d.stroke();
        context2d.fill();
    }

    private void drawLine(Context2d context2d, double startX, double startY, double endX, double endY) {
        context2d.beginPath();
        context2d.moveTo(startX, startY);
        context2d.lineTo(endX, endY);
        context2d.stroke();
    }

    private Context2d setupDrawingContext() {
        Context2d context2d = foregroundCanvas.getContext2d();
        context2d.setTransform(1D, 0D, 0D, 1D, 0D, 0D);
        context2d.setFillStyle(CssColor.make(0, 0, 0));
        context2d.setStrokeStyle(CssColor.make(0, 0, 0));
        RelGraphViewDef graphDef = (RelGraphViewDef) graphSurface.getGraph().getVisualizationDef();
        String value = graphDef.getSettings().getPropertiesMap().get("csi.relgraph.backgroundColor");
        if (value != null) {
            try {
                int color = Integer.parseInt(value);
                if (color == 16777215) { //TODO:looks like this matters
                    color = 16711422;
                }
                ClientColorHelper.Color bgc = ClientColorHelper.get().make(color);
                int retries = 10;
                //L = 0.2126 * Rg + 0.7152 * Gg + 0.0722 * Bg,
                float bgluminance = (float) (.2126 * bgc.getRed() + .7152 * bgc.getGreen() + .0722 * bgc.getBlue()) / 255;
                if (bgluminance < .5) {
                    context2d.setFillStyle(CssColor.make(255, 255, 255));
                    context2d.setStrokeStyle(CssColor.make(255, 255, 255));
                }
            } catch (Exception ignored) {

            }
        }
        return context2d;
    }

    private void drawToolTipLine(Context2d context2d, ToolTip toolTip) {
        context2d.setTransform(1D, 0D, 0D, 1D, 0D, 0D);
        context2d.setFillStyle(CssColor.make(0, 0, 0));
        context2d.setStrokeStyle(CssColor.make(0, 0, 0));
        String left = toolTip.getWidget().getElement().getStyle().getLeft();
        left = left.substring(0, left.length() - 2);
        int toolTipLeft = Integer.parseInt(left);
        String top = toolTip.getWidget().getElement().getStyle().getTop();
        top = top.substring(0, top.length() - 2);
        int toolTipTop = Integer.parseInt(top);
        // line from the top left of the box to the anchor point.

        double lineEndX = toolTip.getItemX();
        double lineEndY = toolTip.getItemY();
        // TODO: MAGIC NUMBERS
        double lineStartX = toolTipLeft + 3;
        double lineStartY = toolTipTop + 3;

        double dx = (lineEndX - lineStartX);
        double dy = (lineEndY - lineStartY);

        boolean xSlope = (Math.abs(dx) > Math.abs(dy));
        double slope = xSlope ? dy / dx : dx / dy;
        context2d.beginPath();
        context2d.moveTo(lineStartX, lineStartY);
        double distRemaining = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        int dashIndex = 0;

        Integer[] dashArray = WebMain.getClientStartupInfo().getGraphAdvConfig().getTooltips().getDashes();
        int dashCount = dashArray.length;
        while (distRemaining >= 0.1) {
            double dashlength = Math.min(distRemaining, dashArray[dashIndex % dashCount]);
            double step = Math.sqrt(dashlength * dashlength / (1 + slope * slope));
            if (xSlope) {
                if (dx < 0) {
                    step = -step;
                }
                lineStartX += step;
                lineStartY += slope * step;
            } else {
                if (dy < 0) {
                    step = -step;

                }
                lineStartX += slope * step;
                lineStartY += step;
            }
            if (dashIndex % 2 == 0) {

                context2d.lineTo(lineStartX, lineStartY);
            } else {
                context2d.moveTo(lineStartX, lineStartY);

            }
            distRemaining -= dashlength;
            dashIndex++;

        }
        // context2d.fill();
        context2d.stroke();

        // The anchor point of the callout.
        context2d.beginPath();
        context2d.arc(toolTip.getItemX(), toolTip.getItemY(), 2, 0, 10);
        context2d.setFillStyle(CssColor.make(0, 0, 0));
        context2d.setStrokeStyle(CssColor.make(0, 0, 0));
        context2d.stroke();
        context2d.fill();
    }

    public void clearForeground() {
        foregroundCanvas.getContext2d().clearRect(0, 0, getCanvasWidth(), getCanvasHeight());

    }
    
    public void clearMainCanvas() {
    	Context2d context = mainCanvas.getContext2d();
        context.setTransform(1D, 0D, 0D, 1D, 0D, 0D);
        context.clearRect(0D, 0D, getCanvasWidth(), getCanvasHeight());
    }

    public void drawBackground() {
        // Only repaint the background if it has changed. A bit of optimization :)
        if (!backgroundFill.equals(lastFilledBackgroundWith)) {
            backgroundCanvas.setCoordinateSpaceHeight(1);
            backgroundCanvas.setCoordinateSpaceWidth(1);
            Context2d context = backgroundCanvas.getContext2d();
            context.setFillStyle(backgroundFill);
            context.fillRect(0D, 0D, 1, 1);
            lastFilledBackgroundWith = backgroundFill;
        }
    }

    private int getCanvasHeight() {
        return mainCanvas.asWidget().getOffsetHeight();
    }

    private int getCanvasWidth() {
        return mainCanvas.asWidget().getOffsetWidth();
    }

    private void resetCoordinateSpace(double ratio) {
        // The canvas coordinate space must be the same aspect ratio as the image to avoid warping
        mainCanvas.setCoordinateSpaceHeight(getCanvasHeight());
        mainCanvas.setCoordinateSpaceWidth(getCanvasWidth());
    }

    @Override
    public void addTooltip(final Widget toolTip, final int x, final int y) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                int widgetHeight = toolTip.getElement().getOffsetHeight();
                int widgetWidth = toolTip.getElement().getOffsetWidth();
                int addX = x;
                int addY = y - widgetHeight;
                if (x + widgetWidth > getCanvasWidth()) {
                    addX = getCanvasWidth() - widgetWidth;
                }
                if (y - widgetHeight <= 0) {
                    addY = y;
                }
                if (toolTip.getParent().equals(absolutePanel)) {
                    absolutePanel.setWidgetPosition(toolTip, addX, addY);
                }
            }
        });
        absolutePanel.add(toolTip);
        //Renders and initially size offscreen
        absolutePanel.setWidgetPosition(toolTip, -10000, -10000);
    }
    
    @Override
    public void addMultiTypePreview(final Widget view, final int x, final int y) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                int widgetHeight = view.getElement().getOffsetHeight();
                int widgetWidth = view.getElement().getOffsetWidth();
                int addX = x;
                int addY = y - widgetHeight;
                addX = x - widgetWidth/2;
//                if (x + widgetWidth > getCanvasWidth()/2) {
//                    addX = getCanvasWidth() - widgetWidth;
//                }
                if (y - widgetHeight <= 0) {
                    addY = y;
                }
                absolutePanel.setWidgetPosition(view, addX, addY);

            }
        });

        absolutePanel.add(view, -10000, -10000);
    }
    

    @Override
    public void moveAllToolTips(int deltaX, int deltaY) {
        List<ToolTip> toolTips = graphSurface.getToolTipManager().getToolTips();
        for (ToolTip toolTip : toolTips) {
            try {
                toolTip.moveAnchor(deltaX, deltaY);
                String left = toolTip.getWidget().getElement().getStyle().getLeft();
                left = left.substring(0, left.length() - 2);
                int oldLeft = Integer.parseInt(left);
                String top = toolTip.getWidget().getElement().getStyle().getTop();
                top = top.substring(0, top.length() - 2);
                int oldTop = Integer.parseInt(top);
                toolTip.getWidget().getElement().getStyle().setLeft(oldLeft + deltaX, Unit.PX);
                toolTip.getWidget().getElement().getStyle().setTop(oldTop + deltaY, Unit.PX);
            } catch (NumberFormatException e) {
                LOG.severe("Could not parse tooltip location.");
            }
        }
    }

	public void broadcastNotify(String text) {
		BroadcastAlert broadcastAlert = new BroadcastAlert(text);
		broadcastAlert.getElement().getStyle().setRight(0, Unit.PX);
		broadcastAlert.getElement().getStyle().setTop(0, Unit.PX);

		absolutePanel.add(broadcastAlert);
	}
}
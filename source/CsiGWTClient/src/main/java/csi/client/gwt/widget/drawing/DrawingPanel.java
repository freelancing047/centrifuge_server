package csi.client.gwt.widget.drawing;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HandlesAllMouseEvents;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public class DrawingPanel extends Composite implements RequiresResize {
   private static final Logger LOG = Logger.getLogger(DrawingPanel.class.getName());

    private Canvas canvas;
    private List<Layer> layers;

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private DrawNowCallback drawNow;
    private boolean onYAxis = false;
    private boolean onXAxis = false;

    public DrawingPanel() {
       LOG.finest(i18n.drawingPanelStartDrawingMessage()); //$NON-NLS-1$
        layers = Lists.newArrayList();
        canvas = checkNotNull(Canvas.createIfSupported());
        canvas.setWidth("100%"); //$NON-NLS-1$
        canvas.setHeight("100%"); //$NON-NLS-1$
        MouseTrap mouseTrap = new MouseTrap();
        mouseTrap.handle(canvas);
        canvas.addClickHandler(mouseTrap);
        canvas.addDoubleClickHandler(mouseTrap);
        initWidget(canvas);
        drawNow = new DrawNowCallback();
        LOG.finest(i18n.drawingPanelFinishedCreatingMessage()); //$NON-NLS-1$
    }

    public void addLayer(Layer layer) {
        checkNotNull(layer);
        layers.add(layer);
        layer.bind(this);
    }

    public void bringToFront(Layer item) {
        layers.remove(item);
        layers.add(item);
    }

    public Layer getLayer(int index) {
        checkPositionIndex(index, layers.size());
        return layers.get(index);
    }

    public ImmutableList<Layer> getLayers() {
        return ImmutableList.copyOf(layers);
    }

    public void addLayerAt(int index, Layer layer) {
        checkNotNull(layer);
        layers.add(index, layer);
    }

    public boolean remove(Layer layer) {
        checkNotNull(layer);
        return layers.remove(layer);
    }

    public void removeAll() {
        layers.clear();
    }

    public void setCursor(final Cursor cursor) {
        checkNotNull(cursor);
        if (canvas != null) {
            canvas.getElement().getStyle().setCursor(cursor);
        }
    }

    public int getWidth() {
        return canvas.getOffsetWidth();
    }

    public int getHeight() {
        return canvas.getOffsetHeight();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public void onResize() {
        // Do something
    }

    public void render() {
        AnimationScheduler.get().requestAnimationFrame(drawNow, canvas.getElement());

        LOG.finest(i18n.drawingPanelFinishedRenderingMessage()); //$NON-NLS-1$
    }

    public void setOnYAxis(boolean value) {
        onYAxis = value;
    }

    public boolean isOnYAxis() {
        return onYAxis;
    }

    public void setOnXAxis(boolean value) {
        onXAxis = value;
    }

    public boolean isOnXAxis() {
        return onXAxis;
    }

    public Optional<Renderable> getRenderableAt(double x, double y) {
        for (Layer layer : Lists.reverse(layers)) {
            if (layer.isVisible()) {
                Optional<Renderable> r = layer.hitTest(x, y);
                if (r.isPresent()) {
                    return r;
                }
            }
        }
        return Optional.absent();
    }

    private final class DrawNowCallback implements AnimationCallback {

        private double timestamp;

        @Override
        public void execute(double timestamp) {
            if(timestamp- this.timestamp<10){
                return;
            }
            this.timestamp = timestamp;
            LOG.finest(i18n.drawingPanelStartRenderingMessage()); //$NON-NLS-1$
            final Context2d context2d = canvas.getContext2d();

            if (isAttached()) {
                if(canvas.getOffsetHeight() == 0 || canvas.getOffsetWidth() == 0){
                    return;
                }
                canvas.setCoordinateSpaceWidth(canvas.getOffsetWidth());
                canvas.setCoordinateSpaceHeight(canvas.getOffsetHeight());
            } else {
                canvas.setCoordinateSpaceWidth(100);
                canvas.setCoordinateSpaceHeight(100);
            }

            for (Layer layer : layers) {
                if (layer.isVisible()) {
                    layer.render();
                }
            }
            context2d.clearRect(0, 0, 100000, 100000);
            for (Layer layer : layers) {
                if (layer.isVisible()) {
                    context2d.drawImage(layer.getCanvas().getCanvasElement(), 0, 0);
                }
            }
        }
    }

    private class MouseTrap extends HandlesAllMouseEvents implements ClickHandler, DoubleClickHandler {

        private HasAllMouseHandlers previousTarget;
        private HasAllMouseHandlers realTarget;

        @SuppressWarnings("rawtypes")
        private void onMouseEvent(MouseEvent event) {
            checkNotNull(event);
            Optional<Renderable> renderableAt = getRenderableAt(event.getX(), event.getY());
            if (renderableAt.isPresent()) {
                if (renderableAt.get() instanceof HasAllMouseHandlers) {
                    realTarget = (HasAllMouseHandlers) renderableAt.get();
                    realTarget.fireEvent(event);
                } else {
                    realTarget = null;
                }
            }else {
                realTarget = null;
            }
            if (previousTarget != realTarget) {
                if (previousTarget != null) {
                    NativeEvent fakeMouseOutEvent = Document.get().createMouseOutEvent(0, event.getScreenX(),
                            event.getScreenY(), event.getClientX(), event.getClientY(),
                            event.getNativeEvent().getCtrlKey(), event.getNativeEvent().getAltKey(),
                            event.getNativeEvent().getShiftKey(), event.getNativeEvent().getMetaKey(),
                            event.getNativeEvent().getButton(), getCanvas().getCanvasElement());
                    DomEvent.fireNativeEvent(fakeMouseOutEvent, previousTarget);
                }
                if (realTarget != null) {
                    NativeEvent fakeMouseOverEvent = Document.get().createMouseOverEvent(0, event.getScreenX(),
                            event.getScreenY(), event.getClientX(), event.getClientY(),
                            event.getNativeEvent().getCtrlKey(), event.getNativeEvent().getAltKey(),
                            event.getNativeEvent().getShiftKey(), event.getNativeEvent().getMetaKey(),
                            event.getNativeEvent().getButton(), getCanvas().getCanvasElement());
                    DomEvent.fireNativeEvent(fakeMouseOverEvent, realTarget);
                }
            }
            previousTarget = realTarget;
        }

        @Override
        public void onMouseDown(MouseDownEvent event) {
            onMouseEvent(event);
        }

        @Override
        public void onMouseUp(MouseUpEvent event) {
            onMouseEvent(event);
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            onMouseEvent(event);
        }

        @Override
        public void onMouseOut(MouseOutEvent event) {
            //FIXME:Maybe
            if (previousTarget != null) {

                previousTarget.fireEvent(event);
                previousTarget = null;
            }
            onMouseEvent(event);
        }

        @Override
        public void onMouseOver(MouseOverEvent event) {
            onMouseEvent(event);
        }

        @Override
        public void onMouseWheel(MouseWheelEvent event) {
            onMouseEvent(event);
        }

        @Override
        public void onClick(ClickEvent event) {
            if (realTarget instanceof HasClickHandlers) {
                realTarget.fireEvent(event);
            }
        }

        @Override
        public void onDoubleClick(DoubleClickEvent event) {
            if (realTarget instanceof HasDoubleClickHandlers) {
                realTarget.fireEvent(event);
            }
        }
    }
}

package csi.client.gwt.widget.drawing;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.canvas.client.Canvas;

public class Layer {

    protected List<Renderable> things;
    private Canvas canvas;
    private DrawingPanel drawingPanel;
    private boolean isVisible = true;

    public Layer() {
        things = Lists.newArrayList();
        canvas = checkNotNull(Canvas.createIfSupported());
    }

    public void addItem(Renderable renderable) {
        checkNotNull(renderable);
        things.add(renderable);
        renderable.bind(this);
    }

    public void addLayerAt(int index, Renderable layer) {
        checkNotNull(layer);
        things.add(index, layer);
    }

    public void bind(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }

    public void bringToFront(Renderable item) {
        if(things.contains(item)) {
            things.remove(item);
            things.add(item);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }

    public int getHeight() {
        if (drawingPanel != null) {
            return drawingPanel.getHeight();
        }
        return 0;
    }

    public Renderable getLayer(int index) {
        checkPositionIndex(index, things.size());
        return things.get(index);
    }

    public ImmutableList<Renderable> getRenderables() {
        return ImmutableList.copyOf(things);
    }

    public int getWidth() {
        if (drawingPanel != null) {
            return drawingPanel.getWidth();
        }
        return 0;
    }

    public Optional<Renderable> hitTest(double x, double y) {
        // need to check transform.
        List<Renderable> reverse = Lists.reverse(things);
        for (Renderable thing : reverse) {
            if (thing.hitTest(x, y)) {
                return Optional.of(thing);
            }
        }
        return Optional.absent();
    }

    @SuppressWarnings("unused")
    private boolean isDirty() {
        for (Renderable thing : things) {
            if (thing.isDirty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void onResize() {
        // Do something
    }

    public boolean remove(Renderable item) {
        checkNotNull(item);
        return things.remove(item);
    }

    public void removeAll() {
        things.clear();
    }

    public void render() {
        canvas.setCoordinateSpaceWidth(drawingPanel.getCanvas().getCoordinateSpaceWidth());
        canvas.setCoordinateSpaceHeight(drawingPanel.getCanvas().getCoordinateSpaceHeight());
        canvas.getContext2d().clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
        for (Renderable thing : things) {
            canvas.getContext2d().save();
            thing.render(canvas.getContext2d());
            canvas.getContext2d().restore();
        }
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void clear() {
        things.clear();
    }
}

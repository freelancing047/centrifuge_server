package csi.client.gwt.widget.drawing;

import com.google.gwt.canvas.dom.client.Context2d;

public interface Renderable {

    public void render(Context2d context2d);

    public boolean hitTest(double x, double y);

    public void bind(Layer layer);

    public boolean isDirty();
}

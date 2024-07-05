package csi.client.gwt.widget.drawing;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class DraggableRectangle extends Rectangle {

    protected boolean dragging;
    protected int mouseX;
    protected int mouseY;
    private HandlerRegistration mouseMoveRegistration;
    private MouseMoveHandler mouseMoveHandler;

    public DraggableRectangle(int x, int y, int w, int h) {
        super(x, y, w, h);

        mouseMoveHandler = new MouseMoveHandler() {

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (dragging) {
                    int oldX = mouseX;
                    int oldY = mouseY;
                    mouseX = event.getX();
                    mouseY = event.getY();

                    setX((getX() + mouseX) - oldX);
                    setY((getY() + mouseY) - oldY);
                    getLayer().getDrawingPanel().render();
                }
            }
        };
        addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                mouseX = event.getX();
                mouseY = event.getY();
                dragging = true;
                mouseMoveRegistration = getLayer().getDrawingPanel().getCanvas().addMouseMoveHandler(mouseMoveHandler);
            }
        });

        addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                dragging = false;
                mouseMoveRegistration.removeHandler();
                getLayer().getDrawingPanel().render();
            }
        });
    }
}

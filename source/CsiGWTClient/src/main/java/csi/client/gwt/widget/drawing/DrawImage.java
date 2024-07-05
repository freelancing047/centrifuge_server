package csi.client.gwt.widget.drawing;

import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class DrawImage extends BaseRenderable {

    protected Canvas scratchPad = Canvas.createIfSupported();
    private double drawHeight;
    private double drawWidth;
    private double drawY;
    private double drawX;
    private double imageHeight;
    private double imageWidth;
    private double imageY;
    private double imageX;
    private DrawImage copy;
    private Layer layer;
    protected MouseMoveHandler mouseMoveHandler;
    protected boolean isDragging;
    protected int mouseX;
    protected int mouseY;
    protected HandlerRegistration mouseMoveRegistration;

    public DrawImage() {
    }

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    private int compareToDrawImage(Optional<DrawImage> optionalThat) {
        if (optionalThat.isPresent()) {
            DrawImage that = optionalThat.get();
            return ComparisonChain.start().compare(this.imageX, that.imageX).compare(this.imageY, that.imageY)
                    .compare(this.imageWidth, that.imageWidth).compare(this.imageHeight, that.imageHeight)
                    .compare(this.drawX, that.drawX).compare(this.drawY, that.drawY)
                    .compare(this.drawWidth, that.drawWidth).compare(this.drawHeight, that.drawHeight).result();
        }
        return -1;
    }

    public DrawImage copy() {
        DrawImage copy = new DrawImage();
        copy.scratchPad.setCoordinateSpaceHeight((int) imageHeight);
        copy.scratchPad.setCoordinateSpaceHeight((int) imageWidth);
        copy.scratchPad.getContext2d().drawImage(scratchPad.getCanvasElement(), 0, 0);
        copy.setDrawHeight(drawHeight);
        copy.setDrawWidth(drawWidth);
        copy.setDrawX(drawX);
        copy.setDrawY(drawY);
        copy.setImageHeight(imageHeight);
        copy.setImageWidth(imageWidth);
        copy.setImageX(imageX);
        copy.setImageY(imageY);
        return copy;
    }

    public void fromImage(ImageElement image) {
        // for now i'll just assume the image is loaded...
        int width = image.getWidth();
        int height = image.getHeight();

        setDrawHeight(height);
        setImageHeight(height);

        setDrawWidth(width);
        setImageWidth(width);

        scratchPad.setCoordinateSpaceWidth(width);
        scratchPad.setCoordinateSpaceHeight(height);

        Context2d context2d = scratchPad.getContext2d();
        context2d.drawImage(image, 0, 0);
    }

    public double getDrawHeight() {
        return drawHeight;
    }

    public double getDrawWidth() {
        return drawWidth;
    }

    public double getDrawX() {
        return drawX;
    }

    public double getDrawY() {
        return drawY;
    }

    public double getImageHeight() {
        return imageHeight;
    }

    public double getImageWidth() {
        return imageWidth;
    }

    public double getImageX() {
        return imageX;
    }

    public double getImageY() {
        return imageY;
    }

    public Layer getLayer() {
        return layer;
    }

    @Override
    public boolean hitTest(double x, double y) {
        if(isDragging){
            
        } else {
            ImageData imageData = scratchPad.getContext2d().getImageData((x + getImageX()) - getDrawX(),
                    (y + getImageY()) - getDrawY(), 1, 1);
            if (imageData.getAlphaAt(0, 0) == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDirty() {
        if (compareToDrawImage(Optional.of(copy)) == 0) {
            return false;
        }
        return true;
    }

    public void makeDraggable() {
        mouseMoveHandler = new MouseMoveHandler() {

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (isDragging) {
                    int oldX = mouseX;
                    int oldY = mouseY;
                    mouseX = event.getX();
                    mouseY = event.getY();
                    setDrawX((getDrawX() + mouseX) - oldX);
                    setDrawY((getDrawY() + mouseY) - oldY);
                    getLayer().getDrawingPanel().render();
                }
            }
        };
        MouseDownHandler mouseDownHandler = new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                dragStart(event.getX(), event.getY());
            }
        };
        addMouseDownHandler(mouseDownHandler);
        MouseUpHandler mouseUpHandler = new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                dragEnd(event.getX(), event.getY());
            }
        };
        addMouseUpHandler(mouseUpHandler);
    }

    protected void dragEnd(int x, int y) {
        isDragging = false;
        mouseMoveRegistration.removeHandler();
        getLayer().getDrawingPanel().render();
    }

    public void dragStart(int x, int y) {
        mouseX = x;
        mouseY = y;
        isDragging = true;
        if(layer!= null) {
            layer.bringToFront(this);
            mouseMoveRegistration = getLayer().getDrawingPanel().getCanvas().addMouseMoveHandler(mouseMoveHandler);
        }
    }

    @Override
    public void render(Context2d context2d) {
        context2d.drawImage(scratchPad.getCanvasElement(), getImageX(), getImageY(), getImageWidth(), getImageHeight(),
                getDrawX(), getDrawY(), getDrawWidth(), getDrawHeight());
        copy = this.copy();
    }

    public void setDrawHeight(double drawHeight) {
        this.drawHeight = drawHeight;
    }

    public void setDrawWidth(double drawWidth) {
        this.drawWidth = drawWidth;
    }

    public void setDrawX(double drawX) {
        this.drawX = drawX;
    }

    public void setDrawY(double drawY) {
        this.drawY = drawY;
    }

    public void setImageHeight(double imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setImageWidth(double imageWidth) {
        this.imageWidth = imageWidth;
    }

    public void setImageX(double imageX) {
        this.imageX = imageX;
    }

    public void setImageY(double imageY) {
        this.imageY = imageY;
    }
}

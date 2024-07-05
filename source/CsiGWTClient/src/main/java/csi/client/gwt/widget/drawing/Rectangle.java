package csi.client.gwt.widget.drawing;

import com.google.common.collect.ComparisonChain;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;

public class Rectangle extends BaseRenderable {

    FillStrokeStyle strokeStyle, fillStyle;
    private double x, y, w, h;
    public Layer layer;
    private Rectangle copy;

    public Rectangle() {
    }

    public Layer getLayer() {
        return layer;
    }

    public Rectangle(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setStrokeStyle(FillStrokeStyle strokeStyle) {
        this.strokeStyle = strokeStyle;
    }

    public void setFillStyle(FillStrokeStyle fillStyle) {
        this.fillStyle = fillStyle;
    }

    public FillStrokeStyle getStrokeStyle() {
        return this.strokeStyle;
    }

    public FillStrokeStyle getFillStyle() {
        return this.fillStyle;
    }

    @Override
    public void render(Context2d context2d) {
        if (fillStyle != null) {
            context2d.setFillStyle(fillStyle);
            context2d.fillRect(getX(), getY(), getW(), getH());
        }
        if (strokeStyle != null) {
            context2d.setStrokeStyle(strokeStyle);
            context2d.strokeRect(getX(), getY(), getW(), getH());
        }
        copy = this.copy();
    }

    private int compareToRectangle(Rectangle that) {
        // needs some more checks here

        if (that == null) {
            return -1;
        }
        if ((this.fillStyle != null) && (that.fillStyle == null)) {
            return -1;
        }
        if ((this.strokeStyle != null) && (that.strokeStyle == null)) {
            return -1;
        }
        if ((this.fillStyle == null) && (that.fillStyle != null)) {
            return 1;
        }
        if ((this.strokeStyle == null) && (that.strokeStyle != null)) {
            return 1;
        }
        return ComparisonChain.start().compare(this.x, that.x).compare(this.y, that.y).compare(this.w, that.w)
                .compare(this.h, that.h).result();
        // return ComparisonChain.start().compare(this.x, that.x).compare(this.y, that.y).compare(this.w, that.w)
        // .compare(this.h, that.h).compare(this.fillStyle.toString(), that.fillStyle.toString())
        // .compare(this.strokeStyle.toString(), that.strokeStyle.toString()).result();
    }

    public Rectangle copy() {
        Rectangle copy = new Rectangle(x, y, w, h);
        setFillStyle(fillStyle);
        setStrokeStyle(strokeStyle);
        return copy;
    }

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    @Override
    public boolean hitTest(double x, double y) {
        // as implemented only affects filled region not stroke.
        if ((x < this.x) || (y < this.y) || ((this.x + this.w) <= x) || ((this.y + this.h) <= y)) {
            // to the left, above, to the right, or bellow
            // the later conditions must check equality.
            return false;
        }
        return true;
    }

    @Override
    public boolean isDirty() {
        if (copy == null) {
            return true;
        }
        if (compareToRectangle(copy) == 0) {
            return false;
        }
        return true;
    }
}

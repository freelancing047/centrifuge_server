package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.VideoElement;

import java.util.Stack;

public class CSIContext2d {
    private final com.google.gwt.canvas.dom.client.Context2d context2d;
    private CanvasTransform transform;
    private Stack<CanvasTransform> transformStack = new Stack<>();

    public void setTransform(CanvasTransform ct) {
        setTransform(ct.m0,ct.m1,ct.m2,ct.m3,ct.m4,ct.m5);
    }

    public static class CanvasTransform {
        double m0 = 1;
        double m1 = 0;
        double m2 = 0;
        double m3 = 1;
        double m4 = 0;
        double m5 = 0;

        public void rotate(double angle) {
            double c = Math.cos(angle);
            double s = Math.sin(angle);
            double m11 = m0 * c + m2 * s;
            double m12 = m1 * c + m3 * s;
            double m21 = m0 * -s + m2 * c;
            double m22 = m1 * -s + m3 * c;
            m0 = m11;
            m1 = m12;
            m2 = m21;
            m3 = m22;
        }

        public void scale(double sx, double sy) {
            m0 *= sx;
            m1 *= sx;
            m2 *= sy;
            m3 *= sy;

        }

        public void translate(double x, double y) {
            m4 += m0 * x + m2 * y;
            m5 += m1 * x + m3 * y;
        }

        public double getX(double px, double py) {
            double x = px;
            px = x * m0 + py * m2 + m4;
            return px;
        }

        public double getY(double px, double py) {
            double y = py;
            py = px * m1 + y * m3 + m5;
            return py;
        }

        public void invert() {
            double d = 1 / (this.m0 * this.m3 - this.m1 * this.m2);
            double m0 = this.m3 * d;
            double m1 = -this.m1 * d;
            double m2 = -this.m2 * d;
            double m3 = this.m0 * d;
            double m4 = d * (this.m2 * this.m5 - this.m3 * this.m4);
            double m5 = d * (this.m1 * this.m4 - this.m0 * this.m5);
            this.m0 = m0;
            this.m1 = m1;
            this.m2 = m2;
            this.m3 = m3;
            this.m4 = m4;
            this.m5 = m5;
        }

        public void multiply(CanvasTransform ct) {
            double m11 = m0 * ct.m0 + m2 * ct.m1;
            double m12 = m1 * ct.m0 + m3 * ct.m1;

            double m21 = m0 * ct.m2 + m2 * ct.m3;
            double m22 = m1 * ct.m2 + m3 * ct.m3;

            double dx = m0 * ct.m4 + m2 * ct.m5 + m4;
            double dy = m1 * ct.m4 + m3 * ct.m5 + m5;

            m0 = m11;
            m1 = m12;
            m2 = m21;
            m3 = m22;
            m4 = dx;
            m5 = dy;
        }

        public void apply(Context2d ctx) {
            ctx.transform(m0,m1,m2,m3,m4,m5);
        }

        public CanvasTransform copy(){
            CanvasTransform ct = new CanvasTransform();
            ct.m0 = m0;
            ct.m1 = m1;
            ct.m2 = m2;
            ct.m3 = m3;
            ct.m4 = m4;
            ct.m5 = m5;
            return ct;
        }

        public void moveOrigin(Context2d ctx, int x, int y) {
            ctx.translate(getX(x, y),getY(x, y));
        }
    }


    public CSIContext2d(com.google.gwt.canvas.dom.client.Context2d context2d) {
        transform = new CanvasTransform();
        this.context2d = context2d;
    }

    public void arc(double x, double y, double radius, double startAngle, double endAngle) {
        context2d.arc(x, y, radius, startAngle, endAngle);
    }

    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean anticlockwise) {
        context2d.arc(x, y, radius, startAngle, endAngle, anticlockwise);
    }

    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        context2d.arcTo(x1, y1, x2, y2, radius);
    }

    public void beginPath() {
        context2d.beginPath();
    }

    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        context2d.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    public void clearRect(double x, double y, double w, double h) {
        context2d.clearRect(x, y, w, h);
    }

    public void clip() {
        context2d.clip();
    }

    public void closePath() {
        context2d.closePath();
    }

    public ImageData createImageData(ImageData imagedata) {
        return context2d.createImageData(imagedata);
    }

    public ImageData createImageData(int w, int h) {
        return context2d.createImageData(w, h);
    }

    public CanvasGradient createLinearGradient(double x0, double y0, double x1, double y1) {
        return context2d.createLinearGradient(x0, y0, x1, y1);
    }

    public CanvasPattern createPattern(CanvasElement image, com.google.gwt.canvas.dom.client.Context2d.Repetition repetition) {
        return context2d.createPattern(image, repetition);
    }

    public CanvasPattern createPattern(CanvasElement image, String repetition) {
        return context2d.createPattern(image, repetition);
    }

    public CanvasPattern createPattern(ImageElement image, com.google.gwt.canvas.dom.client.Context2d.Repetition repetition) {
        return context2d.createPattern(image, repetition);
    }

    public CanvasPattern createPattern(ImageElement image, String repetition) {
        return context2d.createPattern(image, repetition);
    }

    public CanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        return context2d.createRadialGradient(x0, y0, r0, x1, y1, r1);
    }

    public void drawImage(CanvasElement image, double dx, double dy) {
        context2d.drawImage(image, dx, dy);
    }

    public void drawImage(CanvasElement image, double dx, double dy, double dw, double dh) {
        context2d.drawImage(image, dx, dy, dw, dh);
    }

    public void drawImage(CanvasElement image, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh) {
        context2d.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public void drawImage(ImageElement image, double dx, double dy) {
        context2d.drawImage(image, dx, dy);
    }

    public void drawImage(ImageElement image, double dx, double dy, double dw, double dh) {
        context2d.drawImage(image, dx, dy, dw, dh);
    }

    public void drawImage(ImageElement image, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh) {
        context2d.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public void drawImage(VideoElement image, double dx, double dy) {
        context2d.drawImage(image, dx, dy);
    }

    public void drawImage(VideoElement image, double dx, double dy, double dw, double dh) {
        context2d.drawImage(image, dx, dy, dw, dh);
    }

    public void drawImage(VideoElement image, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh) {
        context2d.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public void fill() {
        context2d.fill();
    }

    public void fillRect(double x, double y, double w, double h) {
        context2d.fillRect(x, y, w, h);
    }

    public void fillText(String text, double x, double y) {
        context2d.fillText(text, x, y);
    }

    public void fillText(String text, double x, double y, double maxWidth) {
        context2d.fillText(text, x, y, maxWidth);
    }

    public CanvasElement getCanvas() {
        return context2d.getCanvas();
    }

    public FillStrokeStyle getFillStyle() {
        return context2d.getFillStyle();
    }

    public String getFont() {
        return context2d.getFont();
    }

    public double getGlobalAlpha() {
        return context2d.getGlobalAlpha();
    }

    public String getGlobalCompositeOperation() {
        return context2d.getGlobalCompositeOperation();
    }

    public ImageData getImageData(double sx, double sy, double sw, double sh) {
        return context2d.getImageData(sx, sy, sw, sh);
    }

    public String getLineCap() {
        return context2d.getLineCap();
    }

    public String getLineJoin() {
        return context2d.getLineJoin();
    }

    public double getLineWidth() {
        return context2d.getLineWidth();
    }

    public double getMiterLimit() {
        return context2d.getMiterLimit();
    }

    public double getShadowBlur() {
        return context2d.getShadowBlur();
    }

    public String getShadowColor() {
        return context2d.getShadowColor();
    }

    public double getShadowOffsetX() {
        return context2d.getShadowOffsetX();
    }

    public double getShadowOffsetY() {
        return context2d.getShadowOffsetY();
    }

    public FillStrokeStyle getStrokeStyle() {
        return context2d.getStrokeStyle();
    }

    public String getTextAlign() {
        return context2d.getTextAlign();
    }

    public String getTextBaseline() {
        return context2d.getTextBaseline();
    }

    public boolean isPointInPath(double x, double y) {
        return context2d.isPointInPath(x, y);
    }

    public void lineTo(double x, double y) {
        context2d.lineTo(x, y);
    }

    public TextMetrics measureText(String text) {
        return context2d.measureText(text);
    }

    public void moveTo(double x, double y) {
        context2d.moveTo(x, y);
    }

    public void putImageData(ImageData imagedata, double x, double y) {
        context2d.putImageData(imagedata, x, y);
    }

    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        context2d.quadraticCurveTo(cpx, cpy, x, y);
    }

    public void rect(double x, double y, double w, double h) {
        context2d.rect(x, y, w, h);
    }

    public void restore() {
        if (!transformStack.empty()) {
            transform = transformStack.pop();
        }
        context2d.restore();
    }

    public void rotate(double angle) {
        transform.rotate(angle);
        context2d.rotate(angle);
    }

    public void save() {
        transformStack.push(transform);
        context2d.save();
    }

    public void scale(double x, double y) {
        transform.scale(x, y);
        context2d.scale(x, y);
    }

    public void setFillStyle(FillStrokeStyle fillStyle) {
        context2d.setFillStyle(fillStyle);
    }

    public void setFillStyle(String fillStyleColor) {
        context2d.setFillStyle(fillStyleColor);
    }

    public void setFont(String f) {
        context2d.setFont(f);
    }

    public void setGlobalAlpha(double alpha) {
        context2d.setGlobalAlpha(alpha);
    }

    public void setGlobalCompositeOperation(com.google.gwt.canvas.dom.client.Context2d.Composite composite) {
        context2d.setGlobalCompositeOperation(composite);
    }

    public void setGlobalCompositeOperation(String globalCompositeOperation) {
        context2d.setGlobalCompositeOperation(globalCompositeOperation);
    }

    public void setLineCap(com.google.gwt.canvas.dom.client.Context2d.LineCap lineCap) {
        context2d.setLineCap(lineCap);
    }

    public void setLineCap(String lineCap) {
        context2d.setLineCap(lineCap);
    }

    public void setLineJoin(com.google.gwt.canvas.dom.client.Context2d.LineJoin lineJoin) {
        context2d.setLineJoin(lineJoin);
    }

    public void setLineJoin(String lineJoin) {
        context2d.setLineJoin(lineJoin);
    }

    public void setLineWidth(double lineWidth) {
        context2d.setLineWidth(lineWidth);
    }

    public void setMiterLimit(double miterLimit) {
        context2d.setMiterLimit(miterLimit);
    }

    public void setShadowBlur(double shadowBlur) {
        context2d.setShadowBlur(shadowBlur);
    }

    public void setShadowColor(String shadowColor) {
        context2d.setShadowColor(shadowColor);
    }

    public void setShadowOffsetX(double shadowOffsetX) {
        context2d.setShadowOffsetX(shadowOffsetX);
    }

    public void setShadowOffsetY(double shadowOffsetY) {
        context2d.setShadowOffsetY(shadowOffsetY);
    }

    public void setStrokeStyle(FillStrokeStyle strokeStyle) {
        context2d.setStrokeStyle(strokeStyle);
    }

    public void setStrokeStyle(String strokeStyleColor) {
        context2d.setStrokeStyle(strokeStyleColor);
    }

    public void setTextAlign(String align) {
        context2d.setTextAlign(align);
    }

    public void setTextAlign(com.google.gwt.canvas.dom.client.Context2d.TextAlign align) {
        context2d.setTextAlign(align);
    }

    public void setTextBaseline(String baseline) {
        context2d.setTextBaseline(baseline);
    }

    public void setTextBaseline(com.google.gwt.canvas.dom.client.Context2d.TextBaseline baseline) {
        context2d.setTextBaseline(baseline);
    }

    public void setTransform(double m11, double m12, double m21, double m22, double dx, double dy) {
        CanvasTransform ct = new CanvasTransform();
        ct.m0 = m11;
        ct.m1 = m12;
        ct.m2 = m21;
        ct.m3 = m22;
        ct.m4 = dx;
        ct.m5 = dy;
        this.transform = ct;
        context2d.setTransform(m11, m12, m21, m22, dx, dy);
    }

    public void stroke() {
        context2d.stroke();
    }

    public void strokeRect(double x, double y, double w, double h) {
        context2d.strokeRect(x, y, w, h);
    }

    public void strokeText(String text, double x, double y) {
        context2d.strokeText(text, x, y);
    }

    public void strokeText(String text, double x, double y, double maxWidth) {
        context2d.strokeText(text, x, y, maxWidth);
    }

    public void transform(double m11, double m12, double m21, double m22, double dx, double dy) {
        CanvasTransform ct = new CanvasTransform();
        ct.m0 = m11;
        ct.m1 = m12;
        ct.m2 = m21;
        ct.m3 = m22;
        ct.m4 = dx;
        ct.m5 = dy;
        this.transform = ct;
        transform.multiply(ct);
        context2d.transform(m11, m12, m21, m22, dx, dy);
    }

    public void translate(double x, double y) {
        transform.translate(x, y);
        context2d.translate(x, y);
    }


    public <T extends JavaScriptObject> T cast() {
        return context2d.cast();
    }

    public String toSource() {
        return context2d.toSource();
    }

    public CanvasTransform getTransform() {
        return transform;
    }
}

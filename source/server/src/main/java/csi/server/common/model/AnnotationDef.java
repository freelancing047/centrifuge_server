package csi.server.common.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AnnotationDef extends ModelObject {

    // generic annotation shape values
    protected String annotationType;
    protected int x;
    protected int y;
    protected int height;
    protected int width;
    protected int rotation;
    protected int color;
    protected double alpha;
    protected boolean border;

    // text annotation values
    @Column(columnDefinition = "TEXT")
    protected String text;

    @Column(columnDefinition = "TEXT")
    protected String htmlText;

    // line annotation values
    protected int lineThickness = 5;

    protected boolean immutable;

    public AnnotationDef() {
    	super();
    }

    //
    // getters
    //
    public String getAnnotationType() {
        return annotationType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getRotation() {
        return rotation;
    }

    public int getColor() {
        return color;
    }

    public double getAlpha() {
        return alpha;
    }

    public String getText() {
        return text;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public int getLineThickness() {
        return lineThickness;
    }

    public boolean getBorder() {
        return border;
    }

    public boolean isImmutable() {
        return immutable;
    }

    //
    // setters
    //
    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    public void setLineThickness(int lineThickness) {
        this.lineThickness = lineThickness;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }
}

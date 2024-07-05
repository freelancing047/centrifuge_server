package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.dom.client.ImageElement;

import csi.client.gwt.util.HasXY;
import csi.client.gwt.widget.drawing.DrawImage;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class DrawPatternNode extends DrawImage implements HasXY {
    private PatternNode node;
    private FillStrokeStyle stroke;
    private boolean over;
    private boolean edit;
    private CssColor green = CssColor.make("green");//NON-NLS
    private CssColor blue = CssColor.make("blue");//NON-NLS
    private boolean isLoaded;

    public DrawPatternNode(PatternNode node) {
        this.node = node;
    }

    public PatternNode getNode() {
        return node;
    }

    public void setNode(PatternNode node) {
        this.node = node;
    }

    public DrawImage copyAsDrawImage() {
        return super.copy();
    }

    public double getX() {
        if (getLayer() == null) {
            return 0;
        }
        return (getDrawX() + (getDrawWidth() / 2.0D)) / (double) getLayer().getWidth();
    }

    public double getY() {
        if (getLayer() == null) {
            return 0;
        }
        return (getDrawY() + (getDrawHeight() / 2.0D)) / (double) getLayer().getHeight();
    }

    @Override
    public void fromImage(ImageElement image) {
        super.fromImage(image);
        isLoaded = true;
    }

    @Override
    public void render(Context2d context2d) {
        if(!isLoaded){
            return;
        }
        super.render(context2d);
        stroke = null;
        if(edit) {
            stroke = blue;
        }
        else if(over) {
            stroke = green;
        }
        if(stroke!=null) {
            context2d.setStrokeStyle(stroke);
            context2d.setLineWidth(3);
            context2d.strokeRect(getDrawX(), getDrawY(), getDrawHeight(), getDrawWidth());
        }
    }
    @Override
    public void setDrawX(double drawX) {
        super.setDrawX(drawX);
        if (getLayer() != null) {
            node.setDrawX((drawX + (getDrawWidth() / 2D)) / getLayer().getCanvas().getCoordinateSpaceWidth());
        }
    }

    @Override
    public double getDrawX() {
        Layer layer = getLayer();
        if (layer == null) {
            return 0;
        }
        if (null == layer.getCanvas()) {
            return 0;
        }
        double drawX = (node.getDrawX() * getLayer().getCanvas().getCoordinateSpaceWidth())
                - (getDrawWidth() / 2D);
        super.setDrawX(drawX);
        return drawX;
    }

    @Override
    public double getDrawY() {
        Layer layer = getLayer();
        if (layer == null) {
            return 0;
        }
        if (null == layer.getCanvas()) {
            return 0;
        }
            double drawY = (node.getDrawY() * getLayer().getCanvas().getCoordinateSpaceHeight())
                    - (getDrawHeight() / 2D);
            super.setDrawY(drawY);
            return drawY;
    }

    @Override
    public void setDrawY(double drawY) {
        super.setDrawY(drawY);
        if (getLayer() != null) {
            node.setDrawY((drawY + (getDrawHeight() / 2D)) / getLayer().getCanvas().getCoordinateSpaceHeight());
        }
    }
    private void redraw() {
        Layer layer = getLayer();
        if (layer != null) {
            DrawingPanel drawingPanel = layer.getDrawingPanel();
            if (drawingPanel != null) {
                drawingPanel.render();
            }
        }
    }

    public void setOver(boolean over) {
        this.over = over;
        redraw();
    }

    public boolean isOver() {
        return over;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
        redraw();
    }

    public boolean isEdit() {
        return edit;
    }

    @Override
    public boolean hitTest(double x, double y) {
        if (getLayer() == null) {
            return false;
        }
        if (getLayer().getCanvas() == null) {
            return false;
        }
        return super.hitTest(x, y);
    }
}

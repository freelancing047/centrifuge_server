package csi.client.gwt.viz.graph.settings;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.widget.drawing.DrawImage;

//FIXME: magic numbers need to move.
public class DrawNode extends DrawImage {

    private NodeProxy nodeProxy;
    private boolean onDark;
    private FillStrokeStyle stroke;
    

    public DrawNode(NodeProxy nodeProxy) {
        this.nodeProxy = nodeProxy;
    }

    @Override
    public void render(Context2d context2d) {
        context2d.save();
        super.render(context2d);
        if (onDark) {
            context2d.setFillStyle("rgb(255,255,255)");//NON-NLS
        } else {
            context2d.setFillStyle("rgb(0,0,0)");//NON-NLS
        }
        context2d.setFont("normal 400 13px Arial");//NON-NLS
        context2d.setTextAlign(TextAlign.CENTER);
        String label = nodeProxy.getName();
        int textLength = 150;

        while (context2d.measureText(label).getWidth() > textLength) {
            label = label.substring(0, label.length() - 1);
        }
        int nodeSize = 40;

        context2d.fillText(label, getDrawX() + (nodeSize / 2), getDrawY() + nodeSize + 13, textLength);
        context2d.restore();
        if(stroke!=null) {
            context2d.setStrokeStyle(stroke);
            context2d.setLineWidth(3);
            context2d.strokeRect(getDrawX(), getDrawY(), nodeSize, nodeSize);
        }

    }

    @Override
    public DrawNode copy() {
        DrawNode copyNode = new DrawNode(nodeProxy);
        copyNode.scratchPad.setCoordinateSpaceHeight((int) getImageHeight());
        copyNode.scratchPad.setCoordinateSpaceHeight((int) getImageWidth());
        copyNode.scratchPad.getContext2d().drawImage(scratchPad.getCanvasElement(), 0, 0);
        copyNode.setDrawHeight(getDrawHeight());
        copyNode.setDrawWidth(getDrawWidth());
        copyNode.setImageHeight(getImageHeight());
        copyNode.setImageWidth(getImageWidth());
        copyNode.setImageX(getImageX());
        copyNode.setImageY(getImageY());
        copyNode.onDark = onDark;
        return copyNode;
    }

    @Override
    public void setDrawX(double drawX) {
        super.setDrawX(drawX);
        if (getLayer() != null) {
            nodeProxy.setX((drawX + (getDrawWidth() / 2D)) / getLayer().getCanvas().getCoordinateSpaceWidth());
        }
    }

    @Override
    public double getDrawX() {
        double drawX = (nodeProxy.getX() * getLayer().getCanvas().getCoordinateSpaceWidth())
                - (getDrawWidth() / 2D);
        super.setDrawX(drawX);
        return drawX;
    }

    @Override
    public double getDrawY() {
        double drawY = (nodeProxy.getY() * getLayer().getCanvas().getCoordinateSpaceHeight())
                - (getDrawHeight() / 2D);
        super.setDrawY(drawY);
        return drawY;
    }

    @Override
    public void setDrawY(double drawY) {
        super.setDrawY(drawY);
        if (getLayer() != null) {
            nodeProxy.setY((drawY + (getDrawHeight() / 2D)) / getLayer().getCanvas().getCoordinateSpaceHeight());
        }
    }

    public DrawImage copyAsDrawImage() {
        return super.copy();
    }

    public NodeProxy getNodeProxy() {
        return nodeProxy;
    }

    @Override
    public boolean isDirty() {
        // are nodes always dirty?
        return true;
    }

    public boolean isOnDark() {
        return onDark;
    }

    public void setOnDark(boolean onDark) {
        this.onDark = onDark;
    }

    public void setStroke(CssColor color) {
        stroke = color;

    }
}

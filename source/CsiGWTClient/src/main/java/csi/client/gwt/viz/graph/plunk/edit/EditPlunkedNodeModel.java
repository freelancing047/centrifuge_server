package csi.client.gwt.viz.graph.plunk.edit;

import csi.server.common.graphics.shapes.ShapeType;

/**
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedNodeModel {

    private String label;
    private String type;
    private double size;
    private int transparency;
    private Integer color;
    private ShapeType shape;
    private String icon;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public int getTransparency() {
        return transparency;
    }

    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public ShapeType getShape() {
        return shape;
    }

    public void setShape(ShapeType shape) {
        this.shape = shape;
    }

    public String getIcon() { return icon; }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}

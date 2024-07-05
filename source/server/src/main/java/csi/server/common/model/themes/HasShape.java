package csi.server.common.model.themes;

import csi.server.common.graphics.shapes.ShapeType;

public interface HasShape {
    public ShapeType getShape();
    public void setShape(ShapeType shape);

}

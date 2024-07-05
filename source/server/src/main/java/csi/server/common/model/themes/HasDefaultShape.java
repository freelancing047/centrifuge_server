package csi.server.common.model.themes;

import csi.server.common.graphics.shapes.ShapeType;

public interface HasDefaultShape {
    public ShapeType getDefaultShape();
    public void setDefaultShape(ShapeType shape);
}

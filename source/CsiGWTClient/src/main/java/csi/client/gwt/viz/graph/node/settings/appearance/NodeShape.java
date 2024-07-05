package csi.client.gwt.viz.graph.node.settings.appearance;

import com.google.gwt.resources.client.ImageResource;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.server.common.graphics.shapes.ShapeType;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class NodeShape {

    private Color color;

    public NodeShape(NodeProxy nodeProxy) {
        setType(nodeProxy.getShape());
        setColor(ClientColorHelper.get().make(nodeProxy.getColor()));
        setEnabled(nodeProxy.isShapeOverride());
        setColorEnabled(nodeProxy.isColorOverride());
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public static ImageResource imageFromShapeType(ShapeType shapeType) {
        switch (shapeType) {
            case CIRCLE:
                return NodeShapeResource.IMPL.circle();
            case DIAMOND:
                return NodeShapeResource.IMPL.diamond();
            case HEXAGON:
                return NodeShapeResource.IMPL.hexagon();
            case HOUSE:
                return NodeShapeResource.IMPL.house();
            case NONE:
                return NodeShapeResource.IMPL.none();
            case OCTAGON:
                return NodeShapeResource.IMPL.octagon();
            case PENTAGON:
                return NodeShapeResource.IMPL.pentagon();
            case SQUARE:
                return NodeShapeResource.IMPL.square();
            case STAR:
                return NodeShapeResource.IMPL.star();
            case TRIANGLE:
                return NodeShapeResource.IMPL.triangle();
            default:
                return null;
        }
    }

    private boolean enabled;
    private ShapeType type;
    private boolean colorEnabled;

    public ShapeType getType() {
            return type;
    }

    public void setType(ShapeType type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isColorEnabled() {
        return colorEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setColorEnabled(boolean enabled) {
        this.colorEnabled = enabled;
    }
}

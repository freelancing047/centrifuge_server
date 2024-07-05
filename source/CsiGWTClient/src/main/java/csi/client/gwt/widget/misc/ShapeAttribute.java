package csi.client.gwt.widget.misc;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.graphics.shapes.ShapeType;
import csi.shared.core.util.HasLabel;

import java.io.Serializable;
import java.util.EnumSet;

public enum ShapeAttribute {
    RECTANGLE, CIRCLE,
    TRIANGLE, TRIANGLE_UP, TRIANGLE_DOWN, TRIANGLE_RIGHT, TRIANGLE_LEFT, CROSS, ROUND_RECTANGLE,
    SQUARE,
    DIAMOND,
    STAR,
    HOUSE,
    PENTAGON,
    OCTAGON,
    HEXAGON,
    NONE
    ;

    public static String getInternationalizedShape(ShapeType shape) {
        if(shape == ShapeType.NONE){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_none();
        } else if(EnumSet.of(ShapeType.RECTANGLE, ShapeType.CIRCLE).contains(shape)){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_circle();
        } else if(EnumSet.of(ShapeType.TRIANGLE, ShapeType.TRIANGLE_UP, ShapeType.TRIANGLE_DOWN,
                ShapeType.TRIANGLE_RIGHT, ShapeType.TRIANGLE_LEFT, ShapeType.CROSS,
                ShapeType.ROUND_RECTANGLE).contains(shape)){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_triangle();
        } else if(shape == ShapeType.SQUARE){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_square();
        } else if(shape == ShapeType.DIAMOND){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_diamond();
        } else if(shape == ShapeType.STAR){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_star();
        } else if(shape == ShapeType.HOUSE) {
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_pentagonhouse();
        }else if(shape == ShapeType.PENTAGON){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_pentagon();
        } else if(shape == ShapeType.OCTAGON){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_octagon();
        }else if(shape == ShapeType.HEXAGON){
            return CentrifugeConstantsLocator.get().themeEditor_graph_shape_hexagon();
        };

        return CentrifugeConstantsLocator.get().themeEditor_graph_shape_none();
    }
}

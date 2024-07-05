/**
 * 
 */
package csi.server.common.graphics.shapes;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public enum ShapeType implements IsSerializable {

    NONE{

        @Override
        public String toString() {
            return "None";
        }
    }, RECTANGLE, CIRCLE {

        @Override
        public String toString() {
            return "Circle";
        }
    },
    DIAMOND {

        @Override
        public String toString() {
            return "Diamond";
        }
    },
    HEXAGON {

        @Override
        public String toString() {
            return "Hexagon";
        }
    },
    HOUSE {

        @Override
        public String toString() {
            return "Pentagon/House";
        }
    },
    OCTAGON {

        @Override
        public String toString() {
            return "Octagon";
        }
    },
    PENTAGON {

        @Override
        public String toString() {
            return "Pentagon";
        }
    },
    STAR {

        @Override
        public String toString() {
            return "Star";
        }
    },
    SQUARE {

        @Override
        public String toString() {
            return "Square";
        }
    },
    TRIANGLE_UP {

        @Override
        public String toString() {
            return "Triangle";
        }
    }, TRIANGLE_DOWN, TRIANGLE_RIGHT, TRIANGLE_LEFT, CROSS, ROUND_RECTANGLE, TRIANGLE {

        @Override
        public String toString() {
            return "Triangle";
        }
    },
    LINE;

    public static ShapeType[] nodeShapeWheel = { CIRCLE, TRIANGLE, SQUARE, DIAMOND, STAR, HOUSE, PENTAGON, OCTAGON,
            HEXAGON, NONE};
    private static int nextIndex = 0;

    public static ShapeType getShape(String shape) {
        if(shape == null)
            return null;
        if(shape.equals("None")){
            return NONE;
        }
        if (shape.equals("Circle")) {
            return CIRCLE;
        }
        if (shape.equals("Triangle")) {
            return TRIANGLE_UP;
        }
        if (shape.equals("Square")) {
            return SQUARE;
        }
        if (shape.equals("Diamond")) {
            return DIAMOND;
        }
        if (shape.equals("Star")) {
            return STAR;
        }
        if (shape.equals("Pentagon/House")) {
            return HOUSE;
        }
        if (shape.equals("Pentagon")) {
            return PENTAGON;
        }
        if (shape.equals("Octagon")) {
            return OCTAGON;
        }
        if (shape.equals("Hexagon")) {
            return HEXAGON;
        }
        return NONE;
    }

    public static ShapeType getNextNodeShape() {
        if (nextIndex >= nodeShapeWheel.length) {
            nextIndex = 0;
        }
        
        //we prevent NONE from spinning
        nextIndex++;
        ShapeType shape = nodeShapeWheel[nextIndex%nodeShapeWheel.length];
        
        if(shape == ShapeType.NONE){
            shape = getNextNodeShape();
        }
        
        return shape;
    }
}
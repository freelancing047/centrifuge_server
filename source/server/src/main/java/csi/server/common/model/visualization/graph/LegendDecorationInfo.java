package csi.server.common.model.visualization.graph;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.graphics.shapes.ShapeType;

public class LegendDecorationInfo  implements IsSerializable, Serializable{
    
    private String key;

    private String shape;

    private Integer color;
    
    public LegendDecorationInfo(String key, ShapeType shape, int color) {
        this.setShape(shape.name());
        this.key = key;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
    
//    @Override
//    public int hashCode() {
//        if(getKey() == null) {
//            return 0;
//        }
//        
//        return getKey().hashCode();
//    }
//
//    
//    @Override
//    public boolean equals(Object object) {
//        
//        if(object == null || !(object instanceof LegendDecorationInfo)) {
//            return false;
//        }
//        
//        if(key == null) {
//            return (((LegendDecorationInfo) object).getKey() == null);
//        }
//        
//        return hashCode() == object.hashCode();
//        
//    }
    
    @Override
    public String toString() {
        return "LEGENDDECORATIONINFO" + super.toString();
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
    
}

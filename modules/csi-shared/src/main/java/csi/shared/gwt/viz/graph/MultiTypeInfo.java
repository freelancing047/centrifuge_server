package csi.shared.gwt.viz.graph;

import java.io.Serializable;
import java.util.Set;

public class MultiTypeInfo implements Serializable{
    
    private Set<String> types;
    private double x;
    private double y;
    private boolean isNode = true;

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isNode() {
        return isNode;
    }

    public void setNode(boolean isNode) {
        this.isNode = isNode;
    }

}

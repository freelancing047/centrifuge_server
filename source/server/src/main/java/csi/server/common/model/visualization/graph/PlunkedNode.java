package csi.server.common.model.visualization.graph;

import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.DeepCopiable;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PlunkedNode extends ModelObject implements DeepCopiable<PlunkedNode>{

    private String nodeKey;
    private String nodeName;
    private String nodeType;

    private int transparency = 100;
    private double size = 1.0d;
    private Integer color;
    private String shape;
    private String icon;

    private boolean hasBeenEdited = false;

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public int getTransparency() {
        return transparency;
    }

    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isHasBeenEdited() {
        return hasBeenEdited;
    }

    public void setHasBeenEdited(boolean sizeSetByEditing) {
        this.hasBeenEdited = sizeSetByEditing;
    }

    @Override
    public PlunkedNode copy(Map<String, Object> copies) {
        PlunkedNode clone = new PlunkedNode();
        super.cloneComponents(clone);

        clone.setNodeKey(getNodeKey());
        clone.setNodeName(getNodeName());
        clone.setNodeType(getNodeType());
        clone.setSize(getSize());
        clone.setTransparency(getTransparency());
        clone.setColor(getColor());
        clone.setShape(getShape());
        clone.setIcon(getIcon());
        return clone;
    }
    
    @SuppressWarnings("unchecked")
	public <T extends ModelObject> PlunkedNode trueCopy(Map<String, T> copies) {
        PlunkedNode clone = new PlunkedNode();
        super.cloneComponents(clone);

        clone.setNodeKey(getNodeKey());
        clone.setNodeName(getNodeName());
        clone.setNodeType(getNodeType());
        clone.setSize(getSize());
        clone.setTransparency(getTransparency());
        clone.setColor(getColor());
        clone.setShape(getShape());
        clone.setIcon(getIcon());
        

        copies.put(this.getNodeKey(), (T) clone);
        return clone;
    }
}

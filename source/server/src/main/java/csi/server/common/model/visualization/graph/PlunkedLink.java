package csi.server.common.model.visualization.graph;

import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.DeepCopiable;
import csi.server.common.model.ModelObject;
import csi.shared.gwt.viz.graph.LinkDirection;

/**
 * @author Centrifuge Systems, Inc.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PlunkedLink extends ModelObject implements DeepCopiable<PlunkedLink> {

    private String sourceNodeKey;
    private String targetNodeKey;

    private String label;
    private String linkType;
    private int transparency = 100;
    private int size = 1;
    private LinkDirection linkDirection = LinkDirection.NONE;
    private Integer color;

    public String getSourceNodeKey() {
        return sourceNodeKey;
    }

    public void setSourceNodeKey(String sourceNodeKey) {
        this.sourceNodeKey = sourceNodeKey;
    }

    public String getTargetNodeKey() {
        return targetNodeKey;
    }

    public void setTargetNodeKey(String targetNodeKey) {
        this.targetNodeKey = targetNodeKey;
    }

    public String buildItemKey(){
        return sourceNodeKey + "+" + targetNodeKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getTransparency() {
        return transparency;
    }

    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public LinkDirection getLinkDirection() {
        return linkDirection;
    }

    public void setLinkDirection(LinkDirection linkDirection) {
        this.linkDirection = linkDirection;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    @Override
    public PlunkedLink copy(Map<String, Object> copies) {
        PlunkedLink clone = new PlunkedLink();
        super.cloneComponents(clone);

        clone.setSourceNodeKey(sourceNodeKey);
        clone.setTargetNodeKey(targetNodeKey);

        clone.setLabel(getLabel());
        clone.setColor(getColor());
        clone.setSize(getSize());
        clone.setTransparency(getTransparency());
        clone.setLinkType(getLinkType());
        clone.setLinkDirection(getLinkDirection());
        return clone;
    }

    public <T extends ModelObject> PlunkedLink trueCopy(Map<String, T> copies) {
        PlunkedLink clone = new PlunkedLink();
        super.cloneComponents(clone);
        
        if(copies.containsKey(sourceNodeKey) && copies.get(sourceNodeKey) instanceof PlunkedNode){
        	PlunkedNode node = (PlunkedNode) copies.get(sourceNodeKey);
            clone.setSourceNodeKey(node.getNodeKey());
        } else {
            clone.setSourceNodeKey(sourceNodeKey);
        }
        
        if(copies.containsKey(targetNodeKey) && copies.get(targetNodeKey) instanceof PlunkedNode){
        	PlunkedNode node = (PlunkedNode) copies.get(targetNodeKey);
            clone.setTargetNodeKey(node.getNodeKey());
        } else {
            clone.setTargetNodeKey(targetNodeKey);
        }

        clone.setLabel(getLabel());
        clone.setColor(getColor());
        clone.setSize(getSize());
        clone.setTransparency(getTransparency());
        clone.setLinkType(getLinkType());
        clone.setLinkDirection(getLinkDirection());
        return clone;
    }
}

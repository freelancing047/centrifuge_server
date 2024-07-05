package csi.client.gwt.viz.graph.plunk.edit;

import csi.shared.gwt.viz.graph.LinkDirection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedLinkModel {

    private String label;
    private int size;
    private int transparency;
    private LinkDirection linkDirection;
    private Integer color;
    private String linkType;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTransparency() {
        return transparency;
    }

    public void setTransparency(int transparency) {
        this.transparency = transparency;
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
}

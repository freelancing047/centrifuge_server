package csi.server.business.visualization.graph.base;

import prefuse.util.io.XMLWriter;

import csi.server.common.model.visualization.graph.GraphConstants;
import csi.shared.gwt.viz.graph.LinkDirection;

public class LinkStore extends AbstractGraphObjectStore {

    public static final String LINK_LEGEND_INFO = "linkLegendInfo";

    protected String style;

    protected double width = 1;

    protected NodeStore firstEndpoint;

    protected NodeStore secondEndpoint;

    protected int countForward;

    protected int countReverse;

    protected int countNone;

    public boolean isDisplayable() {
        // return (super.isDisplayable() && firstEndpoint.isDisplayable() && secondEndpoint.isDisplayable());
        return !this.isHidden() && firstEndpoint.isDisplayable() && secondEndpoint.isDisplayable();
    }

    public boolean isEditable() {
    	boolean flag = firstEndpoint.isDisplayable() && secondEndpoint.isDisplayable();
    	return flag;
    }

    public void writeGraphML(XMLWriter xml) {
        xml.start("object", "type", "LinkStore");
        if (style != null) {
         xml.contentTag("style", style);
      }
        if (width > 0) {
         xml.contentTag("width", ((Double) width).toString());
      }
        super.writeSuperML(xml);
        xml.end();
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public NodeStore getFirstEndpoint() {
        return firstEndpoint;
    }

    public void setFirstEndpoint(NodeStore firstEndpoint) {
        this.firstEndpoint = firstEndpoint;
    }

    public NodeStore getSecondEndpoint() {
        return secondEndpoint;
    }

    public void setSecondEndpoint(NodeStore secondEndpoint) {
        this.secondEndpoint = secondEndpoint;
    }

    /**
     * Get the direction of link as a result of merging all direction per each row that create this link.
     * If there are both directed and undirected this will resolve to undirected {@link LinkDirection#NONE}
     *
     * @return the direction of the link
     */
    public LinkDirection getDirection() {
    	if (countNone == 0 ) {
    		if (countReverse == 0) {
    			if (countForward == 0) {
    				//safe net if there is no count
    				return LinkDirection.NONE;
    			} else {
    				return LinkDirection.FORWARD;
    			}
    		} else {
    			if (countForward == 0){
    				return LinkDirection.REVERSE;
    			} else {
    				return LinkDirection.BOTH;
    			}
    		}
    	} else {
    		return LinkDirection.NONE;
    	}
    }

    public void addDirectedEdge(LinkDirection directionValue) {
        switch (directionValue) {
            case FORWARD:
                countForward++;
                break;
            case REVERSE:
                countReverse++;
                break;
            case NONE:
                countNone++;
                break;
            case BOTH:
                countForward++;
                countReverse++;
            default:
                break;
        }
    }

    public int getCountForward() {
        return countForward;
    }

    public void setCountForward(int countForward) {
        this.countForward = countForward;
    }

    public void setCountReverse(int countReverse) {
        this.countReverse = countReverse;
    }

    public void setCountNone(int countNone) {
        this.countNone = countNone;
    }

    public int getCountReverse() {
        return countReverse;
    }

    public int getCountNone() {
        return countNone;
    }

    public void incrementCountForward(int increment) {
        this.countForward += increment;
    }


    public void incrementCountReverse(int increment) {
        this.countReverse += increment;
    }

    public void incrementCountNone(int increment) {
        this.countNone += increment;
    }

    public void clearIncrementCounts(){
        this.countNone = 0;
        this.countForward = 0;
        this.countReverse = 0;
    }

    @Override
    public String toString() {
        return "LinkStore{" +
                firstEndpoint + " -> " +
                secondEndpoint;
    }

    public void addType(String type) {
        if (type.length() == 0) {
            type = GraphConstants.UNSPECIFIED_LINK_TYPE;
        }
        super.addType(type);
        String typeName = type.trim();
        if (this.type.equals(GraphConstants.UNSPECIFIED_LINK_TYPE)) {
            this.type = typeName;
        }
    }

}


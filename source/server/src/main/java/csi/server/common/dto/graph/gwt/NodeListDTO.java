package csi.server.common.dto.graph.gwt;

import com.google.common.base.CaseFormat;
import com.google.gwt.user.client.rpc.IsSerializable;

public class NodeListDTO implements IsSerializable {
    private double bundleX;
    private double bundleY;

    public double getBundleX() {
        return bundleX;
    }

    public void setBundleX(double bundleX) {
        this.bundleX = bundleX;
    }

    public double getBundleY() {
        return bundleY;
    }

    public void setBundleY(double bundleY) {
        this.bundleY = bundleY;
    }

    // public interface NodeListFieldNames {
    //
    // String FIELD_ANCHORED = "anchored";
    // String FIELD_BETWEENNESS = "betweenness";
    // String FIELD_BUNDLE_COUNT = "bundleCount";
    // String FIELD_BUNDLE_NODE_LABEL = "bundleNodeLabel";
    // String FIELD_BUNDLED = "bundled";
    // String FIELD_COMPONENT = "component";
    // String FIELD_COUNT_IN_DISP_EDGES = "countInDispEdges";
    // String FIELD_DISPLAY_X = "displayX";
    // String FIELD_DISPLAY_Y = "displayY";
    // String FIELD_EIGENVECTOR = "eigenvector";
    // String FIELD_HIDDEN = "hidden";
    // String FIELD_HIDE_LABELS = "hideLabels";
    // String FIELD_ID = "id";
    // String FIELD_KEY = "key";
    // String FIELD_LABEL = "label";
    // String FIELD_NEIGHBOR_TYPE_COUNTS = "neighborTypeCounts";
    // String FIELD_NESTED_LEVEL = "nestedLevel";
    // String FIELD_SELECTED = "selected";
    // String FIELD_SIZE = "size";
    // String FIELD_SUBGRAPH_NODE_ID = "subgraphNodeId";
    // String FIELD_TOOLTIPS_LABEL = "tooltips";
    // String FIELD_TYPE = "type";
    // String FIELD_TYPES = "types";
    // String FIELD_VISIBLE_NEIGHBORS = "visibleNeighbors";
    // String FIELD_VISUALIZED = "visualized";
    // String FIELD_X = "x";
    // String FIELD_Y = "y";
    //
    // }

    public static enum NodeListFieldNames {

        ANCHORED, BETWEENNESS, BUNDLE_NODE_LABEL, BUNDLED, CLOSENESS, COMPONENT, DEGREES, DISPLAY_X, DISPLAY_Y, EIGENVECTOR, HIDDEN, HIDE_LABELS, ID, KEY, LABEL, NESTED_LEVEL, SELECTED, SIZE, TRANSPARENCY, TYPE, VISIBLE_NEIGHBORS, VISUALIZED, X, Y, PLUNKED, ANNOTATION, IS_BUNDLE;

        public static NodeListFieldNames get(String s) {
            // this fixes some behavior for passing field names.
            try {
                return valueOf(s);
            } catch (Exception e) {
            }
            String formattedString = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, s);
            try {
                return valueOf(formattedString);
            } catch (Exception e) {
                return null;
            }
        }

    }

    // public CsiMap<String, List<String>> attributeTooltips;
    public boolean anchored;
    public double betweenness;
    public boolean bundled;
    public String bundleNodeLabel;
    public double closeness;
    public int component;
    public double degrees;
    public double displayX;
    public double displayY;
    public double eigenvector;
    public boolean hidden;
    public Boolean hideLabels;
    public int ID;
    public String key;
    public String label;
    public int nestedLevel;
    public boolean selected;
    public boolean plunked;
    public double size;
    public String type;
    public String typedKey;
    public int visibleNeighbors;
    public Boolean visualized;
    public double x;
    public double y;
    public boolean annotation;
    public double transparency;

    public double getTransparency() {
        return transparency;
    }

    public void setTransparency(double transparency) {
        this.transparency = transparency;
    }

    public NodeListDTO() {

    }

    // public CsiMap<String, List<String>> getAttributeTooltips() {
    // return attributeTooltips;
    // }

    public double getBetweenness() {
        return betweenness;
    }

    public String getBundleNodeLabel() {
        return bundleNodeLabel;
    }

    public double getCloseness() {
        return closeness;
    }

    public int getComponent() {
        return component;
    }

    public double getDegrees() {
        return degrees;
    }

    public double getDisplayX() {
        return displayX;
    }

    public double getDisplayY() {
        return displayY;
    }

    public double getEigenvector() {
        return eigenvector;
    }

    public Boolean getHideLabels() {
        return hideLabels;
    }

    public int getID() {
        return ID;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public int getNestedLevel() {
        return nestedLevel;
    }

    public double getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getTypedKey() {
        return typedKey;
    }

    public int getVisibleNeighbors() {
        return visibleNeighbors;
    }

    public Boolean getVisualized() {
        return visualized;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isBundled() {
        return bundled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isSelected() {
        return selected;
    }

    // public void setAttributeTooltips(CsiMap<String, List<String>> attributeTooltips) {
    // this.attributeTooltips = attributeTooltips;
    // }

    public void setBetweenness(double betweenness) {
        this.betweenness = betweenness;
    }

    public void setBundled(boolean bundled) {
        this.bundled = bundled;
    }

    public void setBundleNodeLabel(String bundleNodeLabel) {
        this.bundleNodeLabel = bundleNodeLabel;
    }

    public void setCloseness(double closeness) {
        this.closeness = closeness;
    }

    public void setComponent(int component) {
        this.component = component;
    }

    public void setDegrees(double degrees) {
        this.degrees = degrees;
    }

    public void setDisplayX(double displayX) {
        this.displayX = displayX;
    }

    public void setDisplayY(double displayY) {
        this.displayY = displayY;
    }

    public void setEigenvector(double eigenvector) {
        this.eigenvector = eigenvector;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setHideLabels(Boolean hideLabels) {
        this.hideLabels = hideLabels;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setNestedLevel(int nestedLevel) {
        this.nestedLevel = nestedLevel;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypedKey(String typedKey) {
        this.typedKey = typedKey;
    }

    public void setVisibleNeighbors(int visibleNeighbors) {
        this.visibleNeighbors = visibleNeighbors;
    }

    public void setVisualized(Boolean isVisualized) {
        this.visualized = isVisualized;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isAnchored() {
        return anchored;
    }

    public void setAnchored(boolean anchored) {
        this.anchored = anchored;
    }

	public boolean isPlunked() {
		return plunked;
	}

	public void setPlunked(boolean plunked) {
		this.plunked = plunked;
	}

	public boolean hasAnnotation(){
		return annotation;
	}
	
	public void setAnnotation(boolean annotation){
		this.annotation = annotation;
	}

    public void setBundle(boolean isBundle) {
        this.bundle = isBundle;
    }

    public boolean isBundle(){
        return bundle;
    }

    public boolean bundle;
}

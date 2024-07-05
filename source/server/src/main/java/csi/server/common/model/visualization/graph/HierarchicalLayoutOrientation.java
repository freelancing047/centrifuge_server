package csi.server.common.model.visualization.graph;

public enum HierarchicalLayoutOrientation {
    LEFT_TO_RIGHT(prefuse.Constants.ORIENT_LEFT_RIGHT),
    RIGHT_TO_LEFT(prefuse.Constants.ORIENT_RIGHT_LEFT),
    TOP_TO_BOTTOM(prefuse.Constants.ORIENT_TOP_BOTTOM),
    BOTTOM_TO_TOP(prefuse.Constants.ORIENT_BOTTOM_TOP);

    private final int value;

    HierarchicalLayoutOrientation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

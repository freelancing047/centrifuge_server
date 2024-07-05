package csi.server.business.visualization.graph.pattern;

import prefuse.visual.VisualItem;

public class NodeBatchBody {
    private int csikey;

    public NodeBatchBody(VisualItem nodeVI) {
        csikey =  nodeVI.getRow();
    }
}

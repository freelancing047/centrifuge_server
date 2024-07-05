package csi.server.business.visualization.graph;

import java.util.HashMap;
import java.util.Map;

import csi.server.common.model.visualization.graph.GraphConstants;

public class LayoutHelper {

    static Map<String, GraphConstants.eLayoutAlgorithms> layouts;

    static {
        layouts = new HashMap();

        GraphConstants.eLayoutAlgorithms[] values = GraphConstants.eLayoutAlgorithms.values();
        for (GraphConstants.eLayoutAlgorithms layout : values) {
            layouts.put(layout.toString().toLowerCase(), layout);
        }

        layouts.put("force directed", GraphConstants.eLayoutAlgorithms.forceDirected);
        layouts.put("radial hierarchy", GraphConstants.eLayoutAlgorithms.treeRadial);
        layouts.put("radial", GraphConstants.eLayoutAlgorithms.treeRadial);
        layouts.put("treeradial", GraphConstants.eLayoutAlgorithms.treeRadial);

        layouts.put("linear hierarchy", GraphConstants.eLayoutAlgorithms.treeNodeLink);
        layouts.put("hierarchical", GraphConstants.eLayoutAlgorithms.treeNodeLink);

        layouts.put("centrifuge", GraphConstants.eLayoutAlgorithms.centrifuge);
        layouts.put("scramble & place", GraphConstants.eLayoutAlgorithms.forceDirected);
        layouts.put("scramble", GraphConstants.eLayoutAlgorithms.forceDirected);
        layouts.put("", GraphConstants.eLayoutAlgorithms.forceDirected);
    }

    public static GraphConstants.eLayoutAlgorithms getLayout(String name) {
        name = (name == null) ? "" : name.trim();

        GraphConstants.eLayoutAlgorithms layout = layouts.get(name.toLowerCase());
        return layout;
    }

}

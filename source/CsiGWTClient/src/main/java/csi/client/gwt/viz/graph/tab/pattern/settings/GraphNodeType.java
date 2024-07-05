package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.window.legend.NodeLegendItemProxy;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.model.CsiUUID;

public class GraphNodeType extends GraphType {
    private GraphNodeLegendItem item;
    private final NodeLegendItemProxy nodeLegendItemProxy;

    public GraphNodeType(Graph graph, GraphNodeLegendItem item) {
        this.item = item;
        setUuid(CsiUUID.randomUUID());
        this.nodeLegendItemProxy = new NodeLegendItemProxy(graph, item);
    }

    public String getName() {
        return item.typeName;
    }

    public void setName(String name) {
    }

    public Image getImage(int imageSize) {
        return nodeLegendItemProxy.getImage(imageSize);
    }
}

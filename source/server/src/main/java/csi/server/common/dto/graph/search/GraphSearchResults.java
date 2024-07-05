package csi.server.common.dto.graph.search;



import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class GraphSearchResults implements IsSerializable {

    private List<NodeInfo> nodes;
    private List<EdgeInfo> links;

    public boolean truncatedResults;
    public int total;

    public GraphSearchResults() {
        nodes = new LinkedList<NodeInfo>();
        links = new LinkedList<EdgeInfo>();
    }

    public List<EdgeInfo> getLinks() {
        return links;
    }

    public List<NodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeInfo> nodes) {
        this.nodes = nodes;
    }

    public void setLinks(List<EdgeInfo> links) {
        this.links = links;
    }

}

package csi.server.business.visualization.graph.pattern.neo4j;

import csi.server.business.visualization.graph.pattern.NodeBatchBody;

/**
 * Created by Patrick on 12/29/2014.
 */
public class NodeBatchRequest {
    private String method = "POST";
    private String to = "/node";
    private Object body;
    private int id;
    public NodeBatchRequest(NodeBatchBody body, int id){

        this.body = body;
        this.id = id;
    }
}

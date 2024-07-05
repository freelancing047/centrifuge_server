package csi.server.business.visualization.graph.pattern.neo4j;

public class RelationshipLabelRequest {
    private String method = "POST";
    private String to;
    private String body;

    public RelationshipLabelRequest(String label, int i) {
        this.to = "{" + i + "}/labels";
        this.body = label;
    }
}

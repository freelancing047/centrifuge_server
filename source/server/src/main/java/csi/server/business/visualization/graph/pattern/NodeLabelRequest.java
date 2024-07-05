package csi.server.business.visualization.graph.pattern;

public class NodeLabelRequest {
    private String method = "POST";
    private String to;
    private String body;

    public NodeLabelRequest(String label, int i) {
        this.to = "{"+i+"}/labels";
        this.body = label;
    }
}

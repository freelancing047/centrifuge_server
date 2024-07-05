package csi.server.business.visualization.graph.pattern;

public class RelationshipBatchRequest {
    private String method = "POST";
    private String to;
    private int id;
    private Object body;


    public RelationshipBatchRequest(int node, int node1, String key, int requestId, String type) {
        id = requestId;
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(node).append("}/relationships");
        to = sb.toString();
        body = new RelationshipBatchBody("" + node1, key, type);
    }

    public class RelationshipBatchBody{
        private String to;
        private Data data;
        private String type = "both";

        public RelationshipBatchBody(String to, String data, String type) {
            this.to = "{"+to+"}";
            this.data = new Data(data);
            this.type = type;
        }

        private class Data {
            private String csikey;

            private Data(String data) {
                this.csikey = data;
            }

        }
    }
}

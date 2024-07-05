package csi.server.business.visualization.graph;

import prefuse.data.Graph;
import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.server.common.exception.CentrifugeException;

public class GraphLoader implements Runnable {

    private Graph graph;
    private Authorization auth;
    private GraphManager mgr = GraphManager.getInstance();

    public GraphLoader(Graph graph, Authorization auth) {
        super();
        this.graph = graph;
        this.auth = auth;
    }

    @Override
    public void run() {
        CsiSecurityManager.setAuthorization(auth);

//        try {
//        //    mgr.createNodesAndLinks(graph);
//        } catch (CentrifugeException e) {
//            e.printStackTrace();
//        }
    }

}

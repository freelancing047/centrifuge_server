package csi.server.business.visualization.graph.io;

import java.io.PrintWriter;
import java.util.Iterator;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

public class DotWriter {

    Graph graph;

    public DotWriter() {

    }

    public void write(Graph graph, PrintWriter writer) {

        String s = "nodesep=\".75\"";

        this.graph = graph;
        writer.println("digraph G {");
        writer.println(s);
        Iterator edges = graph.edges();
        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            Node sourceNode = edge.getSourceNode();
            Node targetNode = edge.getTargetNode();

            String skey = getKey(sourceNode);
            String tkey = getKey(targetNode);

            writer.print(skey);
            writer.print(" -> ");
            writer.print(tkey);
            writer.println(";");
        }
        writer.println("}");
    }

    private String getKey(Node node) {
        return Integer.toString(node.getRow());
    }

}

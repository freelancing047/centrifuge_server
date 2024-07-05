package csi.server.business.visualization.graph.base;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.Callable;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.tuple.TupleSet;
import csi.server.common.model.visualization.graph.GraphConstants;

//import prefuse.Visualization;

public class PatchworkAlgorithm implements Callable<Void> {

    protected Graph graph;

    protected List<Rectangle> regions;
    protected int componentCount;
    protected Table nodeTable;
    protected List<Graph> subGraphs;

    public Graph getGraph() {
        return graph;
    }

    @SuppressWarnings("unchecked")
    protected void setup() {
        componentCount = (Integer) graph.getClientProperty(GraphConstants.COMPONENT_COUNT);

        nodeTable = graph.getNodeTable();

        subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);
    }

    public Void call() throws Exception {

        setup();

        int[] nodesPerComponent = new int[componentCount];
        int totalNodes = 0;

        for (int i = 0; i < componentCount; i++) {

            Graph subGraph = subGraphs.get(i);
            TupleSet set = subGraph.getNodes();
            int count = set.getTupleCount();
            nodesPerComponent[i] = count;
            totalNodes += count;
        }

        int spaceDimension = (int) (10d * Math.sqrt(totalNodes));
        int lowerX = 0;
        int lowerY = 0;
        int upperX = 0;

        boolean visited[] = new boolean[componentCount];
        while (true) {
            int max = -1;
            int index = -1;
            for (int i = 0; i < componentCount; i++) {
                if (!visited[i]) {
                    if (nodesPerComponent[i] > max) {
                        max = nodesPerComponent[i];
                        index = i;
                    }
                }
            }

            if (max == -1) {
                break;
            }

            visited[index] = true;

            double fractional = Math.sqrt(((double) nodesPerComponent[index] / (double) totalNodes));
            int patchDim = (int) ((double) spaceDimension * fractional);

            if (lowerY + patchDim > spaceDimension) {
                lowerY = 0;
                lowerX = upperX;
                upperX += patchDim;
            } else if (upperX == 0) {
                upperX = patchDim;
            }

            Rectangle region = new Rectangle();
            region.x = lowerX;
            region.y = lowerY;
            region.width = region.height = patchDim;

            Graph subGraph = subGraphs.get(index);
            subGraph.putClientProperty(GraphConstants.PATCH_REGION, region);

            lowerY += patchDim;
        }

        return null;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

}

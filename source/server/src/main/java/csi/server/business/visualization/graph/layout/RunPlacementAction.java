package csi.server.business.visualization.graph.layout;

import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.VisualGraph;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;

public class RunPlacementAction extends RecursiveAction {
    private final List<Graph> subGraphs;
    private Graph graph;
    private final GraphContext context;
    private final SelectionModel selection;
    private final GraphConstants.eLayoutAlgorithms algorithm;
    private final int netId;
    private final RelGraphViewDef relGraphViewDef;
    private List<Node> networkElected = Lists.newArrayList();
    private TaskContext taskContext;

    public RunPlacementAction(List<Graph> subGraphs, Graph graph, GraphContext context, SelectionModel selection, GraphConstants.eLayoutAlgorithms algorithm, int i, RelGraphViewDef relGraphViewDef, TaskContext taskContext) {
        this.subGraphs = subGraphs;
        this.graph = graph;
        this.context = context;
        this.selection = selection;
        this.algorithm = algorithm;
        this.netId = i;
        this.relGraphViewDef = relGraphViewDef;
        this.taskContext = taskContext;
    }

    @Override
    protected void compute() {
        if(subGraphs.isEmpty()) {
            return;
        } else if(subGraphs.size()==1){
            //do the placement.
            Graph subgraph = subGraphs.get(0);
            for (Integer id : selection.nodes) {
                Node node = graph.getNode(id);
                if (netId == node.getInt(GraphConstants.COMPONENT_ID)) {
                    int subnetId = node.getInt(GraphConstants.SUBGRAPH_NODE_ID);
                    networkElected.add(subgraph.getNode(subnetId));
                }
            }
            VisualGraph visualGraph = (VisualGraph) graph.getClientProperty(GraphContext.VISUAL_GRAPH);
            TaskController.getInstance().setCurrentContext(taskContext);
            try{
                GraphManager.layoutComponent(visualGraph, subgraph, algorithm, context, networkElected, netId, relGraphViewDef);
            } finally{
                TaskController.getInstance().setCurrentContext(null);
            }
        }
        else {
            int size = subGraphs.size();
            int half = IntMath.divide(size, 2, RoundingMode.DOWN);
            List<Graph> list1 = subGraphs.subList(0, half);
            list1 = Lists.newArrayList(list1);
            List<Graph> list2 = subGraphs.subList(half, size);
            list2 = Lists.newArrayList(list2);
            RunPlacementAction.invokeAll(new RunPlacementAction(list1, graph, context, selection, algorithm, netId, relGraphViewDef, taskContext), new RunPlacementAction(list2, graph, context, selection, algorithm, netId + half, relGraphViewDef, taskContext));
        }
    }
}

package csi.server.business.selection.torows;

import java.util.HashSet;
import java.util.Set;

import prefuse.data.Graph;

import com.google.common.collect.Sets;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.base.GraphSupportingRows;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class GraphSelectionToRowsConverter implements SelectionToRowsConverter {
    private final GraphContext graphContext;

    public GraphSelectionToRowsConverter(GraphContext graphContext) {
        this.graphContext = graphContext;
    }

    @Override
    public Set<Integer> convertToRows(Selection selection, boolean excludeBroadcast) {
        if (!(selection instanceof SelectionModel)) {
            return Sets.newHashSet();
        }

        SelectionModel selectionModel = (SelectionModel) selection;
        //There is no selection on graph, so just return empty  
        if(graphContext == null) {
            return new HashSet<Integer>();
        }
        Graph graphData = graphContext.getVisibleGraph();
        return GraphSupportingRows.getSupportingRows(graphData, selectionModel, excludeBroadcast);
    }

}

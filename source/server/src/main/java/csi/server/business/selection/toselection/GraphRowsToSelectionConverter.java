package csi.server.business.selection.toselection;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Multimap;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class GraphRowsToSelectionConverter implements RowsToSelectionConverter {

    private final GraphContext graphContext;

    public GraphRowsToSelectionConverter(GraphContext graphContext) {
        this.graphContext = graphContext;
    }

    @Override
    public Selection toSelection(Set<Integer> rows) {
        Multimap<Integer, Integer> nodesByInternalIds = graphContext.getNodesByRow();
        Multimap<Integer, Integer> linksByInternalIds = graphContext.getLinksByRow();

        TreeSet<Integer> allNodeIds = new TreeSet<Integer>();
        TreeSet<Integer> allLinkIds = new TreeSet<Integer>();

        for (Integer row : rows) {
            allNodeIds.addAll(nodesByInternalIds.get(row));
            allLinkIds.addAll(linksByInternalIds.get(row));
        }

        SelectionModel selectionModel = new SelectionModel();
        selectionModel.nodes = allNodeIds;
        selectionModel.links = allLinkIds;

        return selectionModel;
    }
}

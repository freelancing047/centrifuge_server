package csi.server.business.visualization.graph.pattern.selection;

import prefuse.data.Graph;
import csi.server.business.visualization.graph.pattern.model.Pattern;
import csi.server.common.model.visualization.selection.SelectionModel;

/**
 * Converts a SelectionModel into a Pattern.
 *
 * @author Centrifuge Systems, Inc.
 */
public interface SelectionToPattern {

    public Pattern createPattern(SelectionModel selection, Graph graph);
}

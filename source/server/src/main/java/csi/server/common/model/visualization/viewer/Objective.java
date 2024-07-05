package csi.server.common.model.visualization.viewer;

import csi.server.common.model.visualization.selection.Selection;

import java.io.Serializable;

public interface Objective extends Serializable{

    public Selection getSelection();

    String getDataviewUuid();

    String getVisualizationUuid();

    String getVisualizationName();
}

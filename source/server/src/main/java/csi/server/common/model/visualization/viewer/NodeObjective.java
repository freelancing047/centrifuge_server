package csi.server.common.model.visualization.viewer;

import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;

public class NodeObjective implements Objective {
    private FindItemDTO findItemDTO;
    private String dataviewUuid;
    private String visualizationUuid;
    private String visualizationName;

    public NodeObjective(FindItemDTO dto, String dataviewUuid, String visualizationUuid, String visualizationName) {
        this.findItemDTO = dto;
        this.dataviewUuid = dataviewUuid;
        this.visualizationUuid = visualizationUuid;
        this.visualizationName = visualizationName;
    }

    public FindItemDTO getFindItemDTO() {
        return findItemDTO;
    }


    public NodeObjective() {
    }

    @Override
    public Selection getSelection() {
        SelectionModel selection = new SelectionModel();
        selection.nodes.add(findItemDTO.getID());
        return selection;
    }

    @Override
    public String getDataviewUuid() {
        return dataviewUuid;
    }

    @Override
    public String getVisualizationUuid() {
        return visualizationUuid;
    }

    @Override
    public String getVisualizationName() {
        return visualizationName;
    }

}

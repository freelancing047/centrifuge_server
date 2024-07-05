package csi.server.common.model.visualization.viewer;

import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.model.visualization.viewer.Objective;

public class LinkObjective implements Objective {
    public FindItemDTO getFindItemDTO() {
        return findItemDTO;
    }


    private FindItemDTO findItemDTO;
    private String dataviewUuid;
    private String visualizationUuid;

    public LinkObjective(FindItemDTO dto, String dataviewUuid, String visualizationUuid) {
        this.findItemDTO = dto;
        this.dataviewUuid = dataviewUuid;
        this.visualizationUuid = visualizationUuid;
    }

    public LinkObjective() {
    }

    @Override
    public Selection getSelection() {
        SelectionModel selection = new SelectionModel();
        selection.links.add(findItemDTO.getID());
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
        return null;
    }

}

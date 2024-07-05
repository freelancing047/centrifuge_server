package csi.server.common.dto;

import java.io.Serializable;

import csi.server.common.model.visualization.selection.Selection;

public class SpinoffRequestV2 implements Serializable {

    private String dataViewUuid;
    private String visualizationUuid;
    private Selection selection;

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    public String getVisualizationUuid() {
        return visualizationUuid;
    }

    public void setDataViewUuid(String dataViewUuid) {
        this.dataViewUuid = dataViewUuid;
    }

    public void setVisualizationUuid(String visualizationUuid) {
        this.visualizationUuid = visualizationUuid;
    }

    public Selection getSelection() {
        return selection;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }
}

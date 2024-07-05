package csi.server.business.visualization.graph.pattern.selection;

import csi.server.common.model.visualization.selection.SelectionModel;

public class PatternSelection {
    private SelectionModel selectionModel;
    private String color;

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(SelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

package csi.client.gwt.viz.graph.settings;

import com.google.gwt.cell.client.FieldUpdater;

import csi.client.gwt.viz.graph.settings.fielddef.FieldProxy;

class FieldDefGridSpy implements FieldUpdater<FieldProxy, FieldProxy> {

    private final GraphSettingsPanel graphSettingsPanel;

    FieldDefGridSpy(GraphSettingsPanel graphSettingsPanel) {
        this.graphSettingsPanel = graphSettingsPanel;
    }

    @Override
    public void update(int index, FieldProxy object, FieldProxy value) {
        graphSettingsPanel.getGraphSettings().addNode(value);
    }
}
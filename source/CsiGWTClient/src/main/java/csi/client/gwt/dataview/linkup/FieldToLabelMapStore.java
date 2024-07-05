package csi.client.gwt.dataview.linkup;

import csi.client.gwt.widget.gxt.grid.DataStoreColumnAccess;
import csi.server.common.model.FieldDef;


//
// Data store used for a single row of the field mapping grid
//
public class FieldToLabelMapStore implements DataStoreColumnAccess {

    private LinkupGridMapper<FieldToLabelMapStore> _parent = null;

    public String key;                      // Parameter Local ID

    public Boolean  mapField = false;       // column 1
    public String label;                    // column 2
    public FieldDef mappingField;            // column 3

    public FieldToLabelMapStore(LinkupGridMapper<FieldToLabelMapStore> parentIn, String keyIn, String promptIn) {
        _parent = parentIn;
        key = keyIn;
        label = promptIn;
        mapField = false;
        clearMappingField();
    }

    public FieldToLabelMapStore(LinkupGridMapper<FieldToLabelMapStore> parentIn, String keyIn, String promptIn, FieldDef mappingFieldIn) {
        _parent = parentIn;
        key = keyIn;
        label = promptIn;
        mappingField = mappingFieldIn;
        mapField = true;
    }

    public String getParameterKey() {
        return key;
    }

    public void setLabel(String labelIn) {
        label = labelIn;
        signalChange();
    }

    public String getLabel() {
        return label;
    }

    public void setMappingField(FieldDef parameterDataIn) {
        mappingField = parameterDataIn;
        signalChange();
    }

    public FieldDef getMappingField() {
        return mappingField;
    }

    public Boolean getMapField() {
        return mapField;
    }

    public void setMapField(Boolean mapFieldIn) {
        mapField = mapFieldIn;
        if (!mapField) {

            clearMappingField();
        }
        signalChange();
    }

    public Object getColumnData(int columnIn) {

        Object myData = null;

        return myData;
    }

    public void setColumnData(int columnIn, Object dataIn) {
    }

    public boolean isSelected() {

        return mapField;
    }

    private void signalChange() {

        if (null != _parent) {

            if (null != mappingField) {

                _parent.rowComplete(this);
            }
            _parent.selectionChange(this);
        }
    }

    private void clearMappingField() {

        mappingField = new FieldDef(true);
        mappingField.setLocalId(null);
    }
}

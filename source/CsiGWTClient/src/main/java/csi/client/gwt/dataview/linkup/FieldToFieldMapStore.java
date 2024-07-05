package csi.client.gwt.dataview.linkup;

import csi.client.gwt.widget.gxt.grid.DataStoreColumnAccess;
import csi.server.common.model.FieldDef;


//
// Data store used for a single row of the field mapping grid
//
public class FieldToFieldMapStore implements DataStoreColumnAccess {

    private LinkupGridMapper<FieldToFieldMapStore> _parent = null;

    public String key;                      // mapped field local Id

    public Boolean  mapField = false;       // column 1
    public FieldDef mappedField;            // column 2
    public FieldDef mappingField;           // column 3

    public FieldToFieldMapStore(LinkupGridMapper<FieldToFieldMapStore> parentIn, String keyIn, FieldDef mappedFieldIn) {
        _parent = parentIn;
        key = keyIn;
        mappedField = mappedFieldIn;
        mapField = false;
        clearMappingField();
    }

    public FieldToFieldMapStore(LinkupGridMapper<FieldToFieldMapStore> parentIn, String keyIn, FieldDef mappedFieldIn, FieldDef mappingFieldIn) {
        _parent = parentIn;
        key = keyIn;
        mappedField = mappedFieldIn;
        mappingField = mappingFieldIn;
        mapField = true;
    }

    public void setMappingField(FieldDef mappingFieldIn) {
        mappingField = mappingFieldIn;
        signalChange();
    }

    public FieldDef getMappingField() {
        return mappingField;
    }

    public void setMappedField(FieldDef mappedFieldIn) {
        mappedField = mappedFieldIn;
        signalChange();
    }

    public FieldDef getMappedField() {
        return mappedField;
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

    public void signalChange() {

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

package csi.client.gwt.widget.gxt.grid;

import csi.server.common.model.FieldDef;
import csi.server.common.model.column.InstalledColumn;

/**
 * Created by centrifuge on 9/24/2018.
 */
public class ColumnMappingDataItem implements DataStoreColumnAccess {


    private InstalledColumnMappingGrid _parent = null;

    public String key;                      // mapped field local Id

    public Boolean  mapField = false;       // column 1
    public InstalledColumn mappedColumn;    // column 2
    public FieldDef mappingField;           // column 3

    public ColumnMappingDataItem(InstalledColumnMappingGrid parentIn, String keyIn, InstalledColumn mappedColumnIn) {
        _parent = parentIn;
        key = keyIn;
        mappedColumn = mappedColumnIn;
        mapField = false;
        clearMappingField();
    }

    public ColumnMappingDataItem(InstalledColumnMappingGrid parentIn, String keyIn, InstalledColumn mappedColumnIn, FieldDef mappingFieldIn) {
        _parent = parentIn;
        key = keyIn;
        mappedColumn = mappedColumnIn;
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

    public void setMappedField(InstalledColumn mappedColumnIn) {
        mappedColumn = mappedColumnIn;
        signalChange();
    }

    public InstalledColumn getMappedField() {
        return mappedColumn;
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

package csi.server.common.dto.installed_tables;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.column.InstalledColumn;

/**
 * Created by centrifuge on 8/25/2015.
 */
public class ColumnParameters implements IsSerializable, MapByDataType {

    private String _columnName;
    private String _fieldName;
    private String _localId;
    private String _tableId;
    private CsiDataType _columnType;
    private CsiDataType _fieldType;
    private boolean _nullable;
    private boolean _included;

    public ColumnParameters() {

    }

    public ColumnParameters(String tableIdIn, InstalledColumn columnIn) {

        _localId = columnIn.getLocalId();
        _tableId = tableIdIn;
        _fieldName = columnIn.getFieldName();
        _fieldType = columnIn.getType();
        _nullable = columnIn.isNullable();
        _included = true;
    }

    public ColumnParameters(String nameIn, CsiDataType dataTypeIn, boolean nullableIn, boolean includedIn) {

        _localId = CsiUUID.randomUUID();
        _fieldName = nameIn;
        _columnName = nameIn;
        _fieldType = dataTypeIn;
        _columnType = dataTypeIn;
        _nullable = nullableIn;
        _included = includedIn;
    }

    public ColumnParameters(String tableIdIn, CsiDataType dataTypeIn, boolean nullableIn) {

        _tableId = tableIdIn;
        _localId = null;
        _fieldName = null;
        _columnName = null;
        _fieldType = null;
        _columnType = dataTypeIn;
        _nullable = nullableIn;
        _included = false;
    }

    public ColumnParameters(String fieldNameIn, String columnNameIn, CsiDataType fieldTypeIn,
                            CsiDataType columnTypeIn, boolean nullableIn, boolean includedIn) {

        _fieldName = fieldNameIn;
        _columnName = columnNameIn;
        _localId = CsiUUID.randomUUID();
        _fieldType = fieldTypeIn;
        _columnType = columnTypeIn;
        _nullable = nullableIn;
        _included = includedIn;
    }

    public String getColumnKey() {

        return _localId;
    }

    public String getColumnLocalId() {

        return getLocalId();
    }

    public void setTableLocalId(String tableIdIn) {

        _tableId = tableIdIn;
    }

    public String getTableLocalId() {

        return _tableId;
    }

    public void mapDoubleMapByType(CsiDataType dataTypeIn, Map<String, String> idMapIn, Map<String, String> nameMapIn) {

        if (getDataType() == dataTypeIn) {

            nameMapIn.put(getName(), getLocalId());
            idMapIn.put(getLocalId(), getName());
        }
    }

    public void setFieldName(String fieldNameIn) {

        _fieldName = fieldNameIn;
    }

    public String getFieldName() {

        return _fieldName;
    }

    public void setColumnName(String columnNameIn) {

        _columnName = columnNameIn;
    }

    public String getColumnName() {

        return _columnName;
    }

    public void setName(String nameIn) {

        _fieldName = nameIn;
        _columnName = nameIn;
    }

    public String getName() {

        return (null != _fieldName) ? _fieldName : _columnName;
    }

    public void setLocalId(String localIdIn) {

        _localId = localIdIn;
    }

    public String getLocalId() {

        return _localId;
    }

    public void setFieldType(CsiDataType fieldTypeIn) {

        _fieldType = fieldTypeIn;
    }

    public CsiDataType getFieldType() {

        return _fieldType;
    }

    public void setColumnType(CsiDataType columnTypeIn) {

        _columnType = columnTypeIn;
    }

    public CsiDataType getColumnType() {

        return _columnType;
    }

    public void setDataType(CsiDataType dataTypeIn) {

        _fieldType = dataTypeIn;
        _columnType = dataTypeIn;
    }

    public CsiDataType getDataType() {

        return (null != _fieldType) ? _fieldType : _columnType;
    }

    public void setNullable(boolean nullableIn) {

        _nullable = nullableIn;
    }

    public boolean isNullable() {

        return _nullable;
    }

    public void setIncluded(boolean includedIn) {

        _included = includedIn;
    }

    public boolean isIncluded() {

        return _included;
    }
}

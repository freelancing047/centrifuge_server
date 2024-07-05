package csi.server.common.model.column;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.ColumnKeys;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

/**
 * Created by centrifuge on 4/4/2016.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OrphanColumn extends ModelObject implements ColumnKeys {
   public static final String ORPHAN_STRING = "<orphan>";

    private int ordinal;
    private String localId;
    private String columnName;
    @Enumerated(value = EnumType.STRING)
    private CsiDataType dataType;
    private String tableLocalId;
    private String fieldLocalId;

    public OrphanColumn() {

        super();
    }

    public OrphanColumn(FieldDef fieldIn, ColumnDef columnIn) {

        super();

        ordinal = columnIn.getOrdinal();
        localId = columnIn.getLocalId();
        columnName = columnIn.getColumnName();
        dataType = columnIn.getCsiType();
        fieldLocalId = fieldIn.getLocalId();
    }

    public OrphanColumn(FieldDef fieldIn, String columnIdIn) {

        super();

        ordinal = -1;
        localId = columnIdIn;
        columnName = fieldIn.getFieldName();
        dataType = fieldIn.getValueType();
        fieldLocalId = fieldIn.getLocalId();
    }

    public String getColumnKey() {

        return (null != localId) ? localId + ((null != tableLocalId) ? tableLocalId : ORPHAN_STRING) : null;
    }

    public String getColumnLocalId() {

        return getLocalId();
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localIdIn) {
        localId = localIdIn;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnNameIn) {
        columnName = columnNameIn;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinalIn) {
        ordinal = ordinalIn;
    }

    public String getCsiTypeDisplay() {
        return dataType.getLabel();
    }

    public CsiDataType getDataype() {
        return dataType;
    }

    public void setDataType(CsiDataType dataTypeIn) {
        dataType = dataTypeIn;
    }

    public String getFieldLocalId() {
        return fieldLocalId;
    }

    public void setFieldLocalId(String fieldLocalIdIn) {
        fieldLocalId = fieldLocalIdIn;
    }

    public String getTableLocalId() {
        return tableLocalId;
    }

    public void setTableLocalId(String tableLocalIdIn) {
        tableLocalId = tableLocalIdIn;
    }
}

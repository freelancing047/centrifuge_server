package csi.server.common.model.column;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.tables.InstalledTable;

/**
 * Created by centrifuge on 9/14/2015.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class InstalledColumn extends ModelObject {

    private String fieldName;
    private String columnName;
    private String localId;
    private CsiDataType type;
    private boolean nullable;
    private int ordinal;

    @ManyToOne
    private InstalledTable installedTable;

    public InstalledColumn() {

    }

    public InstalledColumn(String fieldNameIn, String localIdIn, CsiDataType typeIn, boolean nullableIn, int ordinalIn) {

        this(fieldNameIn, localIdIn, typeIn, nullableIn);
        ordinal = ordinalIn;
    }

    public InstalledColumn(String fieldNameIn, String localIdIn, CsiDataType typeIn, boolean nullableIn) {

        fieldName = fieldNameIn;
        localId = localIdIn;
        columnName = localId.replace('-', '_');
        type = typeIn;
        nullable = nullableIn;
    }

    public void setFieldName(String fieldNameIn) {

        fieldName = fieldNameIn;
    }

    public String getFieldName() {

        return fieldName;
    }

    public void setColumnName(String columnNameIn) {

        columnName = columnNameIn;
    }

    public String getColumnName() {

        return columnName;
    }

    public void setLocalId(String localIdIn) {

        localId = localIdIn;
    }

    public String getLocalId() {

        return localId;
    }

    public void setType(CsiDataType typeIn) {

        type = typeIn;
    }

    public CsiDataType getType() {

        return type;
    }

    public void setNullable(boolean nullableIn) {

        nullable = nullableIn;
    }

    public boolean isNullable() {

        return nullable;
    }

    public void setOrdinal(int ordinalIn) {

        ordinal = ordinalIn;
    }

    public int getOrdinal() {

        return ordinal;
    }

    public void setInstalledTable(InstalledTable installedTableIn) {

        installedTable = installedTableIn;
    }

    public InstalledTable getInstalledTable() {

        return installedTable;
    }
}

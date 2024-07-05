package csi.client.gwt.dataview.fieldlist.grid;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldType;

/**
 * @author Centrifuge Systems, Inc.
 * Model for the FieldGrid
 */
public class FieldGridModel {
    private String uuid;
    private Integer ordinal;
    private String name;
    private CsiDataType dataType;
    private String fieldType;
    private boolean deletable;

    public FieldGridModel(){
        deletable = false;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinalIn) {
        this.ordinal = ordinalIn + 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CsiDataType getDataType() {
        return dataType;
    }

    public void setDataType(CsiDataType dataType) {
        this.dataType = dataType;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public static boolean isFieldTypeDeletable(FieldType fieldType) {
        return ((null != fieldType) && fieldType.isDeletable());
    }
}

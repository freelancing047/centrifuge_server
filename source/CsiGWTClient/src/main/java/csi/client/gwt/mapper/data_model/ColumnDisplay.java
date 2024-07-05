package csi.client.gwt.mapper.data_model;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;

/**
 * Created by centrifuge on 3/25/2016.
 */
public class ColumnDisplay extends SelectionDataAccess<ColumnDef> {

    ColumnDef _column;

    public ColumnDisplay(String idIn, ColumnDef columnIn) {

        super(idIn);
        _column = columnIn;
    }

    @Override
    public String getKey() {

        return (null != _column) ? _column.getLocalId() : null;
    }

    @Override
    public int getOrdinal() {

        return (null != _column) ? _column.getOrdinal() : 32000;
    }

    @Override
    public String getMappingName() {

        return getItemDisplayName();
    }

    @Override
    public String getGroupDisplayName() {

        String myGroup = (null != _column) ? _column.getDsoName() : null;

        return (null != myGroup) ? myGroup : "";
    }

    @Override
    public String getItemDisplayName() {

        String myItem = (null != _column) ? _column.getColumnName() : null;

        return (null != myItem) ? myItem : "";
    }

    @Override
    public CsiDataType getItemDataType() {

        CsiDataType myType = (null != _column) ? _column.getCsiType() : null;

        return (null != myType) ? myType : CsiDataType.Unsupported;
    }

    @Override
    public CsiDataType getCastToDataType() {

        CsiDataType myType = (null != _column) ? _column.getCsiType() : null;

        return (null != myType) ? myType : CsiDataType.Unsupported;
    }

    @Override
    public JdbcDriverType getGroupType() {

        JdbcDriverType myType = (null != _column) ? _column.getDsoType() : null;

        return (null != myType) ? myType : JdbcDriverType.OTHER;
    }

    @Override
    public ColumnDef getData() {

        return _column;
    }

    @Override
    public void setMapped(boolean mappedIn) {

        if (null != _column) {

            _column.setMapped(mappedIn);
        }
    }

    @Override
    public boolean isMapped() {

        return (null != _column) ? _column.isMapped() : false;
    }

    public String getParentId() {

        SqlTableDef myParent = (null != getData()) ? getData().getTableDef() : null;

        return (null != myParent) ? myParent.getLocalId() : null;
    }
}

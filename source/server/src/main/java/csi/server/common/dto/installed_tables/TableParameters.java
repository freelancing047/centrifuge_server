package csi.server.common.dto.installed_tables;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.model.ModelObject;

/**
 * Created by centrifuge on 11/13/2015.
 */
public class TableParameters implements IsSerializable {

    private String _tableName = null;
    private String _remarks = null;
    private boolean _deleteFile = false;
    private boolean _fixNulls = false;
    private ColumnParameters[] _columnParameterList = null;

    public TableParameters() {

    }

    public void setTableName(String tableNameIn) {

        _tableName = tableNameIn;
    }

    public String getTableName() {

        return _tableName;
    }

    public void setRemarks(String remarksIn) {

        _remarks = remarksIn;
    }

    public String getRemarks() {

        return _remarks;
    }

    public void setDeleteFile(boolean deleteFileIn) {

        _deleteFile = deleteFileIn;
    }

    public boolean getDeleteFile() {

        return _deleteFile;
    }

    public void setFixNulls(boolean fixNullsIn) {

        _fixNulls = fixNullsIn;
    }

    public boolean getFixNulls() {

        return _fixNulls;
    }

    public void setColumnParameterList(ColumnParameters[] columnParameterListIn) {

        _columnParameterList = columnParameterListIn;
    }

    public ColumnParameters[] getColumnParameterArray() {

        return _columnParameterList;
    }

    public void setColumnParameters(int columnIdIn, ColumnParameters columnParametersIn) {

        if ((0 <= columnIdIn) && (_columnParameterList.length > columnIdIn)) {

            _columnParameterList[columnIdIn] = columnParametersIn;
        }
    }

    public ColumnParameters getColumnParameters(int columnIdIn) {

        if ((0 <= columnIdIn) && (_columnParameterList.length > columnIdIn)) {

            return _columnParameterList[columnIdIn];

        } else {

            return null;
        }
    }
}

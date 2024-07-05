package csi.client.gwt.mapper.data_model;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.column.ColumnDef;

/**
 * Created by centrifuge on 3/25/2016.
 */
public class FieldDisplay extends SelectionDataAccess<FieldDef> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtGroupInUse = _constants.mapper_FieldGroup_InUse();
    private static final String _txtGroupNotInUse = _constants.mapper_FieldGroup_NotInUse();

    FieldDef _field;
    ColumnDef _column;
    boolean _mapped = false;
    String _columnName = null;
    CsiDataType _columnType = null;
    boolean  _inUse = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldDisplay(String idIn, FieldDef fieldIn, ColumnDef columnIn, CsiDataType overrideIn, boolean inUseIn) {

        super(idIn);
        _field = fieldIn;
        _column = columnIn;
        _columnName = (null != columnIn) ? columnIn.getColumnName() : getBaseName(_field.getFieldName());
        _columnType = (null != overrideIn) ? overrideIn : _field.getStorageType();
        if (null == _columnType) {

            _columnType = (null != columnIn) ? columnIn.getCsiType() : _field.getValueType();
        }
        _field.forceStorageType(_columnType);
        _inUse = inUseIn;
    }

    @Override
    public String getKey() {

        return (null != _field) ? _field.getLocalId() : CsiUUID.randomUUID();
    }

    @Override
    public int getOrdinal() {

        return (null != _field) ? _field.getOrdinal() : 32000;
    }

    @Override
    public String getMappingName() {

        return (null != _columnName) ? _columnName : (null != _field) ? _field.getFieldName() : "";
    }

    @Override
    public String getGroupDisplayName() {

        return _inUse ? _txtGroupInUse : _txtGroupNotInUse;
    }

    @Override
    public String getItemDisplayName() {

        String myBaseName = _field.getFieldName();
/*
        if (null != _columnName) {

            if (null != myBaseName) {

                myBaseName += "(" + _columnName + ")";

            } else {

                myBaseName = _columnName;
            }
        }
*/
        return (null != myBaseName) ? myBaseName : "";
    }

    @Override
    public CsiDataType getItemDataType() {

        return (null != _columnType) ? _columnType : CsiDataType.Unsupported;
    }

    @Override
    public JdbcDriverType getGroupType() {

        return JdbcDriverType.OTHER;
    }

    @Override
    public FieldDef getData() {

        return _field;
    }

    @Override
    public void setMapped(boolean mappedIn) {

        if (null != _field) {

            _mapped = mappedIn;
        }
    }

    @Override
    public boolean isMapped() {

        return _mapped;
    }

    public void setName(String nameIn) {

        if (null != _field) {

            _field.setFieldName(nameIn);
        }
    }

    public void setDataType(CsiDataType typeIn) {

        if (null != _field) {

            if (!_inUse) {

                _field.setValueType(typeIn);
            }
            _field.forceStorageType(typeIn);
        }
        _columnType = typeIn;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private String getBaseName(String nameIn) {

        return nameIn;
    }
}

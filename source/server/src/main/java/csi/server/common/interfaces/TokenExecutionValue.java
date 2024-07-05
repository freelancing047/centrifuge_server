package csi.server.common.interfaces;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 3/26/2015.
 */
public class TokenExecutionValue {

    private CsiDataType _dataType;
    private boolean _isStatic;
    private String _value;

    public TokenExecutionValue(CsiDataType dataTypeIn, boolean isStaticIn, String valueIn) {

        _dataType = dataTypeIn;
        _isStatic = isStaticIn;
        _value = valueIn;
    }

    public CsiDataType getDataType() {

        return _dataType;
    }

    public boolean isStatic() {

        return _isStatic;
    }

    public String getValue() {

        return _value;
    }
}

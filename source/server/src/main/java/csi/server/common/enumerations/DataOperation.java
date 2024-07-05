package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 1/5/2015.
 */
public enum DataOperation implements Serializable {

    UNSPECIFIED("unspecified operation"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    UNRECOGNIZED("unknown operation");

    private String _label;

    private DataOperation(String labelIn) {

        _label = labelIn;
    }

    public String getLabel() {

        return _label;
    }
}

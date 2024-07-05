package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 8/3/2017.
 */
public enum ComparingToken implements Serializable {

    GT(">", ">", "Greater Than"),
    GE(">=", ">=", "Greater Than or Equal"),
    EQ("=", "==", "Equal"),
    NE("<>", "!=", "Not Equal"),
    LE("<=", "<=", "Less Than or Equal"),
    LT("<", "<", "Less Than");

    private String _sqlSymbol;
    private String _symbol;
    private String _label;

    public String getSqlSymbol() {

        return _sqlSymbol;
    }

    public String getSymbol() {

        return _symbol;
    }

    public String getLabel() {

        return _label;
    }

    private ComparingToken(String sqlSymbolIn, String symbolIn, String labelIn) {

        _sqlSymbol = sqlSymbolIn;
        _symbol = symbolIn;
        _label = labelIn;
    }

    public ComparingToken getNextValue() {

        return ComparingToken.values()[((ordinal() + 1) % values().length)];
    }
}

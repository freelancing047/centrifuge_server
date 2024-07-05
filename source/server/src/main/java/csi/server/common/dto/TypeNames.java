package csi.server.common.dto;

import java.util.HashSet;

import csi.server.common.enumerations.CsiDataType;

public class TypeNames {
    public static final String AUTOINCREMENT = "INTEGER AUTO_INCREMENT";
    public static final String ARRAY = "ARRAY";
    public static final String BIGINT = "BIGINT";
    public static final String BINARY = "BINARY";
    public static final String BIT = "BIT";
    public static final String BLOB = "BLOB";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String CHAR = "CHAR";
    public static final String CHAR_5 = "CHAR(5)";
    public static final String CURRENCY = "CURRENCY";
    public static final String CLOB = "CLOB";
    public static final String DATALINK = "DATALINK";
    public static final String DATE = "DATE";
    public static final String DECIMAL = "DECIMAL";
    public static final String DISTINCT = "DISTINCT";
    public static final String DOUBLE = "DOUBLE";
    public static final String FLOAT = "FLOAT";
    public static final String INTEGER = "INTEGER";
    public static final String JAVA_OBJECT = "JAVA_OBJECT";
    public static final String LONG = "LONG";
    public static final String LONGVARBINARY = "LONGVARBINARY";
    public static final String LONGVARCHAR = "LONGVARCHAR";
    public static final String NULL = "NULL";
    public static final String NUMBER = "NUMBER";
    public static final String NUMERIC = "NUMERIC";
    public static final String OTHER = "OTHER";
    public static final String PERCENT = "PERCENT_OF_SUM"; // a display format name, not a JDBC data-type name
    public static final String REAL = "REAL";
    public static final String REF = "REF";
    public static final String SHORT = "SHORT";
    public static final String SMALLINT = "SMALLINT";
    public static final String STRUCT = "STRUCT";
    public static final String TIME = "TIME";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String TINYINT = "TINYINT";
    public static final String VARBINARY = "VARBINARY";
    public static final String VARCHAR = "VARCHAR";
    public static final String VARCHAR2 = "VARCHAR2";

    private static HashSet<String> numericTypes;

    static {
        numericTypes = new HashSet<String>();

        numericTypes.add(BIGINT);
        numericTypes.add(DECIMAL);
        numericTypes.add(DOUBLE);
        numericTypes.add(FLOAT);
        numericTypes.add(INTEGER);
        numericTypes.add(LONG);
        numericTypes.add(NUMBER);
        numericTypes.add(NUMERIC);
        numericTypes.add(REAL);
        numericTypes.add(SHORT);
        numericTypes.add(SMALLINT);
        numericTypes.add(TINYINT);
    }

    public static boolean isNumeric(String typeName) {
        return numericTypes.contains((typeName == null) ? null : typeName.toUpperCase());
    }

    public static boolean isNumeric(CsiDataType typeName) {
        return (CsiDataType.Integer == typeName) || (CsiDataType.Number == typeName);
    }
}

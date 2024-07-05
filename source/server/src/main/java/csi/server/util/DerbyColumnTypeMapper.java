package csi.server.util;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.dto.TypeNames;
import csi.server.common.enumerations.CsiDataType;

/* 
 * This class contains utility functions relating to Derby Column types.  In
 * general, this handles cases where we need to modify types to conform to 
 * what is supported by Derby.
 */

@SuppressWarnings("unchecked")
public class DerbyColumnTypeMapper {
    protected static final Logger LOG = LogManager.getLogger(DerbyColumnTypeMapper.class);

    // TODO: All these settings should be externalized into server config

    @SuppressWarnings("unchecked")
    private static Map<String, CsiDataType> desktopCentrifugeTypeMap;
    private static Map<String, CsiDataType> jdbcToCentrifugeTypeMap;
    private static Map<Integer, CsiDataType> jdbcCodeToCentrifugeTypeMap;
    private static Map<String, String> makoToCastTypeMap;
    private static Map<String, Class> makoToJavaTypeMap;
    private static final String _defaultCastType = "text( %1$s )";
    private static final Class _defaultJavaType = String.class;

    static {
        // desktop to Mako type
        desktopCentrifugeTypeMap = new HashMap<String, CsiDataType>();
        desktopCentrifugeTypeMap.put("varchar", CsiDataType.String);
        desktopCentrifugeTypeMap.put("integer", CsiDataType.Integer);
        desktopCentrifugeTypeMap.put("smallint", CsiDataType.Integer);
        desktopCentrifugeTypeMap.put("int", CsiDataType.Integer);
        desktopCentrifugeTypeMap.put("bigint", CsiDataType.Integer);
        desktopCentrifugeTypeMap.put("float", CsiDataType.Number);
        desktopCentrifugeTypeMap.put("real", CsiDataType.Number);
        desktopCentrifugeTypeMap.put("double", CsiDataType.Number);
        desktopCentrifugeTypeMap.put("decimal", CsiDataType.Number);
        desktopCentrifugeTypeMap.put("currency", CsiDataType.Number);
        desktopCentrifugeTypeMap.put("datetime", CsiDataType.DateTime);
        desktopCentrifugeTypeMap.put("date", CsiDataType.Date);
        desktopCentrifugeTypeMap.put("time", CsiDataType.Time);
        desktopCentrifugeTypeMap.put("char", CsiDataType.String);

        jdbcToCentrifugeTypeMap = new HashMap<String, CsiDataType>();
        jdbcToCentrifugeTypeMap.put(TypeNames.BIGINT, CsiDataType.Integer);
        jdbcToCentrifugeTypeMap.put(TypeNames.BIT, CsiDataType.Boolean);
        jdbcToCentrifugeTypeMap.put(TypeNames.BOOLEAN, CsiDataType.Boolean);
        jdbcToCentrifugeTypeMap.put(TypeNames.CHAR, CsiDataType.String);
        jdbcToCentrifugeTypeMap.put(TypeNames.CHAR_5, CsiDataType.String);
        jdbcToCentrifugeTypeMap.put(TypeNames.CURRENCY, CsiDataType.Number);
        jdbcToCentrifugeTypeMap.put(TypeNames.DATE, CsiDataType.Date);
        jdbcToCentrifugeTypeMap.put(TypeNames.DECIMAL, CsiDataType.Number);
        jdbcToCentrifugeTypeMap.put(TypeNames.DOUBLE, CsiDataType.Number);
        jdbcToCentrifugeTypeMap.put(TypeNames.FLOAT, CsiDataType.Number);
        jdbcToCentrifugeTypeMap.put(TypeNames.INTEGER, CsiDataType.Integer);
        jdbcToCentrifugeTypeMap.put(TypeNames.LONG, CsiDataType.Integer);
        jdbcToCentrifugeTypeMap.put(TypeNames.NUMBER, CsiDataType.Number);
        jdbcToCentrifugeTypeMap.put(TypeNames.NUMERIC, CsiDataType.Number);
        jdbcToCentrifugeTypeMap.put(TypeNames.REAL, CsiDataType.Number);
        jdbcToCentrifugeTypeMap.put(TypeNames.SHORT, CsiDataType.Integer);
        jdbcToCentrifugeTypeMap.put(TypeNames.SMALLINT, CsiDataType.Integer);
        jdbcToCentrifugeTypeMap.put(TypeNames.TIME, CsiDataType.Time);
        jdbcToCentrifugeTypeMap.put(TypeNames.TIMESTAMP, CsiDataType.DateTime);
        jdbcToCentrifugeTypeMap.put(TypeNames.TINYINT, CsiDataType.Integer);
        jdbcToCentrifugeTypeMap.put(TypeNames.VARCHAR, CsiDataType.String);
        jdbcToCentrifugeTypeMap.put(TypeNames.VARCHAR2, CsiDataType.String);

        jdbcToCentrifugeTypeMap.put(TypeNames.LONGVARCHAR, CsiDataType.String);
        jdbcToCentrifugeTypeMap.put(TypeNames.LONGVARBINARY, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.ARRAY, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.BINARY, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.BLOB, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.CLOB, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.DATALINK, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.DISTINCT, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.JAVA_OBJECT, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.STRUCT, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.VARBINARY, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.REF, CsiDataType.Unsupported);
        jdbcToCentrifugeTypeMap.put(TypeNames.OTHER, CsiDataType.Unsupported);

        jdbcCodeToCentrifugeTypeMap = new HashMap<Integer, CsiDataType>();
        jdbcCodeToCentrifugeTypeMap.put(Types.BIGINT, CsiDataType.Integer);
        jdbcCodeToCentrifugeTypeMap.put(Types.BIT, CsiDataType.Boolean);
        jdbcCodeToCentrifugeTypeMap.put(Types.BOOLEAN, CsiDataType.Boolean);
        jdbcCodeToCentrifugeTypeMap.put(Types.CHAR, CsiDataType.String);
        jdbcCodeToCentrifugeTypeMap.put(Types.DATE, CsiDataType.Date);
        jdbcCodeToCentrifugeTypeMap.put(Types.DECIMAL, CsiDataType.Number);
        jdbcCodeToCentrifugeTypeMap.put(Types.DOUBLE, CsiDataType.Number);
        jdbcCodeToCentrifugeTypeMap.put(Types.FLOAT, CsiDataType.Number);
        jdbcCodeToCentrifugeTypeMap.put(Types.INTEGER, CsiDataType.Integer);
        jdbcCodeToCentrifugeTypeMap.put(Types.NUMERIC, CsiDataType.Number);
        jdbcCodeToCentrifugeTypeMap.put(Types.REAL, CsiDataType.Number);
        jdbcCodeToCentrifugeTypeMap.put(Types.SMALLINT, CsiDataType.Integer);
        jdbcCodeToCentrifugeTypeMap.put(Types.TIME, CsiDataType.Time);
        jdbcCodeToCentrifugeTypeMap.put(Types.TIMESTAMP, CsiDataType.DateTime);
        jdbcCodeToCentrifugeTypeMap.put(Types.TINYINT, CsiDataType.Integer);
        jdbcCodeToCentrifugeTypeMap.put(Types.VARCHAR, CsiDataType.String);

        jdbcCodeToCentrifugeTypeMap.put(Types.LONGVARCHAR, CsiDataType.String);
        jdbcCodeToCentrifugeTypeMap.put(Types.LONGVARBINARY, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.ARRAY, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.BINARY, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.BLOB, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.CLOB, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.DATALINK, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.DISTINCT, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.JAVA_OBJECT, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.STRUCT, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.VARBINARY, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.REF, CsiDataType.Unsupported);
        jdbcCodeToCentrifugeTypeMap.put(Types.OTHER, CsiDataType.Unsupported);

        // Mako to CAST type
        makoToCastTypeMap = new HashMap<String, String>();
        makoToCastTypeMap.put("string", _defaultCastType);
        makoToCastTypeMap.put("integer", "int8( %1$s )");// TypeNames.BIGINT );
        makoToCastTypeMap.put("number", "float8( %1$s )");// TypeNames.DOUBLE );
        makoToCastTypeMap.put("date", "date( %1$s )");
        makoToCastTypeMap.put("time", "cast( %1$s as time without time zone)");
        makoToCastTypeMap.put("datetime", "cast( %1$s as timestamp)");
        makoToCastTypeMap.put("boolean", "cast( %1$s as boolean)");

        // mako to java type
        makoToJavaTypeMap = new HashMap<String, Class>();
        makoToJavaTypeMap.put("string", _defaultJavaType);
        makoToJavaTypeMap.put("integer", Long.class);
        makoToJavaTypeMap.put("number", Double.class);
        makoToJavaTypeMap.put("date", java.sql.Date.class);
        makoToJavaTypeMap.put("time", java.sql.Time.class);
        makoToJavaTypeMap.put("datetime", java.sql.Timestamp.class);
        makoToJavaTypeMap.put("boolean", Boolean.class);
    }

    public static CsiDataType jdbcToCentrifugeType(String jdbcType) {
        if (jdbcType == null) {
            return CsiDataType.String;
        }
        CsiDataType t = jdbcToCentrifugeTypeMap.get(jdbcType.toUpperCase());
        if (t == null) {
           LOG.debug("Unsupported jdbc data type: " + jdbcType);
            return CsiDataType.Unsupported;
        } else {
            return t;
        }
    }

    public static CsiDataType getCsiTypeFromJdbcType(int jdbcTypeIn) {
        return jdbcCodeToCentrifugeTypeMap.get(jdbcTypeIn);
    }

    public static CsiDataType jdbcCodeToCentrifugeType(int jdbcType) {
        CsiDataType t = jdbcCodeToCentrifugeTypeMap.get(jdbcType);
        if (t == null) {
           LOG.debug("Unsupported jdbc data type: " + jdbcType);
            return CsiDataType.Unsupported;
        } else {
            return t;
        }
    }

    public static CsiDataType desktopToCentrifugeType(String desktopType) {
        if (desktopType == null) {
            return CsiDataType.String;
        }
        CsiDataType t = desktopCentrifugeTypeMap.get(desktopType.trim().toLowerCase());
        if (t == null) {
           LOG.debug("Unsupported desktop data type: " + desktopType);
            return CsiDataType.Unsupported;
        } else {
            return t;
        }
    }

    /**
     * Determine whether or not a field's value should be quoted in an SQL
     * statement.
     * 
     * @param typeName
     *            name of Derby data type of field.
     * @return true iff field value should be quoted in query.
     */
    public static boolean typeValueShouldBeQuoted(String typeName) {

        typeName = typeName.toUpperCase();

        boolean valueIsQuoted;

        if (typeName.equals(TypeNames.VARCHAR)) {

            // varchar should be very common, so knock it out first.
            valueIsQuoted = true;

        } else if (typeName.equals(TypeNames.BIGINT)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.REAL)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.SMALLINT)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.TINYINT)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.INTEGER)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.FLOAT)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.DOUBLE)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.DECIMAL)) {

            valueIsQuoted = false;

        } else if (typeName.equals(TypeNames.NUMERIC)) {

            valueIsQuoted = false;

        } else {

            valueIsQuoted = true;
        }

        return valueIsQuoted;
    }

    public static boolean derbyFunctionShouldBeQuoted(String derbyFunction) {
        boolean quoteValue = true;

        if (derbyFunction.equalsIgnoreCase("year"))
            quoteValue = false;
        else if (derbyFunction.equalsIgnoreCase("month"))
            quoteValue = false;
        else if (derbyFunction.equalsIgnoreCase("day"))
            quoteValue = false;
        else if (derbyFunction.equalsIgnoreCase("hour"))
            quoteValue = false;
        else if (derbyFunction.equalsIgnoreCase("minute"))
            quoteValue = false;
        else if (derbyFunction.equalsIgnoreCase("ceiling"))
            quoteValue = false;
        else if (derbyFunction.equalsIgnoreCase("floor"))
            quoteValue = false;
        else if (derbyFunction.equalsIgnoreCase("length"))
            quoteValue = false;

        return quoteValue;
    }

    public static String getValueExpression(String type, String value, String derbyFunction) {
        if (derbyFunction == null || derbyFunction.length() == 0)
            return getValueExpression(type, value);

        String valueExpression = value;

        if (derbyFunctionShouldBeQuoted(derbyFunction)) {
            StringBuilder sb = new StringBuilder();
            sb.append('\'');
            sb.append(value);
            sb.append('\'');
            valueExpression = sb.toString();
        }

        return valueExpression;
    }

    /**
     * Return the literal value to be used in an SQL expression. For numeric and
     * boolean types, the value is not quoted, while for other types it is.
     * 
     * @param type
     *            Derby type name
     * @param value
     *            string representation of literal value
     * @return expression to be used in SQL statement: a quoted or unquoted
     *         value.
     */
    public static String getValueExpression(String type, String value) {

        String valueExpression;

        if (typeValueShouldBeQuoted(type)) {

            StringBuilder sb = new StringBuilder();
            sb.append('\'');
            sb.append(value);
            sb.append('\'');
            valueExpression = sb.toString();

        } else {

            valueExpression = value;
        }

        return valueExpression;
    }

    public static String jdbcToCacheType(int columnType) {
        String rtnType;

        switch (columnType) {
        case Types.ARRAY:
            rtnType = TypeNames.ARRAY;
            break;
        case Types.BIGINT:
            rtnType = TypeNames.BIGINT;
            break;
        case Types.BINARY:
            rtnType = TypeNames.BINARY;
            break;
        case Types.BIT:
            rtnType = TypeNames.BIT;
            break;
        case Types.BLOB:
            rtnType = TypeNames.BLOB;
            break;
        case Types.BOOLEAN:
            rtnType = TypeNames.BOOLEAN;
            break;
        case Types.CHAR:
            rtnType = TypeNames.CHAR;
            break;
        case Types.CLOB:
            rtnType = TypeNames.CLOB;
            break;
        case Types.DATALINK:
            rtnType = TypeNames.DATALINK;
            break;
        case Types.DATE:
            rtnType = TypeNames.TIMESTAMP;
            break;
        case Types.DECIMAL:
            rtnType = TypeNames.DECIMAL;
            break;
        case Types.DISTINCT:
            rtnType = TypeNames.DISTINCT;
            break;
        case Types.DOUBLE:
            rtnType = TypeNames.DOUBLE;
            break;
        case Types.FLOAT:
            rtnType = TypeNames.FLOAT;
            break;
        case Types.INTEGER:
            rtnType = TypeNames.INTEGER;
            break;
        case Types.JAVA_OBJECT:
            rtnType = TypeNames.JAVA_OBJECT;
            break;
        case Types.LONGVARBINARY:
            rtnType = TypeNames.LONGVARBINARY;
            break;
        case Types.LONGVARCHAR:
            rtnType = TypeNames.LONGVARCHAR;
            break;
        case Types.NULL:
            rtnType = TypeNames.NULL;
            break;
        case Types.NUMERIC:
            rtnType = TypeNames.DECIMAL;
            break;
        case Types.OTHER:
            rtnType = TypeNames.OTHER;
            break;
        case Types.REAL:
            rtnType = TypeNames.REAL;
            break;
        case Types.REF:
            rtnType = TypeNames.REF;
            break;
        case Types.SMALLINT:
            rtnType = TypeNames.INTEGER;
            break;
        case Types.STRUCT:
            rtnType = TypeNames.STRUCT;
            break;
        case Types.TIME:
            rtnType = TypeNames.TIMESTAMP;
            break;
        case Types.TIMESTAMP:
            rtnType = TypeNames.TIMESTAMP;
            break;
        case Types.TINYINT:
            rtnType = TypeNames.INTEGER;
            break;
        case Types.VARBINARY:
            rtnType = TypeNames.VARBINARY;
            break;
        case Types.VARCHAR:
            rtnType = TypeNames.VARCHAR;
            break;
        default:
            rtnType = TypeNames.VARCHAR;
            break;
        }

        return rtnType;
    }

    // TODO: remove these methods...
    // they are is referenced by pre-mako code
    // which should not be executing so if you see these exception
    // being thrown then there's definitely a bug.

    public static String mapJavaToJDBCType(Class columnType) {
        throw new RuntimeException("Deprecated method: mapJavaToJDBCType");
    }

    public static String convertDesktopType(String syntheticType) {
        throw new RuntimeException("Deprecated method: convertDesktopType");
    }

    public static String makoToCastType(String valueTypeIn) {
        
        String myCastType = null;
        
        if (valueTypeIn != null) {
            
            myCastType = makoToCastTypeMap.get(valueTypeIn.toLowerCase());
        }
        return (null != myCastType) ? myCastType :_defaultCastType;
    }

    public static Class makoToJavaClass(String valueTypeIn) {
        
        Class myJavaType = null;
        
        if (valueTypeIn != null) {
            
            myJavaType = makoToJavaTypeMap.get(valueTypeIn.toLowerCase());
        }
        return (null != myJavaType) ? myJavaType :_defaultJavaType;
    }
}

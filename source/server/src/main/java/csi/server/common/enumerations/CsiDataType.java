/**
 *
 */
package csi.server.common.enumerations;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.shared.core.util.HasLabel;

public enum CsiDataType implements Serializable, HasLabel {
    // IMPORTANT DO NOT REMOVE OR CHANGE THE ORDER OF THESE ENTRIES!
    String("string", "String", "TEXT", "VARCHAR", CsiBaseDataType.TEXT, 12), // java.sql.Types.VARCHAR
    Boolean("boolean", "Boolean", "BOOLEAN", "BOOLEAN", CsiBaseDataType.BOOLEAN, 16), // java.sql.Types.BOOLEAN
    Integer("integer", "Integer", "BIGINT", "BIGINT", CsiBaseDataType.NUMERIC, -5), // java.sql.Types.BIGINT
    Number("number", "Float", "DOUBLE PRECISION", "DOUBLE", CsiBaseDataType.NUMERIC, 8), // java.sql.Types.DOUBLE
    DateTime("datetime", "Date-Time", "TIMESTAMP", "TIMESTAMP", CsiBaseDataType.TEMPORAL, 93), // java.sql.Types.TIMESTAMP
    Date("date", "Date", "DATE", "DATE", CsiBaseDataType.TEMPORAL, 91), // java.sql.Types.DATE
    Time("time", "Time", "TIME", "TIME", CsiBaseDataType.TEMPORAL, 92), // java.sql.Types.TIME
    Unsupported("unsupported", "Unsupported", "TEXT", null, null, 12); // java.sql.Types.VARCHAR

    private static final String[] coercionList = new String[]{

            "cast_string",
            "cast_boolean",
            "cast_integer",
            "cast_double",
            "cast_datetime",
            "cast_date",
            "cast_time",
            "cast_string"
    };

    private static final String[] nullList = new String[]{

            "null::TEXT",
            "null::BOOLEAN",
            "null::BIGINT",
            "null::FLOAT8",
            "null::TIMESTAMP",
            "null::DATE",
            "null::TIME",
            "null::TEXT"
    };

    private static String[] _i18nLabels = null;
    private static Map<String, CsiDataType> _i18nValues = new TreeMap<String, CsiDataType>();
    private static Map<String, CsiDataType> _lookupMap = new HashMap<String, CsiDataType>();

    static {
        _i18nValues.put(CsiDataType.String.getInitialLabel().toLowerCase(), CsiDataType.String);
        _i18nValues.put(CsiDataType.Boolean.getInitialLabel().toLowerCase(), CsiDataType.Boolean);
        _i18nValues.put(CsiDataType.Integer.getInitialLabel().toLowerCase(), CsiDataType.Integer);
        _i18nValues.put(CsiDataType.Number.getInitialLabel().toLowerCase(), CsiDataType.Number);
        _i18nValues.put(CsiDataType.DateTime.getInitialLabel().toLowerCase(), CsiDataType.DateTime);
        _i18nValues.put(CsiDataType.Date.getInitialLabel().toLowerCase(), CsiDataType.Date);
        _i18nValues.put(CsiDataType.Time.getInitialLabel().toLowerCase(), CsiDataType.Time);

        _lookupMap.put("string", String);
        _lookupMap.put("char", String);
        _lookupMap.put("nchar", String);
        _lookupMap.put("char_5", String);
        _lookupMap.put("varchar", String);
        _lookupMap.put("nvarchar", String);
        _lookupMap.put("varchar2", String);
        _lookupMap.put("longvarchar", String);
        _lookupMap.put("long varchar", String);
        _lookupMap.put("text", String);

        _lookupMap.put("boolean", Boolean);
        _lookupMap.put("bool", Boolean);
        _lookupMap.put("bit", Boolean);

        _lookupMap.put("integer", Integer);
        _lookupMap.put("int4", Integer);
        _lookupMap.put("int8", Integer);
        _lookupMap.put("int", Integer);
        _lookupMap.put("bigint", Integer);
        _lookupMap.put("smallint", Integer);
        _lookupMap.put("tinyint", Integer);
        _lookupMap.put("long", Integer);
        _lookupMap.put("short", Integer);

        _lookupMap.put("number", Number);
        _lookupMap.put("currency", Number);
        _lookupMap.put("decimal", Number);
        _lookupMap.put("double", Number);
        _lookupMap.put("float", Number);
        _lookupMap.put("float4", Number);
        _lookupMap.put("float8", Number);
        _lookupMap.put("real", Number);
        _lookupMap.put("numeric", Number);

        _lookupMap.put("datetime", DateTime);
        _lookupMap.put("timestamp", DateTime);

        _lookupMap.put("date", Date);

        _lookupMap.put("time", Time);
        _lookupMap.put("unsupported", Unsupported);
    }

    private String _legacyType;
    private String _label;
    private String _sqlType;
    private String _jdbcType;
    private CsiBaseDataType _baseType;
    private int _pgType;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    public static void setI18nValues(TreeMap<String, CsiDataType> i18nValuesIn) {

        _i18nValues = i18nValuesIn;
    }

    private CsiDataType(String legacyTypeIn, String labelIn, String sqlTypeIn, String jdbcTypeIn, CsiBaseDataType baseTypeIn, int pgTypeIn) {

        _legacyType = legacyTypeIn;
        _label = labelIn;
        _sqlType = sqlTypeIn;
        _jdbcType = jdbcTypeIn;
        _baseType = baseTypeIn;
        _pgType = pgTypeIn;
    }

    public String getLegacyType() {
        return _legacyType;
    }

    public String getInitialLabel() {
    	return _label;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public static CsiDataType getValue(String labelIn) {

        CsiDataType myValue = (null != labelIn) ? _i18nValues.get(labelIn.toLowerCase()) : null;

        return (null != myValue) ? myValue : Unsupported;
    }

    public static Collection<CsiDataType> sortedValuesByLabel() {

        return (null != _i18nValues) ? _i18nValues.values() : null;
    }

    public String getSqlType() {

        return _sqlType;
    }

    public String getJdbcType() {

        return _jdbcType;
    }

    public static String toString(CsiDataType type) {
        if (type == null) {
            return null;
        }
        return type.getLegacyType();
    }

   public boolean isTemporal() {
      return (CsiBaseDataType.TEMPORAL == _baseType);
   }

   public boolean isNumeric() {
      return (CsiBaseDataType.NUMERIC == _baseType);
   }

   public int getPgType() {
      return _pgType;
   }

   public static String getCoercion(String toTypeIn) {
      return getCoercion(getMatchingType(toTypeIn));
   }

   public static String getCoercion(CsiDataType fromTypeIn, CsiDataType toTypeIn) {
      String myCoercion = null;

      if ((toTypeIn != null) && (toTypeIn != fromTypeIn)) {
         myCoercion = getCoercion(toTypeIn);
      }
      return myCoercion;
   }

    public static String getCacheNull(CsiDataType toTypeIn) {

        return (null != toTypeIn) ? nullList[toTypeIn.ordinal()] : nullList[0];
    }

    public static String getCoercion(CsiDataType toTypeIn) {

        String myCoercion = null;

        if (null != toTypeIn) {

            int myTo = toTypeIn.ordinal();

            if ((0 <= myTo) && (CsiDataType.Unsupported.ordinal() > myTo)) {

                myCoercion = coercionList[myTo];
            }
        }
        return myCoercion;
    }

    public static CsiDataType getMatchingType(String typeNameIn) {

        String myTypeName = trimTypeName(typeNameIn);

        return ((null != myTypeName) && (0 < typeNameIn.length())) ? _lookupMap.get(myTypeName.toLowerCase()) : null;
    }

   public static String trimTypeName(String typeNameIn) {
      String myTypeName = typeNameIn;

      if (typeNameIn != null) {
         myTypeName = myTypeName.trim();
         int myParenOffset = typeNameIn.indexOf('(');
         myTypeName = (0 < myParenOffset)
                        ? myTypeName.substring(0, myParenOffset)
                        : myTypeName;

         if (_lookupMap.get(myTypeName.toLowerCase()) == null) {
            int mySpaceOffset = myTypeName.indexOf(' ');

            if ((0 < mySpaceOffset) && ((0 > myParenOffset) || (myParenOffset > mySpaceOffset))) {
               myTypeName = myTypeName.substring(0, mySpaceOffset);
            }
         }
         myTypeName = (myTypeName.length() > 0) ? myTypeName : null;
      }
      return myTypeName;
   }

    public static int getDataTypeMask(CsiDataType[] arrayIn) {

        int myMask = 0;

        if (null != arrayIn) {

            for (int i = 0; arrayIn.length > i; i++) {

                CsiDataType myType = arrayIn[i];

                if (null != myType) {

                    myMask |= (1 << myType.ordinal());
                }
            }
        }
        return myMask;
    }

    public int getMask() {

        return 1 << ordinal();
    }

    public static int getDataTypeMask(List<CsiDataType> listIn) {

        int myMask = 0;

        if (null != listIn) {

            for (int i = 0; listIn.size() > i; i++) {

                CsiDataType myType = listIn.get(i);

                if (null != myType) {

                    myMask |= (1 << myType.ordinal());
                }
            }
        }
        return myMask;
    }

    public CsiDataType getNextValue() {

        return CsiDataType.values()[((ordinal() + 1) % (values().length - 1))];
    }
}
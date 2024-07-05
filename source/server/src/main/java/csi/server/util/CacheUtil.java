package csi.server.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.FieldDef;
import csi.server.common.model.Resource;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.connector.ConnectionFactory;
import csi.server.util.sql.CacheTokens;

public class CacheUtil {
//    private static final String CACHE_TABLE_BASE_PATTERN = "cache_xxxxxxxx_xxxx_xxxx_xxxx_xxxxxxxxxxxx_";
//    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    public static final String INTERNAL_ID_NAME = CacheTokens.CSI_ROW_ID;
    public static final String INTERNAL_STATEID = CacheTokens.CSI_GROUP_ID;

    public static final String TAG_TABLE_PREFIX = "tag_";
    public static final String MARKING_TABLE_PREFIX = "capco_";
    public static final String CACHE_TABLE_PREFIX = "cache_";
    public static final String INDEX_TABLE_PREFIX = "index_";
    public static final String IDENTITY_TABLE_SUFFIX = "_internal_id_seq";
    public static final String EXAMPLE_UUID_STRING = CsiUUID.randomUUID();

    public static final String[] TABLE_PREFIX_LIST = {CACHE_TABLE_PREFIX, INDEX_TABLE_PREFIX, MARKING_TABLE_PREFIX};

    public static void trackErrors(Map<String, List<String>> warnings, long rowcnt, String colname, Throwable e) {
        List<String> errorRows = warnings.get(colname);
        if (errorRows == null) {
            errorRows = new ArrayList<String>();
            warnings.put(colname, errorRows);
        }
        if (errorRows.size() < 10) {
            errorRows.add(rowcnt + 1 + " - " + CsiUtil.getStackTraceString(e));
        }
    }

   public static String buildLoadWarningMsg(Map<String,List<String>> warnings) {
      String result = "";

      if ((warnings != null) && !warnings.isEmpty()) {
         StringBuilder buf = new StringBuilder("Encountered invalid data values: ");

         for (Map.Entry<String,List<String>> entry : warnings.entrySet()) {
            buf.append("\n\tcolumn '").append(entry.getKey()).append("' on rows: \n\t\t")
               .append(CsiUtil.toDelimitedString(entry.getValue(), ",\n\t\t"));
         }
         result = buf.toString();
      }
      return result;
   }

   public static String encodeCsv(Object object) {
      String result = null;

      if (object != null) {
         String text = null;

         if (object instanceof java.sql.Time) {
            text = DateTimeFormatter.ISO_LOCAL_TIME.format(((java.sql.Time) object).toLocalTime());
         } else if (object instanceof java.sql.Date) {
            text = DateTimeFormatter.ISO_LOCAL_DATE.format(((java.sql.Date) object).toLocalDate());
         } else if (object instanceof java.sql.Timestamp) {
            text = DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER.format(((java.sql.Timestamp) object).toLocalDateTime());
         } else if (object instanceof Date) {
            text = DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER.format(
                      LocalDateTime.ofInstant(Instant.ofEpochMilli(((java.util.Date) object).getTime()), ZoneId.systemDefault()));
         } else {
            text = object.toString();
         }
         StringBuilder buffer = new StringBuilder(text.length() + 2);

         buffer.append('"');

         if (text.indexOf('"') == -1) {
            buffer.append(text);
         } else {
            char[] charArray = text.toCharArray();

            for (int i = 0; i < charArray.length; i++) {
               if (charArray[i] == '"') {
                  buffer.append("\"\"");
               } else {
                  buffer.append(charArray[i]);
               }
            }
         }
         buffer.append('"');
         result = buffer.toString();
      }
      return result;
   }

    public static List<String> toDbUuid(List<String> uuidListIn) {

        List<String> myList = new ArrayList<String>();

        if (uuidListIn != null) {

            for (String myUuid : uuidListIn) {

                if (null != myUuid) {

                    myList.add(toDbUuid(myUuid));
                }
            }
        }
        return myList.isEmpty() ? null : myList;
    }

    public static String toDbUuid(String uuid) {
        return (null != uuid) ? uuid.replace('-', '_') : null;
    }

    public static String fromDbUuid(String uuid) {
        return (null != uuid) ? uuid.replace('_', '-') : null;
    }

    public static String generateInstalledTableName() {
        return InstalledTable.generateInstalledTableName();
    }

    public static String extractMarkingTableName(String tableNameIn) {

        if (MARKING_TABLE_PREFIX.equals(tableNameIn.substring(0, MARKING_TABLE_PREFIX.length()))) {

            return tableNameIn;
        }
        return getMarkingTableName(extractUuid(tableNameIn));
    }

    public static String extractTagTableName(String tableNameIn) {

        if (TAG_TABLE_PREFIX.equals(tableNameIn.substring(0, TAG_TABLE_PREFIX.length()))) {

            return tableNameIn;
        }
        return getMarkingTableName(extractUuid(tableNameIn));
    }

    public static String getMarkingTableName(Resource resourceIn) {
        return getMarkingTableName(resourceIn.getUuid());
    }

    public static String getMarkingTableName(String dvUuid) {
        return MARKING_TABLE_PREFIX + toDbUuid(dvUuid);
    }

    public static String getCacheTableName(String dvUuid) {
        return CACHE_TABLE_PREFIX + toDbUuid(dvUuid);
    }

    public static String getCacheTableName(String dvUuid, int generationIn) {
        return CACHE_TABLE_PREFIX + toDbUuid(dvUuid) + "_" + Integer.toString(generationIn);
    }

    public static String getColumnViewName(String dvUuid) {
        return getCacheTableName(dvUuid) + "_col";
    }

    public static String getColumnViewName(String dvUuid, int generationIn) {
        return getCacheTableName(dvUuid, generationIn) + "_col";
    }

    public static String getColumnViewNameFromTable(String tableNameIn) {
        if (null != tableNameIn) {
            return tableNameIn + "_col";
        } else {
            return null;
        }
    }

    public static String getTableNameFromColumnView(String viewNameIn) {
        if ((null != viewNameIn) && (4 < viewNameIn.length())) {
            return viewNameIn.substring(0, viewNameIn.length() - 4);
        } else {
            return null;
        }
    }

   public static String extractUuid(String tableNameIn) {
      String myUuid = null;

      if (tableNameIn != null) {
         if ((EXAMPLE_UUID_STRING.length() == tableNameIn.length()) && ('-' == tableNameIn.charAt(8))) {
            myUuid = tableNameIn;
         } else {
            for (int i = 0; i < TABLE_PREFIX_LIST.length; i++) {
               String myPrefix = TABLE_PREFIX_LIST[i];

               if (((myPrefix.length() + EXAMPLE_UUID_STRING.length()) <= tableNameIn.length())
                     && myPrefix.equals(tableNameIn.substring(0, myPrefix.length()))) {

                  myUuid = fromDbUuid(tableNameIn.substring(myPrefix.length(),
                                      myPrefix.length() + EXAMPLE_UUID_STRING.length()));
                  break;
               }
            }
         }
      }
      return myUuid;
   }

    public static String getQuotedColumnName(FieldDef fieldIn) {

        return toQuotedDbUuid(fieldIn.getLocalId());
    }

    public static String getQuotedColumnName(FieldDef fieldIn, String tableAliasIn) {

        if ((null != tableAliasIn) && (0 < tableAliasIn.length())) {

            return getQuotedPrefix(tableAliasIn) + getQuotedColumnName(fieldIn);

        } else {

            return getQuotedColumnName(fieldIn);
        }
    }

    public static String getQuotedPrefix(String prefixIn) {

        if ((null != prefixIn) && (0 < prefixIn.length())) {

            return toQuotedDbUuid(prefixIn) + ".";
        }
        return "";
    }

    public static String getColumnName(FieldDef fieldIn) {

        return toDbUuid(fieldIn.getLocalId());
    }

    public static String getGeneratorNameForCacheTable(String tableNameIn) {
        return tableNameIn.replace('.', '_').replace(' ', '_').toLowerCase() + IDENTITY_TABLE_SUFFIX;
    }

	public static String getQuotedCacheTableName(String dvUuid) {
		return Format.value(CACHE_TABLE_PREFIX + toDbUuid(dvUuid));
	}

    public static String getCacheTablePattern(String dvUuid) {
        return "'" + CACHE_TABLE_PREFIX + toDbUuid(dvUuid) + "%'";
    }

    public static String quote(String stringIn) {

        return ('"' == stringIn.charAt(0)) ? stringIn : "\"" + stringIn + "\"";
    }

    public static String quotePrefix(String stringIn) {

        return quote(stringIn) + ".";
    }

    public static String quoteAndEscape(String stringIn) {

        StringBuilder myBuffer = new StringBuilder();
        char[] myArray = stringIn.toCharArray();

        myBuffer.append('"');
        for (int i = 0; myArray.length > i; i++) {

            char myChar = myArray[i];

            if (('\\' == myChar) || ('"' == myChar)) {

                myBuffer.append(myChar);
            }
            myBuffer.append(myChar);
        }
        myBuffer.append('"');
        return myBuffer.toString();
    }

    public static String toQuotedDbUuid(String uuid) {
        return Format.value(toDbUuid(uuid));
    }

    public static String castExpression(String expresionIn, String sourceTypeIn, String targetTypeIn) {

        String myTemplate = null;

        if ((null != expresionIn) && (null != targetTypeIn) && (!targetTypeIn.equalsIgnoreCase(sourceTypeIn))) {

            myTemplate = DerbyColumnTypeMapper.makoToCastType(targetTypeIn);
        }
        return (null != myTemplate) ? String.format(myTemplate, expresionIn) : expresionIn;
    }

    public static String makeAliasedCastExpression(FieldDef fieldIn, String aliasIn) {
        return coerceField(fieldIn, fieldIn.getValueType(), aliasIn);
    }

    public static String makeCastExpression(FieldDef fieldIn) {
        return coerceField(fieldIn, fieldIn.getValueType(), null);
    }

    public static String makeColumnNameStringFromTable(Table table) {
        StringBuilder dataColumns = new StringBuilder();
        int i = 0;
        for (Column col : table.getColumns()) {
            if (col.getName().startsWith("internal_")) {
                continue;
            }
            if (i > 0) {
                dataColumns.append(',');
            }
            dataColumns.append(SqlUtil.quote(col.getName()));
            i++;
        }
        return dataColumns.toString();
    }

   public static String makeColumnNameStringFromFields(List<ValuePair<FieldDef,Integer>> processFieldsIn) {
      return processFieldsIn.stream().map(v -> getQuotedColumnName(v.getValue1())).collect(Collectors.joining(", "));
   }

    public static String resolveCacheType(String typeName, int typeCode, ConnectionFactory factoryIn) {
        CsiDataType csiType = resolveCsiType(typeName, typeCode, factoryIn);
        return (csiType == null) ? null : csiType.getJdbcType();
    }

    public static CsiDataType resolveCsiType(String typeNameIn, int typeCodeIn, ConnectionFactory factoryIn) {

        CsiDataType myCsiType = DerbyColumnTypeMapper.getCsiTypeFromJdbcType(typeCodeIn);

        if (null == myCsiType) {

            if (null != typeNameIn) {

                String myTypeName = CsiDataType.trimTypeName(typeNameIn);

                if (null != myTypeName) {

                    if (null != factoryIn) {

                        // check for type override in driver settings
                        myCsiType = factoryIn.getMappedCsiType(myTypeName);
                    }
                    if (null == myCsiType) {

                        myCsiType = CsiDataType.getMatchingType(myTypeName);
                    }
                }
            }
        }
        return myCsiType;
    }

    public static boolean isNumeric(String cacheType) {
        CsiDataType type = CsiDataType.getMatchingType(cacheType);
        return ((type == CsiDataType.Integer) || (type == CsiDataType.Number));
    }

    public static void singleQuote(StringBuilder bufferIn, char characterIn) {

        bufferIn.append('\'');

        if ('\'' == characterIn) {

            bufferIn.append('\'');
        }
        bufferIn.append(characterIn);
        bufferIn.append('\'');
    }

    public static void singleQuote(StringBuilder bufferIn, String stringIn) {

        bufferIn.append('\'');
        int len = stringIn.length();

        for (int i = 0; i < len; i++) {

            char myCharacter = stringIn.charAt(i);

            if ('\'' == myCharacter) {

                bufferIn.append('\'');
            }
            bufferIn.append(myCharacter);
        }

        bufferIn.append('\'');
    }

    private static String coerceField(FieldDef fieldIn, CsiDataType sourceTypeIn, String tableAliasIn) {

        StringBuilder myBuffer = new StringBuilder();

        coerceField(myBuffer, fieldIn, sourceTypeIn, tableAliasIn);

        return myBuffer.toString();
    }

    public static void coerceField(StringBuilder bufferIn, FieldDef fieldIn, CsiDataType sourceTypeIn, String tableAliasIn) {

        if (null != fieldIn) {

            CsiDataType mySourceType = (null != sourceTypeIn) ? sourceTypeIn : null;
            String myCoercion = ((null != mySourceType) && (mySourceType != fieldIn.getValueType()))
                    ? fieldIn.getCoercion(mySourceType) : null;
            if (null != myCoercion) {

                bufferIn.append(myCoercion);
                bufferIn.append("(");
                bufferIn.append(getQuotedColumnName(fieldIn, tableAliasIn));
                bufferIn.append(")");

            } else {

                bufferIn.append(getQuotedColumnName(fieldIn, tableAliasIn));
            }
        }
    }

    public static void coerceField(StringBuilder bufferIn, DataViewDef metaIn, FieldDef fieldIn, boolean aliasFieldIn, String tableAliasIn) {

        if (null != fieldIn) {

            ColumnDef myColumn = ((null != metaIn) && (null != fieldIn.getColumnLocalId()))
                    ? metaIn.getColumnByKey(fieldIn.getColumnKey()) : null;
            CsiDataType mySourceType = (null != myColumn) ? myColumn.getCsiType() : null;
            String myCoercion = ((null != mySourceType) && (mySourceType != fieldIn.getValueType()))
                    ? mySourceType.getCoercion(fieldIn.getValueType()) : null;

            if (null != myCoercion) {

                bufferIn.append(myCoercion);
                bufferIn.append("(");
                bufferIn.append(getQuotedColumnName(fieldIn, tableAliasIn));
                bufferIn.append(")");

                if (aliasFieldIn) {

                    bufferIn.append(" as ");
                    bufferIn.append(getQuotedColumnName(fieldIn));
                }

            } else {

                bufferIn.append(getQuotedColumnName(fieldIn, tableAliasIn));
            }
        }
    }

    public static void coerceColumn(StringBuilder bufferIn, DataViewDef metaIn, FieldDef fieldIn,
                                    Map<String, ValuePair<CsiDataType, Integer>> columnMapIn,
                                    boolean aliasFieldIn, String tableAliasIn) {

        if (null != fieldIn) {

            String myColumnName = getColumnName(fieldIn);
            ValuePair<CsiDataType, Integer> myColumnInfo = columnMapIn.get(myColumnName);
            CsiDataType mySourceType = (null != myColumnInfo) ? myColumnInfo.getValue1() : null;
            CsiDataType myTargetType = getColumnType(metaIn, fieldIn);
            String myCoercion = CsiDataType.getCoercion(mySourceType, myTargetType);

            if (null != myCoercion) {

                bufferIn.append(myCoercion);
                bufferIn.append("(");
                bufferIn.append(getQuotedColumnName(fieldIn, tableAliasIn));
                bufferIn.append(")");

                if (aliasFieldIn) {

                    bufferIn.append(" as ");
                    bufferIn.append(getQuotedColumnName(fieldIn));
                }

            } else {

                bufferIn.append(getQuotedColumnName(fieldIn, tableAliasIn));
            }
        }
    }

    public static CsiDataType getColumnType(DataViewDef metaDataIn, FieldDef fieldDefIn) {

        CsiDataType myDataType = null;

        if ((null != metaDataIn) && (null != fieldDefIn)) {

            ColumnDef myColumnDef = (null != fieldDefIn.getColumnLocalId())
                    ? metaDataIn.getColumnByKey(fieldDefIn.getColumnKey())
                    : null;

            myDataType = (null != myColumnDef) ? myColumnDef.getCsiType() : fieldDefIn.getValueType();
        }
        return myDataType;
    }

    public static Map<String, String> getColumnNameMap(List<InstalledColumn> columnsIn) {

        Map<String, String> myColumnMap = new TreeMap<String, String>();

        for (InstalledColumn myColumn : columnsIn) {

            myColumnMap.put(myColumn.getColumnName(), myColumn.getFieldName());
        }

        return myColumnMap;
    }

    public static ValuePair<String, String> getTableNamePair(InstalledTable tableIn) {

        return new ValuePair<String, String>(tableIn.getActiveTableName(), tableIn.getTablePath());
    }

    public static String replaceNames(String originalIn, ValuePair<String, String> tableNamePairIn,
                                      Map<String, String> columnNameMapIn) {

        StringBuilder myBuffer;
        final String myTableName = tableNamePairIn.getValue1();
        final int myTableNameSize = myTableName.length();
        final int myColumnNameSize = getColumnNameSize();
        int myBase = 0;

        myBuffer = new StringBuilder(originalIn);

        for (ValuePair<Integer, Integer> myPair = getNextWord(myBuffer, myBase);
             null != myPair; myPair = getNextWord(myBuffer, myBase)) {

            int myWordStart = myPair.getValue1();
            int myWordSize = myPair.getValue2();
            String myReturnName = null;

            myBase = myWordStart + myWordSize;

            if (myTableNameSize == myWordSize) {

                String myKey = myBuffer.substring(myWordStart, myWordStart + myWordSize);
                if (myTableName.equals(myKey)) {

                    myReturnName = tableNamePairIn.getValue2();
                }

            } else if (myColumnNameSize == myWordSize) {

                String myKey = myBuffer.substring(myWordStart, myWordStart + myWordSize);
                myReturnName = columnNameMapIn.get(myKey);
            }
            if (null != myReturnName) {

                myBuffer.replace(myWordStart, myWordStart + myWordSize, myReturnName);
                myBase = myWordStart + myReturnName.length();
            }
        }
        return myBuffer.toString();
    }

    public static Integer getColumnNameSize() {

        return EXAMPLE_UUID_STRING.length();
    }

    private static ValuePair<Integer, Integer> getNextWord(StringBuilder bufferIn, Integer offsetIn) {

        int myLimit = bufferIn.length();
        Integer myBase;
        Integer myEnd = myLimit;

        for (myBase = offsetIn; myLimit > myBase; myBase++) {

            if (isWordCharacter(bufferIn.charAt(myBase))) {

                break;
            }
        }

        if (myLimit > myBase) {

            for (myEnd = myBase; myLimit > myEnd; myEnd++) {

                if (!isWordCharacter(bufferIn.charAt(myEnd))) {

                    break;
                }
            }
        }

        return (myLimit > myBase) ? new ValuePair<Integer, Integer>(myBase, myEnd - myBase) : null;
    }

    private static boolean isWordCharacter(char charIn) {

        return ((('0' <= charIn) && ('9' >= charIn)) || (('a' <= charIn) && ('z' >= charIn)) || ('_' == charIn) || ('-' == charIn));
    }
}

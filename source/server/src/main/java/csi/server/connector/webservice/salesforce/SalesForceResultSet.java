package csi.server.connector.webservice.salesforce;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.RowSetMetaDataImpl;

import csi.server.common.dto.TypeNames;
import csi.server.common.enumerations.CsiDataType;
import csi.server.util.CsiTypeUtil;
import csi.server.util.DateUtil;

/*
 * Simple ResultSet implementation to cusor through the returned Salesforce objects directly.
 * The big assumption here is that Apache Axis was used to generate the object bindings for
 * the associated enterprise wsdl.  Based on these generated classes, we are doing method lookups
 * on the 'getters' to extract values for each field.  Axis uses a 'get' + field name notation.  So
 * for example, if we had a field firstname, the getter would be getFirstName.
 */
public class SalesForceResultSet implements ResultSet {

    private Object[] records;

    private int currentPosition = -1;

    private boolean closed = false;

    private String[] fields;

    // a little duplicative to have the fields in this map and in the separate array above
    private Map<String,Method> methods = new HashMap<String,Method>();

    protected RowSetMetaDataImpl rsmd;

    SalesForceResultSet(Object[] records, String query) throws SQLException {
        this.records = records;
        buildMetaData(query);
    }

    protected void buildMetaData(String query) throws SQLException {
        parseQuery(query);
        loadMethods();
        rsmd = new RowSetMetaDataImpl();
        rsmd.setColumnCount(fields.length);
        for (int i = 0; i < fields.length; i++) {
            rsmd.setColumnLabel(i + 1, fields[i]);
            rsmd.setColumnName(i + 1, fields[i]);
            rsmd.setColumnType(i + 1, Types.VARCHAR);
            rsmd.setColumnTypeName(i + 1, TypeNames.VARCHAR);
        }
    }

    /*
     * We use reflection, based on the field name(s) provided in the query to access the object's
     * members.  This means that we need to invoke the accessor.  Here we try to find a suitable
     * getter and associate it with the field.
     */
    private void loadMethods() {
        /*
         * Assumptions:
         * 1.  All of the objects (records) are the same type and structure - Probably OK
         * 2.  There are no nested objects.  Probably NOT OK longer term.
         */
        Object obj = records[0];
        Class cls = obj.getClass();
        Method[] meths = cls.getMethods();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            for (int j = 0; j < meths.length; j++) {
                Method m = meths[j];
                String methodName = m.getName();
                if (methodName.compareToIgnoreCase("get" + field) == 0) {
                    methods.put(field, m);
                    break;
                }
            }
        }
    }

    /*
     * In short, look at the select clause to get a list of the fields
     */
    private void parseQuery(String query) {
//        String regex = ",";
//        Pattern pattern = Pattern.compile(regex);
        String substr = query.substring(7);
        int idx = substr.indexOf("from");
        String fieldString = substr.substring(0, idx);
        System.out.println(fieldString);
        fields = fieldString.split(",");
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

    }

    @Override
   public boolean absolute(int row) throws SQLException {
        if (Math.abs(row) > records.length) {
            return false;
        }

        if (row < 0) {
            currentPosition = records.length - row;
        } else {
            currentPosition = row;
        }
        return true;
    }

    @Override
   public void afterLast() throws SQLException {
        currentPosition = records.length;
    }

    @Override
   public void beforeFirst() throws SQLException {
        currentPosition = 1;
    }

    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void close() throws SQLException {
        closed = true;

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {

        return 0;
    }

    @Override
    public boolean first() throws SQLException {

        return false;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {

        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {

        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {

        return false;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {

        return false;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {

        return 0;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {

        return 0;
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public int getConcurrency() throws SQLException {

        return 0;
    }

    @Override
    public String getCursorName() throws SQLException {

        return null;
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {

        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {

        return null;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {

        return 0;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {

        return 0;
    }

    @Override
    public int getFetchDirection() throws SQLException {

        return 0;
    }

    @Override
    public int getFetchSize() throws SQLException {

        return 0;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {

        return 0;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {

        return 0;
    }

    @Override
    public int getHoldability() throws SQLException {

        return 0;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {

        return 0;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {

        return 0;
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {

        return 0;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {

        return 0;
    }

    @Override
   public ResultSetMetaData getMetaData() throws SQLException {
        return rsmd;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

   @Override
   public Object getObject(int columnIndex) throws SQLException {
      Object result = null;

      try {
         Object obj = records[currentPosition];
         Method method = methods.get(fields[columnIndex - 1]);
         Object returnObj = method.invoke(obj, (Object[]) null);

         if (returnObj instanceof java.util.Date) {
            result = CsiTypeUtil.coerceString((java.util.Date) returnObj, DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
         } else if (returnObj instanceof Calendar) {
            result = CsiTypeUtil.coerceString((Calendar) returnObj, DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
         } else {
            result = CsiTypeUtil.coerceType(returnObj, CsiDataType.String, null);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

    @Override
    public Object getObject(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {

        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {

        return null;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public int getRow() throws SQLException {

        return 0;
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {

        return 0;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {

        return 0;
    }

    @Override
    public Statement getStatement() throws SQLException {

        return null;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {

        try {
            Object obj = records[currentPosition];
            Class cls = obj.getClass();
            Field fld = cls.getField(columnLabel);
            return fld.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {

        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {

        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {

        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {

        return null;
    }

    @Override
    public int getType() throws SQLException {

        return 0;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {

        return null;
    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
   public boolean isAfterLast() throws SQLException {

        return currentPosition > records.length;
    }

    @Override
   public boolean isBeforeFirst() throws SQLException {
        return currentPosition == 1;
    }

    @Override
    public boolean isClosed() throws SQLException {

        return false;
    }

    @Override
    public boolean isFirst() throws SQLException {

        return false;
    }

    @Override
    public boolean isLast() throws SQLException {

        return false;
    }

    @Override
    public boolean last() throws SQLException {

        return false;
    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public boolean next() throws SQLException {
        currentPosition++;
        return currentPosition < records.length;
    }

    @Override
    public boolean previous() throws SQLException {

        return false;
    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public boolean relative(int rows) throws SQLException {

        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {

        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {

        return false;
    }

    @Override
    public boolean rowUpdated() throws SQLException {

        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, NClob clob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob clob) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
   public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
   public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNString(int columnIndex, String string) throws SQLException {

    }

    @Override
    public void updateNString(String columnLabel, String string) throws SQLException {

    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {

    }

    @Override
    public void updateRow() throws SQLException {

    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

    }

    @Override
    public boolean wasNull() throws SQLException {

        return false;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {

        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {

        return null;
    }

}

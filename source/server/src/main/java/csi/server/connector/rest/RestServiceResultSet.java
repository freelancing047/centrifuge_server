package csi.server.connector.rest;

import java.io.InputStream;
import java.io.Reader;
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
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.RowSetMetaDataImpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RestServiceResultSet implements ResultSet {
   protected static final Logger LOG = LogManager.getLogger(RestServiceResultSet.class);

   protected static String ID = "RestServiceResultSet";

    interface IValueRetrieval {

        Object getValue(Node container);
    }

    class AttributeRetrieval implements IValueRetrieval {

        String name;

        AttributeRetrieval(String name) {
            this.name = name;
        }

        public Object getValue(Node container) {
            NamedNodeMap attributes = container.getAttributes();
            Node attr = attributes.getNamedItem(name);

            return (attr == null) ? null : attr.getNodeValue();
        }

    }

   class ElementRetrieval implements IValueRetrieval {
      private String name;

      public ElementRetrieval(String name) {
         this.name = name;
      }

      public Object getValue(Node container) {
         Object result = null;

         if (container.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) container;
            NodeList elementsByTagName = e.getElementsByTagName(name);
            StringBuilder buf = new StringBuilder();
            int howMany = elementsByTagName.getLength();

            for (int i = 0; i < howMany; i++) {
               Element sibling = (Element) elementsByTagName.item(i);
               String textContent = sibling.getTextContent();

               if (i > 0) {
                  buf.append('\n');
               }
               buf.append(textContent);
            }
            result = buf.toString();
         }
         return result;
      }
   }

    protected NodeList records;
    protected List<IValueRetrieval> internalSchema;
    protected List<String> names;
    protected int currentPosition = -1;
    protected boolean closed = false;
    protected RowSetMetaDataImpl rsmd;

    RestServiceResultSet() {

    }

    RestServiceResultSet(Document document, String query) {

    }

    public boolean absolute(int row) throws SQLException {
        if (Math.abs(row) > records.getLength()) {
            return false;
        }

        if (row < 0) {
            currentPosition = records.getLength() - row;
        } else {
            currentPosition = row;
        }
        return true;
    }

    public void afterLast() throws SQLException {
        currentPosition = records.getLength();
    }

    public void beforeFirst() throws SQLException {
        currentPosition = 1;
    }

    public void cancelRowUpdates() throws SQLException {
    }

    public void clearWarnings() throws SQLException {
    }

    public void close() throws SQLException {
        closed = true;
    }

    public void deleteRow() throws SQLException {
    }

    public int findColumn(String columnLabel) throws SQLException {
        // return names.indexOf(columnLabel);
        return -1;
    }

    public boolean first() throws SQLException {
        return !closed && (records.getLength() > 0);
    }

    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return null;
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return null;
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return false;
    }

    public boolean getBoolean(String columnLabel) throws SQLException {
        return false;
    }

    public byte getByte(int columnIndex) throws SQLException {
        return 0;
    }

    public byte getByte(String columnLabel) throws SQLException {
        return 0;
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return null;
    }

    public byte[] getBytes(String columnLabel) throws SQLException {
        return null;
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    public int getConcurrency() throws SQLException {
        return 0;
    }

    public String getCursorName() throws SQLException {
        return null;
    }

    public Date getDate(int columnIndex) throws SQLException {
        return null;
    }

    public Date getDate(String columnLabel) throws SQLException {
        return null;
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    public double getDouble(int columnIndex) throws SQLException {
        return 0;
    }

    public double getDouble(String columnLabel) throws SQLException {
        return 0;
    }

    public int getFetchDirection() throws SQLException {
        return 0;
    }

    public int getFetchSize() throws SQLException {
        return 0;
    }

    public float getFloat(int columnIndex) throws SQLException {
        return 0;
    }

    public float getFloat(String columnLabel) throws SQLException {
        return 0;
    }

    public int getHoldability() throws SQLException {
        return 0;
    }

    public int getInt(int columnIndex) throws SQLException {
        return 0;
    }

    public int getInt(String columnLabel) throws SQLException {
        return 0;
    }

    public long getLong(int columnIndex) throws SQLException {
        return 0;
    }

    public long getLong(String columnLabel) throws SQLException {
        return 0;
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return rsmd;
    }

    public Object getObject(int columnIndex) throws SQLException {
        return null;
    }

    public Object getObject(String columnLabel) throws SQLException {
        return null;
    }

    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    public int getRow() throws SQLException {
        return 0;
    }

    public short getShort(int columnIndex) throws SQLException {
        return 0;
    }

    public short getShort(String columnLabel) throws SQLException {
        return 0;
    }

    public Statement getStatement() throws SQLException {
        return null;
    }

    public String getString(int columnIndex) throws SQLException {
        IValueRetrieval valueRetrieval = internalSchema.get(columnIndex - 1);
        Node record = records.item(currentPosition);
        Object data = valueRetrieval.getValue(record);

        return (String) data;

    }

    public String getString(String columnLabel) throws SQLException {

        return null;
    }

    public Time getTime(int columnIndex) throws SQLException {
        return null;
    }

    public Time getTime(String columnLabel) throws SQLException {
        return null;
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    public int getType() throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return null;
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void insertRow() throws SQLException {
    }

    public boolean isAfterLast() throws SQLException {
        return currentPosition > records.getLength();
    }

    public boolean isBeforeFirst() throws SQLException {
        return currentPosition == 1;
    }

    public boolean isFirst() throws SQLException {
        return false;
    }

    public boolean isLast() throws SQLException {
        return false;
    }

    public boolean last() throws SQLException {
        currentPosition = records.getLength() - 1;
        return true;
    }

    public void moveToCurrentRow() throws SQLException {
    }

    public void moveToInsertRow() throws SQLException {
    }

    public boolean next() throws SQLException {
        currentPosition++;
        return currentPosition < records.getLength();
    }

    public boolean previous() throws SQLException {
        return false;
    }

    public void refreshRow() throws SQLException {
    }

    public boolean relative(int rows) throws SQLException {
        int newPos = currentPosition + rows;

        if ((newPos < 0) || (newPos > records.getLength())) {
            return false;
        }

        currentPosition = newPos;
        return true;
    }

    public boolean rowDeleted() throws SQLException {
        return false;
    }

    public boolean rowInserted() throws SQLException {
        return false;
    }

    public boolean rowUpdated() throws SQLException {
        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {
    }

    public void setFetchSize(int rows) throws SQLException {
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
    }

    public void updateArray(String columnLabel, Array x) throws SQLException {
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
    }

    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
    }

    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
    }

    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
    }

    public void updateBlob(String columnLabel, Blob x) throws SQLException {
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    }

    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
    }

    public void updateByte(String columnLabel, byte x) throws SQLException {
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    }

    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
    }

    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
    }

    public void updateClob(String columnLabel, Clob x) throws SQLException {
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
    }

    public void updateDate(String columnLabel, Date x) throws SQLException {
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
    }

    public void updateDouble(String columnLabel, double x) throws SQLException {
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
    }

    public void updateFloat(String columnLabel, float x) throws SQLException {
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
    }

    public void updateInt(String columnLabel, int x) throws SQLException {
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
    }

    public void updateLong(String columnLabel, long x) throws SQLException {
    }

    public void updateNull(int columnIndex) throws SQLException {
    }

    public void updateNull(String columnLabel) throws SQLException {
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
    }

    public void updateObject(String columnLabel, Object x) throws SQLException {
    }

    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
    }

    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
    }

    public void updateRef(String columnLabel, Ref x) throws SQLException {
    }

    public void updateRow() throws SQLException {
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
    }

    public void updateShort(String columnLabel, short x) throws SQLException {
    }

    public void updateString(int columnIndex, String x) throws SQLException {
    }

    public void updateString(String columnLabel, String x) throws SQLException {
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
    }

    public void updateTime(String columnLabel, Time x) throws SQLException {
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    }

    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
    }

    public boolean wasNull() throws SQLException {

        return false;
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
    public boolean isClosed() throws SQLException {

        return false;
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
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
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
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

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

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
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {

        return false;
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {

        return null;
    }
}

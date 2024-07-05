package csi.server.connector.jdbc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.RowIdLifetime;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.net.UrlEscapers;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.task.api.TaskHelper;

public class JsonDynamicConnectionFactory extends JdbcConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(JsonDynamicConnectionFactory.class);

    public static final String WEBSERVICE = "webservice";
    public static final String SLASH = "/";
    private static final String MEMORY = "memory";
    private static final String TEMP_FILE = "file";
    private static final String JDBC_H2_MEM = "jdbc:h2:mem:";
    private static final String JDBC_H2_FILE = "jdbc:h2:file:";
    private static final String[] prefix = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                                                        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private static Cache<String, String> cachedConnection;
    private static String valueColumn = "_VALUE_";

    private final Random random = new Random();
    private int duration;

    @Override
    public synchronized List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException {
        return null;
    }

    @Override
    public synchronized List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
//        List<CsiMap<String, String>> list = super.listSchemas(dsdef, catalog);
//        List<CsiMap<String, String>> itemsToRemove = Lists.newArrayList();
/*        for (CsiMap<String, String> csiMap : list) {
            if (!csiMap.get("TABLE_SCHEM").equals("PUBLIC")) {
                itemsToRemove.add(csiMap);
            }
        }
        list.removeAll(itemsToRemove)*/
        return null;
    }

    @Override
    public synchronized List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException {
        return new ArrayList<String>(Arrays.asList("TABLE"));
    }

    @Override
    public synchronized List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type) throws CentrifugeException, GeneralSecurityException {
        List<String> catalogs = super.listCatalogs(dsdef);
        List<SqlTableDef> tableDefs = super.listTableDefs(dsdef, catalogs.get(0), schema, type);
        for (SqlTableDef tableDef : tableDefs) {
            tableDef.setCatalogName("");
        }
        return tableDefs;
    }

    void prepTables(JsonDatabase current_db, JsonReader jr) {
        boolean initialized = false;
        JsonToken firstToken = null;
        int tableLevel = 0;
        List<Integer> tableNumber = new ArrayList<Integer>();
        JsonTable current_table = current_db.addTable();
        JsonColumn current_col = null;
        JsonColumn parent_col = null;
        ArrayDeque<JsonTable> tableStack = new ArrayDeque<JsonTable>();
        ArrayDeque<JsonColumn> currentColumnStack = new ArrayDeque<JsonColumn>();

        try {
            boolean eof = false;
            tableNumber.add(1); // Initialize the table number used as default table name for top level tables
            while (!eof) {
                JsonToken token = jr.peek();
                if (!initialized) {
                    initialized = true;
                    firstToken = token;
                }
                switch (token) {
                    case BEGIN_ARRAY://Arrays are mapped to tables
                        String tableName = (null != current_col) // Use parent column name if available, else table number
                                                ? current_col.toString()
                                                : genTableName(tableNumber, tableLevel);

                        tableNumber.set(tableLevel, (tableNumber.get(tableLevel) + 1)); // Increment table number for this level
                        tableNumber.add(1); // Initialize the table number used as default table name for child tables
                        tableLevel++;
                        jr.beginArray();
                        tableStack.push(current_table);
                        currentColumnStack.push(current_col);
                        current_table = current_db.addTable(tableName, current_table);
                        current_col = null;
                        break;
                    case END_ARRAY:
                        jr.endArray();
                        current_table = tableStack.pop();
                        current_col = currentColumnStack.pop();
                        if(current_col!= null) {
                            parent_col = current_col.parent;
                        }
                        else {
                            parent_col = null;
                        }
                        tableNumber.remove(tableLevel--);
                        break;
                    case BEGIN_OBJECT://Row?
                        jr.beginObject();
                        parent_col = current_col;
                        break;
                    case END_OBJECT:
                        jr.endObject();
                        if ((current_col != null) && (current_col.parent != null)) {
                            current_col = current_col.parent;
                            if (current_col.parent != null) {
                                parent_col = current_col.parent;
                            } else {
                                parent_col = null;
                            }
                        } else {
                            current_col = parent_col = null;
                        }
                        break;
                    case NAME://Key in k:v
                        String name = jr.nextName();
                        current_col = current_table.addColumn(name, parent_col);
                        break;
                    case STRING:
                        if (current_col == null) {
                            current_col = current_table.addColumn(valueColumn, null);
                            current_col.set_type(JsonToken.STRING);
                            current_col = null;
                        } else {
                            current_col.set_type(JsonToken.STRING);
                        }
                        jr.nextString();
                        break;
                    case NUMBER:
                        if (current_col == null) {
                            current_col = current_table.addColumn(valueColumn, null);
                            current_col.set_type(JsonToken.NUMBER);
                            current_col = null;
                        } else {
                            current_col.set_type(JsonToken.NUMBER);
                        }
                        jr.nextDouble();
                        break;
                    case BOOLEAN:
                        if (current_col == null) {
                            current_col = current_table.addColumn(valueColumn, null);
                            current_col.set_type(JsonToken.BOOLEAN);
                            current_col = null;
                        } else {
                            current_col.set_type(JsonToken.BOOLEAN);
                        }
                        jr.nextBoolean();
                        break;
                    case NULL:
                        if (current_col == null) {
                            current_col = current_table.addColumn(valueColumn, null);
                            current_col.set_type(JsonToken.STRING);
                            jr.nextNull();
                            current_col = null;
                        } else {
                            current_col.set_type(JsonToken.STRING);
                            jr.nextNull();
                        }
                        break;
                    case END_DOCUMENT:
                        if (JsonToken.BEGIN_OBJECT != firstToken) {
                            if ((current_col != null) && (current_col.parent != null)) {
                                current_col = current_col.parent;
                                if (current_col.parent != null) {
                                    parent_col = current_col.parent;
                                } else {
                                    parent_col = null;
                                }
                            } else {
                                current_col = parent_col = null;
                            }
                        }
                        jr.close();
                        eof = true;
                }
            }
        } catch (IOException ignored) {
            //TODO: log here
        }
    }

    void insertData(JsonDatabase current_db, JsonReader jr) throws SQLException, CentrifugeException {
        boolean initialized = false;
        JsonToken firstToken = null;
        int tableLevel = 0;
        List<Integer> tableNumber = new ArrayList<Integer>();
        JsonColumn current_col;
        JsonColumn parent_col;
        current_col = null;
        parent_col = null;
        JsonTable current_table = current_db.getTable(current_db.DEFAULT_TABLE);
        JsonRow currentRow = current_table.addRow();
        ArrayDeque<JsonTable> tableStack = new ArrayDeque<JsonTable>();
        ArrayDeque<JsonColumn> currentColumnStack = new ArrayDeque<JsonColumn>();
        ArrayDeque<JsonRow> currentRowStack = new ArrayDeque<JsonRow>();

        try {
            boolean eof = false;
            tableNumber.add(1); // Initialize the table number used as default table name for top level tables
            while (!eof) {
                JsonToken token = jr.peek();
                if (!initialized) {
                    initialized = true;
                    firstToken = token;
                }
                switch (token) {
                    case BEGIN_ARRAY:
                        String tableName = (null != current_col) // Use parent column name if available, else table number
                                ? current_col.toString()
                                : genTableName(tableNumber, tableLevel);

                        tableNumber.set(tableLevel, (tableNumber.get(tableLevel) + 1)); // Increment table number for this level
                        tableNumber.add(1); // Initialize the table number used as default table name for child tables
                        tableLevel++;
                        jr.beginArray();
                        tableStack.push(current_table);
                        currentColumnStack.push(current_col);
                        currentRowStack.push(currentRow);
                        current_table = current_db.getTable(tableName);
                        current_col = null;
                        if (jr.peek() != JsonToken.END_ARRAY) {
                            int parentId = currentRow.id;
                            currentRow = current_table.addRow();
                            currentRow.parentId = parentId;
                        }
                        break;
                    case END_ARRAY:
                        jr.endArray();
                        current_table = tableStack.pop();
                        current_col = currentColumnStack.pop();
                        parent_col = (null != current_col) ? current_col.parent : null;
                        currentRow = currentRowStack.pop();
                        tableNumber.remove(tableLevel--);
                        break;
                    case BEGIN_OBJECT://Row?
                        jr.beginObject();
                        parent_col = current_col;

                        break;
                    case END_OBJECT:
                        jr.endObject();
                        if ((current_col != null) && (current_col.parent != null)) {
                            current_col = current_col.parent;
                            if (current_col.parent != null) {
                                parent_col = current_col.parent;
                            } else {
                                parent_col = null;
                            }
                        } else {
                            current_col = parent_col = null;
                        }
                        if ((jr.peek() != JsonToken.END_ARRAY) &&
                                (jr.peek() != JsonToken.NAME) &&
                                (jr.peek() != JsonToken.END_OBJECT) &&
                                (jr.peek() != JsonToken.END_DOCUMENT)) {
                            int parentId = currentRow.parentId;
                            currentRow = current_table.addRow();
                            currentRow.parentId = parentId;
                        }
                        break;
                    case NAME://Key in k:v
                        String name = jr.nextName();
                        current_col = current_table.getColumn(current_table.makeName(name, parent_col));
                        break;
                    case END_DOCUMENT:
                        if (JsonToken.BEGIN_OBJECT != firstToken) {
                            if ((current_col != null) && (current_col.parent != null)) {
                                current_col = current_col.parent;
                                if (current_col.parent != null) {
                                    parent_col = current_col.parent;
                                } else {
                                    parent_col = null;
                                }
                            } else {
                                current_col = parent_col = null;
                            }
                        }
                        current_db.finale();
                        jr.close();
                        eof = true;
                        break;
                    default:
                        boolean wasNull = false;
                        if (current_col == null) {
                            current_col = current_table.getColumn(current_table.makeName(valueColumn, null));
                            wasNull = true;
                        }
                        switch (token) {
                            case STRING:
                                String s = jr.nextString();
                                currentRow.setVal(current_col, s);
                                break;
                            case NUMBER:
                                double n = jr.nextDouble();
                                currentRow.setVal(current_col, n);
                                break;
                            case BOOLEAN:
                                boolean bb = jr.nextBoolean();
                                currentRow.setVal(current_col, bb);
                                break;
                            case NULL:
                                jr.nextNull();
                                break;
                            default:
                                break;
                        }
                        if (wasNull) {
                            current_col = null;
                            if (jr.peek() != JsonToken.END_ARRAY) {
                                int parentId = currentRow.parentId;
                                currentRow = current_table.addRow();
                                currentRow.parentId = parentId;
                            }
                        }
                }
            }
        } catch (IOException ignored) {
           LOG.warn("Unable to process json");
           LOG.throwing(Level.WARN, ignored);
            throw new CentrifugeException("Unable to parse JSON");
        }
    }

    @Override
    public Connection getConnection(Map<String, String> propMap) throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException {
        return new Connection() {
            private String sql;

            @Override
            public Statement createStatement() throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql) throws SQLException {
                this.sql = sql;
                return new PreparedStatement() {
                    @Override
                    public ResultSet executeQuery() throws SQLException {
                        String webserviceURL = "";
                        String s2 = sql;
                        if (sql.startsWith("SELECT")) {

                            s2 = sql.substring(sql.indexOf(" (") + 2, sql.indexOf(") "));
                        }
                        String[] split = s2.split("#");
                        s2 = split[0];
                        {
                            String s6 = "";
                            List<String> urlSplit = Splitter.on('\'').splitToList(s2);
                            int howMany = urlSplit.size();

                            for (int i = 0; i < howMany; i++) {
                                String s = urlSplit.get(i);

                                if (s.length() == 0) {
                                    // undoing previous escaping
                                    // see csi.server.business.cachedb.querybuilder.DataSetQueryBuilder.escapeTextValue
                                    s = "'";
                                }
                                if ((i % 2) == 1) {
                                   s6 += UrlEscapers.urlFormParameterEscaper().escape(s);
                                } else {
                                   s6 += s;
                                }
                            }
                            webserviceURL = s6;
                        }
                        ResultSet resultSet = null;
                        Connection conn = magic(webserviceURL);

                        if (conn != null) {
                        String tableName = split[1];
                        String sql1 = "SELECT * FROM \"PUBLIC\".\"" + tableName + "\"";
                        String queryUptoSubquery;

                        if (sql.startsWith("SELECT")) {
                            queryUptoSubquery = sql.substring(0,sql.indexOf(" (")+2);

                            Set<String> selectCols = new HashSet<String>();

                            try (PreparedStatement preparedStatement =
                                    conn.prepareStatement("SELECT COLUMN_NAME FROM information_schema.columns where table_name = '"+tableName+"'");
                                  ResultSet rs = preparedStatement.executeQuery()) {
                               boolean flag = false;

                               while (rs.next()) {
                                  selectCols.add(rs.getString(1));
                                  flag = true;
                               }
                               String[] selectTokens = queryUptoSubquery.split("\"");

                               for (int i = 1; i < selectTokens.length; i+=4) {
                                  if (!selectCols.contains(selectTokens[i])){
                                     selectTokens[i] = null;
                                  }
                               }
                               queryUptoSubquery = "";

                               for (int i = 0; i < selectTokens.length; i++) {
                                  String selectToken = selectTokens[i];

                                  if (selectToken == null) {
                                     queryUptoSubquery += "null";
                                  } else {
                                     queryUptoSubquery += selectToken;

                                     if (((i+1)<selectTokens.length) && (selectTokens[i + 1] == null)) {
                                        continue;
                                     }
                                     if ((i+1)<selectTokens.length) {
                                        queryUptoSubquery += "\"";
                                     }
                                  }
                               }
                               if(!flag) {
                                  sql1 = "SELECT NULL WHERE 1=0 ";
                               }
                               sql1 = queryUptoSubquery + sql1 + ") as t1";
                            }
                        }

                        PreparedStatement preparedStatement = conn.prepareStatement(sql1);

                        resultSet = preparedStatement.executeQuery();
                        }
                        return resultSet;
                    }

                    @Override
                    public int executeUpdate() throws SQLException {
                        return 0;
                    }

                    @Override
                    public void setNull(int parameterIndex, int sqlType) throws SQLException {

                    }

                    @Override
                    public void setBoolean(int parameterIndex, boolean x) throws SQLException {

                    }

                    @Override
                    public void setByte(int parameterIndex, byte x) throws SQLException {

                    }

                    @Override
                    public void setShort(int parameterIndex, short x) throws SQLException {

                    }

                    @Override
                    public void setInt(int parameterIndex, int x) throws SQLException {

                    }

                    @Override
                    public void setLong(int parameterIndex, long x) throws SQLException {

                    }

                    @Override
                    public void setFloat(int parameterIndex, float x) throws SQLException {

                    }

                    @Override
                    public void setDouble(int parameterIndex, double x) throws SQLException {

                    }

                    @Override
                    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

                    }

                    @Override
                    public void setString(int parameterIndex, String x) throws SQLException {

                    }

                    @Override
                    public void setBytes(int parameterIndex, byte[] x) throws SQLException {

                    }

                    @Override
                    public void setDate(int parameterIndex, Date x) throws SQLException {

                    }

                    @Override
                    public void setTime(int parameterIndex, Time x) throws SQLException {

                    }

                    @Override
                    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {

                    }

                    @Override
                    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

                    }

                    @Override
                    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

                    }

                    @Override
                    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

                    }

                    @Override
                    public void clearParameters() throws SQLException {

                    }

                    @Override
                    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

                    }

                    @Override
                    public void setObject(int parameterIndex, Object x) throws SQLException {

                    }

                    @Override
                    public boolean execute() throws SQLException {
                        return false;
                    }

                    @Override
                    public void addBatch() throws SQLException {

                    }

                    @Override
                    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

                    }

                    @Override
                    public void setRef(int parameterIndex, Ref x) throws SQLException {

                    }

                    @Override
                    public void setBlob(int parameterIndex, Blob x) throws SQLException {

                    }

                    @Override
                    public void setClob(int parameterIndex, Clob x) throws SQLException {

                    }

                    @Override
                    public void setArray(int parameterIndex, Array x) throws SQLException {

                    }

                    @Override
                    public ResultSetMetaData getMetaData() throws SQLException {
                        return null;
                    }

                    @Override
                    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

                    }

                    @Override
                    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

                    }

                    @Override
                    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

                    }

                    @Override
                    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

                    }

                    @Override
                    public void setURL(int parameterIndex, URL x) throws SQLException {

                    }

                    @Override
                    public ParameterMetaData getParameterMetaData() throws SQLException {
                        return null;
                    }

                    @Override
                    public void setRowId(int parameterIndex, RowId x) throws SQLException {

                    }

                    @Override
                    public void setNString(int parameterIndex, String value) throws SQLException {

                    }

                    @Override
                    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

                    }

                    @Override
                    public void setNClob(int parameterIndex, NClob value) throws SQLException {

                    }

                    @Override
                    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

                    }

                    @Override
                    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

                    }

                    @Override
                    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

                    }

                    @Override
                    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

                    }

                    @Override
                    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {

                    }

                    @Override
                    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

                    }

                    @Override
                    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

                    }

                    @Override
                    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

                    }

                    @Override
                    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

                    }

                    @Override
                    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

                    }

                    @Override
                    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

                    }

                    @Override
                    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

                    }

                    @Override
                    public void setClob(int parameterIndex, Reader reader) throws SQLException {

                    }

                    @Override
                    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

                    }

                    @Override
                    public void setNClob(int parameterIndex, Reader reader) throws SQLException {

                    }

                    @Override
                    public ResultSet executeQuery(String sql) throws SQLException {
                        return null;
                    }

                    @Override
                    public int executeUpdate(String sql) throws SQLException {
                        return 0;
                    }

                    @Override
                    public void close() throws SQLException {

                    }

                    @Override
                    public int getMaxFieldSize() throws SQLException {
                        return 0;
                    }

                    @Override
                    public void setMaxFieldSize(int max) throws SQLException {

                    }

                    @Override
                    public int getMaxRows() throws SQLException {
                        return 0;
                    }

                    @Override
                    public void setMaxRows(int max) throws SQLException {

                    }

                    @Override
                    public void setEscapeProcessing(boolean enable) throws SQLException {

                    }

                    @Override
                    public int getQueryTimeout() throws SQLException {
                        return 0;
                    }

                    @Override
                    public void setQueryTimeout(int seconds) throws SQLException {

                    }

                    @Override
                    public void cancel() throws SQLException {

                    }

                    @Override
                    public SQLWarning getWarnings() throws SQLException {
                        return null;
                    }

                    @Override
                    public void clearWarnings() throws SQLException {

                    }

                    @Override
                    public void setCursorName(String name) throws SQLException {

                    }

                    @Override
                    public boolean execute(String sql) throws SQLException {
                        return false;
                    }

                    @Override
                    public ResultSet getResultSet() throws SQLException {
                        return null;
                    }

                    @Override
                    public int getUpdateCount() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean getMoreResults() throws SQLException {
                        return false;
                    }

                    @Override
                    public void setFetchDirection(int direction) throws SQLException {

                    }

                    @Override
                    public int getFetchDirection() throws SQLException {
                        return 0;
                    }

                    @Override
                    public void setFetchSize(int rows) throws SQLException {

                    }

                    @Override
                    public int getFetchSize() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getResultSetConcurrency() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getResultSetType() throws SQLException {
                        return 0;
                    }

                    @Override
                    public void addBatch(String sql) throws SQLException {

                    }

                    @Override
                    public void clearBatch() throws SQLException {

                    }

                    @Override
                    public int[] executeBatch() throws SQLException {
                        return new int[0];
                    }

                    @Override
                    public Connection getConnection() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean getMoreResults(int current) throws SQLException {
                        return false;
                    }

                    @Override
                    public ResultSet getGeneratedKeys() throws SQLException {
                        return null;
                    }

                    @Override
                    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
                        return 0;
                    }

                    @Override
                    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
                        return 0;
                    }

                    @Override
                    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean execute(String sql, String[] columnNames) throws SQLException {
                        return false;
                    }

                    @Override
                    public int getResultSetHoldability() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean isClosed() throws SQLException {
                        return false;
                    }

                    @Override
                    public void setPoolable(boolean poolable) throws SQLException {

                    }

                    @Override
                    public boolean isPoolable() throws SQLException {
                        return false;
                    }

                    @Override
                    public void closeOnCompletion() throws SQLException {

                    }

                    @Override
                    public boolean isCloseOnCompletion() throws SQLException {
                        return false;
                    }

                    @Override
                    public <T> T unwrap(Class<T> iface) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean isWrapperFor(Class<?> iface) throws SQLException {
                        return false;
                    }
                };

            }

            @Override
            public CallableStatement prepareCall(String sql) throws SQLException {
                return null;
            }

            @Override
            public String nativeSQL(String sql) throws SQLException {
                return null;
            }

            @Override
            public void setAutoCommit(boolean autoCommit) throws SQLException {

            }

            @Override
            public boolean getAutoCommit() throws SQLException {
                return false;
            }

            @Override
            public void commit() throws SQLException {

            }

            @Override
            public void rollback() throws SQLException {

            }

            @Override
            public void close() throws SQLException {

            }

            @Override
            public boolean isClosed() throws SQLException {
                return false;
            }

            @Override
            public DatabaseMetaData getMetaData() throws SQLException {
                return new DatabaseMetaData() {
                    @Override
                    public boolean allProceduresAreCallable() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean allTablesAreSelectable() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getURL() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getUserName() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean isReadOnly() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedHigh() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedLow() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedAtStart() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedAtEnd() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getDatabaseProductName() throws SQLException {
                        return "";
                    }

                    @Override
                    public String getDatabaseProductVersion() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getDriverName() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getDriverVersion() throws SQLException {
                        return null;
                    }

                    @Override
                    public int getDriverMajorVersion() {
                        return 0;
                    }

                    @Override
                    public int getDriverMinorVersion() {
                        return 0;
                    }

                    @Override
                    public boolean usesLocalFiles() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean usesLocalFilePerTable() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMixedCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesUpperCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesLowerCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesMixedCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getIdentifierQuoteString() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getSQLKeywords() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getNumericFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getStringFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getSystemFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getTimeDateFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getSearchStringEscape() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getExtraNameCharacters() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsAlterTableWithAddColumn() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsAlterTableWithDropColumn() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsColumnAliasing() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullPlusNonNullIsNull() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsConvert() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsConvert(int fromType, int toType) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsTableCorrelationNames() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsExpressionsInOrderBy() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOrderByUnrelated() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGroupBy() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGroupByUnrelated() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGroupByBeyondSelect() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsLikeEscapeClause() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMultipleResultSets() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMultipleTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsNonNullableColumns() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMinimumSQLGrammar() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCoreSQLGrammar() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsExtendedSQLGrammar() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsANSI92IntermediateSQL() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsANSI92FullSQL() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOuterJoins() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsFullOuterJoins() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsLimitedOuterJoins() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getSchemaTerm() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getProcedureTerm() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getCatalogTerm() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean isCatalogAtStart() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getCatalogSeparator() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsSchemasInDataManipulation() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInProcedureCalls() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInTableDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInDataManipulation() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsPositionedDelete() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsPositionedUpdate() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSelectForUpdate() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsStoredProcedures() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInComparisons() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInExists() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInIns() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCorrelatedSubqueries() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsUnion() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsUnionAll() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
                        return false;
                    }

                    @Override
                    public int getMaxBinaryLiteralLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxCharLiteralLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInGroupBy() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInIndex() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInOrderBy() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInSelect() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInTable() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxConnections() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxCursorNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxIndexLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxSchemaNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxProcedureNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxCatalogNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxRowSize() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
                        return false;
                    }

                    @Override
                    public int getMaxStatementLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxStatements() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxTableNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxTablesInSelect() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxUserNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getDefaultTransactionIsolation() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean supportsTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
                        return new ResultSet() {
                            @Override
                            public boolean next() throws SQLException {
                                return false;
                            }

                            @Override
                            public void close() throws SQLException {

                            }

                            @Override
                            public boolean wasNull() throws SQLException {
                                return false;
                            }

                            @Override
                            public String getString(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public boolean getBoolean(int columnIndex) throws SQLException {
                                return false;
                            }

                            @Override
                            public byte getByte(int columnIndex) throws SQLException {
                                return 0;
                            }

                            @Override
                            public short getShort(int columnIndex) throws SQLException {
                                return 0;
                            }

                            @Override
                            public int getInt(int columnIndex) throws SQLException {
                                return 0;
                            }

                            @Override
                            public long getLong(int columnIndex) throws SQLException {
                                return 0;
                            }

                            @Override
                            public float getFloat(int columnIndex) throws SQLException {
                                return 0;
                            }

                            @Override
                            public double getDouble(int columnIndex) throws SQLException {
                                return 0;
                            }

                            @Override
                            public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
                                return null;
                            }

                            @Override
                            public byte[] getBytes(int columnIndex) throws SQLException {
                                return new byte[0];
                            }

                            @Override
                            public Date getDate(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Time getTime(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Timestamp getTimestamp(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public InputStream getAsciiStream(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public InputStream getUnicodeStream(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public InputStream getBinaryStream(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public String getString(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public boolean getBoolean(String columnLabel) throws SQLException {
                                return false;
                            }

                            @Override
                            public byte getByte(String columnLabel) throws SQLException {
                                return 0;
                            }

                            @Override
                            public short getShort(String columnLabel) throws SQLException {
                                return 0;
                            }

                            @Override
                            public int getInt(String columnLabel) throws SQLException {
                                return 0;
                            }

                            @Override
                            public long getLong(String columnLabel) throws SQLException {
                                return 0;
                            }

                            @Override
                            public float getFloat(String columnLabel) throws SQLException {
                                return 0;
                            }

                            @Override
                            public double getDouble(String columnLabel) throws SQLException {
                                return 0;
                            }

                            @Override
                            public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
                                return null;
                            }

                            @Override
                            public byte[] getBytes(String columnLabel) throws SQLException {
                                return new byte[0];
                            }

                            @Override
                            public Date getDate(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public Time getTime(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public Timestamp getTimestamp(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public InputStream getAsciiStream(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public InputStream getUnicodeStream(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public InputStream getBinaryStream(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public SQLWarning getWarnings() throws SQLException {
                                return null;
                            }

                            @Override
                            public void clearWarnings() throws SQLException {

                            }

                            @Override
                            public String getCursorName() throws SQLException {
                                return null;
                            }

                            @Override
                            public ResultSetMetaData getMetaData() throws SQLException {
                                return null;
                            }

                            @Override
                            public Object getObject(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Object getObject(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public int findColumn(String columnLabel) throws SQLException {
                                return 0;
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
                            public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public boolean isBeforeFirst() throws SQLException {
                                return false;
                            }

                            @Override
                            public boolean isAfterLast() throws SQLException {
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
                            public void beforeFirst() throws SQLException {

                            }

                            @Override
                            public void afterLast() throws SQLException {

                            }

                            @Override
                            public boolean first() throws SQLException {
                                return false;
                            }

                            @Override
                            public boolean last() throws SQLException {
                                return false;
                            }

                            @Override
                            public int getRow() throws SQLException {
                                return 0;
                            }

                            @Override
                            public boolean absolute(int row) throws SQLException {
                                return false;
                            }

                            @Override
                            public boolean relative(int rows) throws SQLException {
                                return false;
                            }

                            @Override
                            public boolean previous() throws SQLException {
                                return false;
                            }

                            @Override
                            public void setFetchDirection(int direction) throws SQLException {

                            }

                            @Override
                            public int getFetchDirection() throws SQLException {
                                return 0;
                            }

                            @Override
                            public void setFetchSize(int rows) throws SQLException {

                            }

                            @Override
                            public int getFetchSize() throws SQLException {
                                return 0;
                            }

                            @Override
                            public int getType() throws SQLException {
                                return 0;
                            }

                            @Override
                            public int getConcurrency() throws SQLException {
                                return 0;
                            }

                            @Override
                            public boolean rowUpdated() throws SQLException {
                                return false;
                            }

                            @Override
                            public boolean rowInserted() throws SQLException {
                                return false;
                            }

                            @Override
                            public boolean rowDeleted() throws SQLException {
                                return false;
                            }

                            @Override
                            public void updateNull(int columnIndex) throws SQLException {

                            }

                            @Override
                            public void updateBoolean(int columnIndex, boolean x) throws SQLException {

                            }

                            @Override
                            public void updateByte(int columnIndex, byte x) throws SQLException {

                            }

                            @Override
                            public void updateShort(int columnIndex, short x) throws SQLException {

                            }

                            @Override
                            public void updateInt(int columnIndex, int x) throws SQLException {

                            }

                            @Override
                            public void updateLong(int columnIndex, long x) throws SQLException {

                            }

                            @Override
                            public void updateFloat(int columnIndex, float x) throws SQLException {

                            }

                            @Override
                            public void updateDouble(int columnIndex, double x) throws SQLException {

                            }

                            @Override
                            public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

                            }

                            @Override
                            public void updateString(int columnIndex, String x) throws SQLException {

                            }

                            @Override
                            public void updateBytes(int columnIndex, byte[] x) throws SQLException {

                            }

                            @Override
                            public void updateDate(int columnIndex, Date x) throws SQLException {

                            }

                            @Override
                            public void updateTime(int columnIndex, Time x) throws SQLException {

                            }

                            @Override
                            public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

                            }

                            @Override
                            public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

                            }

                            @Override
                            public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

                            }

                            @Override
                            public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

                            }

                            @Override
                            public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

                            }

                            @Override
                            public void updateObject(int columnIndex, Object x) throws SQLException {

                            }

                            @Override
                            public void updateNull(String columnLabel) throws SQLException {

                            }

                            @Override
                            public void updateBoolean(String columnLabel, boolean x) throws SQLException {

                            }

                            @Override
                            public void updateByte(String columnLabel, byte x) throws SQLException {

                            }

                            @Override
                            public void updateShort(String columnLabel, short x) throws SQLException {

                            }

                            @Override
                            public void updateInt(String columnLabel, int x) throws SQLException {

                            }

                            @Override
                            public void updateLong(String columnLabel, long x) throws SQLException {

                            }

                            @Override
                            public void updateFloat(String columnLabel, float x) throws SQLException {

                            }

                            @Override
                            public void updateDouble(String columnLabel, double x) throws SQLException {

                            }

                            @Override
                            public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

                            }

                            @Override
                            public void updateString(String columnLabel, String x) throws SQLException {

                            }

                            @Override
                            public void updateBytes(String columnLabel, byte[] x) throws SQLException {

                            }

                            @Override
                            public void updateDate(String columnLabel, Date x) throws SQLException {

                            }

                            @Override
                            public void updateTime(String columnLabel, Time x) throws SQLException {

                            }

                            @Override
                            public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

                            }

                            @Override
                            public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

                            }

                            @Override
                            public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

                            }

                            @Override
                            public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

                            }

                            @Override
                            public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

                            }

                            @Override
                            public void updateObject(String columnLabel, Object x) throws SQLException {

                            }

                            @Override
                            public void insertRow() throws SQLException {

                            }

                            @Override
                            public void updateRow() throws SQLException {

                            }

                            @Override
                            public void deleteRow() throws SQLException {

                            }

                            @Override
                            public void refreshRow() throws SQLException {

                            }

                            @Override
                            public void cancelRowUpdates() throws SQLException {

                            }

                            @Override
                            public void moveToInsertRow() throws SQLException {

                            }

                            @Override
                            public void moveToCurrentRow() throws SQLException {

                            }

                            @Override
                            public Statement getStatement() throws SQLException {
                                return null;
                            }

                            @Override
                            public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
                                return null;
                            }

                            @Override
                            public Ref getRef(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Blob getBlob(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Clob getClob(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Array getArray(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
                                return null;
                            }

                            @Override
                            public Ref getRef(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public Blob getBlob(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public Clob getClob(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public Array getArray(String columnLabel) throws SQLException {
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
                            public Time getTime(int columnIndex, Calendar cal) throws SQLException {
                                return null;
                            }

                            @Override
                            public Time getTime(String columnLabel, Calendar cal) throws SQLException {
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
                            public URL getURL(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public URL getURL(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public void updateRef(int columnIndex, Ref x) throws SQLException {

                            }

                            @Override
                            public void updateRef(String columnLabel, Ref x) throws SQLException {

                            }

                            @Override
                            public void updateBlob(int columnIndex, Blob x) throws SQLException {

                            }

                            @Override
                            public void updateBlob(String columnLabel, Blob x) throws SQLException {

                            }

                            @Override
                            public void updateClob(int columnIndex, Clob x) throws SQLException {

                            }

                            @Override
                            public void updateClob(String columnLabel, Clob x) throws SQLException {

                            }

                            @Override
                            public void updateArray(int columnIndex, Array x) throws SQLException {

                            }

                            @Override
                            public void updateArray(String columnLabel, Array x) throws SQLException {

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
                            public void updateRowId(int columnIndex, RowId x) throws SQLException {

                            }

                            @Override
                            public void updateRowId(String columnLabel, RowId x) throws SQLException {

                            }

                            @Override
                            public int getHoldability() throws SQLException {
                                return 0;
                            }

                            @Override
                            public boolean isClosed() throws SQLException {
                                return false;
                            }

                            @Override
                            public void updateNString(int columnIndex, String nString) throws SQLException {

                            }

                            @Override
                            public void updateNString(String columnLabel, String nString) throws SQLException {

                            }

                            @Override
                            public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

                            }

                            @Override
                            public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

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
                            public SQLXML getSQLXML(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public SQLXML getSQLXML(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

                            }

                            @Override
                            public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

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
                            public Reader getNCharacterStream(int columnIndex) throws SQLException {
                                return null;
                            }

                            @Override
                            public Reader getNCharacterStream(String columnLabel) throws SQLException {
                                return null;
                            }

                            @Override
                            public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

                            }

                            @Override
                            public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

                            }

                            @Override
                            public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

                            }

                            @Override
                            public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

                            }

                            @Override
                            public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

                            }

                            @Override
                            public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

                            }

                            @Override
                            public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

                            }

                            @Override
                            public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

                            }

                            @Override
                            public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

                            }

                            @Override
                            public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

                            }

                            @Override
                            public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

                            }

                            @Override
                            public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

                            }

                            @Override
                            public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

                            }

                            @Override
                            public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

                            }

                            @Override
                            public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

                            }

                            @Override
                            public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

                            }

                            @Override
                            public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

                            }

                            @Override
                            public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

                            }

                            @Override
                            public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

                            }

                            @Override
                            public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

                            }

                            @Override
                            public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

                            }

                            @Override
                            public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

                            }

                            @Override
                            public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

                            }

                            @Override
                            public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

                            }

                            @Override
                            public void updateClob(int columnIndex, Reader reader) throws SQLException {
                            }

                            @Override
                            public void updateClob(String columnLabel, Reader reader) throws SQLException {

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
                            public <T> T unwrap(Class<T> iface) throws SQLException {
                                return null;
                            }

                            @Override
                            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                                return false;
                            }
                        };
                    }

                    @Override
                    public ResultSet getSchemas() throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getCatalogs() throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getTableTypes() throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getTypeInfo() throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsResultSetType(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean ownUpdatesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean ownDeletesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean ownInsertsAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean othersUpdatesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean othersDeletesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean othersInsertsAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean updatesAreDetected(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean deletesAreDetected(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean insertsAreDetected(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsBatchUpdates() throws SQLException {
                        return false;
                    }

                    @Override
                    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
                        return null;
                    }

                    @Override
                    public Connection getConnection() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsSavepoints() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsNamedParameters() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMultipleOpenResults() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGetGeneratedKeys() throws SQLException {
                        return false;
                    }

                    @Override
                    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
                        return false;
                    }

                    @Override
                    public int getResultSetHoldability() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getDatabaseMajorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getDatabaseMinorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getJDBCMajorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getJDBCMinorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getSQLStateType() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean locatorsUpdateCopy() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsStatementPooling() throws SQLException {
                        return false;
                    }

                    @Override
                    public RowIdLifetime getRowIdLifetime() throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
                        return false;
                    }

                    @Override
                    public ResultSet getClientInfoProperties() throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean generatedKeyAlwaysReturned() throws SQLException {
                        return false;
                    }

                    @Override
                    public <T> T unwrap(Class<T> iface) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean isWrapperFor(Class<?> iface) throws SQLException {
                        return false;
                    }
                };
            }

            @Override
            public void setReadOnly(boolean readOnly) throws SQLException {

            }

            @Override
            public boolean isReadOnly() throws SQLException {
                return false;
            }

            @Override
            public void setCatalog(String catalog) throws SQLException {

            }

            @Override
            public String getCatalog() throws SQLException {
                return null;
            }

            @Override
            public void setTransactionIsolation(int level) throws SQLException {

            }

            @Override
            public int getTransactionIsolation() throws SQLException {
                return 0;
            }

            @Override
            public SQLWarning getWarnings() throws SQLException {
                return null;
            }

            @Override
            public void clearWarnings() throws SQLException {

            }

            @Override
            public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
                return null;
            }

            @Override
            public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
                return null;
            }

            @Override
            public Map<String, Class<?>> getTypeMap() throws SQLException {
                return null;
            }

            @Override
            public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

            }

            @Override
            public void setHoldability(int holdability) throws SQLException {

            }

            @Override
            public int getHoldability() throws SQLException {
                return 0;
            }

            @Override
            public Savepoint setSavepoint() throws SQLException {
                return null;
            }

            @Override
            public Savepoint setSavepoint(String name) throws SQLException {
                return null;
            }

            @Override
            public void rollback(Savepoint savepoint) throws SQLException {

            }

            @Override
            public void releaseSavepoint(Savepoint savepoint) throws SQLException {

            }

            @Override
            public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                return null;
            }

            @Override
            public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
                return null;
            }

            @Override
            public Clob createClob() throws SQLException {
                return null;
            }

            @Override
            public Blob createBlob() throws SQLException {
                return null;
            }

            @Override
            public NClob createNClob() throws SQLException {
                return null;
            }

            @Override
            public SQLXML createSQLXML() throws SQLException {
                return null;
            }

            @Override
            public boolean isValid(int timeout) throws SQLException {
                return false;
            }

            @Override
            public void setClientInfo(String name, String value) throws SQLClientInfoException {

            }

            @Override
            public void setClientInfo(Properties properties) throws SQLClientInfoException {

            }

            @Override
            public String getClientInfo(String name) throws SQLException {
                return null;
            }

            @Override
            public Properties getClientInfo() throws SQLException {
                return null;
            }

            @Override
            public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
                return null;
            }

            @Override
            public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
                return null;
            }

            @Override
            public void setSchema(String schema) throws SQLException {

            }

            @Override
            public String getSchema() throws SQLException {
                return null;
            }

            @Override
            public void abort(Executor executor) throws SQLException {

            }

            @Override
            public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

            }

            @Override
            public int getNetworkTimeout() throws SQLException {
                return 0;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }
        };

    }


    private Connection magic(String url_in) {
        try{

//            Properties nativeProps = toNativeProperties(propMap);

            if (cachedConnection == null) {
                duration = 1200;

                int size = 1000;

                cachedConnection = CacheBuilder.newBuilder().expireAfterAccess(duration, TimeUnit.SECONDS).maximumSize(size).build();
            }




            String key = "";
            {
                key += LocalDateTime.now().getMinute();
                key += getTypeName();
                if (TaskHelper.getCurrentSession() != null) {
                    key += TaskHelper.getCurrentSession().getId();
                }

            }


          /*  if (cachedConnection.getIfPresent(key) != null) {
                return DriverManager.getConnection(cachedConnection.getIfPresent(key));
            }*/

            Connection conn = null;

            String storeLocation = MEMORY;

            //Directory for temporary file store
//            String tempDir = "./TempFolder/";

            //Location of file
            String webLocation = url_in;

            //Hook to override the root element name
            String safePrefix = "/";

            //Hook to override the value column name
            String valueColumnName = "Value";

            //Cache the raw json somewhere locally if a webservice.
            File jsonFile = null;

            valueColumn = valueColumnName;
            String tempFile = null;
            String tempdb = null;

            try {
//                if (storeLocation.equals(MEMORY)) {
                    tempdb = Double.toString(random.nextDouble());
                    conn = DriverManager.getConnection(JDBC_H2_MEM + tempdb + ";DB_CLOSE_DELAY=" + (duration + 10));
//                }
//                if (storeLocation.equals(TEMP_FILE)) {
//                    try {
//                        tempFile = Long.toString(random.nextLong());
//                        if (tempDir == null) {
//                            tempFile = new File("./temp/", tempFile).toString();
//                        } else {
//                            tempFile = new File(tempDir, tempFile).toString();
//                        }
//                    } catch (Exception e) {
//                       LOG.warn("Could not create temporary file");
//                        return throwGenericError();
//                    }
//                    conn = DriverManager.getConnection(JDBC_H2_FILE + tempFile);
//                    new File(tempFile + ".h2.db").deleteOnExit();
//
//                }
                if (conn == null) {
                    throwGenericError();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                LOG.warn("Could not create temporary store.");
                throwGenericError();
            }

            JsonDatabase jsonDatabase;
            if (safePrefix == null) {
                jsonDatabase = new JsonDatabase(conn);
            } else {
                jsonDatabase = new JsonDatabase(conn, safePrefix);
            }

            String fileLocation = null;
            if (true) {
                //Create a local cache of the json file
                //Processing into h2 requires two passes
                //but should not require two remote calls
                try {
                    jsonFile = File.createTempFile("csi.raw", "json");
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.warn("Could not create Temporary File");
                    throwGenericError();
                }
                Charset utf8 = Charsets.UTF_8;
                CharSink charSink = Files.asCharSink(jsonFile, utf8);
                URL url = null;
                try {
                    url = new URL(webLocation);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    charSink.writeFrom(Resources.asCharSource(url, utf8).openStream());
                } catch (IOException e) {
                   LOG.warn("Could not open JSON source");
                    e.printStackTrace();
                    throwGenericError();
                }
                if (jsonFile != null) {
                    fileLocation = jsonFile.getPath();
                } else {
                   LOG.trace("JsonFile is null");
                }
            }

            FileReader ffr = null;

            try {
                ffr = new FileReader(fileLocation);
            } catch (FileNotFoundException e) {
               LOG.warn("Could not open JSON source");
                e.printStackTrace();
                throwGenericError();
            }

            JsonReader jr = new JsonReader(ffr);
            prepTables(jsonDatabase, jr);

            try {
               ffr.close();
               ffr = null;
            } catch (IOException e) {
                e.printStackTrace();
                LOG.warn("Could not close JsonReader");
                throwGenericError();
            }

            try {
                ffr = new FileReader(fileLocation);
            } catch (FileNotFoundException e) {
               LOG.warn("Could not open JSON source");
                e.printStackTrace();
                throwGenericError();
            }

            jr = new JsonReader(ffr);
            insertData(jsonDatabase, jr);


            try {
               ffr.close();
            } catch (IOException e) {
                e.printStackTrace();
                LOG.warn("Could not close JsonReader");
                throwGenericError();
            }

            String fileMode = "webservice";
            if (isWebService(fileMode)) {
                if (jsonFile != null) {
                    jsonFile.delete();
                } else {
                   LOG.trace("JSON File is null when it should not be.");
                }
            }
            if (storeLocation.equals(MEMORY)) {
                cachedConnection.put(key, JDBC_H2_MEM + tempdb);
            } else {
                cachedConnection.put(key, JDBC_H2_FILE + tempFile);
            }
            return conn;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private boolean isWebService(String fileMode) {
        return WEBSERVICE.equals(fileMode);
    }

    private Connection throwGenericError() throws CentrifugeException {
        throw new CentrifugeException("Unable to create connections. See server logs.");
    }

    private static class JsonTable {
        private static String ID_COLUMN = "__KEY__";
        public boolean isParent = false;
        TreeMap<String, JsonColumn> cols;
        JsonTable parent = null;
        private String name;
        private JsonDatabase _db;
        private ArrayList<JsonRow> data = null;
        private int rowId = 1;

        public JsonTable(JsonDatabase db, String name) {
            cols = new TreeMap<>();
            this.setName(name);
            _db = db;
            data = new ArrayList<>();
        }

        public JsonColumn addColumn(String name, JsonColumn parent) {
            JsonColumn returnColumn;
            JsonColumn newColumn;
            {//need to create column to get key for column map...
                newColumn = new JsonColumn(name);
                newColumn.parent = parent;
                newColumn.setTable(this);
            }
            {//test if we have this column already, if not add it to map
                returnColumn = cols.get(newColumn.toString());
                if (returnColumn == null) {
                    cols.put(newColumn.toString(), newColumn);
                    returnColumn = newColumn;
                }
            }
            return returnColumn;
        }

        public String makeName(String name, JsonColumn parent) {
            String qualifiedName;
            if (parent != null) {
                qualifiedName = parent.toString() + SLASH + name;
            } else {
                qualifiedName = getName() + SLASH + name;
            }
            return qualifiedName;
        }

        public void addParent(JsonTable jt) {
            this.parent = jt;
            jt.isParent = true;
        }

        public void finale() throws SQLException {
            try {
                if (isParent) {
                    JsonColumn id = addColumn(ID_COLUMN, null);
                    id.set_type(JsonToken.NUMBER);
                    id.useTableInName = false;
                }
                if (parent != null) {
                    JsonColumn jc = addColumn(parent.getName(), null);
                    jc.set_type(JsonToken.NUMBER);
                    jc.useTableInName = false;
                }
            } catch (Exception ignored) {
                //TODO: Log this better
            }
            for (JsonColumn jt : cols.values()) {
                if (jt.get_type() != null) {
                    _db._addColumn(this, jt);
                }
            }
            for (JsonRow jr : data) {
                _db._addData(this, jr);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public JsonRow addRow() {
            JsonRow r = new JsonRow();
            if (this.isParent) {
                r.id = rowId++;
            }
            data.add(r);
            return r;
        }

        public JsonColumn getColumn(String valueColumn) {

            return cols.get(valueColumn);
        }

    }

    private static class JsonRow {
        public int id = -1;
        public int parentId = -1;
        private HashMap<JsonColumn, Object> vals;

        public JsonRow() {
            vals = new HashMap<>();
        }

        public Object getVal(JsonColumn c) {
            if (c.getName().equals("__KEY__")) {
                return id;
            }
            if ((c.getTable().parent != null) && c.getName().equals(c.getTable().parent.getName())) {
                return parentId;
            }
            return vals.get(c);
        }

        public void setVal(JsonColumn c, Object o) {
            vals.put(c, o);
        }
    }

    private static class JsonColumn implements Comparable<JsonColumn> {
        public String name = null;
        public JsonColumn parent = null;
        public boolean useTableInName = true;
        private String qualifiedName = null;
        private JsonToken _type = null;
        private JsonTable table = null;

        public JsonColumn(String s) {
            name = s;
        }

        public JsonColumn(String s, JsonColumn p) {
            name = s;
            parent = p;
        }

        public JsonColumn add(String s) {
            return new JsonColumn(s, this);
        }

        @Override
        public int compareTo(JsonColumn o) {
            return name.compareTo(o.name);
        }

        public JsonToken get_type() {
            return _type;
        }

        public void set_type(JsonToken _type) {
            //String is the lowest common denominator
            if (this._type == JsonToken.STRING) {
                return;
            }
            if (_type == JsonToken.STRING) {
                this._type = _type;
                return;
            }
            //Number is common to boolean and number
            if (this._type == JsonToken.NUMBER) {
                return;
            }
            //either setting to boolean or degrading to number
            this._type = _type;
        }

        @Override
        public String toString() {
            if (parent != null) {
                qualifiedName = parent.toString() + SLASH + name;
            } else {
                if (useTableInName) {
                    qualifiedName = table.getName() + SLASH + name;

                } else {
                    qualifiedName = name;
                }
            }
            if (_type == null) {
                return qualifiedName;
            }
            return qualifiedName;
        }

        public JsonTable getTable() {
            return table;
        }

        public void setTable(JsonTable table) {
            this.table = table;
        }

        public String getName() {
            return this.toString();
        }

        public String getDataType() {
            switch (_type) {
                case BOOLEAN:
                    return "BOOLEAN";
                case NUMBER:
                    return "DOUBLE";
                case NULL:
                    return "VARCHAR";
                case STRING:
                    return "VARCHAR";
                default:
                    break;
            }
            return null;
        }
    }

    private static class JsonDatabase {
        private static final String DEFAULT_ROOT_TABLE_NAME = "/";
        final String DEFAULT_TABLE;
        final private Connection conn;
        public HashMap<String, JsonTable> tables;
        private int anonymousCounter = 0;

        public JsonDatabase(Connection conn) {
            this.conn = conn;
            tables = new HashMap<>();
            DEFAULT_TABLE = DEFAULT_ROOT_TABLE_NAME;
        }

        public JsonDatabase(Connection conn, String defaultTableName) {
            this.conn = conn;
            tables = new HashMap<>();
            DEFAULT_TABLE = defaultTableName;
        }

        public JsonTable getTable(String s) {
            return tables.get(s);
        }

        public JsonTable addTable(String s) {
            JsonTable t = tables.get(s);
            if (t == null) {
                t = new JsonTable(this, s);
                tables.put(s, t);
            }
            return t;
        }

        public JsonTable addTable() {
            if (0 == anonymousCounter++) {
                return addTable(DEFAULT_TABLE);
            }
            return addTable(DEFAULT_TABLE + (anonymousCounter));
        }

        public void finale() throws SQLException {
            for (JsonTable jt : tables.values()) {
                _createTable(jt);
                jt.finale();
            }
        }

/*        public void prettyPrint() {
            Iterator<String> i = tables.keySet().iterator();
            while (i.hasNext()) {
                JsonTable jt = tables.get(i.next());
                jt.prettyPrint(conn);
            }
        }*/

        public JsonTable addTable(String string, JsonTable current_tab) {
            JsonTable t = addTable(string);
            t.addParent(current_tab);
            return t;
        }

        public void _createTable(JsonTable t) throws SQLException {
           try {
              String query = "CREATE TABLE \"" + t.getName() + "\"();";
              //                System.out.println(query);
              try (Statement stat = conn.createStatement()) {
                 LOG.trace("add table query: " + query);
                 stat.executeUpdate(query);
              }
            } catch (SQLException e) {
               LOG.warn("Could not create table.");
                throw e;
            }
        }

        public void _addColumn(JsonTable t, JsonColumn c) throws SQLException {
           try {
              //set types.
              String query = "ALTER TABLE \"" + t.getName() + "\" ADD \"" + c.getName() + "\" " + c.getDataType() + ";";

              try (Statement stat = conn.createStatement()) {
                 LOG.trace("add column query: " + query);
                 stat.executeUpdate(query);
              }
           } catch (SQLException e) {
              LOG.warn("Could not add column.");
              throw e;
           }
        }

        public void _addData(final JsonTable jsonTable, JsonRow jr) throws SQLException {
            String query;
            List<String> queryValue = new ArrayList<String>();
            String queryHolder = "";
            try {
                query = "INSERT INTO \"" + jsonTable.getName() + "\"(";
                Iterator<String> i = jsonTable.cols.keySet().iterator();
                Set<String> nonNullColumns = new HashSet<String>();
                while (i.hasNext()) {
                    String next = i.next();
                    JsonColumn jc = jsonTable.cols.get(next);
                    if (jc.get_type() != null) {
                        nonNullColumns.add(next);
                    }
                }
                i = nonNullColumns.iterator();
                while (i.hasNext()) {
                    JsonColumn jc = jsonTable.cols.get(i.next());
                    if (jc.get_type() != null) {
                        query += "\"" + jc.getName() + "\"";
                        query += (i.hasNext() ? "," : ")");
                        queryHolder += "?";
                        if (jr.getVal(jc) != null) {
                            queryValue.add(jr.getVal(jc).toString());
                        } else {
                            queryValue.add(null);
                        }
                        queryHolder += (i.hasNext() ? "," : ")");
                    } else {
                        queryValue.add(null);
                        queryHolder += (i.hasNext() ? "," : ")");
                    }
                }
                query += " VALUES(" + queryHolder + ";";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                   int howMany = queryValue.size();

                   for (int j = 1; j <= howMany; j++) {
                      stmt.setString(j, queryValue.get(j - 1));
                   }
                   LOG.trace("add data query: " + stmt.toString());
                   stmt.executeUpdate();
                }
            } catch (SQLException e) {
               LOG.warn("Could not load record.");
                throw e;
            }
        }
    }

   private String genTableName(List<Integer> tableNumberIn, int tableLevelIn) {
      return prefix[tableLevelIn] + tableNumberIn.get(tableLevelIn).toString();
   }
}

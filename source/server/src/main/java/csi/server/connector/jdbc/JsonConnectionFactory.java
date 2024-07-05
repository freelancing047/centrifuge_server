package csi.server.connector.jdbc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.task.api.TaskHelper;

public class JsonConnectionFactory extends JdbcConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(JsonConnectionFactory.class);

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
        return new ArrayList<String>(Arrays.asList("JSON"));
    }

    @Override
    public synchronized List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
        List<CsiMap<String, String>> list = super.listSchemas(dsdef, catalog);
        List<CsiMap<String, String>> itemsToRemove = new ArrayList<CsiMap<String, String>>();
        for (CsiMap<String, String> csiMap : list) {
            if (!csiMap.get("TABLE_SCHEM").equals("PUBLIC")) {
                itemsToRemove.add(csiMap);
            }
        }
        list.removeAll(itemsToRemove);
        return list;
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
        Properties nativeProps = toNativeProperties(propMap);

        if (cachedConnection == null) {
            duration = 1200;

            String cacheExiprationInSeconds = nativeProps.getProperty("CacheExiprationInSeconds");
            try {
                duration = Integer.parseInt(cacheExiprationInSeconds);
            } catch (NumberFormatException e) {
                //do nothing
            }
            int size = 1000;
            String cacheMaxSize = nativeProps.getProperty("CacheMaxSize");
            try {
                duration = Integer.parseInt(cacheMaxSize);
            } catch (NumberFormatException e) {
                //do nothing
            }

            cachedConnection = CacheBuilder.newBuilder().expireAfterAccess(duration, TimeUnit.SECONDS).maximumSize(size).build();
        }

        //Userfiles or Webservice
        String fileMode = nativeProps.getProperty("FileMode");

        if (!isWebService(fileMode)) {
            // // convert file token to actual path
            // // Note: this is only relavent to file based drivers
            String path = resolveFilePath(propMap);
            if (path != null) {
                propMap.put(CSI_FILEPATH, path);
            }

        }

        String fileLocation = propMap.get("csi.filePath");

        String key = "";
        {
            Set<String> keySet = propMap.keySet();
            for (Object o : keySet) {
                key += o.toString() + "\t" + propMap.get(o);
            }
            key += getTypeName();
            if (TaskHelper.getCurrentSession() != null) {
                key += TaskHelper.getCurrentSession().getId();
            }

        }
        if (!isWebService(fileMode)) {
            key += (new File(fileLocation)).lastModified();
        }

        if (cachedConnection.getIfPresent(key) != null) {
            return DriverManager.getConnection(cachedConnection.getIfPresent(key));
        }

        Connection conn = null;

        String storeLocation = nativeProps.getProperty("StoreLocation");
        if (storeLocation == null) {
            storeLocation = MEMORY;
        }

        //Directory for temporary file store
        String tempDir = nativeProps.getProperty("TempDirectory");

        //Location of file
        String webLocation = propMap.get("WebLocation");

        //Hook to override the root element name
        String safePrefix = nativeProps.getProperty("SafePrefix");

        //Hook to override the value column name
        String valueColumnName = nativeProps.getProperty("ValueColumnName");

        //Cache the raw json somewhere locally if a webservice.
        File jsonFile = null;
        if (valueColumnName != null) {
            valueColumn = valueColumnName;
        }

        String tempFile = null;
        String tempdb = null;
        try {
            if (storeLocation.equals(MEMORY)) {
                tempdb = Double.toString(random.nextDouble());
                conn = DriverManager.getConnection(JDBC_H2_MEM + tempdb + ";DB_CLOSE_DELAY=" + (duration + 10));
            }
            if (storeLocation.equals(TEMP_FILE)) {
                try {
                    tempFile = Long.toString(random.nextLong());
                    if (tempDir == null) {
                        tempFile = new File("./temp/", tempFile).toString();
                    } else {
                        tempFile = new File(tempDir, tempFile).toString();
                    }
                } catch (Exception e) {
                   LOG.warn("Could not create temporary file");
                    return throwGenericError();
                }
                conn = DriverManager.getConnection(JDBC_H2_FILE + tempFile);
                new File(tempFile+".h2.db").deleteOnExit();

            }
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

        if (isWebService(fileMode)) {
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
            CharSink charSink = Files.asCharSink(jsonFile, Charset.defaultCharset());
            URL url = null;
            try {
                url = new URL(webLocation);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                charSink.writeFrom(Resources.asCharSource(url, Charset.defaultCharset()).openStream());
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
            if (ffr != null) {
                ffr.close();
                ffr = null;
            }
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
            if (ffr != null) {
                ffr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warn("Could not close JsonReader");
            throwGenericError();
        }

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
        TreeMap<String,JsonColumn> cols;
        JsonTable parent = null;
        private String name;
        private JsonDatabase _db;
        private ArrayList<JsonRow> data = null;
        private int rowId = 1;

        public JsonTable(JsonDatabase db, String name) {
            cols = new TreeMap<String,JsonColumn>();
            this.setName(name);
            _db = db;
            data = new ArrayList<JsonRow>();
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
                Set<String> nonNullColumns = Sets.newHashSet();
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
                   for (int j = 1; j <= queryValue.size(); j++) {
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

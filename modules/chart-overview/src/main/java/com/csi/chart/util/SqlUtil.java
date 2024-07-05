package com.csi.chart.util;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.empire.data.DataMode;
import org.apache.empire.data.DataType;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBDatabaseDriver;
import org.apache.empire.db.DBTable;

import csi.server.common.util.sql.PostgreSQLDriverExtensions;

public class SqlUtil {
   private static final Logger LOG = LogManager.getLogger(SqlUtil.class);

    static int NAME_COLUMN = 4;
    static int TYPE_COLUMN = 5;
    static int SIZE_COLUMN = 7;
    static int NULLABLE_COLUMN = 11;

    

    public static void quietCloseResulSet( ResultSet rs )
    {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {}
        }
    }

    public static void quietCloseStatement( Statement statement )
    {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {}
        }
    }

    public static void quietCloseConnection( Connection conn )
    {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public static long getLong( Connection connection, String query ) throws SQLException
    {
        long value = 0;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            if( rs.next()) {
                value = rs.getLong(1);
            }
        } catch (SQLException sqlError) {
            quietCloseResulSet(rs);
            quietCloseStatement(stmt);
            throw sqlError;
        }
        
        return value;
    }

    public static Map mapRow( ResultSet rs ) throws SQLException
    {
        Map row = new HashMap();
        ResultSetMetaData metaData = rs.getMetaData();
        int cols = metaData.getColumnCount();
        for( int i=1; i <= cols; i++ ) {
            String name = metaData.getColumnName(i);
            row.put(name, rs.getObject(i));
        }
        
        return row;
    }
    
    public static DBTable getDBTable(Connection conn, String schema, String tableName) throws SQLException, NamingException {
        DBDatabase db = new DBDatabase() {
        };
        
        db.setSchema(schema);

        DBDatabaseDriver driver = new PostgreSQLDriverExtensions();

        DatabaseMetaData metaData = conn.getMetaData();
        
        db.open(driver, conn);
        DBTable table = new DBTable(tableName, db);
        ResultSet columns = metaData.getColumns(null, null, tableName, null);
        while (columns.next()) {
            String columnName = columns.getString(4);
//            columnName = SqlUtil.quote(columnName);
            DataType type = getTypeFromSql(columns.getInt(5));
            double size = columns.getInt(7);
            DataMode required = columns.getInt(NULLABLE_COLUMN) == DatabaseMetaData.columnNullable ? DataMode.Nullable
                    : DataMode.NotNull;
            table.addColumn(columnName, type, size, required);
        }
        
        return table;
    }
    
    public static String quote(String name) {
        if (name == null || name.length() == 0) {
           LOG.warn("quote method passed a null or zero-length fieldName.");
            return "\"\"";
        }

        name = name.trim();

        char c = name.charAt(0);
        if (c == '"' || c == '\'') {
            // assuming that we're dealing w/ a quoted string...don't check the
            // last character
            return name;
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append('"');
        buffer.append(name);
        buffer.append('"');

        return buffer.toString();
    }
    
    public static DataType getTypeFromSql(int sqlType) {
        DataType dt = null;
        switch (sqlType) {
            case Types.INTEGER:
            case Types.BIGINT:
                dt = DataType.INTEGER;
                break;
            case Types.FLOAT:
                dt = DataType.FLOAT;
                break;
            case Types.DECIMAL:
                dt = DataType.DECIMAL;
                break;
            case Types.VARCHAR:
                dt = DataType.TEXT;
                break;
            case Types.DATE:
                dt = DataType.DATE;
                break;
            case Types.TIME:
                dt = DataType.DATETIME;
                break;
            case Types.TIMESTAMP:
                dt = DataType.DATETIME;
                break;
            default:
                dt = DataType.TEXT;
                break;
        }
        return dt;
    }


}

package csi.server.util.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.naming.NamingException;

import org.apache.empire.data.DataMode;
import org.apache.empire.data.DataType;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBDatabaseDriver;
import org.apache.empire.db.DBTable;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.PostgreSQLDriverExtensions;
import csi.server.util.SqlUtil;

public class DBModelHelper {

    static int NAME_COLUMN = 4;
    static int TYPE_COLUMN = 5;
    static int SIZE_COLUMN = 7;
    static int NULLABLE_COLUMN = 11;

   public DBTable createDBTable(DataView dataview) throws NamingException, SQLException, CentrifugeException {
      DBTable table = null;

      @SuppressWarnings("serial")
      DBDatabase db = new DBDatabase() {
      };
      DBDatabaseDriver driver = new PostgreSQLDriverExtensions();

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         db.open(driver, null);

         String cacheTableName = CacheUtil.getQuotedCacheTableName(dataview.getUuid());
         table = new DBTable(cacheTableName, db);
         DatabaseMetaData metaData = conn.getMetaData();

         try (ResultSet columns = metaData.getColumns(null, null, CacheUtil.getCacheTableName(dataview.getUuid()), null)) {
            while (columns.next()) {
               String columnName = columns.getString(4);
               columnName = SqlUtil.quote(columnName);
               DataType type = getTypeFromSql(columns.getInt(5));
               double size = columns.getInt(7);
               DataMode required =
                  (columns.getInt(NULLABLE_COLUMN) == DatabaseMetaData.columnNullable)
                     ? DataMode.Nullable
                     : DataMode.NotNull;

               table.addColumn(columnName, type, size, required);
            }
         }
      }
      return table;
   }

   public DBTable getDBTable(String schema, String tableName)
         throws SQLException, NamingException, CentrifugeException {
      @SuppressWarnings("serial")
      DBDatabase db = new DBDatabase() {
      };
      return getDBTable(db, schema, tableName);
   }

   public DBTable getDBTable(DBDatabase db, String schema, String tableName) throws SQLException, NamingException, CentrifugeException {
      DBTable table = null;

      if (db.getDriver() == null) {
         db.setSchema(schema);
      }
      DBDatabaseDriver driver = new PostgreSQLDriverExtensions();

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         DatabaseMetaData metaData = conn.getMetaData();

         db.open(driver, conn);

         table = new DBTable(tableName, db);

         try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {
               String columnName = columns.getString(4);
               columnName = SqlUtil.quote(columnName);
               DataType type = getTypeFromSql(columns.getInt(5));
               double size = columns.getInt(7);
               DataMode required =
                  (columns.getInt(NULLABLE_COLUMN) == DatabaseMetaData.columnNullable)
                     ? DataMode.Nullable
                     : DataMode.NotNull;

               table.addColumn(columnName, type, size, required);
            }
         }
      }
      return table;
   }

   public DataType getTypeFromSql(int sqlType) {
      DataType dt = null;

      switch (sqlType) {
         case Types.INTEGER:
            dt = DataType.INTEGER;
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

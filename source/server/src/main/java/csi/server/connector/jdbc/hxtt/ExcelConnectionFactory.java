package csi.server.connector.jdbc.hxtt;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.connector.jdbc.JdbcConnectionFactory;
import csi.server.util.CacheUtil;

public class ExcelConnectionFactory extends JdbcConnectionFactory {

    private static final String TABLE_TYPE_WORKSHEET = "Worksheet";

    public ExcelConnectionFactory() {
    }

    @Override
    public Properties toNativeProperties(Map<String, String> propMap) {
        Properties props = super.toNativeProperties(propMap);

        // TODO: fix me...for some reason the driver
        // doesn't seem to respect this property
        String hasHeaders = propMap.get(CSI_SCHEMA_HASHEADERS);
        props.put("firstRowHasNames", Boolean.parseBoolean(hasHeaders));

        return props;
    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        String path = resolveFilePath(propertiesMap);
        return getUrlPrefix() + path;
    }

   @Override
   public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
      // return empty list
      return new ArrayList<String>();
   }

   @Override
   public List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
      // return empty list
      return new ArrayList<CsiMap<String, String>>();
   }

    @Override
    public List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type) throws CentrifugeException, GeneralSecurityException {
    	synchronized(this) {
	        List<SqlTableDef> list = super.listTableDefs(dsdef, null, null, (String[])null);
	        for (SqlTableDef t : list) {
	            t.setTableType(TABLE_TYPE_WORKSHEET);

	            String tname = t.getTableName();
	            String nameQualifier = getTableNameQualifier();
                //TODO: WHat is going on here ????
	            if ((tname.charAt(0) == ' ') || (tname.charAt(tname.length() - 1) == ' ')) {
	                t.setTableName(nameQualifier + tname + nameQualifier);
	            }
	        }
	        return list;
    	}
    }

    @Override
    public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
    	synchronized(this) {
	    	List<String> types = new ArrayList<String>();
	        types.add(TABLE_TYPE_WORKSHEET);
	        return types;
    	}
    }

   @Override
   public synchronized List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table) throws CentrifugeException, GeneralSecurityException {
      List<ColumnDef> result = new ArrayList<ColumnDef>();

      try (Connection conn = getConnection(dsdef)) {
         if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getColumns(catalog, schema, table, null)) {
               while (rs.next()) {
                  ColumnDef col = new ColumnDef();

                  col.setCatalogName(rs.getString(1));
                  col.setSchemaName(rs.getString(2));
                  col.setTableName(rs.getString(3));

                  String colname = rs.getString(4);

                  if (colname != null) {
                     colname = colname.trim();
                  }
                  col.setColumnName(colname);
                  col.setLocalId(UUID.randomUUID().toString().toLowerCase()); // Double-check this !!!!!!!!!!!!!!
                  col.setJdbcDataType(rs.getInt(5));
                  col.setDataTypeName(rs.getString(6));
                  col.setColumnSize(rs.getInt(7));
                  col.setDecimalDigits(rs.getInt(9));
                  col.setDefaultValue(rs.getString(13));
                  col.setOrdinal(rs.getInt(17));
                  col.setNullable(rs.getString(18));

                  CsiDataType csiType = CacheUtil.resolveCsiType(col.getDataTypeName(), col.getJdbcDataType(), this);
                  col.setCsiType(csiType);
                  result.add(col);
               }
            }
         }
      } catch (SQLException e) {
         throw new CentrifugeException("Failed to list tables", e);
      }
      return result;
   }

    public String getQuotedAliasName(String name) {
        String nameQualifier = getTableNameQualifier();
        String escapeChar = getEscapeChar();

       return quoteName(name, nameQualifier, escapeChar);
    }

   public String castExpression(String expressionIn, CsiDataType sourceTypeIn, CsiDataType targetTypeIn) {
      if ((null != expressionIn) && (null != targetTypeIn) && (targetTypeIn != sourceTypeIn)) {
         switch (targetTypeIn) {
            case String:
               return "CSTR( " + expressionIn + " )";
            case Boolean:
               return "CBOOL( " + expressionIn + " )";
            case Integer:
               return "CLNG( " + expressionIn + " )";
            case Number:
               return "CDBL( " + expressionIn + " )";
            case DateTime:
               return "CAST( " + expressionIn + " AS TIMESTAMP )";
            case Date:
               return "CAST( " + expressionIn + " AS DATE )";
            case Time:
               return "CAST( " + expressionIn + " AS TIME )";
            default:
         }
      }
      return expressionIn;
   }
}

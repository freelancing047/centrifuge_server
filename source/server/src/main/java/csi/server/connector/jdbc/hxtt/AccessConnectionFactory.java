package csi.server.connector.jdbc.hxtt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.connector.jdbc.JdbcConnectionFactory;

public class AccessConnectionFactory extends JdbcConnectionFactory {

    private static final int _maxRetries = 1;

    public AccessConnectionFactory() {

    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        String path = resolveFilePath(propertiesMap);
        return getUrlPrefix() + path;
    }

   @Override
   public List<SqlTableDef> listTableDefs(ConnectionDef connectionIn, String catalogIn, String schemaIn, String typeIn)
         throws CentrifugeException, GeneralSecurityException {

      // HACK: We need to try twice in order to avoid NPE in access driver sometimes
      // failing on
      // first request to list views (possibly tables also).
      synchronized (this) {
         for (int retry = 0; _maxRetries >= retry; retry++) {
            try {
                ArrayList<SqlTableDef> myTables = new ArrayList<SqlTableDef>();
                ArrayList<SqlTableDef> myResults = (ArrayList<SqlTableDef>) super.listTableDefs(connectionIn, null,
                                                                                                null, (String[]) null);
                int howMany = myResults.size();

                if (typeIn == null) {
                   for (int i = 0; i < howMany; i++) {
                      SqlTableDef myTable = myResults.get(i);
                      if (myTable.getTableName().charAt(0) != '~') {

                         myTables.add(myTable);
                      }
                   }
                } else {
                   for (int i = 0; i < howMany; i++) {
                      SqlTableDef myTable = myResults.get(i);

                      if ((myTable.getTableName().charAt(0) != '~') && (typeIn.equals(myTable.getTableType()))) {
                         myTables.add(myTable);
                      }
                   }
                }
                if (!myTables.isEmpty()) {
                   return myTables;
                }
             } catch (NullPointerException e) {
             }
          }
       }
       return null;
    }

   public String castExpression(String expressionIn, CsiDataType sourceTypeIn, CsiDataType targetTypeIn) {
      if ((expressionIn != null) && (targetTypeIn != null) && (targetTypeIn != sourceTypeIn)) {
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
/*
    @Override
    public List<SqlTableDef> listFilteredTableDefs(ConnectionDef connectionIn) throws CentrifugeException, GeneralSecurityException {

        List<SqlTableDef> myResults = super.listFilteredTableDefs(connectionIn);
        List<SqlTableDef> myAdditionalResults = listTableDefs(connectionIn, null, null, null);

        if ((null != myAdditionalResults) && !myAdditionalResults.isEmpty()) {

            myResults.addAll(myAdditionalResults);
        }
        return myResults;
    }
*/
}

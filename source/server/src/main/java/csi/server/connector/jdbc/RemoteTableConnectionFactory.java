package csi.server.connector.jdbc;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;

/**
 * Created by centrifuge on 7/7/2015.
 */
public class RemoteTableConnectionFactory extends PostgreSQLConnectionFactory {
   public synchronized List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type)
         throws CentrifugeException, GeneralSecurityException {
      List<SqlTableDef> result = new ArrayList<SqlTableDef>();

      try (Connection conn = this.getConnection(dsdef)) {
         if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rs = meta.getTables(catalog, schema, null, null)) {
               while (rs.next()) {
                  String myType = rs.getString(4);

                  if ("FOREIGN TABLE".equals(myType)) {
                     String myTableName = rs.getString(3);

                     if (authorizedTable(myTableName)) {
                        result.add(new SqlTableDef(rs.getString(1), rs.getString(2), myTableName,
                                                   getAlias(myTableName), myType, null));
                     }
                  }
               }
            }
         }
      } catch (SQLException e) {
         throw new CentrifugeException("Failed to list tables", e);
      }
      return result;
   }
}

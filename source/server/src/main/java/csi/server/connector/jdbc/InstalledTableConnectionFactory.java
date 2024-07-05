package csi.server.connector.jdbc;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import csi.security.queries.AclRequest;
import csi.server.business.helper.DataCacheHelper;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 7/7/2015.
 */
public class InstalledTableConnectionFactory extends CacheConnectionFactory {

    @Override
    public synchronized List<String> listTableTypes(ConnectionDef dsdef)
            throws CentrifugeException, GeneralSecurityException {

        return null;
    }

    @Override
    public synchronized List<String> listTableTypes(ConnectionDef dsdef, String catalogIn, String schemaIn)
            throws CentrifugeException, GeneralSecurityException {

        return null;
    }

    @Override
    public synchronized List<String> listCatalogs(ConnectionDef dsdef)
            throws CentrifugeException, GeneralSecurityException {

        List<String> myList = AclRequest.listInstalledTableOwners();

        return (null == myList) ? new ArrayList<String>(0) : myList;
    }

    @Override
    public synchronized List<String> listExtractedSchemas(ConnectionDef dsdef, String catalog)
            throws CentrifugeException, GeneralSecurityException {

        List<String> myList = AclRequest.listInstalledTableSchemas();

        return (null == myList) ? new ArrayList<String>(0) : myList;
    }

    @Override
    public synchronized List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalogIn,
                                                        String schemaIn, String typeIn)
            throws CentrifugeException, GeneralSecurityException {

        List<InstalledTable> myResults = AclRequest.listAuthorizedInstalledTable(catalogIn, schemaIn, typeIn,
                new AclControlType[]{AclControlType.READ});
        List<SqlTableDef> myList = new ArrayList<SqlTableDef>();

        for (InstalledTable myTable : myResults) {

            myList.add(new SqlTableDef(myTable.getTopLevel(), myTable.getMidLevel(), myTable.getName(),
                                        myTable.getBaseName(), myTable.getLowLevel(), myTable.getUuid()));
        }

        return myList;
    }

    @Override
    public Connection getConnection(ConnectionDef myConnectionDef) throws CentrifugeException {

        return CsiPersistenceManager.getInstalledTableConnection();
    }

    @Override
    public List<ColumnDef> listColumnDefs(ConnectionDef connectionIn, SqlTableDef tableIn) throws CentrifugeException, GeneralSecurityException {

        if (null != tableIn) {

            String myCatalog = tableIn.getCatalogName();
            String mySchema = tableIn.getSchemaName();
            String myUuid = tableIn.getReferenceId();

            if (null != myUuid) {

                InstalledTable myTable = CsiPersistenceManager.findObject(InstalledTable.class, myUuid);

                tableIn.clearColumnList();

                if (null != myTable) {

                    Connection myConnection = CsiPersistenceManager.getCacheConnection();
                    List<InstalledColumn> myColumnListIn = myTable.getColumns();

                    if (myColumnListIn.isEmpty()) {

                        List<String> myColumnNames = DataCacheHelper.getColumnNameList(myConnection, myTable.getTableName());

                        myColumnListIn = AclRequest.linkInstalledColumns(myTable, myColumnNames);
                    }
                    for (InstalledColumn myColumnIn : myColumnListIn) {

                        tableIn.addColumn(new ColumnDef(myColumnIn.getFieldName(), myColumnIn.getType(), myColumnIn.getLocalId(), true));
                    }
                }
                return tableIn.getColumns();

            } else {

                return listColumnDefs(connectionIn, myCatalog, mySchema, tableIn.getTableName());
            }

        } else {

            return new ArrayList<ColumnDef>();
        }
    }

    @Override
    public synchronized List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table) throws CentrifugeException, GeneralSecurityException {

        return super.listColumnDefs(dsdef, null, null, table);
    }

    @Override
    public List<Set<String>> getSourceFilters(ConnectionDef connectionIn) {

        return null;
    }

    @Override
    public String getQualifiedName(SqlTableDef tableDefIn) {

        return Format.value(tableDefIn.getTableName());
    }

    @Override
    public String getEscapeChar() {

        return "\"";
    }
}

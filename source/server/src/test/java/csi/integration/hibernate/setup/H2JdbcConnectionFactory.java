package csi.integration.hibernate.setup;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.connector.jdbc.JdbcConnectionFactory;

/**
 * @author Centrifuge Systems, Inc.
 * Creates a connection to a local h2 database for testing.
 */
public class H2JdbcConnectionFactory extends JdbcConnectionFactory {

    @Override
    public String createConnectString(Map propertiesMap) {
        //jdbc:h2:./db/test;AUTO_SERVER=TRUE

        return "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    }

    //This stubs out the process of creating ColumnDef objects from the ConnectionDef
    @Override
    public synchronized List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table) throws CentrifugeException {
        ColumnDef columnDef = new ColumnDef();
        columnDef.setTableName("TABLE");
        columnDef.setColumnName("c1");
        columnDef.setColumnSize(128);
        columnDef.setCsiType(CsiDataType.Integer);

        ColumnDef columnDef1 = new ColumnDef();
        columnDef1.setTableName("TABLE");
        columnDef1.setColumnName("c2");
        columnDef1.setColumnSize(128);
        columnDef1.setCsiType(CsiDataType.String);

        return Lists.newArrayList(columnDef, columnDef1);

    }
}

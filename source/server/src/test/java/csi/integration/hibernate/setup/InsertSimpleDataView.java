package csi.integration.hibernate.setup;

import java.security.GeneralSecurityException;
import java.util.List;

import com.csi.jdbc.factory.CsiConnectionFactoryException;

import csi.server.business.helper.DataViewFactory;
import csi.server.business.service.DataViewActionsService;
import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.UUID;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;

/**
 * @author Centrifuge Systems, Inc.
 */
public class InsertSimpleDataView {

    private static DataViewActionsService dataViewActionsService = new DataViewActionsService();

    public static String saveNewDataView(String name){
        ConnectionDef connectionDef = setupConnectionDef();
        SqlTableDef sqlTableDef = setupSqlTableDef(connectionDef);

        return tryToSaveDataView(name, sqlTableDef);
    }

    private static String tryToSaveDataView(String name, SqlTableDef sqlTableDef) {
        try {
            return saveDataView(name, sqlTableDef);
        } catch (CentrifugeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String saveDataView(String name, SqlTableDef sqlTableDef) throws CentrifugeException, GeneralSecurityException {
        DataView dv;
        try {
            dv = DataViewFactory.createDataView(name, null, sqlTableDef, null, null, null, true);
            dv = dataViewActionsService.save(dv);
            return dv.getUuid();
        } catch (CsiConnectionFactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    private static SqlTableDef setupSqlTableDef(ConnectionDef connectionIn) {
        SqlTableDef sqlTableDef = new SqlTableDef();
        sqlTableDef.setTableName("SampleTable");
        sqlTableDef.setSource(createBasicSourceDef(connectionIn));
        return sqlTableDef;
    }

    private static ConnectionDef setupConnectionDef() {
        ConnectionDef connectionDef = new ConnectionDef();
        connectionDef.setName("Test");
        connectionDef.setType("CacheData");
        connectionDef.setConnectString("jdbc:h2:./db/test;AUTO_SERVER=TRUE");
        connectionDef.setProperties(new GenericProperties());
        return connectionDef;
    }


    private static DataSourceDef createBasicSourceDef(ConnectionDef connectionIn)  {

        DataSourceDef mySourceDef = new DataSourceDef();
        String myName = connectionIn.getType() + "_1";
        CsiMap<String, String> myProperties = new CsiMap<String, String>();

        myProperties.put("color", "13593813");
        mySourceDef.setOrdinal(0);
        mySourceDef.setClientProperties(myProperties);
        mySourceDef.setName(myName);
        mySourceDef.setLocalId(UUID.randomUUID().toString().toLowerCase());
        mySourceDef.setConnection(connectionIn);
        return mySourceDef;
    }

}

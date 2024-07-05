package csi.server.connector.simple_loaders;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.util.StringUtil;
import csi.server.connector.AbstractConnectionFactory;

/**
 * Created by centrifuge on 1/15/2019.
 */
public class LoaderFactory extends AbstractConnectionFactory {
    @Override
    public boolean isSimpleLoader() {
        return true;
    }

    @Override
    public Properties toNativeProperties(Map<String, String> propMap) {
        return null;
    }

    @Override
    public Connection getConnection(String url, Properties props) throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException {
        return null;
    }

    @Override
    public Connection getConnection(Map<String, String> propMap) throws SQLException, GeneralSecurityException, ClassNotFoundException, CentrifugeException {
        return null;
    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        return null;
    }

    @Override
    public List<String> listTableTypes(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        return null;
    }

    @Override
    public List<String> listCatalogs(ConnectionDef dsdef) throws CentrifugeException, GeneralSecurityException {
        return null;
    }

    @Override
    public List<CsiMap<String, String>> listSchemas(ConnectionDef dsdef, String catalog) throws CentrifugeException, GeneralSecurityException {
        return null;
    }

    @Override
    public List<SqlTableDef> listTableDefs(ConnectionDef connectionDefIn, String catalogIn, String schemaIn, String typeIn)
            throws CentrifugeException, GeneralSecurityException {

        List<SqlTableDef> myList = new ArrayList<SqlTableDef>();
        GenericProperties myProperties = connectionDefIn.getProperties();
        String myUser = myProperties.get("user");
        String myFileType = myProperties.get("filetype");
        String myFileName = myProperties.get("basename");

        myList.add(new SqlTableDef(myUser, myFileType, myFileName, null, "TABLE", null));

        return myList;
    }

    @Override
    public List<ColumnDef> listColumnDefs(ConnectionDef connectionDefIn, String catalog, String schema, String table)
            throws CentrifugeException, GeneralSecurityException {

        List<ColumnDef> myList = new ArrayList<ColumnDef>();
        GenericProperties myProperties = connectionDefIn.getProperties();
        String myColumnInfo = myProperties.get("columns");

        for (String myColumn : StringUtil.split(myColumnInfo, '\n')) {

            String[] myPair = StringUtil.split(myColumn, '|');

            myList.add(new ColumnDef(myPair[0], CsiDataType.getMatchingType(myPair[1]), null, true));
        }
        return myList;
    }
}

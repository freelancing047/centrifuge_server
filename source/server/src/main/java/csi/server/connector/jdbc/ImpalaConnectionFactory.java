package csi.server.connector.jdbc;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;

public class ImpalaConnectionFactory extends JdbcConnectionFactory {
   private static final Logger LOG = LogManager.getLogger(ImpalaConnectionFactory.class);

    private static final String KERBEROS_REALM_KEY = "impala.kerberos.realm";//NON-NLS
    private static final String USERNAME_KEY = "impala.username";//NON-NLS
    private static final String PASSWORD_KEY = "impala.password";//NON-NLS
    private static final String KERBEROS_HOST_KEY = "impala.kerberos.host";//NON-NLS
    private static final String KERBEROS_SERVICE_KEY = "impala.kerberos.service";//NON-NLS

    private String defaultSchema = null;

    public ImpalaConnectionFactory() {
		super();
        defaultSchema = (String)getDefaultProperties().get("csi.internal.defaultSchema");
    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        String host = propertiesMap.get(CSI_HOSTNAME);
        if ((host == null) || host.isEmpty()) {
            throw new RuntimeException("Missing required property " + CSI_HOSTNAME);
        }

        String port = propertiesMap.get(CSI_PORT);
        if ((port == null) || port.isEmpty()) {
            port = "21050";
        }

        StringBuilder sb = new StringBuilder(getUrlPrefix());
        sb.append(host);
        if (!port.isEmpty()) {
            sb.append(':').append(port);
        }

        int authMech = 0;
        try {
            authMech = Integer.parseInt(getDefaultProperties().get("csi.internal.authMech").toString());//NON-NLS
        } catch (NumberFormatException e) {
           LOG.warn("Failed to parse authentication mechanism. Proceeding using no authentication");
        }
        try {
            defaultSchema = (String)getDefaultProperties().get("csi.internal.defaultSchema");   //NON-NLS
        } catch (NumberFormatException e) {
           LOG.warn("Failed to identify default schema name.");
        }

        //TODO: SSL
        //jdbc:impala://localhost:21050;AuthMech=1;SSL=1;SSLKeyStore=C:\\Users\\bsmith\\Desktop\\keystore.jks;SSLKeyStorePwd=*****;UID=impala;PWD=*****
        String username = propertiesMap.get(USERNAME_KEY);
        String password = propertiesMap.get(PASSWORD_KEY);
        String krbRealm = propertiesMap.get(KERBEROS_REALM_KEY);
        String krbHost = propertiesMap.get(KERBEROS_HOST_KEY);
        String krbServiceName = propertiesMap.get(KERBEROS_SERVICE_KEY);
        switch (authMech) {
            case 0:
                sb.append(";AuthMech=0");//NON-NLS
                break;
            case 1:
                //AuthMech=1;KrbRealm=EXAMPLE.COM;KrbHostFQDN=impala.example.com;KrbServiceName=impala

                sb.append(";AuthMech=1;KrbRealm=" + krbRealm + ";KrbHostFQDN=" + krbHost + ";KrbServiceName=" + krbServiceName);//NON-NLS
                break;
            case 2:
                //AuthMech=2;UID=impala
                sb.append(";AuthMech=2;UID=").append(username);//NON-NLS
                break;
            case 3:
                //AuthMech=3;UID=impala;PWD=*****

                sb.append(";AuthMech=3;UID=").append(username).append(";PWD=").append(password);//NON-NLS
                break;
        }
        return sb.toString();
    }

    @Override
    public synchronized List<SqlTableDef> listTableDefs(ConnectionDef dsdef, String catalog, String schema, String type) throws CentrifugeException, GeneralSecurityException {

        Connection conn = null;
        try {
            conn = getConnection(dsdef);
            if (conn == null) {
                return Lists.newArrayList();
            }
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, null, null);
            return formatTableList(dsdef, rs);
        } catch (SQLException e) {
            throw new CentrifugeException("Failed to list tables", e);
        } finally {
            SqlUtil.quietCloseConnection(conn);
        }
    }

    @Override
    public synchronized List<ColumnDef> listColumnDefs(ConnectionDef dsdef, String catalog, String schema, String table) throws CentrifugeException, GeneralSecurityException {
        Connection conn = null;
        try {
            ResultSet rs = null;

            conn = getConnection(dsdef);
            if (conn == null) {
                return Lists.newArrayList();
            }
            DatabaseMetaData meta = conn.getMetaData();

            schema = schema == null ? "" : schema;
            rs = meta.getColumns(catalog, schema, table, null);

            List<ColumnDef> list = Lists.newArrayList();
            while (rs.next()) {
                ColumnDef col = createColumnDef(rs);
                list.add(col);
            }
            return list;
        } catch (SQLException e) {
            throw new CentrifugeException("Failed to list tables", e);
        } finally {
            SqlUtil.quietCloseConnection(conn);
        }
    }

    private ColumnDef createColumnDef(ResultSet rs) throws SQLException {
        ColumnDef col = new ColumnDef();
        col.setCatalogName(rs.getString(1));
        col.setSchemaName(rs.getString(2));
        col.setTableName(rs.getString(3));
        String colname = rs.getString(4);
        col.setColumnName(colname);
        col.setLocalId(UUID.randomUUID().toString().toLowerCase());
        col.setJdbcDataType(rs.getInt(5));
        col.setDataTypeName(rs.getString(6));
        col.setColumnSize(rs.getInt(7));
        col.setDecimalDigits(rs.getInt(9));
        col.setDefaultValue(rs.getString(13));
        col.setOrdinal(rs.getInt(17));
        col.setNullable(rs.getString(18));
        CsiDataType csiType = CacheUtil.resolveCsiType(col.getDataTypeName(), col.getJdbcDataType(), this);
        col.setCsiType(csiType);
        return col;
    }

    @Override
    public String getQualifiedName(SqlTableDef tableIn) {

        if ((null == defaultSchema) || (!defaultSchema.equals(tableIn.getSchemaName()))) {

            return super.getQualifiedName(tableIn);

        } else {

            return getQuotedName(tableIn.getTableName());
        }
    }
}

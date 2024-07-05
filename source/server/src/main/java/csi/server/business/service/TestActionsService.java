package csi.server.business.service;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.XStream;

import csi.config.Configuration;
import csi.security.CsiSecurityManager;
import csi.server.business.cachedb.dataset.DataSetProcessor;
import csi.server.business.cachedb.dataset.DataSetUtil;
import csi.server.business.cachedb.querybuilder.DataSetQueryBuilder;
import csi.server.business.helper.ConnectionHelper;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.common.codec.xstream.converter.CsiUUIDSingleValueConverter;
import csi.server.common.codec.xstream.converter.DateSingleValueConverter;
import csi.server.common.codec.xstream.converter.ResultConverter;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.ConnectionTest;
import csi.server.common.dto.ConnectionTestResults;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.CustomQueryDO;
import csi.server.common.dto.KeyValueItem;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.PreviewResponse;
import csi.server.common.dto.Response;
import csi.server.common.dto.TestQueryResponse;
import csi.server.common.dto.SelectionListData.DriverBasics;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.LogicalQuery;
import csi.server.common.model.Resource;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryInterceptorDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.service.api.TestActionsServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.SystemParameters;
import csi.server.connector.AbstractConnectionFactory;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.connector.config.JdbcDriver;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;
import csi.server.util.CsiUtil;
import csi.server.util.DateUtil;
import csi.server.util.FieldReferenceValidator.ValidationException;
import csi.server.util.SqlUtil;

@Service(path = "/actions/test")
public class TestActionsService extends AbstractService implements TestActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(TestActionsService.class);

    public static final String AUTHENTICATION_REQUIRED = "Authentication required";

    public static final String PASSWORD_REQUIRED = "Password required for ";

    public static final String CONNECTION_SUCCEEDED = "Connection succeeded!";

    public static final String CONNECTION_FAILED = "Connection failed!";

    public static final String DRIVER_NOT_FOUND = "JDBC driver not found!";

    public static final String TEST_QUERY_SUCCEEDED = "Query succeeded!";

    public static final String TEST_QUERY_FAILED = "Query failed!";

    public static final String DATAVIEW = "dataview";

    public static final String TEMPLATE = "template";

    public static final int PREVIEW_ROW_SIZE = 3;

    public TestActionsService() {
        super();
    }

    @Override
    public void initMarshaller(XStream xstream) {
        xstream.alias("String", String.class);

        xstream.registerConverter(new DateSingleValueConverter());
        xstream.registerConverter(new CsiUUIDSingleValueConverter());
        xstream.registerConverter(new ResultConverter(xstream.getMapper()));
    }

    @Operation
    @Interruptable
    public List<KeyValueItem> listConnectionTypes() {
        List<KeyValueItem> validTypes = new ArrayList<KeyValueItem>();

        for (JdbcDriver driver : Configuration.getInstance().getDbConfig().getDrivers().getDrivers()) {
            // **** Cancellation check point ****
            TaskHelper.checkForCancel();

            try {
                if (CsiSecurityManager.canCreateConnectionType(driver.getKey(), null)) {
                    validTypes.add(new KeyValueItem(driver.getKey(), driver.getName()));
                }
            } catch (CentrifugeException e) {
                LOG.warn("Failed to check authorization for connection type: " + driver.getKey(), e);
            }
        }

        return validTypes;
    }

    @Operation
    @Interruptable
    public List<DriverBasics> listConnectionDescriptors() throws CentrifugeException {

        return ConnectionHelper.listConnectionDescriptors();
    }

    @Operation
    @Interruptable
    public boolean isAuthorizedForConnection(@QueryParam(value = "type") String type,
            @QueryParam(value = "url") String url) throws CentrifugeException {

        if ((type == null) || (type.trim().length() == 0)) {
            throw new CentrifugeException("Missing required parameter 'type'.");
        }

        return CsiSecurityManager.canCreateConnectionType(type, url);
    }

    @Operation
    @Interruptable
    public CsiMap<String, String> testConnection(@PayloadParam ConnectionDef dsdef) {
        CsiMap<String, String> response = new CsiMap<String, String>();

        if (Configuration.getInstance().getApplicationConfig().isDisableTestConnections()) {
            response.put("testResult", CONNECTION_SUCCEEDED);
            return response;
        }

        Connection sqlConn = null;
        try {
            TaskHelper.checkForCancel();

            ConnectionFactory factory = ConnectionFactoryManager.getInstance().getConnectionFactory(dsdef);
            if (factory == null) {
                throw new CentrifugeException(DRIVER_NOT_FOUND);
            }
            sqlConn = factory.getConnection(dsdef);
            if (sqlConn != null) {
                response.put("testResult", CONNECTION_SUCCEEDED);
            } else {
                response.put("testResult", CONNECTION_FAILED);
            }
        } catch (GeneralSecurityException gse) {
            String myForce = AbstractConnectionFactory.forceUsername(dsdef);
            if (null != myForce) {
                response.put("testResult", PASSWORD_REQUIRED + myForce);
            } else {
                response.put("testResult", AUTHENTICATION_REQUIRED);
            }
        } catch (CentrifugeException e) {
            LOG.info("Test connection failed: ", e);

            Throwable cause = e.getCause();
            if (cause != null) {
                response.put("exceptionCause", cause.toString());
            }

            if (e.getMessage().contains(ConnectionFactoryManager.FAILED_CONNECTION_FACTORY_PREFIX)) {
                response.put("testResult", DRIVER_NOT_FOUND);
            } else {
                response.put("testResult", CONNECTION_FAILED);
            }
        } finally {
            SqlUtil.quietCloseConnection(sqlConn);
        }

        return response;
    }

    @Operation
    @Interruptable
    public ConnectionTestResults testAllSources(@QueryParam("uuid") String uuid) throws CentrifugeException {

        DataViewDef def = null;
        Object target = CsiPersistenceManager.findObject(Resource.class, uuid);

        if (target instanceof DataView) {
            DataView dv = CsiPersistenceManager.findObject(DataView.class, uuid);
            def = dv.getMeta();
        } else if (target instanceof DataViewDef) {
            def = CsiPersistenceManager.findObject(DataViewDef.class, uuid);
        }

        List<DataSourceDef> sources = def.getDataSources();
        ConnectionTestResults result = new ConnectionTestResults();

        if (sources == null) {
            return result;
        }

        for (DataSourceDef dsdef : sources) {

            ConnectionTest connectionTest = new ConnectionTest();
            connectionTest.connection = dsdef.getConnection();

            HashMap<String, String> status = testConnection(connectionTest.connection);
            String testResult = status.get("testResult");
            String exceptionCause = status.get("exceptionCause");

            if (exceptionCause != null) {
                connectionTest.failureCause = status.get("exceptionCause");
            }

            if (testResult.equals(AUTHENTICATION_REQUIRED)
                    || testResult.startsWith(PASSWORD_REQUIRED)) {
                connectionTest.dsLocalId = dsdef.getLocalId();
                result.authRequired.add(connectionTest);
            } else if (testResult.equals(CONNECTION_FAILED)) {
                result.failedConnections.add(connectionTest);
            } else if (testResult.equals(DRIVER_NOT_FOUND)) {
                result.failedDrivers.add(connectionTest);
            }
        }

        return result;
    }

    @Operation
    @Interruptable
    public Response<String, List<QueryParameterDef>> getLaunchRequirements(String uuidIn, List<AuthDO> credentialsIn) {

        try {

            DataViewDef myTemplate = null;
            Object myTarget = CsiPersistenceManager.findObject(Resource.class, uuidIn);

            if (myTarget instanceof DataView) {
                DataView myDataView = CsiPersistenceManager.findObject(DataView.class, uuidIn);
                myTemplate = myDataView.getMeta();
            } else if (myTarget instanceof DataViewDef) {

                myTemplate = CsiPersistenceManager.findObject(DataViewDef.class, uuidIn);
            }

            return getLaunchRequirements(myTemplate, credentialsIn);

        } catch (Exception myException) {

            return new Response<String, List<QueryParameterDef>>(uuidIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Operation
    @Interruptable
    public Response<String, List<QueryParameterDef>> getLaunchRequirements(DataView dataviewIn, List<AuthDO> credentialsIn) {

        return getLaunchRequirements(dataviewIn.getMeta(), credentialsIn);
    }

    @Operation
    @Interruptable
    public Response<String, List<QueryParameterDef>> getLaunchRequirements(DataViewDef templateIn, List<AuthDO> credentialsIn) {

        try {

            return new Response<String, List<QueryParameterDef>>(templateIn.getUuid(),
                    DataViewHelper.listAuthorizationsRequired(templateIn.getDataSources(), credentialsIn),
                    templateIn.getRequiredParameters());
        } catch (Exception myException) {

            return new Response<String, List<QueryParameterDef>>(templateIn.getUuid(), ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Operation
    @Interruptable
    public Response<String, List<QueryParameterDef>> getLaunchRequirements(AdHocDataSource sourceIn, List<AuthDO> credentialsIn) {

        try {

            return new Response<String, List<QueryParameterDef>>(sourceIn.getUuid(),
                                                                    DataViewHelper.listAuthorizationsRequired(
                                                                            sourceIn.getDataSources(), credentialsIn),
                                                                    sourceIn.getRequiredParameters());
        } catch (Exception myException) {

            return new Response<String, List<QueryParameterDef>>(sourceIn.getUuid(), ServerMessage.CAUGHT_EXCEPTION,
                                                                    Format.value(myException));
        }
    }
/*
    @Operation
    @Interruptable
    public ConnectionTestResults testAllConnections(@QueryParam("uuid") String uuid,
            @QueryParam("templateName") String templateName, @QueryParam("dvname") String dvname)
            throws CentrifugeException {

        ConnectionTestResults result = new ConnectionTestResults();

        DataViewDef dvdef = ModelHelper.resolveToTemplate(uuid, templateName, dvname);

        DataSetOp rootOp = dvdef.getDataTree();
        if (null == rootOp) {
            throw new ValidationException("Invalid dataview.  No data operations defined.");
        }

        Set<DataSourceDef> sources = DataSetUtil.getDistinctSources(rootOp);

        for (DataSourceDef dsdef : sources) {
            ConnectionTest connectionTest = new ConnectionTest();
            connectionTest.connection = dsdef.getConnection();
            HashMap<String, String> status = testConnection(connectionTest.connection);

            String testResult = status.get("testResult");
            String exceptionCause = status.get("exceptionCause");

            if (exceptionCause != null) {
                connectionTest.failureCause = status.get("exceptionCause");
            }

            if (testResult.equals(AUTHENTICATION_REQUIRED)
                    || testResult.startsWith(PASSWORD_REQUIRED)) {
                connectionTest.dsLocalId = dsdef.getLocalId();
                result.authRequired.add(connectionTest);
            } else if (status.equals(CONNECTION_FAILED)) {
                result.failedDrivers.add(connectionTest);

            } else if (status.equals(DRIVER_NOT_FOUND)) {
                result.failedConnections.add(connectionTest);
            }
        }

        return result;
    }
*/
    @Operation
    @Interruptable
    public CsiMap<String, String> testConnections(@PayloadParam DataViewDef dvdef) {

        DataSetOp rootOp = dvdef.getDataTree();
        if (null == rootOp) {
            throw new ValidationException("Invalid dataview.  No data operations defined.");
        }
        return testDataSetConnections(rootOp);
    }

    @Operation
    @Interruptable
    public CsiMap<String, String> testDataSetConnections(@PayloadParam DataSetOp op) {
        CsiMap<String, String> statusMap = new CsiMap<String, String>();

        Set<DataSourceDef> sources;
        try {
            sources = DataSetUtil.getDistinctSources(op);
        } catch (CentrifugeException e) {
            throw Throwables.propagate(e);
        }

        for (DataSourceDef dsdef : sources) {
            ConnectionDef conndef = dsdef.getConnection();
            HashMap<String, String> status = testConnection(conndef);
            statusMap.put(dsdef.getUuid(), status.get("testResult"));
        }

        return statusMap;
    }

    @Operation
    public QueryDef createCustomQuery(SqlTableDef tableDefIn, List<String> requiredColumnsIn,
                                      List<QueryParameterDef> parametersIn) throws CentrifugeException {

        DataSourceDef myDataSource = tableDefIn.getSource();
        ConnectionDef myConnection = myDataSource.getConnection();
        ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(myConnection);

        DataSetQueryBuilder myBuilder = new DataSetQueryBuilder(myFactory, parametersIn);

        return myBuilder.createCustomQuery(tableDefIn, requiredColumnsIn, true);
    }

    private static List<QueryParameterDef> ensureSystemParametersInQueryParameters(final List<QueryParameterDef> rawParameters) {
       Map<String,QueryParameterDef> params = new HashMap<String,QueryParameterDef>();

       if (rawParameters != null) {
          List<QueryParameterDef> systemParams = SystemParameters.getList();

          for (QueryParameterDef param : DataViewHelper.cloneParameters(rawParameters)) {
             params.put(param.getName(), param);
          }
          for (QueryParameterDef systemParam : systemParams) {
             if (params.containsKey(systemParam.getName())) {
                QueryParameterDef paramFromSet = params.get(systemParam.getName());

                if (paramFromSet.getValue() == null) {
                   paramFromSet.setValues(systemParam.getValues());
                }
             } else {
                params.put(systemParam.getName(), systemParam);
             }
          }
       }
       return new ArrayList<QueryParameterDef>(params.values());
    }

    @Operation
    /**
     * TestQueryResponse includes the fully populated TableDef.
     */
    public TestQueryResponse testCustomQuery(@PayloadParam CustomQueryDO customQuery) throws CentrifugeException {
        ConnectionDef connDef = customQuery.getConnection();
        ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(connDef);

        if (myFactory.getBlockCustomQueries().booleanValue()) {

            throw new CentrifugeException("Custom query not allowed with this data source!");

        } else {
            QueryDef qdef = customQuery.getQuery();
            List<QueryParameterDef> myParameters = DataViewHelper.cloneParameters(customQuery.getParameters());

            //TODO: hack due to customQuery.getParameters() being corrupted: Only contains (@USER = null)
            myParameters = ensureSystemParametersInQueryParameters(myParameters);

            List<LaunchParam> myLaunchData = customQuery.getParameterValues();

            TestQueryResponse resp = new TestQueryResponse();

            String sql = qdef.getQueryText();
            qdef.setLinkupText(QueryHelper.genLinkupQuery(sql));
            if ((sql == null) || sql.trim().isEmpty()) {
                resp.setQueryFailed(true);
                resp.setErrorMsg("Missing SQL query");
                return resp;
            }

            ConnectionFactory factory = ConnectionFactoryManager.getInstance().getConnectionFactory(connDef);

            Connection conn = null;
            try {
                conn = factory.getConnection(connDef);
            } catch (GeneralSecurityException e) {
                SqlUtil.quietCloseConnection(conn);
                resp.setRequiresAuth(true);
                resp.setErrorMsg(e.getMessage());
                resp.setErrorTrace(CsiUtil.getStackTraceString(e));
                return resp;
            } catch (CentrifugeException e) {
                SqlUtil.quietCloseConnection(conn);
                resp.setConnectionFailed(true);
                resp.setErrorMsg(e.getMessage());
                resp.setErrorTrace(CsiUtil.getStackTraceString(e));
                return resp;
            }

            if (conn == null) {
                SqlUtil.quietCloseConnection(conn);
                resp.setConnectionFailed(true);
                resp.setErrorMsg("Could not create connection");
                return resp;
            }

            ResultSet rs = null;
            String preSql = null;
            String mainSql = null;
            String postSql = null;
            try {
                ParameterSetFactory myParameterFactory = new ParameterSetFactory(myParameters, myLaunchData);
                DataSetQueryBuilder builder = new DataSetQueryBuilder(factory, myParameterFactory);
                preSql = builder.applySqlParameters(connDef.getPreSql(), myParameterFactory.getParameterList());
                mainSql = builder.applySqlParameters(sql, myParameterFactory.getParameterList());
                postSql = builder.applySqlParameters(connDef.getPostSql(), myParameterFactory.getParameterList());

                QueryHelper.executeSQL(conn, preSql, myParameters);
                rs = QueryHelper.executeSingleQuery(conn, mainSql, myParameters, 1, 1);

                List<ColumnDef> columns = new ArrayList<ColumnDef>();
                SqlTableDef table = new SqlTableDef();
                table.setIsCustom(true);
                table.setCustomQuery(qdef);
                table.setColumns(columns);
                table.setTableName(qdef.getName());

                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    ColumnDef col = new ColumnDef();
                    col.setLocalId(UUID.randomUUID().toString().toLowerCase());
                    try {
                        col.setCatalogName(rsMeta.getCatalogName(i));
                    } catch (SQLException s) {
                        LOG.warn("rsMeta.getCatalogName(i) method not supported for " + connDef.getType());
                    }
                    try {
                        col.setSchemaName(rsMeta.getSchemaName(i));
                    } catch (SQLException s) {
                        LOG.warn("rsMeta.getSchemaName(i) method not supported for " + connDef.getType());
                    }
                    try {
                        col.setTableName(rsMeta.getTableName(i));
                    } catch (SQLException s) {
                        LOG.warn("rsMeta.getTableName(i) method not supported for " + connDef.getType());
                    }
                    try {
                        col.setColumnName(rsMeta.getColumnLabel(i));
                    } catch (SQLException s) {
                        LOG.warn("rsMeta.getColumnLabel(i) method not supported for " + connDef.getType());
                    }
                    try {
                        col.setJdbcDataType(rsMeta.getColumnType(i));
                    } catch (SQLException s) {
                        LOG.warn("rsMeta.getColumnType(i) method not supported for " + connDef.getType());
                    }
                    try {
                        col.setDataTypeName(rsMeta.getColumnTypeName(i));
                    } catch (SQLException s) {
                        LOG.warn("rrsMeta.getColumnTypeName(i) method not supported for " + connDef.getType());
                    }
                    try {
                        col.setColumnSize(rsMeta.getPrecision(i));
                    } catch (SQLException s) {
                        LOG.warn("rsMeta.getPrecision(i) method not supported for " + connDef.getType());
                    }
                    try {
                        col.setDecimalDigits(rsMeta.getScale(i));
                    } catch (SQLException s) {
                        LOG.warn("rsMeta.getScale(i) method not supported for " + connDef.getType());
                    }
                    col.setDefaultValue(null);
                    col.setOrdinal(i);
                    col.setNullable(null);
                    CsiDataType csiType = CacheUtil.resolveCsiType(col.getDataTypeName(),
                                                                    col.getJdbcDataType(), factory);
                    col.setCsiType(csiType);
                    col.setTableDef(table);
                    col.setSelected(true);
                    columns.add(col);
                }

                SqlUtil.quietCloseResulSet(rs);

                try {
                    QueryHelper.executeSQL(conn, postSql, myParameters);
                } catch (Exception e) {
                    resp.setQueryFailed(true);
                    resp.setErrorMsg(e.getMessage());
                    resp.setErrorTrace(CsiUtil.getStackTraceString(e));
                    LOG.error("Failed to execute post sql commands", e);
                }
                resp.setTableDef(table);
                resp.setSuccess(true);

            } catch (SQLException e) {
                resp.setQueryFailed(true);
                resp.setErrorMsg(e.getMessage());
                resp.setErrorTrace(CsiUtil.getStackTraceString(e));
                LOG.error("Test query failed.", e);
            } catch (CentrifugeException e) {
                resp.setQueryFailed(true);
                resp.setErrorMsg(e.getMessage());
                resp.setErrorTrace(CsiUtil.getStackTraceString(e));
                LOG.error("Test query failed.", e);
            } finally {
                SqlUtil.quietCloseResulSet(rs);
                SqlUtil.quietCloseConnection(conn);
            }
            return resp;
        }
    }
/*
    @Operation
    public TableSelectionSetDTO getTables(ConnectionDef conDef) throws CentrifugeException {
        TableSelectionSetDTO tssDto = new TableSelectionSetDTO();
        ConnectionFactory factory = ConnectionFactoryManager.getInstance().getConnectionFactory(conDef);
*/
        /* each list in the loop must be populated
         * factory.listTableDefs(conDef, catalog, schema, type)
         * handles nulls not part of query */
    /*
        List<String> catalogs = factory.listCatalogs(conDef);
        if (catalogs.isEmpty()) {
            catalogs.add(null);
        }
        tssDto.setCatalogs(catalogs);

        List<String> tableTypes = factory.listTableTypes(conDef);
        if (tableTypes.isEmpty()) {
            tableTypes.add(null);
        }
        tssDto.setTableTypes(tableTypes);

        for (String catalog : catalogs) {
            for (String tableType : tableTypes) {
                // FIXME: Add schema
                String schema = null;
                List<SqlTableDef> plainOldTableDefs = factory.listTableDefs(conDef, catalog, schema, tableType);
                for (SqlTableDef plainOldTableDef : plainOldTableDefs) {
                    TableSelectionItemDTO tsiDto = new TableSelectionItemDTO(catalog, schema, tableType,
                            plainOldTableDef);
                    tssDto.addTableDefDto(tsiDto);
                }
            }
        }

        return tssDto;
    }

    /**
     * @deprecated replaced by getTables()
     */

    @Operation
    @Interruptable
    @Deprecated
    public Response<String, List<String>> listTableTypes(ConnectionDef connectionIn, String catalogIn, String schemaIn, String keyIn) {

        try {

            ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);
            List<Set<String>> myFilters = myFactory.getSourceFilters(connectionIn);
            Set<String> myCatalogFilters = ((myFilters != null) && !myFilters.isEmpty()) ? myFilters.get(0) : null;
            Set<String> mySchemaFilters = ((myFilters != null) && (1 < myFilters.size())) ? myFilters.get(1) : null;
            Set<String> myOutputFilters = ((myFilters != null) && (2 < myFilters.size())) ? myFilters.get(2) : null;

            if (stringOK(catalogIn, myCatalogFilters) && stringOK(schemaIn, mySchemaFilters)) {

                return new Response<String, List<String>>(keyIn, filterList(myFactory.listTableTypes(connectionIn,
                                                                                catalogIn, schemaIn), myOutputFilters));
            } else {

                return new Response<String, List<String>>(keyIn, new ArrayList<String>(0));
            }

        } catch (GeneralSecurityException myException) {

            return new Response<String, List<String>>(keyIn, true);

        } catch (Exception myException) {

            return new Response<String, List<String>>(keyIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Operation
    @Interruptable
    public Response<String, List<String>> listCatalogs(ConnectionDef connectionIn, String keyIn) {

        try {

            ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);
            List<Set<String>> myFilters = myFactory.getSourceFilters(connectionIn);
            Set<String> myOutputFilters = ((null != myFilters) && !myFilters.isEmpty()) ? myFilters.get(0) : null;

            return new Response<String, List<String>>(keyIn, filterList(myFactory.listCatalogs(connectionIn), myOutputFilters));

        } catch (GeneralSecurityException myException) {

            return new Response<String, List<String>>(keyIn, true);

        } catch (Exception myException) {

            return new Response<String, List<String>>(keyIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }
/*
    @Deprecated
    @Operation
    @Interruptable
    public Response<String, List<CsiMap<String, String>>> listSchemaMap(String catalog, ConnectionDef connectionIn, String keyIn) {

        try {

            ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);
            List<List<String>> myFilters = myFactory.getSourceFilters(connectionIn);
            List<CsiMap<String, String>> toReturn = myFactory.listSchemas(connectionIn, catalog);
            return new Response<String, List<CsiMap<String, String>>>(keyIn, toReturn);

        } catch (GeneralSecurityException myException) {

            return new Response<String, List<CsiMap<String, String>>>(keyIn, true);

        } catch (Exception myException) {

            return new Response<String, List<CsiMap<String, String>>>(keyIn, ServerMessage.CAUGHT_EXCEPTION, Display.value(myException));
        }
    }
*/
    @Operation
    @Interruptable
    public Response<String, List<String>> listSchemas(ConnectionDef connectionIn, String catalogIn, String keyIn) {

        try {

            ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);
            List<Set<String>> myFilters = myFactory.getSourceFilters(connectionIn);
            Set<String> myInputFilters = ((myFilters != null) && !myFilters.isEmpty()) ? myFilters.get(0) : null;
            Set<String> myOutputFilters = ((myFilters != null) && (1 < myFilters.size())) ? myFilters.get(1) : null;

            if (stringOK(catalogIn, myInputFilters)) {

                return new Response<String, List<String>>(keyIn, filterList(myFactory.listExtractedSchemas(connectionIn, catalogIn), myOutputFilters));

            } else {

                return new Response<String, List<String>>(keyIn, new ArrayList<String>());
            }

        } catch (GeneralSecurityException myException) {

            return new Response<String, List<String>>(keyIn, true);

        } catch (Exception myException) {

            return new Response<String, List<String>>(keyIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Operation
    @Interruptable
    public Response<String, List<ColumnDef>> listTableColumns(ConnectionDef connectionIn, SqlTableDef tableIn, String keyIn) {

        try {

            ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);
            List<Set<String>> myFilters = myFactory.getSourceFilters(connectionIn);
            Set<String> myCatalogFilters = ((myFilters != null) && !myFilters.isEmpty()) ? myFilters.get(0) : null;
            Set<String> mySchemaFilters = ((myFilters != null) && (1 < myFilters.size())) ? myFilters.get(1) : null;
            String myCatalog = tableIn.getCatalogName();
            String mySchema = tableIn.getSchemaName();

            if (stringOK(myCatalog, myCatalogFilters) && stringOK(mySchema, mySchemaFilters)) {

                return new Response<String, List<ColumnDef>>(keyIn, myFactory.listColumnDefs(connectionIn, tableIn));

            } else {

                return new Response<String, List<ColumnDef>>(keyIn, new ArrayList<ColumnDef>(0));
            }

        } catch (GeneralSecurityException myException) {

            return new Response<String, List<ColumnDef>>(keyIn, true);

        } catch (Exception myException) {

            return new Response<String, List<ColumnDef>>(keyIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }

    @Operation
    @Interruptable
    public Response<String, List<SqlTableDef>> listTableDefs(ConnectionDef connectionIn, String catalogIn,
                                                              String schemaIn, String typeIn, String keyIn) {

        try {

            ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);
            List<Set<String>> myFilters = myFactory.getSourceFilters(connectionIn);
            Set<String> myCatalogFilters = ((myFilters != null) && !myFilters.isEmpty()) ? myFilters.get(0) : null;
            Set<String> mySchemaFilters = ((myFilters != null) && (1 < myFilters.size())) ? myFilters.get(1) : null;
            Set<String> myTableTypeFilters = ((myFilters != null) && (2 < myFilters.size())) ? myFilters.get(2) : null;

            if (stringOK(catalogIn, myCatalogFilters)
                    && stringOK(schemaIn, mySchemaFilters) && stringOK(typeIn, myTableTypeFilters)) {

                return new Response<String, List<SqlTableDef>>(keyIn,
                                                myFactory.listTableDefs(connectionIn, catalogIn, schemaIn, typeIn));

            } else {

                return new Response<String, List<SqlTableDef>>(keyIn, new ArrayList<SqlTableDef>(0));
            }

        } catch (GeneralSecurityException myException) {

            return new Response<String, List<SqlTableDef>>(keyIn, true);

        } catch (Exception myException) {

            return new Response<String, List<SqlTableDef>>(keyIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
        }
    }
/*
    @Operation
    @Interruptable
    public List<ColumnDef> listColumnDefs(@PayloadParam ConnectionDef connectionIn,
            @QueryParam(value = "catalog") String catalog, @QueryParam(value = "schema") String schema,
            @QueryParam(value = "table") String table) throws CentrifugeException {

        ConnectionFactory factory = ConnectionFactoryManager.getInstance().getConnectionFactory(connectionIn);
        return factory.listColumnDefs(connectionIn, catalog, schema, table);
    }
*/
    @Operation
    @Interruptable
    public List<String> listQueryParameters(@PayloadParam QueryDef queryDef) {
        List<String> names = QueryHelper.listParameterNames(queryDef.getQueryText());
        for (QueryInterceptorDef qi : queryDef.getInterceptors()) {
            names.addAll(QueryHelper.listParameterNames(qi.getQueryText()));
        }

        return names;
    }
    /*
        @Operation
        @Interruptable
        public List<QueryParameterDef> listDVRequiredParameters(@QueryParam(value = "dvUuid") String dvUuid)
                throws CentrifugeException {

            DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
            if (dv == null) {
                throw new CentrifugeException("Dataview not found: " + dvUuid);
            }

            DataViewDef dvdef = dv.getMeta();

            DataSetOp rootOp = dvdef.getDataTree();
            if (null == rootOp) {
                throw new CentrifugeException("No data operations found in dataview: " + dv.getName());
            }

            return listRequiredParameters(dvdef);
        }

        @Operation
        @Interruptable
        public List<QueryParameterDef> listRequiredParameters(@PayloadParam DataViewDef dvdef) {
            List<QueryParameterDef> requiredList = new ArrayList<QueryParameterDef>();

            DataSetOp rootOp = dvdef.getDataTree();
            if (null == rootOp) {
                throw Throwables.propagate(new CsiClientException("No data operations found."));
            }

            // **** Cancellation check point ****
            TaskHelper.checkForCancel();

            Set<QueryParameterDef> requiredSet;
            try {
                requiredSet = DataSetUtil.listRequiredParameters(rootOp, dvdef.getDataSetParameters());
            } catch (CentrifugeException e) {
                throw Throwables.propagate(e);
            }

            requiredList.addAll(requiredSet);

            return requiredList;
        }

        @Operation
        public List<QueryParameterDef> listRequiredParameters2(@QueryParam("uuid") String uuid,
                @QueryParam("templateName") String templateName, @QueryParam("dvname") String dvname,
                @QueryParam("vizUuid") String vizUuid) throws CentrifugeException {
            DataViewDef viewDef = ModelHelper.resolveToTemplate(uuid, templateName, dvname);

            DataSetOp rootOp = viewDef.getDataTree();
            if (null == rootOp) {
                throw new ValidationException("Invalid dataview.  No data operations defined.");
            }

            List<QueryParameterDef> list = new ArrayList<QueryParameterDef>();

            list.addAll(DataSetUtil.listRequiredParameters(rootOp, viewDef.getDataSetParameters(), vizUuid,
                    dvname));
            return list;
        }
    */
    @Operation
    @Interruptable
    public PreviewResponse previewData(@PayloadParam DataViewDef dvdef) {
        PreviewResponse resp = new PreviewResponse();


        DataSetOp rootOp = dvdef.getDataTree();
        List<DataSourceDef> mySourceList = dvdef.getDataSources();
        if (null == rootOp) {
            Exception e = new CentrifugeException("No data operations found.");
            flagError(resp, e);
            LOG.error(e.getMessage(), e);
            return resp;
        }

        List<QueryParameterDef> params = dvdef.getDataSetParameters();

        List<FieldDef> fields = dvdef.getModelDef().getFieldDefs();
        Map<String, FieldDef> fieldColLocalIdMap = new HashMap<String, FieldDef>();
        for (FieldDef f : fields) {
            fieldColLocalIdMap.put(f.getColumnLocalId(), f);
        }

        // **** Cancellation check point ****
        TaskHelper.checkForCancel();

        List<String> resultLocalIdList = new ArrayList<String>();

        DataSetProcessor processor = new DataSetProcessor();
        try {
            for (ColumnDef col : DataSetUtil.getResultColumns(rootOp, null)) {
                resultLocalIdList.add(col.getLocalId());
            }
            LOG.info("*** PreviewResponse:processor.evaluateDataSet");
            processor.evaluateDataSet(rootOp, mySourceList, new ParameterSetFactory(params), null,
                                        dvdef.getModelDef().getFieldListAccess().getFieldDefMapByColumnKey().keySet(),
                                        true);

        } catch (Exception e) {
            flagError(resp, e);
            LOG.error(e.getMessage(), e);
            return resp;
        }

        // **** Cancellation check point ****
        TaskHelper.checkForCancel();

        List<LogicalQuery> queries = processor.getLogicalQueries();

        HashBiMap<String, String> biAliasMap = HashBiMap.create();
        biAliasMap.putAll(processor.getAliasMap());
        Map<String, String> aliasToIdMap = biAliasMap.inverse();

        // **** Cancellation check point ****
        TaskHelper.checkForCancel();

        Map<String, List<String>> warnings = new HashMap<String, List<String>>();
        List<CsiMap<String, String>> rowList = new ArrayList<CsiMap<String, String>>();

        for (LogicalQuery q : queries) {
            DataSourceDef dsDef = q.source;
            ConnectionDef connDef = dsDef.getConnection();
            ConnectionFactory factory = null;
            Connection conn = null;
            try {
                factory = ConnectionFactoryManager.getInstance().getConnectionFactory(connDef);
                conn = factory.getConnection(connDef);
            } catch (GeneralSecurityException e) {
                SqlUtil.quietCloseConnection(conn);

                resp.setRequiresAuth(true);
                flagError(resp, e);
                LOG.error(e.getMessage(), e);
                return resp;
            } catch (CentrifugeException e) {
                SqlUtil.quietCloseConnection(conn);

                flagError(resp, e);
                LOG.error(e.getMessage(), e);
                return resp;
            }

            // **** Cancellation check point ****
            TaskHelper.checkForCancel();

            ResultSet rs = null;
            try {
                QueryHelper.executeSQL(conn, q.preSql, params);
//                if (dvdef.hasImpalaDataSources()) {
//                    q.stripQuotes();
//                }
                rs = QueryHelper.executeSingleQuery(conn, q.sqlText, params, PREVIEW_ROW_SIZE, PREVIEW_ROW_SIZE);

            } catch (CentrifugeException e) {
                SqlUtil.quietCloseResulSet(rs);
                SqlUtil.quietCloseConnection(conn);

                resp.setQueryFailed(true);
                flagError(resp, e);
                LOG.error(e.getMessage(), e);
                return resp;
            }

            // **** Cancellation check point ****
            TaskHelper.checkForCancel();

            try {
                ResultSetMetaData rsmeta = rs.getMetaData();
                int colCount = rsmeta.getColumnCount();
                int rowcnt = 0;
                while (SqlUtil.hasMoreRows(rs) && (rowcnt < PREVIEW_ROW_SIZE)) {
                    TaskHelper.checkForCancel();

                    CsiMap<String, String> mapRow = null;
                    if (rowcnt > (rowList.size() - 1)) {
                        mapRow = new CsiMap<String, String>();
                        rowList.add(mapRow);
                    } else {
                        mapRow = rowList.get(rowcnt);
                    }

                    for (int i = 1; i <= colCount; i++) {
                        TaskHelper.checkForCancel();

                        String colName = SqlUtil.getColumnName(rsmeta, i);
                        String colTypeName = rsmeta.getColumnTypeName(i);
                        int colTypeCode = rsmeta.getColumnType(i);
                        String colLocalId = aliasToIdMap.get(colName.toLowerCase());
                        if (colLocalId == null) {
                            String colLabel = rsmeta.getColumnLabel(i);
                            colLocalId = aliasToIdMap.get(colLabel.toLowerCase());
                        }

                        if ((colLocalId == null) || !resultLocalIdList.contains(colLocalId)) {
                            continue;
                        }

                        //FIXME: Custom Queries break here since fieldColLocalIdMap does not
                        //       have a key for colLocalId
                        FieldDef field = fieldColLocalIdMap.get(colLocalId);
                        if (field == null) {
                            continue;
                        }

                        Object objValue = null;
                        if (CsiDataType.Unsupported != field.getValueType()) {
                            try {
                                objValue = rs.getObject(i);
                                if ((objValue != null) && (objValue instanceof Calendar)) {
                                    objValue = ((Calendar) objValue).getTime();
                                }
                            } catch (SQLException e) {
                                // track error and treat value as null
                                objValue = null;
                                CacheUtil.trackErrors(warnings, rowcnt + 1, field.getFieldName(), e);
                            }
                            CsiDataType csiColType = CacheUtil.resolveCsiType(colTypeName, colTypeCode, factory);
                            String strVal = null;

                            if (objValue != null) {
                               CsiDataType targetType = field.getValueType();

                               if (csiColType != targetType) {
                                   try {
                                       objValue = CsiTypeUtil.coerceType(objValue, targetType, null);
                                   } catch (Throwable t) {
                                       objValue = null;
                                       CacheUtil.trackErrors(warnings, rowcnt + 1, field.getFieldName(), t);
                                   }
                               }
                               if (objValue instanceof java.sql.Time) {
                                  strVal = CsiTypeUtil.coerceString((java.sql.Time) objValue, DateTimeFormatter.ISO_LOCAL_TIME);
                               } else if (objValue instanceof java.sql.Date) {
                                  strVal = CsiTypeUtil.coerceString((java.sql.Time) objValue, DateTimeFormatter.ISO_LOCAL_DATE);
                               } else if (objValue instanceof java.sql.Timestamp) {
                                  strVal = CsiTypeUtil.coerceString((java.sql.Timestamp) objValue, DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
                               } else if (objValue instanceof java.util.Date) {
                                  strVal = CsiTypeUtil.coerceString((java.util.Date) objValue, DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
                               } else {
                                  strVal = (String) CsiTypeUtil.coerceType(objValue, targetType, null);
                               }
                            }
                            mapRow.put(field.getUuid(), strVal);
                        }
                    }
                    rowcnt++;
                }
                SqlUtil.quietCloseResulSet(rs);

                TaskHelper.checkForCancel();

                try {
                    QueryHelper.executeSQL(conn, q.postSql, params);
                } catch (Exception e1) {
                    resp.setQueryFailed(true);
                    resp.setErrorMsg(e1.getMessage());
                    resp.setErrorTrace(CsiUtil.getStackTraceString(e1));
                    LOG.error("Failed to execute post sql commands", e1);
                }
            } catch (SQLException e) {
                flagError(resp, e);
                LOG.error(e.getMessage(), e);
                return resp;
            } finally {
                SqlUtil.quietCloseResulSet(rs);
                SqlUtil.quietCloseConnection(conn);
            }
        }

        resp.setSuccess(true);
        resp.setPreviewData(rowList);
        resp.setWarningMsg(CacheUtil.buildLoadWarningMsg(warnings));

        return resp;
    }

    private void flagError(PreviewResponse resp, Throwable t) {
        resp.setSuccess(false);
        resp.setErrorMsg(t.getMessage());
        resp.setErrorTrace(CsiUtil.getStackTraceString(t));
    }

    private void flagError(PreviewResponse resp, String errorMsg) {
        resp.setSuccess(false);
        resp.setErrorMsg(errorMsg);
    }

    private boolean stringOK(String stringIn, Set<String> filterListIn) {

        boolean myOkFlag = true;

        if ((null != stringIn) && (null != filterListIn) && !filterListIn.isEmpty()) {

            myOkFlag = filterListIn.contains(stringIn);
        }
        return myOkFlag;
    }

    private List<String> filterList(List<String> stringListIn, Set<String> filterListIn) {

        if ((null != filterListIn) && !filterListIn.isEmpty()) {

            List<String> myList = new ArrayList<String>();

            if (stringListIn != null) {

                for (String myString : stringListIn) {

                    if (filterListIn.contains(myString)) {

                        myList.add(myString);
                    }
                }
            }
            return myList;

        } else {

            List<String> myList = new ArrayList<String>();

            if (stringListIn != null) {

                for (String myString : stringListIn) {

                    myString = fixName(myString);

                    if (myString != null) {

                        myList.add(myString);
                    }
                }
            }
            return ((1 < myList.size()) || (!myList.isEmpty() && ((1 < myList.get(0).length())
                    || ((0 < myList.get(0).length()) && (myList.get(0).charAt(0) != '.'))))) ? myList : null;
        }
    }

    private String fixName(String nameIn) {

        if ((null != nameIn) && (2 < nameIn.length())
                && ('"' == nameIn.charAt(0)) && ('"' == nameIn.charAt(nameIn.length() - 1))) {

            return nameIn.substring(1, (nameIn.length() - 1));
        }
        return nameIn;
    }
}

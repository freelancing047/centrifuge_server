package csi.server.business.cachedb.dataset;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import csi.server.business.cachedb.querybuilder.DataSetQueryBuilder;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.LogicalQuery;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.operator.OpMapType;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.util.sql.CacheCommands;
import csi.server.util.sql.SqlTokens;

public class DataSetProcessor {
   private static final Logger LOG = LogManager.getLogger(DataSetProcessor.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

    private int opNum = 0;
    private int tableNum = 0;
    private int colNum = 0;
    private FieldListAccess fieldAccess;
    private Map<String, String> aliasMap;
    private List<LogicalQuery> logicalQueries;
    private List<Map<String, CsiDataType>> dataTypeMapList;
    private StringBuilder localQuery;
    private List<String> joinColumns;
    private boolean _hasInPlace = false;
    private DataSetOp clonedOp = null;
    private ConnectionFactory cacheFactory = null;
    private ParameterSetFactory parameterValues = null;
    private Map<String, DataSourceDef> sourceMap = null;
    private boolean homogeneous = true;

    public List<LogicalQuery> getLogicalQueries() {
        return logicalQueries;
    }

    public List<Map<String, CsiDataType>> getDataTypeMaps() {
        return dataTypeMapList;
    }

    public void setLogicalQueries(List<LogicalQuery> logicalQueries) {
        this.logicalQueries = logicalQueries;
    }

    public StringBuilder getLocalQuery() {
        return localQuery;
    }

    public void setLocalQuery(StringBuilder localQuery) {
        this.localQuery = localQuery;
    }

    public boolean hasInPlace() {

        return _hasInPlace;
    }

    public List<ColumnDef> getColumnListWithJoins() throws CentrifugeException, GeneralSecurityException {

        return DataSetUtil.getResultColumns(clonedOp, joinColumns);
    }

    public List<ColumnDef> getColumnList() throws CentrifugeException, GeneralSecurityException {

        return DataSetUtil.getResultColumns(clonedOp, null);
    }

    public DataSetProcessor evaluateDataSet(DataSetOp dataSetOp, List<DataSourceDef> sourceListIn,
                                            ParameterSetFactory parameterValuesIn, FieldListAccess fieldAccessIn,
                                            Set<String> activeColumnsIn, boolean returnAllIn)
            throws CentrifugeException, GeneralSecurityException {

        DataSetProcessor myActiveProcessor = null;
        Set<String> myActiveColumns = (null != activeColumnsIn) ? activeColumnsIn  : fieldAccessIn.getColumnKeys();

        /*
         * We need an entity manager in the current scope so we can do a truly local transaction -- this allows us to
         * modify the dataSetOp's structure to our heart's content without persisting it.
         */
        sourceMap = dataSetOp.buildSourceMap();
        clonedOp = dataSetOp.getWorkingCopy(createSourceHash(sourceListIn), new TreeMap<String, SqlTableDef>());
        cacheFactory = ConnectionFactoryManager.cacheFactory;

        if ((null != clonedOp) && (null != cacheFactory)) {

            DataSetQueryBuilder myLocalBuilder;

            unchainAppendMaps(clonedOp);
            clonedOp.deselectUnusedColumns(myActiveColumns);
            opNum = 1;
            tableNum = 1;
            colNum = 1;
            fieldAccess = fieldAccessIn;
            aliasMap = createAliasMap(clonedOp);
            joinColumns = DataSetUtil.listJoinedColumns(clonedOp);
            parameterValues = parameterValuesIn;
            homogeneous = true;
            logicalQueries = new ArrayList<>();
            dataTypeMapList = new ArrayList<>();
            createLogicalQueries(clonedOp, null, parameterValues, returnAllIn, false);
            myLocalBuilder = new DataSetQueryBuilder(cacheFactory, fieldAccess, aliasMap, joinColumns, parameterValues);
            localQuery = myLocalBuilder.createLocalQuery(clonedOp, returnAllIn);

            if (_doDebug) {

                StringBuilder myBuffer = new StringBuilder();

                if (null != activeColumnsIn) {

                    String myPrefix = "\n\n*** Active Columns: ";
                    for (String myColumn : activeColumnsIn) {

                        myBuffer.append(myPrefix);
                        myBuffer.append(Format.value(myColumn));
                        myPrefix = ", ";
                    }
                    myBuffer.append('\n');
                    LOG.debug(myBuffer.toString());

                    myBuffer = new StringBuilder();
                }

                myBuffer.append("\n-------------------------------------------------------------------\n");
                myBuffer.append("Alias Map:\n-------------------------------------------------------------------\n");
                for (Map.Entry<String, String> myEntry : aliasMap.entrySet()) {

                    myBuffer.append(Format.value(myEntry.getKey()));
                    myBuffer.append(" :: ");
                    myBuffer.append(Format.value(myEntry.getValue()));
                    myBuffer.append("\n");
                }
                myBuffer.append("-------------------------------------------------------------------\n");
                myBuffer.append("Join List:\n-------------------------------------------------------------------\n");
                for (String myEntry : joinColumns) {

                    myBuffer.append(Format.value(myEntry));
                    myBuffer.append("\n");
                }
                myBuffer.append("-------------------------------------------------------------------\n");
                myBuffer.append("Column Map:\n-------------------------------------------------------------------\n");
                displayColumnMaps(myBuffer, clonedOp, "");
                myBuffer.append("-------------------------------------------------------------------\n");
                myBuffer.append("Data Tree:\n-------------------------------------------------------------------\n");
                clonedOp.debug(myBuffer, "");
                myBuffer.append("-------------------------------------------------------------------\n");
                LOG.debug(myBuffer.toString());
                int howMany = logicalQueries.size();

                for (int i = 0; i < howMany; i++) {

                    LogicalQuery myQuery = logicalQueries.get(i);
                    String myString = myQuery.sqlText;

                    LOG.debug("<<src_" + Integer.toString(i + 1) + ">> [" + ((null != myString) ? myString : "") + "]");
                }
                LOG.debug("<<local>> [" + ((null != localQuery) ? localQuery : "") + "]");
            }
            myActiveProcessor = this;
        }
        return myActiveProcessor;
    }

    public String createViewQuery(InstalledTable installedTableIn, String lockIn)
            throws CentrifugeException, GeneralSecurityException {

        String myInstalledViewQuery = null;

        try {

            SqlTableDef myTableDef = clonedOp.getTableDef();

            if ((null != installedTableIn) && (null != myTableDef)) {

                String myTableName= installedTableIn.getTableName(lockIn);
                DataSetQueryBuilder myLocalBuilder = new DataSetQueryBuilder(cacheFactory, fieldAccess,
                        aliasMap, null, parameterValues);

                myInstalledViewQuery = myLocalBuilder.createInstalledViewQuery(myTableName, myTableDef, installedTableIn,
                                                                                fieldAccess.getFieldDefMapByColumnKey());
                LOG.debug("<<view>> [" + ((null != myInstalledViewQuery) ? myInstalledViewQuery : "") + "]");
            }

        } catch (Exception IGNORE) {

        }
        return myInstalledViewQuery;
    }

    private void createLogicalQueries(DataSetOp opIn, DataSetOp parent,
                                      ParameterSetFactory parameterValuesIn,
                                      boolean returnAllIn, boolean isLeftIn)
            throws CentrifugeException, GeneralSecurityException {

        List<QueryParameterDef> myParameterBaseSet = parameterValuesIn.getParameterList();
        if (DataSetUtil.isHomogeneous(opIn)) {
            // opIn is homogeneous
            DataSourceDef myDataSource = getActualSource(DataSetUtil.getFirstSource(opIn));
            ConnectionDef conndef = (myDataSource == null) ? null : myDataSource.getConnection();

            ConnectionFactory factory = ConnectionFactoryManager.getInstance().getConnectionFactory(conndef);

            DataSetQueryBuilder builder = new DataSetQueryBuilder(factory, fieldAccess, aliasMap, joinColumns, parameterValuesIn);

            StringBuilder query = builder.createCompleteQuery(opIn, parent, returnAllIn, isLeftIn);

            if (myDataSource.isInPlace()) {

                _hasInPlace = true;
            }

            if (query != null) {

                Map<String, CsiDataType> myDataTypeMap = createDataTypeMap(opIn);
                SqlTableDef myTable = opIn.getTableDef();
                LogicalQuery logical = new LogicalQuery();

                logical.sqlText = query.toString();
                logical.source = myDataSource;
                logical.baseOp = opIn;
                logical.parentOp = parent;
                logical.referenceId = (null != myTable) ? myTable.getReferenceId() : null;
                logical.preSql = builder.applySqlParameters(conndef.getPreSql(), myParameterBaseSet);
                logical.postSql = builder.applySqlParameters(conndef.getPostSql(), myParameterBaseSet);
                logical.keyField = (null != myTable) ? myTable.getKeyField() : null;
                logical.rowLimit = null;
                logicalQueries.add(logical);
                dataTypeMapList.add(myDataTypeMap);
            }
        } else {
            DataSetOp myLeftChild = opIn.getLeftChild();
            DataSetOp myRightChild = opIn.getRightChild();

            homogeneous = false;

            if ((null == myLeftChild) || (null == myRightChild)) {
                throw new CentrifugeException(
                        "Encountered invalid data set operation. Two children required.");
            }

            createLogicalQueries(myLeftChild, opIn, parameterValuesIn, returnAllIn, true);
            createLogicalQueries(myRightChild, opIn, parameterValuesIn, returnAllIn, false);
        }
    }

    private void displayColumnMaps(StringBuilder bufferIn, DataSetOp itemIn, String indentIn) {

        if (itemIn.hasLeftChild()) {

            displayColumnMaps(bufferIn, itemIn.getLeftChild(), indentIn + "     ");
        }
        bufferIn.append(indentIn);
        bufferIn.append(itemIn.getName());
        bufferIn.append('\n');
        if(itemIn.hasMapItems()) {

            itemIn.displayBiMap(bufferIn, indentIn);
        }
        if (itemIn.hasRightChild()) {

            displayColumnMaps(bufferIn, itemIn.getRightChild(), indentIn + "     ");
        }
    }

    private Map<String, DataSourceDef> createSourceHash(List<DataSourceDef> sourceListIn) {

        Map<String, DataSourceDef> mySourceMap = new TreeMap<String, DataSourceDef>();

        for (DataSourceDef mySource : sourceListIn) {

            mySourceMap.put(mySource.getUuid(), mySource);
        }

        return mySourceMap;
    }

    /*
     * Users can create append chains in the data source editor (connecting table a->b, and then b->c), which is useful
     * but causes problems when generating queries. This fixes those chains so we end up with no chains, just direct
     * links (a->b and a->c) -- note that this should NOT be persisted, as it would modify the user's query
     * construction. Instead we must unchain the maps each time the dataset is loaded.
     */
    private void unchainAppendMaps(DataSetOp opIn)
        throws CentrifugeException {

        // collect all append maps in one place
        List<OpMapItem> items = collectAllAppendMaps(opIn);

        // Store the items in maps based on their source/target ID for quick access.  Left items may be used more than once, so use a multimap.
        Multimap<String, OpMapItem> leftMap = HashMultimap.create();
        Map<String, OpMapItem> rightMap = new HashMap<String, OpMapItem>();
        for (OpMapItem item : items) {
            leftMap.put(item.getLeftColumnLocalId(), item);
            rightMap.put(item.getRightColumnLocalId(), item);
        }

        // Loop over the maps again and again until all the segments have been repaired. This could be... long.
        boolean modified = true;
        while (modified) {
            modified = false;

            Set<String> leftKeys = leftMap.keySet();
            Set<String> toKeys = rightMap.keySet();

            Multimap<String, OpMapItem> modifiedItems = HashMultimap.create();
            List<String> oldLeftKeys = new ArrayList<String>();

            for (String leftKey : leftKeys) {
                if (toKeys.contains(leftKey)) {
                    modified = true;
                    OpMapItem prevSegment = rightMap.get(leftKey);
                    Collection<OpMapItem> updatable = leftMap.get(leftKey);
                    for (OpMapItem update : updatable) {
                        update.setLeftColumnLocalId(prevSegment.getLeftColumnLocalId());
                        update.setLeftTableLocalId(prevSegment.getLeftTableLocalId());
                        modifiedItems.put(update.getLeftColumnLocalId(), update);
                    }
                    oldLeftKeys.add(leftKey);
                }
            }

            for (String oldLeftKey : oldLeftKeys) {
                leftMap.removeAll(oldLeftKey);
            }
            leftMap.putAll(modifiedItems);
        }
    }

    private List<OpMapItem> collectAllAppendMaps(DataSetOp opIn) throws CentrifugeException{
        //Iterate over the
        List<OpMapItem> items = new ArrayList<OpMapItem>();
        if ((opIn != null) && (opIn.getTableDef() == null)){
            if (opIn.getMapType() == OpMapType.APPEND){
                items.addAll(opIn.getMapItems());
            }
            DataSetOp myLeftChild = opIn.getLeftChild();
            DataSetOp myRightChild = opIn.getRightChild();

            if ((null == myLeftChild) || (null == myRightChild)) {
                throw new CentrifugeException(
                        "Encountered invalid data set operation. Two children required.");
            }

            items.addAll(collectAllAppendMaps(myLeftChild));
            items.addAll(collectAllAppendMaps(myRightChild));
        }
        return items;
    }

    private Map<String, String> createAliasMap(DataSetOp opIn) throws CentrifugeException {

        String myOpAlias = nextOpAlias();

        if (_doDebug) {
         LOG.debug(">> >> >>  DataSetProcessor::createAliasMap(DSOP:" + Format.lower(opIn.getName()) + ":" + Format.lower(opIn.getLocalId()) + ")");
      }

        Map<String, String> map = new HashMap<String, String>();

        if (_doDebug) {
         LOG.debug("           -- (DST." + Format.lower(opIn.getName()) + ") map.put(" + Format.lower(opIn.getUuid()) + ", " +  Format.lower(myOpAlias) + ")");
      }

        map.put(opIn.getUuid(), myOpAlias);
        SqlTableDef tableDef = opIn.getTableDef();
        if (tableDef != null) {
            boolean isjdbc = ConnectionFactoryManager.isAdvancedFactory(tableDef.getSource().getConnection());
            String myTableAlias = nextTableAlias();
            String myTableId = tableDef.getLocalId();

            if (_doDebug) {
               LOG.debug("           -- (TBL." + Format.lower(tableDef.getTableName()) + ") map.put(\"" + Format.lower(tableDef.getUuid()) + ", " +  Format.lower(myTableAlias) + ")");
            }

            map.put(myTableId, myTableAlias);
            for (ColumnDef col : tableDef.getColumns()) {

                String myColumnId = col.getLocalId();

                if (isjdbc) {
                    // HACK: if custom query with a call then set alias to be
                    // same as column name
                    if (tableDef.getIsCustom() && CacheCommands.CALL_PATTERN.matcher(tableDef.getCustomQuery().getQueryText()).find()) {

                        if (_doDebug) {
                           LOG.debug("           -- (COL." + Format.lower(col.getColumnName()) + ") map.put(" + Format.lower(myColumnId) + ", " + Format.lower(col.getColumnName()) + ")");
                        }

                        map.put(myColumnId, col.getColumnName().toLowerCase());
                    } else {

                        String myColAlias = nextColAlias();
                        if (_doDebug) {
                           LOG.debug("           -- (COL." + Format.lower(col.getColumnName()) + ") map.put(" + Format.lower(myColumnId) + ", " + Format.lower(myColAlias) + ")");
                        }

                        map.put(myColumnId, myColAlias);
                    }
                } else {

                    if (_doDebug) {
                     LOG.debug("           -- (COL." + Format.lower(col.getColumnName()) + ") map.put(" + Format.lower(myColumnId) + ", " + Format.lower(col.getColumnName()) + ")");
                  }

                    map.put(myColumnId, col.getColumnName().toLowerCase());
                }
            }
        } else {
            DataSetOp myLeftChild = opIn.getLeftChild();
            DataSetOp myRightChild = opIn.getRightChild();

            if ((null == myLeftChild) || (null == myRightChild)) {
                throw new CentrifugeException(
                        "Encountered invalid data set operation. Two children required.");
            }

            map.putAll(createAliasMap(myLeftChild));
            map.putAll(createAliasMap(myRightChild));
        }

        if (_doDebug) {
         LOG.debug("           -- map contains " + Integer.toString(map.size()) + " entries");
      }

        if (_doDebug) {
         LOG.debug("<< << <<  DataSetProcessor::createAliasMap(DSOP:" + Format.lower(opIn.getName()) + ":" + Format.lower(opIn.getUuid()) + ")");
      }

        return map;
    }

    private Map<String, CsiDataType> createDataTypeMap(DataSetOp dataTreeIn) throws GeneralSecurityException, CentrifugeException {

        Map<String, CsiDataType> myMap = new TreeMap<String, CsiDataType>();
        List<ColumnDef> myColumnList = DataSetUtil.getResultColumns(dataTreeIn, joinColumns);

        myMap.put(SqlTokens.PARAMETER_ID, CsiDataType.Integer);
        if ((null != myColumnList) && !myColumnList.isEmpty()) {

            for (ColumnDef myColumn : myColumnList) {

                String myColumnName = aliasMap.get(myColumn.getLocalId());
                CsiDataType myDataType = myColumn.getCsiType();

                if ((null != myColumnName) && (null != myDataType)) {

                    myMap.put(myColumnName, myDataType);
                }
            }
        }
        return myMap;
    }

    private String nextOpAlias() {
        return "op" + opNum++;
    }

    private String nextTableAlias() {
        return "t" + tableNum++;
    }

    private String nextColAlias() {
        return "c" + colNum++;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    private DataSourceDef getActualSource(DataSourceDef sourceIn) {

        return (null != sourceIn) ? sourceMap.get(sourceIn.getLocalId()) : null;
    }
}

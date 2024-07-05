package csi.server.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;

import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.query.QueryDef;
import csi.server.common.util.Format;
import csi.shared.core.util.TypedClone;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlTableDef extends ModelObject implements TypedClone<SqlTableDef> {

    protected String localId = null;
    protected String connectionId = null;
    protected String catalogName = null;
    protected String schemaName = null;
    protected String tableType = null;
    protected String tableName = null;
    protected String alias = null;
    protected String referenceId = null;
    protected Integer keyField = null;
    protected boolean isCustom = false;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected DataSourceDef source;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    protected QueryDef customQuery;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "tableDef")
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    protected List<ColumnDef> columns;

    @Transient
    private String pseudoName = null;
    @Transient
    private String dsoName = null;
    @Transient
    private Map<String, ColumnDef> referenceIdMap = null;
    @Transient
    private Map<String, ColumnDef> localIdMap = null;
    @Transient
    private Map<String, ColumnDef> columnIdMap = null;
    @Transient
    private JdbcDriverType dsoType = null;
    @Transient
    private int nextOrdinal = 0;

    public SqlTableDef() {
        super();
    }

    public SqlTableDef(String catalogNameIn, String schemaNameIn, String tableNameIn,
                       String aliasIn, String tableTypeIn, String referenceIdIn ) {
        this();
        catalogName = catalogNameIn;
        schemaName = schemaNameIn;
        tableType = tableTypeIn;
        alias = aliasIn;
        referenceId = referenceIdIn;
        tableName = tableNameIn;
    }

    @Override
    public void resetTransients() {

        pseudoName = null;
        dsoName = null;
        localIdMap = null;
        referenceIdMap = null;
        dsoType = null;

        super.resetTransients();
    }

   public boolean hasSelectedColumns() {
      boolean success = false;

      if (columns != null) {
         for (ColumnDef column : columns) {
            if (column.isSelected()) {
               success = true;
               break;
            }
         }
      }
      return success;
   }

    public void setDsoName(String nameIn) {

        dsoName = nameIn;

        if (null != columns) {

            for (ColumnDef myColumn : columns) {

                myColumn.setDsoName(nameIn);
            }
        }
    }

    public String getDsoName() {

        return dsoName;
    }

    public void setDsoType(JdbcDriverType typeIn) {

        dsoType = typeIn;

        if (null != columns) {

            for (ColumnDef myColumn : columns) {

                myColumn.setDsoType(typeIn);
            }
        }
    }

    public JdbcDriverType getDsoType() {

        if (null == dsoType) {

            if (null != source) {

                ConnectionDef myConnection = source.getConnection();

                if (null !=myConnection) {

                    dsoType = JdbcDriverType.extractValue(myConnection.getType());
                }
            }
        }
        return dsoType;
    }

    public void setPseudoName(String nameIn) {

        pseudoName = nameIn;
    }

    public void regenerateUuid() {

        super.regenerateUuid();

        if (null != columns) {

            for (ColumnDef myChild : columns) {

                if (null != myChild) {

                    myChild.regenerateUuid();
                }
            }
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionIdIn) {
        connectionId = connectionIdIn;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogNameIn) {
        catalogName = catalogNameIn;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaNameIn) {
        schemaName = schemaNameIn;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableTypeIn) {
        this.tableType = tableTypeIn;
    }

    public QueryDef getCustomQuery() {
        return customQuery;
    }

    public void setCustomQuery(QueryDef customQuery) {
        this.customQuery = customQuery;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public DataSourceDef getSource() {
        return source;
    }

    public void setSource(DataSourceDef sourceIn) {
        source = sourceIn;

        keyField = (null != source) ? (source.isInPlace() ? 0 : null) : null;
    }

    public String getTableName() {
        return tableName;
    }

    public String getQueryName() {
        return (null != customQuery) ? customQuery.getName() : null;
    }

    public void setTableName(String tableNameIn) {
        this.tableName = tableNameIn;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceIdIn) {
        this.referenceId = referenceIdIn;
    }

    /**
     * Used to indicate if this table is the result of a custom query
     */
    public boolean getIsCustom() {
        return isCustom;
    }

    /**
     * Used to indicate if this table is the result of a custom query
     */
    public void setIsCustom(boolean isCustomIn) {
        this.isCustom = isCustomIn;
    }

    /**
     * Used to indicate if this table maps a remote table
     */
    public Integer getKeyField() {
        return keyField;
    }

    /**
     * Used to indicate if this table maps a remote table
     */
    public void setKeyField(Integer keyFieldIn) {
        keyField = keyFieldIn;
    }

    /**
     * Used to indicate the parent datasource does not support all joins and unions
     */
   public boolean isSingleTable() {
      return (getSource() != null) && getSource().isSingleTable();
   }

    public void replaceColumns(List<ColumnDef> columnsIn) {

        nextOrdinal = 0;

        if (null != columnsIn) {

            if (null != columns) {

                columns.clear();

            } else {

                columns = new ArrayList<ColumnDef>(columnsIn.size());
            }

            for (nextOrdinal = 0; columnsIn.size() > nextOrdinal;nextOrdinal++) {

                ColumnDef myColumn = columnsIn.get(nextOrdinal);

                myColumn.setTableDef(this);
                myColumn.setOrdinal(nextOrdinal);
                columns.add(myColumn);
            }

        } else {

            columns = null;
        }
    }

    public void setColumns(List<ColumnDef> columnsIn) {

        nextOrdinal = 0;
        columns = columnsIn;

        if (null != columns) {

            for (nextOrdinal = 0; columns.size() > nextOrdinal;nextOrdinal++) {

                ColumnDef myColumn = columns.get(nextOrdinal);

                myColumn.setTableDef(this);
                myColumn.setOrdinal(nextOrdinal);
            }
        }
    }

    public List<ColumnDef> getColumns() {

        if (null == columns) {

            nextOrdinal = 0;
            columns = new ArrayList<ColumnDef>();

        } else {

            if (0 == nextOrdinal) {

                for (nextOrdinal = 0; columns.size() > nextOrdinal; nextOrdinal++) {

                    ColumnDef myColumn = columns.get(nextOrdinal);

                    myColumn.setTableDef(this);
                    myColumn.setOrdinal(nextOrdinal);
                }
            }
        }
        return columns;
    }

    public void clearColumnList() {

        getColumns().clear();
        nextOrdinal = 0;
    }

    public void addColumn(ColumnDef columnIn) {

        columnIn.setOrdinal(nextOrdinal++);
        columnIn.setCatalogName(catalogName);
        columnIn.setSchemaName(schemaName);
        columnIn.setTableName(tableName);
        columnIn.setTableDef(this);
        getColumns().add(columnIn);
    }

    public String getDisplayName() {

        return (null != alias) ? alias : ((null != pseudoName) ? pseudoName : getTableName());
    }

    public boolean hasFilters() {

        boolean myFilterFlag = false;

        for (ColumnDef myColumn : columns) {

            if ((myColumn.getColumnFilters() != null) && !myColumn.getColumnFilters().isEmpty()) {

                myFilterFlag = true;
                break;
            }
        }
        return myFilterFlag;
    }

    public boolean hasDataSource(List<DataSourceDef> dataSourcesIn) {

        boolean mySuccess = false;

        if ((dataSourcesIn != null) && !dataSourcesIn.isEmpty()) {

            String mySourceId = (null != source) ? source.getUuid() : null;

            if (null != mySourceId) {

                for (DataSourceDef mySource : dataSourcesIn) {

                    if ((null != mySource) && mySourceId.equals(mySource.getUuid())) {

                        mySuccess = true;
                        break;
                    }
                }
            }
        }
        return mySuccess;
    }

    public SqlTableDef resetColumnByLocalIdMap() {

        localIdMap = null;
        return this;
    }

    public boolean containsColumn(String localIdIn) {

        return getLocalIdMap().containsKey(localIdIn);
    }

    public ColumnDef getColumnByLocalId(String localIdIn) {

        return getLocalIdMap().get(localIdIn);
    }

    public Map<String, ColumnDef> getLocalIdMap() {

        if (null == localIdMap) {

            localIdMap = new TreeMap<String, ColumnDef>();

            for (ColumnDef myColumn : columns) {

                localIdMap.put(myColumn.getLocalId(), myColumn);
            }
        }
        return localIdMap;
    }

    public SqlTableDef resetColumnByReferenceIdMap() {

        referenceIdMap = null;
        return this;
    }

    public boolean containsInstalledColumn(String referenceIdIn) {

        return getReferenceIdMap().containsKey(referenceIdIn);
    }

    public ColumnDef getColumnByReferenceId(String referenceIdIn) {

        return getReferenceIdMap().get(referenceIdIn);
    }

    public Map<String, ColumnDef> getReferenceIdMap() {

        if (null == referenceIdMap) {

            referenceIdMap = new TreeMap<String, ColumnDef>();

            for (ColumnDef myColumn : columns) {

                String referenceId = myColumn.getReferenceId();

                if (null != referenceId) {

                    referenceIdMap.put(referenceId, myColumn);
                }
            }
        }
        return referenceIdMap;
    }

    public Map<String, ColumnDef> getColumnIdMap() {

        if (null == columnIdMap) {

            columnIdMap = new TreeMap<String, ColumnDef>();

            for (ColumnDef myColumn : columns) {

                columnIdMap.put(myColumn.getLocalId(), myColumn);
            }
        }
        return columnIdMap;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("catalogName", getCatalogName()) //
                .add("schemaName", getSchemaName()) //
                .add("tableName", getTableName()) //
                .add("alias", getAlias()) //
                .add("tableType", getTableType()) //
                .add("referenceId", getReferenceId()) //
                .add("isCustom", getIsCustom()) //
                .add("keyField", Format.value(keyField)) //
                .add("customQuery", getCustomQuery()) //
                .toString();
    }

    @Override
    public SqlTableDef getClone() {
        SqlTableDef td = new SqlTableDef();

        td.setLocalId(UUID.randomUUID());
        td.setConnectionId(this.getConnectionId());
        td.setCatalogName(this.getCatalogName());
        td.setSchemaName(this.getSchemaName());
        td.setTableType(this.getTableType());
        td.setTableName(this.getTableName());
        td.setAlias(this.getAlias());
        td.setKeyField(this.getKeyField());
        td.setReferenceId(this.getReferenceId());
        td.setIsCustom(this.getIsCustom());

        td.setSource(this.getSource());
        td.setCustomQuery(this.getCustomQuery());

        td.setColumns(new ArrayList<ColumnDef>());
        for (ColumnDef cd : this.getColumns()) {
            ColumnDef cloneCd = cd.getClone();
            cloneCd.setTableDef(td);
            td.getColumns().add(cloneCd);
        }
        // Preserve the uuid, only change the local id.
        //cd.setUuid(this.getUuid());

        return td;
    }

    public SqlTableDef clone(boolean genNewIdIn) {

        SqlTableDef myClone = new SqlTableDef(getCatalogName(), getSchemaName(), getTableName(), getAlias(),
                                                getTableType(), getReferenceId());

        super.cloneComponents(myClone);

        myClone.setLocalId(getLocalId());
        myClone.setConnectionId(getConnectionId());
        myClone.setIsCustom(getIsCustom());
        myClone.setKeyField(getKeyField());
        myClone.setSource(getSource());
        if (null != getCustomQuery()) {
            myClone.setCustomQuery(getCustomQuery());
        }
        myClone.setColumns(cloneColumns(myClone, genNewIdIn));

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject> SqlTableDef clone(Map<String, T> dataSourceMapIn) {

        SqlTableDef myClone = new SqlTableDef(getCatalogName(), getSchemaName(), getTableName(), getAlias(),
                getTableType(), getReferenceId());

        super.cloneComponents(myClone);

        myClone.setLocalId(getLocalId());
        myClone.setConnectionId(getConnectionId());
        myClone.setIsCustom(getIsCustom());
        myClone.setKeyField(getKeyField());
        myClone.setSource((DataSourceDef)cloneFromOrToMap(dataSourceMapIn, (T)getSource()));
        if (null != getCustomQuery()) {
            myClone.setCustomQuery(getCustomQuery().clone());
        }
        myClone.setColumns(cloneColumns(myClone));

        return myClone;
    }

    public SqlTableDef fullClone(Map<String, DataSourceDef> sourceMapIn, Map<String, SqlTableDef> tableMapIn) {

        SqlTableDef myClone = new SqlTableDef(getCatalogName(), getSchemaName(), getTableName(),
                                                getAlias(), getTableType(), getReferenceId());

        tableMapIn.put(myClone.getUuid(), myClone);
        super.fullCloneComponents(myClone);

        return fullCloneValues(myClone, sourceMapIn);
    }

    public SqlTableDef fullCloneValues(SqlTableDef cloneIn, Map<String, DataSourceDef> sourceMapIn) {

        DataSourceDef mySource = (null != source) ? sourceMapIn.get(source.getUuid()) : null;
        cloneIn.setLocalId(getLocalId());
        cloneIn.setConnectionId(getConnectionId());
        cloneIn.setIsCustom(getIsCustom());
        cloneIn.setKeyField(getKeyField());
        cloneIn.setSource(mySource);
        if (null != getCustomQuery()) {
            cloneIn.setCustomQuery(getCustomQuery().fullClone());
        }
        cloneIn.setColumns(fullCloneColumns(cloneIn));

        return cloneIn;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, localId, indentIn, "localId");
        debugObject(bufferIn, catalogName, indentIn, "catalogName");
        debugObject(bufferIn, schemaName, indentIn, "schemaName");
        debugObject(bufferIn, tableType, indentIn, "tableType");
        debugObject(bufferIn, tableName, indentIn, "tableName");
        debugObject(bufferIn, alias, indentIn, "alias");
        debugObject(bufferIn, referenceId, indentIn, "referenceId");
        debugObject(bufferIn, isCustom, indentIn, "isCustom");
        debugObject(bufferIn, keyField, indentIn, "keyField");
        doDebug(source, bufferIn, indentIn, "source", "DataSourceDef");
        doDebug(customQuery, bufferIn, indentIn, "customQuery", "QueryDef");
        debugList(bufferIn, columns, indentIn, "columns");
    }

    private List<ColumnDef> cloneColumns(SqlTableDef tableIn, boolean genNewIdIn) {

        if (null != getColumns()) {

            List<ColumnDef>  myList = new ArrayList<ColumnDef>();

            if (genNewIdIn) {

                for (ColumnDef myItem : getColumns()) {

                    ColumnDef myClone = myItem.clone(UUID.randomUUID());
                    myClone.setTableDef(tableIn);
                    myList.add(myClone);
                }

            } else {

                for (ColumnDef myItem : getColumns()) {

                    ColumnDef myClone = myItem.clone();
                    myClone.setTableDef(tableIn);
                    myList.add(myClone);
                }
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<ColumnDef> fullCloneColumns(SqlTableDef cloneIn) {

        if (null != getColumns()) {

            List<ColumnDef>  myList = new ArrayList<ColumnDef>();

            for (ColumnDef myItem : getColumns()) {

                ColumnDef myClone = myItem.fullClone(cloneIn);
                myList.add(myClone);
            }
            return myList;

        } else {

            return null;
        }
    }

    private List<ColumnDef> cloneColumns(SqlTableDef tableIn) {

        if (null != getColumns()) {

            List<ColumnDef>  myList = new ArrayList<ColumnDef>();

            for (ColumnDef myItem : getColumns()) {

                ColumnDef myClone = myItem.clone(UUID.randomUUID());
                myClone.setTableDef(tableIn);
                myList.add(myItem.clone());
            }
            return myList;

        } else {

            return null;
        }
    }
}

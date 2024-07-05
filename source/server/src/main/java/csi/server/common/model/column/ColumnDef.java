package csi.server.common.model.column;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.UUID;
import csi.shared.core.util.TypedClone;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ColumnDef extends ModelObject implements Comparable<ColumnDef>, TypedClone<ColumnDef>, MapByDataType {

    protected String localId;
    protected String catalogName;
    protected String schemaName;
    protected String tableName;
    @Column(columnDefinition = "TEXT")
    protected String columnName;
    protected String dataTypeName;
    protected int jdbcDataType;
    // deprecated
    protected int columnSize;
    // deprecated
    protected int decimalDigits;
    // deprecated
    protected String defaultValue;
    protected int ordinal;
    // deprecated
    protected String nullable;
    protected String referenceId;
    @Enumerated(value = EnumType.STRING)
    protected CsiDataType csiType;          // Data Type as recognized from original source
    protected boolean selected;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    protected List<ColumnFilter> columnFilters;
    // deprecated
    protected String sourceFormat;
    @ManyToOne
    private SqlTableDef tableDef;

    @Transient
    protected boolean mapped;
    @Transient
    private String dsoName = null;
    @Transient
    private JdbcDriverType dsoType = null;

    public ColumnDef() {
        super();
    }

    public ColumnDef(String columnNameIn, CsiDataType dataTypeIn, String referenceIdIn, boolean selectedIn) {

        super();

        columnName = columnNameIn;
        csiType = dataTypeIn;
        localId = UUID.randomUUID();
        referenceId = referenceIdIn;
        selected = selectedIn;
    }

    public ColumnDef(SqlTableDef tableIn, String columnNameIn, CsiDataType dataTypeIn,
                     String referenceIdIn, int ordinalIn, boolean selectedIn) {

        this(columnNameIn, dataTypeIn, referenceIdIn, selectedIn);

        tableDef = tableIn;
        catalogName = tableDef.getCatalogName();
        schemaName= tableDef.getSchemaName();
        tableName = tableDef.getTableName();
        ordinal = ordinalIn;
    }

    public String getColumnKey() {

        return localId;
    }

    public String getColumnLocalId() {

        return getLocalId();
    }

    public String getTableLocalId() {

        String myTableId = null;

        if (null != tableDef) {

            try {

                myTableId = tableDef.getLocalId();

            } catch (Exception IGNORE) {}
        }
        return myTableId;
    }

    public void setDsoName(String nameIn) {

        dsoName = nameIn;
    }

    public String getDsoName() {

        return dsoName;
    }

    public void setDsoType(JdbcDriverType typeIn) {

        dsoType = typeIn;
    }

    public JdbcDriverType getDsoType() {

        return dsoType;
    }

    public void regenerateUuid() {

        super.regenerateUuid();

        if (null != columnFilters) {

            for (ColumnFilter myChild : columnFilters) {

                if (null != myChild) {

                    myChild.regenerateUuid();
                }
            }
        }
    }

    public CsiDataType getDataType() {

        return csiType;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalog) {
        this.catalogName = catalog;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schema) {
        this.schemaName = schema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableNameIn) {

        tableName = tableNameIn;

        if (null == dsoName) {

            dsoName = tableNameIn;
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String typeName) {
        this.dataTypeName = typeName;
    }

    public int getJdbcDataType() {
        return jdbcDataType;
    }

    public void setJdbcDataType(int jdbcType) {
        this.jdbcDataType = jdbcType;
    }
    // deprecated
    public int getColumnSize() {
        return columnSize;
    }
    // deprecated
    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }
    // deprecated
    public int getDecimalDigits() {
        return decimalDigits;
    }
    // deprecated
    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }
    // deprecated
    public String getDefaultValue() {
        return defaultValue;
    }
    // deprecated
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public int getOrdinal() {
        return ordinal;
    }
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
    public CsiDataType getCsiType() {
        return csiType;
    }
    public void setCsiType(CsiDataType csiTypeIn) {

        csiType = csiTypeIn;
    }
    // deprecated
    public String getSourceFormat() {
        return sourceFormat;
    }
    // deprecated
    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isMapped() {
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }
    // deprecated
    public String getNullable() {
        return nullable;
    }
    // deprecated
    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceIdIn) {
        this.referenceId = referenceIdIn;
    }

    public List<ColumnFilter> getColumnFilters() {
        return columnFilters;
    }

    public void setColumnFilters(List<ColumnFilter> columnFilters) {
        this.columnFilters = columnFilters;
    }

    public SqlTableDef getTableDef() {
        return tableDef;
    }

    public void setTableDef(SqlTableDef tableDef) {
        this.tableDef = tableDef;
    }

  public void mapDoubleMapByType(CsiDataType dataTypeIn, Map<String,String> idMapIn, Map<String,String> nameMapIn) {
     if (getCsiType() == dataTypeIn) {
        nameMapIn.put(getColumnName(), getLocalId());
        idMapIn.put(getLocalId(), getColumnName());
     }
  }

    @Override
    public int compareTo(ColumnDef o) {
        return ComparisonChain.start() //
                .compare(this.getTableName(), o.getTableName()) //
                .compare(this.getColumnName(), o.getColumnName()) //
                .result();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("name", getColumnName()) //
                .add("type", getCsiType()) //
                .add("ordinal", getOrdinal()) //
                .toString();
    }

    public ColumnDef getClone() {
        ColumnDef cd = new ColumnDef();

        cd.setCatalogName(this.getCatalogName());
        cd.setColumnFilters(new ArrayList<ColumnFilter>());
        cd.setColumnName(this.getColumnName());
        cd.setColumnSize(this.getColumnSize());
        cd.setCsiType(this.getCsiType());
        cd.setDataTypeName(this.getDataTypeName());
        cd.setDecimalDigits(this.getDecimalDigits());
        cd.setDefaultValue(this.getDefaultValue());
        cd.setJdbcDataType(this.getJdbcDataType());
        cd.setLocalId(UUID.randomUUID());
        cd.setNullable(this.getNullable());
        cd.setReferenceId(this.getReferenceId());
        cd.setOrdinal(this.getOrdinal());
        cd.setSchemaName(this.getSchemaName());
        cd.setSelected(this.isSelected());
        cd.setSourceFormat(this.getSourceFormat());
        cd.setTableName(this.getTableName());
        cd.setTableDef(this.getTableDef());
        // Preserve the uuid, only change the local id.
        //cd.setUuid(this.getUuid());

        return cd;
    }

    public ColumnDef clone(String localIdIn) {

        ColumnDef myClone = clone();

        myClone.setLocalId(localIdIn);

        return myClone;
    }

    @Override
    public ColumnDef clone() {

        ColumnDef myClone = new ColumnDef();

        super.cloneComponents(myClone);
        myClone.setTableDef(getTableDef());
        myClone.setColumnFilters(cloneColumnFilters());

        return cloneValues(myClone);
    }

    public ColumnDef fullClone(SqlTableDef tableIn) {

        ColumnDef myClone = new ColumnDef();

        super.cloneComponents(myClone);
        myClone.setTableDef(tableIn);
        myClone.setColumnFilters(fullCloneColumnFilters(myClone));

        return cloneValues(myClone);
    }

    public ColumnDef cloneValues(ColumnDef cloneIn) {

        cloneIn.setLocalId(getLocalId());
        cloneIn.setCatalogName(getCatalogName());
        cloneIn.setSchemaName(getSchemaName());
        cloneIn.setTableName(getTableName());
        cloneIn.setColumnName(getColumnName());
        cloneIn.setDataTypeName(getDataTypeName());
        cloneIn.setJdbcDataType(getJdbcDataType());
        cloneIn.setColumnSize(getColumnSize());
        cloneIn.setDecimalDigits(getDecimalDigits());
        cloneIn.setDefaultValue(getDefaultValue());
        cloneIn.setOrdinal(getOrdinal());
        cloneIn.setNullable(getNullable());
        cloneIn.setReferenceId(getReferenceId());
        cloneIn.setCsiType(getCsiType());
        cloneIn.setSelected(isSelected());
        cloneIn.setSourceFormat(getSourceFormat());

        return cloneIn;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, localId, indentIn, "localId");
        debugObject(bufferIn, catalogName, indentIn, "catalogName");
        debugObject(bufferIn, schemaName, indentIn, "schemaName");
        debugObject(bufferIn, tableName, indentIn, "tableName");
        debugObject(bufferIn, columnName, indentIn, "columnName");
        debugObject(bufferIn, dataTypeName, indentIn, "dataTypeName");
        debugObject(bufferIn, jdbcDataType, indentIn, "jdbcDataType");
        debugObject(bufferIn, columnSize, indentIn, "columnSize");
        debugObject(bufferIn, decimalDigits, indentIn, "decimalDigits");
        debugObject(bufferIn, defaultValue, indentIn, "defaultValue");
        debugObject(bufferIn, ordinal, indentIn, "ordinal");
        debugObject(bufferIn, nullable, indentIn, "nullable");
        debugObject(bufferIn, referenceId, indentIn, "referenceId");
        debugObject(bufferIn, csiType, indentIn, "csiType");
        debugObject(bufferIn, selected, indentIn, "selected");
        debugObject(bufferIn, sourceFormat, indentIn, "sourceFormat");
        debugObject(bufferIn, tableDef, indentIn, "tableDef");
        debugList(bufferIn, columnFilters, indentIn, "columnFilters");
    }

    private List<ColumnFilter> cloneColumnFilters() {

        if (null != getColumnFilters()) {

            List<ColumnFilter>  myList = new ArrayList<ColumnFilter>();

            for (ColumnFilter myItem : getColumnFilters()) {

                myList.add(myItem.clone());
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<ColumnFilter> fullCloneColumnFilters(ColumnDef cloneIn) {

        if (null != getColumnFilters()) {

            List<ColumnFilter>  myList = new ArrayList<ColumnFilter>();

            for (ColumnFilter myItem : getColumnFilters()) {

                myList.add(myItem.fullClone(cloneIn));
            }
            return myList;

        } else {

            return null;
        }
    }

    @Override
    public String getName() {
        return getColumnName();
    }
}

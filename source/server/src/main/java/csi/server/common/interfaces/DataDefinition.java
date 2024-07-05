package csi.server.common.interfaces;

import com.google.common.base.Objects;
import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.*;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.OrphanColumn;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.extension.SimpleExtension;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.SystemParameters;

import javax.persistence.PreRemove;
import java.util.*;

/**
 * Created by centrifuge on 5/15/2017.
 */
public interface DataDefinition {

    public void resetTransients();

    /*
        The following entry points are handled by SourceAccess component
     */
    public List<DataSourceDef> getDataSources();
    public void setDataSources(List<DataSourceDef> dataSetList);
    public void clearTransientValues();
    public void clearAllRuntimeValues();
    public void clearRuntimeValues(boolean transientOnly);
    public FieldListAccess getFieldListAccess();
    public ParameterListAccess getParameterListAccess();

    public List<FieldDef> getFieldList();
    public List<QueryParameterDef> getParameterList();

    public DataSetOp getDataTree();
    public void setDataTree(DataSetOp dataTreeIn);
    public List<OrphanColumn> getOrphanColumns();
    public void setOrphanColumns(List<OrphanColumn> orphanColumnsIn);
    public int getNextJoinNumber();
    public void setNextJoinNumber(int nextJoinNumberIn);
    public int getNextAppendNumber();
    public void setNextAppendNumber(int nextAppendNumberIn);
    public int getAndIncrementNextJoinNumber();
    public int getAndIncrementNextAppendNumber();
    public Map<String, ColumnDef> getColumnByKeyMap();
    public void resetColumnByKeyMap();
    public ColumnDef getColumnByKey(String columnKeyIn);
    public Set<String> getDataSourceKeySet();

    public boolean hasStorageTypes();
    public void setStorageTypesFlag();
    public void setRowLimit(Integer rowLimitIn);
    public Integer getRowLimit();
}

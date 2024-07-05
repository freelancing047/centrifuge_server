package csi.server.common.interfaces;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.Resource;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ValuePair;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by centrifuge on 12/14/2017.
 */
public interface DataWrapper extends DataContainer {

    public Resource getResource();
    public String getName();
    public String getVersion();
    public String getUuid();
    public DataDefinition getDataDefinition();
    public SecurityAccess getSecurityAccess();
    public List<FieldDef> getFieldList();
    public Map<String, FieldDef> getCapcoColumnMap();
    public Map<String, FieldDef> getTagColumnMap();
    public List<QueryParameterDef> getParameterList();
    public void setNeedsSource(boolean needsSource);
    public List<ValuePair<FieldDef, FieldDef>> getPhysicalPairs();
    public List<String> getCapcoColumnNames();
    public List<String> getTagColumnNames();
    public Set<String> getDataSourceKeySet();
    public void lockDataKeys();
}

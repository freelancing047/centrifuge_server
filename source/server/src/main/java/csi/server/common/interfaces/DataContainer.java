package csi.server.common.interfaces;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ValuePair;

import java.util.List;

/**
 * Created by centrifuge on 5/17/2017.
 */
public interface DataContainer {

    public String getName();
    public AclResourceType getResourceType();
    public DataDefinition getDataDefinition();
    public SecurityAccess getSecurityAccess();

    public List<DataSourceDef> getDataSources();
    public List<FieldDef> getFieldList();
    public List<QueryParameterDef> getParameterList();
}

package csi.server.common.interfaces;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.SystemParameters;

import java.util.*;

/**
 * Created by centrifuge on 5/15/2017.
 */
public interface ParameterListAccess extends IsSerializable {

    public void clearTransientValues();
    public void clearAllRuntimeValues();
    public void clearRuntimeValues(boolean transientOnly);
    public List<QueryParameterDef> getDataSetParameters();
    public List<QueryParameterDef> getSystemParameters();
    public void setDataSetParameters(List<QueryParameterDef> dataSetParametersIn);
    public void addParameter(QueryParameterDef parameterIn);
    public void deleteParameter(QueryParameterDef parameterIn);
    public void updateParameter(QueryParameterDef parameterIn);
    public List<QueryParameterDef> getOrderedParameterList(CsiDataType dataTypeIn);
    public List<QueryParameterDef> getOrderedSystemParameterList(CsiDataType dataTypeIn);
    public List<QueryParameterDef> getOrderedFullParameterList(CsiDataType dataTypeIn);
    public List<QueryParameterDef> getOrderedParameterList();
    public List<QueryParameterDef> getOrderedSystemParameterList();
    public List<QueryParameterDef> getOrderedFullParameterList();
    public void refreshParameters();
    public Map<String, QueryParameterDef> getParameterIdMap();
    public Map<String, QueryParameterDef> getParameterNameMap();
    public Map<String, QueryParameterDef> getSystemParameterNameMap();
    public Map<String, QueryParameterDef> getSystemParameterIdMap();
    public List<QueryParameterDef> getRequiredParameters();
    public QueryParameterDef getParameterById(String idIn);
    public QueryParameterDef getParameterByName(String nameIn);
    public Map<String, QueryParameterDef> initializeParameterUse();
}

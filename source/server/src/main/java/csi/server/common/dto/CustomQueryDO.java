package csi.server.common.dto;



import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.ConnectionDef;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryParameterDef;


public class CustomQueryDO implements IsSerializable {

    private ConnectionDef _connection;
    private QueryDef _query;
    private List<QueryParameterDef> _parameters;
    private List<LaunchParam> _parameterValues;
    
    public CustomQueryDO() {
        
    }
    
    public CustomQueryDO(ConnectionDef connectionIn, QueryDef queryIn, List<QueryParameterDef> parametersIn, List<LaunchParam> parameterValuesIn) {
        
        _connection = connectionIn;
        _query = queryIn;
        _parameters = parametersIn;
        _parameterValues = parameterValuesIn;
    }

    public ConnectionDef getConnection() {

        return _connection;
    }

    public void setConnection(ConnectionDef connectionIn) {

        _connection = connectionIn;
    }

    public QueryDef getQuery() {

        return _query;
    }

    public void setQuery(QueryDef queryIn) {

        _query = queryIn;
    }

    public List<QueryParameterDef> getParameters() {

        return _parameters;
    }

    public void setParameters(List<QueryParameterDef> parametersIn) {

        _parameters = parametersIn;
    }

    public List<LaunchParam> getParameterValues() {

        return _parameterValues;
    }

    public void setParameterValues(List<LaunchParam> parameterValuesIn) {

        _parameterValues = parameterValuesIn;
    }
}

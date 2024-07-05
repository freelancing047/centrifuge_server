package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.SqlTableDef;

public class TestQueryResponse implements IsSerializable {

    private boolean success = false;
    private boolean requiresAuth = false;
    private boolean connectionFailed = false;
    private boolean queryFailed = false;
    private SqlTableDef tableDef;
    private String errorMsg;
    private String errorTrace;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public void setRequiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }

    public boolean isConnectionFailed() {
        return connectionFailed;
    }

    public void setConnectionFailed(boolean connectionFailed) {
        this.connectionFailed = connectionFailed;
    }

    public boolean getQueryFailed() {
        return queryFailed;
    }

    public void setQueryFailed(boolean queryFailed) {
        this.queryFailed = queryFailed;
    }

    public SqlTableDef getTableDef() {
        return tableDef;
    }

    public void setTableDef(SqlTableDef tableDef) {
        this.tableDef = tableDef;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorTrace() {
        return errorTrace;
    }

    public void setErrorTrace(String errorTrace) {
        this.errorTrace = errorTrace;
    }
}

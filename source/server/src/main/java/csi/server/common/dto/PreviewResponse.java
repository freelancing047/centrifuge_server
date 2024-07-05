package csi.server.common.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PreviewResponse implements IsSerializable {

    private boolean success = false;
    private boolean requiresAuth = false;
    private boolean connectionFailed = false;
    private boolean queryFailed = false;
    private List<CsiMap<String, String>> previewData;
    private String warningMsg;
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

    public boolean getConnectionFailed() {
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

    public List<CsiMap<String, String>> getPreviewData() {
        return previewData;
    }

    public void setPreviewData(List<CsiMap<String, String>> previewData) {
        this.previewData = previewData;
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

    public String getWarningMsg() {
        return warningMsg;
    }

    public void setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
    }
}

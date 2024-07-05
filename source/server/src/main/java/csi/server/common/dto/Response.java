package csi.server.common.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.ServerMessage;
import csi.server.common.model.DataSourceDef;

public class Response<S, T> implements IsSerializable {

    private T _result;
    private ServerMessage _message = null;
    private String _exception = null;
    private S _key;
    private boolean _success;
    private boolean _limitedData;
    private long _count;
    private boolean _requiresAuthorization;
    private List<DataSourceDef> _authorizationList = null;
    private ResponseArgument _argument = null;

    public Response() {

    }

    public Response(boolean authorizationRequiredIn) {

        _key = null;
        _success = !authorizationRequiredIn;
        _requiresAuthorization = authorizationRequiredIn;
        _count = 0L;
        _limitedData = false;
    }

    public Response(S keyIn, boolean authorizationRequiredIn) {

        _key = keyIn;
        _success = !authorizationRequiredIn;
        _requiresAuthorization = authorizationRequiredIn;
        _count = 0L;
        _limitedData = false;
    }

    public Response(List<DataSourceDef> authorizationListIn) {

        _key = null;
        _success = false;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
        _count = 0L;
        _limitedData = false;
    }

    public Response(S keyIn, List<DataSourceDef> authorizationListIn) {

        _key = keyIn;
        _success = false;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
        _count = 0L;
        _limitedData = false;
    }

    public Response(T resultIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = 0L;
        _limitedData = false;
    }

    public Response(T resultIn, long countIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _limitedData = false;
    }

    public Response(T resultIn, long countIn, boolean limitedDataIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _limitedData = limitedDataIn;
    }

    public Response(T resultIn, long countIn, ServerMessage messageIn) {

        this(resultIn, countIn, messageIn ,(ResponseArgument)null);
    }

    public Response(T resultIn, long countIn, ServerMessage messageIn, ResponseArgument argumentIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _message = messageIn;
        _argument = argumentIn;
        _limitedData = false;
    }
    public Response(T resultIn, long countIn, ServerMessage messageIn, ResponseArgument argumentIn, boolean limitedDataIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _message = messageIn;
        _argument = argumentIn;
        _limitedData = limitedDataIn;
    }

    public Response(S keyIn, T resultIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = 0L;
        _limitedData = false;
    }

    public Response(S keyIn, T resultIn, long countIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _limitedData = false;
    }

    public Response(S keyIn, T resultIn, long countIn, boolean limitedDataIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _limitedData = limitedDataIn;
    }

    public Response(S keyIn, T resultIn, long countIn, ServerMessage messageIn) {

        this(keyIn, resultIn, countIn, messageIn ,(ResponseArgument)null);
    }

    public Response(S keyIn, T resultIn, long countIn, ServerMessage messageIn, boolean limitedDataIn) {

        this(keyIn, resultIn, countIn, messageIn ,(ResponseArgument)null, limitedDataIn);
    }

    public Response(S keyIn, T resultIn, long countIn, ServerMessage messageIn, ResponseArgument argumentIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _message = messageIn;
        _argument = argumentIn;
        _limitedData = false;
    }

    public Response(S keyIn, T resultIn, long countIn, ServerMessage messageIn, ResponseArgument argumentIn, boolean limitedDataIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = false;
        _count = countIn;
        _message = messageIn;
        _argument = argumentIn;
        _limitedData = limitedDataIn;
    }

    public Response(List<DataSourceDef> authorizationListIn, T resultIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
        _count = 0L;
        _limitedData = false;
    }

    public Response(List<DataSourceDef> authorizationListIn, T resultIn, long countIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
         _count = countIn;
        _limitedData = false;
    }

    public Response(List<DataSourceDef> authorizationListIn, T resultIn, long countIn, boolean limitedDataIn) {

        _key = null;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
        _count = countIn;
        _limitedData = limitedDataIn;
    }

    public Response(S keyIn, List<DataSourceDef> authorizationListIn, T resultIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
        _count = 0L;
        _limitedData = false;
    }

    public Response(S keyIn, List<DataSourceDef> authorizationListIn, T resultIn, long countIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
        _count = countIn;
        _limitedData = false;
    }

    public Response(S keyIn, List<DataSourceDef> authorizationListIn, T resultIn, long countIn, boolean limitedDataIn) {

        _key = keyIn;
        _success = true;
        _result = resultIn;
        _requiresAuthorization = ((authorizationListIn != null) && !authorizationListIn.isEmpty());
        _authorizationList = authorizationListIn;
        _count = countIn;
        _limitedData = limitedDataIn;
    }

    public Response(ServerMessage messageIn) {

        this(messageIn ,(ResponseArgument)null);
    }

    public Response(ServerMessage messageIn, ResponseArgument argumentIn) {

        _key = null;
        _success = false;
        _message = messageIn;
        _requiresAuthorization = false;
        _count = 0L;
        _argument = argumentIn;
        _limitedData = false;
    }

    public Response(S keyIn, ServerMessage messageIn) {

        this(keyIn, messageIn ,(ResponseArgument)null);
    }

    public Response(S keyIn, ServerMessage messageIn, ResponseArgument argumentIn) {

        _key = keyIn;
        _success = false;
        _message = messageIn;
        _requiresAuthorization = false;
        _count = 0L;
        _argument = argumentIn;
        _limitedData = false;
    }

    public Response(ServerMessage messageIn, String exceptionIn) {

        this(messageIn, exceptionIn ,(ResponseArgument)null);
    }

    public Response(ServerMessage messageIn, String exceptionIn, ResponseArgument argumentIn) {

        _key = null;
        _success = false;
        _message = messageIn;
        _exception = exceptionIn;
        _requiresAuthorization = false;
        _count = 0L;
        _argument = argumentIn;
        _limitedData = false;
    }

    public Response(S keyIn, ServerMessage messageIn, String exceptionIn) {

        this(keyIn, messageIn, exceptionIn ,(ResponseArgument)null);
    }

    public Response(S keyIn, ServerMessage messageIn, String exceptionIn, ResponseArgument argumentIn) {

        _key = keyIn;
        _success = false;
        _message = messageIn;
        _exception = exceptionIn;
        _requiresAuthorization = false;
        _count = 0L;
        _argument = argumentIn;
        _limitedData = false;
    }

    public Response(S keyIn, T resultIn, ServerMessage messageIn, String exceptionIn) {

        this( keyIn, resultIn, messageIn, exceptionIn,(ResponseArgument)null);
    }

    public Response(S keyIn, T resultIn, ServerMessage messageIn, String exceptionIn, ResponseArgument argumentIn) {

        _key = keyIn;
        _result = resultIn;
        _success = false;
        _message = messageIn;
        _exception = exceptionIn;
        _requiresAuthorization = false;
        _count = 0L;
        _argument = argumentIn;
        _limitedData = false;
    }

    public void setKey(S keyIn) {

        _key = keyIn;
    }

    public S getKey() {

        return _key;
    }

    public void setSuccess(boolean successIn) {

        _success = successIn;
    }

    public boolean isSuccess() {

        return _success;
    }

    public void setAuthorizationRequired(boolean authorizationRequiredIn) {

        _requiresAuthorization = authorizationRequiredIn;
    }

    public boolean isAuthorizationRequired() {

        return _requiresAuthorization;
    }

    public void setAuthorizationList(List<DataSourceDef> authorizationListIn) {

        _authorizationList = authorizationListIn;
    }

    public List<DataSourceDef> getAuthorizationList() {

        return _authorizationList;
    }

    public void setResult(T resultIn) {

        _result = resultIn;
    }

    public T getResult() {

        return _result;
    }

    public void setException(String exceptionIn) {

        _exception = exceptionIn;
    }

    public String getException() {

        return _exception;
    }

    public void setMessage(ServerMessage messageIn) {

        _message = messageIn;
    }
    public ServerMessage getMessage() {

        return _message;
    }

    public void setArgumente(ResponseArgument argumentIn) {

        _argument = argumentIn;
    }


    public ResponseArgument getArgument() {

        return _argument;
    }

    public void setCount(long countIn) {

        _count = countIn;
    }

    public long getCount() {

        return _count;
    }

    public void setLimitedData(boolean limitedDataIn) {

        _limitedData = limitedDataIn;
    }

    public boolean getLimitedData() {

        return _limitedData;
    }
}

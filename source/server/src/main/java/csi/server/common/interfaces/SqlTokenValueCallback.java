package csi.server.common.interfaces;


/**
 * Created by centrifuge on 3/23/2015.
 */
public interface SqlTokenValueCallback {

    public String getFieldDisplayValue(String valueIn);
    public TokenExecutionValue getFieldExecutionValue(String valueIn);
    public String getParameterDisplayValue(String valueIn);
    public TokenExecutionValue getParameterExecutionValue(String valueIn);
}

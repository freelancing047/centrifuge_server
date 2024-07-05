package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Results of testing dynamic variables.
 * 
 * @author bstine
 */

public class CompilationResult implements IsSerializable {

    public boolean success;
    public String sampleValue;
    public String errorMsg;
    public String cause;
}

package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class FilterConstraintsRequest implements IsSerializable {

    public String dvUuid;
    public String vizUuid;
    // Flag for specifying if the filter query distinct values should be case sensitive
    public boolean caseSensitive;
    // Limit on the number of field values to be returned; if 0 no limit is considered
    public int limit;
}

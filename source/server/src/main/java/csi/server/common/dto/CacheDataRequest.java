package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO used to request rows of data from a DataView's cache.
 * 
 * @author bstine
 *
 */

public class CacheDataRequest implements IsSerializable {

    public String dataViewUuid;
    public String visualizationUuid;
    public Integer startRow;
    public Integer endRow;
}

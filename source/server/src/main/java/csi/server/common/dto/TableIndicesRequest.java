package csi.server.common.dto;



import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO used to request table indices
 * that correspond to internal_ids.
 *
 */

public class TableIndicesRequest implements IsSerializable {

    public String dataViewUuid;
    public String visualizationUuid;
    public List<Integer> rowIDs;
}

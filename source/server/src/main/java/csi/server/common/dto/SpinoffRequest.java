package csi.server.common.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.SpinoffTuple;

/**
 * <tt>SpinoffRequest</tt>.
 */
@Deprecated
/**
 * As of Hawkeye, this is no longer be in active use (...I think)
 */
public class SpinoffRequest implements IsSerializable {

    public String dataviewName;
    public String dataViewSessionID;
    public String visualizationID;
    public String requestType;
    public String graphTypes;
    public String spinoffName;
    public String selectedRows;
    public List<SpinoffTuple> tuples;
}

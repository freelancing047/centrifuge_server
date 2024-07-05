package csi.server.common.model.broadcast;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public enum BroadcastRequestType implements Serializable {

    FILTER_DISPLAY,
    FILTER_HIDE,
    SELECTION_ADD,
    SELECTION_REPLACE,
    SELECTION_REMOVE,
    CLEAR,
    FILTER_SET,
    SELECT_SET,
    PIN

}

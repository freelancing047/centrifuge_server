package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 6/4/2015.
 */
public enum JdbcDriverRestrictions implements Serializable {

    DRIVER_ACCESS,
    SOURCE_EDIT,
    CONNECTION_EDIT,
    QUERY_EDIT,
    DATA_PREVIEWING
}

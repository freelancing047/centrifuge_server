package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 5/11/2015.
 */
public enum CapcoSource implements Serializable {

    DATA_ONLY,
    USER_ONLY,
    USER_AND_DATA,
    USE_DEFAULT
}

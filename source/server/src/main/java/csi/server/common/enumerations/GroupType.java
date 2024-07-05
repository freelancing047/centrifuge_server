package csi.server.common.enumerations;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


public enum GroupType implements Serializable, IsSerializable {

    SHARING,            // Simple ownership and sharing of resources
                        // -- only users and SHARING groups can be members
    SECURITY,           // CAPCO style classification and compartmentalization
                        // -- only users and SECURITY groups can be members
    COUNTRY,            // Country trigraphs for RELTO and EYES-ONLY
                        // -- only users can be members
    TREATY              // Organization tetragraphs such as NATO
                        // -- only countries can be members
}

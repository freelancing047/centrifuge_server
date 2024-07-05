package csi.security;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.interfaces.KeyRetrieval;

/**
 * Created by centrifuge on 10/3/2016.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LinkupAclEntry implements KeyRetrieval {

    @Id
    private String key;
    private String driverAccessRole;    // can use driver -- required to view or export DV or Template

    public LinkupAclEntry() {
    }

    public LinkupAclEntry(String keyIn, String driverAccessRoleIn) {

        key = keyIn;
        driverAccessRole = driverAccessRoleIn;
    }

    public void setKey(String keyIn) {

        key = keyIn;
    }

    public String getKey() {

        return key;
    }

    public boolean getEnforce() {

        return true;
    }

    public void setDataAccessRole(String driverAccessRoleIn) {

        driverAccessRole = driverAccessRoleIn;
    }

    public String getDataAccessRole() {

        return driverAccessRole;
    }
}

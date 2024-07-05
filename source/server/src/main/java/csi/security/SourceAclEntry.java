package csi.security;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.AclSecurityMode;
import csi.server.common.interfaces.KeyRetrieval;

/**
 * Created by centrifuge on 9/22/2016.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SourceAclEntry implements KeyRetrieval {

    public static int driverAccessMask = (1 << AclSecurityMode.CONFIGURED_ACCESS.ordinal());
    public static int sourceEditMask = (1 << AclSecurityMode.CONFIGURED_SOURCE_EDIT.ordinal());
    public static int exportMask = (1 << AclSecurityMode.CONFIGURED_EXPORT.ordinal());

    @Id
    private String key;
    private String driverAccessRole;    // can use driver -- required to view or export DV or Template
    private String sourceEditRole;      // can edit source data -- restricts DV and Template DS Editor and exports
    private String connectionEditRole;  // can edit connection information -- restricts DV and Template exports
    
    public SourceAclEntry() {
    }

    public SourceAclEntry(String keyIn, String driverAccessRoleIn, String sourceEditRoleIn, String connectionEditRoleIn) {

        key = keyIn;
        driverAccessRole = driverAccessRoleIn;
        sourceEditRole = sourceEditRoleIn;
        connectionEditRole = connectionEditRoleIn;
    }

    public int getTagMask() {

        return ((null != driverAccessRole) ? driverAccessMask : 0)
                | ((null != driverAccessRole) || (null != sourceEditRole) ? sourceEditMask : 0)
                | ((null != driverAccessRole) || (null != sourceEditRole) || (null != connectionEditRole) ? exportMask : 0);
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

    public void setSourceEditRole(String sourceEditRoleIn) {

        sourceEditRole = sourceEditRoleIn;
    }

    public String getSourceEditRole() {

        return sourceEditRole;
    }

    public void setConnectionEditRole(String connectionEditRoleIn) {

        connectionEditRole = connectionEditRoleIn;
    }

    public String getConnectionEditRole() {

        return connectionEditRole;
    }
}

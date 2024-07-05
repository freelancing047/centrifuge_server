package csi.security;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.AclControlType;

/*
 * Access Types
 *
 * "delete" -- allows deleting and replacement of resource
 *
 * "read"   -- allows viewing of resource
 *
 * "edit"   -- allows viewing and changing of resource
 *
 *     (set means at least one entry exists with that particular type)
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AccessControlEntry implements IsSerializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    protected AclControlType accessType;
    protected String roleName;

    public AccessControlEntry() {
    }

    public AccessControlEntry(AclControlType typeIn, String roleIn) {

        accessType = typeIn;
        roleName = (null != roleIn) ? roleIn.trim().toLowerCase() : null;
    }

    public Long getID() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AclControlType getAccessType() {
        return accessType;
    }

    public void setAccessType(AclControlType accessType) {
        this.accessType = accessType;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public AccessControlEntry clone() {

        return new AccessControlEntry(getAccessType(), getRoleName());
    }

    public boolean match(Collection<String> roleListIn) {

        boolean mySuccess = false;

        return mySuccess;
    }
}

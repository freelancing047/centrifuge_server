package csi.security;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.interfaces.KeyRetrieval;

/**
 * Created by centrifuge on 10/2/2016.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GenericSecurityTag implements KeyRetrieval, IsSerializable, Comparable<GenericSecurityTag> {

    @Id
    protected String roleName;
    protected boolean enforce;

    public GenericSecurityTag() {
    }

    public GenericSecurityTag(String roleNameIn) {

        this(roleNameIn, true);
    }

    public GenericSecurityTag(String roleNameIn, boolean enforceIn) {

        roleName = roleNameIn.trim().toLowerCase();
        enforce = enforceIn;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleNameIn) {
        this.roleName = roleNameIn.toLowerCase();
    }

    public boolean getEnforce() {
        return enforce;
    }

    public void setEnforce(boolean enforceIn) {
        enforce = enforceIn;
    }

    public String getKey() {

        return roleName;
    }

    public int compareTo(GenericSecurityTag tagIn) {
        return (null != tagIn) ? roleName.compareTo(tagIn.getRoleName()) : 1;
    }
}

package csi.security;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.interfaces.KeyRetrieval;

/**
 * Created by centrifuge on 9/29/2016.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CapcoSecurityTag implements KeyRetrieval, Comparable<CapcoSecurityTag> {

    @Id
    protected String roleName;
    protected boolean enforce = true;

    public CapcoSecurityTag() {
    }

    public CapcoSecurityTag(String roleNameIn) {

        this();
        setRoleName(roleNameIn.trim().toLowerCase());
    }

    public CapcoSecurityTag(String roleIn, boolean enforceIn) {

        this(roleIn);
        setEnforce(enforceIn);
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleNameIn) {
        this.roleName = roleNameIn.trim().toLowerCase();
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

    public int compareTo(CapcoSecurityTag tagIn) {
        return (null != tagIn) ? roleName.compareTo(tagIn.getRoleName()) : 1;
    }
}

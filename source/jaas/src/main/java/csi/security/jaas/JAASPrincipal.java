package csi.security.jaas;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

/**
 * A JAASPrincipal represents the foundation for users, groups, and roles.
 * 
 * 
 * @author Centrifuge Systems Inc.
 *
 */
public class JAASPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String dn;
    private String[] roles;

    public JAASPrincipal(String userNameIn) {
        name = userNameIn;
    }

    public JAASPrincipal(String userNameIn, String dnIn, List<String> rolesIn) {
        name = userNameIn;
        dn = dnIn;
        roles = (null != rolesIn) ? rolesIn.toArray(new String[0]) : null;
    }

    public JAASPrincipal(String userNameIn, String dnIn) {
        name = userNameIn;
        dn = dnIn;
    }

    public boolean equals(Object o) {
        if (!(o instanceof JAASPrincipal)) {
            return false;
        }

        JAASPrincipal that = (JAASPrincipal) o;

        return getName().equals(that.getName());
    }

    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDN() {
        return dn;
    }

    public String[] getRoles() {
        return roles;
    }

    public String toString() {
        return getName();
    }

}

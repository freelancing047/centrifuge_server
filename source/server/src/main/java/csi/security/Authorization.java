package csi.security;

import java.util.Set;

public interface Authorization {

    public String getName();

    public boolean hasRole(String name);

    public boolean hasAnyRole(String[] roles);

    public boolean hasAllRoles(String[] roles);

    public String getDistinguishedName();

    public void setDistinguishedName(String dn);

    public Set<String> listRoles();

}

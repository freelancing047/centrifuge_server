package csi.security.jaas.spi;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.login.LoginException;

import csi.security.jaas.JAASRole;

public class RoleMapperModule extends SimpleLoginModule {

    private Principal mappedRole;

    private Set<String> allowedNames;

    @Override
    public boolean commit() throws LoginException {
        if (mappedRole != null) {
            boolean allowMapping = false;
            Set<Principal> principals = subject.getPrincipals();
            Iterator<Principal> iterator = principals.iterator();

            while (iterator.hasNext() && !allowMapping) {
                Principal role = iterator.next();
                if (allowedNames.contains(role.getName())) {
                    allowMapping = true;
                }
            }

            if (allowMapping) {
                principals.add(mappedRole);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Added mapped role");
                }
            }

        }

        return true;
    }

    @Override
    public boolean login() throws LoginException {
        verifyConfiguration();
        return true;
    }

    private void verifyConfiguration() {
        mappedRole = getMappedRole();
        allowedNames = getAllowedRoles();

        if (mappedRole == null) {
            if (allowedNames.size() > 0) {
                LOG.warn("No target role is specified for role mapping");
            }
        } else if (allowedNames.size() == 0) {
            LOG.warn("No source role names are provided.");
        }
    }

    private Set<String> getAllowedRoles() {
        String rolesValue = (String) options.get(Constants.PROPERTY_ROLE_NAMES);
        String[] split = rolesValue.split(",");

        Set<String> set = new HashSet<String>();

        if (split != null) {
            for (String name : split) {
                set.add(name.trim());
            }
        }

        return set;
    }

    private Principal getMappedRole() {
        String roleName = (String) options.get(Constants.PROPERTY_MAPPED_ROLE);
        roleName = (roleName == null) ? null : roleName.trim();

        Principal role = null;

        if (roleName != null && roleName.length() > 0) {
            role = new JAASRole(roleName);
        }

        return role;
    }

}

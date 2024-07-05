package csi.security.jaas.spi;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.login.LoginException;

import csi.security.jaas.JAASRole;

public class RoleAssignmentModule extends SimpleLoginModule {
   private String[] roles;

   public boolean abort() throws LoginException {
      return false;
   }

   public boolean commit() throws LoginException {
      boolean debug = LOG.isDebugEnabled();
      Set<Principal> principals = subject.getPrincipals();

      for (String role : roles) {
         if (debug) {
            String msg = "Adding role " + role;
            LOG.debug(msg);
         }
         principals.add(new JAASRole(role));
      }
      return true;
   }

   public boolean login() throws LoginException {
      verifyConfiguration();
      return true;
   }

   private void verifyConfiguration() {
      String tmp = (String) options.get(Constants.PROPERTY_ROLE_NAMES);

      if (tmp != null && tmp.trim().length() > 0) {
         roles = tmp.split(",");
      }
      for (int i = 0; i < roles.length; i++) {
         roles[i] = roles[i].trim();
      }
   }
}

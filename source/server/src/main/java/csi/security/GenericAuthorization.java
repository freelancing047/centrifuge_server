package csi.security;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.catalina.realm.GenericPrincipal;

import csi.security.jaas.JAASPrincipal;

public class GenericAuthorization implements Authorization {
   protected String name;
   private String dn;
   protected Set<String> roles = new HashSet<String>();

   public GenericAuthorization(String nameIn, Subject subjectIn) {
      if (nameIn != null) {
         name = nameIn.toLowerCase();
         roles.add(name);
      }
      if (subjectIn != null) {
         Collection<Principal> principals = subjectIn.getPrincipals();

         if (principals != null) {
            for (Principal role : principals) {
               roles.add(role.getName().toLowerCase());
            }
         }
      }
   }

//    GenericAuthorization(X509Certificate[] certsIn, Principal userIn, Subject subjectIn) {

   public GenericAuthorization(Principal userIn, Subject subjectIn) {
      this((userIn == null) ? null : userIn.getName(), subjectIn);
      Principal dnPrincipal = (userIn instanceof GenericPrincipal)
                                 ? ((GenericPrincipal) userIn).getUserPrincipal()
                                 : userIn;
      String[] myRoles = (userIn instanceof GenericPrincipal)
                            ? ((GenericPrincipal) userIn).getRoles()
                            : (userIn instanceof JAASPrincipal)
                                 ? ((JAASPrincipal) userIn).getRoles()
                                 : null;

      if ((myRoles != null) && (myRoles.length > 0)) {
         for (String myRole : myRoles) {
            roles.add(myRole.toLowerCase());
         }
      }
      if ((dnPrincipal != null) && (dnPrincipal instanceof JAASPrincipal)) {
         dn = ((JAASPrincipal) dnPrincipal).getDN();
      }
   }

   public GenericAuthorization(Principal user) {
      this(user, null);
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public boolean hasRole(String name) {
      return this.name.equalsIgnoreCase(name) || roles.contains(name.toLowerCase());
   }

   public String getDistinguishedName() {
      return dn;
   }

   public void setDistinguishedName(String dn) {
      this.dn = dn;
   }

   @Override
   public boolean hasAllRoles(String[] roles) {
      boolean result = true;

      if ((roles != null) && (roles.length > 0)) {
         for (String r : roles) {
            if (!hasRole(r.trim())) {
               result = false;
               break;
            }
         }
      }
      return result;
   }

   @Override
   public boolean hasAnyRole(String[] roles) {
      boolean result = true;

      if ((roles != null) && (roles.length > 0)) {
         result = false;

         for (String r : roles) {
            if (hasRole(r.trim())) {
               result = true;
               break;
            }
         }
      }
      return result;
   }

   public Set<String> listRoles() {
      return Collections.unmodifiableSet(roles);
   }
}

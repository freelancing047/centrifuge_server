package csi.license.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.license.persist.persistence.LicensePersistenceV1;
import csi.license.persist.persistence.LicensePersistenceV2;
import csi.security.queries.Users;

public class NamedLicense extends AbstractLicense {
   private static final Logger LOG = LogManager.getLogger(NamedLicense.class);

   public NamedLicense(final LicensePersistenceV1 licensePersistenceV1) {
      super(licensePersistenceV1);
   }

   public NamedLicense(final LicensePersistenceV2 licensePersistenceV2) {
      super(licensePersistenceV2);
   }

   //~ ---------------------------------------------------------------
   @Override
   public int nonAdminLicensesBeingUsed() {
      int currentNonAdminUserCount = 0;

      for (Boolean usage : userUsages.values()) {
         if (!usage.booleanValue()) {
            currentNonAdminUserCount++;
         }
      }
      return currentNonAdminUserCount;
   }

   @Override
   public boolean addingUserAllowed(final int persistentUserCount) {
      return (persistentUserCount < getUserCount());
   }

   @Override
   public boolean persistedUsersWithinLimit(final int persistentUserCount) {
      return (persistentUserCount <= getUserCount());
   }

   @Override
   public int availableLicenses(final int persistentUserCount) {
      return getUserCount() - persistentUserCount;
   }

   //~ ---------------------------------------------------------------
   private boolean userCountNotExceeded() {
      boolean result = (Users.getUserCount() <= getUserCount());

      if (!result) {
         LOG.error("User count ({}) exceeds licensed number of users: {}",
                   () -> Long.toString(Users.getUserCount()), () -> Integer.toString(getUserCount()));
      }
      return result;
   }

   @Override
   public boolean additionalValidationChecks() {
      return userCountNotExceeded();
   }
}

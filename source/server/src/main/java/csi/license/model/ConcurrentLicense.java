package csi.license.model;

import java.util.Map;
import java.util.PriorityQueue;

import csi.license.persist.persistence.LicensePersistenceV1;
import csi.license.persist.persistence.LicensePersistenceV2;

public class ConcurrentLicense extends AbstractLicense {
   private static final int MAX_CONCURRENT_USERS = 10000;

   public ConcurrentLicense(final LicensePersistenceV1 licensePersistenceV1) {
      super(licensePersistenceV1);
   }

   public ConcurrentLicense(final LicensePersistenceV2 licensePersistenceV2) {
      super(licensePersistenceV2);
   }

   //~ ---------------------------------------------------------------
   @Override
   public int nonAdminLicensesBeingUsed() {
      int sessionsUsed = 0;

      for (Map.Entry<String,PriorityQueue<SessionAt>> entry : userSessions.entrySet()) {
         Boolean licenseUsage = userUsages.get(entry.getKey());

         if ((licenseUsage != null) && !licenseUsage.booleanValue()) {
            sessionsUsed += entry.getValue().size();
         }
      }
      return sessionsUsed;
   }

   @Override
   public boolean addingUserAllowed(final int persistentUserCount) {
      return true;
   }

   @Override
   public boolean persistedUsersWithinLimit(final int persistentUserCount) {
      return true;
   }

   @Override
   public int availableLicenses(final int persistentUserCount) {
      return MAX_CONCURRENT_USERS;
   }
}

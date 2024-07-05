package csi.license.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.license.persist.persistence.LicensePersistenceV1;
import csi.license.persist.persistence.LicensePersistenceV2;
import csi.security.loginevent.EventReasons;
import csi.security.loginevent.LoginEventService;
import csi.server.util.Version;

public abstract class AbstractLicense {
   private static final Logger LOG = LogManager.getLogger(AbstractLicense.class);
   private static final Logger USERLOG = LogManager.getLogger(AbstractLicense.class.getName() + "_usage");

   private static final Comparator<SessionAt> MRU_SESSION_AT =
      new Comparator<SessionAt>() {
         public int compare(final SessionAt sessionAt1, final SessionAt sessionAt2) {
            return sessionAt2.getLastUsed().compareTo(sessionAt1.getLastUsed());
         }
   };

   private String customer;
   private int versionMajor;
   private int versionMinor;
   private int versionLabel;
   private int userCount;
   private boolean internal;
   private boolean expiring;
   private boolean nodeLock;
   private ZonedDateTime startDateTime;
   private ZonedDateTime endDateTime;
   private ScheduledExecutorService expirationNotificationScheduler;

   protected Map<String,PriorityQueue<SessionAt>> userSessions;
   protected Map<String,Boolean> userUsages;

   public AbstractLicense(final LicensePersistenceV1 licensePersistenceV1) {
      customer = licensePersistenceV1.getCustomer();
      versionMajor = licensePersistenceV1.getVersionMajor();
      versionMinor = licensePersistenceV1.getVersionMinor();
      versionLabel = licensePersistenceV1.getVersionLabel();
      userCount = licensePersistenceV1.getUserCount();
      internal = licensePersistenceV1.isInternal();
      expiring = licensePersistenceV1.isExpiring();
      nodeLock = licensePersistenceV1.isNodeLock();
      startDateTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
      endDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(licensePersistenceV1.getExpirationDate().getTime()), ZoneId.systemDefault());

      if (expiring) {
         scheduleExpiringLicenseNotifications(endDateTime);
      }
      userSessions = new HashMap<String,PriorityQueue<SessionAt>>();
      userUsages = new HashMap<String,Boolean>();
   }

   public AbstractLicense(final LicensePersistenceV2 licensePersistenceV2) {
      customer = licensePersistenceV2.getCustomer();
      versionMajor = licensePersistenceV2.getVersionMajor();
      versionMinor = licensePersistenceV2.getVersionMinor();
      versionLabel = licensePersistenceV2.getVersionLabel();
      userCount = licensePersistenceV2.getUserCount();
      internal = licensePersistenceV2.isInternal();
      expiring = licensePersistenceV2.isExpiring();
      nodeLock = licensePersistenceV2.isNodeLock();
      startDateTime = licensePersistenceV2.getStartDateTime();
      endDateTime = licensePersistenceV2.getExpirationDateTime();

      if (expiring) {
         scheduleExpiringLicenseNotifications(endDateTime);
      }
      userSessions = new HashMap<String,PriorityQueue<SessionAt>>();
      userUsages = new HashMap<String,Boolean>();
   }

   public void shutdown() {
      if (expiring) {
         expirationNotificationScheduler.shutdownNow();
      }
      removeAllUsersFromLicense();
   }

   //~ ---------------------------------------------------------------
   public abstract int nonAdminLicensesBeingUsed();
   public abstract boolean addingUserAllowed(final int persistentUserCount);
   public abstract boolean persistedUsersWithinLimit(final int persistentUserCount);
   public abstract int availableLicenses(final int persistentUserCount);

   public class SessionAt {
      private String sessionId;
      private LocalDateTime lastUsed;

      public SessionAt(final String sessionId) {
         this.sessionId = sessionId;
         lastUsed = LocalDateTime.now();
      }

      public String getSessionId() {
         return sessionId;
      }
      public LocalDateTime getLastUsed() {
         return lastUsed;
      }

      public void setSessionId(final String sessionId) {
         this.sessionId = sessionId;
      }
      public void setLastUsed(final LocalDateTime lastUsed) {
         this.lastUsed = lastUsed;
      }
   }

   public void sessionActivityForUser(final String userName, final String sessionId) {
      PriorityQueue<SessionAt> namedUserSessions = userSessions.get(userName);

      if (namedUserSessions == null) {
         namedUserSessions = new PriorityQueue<SessionAt>(3, MRU_SESSION_AT);

         namedUserSessions.add(new SessionAt(sessionId));
         userSessions.put(userName, namedUserSessions);
      } else {
         boolean found = false;

         synchronized (namedUserSessions) {
            for (SessionAt namedUserSession : namedUserSessions) {
               if (namedUserSession.getSessionId().equals(sessionId)) {
                  namedUserSessions.remove(namedUserSession);
                  namedUserSession.setLastUsed(LocalDateTime.now());
                  namedUserSessions.add(namedUserSession);
                  found = true;
                  break;
               }
            }
         }
         if (!found) {
            namedUserSessions.add(new SessionAt(sessionId));
         }
      }
   }

   private boolean removeUserSessionActivity(final String userName) {
      boolean removed = false;
      PriorityQueue<SessionAt> namedUserSessions = userSessions.get(userName);

      namedUserSessions.poll();

      if (namedUserSessions.isEmpty()) {
         userSessions.remove(userName);
         removed = true;
      }
      return removed;
   }

   protected boolean inInclusiveLicenseRange(final ZonedDateTime now) {
      return !now.isBefore(startDateTime) && now.isBefore(endDateTime);
   }

   private void pruneStaleSessions() {
      LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

      for (PriorityQueue<SessionAt> sessions : userSessions.values()) {
         for (SessionAt eachSession : sessions) {
            if (eachSession.getLastUsed().isBefore(tenMinutesAgo)) {
               sessions.remove(eachSession);
            }
         }
      }
      for (Map.Entry<String,PriorityQueue<SessionAt>> entry : userSessions.entrySet()) {
         if (entry.getValue().isEmpty()) {
            userSessions.remove(entry.getKey());
         }
      }
   }

   public boolean licenseAvailable(final boolean isAdmin) {
      return (isAdmin || (nonAdminLicensesBeingUsed() < userCount));
   }

   public boolean addUserToLicense(final String userName, final boolean admin) {
      boolean added = false;

      if (!isExpiring() || inInclusiveLicenseRange(ZonedDateTime.now())) {
         pruneStaleSessions();

         if (licenseAvailable(admin)) {
            int licensesUsed = (nonAdminLicensesBeingUsed() + (admin ? 0 : 1));

            userUsages.put(userName, Boolean.valueOf(admin));
            USERLOG.info("add,{},{}", () -> userName, () -> Integer.toString(licensesUsed));
            added = true;
         }
      }
      return added;
   }

   public boolean removeUserFromLicense(final String userName, final EventReasons reason) {
      boolean result = userUsages.containsKey(userName);

      if (result) {
         removeUserSessionActivity(userName);
         userUsages.remove(userName);
         USERLOG.info("remove,{},{},{}", () -> userName,
                      () -> Integer.toString(nonAdminLicensesBeingUsed()),
                      () -> reason.name().toLowerCase());
      }
      return result;
   }

   public void removeAllUsersFromLicense() {
      for (String userName : userSessions.keySet()) {
         if (removeUserFromLicense(userName, EventReasons.LOGOUT_SERVER_SHUTDOWN)) {
            LoginEventService.saveLogoutServerShutdown(userName);
         }
      }
   }

   //~ ---------------------------------------------------------------
   public boolean isValid() {
      return licenseValidForThisVersion() &&
             licenseHasStarted() &&
             licenseHasNotExpired() &&
             additionalValidationChecks();
   }

   private boolean licenseValidForThisVersion() {
      boolean result = ((versionMajor == Version.getMajorVersion()) &&
                        (versionMinor == Version.getMinorVersion()));

      if (!result) {
         LOG.error("License version {}.{} not valid for this version: {}.{}",
                   () -> Integer.toString(versionMajor),
                   () -> Integer.toString(versionMinor),
                   () -> Integer.toString(Version.getMajorVersion()),
                   () -> Integer.toString(Version.getMinorVersion()));
      }
      return result;
   }

   private boolean licenseHasStarted() {
      boolean result = (!expiring || startDateTime.isBefore(ZonedDateTime.now()));

      if (!result) {
         LOG.info("License does not become valid until: {}", () -> startDateTime);
      }
      return result;
   }

   private boolean licenseHasNotExpired() {
      boolean result = (!expiring || endDateTime.isAfter(ZonedDateTime.now()));

      if (!result) {
         LOG.info("License expired on: {}", () -> endDateTime);
      }
      return result;
   }

   public boolean additionalValidationChecks() {
      return true;
   }

   //~ ---------------------------------------------------------------
   public String getCustomer() {
      return customer;
   }
   public int getVersionMajor() {
      return versionMajor;
   }
   public int getVersionMinor() {
      return versionMinor;
   }
   public int getVersionLabel() {
      return versionLabel;
   }
   public int getUserCount() {
      return userCount;
   }
   public boolean isInternal() {
      return internal;
   }
   public boolean isExpiring() {
      return expiring;
   }
   public boolean isNodeLock() {
      return nodeLock;
   }
   public ZonedDateTime getStartDateTime() {
      return startDateTime;
   }
   public ZonedDateTime getEndDateTime() {
      return endDateTime;
   }

   public void setCustomer(final String customer) {
      this.customer = customer;
   }
   public void setVersionMajor(final int versionMajor) {
      this.versionMajor = versionMajor;
   }
   public void setVersionMinor(final int versionMinor) {
      this.versionMinor = versionMinor;
   }
   public void setVersionLabel(final int versionLabel) {
      this.versionLabel = versionLabel;
   }
   public void setUserCount(final int userCount) {
      this.userCount = userCount;
   }
   public void setInternal(final boolean internal) {
      this.internal = internal;
   }
   public void setExpiring(final boolean expiring) {
      this.expiring = expiring;
   }
   public void setNodeLock(final boolean nodeLock) {
      this.nodeLock = nodeLock;
   }
   public void setStartDateTime(final ZonedDateTime startDateTime) {
      this.startDateTime = startDateTime;
   }
   public void setEndDateTime(final ZonedDateTime endDateTime) {
      this.endDateTime = endDateTime;
   }

   //~ ---------------------------------------------------------------
   /*
    * If a server is operating under an expiring license, we need to register a timer. It is not sufficient to check
    * the expiration date on startup alone, as a long running server could keep running through the expiration date.
    *
    * The timer will warn the user, via log messages, at intervals of: -30 days prior to expiration -7 days prior to
    * expiration -Daily all the way down to the last day At expiration, the server will then terminate.
    */
   public class NumberOfDaysNotice implements Runnable {
      private int numberOfDays;

      public NumberOfDaysNotice(final int numberOfDays) {
         this.numberOfDays = numberOfDays;
      }

      public void run() {
         LOG.info("WARNING: LICENSE WILL EXPIRE IN {} DAY{}.",
                  () -> Integer.valueOf(numberOfDays), () -> (numberOfDays == 1) ? "" : "S");
      }
   }

   public void scheduleExpiringLicenseNotifications(final ZonedDateTime expirationDateTime) {
      expirationNotificationScheduler = Executors.newScheduledThreadPool(9);

      // Schedule the expiration timer
      expirationNotificationScheduler.schedule(
         new Runnable() {
            public void run() {
               LOG.info("YOUR LICENSE HAS EXPIRED.");
               System.exit(-1);
            }
         },
         expirationDateTime.toInstant().toEpochMilli(),
         TimeUnit.MILLISECONDS);

      // Set the 30 day and final 7 day timers. Check on each one to make sure that we are
      // not already inside that window.
      ZonedDateTime now = ZonedDateTime.now();

      for (int i = 0; i < 8; i++) {
         int daysOffset = (i == 0) ? 30 : i;

         if (now.plusDays(daysOffset).isBefore(expirationDateTime)) {
            expirationNotificationScheduler.schedule(
               new NumberOfDaysNotice(daysOffset),
               expirationDateTime.minusDays(daysOffset).toInstant().toEpochMilli(),
               TimeUnit.MILLISECONDS);
         }
      }
   }
}

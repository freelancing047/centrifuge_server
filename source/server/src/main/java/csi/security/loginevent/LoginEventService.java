package csi.security.loginevent;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.queries.Users;
import csi.startup.Product;

public class LoginEventService {
   private static final Logger LOG = LogManager.getLogger(LoginEventService.class);

   private static final Set<EventReasons> USER_KNOWN_CHECK_NEEDED =
      new HashSet<EventReasons>(Arrays.asList(EventReasons.LOGIN_UNKNOWN_USER, EventReasons.LOGIN_BAD_PASSWORD));

   public static void saveLoginEvent(final String userName, final EventReasons reason, final ZonedDateTime eventDateTime) {
      try {
         Timestamp timestamp = Timestamp.valueOf(eventDateTime.toLocalDateTime());
         LoginEventRepository.saveLoginEvent(
            new LoginEvent(reason, timestamp, userName,
                           !USER_KNOWN_CHECK_NEEDED.contains(reason) || (Users.getUserByName(userName) != null),
                           Product.getLicense().nonAdminLicensesBeingUsed(), (int) Users.getUserCount()));
//LOG.fatal(userName + " " + reason.name());
      } catch (RepositoryException re) {
         LOG.error(re.getMessage());
      }
   }

   public static void saveLoginEvent(final String userName, final EventReasons reason) {
      saveLoginEvent(userName, reason, ZonedDateTime.now());
   }

   //~ EventReasons --------------------------------------------------
   public static void saveLoginSuccess(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGIN_SUCCESS);
   }

   public static void saveLoginUnknownUser(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGIN_UNKNOWN_USER);
   }

   public static void saveLoginBadPassword(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGIN_BAD_PASSWORD);
   }

   public static void saveLoginDisabled(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGIN_DISABLED);
   }

   public static void saveLoginConcurrentLimit(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGIN_CONCURRENT_LIMIT);
   }

   public static void saveLogoutSuccess(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGOUT_SUCCESS);
   }

   public static void saveLogoutServerShutdown(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGOUT_SERVER_SHUTDOWN);
   }

   public static void saveLogoutEvicted(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGOUT_EVICTED);
   }

   public static void saveLogoutTimeout(final String userName) {
      saveLoginEvent(userName, EventReasons.LOGOUT_INACTIVITY_TIMEOUT);
   }

   //~ LoginEvent ----------------------------------------------------
   public static Map<Integer,YearEventPeriods> fetchLoginEvents(final ZonedDateTime start, final ZonedDateTime end)
         throws RepositoryException {
      return LoginEventRepository.fetchLoginEvents(start, end);
   }
}

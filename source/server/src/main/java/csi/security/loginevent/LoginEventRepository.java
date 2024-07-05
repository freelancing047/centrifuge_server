package csi.security.loginevent;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import csi.server.dao.CsiPersistenceManager;

public class LoginEventRepository {
   public static void saveLoginEvent(final LoginEvent loginEvent) throws RepositoryException {
      EntityManager manager = CsiPersistenceManager.createMetaEntityManager();
      EntityTransaction transaction = manager.getTransaction();
      try {
         transaction.begin();
         manager.persist(loginEvent);
         transaction.commit();
      } catch (Exception exception) {
         transaction.rollback();
         throw new RepositoryException(exception);
      } finally {
         manager.close();
      }
   }

   private static void addEvent(final Map<Integer,YearEventPeriods> yearEvents, final LoginEvent loginEvent) {
      Calendar cal = Calendar.getInstance();
      long timestamp = loginEvent.getEventDateTime().getTime();
      cal.setTimeInMillis(timestamp);
      Integer year = Integer.valueOf(cal.get(Calendar.YEAR));
      YearEventPeriods yearPeriod = yearEvents.get(year);

      if (yearPeriod == null) {
         yearPeriod = new YearEventPeriods(year.intValue());

         yearEvents.put(year, yearPeriod);
      }
      yearPeriod.addEvent(loginEvent);
   }

   public static Map<Integer,YearEventPeriods> fetchLoginEvents(final ZonedDateTime start, final ZonedDateTime end)
         throws RepositoryException {
      Map<Integer,YearEventPeriods> loginEvents = new HashMap<Integer,YearEventPeriods>();
      boolean activeOnEntry = CsiPersistenceManager.isActive();

      try {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         String sql =
            new StringBuilder(
               "SELECT *" +
                " FROM login_events" +
               " WHERE (event_date_time >= TIMESTAMP '").append(Timestamp.valueOf(start.toLocalDateTime())).append("')" +
                 " AND (event_date_time <= TIMESTAMP '").append(Timestamp.valueOf(end.toLocalDateTime())).append("')").toString();
         Query query = manager.createNativeQuery(sql, LoginEvent.class);

         for (Object loginEvent : query.getResultList()) {
            addEvent(loginEvents, (LoginEvent) loginEvent);
         }
      } catch (Exception exception) {
         throw new RepositoryException(exception);
      } finally {
         if (!activeOnEntry) {
            CsiPersistenceManager.close();
         }
      }
      return loginEvents;
   }
}

package csi.security.loginevent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginEventReportService {
   private static final Logger LOG = LogManager.getLogger(LoginEventReportService.class);

   private MailService mailService = new MailService();

   private boolean useEmail = true;
   private boolean useFile = false;

   public class LoginEventReportRunner implements Runnable {

      @Override
      public void run() {
         LocalDate runAt = LocalDate.now();
         LocalDate startLocalDate = runAt.minusDays(1);
         ZonedDateTime start = startLocalDate.atStartOfDay(ZoneId.systemDefault());
         ZonedDateTime end = start.plusDays(1);

         try {
            Map<Integer,YearEventPeriods> loginEvents = LoginEventService.fetchLoginEvents(start, end);
            YearEventPeriods yearEventPeriods = loginEvents.get(Integer.valueOf(start.getYear()));
            if(useEmail) {
               mailService.send(yearEventPeriods.getDailyEvents().get(startLocalDate));
            }
            if(useFile) {
               //fileService.send(yearEventPeriods.getDailyEvents().get(startLocalDate));
            }
         } catch (RepositoryException re) {
            LOG.error(re.getMessage());
         }
      }
   }

   private static ScheduledExecutorService reportSenderScheduler;

   private LoginEventReportService() {
   }

   public static void initialize(final int hourOfDayToRunReport) {
      reportSenderScheduler = Executors.newScheduledThreadPool(1);

      reportSenderScheduler
         .scheduleAtFixedRate(
            new LoginEventReportService().new LoginEventReportRunner(),
            LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).plusHours(hourOfDayToRunReport)
               .toInstant().toEpochMilli() + 1,
            Duration.ofDays(1).toMillis(), TimeUnit.MILLISECONDS);
   }

   public static void shutdown() {
      reportSenderScheduler.shutdownNow();
   }
}

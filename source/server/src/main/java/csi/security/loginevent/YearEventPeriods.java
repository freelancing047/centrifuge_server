package csi.security.loginevent;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

public class YearEventPeriods {
   private LocalDate start;
   private LocalDate end;
   private PeriodLoginEvents yearlyEvents;
   private PeriodLoginEvents[] monthlyEvents;
   private Map<LocalDate,PeriodLoginEvents> dailyEvents;

   public YearEventPeriods(final int year) {
      start = LocalDate.of(year, Month.JANUARY, 1);
      end = LocalDate.of(year + 1, Month.JANUARY, 1);
      yearlyEvents = new PeriodLoginEvents(Period.between(start, end));
      monthlyEvents = new PeriodLoginEvents[12];
      long days = ChronoUnit.DAYS.between(start, end);

      for (int i = 0; i < 11; i++) {
         monthlyEvents[i] =
            new PeriodLoginEvents(Period.between(LocalDate.of(year, i + 1, 1), LocalDate.of(year, i + 2, 1)));
      }
      monthlyEvents[11] = new PeriodLoginEvents(Period.between(LocalDate.of(year, Month.DECEMBER, 1), end));
      dailyEvents = new TreeMap<LocalDate,PeriodLoginEvents>();

      for (int i = 0; i < days; i++) {
         LocalDate eachDayDate = start.plusDays(i);

         dailyEvents.put(eachDayDate, new PeriodLoginEvents(Period.between(eachDayDate, eachDayDate.plusDays(1))));
      }
   }

   private boolean contains(final LocalDate localDate) {
      return !localDate.isBefore(start) && localDate.isBefore(end);
   }

   private LocalDate makeLocalDate(Timestamp timestamp) {
      ZoneId defaultZoneId = ZoneId.systemDefault();
      return timestamp.toInstant().atZone(defaultZoneId).toLocalDate();
   }

   public void addEvent(final LoginEvent loginEvent) {
      LocalDate eventLocalDate = makeLocalDate(loginEvent.getEventDateTime());

      if (contains(eventLocalDate)) {
         yearlyEvents.getPeriodEvents().add(loginEvent);
         monthlyEvents[eventLocalDate.getMonth().getValue() - 1].getPeriodEvents().add(loginEvent);
         dailyEvents.get(eventLocalDate).getPeriodEvents().add(loginEvent);
      }
   }

   public LocalDate getStart() {
      return start;
   }
   public LocalDate getEnd() {
      return end;
   }
   public PeriodLoginEvents getYearlyEvents() {
      return yearlyEvents;
   }
   public PeriodLoginEvents[] getMonthlyEvents() {
      return monthlyEvents;
   }
   public Map<LocalDate,PeriodLoginEvents> getDailyEvents() {
      return dailyEvents;
   }

   public void setStart(final LocalDate start) {
      this.start = start;
   }
   public void setEnd(final LocalDate end) {
      this.end = end;
   }
   public void setYearlyEvents(final PeriodLoginEvents yearlyEvents) {
      this.yearlyEvents = yearlyEvents;
   }
   public void setMonthlyEvents(final PeriodLoginEvents[] monthlyEvents) {
      this.monthlyEvents = monthlyEvents;
   }
   public void setDailyEvents(final Map<LocalDate,PeriodLoginEvents> dailyEvents) {
      this.dailyEvents = dailyEvents;
   }
}

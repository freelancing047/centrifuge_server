package csi.security.loginevent;

import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class PeriodLoginEvents {
   private Period eventPeriod;
   private Set<LoginEvent> periodEvents;

   public PeriodLoginEvents(final Period eventPeriod, final Collection<LoginEvent> periodEvents) {
      this.eventPeriod = eventPeriod;
      this.periodEvents = new TreeSet<LoginEvent>(Comparator.comparing(LoginEvent::getEventDateTime));

      this.periodEvents.addAll(periodEvents);
   }

   public PeriodLoginEvents(final Period eventPeriod) {
      this(eventPeriod, new ArrayList<LoginEvent>());
   }

   public Period getEventPeriod() {
      return eventPeriod;
   }
   public Set<LoginEvent> getPeriodEvents() {
      return periodEvents;
   }

   public void setEventPeriod(final Period eventPeriod) {
      this.eventPeriod = eventPeriod;
   }
   public void setPeriodEvents(final Set<LoginEvent> periodEvents) {
      this.periodEvents = periodEvents;
   }
}

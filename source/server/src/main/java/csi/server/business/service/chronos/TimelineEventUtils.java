package csi.server.business.service.chronos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.timeline.TimelineEventDefinition;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.util.CsiTypeUtil;
import csi.shared.core.visualization.timeline.BaseTimelineEvent;
import csi.shared.core.visualization.timeline.SummarizedTimelineEvent;
import csi.shared.gwt.viz.timeline.TimeUnit;

public class TimelineEventUtils {
   public static void fixAndValidateEvents(List<SummarizedTimelineEvent> eventsInRow, TimelineSettings settings,
                                           Map<TimelineEventDefinition, Boolean> useEndAsStartMap,
                                           Map<TimelineEventDefinition, Boolean> startCalculationMap,
                                           Map<TimelineEventDefinition, Boolean> endCalculationMap) {
      int count = 0;

      for (TimelineEventDefinition eventDefinition : settings.getEvents()) {
         if (useEndAsStartMap.get(eventDefinition).booleanValue()) {
            moveEndToStart(eventsInRow.get(count), eventDefinition);
         }
         if (startCalculationMap.get(eventDefinition).booleanValue()) {
            calculateStartFromEnd(eventsInRow.get(count), eventDefinition);
         }
         if (endCalculationMap.get(eventDefinition).booleanValue()) {
            calculateEndFromStart(eventsInRow.get(count), eventDefinition);
         }
         count++;
      }
      removeBadEvents(eventsInRow);
   }

    private static void removeBadEvents(List<SummarizedTimelineEvent> eventsInRow) {
        for(int ii=eventsInRow.size()-1; ii>=0; ii--){
            SummarizedTimelineEvent event = eventsInRow.get(ii);
            if(event.getStartTime() == null){
                eventsInRow.remove(ii);
            }
        }
    }

    private static void calculateStartFromEnd(SummarizedTimelineEvent timelineEvent, TimelineEventDefinition eventDefinition) {

        if((timelineEvent.getEndTime() != null) && (timelineEvent.getStartTime() != null)){
            timelineEvent.setStartTime(timelineEvent.getEndTime() - timelineEvent.getStartTime());
        }
        else {
            timelineEvent.setStartTime(null);
        }

    }

    private static void calculateEndFromStart(SummarizedTimelineEvent timelineEvent, TimelineEventDefinition eventDefinition) {

        if((timelineEvent.getEndTime() != null) && (timelineEvent.getStartTime() != null)){
            timelineEvent.setEndTime(timelineEvent.getStartTime() + timelineEvent.getEndTime());
        }
        else {
            timelineEvent.setStartTime(null);
        }

    }

    private static void moveEndToStart(SummarizedTimelineEvent timelineEvent, TimelineEventDefinition eventDefinition) {

        timelineEvent.setStartTime(timelineEvent.getEndTime());
        timelineEvent.setEndTime(null);


    }

    public static void populateEventsWithDate(Object value, FieldDef fieldDef, List<SummarizedTimelineEvent> eventsInRow, TimelineSettings settings) {
        if(value == null){
            return;
        }
        Long time = CsiTypeUtil.coerceDate(value).getTime();
        int count = 0;
        for(SummarizedTimelineEvent event: eventsInRow){
            addValueBasedOnEventDefinition(time, fieldDef, event, settings.getEvents().get(count));
            addValueBasedOnSettings(time, fieldDef, event, settings);
            count++;
        }

    }

    public static void populateEventsWithNumber(Object value, FieldDef fieldDef, List<SummarizedTimelineEvent> eventsInRow, TimelineSettings settings) {
        double size = CsiTypeUtil.coerceDecimal(value);

        int count = 0;
        for(SummarizedTimelineEvent event: eventsInRow){
            addValueBasedOnEventDefinition(size, fieldDef, event, settings.getEvents().get(count));
            addValueBasedOnSettings(size, fieldDef, event, settings);
            count++;
        }

    }

    public static void populateEventsWithInteger(Object value, FieldDef fieldDef, List<SummarizedTimelineEvent> eventsInRow, TimelineSettings settings) {
        double size = CsiTypeUtil.coerceLong(value);

        int count = 0;
        for(SummarizedTimelineEvent event: eventsInRow){
            addValueBasedOnEventDefinition(size, fieldDef, event, settings.getEvents().get(count));
            addValueBasedOnSettings(size, fieldDef, event, settings);
            count++;
        }

    }

    public static void populateEventsWithTime(Object value, FieldDef fieldDef, List<SummarizedTimelineEvent> eventsInRow, TimelineSettings settings) {
        Long time = CsiTypeUtil.coerceTime(value).getTime();
        int count = 0;
        for(SummarizedTimelineEvent event: eventsInRow){
            addValueBasedOnEventDefinition(time, fieldDef, event, settings.getEvents().get(count));
            addValueBasedOnSettings(time, fieldDef, event, settings);
            count++;
        }
    }

    public static void populateEventsWithString(Object value, FieldDef fieldDef, List<SummarizedTimelineEvent> eventsInRow, TimelineSettings settings) {
        String object = value.toString();
        int count = 0;
        for(SummarizedTimelineEvent event: eventsInRow){
            addValueBasedOnEventDefinition(object, fieldDef, event, settings.getEvents().get(count));
            addValueBasedOnSettings(object, fieldDef, event, settings);
            count++;
        }
    }



    private static void addValueBasedOnEventDefinition(Object value, FieldDef fieldDef, SummarizedTimelineEvent event,
            TimelineEventDefinition timelineEventDefinition) {

        if((timelineEventDefinition.getStartField() != null) && timelineEventDefinition.getStartField().getFieldDef().equals(fieldDef)){
            event.setStartTime((Long) value);
        }
        if((timelineEventDefinition.getEndField() != null) && timelineEventDefinition.getEndField().getFieldDef().equals(fieldDef)){
            event.setEndTime((Long) value);
        }
//        if(timelineEventDefinition.getLabelField().equals(fieldDef)){
//            event.setLabel((String) value);
//        }

    }

    private static void addValueBasedOnSettings(Object value, FieldDef fieldDef, SummarizedTimelineEvent event,
            TimelineSettings settings) {

        if((settings.getGroupByField() != null) && settings.getGroupByField().equals(fieldDef)){
            event.setTrackValue((String)value);
        }
//        if(settings.getColorByField().equals(fieldDef)){
//            event.setColorValue((String) value);
//        }
//        if(settings.getDotSize().equals(fieldDef)){
//            event.setDotSize((Double) value);
//        }
//        if(settings.getFieldList().contains(fieldDef)){
//
//        }

    }

   public static <T extends BaseTimelineEvent> List<SummarizedTimelineEvent> summarize(List<T> tobeSummarizedEvents,
                                                                                       TimeUnit unit, int limit) {
      int howManyToBeSummarizedEvents = tobeSummarizedEvents.size();
      List<SummarizedTimelineEvent> summarizedEvents = new ArrayList<SummarizedTimelineEvent>(howManyToBeSummarizedEvents / 2);

       Collections.sort(tobeSummarizedEvents);

//        summarizedEvents.add(new SummarizedTimelineEvent(tobeSummarizedEvents.get(0)));
//        tobeSummarizedEvents.remove(0);
       int lastIndex = 0;

       for (int i = 0; i < howManyToBeSummarizedEvents; i++) {
          if (i == 0) {
             summarizedEvents.add(new SummarizedTimelineEvent(tobeSummarizedEvents.get(0)));
             continue;
          }
          BaseTimelineEvent event = tobeSummarizedEvents.get(i);
          for (int ii = lastIndex; ii < summarizedEvents.size(); ii++) {

             lastIndex = ii;
             if ((summarizedEvents.get(ii).getStartTime() + unit.getRoughSize()) >= event.getStartTime()) {
                adjustSummary(summarizedEvents.get(ii), event);
                break;
             } else {
                lastIndex++;
                if (!(event instanceof SummarizedTimelineEvent)) {
                   SummarizedTimelineEvent summaryEvent = new SummarizedTimelineEvent(event);
                   summarizedEvents.add(summaryEvent);
                   if (summarizedEvents.size() > limit) {
                      return summarizedEvents;
                   }
                } else {
                   summarizedEvents.add((SummarizedTimelineEvent) event);
                   if (summarizedEvents.size() > limit) {
                      return summarizedEvents;
                   }
                }
                break;
             }

          }
       }

       return summarizedEvents;
    }


    private static void adjustSummary(BaseTimelineEvent firstEvent, BaseTimelineEvent event) {
        if(firstEvent.getEndTime() == null){
            firstEvent.setEndTime(event.getEndTime());
        } else if((event.getEndTime() != null) && (firstEvent.getEndTime() < event.getEndTime())){
            firstEvent.setEndTime(event.getEndTime());
        }

        firstEvent.combine(event);
    }
}

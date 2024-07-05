package csi.server.business.service.chronos.storage.postgres;

import java.util.List;
import java.util.function.Function;

import com.mongodb.DBObject;

import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.OverviewTrack;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;
import csi.shared.core.visualization.timeline.TimelineTrack;

public class DataToTimelineResultTransformer implements Function<Object,DetailedTimelineResult> {
   public DataToTimelineResultTransformer() {
   }

   @SuppressWarnings("unchecked")
   @Override
   public DetailedTimelineResult apply(Object object) {
      DetailedTimelineResult timelineResult = null;

      if (object instanceof DBObject) {
         DBObject result = (DBObject) object;
         timelineResult = new DetailedTimelineResult();
         List<SingularTimelineEvent> events = (List<SingularTimelineEvent>) result.get(TimelineKeyConstants.EVENTS_KEY);

         timelineResult.setEvents(events);
         timelineResult.setTracks((List<TimelineTrack>) result.get(TimelineKeyConstants.TRACKS_KEY));
         timelineResult.setMax((double) result.get(TimelineKeyConstants.LARGEST_VALUE_KEY));
         timelineResult.setMin((double) result.get(TimelineKeyConstants.SMALLEST_VALUE_KEY));
         timelineResult.setTotalEvents((int) result.get(TimelineKeyConstants.EVENT_COUNT_KEY));
         timelineResult.setOverviewData((OverviewTrack) result.get(TimelineKeyConstants.OVERVIEW_KEY));
         timelineResult.setGroupLimit((boolean) result.get(TimelineKeyConstants.GROUP_LIMIT_KEY));
         timelineResult.setColorLimit((boolean) result.get(TimelineKeyConstants.COLOR_LIMIT_KEY));
      }
      return timelineResult;
   }
}

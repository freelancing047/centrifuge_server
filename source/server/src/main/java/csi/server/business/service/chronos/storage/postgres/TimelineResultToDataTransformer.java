package csi.server.business.service.chronos.storage.postgres;

import java.util.function.Function;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import csi.shared.core.visualization.timeline.DetailedTimelineResult;

public class TimelineResultToDataTransformer implements Function<DetailedTimelineResult,DBObject> {
   @Override
   public DBObject apply(DetailedTimelineResult result) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

      builder.append(TimelineKeyConstants.EVENTS_KEY, result.getEvents());
      builder.append(TimelineKeyConstants.TRACKS_KEY, result.getTracks());
      builder.append(TimelineKeyConstants.LARGEST_VALUE_KEY, result.getMax());
      builder.append(TimelineKeyConstants.SMALLEST_VALUE_KEY, result.getMin());
      builder.append(TimelineKeyConstants.EVENT_COUNT_KEY, result.getEvents().size());
      builder.append(TimelineKeyConstants.OVERVIEW_KEY, result.getOverviewData());
      builder.append(TimelineKeyConstants.GROUP_LIMIT_KEY, result.isGroupLimit());
      builder.append(TimelineKeyConstants.COLOR_LIMIT_KEY, result.isColorLimit());
      return builder.get();
   }
}

package csi.server.business.service.export.csv;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.cachedb.querybuilder.TimelineQueryBuilder;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.service.chronos.ChronosActionsService;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.DateUtil;
import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;
import csi.shared.core.visualization.timeline.TimelineTrack;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Writes the table data as a CSV.
 *
 * @author Centrifuge Systems, Inc.
 */
public class TimelineCsvWriter implements CsvWriter {
   private static final Logger LOG = LogManager.getLogger(TimelineCsvWriter.class);

   private static final String DEFAULT_STRING = "";

   private final DataView dataView;
   private final TimelineViewDef timelineViewDef;
   private final boolean useSelectionOnly;

   public TimelineCsvWriter(String dvUuid, TimelineViewDef visualizationDef, boolean useSelectionOnly) {
      dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
      this.timelineViewDef = visualizationDef;
      this.useSelectionOnly = useSelectionOnly;
   }

   @Override
   public void writeCsv(File fileToWrite) {
      try (FileWriter fileWriter = new FileWriter(fileToWrite);
           CSVWriter csvWriter = new CSVWriter(fileWriter)) {
         csvWriter.writeNext(buildHeader());
         writeRows(csvWriter);
      } catch (Exception e) {
         LOG.error(e);
      }
   }

   private void writeRows(CSVWriter csvWriter) throws Exception {
      if (dataView == null) {
         throw new CentrifugeException(String.format("Dataview not found."));
      }
      DataCacheHelper cacheHelper = new DataCacheHelper();
      DetailedTimelineResult result = new DetailedTimelineResult();
      TimelineQueryBuilder queryBuilder = new TimelineQueryBuilder(dataView, timelineViewDef);

      try (Connection connection = CsiPersistenceManager.getCacheConnection();
           ResultSet rs = cacheHelper.getTimelineView(connection, queryBuilder)) {
         result = ChronosActionsService.processResult(rs, timelineViewDef, queryBuilder, dataView, result, 0, 0);
         writeEachRow(csvWriter, result);
      } finally {
         csvWriter.close();
      }
   }

   private void writeEachRow(CSVWriter csvWriter, DetailedTimelineResult result) {
      TimelineEventSelection selection = timelineViewDef.getSelection();
      String[] row = null;

      if (selection.getSelectedItems().isEmpty() || !shouldLimitResultsBySelection(selection)) {
         for (SingularTimelineEvent event : result.getEvents()) {
            row = createRow(event);

            csvWriter.writeNext(row);
         }
      } else {
         for (SingularTimelineEvent event : result.getEvents()) {
            if (selection.getSelectedItems().contains(event.getEventDefinitionId())) {
               row = createRow(event);

               csvWriter.writeNext(row);
            }
         }
      }
   }

   private String[] createRow(SingularTimelineEvent event) {
      String[] row = new String[5];
      String label = event.getLabel();

      if ((label == null) || label.equals("null")) {
         label = DEFAULT_STRING;
      }
      String startTime =
         (event.getStartTime() == null)
            ? DEFAULT_STRING
            : ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getStartTime().longValue()),
                                      ZoneId.systemDefault())
                 .format(DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
      String endTime =
         (event.getEndTime() == null)
            ? DEFAULT_STRING
            : ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getEndTime().longValue()),
                                      ZoneId.systemDefault())
                 .format(DateUtil.JAVA_UTIL_DATE_DATE_TIME_FORMATTER);
      String trackValue;

      if ((event.getTrackValue() == null) || event.getTrackValue().equals(TimelineTrack.NULL_TRACK)) {
         trackValue = DEFAULT_STRING;
      } else {
         trackValue = event.getTrackValue();
      }
      String sizeValue;

      if (event.getDotSize() == null) {
         sizeValue = DEFAULT_STRING;
      } else {
         sizeValue = DEFAULT_STRING + event.getDotSize();
      }
      row[0] = label;
      row[1] = startTime;
      row[2] = endTime;
      row[3] = trackValue;
      row[4] = sizeValue;
      return row;
   }

   private boolean shouldLimitResultsBySelection(Selection selection) {
      return useSelectionOnly && !selection.isCleared();
   }

   private static String[] buildHeader() {
      String[] headers = new String[5];
      headers[0] = "Label";
      headers[1] = "Start";
      headers[2] = "End";
      headers[3] = "Group";
      headers[4] = "Size";
      return headers;
   }
}

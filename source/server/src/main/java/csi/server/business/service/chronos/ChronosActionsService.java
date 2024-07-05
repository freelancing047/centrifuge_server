/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.server.business.service.chronos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.mongodb.DBObject;

import csi.config.Configuration;
import csi.config.TimelineConfig;
import csi.server.business.cachedb.querybuilder.TimelineQueryBuilder;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.service.FilterActionsService;
import csi.server.business.service.chronos.storage.AbstractTimelineStorageService;
import csi.server.business.service.chronos.storage.TimelineStorage;
import csi.server.business.service.chronos.storage.postgres.DataToTimelineResultTransformer;
import csi.server.business.service.chronos.storage.postgres.TimelineResultToDataTransformer;
import csi.server.business.visualization.timeline.EventIdComparator;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.TimelineMetricsType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineEventDefinition;
import csi.server.common.model.visualization.timeline.TimelineField;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineTimeSetting;
import csi.server.common.model.visualization.timeline.TimelineTrackState;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.common.service.api.ChronosActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;
import csi.server.util.sql.ScrollCallback;
import csi.server.util.sql.SelectSQL;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.map.MetricsDTO;
import csi.shared.core.visualization.timeline.BaseTimelineEvent;
import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.MeasuredTimelineResult;
import csi.shared.core.visualization.timeline.MeasuredTrack;
import csi.shared.core.visualization.timeline.MeasuredTrackItem;
import csi.shared.core.visualization.timeline.OverviewTrack;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;
import csi.shared.core.visualization.timeline.SummarizedTimelineEvent;
import csi.shared.core.visualization.timeline.SummaryTimelineResult;
import csi.shared.core.visualization.timeline.TimelineResult;
import csi.shared.core.visualization.timeline.TimelineTrack;
import csi.shared.core.visualization.timeline.Tooltip;
import csi.shared.gwt.viz.timeline.TimeUnit;
import csi.shared.gwt.viz.timeline.TimelineRequest;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChronosActionsService implements ChronosActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(ChronosActionsService.class);

    public static final String NO_VALUE = "No Value";
    private static List<TimeUnit> timeUnits = Arrays.asList(
            TimeUnit.YEAR,
            TimeUnit.MONTH, TimeUnit.DAY, TimeUnit.HOUR,
            TimeUnit.MINUTE, TimeUnit.SECOND, TimeUnit.QUARTER_MILLISECOND, TimeUnit.MILLISECOND);
    @Inject
    private FilterActionsService filterActionsService;
    @Inject
    private TimelineActionsServiceUtil timelineActionsServiceUtil;

    public static DetailedTimelineResult processResult(ResultSet rs, TimelineViewDef timelineViewDef, TimelineQueryBuilder queryBuilder, DataView dataView,
                                                       DetailedTimelineResult result, int eventId, int count) throws SQLException {
        List<String> names = new ArrayList<String>();
        List<FieldDef> fields;
        TimelineSettings settings = timelineViewDef.getTimelineSettings();
        List<SingularTimelineEvent> events = new ArrayList<SingularTimelineEvent>();

        double min = Double.MAX_VALUE;
        double max = 0;

        if ((queryBuilder.getSelectColumns() == null) || queryBuilder.getSelectColumns().isEmpty()) {
            fields = dataView.getMeta().getModelDef().getFieldDefs();
        } else {
            fields = new ArrayList<FieldDef>();
        }

        for (FieldDef field : fields) {
            names.add(field.getFieldName());
        }

        Map<TimelineEventDefinition, Boolean> useEndMap = new HashMap<TimelineEventDefinition, Boolean>();
        Map<TimelineEventDefinition, Boolean> postEndMap = new HashMap<TimelineEventDefinition, Boolean>();
        Map<TimelineEventDefinition, Boolean> postStartMap = new HashMap<TimelineEventDefinition, Boolean>();

        for (TimelineEventDefinition eventDef : settings.getEvents()) {
            TimelineTimeSetting startField = eventDef.getStartField();

            if ((startField != null) && (startField.getFieldDef() != null)) {
                if (!names.contains(startField.getFieldDef().getFieldName())) {
                    fields.add(startField.getFieldDef());
                    names.add(startField.getFieldDef().getFieldName());
                }
                postStartMap.put(eventDef, Boolean.valueOf(startField.getFieldDef().getValueType() == CsiDataType.Time));
                useEndMap.put(eventDef, Boolean.FALSE);
            } else {
                postStartMap.put(eventDef, Boolean.FALSE);
            }
            TimelineTimeSetting endField = eventDef.getEndField();

            if ((endField != null) && (endField.getFieldDef() != null)) {
                if (!names.contains(endField.getFieldDef().getFieldName())) {
                    fields.add(endField.getFieldDef());
                    names.add(endField.getFieldDef().getFieldName());
                }
                postEndMap.put(eventDef, Boolean.valueOf(endField.getFieldDef().getValueType() == CsiDataType.Time));
            } else {
               postEndMap.put(eventDef, Boolean.FALSE);
            }
            useEndMap.put(eventDef, Boolean.valueOf((startField == null) && (endField != null)));
        }

        FieldDef dotSizeField = settings.getDotSize();
        if (dotSizeField != null) {
            if (!names.contains(dotSizeField.getFieldName())) {
                fields.add(dotSizeField);
                names.add(dotSizeField.getFieldName());
            }
        }

        FieldDef groupBy = settings.getGroupByField();

        String groupByColumn = null;
        if (groupBy != null) {
            groupByColumn = CacheUtil.getColumnName(groupBy);
        }

        FieldDef colorBy = settings.getColorByField();
        String colorColumn = null;
        if (colorBy != null) {
            colorColumn = CacheUtil.getColumnName(colorBy);
        }
        Map<Object, Integer> colorObjectMap = new HashMap<Object, Integer>();

        int limit = Configuration.getInstance().getTimelineConfig().getEventLimit();

        while (rs.next()) {
            String internalId = "internal_id";
            int id = rs.getInt(internalId);

            if (result.isColorLimit() || result.isGroupLimit()) {
                break;
            }
            for (TimelineEventDefinition eventDef : settings.getEvents()) {
                SingularTimelineEvent event = new SingularTimelineEvent();
                event.setRowId(id);
                event.setEventDefinitionId(eventId);
                eventId++;

                Time startTime = null;
                Time endTime = null;
                for (FieldDef field : fields) {
                    String colname = CacheUtil.getColumnName(field);
                    if ((field.getValueType() == CsiDataType.Date) || (field.getValueType() == CsiDataType.DateTime)) {
                        Object unknownType = rs.getObject(colname);
                        Long time = null;

                        try {
                            if (field.getValueType() == CsiDataType.Date) {
                                time = CsiTypeUtil.coerceDate(unknownType).getTime();
                            } else if (field.getValueType() == CsiDataType.DateTime) {
                                time = CsiTypeUtil.coerceTimestamp(unknownType).getTime();
                            }
                        } catch (Exception exception) {
                            if (unknownType != null) {
                               LOG.debug("Can't handle object:" + unknownType.toString());
                            }
                        }

                        if (time == null) {
                            if (unknownType != null) {
                               LOG.debug("This object could not be coerced:" + unknownType.toString());
                            }
                        } else {
                            if ((eventDef.getStartField() != null) && field.getUuid().equals(eventDef.getStartField().getFieldDef().getUuid())) {
                                event.setStartTime(time);
                            } else if ((eventDef.getEndField() != null) && field.getUuid().equals(eventDef.getEndField().getFieldDef().getUuid())) {
                                event.setEndTime(time);
                            }
                        }
                    } else if (field.getValueType() == CsiDataType.Time) {
                        Object unknownType = rs.getObject(colname);
                        Time time = null;

                        try {
                            if (field.getValueType() == CsiDataType.Time) {
                                time = CsiTypeUtil.coerceTime(unknownType);
                            }
                        } catch (Exception exception) {
                            if (unknownType != null) {
                               LOG.error("Can't handle object:" + unknownType.toString());
                            }
                        }

                        if (postStartMap.get(eventDef) || postEndMap.get(eventDef)) {
                            if ((eventDef.getStartField() != null) && field.getUuid().equals(eventDef.getStartField().getFieldDef().getUuid())) {
                                startTime = time;
                            } else if ((eventDef.getEndField() != null) && field.getUuid().equals(eventDef.getEndField().getFieldDef().getUuid())) {
                                endTime = time;
                            }
                        }
                    } else if ((field.getValueType() == CsiDataType.Number) || (field.getValueType() == CsiDataType.Integer)) {
                        Object unknownType = rs.getObject(colname);
                        double size = 0;
                        try {
                            if ((settings.getDotSize() != null) && field.getUuid().equals(settings.getDotSize().getUuid())) {
                                if (field.getValueType() == CsiDataType.Number) {
                                    size = CsiTypeUtil.coerceDecimal(unknownType);
                                    event.setDotSize(size);
                                    min = Math.min(event.getDotSize(), min);
                                    max = Math.max(event.getDotSize(), max);
                                } else if (field.getValueType() == CsiDataType.Integer) {
                                    size = CsiTypeUtil.coerceLong(unknownType);
                                    event.setDotSize(size);
                                    min = Math.min(event.getDotSize(), min);
                                    max = Math.max(event.getDotSize(), max);
                                }
                            }
                        } catch (Exception exception) {
                            if (unknownType != null) {
                               LOG.error("Can't handle object:" + unknownType.toString());
                            }
                        }
                    }
                }

                if (eventDef.getLabelField() != null) {
                    Object value = null;
                    String colname = CacheUtil.getColumnName(eventDef.getLabelField());
                    value = rs.getObject(colname);

                    if (value != null) {
                        event.setLabel(value.toString());
                    }
                }

                long calcTime;

                if (postEndMap.get(eventDef).booleanValue()) {
                    if ((endTime != null) && (event.getStartTime() != null)) {
                        calcTime = event.getStartTime() + endTime.getTime();

                        event.setEndTime(calcTime);
                    }
                } else if (postStartMap.get(eventDef).booleanValue()) {
                    if ((startTime != null) && (event.getEndTime() != null)) {
                        calcTime = event.getEndTime() - startTime.getTime();

                        event.setStartTime(calcTime);
                    }
                }

                if (colorColumn != null) {
                    Object value = null;
                    value = rs.getObject(colorColumn);

                    if (value != null) {
                        String stringValue = value.toString().trim();
                        if (!colorObjectMap.containsKey(stringValue)) {
                            colorObjectMap.put(stringValue, count);
                            count++;
                            result.setColorLimit(colorObjectMap.size() > TimelineConfig.getLegendLimit());
                        }
                        event.setColorValue(stringValue);
                    }
                }
                if (groupByColumn != null) {
                    Object group = rs.getObject(groupByColumn);
                    if (group != null) {
                        String trimmedGroup = group.toString().trim();
                        event.setTrackValue(trimmedGroup);
                        TimelineTrack track = new TimelineTrack();
                        track.setName(trimmedGroup);
                        if ((colorColumn != null) && colorColumn.equals(groupByColumn)) {
                            Integer object = colorObjectMap.get(trimmedGroup);
                            if (object != null) {
                              track.setColor(object);
                           }
                        }
                        if (!result.getTracks().contains(track)) {
                           result.addTrack(track);
                        }
                    } else {
                        TimelineTrack track = new TimelineTrack();
                        track.setName("");
                        event.setTrackValue("");
                        if (!result.getTracks().contains(track)) {
                           result.addTrack(track);
                        }
                    }

                    result.setGroupLimit(result.getTracks().size() > TimelineConfig.getLegendLimit());
                }

                //flip if necessary
                if (useEndMap.get(eventDef).booleanValue() && (event.getEndTime() != null)) {
                    event.setStartTime(event.getEndTime());
                    event.setEndTime(null);
                }

                if (event.getStartTime() != null) {
                    events.add(event);

                    if (event.getColorValue() == null) {
                        event.setColorValue(TimelineTrack.NULL_TRACK);
                    }
                    if (result.getLowerTimeBound() > event.getStartTime()) {
                        result.setLowerTimeBound(event.getStartTime());
                    }
                    if (result.getUpperTimeBound() < event.getStartTime()) {
                        result.setUpperTimeBound(event.getStartTime());
                    }
                    if (event.getEndTime() != null) {
                        if (result.getLowerTimeBound() > event.getEndTime()) {
                            result.setLowerTimeBound(event.getEndTime());
                        }
                        if (result.getUpperTimeBound() < event.getEndTime()) {
                            result.setUpperTimeBound(event.getEndTime());
                        }

                        //Clean up invalid events
                        if (event.getStartTime() > event.getEndTime()) {
                            Long start = event.getStartTime();
                            event.setStartTime(event.getEndTime());
                            event.setEndTime(start);
                        }
                    }
                }

                int allEvents = events.size();

                if (result.getEvents() != null) {
                    allEvents += result.getEvents().size();
                }

                if (allEvents >= limit) {
                    result.setLimitReached(true);
                }
            }
        }

        if (min >= max) {
            min = max;
        }

        result.setMin(Math.min(result.getMin(), min));
        result.setMax(Math.max(result.getMax(), max));
        if (result.isColorLimit() || result.isGroupLimit()) {
            events.clear();
        } else {
            result.getEvents().addAll(events);
        }
        return result;
    }

    public static TimeUnit calculateRelevantTimeUnit(long start, long end, int viewportWidth) {
        List<TimeUnit> list = new ArrayList<TimeUnit>();
        int howMany = timeUnits.size();

        for (int ii = 0; ii < howMany; ii++) {
            TimeUnit unit = timeUnits.get(ii);
            long estimate = unit.approxNumInRange(start, end);

            if (estimate < (viewportWidth / TimeUnit.BAR_MIN_WIDTH)) {
                if (!list.isEmpty()) {
                    TimeUnit last = list.get(0);
                    if (unit == last) {
                        list.remove(0);
                    }
                }
                list.add(unit);

            }
        }
        while (list.size() > 2) {
            list.remove(0);
        }

        if (list.isEmpty()) { // uh oh! must be many years. we will add in bigger increments.
            long length = end - start;
            long size = 365 * 24 * 60 * 60 * 1000L;
            int m = 1;
            int maxTics = 15;

            while ((m < 2000000000) && ((length / (m * size)) > maxTics)) {
                if ((length / (2 * m * size)) <= maxTics) {
                    m *= 2;
                    break;
                }
                if ((length / (5 * m * size)) <= maxTics) {
                    m *= 5;
                    break;
                }
                m *= 10;
            }
            list.add(TimeUnit.multipleYears(m));
        }

        return list.get(list.size() - 1);
    }

//    public TimelineResult loadTimelineCache(String dataViewUuid, String timelineViewDefUuid) {
//        Connection connection = null;
//        TimelineResult result = null;
//
//        try {
//            connection = CsiPersistenceManager.getCacheConnection();
//
//            final DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
//            final TimelineViewDef timelineViewDef = CsiPersistenceManager.findObject(TimelineViewDef.class, timelineViewDefUuid);
//
//            if (dataView == null) {
//
//                throw new CentrifugeException(String.format("Dataview not found."));
//            }
//            final TimelineQueryBuilder queryBuilder =  timelineActionsServiceUtil.getTimelineQueryBuilder(dataView, timelineViewDef);
//            DataCacheHelper cacheHelper = new DataCacheHelper();
//            TimelineSettings settings = timelineViewDef.getTimelineSettings();
//            //Fixes old timeline viewdefs that are migrated
//            if(settings != null && settings.getEvents() == null || settings.getEvents().isEmpty()){
//                settings.setEvents(new ArrayList<TimelineEventDefinition>());
//                CsiPersistenceManager.merge(timelineViewDef);
//            }
//
//            int count = cacheHelper.getCountOfEvents(connection,queryBuilder);
//
//            if(count >= Configuration.instance().getTimelineConfig().getEventLimit() && settings.getGroupByField() == null){
//                result = processSummaryData(connection, queryBuilder, cacheHelper, timelineViewDef, dataView);
//                result.setLimitReached(true);
//            } else if(count >= Configuration.instance().getTimelineConfig().getEventLimit()){
//                //result = processMeasureData(connection, queryBuilder, cacheHelper, timelineViewDef, dataView, count, request);
//                result.setLimitReached(true);
//            } else {
//
//                final DetailedTimelineResult processedResult = new DetailedTimelineResult();
//                ScrollCallback<DetailedTimelineResult> detailedScrollCallback = createDetailedScrollCallback(dataView, timelineViewDef, queryBuilder, processedResult);
//                SelectSQL sql = cacheHelper.getTimelineDetailedData(connection, queryBuilder, detailedScrollCallback);
//
//                result = sql.scroll(detailedScrollCallback);
//
//                result.setLimitReached(false);
//            }
//        } catch (CentrifugeException e) {
//            logger.trace(e);
//        }
//
//        return result;
//    }

    public FilterActionsService getFilterActionsService() {
        return filterActionsService;
    }

    public void setFilterActionsService(FilterActionsService filterActionsService) {
        this.filterActionsService = filterActionsService;
    }

//    public SummaryTimelineResult processSummaryData(Connection connection, final TimelineQueryBuilder queryBuilder,
//                                                    DataCacheHelper cacheHelper, TimelineViewDef timelineViewDef, DataView dataView, TimelineRequest request) throws CentrifugeException {
//        SelectSQL sql = cacheHelper.getTimelineBundledDataQuery(connection, queryBuilder);
//
//        Set<FieldDef> fields = new HashSet(queryBuilder.getSelectedColumns());
//        TimelineSettings settings = timelineViewDef.getTimelineSettings();
//        List<SummarizedTimelineEvent> events = new ArrayList<SummarizedTimelineEvent>();
//
//        Map<TimelineEventDefinition, Boolean> useEndAsStartMap = new HashMap<TimelineEventDefinition, Boolean>();
//        Map<TimelineEventDefinition, Boolean> endCalculationMap = new HashMap<TimelineEventDefinition, Boolean>();
//        Map<TimelineEventDefinition, Boolean> startCalculationMap = new HashMap<TimelineEventDefinition, Boolean>();
//
//        populateCalculationMaps(settings, useEndAsStartMap, endCalculationMap, startCalculationMap);
//
//        int limit = Configuration.instance().getTimelineConfig().getEventLimit();
//
//        HashMap<FieldDef, String> fieldToColumnNames = new HashMap<FieldDef, String>();
//        for (FieldDef field : fields) {
//            fieldToColumnNames.put(field, CacheUtil.getColumnName(field));
//        }
//
//        ScrollCallback<List<SummarizedTimelineEvent>> callback = new ScrollCallback<List<SummarizedTimelineEvent>>() {
//
//
//            @Override
//            public List<SummarizedTimelineEvent> scroll(ResultSet resultSet) throws SQLException {
//                List<SummarizedTimelineEvent> events = new ArrayList<SummarizedTimelineEvent>();
//
//                while (resultSet.next()) {
//                    createEventsFromResultSet(settings, useEndAsStartMap, endCalculationMap, startCalculationMap,
//                            fieldToColumnNames, resultSet, events);
//
//                }
//                return events;
//            }
//
//
//        };
//
//        events.addAll(sql.scroll(callback));
//
//        events = summarizeResults(events, limit, request.getSummaryLevel());
//
//        SummaryTimelineResult result = new SummaryTimelineResult();
//        result.setEvents(events);
//
//        return result;
//    }

    private MeasuredTimelineResult processMeasureData(Connection connection, TimelineQueryBuilder queryBuilder,
                                                      DataCacheHelper cacheHelper, TimelineViewDef timelineViewDef, DataView dataView, TimelineRequest request, List<Object> groups, DetailedTimelineResult fullResult) throws CentrifugeException {
        Set<FieldDef> fields = new HashSet(queryBuilder.getSelectedColumns());
        TimelineSettings settings = timelineViewDef.getTimelineSettings();
//        List<SummarizedTimelineEvent> events = new ArrayList<SummarizedTimelineEvent>();

        Map<TimelineEventDefinition, Boolean> useEndAsStartMap = new HashMap<TimelineEventDefinition, Boolean>();
        Map<TimelineEventDefinition, Boolean> endCalculationMap = new HashMap<TimelineEventDefinition, Boolean>();
        Map<TimelineEventDefinition, Boolean> startCalculationMap = new HashMap<TimelineEventDefinition, Boolean>();

        populateCalculationMaps(settings, useEndAsStartMap, endCalculationMap, startCalculationMap);

        int endIndex = (request.getStartGroupIndex() + request.getGroupLimit()) - 1;
        if (endIndex < 2) {
            endIndex = 10;
        }
        if (groups.size() < endIndex) {
            endIndex = groups.size() - 1;
        }
        if (request.getStartGroupIndex() < 0) {
            throw new CentrifugeException("Group Index request exceeded bounds:" + request.getStartGroupIndex() + "-" + endIndex);
        }
        HashMap<FieldDef, String> fieldToColumnNames = new HashMap<FieldDef, String>();
        for (FieldDef field : fields) {
            fieldToColumnNames.put((field), CacheUtil.getColumnName(field));
        }

        if (groups.size() < endIndex) {
            endIndex = groups.size() - 1;
        }

        List<Object> groupsToQuery = groups.subList(request.getStartGroupIndex(), endIndex);

        //Null group couldn't be added to list, gotta flag null to be queried or not
        if (groupsToQuery.size() < (endIndex - request.getStartGroupIndex())) {

        }

//        SelectSQL sql = queryBuilder.createScrollingGroupQuery(null, false, groupsToQuery);
//
//        //Create Events
//        events.addAll(sql.scroll(new ScrollCallback<List<SummarizedTimelineEvent>>(){
//
//            @Override
//            public List<SummarizedTimelineEvent> scroll(ResultSet resultSet) throws SQLException {
//
//                while(resultSet.next()){
//
//                    createEventsFromResultSet(settings, useEndAsStartMap, endCalculationMap, startCalculationMap,
//                            fieldToColumnNames, resultSet, events);
//
//                }
//
//                return events;
//            }}));
//
//        TimelineResult detailedResult = new SummaryTimelineResult();
//        detailedResult.setEvents(events);
//
        MeasuredTimelineResult result = createMeasuresFromDetails(fullResult, request, groups);

        return result;
    }

    public ScrollCallback<DetailedTimelineResult> createDetailedScrollCallback(final DataView dataView,
                                                                               final TimelineViewDef timelineViewDef, final TimelineQueryBuilder queryBuilder,
                                                                               final DetailedTimelineResult processedResult) {
        return new ScrollCallback<DetailedTimelineResult>() {
            int eventId = 1;
            int count = 0;

            @Override
            public DetailedTimelineResult scroll(ResultSet resultSet) throws SQLException {
                processResult(resultSet, timelineViewDef, queryBuilder, dataView, processedResult, eventId, count);
                return processedResult;
            }
        };
    }

    public List<SummarizedTimelineEvent> summarizeResults(List<? extends BaseTimelineEvent> allEvents, int limit, TimeUnit currentSummaryLevel) {
        TimeUnit unit = TimeUnit.SECOND;

        if (currentSummaryLevel != null) {
            unit = currentSummaryLevel;
        }

        int count = 0;

        List<SummarizedTimelineEvent> events = null;
        while (allEvents.size() > limit) {
            events = TimelineEventUtils.summarize(allEvents, unit, limit);
            count++;
            if (events.size() <= limit) {
                break;
            }
            unit = TimeUnit.next(unit);
        }

        //If we make a guess that immediately summarizes, we check one back in some cases to make sure we are giving the best summary
        if ((count == 1) && !unit.equals(TimeUnit.SECOND) && (currentSummaryLevel != null)) {
//            TimeUnit moreDetailedUnit = TimeUnit.last(unit);
            List<SummarizedTimelineEvent> moreDetailedEvents = TimelineEventUtils.summarize(allEvents, unit, limit);

            if (moreDetailedEvents.size() <= limit) {
//                unit = moreDetailedUnit;
                events = moreDetailedEvents;
            }
        }

//        ArrayList<SummarizedTimelineEvent> summaryEvents = new ArrayList<SummarizedTimelineEvent>();
//        for(BaseTimelineEvent event: events){
//            SummarizedTimelineEvent summaryEvent = new SummarizedTimelineEvent();
//            summaryEvent.setColorValue(event.getColorValue());
//            summaryEvent.setDotSize(event.getDotSize());
//            summaryEvent.setEndTime(event.getEndTime());
//            summaryEvent.setStartTime(event.getStartTime());
//            summaryEvent.setTrackValue(event.getTrackValue());
//            //summaryEvent.set
//            summaryEvents.add(summaryEvent);
//        }

        return events;
    }

   private void createEventsFromResultSet(TimelineSettings settings,
                                          Map<TimelineEventDefinition,Boolean> useEndAsStartMap,
                                          Map<TimelineEventDefinition,Boolean> endCalculationMap,
                                          Map<TimelineEventDefinition,Boolean> startCalculationMap,
                                          Map<FieldDef,String> fieldToColumnNames, ResultSet resultSet,
                                          List<SummarizedTimelineEvent> events) throws SQLException {
      List<SummarizedTimelineEvent> eventsInRow = new ArrayList<SummarizedTimelineEvent>();

      for (TimelineEventDefinition eventDefinition : settings.getEvents()) {
         eventsInRow.add(new SummarizedTimelineEvent());
      }
      for (Map.Entry<FieldDef,String> entry: fieldToColumnNames.entrySet()) {
         FieldDef fieldDef = entry.getKey();
         Object value = resultSet.getObject(entry.getValue());

         switch (fieldDef.getValueType()) {
            case Date:
               TimelineEventUtils.populateEventsWithDate(value, fieldDef, eventsInRow, settings);
               break;
            case DateTime:
               TimelineEventUtils.populateEventsWithDate(value, fieldDef, eventsInRow, settings);
               break;
            case Time:
               TimelineEventUtils.populateEventsWithTime(value, fieldDef, eventsInRow, settings);
               break;
            case String:
               TimelineEventUtils.populateEventsWithString(value, fieldDef, eventsInRow, settings);
               break;
            case Number:
               TimelineEventUtils.populateEventsWithNumber(value, fieldDef, eventsInRow, settings);
               break;
            case Integer:
               TimelineEventUtils.populateEventsWithInteger(value, fieldDef, eventsInRow, settings);
               break;
            case Boolean:
            case Unsupported:
            default:
               LOG.error("Unknown type can't be coerced:" + value.toString());
               break;
         }
      }
      TimelineEventUtils.fixAndValidateEvents(eventsInRow, settings, useEndAsStartMap, startCalculationMap,
                                              endCalculationMap);
      events.addAll(eventsInRow);
   }

   private void populateCalculationMaps(TimelineSettings settings,
                                        Map<TimelineEventDefinition, Boolean> useEndAsStartMap,
                                        Map<TimelineEventDefinition, Boolean> endCalculationMap,
                                        Map<TimelineEventDefinition, Boolean> startCalculationMap) {
      for (TimelineEventDefinition eventDef : settings.getEvents()) {
         TimelineTimeSetting startField = eventDef.getStartField();

         if ((startField != null) && (startField.getFieldDef() != null)) {
            startCalculationMap.put(eventDef, Boolean.valueOf(startField.getFieldDef().getValueType() == CsiDataType.Time));
            useEndAsStartMap.put(eventDef, Boolean.FALSE);
         } else {
            startCalculationMap.put(eventDef, Boolean.FALSE);
         }
         TimelineTimeSetting endField = eventDef.getEndField();

         if ((endField != null) && (endField.getFieldDef() != null)) {
            endCalculationMap.put(eventDef, Boolean.valueOf(endField.getFieldDef().getValueType() == CsiDataType.Time));
         } else {
            endCalculationMap.put(eventDef, Boolean.FALSE);
         }
         useEndAsStartMap.put(eventDef, Boolean.valueOf((startField == null) && (endField != null)));
      }
   }

   @Override
   public Tooltip createTooltip(String dataviewUuid, String vizUuid, SingularTimelineEvent timelineEvent) throws CentrifugeException {
      TimelineViewDef timelineViewDef = CsiPersistenceManager.findObject(TimelineViewDef.class, vizUuid);
      DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataviewUuid);

      if (dataView == null) {
         throw new CentrifugeException(String.format("Dataview not found."));
      }
      Tooltip tooltip = new Tooltip();
      DataCacheHelper cacheHelper = new DataCacheHelper();

      try (Connection connection = CsiPersistenceManager.getCacheConnection();
           ResultSet rs = cacheHelper.getSingleRow(connection, timelineEvent.getRowId(), dataView.getUuid())) {
         if (rs != null) {
            while (rs.next()) {
               TimelineSettings settings = timelineViewDef.getTimelineSettings();
               List<TimelineField> fields = settings.getFieldList();

               for (TimelineField field : fields) {
                  Object object = rs.getObject(CacheUtil.getColumnName(field.getFieldDef()));

                  if (object == null) {
                     tooltip.addField(field.getFieldDef().getFieldName(), "");
                  } else {
                     tooltip.addField(field.getFieldDef().getFieldName(), object.toString());
                  }
               }
            }
         }
      } catch (SQLException e) {
         throw new CentrifugeException(e);
      }
      return tooltip;
   }

    @Override
    public TimelineResult loadTimeline(TimelineRequest request) throws CentrifugeException {
        Connection connection = null;
        TimelineResult result = null;

        int eventLimit = Configuration.getInstance().getTimelineConfig().getEventLimit();
        try {
            connection = CsiPersistenceManager.getCacheConnection();

            final DataView dataView = CsiPersistenceManager.findObject(DataView.class, request.getDvUuid());
            final TimelineViewDef timelineViewDef = CsiPersistenceManager.findObject(TimelineViewDef.class, request.getVizUuid());

            if (dataView == null) {
                throw new CentrifugeException(String.format("Dataview not found."));
            }
            final TimelineQueryBuilder queryBuilder = timelineActionsServiceUtil.getTimelineQueryBuilder(dataView, timelineViewDef);
            DataCacheHelper cacheHelper = new DataCacheHelper();
            TimelineSettings settings = timelineViewDef.getTimelineSettings();
            //Fixes old timeline viewdefs that are migrated
            if (((settings != null) && (settings.getEvents() == null)) || settings.getEvents().isEmpty()) {
                settings.setEvents(new ArrayList<TimelineEventDefinition>());
                CsiPersistenceManager.merge(timelineViewDef);
            }

            //int count = cacheHelper.getCountOfEvents(connection,queryBuilder);
            AbstractTimelineStorageService storageService = AbstractTimelineStorageService.instance();

            DetailedTimelineResult fullResult = new DetailedTimelineResult();
            if (storageService.hasVisualizationData(request.getVizUuid())) {
                TimelineStorage storage = storageService.getTimelineStorage(request.getVizUuid());
                DataToTimelineResultTransformer transformer = new DataToTimelineResultTransformer();
                fullResult = transformer.apply(storage.getResult());
                result = fullResult;
            } else {
                ScrollCallback<DetailedTimelineResult> detailedScrollCallback = createDetailedScrollCallback(dataView, timelineViewDef, queryBuilder, fullResult);
                SelectSQL sql = cacheHelper.getTimelineDetailedData(connection, queryBuilder, detailedScrollCallback);

                result = sql.scroll(detailedScrollCallback);
                if (request.isCalculateOverview() && (fullResult.getOverviewData() == null)) {
                    OverviewTrack overview = createOverview(fullResult, request.getVizWidth());
                    fullResult.setOverviewData(overview);
                    fullResult.setTotalEvents(fullResult.getEvents().size());
                }

                cacheResult(request.getVizUuid(), (DetailedTimelineResult) result);
            }
            OverviewTrack overview = null;
            if (request.isCalculateOverview() && (request.getTrackName() == null)) {
                overview = result.getOverviewData();
            }

            int count = fullResult.getEvents().size();

            String trackName = request.getTrackName();

            if ((count >= eventLimit) && ((settings.getGroupByField() == null) || (trackName != null))) {
                Long endTime = request.getEndTime();
                Long startTime = request.getStartTime();

                List<SingularTimelineEvent> events = null;
                if ((startTime != null) && (endTime != null)) {
                    events = pairDownByTime(fullResult.getEvents(), startTime, endTime);
                } else {
                    events = fullResult.getEvents();
                }

                if (trackName != null) {
                    events = pairDownByTrackName(events, trackName);
                    if (request.isCalculateOverview() && (events.size() > eventLimit)) {
                        overview = createTrackNameOverview(events, request.getVizWidth());
                    }
                }

                selectEvents(events, request.getEventIdSelection());

                // this will scope the legend to only items within this group.
                if (events.size() <= eventLimit) {
                    result = new SummaryTimelineResult();
                    ((SummaryTimelineResult) result).setSingularEvents(events);
                    Set<String> legendInfo;
                    legendInfo = generateLegendInfo(events);
                    result.setLimitReached(false);
                    ((SummaryTimelineResult) result).setLegendInfo(legendInfo);
                } else {
                    List<SummarizedTimelineEvent> summaryEvents = summarizeResults(events, eventLimit, request.getSummaryLevel());
                    result = new SummaryTimelineResult();
                    result.setLimitReached(true);
                    result.setEvents(summaryEvents);
                    Set<String> legendInfo;
                    legendInfo = generateLegendInfo(events);
                    ((SummaryTimelineResult) result).setLegendInfo(legendInfo);
                }
            } else if (count >= eventLimit) {
                List<Object> groups = cacheHelper.getDistinctGroups(connection, queryBuilder);

                result = processMeasureData(connection, queryBuilder, cacheHelper, timelineViewDef, dataView, request, groups, fullResult);
                result.setLimitReached(true);
            } else {
                result.setLimitReached(false);
            }

            result.setTotalEvents(count);
            result.setOverviewData(overview);
            result.setTrackName(trackName);
        } catch (CentrifugeException e) {
           LOG.error(e);
        }

        return result;
    }

    private HashSet<String> generateLegendInfo(List<SingularTimelineEvent> events) {
        HashSet<String> legendInfo = Sets.newHashSet();
        for (SingularTimelineEvent event : events) {
            legendInfo.add(event.getColorValue());
        }
        return legendInfo;
    }

    private OverviewTrack createTrackNameOverview(List<SingularTimelineEvent> events, int viewportWidth) {
        long upperBound = Long.MIN_VALUE;
        long lowerBound = Long.MAX_VALUE;
        for (SingularTimelineEvent event : events) {
            Long startTime = event.getStartTime();
            if (startTime != null) {
                //events.add(event);

                if (lowerBound > startTime) {
                    lowerBound = startTime;
                }
                if (upperBound < startTime) {
                    upperBound = startTime;
                }
                Long endTime = event.getEndTime();
                if (endTime != null) {
                    if (lowerBound > endTime) {
                        lowerBound = endTime;
                    }
                    if (upperBound < endTime) {
                        upperBound = endTime;
                    }
                }
            }
        }
        long minTime = lowerBound;
        long maxTime = upperBound;
        TimeUnit unit = calculateRelevantTimeUnit(minTime, maxTime, viewportWidth);
        double padTime = TimeUnit.calculateTimePadding(maxTime - minTime);
        minTime = (long) (minTime - padTime);
        maxTime = (long) (maxTime + padTime);

        long unitMinTime = unit.roundDown(minTime);
        long unitMaxTime = unit.roundUp(maxTime);

        List<Long> totalTics = new ArrayList<Long>();
        Long time = unitMinTime;
        totalTics.add(time);
        int count = 0;
        do {
            time = unit.addTo(time);
            totalTics.add(time);
            count++;
        } while ((time < unitMaxTime) && (count < 90000));

        int initialCapacity = totalTics.size();

        List<Integer> overviewNumbers = new ArrayList<Integer>(initialCapacity);
        for (int ii = 0; ii < initialCapacity; ii++) {
            overviewNumbers.add(0);
        }

        Long start = null;
        Long end = null;
        long duration = maxTime - minTime;
        for (SingularTimelineEvent event : events) {
            start = event.getStartTime();
            end = event.getEndTime();

            addMeasureToGroup(minTime, maxTime, unit, overviewNumbers, start, end, duration);
        }

        OverviewTrack overview = new OverviewTrack();
        overview.setMeasures(overviewNumbers);
        overview.setTimeUnit(unit);
        overview.setOverviewEnd(upperBound);
        overview.setOverviewStart(lowerBound);

        return overview;
    }

    private void selectEvents(List<SingularTimelineEvent> events, IntCollection eventIdSelection) {
       eventIdSelection.sort();

       Collections.sort(events, new EventIdComparator());

       int howManyEvents = events.size();
       int howManyEventIdSelection = eventIdSelection.size();
       int jj = 0;

       for (int ii = 0; ii < howManyEventIdSelection; ii++) {
          int selectedItemId = eventIdSelection.get(ii);

          for (; jj < howManyEvents; jj++) {
             SingularTimelineEvent singularTimelineEvent = events.get(jj);
             int eventDefinitionId = singularTimelineEvent.getEventDefinitionId();

             if (selectedItemId == eventDefinitionId) {
                singularTimelineEvent.setSelected(true);
             } else if (selectedItemId > eventDefinitionId) {
                continue;
             } else {
                break;
             }
          }
       }
    }

    private void calculateBounds(DetailedTimelineResult fullResult) {
        long lowerTimeBound = Long.MAX_VALUE;
        long upperTimeBound = Long.MIN_VALUE;

        for (SingularTimelineEvent event : fullResult.getEvents()) {
            Long startTime = event.getStartTime();
            if (startTime != null) {
                //events.add(event);

                if (lowerTimeBound > startTime) {
                    lowerTimeBound = startTime;
                }
                if (upperTimeBound < startTime) {
                    upperTimeBound = startTime;
                }
                Long endTime = event.getEndTime();
                if (endTime != null) {
                    if (lowerTimeBound > endTime) {
                        lowerTimeBound = endTime;
                    }
                    if (upperTimeBound < endTime) {
                        upperTimeBound = endTime;
                    }
                }
            }
        }

        fullResult.setUpperTimeBound(upperTimeBound);
        fullResult.setLowerTimeBound(lowerTimeBound);
    }

    private List<SingularTimelineEvent> pairDownByTrackName(List<SingularTimelineEvent> events, String trackName) {
        List<SingularTimelineEvent> filteredEvents = new ArrayList<SingularTimelineEvent>();
        for (SingularTimelineEvent event : events) {
            if ((event.getTrackValue() == null) || !event.getTrackValue().equals(trackName)) {
                continue;
            }
            filteredEvents.add(event);

        }
        return filteredEvents;
    }

    private List<SingularTimelineEvent> pairDownByTime(List<SingularTimelineEvent> events, Long startTime, Long endTime) {
        List<SingularTimelineEvent> filteredEvents = new ArrayList<SingularTimelineEvent>();
        for (SingularTimelineEvent event : events) {
            Long eventStartTime = event.getStartTime();
            if ((eventStartTime > startTime) && (eventStartTime < endTime)) {
                filteredEvents.add(event);
                continue;
            }
            Long eventEndTime = event.getEndTime();
            if (eventEndTime != null) {
                if ((eventEndTime > startTime) && (eventEndTime < endTime)) {
                    filteredEvents.add(event);
                    continue;
                }
                if ((eventStartTime < startTime) && (eventEndTime > endTime)) {
                    filteredEvents.add(event);
                    continue;
                }
            }
        }
        return filteredEvents;
    }

    private MeasuredTimelineResult createMeasuresFromDetails(TimelineResult result, TimelineRequest request, List<Object> groups) throws CentrifugeException {
        MeasuredTimelineResult measuredTimelineResult = new MeasuredTimelineResult();
        long lowerTimeBound;
        long upperTimeBound;

         if ((request.getStartTime() != null) && (request.getEndTime() != null)) {
            lowerTimeBound = request.getStartTime();
            upperTimeBound = request.getEndTime();
        } else {
            lowerTimeBound = result.getLowerTimeBound();
            upperTimeBound = result.getUpperTimeBound();
            for (Object object : result.getEvents()) {
                BaseTimelineEvent event = (BaseTimelineEvent) object;
                Long startTime = event.getStartTime();
                if (startTime != null) {
                    //events.add(event);

                    if (lowerTimeBound > startTime) {
                        lowerTimeBound = startTime;
                    }
                    if (upperTimeBound < startTime) {
                        upperTimeBound = startTime;
                    }
                    Long endTime = event.getEndTime();
                    if (endTime != null) {
                        if (lowerTimeBound > endTime) {
                            lowerTimeBound = endTime;
                        }
                        if (upperTimeBound < endTime) {
                            upperTimeBound = endTime;
                        }
                    }
                }
            }
            double padTime = TimeUnit.calculateTimePadding(upperTimeBound - lowerTimeBound);
            lowerTimeBound = (long) (lowerTimeBound - padTime);
            upperTimeBound = (long) (upperTimeBound + padTime);
        }

        TimeUnit unit = calculateRelevantTimeUnit(lowerTimeBound, upperTimeBound, request.getVizWidth());

        long unitMinTime = unit.roundDown(lowerTimeBound);
        long unitMaxTime = unit.roundUp(upperTimeBound);

        List<Long> totalTics = new ArrayList<Long>();
        Long time = unitMinTime;
        totalTics.add(time);
        int count = 0;
        do {
            time = unit.addTo(time);
            totalTics.add(time);
            count++;
        } while ((time < unitMaxTime) && (count < 90000));

        int initialCapacity = totalTics.size();
        List<MeasuredTrackItem> nullGroup = new ArrayList<MeasuredTrackItem>(initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            //FIXME: would like to use null, but need to add null guards everywhere...
            MeasuredTrackItem trackItem = new MeasuredTrackItem();
            nullGroup.add(i, trackItem);
        }

        int endIndex = (request.getStartGroupIndex() + request.getGroupLimit()) - 1;
        if (request.getStartGroupIndex() < 0) {
            throw new CentrifugeException("Group Index request exceeded bounds:" + request.getStartGroupIndex() + "-" + endIndex);
        }
//
        List<String> groupNames = new ArrayList<String>();
        List<List<MeasuredTrackItem>> measures = new ArrayList<List<MeasuredTrackItem>>();

        for (Object groupObject : groups) {
            if (groupObject == null) {
                groupNames.add("");
            } else {
                String group = groupObject.toString();
                groupNames.add(group);
            }
            List<MeasuredTrackItem> track = new ArrayList<MeasuredTrackItem>(initialCapacity);
            for (int i = 0; i < initialCapacity; i++) {
                MeasuredTrackItem trackItem = new MeasuredTrackItem();
                track.add(i, trackItem);
            }
            measures.add(track);
        }

        Map<String, Integer> groupToRow = new HashMap<String, Integer>();
        for (int ii = 0; ii < groupNames.size(); ii++) {
            groupToRow.put(groupNames.get(ii), ii);
        }

        Long start = null;
        Long end = null;

        List<BaseTimelineEvent> events = result.getEvents();

        long duration = upperTimeBound - lowerTimeBound;

        Collection eventIdSelection = Sets.newHashSet(request.getEventIdSelection());
        for (BaseTimelineEvent event : events) {
            String group = event.getTrackValue();

            start = event.getStartTime();
            end = event.getEndTime();

            if (((event.getStartTime() != null) && ((event.getStartTime() >= lowerTimeBound) && (event.getStartTime() <= upperTimeBound))) ||
                    ((event.getEndTime() != null) && (((event.getEndTime() >= lowerTimeBound) && (event.getEndTime() <= upperTimeBound)) ||
                            ((event.getStartTime() != null) && (event.getEndTime() > upperTimeBound) && (event.getStartTime() < lowerTimeBound))))) {
                if (!groupToRow.containsKey(group)) {
                    continue;
                }

                List<MeasuredTrackItem> groupMeasure = measures.get(groupToRow.get(group));
                boolean selected = eventIdSelection.contains(event.getEventDefinitionId());
                if (((group == null) || (groupMeasure == null)) && (request.getStartGroupIndex() == 0)) {
                    addMeasureToGroup2(lowerTimeBound, upperTimeBound, unit, nullGroup, start, end, duration, selected);
                } else {
                    addMeasureToGroup2(lowerTimeBound, upperTimeBound, unit, groupMeasure, start, end, duration, selected);
                }
            }
        }

        List<MeasuredTrack> tracks = new ArrayList<MeasuredTrack>();

        for (int ii = 0; ii < groupNames.size(); ii++) {
            MeasuredTrack track = new MeasuredTrack();
            track.setMeasures(measures.get(ii));
            track.setName(groupNames.get(ii));
            if (groupNames.get(ii).isEmpty()) {
                String noValue = getNoValue();
                track.setNameOverride(noValue);
            }
            track.setStartTime(lowerTimeBound);
            tracks.add(track);
            track.setTimeUnit(unit);
        }

        for (MeasuredTrack track : tracks) {
            if (request.getFilteredKeys().contains(track.getName())) {
                track.setVisible(false);
            }
        }

        measuredTimelineResult.setGroupCount(groups.size());
        measuredTimelineResult.setStartGroup(request.getStartGroupIndex());
        measuredTimelineResult.setMeasuredTracks(tracks);
        return measuredTimelineResult;
    }

    private String getNoValue() {
//        HttpServletRequest curRequest = TaskController.getInstance().getCurrentContext().get((ServletRequestAttributes).currentRequestAttributes())
//                        .getRequest();
//        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(curRequest.getServletContext());
//        InternationalizationService i18n = (InternationalizationService) context.getBean("csi.server.business.service.InternationalizationService");
//
//        Map<String, String> properties = i18n.getProperties(curRequest.getLocale());

        return "No Value";
    }

    public void addMeasureToGroup(long minTime, long maxTime, TimeUnit unit, List<Integer> measures, Long start, Long end, Long duration) {
        int position = 0;
        if (start != null) {
            //We don't do measures outside of our range

            if (start < minTime) {
                start = minTime;
            }

            long normalizedStart = (unit.roundDown(start));

            long cursor = minTime;

            int x = 0;
            while (cursor < normalizedStart) {
                cursor = unit.addTo(cursor);
                x++;
            }

            position = x;
            measures.set(position, measures.get(position) + 1);
            cursor = unit.roundDown(cursor);

            if (end != null) {
                if (end > maxTime) {
                    end = maxTime;
                }

                //This prevents incrementing same measure if start and end are in the same bucket
                while (position < (measures.size() - 1)) {
                    cursor = unit.addTo(cursor);
                    position++;
                    if (position > measures.size()) {
                        break;
                    }
                    if (cursor < end) {
                        measures.set(position, measures.get(position) + 1);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void addMeasureToGroup2(long minTime, long maxTime, TimeUnit unit, List<MeasuredTrackItem> measures, Long start, Long end, Long duration, boolean select) {
        int position = 0;
        if (start != null) {
            //We don't do measures outside of our range

            if (start < minTime) {
                start = minTime;
            }

            long normalizedStart = (unit.roundDown(start));

            long cursor = minTime;

            int x = 0;
            while (cursor < normalizedStart) {
                cursor = unit.addTo(cursor);
                x++;
            }

            position = x;
            MeasuredTrackItem trackItem = measures.get(position);
            if (trackItem == null) {
                trackItem = new MeasuredTrackItem();
                measures.add(position, trackItem);
            }
            if (!trackItem.isSelected()) {
                trackItem.setSelected(select);
            }
            trackItem.setValue(trackItem.getValue() + 1);
            cursor = unit.roundDown(cursor);

            if (end != null) {
                if (end > maxTime) {
                    end = maxTime;
                }

                //This prevents incrementing same measure if start and end are in the same bucket
                while (position < (measures.size() - 1)) {
                    cursor = unit.addTo(cursor);
                    position++;
                    if (position > measures.size()) {
                        break;
                    }
                    if (cursor < end) {
                        trackItem = measures.get(position);
                        if (!trackItem.isSelected()) {
                            trackItem.setSelected(select);
                        }
                        trackItem.setValue(trackItem.getValue() + 1);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void cacheResult(String vizuuid, DetailedTimelineResult result) {
        AbstractTimelineStorageService storageService = AbstractTimelineStorageService.instance();
        TimelineResultToDataTransformer transformer = new TimelineResultToDataTransformer();
        DBObject object = transformer.apply(result);
        TimelineStorage storage = storageService.createEmptyStorage(vizuuid);
        storage.addResult(object);
        storageService.save(vizuuid, storage);
    }

    @Override
    public TimelineEventSelection doServerTrackSelection(String uuid, TimelineEventSelection currentSelection, boolean doSelect, String trackName) {
        Object object = getCache(uuid);
        if (object == null) {
            return currentSelection;
        }
        List<Integer> matches = new ArrayList<Integer>();
        DetailedTimelineResult result = (DetailedTimelineResult) object;
        for (SingularTimelineEvent event : result.getEvents()) {
            if (trackName.equals(event.getTrackValue())) {
                matches.add(event.getEventDefinitionId());
            }
        }

        if (doSelect) {
            currentSelection.getSelectedItems().addAll(matches);
        } else {
            currentSelection.getSelectedItems().removeAll(matches);
        }

        return currentSelection;
    }

    @Override
    public TimelineEventSelection doServerSelection(String uuid, String color, TimelineEventSelection currentSelection, boolean doSelect, ArrayList<String> trackNames) {

        Object object = getCache(uuid);
        if (object == null) {
            return currentSelection;
        }

        List<Integer> matches = new ArrayList<Integer>();
        DetailedTimelineResult result = (DetailedTimelineResult) object;

        if (trackNames != null) {
            List<String> allTracks = new ArrayList<String>();
            List<String> excludeTracks = new ArrayList<String>();
            for (TimelineTrack timelineTrack : result.getTracks()) {
                allTracks.add(timelineTrack.getName());
            }
            excludeTracks.addAll(allTracks);
            for (String trackName : trackNames) {
                excludeTracks.remove(trackName);
            }

            for (String trackName : excludeTracks) {
                result.getEvents().removeIf(new Predicate<SingularTimelineEvent>() {
                    @Override
                    public boolean test(SingularTimelineEvent singularTimelineEvent) {
                        return trackName.equals(singularTimelineEvent.getTrackValue());
                    }
                });
            }
        }
        for (SingularTimelineEvent event : result.getEvents()) {
            if (color.equals(event.getColorValue())) {
                matches.add(event.getEventDefinitionId());
            }
        }

        if (doSelect) {
            currentSelection.getSelectedItems().addAll(matches);
        } else {
            currentSelection.getSelectedItems().removeAll(matches);
        }

        return currentSelection;

    }

    @Override
    public IntCollection getItems(String dataviewUuid, String vizUuid, long from, long to, ArrayList<String> trackNames) {
        Object object = getCache(vizUuid);
        if (object == null) {
            return new IntCollection();
        }
        // due to long cast on convert from pixels to ms
        to++;
        List<Integer> matches = new ArrayList<Integer>();
        DetailedTimelineResult result = (DetailedTimelineResult) object;
        if (trackNames != null) {
            List<String> allTracks = new ArrayList<String>();
            List<String> excludeTracks = new ArrayList<String>();
            for (TimelineTrack timelineTrack : result.getTracks()) {
                allTracks.add(timelineTrack.getName());
            }
            excludeTracks.addAll(allTracks);
            for (String trackName : trackNames) {
                excludeTracks.remove(trackName);
            }

            for (String trackName : excludeTracks) {
                result.getEvents().removeIf(singularTimelineEvent -> trackName.equals(singularTimelineEvent.getTrackValue()));
            }
        }
        for (SingularTimelineEvent event : result.getEvents()) {
            if ((event.getStartTime() != null) && (event.getStartTime() < to) && (event.getStartTime() > from)) {
                matches.add(event.getEventDefinitionId());
            } else if ((event.getEndTime() != null) && (event.getEndTime() > from) && (event.getEndTime() < to)) {
                matches.add(event.getEventDefinitionId());

            } else if ((event.getStartTime() != null) && (event.getEndTime() != null) && (event.getEndTime() > to) && (event.getStartTime() < from)) {
                matches.add(event.getEventDefinitionId());
            }
        }
        IntCollection integers = new IntCollection();
        integers.addAll(matches);
        return integers;
    }

    private OverviewTrack createOverview(DetailedTimelineResult result, int viewportWidth) {
        long minTime = result.getLowerTimeBound();
        long maxTime = result.getUpperTimeBound();

        TimeUnit unit = calculateRelevantTimeUnit(minTime, maxTime, viewportWidth);
        double padTime = TimeUnit.calculateTimePadding(maxTime - minTime);
        minTime = (long) (minTime - padTime);
        maxTime = (long) (maxTime + padTime);

        long unitMinTime = unit.roundDown(minTime);
        long unitMaxTime = unit.roundUp(maxTime);

        List<Long> totalTics = new ArrayList<Long>();
        Long time = unitMinTime;
        totalTics.add(time);
        int count = 0;
        do {
            time = unit.addTo(time);
            totalTics.add(time);
            count++;
        } while ((time < unitMaxTime) && (count < 90000));

        int initialCapacity = totalTics.size();

        List<Integer> overviewNumbers = new ArrayList<Integer>(initialCapacity);
        for (int ii = 0; ii < initialCapacity; ii++) {
            overviewNumbers.add(0);
        }

        Long start = null;
        Long end = null;
        long duration = maxTime - minTime;
        for (SingularTimelineEvent event : result.getEvents()) {
            start = event.getStartTime();
            end = event.getEndTime();

            addMeasureToGroup(minTime, maxTime, unit, overviewNumbers, start, end, duration);
        }

        OverviewTrack overview = new OverviewTrack();
        overview.setMeasures(overviewNumbers);
        overview.setTimeUnit(unit);
        overview.setOverviewEnd(result.getUpperTimeBound());
        overview.setOverviewStart(result.getLowerTimeBound());

        return overview;
    }

    public void clearCache(VisualizationDef viz) {
        AbstractTimelineStorageService storageService = AbstractTimelineStorageService.instance();
        storageService.resetData(viz.getUuid());
    }

    public Object getCache(String vizUuid) {
        AbstractTimelineStorageService storageService = AbstractTimelineStorageService.instance();
        if (storageService.hasVisualizationData(vizUuid)) {
            TimelineStorage storage = storageService.getTimelineStorage(vizUuid);
            DataToTimelineResultTransformer transformer = new DataToTimelineResultTransformer();
            DetailedTimelineResult result = transformer.apply(storage.getResult());
            return result;
        } else {

        }

        return null;
    }

    @Override
    public MetricsDTO getViewMetrics(TimelineRequest req, List<String> visibleGroups) {
        MetricsDTO ret = new MetricsDTO();
        Object cache = getCache(req.getVizUuid());
        if (cache != null) {
            // is it always detailed or is there more to it?
            DetailedTimelineResult timeline = (DetailedTimelineResult) cache;

            int eventcount = 0;
            Set<String> tracks = new HashSet<>();
            for (SingularTimelineEvent event : timeline.getEvents()) {

                Long startTime = event.getStartTime();
                Long endTime = event.getEndTime();

                if ((startTime != null) && (endTime != null)) {

                    if ((startTime <= req.getStartTime()) && (endTime >= req.getEndTime())) {
                        eventcount = countEventsInGroups(req, eventcount, tracks, event, visibleGroups);

                    } else if ((startTime >= req.getStartTime()) && (startTime <= req.getEndTime())) {
                        eventcount = countEventsInGroups(req, eventcount, tracks, event, visibleGroups);

                    } else if ((endTime >= req.getStartTime()) && (endTime <= req.getEndTime())) {
                        eventcount = countEventsInGroups(req, eventcount, tracks, event, visibleGroups);
                    }
                } else if (startTime != null) {
                    if ((startTime >= req.getStartTime()) && (startTime <= req.getEndTime())) {
                        eventcount = countEventsInGroups(req, eventcount, tracks, event, visibleGroups);
                    }
                } else if (endTime != null) {
                    if ((endTime >= req.getStartTime()) && (endTime <= req.getEndTime())) {
                        eventcount = countEventsInGroups(req, eventcount, tracks, event, visibleGroups);
                    }
                }
            }

            ret.add(TimelineMetricsType.EVENTS.getLabel(), eventcount + "");
            if (tracks.size() > 1) {
                ret.add("Groups with Events", tracks.size() + "");
            }
        }
        LOG.trace("ChronosActionsService.getViewMetrics end");
        return ret;
    }

    private int countEventsInGroups(TimelineRequest req, int eventcount, Set<String> tracks, SingularTimelineEvent event, List<String> visibleGroups) {
        if (req.getTrackName() != null) {
            if (visibleGroups != null) {
                if (visibleGroups.contains(req.getTrackName())) {
                    if (event.getTrackValue().equals(req.getTrackName())) {
                        eventcount++;
                    }
                }
            } else {
                if (event.getTrackValue().equals(req.getTrackName())) {
                    eventcount++;
                }
            }
        } else {
            if (visibleGroups != null) {
                if (visibleGroups.contains(event.getTrackValue())) {
                    tracks.add(event.getTrackValue());
                    eventcount++;
                }
            } else {
                tracks.add(event.getTrackValue());
                eventcount++;
            }
        }
        return eventcount;
    }

    @Override
    public MetricsDTO getTotalMetrics(String uuid, String drillGroup) {
        Object cache = getCache(uuid);
        MetricsDTO ret = new MetricsDTO();
        if (cache != null) {
            // is it always detailed or is there more to it?
            DetailedTimelineResult result = (DetailedTimelineResult) cache;

            if (drillGroup == null) {
                // tracks seem like groups.
                int totalTracks = result.getTracks().size();
                // individual event count
                int totalEvents = result.getTotalEvents();
//                String x = TimelineMetricsType.EVENTS.getLabel();
                ret.add(TimelineMetricsType.EVENTS.getLabel(), totalEvents + "");
                if (totalTracks != 0) {
//                    String y = TimelineMetricsType.GROUPS.getLabel();
                    ret.add(TimelineMetricsType.GROUPS.getLabel(), totalTracks + "");
                }
            } else {
                int count = 0;
                for (TimelineTrack track : result.getTracks()) {
                    if (track.getName().equalsIgnoreCase(drillGroup)) {
                        for (SingularTimelineEvent event : result.getEvents()) {
                            if (event.getTrackValue().equals(drillGroup)) {
                                count++;
                            }
                        }
                    }
                }

                ret.add(TimelineMetricsType.EVENTS.getLabel(), count);
            }
        }
        return ret;
    }

    public void createCache(VisualizationDef viz, DataView dataView) {
       TimelineViewDef timelineViewDef = (TimelineViewDef) viz;
       TimelineResult result = null;

       try (Connection connection = CsiPersistenceManager.getCacheConnection()) {
          if (dataView == null) {
             throw new CentrifugeException(String.format("Dataview not found."));
          }
          final TimelineQueryBuilder queryBuilder = timelineActionsServiceUtil.getTimelineQueryBuilder(dataView, timelineViewDef);
          DataCacheHelper cacheHelper = new DataCacheHelper();
          DetailedTimelineResult fullResult = new DetailedTimelineResult();
          ScrollCallback<DetailedTimelineResult> detailedScrollCallback = createDetailedScrollCallback(dataView, timelineViewDef, queryBuilder, fullResult);
          SelectSQL sql = cacheHelper.getTimelineDetailedData(connection, queryBuilder, detailedScrollCallback);
          result = sql.scroll(detailedScrollCallback);
          OverviewTrack overview = createOverview(fullResult, 1000);

          fullResult.setOverviewData(overview);
          fullResult.setTotalEvents(fullResult.getEvents().size());
          cacheResult(timelineViewDef.getUuid(), (DetailedTimelineResult) result);
       } catch (Exception e) {
          LOG.error("Failed to create Timeline Cache", e);
       }
    }

    public Set<Integer> validateVisibleEvents(TimelineViewDef timelineViewDef, IntCollection selectedEventIds) {
        Object object = getCache(timelineViewDef.getUuid());

        if (object == null) {
            return Sets.newHashSet();
        }

        DetailedTimelineResult cache = (DetailedTimelineResult) object;

        Set<Integer> visibleSelection = retrieveFocusedEvents(timelineViewDef, selectedEventIds, cache);
        visibleSelection = removeHiddenEvents(timelineViewDef, visibleSelection, cache);

        return visibleSelection;
    }

    private Set<Integer> removeHiddenEvents(TimelineViewDef timelineViewDef,
                                            Set<Integer> eventIds, DetailedTimelineResult cache) {
        Set<Integer> selectionHash = Sets.newConcurrentHashSet(eventIds);
        Set<String> filteredKeys = new HashSet<String>();

        if ((timelineViewDef.getState() != null) && (timelineViewDef.getState().getTrackStates() != null)) {
            for (TimelineTrackState trackState : timelineViewDef.getState().getTrackStates()) {
                if (!trackState.getVisible().booleanValue()) {
                    filteredKeys.add(trackState.getTrackName());
                }
            }
        }

        Set<Integer> ids = retrieveHiddenItems(filteredKeys, timelineViewDef.getUuid(), cache);
        selectionHash.removeAll(ids);
        return selectionHash;
    }

    private Set<Integer> retrieveHiddenItems(Set<String> filteredKeys, String uuid, DetailedTimelineResult cache) {
        Set<Integer> nonVisibleEvents = Sets.newHashSet();

        for (SingularTimelineEvent event : cache.getEvents()) {
            if (filteredKeys.contains(event.getTrackValue())) {
                nonVisibleEvents.add(event.getEventDefinitionId());
            }
        }

        return nonVisibleEvents;
    }

    private Set<Integer> retrieveFocusedEvents(TimelineViewDef timelineViewDef,
                                               IntCollection eventIds, DetailedTimelineResult cache) {
        Set<Integer> selectedEvents = Sets.newHashSet(eventIds);

        if ((timelineViewDef.getState() == null) || (timelineViewDef.getState().getFocusedTrack() == null)) {
            return selectedEvents;
        }

        Set<Integer> focusedEvents = Sets.newHashSet();
        for (SingularTimelineEvent event : cache.getEvents()) {
            if ((event.getTrackValue() != null) && event.getTrackValue().equals(timelineViewDef.getState().getFocusedTrack())) {
                focusedEvents.add(event.getEventDefinitionId());
            }
        }
        focusedEvents.retainAll(selectedEvents);

        return focusedEvents;
    }

}

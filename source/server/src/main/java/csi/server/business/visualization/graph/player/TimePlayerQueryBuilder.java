package csi.server.business.visualization.graph.player;

import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import csi.server.business.helper.DataCacheHelper;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;

public class TimePlayerQueryBuilder {
    private String dataviewUuid;
    private String startFieldUuid;
    private LocalDateTime playbackEnd;
    private Duration duration;
    private TimePlayerPlaybackMode mode;
    private String startExpr;
    private String endExpr;
    private String coalesceExpr;
    private boolean isDateTime;
    private String visDefUuid;
    private String startColumn;
    private Connection connection;
    private boolean disallowInvertedRanges = true;

    public TimePlayerQueryBuilder(String dataviewUuid, String visDefUuid, String startFieldUuid, LocalDateTime playbackEnd,
                                  Duration duration, TimePlayerPlaybackMode mode, boolean asTimestamps) {
        if ((dataviewUuid == null) || (startFieldUuid == null)) {
            throw new IllegalArgumentException();
        }
        this.dataviewUuid = dataviewUuid;
        this.visDefUuid = visDefUuid;
        this.startFieldUuid = startFieldUuid;
        this.playbackEnd = playbackEnd;
        this.duration = duration;
        this.mode = mode;
        this.isDateTime = asTimestamps;

        DataView dv = CsiPersistenceManager.findObject(DataView.class, dataviewUuid);
        FieldListAccess modelDef = dv.getMeta().getModelDef().getFieldListAccess();
        FieldDef fieldDef = modelDef.getFieldDefByLocalId(startFieldUuid);

        startExpr = CacheUtil.makeCastExpression(fieldDef);
        this.startColumn = CacheUtil.getQuotedColumnName(fieldDef);

        buildCoalesceExpr();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void setConnection(Connection value) {
        connection = value;
    }

    private void buildCoalesceExpr() {
       List<String> parts = new ArrayList<String>(5);

       if ((mode == TimePlayerPlaybackMode.DYNAMIC_TIME_SPAN) || (mode == TimePlayerPlaybackMode.FIXED_TIME_SPAN)) {
          if (endExpr != null) {
             parts.add(endExpr);
          }
          if (duration != null) {
             parts.add(startExpr + " + interval '" + duration.toString() + "'");
          } else {
             parts.add(getTemporalType() + " '" + playbackEnd.plus(1L, ChronoUnit.MILLIS).format(getTemporalTypeFormatter()) + "'");
          }
       }
       if ((mode == TimePlayerPlaybackMode.CUMULATIVE) || parts.isEmpty()) {
          parts.add(startExpr + " + interval 'PT0.0001S'");
       }
       coalesceExpr = parts.stream().collect(Collectors.joining(", ", " coalesce( ", ") "));
    }

    public String buildQuery(LocalDateTime head, LocalDateTime tail) throws CentrifugeException {

        DateTimeFormatter formatter = getTemporalTypeFormatter();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT \"internal_id\" FROM ");

        builder.append(CacheUtil.getCacheTableName(dataviewUuid)).append(" ");
        builder.append("WHERE ( ");
        {
            builder.append(startExpr).append(", ");
            builder.append(coalesceExpr);
        }
        builder.append(") ");
        builder.append("OVERLAPS (");
        {
            builder.append(getTemporalType()).append(" '").append(head.format(formatter)).append("' , ");
            // HACK: need tweak here on the time. This is because PostgreSQL treats
            // the overlaps query as a [x, y) interval i.e. x <= value < y. To account for
            // data points that fall directly at y, we need to extend the window of time ever so slightly
            LocalDateTime usingTail = tail.plus(1L, ChronoUnit.MILLIS);
            builder.append(getTemporalType()).append(" '").append(usingTail.format(formatter)).append("' ");
        }
        builder.append(") ");

        if (disallowInvertedRanges) {
            builder.append(" AND (");
            builder.append(startExpr).append(" <= ").append(coalesceExpr);
            builder.append(" ) ");
        }

        VisualizationDef vizdef = CsiPersistenceManager.findObject(VisualizationDef.class, this.visDefUuid);

        String filterClause = new DataCacheHelper().getQueryFilter(dataviewUuid, vizdef);
        if ((filterClause != null) && (filterClause.length() > 0)) {
            builder.append(" AND ").append(filterClause);
        }

        return builder.toString();

    }

    public String buildQueryForPercentage(LocalDateTime head, LocalDateTime tail, int distinctPosition) throws CentrifugeException {

        DateTimeFormatter formatter = getTemporalTypeFormatter();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT DISTINCT " + startExpr + " FROM ");

        builder.append(CacheUtil.getCacheTableName(dataviewUuid)).append(" ");
        builder.append(startColumn);
        builder.append(" WHERE ( ");
        {
            builder.append(startExpr).append(", ");
            builder.append(coalesceExpr);
        }
        builder.append(") ");
        builder.append("OVERLAPS (");
        {
            builder.append(getTemporalType()).append(" '").append(head.format(formatter)).append("' , ");
            // HACK: need tweak here on the time. This is because PostgreSQL treats
            // the overlaps query as a [x, y) interval i.e. x <= value < y. To account for
            // data points that fall directly at y, we need to extend the window of time ever so slightly
            LocalDateTime usingTail = tail.plus(1L, ChronoUnit.MILLIS);
            builder.append(getTemporalType()).append(" '").append(usingTail.format(formatter)).append("' ");
        }
        builder.append(") ");

        if (disallowInvertedRanges) {
            builder.append(" AND (");
            builder.append(startExpr).append(" <= ").append(coalesceExpr);
            builder.append(" ) ");
        }

        VisualizationDef vizdef = CsiPersistenceManager.findObject(VisualizationDef.class, this.visDefUuid);

        String filterClause = new DataCacheHelper().getQueryFilter(dataviewUuid, vizdef);
        if ((filterClause != null) && (filterClause.length() > 0)) {
            builder.append(" AND ").append(filterClause);
        }

        builder.append(" ORDER BY ");

        builder.append(startExpr);

        builder.append(" LIMIT ");
        builder.append(distinctPosition);

        return builder.toString();

    }

    public String buildTrailingQuery(LocalDateTime start, LocalDateTime end) throws CentrifugeException{
    	DateTimeFormatter formatter = getTemporalTypeFormatter();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT MAX(sub.date)");
        builder.append(" FROM (");



        builder.append("SELECT ");
        builder.append(CacheUtil.toQuotedDbUuid(startFieldUuid));

        builder.append(" as date FROM ");

        builder.append(CacheUtil.getCacheTableName(dataviewUuid)).append(" ");
        builder.append("WHERE ( ");
        {
            builder.append(startExpr).append(", ");
            builder.append(coalesceExpr);
        }
        builder.append(") ");
        builder.append("OVERLAPS (");
        {
            builder.append(getTemporalType()).append(" '").append(start.format(formatter)).append("' , ");
            // HACK: need tweak here on the time. This is because PostgreSQL treats
            // the overlaps query as a [x, y) interval i.e. x <= value < y. To account for
            // data points that fall directly at y, we need to extend the window of time ever so slightly
            LocalDateTime usingEnd = end.plus(1L, ChronoUnit.MILLIS);
            builder.append(getTemporalType()).append(" '").append(usingEnd.format(formatter)).append("' ");
        }
        builder.append(") ");

        if (disallowInvertedRanges) {
            builder.append(" AND (");
            builder.append(startExpr).append(" <= ").append(coalesceExpr);
            builder.append(" ) ");
        }

        VisualizationDef vizdef = CsiPersistenceManager.findObject(VisualizationDef.class, this.visDefUuid);

        String filterClause = new DataCacheHelper().getQueryFilter(dataviewUuid, vizdef);
        if ((filterClause != null) && (filterClause.length() > 0)) {
            builder.append(" AND ").append(filterClause);
        }

        builder.append(" ORDER BY ");

        builder.append(startExpr);
        builder.append(") sub");

        return builder.toString();
    }

    public String buildQueryToLimit(LocalDateTime head, LocalDateTime tail, Integer position) throws CentrifugeException {

        DateTimeFormatter formatter = getTemporalTypeFormatter();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT \"internal_id\", ");
        builder.append(CacheUtil.toQuotedDbUuid(startFieldUuid));
        builder.append(" FROM ");

        builder.append(CacheUtil.getCacheTableName(dataviewUuid)).append(" ");
        builder.append("WHERE ( ");
        {
            builder.append(startExpr).append(", ");
            builder.append(coalesceExpr);
        }
        builder.append(") ");
        builder.append("OVERLAPS (");
        {
            builder.append(getTemporalType()).append(" '").append(head.format(formatter)).append("' , ");
            // HACK: need tweak here on the time. This is because PostgreSQL treats
            // the overlaps query as a [x, y) interval i.e. x <= value < y. To account for
            // data points that fall directly at y, we need to extend the window of time ever so slightly
            LocalDateTime usingTail = tail.plus(1L, ChronoUnit.MILLIS);
            builder.append(getTemporalType()).append(" '").append(usingTail.format(formatter)).append("' ");
        }
        builder.append(") ");

        if (disallowInvertedRanges) {
            builder.append(" AND (");
            builder.append(startExpr).append(" <= ").append(coalesceExpr);
            builder.append(" ) ");
        }

        VisualizationDef vizdef = CsiPersistenceManager.findObject(VisualizationDef.class, this.visDefUuid);

        String filterClause = new DataCacheHelper().getQueryFilter(dataviewUuid, vizdef);
        if ((filterClause != null) && (filterClause.length() > 0)) {
            builder.append(" AND ").append(filterClause);
        }

        builder.append(" ORDER BY ");

        builder.append(startExpr);

        builder.append(" LIMIT ");
        builder.append(position);

        return builder.toString();

    }

    public String buildSeekToDataQuery(LocalDateTime head) throws CentrifugeException {
        String template = "SELECT %2$s FROM %1$s WHERE %2$s > " + getTemporalType() + " '%3$s' AND %4$s ORDER BY %2$s";
        String currentTime = head.format(getTemporalTypeFormatter());

        // FIXME: update where clause w/ filters and broadcast
        VisualizationDef vizdef = CsiPersistenceManager.findObject(VisualizationDef.class, this.visDefUuid);

        String filterClause = new DataCacheHelper().getQueryFilter(dataviewUuid, vizdef);
        if ((filterClause == null) || (filterClause.length() == 0)) {
            filterClause = " TRUE = TRUE ";
        }

        String seekQuery = String.format(template, CacheUtil.getCacheTableName(dataviewUuid), startExpr, currentTime, filterClause);
        return seekQuery;
    }

    protected String getTemporalType() {
        return (isDateTime) ? "timestamp" : "time";
    }

    protected DateTimeFormatter getTemporalTypeFormatter() {
        DateTimeFormatter formatter;

        if (isDateTime) {
            formatter = DateTimeFormatter.ISO_DATE_TIME;
        } else {
            formatter = DateTimeFormatter.ISO_TIME;
        }
        return formatter;
    }
}

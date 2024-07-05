package csi.server.business.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.Display;

import csi.config.Configuration;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.player.CumulativePlaybackActionHandler;
import csi.server.business.visualization.graph.player.GraphPlayer;
import csi.server.business.visualization.graph.player.WindowedPlaybackHandler;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;
import csi.server.common.service.api.GraphTimePlayerServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;

@Service(path = "/services/graph/timeplayer")
public class GraphTimePlayerService extends AbstractService implements GraphTimePlayerServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(GraphTimePlayerService.class);

    private static final String DVUUID_PARAM = "dvuuid";
    private static final String VIZUUID_PARAM = "vduuid";

    private static void throwCentrifugeException(String message) throws CentrifugeException {
        throw new CentrifugeException(message);

    }

    /*
     * Careful using this method -- the presumption is that the startType is
     * never null! If there's no end type or both types are equal we're ok.
     *
     * The edge case is where one of them is a time
     */
   private static boolean areSimilar(CsiDataType startType, CsiDataType endType) {
      return ((startType == endType) || ((startType != CsiDataType.Time) && (endType != CsiDataType.Time)));
   }

   private static DateRangeResultObject queryForDataRange(String dataCacheId, String query)
         throws CentrifugeException, SQLException {
      DateRangeResultObject range = new DateRangeResultObject();

      try (Connection conn = CsiPersistenceManager.getCacheConnection();
           ResultSet rs = QueryHelper.executeSingleQuery(conn, query, null)) {
         if (!rs.next() || (rs.getTimestamp(2) == null)) {
            throw new CentrifugeException("invalid data range encountered");
         }
         range.setCount(rs.getInt(1));
         range.setStart(rs.getTimestamp(2).getTime());
         range.setEnd(rs.getTimestamp(3).getTime());
      }
      return range;
   }

   @Override
   @Operation
   public GraphPlayerSettings getTimePlayerSettings(@QueryParam(value = VIZUUID_PARAM) String vizUuid)
         throws CentrifugeException {
      GraphPlayerSettings settings = null;

      try {
         //TimePlayerHelper helper = new TimePlayerHelper();
         // settings = helper.getTimePlayerSettings(vizUuid);

         //if (settings == null) {
         RelGraphViewDef viewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);

         if (viewDef.getPlayerSettings() == null) {
            GraphPlayerSettings modelSettings = new GraphPlayerSettings();

            viewDef.setPlayerSettings(modelSettings);

            settings = new GraphPlayerSettings();
            settings.speed_ms = Configuration.getInstance().getTimePlayerConfig().getFastSpeed();

            // FIXME: commenting out for compile
            // new DataViewHelper().copyTo(modelSettings, settings);
            CsiPersistenceManager.persist(modelSettings);
         } else {
            settings = viewDef.getPlayerSettings();
         }
         //}
      } catch (Exception e) {
         e.printStackTrace();
      }
      return settings;
   }

    @Override
   @Operation
    public List<String> seek(@QueryParam(VIZUUID_PARAM) String vizId, @QueryParam("pos") Long position)
            throws CentrifugeException {

        GraphContext context = GraphServiceUtil.getGraphContext(vizId);
        if (context == null) {
            throw new CentrifugeException("Graph Data not loaded");
        }

        synchronized (context) {
            GraphPlayer player = context.getPlayer();
            if (player != null) {
                if (player.seek(position)) {
                    context.setPlayerRunning(true);
                    player.play();
                }
                List<String> range = getWindowPositions(player);
                return range;
            }
        }
        return new ArrayList<String>();
    }

    @Override
   @Operation
    public List<String> seekPosition(@QueryParam(VIZUUID_PARAM) String vizId, @QueryParam("pos") Integer position)
            throws CentrifugeException {

        GraphContext context = GraphServiceUtil.getGraphContext(vizId);
        if (context == null) {
            throw new CentrifugeException("Graph Data not loaded");
        }

        synchronized (context) {
            GraphPlayer player = context.getPlayer();

            if (player != null) {
                if (player.seek(position)) {
                    context.setPlayerRunning(true);
                    player.play();
                }
                List<String> range = getWindowPositions(player);
                return range;
            }
        }
        return new ArrayList<String>();
    }

    private List<String> getWindowPositions(GraphPlayer player) throws CentrifugeException {
        List<String> range = new ArrayList<String>(3);

        if (player.getHead() == null){
            throw new CentrifugeException("Graph Player not properly initialized");
        }
        range.add(String.valueOf(player.getHead().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

        if (player.isWindowedPlayback()) {
           LocalDateTime pos = player.getTrailingPosition();

           if (pos != null) {
              range.add(String.valueOf(pos.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
           }
        }
        return range;
    }

    @Override
    public List<String> activatePlayer(String dvUuid, String vizUuid, GraphPlayerSettings data)
            throws CentrifugeException {
      List<String> positions = null;
      GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);

      if (context == null) {
         TaskHelper.abortTask("No graph data present");
         // Never reached since abortTask() throws a run-time exception. here
         // for posterity sake.
         return null;
      }
      synchronized (context) {
         context.resetPlayer();
         context.createPlayer();

         GraphPlayer player = context.getPlayer();

         if (player == null) {
            if (LOG.isDebugEnabled()) {
               LOG.info("Player has been destroyed.");
            }
         } else {
            if (data.playbackMode == TimePlayerPlaybackMode.CUMULATIVE) {
               player.setPlaybackHandler(new CumulativePlaybackActionHandler());
            } else {
               player.setPlaybackHandler(new WindowedPlaybackHandler());
            }
            if (!data.hideNonVisibleItems) {
               Display display = context.getDisplay();
               Color orig = display.getBackground();

               display.setBackground(new Color(0x00ffffff, true));

               // Dimension viewport = context.getDisplay().getSize();
               // BufferedImage origGraph = GraphManager.getInstance().renderGraph(context, viewport);
               BufferedImage origGraph =
                  new BufferedImage(display.getWidth(), display.getHeight(), BufferedImage.TYPE_INT_ARGB);
               GraphManager.getInstance().renderToImage(origGraph, display);

               display.setBackground(orig);

               List<BufferedImage> layers = Collections.singletonList(origGraph);

               context.setImageLayers(layers);
            }
            try {
               player.initialize(data);
               positions = getWindowPositions(player);
            } catch (Exception exception) {
               return null;
            }
         }
      }
      return positions;
   }

    @Override
   @Operation
    public List<String> stepPlayer(@QueryParam(value = VIZUUID_PARAM) String vizUuid) throws CentrifugeException {

        if (StringUtils.isBlank(vizUuid)) {
            TaskHelper.abortTask("no viz uuid provided");
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        if (context == null) {
            // TaskHelper.abortTask( "No graph data present");
            throw new CentrifugeException("Graph data has not been loaded");
        }

        synchronized (context) {
            GraphPlayer player = context.getPlayer();
            if (player == null) {
                context.setPlayerRunning(false);
                throw new CentrifugeException("Player is not initialized or has been stopped");
            }
            try {
                boolean canStep = player.step();

                if (canStep) {
                    context.setPlayerRunning(true);
                    player.play();
                }

            } catch (Exception e) {
                throw new CentrifugeException(e);
            }
            return getWindowPositions(player);
        }
    }

    @Override
   @Operation
    public void stopPlayer(@QueryParam(value = VIZUUID_PARAM) String vizUuid) throws CentrifugeException {
        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        if (context == null) {
            throwCentrifugeException("No graph data present.");
        }

        synchronized (context) {
            context.resetPlayer();
        }
    }

    private CsiMap<String, String> getRangeByField(@QueryParam(value = DVUUID_PARAM) String dvUuid,
            @QueryParam(value = VIZUUID_PARAM) String vizUuid,
            @QueryParam(value = "startFieldUUID") String startFieldUuid,
            @QueryParam(value = "endFieldUUID") String endFieldUuid,
            @QueryParam("durationNumber") Long durationValue,
            TimePlayerUnit durationPeriod, Date start, Date end

    ) throws CentrifugeException {

        if (dvUuid == null) {
            throwCentrifugeException("Dataview identifier required for this operation");
        }

        if (vizUuid == null) {
            throwCentrifugeException("Visualization identifier required");
        }

        if (StringUtils.isEmpty(startFieldUuid)) {
            throwCentrifugeException("startFieldUUID is required");
        }

        // construct our coalesced query....really should have a standard
        // extractor for this, but
        // the casts and offsets are pretty ugly.
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        FieldListAccess modelDef = dataView.getMeta().getModelDef().getFieldListAccess();

        FieldDef startFieldDef = modelDef.getFieldDefByLocalId(startFieldUuid);

        if (startFieldDef == null) {
            throwCentrifugeException("startFieldUUID does not reference a valid field.");
        } else if (startFieldDef.getFieldType() == FieldType.STATIC) {
            throwCentrifugeException("startField cannot reference a static field.");
        }

        CsiDataType startFieldType = startFieldDef.getValueType();

        CsiDataType endFieldType = null;

        FieldDef endField = null;

        List<String> options = new ArrayList<String>();
        if (StringUtils.isNotEmpty(endFieldUuid)) {
                endField = modelDef.getFieldDefByLocalId(endFieldUuid);
            if (endField == null){
                TaskHelper.abortTask("Invalid end field provided");
            } else if (endField.getFieldType() == FieldType.STATIC) {
                throwCentrifugeException("endField cannot reference a static field.");
            }

            endFieldType = endField.getValueType();

            String cacheTyped = CacheUtil.makeCastExpression(endField);
            options.add(cacheTyped);
        }

        if ((durationPeriod != null) && (durationValue != null)) {
           //entire period: String value = cachedType + " + interval 'PT0.0001S'";
           Duration duration = GraphPlayer.computeDuration(durationValue.intValue(), durationPeriod);
           String cachedType = CacheUtil.makeCastExpression(startFieldDef);

            if (duration.isZero()) {
               options.add(cachedType);
            } else {
               options.add(cachedType + " + interval '" + duration.toString() + "' ");
            }
        }

        if (endFieldType != null) {
            // validate equivalent types.
            if (!areSimilar(startFieldType, endFieldType)) {

                throwCentrifugeException("The start and end fields are not compatible types for playback.");
            }
        }
        String join = options.isEmpty() ? null : options.stream().collect(Collectors.joining(",", "coalesce( ", ") "));
        String startCastExpr = CacheUtil.makeCastExpression(startFieldDef);
        String minExpr = startCastExpr;
        String maxExpr;
        String endAsColumn = CacheUtil.getQuotedColumnName(startFieldDef);

        if(join == null){
            maxExpr = minExpr;
        } else {
            maxExpr = join;
            if(endField != null){
                endAsColumn = CacheUtil.getQuotedColumnName(endField);
            }
        }

        VisualizationDef vizDef = CsiPersistenceManager.findObject(VisualizationDef.class, vizUuid);

        String queryFilter = new DataCacheHelper().getQueryFilter(dvUuid, vizDef);

        String query;
        Timestamp startTime = null;
        Timestamp endTime = null;
        String startRestrictionExpression = "TRUE = TRUE";
        String endRestrictionExpression = "TRUE = TRUE";

        if(start != null){
            try{
                startTime = CsiTypeUtil.coerceTimestamp(start);
            } catch(Exception exception){
                if(start != null){
                   LOG.debug("Can't handle object:" + start.toString());
                }
            }
        }

        if (startTime != null) {
            startRestrictionExpression = startCastExpr + " >= '" + startTime.toString()+"'";
        }

        if(end != null){
            try{
                endTime = CsiTypeUtil.coerceTimestamp(end);
            } catch(Exception exception){
                if(end != null){
                   LOG.debug("Can't handle object:" + end.toString());
                }
            }
        }

        if (endTime != null) {
            endRestrictionExpression = endAsColumn + " <= '" + endTime.toString() +"'";
        }


        //Don't bother using endfield if it's the same as startfield
        if((endFieldUuid == null) || endFieldUuid.equals(startFieldUuid)){
            if (StringUtils.isNotEmpty(queryFilter)) {
                query = String.format("SELECT  count(*), min( %2$s ), max( %3$s ) FROM (SELECT DISTINCT %2$s FROM %1$s WHERE %4$s AND %6$s AND %7$s) as %5$s",
                        CacheUtil.getCacheTableName(dvUuid), minExpr, maxExpr, queryFilter, CacheUtil.getQuotedColumnName(startFieldDef), startRestrictionExpression, endRestrictionExpression);
            } else {
                query = String.format("SELECT  count(*), min( %2$s ), max( %3$s ) FROM (SELECT DISTINCT %2$s FROM %1$s WHERE %5$s AND %6$s) as %4$s",
                        CacheUtil.getCacheTableName(dvUuid), minExpr, maxExpr, CacheUtil.getQuotedColumnName(startFieldDef), startRestrictionExpression, endRestrictionExpression);
            }
        } else {
           if (StringUtils.isNotEmpty(queryFilter)) {
            	query = String.format("SELECT  count(*), min( %2$s ), max( %3$s ) FROM (SELECT DISTINCT %2$s, max( %3$s ) as %6$s FROM %1$s WHERE %4$s AND %8$s AND %7$s Group By %5$s) as %5$s",
            	        CacheUtil.getCacheTableName(dvUuid), minExpr, maxExpr, queryFilter, CacheUtil.getQuotedColumnName(startFieldDef),  endAsColumn, startRestrictionExpression, endRestrictionExpression);
            } else {
            	query = String.format("SELECT  count(*), min( %2$s ), max( %3$s ) FROM (SELECT DISTINCT %2$s, max( %3$s ) as %5$s FROM %1$s WHERE %6$s AND %7$s Group By %4$s) as %4$s",
            	        CacheUtil.getCacheTableName(dvUuid), minExpr, maxExpr, CacheUtil.getQuotedColumnName(startFieldDef), endAsColumn, startRestrictionExpression, endRestrictionExpression);
            }
        }

        if (endField != null ) {
            query += " WHERE " + startCastExpr + " <= " + maxExpr;
        }



        CsiMap<String, String> prop = new CsiMap<String, String>();
        try {
            DateRangeResultObject range = queryForDataRange(dvUuid, query);

            prop.put("count", Integer.toString(range.getCount()));
            prop.put("min", Long.toString(range.getStart()));
            prop.put("max", Long.toString(range.getEnd()));
        } catch (Exception e) {
            //CEN-753 don't log error in this instance
        }
        return prop;

    }

    @Override
    public CsiMap<String, String> getEndPoints(String dataviewUuid, String graphUuid, String startFieldUuid, String endFieldUuid,
            int duration, TimePlayerUnit unit, Date start, Date end) {
        CsiMap<String, String> rangeByField = null;
        try {
            rangeByField = getRangeByField(dataviewUuid, graphUuid, startFieldUuid, endFieldUuid, Long.valueOf(duration), unit, start, end);
        } catch (NumberFormatException e) {
            //CEN-753 don't log error in this instance
            // e.printStackTrace();
        } catch (CentrifugeException e) {
            //CEN-753 don't log error in this instance
            // e.printStackTrace();
        }
        return rangeByField;
    }
}

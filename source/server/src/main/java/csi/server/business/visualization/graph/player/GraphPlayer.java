package csi.server.business.visualization.graph.player;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.server.business.helper.QueryHelper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;
import csi.server.dao.CsiPersistenceManager;

/*
 *
 * TODO:
 *
 * Remove getters for current and last frame -- require user of this API to
 * pass in an Action interface.  The action will embody what operations
 * are performed on the deltas between steps.
 *
 */
public class GraphPlayer {
   private static final Logger LOG = LogManager.getLogger(GraphPlayer.class);

    // where the current head of playback is located.
    private LocalDateTime head;
    private String vizUuid;
    private String dvUuid;
    private GraphContext graphContext;
    private Multimap<Integer, Node> nodeIndex;
    private Multimap<Integer, Edge> edgeIndex;
    private GraphData lastFrame;
    private GraphData currentFrame;
    private boolean hasStepped;
    private boolean isSeeking;
    private GraphData seekFrame;
    // represents the absolute min/max values of the data
    private LocalDateTime playbackStart;
    private LocalDateTime playbackEnd;

    // Player concepts.
    // how much to advance a single frame
    private Duration stepSize;
    private TimePlayerStepMode stepMode;
    // how much time is included in the frame
    private Duration frameSize;
    // providing a window over the data, or operating in cumulative play mode?
    // determines behavior of the display for nodes and links.
    private TimePlayerPlaybackMode dataMode;
    /*
     * identify the fields to be used for determining the duration of an
     * object's occurrence. The optionalDuration allows us to declare a relative
     * offset to the startField when the end has no value.
     */
    private String startField;
    private String endField;
    private Duration duration;
    private boolean initialized;
    private Lock lock = new ReentrantLock();
    private PlaybackHandler playbackHandler;
    private boolean hideInactiveNodes;
    private TimePlayerQueryBuilder queryBuilder;
    // type that we need to cast our values to. either timestamp or time.
    private CsiDataType targetType;
    private GraphState initialGraphState;
    private boolean firstPlay;

    public GraphPlayer(String uuid) {
        this.vizUuid = uuid;
        initialized = false;
    }

   public void initialize(GraphPlayerSettings data) {
      destroy();

      if (LOG.isDebugEnabled()) {
         LOG.debug("Graph Time Player initializing");
      }
      firstPlay = true;

      try {
         hasStepped = false;

         lock.lock();

         hideInactiveNodes = data.hideNonVisibleItems;
         graphContext = GraphServiceUtil.getGraphContext(vizUuid);
         dvUuid = graphContext.getDvUuid();
         startField = toSafeString(data.startField.getLocalId());

         if (data.endField != null) {
            endField = toSafeString(data.endField.getLocalId());
         }
         stepMode = data.stepMode;
         stepSize = computeDuration(data.stepSizeNumber, data.stepSizePeriod);

         if ((data.durationNumber > 0) && (data.durationUnit != null)) {
            duration = computeDuration(data.durationNumber, data.durationUnit);
         }
         if ((data.playbackMode == TimePlayerPlaybackMode.DYNAMIC_TIME_SPAN) ||
             (data.playbackMode == TimePlayerPlaybackMode.FIXED_TIME_SPAN)) {
            dataMode = TimePlayerPlaybackMode.DYNAMIC_TIME_SPAN;
            frameSize = computeDuration(data.frameSizeNumber, data.frameSizePeriod);
            hideInactiveNodes = data.hideNonVisibleItems;
         } else {
            dataMode = TimePlayerPlaybackMode.CUMULATIVE;
            frameSize = stepSize;
         }
         if ((data.playbackStart != null) && (data.playbackEnd != null)) {
            playbackStart = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.playbackStart.getTime()), ZoneId.systemDefault());
            playbackEnd = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.playbackEnd.getTime()), ZoneId.systemDefault());

            initializeState();
            buildQueryTemplates();

            if (playbackHandler != null) {
               playbackHandler.initialize(this, graphContext);
            }
            initialized = true;
         }
      } finally {
         lock.unlock();
      }
      if (LOG.isDebugEnabled()) {
         LOG.debug("Graph Time Player initialization complete.");
      }
   }

    private void buildQueryTemplates() {
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        DataViewDef meta = dataView.getMeta();
        FieldListAccess modelDef = meta.getModelDef().getFieldListAccess();
        FieldDef startFieldDef = modelDef.getFieldDefByLocalId(startField);

        if (startFieldDef.getFieldType() == FieldType.STATIC) {
            throw new PlayerException("Static fields cannot be used for playback.");
        }
        FieldDef endFieldDef = null;

        if (endField != null) {
            endFieldDef = modelDef.getFieldDefByLocalId(endField);

            if (endFieldDef.getFieldType() == FieldType.STATIC) {
                throw new PlayerException("Static fields cannot be used for playback.");
            }
        }
        CsiDataType startType = startFieldDef.getValueType();
        CsiDataType endType = null;

        if (endFieldDef != null) {
            endType = endFieldDef.getValueType();
        }
        if ((endFieldDef != null) && !isSimilarTypes(startType, endType)) {
            throw new PlayerException("incompatible data types for start and end fields");
        }
        targetType = findRelatedType(startType, endType);

        boolean asTimestamps = targetType == CsiDataType.DateTime;
        queryBuilder = new TimePlayerQueryBuilder(dvUuid, vizUuid, startField, playbackEnd, duration, dataMode, asTimestamps);
    }

    private CsiDataType findRelatedType(CsiDataType t1, CsiDataType t2) {
        if (t1 == CsiDataType.Time) {
            return CsiDataType.Time;
        } else {
            return CsiDataType.DateTime;
        }
    }

   private boolean isSimilarTypes(CsiDataType t1, CsiDataType t2) {
      boolean valid = (t1 == t2);

      if (!valid) {
         if ((t1 == null) ^ (t2 == null)) {
            valid = true;
         } else if (t1 == CsiDataType.Time) {
            valid = false;
         } else if (t1 == CsiDataType.Date) {
            valid = (t2 == CsiDataType.DateTime);
         } else if (t1 == CsiDataType.DateTime) {
            valid = (t2 == CsiDataType.Date);
         }
      }
      return valid;
   }

    private String toSafeString(String name) {
        // this was originally here to quote out uuids for queries...can get rid
        // of this.
        if ((name == null) || (name.length() == 0)) {
            return null;
        }
        return name;
    }

    // Note: if we decide to do gradual fade outs over a frame,
    // we'll need to augment how we maintain state. e.g.
    // maintain the last 5 frames of state, where each frame
    // gets a different alpha.
    private void initializeState() {
        if (LOG.isDebugEnabled()) {
           LOG.debug("Player initialized for range: " + playbackStart.toString() + " : " + playbackEnd.toString());
        }
        head = playbackStart;
        lastFrame = new GraphData();
        currentFrame = new GraphData();

        createIndexes();

        initialGraphState = new GraphState(graphContext.getVisualGraph());
    }

   public static Duration computeDuration(int value, TimePlayerUnit durationPeriod) {
      Duration result = Duration.ZERO;

      switch (durationPeriod) {
         case DAY:
            result = Duration.of(value, ChronoUnit.DAYS);
            break;
         case HOUR:
            result = Duration.of(value, ChronoUnit.HOURS);
            break;
         case MILLISECOND:
            result = Duration.of(value, ChronoUnit.MILLIS);
            break;
         case MINUTE:
            result = Duration.of(value, ChronoUnit.MINUTES);
            break;
         case MONTH:
            result = Duration.of(value, ChronoUnit.MONTHS);
            break;
         case QUARTER:
            result = Duration.of(3 * value, ChronoUnit.MONTHS);
            break;
         case SECOND:
            result = Duration.of(value, ChronoUnit.SECONDS);
            break;
         case WEEK:
            result = Duration.of(value, ChronoUnit.WEEKS);
            break;
         case YEAR:
            result = Duration.of(value, ChronoUnit.YEARS);
            break;
         default:
            break;
      }
      return result;
   }

    public GraphState getInitialGraphState() {
        return initialGraphState;
    }

    public void destroy() {
        try {
            lock.lock();

            if (initialized) {
                if (playbackHandler != null) {
                    playbackHandler.destroy();
                }
                initialGraphState.reset();

                firstPlay = true;
                nodeIndex = null;
                edgeIndex = null;
                currentFrame = lastFrame = null;
                initialized = false;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean step() {
        validateState();

        try {
            lock.lock();
            LocalDateTime end = playbackEnd;
            LocalDateTime nextStep = head.plus(stepSize);
            boolean stepped = false;
            // advance head -- handle edge case of just starting off where we
            // haven't stepped yet.
            // we need this since we always have a positioned head on the first
            // data point.
            // Ideally we would play then check step, but then state is lost on
            // what is 'new';
            // unless we add in a 'canStep' method.
            if (!hasStepped) {
                hasStepped = true;
                stepped = true;
            } else if (nextStep.isBefore(end)) {
                head = head.plus(stepSize);
                stepped = true;
            } else if (head.isBefore(end) && (nextStep.isAfter(end) || nextStep.isEqual(end))) {
                head = end;
                stepped = true;
            }

            return stepped;
        } finally {
            lock.unlock();
        }
    }

    public boolean seek(Integer position) {
       Integer usePosition = (position.intValue() <= 0) ? Integer.valueOf(1) : position;
       isSeeking = false;
       seekFrame = null;

       if (stepMode == TimePlayerStepMode.PERCENTAGE) {
          isSeeking = true;
          seekFrame = (dataMode == TimePlayerPlaybackMode.CUMULATIVE)
                         ? populateDataForWindow(playbackStart, playbackEnd, usePosition)
                         : populateCurrentFrame(usePosition);

          playbackHandler.resetVisuals();
          playbackHandler.seek(seekFrame);

          currentFrame = new GraphData();
       }
       return isSeeking;
    }

    public boolean seek(Long position) throws CentrifugeException {
        isSeeking = false;
        seekFrame = null;
        LocalDateTime seekTo = LocalDateTime.ofInstant(Instant.ofEpochMilli(position.longValue()), ZoneId.systemDefault());

        if ((playbackStart.isBefore(seekTo) && playbackEnd.isAfter(seekTo)) || playbackStart.isEqual(seekTo) || playbackEnd.isEqual(seekTo)) {
            if (stepMode == TimePlayerStepMode.ABSOLUTE) {  //TODO: both branches are identical
                isSeeking = true;
                head = seekTo;
                seekFrame = (dataMode == TimePlayerPlaybackMode.CUMULATIVE) ? populateDataForWindow(playbackStart, seekTo) : populateCurrentFrame();

                playbackHandler.resetVisuals();
                playbackHandler.seek(seekFrame);

                currentFrame = new GraphData();
            } else {
                isSeeking = true;
                head = seekTo;
                seekFrame = (dataMode == TimePlayerPlaybackMode.CUMULATIVE) ? populateDataForWindow(playbackStart, seekTo) : populateCurrentFrame();

                playbackHandler.resetVisuals();
                playbackHandler.seek(seekFrame);

                currentFrame = new GraphData();
            }
        }
        return isSeeking;
    }

    private GraphData populateCurrentFrame(Integer position) {
        return populateDataForWindow(position);
    }

    /**
     * This method works slightly different than the others,
     * it resets the head based on the position of the scrubber
     * @param position
     * @return
     */
   private GraphData populateDataForWindow(Integer position) {
      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         queryBuilder.setConnection(conn);

         String firstQuery = queryBuilder.buildQueryForPercentage(playbackStart, playbackEnd, position);
         //String query = queryBuilder.buildTrailingQuery(playbackInterval.getStart(), playbackInterval.getEnd(), position);

         queryBuilder.setConnection(null);

         //We attempt to grab all rows for a current time if in PERCENTAGE mode
         try (ResultSet rs =
               (stepMode == TimePlayerStepMode.PERCENTAGE)
                  ? QueryHelper.executeSingleQuerySized(conn, firstQuery, 2048)
                  : QueryHelper.executeSingleQuery(conn, firstQuery, null)) {
            Date lastDate = new Date(0);

            while (rs.next()) {
               lastDate = rs.getDate(1);
            }
            head = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastDate.getTime()), ZoneId.systemDefault());
         }
      } catch (SQLException sqle) {
         throw new PlayerException(sqle);
      } catch (CentrifugeException ce) {
         throw new PlayerException(ce);
      }
      return populateDataForWindow(computeTrailingPosition(), head);
   }

    public void play() throws CentrifugeException {
        validateState();

        lock.lock();
        try {

            lastFrame = currentFrame;
            if (isSeeking) {
                currentFrame = seekFrame;
                isSeeking = false;
            } else {
            	if(stepMode != TimePlayerStepMode.PERCENTAGE) {
                  currentFrame = populateCurrentFrame();
               }
            }

            boolean noChanges = currentFrame.isEmpty() || currentFrame.equals(lastFrame);
            int maxRetry = 0;

            while ((stepMode != TimePlayerStepMode.ABSOLUTE) && noChanges) {
            	if (!firstPlay) {
            		seekToNextDataPoint();
            	} else {
            		firstPlay = false;
            	}
            	currentFrame = populateCurrentFrame();
            	noChanges = currentFrame.isEmpty() || currentFrame.equals(lastFrame);

            	if (stepMode == TimePlayerStepMode.PERCENTAGE) {
            	   noChanges = false; //Don't think I need this for percentage
            	}
            	//kind of a hack, gotta find a way to determine end
            	if (maxRetry == 10) {
            	   break;
            	} else {
            	   maxRetry++;
            	}
            }
            if (playbackHandler != null) {
                playbackHandler.step(lastFrame, currentFrame);
            }
        } finally {
            lock.unlock();
        }
    }

    private void validateState() {
        if (!initialized) {
            throw new IllegalStateException();
        }
    }

    /*
     * This should be a temporary operation. Once the graph is constructed and
     * stored in the cache, we will not need to create this index of rows -->
     * nodes.
     *
     * Note that we could do this at graph creation; but that would
     * significantly increase memory usage at the cost of potentially never
     * running the playback!
     */
    @SuppressWarnings("unchecked")
    private void createIndexes() {
        if (LOG.isDebugEnabled()) {
           LOG.debug("Constructing indexes for graph");
        }
        StopWatch total = new StopWatch();
        total.start();

        nodeIndex = HashMultimap.create();
        edgeIndex = HashMultimap.create();
        Graph graph = graphContext.getGraphData();

        for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext(); ) {
            Node node = nodes.next();

            if (!GraphContext.Predicates.IsVisualizedAndDisplayable.test(node)) {
                continue;
            }
            NodeStore details = GraphManager.getNodeDetails(node);
            Map<String, List<Integer>> rows = details.getRows();

            for (List<Integer> specRows : rows.values()) {
                for (Integer row : specRows) {
                    nodeIndex.put(row, node);
                }
            }
        }
        total.split();

        StopWatch edgeTimer = new StopWatch();
        edgeTimer.start();

        Predicate<Edge> isEdgeVisible = GraphContext.Predicates.IsEdgeVisualizedAndDisplayable;

        for (Iterator<Edge> edges = graph.edges(); edges.hasNext(); ) {
            Edge edge = edges.next();

            if (isEdgeVisible.test(edge)) {
               LinkStore details = GraphManager.getEdgeDetails(edge);

               if (BundleUtil.isBundleLink(details)) {
                  Set<Integer> rows = BundleUtil.getBundleLinkRows(details);

                  for (Integer row : rows) {
                     edgeIndex.put(row, edge);
                  }
               } else {
                  Map<String, List<Integer>> rows = details.getRows();

                  for (List<Integer> specRows : rows.values()) {
                     for (Integer row : specRows) {
                        edgeIndex.put(row, edge);
                     }
                  }
               }
            }
        }
        edgeTimer.stop();
        total.stop();

        if (LOG.isDebugEnabled()) {
           LOG.debug("Index for graph time player constructed.");
           LOG.trace("Node indexing time: " + total.toSplitString());
           LOG.trace("Edge indexing time: " + edgeTimer.toString());
           LOG.trace("Total indexing time: " + total.toString());
        }
    }

    private GraphData populateCurrentFrame() throws CentrifugeException {
       LocalDateTime trailing = computeTrailingPosition();

       return populateDataForWindow(trailing, head);
    }

    public LocalDateTime computeTrailingPosition() {
       LocalDateTime trailing = head.minus(frameSize);

        if (trailing.isBefore(playbackStart)) {
           trailing = playbackStart;
        }
        return trailing;
    }

   private void seekToNextDataPoint() throws CentrifugeException {
      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         queryBuilder.setConnection(conn);

         String query = queryBuilder.buildSeekToDataQuery(head);

         queryBuilder.setConnection(null);

         try (ResultSet rs = QueryHelper.executeSingleQuery(conn, query, null)) {
            if (rs.next()) {
               Timestamp ts = rs.getTimestamp(1);
               LocalDateTime nextPoint = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime()), ZoneId.systemDefault());

               if (playbackStart.isAfter(nextPoint) || playbackEnd.isBefore(nextPoint)) {
                  nextPoint = playbackEnd;
               }
               head = nextPoint;
            } else {
               head = playbackEnd;
            }
         }
      } catch (SQLException sqle) {
         throw new PlayerException(sqle);
      } catch (CentrifugeException ce) {
         throw new PlayerException(ce);
      }
   }

   private GraphData populateDataForWindow(LocalDateTime startTime, LocalDateTime endTime) {
      GraphData gd = new GraphData();

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         queryBuilder.setConnection(conn);

         String query = queryBuilder.buildQuery(startTime, endTime);

         queryBuilder.setConnection(null);

         //We attempt to grab all rows for a current time if in PERCENTAGE mode
         try (ResultSet rs =
                 (stepMode == TimePlayerStepMode.PERCENTAGE)
                    ? QueryHelper.executeSingleQuerySized(conn, query, 2048)
                    : QueryHelper.executeSingleQuery(conn, query, null)) {
            while (rs.next()) {
               updateData(gd, rs.getInt(1));
            }
         }
      } catch (SQLException sqle) {
         throw new PlayerException(sqle);
      } catch (CentrifugeException ce) {
         throw new PlayerException(ce);
      }
      return gd;
   }

   private GraphData populateDataForWindow(LocalDateTime startTime, LocalDateTime endTime, Integer position) {
      GraphData gd = new GraphData();

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         queryBuilder.setConnection(conn);

         String firstQuery = queryBuilder.buildQueryForPercentage(startTime, endTime, position);

         queryBuilder.setConnection(null);

         //We attempt to grab all rows for a current time if in PERCENTAGE mode
         try (ResultSet rs =
                 (stepMode == TimePlayerStepMode.PERCENTAGE)
                    ? QueryHelper.executeSingleQuerySized(conn, firstQuery, null, 2048)
                    : QueryHelper.executeSingleQuery(conn, firstQuery, null)) {
            Date lastDate = new Date(0);

            while (rs.next()) {
               lastDate = rs.getDate(1);
            }
            head = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastDate.getTime()), ZoneId.systemDefault());
         }
         String queryAll = queryBuilder.buildQuery(startTime, head);

         try (ResultSet rs =
                 (stepMode == TimePlayerStepMode.PERCENTAGE)
                    ? QueryHelper.executeSingleQuerySized(conn, queryAll, null, 2048)
                    : QueryHelper.executeSingleQuery(conn, queryAll, null)) {
            while (rs.next()) {
                updateData(gd, rs.getInt(1));
            }
         }
      } catch (SQLException sqle) {
         throw new PlayerException(sqle);
      } catch (CentrifugeException ce) {
         throw new PlayerException(ce);
      }
      return gd;
   }

    private void updateData(GraphData data, int row_id) {
        Collection<Node> nodes = nodeIndex.get(row_id);

        for (Node node : nodes) {
            if (GraphManager.getNodeDetails(node).isDisplayable()) {
                data.nodes.add(node.getRow());
            }
        }
        Collection<Edge> edges = edgeIndex.get(row_id);

        for (Edge edge : edges) {
            LinkStore details = GraphManager.getEdgeDetails(edge);

            if (details.isDisplayable()) {
                data.edges.add(edge.getRow());
            }
        }
    }

    public LocalDateTime getHead() {
       return head;
    }

    public GraphData getLastFrame() {
        return lastFrame;
    }

    public GraphData getCurrentFrame() {
        return currentFrame;
    }

    public PlaybackHandler getPlaybackHandler() {
        return playbackHandler;
    }

    public void setPlaybackHandler(PlaybackHandler playbackHandler) {
        this.playbackHandler = playbackHandler;
    }

    public boolean isHideInactiveNodes() {
        return hideInactiveNodes;
    }

    public boolean isWindowedPlayback() {
        return ((dataMode == TimePlayerPlaybackMode.DYNAMIC_TIME_SPAN) || (dataMode == TimePlayerPlaybackMode.FIXED_TIME_SPAN));
    }

    public LocalDateTime getTrailingPosition() {
       return isWindowedPlayback() ? computeTrailingPosition() : null;
    }

    public static class GraphState {
        Set<NodeItem> nodes = new HashSet<NodeItem>();
        Set<EdgeItem> links = new HashSet<EdgeItem>();
        Set<NodeItem> hilightNodes = new HashSet<NodeItem>();
        Set<EdgeItem> hilightEdges = new HashSet<EdgeItem>();

        private VisualGraph vgraph;

        public GraphState(VisualGraph graph) {
            this.vgraph = graph;
            captureState();
        }

      private void captureState() {
         for (Iterator<NodeItem> nodeIterator = vgraph.nodes(); nodeIterator.hasNext();) {
            NodeItem ni = nodeIterator.next();

            if (!GraphContext.Predicates.IsBundled.test(ni)) {
               if (!ni.isVisible()) {
                  nodes.add(ni);
               }
               if (ni.isHighlighted()) {
                  hilightNodes.add(ni);
                  ni.setHighlighted(false);
               }
            }
         }
         for (Iterator<EdgeItem> edgeIterator = vgraph.edges(); edgeIterator.hasNext();) {
            EdgeItem ei = edgeIterator.next();

            if (GraphContext.Predicates.IsEdgeDisplayable.test(ei)) {
               if (!ei.isVisible()) {
                  links.add(ei);
               }
               if (ei.isHighlighted()) {
                  hilightEdges.add(ei);
                  ei.setHighlighted(false);
               }
            }
         }
      }

        public void reset() {
            for (NodeItem ni : nodes) {
                ni.setVisible(false);
                ni.setHighlighted(false);
            }
            for (EdgeItem ei : links) {
                ei.setVisible(false);
                ei.setHighlighted(false);
            }
            for (NodeItem ni : hilightNodes) {
                ni.setHighlighted(true);
            }
            for (EdgeItem ei : hilightEdges) {
                ei.setHighlighted(true);
            }
        }
    }

    public static class GraphData {
        public Set<Integer> nodes = new HashSet<Integer>();
        public Set<Integer> edges = new HashSet<Integer>();

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((edges == null) ? 0 : edges.hashCode());
            result = (prime * result) + ((nodes == null) ? 0 : nodes.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
           return (this == obj) ||
                  ((obj != null) &&
                   (obj instanceof GraphData) &&
                   !((edges == null) ^ (((GraphData) obj).edges == null)) &&
                   !((nodes == null) ^ (((GraphData) obj).nodes == null)) &&
                   ((edges != null) &&
                    (edges.size() == ((GraphData) obj).edges.size()) &&
                    edges.containsAll(((GraphData) obj).edges)) &&
                   ((nodes != null) &&
                    (nodes.size() == ((GraphData) obj).nodes.size()) &&
                    nodes.containsAll(((GraphData) obj).nodes)));
        }

        public boolean isEmpty() {
            return ((nodes == null) || nodes.isEmpty()) && ((edges == null) || edges.isEmpty());
        }
    }
}

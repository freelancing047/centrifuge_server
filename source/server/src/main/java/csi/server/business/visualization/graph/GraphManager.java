package csi.server.business.visualization.graph;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.Bindings;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.layout.CircleLayout;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.GridLayout2;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.SpanningTree;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TableEdge;
import prefuse.data.tuple.TableNode;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.BreadthFirstIterator;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.GravitationalForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.client.gwt.viz.graph.node.settings.tooltip.AnchorLinkType;
import csi.config.Configuration;
import csi.config.RelGraphConfig;
import csi.config.TimePlayerConfig;
import csi.graph.mongo.Helper;
import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.FilterActionsService;
import csi.server.business.service.visualization.theme.ThemeManager;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.graph.base.property.AggregateProperty;
import csi.server.business.visualization.graph.base.property.ComputedProperty;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.graph.base.property.ProxiedProperty;
import csi.server.business.visualization.graph.cache.LayoutCache;
import csi.server.business.visualization.graph.cluster.FastCommunityClusterer;
import csi.server.business.visualization.graph.data.GraphDataManager;
import csi.server.business.visualization.graph.layout.CentrifugeLayout;
import csi.server.business.visualization.graph.layout.CircularLayout;
import csi.server.business.visualization.graph.layout.CopyPositionLayout;
import csi.server.business.visualization.graph.layout.CsiApplyForceLayout;
import csi.server.business.visualization.graph.layout.CsiForceDirectedLayout;
import csi.server.business.visualization.graph.layout.CsiRungeKuttaIntegrator;
import csi.server.business.visualization.graph.layout.DirectedNodeLinkTreeLayout;
import csi.server.business.visualization.graph.layout.DirectedRadialTreeLayout;
import csi.server.business.visualization.graph.layout.RunPlacementAction;
import csi.server.business.visualization.graph.listeners.AugmentGraphListener;
import csi.server.business.visualization.graph.placement.BreadthFirstSearch;
import csi.server.business.visualization.graph.placement.DirectedBreadthFirstSearch;
import csi.server.business.visualization.graph.placement.DirectedSpanningTree;
import csi.server.business.visualization.graph.plunk.LinkPlunker;
import csi.server.business.visualization.graph.plunk.NodePlunker;
import csi.server.business.visualization.graph.query.GraphQueryBuilder;
import csi.server.business.visualization.graph.util.BundleMetrics;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.business.visualization.legend.LegendItem;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.GraphTooManyTypesException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.ConditionalExpression;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeAggregateType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.server.common.model.visualization.graph.GraphCachedState;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.GraphConstants.eLayoutAlgorithms;
import csi.server.common.model.visualization.graph.HierarchicalLayoutOrientation;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskHelper;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;
import csi.server.util.SqlUtil;
import csi.server.util.sql.CacheCommands;
import csi.server.util.sql.SQLFactory;
import csi.server.util.sql.ScrollCallback;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.visualization.graph.GraphLayout;
import csi.shared.gwt.viz.graph.LinkDirection;

public class GraphManager {
   private static final Logger LOG = LogManager.getLogger(GraphManager.class);

    public static final String GRAPH_SELECTIONS = "selections";
    public static final String DEFAULT_SELECTION = "default.selection";
    public static final String OPTION_SET_NAME = "optionSetName";
    public static final String VIEWDEF_UUID = "viewUuid";
    public static final String DV_UUID = "dvUuid";
    public static final String EDGE_HASH_TABLE = "edgeHashTable";
    public static final String NODE_HASH_TABLE = "nodeHashTable";
    public static final String DATA_INTERNAL_ID = "internal_id";
    public static final String NODES_BY_DEFINITION = "nodesByNodeDef";
    public static final String LINKS_BY_DEFINITION = "linksByLinkDef";
    public static final String NODES_BY_INTERNAL_ROW = "nodesByRows";
    public static final EnumSet<eLayoutAlgorithms> DIRECTED_LAYOUTS = EnumSet.of(eLayoutAlgorithms.treeRadial,
            eLayoutAlgorithms.treeNodeLink);
    public static final String GRAPH_CONTEXT = "graphContext";

    private static final Pattern COLOR_PATTERN = Pattern.compile("([0-9]*),([0-9]*),([0-9]*),([0-9]*)");
    private static final Pattern COUNT_PATTERN = Pattern.compile("(?i)countDistinctBy\\(\\s*([^\\)]*)\\s*\\)");

    static {
        populateIgnoreAttributes();

        populateDistinctValueAttributes();
    }

    private static final int DEFAULT_BUNDLE_THRESHOLD = 1000;
    private static final int PATCH_MARGIN = 40;
    private static final int PATCH_PADDING = 40;
    private static final String TEMP_GRAPH = "temp.coarsened.graph";
    private static final String TEMP_SELECTION = "temp.selection";
    public static String LINKS_BY_INTERNAL_ROW = "linksByRows";

    public static String LAYOUT_AND_COMPONENTS_COMPUTED_COMPLETE = "InitialLayoutAndComponentsComputedComplete";
    private static Set<String> ignoreAttributes;
    private static Set<String> distinctValueAttrbutes;
    private static ImageProvider ImageWithAlphaProvider = new DefaultImageProvider(true);
    private static GraphManager instance = null;
    private static final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private static Cache<String, Layout> layouts;
    {
        layouts = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(new RemovalListener<String, Layout>() {
            @Override
            public void onRemoval(RemovalNotification<String, Layout> notification) {
                Visualization visualization = notification.getValue().getVisualization();
                visualization.invalidateAll();
                visualization.reset();
            }
        }).build();
    }

    private GraphManager() {
    }

    private static void populateDistinctValueAttributes() {
        distinctValueAttrbutes = new HashSet<String>();
        distinctValueAttrbutes.add(ObjectAttributes.CSI_INTERNAL_LABEL);
    }

    private static void populateIgnoreAttributes() {
        ignoreAttributes = new HashSet<String>();
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_ID);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_X_POS);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_Y_POS);
        //        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_ICON);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_SHAPE);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_SHAPE_OVERRIDE);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_WIDTH_OVERRIDE);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_ICON_OVERRIDE);
        ignoreAttributes.add(ObjectAttributes.CSI_INTERNAL_COLOR);
    }

    public static GraphManager getInstance() {
        if (instance == null) {
            instance = new GraphManager();
        }
        return instance;
    }

    public static BundleMetrics computeBundleIconSize(VisualGraph vGraph, NodeStore bundle, BundleMetrics metrics) {
        List<AbstractGraphObjectStore> children = bundle.getChildren();
        if ((children == null) || children.isEmpty()) {
            return metrics;
        }
        for (AbstractGraphObjectStore graphObjectStore : children) {
            NodeStore child = (NodeStore) graphObjectStore;
            if (!metrics.bySize) {
                metrics.bySize = child.isBySize();
            }
            if (!metrics.byTransparency) {
                //                metrics.byTransparency = child.isByTransparency();
            }
            metrics.numNodes++;
            metrics.size += child.getRelativeSize();
            metrics = computeBundleIconSize(vGraph, child, metrics);
        }
        return metrics;
    }

    public static AggregateProperty computeAggregateProperty(Property prop, AttributeAggregateType function)
            throws InstantiationException, IllegalAccessException {

        Property property = prop;

        // unwrap the AggregateProperty instance
        if (property instanceof AggregateProperty) {
            property = ((AggregateProperty) property).getProperty();
        }

        if (property instanceof ComputedProperty) {
            ((ComputedProperty) property).setType(function);
        }
        Double results = 1D;
        try {
            Function<Property, Double> aggregate = AttributeFunctions.getAggregateFunction(function);
            results = aggregate.apply(property);
        }catch (NullPointerException npe){
            String s = "null";
            if(function!=null){
                s = function.toString();
            }
            LOG.warn("Unable to find aggregate function "+ s,npe);
        }
        AggregateProperty ap = new AggregateProperty(property.getName());
        ap.setValue(results);
        ap.setProperty(property);
        return ap;
    }

    private static boolean evaluateConditional(String expression, CsiScriptRunner scriptRunner, Bindings bindings)
            throws CentrifugeException {
        if ((expression == null) || (expression.trim().length() == 0)) {
            return true;
        }

        Object evalExpression = scriptRunner.evalExpression(expression, bindings);
        boolean results = true;

        if (evalExpression != null) {
            results = Boolean.parseBoolean(evalExpression.toString());
        }

        return results;
    }

    public static boolean isDynamicType(NodeDef nodeDef) {
        AttributeDef typeAttr = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        if (typeAttr != null) {
            FieldDef fieldDef = typeAttr.getFieldDef();
            if (fieldDef == null) {
                return false;
            }
            return fieldDef.getFieldType() != FieldType.STATIC;
        }

        AttributeDef labelAttr = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
        return (labelAttr == null);
    }

    public static boolean isDynamicType(LinkDef linkDef) {
        AttributeDef typeAttr = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        if (typeAttr != null) {
            FieldDef fieldDef = typeAttr.getFieldDef();
            if (fieldDef == null) {
                return false;
            }
            return fieldDef.getFieldType() != FieldType.STATIC;
        }

        AttributeDef labelAttr = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
        return (labelAttr == null);
    }

    public static Object getNodeType(NodeDef nodeDef, Map<String, Object> attributeValues) {
        Object nodeType = attributeValues.get(ObjectAttributes.CSI_INTERNAL_TYPE);

        if ((nodeType == null) && (nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE) != null)) {
            AttributeDef nodeTypeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
            if (nodeTypeDef.getFieldDef().isAnonymous()) {
                nodeType = nodeTypeDef.getFieldDef().getStaticText();
            }

        }

        if (nodeType == null) {
            AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
            if (attributeDef == null) {
                attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID);
            }
            FieldDef fd = attributeDef.getFieldDef();

            if (fd != null) {
                nodeType = fd.getFieldName();

                if ((nodeType == null) || ((nodeType.toString().length() == 0) && (fd.getFieldType() == FieldType.STATIC))) {
                    nodeType = fd.getStaticText();
                }
            }
        }

        if (nodeType == null) {
            return GraphConstants.UNSPECIFIED_NODE_TYPE;
        } else {
            return nodeType;
        }
    }

    private static String getLabel(NodeDef nodeDef, Map<String, Object> attributeValues) {
        Object object = attributeValues.get(ObjectAttributes.CSI_INTERNAL_LABEL);
        if (object == null) {
            object = attributeValues.get(ObjectAttributes.CSI_INTERNAL_ID);
        } else if (object instanceof Number) {
            object = CsiTypeUtil.formatNumberValue((Number) object, CsiTypeUtil.DEFAULT_NUMBER_FORMAT);
        }

        return (object == null) ? null : object.toString();
    }

    public static boolean skip(String key) {
        return ignoreAttributes.contains(key) || GraphMetrics.isMetricName(key);
    }

    private static String computeAttributeKey(AttributeDef attributeDef, LinkDirection direction) {
        if ((direction != null) && (attributeDef.getKind() == AttributeKind.COMPUTED)) {
            return attributeDef.getName() + "." + direction;
        }

        return attributeDef.getName();
    }

    private static void addComputedPropertyValue(Map<String, Property> attributes, AttributeDef attributeDef,
            Object value, LinkDirection direction) {
        String attributeName = attributeDef.getName();
        String key = direction != null ? attributeName + "." + direction : attributeName;
        Property property = attributes.get(key);

        if (property == null) {
            property = new ComputedProperty(attributeName);
            property.setIncludeInTooltip(attributeDef.isIncludeInTooltip());
            property.setHideEmptyInTooltip(attributeDef.isHideEmptyInTooltip());
            attributes.put(key, property);
        }

        // unwrap the AggreageProperty
        List<Object> values = null;
        if (property instanceof AggregateProperty) {
            values = ((AggregateProperty) property).getProperty().getValues();
        } else {
            values = property.getValues();
        }
        if (value != null) {
            if (distinctValueAttrbutes.contains(attributeName)) {
                if (!values.contains(value)) {
                    values.add(value);
                }
            } else {
                values.add(value);
            }
        } else {
            if (!values.contains(null)) {
                values.add(value);
            }
        }
    }

    private static void addPropertyValue(Map<String, Property> attributes, AttributeDef attributeDef, Object value,
            String key) {
        String attributeName = key;
        if (attributeDef == null) {
            attributeDef = new AttributeDef();
        }
        Property property = attributes.get(attributeName);
        if (property == null) {
            property = new Property(attributeName);
            property.setIncludeInTooltip(attributeDef.isIncludeInTooltip());
            property.setHideEmptyInTooltip(attributeDef.isHideEmptyInTooltip());
            property.setTooltipOrdinal(attributeDef.getTooltipOrdinal());
            attributes.put(attributeName, property);
        }

        // unwrap the AggregatePropery object
        List<Object> values = null;
        if (property instanceof AggregateProperty) {
            values = ((AggregateProperty) property).getProperty().getValues();
        } else {
            values = property.getValues();
        }
        if (value != null) {
            if (distinctValueAttrbutes.contains(attributeName)) {
                if (!values.contains(value)) {
                    values.add(value);
                }
            } else {
                values.add(value);
            }
        } else {
            if (!values.contains(null)) {
                try {
                    values.add(value);
                }catch (Exception ignored){}
            }
        }
    }

    private static Map<String, Object> retrieveAttributeValues(Set<AttributeDef> attributeDefs, CacheRowSet rawData)
            throws Exception {
        Map<String, Object> attrMap = new HashMap<String, Object>();
        for (AttributeDef adef : attributeDefs) {
            Object value = resolveValue(adef, rawData);
            if(value != null) {
               value = possiblyChangeValueForTooltip(rawData, adef, value);
            }
            attrMap.put(adef.getName(), value);
        }
        return attrMap;
    }

   private static Object possiblyChangeValueForTooltip(CacheRowSet rawData, AttributeDef adef, Object valueIn) {
      Object valueOut = valueIn;

      if (adef.getTooltipLinkFeildDef() != null) {
         // special wrapping to make sure url, and text travel together.
         JsonObject o = new JsonObject();

         o.addProperty(AnchorLinkType.URL, valueIn.toString());

         if (rawData.get(adef.getTooltipLinkFeildDef()) != null) {
            o.addProperty(AnchorLinkType.TEXT, rawData.get(adef.getTooltipLinkFeildDef()).toString());
         }
         valueOut = o.toString();
      }
      if (adef.getTooltipLinkText() != null) {
         // special wrapping to make sure url, and text travel together.
         JsonObject o = new JsonObject();

         o.addProperty(AnchorLinkType.URL, valueIn.toString());
         o.addProperty("text", adef.getTooltipLinkText());

         valueOut = o.toString();
      }
      return valueOut;
   }

    private static Object resolveValue(AttributeDef adef, CacheRowSet rowset) throws Exception {
        FieldDef fieldDef = adef.getFieldDef();
        return rowset.get(fieldDef);

    }

    private static String getID(NodeDef nodeDef, Map<String, Object> attributeValues) throws Exception {
        Object object = attributeValues.get(ObjectAttributes.CSI_INTERNAL_ID);
        if (object == null) {
            object = attributeValues.get(ObjectAttributes.CSI_INTERNAL_LABEL);
        }

        if (object instanceof Number) {
            object = CsiTypeUtil.formatNumberValue((Number) object, CsiTypeUtil.DEFAULT_NUMBER_FORMAT);
        }
        return (object == null) ? null : object.toString();
    }

    private static boolean doesNodeExist(Graph graph, NodeDef nodeDef, String nodeID, Map<String, Object> attributeValues) {
        HashMap<String, Node> nodeMap = (HashMap<String, Node>) graph.getClientProperty(NODE_HASH_TABLE);
        return (nodeMap.get(nodeID) == null);
    }

    private static Node createNode(Graph graph, NodeDef nodeDef, String nodeID, Map<String, Object> attributeValues) {
        HashMap<String, Node> nodeMap = (HashMap<String, Node>) graph.getClientProperty(NODE_HASH_TABLE);

        Node node = nodeMap.get(nodeID);
        if (node == null) {
            node = graph.addNode();
            NodeStore nodeStore = new NodeStore();
            nodeStore.setDocId(ObjectId.get());

            nodeStore.setKey(nodeID);
            nodeStore.setSpecID(nodeDef.getName());
            GraphManager.setNodeDetails(node, nodeStore);
            nodeMap.put(nodeID, node);
        }
        return node;
    }

    public static void setLinkDetails(Edge edge, LinkStore linkStore) {
        // FIXME : momve to retrieval from storage.
        edge.set(GraphConstants.LINK_DETAIL, linkStore);
    }

    private static String getConditionalExpr(NodeDef nodeDef, String type) {
        String expr = null;
        AttributeDef def = null;

        def = nodeDef.getAttributeDef(type);
        if (def != null) {
            FieldDef f = def.getFieldDef();
            if ((f != null) && (f.getFieldType() == FieldType.STATIC)) {
                expr = f.getStaticText();
            }
        }

        if ((expr == null) || expr.trim().isEmpty()) {
            ConditionalExpression conditional = null;
            if (ObjectAttributes.CSI_CREATE_IF.equals(type)) {
                conditional = nodeDef.getCreateConditional();
            } else {
                conditional = nodeDef.getHiddenConditional();
            }
            if (conditional != null) {
                expr = conditional.getExpression();
            }
        }

        if ((expr != null) && expr.trim().isEmpty()) {
            expr = null;
        }

        return expr;
    }

   public static final String computeLinkID(Node source, Node target) {
      return new StringBuilder().append(source.getRow()).append('+').append(target.getRow()).toString();
   }

   public static final String invertLinkID(String linkID) {
      int index = linkID.indexOf('+');

      return new StringBuilder().append(linkID, index + 1, linkID.length()).append('+').append(linkID, 0, index).toString();
   }

    public static void fitToSize(Visualization vis) {
        synchronized (vis) {
            Display display = vis.getDisplay(0);
            synchronized (display) {
                Rectangle2D bounds = vis.getBounds(ObjectAttributes.NODES_OBJECT_TYPE);
                fitToRegion(vis, bounds);
            }
        }
    }

    public static void fitToRegion(Visualization vis, Rectangle2D bounds) {
        // bounds is expected to be in global coordinates
        fitToRegion(vis, bounds, true, true);
    }

    public static void zoomToRegion(Visualization vis, Rectangle2D bounds) {
        // bounds is expected to be in display coordinates
        fitToRegion(vis, bounds, false, false);
    }

   public static void fitToRegion(Visualization vis, Rectangle2D bounds, boolean absoluteRegion, boolean absoluteScale) {
      synchronized (vis) {
         Display display = vis.getDisplay(0);

         synchronized (display) {
            double dw = display.getWidth();
            double dh = display.getHeight();
            double bw = bounds.getWidth();
            double bh = bounds.getHeight();

            if ((bw <= 0) || (bh <= 0)) {
               LOG.warn("Invalid region: " + bounds);
            } else {
               double scaleW = dw / bw;
               double scaleH = dh / bh;
               double scale = Math.min(scaleW, scaleH);

               if (absoluteScale && !Double.isNaN(display.getScale())) {
                  scale = scale / display.getScale();
               }
               Point2D center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());

               if (!absoluteRegion) {
                  center = display.getAbsoluteCoordinate(center, null);
               }
               display.panToAbs(center);
               display.zoomAbs(center, scale);
            }
         }
      }
   }

    public static String getComponentAt(Visualization vis, Point2D ref) {
        Display display = vis.getDisplay(0);
        Point2D displayPoint = toDataPoint(display, ref);
        Graph graph = (Graph) vis.getSourceData("graph");
        List<Graph> subgraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);

        if (subgraphs == null) {
            return null;
        }
        String componentId = "-1";
        int howMany = subgraphs.size();

        for (int i = 0; i < howMany; i++) {
            Graph g = subgraphs.get(i);
            Rectangle region = (Rectangle) g.getClientProperty(GraphConstants.PATCH_REGION);
            if ((region != null) && region.contains(displayPoint)) {
                componentId = Integer.toString(i);
                break;
            }
        }

        if (componentId.equals("-1")) {
            return null;
        } else {
            return componentId;
        }
    }

    public static Point2D toDataPoint(Display display, Point2D location) {
        AffineTransform transform = display.getInverseTransform();
        Point2D doublePoint = (transform == null ? location : transform.transform(location, null));

        Point point = new Point();
        point.setLocation(doublePoint);

        return point;

    }

    /*
     * Applies the grouping definition to the supplied graph. The nodes considered for gro
     *
     * @param graph
     *
     * @param groupingDefiniton
     */
    public static void applyBundleDefinition(Graph graph, BundleDef groupingDefiniton) {

    }

    public static Node createBundle(Graph graph, String name, SelectionModel currentSelection) {
        VisualGraph vGraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);
        Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);

        Node bundleNode = idNodeMap.get(name);
        if (bundleNode == null) {
            bundleNode = graph.addNode();
            NodeStore bundleDetails = new NodeStore();
            bundleDetails.setDocId(ObjectId.get());

            bundleDetails.addLabel(name);
            bundleDetails.setBundle(true);
            bundleNode.setBoolean(GraphContext.IS_VISUALIZED, true);
            GraphManager.setNodeDetails(bundleNode, bundleDetails);
            bundleDetails.setChildren(new ArrayList<AbstractGraphObjectStore>());
            bundleDetails.setKey(new CsiUUID().toString());
            // bundleDetails.setShape( "hexagon" );
            idNodeMap.put(bundleDetails.getKey(), bundleNode);
        }

        NodeStore bundleDetails = getNodeDetails(bundleNode);

        if (bundleDetails != null) {
           Set<Number> components = new HashSet<Number>();

           for (Integer id : currentSelection.nodes) {
              Node node = vGraph.getNode(id.intValue());
              int cid = node.getInt(GraphConstants.COMPONENT_ID);

              components.add(Integer.valueOf(cid));

              NodeStore childDetails = getNodeDetails(node);

              bundleDetails.addChild(childDetails);
              bundleDetails.setType(GraphConstants.BUNDLED_NODES);
           }
        }
        // update the graph's edges to represent connections
        // from children nodes contained by this bundle.
        BundleUtil.augmentEdges(bundleNode);
        graph.putClientProperty("dirty", Boolean.TRUE);

        // TODO: Optimization to determine which components are merged by this bundle.
        // if( components.size() > 1 ) {
        // GraphManager gm = new GraphManager();
        // gm.computeComponents( graph );
        // gm.computeComponentRegions( graph );
        // }
        return bundleNode;
    }

    public static String getNodeIdentifier(Node s) {
        NodeStore details = getNodeDetails(s);

        return (details != null) ? details.getKey() : null;
    }

    public static NodeStore getNodeDetails(Node node) {
        if(node == null){
            return null;
        }

        Object details = node.get(GraphConstants.NODE_DETAIL);
        NodeStore nodeStore = null;
        if(details != null) {
         nodeStore = (NodeStore)details;
      }
        return nodeStore;
    }

    public static void setNodeDetails(Node node, NodeStore details) {
        node.set(GraphConstants.NODE_DETAIL, details);
    }

    public static NodeStore getNodeDetails(Tuple tuple) {
        if (tuple instanceof Node) {
            return getNodeDetails((Node) tuple);
        } else {
            NodeStore retValue = new NodeStore();
            retValue.setDocId(ObjectId.get());
            return retValue;
        }
        // String id = tuple.getString(GraphConstants.DOC_ID);
        // Graph graph = tuple.getGraph();
        // MongoGraphStorage storage = (MongoGraphStorage) graph.getClientProperty(GraphConstants.STORAGE);
        //
        // DBObject query = Helper.getIdQuery(id);
        //
        // DBObject vertex = storage.findVertex(query);
        // NodeStore details = buildNodeStore( vertex );
        // return details;
        // return (NodeStore) tuple.get(GraphConstants.NODE_DETAIL);
    }

    public static LinkStore getEdgeDetails(Edge edge) {
        if(edge == null){
            return null;
        }
        Object details = edge.get(GraphConstants.LINK_DETAIL);

        LinkStore linkStore = null;
        if(details != null) {
         linkStore = (LinkStore)details;
      }
        return linkStore;
    }

    public static LinkStore getEdgeDetails(Tuple edge) {
        return getEdgeDetails((Edge) edge);
    }

    public static Point getXYFrom(Double x, Double y) {
        return new Point(x.intValue(), y.intValue());
    }

    public static void hideNodeAndEdges(VisualGraph vGraph, Node node) {
        NodeItem ni = (NodeItem) vGraph.getNode(node.getRow());
        ni.setVisible(false);

        Iterator edges = vGraph.edges(ni);
        while (edges.hasNext()) {
            VisualItem ve = (VisualItem) edges.next();
            ve.setVisible(false);
        }
    }

    public static SelectionModel getSelection(Visualization vis, String id) {
        TupleSet sourceData = vis.getSourceData("graph");
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) sourceData
                .getClientProperty(GRAPH_SELECTIONS);
        if (selections == null) {
            selections = new HashMap<String, SelectionModel>();
            sourceData.putClientProperty(GRAPH_SELECTIONS, selections);
        }

        if (id == null) {
            id = DEFAULT_SELECTION;
        }

        SelectionModel selection = selections.get(id);

        if (selection == null) {
            selection = new SelectionModel();
            selections.put(id, selection);
        }

        return selection;

    }

    public static String getVisualizationId(Graph graph) {
        return (String) graph.getClientProperty(VIEWDEF_UUID);
    }

    public static Object getDocId(Tuple tuple) {
        return tuple.get(GraphConstants.DOC_ID);
    }

    public Graph createGraph(String dvUuid) throws CentrifugeException {
        GraphContext graphContext = new GraphContext(dvUuid, null, null);
        return createGraph(graphContext, dvUuid, null);
    }

    public Graph createGraph(GraphContext context) throws CentrifugeException {
        String dvUuid = context.getDvUuid();
        String vizUuid = context.getVizUuid();

        return createGraph(context, dvUuid, vizUuid);
    }

    // private void addVertexData(DBObject data, NodeDef nodeDef, String nodeLabel, Map<String, Object> attrs) {
    // DBObject internalPayload = Helper.getInternalPayload(data);
    // List defs = (List) internalPayload.get("defs");
    // if (defs == null) {
    // defs = new BasicDBList();
    // internalPayload.put("defs", defs);
    // }
    //
    // if (nodeDef != null && !defs.contains(nodeDef.getName())) {
    // defs.add(nodeDef.getName());
    // }
    //
    // if (!data.containsField(GraphConstants.LABEL)) {
    // data.put(GraphConstants.LABEL, nodeLabel);
    // }
    //
    // Set<String> keys = attrs.keySet();
    // for (String key : keys) {
    // if (key.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
    // continue;
    //
    // }
    //
    // Object value = attrs.get(key);
    // if (!data.containsField(key)) {
    // data.put(key, value);
    // } else {
    // List l;
    // if (data.get(key) instanceof List) {
    // l = (List) data.get(key);
    // } else {
    // l = new BasicDBList();
    // l.add(data.get(key));
    // data.put(key, l);
    // }
    //
    // if (!l.contains(value)) {
    // l.add(value);
    // }
    // }
    // }
    //
    // }

    public void augmentGraph(Connection conn, GraphContext context, int generation, boolean visible)
            throws CentrifugeException {
        String dvUuid = context.getDvUuid();
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        RelGraphViewDef viewDef = context.getVisualizationDef();

        DataCacheHelper cacheHelper = new DataCacheHelper();
        String filter = cacheHelper.getQueryFilter(dvUuid, viewDef);
        filter = (filter == null) ? "" : filter.trim();

        // generation clause...
        String s = "\"" + CacheUtil.INTERNAL_STATEID + "\" = " + generation;
        if (filter.length() == 0) {
            filter = s;
        } else {
            filter = s + " AND " + filter;
        }
        String query = String.format(CacheCommands.SELECT_ALL_QUERY, CacheUtil.getQuotedCacheTableName(dvUuid));
        query += " WHERE " + filter;
        ResultSet resultSet = null;
        try {

            GraphContext.Current.set(context);

//            String optionSetName = context.getOptionSetName();
//            OptionSet optionSet = OptionSetManager.getOptionSet(optionSetName);

            GraphTheme theme = ThemeManager.getGraphTheme(viewDef.getThemeUuid());

            resultSet = QueryHelper.executeSingleQuery(conn, query, null);

            GraphProcessorListener listener = new AugmentGraphListener(context, visible);
            processGraphResults(context.getVisibleGraph(), viewDef, theme, dataView, resultSet, listener);
            computeAndLayoutComponents(context);
        } catch (Exception t) {
            String dvName = (dataView == null) ? "*null*" : dataView.getName();

            LOG.error(String.format("error creating graph for dv '%s'", dvName), t);
            CentrifugeException ce = new CentrifugeException(t);
            ce.setLogged(true);
            throw ce;
        } finally {
            SqlUtil.quietCloseResulSet(resultSet);
            GraphContext.Current.set(null);
        }

    }

    public Graph createGraph(GraphContext context, String dvUuid, String vizUuid) throws CentrifugeException {
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        if (dv == null) {
            throw new CentrifugeException("Dataview not found: " + dvUuid);
        }

        if (vizUuid == null) {
            throw new CentrifugeException("Visualization Id is required");
        }

        DataModelDef modelDef = dv.getMeta().getModelDef();

        RelGraphViewDef graphDef = null;
        graphDef = (RelGraphViewDef) modelDef.findVisualizationByUuid(vizUuid);

        // bail if there's no view def info
        if (graphDef == null) {
            throw new CentrifugeException("The graph definition could not be located.");
        }

        List<NodeDef> nodeDefs = graphDef.getNodeDefs();
        if ((nodeDefs == null) || nodeDefs.isEmpty()) {
            // This is true of every newly created dataview.
            throw new CentrifugeException("No nodes are specified in the graph definition");
        }

        // allow creation of the graph if no links are defined--not really
        // interesting graph since there's no relations....
        if ((graphDef.getLinkDefs() == null) || graphDef.getLinkDefs().isEmpty()) {
            LOG.warn("No links are specified in the graph definition");
        }

        Graph graph = context.getGraphData();
        //context.setOptionSetName(graphDef.getOptionSetName());
        context.setThemeUuid(graphDef.getThemeUuid());

        graph.putClientProperty(NODES_BY_DEFINITION, null);
        graph.putClientProperty(LINKS_BY_DEFINITION, null);

        graph.putClientProperty(NODES_BY_INTERNAL_ROW, HashMultimap.<Integer, Integer>create());
        graph.putClientProperty(LINKS_BY_INTERNAL_ROW, HashMultimap.<Integer, Integer>create());

        graph.putClientProperty(GRAPH_CONTEXT, context);

        return graph;
    }

    public void processResultSet(Graph graph, RelGraphViewDef graphDef, ResultSet rs,
            CacheRowSet cachedData, CsiScriptRunner scriptRunner, Bindings bindings,
            GraphProcessorListener listener, Map<String, Node> nodesOnRow, GraphTheme theme) throws SQLException, Exception, InstantiationException,
            IllegalAccessException {
        // NB: there are two inner loops to process all known specs for all
        // node and link specs refactor to break each one out into it's own
        // method!
        // Enables processing/creation of nodes independent of each other --
        // the tough issue is
        // determining which nodes were present on a specific node
        // efficiently, and vice-versa.
        int row;
        while (cachedData.nextRow()) {
            TaskHelper.checkForCancel();

            nodesOnRow.clear();
            row = rs.getInt(DATA_INTERNAL_ID);

            createNodesOnRow(graph, cachedData, nodesOnRow, row, graphDef, scriptRunner, bindings, listener, theme);

            List<LinkDef> linkSpecs = graphDef.getLinkDefs();

            createLinksOnRow(graph, linkSpecs, cachedData, nodesOnRow, row, scriptRunner, bindings, listener);
        }

        matchLegend(graph, graphDef);

        // boolean snaMetricsComputed = false;
        // NOTE: always compute SNA metrics so that we have them for the nodes list.
        // GraphMetrics.computeMetrics(GraphContext.Current.get().getGraphData());
        // snaMetricsComputed = true;

        // Previous logic:
    }

    public void createNodesAndLinks(Graph graph, SQLFactory sqlFactory, FilterActionsService filter) throws CentrifugeException {

        HashMap<String, Collection<Node>> nodeContainer = new HashMap<String, Collection<Node>>();
        Supplier<? extends Collection<Node>> nodeSetSupplier = new Supplier<Collection<Node>>() {

            @Override
            public Collection<Node> get() {
                return new HashSet<Node>(15);
            }
        };

        Multimap<String, Node> nodesByDefinition = Multimaps.newMultimap(nodeContainer, nodeSetSupplier);
        graph.putClientProperty(NODES_BY_DEFINITION, nodesByDefinition);
        getOrCreateLinksByDefMap(graph);

        String dvUuid = (String) graph.getClientProperty(DV_UUID);
        String viewDefUuid = (String) graph.getClientProperty(VIEWDEF_UUID);
//        String optionSetName = (String) graph.getClientProperty(OPTION_SET_NAME);
//        OptionSet optionSet = OptionSetManager.getOptionSet(optionSetName);

        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        if (dv == null) {
            throw new CentrifugeException("Dataview not found: " + dvUuid);
        }

        RelGraphViewDef graphDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, viewDefUuid);
        if (graphDef == null) {
            throw new CentrifugeException("A graph definition is required.");
        }

        GraphTheme theme = ThemeManager.getGraphTheme(graphDef.getThemeUuid());
        // ensure we clear out the selection when processing data.
        graphDef.setOldSelection(new SelectionModel());

        try {
            if(!Configuration.getInstance().getFeatureToggleConfig().isScriptingEnabled()){
                GraphQueryBuilder queryBuilder = createQueryBuilder(dv, graphDef, sqlFactory, filter);
                DataModelDef modelDef = dv.getMeta().getModelDef();
                CsiScriptRunner scriptRunner = new EcmaScriptRunner();
                Bindings bindings = scriptRunner.createBindings();
                GraphProcessorListener listener = null;
                Map<String, Node> nodesOnRow = new HashMap<String, Node>();

                queryBuilder.getQuery().scroll(new ScrollCallback<Void>() {

                    @Override
                    public Void scroll(ResultSet resultSet) throws SQLException {
                        try {
                            CacheRowSet cachedData = new CacheRowSet(modelDef.getFieldDefs(), resultSet);
                            bindings.put("csiRow", cachedData);
                            processResultSet(graph, graphDef, resultSet, cachedData, scriptRunner, bindings, listener, nodesOnRow, theme);
                        } catch (Exception e) {
                            throw(new SQLException(e));
                        }
                        return null;
                    }
                });

                completeGraphProcessing(graph, graphDef, theme, scriptRunner, bindings);

                TaskHelper.checkForCancel();

                LOG.debug(String.format("graph processing complete for dataview '%s'", dv.getName()));
            } else {
               try (Connection conn = CsiPersistenceManager.getCacheConnection();
                    ResultSet rs = getFilteredCacheData(conn, dv, graphDef)) {
//                DataCacheHelper cacheHelper = new DataCacheHelper();
//                rs = cacheHelper.getFilteredCacheData(conn, dv.getUuid(), graphDef, false);
                  processGraphResults(graph, graphDef, theme, dv, rs, null);
               }
            }
            Map<String, TypeInfo> legendInfo = (HashMap<String, TypeInfo>) graph.getClientProperty(NodeStore.NODE_LEGEND_INFO);
            Map<String, GraphLinkLegendItem> linkLegendInfo = (HashMap<String, GraphLinkLegendItem>) graph.getClientProperty(LinkStore.LINK_LEGEND_INFO);

            if((legendInfo != null) && (linkLegendInfo != null)) {
                if ((legendInfo.size() + linkLegendInfo.size()) > Configuration.getInstance().getGraphConfig().getTypeLimit()) {
                    TaskHelper.reportError("TOO_MANY_TYPES", null);
                    throw new GraphTooManyTypesException();
                }
            }
        } catch (CentrifugeException e) {
            throw e;
        } catch (Exception e) {
            String msg = String.format("An error was encountered while creating the graph for the Dataview '%s'",
                  dv.getName());
            throw new CentrifugeException(msg, e);
        }
    }

    private ResultSet getFilteredCacheData(Connection conn, DataView dv, RelGraphViewDef graphDef) throws CentrifugeException {
        DataCacheHelper cacheHelper = new DataCacheHelper();
        //                String filter = cacheHelper.getQueryFilter(dvUuid, graphDef);
        String filter = getQueryFilter(dv, graphDef, cacheHelper);
        return cacheHelper.getCacheData(conn, dv.getUuid(), null, filter, null, -1, -1, false);
    }

    private String getQueryFilter(DataView dv, RelGraphViewDef graphDef, DataCacheHelper cacheHelper) {
        BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(graphDef.getUuid());
        StringJoiner joiner = new StringJoiner(" AND ");
        String filterString = cacheHelper.buildTableSelectionFilterClause(broadcastResult.getBroadcastFilter(), broadcastResult.isExcludeRows(), dv);
        if (filterString.length() > 0) {
            joiner.add(filterString);
        }
        if (graphDef.getFilterUuid() != null) {
            filterString  = cacheHelper.buildFilterClause(graphDef, dv);
            if ((filterString != null) && (filterString.length() > 0)) {
                joiner.add(filterString);
            }
        }
        return joiner.toString();
    }

    private GraphQueryBuilder createQueryBuilder(DataView dv, RelGraphViewDef graphDef, SQLFactory sqlFactory, FilterActionsService filter) {
        GraphQueryBuilder queryBuilder = new GraphQueryBuilder();
        queryBuilder.setViewDef(graphDef);
        queryBuilder.setDataView(dv);
        queryBuilder.setSqlFactory(sqlFactory);
        queryBuilder.setFilterActionsService(filter);

        return queryBuilder;
    }

    public void completeGraphProcessing(Graph graph, RelGraphViewDef graphDef, GraphTheme theme,
            CsiScriptRunner scriptRunner, Bindings bindings) throws InstantiationException, IllegalAccessException {

        new NodePlunker().addNodesFromRelGraph(graphDef, graph);
        new LinkPlunker().addEdgesFromRelGraph(graphDef, graph);

        processAggregateAttributes(graphDef, graph);
        processAttributeReferences(graphDef, graph);

        TaskHelper.checkForCancel();
        List<NodeDef> nodeDefs = graphDef.getNodeDefs();
        boolean snaMetricsComputed = false;
        for (NodeDef nd : nodeDefs) {
            AttributeDef attr = nd.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE);
            if (attr == null) {
                continue;
            }
            if ((attr.getKind() == AttributeKind.REFERENCE) && (GraphMetrics.isMetricName(attr.getReferenceName()))) {
                GraphMetrics.computeMetrics(GraphContext.Current.get().getVisibleGraph());
                snaMetricsComputed = true;
                break;
            }
            attr = nd.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY);
            if (attr == null) {
                continue;
            }
            if ((attr.getKind() == AttributeKind.REFERENCE) && (GraphMetrics.isMetricName(attr.getReferenceName()))) {
                GraphMetrics.computeMetrics(GraphContext.Current.get().getVisibleGraph());
                snaMetricsComputed = true;
                break;
            }
        }

        applyVisualAttributes(graphDef, graph, scriptRunner, bindings, snaMetricsComputed, theme);

        boolean saveBeforeLayout = Configuration.getInstance().getGraphAdvConfig().saveBeforeLayout();
        if (saveBeforeLayout) {
            GraphDataManager.saveGraphData(GraphContext.Current.get());
        }
    }

    private void processGraphResults(Graph graph, RelGraphViewDef graphDef, GraphTheme theme, DataView dv,
            ResultSet rs, GraphProcessorListener listener) throws SQLException, Exception, InstantiationException,
            IllegalAccessException {

        DataModelDef modelDef = dv.getMeta().getModelDef();
        CacheRowSet cachedData = new CacheRowSet(modelDef.getFieldDefs(), rs);
        CsiScriptRunner scriptRunner = new EcmaScriptRunner();
        Bindings bindings = scriptRunner.createBindings();
        bindings.put("csiRow", cachedData);

        // NB: there are two inner loops to process all known specs for all
        // node and link specs refactor to break each one out into it's own
        // method!
        // Enables processing/creation of nodes independent of each other --
        // the tough issue is
        // determining which nodes were present on a specific node
        // efficiently, and vice-versa.
        Map<String, Node> nodesOnRow = new HashMap<String, Node>();
        int row;
        while (cachedData.nextRow()) {
            TaskHelper.checkForCancel();

            nodesOnRow.clear();
            row = rs.getInt(DATA_INTERNAL_ID);

            createNodesOnRow(graph, cachedData, nodesOnRow, row, graphDef, scriptRunner, bindings, listener, theme);

            List<LinkDef> linkSpecs = graphDef.getLinkDefs();

            createLinksOnRow(graph, linkSpecs, cachedData, nodesOnRow, row, scriptRunner, bindings, listener);
        }

        matchLegend(graph, graphDef);

//         Moved this up in the stack, it stopped prop up for some reason, orrr we don't have the links and stuff here.
//        HashMap<String, TypeInfo> legendInfo = (HashMap<String, TypeInfo>) graph.getClientProperty(NodeStore.NODE_LEGEND_INFO);
//        HashMap<String, GraphLinkLegendItem> linkLegendInfo = (HashMap<String, GraphLinkLegendItem>) graph.getClientProperty(LinkStore.LINK_LEGEND_INFO);
//        if(legendInfo.size() + linkLegendInfo.size() > Configuration.getInstance().getGraphConfig().getTypeLimit() ){
//            TaskHelper.reportError("TOO_MANY_TYPES",null);
//            throw new CentrifugeException("Type limit exceeded");
//        }

        processAggregateAttributes(graphDef, graph);
        processAttributeReferences(graphDef, graph);

        TaskHelper.checkForCancel();

        // boolean snaMetricsComputed = false;
        // NOTE: always compute SNA metrics so that we have them for the nodes list.
        // GraphMetrics.computeMetrics(GraphContext.Current.get().getGraphData());
        // snaMetricsComputed = true;

        completeGraphProcessing(graph, graphDef, theme, scriptRunner, bindings);

        TaskHelper.checkForCancel();

        LOG.debug(String.format("graph processing complete for dataview '%s'", dv.getName()));
    }

    private void matchLegend(Graph graph, RelGraphViewDef graphDef) {
        if(graphDef.getState() == null) {
            graphDef.setState(new GraphCachedState());
        }
        GraphTheme theme = null;
        try {
            theme = CsiPersistenceManager.getMetaEntityManager().find(GraphTheme.class, new CsiUUID(graphDef.getThemeUuid()));
        } catch (Exception e) {
        }

        HashMap<String, ArrayList> legendDecorations;
        if(graphDef.getState().getNodeLegendDecorations() == null) {
            legendDecorations = new LinkedHashMap<String, ArrayList>();
            graphDef.getState().setNodeLegendDecorations(legendDecorations);
        } else {
            legendDecorations = graphDef.getState().getNodeLegendDecorations();
        }

        HashMap<String, ArrayList> linkLegendDecorations;
        if(graphDef.getState().getLinkLegendDecorations() == null) {
            linkLegendDecorations = new LinkedHashMap<String, ArrayList>();
            graphDef.getState().setLinkLegendDecorations(linkLegendDecorations);
        } else {
            linkLegendDecorations = graphDef.getState().getLinkLegendDecorations();
        }
        {
            HashMap<String, TypeInfo> legendInfo = (HashMap<String, TypeInfo>) graph
                    .getClientProperty(NodeStore.NODE_LEGEND_INFO);

            for (Map.Entry<String,TypeInfo> entry : legendInfo.entrySet()) {
               TypeInfo typeInfo = entry.getValue();

               if (legendDecorations.containsKey(typeInfo.key)) {
                  typeInfo.color = (Integer) legendDecorations.get(typeInfo.key).get(1);
                  typeInfo.shape = ShapeType.valueOf((String) (legendDecorations.get(typeInfo.key)).get(0));
               }
            }
            GraphNodeLegendItem li = new GraphNodeLegendItem();
            if (theme != null) {
                Set<String> allNames = new HashSet<String>();
                for (NodeStyle nodeStyle : theme.getNodeStyles()) {
                    allNames.addAll(nodeStyle.getFieldNames());
                }
                for (Entry<String, TypeInfo> entry : legendInfo.entrySet().stream().filter(x -> !allNames.contains(x.getKey())).collect(Collectors.toList())) {
                    //seems odd... but correcting for difference in maps of legend info on the graph
                    li.color = entry.getValue().color;
                    preventColorsCloseToBackground(graphDef, li);
                    entry.getValue().color = Math.toIntExact(li.color);
                }
            }else{
                for (Entry<String, TypeInfo> entry : legendInfo.entrySet()) {
                    li.color = entry.getValue().color;
                    preventColorsCloseToBackground(graphDef, li);
                    entry.getValue().color = Math.toIntExact(li.color);

                }
            }
        }

        {
            HashMap<String, GraphLinkLegendItem> legendInfo= (HashMap<String, GraphLinkLegendItem>) graph
                .getClientProperty(LinkStore.LINK_LEGEND_INFO);

            for (Map.Entry<String,GraphLinkLegendItem> entry : legendInfo.entrySet()) {
               GraphLinkLegendItem typeInfo = entry.getValue();

               if (linkLegendDecorations.containsKey(typeInfo.key)) {
                  typeInfo.color = (Integer) linkLegendDecorations.get(typeInfo.key).get(1);
                  // typeInfo.shape = ShapeType.valueOf((String)
                  // (legendDecorations.get(typeInfo.key)).get(0));
               }
            }
            if (theme != null) {

                Set<String> allNames = new HashSet<String>();
                for (LinkStyle linkStyle : theme.getLinkStyles()) {
                    allNames.addAll(linkStyle.getFieldNames());
                }

                for (Entry<String, GraphLinkLegendItem> entry : legendInfo.entrySet().stream().filter(x -> !allNames.contains(x.getKey())).collect(Collectors.toList())) {
                    preventColorsCloseToBackground(graphDef, entry.getValue());
                }
            }else{
                for (Entry<String, GraphLinkLegendItem> entry : legendInfo.entrySet()) {
                    preventColorsCloseToBackground(graphDef, entry.getValue());
                }
            }
        }
    }

    public void preventColorsCloseToBackground(RelGraphViewDef graphDef, LegendItem typeInfo) {

        long intColor = 1;
        if (typeInfo instanceof GraphLinkLegendItem) {
            intColor = ((GraphLinkLegendItem) typeInfo).color;
        }
        if (typeInfo instanceof GraphNodeLegendItem) {
            intColor = ((GraphNodeLegendItem) typeInfo).color;

        }
        if (intColor != 16777216) {
            return;
        }
        intColor = ClientColorHelper.get().randomHueWheel().getIntColor();
        String value = graphDef.getSettings().getPropertiesMap().get("csi.relgraph.backgroundColor");
        if (value != null) {
            try {
                int color = Integer.parseInt(value);
                if (color == 16777215) { //TODO:looks like this matters
                    color = 16711422;
                }
                Color bgc = new Color(color);
                int retries = 10;
                //L = 0.2126 * Rg + 0.7152 * Gg + 0.0722 * Bg,
                float bgluminance = (float) ((.2126 * bgc.getRed()) + (.7152 * bgc.getGreen()) + (.0722 * bgc.getBlue())) / 255;
                while (retries > 0) {

                    ClientColorHelper.Color linkColor = ClientColorHelper.get().make((int)intColor);
                    float luminance = (float) ((.2126 * linkColor.getRed()) + (.7152 * linkColor.getGreen()) + (.0722 * linkColor.getBlue())) / 255;
                    float contrast = (float) Math.max((luminance + .05) / (bgluminance + .05), (bgluminance + .05) / (luminance + .05));
                    if (contrast < 3) {
                        intColor = ClientColorHelper.get().makeFromRGB((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)).getIntColor();
                    }
                    retries--;
                }
            } catch (Exception e) {

            }
        }
        if (typeInfo instanceof GraphLinkLegendItem) {
            ((GraphLinkLegendItem) typeInfo).color = intColor;
        }
        if (typeInfo instanceof GraphNodeLegendItem) {
            ((GraphNodeLegendItem) typeInfo).color = intColor;

        }
    }

    public boolean hasComponentizedLayout(GraphContext graphContext) {
        Boolean flag = (Boolean) graphContext.getGraphData().getClientProperty(
                GraphManager.LAYOUT_AND_COMPONENTS_COMPUTED_COMPLETE);
        return (flag != null) && flag.booleanValue();
    }

    // private void addPropertyValue(Map<String, Property> attributes, String key, Object value) {
    // Property property = attributes.get(key);
    // if (property == null) {
    // property = new Property(new QName(key));
    // attributes.put(key, property);
    // }
    //
    // if (!property.getValues().contains(value)) {
    // property.getValues().add(value);
    // }
    // }

   private double getSizeValue(Node node) {
      double value = 0.0d;
      NodeStore details = getNodeDetails(node);

      if (details != null) {
         Map<String, List<Integer>> rows = details.getRows();
         Iterator<Entry<String, List<Integer>>> iterator = rows.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<String, List<Integer>> entry = iterator.next();

            if (entry.getValue() != null) {
               value += entry.getValue().size();
            }
         }
      }
      return value;
   }

   private double getSizeValue(Edge edge) {
      double value = 0.0d;
      LinkStore details = getEdgeDetails(edge);

      if (details != null) {
         Map<String, List<Integer>> rows = details.getRows();
         Iterator<Entry<String, List<Integer>>> iterator = rows.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<String, List<Integer>> entry = iterator.next();

            if (entry.getValue() != null) {
               value += entry.getValue().size();
            }
         }
      }
      return value;
   }

    private void applyVisualAttributes(RelGraphViewDef viewDef, Graph graph, CsiScriptRunner scriptRunner,
            Bindings bindings, boolean snaMetricsComputed, GraphTheme theme) {

        VisualGraph vgraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);
        List<NodeDef> nodeDefs = viewDef.getNodeDefs();
        HashMap<String, DoubleRange> snaMetricRanges = new HashMap<String, DoubleRange>();

        // last node def in wins for now...regarding size
        Multimap<String, Node> nodesByDef = (Multimap<String, Node>) graph.getClientProperty(NODES_BY_DEFINITION);
        if (snaMetricsComputed) {
            computeComponents(graph);
            Set<String> componentsWithMoreThanOneNode = getComponentsWithMoreThanOneNode(graph);
            for (NodeDef def : nodeDefs) {
                Collection<Node> collection = (null != nodesByDef) ? nodesByDef.get(def.getName())
                        : new ArrayList<Node>();
                Set<String> snaKeys = GraphMetrics.metrics.keySet();
                for (Node node : collection) {
                    String comp = getSubGraphId(node);
                    if (!componentsWithMoreThanOneNode.contains(comp)) {
                        continue;
                    }
                    NodeStore details = GraphManager.getNodeDetails(node);

                    for (String refName : snaKeys) {
                        if (!refName.equalsIgnoreCase("degree")) {
                            DoubleRange range = snaMetricRanges.get(refName);
                            if (range == null) {
                                range = new DoubleRange();
                                range.factor = 5;
                                snaMetricRanges.put(refName, range);
                            }

                            Property property = details.getAttributes().get(refName);
                            if (property != null) {
                                Object o = property.getValues().get(0);
                                if (o instanceof Number) {
                                    Double d = ((Number) o).doubleValue();
                                    if (Double.isInfinite(d)) {
                                        if (refName.equals("Closeness")) {
                                            d = 0d;
                                        }
                                    }
                                    range.evaluate(comp, d);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (NodeDef def : nodeDefs) {
            TaskHelper.checkForCancel();
            {
                AttributeDef attributeDef = def.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE);
                if (attributeDef != null) {
                    setSize(vgraph, snaMetricRanges, nodesByDef, def, attributeDef);
                }
            }
            {
                AttributeDef attributeDef = def.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY);
                if (attributeDef != null) {
                    setTransparency(vgraph, snaMetricRanges, nodesByDef, def, attributeDef);
                }
            }
        }

        for (NodeDef def : nodeDefs) {
            TaskHelper.checkForCancel();

            Collection<Node> nodes = (null != nodesByDef) ? nodesByDef.get(def.getName()) : new ArrayList<Node>();
            processPostScript(vgraph, nodes, def, scriptRunner, bindings);
        }

        List<LinkDef> linkDefs = viewDef.getLinkDefs();
        Multimap<String, Edge> linksByDef = getOrCreateLinksByDefMap(graph);

        for (LinkDef linkDef : linkDefs) {
            TaskHelper.checkForCancel();

            calcLinkSize(vgraph, linksByDef, linkDef, theme);
            calcLinkTransparency(vgraph, linksByDef, linkDef);
            calcHighValueLinks(vgraph, linksByDef, linkDef);

            Collection<Edge> edges = linksByDef.get(linkDef.getUuid());
            processPostScript(vgraph, edges, linkDef, scriptRunner, bindings);
        }
    }

    private Set<String> getComponentsWithMoreThanOneNode(Graph graph) {
        Set<String> componentsWithMoreThanOneNode = new HashSet<String>();
        Object components = graph.getClientProperty(GraphConstants.COMPONENTS);
        if (components instanceof List) {
            List listOfSubGraphs = (List) components;
            int howMany = listOfSubGraphs.size();

            for (int i = 0; i < howMany; i++) {
                Object o = listOfSubGraphs.get(i);
                if (o instanceof Graph) {
                    Graph graph1 = (Graph) o;
                    if (graph1.getNodeCount() > 1) {
                        componentsWithMoreThanOneNode.add(i + "");
                    }
                }
            }
        }
        return componentsWithMoreThanOneNode;
    }

   private void setSize(VisualGraph vgraph, HashMap<String, DoubleRange> snaMetricRanges,
                        Multimap<String, Node> nodesByDef, NodeDef def, AttributeDef attributeDef) {
      Collection<Node> collection = (null != nodesByDef) ? nodesByDef.get(def.getName()) : new ArrayList<Node>();

      if (attributeDef.getKind() == AttributeKind.REFERENCE) {
         String refName = attributeDef.getReferenceName();
         Function<Node, Double> getter = getNodeAccessorFor(def, refName);

         if (getter != null) {
            DoubleRange range = new DoubleRange();

            range.factor = 5;

            if (!GraphMetrics.isMetricName(refName)) {
               for (Node node : collection) {
                  TaskHelper.checkForCancel();
                  range.evaluate(getter.apply(node));
               }
            }
            for (Node node : collection) {
               TaskHelper.checkForCancel();

               NodeStore details = GraphManager.getNodeDetails(node);

               if (details != null) {
                  double value = 1.0d;

                  if (GraphMetrics.isMetricName(refName)) {
                     range = snaMetricRanges.get(refName);

                     if (range != null) {
                        value = range.normalize(getSubGraphId(node), getter.apply(node));
                     }
                  } else {
                     if (range != null) {
                        value = range.normalize(getter.apply(node));
                     }
                  }
                  details.setRelativeSize(value);

                  if (vgraph != null) {
                     VisualItem vi = (VisualItem) vgraph.getNode(node.getRow());

                     vi.setSize(details.getRelativeSize());
                  }
               }
            }
         }
         return;
      }

        FieldDef fieldDef = attributeDef.getFieldDef();
        if ((fieldDef != null) && (fieldDef.getFieldType() == FieldType.STATIC)) {
            String staticText = fieldDef.getStaticText();

            double value = 1.0d;

            if (staticText != null) {
                try {
                    value = Double.valueOf(staticText);
                } catch (NumberFormatException e) {
                }

                if (Double.isNaN(value)) {
                    value = 1.0d;
                }
            }

            for (Node node : collection) {
                TaskHelper.checkForCancel();

                NodeStore details = getNodeDetails(node);

                if (details != null) {
                   details.setRelativeSize(value);

                   if (vgraph != null) {
                      VisualItem vi = (VisualItem) vgraph.getNode(node.getRow());

                      vi.setSize(details.getRelativeSize());
                   }
                }
            }

        } else {
            //This case is graph Metric occurrence???

            double value;
            DoubleRange range = new DoubleRange();
            range.factor = 3.0d;
            Iterator<Node> nodes = collection.iterator();
            while (nodes.hasNext()) {
                TaskHelper.checkForCancel();

                Node node = nodes.next();
                value = getSizeValue(node);
                range.evaluate(value);
            }

            nodes = collection.iterator();
            while (nodes.hasNext()) {
                TaskHelper.checkForCancel();

                Node node = nodes.next();
                NodeStore details = getNodeDetails(node);

                if (details != null) {
                   details.setRelativeSize(range.normalize(getSizeValue(node)));

                   if (vgraph != null) {
                      VisualItem vi = (VisualItem) vgraph.getNode(node.getRow());

                      vi.setSize(details.getRelativeSize());
                   }
                }
            }
        }
    }

    private void setTransparency(VisualGraph vgraph, HashMap<String, DoubleRange> snaMetricRanges, Multimap<String, Node> nodesByDef, NodeDef def, AttributeDef attributeDef) {
        Collection<Node> collection = (null != nodesByDef) ? nodesByDef.get(def.getName()) : new ArrayList<Node>();

        if (attributeDef.getKind() == AttributeKind.REFERENCE) {
            String refName = attributeDef.getReferenceName();
            Function<Node, Double> getter = getNodeAccessorFor(def, refName);

            if (getter != null) {
               DoubleRange range = new DoubleRange();
               range.factor = .8;
               range.base = .20;

               if (!GraphMetrics.isMetricName(refName)) {
                  for (Node node : collection) {
                     TaskHelper.checkForCancel();
                     range.evaluate(getter.apply(node));
                  }
               }
               for (Node node : collection) {
                  TaskHelper.checkForCancel();

                  NodeStore details = GraphManager.getNodeDetails(node);
                  double value = 1.0d;

                  if (GraphMetrics.isMetricName(refName)) {
                     range = snaMetricRanges.get(refName);

                     if (range != null) {
                        value = range.normalize(getSubGraphId(node), getter.apply(node));
                     }
                  } else {
                     if (range != null) {
                        value = range.normalize(getter.apply(node));
                     }
                  }
                  details.setTransparency(value * 100);
               }
            }
            return;
        }

        FieldDef fieldDef = attributeDef.getFieldDef();
        if (fieldDef.getFieldType() == FieldType.STATIC) {
            String staticText = fieldDef.getStaticText();

            double value = 100.0d;

            if (staticText != null) {
                try {
                    value = Double.valueOf(staticText);
                } catch (NumberFormatException e) {
                }

                if (Double.isNaN(value)) {
                    value = 100.0d;
                }
            }

            for (Node node : collection) {
                TaskHelper.checkForCancel();

                NodeStore details = getNodeDetails(node);

                if (details != null) {
                   details.setTransparency(value);
                }
            }
            return;
        }
        {
            //This case is graph Metric occurrence???

            double value;
            DoubleRange range = new DoubleRange();
            range.factor = 1.0d;
            Iterator<Node> nodes = collection.iterator();
            while (nodes.hasNext()) {
                TaskHelper.checkForCancel();

                Node node = nodes.next();
                value = getSizeValue(node);//calc occurance
                range.evaluate(value);
            }

            nodes = collection.iterator();
            while (nodes.hasNext()) {
                TaskHelper.checkForCancel();

                Node node = nodes.next();
                NodeStore details = getNodeDetails(node);

                if (details != null) {
                   details.setTransparency(range.normalize(getSizeValue(node)) * 100);
                }
            }
        }
    }

    private String getSubGraphId(Node node) {
//        NodeStore details = GraphManager.getNodeDetails(node);
        //Property property = details.getAttributes().get("subGraphId");

        int property = node.getInt(GraphConstants.COMPONENT_ID);
        //        if (property != null) {
        //            return property.getValues().get(0).toString();
        //
        //        }
        return "" + property;
    }

    private void processPostScript(VisualGraph vgraph, Collection<Node> nodes, NodeDef nodeDef,
            CsiScriptRunner scriptRunner, Bindings bindings) {
        String expr = null;

        if (nodes == null) {
            return;
        }

        AttributeDef attribute = nodeDef.getAttributeDef(ObjectAttributes.CSI_POST_SCRIPT);
        if (attribute == null) {
            return;
        }

        FieldDef f = attribute.getFieldDef();
        if ((f != null) && (f.getFieldType() == FieldType.STATIC)) {
            expr = f.getStaticText();
        }

        if ((expr == null) || expr.trim().isEmpty()) {
            return;
        }

        for (Node node : nodes) {
            TaskHelper.checkForCancel();

            NodeStore nodeStore = getNodeDetails(node);

            if (nodeStore != null) {
               VisualItem vitem = (VisualItem) vgraph.getNode(node.getRow());

               try {
                  bindings.put("node", node);
                  bindings.put("nodeStore", nodeStore);
                  bindings.put("visual", vitem);

                  scriptRunner.evalExpression(expr, bindings);
               } catch (CentrifugeException e) {
                  LOG.warn("Failed to evaluate post processing script for node: " + nodeStore.getLabel(), e);
               } finally {
                  bindings.remove("node");
                  bindings.remove("nodeStore");
                  bindings.remove("visual");
               }
            }
        }
    }

    private void processPostScript(VisualGraph vgraph, Collection<Edge> edges, LinkDef linkDef,
            CsiScriptRunner scriptRunner, Bindings bindings) {
        String expr = null;

        if (edges == null) {
            return;
        }

        AttributeDef attribute = linkDef.getAttributeDef(ObjectAttributes.CSI_POST_SCRIPT);
        if (attribute == null) {
            return;
        }

        FieldDef f = attribute.getFieldDef();
        if ((f != null) && (f.getFieldType() == FieldType.STATIC)) {
            expr = f.getStaticText();
        }

        if ((expr == null) || expr.trim().isEmpty()) {
            return;
        }

        for (Edge edge : edges) {
            TaskHelper.checkForCancel();

            LinkStore linkStore = getEdgeDetails(edge);
            try {
                bindings.put("link", edge);
                bindings.put("linkStore", linkStore);
                bindings.put("visual", vgraph.getEdge(edge.getRow()));

                scriptRunner.evalExpression(expr, bindings);
            } catch (CentrifugeException e) {
                LOG.warn("Failed to evaluate post processing script for link: " + linkStore.getLabel(), e);
            } finally {
                bindings.remove("link");
                bindings.remove("linkStore");
                bindings.remove("visual");
            }
        }
    }

    private Function<Edge, Double> getLinkAccessorFor(LinkDef def, String referenceName) {
        final String name = (referenceName == null) ? "" : referenceName.trim().toLowerCase();

        if (name.equalsIgnoreCase("weight") || name.equalsIgnoreCase("count")) {
            return new Function<Edge, Double>() {

                @Override
                public Double apply(Edge edge) {
                    LinkStore details = getEdgeDetails(edge);
                    return Double.valueOf((details == null) ? 0D : details.getWeight());
                }
            };
        } else {
            final String propName = referenceName;
            return new Function<Edge, Double>() {

                @Override
                public Double apply(Edge edge) {
                    LinkStore details = getEdgeDetails(edge);

                    if (details != null) {
                       Property property = details.getAttributes().get(propName);

                       if (property != null) {
                          Object o = property.getValues().get(0);

                          if (o instanceof Number) {
                             return ((Number) o).doubleValue();
                          } else {
                             return 0.0d;
                          }
                       } else {
                          return 0.0d;
                       }
                    } else {
                       return 0.0d;
                    }
                }
            };
        }
    }

    private Function<Node, Double> getNodeAccessorFor(NodeDef def, String referenceName) {
        final String name = (referenceName == null) ? "" : referenceName.trim().toLowerCase();
        if (name.equals("degree") || name.equalsIgnoreCase("number of neighbors")) {
            return new Function<Node, Double>() {

                @Override
                public Double apply(Node node) {
                    return (double) node.getDegree();
                }
            };
        } else if (name.equals("weight") || name.equalsIgnoreCase("count")) {
            return new Function<Node, Double>() {

                @Override
                public Double apply(Node node) {
                    NodeStore details = GraphManager.getNodeDetails(node);
                    return Double.valueOf((details == null) ? 0D : details.getWeight());
                }
            };
        } else if (name.equals("eigenvector") || name.equals("betweenness") || name.equals("closeness")) {
            final String refName = referenceName;
            return new Function<Node, Double>() {

                @Override
                public Double apply(Node node) {
                    NodeStore details = GraphManager.getNodeDetails(node);
                    Double d = Double.valueOf(0.0d);

                    if (details != null) {
                       Property property = details.getAttributes().get(refName);

                       if ((property != null) && !property.getValues().isEmpty()) {
                          Object o = property.getValues().get(0);

                          if (o instanceof Number) {
                             d = ((Number) o).doubleValue();
                          }
                        // else {
                        // return 0.0d;
                        // }
                       }
                    }
                    return d;
                }
            };
        } else {
            AttributeDef attributeDef = def.getAttributeDef(referenceName);
            if (attributeDef != null) {
                final String propName = referenceName;
                return new Function<Node, Double>() {

                    @Override
                    public Double apply(Node node) {
                        NodeStore details = GraphManager.getNodeDetails(node);

                        if (details != null) {
                           Property property = details.getAttributes().get(propName);
                           Object o = property.getValues().get(0);

                           if (o instanceof Number) {
                              return ((Number) o).doubleValue();
                           } else {
                              return 0.0d;
                           }
                        } else {
                           return 0.0d;
                        }
                    }
                };
            }
        }
        return null;
    }

    private void calcHighValueLinks(VisualGraph vgraph, Multimap<String, Edge> linksByDef, LinkDef linkDef) {
        AttributeDef highValueAttr = linkDef.getAttributeDef("highValue");
        AttributeDef highValueLimit = linkDef.getAttributeDef("highValueLimit");
        AttributeDef highValueColor = linkDef.getAttributeDef("highValueColor");
        if (highValueAttr == null) {
            return;
        }

        if (highValueLimit == null) {
            LOG.warn("Invalid highValueLimit");
            return;
        }

        float limit = 1;
        FieldDef f = highValueLimit.getFieldDef();
        if (f.getFieldType() == FieldType.STATIC) {
            limit = Float.parseFloat(f.getStaticText());
        }

        Integer intColor = null;
        if (highValueColor != null) {
            Matcher colorMatcher = COLOR_PATTERN.matcher(highValueColor.getFieldDef().getStaticText());

            if (colorMatcher.matches()) {
                int r = Integer.parseInt(colorMatcher.group(1));
                int g = Integer.parseInt(colorMatcher.group(2));
                int b = Integer.parseInt(colorMatcher.group(3));
                int a = Integer.parseInt(colorMatcher.group(4));
                Color color = ColorLib.getColor(r, g, b, a);
                intColor = Integer.valueOf(makeColorAsInt(color));
            }
        }

        Collection<Edge> links = linksByDef.get(linkDef.getUuid());
        Iterator<Edge> linkIter = links.iterator();
        while (linkIter.hasNext()) {
            TaskHelper.checkForCancel();

            Edge edge = linkIter.next();
            LinkStore details = getEdgeDetails(edge);

            if (details != null) {
               Map<String, Property> attributes = details.getAttributes();
               Property property = attributes.get("highValue");
               List<Object> values = property.getValues();
               boolean isHighValue = false;

               for (Object object : values) {
                  TaskHelper.checkForCancel();

                  if (object == null) {
                     continue;
                  }
                  Double v = Double.parseDouble(object.toString());

                  if (Math.abs(v) > limit) {
                     isHighValue = true;
                     break;
                  }
               }
               setAttributeValue(attributes, "isHighValue", Boolean.valueOf(isHighValue));
               setAttributeValue(attributes, "highValueColor", intColor);
            }
        }
    }

    private void setAttributeValue(Map<String, Property> attributes, String name, Object value) {
        setAttributeValue(attributes, name, value, false);
    }

    private void setAttributeValue(Map<String, Property> attributes, String name, Object value, boolean tooltip) {
        Property prop = attributes.get(name);
        if (prop == null) {
            prop = new Property(name);
            prop.setIncludeInTooltip(tooltip);
            attributes.put(name, prop);
        }
        prop.getValues().clear();
        if (value != null) {
            prop.getValues().add(value);
        }
    }

    private int makeColorAsInt(Color color) {
        return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    private void calcLinkSize(VisualGraph vgraph, Multimap<String, Edge> linksByDef, LinkDef linkDef, GraphTheme theme) {
        Collection<Edge> edges = linksByDef.get(linkDef.getUuid());
        Set<String> unknownTypes = new HashSet<String>();
        Map<String,LinkStyle> typeStyleMap = new HashMap<String, LinkStyle>();
        boolean widthOverride = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_WIDTH_OVERRIDE) != null;

        if (linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE) != null) {
            AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE);

            if (attributeDef.getReferenceName() != null) {
                // process referenced attribute...

                Function<Edge, Double> getter = getLinkAccessorFor(linkDef, attributeDef.getReferenceName());
                Function<Edge, LinkStore> linkDetailsGetter = new Function<Edge, LinkStore>() {
                   @Override
                   public LinkStore apply(Edge edge) {
                      return getEdgeDetails(edge);
                   }
                };
                DoubleRange range = new DoubleRange();
                range.factor = 10.0d;

                for (Edge edge : edges) {
                   TaskHelper.checkForCancel();

                   double value = getter.apply(edge);
                   range.evaluate(value);
                }
                if (vgraph != null) {
                   for (Edge edge : edges) {
                      TaskHelper.checkForCancel();

                      LinkStore details = linkDetailsGetter.apply(edge);

                      if (details != null) {
                         double sizeValue = getter.apply(edge);
                         VisualItem vi = (VisualItem) vgraph.getEdge(edge.getRow());
                         double normalized = range.normalize(sizeValue);

                         vi.setSize(normalized);
                         details.setWidth((int) normalized);
                      }
                   }
                }
                return;
            }

            FieldDef fieldDef = attributeDef.getFieldDef();

            if (fieldDef.getFieldType() == FieldType.STATIC) {

                String staticText = fieldDef.getStaticText();
                if (staticText != null) {
                    Matcher countDistinctMatcher = COUNT_PATTERN.matcher(staticText.trim());
                    if (countDistinctMatcher.matches()) {
                        String nodeDefName = countDistinctMatcher.group(1);
                        handleCountDistinctBy(vgraph, linksByDef, linkDef, nodeDefName);

                    } else {
                        double value = 1.0d;
                        if(widthOverride){
                            try {
                                value = Double.valueOf(staticText);
                            } catch (NumberFormatException e) {
                            }
                        } else {

                        }

                        if (Double.isNaN(value)) {
                            value = 1.0d;
                        }

                        if (vgraph != null) {
                            for (Edge edge : edges) {
                                TaskHelper.checkForCancel();

                                EdgeItem ei = (EdgeItem) vgraph.getEdge(edge.getRow());
                                // LinkStore details = (LinkStore) ei.get(GraphConstants.LINK_DETAIL);
                                LinkStore details = GraphManager.getEdgeDetails(ei);
                                String type = details.getFirstType();

                                details.setWidth( value);
                                ei.setSize(value);

                                if(!widthOverride && (type != null) && (theme != null)){
                                    LinkStyle style = typeStyleMap.get(type);
                                    if((style == null) && !unknownTypes.contains(type)){
                                        style = theme.findLinkStyle(type);

                                        if(style == null){
                                            unknownTypes.add(type);
                                        }
                                    }

                                    if((style != null) && (style.getWidth() != null)){

                                        details.setWidth(style.getWidth());
                                        ei.setSize(style.getWidth());
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                double value;
                DoubleRange range = new DoubleRange();
                range.factor = 10.0d;
                for (Edge edge : edges) {
                    TaskHelper.checkForCancel();

                    value = getSizeValue(edge);
                    range.evaluate(value);
                }

                if (vgraph != null) {
                    for (Edge edge : edges) {
                        TaskHelper.checkForCancel();

                        double sizeValue = getSizeValue(edge);
                        VisualItem vi = (VisualItem) vgraph.getEdge(edge.getRow());
                        double normalized = range.normalize(sizeValue);
                        vi.setSize(normalized);

                        // LinkStore details = (LinkStore) vi.get(GraphConstants.LINK_DETAIL);
                        LinkStore details = GraphManager.getEdgeDetails(vi);
                        details.setWidth((int) normalized);
                    }
                }
            }

        }
    }

    // private void saveEdgeData(Edge edge, LinkDef linkDef, Object labelValue, Object linkType, Map<String, Object> attrs) {
    // DBObject query = buildEdgeQuery(edge);
    // DBObject data = graphStorage.findEdge(query);
    // if( data == null ) {
    // data = Helper.buildData();
    // }
    //
    // if (data.containsField(GraphConstants.LABEL) == false && labelValue != null) {
    // data.put(GraphConstants.LABEL, labelValue.toString());
    // }
    //
    // DBObject internalPayload = Helper.getInternalPayload(data);
    // List defs = (List) internalPayload.get("defs");
    // if (defs == null) {
    // defs = new BasicDBList();
    // internalPayload.put("defs", defs);
    // }
    //
    // if (!defs.contains(linkDef.getName())) {
    // defs.add(linkDef.getName());
    // }
    //
    //
    // Set<String> keys = attrs.keySet();
    // for (String key : keys) {
    // if (key.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
    // continue;
    //
    // }
    //
    // Object value = attrs.get(key);
    // if (!data.containsField(key)) {
    // data.put(key, value);
    // } else {
    // List l;
    // if (data.get(key) instanceof List) {
    // l = (List) data.get(key);
    // } else {
    // l = new BasicDBList();
    // l.add(data.get(key));
    // data.put(key, l);
    // }
    //
    // if (!l.contains(value)) {
    // l.add(value);
    // }
    // }
    // }
    //
    // graphStorage.updateEdge(data);
    //
    // }

    private void calcLinkTransparency(VisualGraph vgraph, Multimap<String, Edge> linksByDef, LinkDef linkDef) {
        Collection<Edge> edges = linksByDef.get(linkDef.getUuid());

        if (linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY) != null) {
            AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY);

            if (attributeDef.getReferenceName() != null) {
                // process referenced attribute...

                Function<Edge, Double> getter = getLinkAccessorFor(linkDef, attributeDef.getReferenceName());
                Function<Edge, LinkStore> linkDetailsGetter = new Function<Edge, LinkStore>() {
                    @Override
                    public LinkStore apply(Edge edge) {
                        return getEdgeDetails(edge);
                    }
                };
                DoubleRange range = new DoubleRange();
                range.factor = .8;
                range.base = .2;

                for (Edge edge : edges) {
                    TaskHelper.checkForCancel();

                    double value = getter.apply(edge);
                    range.evaluate(value);
                }

                if (vgraph != null) {
                    for (Edge edge : edges) {
                        TaskHelper.checkForCancel();

                        LinkStore details = linkDetailsGetter.apply(edge);

                        if (details != null) {
                           double sizeValue = getter.apply(edge);
                           VisualItem vi = (VisualItem) vgraph.getEdge(edge.getRow());
                           double normalized = range.normalize(sizeValue);

                           vi.setSize(normalized);
                        //                        details.setWidth((int) normalized);
                           details.setTransparency(normalized * 100);
                        }
                    }
                }
                return;
            }

            FieldDef fieldDef = attributeDef.getFieldDef();

            if (fieldDef.getFieldType() == FieldType.STATIC) {

                String staticText = fieldDef.getStaticText();
                if (staticText != null) {
                    Matcher countDistinctMatcher = COUNT_PATTERN.matcher(staticText.trim());
                    if (countDistinctMatcher.matches()) {
                        String nodeDefName = countDistinctMatcher.group(1);
                        handleCountDistinctBy(vgraph, linksByDef, linkDef, nodeDefName);

                    } else {
                        double value = 100.0d;
                        try {
                            value = Double.valueOf(staticText);
                        } catch (NumberFormatException e) {
                        }

                        if (Double.isNaN(value)) {
                            value = 100.0d;
                        }

                        if (vgraph != null) {
                            for (Edge edge : edges) {
                                TaskHelper.checkForCancel();

                                EdgeItem ei = (EdgeItem) vgraph.getEdge(edge.getRow());
                                // LinkStore details = (LinkStore) ei.get(GraphConstants.LINK_DETAIL);
                                LinkStore details = GraphManager.getEdgeDetails(ei);
                                //                                details.setWidth((int) value);

                                if (details != null) {
                                   details.setTransparency(value);
                                }
                            }
                        }
                    }
                }

            } else {
                double value;
                DoubleRange range = new DoubleRange();
                range.factor = .8;
                range.base = .2;
                for (Edge edge : edges) {
                    TaskHelper.checkForCancel();

                    value = getSizeValue(edge);
                    range.evaluate(value);
                }

                if (vgraph != null) {
                    for (Edge edge : edges) {
                        TaskHelper.checkForCancel();

                        double sizeValue = getSizeValue(edge);
                        VisualItem vi = (VisualItem) vgraph.getEdge(edge.getRow());
                        double normalized = range.normalize(sizeValue);
                        vi.setSize(normalized);

                        // LinkStore details = (LinkStore) vi.get(GraphConstants.LINK_DETAIL);
                        LinkStore details = GraphManager.getEdgeDetails(vi);

                        if (details != null) {
                           details.setTransparency(normalized * 100);
                        }
                    }
                }
            }
        }
    }

    private void handleCountDistinctBy(VisualGraph vgraph, Multimap<String, Edge> linksByDef, LinkDef linkDef,
            String nodeDefName) {
        NodeDef byDef = null;
        if (linkDef.getNodeDef1().getName().equalsIgnoreCase(nodeDefName)) {
            byDef = linkDef.getNodeDef1();
        } else {
            byDef = linkDef.getNodeDef2();
        }

        HashMap<Node, Set<Edge>> edgeByNodes = new HashMap<Node, Set<Edge>>();

        Collection<Edge> links = linksByDef.get(linkDef.getUuid());
        Iterator<Edge> linkIter = links.iterator();
        while (linkIter.hasNext()) {
            TaskHelper.checkForCancel();

            Edge edge = linkIter.next();
            Node keyNode = null;
            if (isNodeOfType(edge.getSourceNode(), byDef)) {
                keyNode = edge.getSourceNode();
            } else {
                keyNode = edge.getTargetNode();
            }

            Set<Edge> edgeset = edgeByNodes.get(keyNode);
            if (edgeset == null) {
                edgeset = new HashSet<Edge>();
                edgeByNodes.put(keyNode, edgeset);
            }
            edgeset.add(edge);
        }

        Set<Node> nodeSet = edgeByNodes.keySet();
        for (Node keyNode : nodeSet) {
            TaskHelper.checkForCancel();

            DoubleRange range = new DoubleRange();
            range.factor = 10.0d;
            Set<Edge> edgeSet = edgeByNodes.get(keyNode);
            HashMap<Edge, Integer> countMap = new HashMap<Edge, Integer>();
            for (Edge edge : edgeSet) {
                TaskHelper.checkForCancel();

                LinkStore details = getEdgeDetails(edge);
                int count = 0;

                if (details != null) {
                   List<Integer> list = details.getRows().get(linkDef.getUuid());

                   if (list != null) {
                      count = list.size();
                   }
                }
                range.evaluate(count);
                countMap.put(edge, count);
            }

            for (Edge edg : edgeSet) {
                TaskHelper.checkForCancel();

                Integer size = countMap.get(edg);
                VisualItem vi = (VisualItem) vgraph.getEdge(edg.getRow());
                // LinkStore details = (LinkStore) vi.get(GraphConstants.LINK_DETAIL);
                LinkStore details = getEdgeDetails(vi);

                if (details != null) {
                   details.setWidth((int) range.normalize(size));
                }
                vi.setSize(range.normalize(size));
            }
        }
    }

    private boolean isNodeOfType(Node node, NodeDef type) {
        NodeStore details = getNodeDetails(node);
        return (details != null) && details.getRows().containsKey(type);
    }

    private void processAggregateAttributes(RelGraphViewDef modelDef, Graph graph) throws InstantiationException,
            IllegalAccessException {
        List<NodeDef> nodeDefs = modelDef.getNodeDefs();

        Multimap<String, AttributeDef> aggregates = ArrayListMultimap.create();
        for (NodeDef def : nodeDefs) {
            for (AttributeDef attr : def.getAttributeDefs()) {

                boolean isComputed = isComputed(attr);
                if (isComputed) {
                    aggregates.put(def.getName(), attr);
                }

            }
        }

        Multimap<String, Node> nodesByDef = (Multimap<String, Node>) graph.getClientProperty(NODES_BY_DEFINITION);

        Set<Node> visitedNodes = new HashSet<Node>();

        if ((null != nodesByDef) && !nodesByDef.isEmpty()) {

            for (String nodeDefName : aggregates.keySet()) {
                TaskHelper.checkForCancel();

                Collection<Node> nodeSet = nodesByDef.get(nodeDefName);
                for (Node node : nodeSet) {
                    TaskHelper.checkForCancel();

                    if (visitedNodes.contains(node)) {
                        continue;
                    }

                    visitedNodes.add(node);
                    NodeStore details = getNodeDetails(node);

                    if (details != null) {
                       Map<String, Property> attributes = details.getAttributes();
                       Map<String, List<Integer>> supporting = details.getRows();
                       Set<String> templateNames = supporting.keySet();

                       processObjectAggregateAttributes(aggregates, attributes, templateNames);
                    }
                }
            }
        }
        aggregates = ArrayListMultimap.create();
        Multimap<String, AttributeDef> aggregatesNames = ArrayListMultimap.create();
        Multimap<String, AttributeDef> linkCountByAggregates = ArrayListMultimap.create();
        List<LinkDef> linkDefs = modelDef.getLinkDefs();

        for (LinkDef def : linkDefs) {
            for (AttributeDef attr : def.getAttributeDefs()) {
                if (hasValidComputation(attr)) {
                    // if (attr.getAggregateFunction() != null) {
                    aggregates.put(def.getUuid(), attr);
                    aggregatesNames.put(def.getName(), attr);
                } else if (attr.getName().startsWith("countBy")) {
                    //FIXME: does this do anything?
                    linkCountByAggregates.put(def.getName(), attr);
                }
            }
        }
        Set<Edge> visitedEdges = new HashSet<Edge>();
        Multimap<String, Edge> linksByDef = getOrCreateLinksByDefMap(graph);

        for (String uuid : aggregates.keySet()) {
            TaskHelper.checkForCancel();

            Collection<Edge> edgeSet = linksByDef.get(uuid);
            for (Edge edge : edgeSet) {
                TaskHelper.checkForCancel();

                if (visitedEdges.contains(edge)) {
                    continue;
                }

                visitedEdges.add(edge);
                LinkStore details = getEdgeDetails(edge);

                if (details != null) {
                   Map<String, Property> attributes = details.getAttributes();
                   Map<String, List<Integer>> supporting = details.getRows();
                   Set<String> templateNames = supporting.keySet();

                   processObjectAggregateAttributes(aggregatesNames, attributes, templateNames);
                }
            }
        }
    }

   private boolean isComputed(AttributeDef attr) {
      return ((attr.getKind() == AttributeKind.COMPUTED) && (attr.getAggregateFunction() != null));
   }

   private void processAttributeReferences(RelGraphViewDef graphDef, Graph graph) {
      Multimap<String, AttributeDef> aggregates = ArrayListMultimap.create();

      Predicate<AttributeDef> isAttrRef = new Predicate<AttributeDef>() {
         @Override
         public boolean test(AttributeDef attribute) {
            return ((attribute.getKind() == AttributeKind.REFERENCE) && (attribute.getReferenceName() != null));
         }
      };

      for (NodeDef def : graphDef.getNodeDefs()) {
         for (AttributeDef attr : def.getAttributeDefs()) {
            if (isAttrRef.test(attr)) {
               aggregates.put(def.getName(), attr);
            }
         }
      }

        Set<Node> visitedNodes = new HashSet<Node>();

        Multimap<String, Node> nodesByDef = (Multimap<String, Node>) graph.getClientProperty(NODES_BY_DEFINITION);

        if ((null != nodesByDef) && !nodesByDef.isEmpty()) {

            for (String nodeDefName : aggregates.keySet()) {
                TaskHelper.checkForCancel();

                Collection<Node> nodeSet = nodesByDef.get(nodeDefName);
                for (Node node : nodeSet) {
                    TaskHelper.checkForCancel();

                    if (visitedNodes.contains(node)) {
                        continue;
                    }

                    visitedNodes.add(node);
                    NodeStore details = getNodeDetails(node);
                    Map<String, Property> attributes = details.getAttributes();
                    Map<String, List<Integer>> supporting = details.getRows();
                    Set<String> templateNames = supporting.keySet();
                    processAttributeReferences(aggregates, attributes, templateNames);
                }
            }
        }

        aggregates = ArrayListMultimap.create();
        Multimap<String, AttributeDef> linkCountByAggregates = ArrayListMultimap.create();

        List<LinkDef> linkDefs = graphDef.getLinkDefs();

        for (LinkDef def : linkDefs) {
           for (AttributeDef attr : def.getAttributeDefs()) {
              if (isAttrRef.test(attr)) {
                 aggregates.put(def.getUuid(), attr);
              } else if (attr.getName().startsWith("countBy")) {
                 linkCountByAggregates.put(def.getName(), attr);
              }
           }
        }
        Set<Edge> visitedEdges = new HashSet<Edge>();
        Multimap<String, Edge> linksByDef = getOrCreateLinksByDefMap(graph);

        if (linksByDef != null) {

            for (String uuid : aggregates.keySet()) {
                TaskHelper.checkForCancel();

                Collection<Edge> edgeSet = linksByDef.get(uuid);
                for (Edge edge : edgeSet) {
                    TaskHelper.checkForCancel();

                    if (visitedEdges.contains(edge)) {
                        continue;
                    }

                    visitedEdges.add(edge);
                    LinkStore details = getEdgeDetails(edge);
                    Map<String, Property> attributes = details.getAttributes();
                    Map<String, List<Integer>> supporting = details.getRows();
                    Set<String> templateNames = supporting.keySet();
                    processAttributeReferences(aggregates, attributes, templateNames);
                }
            }
        }
    }

    private void processAttributeReferences(Multimap<String, AttributeDef> aggregates,
            Map<String, Property> attributes, Set<String> templateNames) {
        for (String name : templateNames) {
            TaskHelper.checkForCancel();

            if (aggregates.containsKey(name)) {
                Collection<AttributeDef> attrs = aggregates.get(name);

                for (AttributeDef attr : attrs) {
                    TaskHelper.checkForCancel();

                    String referenceName = attr.getReferenceName();
                    Property propRef = attributes.get(referenceName);
                    if (propRef == null) {
                        continue;
                    }

                    ProxiedProperty property = new ProxiedProperty(attr.getName());
                    property.setReference(propRef);
                    attributes.put(attr.getName(), property);
                }

            }
        }
    }

    private boolean hasValidComputation(AttributeDef attr) {
        boolean valid= false;
        if (attr == null) {
            valid = false;
        } else if ((attr.getKind() == AttributeKind.COMPUTED) && (attr.getAggregateFunction() != null)) {
            valid = true;
        }
        return valid;
    }

    private void processObjectAggregateAttributes(Multimap<String, AttributeDef> aggregateAttributes,
            Map<String, Property> attributes, Set<String> templateNames) throws InstantiationException,
            IllegalAccessException {
        for (String name : templateNames) {
            TaskHelper.checkForCancel();

            if (aggregateAttributes.containsKey(name)) {
                Collection<AttributeDef> attrs = aggregateAttributes.get(name);

                for (AttributeDef attributeDef : attrs) {
                    TaskHelper.checkForCancel();

                    String attributeName = attributeDef.getName();
                    String forwardKey = attributeName + "." + LinkDirection.FORWARD;
                    if (attributes.containsKey(forwardKey)) {
                        attributes.put(
                                forwardKey,
                                computeAggregateProperty(attributes.get(forwardKey),
                                        attributeDef.getAggregateFunction()));
                    }

                    String reverseKey = attributeName + "." + LinkDirection.REVERSE;
                    if (attributes.containsKey(reverseKey)) {
                        attributes.put(
                                reverseKey,
                                computeAggregateProperty(attributes.get(reverseKey),
                                        attributeDef.getAggregateFunction()));
                    }

                    String noneKey = attributeName + "." + LinkDirection.NONE;
                    if (attributes.containsKey(noneKey)) {
                        attributes.put(noneKey,
                                computeAggregateProperty(attributes.get(noneKey), attributeDef.getAggregateFunction()));
                    }

                    if (attributes.containsKey(attributeName)) {
                        attributes.put(
                                attributeName,
                                computeAggregateProperty(attributes.get(attributeName),
                                        attributeDef.getAggregateFunction()));
                    }
                }

            }
        }
    }

    private void processObjectAggregateAttributes(List<AttributeDef> attributeDefs,
            Map<String, Property> attributes) throws InstantiationException,
            IllegalAccessException {

                for (AttributeDef attributeDef : attributeDefs) {
                    TaskHelper.checkForCancel();

                    String attributeName = attributeDef.getName();
                    String forwardKey = attributeName + "." + LinkDirection.FORWARD;
                    if (attributes.containsKey(forwardKey)) {
                        attributes.put(
                                forwardKey,
                                computeAggregateProperty(attributes.get(forwardKey),
                                        attributeDef.getAggregateFunction()));
                    }

                    String reverseKey = attributeName + "." + LinkDirection.REVERSE;
                    if (attributes.containsKey(reverseKey)) {
                        attributes.put(
                                reverseKey,
                                computeAggregateProperty(attributes.get(reverseKey),
                                        attributeDef.getAggregateFunction()));
                    }

                    String noneKey = attributeName + "." + LinkDirection.NONE;
                    if (attributes.containsKey(noneKey)) {
                        attributes.put(noneKey,
                                computeAggregateProperty(attributes.get(noneKey), attributeDef.getAggregateFunction()));
                    }

                    if (attributes.containsKey(attributeName)) {
                        attributes.put(
                                attributeName,
                                computeAggregateProperty(attributes.get(attributeName),
                                        attributeDef.getAggregateFunction()));
                    }
                }

    }


    protected boolean exceedsBundleThreshold(VisualGraph graph, Document optionSet) {
        int threshold = DEFAULT_BUNDLE_THRESHOLD;
        if (optionSet != null) {
            NodeList values = optionSet.getElementsByTagName("BundleThreshold");
            if ((values != null) && (values.getLength() > 0)) {
                org.w3c.dom.Node element = values.item(0);
                try {
                    threshold = Integer.parseInt(element.getTextContent());
                } catch (NumberFormatException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Invalid formatting encountered for bundling threshold in optionset");
                    }
                }
            }
        }
        return (graph.getNodeCount() > threshold);
    }

    public void createNodesOnRow(Graph graph, CacheRowSet rowset, Map<String, Node> nodesOnRow, int row,
            RelGraphViewDef graphDef, CsiScriptRunner scriptRunner, Bindings bindings, GraphProcessorListener listener, GraphTheme theme)
            throws Exception {

        HashMap<String, Node> nodeMap = (HashMap<String, Node>) graph.getClientProperty(NODE_HASH_TABLE);
        HashMap<String, TypeInfo> legendInfo = (HashMap<String, TypeInfo>) graph
                .getClientProperty(NodeStore.NODE_LEGEND_INFO);

        Multimap<String, Node> nodesByDef = (Multimap<String, Node>) graph.getClientProperty(NODES_BY_DEFINITION);

        // TODO: This map is set on the graph only for convenience - in order to avoid reiterating the nodes and links
        // in order to
        // TODO: retrieve the node/links-ids based on a supporting row.
        Multimap<Integer, Integer> nodesByRow = (Multimap<Integer, Integer>) graph
                .getClientProperty(NODES_BY_INTERNAL_ROW);

        if (nodesByDef == null) {
            HashMap<String, Collection<Node>> nodeContainer = new HashMap<String, Collection<Node>>();
            Supplier<? extends Collection<Node>> nodeSetSupplier = new Supplier<Collection<Node>>() {

                @Override
                public Collection<Node> get() {
                    return new HashSet<Node>(15);
                }
            };
            nodesByDef = Multimaps.newMultimap(nodeContainer, nodeSetSupplier);
            graph.putClientProperty(NODES_BY_DEFINITION, nodesByDef);

        }
        List<NodeDef> nodeDefs = graphDef.getNodeDefs();
        Iterator<NodeDef> nodeDefIterator = nodeDefs.iterator();

        while (nodeDefIterator.hasNext()) {
            NodeDef nodeDef = nodeDefIterator.next();

            String expr = getConditionalExpr(nodeDef, ObjectAttributes.CSI_CREATE_IF);
            if (!evaluateConditional(expr, scriptRunner, bindings)) {
                continue;
            }

            Map<String, Object> attributeValues = retrieveAttributeValues(nodeDef.getAttributeDefs(), rowset);

            String nodeID = getID(nodeDef, attributeValues);
            String nodeLabel = getLabel(nodeDef, attributeValues);

            if ((nodeID == null) || (nodeID.trim().length() == 0)) {
                continue;
            }
            //put ID into attributedef list
            if (nodeDef.isAddPrefixId()) {
                Set<AttributeDef> defs = nodeDef.getAttributeDefs();
                for (AttributeDef attr : defs) {
                    if (attr.getName().equals(ObjectAttributes.CSI_INTERNAL_ID)) {
                        FieldDef field = attr.getFieldDef();
                        nodeID = field.getFieldName() + "_" + nodeID;
                        break;
                    }
                }
                attributeValues.put(ObjectAttributes.CSI_INTERNAL_ID, nodeID);
            }

            boolean exists = nodeMap.containsKey(nodeID);

            //really get or create node
            Node node = createNode(graph, nodeDef, nodeID, attributeValues);

            nodesByRow.put(row, node.getRow());

            NodeStore nodeStore = getNodeDetails(node);
            nodeStore.addLabel(((nodeLabel == null) || (nodeLabel.trim().length() == 0)) ? nodeID : nodeLabel.trim());
            nodeStore.addSpecRow(nodeDef.getName(), row);
            nodeStore.setHideLabels(nodeDef.getHideLabels());
            int mode = getSizeMode(nodeDef.getAttributeDefs());
            if (mode != 0) {
                nodeStore.setSizeMode(mode);
            }

            if ((nodesByDef != null) && !nodesByDef.get(nodeDef.getName()).contains(node)) {
                nodesByDef.put(nodeDef.getName(), node);
            }

            Object nodeType = getNodeType(nodeDef, attributeValues);
            if ((nodeType != null) && (nodeType.toString().length() > 0)) {
                TypeInfo typeInfo = legendInfo.get(nodeType.toString().trim());
                if (typeInfo == null) {
                    typeInfo = TypeInfo.initializeTypeInfo(nodeType.toString().trim(), attributeValues,
                            isDynamicType(nodeDef), isDynamicIcon(nodeDef), theme);

                    legendInfo.put(typeInfo.name, typeInfo);
                }

                // update our legend count if this is a new node
                // or this node existed, but previously didn't have
                // the type specified by the currently computed type string.
                Map<String, Integer> types = nodeStore.getTypes();
                if (!exists) {
                    typeInfo.totalCount++;
                } else if ((types != null) && !types.containsKey(nodeType.toString().trim())) {
                    typeInfo.totalCount++;
                }
            }

            if (nodeType != null) {
                nodeStore.addType(nodeType.toString());
            }

            Map<String, Property> attributes = nodeStore.getAttributes();

            //This section builds tooltips on load if dynamic is set to false
            if(Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips()){
                Set<Entry<String, Object>> values = attributeValues.entrySet();
                for (Entry<String, Object> entry : values) {
                    String key = entry.getKey();
                    if (!skip(key)) {
                        AttributeDef attributeDef = nodeDef.getAttributeDef(key, true);
                        if(!attributeDef.isIncludeInTooltip()){
                            if(attributeDef.getName() != null){
                                String name = attributeDef.getName();
                                if(AttributeDef.nonToolTips.contains(name)){
                                    continue;
                                }
                            }
                            addPropertyValue(attributes, attributeDef, entry.getValue(), key);
                        }
                    }
                }
            }else{
                Set<Entry<String, Object>> values = attributeValues.entrySet();
                for (Entry<String, Object> entry : values) {
                    String key = entry.getKey();
                    if (!skip(key)) {
                        AttributeDef attributeDef = nodeDef.getAttributeDef(key, true);
                        addPropertyValue(attributes, attributeDef, entry.getValue(), key);
                    }

                }
            }
            Object o = attributeValues.get(ObjectAttributes.CSI_INTERNAL_ICON);
            if (o != null) {

                nodeStore.setIcon(o.toString());
            }

            setNodeDetails(node, nodeStore);

            if (listener != null) {
                listener.handleNode(node, exists);
            }

            //This checks if the node is already hidden then checks if it is part of a linkup and if it's a new node in that linkup
            if (!nodeStore.isHidden() && ((listener == null) || listener.isNewNode(node) )) {
                try {
                    bindings.put("node", node);
                    bindings.put("nodeStore", nodeStore);
                    //NOTE: this check can be dones sooner
                    String hiddenIf = getConditionalExpr(nodeDef, ObjectAttributes.CSI_HIDDEN_IF);
                    if (!evaluateConditional(hiddenIf, scriptRunner, bindings)) {
                        nodeStore.setHidden(true);
                    }
                } finally {
                    bindings.remove("node");
                    bindings.remove("nodeStore");
                }
            }

            nodesOnRow.put(nodeDef.getName(), node);
        }
    }

//    public void createTooltipProperties(DataView dataview, ResultSet resultSet, Set<AttributeDef> attributeDefs, NodeStore nodeStore) throws InstantiationException, IllegalAccessException, SQLException {
//        CacheRowSet rowset = new CacheRowSet(dataview.getMeta().getModelDef().getFieldDefs(), resultSet);
//
//        Map<String, Property> attributes = nodeStore.getAttributes();
//        attributes.clear();
//        //Make attribute value pairs with cache data
//        while(resultSet.next()){
//            Map<String, Object> attributeValues = populateAttributeWithRow(attributeDefs, rowset);
//
//
//        //Create Properties based on def's and attribute value pairs
//        Set<Entry<String, Object>> values = attributeValues.entrySet();
//        for (Entry<String, Object> entry : values) {
//            String key = entry.getKey();
//            if (!GraphManager.skip(key)) {
//
//                AttributeDef myDef = null;
//                    for (AttributeDef def : attributeDefs) {
//                        if (def.getName().equals(key)) {
//                            myDef = def;
//                            break;
//                        }
//                    }
//
//                addPropertyValue(attributes, myDef, entry.getValue(), key);
//            }
//
//        }
//
//        //Determine which def's are aggregates
//        List<AttributeDef> aggregates =new ArrayList<AttributeDef>();
//            for (AttributeDef def : attributeDefs) {
//
//                boolean isComputed = isComputed(def);
//                if (isComputed) {
//                    aggregates.add(def);
//                }
//
//            }
//
//        //Process aggregate properties
//        processObjectAggregateAttributes(aggregates, attributes);
//
//        }
//    }

    public void createTooltipProperties(DataView dataview, ResultSet resultSet, Set<AttributeDef> attributeDefs, AbstractGraphObjectStore store) throws InstantiationException, IllegalAccessException, SQLException {
        CacheRowSet rowset = new CacheRowSet(dataview.getMeta().getModelDef().getFieldDefs(), resultSet);

        Map<String, Property> attributes = store.getAttributes();
        attributes.clear();
        //Make attribute value pairs with cache data
        while(rowset.nextRow()){
            Map<String, Object> attributeValues = populateAttributeWithRow(attributeDefs, rowset);

            if (attributeValues != null) {
               //Create Properties based on def's and attribute value pairs
               Set<Entry<String, Object>> values = attributeValues.entrySet();

               for (Entry<String, Object> entry : values) {
                  String key = entry.getKey();

                  if (!GraphManager.skip(key)) {
                     AttributeDef myDef = null;

                     for (AttributeDef def : attributeDefs) {
                        if (def.getName().equals(key)) {
                           myDef = def;
                           break;
                        }
                     }
                     addPropertyValue(attributes, myDef, entry.getValue(), key);
                  }
               }
            }
        }

        //Determine which def's are aggregates
        List<AttributeDef> aggregates =new ArrayList<AttributeDef>();
        for (AttributeDef def : attributeDefs) {

            boolean isComputed = isComputed(def);
            if (isComputed) {
                aggregates.add(def);
            }

        }

        //Process aggregate properties
        processObjectAggregateAttributes(aggregates, attributes);

    }

    private boolean isDynamicIcon(NodeDef nodeDef) {

        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON);
        if (attributeDef != null) {

            FieldDef fieldDef = attributeDef.getFieldDef();
            if (fieldDef != null) {

                FieldType fieldType = fieldDef.getFieldType();
                if (fieldType != null) {

                    return (fieldType != FieldType.STATIC);
                }
            }
        }
        return false;
    }

    private void addType(DBObject data, Object type) {
        if (type != null) {
            if (!data.containsField(GraphConstants.TYPE)) {
                BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
                builder.add(type.toString(), 1);
                data.put(GraphConstants.TYPE, builder.get());
            } else {
                DBObject typeInfo = (DBObject) data.get(GraphConstants.TYPE);
                if (typeInfo.containsField(type.toString())) {
                    Integer count = (Integer) typeInfo.get(type.toString());
                    typeInfo.put(type.toString(), count.intValue() + 1);
                } else {
                    typeInfo.put(type.toString(), 1);
                }
            }
        }
    }

    private DBObject buildVertexQuery(String id) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add(Helper.APP_ID, id);
        return builder.get();
    }

    private int getSizeMode(Set<AttributeDef> defs) {
        for (AttributeDef def : defs) {
            if (def.getName().equals(ObjectAttributes.CSI_INTERNAL_SIZE)) {
                if (def.getBySize() || (!def.getBySize() && (def.getReferenceName() != null))) {
                    return ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE;
                }
                if (def.getByStatic()) {
                    return ObjectAttributes.CSI_INTERNAL_SIZE_BY_STATIC;
                }
            }
        }

        return ObjectAttributes.CSI_INTERNAL_SIZE_BY_STATIC;
    }

    private void addDirectionPropertyValue(Map<String, Property> attributes, AttributeDef attributeDef,
            Object attributeValue, LinkDirection direction) {

        String key = attributeDef.getName() + "." + direction;

        Property property = attributes.get(key);

        if (property == null) {
            property = new ComputedProperty(attributeDef.getName());
            property.setIncludeInTooltip(attributeDef.isIncludeInTooltip());
            property.setHideEmptyInTooltip(attributeDef.isHideEmptyInTooltip());
            attributes.put(key, property);
        }

        List<Object> values = property.getValues();
        values.add(attributeValue);
    }

    public void createLinksOnRow(Graph graph, Collection<LinkDef> linkDefs, CacheRowSet rowset,
            Map<String, Node> nodesOnRow, int row, CsiScriptRunner scriptRunner, Bindings bindings,
            GraphProcessorListener listener) throws Exception {

        HashMap<String, Edge> edgeMap = getOrCreateEdgeMap(graph);
        Multimap<Integer, Integer> linksByRow = (Multimap<Integer, Integer>) graph
                .getClientProperty(LINKS_BY_INTERNAL_ROW);
        Multimap<String, Edge> linksByDef = getOrCreateLinksByDefMap(graph);

        Iterator<LinkDef> specs = linkDefs.iterator();
        while (specs.hasNext()) {
            LinkDef linkDef = specs.next();

            String expr = getConditionalExpr(linkDef, ObjectAttributes.CSI_CREATE_IF);
            if (!evaluateConditional(expr, scriptRunner, bindings)) {
                continue;
            }

            NodeDef sourceDef = linkDef.getNodeDef1();
            NodeDef targetDef = linkDef.getNodeDef2();

            Node source = nodesOnRow.get(sourceDef.getName());
            Node target = nodesOnRow.get(targetDef.getName());

            if (source == target) {
                // can happen in case of multitype node
                continue;
            }

            if ((source == null) || (target == null)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping link creation on row " + row + " one or both nodes were not created.");
                }
                continue;
            }

            // links between 2 nodes are merged in a single edge
            // check if the edge existed before
            String linkId = computeLinkID(source, target);
            Edge edge = edgeMap.get(linkId);
            boolean existedEdge = false;
            boolean existedRevertedEdge = false;
            if (edge == null) {
                // check id the reverse edge existed before
                edge = edgeMap.get(computeLinkID(target, source));
                if (edge == null) {
                    // we need to create a new edge and an asociated LinkStore
                    edge = createEdge(graph, linkId, source, target, linkDef);
                    edgeMap.put(linkId, edge);
                } else {
                    existedEdge = true;
                    existedRevertedEdge = true;
                }
            } else {
                existedEdge = true;
            }

            linksByRow.put(row, edge.getRow());

            // it could be same edge but different LinkDef in case of multi-type nodes
            // linksByDef is a multimap with set values the edge will be added only once
            linksByDef.put(linkDef.getUuid(), edge);

            // update linkStore
            updateLinkStore(edge, linkDef, rowset, row, existedRevertedEdge, graph, existedEdge);

            if (listener != null) {
                listener.handleEdge(edge, existedEdge);
            }

            if((listener == null) || listener.isNewEdge(edge)) {
               updateHidden(edge, linkDef, bindings, scriptRunner);
            }


        }
    } // end of createLinksOnRow

    private HashMap<String, Edge> getOrCreateEdgeMap(Graph graph) {
        HashMap<String, Edge> edgeMap = (HashMap<String, Edge>) graph.getClientProperty(EDGE_HASH_TABLE);
        if (edgeMap == null) {
            edgeMap = new HashMap<String, Edge>();
            graph.putClientProperty(EDGE_HASH_TABLE, edgeMap);
        }
        return edgeMap;
    }

    /**
     * Create a new edge and the associated LinkStore
     *
     * @param graph
     * @param linkId
     * @param source
     * @param target
     * @return
     */
    private Edge createEdge(Graph graph, String linkId, Node source, Node target, LinkDef linkDef) {
        Edge edge = graph.addEdge(source, target);
        LinkStore linkStore = new LinkStore();
        linkStore.setDocId(ObjectId.get());

        linkStore.setKey(linkId);
        linkStore.setFirstEndpoint(getNodeDetails(source));
        linkStore.setSecondEndpoint(getNodeDetails(target));
        linkStore.setSpecID(linkDef.getName());
        int mode = getSizeMode(linkDef.getAttributeDefs());
        if (mode != 0) {
            linkStore.setSizeMode(mode);
        }

        setLinkDetails(edge, linkStore);
        return edge;
    }

    private DBObject buildVertexQuery(Node node) {
        Object value = node.get(GraphConstants.DOC_ID);
        if (value != null) {
            return Helper.getIdQuery(value);
        } else {
            NodeStore details = getNodeDetails(node);
            return Helper.getNodeQuery((details == null) ? null : details.getKey());
        }
    }

    private void updateLinkStore(Edge edge, LinkDef linkDef, CacheRowSet rowset, int row, boolean existedRevertedEdge,
            Graph graph, boolean existedEdge) throws Exception {

        LinkStore linkStore = getEdgeDetails(edge);

        Map<String, Object> attributeValues = retrieveAttributeValues(linkDef.getAttributeDefs(), rowset);

        Map<String, Property> attributes = linkStore.getAttributes();
        Object labelValue = attributeValues.get(ObjectAttributes.CSI_INTERNAL_LABEL);
        if (labelValue != null) {
            linkStore.addLabel(labelValue.toString());
        }

        linkStore.addSpecRow(linkDef.getName(), row);
        linkStore.setHideLabels(linkDef.isHideLabels());
        Object linkType = getLinkType(linkDef, attributeValues);

        if (linkStore.getSizeMode() == 0) {
            int mode = getSizeMode(linkDef.getAttributeDefs());
            if (mode != 0) {
                linkStore.setSizeMode(mode);
            }
        }

        if ((linkType != null) && (linkType.toString().length() > 0)) {
            HashMap<String, GraphLinkLegendItem> legendInfo = (HashMap<String, GraphLinkLegendItem>) graph
                    .getClientProperty(LinkStore.LINK_LEGEND_INFO);
            GraphLinkLegendItem linkLegendItem = legendInfo.get(linkType.toString().trim());
            if (linkLegendItem == null) {

                boolean isDynamic = GraphManager.isDynamicType(linkDef);
                linkLegendItem = GraphDataManager.initializeLinkLegendItem(linkType.toString().trim(), attributeValues, isDynamic, null);

                legendInfo.put(linkLegendItem.typeName, linkLegendItem);
            }

            // update our legend count if this is a new node
            // or this node existed, but previously didn't have
            // the type specified by the currently computed type string.
            Map<String, Integer> types = linkStore.getTypes();
            if (!existedEdge) {
                linkLegendItem.totalCount++;
            } else if ((types != null) && !types.containsKey(linkType.toString().trim())) {
                linkLegendItem.totalCount++;

            }
        }

        if (linkType != null) {
            // CTWO-6870 - This is so we don't keep "unspecified type" around on migrated dataviews IF we have a new
            // type for them.
            // If we change the behavior for "Unspecified Type" so it's possible to have an unspecified type, this may
            // need to change.
            Map<String, Integer> types = linkStore.getTypes();
            types.remove(GraphConstants.UNSPECIFIED_LINK_TYPE);

            linkStore.addType(linkType.toString());
        } else {
            linkStore.addType("");
        }

        // each time we found an edge we check for direction and add it to LinkStore
        LinkDirection direction = computeDirection(linkDef, rowset);

        if (existedRevertedEdge) {
            // if the existing edge is reverted (from target, source instead of source target)
            // direction also need to be reverted
            direction = LinkDirection.revert(direction);
        }
        linkStore.addDirectedEdge(direction);

        AttributeDef color = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR);
        if (color != null) {
            FieldDef fieldDef = color.getFieldDef();
            if (fieldDef.getFieldType() == FieldType.STATIC) {
                String staticText = fieldDef.getStaticText();

                Integer colorValue = null;
                try {
                    colorValue = Integer.parseInt(staticText);
                } catch (NumberFormatException e) {

                }
                linkStore.setColor(colorValue);
            }
        }

        // saveEdgeData(edge, linkDef, labelValue, linkType, attributeValues);

        Set<Entry<String, Object>> values = attributeValues.entrySet();
        for (Entry<String, Object> entry : values) {
            String key = entry.getKey();
            Object attributeValue = entry.getValue();

            AttributeDef attrDef = linkDef.getAttributeDef(key);

            if (skip(key)) {
                continue;
            }

            if(Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips()){

                if(!attrDef.isIncludeInTooltip()){
                    if (attrDef.getKind() == AttributeKind.COMPUTED) {
                        addComputedPropertyValue(attributes, attrDef, attributeValue, direction);
                        addComputedPropertyValue(attributes, attrDef, attributeValue, null);
                    } else {
                        addPropertyValue(attributes, attrDef, attributeValue, key);
                    }
                }
            } else {
                if (attrDef.getKind() == AttributeKind.COMPUTED) {
                    addComputedPropertyValue(attributes, attrDef, attributeValue, direction);
                    addComputedPropertyValue(attributes, attrDef, attributeValue, null);
                } else {
                    addPropertyValue(attributes, attrDef, attributeValue, key);
                }
            }
        }

    }

    private LinkDirection computeDirection(LinkDef linkDef, CacheRowSet rowset) {
        DirectionDef directionDef = linkDef.getDirectionDef();

        if ((directionDef == null) || (directionDef.getFieldDef() == null)) {
            return LinkDirection.NONE;
        }

        FieldDef directionFieldDef = directionDef.getFieldDef();
        if (directionFieldDef.getFieldType() == FieldType.STATIC) {
            String directionValue = directionFieldDef.getStaticText();
            return directionValue != null ? LinkDirection.valueOf(directionValue) : LinkDirection.NONE;
        } else if ((directionFieldDef.getFieldType() == FieldType.COLUMN_REF)
                || (directionFieldDef.getFieldType() == FieldType.LINKUP_REF)
                || (directionFieldDef.getFieldType() == FieldType.DERIVED)
                || (directionFieldDef.getFieldType() == FieldType.SCRIPTED)) {
            Object value = rowset.get(directionFieldDef);

            // The 'null' string representation for a field direction value is received as '' from the client
            if (value == null) {
                value = "";
            }

            if (value instanceof String) {
                String stringValue = (String) value;
                return directionDef.resolveDirectionByValue(stringValue);
            }

        }

        return LinkDirection.NONE;
    }

    private void updateHidden(Edge edge, LinkDef linkDef, Bindings bindings, CsiScriptRunner scriptRunner)
            throws CentrifugeException {
        LinkStore linkStore = getEdgeDetails(edge);

        if ((linkStore != null) && !linkStore.isHidden()) {
           try {
              bindings.put("link", edge);
              bindings.put("linkStore", linkStore);

              String hiddenIf = getConditionalExpr(linkDef, ObjectAttributes.CSI_HIDDEN_IF);

              if (!evaluateConditional(hiddenIf, scriptRunner, bindings)) {
                 linkStore.setHidden(true);
              }
           } finally {
              bindings.remove("link");
              bindings.remove("linkStore");
           }
        }
    }

    private String getConditionalExpr(LinkDef linkDef, String type) {
        String expr = null;
        AttributeDef def = null;

        def = linkDef.getAttributeDef(type);
        if (def != null) {
            FieldDef f = def.getFieldDef();
            if ((f != null) && (f.getFieldType() == FieldType.STATIC)) {
                expr = f.getStaticText();
            }
        }

        if ((expr == null) || expr.trim().isEmpty()) {
            ConditionalExpression conditional = null;
            if (ObjectAttributes.CSI_CREATE_IF.equals(type)) {
                conditional = linkDef.getCreateConditional();
            } else {
                conditional = linkDef.getHiddenConditional();
            }
            if (conditional != null) {
                expr = conditional.getExpression();
            }
        }

        if ((expr != null) && expr.trim().isEmpty()) {
            expr = null;
        }

        return expr;
    }

    private Multimap<String, Edge> getOrCreateLinksByDefMap(Graph graph) {
        Multimap<String, Edge> linksByDef = (Multimap<String, Edge>) graph.getClientProperty(LINKS_BY_DEFINITION);
        if (linksByDef == null) {
            HashMap<String, Collection<Edge>> linkContainer = new HashMap<String, Collection<Edge>>();
            Supplier<? extends Collection<Edge>> linkSetSupplier = new Supplier<Collection<Edge>>() {

                @Override
               public Collection<Edge> get() {
                    return new HashSet<Edge>(15);
                }
            };

            linksByDef = Multimaps.newMultimap(linkContainer, linkSetSupplier);
            graph.putClientProperty(LINKS_BY_DEFINITION, linksByDef);
        }
        return linksByDef;
    }

    public Object getLinkType(LinkDef linkDef, Map<String, Object> attributeValues) {
        Object type = attributeValues.get(ObjectAttributes.CSI_INTERNAL_TYPE);

        if (type != null) {
            return type;
        }

        AttributeDef attr = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
        if (attr == null) {
            attr = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID);
        }

        if (attr == null) {
            return null;
        }

        FieldDef field = attr.getFieldDef();
        String name = field.getFieldName();

        if (((name == null) || (name.length() == 0)) && (field.getFieldType() == FieldType.STATIC)) {
            name = field.getStaticText();
        }

        return name;
    }

    public boolean computeComponentsIfNeeded(GraphContext graphContext, GraphConstants.eLayoutAlgorithms layout) {
        Graph graph = graphContext.getGraphData();
        boolean directed = GraphManager.DIRECTED_LAYOUTS.contains(layout);
        Collection<Integer> selectedNodeIds = null;
        // use selection node for root candidate only for hierarchy layout
        if (eLayoutAlgorithms.treeNodeLink == layout) {// check "directed" for all directed layouts
            SelectionModel selectionModel = graphContext.getSelection(DEFAULT_SELECTION);
            if (selectionModel != null) {
                selectedNodeIds = selectionModel.nodes;
            }
        }

        Object wasDirected = graph.getClientProperty(GraphConstants.COMPONENTS_DIRECTED);
        boolean needRecompute = ((wasDirected == null) || !wasDirected.equals(directed)) || graphContext.isSubnetsDirty();

        if (!needRecompute && directed) {
            // check if the selection has been changed
            Collection<Integer> previousSelectedNodeIds = (Collection<Integer>) graph
                    .getClientProperty(GraphConstants.COMPONENT_ROOT_SELECTION);
            if (((selectedNodeIds != previousSelectedNodeIds) && ((selectedNodeIds == null) || (previousSelectedNodeIds == null) || (selectedNodeIds.size() != previousSelectedNodeIds.size()) || !selectedNodeIds
              .containsAll(previousSelectedNodeIds)))) {
//                needRecompute = true;
            }
        }

        if (true) {
            computeComponents(graph, directed, selectedNodeIds);
            computeComponentRegions(graphContext);
            graphContext.setSubnetsDirty(false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Performs a search against the provided graph to count and mark the distinct, connected (sub)graphs.
     * <p>
     * The backing Table of the graph is augmented by adding a COMPONENT_ID column of type int.
     * <p>
     * Each node is annotated with the component number to which they belong.
     * <p>
     * The Graph's client property COMPONENT_COUNT is updated with the total number of components discovered; the
     * property value is of type Integer.
     *
     * @param graph
     */
    public int computeComponents(Graph graph) {
        return computeComponents(graph, false, null);
    }

    private void computeComponentsFor(Iterator<Node> rootCandidates, Graph graph, BreadthFirstIterator search,
            List<Graph> subGraphs) {
        int componentCount = subGraphs.size();
        while (rootCandidates.hasNext()) {
            TaskHelper.checkForCancel();
            Node node = rootCandidates.next();

            // check if is vizible and was not allready added to a subgraph
            if ((node.getInt(GraphConstants.COMPONENT_ID) != -1)
                    || !GraphContext.Predicates.IsVisualizedAndDisplayable.test(node)) {
                continue;
            }
            Graph subGraph = createVisibleSubGraph(graph, node, search, componentCount);
            subGraphs.add(subGraph);
            componentCount++;
        }
    }

    /**
     * Performs a search against the provided graph to count and mark the distinct, connected (sub)graphs.
     * <p>
     * The backing Table of the graph is augmented by adding a COMPONENT_ID column of type int.
     * <p>
     * Each node is annotated with the component number to which they belong.
     * <p>
     * The Graph's client property COMPONENT_COUNT is updated with the total number of components discovered; the
     * property value is of type Integer.
     *
     * @param graph
     * @param directedLayout
     *            if true the connected (sub)graphs needs to have a directed tree
     *            that include all the nodes
     */
    public int computeComponents(final Graph graph, boolean directedLayout, Collection<Integer> rootIdCandidates) {

        initSubgraphData(graph, directedLayout);

        List<Graph> subGraphs = new ArrayList<Graph>();

        BreadthFirstIterator search;
        if (directedLayout) {
            search = new DirectedBreadthFirstSearch();
        } else {
            search = new BreadthFirstSearch();
        }

//        Predicate notPartOfSubgraph = (Predicate) ExpressionParser.parse(GraphConstants.COMPONENT_ID + " == -1");

        if (directedLayout) {

            // for directed layout selection of root also decide which node will be part of the subgraph
            // first we iterate over the nodes that don't have in edges those must be root or else they will became
            // isolated nodes (subgraph with a single node)

            // check which node have no in edges are no reachable from other node in a directed graph
            Predicate<Node> unreachableNodePredicate = new Predicate<Node>() {
               @Override
               public boolean test(Node node) {
                  return (node.getInt(GraphConstants.COUNT_IN_DISPEDGES) == 0);
               }
            };

            if ((rootIdCandidates != null) && !rootIdCandidates.isEmpty()) {
               Collection<Node> rootCandidates = new ArrayList<Node>();
               Collection<Node> reachable = new ArrayList<Node>();

               for (Iterator<Integer> rootIdCandidate = rootIdCandidates.iterator(); rootIdCandidate.hasNext();) {
                  Node node = graph.getNode(rootIdCandidate.next());

                  if (unreachableNodePredicate.test(node)) {
                     rootCandidates.add(node);
                  } else {
                     reachable.add(node);
                  }
               }
               // extract first trees starting from unreachable selected candidates those is assure that will became roots
               computeComponentsFor(rootCandidates.iterator(), graph, search, subGraphs);

               // extract trees from remaining selected candidates
               computeComponentsFor(reachable.iterator(), graph, search, subGraphs);
            }
            // extract trees from remaining node that are not reachable
            Collection<Node> unreachable = new ArrayList<Node>();

            for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
               Node node = nodes.next();

               if (unreachableNodePredicate.test(node)) {
                  unreachable.add(node);
               }
            }
            computeComponentsFor(unreachable.iterator(), graph, search, subGraphs);
        }
        // extract trees from remaining nodes
        computeComponentsFor(graph.nodes(), graph, search, subGraphs);

        Iterator<Edge> edgeIterator = graph.edges();
        while (edgeIterator.hasNext()) {
            TaskHelper.checkForCancel();

            Edge edge = edgeIterator.next();

            Node sourceNode = edge.getSourceNode();
            Node targetNode = edge.getTargetNode();

            int cid = sourceNode.getInt(GraphConstants.COMPONENT_ID);
            // don't add the link if the target belong to different tree or are not part of a component
            //
            if ((cid == -1) || (cid != targetNode.getInt(GraphConstants.COMPONENT_ID))) {
                // different tree
                continue;
            }

            LinkStore linkStore = getEdgeDetails(edge);
            if ((linkStore != null) && !linkStore.isDisplayable()) {
                continue;
            }

            Graph subGraph = subGraphs.get(cid);

            edge.setInt(GraphConstants.COMPONENT_ID, cid);

            int addedEdge = subGraph.addEdge(sourceNode.getInt(GraphConstants.SUBGRAPH_NODE_ID),
                    targetNode.getInt(GraphConstants.SUBGRAPH_NODE_ID));
            Edge subEdge = subGraph.getEdge(addedEdge);
            GraphManager.setLinkDetails(subEdge, linkStore);
        }

        graph.putClientProperty(GraphConstants.COMPONENT_COUNT, Integer.valueOf(subGraphs.size()));
        graph.putClientProperty(GraphConstants.COMPONENTS, subGraphs);
        graph.putClientProperty(GraphConstants.COMPONENTS_DIRECTED, directedLayout);

        if (directedLayout && (rootIdCandidates != null)) {
            graph.putClientProperty(GraphConstants.COMPONENT_ROOT_SELECTION, new ArrayList<Integer>(rootIdCandidates));
        } else {
            graph.putClientProperty(GraphConstants.COMPONENT_ROOT_SELECTION, null);
        }
        return subGraphs.size();
    }

    /**
     * Create need column for storing components/subgraphs ids, or reset them if exist
     * Update GraphConstants.COUNT_IN_DISPEDGES for each node if updateCountInDispEdges is true
     *
     * @param graph
     * @param updateCountInDispEdges
     */
    public void initSubgraphData(Graph graph, boolean updateCountInDispEdges) {
        Table nodeTable = graph.getNodeTable();
        Table edgeTable = graph.getEdgeTable();

        boolean edgesNeedsReset = false;
        boolean nodeInVisibleEdgesNeedsReset = updateCountInDispEdges;
        // add in our component-oriented column, we'll use string based
        // names for the groups when creating visual graphs for the
        // layout/placement

        if (nodeTable.getColumn(GraphConstants.COMPONENT_ID) == null) {
            nodeTable.addColumn(GraphConstants.COMPONENT_ID, int.class, -1);
            nodeTable.addColumn(GraphConstants.SUBGRAPH_NODE_ID, int.class, -1);
            edgeTable.addColumn(GraphConstants.COMPONENT_ID, int.class, -1);
            nodeTable.addColumn(GraphConstants.COUNT_IN_DISPEDGES, int.class, 0);
        } else {
            // check separate for this column to avoid problems when update the server
            // and keep old graph data
            if (nodeTable.getColumn(GraphConstants.COUNT_IN_DISPEDGES) == null) {
                nodeTable.addColumn(GraphConstants.COUNT_IN_DISPEDGES, int.class, 0);
                nodeInVisibleEdgesNeedsReset = false;
            }
            // already exists - reset to default
            // Note: don't use the table's row count!!! This for some reason
            // does not return the true total #
            // of nodes that need to be reset e.g. looping while count <
            // nodeTable.getRowCount()
            // Using Iterators on the nodes and edges works much more cleanly!
            Iterator<Node> nodes = graph.nodes();
            while (nodes.hasNext()) {
                TaskHelper.checkForCancel();

                Node node = nodes.next();
                node.setInt(GraphConstants.COMPONENT_ID, -1);
                node.setInt(GraphConstants.SUBGRAPH_NODE_ID, -1);
                if (nodeInVisibleEdgesNeedsReset) {
                    node.setInt(GraphConstants.COUNT_IN_DISPEDGES, 0);
                }
            }
            edgesNeedsReset = true;
        }

        if (edgesNeedsReset || updateCountInDispEdges) {
            Iterator<Edge> edges = graph.edges();
            while (edges.hasNext()) {
                TaskHelper.checkForCancel();
                Edge edge = edges.next();
                if (edgesNeedsReset) {
                    edge.setInt(GraphConstants.COMPONENT_ID, -1);
                }
                if (updateCountInDispEdges) {
                    updateCountInDispEdges(edge);
                }
            }
        }
    }

    private void updateCountInDispEdges(Edge edge) {
        Node src = edge.getSourceNode();
        Node trg = edge.getTargetNode();
        Predicate<Node> nodeVisible = GraphContext.Predicates.IsVisualizedAndDisplayable;

        if (nodeVisible.test(src) && nodeVisible.test(trg)) {
            LinkStore ls = getEdgeDetails(edge);
            if ((ls != null) && !ls.isHidden()) {
                switch (ls.getDirection()) {
                    case FORWARD:
                        trg.setInt(GraphConstants.COUNT_IN_DISPEDGES, 1);
                        break;
                    case REVERSE:
                        src.setInt(GraphConstants.COUNT_IN_DISPEDGES, 1);
                        break;
                    default:
                        trg.setInt(GraphConstants.COUNT_IN_DISPEDGES, 1);
                        src.setInt(GraphConstants.COUNT_IN_DISPEDGES, 1);
                        break;
                }
            }
        }
    }

    /**
     * Create a subgraph base on tree starting from given root and using the given BreadthFirstIterator.
     * Selected nodes get COMPONENT_ID value set with id.
     * The subgraph will not contain edges
     *
     * @param graph
     *            - parent Graph
     * @param root
     *            - the node that will be the root for the subgraph tree
     * @param search
     *            - the BreadthFirstIterator used to determine the tree
     * @param id
     *            - used to mark the selected node in parent Graph
     * @return the subgraph/tree with the selected nodes
     */
    private Graph createVisibleSubGraph(Graph graph, Node root, BreadthFirstIterator search, int id) {
        search.init(root, Integer.MAX_VALUE, Constants.NODE_TRAVERSAL);

        Graph subGraph = new Graph(graph.isDirected());
        Table subNodes = subGraph.getNodeTable();
        subNodes.addColumn(GraphConstants.ORIG_NODE_ID, int.class);
        subNodes.addColumn(GraphConstants.NODE_DETAIL, NodeStore.class);
        subNodes.addColumn(GraphConstants.DOC_ID, Object.class);

        Table subEdges = subGraph.getEdgeTable();
        subEdges.addColumn(GraphConstants.LINK_DETAIL, Object.class);

        while (search.hasNext()) {
            TaskHelper.checkForCancel();

            Node node = (Node) search.next();

            // don't add same node to different components
            if (node.getInt(GraphConstants.COMPONENT_ID) != -1) {
                continue;
            }

            if (!GraphContext.Predicates.IsVisualizedAndDisplayable.test(node)) {
                continue;
            }
            Node copied = subGraph.addNode();

            node.setInt(GraphConstants.COMPONENT_ID, id);
            node.setInt(GraphConstants.SUBGRAPH_NODE_ID, copied.getRow());
            copied.setInt(GraphConstants.ORIG_NODE_ID, node.getRow());
            copied.set(GraphConstants.DOC_ID, node.get(GraphConstants.DOC_ID));
            GraphManager.setNodeDetails(copied, getNodeDetails(node));
        }
        return subGraph;
    }
    public Rectangle computeComponentRegions(GraphContext gc) {
        Graph graph = gc.getGraphData();
        List<Graph> subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);
        List<Rectangle> regions = new ArrayList<Rectangle>();
        GraphLayout layout = gc.getVisualizationDef().getLayout();

        if (GraphLayout.treeNodeLink == layout) {
            for (Graph subGraph : subGraphs) {
                int k = 1;
                SpanningTree tree;
                tree = new DirectedSpanningTree(subGraph);
                Iterator nodes = tree.nodes();
                int i = 1;
                Map<Integer, AtomicInteger> j = Maps.newConcurrentMap();
                while (nodes.hasNext()) {
                    int depth = ((Node) nodes.next()).getDepth();
                    AtomicInteger ai = j.getOrDefault(depth, new AtomicInteger());
                    ai.incrementAndGet();
                    i = Math.max(i, depth);
                    j.put(depth, ai);
                }
                for (Entry<Integer, AtomicInteger> integerAtomicIntegerEntry : j.entrySet()) {
                    k = Math.max(k, integerAtomicIntegerEntry.getValue().get());
                }
                Rectangle region = new Rectangle();
//                if (region.width < 60 * k || region.height < 60 * k) {
                    region.setSize(Math.max(60 * k, region.width), Math.max(60 * k, region.width));

//                }
                regions.add(region);
            }
        }
        int componentCount = (Integer) graph.getClientProperty(GraphConstants.COMPONENT_COUNT);

        int[] nodesPerComponent = new int[componentCount];
        int totalNodes = 0;

        for (int i = 0; i < componentCount; i++) {
            TaskHelper.checkForCancel();

            Graph subGraph = subGraphs.get(i);
            int count = subGraph.getNodeCount();
            nodesPerComponent[i] = count;
            totalNodes += count;
        }

        Rectangle fullRegion = new Rectangle(0, 0, 0, 0);
        int spaceDimension = (int) (200.0d * Math.sqrt(totalNodes));
        int lowerX = 0;
        int lowerY = 0;
        int upperX = 0;

        boolean visited[] = new boolean[componentCount];
        while (true) {
            TaskHelper.checkForCancel();

            int max = -1;
            int index = -1;
            for (int i = 0; i < componentCount; i++) {
                if (!visited[i]) {
                    if (nodesPerComponent[i] > max) {
                        max = nodesPerComponent[i];
                        index = i;
                    }
                }
            }

            if (max == -1) {
                break;
            }

            visited[index] = true;

            double fractional = Math.sqrt(((double) nodesPerComponent[index] / (double) totalNodes));
            int patchDim = (int) (spaceDimension * fractional);

            try {
                patchDim = Math.max(patchDim,regions.get(index).width+300);
                if (patchDim > spaceDimension) {
                    spaceDimension = patchDim;
                }
            } catch (Exception e) {
            }

            if ((lowerY + patchDim) > spaceDimension) {
                upperX = upperX + PATCH_PADDING;
                lowerY = 0;
                lowerX = upperX;
                upperX += patchDim;
            } else if (upperX == 0) {
                upperX = patchDim;
            }

            Rectangle region = new Rectangle();
            region.x = lowerX;
            region.y = lowerY;
            region.width = region.height = patchDim;
            fullRegion.add(region);

            Graph subGraph = subGraphs.get(index);
            subGraph.putClientProperty(GraphConstants.PATCH_REGION, region);

            lowerY += patchDim + PATCH_PADDING;

        }

        graph.putClientProperty(GraphConstants.PATCH_BOUNDS, fullRegion);

        return fullRegion;
    }

    public void runPlacement(Graph graph, GraphConstants.eLayoutAlgorithms algorithm) {
        if(Configuration.getInstance().getGraphAdvConfig().useConcurrentLayout()){
            runPlacement(graph, algorithm, null);
        } else {
            runOldPlacement(graph, algorithm, null);
        }
    }

   public void runPlacement(Graph graph, GraphConstants.eLayoutAlgorithms algorithm, GraphContext context) {
      if (context != null) {
         if (Configuration.getInstance().getGraphAdvConfig().useConcurrentLayout()) {
            final List<Graph> subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);
            SelectionModel selection = new SelectionModel();

            if (context.getSelection(DEFAULT_SELECTION) != null) {
               selection = context.getSelection(DEFAULT_SELECTION);
            }
            RelGraphViewDef relGraphViewDef = context.getVisualizationDef();
            TaskContext taskContext = TaskController.getInstance().getCurrentContext();
            forkJoinPool.invoke(
               new RunPlacementAction(subGraphs, graph, context, selection, algorithm,0, relGraphViewDef, taskContext));
         } else {
            runOldPlacement(graph, algorithm, context);
         }
      }
   }

    public void runOldPlacement(Graph graph, GraphConstants.eLayoutAlgorithms algorithm, GraphContext context) {
        List<Graph> subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);

        VisualGraph visualGraph = (VisualGraph) graph.getClientProperty(GraphContext.VISUAL_GRAPH);

        SelectionModel selection = new SelectionModel();
        if (context != null) {
           if (context.getSelection(DEFAULT_SELECTION) != null) {
              selection = context.getSelection(DEFAULT_SELECTION);
           }
           List<Node> networkElected = new ArrayList<Node>();
           RelGraphViewDef relGraphViewDef = context.getVisualizationDef();
           int netId = 0;
           VisualGraph rootVG = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);

           for (Graph subGraph : subGraphs) {
              TaskHelper.checkForCancel();

            // identify the set of nodes part of this subgraph.
            networkElected.clear();
            for (Integer id : selection.nodes) {
                Node node = graph.getNode(id);
                if (netId == node.getInt(GraphConstants.COMPONENT_ID)) {
                    int subnetId = node.getInt(GraphConstants.SUBGRAPH_NODE_ID);
                    networkElected.add(subGraph.getNode(subnetId));
                }
            }
            visualGraph.putClientProperty(TEMP_SELECTION, networkElected);
            // layoutComponentAsync(rootVG, subGraph, algorithm);
            List<Node> elected = (List<Node>) rootVG.getClientProperty(TEMP_SELECTION);

            layoutComponent(visualGraph, subGraph, algorithm, context, elected, netId, relGraphViewDef);
            visualGraph.putClientProperty(TEMP_SELECTION, null);

            netId++;
           }
        }
    }

    public static void layoutComponent(VisualGraph rootVG, Graph subGraph, eLayoutAlgorithms algorithm, GraphContext context, List<Node> elected, int netId, RelGraphViewDef relGraphViewDef) {
        Visualization localVis = new Visualization();
        String graphName = "tempSubGraph";
        VisualGraph localVisGraph = localVis.addGraph(graphName, subGraph);

        Rectangle fullregion = (Rectangle) subGraph.getClientProperty(GraphConstants.PATCH_REGION);
        Rectangle region = new Rectangle(fullregion.x + PATCH_MARGIN, fullregion.y + PATCH_MARGIN, fullregion.width - (2
                * PATCH_MARGIN), fullregion.height - (2 * PATCH_MARGIN));

        localVisGraph.putClientProperty(CopyPositionLayout.ROOT_GRAPH, rootVG);
        localVisGraph.putClientProperty(GraphConstants.PATCH_REGION, region);
        String key = null;
        try {
            Iterator<Tuple> iterator = localVisGraph.nodes();
            int nodeCount = subGraph.getNodeCount();
            if (nodeCount == 1) {
                VisualItem tni = (VisualItem) iterator.next();
                VisualItem rootNI = (VisualItem) rootVG.getNode(tni.getInt(GraphConstants.ORIG_NODE_ID));
                prefuse.util.PrefuseLib.setX(rootNI, null, region.getCenterX());
                prefuse.util.PrefuseLib.setY(rootNI, null, region.getCenterY());
            } else if (nodeCount == 2) {
                VisualItem node = (VisualItem) iterator.next();
                VisualItem rootNI = (VisualItem) rootVG.getNode(node.getInt(GraphConstants.ORIG_NODE_ID));
                prefuse.util.PrefuseLib.setX(rootNI, null, region.x);
                prefuse.util.PrefuseLib.setY(rootNI, null, region.y);

                node = (VisualItem) iterator.next();
                rootNI = (VisualItem) rootVG.getNode(node.getInt(GraphConstants.ORIG_NODE_ID));
                prefuse.util.PrefuseLib.setX(rootNI, null, region.x + region.width);
                prefuse.util.PrefuseLib.setY(rootNI, null, region.y + region.height);

            } else if (nodeCount == 3) {
                NodeItem[] slot = new NodeItem[3];
                for (int i = 0; i < slot.length; i++) {
                    Node n = subGraph.getNode(i);
                    slot[i] = (NodeItem) rootVG.getNode(n.getInt(GraphConstants.ORIG_NODE_ID));
                }

                int linkCount = 0;
                for (int i = 0; i < slot.length; i++) {
                    linkCount += slot[i].getDegree();
                }
                if (linkCount <= 4) {
                    // dealing with graph that isn't dense
                    if (slot[0].getDegree() == 2) {
                        NodeItem swap = slot[0];
                        slot[0] = slot[1];
                        slot[1] = swap;
                    } else if (slot[2].getDegree() == 2) {
                        NodeItem swap = slot[2];
                        slot[2] = slot[1];
                        slot[1] = swap;
                    }
                }

                // NB: slot[ 1 ] represents a node w/ 2 edges! -- other may be...
                PrefuseLib.setX(slot[1], null, (region.x + (region.width / 2)));
                PrefuseLib.setY(slot[1], null, (region.y));

                PrefuseLib.setX(slot[0], null, region.x);
                PrefuseLib.setY(slot[0], null, (region.y + region.height));

                PrefuseLib.setX(slot[2], null, region.x + region.width);
                PrefuseLib.setY(slot[2], null, (region.y + region.height));
            } else {

                // perform community detection and layout of the communities. use the resulting layout
                // of the communities to map back to the nodes for initial positioning.

                Layout layout = null;

                boolean requiresWrapper = true;

                if (algorithm == null) {
                    algorithm = GraphConstants.eLayoutAlgorithms.forceDirected;
                }

                /*
                 * Centrifuge algorithm is a composite layout.
                 */
                if (algorithm == GraphConstants.eLayoutAlgorithms.centrifuge) {
                    CentrifugeLayout cl = new CentrifugeLayout(graphName, false, true);
                    cl.setIterations(100);
                    cl.getForceSimulator().setIntegrator(new CsiRungeKuttaIntegrator());
                    layout = cl;
                    requiresWrapper = false;

                    if ((elected != null) && (elected.size() > 1)) {
                        cl.setElectedNodes(elected);
                    }

                }
                else if (algorithm == GraphConstants.eLayoutAlgorithms.forceDirected) {

                    CsiForceDirectedLayout csiLayout = new CsiForceDirectedLayout(graphName, false, true);
                    csiLayout.getForceSimulator().addForce(new GravitationalForce());
                    int i = Configuration.getInstance().getGraphAdvConfig().getDefaultLayoutIterations();
                    try {
                        String iterations = relGraphViewDef.getSettings().getPropertiesMap().get("iterations");//NON-NLS
                        i = Integer.parseInt(iterations);
                        {//enforce max iteration policy
                            int maxLayoutIterations = Configuration.getInstance().getGraphAdvConfig().getMaxLayoutIterations();
                            i = Math.min(maxLayoutIterations, i);
                        }
                    } catch (NumberFormatException e) {
                        //ignore and just go with 100
                    }
                    i = Math.max(i, 2);
                    csiLayout.setIterations(i);
                    csiLayout.getForceSimulator().setIntegrator(new CsiRungeKuttaIntegrator());
                    layout = csiLayout;
                    requiresWrapper = false;

                }
                else if (algorithm == eLayoutAlgorithms.applyForce) {
                    StringBuilder keyStringBuffer = new StringBuilder(1000);
                    keyStringBuffer.append(context).append(netId);
                    {
                        Iterator nodes = subGraph.nodes();
                        while (nodes.hasNext()) {
                            TableNode node = (TableNode) nodes.next();
                            keyStringBuffer.append(node.getRow()).append(",");
                        }
                    }
                    {
                        Iterator edges = subGraph.edges();
                        while (edges.hasNext()) {
                            TableEdge edge = (TableEdge) edges.next();
                            keyStringBuffer.append(edge.getRow()).append(",");
                        }
                    }
                    key = keyStringBuffer.toString();
                    Layout layout1 = layouts.getIfPresent(key);
                    if(layout1 !=null){
                        layout = layout1;
                    }else {
                        CsiApplyForceLayout csiLayout = new CsiApplyForceLayout(graphName, false, true);
                        csiLayout.getForceSimulator().addForce(new GravitationalForce());
                        csiLayout.getForceSimulator().setIntegrator(new CsiRungeKuttaIntegrator());
                        layout = csiLayout;
                        requiresWrapper = false;
                        layouts.put(key, layout);
                    }
                        int i = 25;
                        try {
                            String iterations = relGraphViewDef.getSettings().getPropertiesMap().get("moreIterations");//NON-NLS
                            i = Integer.parseInt(iterations);
                            {//enforce max iteration policy
                                int maxLayoutIterations = Configuration.getInstance().getGraphAdvConfig().getMaxLayoutIterations();
                                i = Math.min(maxLayoutIterations, i);
                            }
                        } catch (NumberFormatException e) {
                            //ignore and just go with 25
                        }
                        i = Math.max(i, 2);
                    ((CsiApplyForceLayout) layout).setIterations(i);
                } else if (algorithm == GraphConstants.eLayoutAlgorithms.circular) {
                    layout = new CircularLayout(graphName);
                } else if (algorithm == GraphConstants.eLayoutAlgorithms.circle) {
                    layout = new CircleLayout(graphName);
                } else if (algorithm == GraphConstants.eLayoutAlgorithms.treeRadial) {
                    layout = new DirectedRadialTreeLayout(graphName);

                } else if (algorithm == GraphConstants.eLayoutAlgorithms.treeNodeLink) {
                    layout = new DirectedNodeLinkTreeLayout(graphName);
                    String layoutOrientationStr = relGraphViewDef.getSettings().getPropertiesMap().get("layoutOrientation");//NON-NLS
                    HierarchicalLayoutOrientation layoutOrientation;
                    if (layoutOrientationStr == null) {
                        layoutOrientation = Configuration.getInstance().getGraphAdvConfig().getDefaultHierarchicalLayoutOrientation();
                    } else {

                        layoutOrientation = HierarchicalLayoutOrientation.valueOf(layoutOrientationStr);
                    }
                    int layoutOrientationInt = layoutOrientation.getValue();
                    ((NodeLinkTreeLayout) layout).setOrientation(layoutOrientationInt);
                    ((NodeLinkTreeLayout) layout).setBreadthSpacing(15.0d);
                    TupleSet ts = localVis.getGroup(graphName);
                    Graph g = (Graph) ts;
                    SpanningTree tree;
                    tree = new DirectedSpanningTree(g);
                    Iterator nodes = tree.nodes();
                    int i = 1;
                    Map<Integer, AtomicInteger> j = Maps.newConcurrentMap();
                    while (nodes.hasNext()) {
                        int depth = ((Node) nodes.next()).getDepth();
                        AtomicInteger ai = j.getOrDefault(depth, new AtomicInteger());
                        ai.incrementAndGet();
                        i = Math.max(i, depth);
                        j.put(depth, ai);
                    }
                    int k = 1;
                    for (Entry<Integer, AtomicInteger> integerAtomicIntegerEntry : j.entrySet()) {
                        k = Math.max(k, integerAtomicIntegerEntry.getValue().get());
                    }
                    DirectedNodeLinkTreeLayout layout1 = (DirectedNodeLinkTreeLayout) layout;
                    double d = (k*15)/ (double) (Math.max(3,(i/2)+2));
                    layout1.setDepthSpacing(d);
                    if ((region.width < (60 * k)) || (region.height < (60 * k))) {
                        //region.setSize(Math.max(60 * k, region.width), Math.max(60 * k, region.width));
                    }
                } else if (algorithm == GraphConstants.eLayoutAlgorithms.grid) {
                    layout = new GridLayout2(graphName);
                } else {
                    LOG.error(String.format("Unrecognized layout choice %s, default to Force Directed",
                            algorithm.toString()));
                    algorithm = GraphConstants.eLayoutAlgorithms.forceDirected;
                    CsiForceDirectedLayout csiLayout = new CsiForceDirectedLayout(graphName, false, true);
                    csiLayout.setIterations(50);
                    csiLayout.getForceSimulator().setIntegrator(new CsiRungeKuttaIntegrator());
                    layout = csiLayout;

                    requiresWrapper = false;
                }

                if (requiresWrapper) {
                    layout = new CopyPositionLayout(layout);
                }
                layout.setVisualization(localVis);
                layout.setLayoutBounds(new Rectangle2D.Double(region.x, region.y, region.width, region.height));
                layout.setLayoutAnchor(new Point2D.Double(region.getCenterX(), region.getCenterY()));
                layout.setGroup(graphName);

                StopWatch stopWatch = new StopWatch();

                stopWatch.start();
                layout.run(0.0);
                stopWatch.stop();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Layout " + algorithm.toString() + " computation time: " + stopWatch.getTime() + " ms");
                }

            }
        }catch (NullPointerException e){
            if (key != null) {
                layouts.invalidate(key);
            }
        } finally {
            subGraph.removeAllListeners();
            localVisGraph.removeAllListeners();
            // clean up stuff so the visualization and visual graph can be garbage collected
/*
            localVisGraph.putClientProperty(CopyPositionLayout.ROOT_GRAPH, null);
            localVisGraph.putClientProperty(GraphConstants.PATCH_REGION, null);

            localVis.removeGroup(graphName);
            localVisGraph.removeAllSets();
            localVis.reset();*/
        }
    }

    private void layoutCoarsenedCommunity(VisualGraph rootVG, VisualGraph graphVG) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing community discovery for optimized layout");
        }
        PrefuseToJungTransformer transformer = new PrefuseToJungTransformer();
        edu.uci.ics.jung.graph.Graph<String, String> jGraph = transformer.apply(graphVG);
        FastCommunityClusterer<String, String> clusterer = new FastCommunityClusterer<String, String>();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        Set<Set<String>> communities = clusterer.apply(jGraph);
        stopWatch.stop();

        // create coarsened graph based on communities....
        Graph coarsened = new Graph();
        Table nodeTable = coarsened.getNodeTable();
        nodeTable.addColumn("community", Set.class);

        Map<Set<String>, Node> cMap = new HashMap<Set<String>, Node>();
        for (Set<String> community : communities) {
            Node node = coarsened.addNode();
            node.set("community", community);
            cMap.put(community, node);
        }

        Iterator edges = graphVG.edges();
        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            LinkStore data = getEdgeDetails(edge);

            if (data != null) {
               String one = data.getFirstEndpoint().getKey();
               String two = data.getSecondEndpoint().getKey();
               Set<String> oneC = clusterer.getCommunity(one);

               if ((oneC == null) || oneC.contains(two)) {
                  continue;
               }
               Set<String> twoC = clusterer.getCommunity(two);

               coarsened.addEdge(cMap.get(oneC), cMap.get(twoC));
            }
        }

        //
        // execute layout for the coarsened graph
        //
        Rectangle fullregion = (Rectangle) graphVG.getClientProperty(GraphConstants.PATCH_REGION);
        Rectangle region = new Rectangle(fullregion.x + PATCH_MARGIN, fullregion.y + PATCH_MARGIN, fullregion.width - (2
                * PATCH_MARGIN), fullregion.height - (2 * PATCH_MARGIN));
        Visualization viz = new Visualization();
        VisualGraph coarsenedVG = viz.addGraph(TEMP_GRAPH, coarsened);
        ForceDirectedLayout layout = new ForceDirectedLayout(TEMP_GRAPH, false, true);
        layout.setIterations(100);
        layout.setVisualization(viz);
        layout.setLayoutBounds(new Rectangle2D.Double(0, 0, region.width, region.height));
        layout.setLayoutAnchor(new Point2D.Double(region.getCenterX(), region.getCenterY()));
        layout.setGroup(TEMP_GRAPH);
        layout.run(0.0);

        //
        // extract mapped positions from the communities back into each node for an initial position.
        //
        Rectangle2D nodeRegion = viz.getBounds(TEMP_GRAPH + ".nodes");
        double regionW = region.getWidth();
        double regionH = region.getHeight();
        double nodeRegionW = nodeRegion.getWidth();
        double nodeRegionH = nodeRegion.getHeight();

        if ((BigDecimal.valueOf(nodeRegionW).compareTo(BigDecimal.ZERO) == 0) || Double.isNaN(nodeRegionW)) {
            nodeRegionW = 1.0;
        }
        if (BigDecimal.valueOf(nodeRegionH).compareTo(BigDecimal.ZERO) == 0) {
            nodeRegionH = 1.0;
        }
        double scaleX = regionW / nodeRegionW;
        double scaleY = regionH / nodeRegionH;

        // create temp visual Graph mapping from node key to the actual object. we
        // don't have this mapping anywhere for a component. the layout uses
        // the temp visual graph and not the rootVG positions!
        Map<String, VisualItem> nodeMap = new HashMap<String, VisualItem>();
        Iterator nodes = graphVG.nodes();
        while (nodes.hasNext()) {
            NodeItem vi = (NodeItem) nodes.next();
            NodeStore store = GraphManager.getNodeDetails(vi);

            if (store != null) {
               nodeMap.put(store.getKey(), vi);
            }
        }

        Visualization rootViz = rootVG.getVisualization();
        /*Graph rawRootGraph = (Graph) */rootViz.getSourceData(rootVG.getGroup());
//        Map<String, Node> rootNodeIdMap = (Map<String, Node>) rawRootGraph
//                .getClientProperty(GraphManager.NODE_HASH_TABLE);

        // now copy out the locations into the root visualization graph
        Iterator iterator = coarsenedVG.nodes();
        Point2D position = new Point2D.Double();
        while (iterator.hasNext()) {
            VisualItem vi = (VisualItem) iterator.next();
            Tuple sourceTuple = vi.getSourceTuple();

            // account for singular nodes and/or positions that are not
            // defined!
            double x = vi.getX();
            if (Double.isNaN(x)) {
                x = nodeRegion.getCenterX();
            }

            double y = vi.getY();
            if (Double.isNaN(y)) {
                y = nodeRegion.getCenterY();
            }

            x = ((x - nodeRegion.getMinX()) * scaleX) + region.x;
            y = ((y - nodeRegion.getMinY()) * scaleY) + region.y;
            position.setLocation(x, y);
            Set<String> community = (Set<String>) sourceTuple.get("community");

            for (String nodeKey : community) {
                NodeItem child = (NodeItem) nodeMap.get(nodeKey);
                NodeStore nodeStore = GraphManager.getNodeDetails(child);

                if (nodeStore != null) {
                   nodeStore.setPosition(GraphConstants.eLayoutAlgorithms.forceDirected, position);
                }
                prefuse.util.PrefuseLib.setX(child, null, position.getX());
                prefuse.util.PrefuseLib.setY(child, null, position.getY());
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Community discovery and optimization complete.");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Community discovery found : " + communities.size() + "in " + stopWatch.getTime() + " ms");
            }
        }

    }

    private void scaleToPatch(VisualGraph rootVG, Visualization localVis, VisualGraph localVisGraph, Rectangle region,
            GraphConstants.eLayoutAlgorithms algorithm) {
        Rectangle2D nodeRegion = localVis.getBounds("tempSubGraph.nodes");
        double regionW = region.getWidth();
        double regionH = region.getHeight();
        double nodeRegionW = nodeRegion.getWidth();
        double nodeRegionH = nodeRegion.getHeight();

        double scaleX = regionW / nodeRegionW;
        double scaleY = regionH / nodeRegionH;

        if (algorithm == GraphConstants.eLayoutAlgorithms.circular) {
            scaleX = scaleY = Math.min(scaleX, scaleY);
        }

        // now copy out the locations into the root visualization graph
        Iterator iterator = localVisGraph.nodes();
        Point2D position = new Point2D.Double();
        while (iterator.hasNext()) {
            VisualItem vi = (VisualItem) iterator.next();
            Tuple sourceTuple = vi.getSourceTuple();
            NodeStore nodeStore = GraphManager.getNodeDetails(sourceTuple);

            if (nodeStore != null) {
               // account for singular nodes and/or positions that are not defined!
               double x = vi.getX();

               if (Double.isNaN(x)) {
                  x = nodeRegion.getCenterX();
               }
               double y = vi.getY();

               if (Double.isNaN(y)) {
                  y = nodeRegion.getCenterY();
               }
               x = ((x - nodeRegion.getMinX()) * scaleX) + region.x;
               y = ((y - nodeRegion.getMinY()) * scaleY) + region.y;
               position.setLocation(x, y);
               nodeStore.setPosition(algorithm, position);

               VisualItem rootNI = (VisualItem) rootVG.getNode(vi.getInt(GraphConstants.ORIG_NODE_ID));
               prefuse.util.PrefuseLib.setX(rootNI, null, position.getX());
               prefuse.util.PrefuseLib.setY(rootNI, null, position.getY());
            }
        }
    }

    public Rectangle2D getItemBounds(List<VisualItem> items) {
        Rectangle2D bounds = null;
        GraphContext gc = GraphContext.Current.get();
        RelGraphViewDef rgdef = gc.getVisualizationDef();
        double minimumNodeScaleFactor = rgdef.getMinimumNodeScaleFactor();
        Double zoom = null;
        for (VisualItem item : items) {
            if (zoom == null) {
                zoom = item.getVisualization().getDisplay(0).getScale();
            }
            double scaleBy = Math.min(1, zoom * minimumNodeScaleFactor);
            Rectangle2D itemBounds = item.getBounds();

            double w = itemBounds.getWidth()/scaleBy;
            double h = itemBounds.getHeight()/scaleBy;
            itemBounds = new Rectangle2D.Double(itemBounds.getX()-(.5*(w-itemBounds.getWidth())), itemBounds.getY()-(.5*(h-itemBounds.getHeight())), w, h);
            if (bounds == null) {
                bounds = (Rectangle2D) itemBounds.clone();
            } else {
                bounds.add(itemBounds);
            }
        }

        return bounds;
    }

    public void computeAndLayoutComponents(GraphContext graphContext) {
        Visualization vis = graphContext.getVisualization();
        Graph graph = graphContext.getGraphData();
        SelectionModel selectionModel = graphContext.getSelection(DEFAULT_SELECTION);
        Collection<Integer> selectedNodeIds = null;
        if (selectionModel != null) {
            selectedNodeIds = selectionModel.nodes;
        }

        RelGraphConfig config = Configuration.getInstance().getGraphConfig();
        eLayoutAlgorithms standard = eLayoutAlgorithms.forceDirected;
        eLayoutAlgorithms cacheLayout = LayoutCache.getInstance().getLayout(graphContext.getVizUuid());
        if (cacheLayout != null) {
            standard = cacheLayout;
        }
        else {
            RelGraphViewDef viewDef = graphContext.getVisualizationDef();
            String defaultLayout = viewDef.getSettings().getPropertiesMap().get("defaultLayout");
            if (!Strings.isNullOrEmpty(defaultLayout)) {
                standard = LayoutHelper.getLayout(defaultLayout);
            } else if (viewDef.getLayout() != null){
                standard = eLayoutAlgorithms.valueOf(viewDef.getLayout().toString());
            } else if ((config != null) && (config.getInitialLayout() != null)) {
                try {
                    standard = eLayoutAlgorithms.valueOf(config.getInitialLayout());
                } catch (IllegalArgumentException iae) {
                }
            }
        }

        computeComponents(graph, DIRECTED_LAYOUTS.contains(standard), selectedNodeIds);

        LOG.debug("Computing patch regions");
        Rectangle patchBounds = computeComponentRegions(graphContext);

        LOG.debug("Running placement");

        runPlacement(graph, standard,graphContext);
        double dim = Math.max(patchBounds.getWidth(), patchBounds.getHeight());
        GraphicsLib.expand(patchBounds, dim*.1);
        GraphManager.fitToRegion(vis, patchBounds);

        graph.putClientProperty(LAYOUT_AND_COMPONENTS_COMPUTED_COMPLETE, true);
    }

    public BufferedImage renderGraph(GraphContext gc, Dimension vdim) {

        BufferedImage img = null;

        synchronized (gc) {
            if (gc.isPlayerRunning() && !gc.getPlayer().isHideInactiveNodes()) {
                return renderGraph(gc, vdim, gc.getImageLayers());
            }

            Visualization vis = gc.getVisualization();
            synchronized (vis) {
                Display display = vis.getDisplay(0);
                synchronized (display) {
                    if ((vdim.width > 0) && (vdim.height > 0)
                            && ((vdim.height != display.getHeight()) || (vdim.width != display.getWidth()))) {
                        display.setSize(vdim);
                    }

                    ImageProvider provider = gc.getImageProvider();

                    int width = display.getWidth();
                    int height = display.getHeight();
                    if (width <= 0) {
                        width = 400;
                    }
                    if (height <= 0) {
                        height = 400;
                    }
                    img = provider.create(width, height);
                    renderToImage(img, display);
                }
            }
        }
        return img;
    }

    public void renderToImage(BufferedImage img, Display display) {
        Graphics2D g = img.createGraphics();
        setRenderingHints(g, true);

        display.damageReport();

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        display.paintDisplay(g, new Dimension((img.getWidth()), (img.getHeight())));
        stopWatch.stop();
        LOG.debug("time to paint display: " + stopWatch.getTime());
        g.dispose();
        img.flush();
    }

    private BufferedImage renderGraph(GraphContext gc, Dimension viewport, Collection<BufferedImage> layers) {
        BufferedImage composite = null;
        synchronized (gc) {
            Visualization vis = gc.getVisualization();
//            RelGraphViewDef rgdef = gc.getVisualizationDef();
            synchronized (vis) {
                Display display = vis.getDisplay(0);
                updateDisplayViewport(display, viewport);

                Color background = display.getBackground();

                ImageProvider provider = gc.getImageProvider();
                BufferedImage imageWithBG = provider.create(display.getWidth(), display.getHeight());
                Graphics2D graphics = imageWithBG.createGraphics();
                graphics.setColor(background);
                graphics.fillRect(0, 0, display.getWidth(), display.getHeight());

                if ((layers != null) && !layers.isEmpty()) {
                    TimePlayerConfig playerConfig = Configuration.getInstance().getTimePlayerConfig();
                    float alphaLevel = playerConfig.getAlphaLevel();
                    AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaLevel);
                    Composite orig = graphics.getComposite();
                    graphics.setComposite(alpha);
                    for (BufferedImage layer : layers) {
                        graphics.drawImage(layer, 0, 0, null);
                    }

                    graphics.setComposite(orig);
                }

                display.setBackground(new Color(0x00ffffff, true));

                BufferedImage image = ImageWithAlphaProvider.create(display.getWidth(), display.getHeight());
                Graphics2D g = image.createGraphics();
                setRenderingHints(g, true);

                // report everything on screen as damaged for now
                display.damageReport();

                StopWatch stopWatch = new StopWatch();

                stopWatch.start();
                display.paintDisplay(g, new Dimension((display.getWidth()), (display.getHeight())));
                display.setBackground(background);
                stopWatch.stop();
                LOG.debug("time to paint display: " + stopWatch.getTime());

                g.dispose();
                image.flush();

                graphics.drawImage(image, 0, 0, null);
                graphics.dispose();

                imageWithBG.flush();

                composite = imageWithBG;

            }
        }

        return composite;

    }

   private void updateDisplayViewport(Display display, Dimension viewport) {
      if ((viewport != null) && (viewport.width > 0) && (viewport.height > 0) &&
          ((viewport.height != display.getHeight()) || (viewport.width != display.getWidth()))) {
         display.setSize(viewport);
      }
   }

    public BufferedImage renderDragItems(GraphContext gc, DragItems dragItems) {

        if ((dragItems == null) || dragItems.items.isEmpty()) {
            return null;
        }

        BufferedImage img = null;
        synchronized (gc) {
            Visualization vis = gc.getVisualization();
            synchronized (vis) {
                Display display = vis.getDisplay(0);
                synchronized (display) {

                    Rectangle2D imageBounds = dragItems.clipArea;
                    img = new BufferedImage(dragItems.imageW, dragItems.imageH, BufferedImage.BITMASK);
                    Graphics2D g = img.createGraphics();
/*                    g.setPaint(ColorLib.getColor(0,0,0));
                    g.fillRect(0,0,img.getWidth(),img.getHeight());*/
                    GraphManager.getInstance().setRenderingHints(g, true);
                    AffineTransform dispTx = display.getTransform();
                    g.scale(dispTx.getScaleX(), dispTx.getScaleY());
                    g.translate(-imageBounds.getX(), -imageBounds.getY());
//                    g.translate(-imageBounds.getX()+50, -imageBounds.getY()+50);

                    for (VisualItem dItem : dragItems.items) {
                        if (dragItems.clipArea.intersects(dItem.getBounds())) {
                            Renderer renderer = vis.getRenderer(dItem);
                            renderer.render(g, dItem);
                        }
                    }
                    g.dispose();
                }
            }
        }

        return img;
    }

    public Point2D toDisplayPoint(Display display, Point2D absPoint) {
        AffineTransform transform = display.getTransform();

        Point2D doublePoint = (transform == null ? absPoint : transform.transform(absPoint, null));

        Point point = new Point();
        point.setLocation(doublePoint);

        return point;
    }

    public void setRenderingHints(Graphics2D g, boolean highQuality) {
        if (highQuality) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    class DoubleRange {

        HashMap<String, DoubleRange> subGraphRange = new HashMap<String, DoubleRange>();
        double min = Double.MAX_VALUE;

        double max = Double.MIN_VALUE;

        double factor = 1.0;
        double base = 1.0;

        public void evaluate(String subGraphId, double value) {
            DoubleRange rng = subGraphRange.get(subGraphId);
            if (rng == null) {
                rng = new DoubleRange();
                rng.factor = this.factor;
            }
            rng.evaluate(value);
            subGraphRange.put(subGraphId, rng);
        }

        public void evaluate(double value) {
            if (value < min) {
                min = value;
            }

            if (value > max) {
                max = value;
            }
        }

        public double normalize(String subGraphId, double value) {
            DoubleRange rng = subGraphRange.get(subGraphId);
            if (rng == null) {
                return base;
            }
            return rng.normalize(value);
        }

        public double normalize(double value) {
            if (Double.isInfinite(value)) {
                return base;
            }
            if (BigDecimal.valueOf(max).compareTo(BigDecimal.valueOf(min)) == 0) {
                return base;
            } else {
                double results = (value - min) / (max - min);
                results = Math.max(results, 0D);
                results = Math.min(results, 1D);
                if (Double.isNaN(results)) {
                    results = base;
                } else {
                    results = (results * factor) + base;
                    // results += 0.5;
                }

                return results;
            }
        }
    }

    public static Map<String, Object> populateAttributeWithRow(Set<AttributeDef> attributes, CacheRowSet rowset) {
        try {
            Map<String, Object> attributeValues = retrieveAttributeValues(attributes, rowset);
//
//            Set<Entry<String, Object>> values = attributeValues.entrySet();
//            for (Entry<String, Object> entry : values) {
//                String key = entry.getKey();
//                if (!skip(key)) {
//                    AttributeDef attributeDef = nodeDef.getAttributeDef(key, true);
//                    addPropertyValue(attributeValues, attributeDef, entry.getValue(), key);
//                }
//
//            }
//
            return attributeValues;
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }

}

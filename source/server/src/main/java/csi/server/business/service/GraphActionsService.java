package csi.server.business.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.math.DoubleMath;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
import com.thoughtworks.xstream.XStream;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.util.BreadthFirstIterator;
import prefuse.data.util.TableIterator;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.util.collections.IntIterator;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;

import csi.config.Configuration;
import csi.graph.AbstractStorageService;
import csi.security.CsiSecurityManager;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.service.annotation.ServletResponseParam;
import csi.server.business.service.export.ExportActionsService;
import csi.server.business.service.icon.IconActionsService;
import csi.server.business.service.theme.ThemeActionsService;
import csi.server.business.service.visualization.theme.ThemeManager;
import csi.server.business.visualization.graph.AutoSizingPolicy;
import csi.server.business.visualization.graph.ClearGraphContextListener;
import csi.server.business.visualization.graph.DragItems;
import csi.server.business.visualization.graph.GraphAttributeHelper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphDataActions;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphMetrics;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.ImageFactory;
import csi.server.business.visualization.graph.LayoutHelper;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.GraphHelper;
import csi.server.business.visualization.graph.base.GraphLegendNodeSummary;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.graph.base.property.AggregateProperty;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.graph.cache.LayoutCache;
import csi.server.business.visualization.graph.data.GraphDataManager;
import csi.server.business.visualization.graph.grouping.UnGroupNodesCommand;
import csi.server.business.visualization.graph.paths.Path;
import csi.server.business.visualization.graph.paths.YenTopKShortestPathsAlg;
import csi.server.business.visualization.graph.pattern.GraphDatabaseHelper;
import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.business.visualization.graph.pattern.selection.PatternSelection;
import csi.server.business.visualization.graph.placement.BreadthFirstSearch;
import csi.server.business.visualization.graph.plunk.LinkPlunker;
import csi.server.business.visualization.graph.plunk.NodePlunker;
import csi.server.business.visualization.graph.plunk.PlunkDeleter;
import csi.server.business.visualization.graph.renderers.EdgeRenderer;
import csi.server.business.visualization.graph.renderers.ImageLocation;
import csi.server.business.visualization.graph.renderers.NodeRenderer;
import csi.server.business.visualization.graph.util.BundleMetrics;
import csi.server.business.visualization.legend.GraphLegendInfo;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.business.visualization.legend.LegendItemComparator;
import csi.server.common.codec.xstream.converter.EdgeListingConverter;
import csi.server.common.codec.xstream.converter.GraphConverter;
import csi.server.common.codec.xstream.converter.MongoObjectIdConverter;
import csi.server.common.codec.xstream.converter.NodeListingConverter;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.EdgeListing;
import csi.server.common.dto.graph.GraphInfo;
import csi.server.common.dto.graph.GraphOperation;
import csi.server.common.dto.graph.GraphRequest;
import csi.server.common.dto.graph.GraphSelectionOperation;
import csi.server.common.dto.graph.GraphStateFlags;
import csi.server.common.dto.graph.NodeListing;
import csi.server.common.dto.graph.gwt.AbstractItemTypeBase;
import csi.server.common.dto.graph.gwt.AnnotationDTO;
import csi.server.common.dto.graph.gwt.DragStartDTO;
import csi.server.common.dto.graph.gwt.EdgeListDTO;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.dto.graph.gwt.GraphTypesDTO;
import csi.server.common.dto.graph.gwt.ItemInfoDTO;
import csi.server.common.dto.graph.gwt.ItemTypeBoolean;
import csi.server.common.dto.graph.gwt.ItemTypeDate;
import csi.server.common.dto.graph.gwt.ItemTypeDouble;
import csi.server.common.dto.graph.gwt.ItemTypeInteger;
import csi.server.common.dto.graph.gwt.ItemTypeLong;
import csi.server.common.dto.graph.gwt.ItemTypeString;
import csi.server.common.dto.graph.gwt.NeighborPropDTO;
import csi.server.common.dto.graph.gwt.NeighborsDTO;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.dto.graph.gwt.NodeMapDTO;
import csi.server.common.dto.graph.gwt.NodePositionDTO;
import csi.server.common.dto.graph.gwt.PatternHighlightRequest;
import csi.server.common.dto.graph.gwt.PlunkLinkDTO;
import csi.server.common.dto.graph.gwt.PlunkNodeDTO;
import csi.server.common.dto.graph.gwt.PlunkedItemsToDeleteDTO;
import csi.server.common.dto.graph.gwt.TooltipPropsDTO;
import csi.server.common.dto.graph.gwt.VisualItemArray;
import csi.server.common.dto.graph.gwt.VisualItemString;
import csi.server.common.dto.graph.path.FindAllNodesMetaRequest;
import csi.server.common.dto.graph.path.FindPathRequest;
import csi.server.common.dto.graph.path.FindPathResponse;
import csi.server.common.dto.graph.path.PathMeta;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.server.common.dto.graph.search.EdgeInfo;
import csi.server.common.dto.graph.search.GraphSearchResults;
import csi.server.common.dto.graph.search.NodeInfo;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.GraphTooManyTypesException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.Annotation;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.GraphCachedState;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.model.visualization.graph.PlunkedNode;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.PostInvoke;
import csi.server.task.api.TaskHelper;
import csi.server.task.api.TaskSession;
import csi.server.task.exception.TaskAbortedException;
import csi.server.util.CacheUtil;
import csi.server.util.GeometryUtil;
import csi.server.util.ImageUtil;
import csi.server.util.IntegerUtil;
import csi.server.util.SqlUtil;
import csi.server.util.sql.CacheCommands;
import csi.server.util.sql.SQLFactory;
import csi.shared.gwt.viz.graph.GraphGetDisplayResponse;
import csi.shared.gwt.viz.graph.LinkDirection;
import csi.shared.gwt.viz.graph.MultiTypeInfo;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.vortex.CsiPair;

@Service(path = "/services/graphs2/actions")
@PostInvoke(listeners = {ClearGraphContextListener.class})
public class GraphActionsService extends AbstractService implements GraphActionServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(GraphActionsService.class);

    public static final String BROKEN_IMAGE = "BROKEN_IMAGE";
    public static final String NO_SHAPE_ICON = "img/LegendItem_TEXT.png";
    public static final String DIRECTION_TYPE = "csi.internal.direction.type";
    public static final String DIRECTION_AGGREGATES = "csi.internal.direction.aggregates";
    public static final String DEFAULT_SELECTION = "default.selection";
    public static final String ALL_LABELS = "allLabels";
    private static final String RESET = "reset";
    private static final String GRAPH_DATA_HAS_NOT_BEEN_LOADED = "Graph data has not been loaded.";
    private static final String CSI_MORE_DETAILS = "csi.tooltips.more";
    private static final String COMMENTS = "Comments";
    private static final String TOOLTIPS_TYPES = "tooltips";
    private static final int TWICE_THE_PADDING_FOR_RENDERED_NODE = 4;
    // Uses escaped tags in 3.x otherwise won't render on html correctly.
    // I don't think we can escape all tooltips because some need html to
    // perform functions, ex. links
    private static final String EMPTY_LABEL = "&lt;EMPTY&gt;";
    private static final String DVUUID_PARAM = "dvuuid";
    private static final String VIZUUID_PARAM = "vduuid";
    private static final String DISPLAY_LABEL = "displayLabel";
    private static final String BUNDLE_COUNT_INFO = "bundleCountInfo";
    private static final String VISUAL_ITEM_TYPE = "visualItemType";
    private static final String MEMBER_TYPES = "memberTypes";
    private static final String DIRECTION = "direction";
    private static final String COMPUTED = "computed";
    private static final String ACTION_BUNDLE = "bundle";
    private static final String ACTION_BUNDLE_BY_SPEC = "bundle.bySpec";
    private static final String LINK_UP_QUERY_NAME_PARAMETER = "query";
    private static final String ACTION_LINK_UP = "linkUp";
    private static final String ACTION_SPIN_OFF = "spinOff";
    private static final String BUNDLE_NAME = "bundleName";
    // private static final String IS_BUNDLE = "isBundle";
    private static final String PROPERTY_SIZE = "size";
    private static final String PROPERTY_ANCHOR = "anchor";
    private static final String PROPERTY_HIDE_LABELS = "hideLabels";
    private static final String LABEL = "label";
    private static final String VALUE_PARAMETER = "value";
    private static final String NAME_PARAMETER = "name";
    private static final String ACTION_SET_PROPERTY = "setProperty";
    private static final String OPERATION_SELECTION_MODEL = "selection";
    private static final String OPERATION_EDGES_SIMPLE = "edges";
    private static final String OPERATION_EDGE_COMPLEX = "graph.edges";
    private static final String OPERATION_EDGE_SIMPLE = "link";
    private static final String OPERATION_NODE_SIMPLE = "node";
    private static final String OPERATION_GRAPH = "graph";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String KEY_RESOURCE_CONNECTION = "KeyResourceConnection";
    private static final String INCLUDE_BUNDLE_ANIMATION = "includeBundleAnimation";
    private static final String CURRENT_LAYOUT = "currentLayout";
    private static final String ACTION_UNBUNDLE = "unbundle";
    private static final Object RENDER_THRESHOLD = null;
    private static final String METRIC_NAME = "metric";
    private static final String CLEAR_NODES = "resetNodes";
    private static final String CLEAR_LINKS = "resetLinks";
    private static final String TOOLTIP_ORDER = "csi.intenal.tooltip.order";
    private static final String PLUNKED = "plunked";
    private static final String ANNOTATION = "annotation";

    @Autowired
    private SelectionBroadcastCache selectionBroadcastCache;

    @Autowired
    private ExportActionsService exportActionsService;

    @Autowired
    private IconActionsService iconActionsService;

    private DirectionAggregator directionAggregator = new DirectionAggregator();
    @Inject
    private SQLFactory sqlFactory;
    @Inject
    private FilterActionsService filterActionsService;

    public GraphActionsService() {
        super();
    }

   @Operation
   public static SelectionModel getSelection(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                                             @QueryParam(value = "id") String id) {
      SelectionModel result = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext == null) {
         result = new SelectionModel();
      } else {
         synchronized (graphContext) {
            result = graphContext.getOrCreateSelection((id == null) ? "default.selection" : id);
         }
      }
      return result;
   }

    public static Point2D toDisplayPoint(Display display, Point2D absPoint) {
        AffineTransform transform = display.getTransform();

        Point2D doublePoint = (transform == null ? absPoint : transform.transform(absPoint, null));

        Point point = new Point();
        point.setLocation(doublePoint);

        return point;
    }

    /**
     * Augments the definitions of the graphs contained in the given dataview.
     * Adds the attribute ID and Type as functional fields, for all the nodeDefs
     * contained in the graphs. This method applies only for Imported dataviews
     * and dataviews created from templates. For the new ones, these fields are
     * set on the client.
     *
     * @param def The dataview definition for which we want to augment all
     *            relGraphs.
     */
    public static void augmentRelGraphViewDef(DataViewDef def) {
        DataModelDef model = def.getModelDef();
        List<VisualizationDef> vizList = model.getVisualizations();

        if (vizList != null) {
            for (VisualizationDef viz : vizList) {
                if (viz instanceof RelGraphViewDef) {
                    List<NodeDef> nodeDefs = ((RelGraphViewDef) viz).getNodeDefs();
                    if (nodeDefs != null) {
                        for (NodeDef nodeDef : nodeDefs) {
                            AttributeDef labelAttr = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
                            if (labelAttr != null) {
                                FieldDef fieldDef = labelAttr.getFieldDef();
                                if (fieldDef != null) {
                                    if (nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID) == null) {
                                        nodeDef.addAttributeDef(
                                                new AttributeDef(ObjectAttributes.CSI_INTERNAL_ID, fieldDef));
                                    }
                                    if (nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE) == null) {
                                        FieldDef typeFieldDef = new FieldDef();
                                        typeFieldDef.setStaticText(fieldDef.getFieldName());
                                        typeFieldDef.setFieldType(FieldType.STATIC);
                                        nodeDef.addAttributeDef(
                                                new AttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE, typeFieldDef));
                                    }
                                }
                            }
                            /*
                             * This code is for backwards compatibility on
                             * import. We grab the attribute, the attribute's
                             * fieldDef, the fieldDef's static text to know
                             * where it think there should be an icon. We test
                             * that location to see if there is something there.
                             * When there isn't a file where it thinks there
                             * should be one, we check the location where old
                             * node icons were moved. If we find what we are
                             * looking for in that folder, we update the icon
                             * location.
                             */
                            AttributeDef iconDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON);
                            if (iconDef != null) {
                                String s = iconDef.getFieldDef().getStaticText();
                                iconDef.getFieldDef().setStaticText(fixIconUrl(s));
                            }
                        }
                    }

                    List<LinkDef> linkDefs = ((RelGraphViewDef) viz).getLinkDefs();
                    if (linkDefs != null) {
                        for (LinkDef linkDef : linkDefs) {
                            AttributeDef labelAttr = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
                            if (labelAttr != null) {
                                FieldDef fieldDef = labelAttr.getFieldDef();
                                if (fieldDef != null) {
                                    if (linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID) == null) {
                                        linkDef.addAttributeDef(
                                                new AttributeDef(ObjectAttributes.CSI_INTERNAL_ID, fieldDef));
                                    }
                                    if (linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE) == null) {
                                        FieldDef typeFieldDef = new FieldDef();
                                        typeFieldDef.setStaticText(fieldDef.getFieldName());
                                        typeFieldDef.setFieldType(FieldType.STATIC);
                                        linkDef.addAttributeDef(
                                                new AttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE, typeFieldDef));
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    public static String fixIconUrl(String iconUrl) {
        return iconUrl;
    }

    @Override
    public void initMarshaller(XStream xstream) {
        xstream.addDefaultImplementation(HashMap.class, Map.class);
        xstream.alias("csi-map", CsiMap.class);
        xstream.alias("String", String.class);
        xstream.alias("selectionOperation", GraphSelectionOperation.class);
        // xstream.aliasType("docId", ObjectId.class);
        xstream.registerConverter(new GraphConverter());
        xstream.registerConverter(new NodeListingConverter());
        xstream.registerConverter(new EdgeListingConverter());
        xstream.registerConverter(new MongoObjectIdConverter());
    }

    @Override
    @Operation
    public void doAppearanceEditTask(@QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam(DVUUID_PARAM) String dvUuid,
                                     @QueryParam("id") String elementId, @QueryParam("name") String name, @QueryParam("value") String value,
                                     @QueryParam("type") String type) throws CentrifugeException {

        GraphOperation operation = new GraphOperation();
        operation.id = elementId;
        CsiMap<String, String> props = new CsiMap<String, String>();
        props.put("name", name);
        props.put("value", value);

        operation.operation = "setProperty";
        operation.type = type;
        operation.parameters = props;
        operateOn(vizUuid, dvUuid, operation);
    }

    @Override
    @Operation
    public void dohideUnhideTask(@QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam(DVUUID_PARAM) String dvUuid,
                                 @QueryParam("id") String elementId, @QueryParam("type") String type, @QueryParam("flag") String flag)
            throws CentrifugeException {

        GraphOperation operation = new GraphOperation();
        operation.id = elementId;
        CsiMap<String, String> props = new CsiMap<String, String>();
        props.put("name", "csi.internal.Hidden");
        props.put("value", flag);

        operation.operation = "setProperty";
        operation.type = type;
        operation.parameters = props;
        operateOn(vizUuid, dvUuid, operation);
    }

    @Override
    @Operation
    public void unhideSelection(@QueryParam(DVUUID_PARAM) String dvUuid, @QueryParam(VIZUUID_PARAM) String vizUuid)
            throws CentrifugeException {

        if (vizUuid == null) {
            throw new CentrifugeException("Missing required parameter: vduuid");
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        synchronized (context) {
            VisualGraph vGraph = context.getVisualGraph();

            Predicate<Node> canRenderNode = GraphContext.Predicates.IsNodeVisualized;

            String selectionName = GraphManager.DEFAULT_SELECTION;
            SelectionModel selection = context.getOrCreateSelection(selectionName);
            for (Integer nodeId : selection.nodes) {
                NodeItem nodeItem = (NodeItem) vGraph.getNode(nodeId);

                if (canRenderNode.test(nodeItem)) {
                    context.showNode(nodeId);
                }
            }

            for (Integer linkId : selection.links) {
                EdgeItem edge = (EdgeItem) vGraph.getEdge(linkId);
                if (areEndpointsVisible(edge)) {
                    context.showLink(linkId);
                }
            }
        }

        if (context.isPlayerRunning()) {
            context.updateDisplay();
        }

    }

    /**
     * Operation used to unhide all nodes and links that are hidden.
     *
     * @param dvUuid  dataview uuid.
     * @param vizUuid visualization (relgraph) uuid.
     * @throws CentrifugeException Exception thrown in case the vizUuid param is null.
     */
   @Override
   @Operation
   public void unhideAll(@QueryParam(DVUUID_PARAM) String dvUuid, @QueryParam(VIZUUID_PARAM) String vizUuid)
         throws CentrifugeException {
      if (vizUuid == null) {
         throw new CentrifugeException("Missing required parameter: vduuid");
      }
      GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);

      if (context != null) {
         synchronized (context) {
            VisualGraph vGraph = context.getVisualGraph();
            Predicate<Node> canRenderNode = GraphContext.Predicates.IsNodeVisualized;
            IntIterator it = vGraph.nodeRows();

            while (it.hasNext()) {
               Integer nodeId = (Integer) it.next();
               NodeItem nodeItem = (NodeItem) vGraph.getNode(nodeId);

               if (canRenderNode.test(nodeItem)) {
                  context.showNode(nodeId);
               }
            }
            for (Iterator iter = vGraph.edges(); iter.hasNext(); ) {
               Edge edge = (Edge) iter.next();
               LinkStore edgeDetails = GraphManager.getEdgeDetails(edge);
               NodeStore sourceDetails = GraphManager.getNodeDetails(edge.getSourceNode());
               NodeStore targetDetails = GraphManager.getNodeDetails(edge.getTargetNode());

                // If the edge is bundled or one of its endpoints is bundled,
                // then the link shouldn't be shown.
               if (!edgeDetails.isBundled() && sourceDetails.isDisplayable() && targetDetails.isDisplayable()) {
                  context.showLink(edge.getRow());
               }
            }
         }
      }
   }

    private boolean areEndpointsVisible(EdgeItem edge) {
        boolean results = false;

        if (edge != null) {
            LinkStore details = GraphManager.getEdgeDetails(edge);
            NodeStore ep1 = details.getFirstEndpoint();
            NodeStore ep2 = details.getSecondEndpoint();
            results = ep1.isDisplayable() && ep2.isDisplayable();
        }

        return results;
    }

    @Override
    @Operation
    public List<Integer> hideSelection(@QueryParam(DVUUID_PARAM) String dvUuid,
                                       @QueryParam(VIZUUID_PARAM) String vizUuid) throws CentrifugeException {
        if (vizUuid == null) {
            throw new CentrifugeException("Missing required parameter: vduuid");
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        if (context == null) {
            return null;
        }

        synchronized (context) {
            List<Integer> hideIds = new ArrayList<Integer>();
            String selectionName = GraphManager.DEFAULT_SELECTION;
            SelectionModel selection = context.getOrCreateSelection(selectionName);

            for (Integer nodeId : selection.nodes) {
                context.hideNode(nodeId);
                hideIds.add(nodeId);
            }

            for (Integer linkId : selection.links) {
                context.hideLink(linkId);
                hideIds.add(linkId);
            }

            return hideIds;
        }

    }

   @Override
   public void showOnlySelection(String dvUuid, String vizUuid) {
      String selectionName = GraphManager.DEFAULT_SELECTION;

      invertSelection(selectionName, vizUuid);

      GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);

      if (context != null) {
         synchronized (context) {
            SelectionModel selection = context.getOrCreateSelection(selectionName);

            for (Integer nodeId : selection.nodes) {
                context.hideNode(nodeId);
            }
            for (Integer linkId : selection.links) {
                context.hideLink(linkId);
            }
         }
         invertSelection(selectionName, vizUuid);
      }
   }

    @Override
    public void hideNodeById(String vizUuid, ArrayList<Integer> nodeIds) {
        checkNotNull(vizUuid, "Missing required parameter: vizUuid");
        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        checkNotNull(context, "Could not get Graph Context");
//        String selectionName = GraphManager.DEFAULT_SELECTION;
        synchronized (context) {
//            SelectionModel selection = context.getOrCreateSelection(selectionName);
            for (Integer nodeId : nodeIds) {
                context.hideNode(nodeId);
            }
        }
    }

    @Override
    public void unhideNodeById(String vizUuid, ArrayList<Integer> nodeIds) {
        checkNotNull(vizUuid, "Missing required parameter: vizUuid");
        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        checkNotNull(context, "Could not get Graph Context");

        synchronized (context) {
            for (Integer nodeId : nodeIds) {
                context.showNode(nodeId);
            }
        }
    }

    @Override
    public void hideLinkById(String vizUuid, ArrayList<Integer> linkIds) {
        checkNotNull(vizUuid, "Missing required parameter: vizUuid");
        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        checkNotNull(context, "Could not get Graph Context");
//        String selectionName = GraphManager.DEFAULT_SELECTION;
        synchronized (context) {
//            SelectionModel selection = context.getOrCreateSelection(selectionName);
            for (Integer linkId : linkIds) {
                context.hideLink(linkId);
            }
        }
    }

    @Override
    public void unhideLinkById(String vizUuid, ArrayList<Integer> linkIds) {
        checkNotNull(vizUuid, "Missing required parameter: vizUuid");
        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        checkNotNull(context, "Could not get Graph Context");

        synchronized (context) {
            for (Integer linkID : linkIds) {
                context.showLink(linkID);
            }
        }
    }

   @Override
   @Operation
   public void hideUnSelected(@QueryParam(DVUUID_PARAM) String dvUuid, @QueryParam(VIZUUID_PARAM) String vizUuid)
         throws CentrifugeException {
      if (vizUuid == null) {
         throw new CentrifugeException("Missing required parameter: vduuid");
      }
      GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);

      if (context != null) {
         synchronized (context) {
            VisualGraph vGraph = context.getVisualGraph();
            String selectionName = GraphManager.DEFAULT_SELECTION;
            SelectionModel selection = context.getOrCreateSelection(selectionName);
            ArrayList<Integer> selectedNodeKeys = new ArrayList<Integer>();

            for (Integer nodeId : selection.nodes) {
                selectedNodeKeys.add(nodeId);
            }
            ArrayList<Integer> selectedEdgeKeys = new ArrayList<Integer>();

            for (Integer linkId : selection.links) {
                selectedEdgeKeys.add(linkId);
            }
            Predicate<Node> nodePredicate = getDisplayableNodePredicate();
            ArrayList<Integer> nodeKeys = new ArrayList<Integer>();

            for (Iterator<NodeItem> nodes = vGraph.nodes(); nodes.hasNext();) {
               NodeItem nodeItem = nodes.next();

               if (nodePredicate.test(nodeItem) && nodeItem.isVisible()) {
                  nodeKeys.add(nodeItem.getRow());
               }
            }
            Predicate<Edge> edgePredicate = getDisplayableEdgePredicate();
            ArrayList<Integer> edgeKeys = new ArrayList<Integer>();

            for (Iterator<EdgeItem> edges = vGraph.edges(); edges.hasNext();) {
               EdgeItem edgeItem = edges.next();

               if (edgePredicate.test(edgeItem) && edgeItem.isVisible()) {
                  edgeKeys.add(edgeItem.getRow());
               }
            }
            nodeKeys = (ArrayList<Integer>) CollectionUtils.subtract(nodeKeys, selectedNodeKeys);
            edgeKeys = (ArrayList<Integer>) CollectionUtils.subtract(edgeKeys, selectedEdgeKeys);

            for (Integer nodeId : nodeKeys) {
               context.hideNode(nodeId);
            }
            for (Integer linkId : edgeKeys) {
               context.hideLink(linkId);
            }
         }
      }
   }

    @Override
    public Boolean isEmpty(String vizUuid) {
        try {
            checkNotNull(vizUuid, "Missing required parameter: vizUuid");
//			checkNotNull(dvUuid, "Missing required parameter: dvUuid");
            GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
            if (context == null) {
                return true;
            }
            TableIterator iterator = context.getGraphData().getNodeTable().iterator();
            return !iterator.hasNext();
        } catch (TaskAbortedException e) {
            return false;
        }
    }

    @Override
    public void loadGraph(String vizUuid, String dvUuid, int viewWidth, int viewHeight) throws CentrifugeException {
        loadGraph(vizUuid, dvUuid, "" + viewWidth, "" + viewHeight);
    }

    @Override
    @Operation
    @Interruptable
    public void loadGraph(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                          @QueryParam(value = DVUUID_PARAM) String dvUuid, @QueryParam(value = "vw") String viewWidth,
                          @QueryParam(value = "vh") String viewHeight)
            throws /* CentrifugeException */csi.server.common.exception.CentrifugeException {
        // Generated by UModel. This code will be overwritten when you re-run
        // code generation.

        if (dvUuid == null) {
            throw new CentrifugeException("Missing required parameter: dvuuid");
        }

        if (vizUuid == null) {
            throw new CentrifugeException("Missing required parameter: vduuid");
        }
        Dimension vdim = new Dimension(720, 500);

        if (viewWidth != null) {
            vdim.width = Integer.parseInt(viewWidth);
        }

        if (viewHeight != null) {
            vdim.height = Integer.parseInt(viewHeight);
        }
        validiateDimensions(vdim);

        try {
            GraphContext curContext = GraphServiceUtil.getGraphContext(vizUuid);

            if (curContext != null) {

                synchronized (curContext) {
                    LOG.debug("Invalidating existing graph");
                    curContext.setInvalidated(true);
                }
            }
        } catch (TaskAbortedException tae) {
        }

        TaskHelper.checkForCancel();
        AbstractStorageService.instance().initializeData(vizUuid);
        GraphContext graphContext = GraphDataManager.loadGraphContext(dvUuid, vizUuid);
        RelGraphViewDef rgDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
        Future<?> future = GraphDataManager.saveJobs.get(rgDef.getUuid());

        if ((future == null) || future.isDone()) {
            if (graphContext != null) {
                graphContext.getDisplay().setSize(vdim);

                if ((graphContext.getNodesByRow() == null) || graphContext.getNodesByRow().isEmpty()
                        || (graphContext.getLinksByRow() == null) || graphContext.getLinksByRow().isEmpty()) {
                    initializeGraphContextMaps(graphContext, rgDef, dvUuid);
                }
                registerGraphContext(vizUuid, graphContext);
                GraphManager gm = GraphManager.getInstance();
                graphContext.setSubnetsDirty(true);
                gm.initSubgraphData(graphContext.getGraphData(), false);
                gm.computeComponents(graphContext.getGraphData());
                gm.computeComponentRegions(graphContext);

                if (rgDef.isBroadcastListener()) {
                    GraphHelper.restoreSelectionFromVizDef(rgDef);
                }
                graphContext.fitToSize();
                graphContext.zoom(.9);
                graphContext.applyLinkThemeToLegend();
                return;

            }
        }

        ThemeActionsService.updateOptionSet(rgDef);
        graphContext = new GraphContext(dvUuid, vizUuid, rgDef.getThemeUuid());
        try {
            GraphHelper.initializeGraphContext(graphContext, rgDef, vdim, true, sqlFactory, filterActionsService);
//            fitToSize(rgDef.getUuid(), (int) vdim.getWidth(), (int) vdim.getHeight());
        } catch (GraphTooManyTypesException e) {
            TaskHelper.reportError("TOO_MANY_TYPES", null);
            LOG.warn("Graph type limit exceeded");
        }
        graphContext.applyLinkThemeToLegend();
    }

    /**
     * Initializes the nodesByROw and LinksByRow maps. Usually, these maps are
     * initialized for graphContext, but for the migrated dataviews coming from
     * older versions, these maps are not set. They are used for broadcast as
     * selection. If they are not set, no selection will be done on graph.
     *
     * @param graphContext the graph Context unmarshaled from graph file (*.dat).
     * @param rgDef        the definition of the graph.
     * @param dvUuid       the dataview uuid.
     * @throws CentrifugeException thrown if no dv found, cache connection problems or any
     *                             problem encountered while setting the two maps.
     */
    private void initializeGraphContextMaps(GraphContext graphContext, RelGraphViewDef rgDef, String dvUuid)
            throws CentrifugeException {
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        if (dv == null) {
            throw new CentrifugeException("Dataview not found: " + dvUuid);
        }

        graphContext.initializeRowMaps();
        Multimap<Integer, Integer> nodesByRow = graphContext.getNodesByRow();

        Graph graph = graphContext.getGraphData();
        Iterator<Node> nodes = graph.nodes();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            int id = node.getRow();
            NodeStore details = GraphManager.getNodeDetails(node);
            Map<String, List<Integer>> rows = details.getRows();
            Collection<List<Integer>> vals = rows.values();
            for (List<Integer> list : vals) {
                for (Integer sr : list) {
                    nodesByRow.put(sr, id);
                }
            }
        }

        Multimap<Integer, Integer> edgesByRow = graphContext.getLinksByRow();
        Iterator<Edge> edges = graph.edges();
        while (edges.hasNext()) {
            Edge edge = edges.next();
            int id = edge.getRow();
            LinkStore details = GraphManager.getEdgeDetails(edge);
            Map<String, List<Integer>> rows = details.getRows();
            Collection<List<Integer>> vals = rows.values();
            for (List<Integer> list : vals) {
                for (Integer sr : list) {
                    edgesByRow.put(sr, id);
                }
            }

        }
    }

    private void validiateDimensions(Dimension dimensions) {

        if ((dimensions.width <= 0) || (dimensions.height <= 0)) {
            dimensions.width = 400;
            dimensions.height = 400;

            if (LOG.isInfoEnabled()) {
                LOG.info("Received invalid dimensions for displaying the graph, reverting to 400x400");
            }
        }
    }

    @Override
    @Operation
    public GraphInfo summary(@QueryParam(VIZUUID_PARAM) String vizId) throws CentrifugeException {
        GraphContext context = GraphServiceUtil.getGraphContext(vizId);

        if (context == null) {
            return new GraphInfo();
        }

        synchronized (context) {
            GraphInfo info = getGraphInfo(context);

            info.renderThreshold = GraphHelper.getRenderThresholdLimit(context.getVisualizationDef());
            return info;
        }

    }

    private GraphInfo getGraphInfo(GraphContext context) {

        Graph graph = context.getGraphData();
        GraphInfo info = new GraphInfo();

        // nodeCount is set to the total of nodes in graph because summary
        // should count all of them according to CTWO-5920
        info.nodeCount = graph.getNodeCount();
        info.edgeCount = graph.getEdgeCount();
        info.visualizedNodeCount = 0;

        for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
           if (GraphContext.Predicates.IsVisualizedAndDisplayable.test(nodes.next())) {
              info.visualizedNodeCount++;
           }
        }
        Map<String, TypeInfo> nodeLegendInfo = context.getNodeLegend();

        for (TypeInfo type : nodeLegendInfo.values()) {
            csi.server.common.dto.graph.TypeInfo typeSummary = new csi.server.common.dto.graph.TypeInfo();
            typeSummary.name = type.name;
            typeSummary.count = type.totalCount;
            info.nodeTypes.add(typeSummary);
        }

        Map<String, GraphLinkLegendItem> linkLegendInfo = context.getLinkLegend();

        if (linkLegendInfo != null) {
            for (GraphLinkLegendItem type : linkLegendInfo.values()) {
                csi.server.common.dto.graph.TypeInfo typeSummary = new csi.server.common.dto.graph.TypeInfo();
                typeSummary.name = type.typeName;
                typeSummary.count = type.totalCount;
                info.edgeTypes.add(typeSummary);
            }
        }

        return info;
    }

   @Override
   @Operation
   public void visualizeNodes(@QueryParam(VIZUUID_PARAM) String vizId, @PayloadParam GraphRequest request) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizId);

      if (graphContext == null) {
         if (LOG.isInfoEnabled()) {
            LOG.info("Request to visualize nodes for an unknown graph");
         }
      } else {
         synchronized (graphContext) {
            // NB: marked final for potential usage in anon Functions below.
            final Graph graphData = graphContext.getGraphData();

            TaskSession taskSession = TaskHelper.getCurrentSession();
            GraphSearchResults searchResults =
               (GraphSearchResults) taskSession.getAttribute(GraphSearchActionsService.SEARCH_RESULTS);
            Collection<Node> nodes = new HashSet<Node>();

            if (request.allNodes) {
               for (Iterator<NodeInfo> nodeInfos = searchResults.getNodes().iterator(); nodeInfos.hasNext();) {
                  nodes.add(graphData.getNode(nodeInfos.next().id.intValue()));
               }
            } else {
               if (request.nodes != null) {
                  for (Integer id : request.nodes) {
                     nodes.add(graphData.getNode(id));
                  }
               }
            }
            // When processing links, all we really need is to add the endpoints
            // to the viz.
            // the graphContext handles ensuring that links between visible
            // nodes are present.
            if (!request.allLinks) {
               if (request.links != null) {
                  for (Integer id : request.links) {
                     Edge edge = graphData.getEdge(id.intValue());

                     nodes.add(edge.getSourceNode());
                     nodes.add(edge.getTargetNode());
                  }
               }
            } else {
               List<EdgeInfo> links = searchResults.getLinks();

               for (EdgeInfo edgeInfo : links) {
                  Edge edge = graphData.getEdge(edgeInfo.id.intValue());

                  nodes.add(edge.getSourceNode());
                  nodes.add(edge.getTargetNode());
               }
            }
            graphContext.showItems(nodes);
         }
      }
   }

    @Override
    @Operation
    public void retractNodes(@QueryParam(VIZUUID_PARAM) String vizId, @QueryParam("id") String selection) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizId);
        if (graphContext == null) {
            if (LOG.isInfoEnabled()) {
                String msg = "Referenced graph data for retractNodes does not exist; ignoring request";
                LOG.info(msg);
            }
            return;
        }

        synchronized (graphContext) {
            if (selection == null) {
                selection = GraphManager.DEFAULT_SELECTION;
            }

            SelectionModel selectionModel = graphContext.getSelection(selection);
            graphContext.retractVisualItems(selectionModel);
        }

    }

    @Override
    @Operation
    public GraphStateFlags getGraphStateFlags(@QueryParam(VIZUUID_PARAM) String vizId) throws CentrifugeException {
        GraphContext context = GraphServiceUtil.getGraphContext(vizId);
        GraphStateFlags stateFlags;
        if (context != null) {
            stateFlags = context.getGraphStateFlags();
        } else {
            stateFlags = new GraphStateFlags();
        }
        return stateFlags;
    }

    @Override
    @Operation
    public SelectionModel clearSelection(@QueryParam(value = "id") String id,
                                         @QueryParam(value = VIZUUID_PARAM) String vizuuid) {
        GraphContext context = GraphServiceUtil.getGraphContext(vizuuid);
        if (context == null) {
            return new SelectionModel();
        }
        synchronized (context) {
            SelectionModel selection = getSelectionModel(id, vizuuid);
            selection.reset();
            selectionBroadcastCache.addSelection(vizuuid, selection);
            context.updateDisplay();
            return selection;
        }

    }

    @Override
    @Operation
    public SelectionModel select(@QueryParam(value = VIZUUID_PARAM) String vizuuid,
                                 @QueryParam(RESET) Boolean resetSelection, @QueryParam(CLEAR_NODES) Boolean resetNodes,
                                 @QueryParam(CLEAR_LINKS) Boolean resetLinks, @PayloadParam GraphRequest request)
            throws CentrifugeException {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

        synchronized (graphContext) {
            String selectionName = GraphManager.DEFAULT_SELECTION;
            SelectionModel selection = graphContext.getOrCreateSelection(selectionName);
            if ((resetSelection != null) && resetSelection.booleanValue()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Clearing default selection for Graph visualization: " + vizuuid);
                }

                selection.reset();
            }

            if ((resetNodes != null) && resetNodes.booleanValue()) {
                selection.nodes.clear();
            }

            if ((resetLinks != null) && resetLinks.booleanValue()) {
                selection.links.clear();
            }

            GraphSearchResults searchResults = (GraphSearchResults) taskSession
                    .getAttribute(GraphSearchActionsService.SEARCH_RESULTS);
            if (request.useSearchResults && (searchResults == null)) {
                throw new CentrifugeException("No search has been performed yet");
            }

            if (request.useSearchResults && request.allNodes) {
                for (NodeInfo nodeInfo : searchResults.getNodes()) {
                    selection.nodes.add(nodeInfo.id.intValue());
                }
            } else {
                if (request.nodes != null) {
                    selection.nodes.addAll(request.nodes);
                }
            }

            if (request.useSearchResults && request.allLinks) {
                for (EdgeInfo edgeInfo : searchResults.getLinks()) {
                    selection.links.add(edgeInfo.id.intValue());
                }
            } else {
                if (request.links != null) {
                    selection.links.addAll(request.links);
                }
            }

            if (graphContext.isPlayerRunning()) {
                graphContext.updateDisplay();
            }

            GraphHelper.removeHiddenFromSelection(selection);
            return selection;

        }
    }

   @Override
   @Operation
   public SelectionModel selectAll(@QueryParam(value = "id") String id,
                                   @QueryParam(value = VIZUUID_PARAM) String vizuuid) {
      SelectionModel selectionModel = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

      if (graphContext == null) {
         LOG.info("Non-existent graph data referenced for selectAll operation.");
         selectionModel = new SelectionModel();
      } else {
         synchronized (graphContext) {
            VisualGraph graph = graphContext.getVisualGraph();
            Predicate<Node> nodeFilter = getDisplayableNodePredicate();
            Predicate<Edge> edgeFilter = getDisplayableEdgePredicate();
            selectionModel = getSelectionModel(id, vizuuid);

            selectionModel.reset();

            for (Iterator<NodeItem> nodes = graph.nodes(); nodes.hasNext();) {
               NodeItem nodeItem = nodes.next();

               if (nodeFilter.test(nodeItem) && nodeItem.isVisible()) {
                  selectionModel.nodes.add(Integer.valueOf(nodeItem.getRow()));
               }
            }
            for (Iterator<EdgeItem> edges = graph.edges(); edges.hasNext();) {
               EdgeItem edgeItem = edges.next();

               if (edgeFilter.test(edgeItem) && edgeItem.isVisible()) {
                  selectionModel.links.add(Integer.valueOf(edgeItem.getRow()));
               }
            }
            if (graphContext.isPlayerRunning()) {
               graphContext.updateDisplay();
            }
//            already protected above
//            GraphHelper.removeHiddenFromSelection(selectionModel);
         }
      }
      return selectionModel;
   }

   @Override
   @Operation
   public SelectionModel invertSelection(@QueryParam(value = "id") String id,
                                         @QueryParam(value = VIZUUID_PARAM) String vizuuid) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

      synchronized (graphContext) {
         VisualGraph graph = graphContext.getVisualGraph();
         Predicate<Node> nodeFilter = getDisplayableNodePredicate();
         Predicate<Edge> edgeFilter = getDisplayableEdgePredicate();
         SelectionModel selectionModel = getSelectionModel(id, vizuuid);
         SelectionModel newSelectionModel = new SelectionModel();

         newSelectionModel.merge(selectionModel);
         selectionModel.reset();

         if (!newSelectionModel.links.isEmpty()) {
            // If links are selected, invert selection on links
            for (Iterator<EdgeItem> edges = graph.edges(); edges.hasNext();) {
               EdgeItem edgeItem = edges.next();

               if (edgeFilter.test(edgeItem) && edgeItem.isVisible() &&
                   !newSelectionModel.links.contains(edgeItem.getRow())) {
                  selectionModel.links.add(edgeItem.getRow());
               }
            }
         }
         if (!newSelectionModel.nodes.isEmpty()) {
            // If nodes are selected, invert selection on nodes
            for (Iterator<NodeItem> nodes = graph.nodes(); nodes.hasNext();) {
               NodeItem nodeItem = nodes.next();

               if (nodeFilter.test(nodeItem) && nodeItem.isVisible() &&
                   !newSelectionModel.nodes.contains(nodeItem.getRow())) {
                  selectionModel.nodes.add(nodeItem.getRow());
               }
            }
         }
         if (graphContext.isPlayerRunning()) {
            graphContext.updateDisplay();
         }
         GraphHelper.removeHiddenFromSelection(selectionModel);
         return selectionModel;
      }
   }

    @Override
    @Operation
    public SelectionModel deselectRegion(@QueryParam(value = VIZUUID_PARAM) String vizuuid,
                                         @QueryParam(value = "x1") Double x, @QueryParam(value = "y1") Double y, @QueryParam(value = "x2") Double x2,
                                         @QueryParam(value = "y2") Double y2, @QueryParam(value = "id") String selid,
                                         @QueryParam("reset") Boolean reset) throws CentrifugeException {

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

        synchronized (graphContext) {
            String id = getSelectionIdFromRequest(selid);
            SelectionModel selectionModel = getSelectionModel(id, vizuuid);

            Visualization vis1 = getVisualization(vizuuid);
            Visualization vis = vis1;
            if (vis == null) {
                throw new CentrifugeException("Graph has not been loaded.");
            }

            Display display = vis.getDisplay(0);
            Point2D tl = display.getAbsoluteCoordinate(new Point2D.Double(x.doubleValue(), y.doubleValue()), null);
            Point2D br = display.getAbsoluteCoordinate(new Point2D.Double(x2.doubleValue(), y2.doubleValue()),
                    null);

            Rectangle2D region = new Rectangle2D.Double();
            region.setFrameFromDiagonal(tl, br);
            if (region.isEmpty() || (region.getWidth() <= 0) || (region.getHeight() <= 0)) {
                return selectionModel;
            }

            if (Boolean.TRUE.equals(reset)) {
                selectionModel.reset();
            }

            synchronized (vis) {
                synchronized (display) {
                    // Predicate visible = (Predicate)
                    // FunctionTable.createFunction("VISIBLE");
                    // Iterator items = vis.items(visible);
                    Iterator items = vis.items();
                    while (items.hasNext()) {
                        VisualItem vi = (VisualItem) items.next();
                        if (!vi.getBoolean(vi.VISIBLE)) {
                            continue;
                        }
                        if (vi instanceof TableNodeItem) {
                            if (region.contains(vi.getX(), vi.getY())) {
                                selectionModel.nodes.remove(vi.getRow());
                            }
                        } else {
                            Edge edg = (Edge) vi;
                            double nx1 = ((VisualItem) edg.getSourceNode()).getX();
                            double ny1 = ((VisualItem) edg.getSourceNode()).getY();

                            double nx2 = ((VisualItem) edg.getTargetNode()).getX();
                            double ny2 = ((VisualItem) edg.getTargetNode()).getY();

                            if (region.intersectsLine(nx1, ny1, nx2, ny2)) {
                                selectionModel.links.remove(vi.getRow());
                            }
                        }
                    }
                }
            }

            if (graphContext.isPlayerRunning()) {
                graphContext.updateDisplay();
            }
            GraphHelper.removeHiddenFromSelection(selectionModel);
            return selectionModel;

        }
    }

    @Override
    @Operation
    public SelectionModel selectRegion(@QueryParam(value = VIZUUID_PARAM) String vizuuid,
                                       @QueryParam(value = "x1") Double x, @QueryParam(value = "y1") Double y, @QueryParam(value = "x2") Double x2,
                                       @QueryParam(value = "y2") Double y2, @QueryParam(value = "id") String selid,
                                       @QueryParam("reset") Boolean reset) throws CentrifugeException {

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

        synchronized (graphContext) {
            String id = getSelectionIdFromRequest(selid);
            SelectionModel selectionModel = getSelectionModel(id, vizuuid);

            Visualization vis1 = getVisualization(vizuuid);
            Visualization vis = vis1;
            if (vis == null) {
                throw new CentrifugeException("Graph has not been loaded.");
            }

            Display display = vis.getDisplay(0);
            Point2D tl = display.getAbsoluteCoordinate(new Point2D.Double(x.doubleValue(), y.doubleValue()), null);
            Point2D br = display.getAbsoluteCoordinate(new Point2D.Double(x2.doubleValue(), y2.doubleValue()),
                    null);

            Rectangle2D region = new Rectangle2D.Double();
            region.setFrameFromDiagonal(tl, br);
            if (region.isEmpty() || (region.getWidth() <= 0) || (region.getHeight() <= 0)) {
                return selectionModel;
            }

            if (Boolean.TRUE.equals(reset)) {
                selectionModel.reset();
            }

            synchronized (vis) {
                synchronized (display) {
                    // Predicate visible = (Predicate)
                    // FunctionTable.createFunction("VISIBLE");
                    // Iterator items = vis.items(visible);
                    Iterator items = vis.items();
                    while (items.hasNext()) {
                        VisualItem vi = (VisualItem) items.next();
                        if (!vi.getBoolean(vi.VISIBLE)) {
                            continue;
                        }
                        if (vi instanceof TableNodeItem) {
                            if (region.contains(vi.getX(), vi.getY())) {
                                selectionModel.nodes.add(vi.getRow());
                            }
                        } else {
                            Edge edg = (Edge) vi;
                            double nx1 = ((VisualItem) edg.getSourceNode()).getX();
                            double ny1 = ((VisualItem) edg.getSourceNode()).getY();

                            double nx2 = ((VisualItem) edg.getTargetNode()).getX();
                            double ny2 = ((VisualItem) edg.getTargetNode()).getY();

                            if (region.intersectsLine(nx1, ny1, nx2, ny2)) {
                                selectionModel.links.add(vi.getRow());
                            }
                        }
                    }
                }
            }

            if (graphContext.isPlayerRunning()) {
                graphContext.updateDisplay();
            }
            GraphHelper.removeHiddenFromSelection(selectionModel);
            return selectionModel;

        }
    }

   @Override
   @Operation
   public int degrees(@QueryParam(value = VIZUUID_PARAM) String vizuuid, @QueryParam("n") Integer nSteps,
                      @QueryParam(value = "id") String id) {
      int nodeCount = 0;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            if ((id == null) || (id.trim().length() == 0)) {
               id = GraphManager.DEFAULT_SELECTION;
            }
            SelectionModel selection = graphContext.getSelection(id);

            if ((selection != null) && !selection.nodes.isEmpty()) {
               Graph visibleGraph = graphContext.getVisibleGraph();
               Collection<Node> roots = new HashSet<Node>();
               Collection<Node> targets = new ArrayList<Node>();
               Function<Integer,Node> transform = GraphContext.Functions.mapIdToNodeFunction(visibleGraph);

               for (Integer node : selection.nodes) {
                  roots.add(transform.apply(node));
               }
               for (BreadthFirstIterator nodes =
                      new BreadthFirstIterator(roots.iterator(), nSteps.intValue(), Constants.NODE_TRAVERSAL);
                    nodes.hasNext();) {
                  Node node = (Node) nodes.next();

                  if (!roots.contains(node)) {
                     targets.add(node);

                     if (GraphContext.Predicates.IsNodeHidden.test(node)) {
                        nodeCount++;
                     }
                  }
               }
               graphContext.showNodes(targets);
            }
         }
      }
      return nodeCount;
   }

   @Override
   @Operation
   public void selectLinkById(@QueryParam(value = VIZUUID_PARAM) String vizuuid, @QueryParam(value = "id") String id,
                              @QueryParam(value = "reset") String resetString) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

      synchronized (graphContext) {
         String selectionName = DEFAULT_SELECTION;
         SelectionModel selectionModel = getSelectionModel(selectionName, vizuuid);
         Visualization vis = getVisualization(vizuuid);

         if (vis != null) {
            Graph graph = (Graph) vis.getSourceData("graph");
            Boolean reset = Boolean.valueOf(resetString);

            if (reset.booleanValue()) {
                selectionModel.links.clear();
            }
            Iterator<Edge> edges = graph.edges();

            while (edges.hasNext()) {
                Edge edgeItem = edges.next();

                if (edgeItem.getRow() == Integer.parseInt(id)) {
                    if (selectionModel.links.contains(Integer.parseInt(id))) {
                        selectionModel.links.remove(edgeItem.getRow());
                    } else {
                        selectionModel.links.add(edgeItem.getRow());
                    }
                }
            }
            if (graphContext.isPlayerRunning()) {
               graphContext.updateDisplay();
            }
         }
      }
   }

    @Override
    @Operation
    public void selectNodeById(@QueryParam(value = VIZUUID_PARAM) String vizuuid, @QueryParam(value = "id") Integer id,
                               @QueryParam(value = "reset") String resetString) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

        synchronized (graphContext) {
            String selectionName = DEFAULT_SELECTION;
            SelectionModel selectionModel = getSelectionModel(selectionName, vizuuid);
            Boolean reset = Boolean.valueOf(resetString);

            if (reset.booleanValue()) {
                selectionModel.nodes.clear();
            }
            if (selectionModel.nodes.contains(id)) {
                selectionModel.nodes.remove(id);
            } else {
                selectionModel.nodes.add(id);
            }

            if (graphContext.isPlayerRunning()) {
                graphContext.updateDisplay();
            }

        }
    }

   @Override
   @Operation
   public SelectionModel selectionByPoint(@QueryParam(value = VIZUUID_PARAM) String vizuuid,
                                          @QueryParam(value = "x") Double x, @QueryParam(value = "y") Double y,
                                          @QueryParam(value = "reset") Boolean reset) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

      synchronized (graphContext) {
         String selectionName = DEFAULT_SELECTION;
         SelectionModel selectionModel = getSelectionModel(selectionName, vizuuid);

         if (reset.booleanValue()) {
            selectionModel.nodes.clear();
            selectionModel.links.clear();
         }
         Visualization vis = getVisualization(vizuuid);

         if (vis != null) {
            synchronized (vis) {
               Display display = vis.getDisplay(0);

               synchronized (display) {
                  VisualItem item = display.findItem(GraphManager.getXYFrom(x, y));

                  if (item == null) {
                     item = getVisualLinkNear(graphContext.getVisualGraph().edges(), x.intValue(), y.intValue(), vis);
                  }
                  if (item instanceof Node) {
                     if (selectionModel.nodes.contains(item.getRow())) {
                        selectionModel.nodes.remove(item.getRow());
                     } else {
                        selectionModel.nodes.add(item.getRow());
                     }
                  } else if (item instanceof Edge) {
                     if (selectionModel.links.contains(item.getRow())) {
                        selectionModel.links.remove(item.getRow());
                     } else {
                        selectionModel.links.add(item.getRow());
                     }
                  }
               }
            }
            if (graphContext.isPlayerRunning()) {
                graphContext.updateDisplay();
            }
            GraphHelper.removeHiddenFromSelection(selectionModel);
         }
         return selectionModel;
      }
   }

    @Override
    @Operation
    public void selectItemAt(@QueryParam(value = VIZUUID_PARAM) String vizuuid, @QueryParam(value = "x") Double x,
                             @QueryParam(value = "y") Double y, @QueryParam(value = "reset") Boolean reset) {
        // same as selectionByPoint but with no return parameter
        selectionByPoint(vizuuid, x, y, reset);
    }

    private String getSelectionIdFromRequest(String id) {
        id = (id == null) ? "" : id.trim();

        if (id.length() == 0) {
            id = DEFAULT_SELECTION;
        }
        return id;
    }

   @Override
   @Operation
   public SelectionModel selectNodesByType(@QueryParam(value = VIZUUID_PARAM) String vizuuid,
                                           @QueryParam(value = DVUUID_PARAM) String dvUuid, @QueryParam(value = "nodeType") String nodeType,
                                           @QueryParam(value = "addToSelection") Boolean addToSelection) {
      String operation = addToSelection.booleanValue()
                            ? GraphConstants.SELECTION_OPERATION_APPEND
                            : GraphConstants.SELECTION_OPERATION_CLEAR;
      SelectionModel selectionModel = toggleNodeSelectionByType(vizuuid, dvUuid, nodeType, operation);
      GraphHelper.removeHiddenFromSelection(selectionModel);

      return selectionModel;
   }

    @Override
    @Operation
    public SelectionModel toggleNodeSelectionByType(@QueryParam(value = VIZUUID_PARAM) String vizuuid,
                                                    @QueryParam(value = DVUUID_PARAM) String dvUuid, @QueryParam(value = "nodeKey") String nodeKey,
                                                    @QueryParam(value = "selectionOperation") String selectionOperation) {

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);

        synchronized (graphContext) {
            String selectionName = DEFAULT_SELECTION;
            SelectionModel selectionModel = getSelectionModel(selectionName, vizuuid);
            Visualization vis = getVisualization(vizuuid);

            if (vis != null) {
            GraphContext context = GraphServiceUtil.getGraphContext(vizuuid);
            Graph graph = (Graph) vis.getSourceData("graph");
            VisualGraph visibleGraph = graphContext.getVisualGraph();
            GraphDataActions graphDataActions = new GraphDataActions();

            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_CLEAR)) {
                selectionModel.nodes.clear();
                selectionModel.links.clear();
            }

            SelectionModel copySelectionModel;
            if (nodeKey.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                String type = nodeKey.substring(GraphConstants.CSI_INTERNAL_NAMESPACE.length() + 1);
                if (type.equals(GraphConstants.BUNDLED_NODES)) {
                    for (Iterator nodes = graph.nodes(); nodes.hasNext(); ) {
                        Node node = (Node) nodes.next();
                        // check if tuple has not been invalidate (row still
                        // exist in backing table)
                        if (node.getRow() != -1) {
                            NodeStore details = GraphManager.getNodeDetails(node);
                            if (details.isBundle()) {
                                VisualItem vizNode = (VisualItem) visibleGraph.getNode(node.getRow());
                                if (vizNode.isVisible()) {
                                    if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                                        selectionModel.nodes.add(node.getRow());
                                    } else {
                                        selectionModel.nodes.remove(node.getRow());
                                    }
                                }
                            }
                        }
                    }
                } else if (GraphConstants.NEW_GENERATION_FIELD_TYPE.equals(type)) {
                    copySelectionModel = context.getSelection(GraphConstants.NEW_GENERATION);
                    if (copySelectionModel != null) {
                        if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                            selectionModel.nodes.addAll(copySelectionModel.nodes);
                        } else {
                            selectionModel.nodes.removeAll(copySelectionModel.nodes);
                        }
                    }
                } else if (GraphConstants.UPDATED_GENERATION_FIELD_TYPE.equals(type)) {
                    copySelectionModel = context.getSelection(GraphConstants.UPDATED_GENERATION);
                    if (copySelectionModel != null) {
                        if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                            selectionModel.nodes.addAll(copySelectionModel.nodes);
                        } else {
                            selectionModel.nodes.removeAll(copySelectionModel.nodes);
                        }
                    }
                } else if ("multitype".equals(type)) {
                    for (Iterator nodes = graph.nodes(); nodes.hasNext(); ) {
                        Node node = (Node) nodes.next();
                        // check if tuple has not been invalidate (row still
                        // exist in backing table)
                        if (node.getRow() != -1) {
                            NodeStore details = GraphManager.getNodeDetails(node);
                            if (details.getTypes().size() > 1) {
                                VisualItem vizNode = (VisualItem) visibleGraph.getNode(node.getRow());
                                if (vizNode.isVisible()) {
                                    if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                                        selectionModel.nodes.add(node.getRow());
                                    } else {
                                        selectionModel.nodes.remove(node.getRow());
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (GraphConstants.ALL_FIELD_TYPE.equals(nodeKey)) {
                IntIterator it = visibleGraph.nodeRows();
                while (it.hasNext()) {
                    Integer nodeId = (Integer) it.next();
                    NodeItem nodeItem = (NodeItem) visibleGraph.getNode(nodeId);

                    if ((nodeItem != null) && nodeItem.isVisible()) {
                        selectionModel.nodes.add(nodeId);
                        if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                            selectionModel.nodes.add(nodeId);
                        } else {
                            selectionModel.nodes.remove(nodeId);
                        }
                    }
                }
            } else {

                // TODO Because the NODE_TYPES_LIST graph property is not set
                // when bundling, the data is
                // not consistent. Update the property so that the node types
                // are not updated every time.
                // Same goes for edges.

                Multimap<String, Node> typesMap; // = (Multimap<String, Node>)
                // graph.getClientProperty(GraphDataActions.NODE_TYPES_LIST);
                typesMap = graphDataActions.buildNodeTypes(graph);
                graph.putClientProperty(GraphDataActions.NODE_TYPES_LIST, typesMap);
                Iterable<Node> typesList = typesMap.get(nodeKey);
                for (Node nodeItem : typesList) {
                    // check if tuple has not been invalidate (row still exist
                    // in backing table)
                    if (nodeItem.getRow() != -1) {
                        VisualItem vizNode = (VisualItem) visibleGraph.getNode(nodeItem.getRow());
                        if (vizNode.isVisible()) {
                            if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                                selectionModel.nodes.add(nodeItem.getRow());
                            } else {
                                selectionModel.nodes.remove(nodeItem.getRow());
                            }
                        }
                    }
                }
            }

            if (graphContext.isPlayerRunning()) {
                graphContext.updateDisplay();
            }
            GraphHelper.removeHiddenFromSelection(selectionModel);
            }
            return selectionModel;
        }
    }

    @Override
    @Operation
    public SelectionModel toggleLinkSelectionByType(@QueryParam(value = VIZUUID_PARAM) String vizuuid,
                                                    @QueryParam(value = DVUUID_PARAM) String dvUuid, @QueryParam(value = "linkKey") String linkKey,
                                                    @QueryParam(value = "selectionOperation") String selectionOperation) {
        GraphContext context = GraphServiceUtil.getGraphContext(vizuuid);

        synchronized (context) {
            String selectionName = DEFAULT_SELECTION;
            SelectionModel selectionModel = getSelectionModel(selectionName, vizuuid);
            Visualization vis = getVisualization(vizuuid);

            if (vis != null) {
            // FIXME: why aren't we using GraphContext methods here?
            Graph graph = (Graph) vis.getSourceData("graph");
            VisualGraph visibleGraph = context.getVisualGraph();
            GraphDataActions graphDataActions = new GraphDataActions();

            if (selectionOperation.equals(GraphConstants.SELECTION_OPERATION_CLEAR)) {
                selectionModel.nodes.clear();
                selectionModel.links.clear();
            }

            if (linkKey.equals(GraphConstants.CSI_INTERNAL_NAMESPACE + "." + GraphConstants.BUNDLED_LINKS)) {
//                String type = linkKey.substring(GraphConstants.CSI_INTERNAL_NAMESPACE.length() + 1);

                for (Iterator edges = graph.edges(); edges.hasNext(); ) {
                    Edge edge = (Edge) edges.next();
                    // check if tuple has not been invalidate (row still exist in backing table)
                    if (edge.getRow() != -1) {
                        NodeStore sourceDetails = GraphManager.getNodeDetails(edge.getSourceNode());
                        NodeStore targetDetails = GraphManager.getNodeDetails(edge.getTargetNode());
                        if ((sourceDetails.isBundle() || targetDetails.isBundle()) && !targetDetails.isBundled()
                                && !sourceDetails.isBundled()) {
                            if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                                selectionModel.links.add(edge.getRow());
                            } else {
                                selectionModel.links.remove(edge.getRow());
                            }
                        }
                    }
                }
            } else if (linkKey.equals(GraphConstants.CSI_INTERNAL_NAMESPACE + ".Newly Added.link")) {
                SelectionModel copySelectionModel = context.getSelection(GraphConstants.NEW_GENERATION);
                if (copySelectionModel != null) {
                    if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                        selectionModel.links.addAll(copySelectionModel.links);
                    } else {
                        selectionModel.links.removeAll(copySelectionModel.links);
                    }
                }
            } else if (linkKey.equals(GraphConstants.CSI_INTERNAL_NAMESPACE + ".In Common.link")) {
                SelectionModel copySelectionModel = context.getSelection(GraphConstants.UPDATED_GENERATION);
                if (copySelectionModel != null) {
                    if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                        selectionModel.links.addAll(copySelectionModel.links);
                    } else {
                        selectionModel.links.removeAll(copySelectionModel.links);
                    }
                }
            } else if (linkKey.equals(GraphConstants.CSI_INTERNAL_NAMESPACE + ".multitype.link")) {
                for (Iterator edges = graph.edges(); edges.hasNext(); ) {
                    Edge edge = (Edge) edges.next();
                    // check if tuple has not been invalidate (row still exist in backing table)
                    if (edge.getRow() != -1) {
                        LinkStore details = GraphManager.getEdgeDetails(edge);
                        if (details.getTypes().size() > 1) {
                            VisualItem vizEdge = (VisualItem) visibleGraph.getEdge(edge.getRow());
                            if (vizEdge.isVisible()) {
                                if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                                    selectionModel.links.add(edge.getRow());
                                } else {
                                    selectionModel.links.remove(edge.getRow());
                                }
                            }
                        }
                    }
                }
            } else if (GraphConstants.ALL_FIELD_TYPE.equals(linkKey)) {
                IntIterator it = visibleGraph.edgeRows();
                while (it.hasNext()) {
                    Integer linkId = (Integer) it.next();
                    EdgeItem edgeItem = (EdgeItem) visibleGraph.getEdge(linkId);
                    if ((edgeItem != null) && edgeItem.isVisible()) {
                        if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                            selectionModel.links.add(edgeItem.getRow());
                        } else {
                            selectionModel.links.remove(edgeItem.getRow());
                        }
                    }
                }
            } else {
                Multimap<String, Edge> linkTypesMap = graphDataActions.getOrCreateLinkTypes(graph);
                Iterable<Edge> typesList = linkTypesMap.get(linkKey);
                for (Edge edgeItem : typesList) {
                    // check if tuple has not been invalidated (row still exist
                    // in backing table)
                    if (edgeItem.getRow() != -1) {
                        VisualItem vizLink = (VisualItem) visibleGraph.getEdge(edgeItem.getRow());
                        if (vizLink.isVisible()) {
                            if (!selectionOperation.equals(GraphConstants.SELECTION_OPERATION_DESELECT)) {
                                selectionModel.links.add(edgeItem.getRow());
                            } else {
                                selectionModel.links.remove(edgeItem.getRow());
                            }
                        }
                    }
                }
            }

            if (context.isPlayerRunning()) {
                context.updateDisplay();
            }
            GraphHelper.removeHiddenFromSelection(selectionModel);
            }
            return selectionModel;
        }
    }

   @Override
   @Operation
   public void clearMergeHighlights(@QueryParam(value = VIZUUID_PARAM) String vizuuid) {
      GraphContext context = GraphServiceUtil.getGraphContext(vizuuid);

      if (context != null) {
         synchronized (context) {
            context.removeSelectionModel(GraphConstants.NEW_GENERATION);
            context.removeSelectionModel(GraphConstants.UPDATED_GENERATION);
         }
      }
   }

    @Override
    public void clearGraphBeforeLoad(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                                     @QueryParam(value = "value") String value) {
        // GraphContext graphContext =
        // GraphServiceUtil.getGraphContext(vizUuid);
        // GraphConstants.eLayoutAlgorithms layout =
        // LayoutHelper.getLayout(value);
        // LayoutCache.getInstance().addLayout(graphContext.getVizUuid(),
        // layout);
        clearCache(vizUuid);
        GraphConstants.eLayoutAlgorithms layout = LayoutHelper.getLayout(value);
        LayoutCache.getInstance().addLayout(vizUuid, layout);
    }

    @Override
    public void clearCache(String vizUuid) {
        AbstractStorageService service = AbstractStorageService.instance();
        if (service.hasVisualizationData(vizUuid)) {
            service.resetData(vizUuid);
        }
    }

    @Override
    @Operation
    public List<CsiMap<String, String>> componentLayoutAction(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                                                              @QueryParam(value = "componentId") String componentIDParam, @QueryParam(value = "x") String x,
                                                              @QueryParam(value = "y") String y, @QueryParam(value = "action") String action,
                                                              @QueryParam(value = "value") String value, @QueryParam(value = "async") String asyncParam)
            throws CentrifugeException {

        // TODO: verify we have inputs
//        String componentId = getComponentIdentifierFromRequest(vizUuid, componentIDParam, x, y);

        GraphManager graphManager = GraphManager.getInstance();
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

        if (!Strings.isNullOrEmpty(action)) {
            try {
//                int iterations = Integer.parseInt(action);
                graphContext.getVisualizationDef().setPropertyValue("moreIterations", action);
            } catch (Exception ignored) {
            }
        }

        if (graphContext == null) {
            return null;
        }

        synchronized (graphContext) {
            // int animationThreshold = Configuration.instance().getGraphConfig().getLayoutAnimationThreshold();
            GraphConstants.eLayoutAlgorithms layout = LayoutHelper.getLayout(value);
            LayoutCache.getInstance().addLayout(graphContext.getVizUuid(), layout);

            boolean autoSizeCheckRequired = graphManager.computeComponentsIfNeeded(graphContext, layout);
            Graph visibleGraph = graphContext.getGraphData();

//            boolean async = Boolean.parseBoolean(asyncParam);
//            SelectionModel selection = getSelectionModel(null, vizUuid);
//            Visualization vis = graphContext.getVisualization();
//            Graph graph = (Graph) vis.getSourceData("graph");
//            Display display = vis.getDisplay(0);
//            List<CsiMap<String, String>> beforeLayoutPositionList = new ArrayList<CsiMap<String, String>>();
//            List<CsiMap<String, String>> finalLayoutPositionList = new ArrayList<CsiMap<String, String>>();
//            Iterator filteredNodes;
//            Point2D itemPoint;
//            Point2D displayPoint;
            // Capture before layout positions for animation.
//            filteredNodes = visibleGraph.nodes();
            // Get the list of nodes not filtered out because they are bundled
            // or not displayable for some other reason...
//            filteredNodes = Iterators.filter(filteredNodes, getDisplayableNodePredicate());
            /*
             * int totalNodes = Iterators.size(filteredNodes); // Get the list
             * of nodes not filtered out because they are bundled // or not
             * displayable for some other reason... filteredNodes =
             * visibleGraph.nodes(); filteredNodes =
             * Iterators.filter(filteredNodes, getDisplayableNodePredicate());
             * VisualGraph visualGraph = graphContext.getVisualGraph(); if
             * (totalNodes <= animationThreshold) { while
             * (filteredNodes.hasNext()) { TaskHelper.checkForCancel();
             *
             * Node node = (Node) filteredNodes.next();
             *
             * VisualItem vizNode = (VisualItem)
             * visualGraph.getNode(node.getRow());
             *
             * itemPoint = new Point2D.Double(vizNode.getX(), vizNode.getY());
             * displayPoint = toDisplayPoint(display, itemPoint);
             *
             * CsiMap<String, String> prop = new CsiMap<String, String>();
             * prop.put("ID", Integer.toString(vizNode.getRow()));
             * prop.put("beforeX", Double.toString(displayPoint.getX()));
             * prop.put("beforeY", Double.toString(displayPoint.getY()));
             * prop.put("width",
             * Double.toString(vizNode.getBounds().getWidth()));
             * prop.put("height",
             * Double.toString(vizNode.getBounds().getHeight()));
             *
             * beforeLayoutPositionList.add(prop); } }
             */
            TaskHelper.checkForCancel();
            // **** Progress report point ****
            TaskHelper.reportProgress("Computing graph layout", 30);
            // Run layout
            graphManager.runPlacement(visibleGraph, layout, graphContext);
            if (autoSizeCheckRequired) {
                Predicate<GraphContext> autoSize = AutoSizingPolicy.createPercentage(0.20d);
                if (autoSize.test(graphContext)) {
                    graphContext.fitToSize();
                    // GraphManager.fitToSize( graphContext.getVisualization()
                    // );
                }
            }

            // Capture after layout positions for animation.
            // componentNodes = component.nodes();
            /*
             * if (totalNodes <= animationThreshold) {
             * TaskHelper.reportProgress("Capturing positions", 80);
             *
             * filteredNodes = visibleGraph.nodes(); filteredNodes =
             * Iterators.filter(filteredNodes, getDisplayableNodePredicate());
             * VisualGraph afterLayoutVizGraph = graphContext.getVisualGraph();
             *
             * // Loop through the nodes while (filteredNodes.hasNext()) {
             * TaskHelper.checkForCancel();
             *
             * Node node = (Node) filteredNodes.next();
             *
             * // Find the visual item for the current node in the graph.
             * VisualItem vizNode = (VisualItem)
             * afterLayoutVizGraph.getNode(node.getRow());
             *
             * // Translate that point to the display point. itemPoint = new
             * Point2D.Double(vizNode.getX(), vizNode.getY()); displayPoint =
             * toDisplayPoint(display, itemPoint);
             *
             * // Loop through our before layout position list and add in // the
             * after // layout position values. for (CsiMap m :
             * beforeLayoutPositionList) { TaskHelper.checkForCancel();
             *
             * Object itemId = m.get("ID");
             *
             * if (Integer.parseInt(itemId.toString()) == vizNode.getRow()) {
             *
             * CsiMap<String, String> prop = new CsiMap<String, String>();
             * boolean selected = selection.nodes.contains(vizNode.getRow());
             * prop.put(PROPERTY_SELECTED, Boolean.toString(selected));
             * prop.put("ID", Integer.toString(vizNode.getRow()));
             * prop.put("afterX", Double.toString(displayPoint.getX()));
             * prop.put("afterY", Double.toString(displayPoint.getY()));
             * prop.put("height",
             * Double.toString(vizNode.getBounds().getHeight()));
             * prop.put("width",
             * Double.toString(vizNode.getBounds().getWidth()));
             * prop.put("beforeX", (String) m.get("beforeX"));
             * prop.put("beforeY", (String) m.get("beforeY"));
             *
             * // get the label for (int i = 0; i < vizNode.getColumnCount();
             * i++) { TaskHelper.checkForCancel();
             *
             * String colname = vizNode.getColumnName(i); if
             * (!colname.startsWith("_") &&
             * !GraphConstants.DOC_ID.equals(colname)) { Object val =
             * vizNode.get(i); if (colname.equals(GraphConstants.NODE_DETAIL)) {
             * NodeStore ns = (NodeStore) val; List<String> labels =
             * ns.getLabels(); if ((labels != null) && (labels.size() > 0)) {
             * prop.put(NAME_PARAMETER, labels.get(0)); } prop.put("type",
             * ns.getType()); } } }
             *
             * finalLayoutPositionList.add(prop); }
             *
             * } } Double currentScale = display.getScale(); CsiMap<String,
             * String> prop = new CsiMap<String, String>();
             * prop.put("currentScale", Double.toString(currentScale));
             * finalLayoutPositionList.add(0, prop); return
             * finalLayoutPositionList; }
             */
            graphContext.fitToSize();
            graphContext.zoom(.9);
            graphContext.updateDisplay();
            return null;
        }
    }

   private Predicate<Edge> getDisplayableEdgePredicate() {
      Predicate<Edge> predicate = new Predicate<Edge>() {
         private Predicate<Node> isNodeDisplayable = getDisplayableNodePredicate();

         @Override
         public boolean test(Edge input) {
            boolean results = isNodeDisplayable.test(input.getSourceNode()) &&
                              isNodeDisplayable.test(input.getTargetNode());

            if (results) {
               LinkStore details = GraphManager.getEdgeDetails(input);
               results = !details.isHidden();
            }
            return results;
         }
      };
      return predicate;
   }

   private Predicate<Node> getDisplayableNodePredicate() {
      return GraphContext.Predicates.IsNodeVisualized.and(GraphContext.Predicates.IsNodeDisplayable);
   }

    @SuppressWarnings("unchecked")
    @Override
    public DragStartDTO gwtDragStart(String startx, String starty, String vizUuid) {
        DragStartDTO dto = new DragStartDTO();
        CsiMap<String, Object> info;
        try {
            info = dragStart(startx, starty, vizUuid);
        } catch (IOException e1) {
            e1.printStackTrace();
            return dto;
        }
        {
            int imageXi = 0;
            if (info.get("imageX") != null) {
                try {
                    Object value = info.get("imageX");
                    if (value instanceof Integer) {
                        imageXi = (Integer) value;
                    }
                } catch (Exception e) {
                    // LOG error here
                }
            }
            info.remove("imageX");
            dto.setImageX(imageXi);
        }
        {
            int imageYi = 0;
            if (info.get("imageY") != null) {
                try {
                    Object value = info.get("imageY");
                    if (value instanceof Integer) {
                        imageYi = (Integer) value;
                    }
                } catch (Exception e) {
                    // LOG error here
                }
            }
            info.remove("imageY");
            dto.setImageY(imageYi);
        }
        dto.dragNodes = new ArrayList<NodeMapDTO>();
        List<CsiMap<String, Object>> dragNodes;
        if (info.containsKey("dragNodes")) {
            dragNodes = (List<CsiMap<String, Object>>) info.remove("dragNodes");
        } else {
            dragNodes = new ArrayList<CsiMap<String, Object>>();
        }

        for (CsiMap<String, Object> nodeMap : dragNodes) {

            dto.dragNodes.add(createNodeMapDTO(nodeMap));
        }

        if (!info.isEmpty()) {
            dumpMap(info, "DragStartDTO");
        }
        return dto;
    }

    @SuppressWarnings("unchecked")
    private NodeMapDTO createNodeMapDTO(CsiMap<String, Object> nodeMap) {
        NodeMapDTO dto = new NodeMapDTO();
        dto.ID = nodeMap.containsKey("ID") ? ((Integer) nodeMap.remove("ID")).intValue() : null;
        dto.displayX = nodeMap.containsKey("displayX") ? (Double) nodeMap.remove("displayX") : null;
        dto.displayY = nodeMap.containsKey("displayY") ? (Double) nodeMap.remove("displayY") : null;
        dto.relativeX = nodeMap.containsKey("relativeX") ? (Double) nodeMap.remove("relativeX") : null;
        dto.relativeY = nodeMap.containsKey("relativeY") ? (Double) nodeMap.remove("relativeY") : null;
        dto.neighbors = new ArrayList<NeighborPropDTO>();
        List<CsiMap<String, Object>> neighborList;
        if (nodeMap.containsKey("neighbors")) {
            neighborList = ((List<CsiMap<String, Object>>) nodeMap.remove("neighbors"));
        } else {
            neighborList = new ArrayList<CsiMap<String, Object>>();
        }
        for (CsiMap<String, Object> neighborProp : neighborList) {
            dto.neighbors.add(createNeighborPropDTO(neighborProp));
        }
        if (!nodeMap.isEmpty()) {
            dumpMap(nodeMap, "NodeMapDTO");
        }
        return dto;
    }

    // // TODO: KA - wire up for node filtering.
    // public List<String> getNodeTypesInNodeListing(String vizUuid) {
    // List<NodeListDTO> data = gwtNodeListing(vizUuid);
    // Set<String> result = new HashSet<String>();
    // for (NodeListDTO dto : data) {
    // for (GraphTypesDTO graphTypesDTO : dto.getTypes()) {
    // result.add(graphTypesDTO.getType());
    // }
    // }
    // return new ArrayList<String>(result);
    // }

    private NeighborPropDTO createNeighborPropDTO(CsiMap<String, Object> neighProps) {
        NeighborPropDTO dto = new NeighborPropDTO();

        dto.ID = neighProps.containsKey("ID") ? ((Integer) neighProps.remove("ID")).intValue() : null;
        dto.displayX = neighProps.containsKey("displayX") ? (Double) neighProps.remove("displayX") : null;
        dto.displayY = neighProps.containsKey("displayY") ? (Double) neighProps.remove("displayY") : null;
        dto.isInSelection = neighProps.containsKey(PROPERTY_SELECTED)
                ? new Boolean((String) neighProps.remove(PROPERTY_SELECTED)) : null;
        dto.relativeX = neighProps.containsKey("relativeX") ? (Double) neighProps.remove("relativeX") : null;
        dto.relativeY = neighProps.containsKey("relativeY") ? (Double) neighProps.remove("relativeY") : null;

        if (!neighProps.isEmpty()) {
            dumpMap(neighProps, "NeighborPropDTO");
        }
        return dto;
    }

   @Operation
   public CsiMap<String, Object> dragStart(@QueryParam(value = "startx") String startx,
                                           @QueryParam(value = "starty") String starty, @QueryParam(value = VIZUUID_PARAM) String vizUuid)
            throws IOException {
      CsiMap<String,Object> dragData = new CsiMap<String,Object>();
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            Visualization vis1 = getVisualization(vizUuid);
            Visualization vis = vis1;

            if (vis != null) {
               synchronized (vis) {
                  Display display = vis.getDisplay(0);

                  synchronized (display) {
                     Integer intStartX = IntegerUtil.valueOf(startx);
                     Integer intStartY = IntegerUtil.valueOf(starty);
                     VisualItem item = display.findItem(new Point(intStartX, intStartY));

                     if ((item != null) && (item instanceof TableNodeItem)) {
                        VisualGraph vgraph = (VisualGraph) vis.getGroup("graph");
                        Graph graph = (Graph) vis.getSourceData("graph");
                        DragItems dragItems = new DragItems();

                        dragItems.items.add(item);
                        int itemRow = item.getRow();

                        boolean itemSelected = false;
                        SelectionModel selections = null;
                        String selectionName = DEFAULT_SELECTION;
                        selections = getSelectionModel(selectionName, vizUuid);

                        itemSelected = selections.nodes.contains(itemRow);

                        if (itemSelected) {
                           for (Integer rowid : selections.nodes) {
                              if (rowid == itemRow) {
                                 continue;
                              }
                              NodeItem node = (NodeItem) vgraph.getNode(rowid);
                              NodeStore details = GraphManager.getNodeDetails(node);

                              if (node.isVisible() && !details.isBundled()) {
                                 dragItems.items.add(node);
                              }
                           }
                        }
                        AffineTransform displayTx = display.getTransform();

                        GraphManager graphManager = GraphManager.getInstance();
                        Rectangle2D itemBounds = graphManager.getItemBounds(dragItems.items);
                        Rectangle2D.Double clipBounds;

                        // calculate the display bounds in absolute coordinate space
                        double dispAbsX = display.getDisplayX();
                        double dispAbsY = display.getDisplayY();
                        double dispAbsW = (display.getWidth() / displayTx.getScaleX());
                        double dispAbsH = (display.getHeight() / displayTx.getScaleY());
//                    Rectangle2D dispAbsBounds = new Rectangle2D.Double(dispAbsX, dispAbsY, dispAbsW, dispAbsH);

                        // determine max bounds as 2 times size of display centered
                        // around startx,starty
                        float scale = 2;
                        Point2D absStartPoint = display.getAbsoluteCoordinate(new Point2D.Double(intStartX, intStartY),
                              null);
                        double maxAbsW = dispAbsW * scale;
                        double maxAbsH = dispAbsH * scale;
                        double maxAbsX = dispAbsX - ((maxAbsW - dispAbsW) / 2);
                        double maxAbsY = dispAbsY - ((maxAbsH - dispAbsH) / 2);
                        double maxAbsCx = maxAbsX + (maxAbsW / 2);
                        double maxAbsCy = maxAbsY + (maxAbsH / 2);
                        maxAbsX = maxAbsX - (maxAbsCx - absStartPoint.getX());
                        maxAbsY = maxAbsY - (maxAbsCy - absStartPoint.getY());

                        // clip the bounds to max bounds
                        double clipAbsX = itemBounds.getX() - 5;
                        double clipAbsY = itemBounds.getY() - 5;
                        double clipAbsW = itemBounds.getWidth() + 10;
                        double clipAbsH = itemBounds.getHeight() + 10;

                        if (clipAbsW > maxAbsW) {
                           clipAbsX = maxAbsX;
                           clipAbsW = maxAbsW;
                        }
                        if (clipAbsH > maxAbsH) {
                           clipAbsY = maxAbsY;
                           clipAbsH = maxAbsH;
                        }
                        clipBounds = new Rectangle2D.Double(clipAbsX, clipAbsY, clipAbsW, clipAbsH);
                        // clip the clipbound with the area that has items. this
                        // will minimize
                        // the bounds when the startx is along the boundary of the
                        // itembounds.
                        Rectangle2D.intersect(clipBounds, itemBounds, clipBounds);

                        Point2D displayTopLeft = toDisplayPoint(display,
                              new Point2D.Double(clipBounds.getX(), clipBounds.getY()));
                        Point2D displayBottomRight = toDisplayPoint(display,
                              new Point2D.Double(clipBounds.getMaxX(), clipBounds.getMaxY()));
                        /*
                         * Point2D displayTopLeft = toDisplayPoint(display, new
                         * Point2D.Double(clipBounds.getX()-50, clipBounds.getY()-50)); Point2D
                         * displayBottomRight = toDisplayPoint(display, new
                         * Point2D.Double(clipBounds.getMaxX()+100, clipBounds.getMaxY()+100));
                         */
                        Rectangle2D localClipBounds = new Rectangle2D.Double();
                        localClipBounds.setFrameFromDiagonal(displayTopLeft, displayBottomRight);

                        int imageX = (int) localClipBounds.getX();
                        int imageY = (int) localClipBounds.getY();
                        int imageW = (int) localClipBounds.getWidth();
                        int imageH = (int) localClipBounds.getHeight();

                        dragItems.imageX = imageX;
                        dragItems.imageY = imageY;
                        dragItems.imageW = imageW;
                        dragItems.imageH = imageH;
                        Point2D.Double ptSrc = new Point2D.Double(50, 50);
                        try {
                           AffineTransform inverse = display.getTransform().createInverse();
                           inverse.transform(ptSrc, null);
                        } catch (NoninvertibleTransformException e) {
                           e.printStackTrace();
                        }
                        dragItems.clipArea = new Rectangle2D.Double(clipBounds.getX() - ptSrc.getX(),
                              clipBounds.getY() - ptSrc.getY(), clipBounds.getWidth() + (2 * ptSrc.getX()),
                              clipBounds.getHeight() + (2 * ptSrc.getY()));
                        dragItems.clipArea = clipBounds;
                        graph.putClientProperty(GraphConstants.DRAG_ITEMS, dragItems);

                        dragData.put("imageX", dragItems.imageX);
                        dragData.put("imageY", dragItems.imageY);

                        List<CsiMap<String,Object>> dragNodes = new ArrayList<CsiMap<String,Object>>();
                        dragData.put("dragNodes", dragNodes);

                        Predicate<Node> isDisplayable = getDisplayableNodePredicate();

                        for (VisualItem dItem : dragItems.items) {
                           if (dItem instanceof TableNodeItem) {

                              if (!clipBounds.intersects(dItem.getBounds())) {
                                 continue;
                              }

                              CsiMap<String,Object> nodeMap = new CsiMap<String,Object>();
                              dragNodes.add(nodeMap);

                              int itemId = dItem.getRow();
                              Point2D itemPoint = new Point2D.Double(dItem.getX(), dItem.getY());
                              Point2D dispItemPoint = toDisplayPoint(display, itemPoint);

                              nodeMap.put("ID", itemId);
                              nodeMap.put("displayX", dispItemPoint.getX());
                              nodeMap.put("displayY", dispItemPoint.getY());
                              nodeMap.put("relativeX", dispItemPoint.getX() - dragItems.imageX);
                              nodeMap.put("relativeY", dispItemPoint.getY() - dragItems.imageY);

                              TableNodeItem vItem = (TableNodeItem) dItem;
                              List<CsiMap> neighborList = new ArrayList<CsiMap>();
                              nodeMap.put("neighbors", neighborList);

                              Iterator edges = vItem.edges();
                              while (edges.hasNext()) {
                                 TableEdgeItem edge = (TableEdgeItem) edges.next();

                                 if (!clipBounds.intersects(edge.getBounds())) {
                                    continue;
                                 }

                                 NodeItem neighbor = edge.getAdjacentItem((NodeItem) dItem);
                                 NodeStore otherDetails = GraphManager.getNodeDetails(neighbor);

                                 if (!neighbor.isVisible() || !isDisplayable.test(neighbor)
                                       || otherDetails.isBundled()) {
                                    continue;
                                 }

                                 LinkStore edgeDetails = GraphManager.getEdgeDetails(edge);
                                 if (!edge.isVisible() || !edgeDetails.isDisplayable()) {
                                    continue;
                                 }

                                 CsiMap<String,Object> neighProps = new CsiMap<String,Object>();
                                 Point2D neighPoint = new Point2D.Double(neighbor.getX(), neighbor.getY());
                                 Point2D dNeighPoint = toDisplayPoint(display, neighPoint);

                                 neighProps.put("ID", neighbor.getRow());
                                 neighProps.put("displayX", dNeighPoint.getX());
                                 neighProps.put("displayY", dNeighPoint.getY());
                                 boolean isInSelection = (itemSelected && (selections != null)
                                       && selections.nodes.contains(neighbor.getRow()));
                                 neighProps.put(PROPERTY_SELECTED, Boolean.toString(isInSelection));
                                 neighProps.put("relativeX", dNeighPoint.getX() - dragItems.imageX);
                                 neighProps.put("relativeY", dNeighPoint.getY() - dragItems.imageY);
                                 neighborList.add(neighProps);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      return dragData;
   }

   @Override
   @Operation
   public String dragEnd(@QueryParam(value = "endx") String endx, @QueryParam(value = "endy") String endy,
                         @QueryParam(value = VIZUUID_PARAM) String vizUuid) {

      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
      if (graphContext == null) {
         return "";
      }

      synchronized (graphContext) {
         Visualization vis = getVisualization(vizUuid);

         if (vis != null) {
            synchronized (vis) {
               Display display = vis.getDisplay(0);
               synchronized (display) {
                  Graph graph = (Graph) vis.getSourceData("graph");
                  DragItems dragItems = (DragItems) graph.getClientProperty(GraphConstants.DRAG_ITEMS);
                  if ((dragItems == null) || dragItems.items.isEmpty()) {
                     return "";
                  }

//                    Point2D absImg = display.getAbsoluteCoordinate(new Point(dragItems.imageX, dragItems.imageY), null);
                  Point2D absEnd = display
                        .getAbsoluteCoordinate(new Point(Integer.parseInt(endx), Integer.parseInt(endy)), null);

                  for (VisualItem item : dragItems.items) {
                     if (item instanceof TableNodeItem) {

                        double relativeX = item.getX() - dragItems.clipArea.getX();
                        double relativeY = item.getY() - dragItems.clipArea.getY();

                        PrefuseLib.setX(item, item, absEnd.getX() + relativeX);
                        PrefuseLib.setY(item, item, absEnd.getY() + relativeY);
                     }
                  }
               }
            }
         }
      }

      graphContext.updateDisplay();
      return "";
   }

   @Override
   @Operation
   @Interruptable
   public void graphMeta(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                         @QueryParam(value = "nodesOnly") Boolean nodesOnly) throws CentrifugeException, IOException {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
        if (nodesOnly == null) {
            nodesOnly = Boolean.FALSE;
        }
        synchronized (graphContext) {
           Graph graph = graphContext.getGraphData();

           if (graph == null) {
              throw new CentrifugeException("No graph context.");
           }
           // if nodesOnly, prune overall graph to just node information.
           if (nodesOnly.booleanValue()) {
              Table nodeTable = graph.getNodeTable();
              Map<String, Node> idNodeIndex = graphContext.getNodeKeyIndex();
              graph = new Graph(nodeTable, false);

              graph.putClientProperty(GraphManager.NODE_HASH_TABLE, idNodeIndex);
           }
           // manual capture this return object so that we maintain the sync
           // block on the graph object.
           TaskHelper.reportComplete(graph);
           // return null;
        }
      }
   }

   @Override
   public List<NodeListDTO> gwtNodeListing(String vizUuid) {
      List<NodeListDTO> nodes = new ArrayList<NodeListDTO>();
      NodeListing listing;

      try {
         listing = nodeListing(vizUuid);
      } catch (Exception e) {
         throw Throwables.propagate(e);
      }
      if (listing != null) {
         Graph graph = listing.getGraph();
         Map<String,Node> idNodeIndex = (Map<String,Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
         VisualGraph vg;

         try {
            vg = GraphServiceUtil.getGraphContext(vizUuid).getVisualGraph();
         } catch (TaskAbortedException e) {
            return new ArrayList<NodeListDTO>();
         }
         Visualization vis = getVisualization(vizUuid);

         if (vis != null) {
            SelectionModel selection = getSelectionModel(DEFAULT_SELECTION, vizUuid);
            Display display = vis.getDisplay(0);
            Predicate<Node> listingFilter = listing.getFilter();

            for (Iterator<Node> graphNodes = graph.nodes(); graphNodes.hasNext();) {
               Node node = graphNodes.next();

               if (listingFilter.test(node)) {
                  try {
                     if (shouldEmitNode(node)) {
                        createListNode(node, idNodeIndex, nodes, vg, selection, vis, display,
                                       GraphServiceUtil.getGraphContext(vizUuid).getVisualizationDef());
                     }
                  } catch (IllegalArgumentException iae) {
                     LOG.warn("Encountered transient error while writing node information.  Not all data may have been conveyed",
                              iae);
                  }
               }
            }
         }
      }
      return nodes;
   }

    @Override
    public PagingLoadResult<EdgeListDTO> getPageableEdgeListing(String vizUuid, FilterPagingLoadConfig loadConfig) {
        PagingLoadResultBean<EdgeListDTO> loadResult = new PagingLoadResultBean<EdgeListDTO>();

        List<EdgeListDTO> data = gwtEdgeListing(vizUuid);

        // Filter first, then sort, then get offset and limit.
        EdgeListDTOFilteringPredicate predicate = new EdgeListDTOFilteringPredicate();
        predicate.setFilterConfigs(loadConfig.getFilters());

        Collection<EdgeListDTO> filteredEdgeListings = new ArrayList<EdgeListDTO>();

        for (Iterator<EdgeListDTO> edgeListings = data.iterator(); edgeListings.hasNext();) {
           EdgeListDTO edgeListing = edgeListings.next();

           if (predicate.test(edgeListing)) {
              filteredEdgeListings.add(edgeListing);
           }
        }
        Ordering<EdgeListDTO> ordering = new Ordering<EdgeListDTO>() {
            @Override
            public int compare(@Nullable EdgeListDTO left, @Nullable EdgeListDTO right) {
                return 0;
            }
        };
        // Add each of the specified ordering in loadCnnfig.
        for (final SortInfo sortInfo : loadConfig.getSortInfo()) {
            EdgeListDTOComparator comparator = new EdgeListDTOComparator();
            comparator.setFieldToCompare(sortInfo.getSortField());
            comparator.setSortDir(sortInfo.getSortDir());
            ordering = ordering.compound(comparator);
        }

        // FluentIterables.toList returns an immutable list - which jogformer
        // doesn't like!
        loadResult.setData(new ArrayList<EdgeListDTO>(FluentIterable.from(ordering.sortedCopy(filteredEdgeListings))
                .skip(loadConfig.getOffset()).limit(loadConfig.getLimit()).toList()));

        loadResult.setOffset(loadConfig.getOffset());
        loadResult.setTotalLength(filteredEdgeListings.size());
        return loadResult;
    }

    @Override
    public PagingLoadResult<NodeListDTO> getPageableNodeListing(String vizUuid, FilterPagingLoadConfig loadConfig) {
        PagingLoadResultBean<NodeListDTO> loadResult = new PagingLoadResultBean<NodeListDTO>();

        List<NodeListDTO> data = gwtNodeListing(vizUuid);

        // Filter first, then sort, then get offset and limit.
        NodeListDTOFilteringPredicate predicate = new NodeListDTOFilteringPredicate();
        predicate.setFilterConfigs(loadConfig.getFilters());

        Collection<NodeListDTO> filteredNodeListings = new ArrayList<NodeListDTO>();

        for (Iterator<NodeListDTO> nodeListings = data.iterator(); nodeListings.hasNext();) {
           NodeListDTO nodeListing = nodeListings.next();

           if (predicate.test(nodeListing)) {
              filteredNodeListings.add(nodeListing);
           }
        }
        Ordering<NodeListDTO> ordering = new Ordering<NodeListDTO>() {
            @Override
            public int compare(@Nullable NodeListDTO left, @Nullable NodeListDTO right) {
                return 0;
            }
        };
        // Add each of the specified ordering in loadConfig.
        for (final SortInfo sortInfo : loadConfig.getSortInfo()) {
            NodeListDTOComparator comparator = new NodeListDTOComparator();
            comparator.setFieldToCompare(sortInfo.getSortField());
            comparator.setSortDir(sortInfo.getSortDir());
            ordering = ordering.compound(comparator);
        }

        // FluentIterables.toList returns an immutable list - which jogformer
        // doesn't like!
        loadResult.setData(new ArrayList<NodeListDTO>(FluentIterable.from(ordering.sortedCopy(filteredNodeListings))
                .skip(loadConfig.getOffset()).limit(loadConfig.getLimit()).toList()));

        loadResult.setOffset(loadConfig.getOffset());
        loadResult.setTotalLength(filteredNodeListings.size());
        return loadResult;
    }

    public String exportNodeList(String vizUuid, FilterPagingLoadConfig loadConfig, List<String> visibleColumns) {
        List<NodeListDTO> data = gwtNodeListing(vizUuid);

        // Filter first, then sort, then get offset and limit.
        NodeListDTOFilteringPredicate predicate = new NodeListDTOFilteringPredicate();
        predicate.setFilterConfigs(loadConfig.getFilters());

        Collection<NodeListDTO> filteredNodeListings = new ArrayList<NodeListDTO>();

        for (Iterator<NodeListDTO> nodeListings = data.iterator(); nodeListings.hasNext();) {
           NodeListDTO nodeListing = nodeListings.next();

           if (predicate.test(nodeListing)) {
              filteredNodeListings.add(nodeListing);
           }
        }
        Ordering<NodeListDTO> ordering = new Ordering<NodeListDTO>() {
            @Override
            public int compare(@Nullable NodeListDTO left, @Nullable NodeListDTO right) {
                return 0;
            }
        };
        // Add each of the specified ordering in loadConfig.
        for (final SortInfo sortInfo : loadConfig.getSortInfo()) {
            NodeListDTOComparator comparator = new NodeListDTOComparator();
            comparator.setFieldToCompare(sortInfo.getSortField());
            comparator.setSortDir(sortInfo.getSortDir());
            ordering = ordering.compound(comparator);
        }
        ArrayList<NodeListDTO> prettyResults = new ArrayList<NodeListDTO>(
                FluentIterable.from(ordering.sortedCopy(filteredNodeListings)).toList());
        return exportActionsService.exportNodesList(prettyResults, visibleColumns);
    }

    private void createListNode(Node node, Map<String, Node> idNodeIndex, List<NodeListDTO> nodes, VisualGraph vg,
                                SelectionModel selection, Visualization vis, Display display, RelGraphViewDef visualizationDef) {
        checkNotNull(node);
        checkNotNull(idNodeIndex);
        checkNotNull(nodes);
        // For now, these new items are required. We could degrade more
        // gracefully
        checkNotNull(vg);
        checkNotNull(selection);
        checkNotNull(vis);
        NodeStore details = GraphManager.getNodeDetails(node);
        VisualItem visualNode = null;
        Node node2 = vg.getNode(node.getRow());// rename
        if (node2 instanceof VisualItem) {
            visualNode = (VisualItem) vg.getNode(node.getRow());
        }
        checkNotNull(visualNode);
        Point2D itemPoint = new Point2D.Double(visualNode.getX(), visualNode.getY());
        Point2D displayPoint = toDisplayPoint(display, itemPoint);

        CsiMap<String, Object> info = new CsiMap<String, Object>();
        populatePropertyDetails(visualNode, info, true);

//        Map<String, Property> attributes = details.getAttributes();
        // At this point I have all most of the items i need to copy over to the
        // DTO
        // The current spec is to have one instance for each type of a
        // multi-typed node
        Iterator<Map.Entry<String, Integer>> typesIter = details.getTypes().entrySet().iterator();
        CsiMap<String, Object> tips = info.containsKey("toolTipProps")
                ? (CsiMap<String, Object>) info.get("toolTipProps") : null;
        CsiMap<String, String> snaMetrics = tips.containsKey("SNA Metrics")
                ? (CsiMap<String, String>) tips.get("SNA Metrics") : null;

        while (typesIter.hasNext()) {
            Map.Entry<String, Integer> currentTypeItteration = typesIter.next();
            NodeListDTO dto = new NodeListDTO();
            // anchored
            dto.anchored = details.isAnchored();
            // betweenness
            if (snaMetrics != null) {
                String betweennessString = snaMetrics.get("BETWEENNESS");
                if (!Strings.isNullOrEmpty(betweennessString)) {
                    try {
                        dto.betweenness = Double.parseDouble(betweennessString);
                    } catch (NumberFormatException e) {
                        dto.betweenness = -1;
                    }
                }
            }
            // isBundle
            if (details.getChildren() != null) {

                dto.bundle = !details.getChildren().isEmpty();
            }
            // bundled
            dto.bundled = details.isBundled();
            // bundleNodeLabel
            dto.bundleNodeLabel = "-";
            AbstractGraphObjectStore parent = details.getParent();
            if (parent != null) {
                AbstractGraphObjectStore store = parent;
                while (store.getParent() != null) {
                    store = store.getParent();
                }
                Map<String, Node> idNodeMap = (Map<String, Node>) node.getGraph()
                        .getClientProperty(GraphManager.NODE_HASH_TABLE);
                Node parentNode = idNodeMap.get(store.getKey());
                Node parentVisualNode = vg.getNode(parentNode.getRow());
                VisualItem visualItem = parentVisualNode instanceof VisualItem ? ((VisualItem) parentVisualNode) : null;
                if (visualItem != null) {
                    dto.setBundleX(visualItem.getX());
                    dto.setBundleY(visualItem.getY());
                }

                dto.bundleNodeLabel = parent.getLabel();
                node.getParent();
            }
            // closeness
            if (snaMetrics != null) {
                String closenessString = snaMetrics.get("CLOSENESS");
                if (!Strings.isNullOrEmpty(closenessString)) {
                    try {
                        dto.closeness = Double.parseDouble(closenessString);
                    } catch (NumberFormatException e) {
                        dto.closeness = -1;
                    }
                }
            }
            // component
            try {
                dto.component = info.containsKey(GraphConstants.COMPONENT_ID)
                        ? (Integer) info.get(GraphConstants.COMPONENT_ID) : null;
            } catch (Exception e) {

            }

            // degrees
            dto.degrees = node.getDegree();

            // displayX
            dto.displayX = displayPoint.getX();
            // displayY
            dto.displayY = displayPoint.getY();
            // eigenvector
            if (snaMetrics != null) {
                String eigenvectorString = snaMetrics.get("EIGENVECTOR");
                if (!Strings.isNullOrEmpty(eigenvectorString)) {
                    try {
                        dto.eigenvector = Double.parseDouble(eigenvectorString);
                    } catch (NumberFormatException e) {
                        dto.eigenvector = -1;
                    }
                }
            }
            // hidden
            dto.hidden = details.isHidden();
            // hidelabels
            dto.hideLabels = info.containsKey(PROPERTY_HIDE_LABELS) ? (Boolean) info.get(PROPERTY_HIDE_LABELS) : null;
            // id
            dto.ID = node.getRow();
            // key
            dto.key = details.getKey();
            // label
            dto.label = details.getLabel();
            // annotation/comment
            List<Annotation> annotations = visualizationDef.getAnnotations();
            if (annotations != null) {

                    dto.annotation = annotations.stream().filter(x -> x.getParentKey().equals(dto.getKey())).count() > 0;
            }
            // plunked
            dto.plunked = info.containsKey(PLUNKED) ? (Boolean) info.get(PLUNKED) : null;
            // nestedLevel
            int nestedLevel = 0;

            // transparency/opacity
            dto.setTransparency(details.getTransparency());

            AbstractGraphObjectStore myParent = parent;
            while (myParent != null) {
                nestedLevel += 1;
                myParent = myParent.getParent();
            }
            dto.nestedLevel = nestedLevel;
            // selected
            dto.selected = selection.nodes.contains(node.getRow());
            // size
            dto.size = visualNode.getSize();
            // type
            dto.type = currentTypeItteration.getKey();
            // typeKey
            // NOTE: use augmented key to prevent deduplication
            dto.typedKey = dto.key + currentTypeItteration.getKey();
            // Visible Neighbors
            dto.visibleNeighbors = 0;

            for (Iterator<Edge> edges = node.edges(); edges.hasNext();) {
               Edge edge = edges.next();

               if  ((edge != null) && GraphContext.Predicates.IsEdgeVisualizedAndDisplayable.test(edge) &&
                    !GraphManager.getEdgeDetails(edge).isHidden()) {
                  dto.visibleNeighbors++;
               }
            }

            // visualized
            //Object obj = info.containsKey(VISUAL_ITEM_TYPE) ? info.get(VISUAL_ITEM_TYPE) : null;
            Object obj = info.containsKey(GraphContext.IS_VISUALIZED) ? info.get(GraphContext.IS_VISUALIZED) : null;
            if ((obj != null) && (obj instanceof Boolean)) {
                dto.visualized = (Boolean) obj;
            }
            if ((obj != null) && (obj instanceof String)) {
                dto.visualized = new Boolean((String) obj);
            }

            // x
            dto.x = visualNode.getX();
            // y
            dto.y = visualNode.getY();

            //
            nodes.add(dto);
            // if (details.hasChildren()) {// Not sure i need this
            // for (GraphObjectStore store : details.getChildren()) {
            // Node child = idNodeIndex.get(store.getKey());
            // createListNode(child, idNodeIndex, nodes, vg, selection, vis,
            // display);
            // }
            // }
        }
    }

    public String exportLinkList(String vizUuid, FilterPagingLoadConfig loadConfig, List<String> visibleColumns) {
//        PagingLoadResultBean<EdgeListDTO> loadResult = new PagingLoadResultBean<EdgeListDTO>();
        List<EdgeListDTO> data = gwtEdgeListing(vizUuid);

        // Filter first, then sort, then get offset and limit.
        EdgeListDTOFilteringPredicate predicate = new EdgeListDTOFilteringPredicate();
        predicate.setFilterConfigs(loadConfig.getFilters());

        Collection<EdgeListDTO> filteredEdgeListings = new ArrayList<EdgeListDTO>();

        for (Iterator<EdgeListDTO> edgeListings = data.iterator(); edgeListings.hasNext();) {
           EdgeListDTO edgeListing = edgeListings.next();

           if (predicate.test(edgeListing)) {
              filteredEdgeListings.add(edgeListing);
           }
        }
        Ordering<EdgeListDTO> ordering = new Ordering<EdgeListDTO>() {
            @Override
            public int compare(@Nullable EdgeListDTO left, @Nullable EdgeListDTO right) {
                return 0;
            }
        };

        // Add each of the specified ordering in loadCnnfig.
        for (final SortInfo sortInfo : loadConfig.getSortInfo()) {
            EdgeListDTOComparator comparator = new EdgeListDTOComparator();
            comparator.setFieldToCompare(sortInfo.getSortField());
            comparator.setSortDir(sortInfo.getSortDir());
            ordering = ordering.compound(comparator);
        }

        // FluentIterables.toList returns an immutable list - which jogformer
        // doesn't like!
        ArrayList<EdgeListDTO> prettyList = new ArrayList<EdgeListDTO>(
                FluentIterable.from(ordering.sortedCopy(filteredEdgeListings)).toList());
        return exportActionsService.exportLinksList(prettyList, visibleColumns);
    }

   private boolean shouldEmitNode(Node node) {
      return (node != null) && (GraphManager.getNodeDetails(node) != null);
   }

    @Operation
    public NodeListing nodeListing(@QueryParam(value = VIZUUID_PARAM) String vizUuid) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext == null) {
            return null;
        }

        NodeListing listing = null;
        synchronized (graphContext) {
            Graph graph = graphContext.getGraphData();
            if (graph == null) {
                throw new RuntimeException("No graph context.");
            }

            Predicate<Node> filter = GraphContext.Predicates.NoParents;
            listing = new NodeListing(graph, filter);

            // TaskHelper.reportComplete(listing);
        }

        return listing;
    }

   @Override
   @Operation
   public Collection<Integer> nodeNeighbors(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                                            @QueryParam("nodeId") Integer nodeId) {
      Collection<Integer> neighborIds = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            Graph graph = graphContext.getGraphData();
            Node node = graph.getNode(nodeId);
            neighborIds = new ArrayList<Integer>();
            Function<Node,Integer> transform = GraphContext.Functions.mapNodeToId();

            for (Iterator<Node> neighborNodes = node.neighbors(); neighborNodes.hasNext();) {
               Node neighborNode = neighborNodes.next();

               if (GraphContext.Predicates.IsVisualizedAndDisplayable.test(neighborNode)) {
                  neighborIds.add(transform.apply(neighborNode));
               }
            }
         }
      }
      return neighborIds;
   }

   @Override
   @Operation
   public List<CsiMap<String,Integer>> getNodeNeighborCounts(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                                                             @QueryParam("nodeId") Integer nodeId) {
      List<CsiMap<String,Integer>> neighborsList = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            Graph graph = graphContext.getGraphData();

            // Count the given node's neighbors neighbors.
            neighborsList = new ArrayList<CsiMap<String,Integer>>();
            Node node = graph.getNode(nodeId);
            int howMany = 0;
            Function<Node,CsiMap<String,Integer>> transform = GraphContext.Functions.mapNodeToNeighborCount();

            for (Iterator<Node> neighborNodes = node.neighbors(); neighborNodes.hasNext();) {
               Node neighborNode = neighborNodes.next();

               if (GraphContext.Predicates.IsVisualizedAndDisplayable.test(neighborNode)) {
                  neighborsList.add(transform.apply(neighborNode));
                  howMany++;
               }
            }
            // Count the given nodes neighbors.
            CsiMap<String,Integer> currentNodeProp = new CsiMap<String,Integer>();

            currentNodeProp.put("id", node.getRow());

            NodeStore details = GraphManager.getNodeDetails(node);

            if (details.isDisplayable() && node.getBoolean(GraphContext.IS_VISUALIZED)) {
                currentNodeProp.put("count", Integer.valueOf(howMany));
            } else {
                currentNodeProp.put("count", Integer.valueOf(0));
            }
            neighborsList.add(currentNodeProp);
         }
      }
      return neighborsList;
   }

   @Override
   public List<EdgeListDTO> gwtEdgeListing(String vizUuid) {
      List<EdgeListDTO> edgesDTO = new ArrayList<EdgeListDTO>();
      EdgeListing listing = edgeListing(vizUuid);

      if (listing != null) {
         Graph graph = listing.getGraph();
         GraphContext context;

         try {
            context = GraphServiceUtil.getGraphContext(vizUuid);
         } catch (TaskAbortedException e) {
            return new ArrayList<EdgeListDTO>();
         }
         SelectionModel selection = context.getSelection(GraphManager.DEFAULT_SELECTION);

         for (Iterator<Edge> edges = graph.edges(); edges.hasNext();) {
            Edge edge = edges.next();

            if (GraphContext.Predicates.IsEdgeVisualized.test(edge)) {
               try {
                  edgesDTO.add(createEdge(edge, selection));
               } catch (IllegalArgumentException iae) {
                  LOG.warn("Encountered transient error while writing edge information.  Not all data may have been conveyed",
                           iae);
               }
            }
         }
      }
      return edgesDTO;
   }

    private EdgeListDTO createEdge(Edge edge, SelectionModel selection) {
        EdgeListDTO dto = new EdgeListDTO();

        LinkStore details = GraphManager.getEdgeDetails(edge);
        dto.ID = edge.getRow();
        dto.sourceId = edge.getSourceNode().getRow();
        dto.targetId = edge.getTargetNode().getRow();
        dto.type = details.getType();
        dto.hidden = details.isHidden();
        dto.plunked = details.isPlunked();
        // only nodes can be bundled
        // dto.bundled = details.isBundled();
        dto.displayable = details.isDisplayable();
        dto.isVisualized = details.isVisualized();
        dto.editable = details.isEditable();
        dto.label = details.getLabel();
        dto.width = details.getWidth();
        dto.setOpacity(details.getTransparency());
        dto.types = new ArrayList<GraphTypesDTO>();

        if ((null != selection) && (null != selection.links)) {

            dto.selected = selection.links.contains(edge.getRow());

        } else {

            dto.selected = false;
        }

        Iterator<Map.Entry<String, Integer>> iter = details.getTypes().entrySet().iterator();
        while (iter.hasNext()) {
            GraphTypesDTO gdto = new GraphTypesDTO();
            Map.Entry<String, Integer> entry = iter.next();
            gdto.type = entry.getKey();
            gdto.value = entry.getValue();
            dto.types.add(gdto);
        }

        dto.source = details.getFirstEndpoint().getLabel();
        dto.target = details.getSecondEndpoint().getLabel();
        return dto;
    }

    @Operation
    public EdgeListing edgeListing(@QueryParam(value = VIZUUID_PARAM) String vizUuid) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext == null) {
            return null;
        }
        EdgeListing listing = null;

        synchronized (graphContext) {
            Graph graph = graphContext.getGraphData();
            if (graph == null) {
                throw new RuntimeException("No graph context.");
            }

//            Predicate<Node> filter = GraphContext.Predicates.IsNodeVisualized;
            listing = new EdgeListing(graph);
            // TaskHelper.reportComplete(listing);
        }

        return listing;
    }

   @Override
   @Operation
   public void fitToSelection(@QueryParam(value = VIZUUID_PARAM) String vizUuid) {
      GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);

      if (context != null) {
         synchronized (context) {
            SelectionModel selection = context.getSelection(GraphManager.DEFAULT_SELECTION);

            if (!selection.isCleared()) {
               VisualGraph visualGraph = context.getVisualGraph();
               // TODO: refactor to GraphManger with computation of bounding area
               // instead of
               // building a running list of all the items then computing the area.
               List<VisualItem> items = new ArrayList<VisualItem>();

               for (Integer id : selection.nodes) {
                  VisualItem item = (VisualItem) visualGraph.getNode(id);

                  if (item != null) {
                     item = context.getDisplayableFor((NodeItem) item);

                     items.add(item);
                  }
               }
               for (Integer id : selection.links) {
                  VisualItem item = (VisualItem) visualGraph.getEdge(id);

                  if (item != null) {
                     items.add(item);
                  }
               }
               GraphManager graphManager = GraphManager.getInstance();
               Rectangle2D bounds = graphManager.getItemBounds(items);

               context.computeSoftMinimumBounds(bounds);
               // expand with 15% border to ensure nothing gets clipped on the
               // edges of the display
               double dim = Math.max(bounds.getWidth(), bounds.getHeight());
               GraphicsLib.expand(bounds, dim * .15);
               context.fitToRegion(bounds);
            }
         }
      }
   }

   @Override
   @Operation
   public void fitToSize(@QueryParam(value = VIZUUID_PARAM) String vizUuid, int width, int height) {
      GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);

      if ((context != null) && !context.isInvalidated()) {
//		System.out.println("fit");
//		System.out.println(width);
//		System.out.println(height);

         synchronized (context) {
            Dimension vdim = new Dimension(width, height);

            validiateDimensions(vdim);
            context.getDisplay().setSize(vdim);
            context.fitToSize();
            context.applySoftBounds();
            context.zoom(.9);
         }
      }
   }

    private void addPropertyValue(Map<String, Property> attributes, String key, Object value) {
        Property property = attributes.get(key);
        if (property == null) {
            property = new Property(key);
            attributes.put(key, property);
        }

        if (!property.getValues().contains(value)) {
            property.getValues().add(value);
        }
    }

    @SuppressWarnings("unchecked")
    private void layoutAndMergeManualBundleNodes(List<CsiMap> positions, Node bundleNode, String animationLayout,
                                                 String vizuuid) {

//        List<CsiMap> mergedList = new ArrayList<CsiMap>();
        Visualization vis = getVisualization(vizuuid);

        if (vis != null) {
        Graph graph = (Graph) vis.getSourceData("graph");
        Display display = vis.getDisplay(0);
        Point2D itemPoint;
        Point2D displayPoint = new Point2D.Double(0, 0);
        SelectionModel selection = getSelectionModel(DEFAULT_SELECTION, vizuuid);

        // Run layout
        GraphConstants.eLayoutAlgorithms layout = LayoutHelper.getLayout(animationLayout);
        GraphManager gm = GraphManager.getInstance();

        TaskHelper.checkForCancel();
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizuuid);
        gm.runPlacement(graph, layout, graphContext);

        VisualGraph vGraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);
        Iterator nodes = graph.nodes();

        while (nodes.hasNext()) {
            TaskHelper.checkForCancel();

            Node node = (Node) nodes.next();
            NodeItem vizNode = (NodeItem) vGraph.getNode(node.getRow());
            NodeStore ns = GraphManager.getNodeDetails(vizNode);

            // Translate that point to the display point.
            // If we are on a bundled node it will not be visible. We also need
            // the displayPoint to be the bundles x,y
            // Because the bundled nodes end positions will be the same as the
            // bundle node.
            if (ns.isBundled()) {
                VisualItem vizBundleNode = (VisualItem) vGraph.getNode(1);

                itemPoint = new Point2D.Double(vizBundleNode.getX(), vizBundleNode.getY());
                displayPoint = toDisplayPoint(display, itemPoint);

            } else if (!ns.isBundled() && vizNode.isVisible()) {
                itemPoint = new Point2D.Double(vizNode.getX(), vizNode.getY());
                displayPoint = toDisplayPoint(display, itemPoint);
            }

            // Loop through our before layout position list and add in the after
            // layout position values.
            for (CsiMap m : positions) {
                TaskHelper.checkForCancel();

                Object itemId = m.get("ID");

                if (Integer.parseInt(itemId.toString()) == vizNode.getRow()) {
                    CsiMap<String, Object> prop = m;

                    boolean selected = selection.nodes.contains(vizNode.getRow());
                    prop.put(PROPERTY_SELECTED, Boolean.toString(selected));

                    prop.put("height", vizNode.getBounds().getHeight());
                    prop.put("width", vizNode.getBounds().getWidth());
                    prop.put("afterX", displayPoint.getX());
                    prop.put("afterY", displayPoint.getY());

                    // get the label
                    NodeStore ns2 = GraphManager.getNodeDetails(vizNode);
                    List<String> labels = ns2.getLabels();
                    if ((labels != null) && !labels.isEmpty()) {
                        prop.put(NAME_PARAMETER, labels.get(0));
                    }
                    prop.put("type", ns2.getType());
                }
            }
        }

        Double currentScale = display.getScale();
        CsiMap<String, Object> prop = new CsiMap<String, Object>();
        prop.put("currentScale", currentScale);
        positions.add(0, prop);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Operation
    @Interruptable
    public GraphLegendInfo legendData(@QueryParam(value = VIZUUID_PARAM) String vizUuid) throws CentrifugeException {

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        GraphLegendInfo graphLegend = new GraphLegendInfo();

        if (graphContext == null) {
            return graphLegend;
        }

        synchronized (graphContext) {
            RelGraphViewDef def = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
            graphLegend.setNodeLegendItems(getNodeLegendItems(def));
            graphLegend.setLinkLegendItems(getLinkLegendItems(def));

            graphLegend.graphLegendNodeSummary = new GraphLegendNodeSummary();
            int totalNodes = 0;
            int visibleNodes = 0;
            List<GraphNodeLegendItem> nodeItems = graphLegend.getNodeLegendItems();
            for (GraphNodeLegendItem item : nodeItems) {
                if (!item.key
                        .equals(GraphConstants.CSI_INTERNAL_NAMESPACE + "." + GraphConstants.NEW_GENERATION_FIELD_TYPE)
                        && !item.key.equals(GraphConstants.CSI_INTERNAL_NAMESPACE + "."
                        + GraphConstants.UPDATED_GENERATION_FIELD_TYPE)) {
                    totalNodes += item.totalCount;
                    visibleNodes += item.count;
                }
            }

            graphLegend.graphLegendNodeSummary.totalNodes = totalNodes;
            graphLegend.graphLegendNodeSummary.totalVisibleNodes = visibleNodes;

            // do we have visible multi-types? if so include that count as well
            TypeInfo multiTypes = graphContext.getMultiTypeNodeLegendEntry();

            if ((multiTypes != null) && (multiTypes.totalCount > 0) && (multiTypes.visible > 0)) {
                graphLegend.graphLegendNodeSummary.multiTypedNodes = multiTypes.visible;
            }
            TypeInfo linkMultiTypes = graphContext.getMultiTypeLinkLegendEntry();

            if ((linkMultiTypes != null) && (linkMultiTypes.totalCount > 0) && (linkMultiTypes.visible > 0)) {
                graphLegend.graphLegendNodeSummary.multiTypedLinks = linkMultiTypes.visible;
            }
        }

        return graphLegend;
    }

   // Graph Legend has changed for
   @Deprecated
   private List<GraphNodeLegendItem> getNodeLegendItems(RelGraphViewDef def) throws CentrifugeException {
      List<GraphNodeLegendItem> legendInfo = Collections.emptyList();
      GraphContext graphContext = GraphServiceUtil.getGraphContext(def.getUuid());

      if (graphContext != null) {
         legendInfo = new ArrayList<GraphNodeLegendItem>();

         synchronized (graphContext) {
            Visualization vis = graphContext.getVisualization();
            GraphTheme theme = graphContext.getTheme();
            Graph graph = (Graph) vis.getSourceData("graph");

            if (graph == null) {
               throw new CentrifugeException("No graph passed to legendData task");
            }
            graphContext.updateVisibleNodeLegend();

            Map<String,TypeInfo> legend = graphContext.getNodeLegend();

            for (TypeInfo typeInfo : legend.values()) {
               // Options options = null;
               // if (optionSet != null) {
               // options = optionSet.getOptions(OptionSet.NODE_TYPE,
               // typeInfo.name);
               // }

               // FIXME: the styling information needs to come from the
               // OptionSet and not the run-time representation.
               // This is needed since a node can have multiple types.
               GraphNodeLegendItem nl = new GraphNodeLegendItem();
               nl.iconScale = (float) 0.75;
               NodeStyle nodeStyle = null;
               NodeStyle emptyStyle = null;
               ShapeType defaultShape = null;

               if (theme != null) {
                  nodeStyle = theme.findNodeStyle(typeInfo.name);
                  defaultShape = theme.getDefaultShape();
               }
               if (GraphAttributeHelper.resolveAttribute(ObjectAttributes.CSI_INTERNAL_SCALE, typeInfo, nodeStyle,
                     null) != null) {
                  nl.iconScale = Float.parseFloat(GraphAttributeHelper
                        .resolveAttribute(ObjectAttributes.CSI_INTERNAL_SCALE, typeInfo, nodeStyle, null).toString());
               }
               if (typeInfo.key.equals(GraphConstants.CSI_INTERNAL_NAMESPACE + "." + GraphConstants.BUNDLED_NODES)) {
                  if ((theme != null) && (theme.getBundleStyle() != null)) {
                     if (theme.getBundleStyle().getIconId() != null) {
                        nl.iconId = theme.getBundleStyle().getIconId();
                     }
                     if (theme.getBundleStyle().getShape() != null) {
                        nl.shape = theme.getBundleStyle().getShape();
                     }
                     if (theme.getBundleStyle().getColor() != null) {
                        nl.color = theme.getBundleStyle().getColor();
                     }
                  } else {

                  }

                  // nl.iconURI =
                  // OptionSetManager.toResourceUrl(optionSet.getBundleIcon());
                  // nl.shape = optionSet.bundleShape;
                  // nl.color = Long.parseLong(optionSet.bundleColor);
               } else {
                  Integer color = null;

                  if (typeInfo.colorOverride) {
                     color = (Integer) GraphAttributeHelper.resolveAttribute(ObjectAttributes.CSI_INTERNAL_COLOR,
                           typeInfo, emptyStyle, null);

                     nl.shape = (ShapeType) GraphAttributeHelper.resolveAttribute(ObjectAttributes.CSI_INTERNAL_SHAPE,
                           typeInfo, emptyStyle, null);
                     // } else if(nodeStyle == null && theme != null &&
                     // theme.getDefaultShape() != null){
                     // color = (Integer)
                     // GraphAttributeHelper.resolveAttribute(
                     // ObjectAttributes.CSI_INTERNAL_COLOR, typeInfo,
                     // nodeStyle);
                     //
                     // nl.shape = theme.getDefaultShape();
                  } else {
                     color = (Integer) GraphAttributeHelper.resolveAttribute(ObjectAttributes.CSI_INTERNAL_COLOR,
                           typeInfo, nodeStyle, null);

                     nl.shape = (ShapeType) GraphAttributeHelper.resolveAttribute(ObjectAttributes.CSI_INTERNAL_SHAPE,
                           typeInfo, nodeStyle, defaultShape);
                  }
                  nl.color = (color == null) ? 0xFF0000 : color;
                  nl.iconId = (String) GraphAttributeHelper.resolveAttribute(ObjectAttributes.CSI_INTERNAL_ICON,
                        typeInfo, nodeStyle, null);
               }
               nl.count = typeInfo.visible;
               nl.typeName = typeInfo.name;
               nl.totalCount = typeInfo.totalCount;
               nl.key = typeInfo.key;

               legendInfo.add(nl);
            }
            if (graphContext.getSelection(GraphConstants.NEW_GENERATION) != null) {
               GraphNodeLegendItem nl = new GraphNodeLegendItem();
               nl.color = Configuration.getInstance().getGraphAdvConfig().getDefaultNewGenColor().getRGB();
               Collection<Integer> newNodes = graphContext.getSelection(GraphConstants.NEW_GENERATION).nodes;

               if ((newNodes != null) && !newNodes.isEmpty()) {
                  nl.count = countOnDisplay(graphContext.getVisualGraph(), newNodes);
                  // I have no idea what this shape was supposed to mean, RIP
                  // shape
                  // nl.shape = "Generation";
                  nl.typeName = GraphConstants.NEW_GENERATION_FIELD_TYPE;
                  nl.totalCount = newNodes.size();
                  nl.key = GraphConstants.CSI_INTERNAL_NAMESPACE + "." + nl.typeName;
                  legendInfo.add(nl);
               }
            }
            if (graphContext.getSelection(GraphConstants.UPDATED_GENERATION) != null) {
               Collection<Integer> updatedNodes = graphContext.getSelection(GraphConstants.UPDATED_GENERATION).nodes;

               if ((updatedNodes != null) && !updatedNodes.isEmpty()) {
                  GraphNodeLegendItem nl = new GraphNodeLegendItem();

                  nl.color = Configuration.getInstance().getGraphAdvConfig().getDefaultUpdateGenColor().getRGB();
                  nl.count = countOnDisplay(graphContext.getVisualGraph(), updatedNodes);
                  // I have no idea what this shape was supposed to mean, RIP
                  // shape
                  // nl.shape = "Generation";
                  nl.typeName = GraphConstants.UPDATED_GENERATION_FIELD_TYPE;
                  nl.totalCount = updatedNodes.size();
                  nl.key = GraphConstants.CSI_INTERNAL_NAMESPACE + "." + nl.typeName;
                  legendInfo.add(nl);
               }
            }
         }
         Collections.sort(legendInfo, new LegendItemComparator());
      }
      return legendInfo;
   }

   private List<GraphLinkLegendItem> getLinkLegendItems(RelGraphViewDef def) throws CentrifugeException {
      List<GraphLinkLegendItem> legendInfo = Collections.emptyList();
      GraphContext graphContext = GraphServiceUtil.getGraphContext(def.getUuid());

      if (graphContext != null) {
         legendInfo = new ArrayList<GraphLinkLegendItem>();

         synchronized (graphContext) {
            Visualization vis = graphContext.getVisualization();
            Graph graph = (Graph) vis.getSourceData("graph");

            if (graph == null) {
                throw new CentrifugeException("No graph passed to legendData task");
            }
            // if (graphContext.getSelection(GraphConstants.NEW_GENERATION) !=
            // null ||
            // graphContext.getSelection(GraphConstants.UPDATED_GENERATION) !=
            // null) {
            // Possibly new links visible in legend, so we update
            graphContext.updateVisibleLinkLegend();
            // }
            Map<String, GraphLinkLegendItem> legend = graphContext.getLinkLegend();
            // OptionSet optionSet = graphContext.getOptionSet();

            for (GraphLinkLegendItem legendItem : legend.values()) {
                Theme theme = graphContext.getTheme();
                LinkStyle linkStyle = null;

                if (theme != null) {
                    linkStyle = ThemeManager.getLinkStyle(legendItem.typeName, (GraphTheme) theme);
                }
                GraphLinkLegendItem nl = new GraphLinkLegendItem();
                Long color = (Long) GraphAttributeHelper.resolveLinkAttribute(ObjectAttributes.CSI_INTERNAL_COLOR,
                        legendItem, linkStyle);
                if (color == null) {
                   nl.color = 0L;
                } else {
                    legendItem.color = color;
                    nl.color = color;
                }
                // nl.shape = (String)
                // GraphAttributeHelper.resolveAttribute(ObjectAttributes.CSI_INTERNAL_SHAPE,
                // legendItem);
                nl.count = legendItem.count;
                nl.typeName = legendItem.typeName;
                nl.totalCount = legendItem.totalCount;
                nl.key = legendItem.key;

                legendInfo.add(nl);
            }
            if (graphContext.getSelection(GraphConstants.NEW_GENERATION) != null) {
               GraphLinkLegendItem nl = new GraphLinkLegendItem();
                nl.color = Configuration.getInstance().getGraphAdvConfig().getDefaultNewGenColor().getRGB();
                Collection<Integer> newNodes = graphContext.getSelection(GraphConstants.NEW_GENERATION).links;

                if ((newNodes != null) && !newNodes.isEmpty()) {
//					nl.count = countOnDisplay(graphContext.getVisualGraph(), newNodes);
                    // I have no idea what this shape was supposed to mean, RIP
                    // shape
                    // nl.shape = "Generation";
                    nl.typeName = GraphConstants.NEW_GENERATION_FIELD_TYPE;
                    nl.totalCount = newNodes.size();
                    nl.key = GraphConstants.CSI_INTERNAL_NAMESPACE + "." + nl.typeName + ".link";
                    legendInfo.add(nl);
                }
            }
            if (graphContext.getSelection(GraphConstants.UPDATED_GENERATION) != null) {
                Collection<Integer> updatedNodes = graphContext.getSelection(GraphConstants.UPDATED_GENERATION).links;

                if ((updatedNodes != null) && !updatedNodes.isEmpty()) {
                   GraphLinkLegendItem nl = new GraphLinkLegendItem();
                    nl.color = Configuration.getInstance().getGraphAdvConfig().getDefaultUpdateGenColor().getRGB();
//					nl.count = countOnDisplay(graphContext.getVisualGraph(), updatedNodes);
                    // I have no idea what this shape was supposed to mean, RIP shape
                    // nl.shape = "Generation";
                    nl.typeName = GraphConstants.UPDATED_GENERATION_FIELD_TYPE;
                    nl.totalCount = updatedNodes.size();
                    nl.key = GraphConstants.CSI_INTERNAL_NAMESPACE + "." + nl.typeName + ".link";
                    legendInfo.add(nl);
                }
            }
         }
         Collections.sort(legendInfo, new LegendItemComparator());
      }
      return legendInfo;
   }

   private int countOnDisplay(VisualGraph graph, Collection<Integer> nodeIds) {
      int result = 0;

      if (nodeIds != null) {
         for (Integer id : nodeIds) {
            NodeItem node = (NodeItem) graph.getNode(id);

            if (node.isVisible()) {
               result++;
            }
         }
      }
      return result;
   }

   @Override
   @Operation
   @Interruptable
   public void zoomTo(@QueryParam(value = "zoom") String zoom, @QueryParam(value = VIZUUID_PARAM) String vizUuid) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         double scale = Double.parseDouble(zoom);

         graphContext.zoom(scale);
      }
   }

   @Override
   @Operation
   @Interruptable
   public void zoomPercent(@QueryParam(value = "percent") String percent,
                           @QueryParam(value = VIZUUID_PARAM) String vizUuid) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         graphContext.zoomPercent(Double.valueOf(percent));
      }
   }

   @Override
   public FindItemDTO gwtFindItem2(int x1, int y1, String vizUuid, boolean all) throws CentrifugeException {
      Map<String, Object> item = findItem2("" + x1, "" + y1, vizUuid, all);

      return (item == null) ? null : createFindItemDTO(item);
   }

    @Override
    public FindItemDTO getItem(String vizUuid, FindItemDTO item, boolean aggregates) {
        Visualization vis = GraphHelper.getVisualization(vizUuid);
        Display display = vis.getDisplay(0);
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        VisualItem vi = null;
        if (item.getItemType().equals("node")) {


            Iterator nodes = graphContext.getVisualGraph().nodes();
            while (nodes.hasNext()) {
                VisualItem nodee = (VisualItem) nodes.next();
                if (nodee.getRow() == item.ID) {
                    vi = nodee;
                    break;
                }


            }
        } else {
            Iterator edges = graphContext.getVisualGraph().edges();
            while (edges.hasNext()) {
                VisualItem edge = (VisualItem) edges.next();
                if (edge.getRow() == item.ID) {
                    vi = edge;
                    break;
                }


            }
        }


        SelectionModel selection = getSelectionModel(item.getID() + "", vizUuid);
        CsiMap<String, Object> mapOfItem = createMapOfItem(selection, 0, 0, display, vi);
        try {
            buildInfoMap(graphContext, vi, true, aggregates, mapOfItem);
            return createFindItemDTO(mapOfItem);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        return null;
    }

   @Override
   public MultiTypeInfo gwtFindItemTypes(int x1, int y1, String vizUuid) throws CentrifugeException {
      MultiTypeInfo info = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            int x = Double.valueOf(x1).intValue();
            int y = Double.valueOf(y1).intValue();
            Visualization vis1 = getVisualization(vizUuid);
            Visualization vis = vis1;

            if (vis != null) {
               synchronized (vis) {
                  Display display = vis.getDisplay(0);

                  synchronized (display) {
                     VisualItem item = display.findItem(new Point(x, y));

                     if (item != null) {
                        if (item instanceof TableNodeItem) {
                            TableNodeItem node = (TableNodeItem) item;
                            NodeStore detail = GraphManager.getNodeDetails(item);
                            Map<String, Integer> typeMap = detail.getTypes();

                            // Returns only if multityped
                            if ((typeMap != null) && (typeMap.size() >= 2)) {
                               double nodeX = node.getX();
                               double nodeY = node.getY();
                               info = new MultiTypeInfo();

                               info.setTypes(new HashSet(typeMap.keySet()));

                               try {
                                  Point2D center = display.transformItemPointToScreen(new Point((int) nodeX, (int) nodeY));

                                  info.setX(center.getX());
                                  info.setY(center.getY());
                               } catch (NoninvertibleTransformException e) {
                                  throw new CentrifugeException(e.getMessage(), e);
                               }
                            }
                        } else if (item instanceof TableEdgeItem) {
                            LinkStore detail = GraphManager.getEdgeDetails(item);
                            Map<String, Integer> typeMap = detail.getTypes();

                            // Returns only if multityped
                            if ((typeMap != null) && (typeMap.size() >= 2)) {
                               TableEdgeItem edge = (TableEdgeItem) item;
                               TableNodeItem source = (TableNodeItem) edge.getSourceNode();
                               TableNodeItem target = (TableNodeItem) edge.getTargetNode();
                               double startX = source.getX();
                               double startY = source.getY();
                               double endX = target.getX();
                               double endY = target.getY();
                               int edgeX = (int) (startX + endX) / 2;
                               int edgeY = (int) (startY + endY) / 2;
                               info = new MultiTypeInfo();

                               info.setNode(false);
                               info.setTypes(new HashSet(typeMap.keySet()));

                               try {
                                  Point2D center = display.transformItemPointToScreen(new Point(edgeX, edgeY));

                                  info.setX(center.getX());
                                  info.setY(center.getY());
                               } catch (NoninvertibleTransformException e) {
                                  throw new CentrifugeException(e.getMessage(), e);
                               }
                            }
                        }
                     }
                  }
               }
            }
         }
      }
      return info;
   }

   @Operation
   public Map<String,Object> findItem2(@QueryParam("x") String x1, @QueryParam("y") String y1,
                                       @QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam("all") boolean all)
         throws CentrifugeException {
      Map<String,Object> result = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            int x = Double.valueOf(x1).intValue();
            int y = Double.valueOf(y1).intValue();
            Visualization vis1 = getVisualization(vizUuid);
            Visualization vis = vis1;

            if (vis != null) {
               synchronized (vis) {
                  Display display = vis.getDisplay(0);

                  synchronized (display) {
                     VisualItem item = display.findItem(new Point(x, y));

                     if (item != null) {
                        result = getItemInfo(graphContext, item, true, x1, y1, all);
                     }
                  }
               }
            }
         }
      }
      return result;
   }

    @Override
    public FindItemDTO gwtFindItem(String id, String x1, String y1, String vizUuid) throws CentrifugeException {

        Map<String, Object> item = findItem(id, x1, y1, vizUuid);

        if (item == null) {
            return null;
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> oldtips = (Map<String, Object>) item.remove("toolTipProps");
            if (oldtips != null) {
                Map<String, Object> newtips = new CsiMap<String, Object>();
                newtips.put("Label", item.get(DISPLAY_LABEL));
                newtips.put("Type", item.get(VISUAL_ITEM_TYPE));
                if (item.get(MEMBER_TYPES) != null) {
                    newtips.put("Member Types", item.get(MEMBER_TYPES));
                }
                String key = (String) item.get("itemKey");
                RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);

                List<Annotation> annotations = relGraphViewDef.getAnnotations();

                List<String> itemAnnotations = new ArrayList<String>();

                for (Annotation annotation : annotations) {
                    if (annotation.getParentKey().equals(key)) {
                        itemAnnotations.add(annotation.getHtmlString());
                    }
                }

                if (!itemAnnotations.isEmpty()) {
                    newtips.put(COMMENTS, itemAnnotations);
                }

                newtips.putAll(oldtips);

                item.put(TOOLTIPS_TYPES, newtips);
            }

            return createFindItemDTO(item);
        }
    }

    @Override

    public FindItemDTO gwtFindItemNear(String id, String x1, String y1, String vizUuid) {

        List<CsiMap<String, Object>> items = new ArrayList<CsiMap<String, Object>>();
        try {
            items.add(findLinksNear(id, x1, y1, vizUuid));
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }

        if (items == null) {
            return null;
        }

        FindItemDTO out = null;
        for (CsiMap<String, Object> item : items) {
            if (item.isEmpty()) {
                return null;
            }


            Map<String, Object> oldtips = (Map<String, Object>) item.remove("toolTipProps");
            if (oldtips != null) {
                Map<String, Object> newtips = new CsiMap<String, Object>();
                newtips.put("Label", item.get(DISPLAY_LABEL));
                newtips.put("Type", item.get(VISUAL_ITEM_TYPE));
                if (item.get(MEMBER_TYPES) != null) {
                    newtips.put("Member Types", item.get(MEMBER_TYPES));
                }
                String key = (String) item.get("itemKey");
                RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);

                List<Annotation> annotations = relGraphViewDef.getAnnotations();

                List<String> itemAnnotations = new ArrayList<String>();

                for (Annotation annotation : annotations) {
                    if (annotation.getParentKey().equals(key)) {
                        itemAnnotations.add(annotation.getHtmlString());
                    }
                }

                if (!itemAnnotations.isEmpty()) {
                    newtips.put(COMMENTS, itemAnnotations);
                }

                newtips.putAll(oldtips);

                item.put(TOOLTIPS_TYPES, newtips);
            }

            try {
                out = createFindItemDTO(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return out;

    }

    private FindItemDTO createFindItemDTO(Map<String, Object> info) throws CentrifugeException {
        FindItemDTO dto = new FindItemDTO();
        dto.ID = info.containsKey("ID") ? ((Integer) info.remove("ID")) : null;
        dto.X = info.containsKey("X") ? (Double) info.remove("X") : null;
        dto.Y = info.containsKey("Y") ? (Double) info.remove("Y") : Double.valueOf(0.0);
        dto.displayX = info.containsKey("displayX") ? (Double) info.remove("displayX") : null;
        dto.displayY = info.containsKey("displayY") ? (Double) info.remove("displayY") : null;
        dto.clickX = info.containsKey("clickX") ? Double.valueOf(((Integer) info.remove("clickX")).doubleValue())
                : null;
        dto.clickY = info.containsKey("clickY") ? Double.valueOf(((Integer) info.remove("clickY")).doubleValue())
                : null;
        dto.selected = info.containsKey(PROPERTY_SELECTED) ? Boolean.valueOf((String) info.remove(PROPERTY_SELECTED))
                : null;
        dto.setMoreDetails(info.containsKey(CSI_MORE_DETAILS) && (boolean) info.remove(CSI_MORE_DETAILS));
        dto.itemId = info.containsKey("itemId") ? (String) info.remove("itemId") : null;
        dto.itemKey = info.containsKey("itemKey") ? ((String) info.remove("itemKey")) : null;
        dto.itemType = info.containsKey("itemType") ? (String) info.remove("itemType") : null;
        dto.objectType = info.containsKey("csi.object.type") ? (String) info.remove("csi.object.type") : null;
        dto.anchored = info.containsKey("anchored") ? (Boolean) info.remove("anchored") : null;
        dto.hideLabels = info.containsKey(PROPERTY_HIDE_LABELS) ? (Boolean) info.remove(PROPERTY_HIDE_LABELS) : null;
        dto.size = info.containsKey(PROPERTY_SIZE) ? ((Double) info.remove(PROPERTY_SIZE)) : null;
        dto.bundle = info.containsKey(ACTION_BUNDLE) ? (Boolean) info.remove(ACTION_BUNDLE) : null;
        dto.tooltipOrder = info.containsKey(TOOLTIP_ORDER) ? (Map<String, Integer>) info.remove(TOOLTIP_ORDER) : null;
        dto.hidden = info.containsKey(ObjectAttributes.CSI_INTERNAL_HIDDEN)
                ? (Boolean) info.remove(ObjectAttributes.CSI_INTERNAL_HIDDEN) : null;
        dto.bundleCount = info.containsKey(BUNDLE_COUNT_INFO) ? (String) info.remove(BUNDLE_COUNT_INFO) : null;
        dto.label = info.containsKey(DISPLAY_LABEL) ? (String) info.remove(DISPLAY_LABEL) : null;
        dto.labels = info.containsKey(ALL_LABELS) ? new ArrayList<String>((List<String>) info.remove(ALL_LABELS)) : null;
        dto.visualItemType = null;
        dto.setPlunked(info.containsKey(PLUNKED) && (Boolean) info.remove(PLUNKED));
        Object obj = info.containsKey(VISUAL_ITEM_TYPE) ? info.remove(VISUAL_ITEM_TYPE) : null;
        if ((obj != null) && (obj instanceof String)) {
            dto.visualItemType = new VisualItemString((String) obj);
        }
        if ((obj != null) && (obj instanceof List)) {
            dto.visualItemType = new VisualItemArray((List<String>) obj);
        }
        obj = info.containsKey(GraphContext.IS_VISUALIZED) ? info.remove(GraphContext.IS_VISUALIZED) : null;
        if ((obj != null) && (obj instanceof Boolean)) {
            dto.isVisualized = (Boolean) obj;
        }
        if ((obj != null) && (obj instanceof String)) {
            dto.isVisualized = new Boolean((String) obj);
        }
        dto.component_id = info.containsKey(GraphConstants.COMPONENT_ID)
                ? (Integer) info.remove(GraphConstants.COMPONENT_ID) : null;
        dto.countInDispEdges = info.containsKey(GraphConstants.COUNT_IN_DISPEDGES)
                ? (Integer) info.remove(GraphConstants.COUNT_IN_DISPEDGES) : null;
        dto.subgraphNodeId = info.containsKey(GraphConstants.SUBGRAPH_NODE_ID)
                ? (Integer) info.remove(GraphConstants.SUBGRAPH_NODE_ID) : null;

        dto.neighborTypeCounts = info.containsKey("neighbors.types")
                ? (Map<String, Integer>) info.remove("neighbors.types") : null;
        CsiMap<String, Object> tips = info.containsKey(TOOLTIPS_TYPES)
                ? (CsiMap<String, Object>) info.remove(TOOLTIPS_TYPES) : null;
        dto.tooltips = createTooltipPropsDTO(tips);
        List<CsiMap<String, Object>> neighborMap = info.containsKey("neighbors")
                ? (List<CsiMap<String, Object>>) info.remove("neighbors") : new ArrayList<CsiMap<String, Object>>();
        for (CsiMap<String, Object> map : neighborMap) {
            dto.neighbors.add(createNeighborsDTO(map));
        }
        if (info.containsKey(DIRECTION)) {
            dto.directionMap = (HashMap<String, String>) info.get(DIRECTION);
            info.remove(DIRECTION);
        }
        if (info.containsKey(COMPUTED)) {
            dto.computed = (CsiMap<String, CsiMap<String, String>>) info.get(COMPUTED);
            info.remove(COMPUTED);
        }
        if (!info.isEmpty()) {
            dumpMap(info, "FindItemDTO");
        }

        return dto;

    }

    private NeighborsDTO createNeighborsDTO(CsiMap<String, Object> map) {
        NeighborsDTO dto = new NeighborsDTO();

        dto.ID = map.containsKey("ID") ? ((Integer) map.remove("ID")).intValue() : null;
        dto.objectType = map.containsKey("csi.object.type") ? (String) map.remove("csi.object.type") : null;
        dto.X = map.containsKey("X") ? (Double) map.remove("X") : null;
        dto.Y = map.containsKey("Y") ? (Double) map.remove("Y") : null;
        dto.displayX = map.containsKey("displayX") ? (Double) map.remove("displayX") : null;
        dto.displayY = map.containsKey("displayY") ? (Double) map.remove("displayY") : null;
        dto.label = map.containsKey(NAME_PARAMETER) ? (String) map.remove(NAME_PARAMETER) : null;
        dto.columns = convertColumnNameEntries(map);
        if (!map.isEmpty()) {
            dumpMap(map, "NeighborsDTO");
        }
        return dto;
    }

   private CsiMap<String,Object> findLinksNear(String id, String x1, String y1, String vizUuid)
         throws CentrifugeException {
      CsiMap<String,Object> result = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext == null) {
         result = new CsiMap<String,Object>();
      } else {
         synchronized (graphContext) {
            Iterator edges = graphContext.getVisualGraph().edges();
            SelectionModel selection = getSelectionModel(id, vizUuid);
            int x = Double.valueOf(x1).intValue();
            int y = Double.valueOf(y1).intValue();
            Visualization vis1 = getVisualization(vizUuid);
            Visualization vis = vis1;

            if (vis == null) {
               result = new CsiMap<String,Object>();
            } else {
               VisualItem item = getVisualLinkNear(edges, x, y, vis);

               result = (item == null)
                           ? new CsiMap<String,Object>()
                           : createMapOfItem(selection, x, y, vis.getDisplay(0), item);
            }
         }
      }
      return result;
   }

    private VisualItem getVisualLinkNear(Iterator edges, int x, int y, Visualization vis) {
        VisualItem item;
        synchronized (vis) {
            Display display;
            display = vis.getDisplay(0);
            synchronized (display) {
                double padding = (display.transformScreenPointToItem(new Point(0, Configuration.getInstance().getGraphAdvConfig().getLinkTargetingHelp())).getY() - display.transformScreenPointToItem(new Point(0, 0)).getY());
                List<VisualItem> foundItems = new ArrayList<VisualItem>();
                Point2D p = display.transformScreenPointToItem(new Point(x, y));
                while (edges.hasNext()) {
                    VisualItem vi = (VisualItem) edges.next();
//						VisualItem item = display.findItem(new Point(x, y));
                    if ((vi != null) && !(vi instanceof TableNodeItem)) {
                        if (!vi.isVisible()) {
                            continue;
                        }
//                        int padding = 10;
                        if (EdgeRenderer.inBounds(p.getX(), p.getY(), vi, padding)) {
                            {//test if closer than current item
                                if (foundItems.isEmpty()) {
                                    foundItems.add(vi);
                                } else if (isCloser(vi, foundItems.get(0), p.getX(), p.getY())) {
                                    foundItems.set(0, vi);
                                }

                            }
                        }
                    }
                }
                if (foundItems.isEmpty()) {
                    return null;
                }
                {
                    double distanceFromEdge = EdgeRenderer.distanceFromEdge(p.getX(), p.getY(), foundItems.get(0));
                    Point p1 = new Point(0, (int) distanceFromEdge);
                    Point p2 = new Point(0, 0);
                    double pixelDistance = Double.MAX_VALUE;
                    try {
                        double yy = display.transformItemPointToScreen(p1).getY();
                        double yy2 = display.transformItemPointToScreen(p2).getY();
                        pixelDistance = Math.abs(yy - yy2);
                        //this math is to convert distance in item space to pixels on screen.
                    } catch (NoninvertibleTransformException e) {
                        e.printStackTrace();
                    }
                    if (pixelDistance > Configuration.getInstance().getGraphAdvConfig().getLinkTargetingHelp()) {
                        //If we are more than configured # of pixels away from edge it does not count as a hit.
                        return null;

                    }
                }
                item = foundItems.get(0);
                {
                    p = display.transformScreenPointToItem(new Point(x, y));
                    EdgeItem edge = (EdgeItem) item;
                    VisualItem item0 = edge.getSourceItem();
                    VisualItem item1 = edge.getTargetItem();

                    double xx0 = item0.getX();
                    double yy0 = item0.getY();
                    double xx1 = item1.getX();
                    double yy1 = item1.getY();
                    double deltaX = (xx1 - xx0);

                    if (BigDecimal.valueOf(deltaX).compareTo(BigDecimal.ZERO) == 0) {
                        deltaX = .00000001;
                    }
                    double m1 = (yy1 - yy0) / deltaX;

                    if (BigDecimal.valueOf(m1).compareTo(BigDecimal.ZERO) == 0) {
                        m1 = .00000001;
                    }
                    double b1 = yy0 - (m1 * xx0);

                    double m2 = -1 / m1;
                    double b2 = p.getY() - (m2 * p.getX());


                    double closestX = (b2 - b1) / (m1 - m2);
                    double closestY = (m1 * closestX) + b1;
                    Point2D closestPoint = null;
                    closestPoint =
                        new Point(DoubleMath.roundToInt(closestX, RoundingMode.HALF_DOWN),
                                  DoubleMath.roundToInt(closestY, RoundingMode.HALF_DOWN));
                    edge.setX(closestPoint.getX());
                    edge.setY(closestPoint.getY());

                }
            }
        }
        return item;
    }

    private boolean isCloser(VisualItem item, VisualItem item2, double x1, double y1) {
        return EdgeRenderer.distanceFromEdge(x1, y1, item) < EdgeRenderer.distanceFromEdge(x1, y1, item2);

    }

   @Operation
   @Interruptable
   public CsiMap<String, Object> findItem(@QueryParam(value = "id") String id, @QueryParam(value = "x") String x1,
                                          @QueryParam(value = "y") String y1, @QueryParam(value = VIZUUID_PARAM) String vizUuid)
         throws CentrifugeException {
      CsiMap<String,Object> result = new CsiMap<String,Object>();
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            SelectionModel selection = getSelectionModel(id, vizUuid);
            int x = Double.valueOf(x1).intValue();
            int y = Double.valueOf(y1).intValue();
            Visualization vis1 = getVisualization(vizUuid);
            Visualization vis = vis1;

            if (vis != null) {
               synchronized (vis) {
                  Display display = vis.getDisplay(0);

                  synchronized (display) {
                     VisualItem item = display.findItem(new Point(x, y));

                     if (item == null) {
                        LOG.debug("No graph object located at " + x + ", " + y);
                     } else {
                        result = createMapOfItem(selection, x, y, display, item);
                     }
                  }
               }
            }
         }
      }
      return result;
   }

    public CsiMap<String, Object> createMapOfItem(SelectionModel selection, int x, int y, Display display, VisualItem item) {
        int itemId = item.getRow();
        Point2D itemPoint = new Point2D.Double(item.getX(), item.getY());
        if (item instanceof Edge) {
            item.setX(0);
            item.setY(0);
        }
        Point2D displayPoint = toDisplayPoint(display, itemPoint);

        CsiMap<String, Object> prop = new CsiMap<String, Object>();
        prop.put("ID", itemId);
        prop.put("X", itemPoint.getX());
        prop.put("Y", itemPoint.getY());
        prop.put("displayX", displayPoint.getX());
        prop.put("displayY", displayPoint.getY());
        prop.put("clickX", x);
        prop.put("clickY", y);
        // FIXME: find item can be a link
        boolean selected = selection.nodes.contains(itemId);
        prop.put(PROPERTY_SELECTED, Boolean.toString(selected));
        populatePropertyDetails(item, prop, true);

        if (item instanceof TableNodeItem) {
            TableNodeItem vItem = (TableNodeItem) item;
            populateAdjacentTypeInfo(vItem, prop);
            Iterator neighbors = vItem.neighbors();
            List<CsiMap<String, Object>> eList = new ArrayList<CsiMap<String, Object>>();
            prop.put("neighbors", eList);
            while (neighbors.hasNext()) {
                TableNodeItem next = (TableNodeItem) neighbors.next();

                if (next != null) {

                    CsiMap<String, Object> neighProps = new CsiMap<String, Object>();
                    Point2D neighPoint = new Point2D.Double(next.getX(), next.getY());
                    Point2D dNeighPoint = toDisplayPoint(display, neighPoint);

                    neighProps.put("ID", next.getRow());
                    neighProps.put("csi.object.type", next.getGroup());
                    neighProps.put("X", next.getX());
                    neighProps.put("Y", next.getY());
                    neighProps.put("displayX", dNeighPoint.getX());
                    neighProps.put("displayY", dNeighPoint.getY());
                    for (int i = 0; i < next.getColumnCount(); i++) {
                        String colname = next.getColumnName(i);
                        if (!colname.startsWith("_") && !GraphConstants.DOC_ID.equals(colname)) {
                            Object val = next.get(i);
                            if (colname.equals(GraphConstants.NODE_DETAIL)) {
                                NodeStore ns = (NodeStore) val;
                                neighProps.put(NAME_PARAMETER, ns.getLabel());
                            } else if (colname.equals(GraphConstants.LINK_DETAIL)) {
                                LinkStore ls = (LinkStore) val;
                                neighProps.put(NAME_PARAMETER, ls.getLabel());
                            } else {
                                neighProps.put(next.getColumnName(i), val);
                            }
                        }
                    }
                    eList.add(neighProps);
                }
            }

        }
        return prop;
    }

   private void populateAdjacentTypeInfo(NodeItem item, CsiMap<String, Object> prop) {
      if (item != null) {
         Iterator<NodeItem> neighbors = item.neighbors();
         Map<String, Integer> typeCounts = new CsiMap<String, Integer>();

         while (neighbors.hasNext()) {
            NodeItem adjacent = neighbors.next();
            NodeStore details = GraphManager.getNodeDetails(adjacent);

            if (adjacent.getBoolean(GraphContext.IS_VISUALIZED) && !details.isHidden()) {
                continue;
            }
            Map<String, Integer> types = details.getTypes();

            for (String key : types.keySet()) {
               if (!typeCounts.containsKey(key)) {
                  typeCounts.put(key, 1);
               } else {
                  int current = typeCounts.get(key);

                  typeCounts.put(key, (current + 1));
               }
            }
         }
         prop.put("neighbors.types", typeCounts);
      }
   }

    private void populateItemKey(VisualItem item, CsiMap<String, String> prop) {
        AbstractGraphObjectStore details = null;
        String itemType = null;
        if (item instanceof TableNodeItem) {
            details = GraphManager.getNodeDetails((Node) item);
            itemType = "node";
        } else {
            itemType = "link";
            details = GraphManager.getEdgeDetails((Edge) item);
        }

        prop.put("itemId", Integer.toString(item.getRow()));
        prop.put("itemKey", details.getKey());
        prop.put("itemType", itemType);
    }

    private void populatePropertyDetails(VisualItem item, CsiMap<String, Object> prop, boolean includeToolTips) {
        CsiMap<String, String> itemkeys = new CsiMap<String, String>();
        populateItemKey(item, itemkeys);
        prop.putAll(itemkeys);
        prop.put("csi.object.type", item.getGroup());

        for (int i = 0; i < item.getColumnCount(); i++) {
            String colname = item.getColumnName(i);
            if (!colname.startsWith("_") && !colname.equalsIgnoreCase(GraphConstants.DOC_ID)) {
                Object val = item.get(i);

                if (colname.equals(GraphConstants.NODE_DETAIL)) {
                    NodeStore ns = GraphManager.getNodeDetails(item);
                    populatePropsFromNodeStore(item, prop, includeToolTips, ns);
                } else if (colname.equals(GraphConstants.LINK_DETAIL)) {
                    LinkStore ls = (LinkStore) val;

                    List<String> labels = ls.getLabels();
                    if ((labels != null) && !labels.isEmpty()) {
                        prop.put(DISPLAY_LABEL, labels.get(0));
                    }
                    prop.put(ALL_LABELS, labels);
                    populateTypeLabels(prop, ls);

                    /*
                     * Populate the map with direction-related information, for
                     * the client to display in the tooltip
                     */
                    prop.put(DIRECTION, directionAggregator.aggregateDirectionTypeStrings(ls));

                    if (!Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips()) {
                        prop.put(COMPUTED, directionAggregator.aggregateComputedFields(ls));
                    }

                    prop.put(PROPERTY_SIZE, item.getSize());
                    prop.put(PLUNKED, ls.isPlunked());
                    if (includeToolTips) {
                        Map<String, Property> attributes = ls.getAttributes();
                        Set<Map.Entry<String, Property>> entrySet = attributes.entrySet();
                        CsiMap<String, Object> toolTipProps = new CsiMap<String, Object>();
                        for (Map.Entry<String, Property> entry : entrySet) {
                            String key = entry.getKey();
                            Property value = entry.getValue();

                            if (!value.isIncludeInTooltip()) {
                                continue;
                            }

                            if (isDirectionValue(key)) {
                                continue;
                            }

                            if (value instanceof AggregateProperty) {
                                continue;
                            }

                            List<Object> values = value.getValues();

                            List<String> valuesWithCounts = getValuesWithCounts(values, value.isHideEmptyInTooltip());

                            if (key.contains(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                                prop.put(key, valuesWithCounts);
                            } else if (!valuesWithCounts.isEmpty() || !value.isHideEmptyInTooltip()) {
                                toolTipProps.put(key, valuesWithCounts);
                            }
                        }
                        prop.put("toolTipProps", toolTipProps);
                    }

                } else {
                    prop.put(item.getColumnName(i), val);
                }
            }

        }
    }

    protected void populatePropsFromNodeStore(VisualItem item, CsiMap<String, Object> prop, boolean includeToolTips,
                                              NodeStore ns) {
        prop.put("anchored", ns.isAnchored());
        prop.put(PROPERTY_HIDE_LABELS, ns.isHideLabels());
        prop.put(PROPERTY_SIZE, item.getSize());
        prop.put(ACTION_BUNDLE, ns.isBundle());
        prop.put(ObjectAttributes.CSI_INTERNAL_HIDDEN, ns.isHidden());
        prop.put(PLUNKED, ns.isPlunked());
        Map<String,Integer> tooltipOrderMap = new HashMap<String,Integer>();
        prop.put(TOOLTIP_ORDER, tooltipOrderMap);

        populateTypeLabels(prop, ns);

        if (ns.isBundle()) {
            prop.put(BUNDLE_COUNT_INFO, BundleUtil.buildBundleCountInfo(ns));
        }

        List<String> labels = ns.getLabels();
        if ((labels != null) && !labels.isEmpty()) {
            prop.put(DISPLAY_LABEL, labels.get(0));
        }
        prop.put(ALL_LABELS, labels);

        if (includeToolTips) {

            Map<String, Property> attributes = ns.getAttributes();
            Set<Map.Entry<String, Property>> entrySet = attributes.entrySet();
            CsiMap<String, Object> toolTipProps = new CsiMap<String, Object>();
            for (Map.Entry<String, Property> entry : entrySet) {
                String key = entry.getKey();
                Property value = entry.getValue();
                tooltipOrderMap.put(key, value.getTooltipOrdinal());

                if (isMetricProperty(value) || key.equals(GraphMetrics.SUBGRAPH_PROP_NAME.getLocalPart())) {
                    continue;
                }

                // Use simple name here
                key = value.getName();
                if (!value.isIncludeInTooltip()) {
                    continue;
                }
                List<Object> values = value.getValues();

                List<String> valuesWithCounts = getValuesWithCounts(values, value.isHideEmptyInTooltip());

                if (key.contains(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                    values.remove(null);
                    prop.put(key, valuesWithCounts);
                } else {
                    if (value.isHideEmptyInTooltip()) {
                        while (values.indexOf(null) != -1) {
                            values.remove(null);
                        }

                        while (values.contains("")) {
                            values.remove("");
                        }
                    }

                    if (!values.isEmpty()) {
                        toolTipProps.put(key, valuesWithCounts);
                    }
                }
            }
            prop.put("toolTipProps", toolTipProps);

            if (ns.isBundle()) {
                populateBundleContents(toolTipProps, ns);
            }

            populateSNAMetrics(toolTipProps, ns);
        }
    }

    private boolean isDirectionValue(String name) {
        return name.contains("." + LinkDirection.FORWARD)
                || (name.contains("." + LinkDirection.REVERSE) || name.contains("." + LinkDirection.NONE));
    }

    private void populateSNAMetrics(CsiMap<String, Object> toolTipProps, NodeStore ns) {

        DecimalFormat formatter = new DecimalFormat("#0.########");

        CsiMap<String, Object> metrics = new CsiMap<String, Object>();
        Map<String, Property> attributes = ns.getAttributes();
        for (java.util.Map.Entry<String, Property> entry : attributes.entrySet()) {
            if (isMetricProperty(entry.getValue())) {
                Property property = entry.getValue();
                if (property.isIncludeInTooltip() && property.hasValues()) {
                    Number n = (Number) property.getValues().get(0);
                    if ((n != null) && !Double.isNaN(n.doubleValue())) {
                        String formattedValue = formatter.format(n.doubleValue());
                        metrics.put(property.getName(), formattedValue);
                    }
                }
            }
        }

        if (!metrics.isEmpty()) {
            toolTipProps.put("SNA Metrics", metrics);
        }

    }

    private boolean isMetricProperty(Property value) {
        boolean results = GraphMetrics.isMetricName(value.getName());
        return results;
    }

    private void populateBundleContents(CsiMap<String, Object> prop, AbstractGraphObjectStore linkStore) {

        if (!Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips()) {
            List<AbstractGraphObjectStore> children = linkStore.getChildren();
            List<Map<String, String>> labels = new ArrayList<Map<String, String>>(children.size());
            for (AbstractGraphObjectStore details : children) {
                Map<String, String> label = new TreeMap<String, String>();
                label.put("label", details.getLabel());
                for (String key : details.getAttributes().keySet()) {
                    if (!key.contains(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                        if (details.getAttributes().get(key) instanceof Property) {
                            Property property = details.getAttributes().get(key);
                            label.put(key, property.getValues().toString());
                        } else {
                            label.put(key, details.getAttributes().get(key).toString());
                        }
                    }
                }
                labels.add(label);
            }
            prop.put("Contents", labels);
        } else {
            prop.remove("Contents");
        }

    }

    private void populateTypeLabels(CsiMap<String, Object> prop, AbstractGraphObjectStore ns) {
        StringBuilder buffer;
        Map<String, Integer> types = ns.getTypes();
        if (types.isEmpty()) {
            // if no elements found in type, the unknown type should be added by
            // default.
            types.put(GraphConstants.UNSPECIFIED_NODE_TYPE, 1);
        }
        // sort the type labels
        List<String> typeLabels = new ArrayList<String>(types.keySet());
        Collections.sort(typeLabels);

        List<String> typesWithCounts = new ArrayList<String>(types.size());

        for (String label : typeLabels) {
            buffer = new StringBuilder();
            int count = types.get(label);
            buffer.append(label);
            if (count > 1) {
                buffer.append(" (").append(count).append(")");
            }
            typesWithCounts.add(buffer.toString());
        }

        if ((ns instanceof LinkStore) && (((LinkStore) ns).getFirstEndpoint().isBundle()
                || ((LinkStore) ns).getSecondEndpoint().isBundle())) {
            prop.put(VISUAL_ITEM_TYPE, GraphConstants.BUNDLED_LINKS);
            prop.put(MEMBER_TYPES, typesWithCounts);
        } else if (ns.isBundle()) {
            prop.put(VISUAL_ITEM_TYPE, GraphConstants.BUNDLED_NODES);
            prop.put(MEMBER_TYPES, typesWithCounts);
        } else {
            prop.put(VISUAL_ITEM_TYPE, typesWithCounts);
        }
    }

    private List<String> getValuesWithCounts(List<Object> values, boolean hideEmpty) {
        Multiset bag = LinkedHashMultiset.create();

        bag.addAll(values);

        Set<Multiset.Entry> entrySet = bag.entrySet();
        List<String> withCounts = new ArrayList<String>(entrySet.size());
        for (Entry entry : entrySet) {
            StringBuilder builder = new StringBuilder();
            Object element = entry.getElement();
            String value = element == null ? EMPTY_LABEL : element.toString();
            if ("".equals(value)) {
                value = EMPTY_LABEL;
            }

            if (!hideEmpty || (hideEmpty && !EMPTY_LABEL.equals(value))) {
                builder.append(value);
            }
            if ((entry.getCount() > 1) && !EMPTY_LABEL.equals(value)) {
                builder.append(" (");
                builder.append(entry.getCount());
                builder.append(")");
            }
            if (!hideEmpty || (builder.length() > 0)) {
                withCounts.add(builder.toString());
            }
        }
        return withCounts;
    }

    @Override
    @Operation
    @Interruptable
    /*
     * I don't know why we have two services that do the same thing, but I'm
     * worried removing one of them would break things.
     */
    public SelectionModel getSelectionModel(@QueryParam(value = "id") String id,
                                            @QueryParam(value = "vizuuid") String vizuuid) {
        SelectionModel selection = GraphHelper.getSelection(vizuuid, id);
        GraphHelper.removeHiddenFromSelection(selection);
        return selection;
    }

   @Override
   @Operation
   @Interruptable
   public void panTo(@QueryParam(value = "x") String x1, @QueryParam(value = "y") String y1,
                     @QueryParam(value = VIZUUID_PARAM) String vizUuid) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            int x = Integer.parseInt(x1);
            int y = Integer.parseInt(y1);

            graphContext.pan(x, y);
         }
      }
   }

   @Override
   public void panTo(int x, int y, String vizUuid) {
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         synchronized (graphContext) {
            graphContext.pan(x, y);
         }
      }
   }

   private Visualization getVisualization(String vizUuid) {
      Visualization visualization = null;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

      if (graphContext != null) {
         if (graphContext.isInvalidated()) {
            throw new TaskAbortedException("Graph has been invalidated.");
         }
         visualization = graphContext.getVisualization();
      }
      return visualization;
   }

    private void registerGraphContext(String vizUuid, GraphContext graphContext) {
        GraphServiceUtil.setGraphContext(graphContext);
    }

    private String getComponentIdentifierFromRequest(String vizUuid, String parameter, String x, String y) {

        if (parameter != null) {
            return parameter;
        }

        Visualization vis = getVisualization(vizUuid);

        Point2D.Double ref = new Point2D.Double();
        ref.x = Double.valueOf(x);
        ref.y = Double.valueOf(y);
        String componentId = GraphManager.getComponentAt(vis, ref);

        return componentId;
    }

    @Override
    @Operation
    public boolean hasBundleSelected(@QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam(value = "id") String id)
            throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext == null) {
            return false;
        }

        synchronized (graphContext) {
            if (id == null) {
                id = "default.selection";
            }
            SelectionModel selection = graphContext.getOrCreateSelection(id);
            Graph graph = graphContext.getVisibleGraph();

            if ((selection != null) && (selection.nodes != null)) {
                for (int nodeid : selection.nodes) {
                    Node node = graph.getNode(nodeid);

                    if (node != null) {
                        NodeStore details = GraphManager.getNodeDetails(node);

                        if ((details != null) && details.isBundle()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Operation
    public Map<String, List<Map<String, String>>> selectionInfo(@QueryParam(VIZUUID_PARAM) String vizUuid)
            throws CentrifugeException {
        GraphContext gc = GraphServiceUtil.getGraphContext(vizUuid);
        if (gc == null) {
            throw new CentrifugeException("Graph context not found. uuid=" + vizUuid);
        }

        synchronized (gc) {
            SelectionModel selection = gc.getOrCreateSelection("default.selection");

            Collection<Integer> selectedNodes = selection.nodes;
            Map<String, List<Map<String, String>>> selectedProperties = new HashMap<String, List<Map<String, String>>>();

            if (!selectedNodes.isEmpty()) {
                List<Map<String, String>> nodes = new ArrayList<Map<String, String>>();
                for (Integer nodeInt : selectedNodes) {
                    nodes.add(getItemKey(gc, nodeInt, "node"));
                }
                selectedProperties.put("nodes", nodes);
            }

            Collection<Integer> selectedLinks = selection.links;
            if (!selectedLinks.isEmpty()) {
                List<Map<String, String>> links = new ArrayList<Map<String, String>>();
                for (Integer linkInt : selectedLinks) {
                    links.add(getItemKey(gc, linkInt, "link"));
                }
                selectedProperties.put("links", links);
            }

            return selectedProperties;
        }
    }

    private Map<String, String> getItemKey(GraphContext graphContext, int itemId, String itemType)
            throws CentrifugeException {
        VisualItem item = getVisualItem(graphContext, itemId, itemType);
        CsiMap<String, String> itemInfo = new CsiMap<String, String>();
        populateItemKey(item, itemInfo);
        return itemInfo;
    }

    @Override
    public NodePositionDTO gwtGetNodePosition(String vizUuid, Integer nodeid) throws CentrifugeException {
        Map<String, Object> nodepos = getNodePosition(vizUuid, nodeid);
        NodePositionDTO dto = new NodePositionDTO();
        dto.x = nodepos.containsKey("x") ? Integer.toString((Integer) nodepos.remove("x")) : null;
        dto.y = nodepos.containsKey("y") ? Integer.toString((Integer) nodepos.remove("y")) : null;
        dto.displayX = nodepos.containsKey("displayX") ? Integer.toString((Integer) nodepos.remove("displayX")) : null;
        dto.displayY = nodepos.containsKey("displayY") ? Integer.toString((Integer) nodepos.remove("displayY")) : null;

        if (!nodepos.isEmpty()) {
            dumpMap(nodepos, "NodePositionDTO");
        }
        return dto;
    }

    @Operation
    public Map<String, Object> getNodePosition(@QueryParam(VIZUUID_PARAM) String vizUuid,
                                               @QueryParam("nodeid") Integer nodeid) throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext == null) {
            throw new CentrifugeException("Graph context not found. uuid=" + vizUuid);
        }

        synchronized (graphContext) {
            VisualItem item = getVisualItem(graphContext, nodeid, "node");
            Point2D itemPoint = new Point2D.Double(item.getX(), item.getY());
            Point2D displayPoint = toDisplayPoint(item.getVisualization().getDisplay(0), itemPoint);

            Map<String, Object> prop = new CsiMap<String, Object>();
            prop.put("x", item.getX());
            prop.put("y", item.getY());
            prop.put("displayX", displayPoint.getX());
            prop.put("displayY", displayPoint.getY());
            return prop;
        }
    }

    @Override
    @Operation
    public void setNodePosition(@QueryParam(value = VIZUUID_PARAM) String vizUuid, @QueryParam("nodeid") Integer nodeid,
                                @QueryParam("x") String x, @QueryParam("y") String y, @QueryParam("abs") Boolean abs)
            throws CentrifugeException {
        if (nodeid == null) {
            throw new CentrifugeException("Missing required parameter 'nodeid'");
        }

        if (x == null) {
            throw new CentrifugeException("Missing required parameter 'x'");
        }

        if (y == null) {
            throw new CentrifugeException("Missing required parameter 'y'");
        }

        if (abs == null) {
            abs = Boolean.FALSE;
        }

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext == null) {
            throw new CentrifugeException("Graph context not found. uuid=" + vizUuid);
        }

        synchronized (graphContext) {
            VisualItem item = getVisualItem(graphContext, nodeid, "node");
            if (item == null) {
                throw new CentrifugeException("Node not found: " + nodeid);
            }

            if (abs.booleanValue()) {
                Point2D absEnd = new Point(Integer.parseInt(x), Integer.parseInt(y));
                PrefuseLib.setX(item, item, absEnd.getX());
                PrefuseLib.setY(item, item, absEnd.getY());
            } else {
                Visualization vis1 = getVisualization(vizUuid);
                Visualization vis = vis1;

                if (vis != null) {
                   synchronized (vis) {
                      Display display = vis.getDisplay(0);

                      synchronized (display) {
                         Point2D pos =
                            display.getAbsoluteCoordinate(new Point(Integer.parseInt(x), Integer.parseInt(y)), null);

                         PrefuseLib.setX(item, item, pos.getX());
                         PrefuseLib.setY(item, item, pos.getY());
                      }
                   }
                }
            }
            graphContext.updateDisplay();
        }
    }

    @Override
    public ItemInfoDTO gwtItemInfo(String vizUuid, Integer itemid, String itemType, Boolean includeTooltips)
            throws CentrifugeException {
        return convertItemInfoToDTO(itemInfo(vizUuid, itemid, itemType, includeTooltips));
    }

    private ItemInfoDTO convertItemInfoToDTO(Map<String, Object> info) {
        ItemInfoDTO dto = new ItemInfoDTO();
        dto.itemid = info.containsKey("itemId") ? ((Integer) info.remove("itemId")).intValue() : null;
        dto.itemKey = info.containsKey("itemKey") ? (String) info.remove("itemKey") : null;
        dto.itemType = info.containsKey("itemType") ? (String) info.remove("itemType") : null;
        dto.objectType = info.containsKey("csi.object.type") ? (String) info.remove("csi.object.type") : null;
        dto.displayLabel = info.containsKey(DISPLAY_LABEL) ? (String) info.remove(DISPLAY_LABEL) : null;
        dto.direction = info.containsKey(DIRECTION) ? (CsiMap<String, String>) info.remove(DIRECTION) : null;
        dto.computed = info.containsKey(COMPUTED) ? (CsiMap<String, CsiMap<String, String>>) info.remove(COMPUTED)
                : null;
        dto.size = info.containsKey(PROPERTY_SIZE) ? (Double) info.remove(PROPERTY_SIZE) : null;
        dto.anchored = info.containsKey("anchored") ? (Boolean) info.remove("anchored") : null;
        dto.hideLabels = info.containsKey(PROPERTY_HIDE_LABELS) ? (Boolean) info.remove(PROPERTY_HIDE_LABELS) : null;
        dto.bundled = info.containsKey(ACTION_BUNDLE) ? (Boolean) info.remove(ACTION_BUNDLE) : null;
        dto.hidden = info.containsKey(ObjectAttributes.CSI_INTERNAL_HIDDEN)
                ? (Boolean) info.remove(ObjectAttributes.CSI_INTERNAL_HIDDEN) : null;
        dto.bundleCount = info.containsKey(BUNDLE_COUNT_INFO) ? (String) info.remove(BUNDLE_COUNT_INFO) : null;
        Object obj = info.containsKey(VISUAL_ITEM_TYPE) ? info.remove(VISUAL_ITEM_TYPE) : null;
        dto.visualItemType = null;
        if ((obj != null) && (obj instanceof String)) {
            dto.visualItemType = new VisualItemString((String) obj);
        }
        if ((obj != null) && (obj instanceof List)) {
            dto.visualItemType = new VisualItemArray((List<String>) obj);
        }
        dto.memberTypes = info.containsKey(MEMBER_TYPES) ? (List<String>) info.remove(MEMBER_TYPES) : null;

        // Convert tooltip datastructure to DTO
        CsiMap<String, Object> wrkTooltip = info.containsKey(TOOLTIPS_TYPES)
                ? (CsiMap<String, Object>) info.remove(TOOLTIPS_TYPES) : null;
        if (wrkTooltip != null) {
            dto.toolTipProps = createTooltipPropsDTO(wrkTooltip);
        }
        // extract all csi.internl.* entries
        dto.internalKeys = new CsiMap<String, List<String>>();
        Set<String> internalKeys = info.keySet();
        for (String internalKey : internalKeys) {
            dto.internalKeys.put(internalKey, (List<String>) info.remove(internalKey));
        }

        // the remaining entries should be column name entries
        dto.columnName = convertColumnNameEntries(info);

        if (!info.isEmpty()) {
            dumpMap(info, "ItemInfoDTO");
        }

        return dto;
    }

    private TooltipPropsDTO createTooltipPropsDTO(CsiMap<String, Object> wrkTooltip) {
        TooltipPropsDTO toolTipProps = new TooltipPropsDTO();
        if (wrkTooltip == null) {
            //System.out.println("tooltips are null 1");
            return toolTipProps;
        }
        toolTipProps.bundleContents = wrkTooltip.containsKey("Contents")
                ? (List<Map<String, String>>) wrkTooltip.remove("Contents") : null;
        toolTipProps.snaMetrics = wrkTooltip.containsKey("SNA Metrics")
                ? (CsiMap<String, String>) wrkTooltip.remove("SNA Metrics") : null;
        toolTipProps.displayLabel = wrkTooltip.containsKey("Label") ? (String) wrkTooltip.remove("Label") : null;
        toolTipProps.visualItemType = null;
        Object obj = wrkTooltip.containsKey(VISUAL_ITEM_TYPE) ? wrkTooltip.remove(VISUAL_ITEM_TYPE) : null;
        if (obj instanceof String) {
            toolTipProps.visualItemType = new VisualItemString((String) obj);
        }
        if (obj instanceof List) {
            toolTipProps.visualItemType = new VisualItemArray((List<String>) obj);
        }
        toolTipProps.memberTypes = wrkTooltip.containsKey(MEMBER_TYPES) ? (List<String>) wrkTooltip.remove(MEMBER_TYPES)
                : null;

        // there should only be attribute key/value pairs left in the tooltip
        // map
        toolTipProps.setAttributeNames(new CsiMap<String, List<String>>());

        for (Map.Entry<String, Object> entry : wrkTooltip.entrySet()) {
           Object value = entry.getValue();

           if (value instanceof List<?>) {
              toolTipProps.getAttributeNames().put(entry.getKey(), (List<String>) value);
           }
        }
        return toolTipProps;
    }

   private void dumpMap(Map<String,Object> map, String dtoname) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("DTO conversion to " + dtoname + " from a CsiMap has " + map.size() + " unhandled map entries:");

         for (String unhandledKey : map.keySet()) {
            LOG.debug("   unhandled key=" + unhandledKey);
         }
      }
   }

   private CsiMap<String,AbstractItemTypeBase> convertColumnNameEntries(Map<String,Object> map) {
      CsiMap<String,AbstractItemTypeBase> cols = new CsiMap<String,AbstractItemTypeBase>();
      AbstractItemTypeBase value = null;
//TODO: always returns empty CsiMap()
      for (Object val : map.values()) {
         if (val instanceof Date) {
            value = new ItemTypeDate((Date) val);
         } else {
            if (val instanceof Double) {
               value = new ItemTypeDouble((Double) val);
            } else {
               if (val instanceof Integer) {
                  value = new ItemTypeInteger((Integer) val);
               } else {
                  if (val instanceof String) {
                     value = new ItemTypeString((String) val);
                  } else {
                     if (val instanceof Long) {
                        value = new ItemTypeLong((Long) val);
                     } else {
                        if (val instanceof Boolean) {
                           value = new ItemTypeBoolean((Boolean) val);
                        } else {
                           LOG.error("unhandled Item Type class:{}", () -> val.getClass().getName());
                        }
                     }
                  }
               }
            }
         }
      }
      return cols;
   }

    @Operation
    public Map<String, Object> itemInfo(@QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam("itemid") Integer itemid,
                                        @QueryParam("type") String itemType, @QueryParam("tips") Boolean includeTooltips)
            throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext == null) {
            throw new CentrifugeException("Graph context not found. uuid=" + vizUuid);
        }

        if (itemid == null) {
            throw new CentrifugeException("Missing required parameter 'itemid'");
        }

        if ((itemType == null) || itemType.isEmpty()) {
            throw new CentrifugeException("Missing required parameter 'itemType'");
        }

        Map<String, Object> itemProps = getItemInfo(graphContext, itemid, itemType,
                ((includeTooltips != null) && includeTooltips));

        return itemProps;
    }

    private Map<String, Object> getItemInfo(GraphContext graphContext, VisualItem item, boolean includeTooltips,
                                            String x1, String y1, boolean aggregates) throws CentrifugeException {
        CsiMap<String, Object> infoMap = findItem("" + item.getRow(), x1, y1, graphContext.getVizUuid());

        buildInfoMap(graphContext, item, includeTooltips, aggregates, infoMap);
        return infoMap;
    }

    private void buildInfoMap(GraphContext graphContext, VisualItem item, boolean includeTooltips, boolean aggregates, CsiMap<String, Object> infoMap) throws CentrifugeException {
        boolean more = false;
        if (includeTooltips) {
            Map<String, Object> oldtips = (Map<String, Object>) infoMap.remove("toolTipProps");
            if (oldtips != null) {

                if (Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips()) {
                    oldtips.clear();
                }

                Map<String, Object> newtips;
                // putting this here for easy reversion of dynamic tooltips
                boolean isEdge = infoMap.get("csi.object.type").equals("graph.edges");

                if (Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips() && aggregates) {

                    /*newtips = */createTooltip(graphContext, infoMap, item, oldtips, isEdge);
//TODO: newtips is not used
                } else {

                    if (Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips() && !aggregates) {

                        more = determineIfMore(isEdge, infoMap, item, graphContext);

                    }
                    newtips = new CsiMap<String, Object>();
                    newtips.put("Label", infoMap.get(DISPLAY_LABEL));
                    newtips.put("Type", infoMap.get(VISUAL_ITEM_TYPE));
                    if (infoMap.get(MEMBER_TYPES) != null) {
                        newtips.put("Member Types", infoMap.get(MEMBER_TYPES));
                    }
                    newtips.putAll(oldtips);
                    String key;

                    // Check if link or node, then get key from appropriate
                    // store.
                    if (isEdge) {
                        LinkStore linkStore = GraphManager.getEdgeDetails(item);
                        key = linkStore.getKey();
                    } else {
                        NodeStore nodeStore = GraphManager.getNodeDetails(item);
                        if (Configuration.getInstance().getGraphAdvConfig().dynamicallyCreatedTooltips()
                                && nodeStore.isBundle()) {

                            CsiMap<String, Object> toolTipProps = new CsiMap<String, Object>();
                            populateBundleContents(toolTipProps, nodeStore);
                            newtips.putAll(toolTipProps);

                        }
                        key = nodeStore.getKey();
                    }

                    RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class,
                            graphContext.getVizUuid());

                    List<Annotation> annotations = relGraphViewDef.getAnnotations();

                    List<String> itemAnnotations = new ArrayList<String>();

                    for (Annotation annotation : annotations) {
                        if (annotation.getParentKey().equals(key)) {
                            itemAnnotations.add(annotation.getHtmlString());
                        }
                    }

                    if (!itemAnnotations.isEmpty()) {
                        newtips.put(COMMENTS, itemAnnotations);

                        Map<String, Integer> tooltipOrderMap = (Map<String, Integer>) infoMap.get(TOOLTIP_ORDER);
                        if (tooltipOrderMap != null) {
                            tooltipOrderMap.put(COMMENTS, Annotation.tooltipOrder);
                        }
                    }

                    infoMap.put(CSI_MORE_DETAILS, more);
                    infoMap.put(TOOLTIPS_TYPES, newtips);
                }
            }
        }
    }

    private boolean determineIfMore(boolean isEdge, CsiMap<String, Object> infoMap, VisualItem item,
                                    GraphContext graphContext) {
        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class,
                graphContext.getVizUuid());

        if (isEdge) {
//            LinkStore linkStore = GraphManager.getEdgeDetails(item);

            for (LinkDef linkDef : relGraphViewDef.getLinkDefs()) {
                for (AttributeDef attributeDef : linkDef.getAttributeDefs()) {
                    if (attributeDef.isIncludeInTooltip()) {
                        if (attributeDef.getName() != null) {
                            if (!GraphManager.skip(attributeDef.getName())
                                    && !attributeDef.getName().startsWith("csi.")) {
                                return true;
                            }
                        } else {
                            // return true;
                        }
                    }
                }
            }
        } else {
            NodeStore nodeStore = GraphManager.getNodeDetails(item);
            for (NodeDef nodeDef : relGraphViewDef.getNodeDefs()) {
                if (nodeStore.incorporatesNodeDef(nodeDef.getName())) {
                    for (AttributeDef attributeDef : nodeDef.getAttributeDefs()) {
                        if (attributeDef.isIncludeInTooltip()) {
                            if (attributeDef.getName() != null) {
                                if (!GraphManager.skip(attributeDef.getName())
                                        && !attributeDef.getName().startsWith("csi.")) {
                                    return true;
                                }
                            } else {
                                // return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private Map<String, Object> getItemInfo(GraphContext graphContext, Integer itemid, String itemType,
                                            boolean includeTooltips) throws CentrifugeException {

        synchronized (graphContext) {
            VisualItem item = getVisualItem(graphContext, itemid, itemType);
            return getItemInfo(graphContext, item, includeTooltips);
        }
    }

    private VisualItem getVisualItem(GraphContext graphContext, int itemid, String itemType)
            throws CentrifugeException {
        VisualGraph graph = graphContext.getVisualGraph();

        VisualItem item = null;
        if (itemType.equalsIgnoreCase("node")) {
            item = (VisualItem) graph.getNode(itemid);
        } else if (itemType.equalsIgnoreCase("link")) {
            item = (VisualItem) graph.getEdge(itemid);
        } else {
            throw new CentrifugeException("Unsupported graph item type: " + itemType);
        }
        return item;
    }

    private Map<String, Object> getItemInfo(GraphContext graphContext, VisualItem item, boolean includeTooltips) {
        CsiMap<String, Object> infoMap = new CsiMap<String, Object>();

        populatePropertyDetails(item, infoMap, includeTooltips);
        if (includeTooltips) {
            Map<String, Object> oldtips = (Map<String, Object>) infoMap.remove("toolTipProps");
            if (oldtips != null) {
                Map<String, Object> newtips = new CsiMap<String, Object>();
                newtips.put("Label", infoMap.get(DISPLAY_LABEL));
                newtips.put("Type", infoMap.get(VISUAL_ITEM_TYPE));
                if (infoMap.get(MEMBER_TYPES) != null) {
                    newtips.put("Member Types", infoMap.get(MEMBER_TYPES));
                }
                newtips.putAll(oldtips);

                infoMap.put(TOOLTIPS_TYPES, newtips);
            }
        }
        return infoMap;
    }

    @Override
    @Operation
    public void bundleSelectionBySpec(@QueryParam(VIZUUID_PARAM) String vizUuid,
                                      @QueryParam(DVUUID_PARAM) String dvUuid) throws CentrifugeException {

        GraphOperation operation = new GraphOperation();
        CsiMap<String, String> props = new CsiMap<String, String>();
        props.put("entireGraph", "false");
        props.put("currentLayout", "");
        props.put("includeBundleAnimation", "false");

        operation.operation = ACTION_BUNDLE_BY_SPEC;
        operation.type = OPERATION_GRAPH;
        operation.parameters = props;
        operateOn(vizUuid, dvUuid, operation);
    }

    @Override
    @Operation
    public void bundleEntireGraphBySpec(@QueryParam(VIZUUID_PARAM) String vizUuid,
                                        @QueryParam(DVUUID_PARAM) String dvUuid) throws CentrifugeException {
        GraphOperation operation = new GraphOperation();
        CsiMap<String, String> props = new CsiMap<String, String>();
        props.put("entireGraph", "true");
        props.put("currentLayout", "");
        props.put("includeBundleAnimation", "false");

        operation.operation = ACTION_BUNDLE_BY_SPEC;
        operation.type = OPERATION_GRAPH;
        operation.parameters = props;
        operateOn(vizUuid, dvUuid, operation);
    }

    @Override
    @Operation
    public void unbundleEntireGraph(@QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam(DVUUID_PARAM) String dvUuid)
            throws CentrifugeException {
        GraphOperation operation = new GraphOperation();
        CsiMap<String, String> props = new CsiMap<String, String>();
        props.put("entireGraph", "true");
        props.put("currentLayout", "");
        props.put("includeBundleAnimation", "false");

        operation.operation = ACTION_UNBUNDLE;
        operation.type = OPERATION_GRAPH;
        operation.parameters = props;
        // Twice on purpose. Nested bundles are handled by the second operation.
        operateOn(vizUuid, dvUuid, operation);
        operateOn(vizUuid, dvUuid, operation);
    }

    @Override
    @Operation
    public void unbundleSelection(@QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam(DVUUID_PARAM) String dvUuid)
            throws CentrifugeException {

        GraphOperation operation = new GraphOperation();
        CsiMap<String, String> props = new CsiMap<String, String>();
        props.put("entireGraph", "false");
        props.put("currentLayout", "");
        props.put("includeBundleAnimation", "false");

        operation.operation = ACTION_UNBUNDLE;
        operation.type = OPERATION_GRAPH;
        operation.parameters = props;
        operateOn(vizUuid, dvUuid, operation);
    }

    @Override
    @Operation
    public CsiMap<String, String> operateOn(@QueryParam(VIZUUID_PARAM) String vizUuid,
                                            @QueryParam(DVUUID_PARAM) String dvUuid, @PayloadParam GraphOperation operation)
            throws CentrifugeException {

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

        if (graphContext == null) {
            return new CsiMap<String, String>();
        }

        synchronized (graphContext) {
            Visualization vis = graphContext.getVisualization();
            Graph graph = graphContext.getVisibleGraph();
            // VisualGraph vGraph = (VisualGraph)
            // graph.getClientProperty(GraphConstants.ROOT_GRAPH);
            VisualGraph vGraph = graphContext.getVisualGraph();
            SelectionModel model = null;
            CsiMap<String, String> results = new CsiMap<String, String>();
            if (OPERATION_NODE_SIMPLE.equals(operation.type)
                    || ObjectAttributes.NODES_OBJECT_TYPE.equals(operation.type)) {
                model = new SelectionModel();
                model.nodes.add(Integer.parseInt(operation.id));

            } else if (OPERATION_EDGE_SIMPLE.equals(operation.type) || OPERATION_EDGE_COMPLEX.equals(operation.type)
                    || OPERATION_EDGES_SIMPLE.equals(operation.type)) {
                model = new SelectionModel();
                model.links.add(Integer.parseInt(operation.id));

            } else if (OPERATION_SELECTION_MODEL.equals(operation.type)) {
                model = GraphManager.getSelection(vis, operation.id);
            } else if (OPERATION_GRAPH.equals(operation.type)) {
                // We no not need selection to operate on.
            } else {
                throw new CentrifugeException("Cannot perform operations on unknown graph types");
            }
            SelectionModel currentSelection = graphContext.getSelection(GraphManager.DEFAULT_SELECTION);
            if (operation.operation.equals(ACTION_SET_PROPERTY)) {
                CsiMap<String, String> params = operation.parameters;
                String propName = params.get(NAME_PARAMETER);
                Object value = params.get(VALUE_PARAMETER);

                boolean flag = false;
                if (propName.equals(ObjectAttributes.CSI_INTERNAL_HIDDEN) || propName.equals(PROPERTY_ANCHOR)
                        || propName.equals(PROPERTY_SELECTED)) {
                    flag = Boolean.parseBoolean(value.toString());
                }

                for (int id : model.nodes) {
                    NodeItem node = (NodeItem) vGraph.getNode(id);
                    NodeStore details = GraphManager.getNodeDetails(node);
                    if (propName.equals(ObjectAttributes.CSI_INTERNAL_HIDDEN)) {
                        if (flag) {
                            graphContext.hideNode(id);
                        } else {
                            graphContext.showNode(id);
                        }

                        // TODO: optimize discovery of whether the subnetwork is
                        // really
                        // dirty....
                        graphContext.setSubnetsDirty(true);
                    } else if (propName.equals(PROPERTY_ANCHOR)) {
                        details.setAnchored(flag);
                        node.setFixed(flag);
                    } else if (propName.equals(PROPERTY_HIDE_LABELS)) {
                        details.setHideLabels(flag);
                    } else if (propName.equals(LABEL)) {
                        ArrayList<String> label = new ArrayList<String>();
                        label.add(params.get("value"));
                        details.setLabels(label);
                    } else if (propName.equals(PROPERTY_SELECTED)) {
                        if (flag) {
                            currentSelection.nodes.add(node.getRow());
                        } else {
                            currentSelection.nodes.remove(node.getRow());
                        }
                    } else if (propName.equals(PROPERTY_SIZE)) {
                        double sizeVal = 1.0d;
                        if (value instanceof String) {
                            try {
                                sizeVal = Double.parseDouble((String) value);
                            } catch (NumberFormatException e) {
                            }
                        } else if (value instanceof Integer) {
                            sizeVal = ((Integer) value).doubleValue();
                        } else if (value instanceof Number) {
                            sizeVal = ((Number) value).doubleValue();
                        }
                        node.setSize(sizeVal);
                    } else if (propName.equalsIgnoreCase(ObjectAttributes.CSI_INTERNAL_COLOR)) {
                        if (value instanceof Number) {
                            details.setColor((Integer) value);
                        } else {
                            try {
                                Integer color = Integer.parseInt(value.toString());
                                details.setColor(color);
                            } catch (Throwable t) {

                            }
                        }
                    } else {
                        Map<String, Property> attributes = details.getAttributes();
                        Property property = attributes.get(propName);
                        if (property == null) {
                            property = new Property(propName);
                            attributes.put(propName, property);
                        }
                        List<Object> values = property.getValues();
                        values.clear();
                        values.add(value);
                    }
                }

                for (int id : model.links) {
                    Edge edge = vGraph.getEdge(id);
                    LinkStore details = GraphManager.getEdgeDetails(edge);
                    if (propName.equals(ObjectAttributes.CSI_INTERNAL_HIDDEN)) {
                        if (flag) {
                            graphContext.hideLink(id);
                        } else {
                            graphContext.showLink(id);
                        }
                        // details.setHidden( flag );
                        // ( (VisualItem)edge ).setVisible( !flag );

                        graphContext.setSubnetsDirty(true);
                    } else if (propName.equals(PROPERTY_SELECTED)) {
                        if (flag) {
                            currentSelection.links.add(edge.getRow());
                        } else {
                            currentSelection.links.remove(edge.getRow());
                        }
                    } else if (propName.equalsIgnoreCase(ObjectAttributes.CSI_INTERNAL_COLOR)) {
                        if (value instanceof Number) {
                            details.setColor((Integer) value);
                        } else {
                            try {
                                Integer color = Integer.parseInt(value.toString());
                                details.setColor(color);
                            } catch (Throwable t) {

                            }
                        }
                    } else if (propName.equals(PROPERTY_SIZE)) {
                        double sizeVal = 1.0d;
                        if (value instanceof String) {
                            try {
                                sizeVal = Double.parseDouble((String) value);
                            } catch (NumberFormatException e) {
                            }
                        } else if (value instanceof Integer) {
                            sizeVal = ((Integer) value).doubleValue();
                        } else if (value instanceof Number) {
                            sizeVal = ((Number) value).doubleValue();
                        }
                        // double computedSize = 2 * sizeVal - 1;
                        // ( (VisualItem)edge ).setSize( computedSize );
                        details.setScale((int) sizeVal);
                    } else {
                        Map<String, Property> attributes = details.getAttributes();
                        Property property = attributes.get(propName);
                        if (property == null) {
                            property = new Property(propName);
                            attributes.put(propName, property);
                        }

                        List<Object> values = property.getValues();
                        values.clear();
                        values.add(value);

                        if (ObjectAttributes.CSI_INTERNAL_COLOR.equalsIgnoreCase(propName)) {
                            int parsedColor;
                            if (!(value instanceof Number)) {
                                parsedColor = Integer.parseInt(value.toString());
                            } else {
                                parsedColor = ((Number) value).intValue();
                            }
                            details.setColor(parsedColor);
                        }

                    }

                }

            } else if (operation.operation.equals(ACTION_BUNDLE)) {
                String bundleName = operation.parameters.get(NAME_PARAMETER);
                Boolean includeAnimation = null;
                Object obj = operation.parameters.get(INCLUDE_BUNDLE_ANIMATION);
                if ((obj != null) && (obj instanceof Boolean)) {
                    includeAnimation = (Boolean) obj;
                }
                if ((obj != null) && (obj instanceof String)) {
                    includeAnimation = new Boolean((String) obj);
                }
                String animationLayout = operation.parameters.get(CURRENT_LAYOUT);
                performManualBundle(graphContext, results, bundleName, includeAnimation, animationLayout);

            } else if (ACTION_BUNDLE_BY_SPEC.equals(operation.operation)) {

                String value = operation.parameters.get("entireGraph");
                boolean entireGraph = ((value != null) && value.equals("true"));
                if (!entireGraph && ((currentSelection == null) || (currentSelection.nodes == null)
                        || currentSelection.nodes.isEmpty())) {
                    throw new CentrifugeException("No nodes selected for bundle operation");
                }

                Boolean includeAnimation = new Boolean(operation.parameters.get(INCLUDE_BUNDLE_ANIMATION));
                String animationLayout = operation.parameters.get(CURRENT_LAYOUT);

                DataView dataview = CsiPersistenceManager.findObject(DataView.class, dvUuid);
                DataModelDef modelDef = dataview.getMeta().getModelDef();
                RelGraphViewDef graphDef = (RelGraphViewDef) modelDef.findVisualizationByUuid(vizUuid);
                BundleDef bundleSpec = findBundleType(graphDef);

                if (bundleSpec == null) {
                    throw new CentrifugeException("No bundling definition found");
                }

                performBundleBySpec(graphContext, GraphManager.DEFAULT_SELECTION, results, includeAnimation,
                        entireGraph, animationLayout);

            } else if (ACTION_UNBUNDLE.equalsIgnoreCase(operation.operation)) {
                String value = operation.parameters.get("entireGraph");
                boolean entireGraph = ((value != null) && value.equals("true"));
                Predicate<Node> isBundle = new Predicate<Node>() {
                    @Override
                    public boolean test(Node node) {
                        if (node == null) {
                            return false;
                        }
                        NodeStore details = GraphManager.getNodeDetails(node);

                        return ((details != null) && details.isBundle());
                    }
                };
                TaskHelper.reportProgress(40);

                Collection<Node> targetNodes = new ArrayList<Node>();

                if (entireGraph) {
                   for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
                      Node node = nodes.next();

                      if (isBundle.test(node)) {
                         targetNodes.add(node);
                      }
                   }
                } else {
                   final Graph ref = graph;
                   final Table nodeTable = ref.getNodeTable();

                   for (Iterator<Integer> ids = currentSelection.nodes.iterator(); ids.hasNext();) {
                      Integer id = ids.next();
                      int intId = id.intValue();

                      if (nodeTable.isValidRow(intId)) {
                         Node node = ref.getNode(intId);

                         if (node != null) {
                            targetNodes.add(node);
                         }
                      }
                   }
                }
                try {
                    UnGroupNodesCommand command = new UnGroupNodesCommand();
                    command.setGraph(graph);
                    command.setNodes(targetNodes);
                    command.call();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                graphContext.setSubnetsDirty(true);

                TaskHelper.reportProgress(75);
                // TODO: Optimize checking for decomposed components e.g.
                // removing
                // a bundle results in splitting an existing component.
                // Ideally this involves choosing a neighbor of one of the
                // nodes no longer bundled

                // FIXME: add smarts for handling selection updates
                SelectionModel selection = graphContext.getSelection(GraphManager.DEFAULT_SELECTION);
                if (selection != null) {
                    selection.reset();
                }

                graphContext.updateVisibleNodeLegend();
                graphContext.updateVisibleLinkLegend();
            }

            if (graphContext.isPlayerRunning()) {
                graphContext.updateDisplay();
            }
            return results;
        }
    }

    @Override
    @Operation
    public void unbundleSingleNode(@QueryParam(VIZUUID_PARAM) String vizId, @QueryParam("id") Integer id)
            throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizId);
        synchronized (graphContext) {
            Graph graph = graphContext.getGraphData();
            Node node = graph.getNode(id);
            if (node == null) {
                throw new CentrifugeException("Invalid node identifier");
            }

            VisualItem item = getVisualItem(graphContext, id, "node");
            NodeStore nodeStore = GraphManager.getNodeDetails(item);
            String key = nodeStore.getKey();
            removeAnnotation(vizId, key);

            Collection<Node> targetNodes = new ArrayList<Node>(Arrays.asList(node));
            try {
                UnGroupNodesCommand command = new UnGroupNodesCommand();
                command.setGraph(graph);
                command.setNodes(targetNodes);
                command.call();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // update our selection model to account for nodes that no longer
            // exist.
            graphContext.validateSelection(GraphManager.DEFAULT_SELECTION);
            graphContext.setSubnetsDirty(true);
//            graph = graphContext.getVisibleGraph();
            TaskHelper.reportProgress(75);
            // TODO: Optimize checking for decomposed components e.g. removing
            // a bundle results in splitting an existing component.
            // Ideally this involves choosing a neighbor of one of the
            // nodes no longer bundled
            GraphManager graphManager = GraphManager.getInstance();
            graphManager.computeAndLayoutComponents(graphContext);
            graphContext.updateVisibleNodeLegend();
            graphContext.updateVisibleLinkLegend();

        }
    }

    @Override
    public void unbundleNodesById(String vizId, ArrayList<Integer> nodeIds) throws CentrifugeException {
        for (Integer id : nodeIds) {
            unbundleSingleNode(vizId, id);
        }
    }

    private void performBundleBySpec(GraphContext graphContext, String selectionName, CsiMap results,
                                     boolean withAnimation, boolean entireGraph, String animationLayout) throws CentrifugeException {
        GraphHelper.performBundleBySpec(graphContext, selectionName, results, withAnimation, entireGraph,
                animationLayout, getDisplayableNodePredicate(), true);
    }

    private BundleDef findBundleType(RelGraphViewDef graphDef) {
        List<BundleDef> bundleDefs = graphDef.getBundleDefs();
        BundleDef bundleSpec = null;
        for (BundleDef bd : bundleDefs) {
            bundleSpec = bd;
            break;
        }
        return bundleSpec;
    }

    private void performManualBundle(GraphContext graphContext, CsiMap results, String bundleName,
                                     Boolean capturePositions, String animationLayout) throws CentrifugeException {
       // Capture positions before animation for delta.
        List<CsiMap> animationPositionList = capturePositions.booleanValue()
                                                ? GraphHelper.captureGraphBundleNodeLayout(graphContext)
                                                : new ArrayList<CsiMap>();

        TaskHelper.reportProgress("Bundling", 20);

        Graph graph = graphContext.getVisibleGraph();
        VisualGraph vGraph = graphContext.getVisualGraph();
        SelectionModel currentSelection = graphContext.getSelection(GraphManager.DEFAULT_SELECTION);
        boolean hideLabels = true;
        Function<Integer,Node> transform = GraphContext.Functions.mapIdToNodeFunction(graph);

        for (Iterator<Integer> ids = currentSelection.nodes.iterator(); ids.hasNext();) {
           Node node = transform.apply(ids.next());
           NodeStore nodeDetails = GraphManager.getNodeDetails(node);

            if (nodeDetails.isBundled()) {
                // TaskHelper.reportError(
                // "Bundling can only be performed against nodes that are not
                // already a member of a bundle",
                // new IllegalArgumentException());
                throw new CentrifugeException(
                        "Bundling can only be performed against nodes that are not already a member of a bundle");
            }
            hideLabels = hideLabels && nodeDetails.isHideLabels();
        }

        Node bundleNode = GraphManager.createBundle(graph, bundleName, currentSelection);

        currentSelection.reset();
        TaskHelper.reportProgress(50);

        List<Node> children = new ArrayList<Node>();
        NodeStore details = GraphManager.getNodeDetails(bundleNode);
        details.setHideLabels(hideLabels);
        for (AbstractGraphObjectStore child : details.getChildren()) {
            children.add(graphContext.getNodeFromDetails((NodeStore) child));
        }

        Collection<Point2D> points = new ArrayList<Point2D>(children.size());

        for (Node n : children) {
            NodeItem ni = (NodeItem) vGraph.getNode(n.getRow());
            points.add(new Point2D.Double(ni.getX(), ni.getY()));
            hideNodeAndEdges(vGraph, n);
        }

        Point2D centroid = GeometryUtil.centroidOf(points);
        NodeItem bundleNI = (NodeItem) vGraph.getNode(bundleNode.getRow());
        PrefuseLib.setX(bundleNI, null, centroid.getX());
        PrefuseLib.setY(bundleNI, null, centroid.getY());

        TaskHelper.reportProgress(60);

        graphContext.showNodes(new ArrayList<Node>(Arrays.asList(bundleNode)).iterator());

        if (bundleNode instanceof VisualItem) {
            populatePropertyDetails((VisualItem) bundleNode, results, true);
        }

        // Capture positions after bundling for delta.
        if (capturePositions.booleanValue()) {
            layoutAndMergeManualBundleNodes(animationPositionList, bundleNode, animationLayout,
                    graphContext.getVizUuid());
            results.put("animationLayoutList", animationPositionList);
        }

        BundleMetrics metrics = new BundleMetrics();
        NodeStore bundle = GraphManager.getNodeDetails(bundleNode);
        metrics = GraphManager.computeBundleIconSize(vGraph, bundle, metrics);
        bundle.setRelativeSize(metrics.computeSize());
        if (metrics.bySize) {
            bundle.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE);
        } else {
            if (metrics.byTransparency) {
                // bundle.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_TRANSPARENCY);
            }
        }
        VisualItem vi = (VisualItem) vGraph.getNode(bundleNode.getRow());
        vi.setSize(bundle.getRelativeSize());
        TaskHelper.reportProgress(75);
    }

    private void hideNodeAndEdges(VisualGraph vGraph, Node node) {
        GraphManager.hideNodeAndEdges(vGraph, node);
    }

    private void populateListsFromSelection(Graph graph, SelectionModel selection, List<Node> nodes, List<Edge> edges) {
        for (int id : selection.nodes) {
            Node node = graph.getNode(id);
            nodes.add(node);
        }

        for (int id : selection.links) {
            Edge edge = graph.getEdge(id);
            edges.add(edge);
        }
    }

    @Override
    @Operation
    public void zoomToRegion(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                             @QueryParam(value = DVUUID_PARAM) String dvUuid, @QueryParam(value = "x1") Double x1,
                             @QueryParam(value = "x2") Double x2, @QueryParam(value = "y1") Double y1,
                             @QueryParam(value = "y2") Double y2) throws CentrifugeException {

        Rectangle2D bounds = new Rectangle();
        bounds.setFrameFromDiagonal(x1, y1, x2, y2);

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        synchronized (graphContext) {
            graphContext.zoomToRegion(bounds);
        }
    }

    @Override
    @Operation
    public void componentAction(@QueryParam("action") String action, @QueryParam("value") String value,
                                @QueryParam(VIZUUID_PARAM) String vizId) throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizId);
        synchronized (graphContext) {
//            Graph visibleGraph = graphContext.getVisibleGraph();
            if ("sna".equals(action)) {
                if ("reset".equals(value)) {
                    resetVisibleProperties(graphContext);
                } else {
                    computeNetworkMetrics(graphContext, value);
                    applyVisibleProperties(graphContext);
                }
            }
        }
    }

    @Override
    @Operation
    public int showAdjacentFor(@QueryParam(VIZUUID_PARAM) String vizId, @QueryParam("filter") String type) {
        SelectionModel selection = getSelection(vizId, DEFAULT_SELECTION);
        TreeSet<Integer> selectedNodes = selection.nodes;
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizId);
        synchronized (graphContext) {
            Graph graph = graphContext.getGraphData();
            Collection<Node> filteredNeighbors = new HashSet<Node>();
            // TODO: isolation -- don't expose notion of an integer for the node
            // once we're persisting the graph.
            for (Integer nodeId : selectedNodes) {
                Node node = graph.getNode(nodeId);
                filteredNeighbors.addAll(collectFilteredNeighbors(graphContext, node, type));
            }
            graphContext.showItems(filteredNeighbors);

            return filteredNeighbors.size();
        }
    }

    private HashSet<Node> collectFilteredNeighbors(GraphContext graphContext, Node node, String type) {
        HashSet<Node> filteredNeighbors = new HashSet<Node>();
        Iterator<Edge> edges = node.edges();
        while (edges.hasNext()) {
            Edge edge = edges.next();
            Node opposite = edge.getAdjacentNode(node);
            NodeItem ni = (NodeItem) graphContext.getVisualGraph().getNode(opposite.getRow());
            NodeStore details = GraphManager.getNodeDetails(opposite);
            if (((type == null) || graphContext.hasType(opposite, type)) && !details.isBundled() && !ni.isVisible()) {
                filteredNeighbors.add(opposite);
                LinkStore linkDetails = GraphManager.getEdgeDetails(edge);
                linkDetails.setHidden(false);
            }
        }
        return filteredNeighbors;
    }

    @Override
    @Operation
    public int showAdjacentFor(@QueryParam(VIZUUID_PARAM) String vizId, @QueryParam("id") String id,
                               @QueryParam("filter") String type) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizId);
        synchronized (graphContext) {
            Graph graph = graphContext.getGraphData();
            // TODO: isolation -- don't expose notion of an integer for the node
            // once we're persisting the graph.
            int nodeId = Integer.parseInt(id);
            Node node = graph.getNode(nodeId);
            Collection<Node> filteredNeighbors = new HashSet<Node>();
            filteredNeighbors.addAll(collectFilteredNeighbors(graphContext, node, type));
            graphContext.showItems(filteredNeighbors);

            return filteredNeighbors.size();
        }
    }

    private void applyVisibleProperties(GraphContext graphContext) {
//        Graph canvasGraph = graphContext.getVisibleGraph();
        VisualGraph visualGraph = graphContext.getVisualGraph();
        Iterator nodes = visualGraph.nodes();
        int alpha = 32;
        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();
            VisualItem vi = (VisualItem) node;
            int fillColor = vi.getFillColor();
            fillColor = ColorLib.setAlpha(fillColor, alpha);
            vi.setFillColor(fillColor);
        }

        Iterator edges = visualGraph.edges();
        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            VisualItem vi = (VisualItem) edge;
            int fillColor = vi.getFillColor();
            fillColor = ColorLib.setAlpha(fillColor, alpha);
            vi.setFillColor(fillColor);
        }
    }

    private void resetVisibleProperties(GraphContext graphContext) {
//        Graph canvasGraph = graphContext.getVisibleGraph();
        VisualGraph visualGraph = graphContext.getVisualGraph();
        Iterator nodes = visualGraph.nodes();
        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();
            VisualItem vi = (VisualItem) node;
            int fillColor = vi.getFillColor();
            fillColor = ColorLib.setAlpha(fillColor, 255);
            vi.setFillColor(fillColor);
        }

        Iterator edges = visualGraph.edges();
        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            VisualItem vi = (VisualItem) edge;
            int fillColor = vi.getFillColor();
            fillColor = ColorLib.setAlpha(fillColor, 255);
            vi.setFillColor(fillColor);
        }
    }

    private void computeNetworkMetrics(GraphContext graphContext, String value) {
        Graph visibleGraph = graphContext.getVisibleGraph();
        if (!GraphMetrics.isMetricsComputed(visibleGraph)) {
            GraphMetrics.computeMetrics(visibleGraph);
        }

    }

   @Override
   @Operation
   public void computeSNA(@QueryParam(VIZUUID_PARAM) String vizId, @QueryParam(METRIC_NAME) String metric)
         throws CentrifugeException {
      TaskHelper.reportProgress("Computing Network Metrics", 5);

      GraphContext context = GraphServiceUtil.getGraphContext(vizId);

      if (context == null) {
         LOG.debug("Request to compute SNA for non-existent visualization; doing nothing.");
      } else {
         synchronized (context) {
            Graph visibleGraph = context.getGraphData();

            GraphMetrics.computeMetrics(visibleGraph);
            TaskHelper.reportProgress("Computing Network Metrics", 100);
         }
      }
   }

    @Operation
    public void getTile(@QueryParam("vduuid") String vzuuid, @QueryParam("tw") String tileWidthStr,
                        @QueryParam("th") String tileHeightStr, @QueryParam("z") String zoomLevelStr,
                        @QueryParam("x") String tileXStr, @QueryParam("y") String tileYStr,
                        @ServletResponseParam HttpServletResponse resp) throws IOException, NoninvertibleTransformException {
        BufferedImage img = getImage(vzuuid, tileWidthStr, tileHeightStr, zoomLevelStr, tileXStr, tileYStr);
        writeImageResponse(img, resp);

    }

   @Override
   public String getTile(String vzuuid, String tileWidthStr, String tileHeightStr, String zoomLevelStr,
                         String tileXStr, String tileYStr) throws IOException {
      String base64 = null;
      BufferedImage img;

      try {
         img = getImage(vzuuid, tileWidthStr, tileHeightStr, zoomLevelStr, tileXStr, tileYStr);
      } catch (NoninvertibleTransformException e) {
         LOG.error(e.getMessage(), e);
         throw new RuntimeException(e.getMessage());
      }
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
         ImageUtil.writePNG(img, bos);

         base64 = new StringBuilder("data:image/png;base64,")
                            .append(new String(Base64.getEncoder().encode(bos.toByteArray())))
                            .toString();
      }
      return base64;
   }

    private BufferedImage getImage(String vzuuid, String tileWidthStr, String tileHeightStr, String zoomLevelStr,
                                   String tileXStr, String tileYStr) throws IOException, java.awt.geom.NoninvertibleTransformException {
        // the size, in pixels, of our tiles, and the image to return
        int tileWidth = Integer.parseInt(tileWidthStr);
        int tileHeight = Integer.parseInt(tileHeightStr);

        // each successive zoom level is twice as large as the previous level
        // zoom level 5 = 2 ^ (5 - 5) = 2 ^ 0 = 1.0
        // zoom level 6 = 2 ^ (6 - 5) = 2 ^ 1 = 2.0
        double zoomLevel = Double.parseDouble(zoomLevelStr);
        double zoom = Math.pow(2, zoomLevel - 5);

        // these are columns within the zoom level, so 0,0 is the
        // top-left grid space and 1,0 is the next space to the right
        int x = Integer.parseInt(tileXStr);
        int y = Integer.parseInt(tileYStr);

        GraphContext gc = GraphServiceUtil.getGraphContext(vzuuid);
        Visualization viz = gc.getVisualization();
        Display d = viz.getDisplay(0);

        BufferedImage img = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_RGB);

        // ensure that other concurrent requests to getTile do not use
        // this particular Display at the same time
        synchronized (d) {
            d.setSize(tileWidth, tileHeight);

            // create a transform to pan and zoom to the desired tile
            AffineTransform t = new AffineTransform();

            t.translate((double) -x * tileWidth, (double) -y * tileHeight);

            // scaling by zero is non-invertible so don't do it,
            // Display will throw when you try to set the transform
            if (BigDecimal.valueOf(zoomLevel).compareTo(BigDecimal.ZERO) != 0) {
                t.scale(zoom, zoom);
            }
            d.setTransform(t);

            Graphics2D g = img.createGraphics();

            d.paintDisplay(g, new Dimension(tileWidth, tileHeight));
            g.dispose();
        }
        return img;
    }

    @Operation
    @Interruptable
    public void getDisplay(@QueryParam("vduuid") String vizuuid, @QueryParam("vw") String viewWidth,
                           @QueryParam("vh") String viewHeight, @ServletResponseParam HttpServletResponse servletResponse)
            throws IOException, CentrifugeException {
        BufferedImage img = getDisplayImage(vizuuid, viewWidth, viewHeight);
        if (img != null) {
//            long start = System.currentTimeMillis();
            writeImageResponse(img, servletResponse);

//            long end = System.currentTimeMillis();
        } else {
            LOG.error("No image rendered for graph.");
        }
    }

    @Override
    public GraphGetDisplayResponse getDisplay(String vizuuid, String viewWidth, String viewHeight)
            throws IOException, CentrifugeException {
//	    System.out.println("Display");
//	    System.out.println(viewHeight);
//	    System.out.println(viewWidth);
        String base64String = ImageUtil.toBase64String(getDisplayImage(vizuuid, viewWidth, viewHeight));
        boolean hiddenItemsOnGraph = false;
        {
            if (vizuuid == null) {
                throw new CentrifugeException("Missing required parameter: vduuid");
            }

            GraphContext context = GraphServiceUtil.getGraphContext(vizuuid);
            if (context != null) {

                synchronized (context) {
                    VisualGraph vGraph = context.getVisualGraph();

                    Predicate<Node> canRenderNode = GraphContext.Predicates.IsNodeVisualized;
                    IntIterator it = vGraph.nodeRows();
                    while (it.hasNext()) {
                        Integer nodeId = (Integer) it.next();
                        NodeItem nodeItem = (NodeItem) vGraph.getNode(nodeId);
                        if (canRenderNode.test(nodeItem)) {
                            {
                                NodeItem node = (NodeItem) vGraph.getNode(nodeId);
                                if (node != null) {

                                    NodeStore details = GraphContext.Functions.GetNodeDetails.apply(node);
                                    if(details.isHidden()&&!details.isBundled()){
                                        hiddenItemsOnGraph = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!hiddenItemsOnGraph) {
                        for (Iterator iter = vGraph.edges(); iter.hasNext(); ) {
                            Edge edge = (Edge) iter.next();
                            LinkStore edgeDetails = GraphManager.getEdgeDetails(edge);
                            NodeStore sourceDetails = GraphManager.getNodeDetails(edge.getSourceNode());
                            NodeStore targetDetails = GraphManager.getNodeDetails(edge.getTargetNode());

                            // If the edge is bundled or one of its endpoints is bundled,
                            // then the link shouldn't be shown.
                            if (!edgeDetails.isBundled() && sourceDetails.isDisplayable() && targetDetails.isDisplayable()) {
                                EdgeItem vedge = (EdgeItem) vGraph.getEdge(edge.getRow());
                                LinkStore details = GraphContext.Functions.GetLinkDetails.apply(edge);
                                if (details.isHidden() || !vedge.isVisible()) {
                                    hiddenItemsOnGraph = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return new GraphGetDisplayResponse(base64String,hiddenItemsOnGraph);
    }

    public BufferedImage getDisplayImage(String vizuuid, String viewWidth, String viewHeight)
            throws IOException, CentrifugeException {

        Dimension vdim = new Dimension(720, 500);
        if ((viewWidth != null) && (viewHeight != null)) {
            int w = Integer.parseInt(viewWidth);
            int h = Integer.parseInt(viewHeight);
            if ((w > 0) && (h > 0)) {
                vdim.width = w;
                vdim.height = h;
            }
        }

        try {
            GraphContext gc = GraphServiceUtil.getGraphContext(vizuuid);
            if (gc == null) {
                throw new CentrifugeException(GRAPH_DATA_HAS_NOT_BEEN_LOADED);
            }

            BufferedImage img = GraphManager.getInstance().renderGraph(gc, vdim);

            return img;
        } catch (Exception exception) {
            LOG.warn(GRAPH_DATA_HAS_NOT_BEEN_LOADED, exception);
            return null;
        }
    }

    @Operation
    @Interruptable
    public String getDragImage(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                               @ServletResponseParam HttpServletResponse response) throws IOException, CentrifugeException {
        BufferedImage img = dragImageHelper(vizUuid);
        if (img != null) {
            writeImageResponse(img, response);
            return null;
        } else {
            LOG.warn("No image rendered for graph.");
            return "";
        }
    }

    private BufferedImage dragImageHelper(String vizUuid) throws IOException, CentrifugeException {
        BufferedImage img = null;

        GraphContext gc = GraphServiceUtil.getGraphContext(vizUuid);
        if (gc == null) {
            throw new CentrifugeException(GRAPH_DATA_HAS_NOT_BEEN_LOADED);
        }

        synchronized (gc) {
            Visualization vis = gc.getVisualization();

            synchronized (vis) {
                Graph graph = (Graph) vis.getSourceData("graph");
                DragItems dragItems = (DragItems) graph.getClientProperty(GraphConstants.DRAG_ITEMS);
                if ((dragItems != null) && !dragItems.items.isEmpty()) {
                    img = GraphManager.getInstance().renderDragItems(gc, dragItems);
                }
            }
        }
        return img;
    }

    @Override
    public String getDragImage(String vizUuid) throws IOException, CentrifugeException {
        BufferedImage img = dragImageHelper(vizUuid);
        return ImageUtil.toBase64String(img);
    }

    @Override
    @Operation
    public void showLabels(@QueryParam(VIZUUID_PARAM) String vizUuid, @QueryParam("nodes") Boolean showNodeLabels)
            throws CentrifugeException {
        GraphContext gc = GraphServiceUtil.getGraphContext(vizUuid);
        if (gc == null) {
            throw new CentrifugeException(GRAPH_DATA_HAS_NOT_BEEN_LOADED);
        }
        synchronized (gc) {
            gc.toggleHideLabels();
        }
    }

   @Override
   @Operation
   @Interruptable
   public void saveGraph(@QueryParam(value = VIZUUID_PARAM) String vizUuid) throws IOException, CentrifugeException {
      checkReadOnly(vizUuid);
      TaskHelper.reportProgress("Saving graph", 0);

      GraphContext gc = GraphServiceUtil.getGraphContext(vizUuid);

      if (gc == null) {
            LOG.warn(GRAPH_DATA_HAS_NOT_BEEN_LOADED);
      } else {
         GraphDataManager.saveGraphData(gc);
      }
   }

   @Operation
   @Interruptable
   public void exportGraph(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                           @ServletResponseParam HttpServletResponse servletResponse) throws IOException, CentrifugeException {
      returnGraph(servletResponse, getGraphFile(vizUuid));
   }

    @Override
    public byte[] exportGraph(String vizUuid) throws IOException, CentrifugeException {
        File f = getGraphFile(vizUuid);
        ByteArrayOutputStream oimg = new ByteArrayOutputStream();
        FileInputStream fin = new FileInputStream(f);
        IOUtils.copy(fin, oimg);
        return Base64.getEncoder().encode(oimg.toByteArray());
        /*
         * Alternatively return a String File f = getGraphFile(vizUuid);
         * ByteArrayOutputStream bos = new ByteArrayOutputStream();;
         * FileInputStream fin = new FileInputStream(f); IOUtils.copy(fin, bos);
         * bos.close(); String base64 = new
         * String(Base64.getEncoder().encode(bos.toByteArray())); base64 =
         * "data:image/png;base64,"+base64; return base64;
         */
    }

    private File getGraphFile(String vizUuid) throws CentrifugeException {
        // Stubbed out for UI to add save button
        TaskHelper.reportProgress("Exporting graph", 0);
        GraphContext gc = GraphServiceUtil.getGraphContext(vizUuid);
        if (gc == null) {
            throw new CentrifugeException(GRAPH_DATA_HAS_NOT_BEEN_LOADED);
        }

        File f = new File(GraphDataManager.getGraphDir(), GraphDataManager.getVizFileName(gc.getVizUuid()));
        if (!f.exists()) {
            GraphDataManager.saveGraphContext(gc);
        }

        return f;
    }

    private void returnGraph(HttpServletResponse servletResponse, File f) throws IOException {
        try (FileInputStream fin = new FileInputStream(f)) {
            servletResponse.setStatus(200);
            servletResponse.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
            servletResponse.setContentType("application/octet-stream");
            servletResponse.setContentLength((int) f.length());
            IOUtils.copy(fin, servletResponse.getOutputStream());
        } finally {
            try {
                servletResponse.flushBuffer();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

   @Override
   public int selectVisibleNeighbors(String vizUuid, Integer degreesAway, Integer nodeId) {
      int result = 0;

      if (vizUuid != null) {
         GraphContext gc = GraphServiceUtil.getGraphContext(vizUuid);

         if (gc != null) {
            synchronized (gc) {
               if ((degreesAway != null) && (degreesAway.intValue() >= 0)) {
                  SelectionModel selection = gc.getSelection(GraphManager.DEFAULT_SELECTION);
                  Iterator<Node> selectedNodes =
                     new ArrayList<Node>(Arrays.asList(GraphContext.Functions.mapIdToNodeFunction(gc.getGraphData()).apply(nodeId))).iterator();
                  int currentSelectionNodeCount = selection.nodes.size();
                  BreadthFirstSearch bfs = new BreadthFirstSearch();

                  bfs.init(selectedNodes, degreesAway, Constants.NODE_TRAVERSAL);

                  while (bfs.hasNext()) {
                     Node node = (Node) bfs.next();

                     if (GraphContext.Predicates.IsVisualizedAndDisplayable.test(node)) {
                        selection.nodes.add(node.getRow());
                     }
                  }
                  if (gc.isPlayerRunning()) {
                     gc.updateDisplay();
                  }
                  int selectionNodeCountAfterAddition = selection.nodes.size();
                  int totalNewSelected = selectionNodeCountAfterAddition - currentSelectionNodeCount;

                  if (totalNewSelected > 0) {
                     result = totalNewSelected;
                  }
               }
            }
         }
      }
      return result;
   }

   @Override
   @Operation
   public int selectVisibleNeighbors(@QueryParam(value = VIZUUID_PARAM) String vizUuid,
                                     @QueryParam("degrees") Integer degreesAway) {
      int result = 0;

      if (vizUuid != null) {
         GraphContext gc = GraphServiceUtil.getGraphContext(vizUuid);

         if (gc != null) {
            synchronized (gc) {
               if ((degreesAway != null) && (degreesAway.intValue() >= 0)) {
                  SelectionModel selection = gc.getSelection(GraphManager.DEFAULT_SELECTION);
                  Collection<Node> selectedNodes = new ArrayList<Node>();
                  Function<Integer,Node> transform = GraphContext.Functions.mapIdToNodeFunction(gc.getGraphData());

                  for (Iterator<Integer> ids = selection.nodes.iterator(); ids.hasNext();) {
                     selectedNodes.add(transform.apply(ids.next()));
                  }
                  int currentSelectionNodeCount = selection.nodes.size();
                  BreadthFirstSearch bfs = new BreadthFirstSearch();

                  bfs.init(selectedNodes.iterator(), degreesAway, Constants.NODE_TRAVERSAL);

                  while (bfs.hasNext()) {
                     Node node = (Node) bfs.next();

                     if (GraphContext.Predicates.IsVisualizedAndDisplayable.test(node)) {
                        selection.nodes.add(node.getRow());
                     }
                  }
                  if (gc.isPlayerRunning()) {
                     gc.updateDisplay();
                  }
                  int selectionNodeCountAfterAddition = selection.nodes.size();
                  int totalNewSelected = selectionNodeCountAfterAddition - currentSelectionNodeCount;

                  if (totalNewSelected > 0) {
                     result = totalNewSelected;
                  }
               }
            }
         }
      }
      return result;
   }

    private void writeImageResponse(BufferedImage img, HttpServletResponse resp) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageUtil.writePNG(img, bos);

            resp.setStatus(200);
            resp.setContentType("image/png");
            resp.setContentLength(bos.size());
            resp.setHeader("Content-Disposition",
                           "attachment; filename=" + "graph-image" + System.currentTimeMillis() + ".png");

            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
               IOUtils.copy(bis, resp.getOutputStream());
            }
        } catch (ClientAbortException ce) {
            LOG.warn("Client aborted while writing image response", ce);
        } finally {
            try {
                resp.flushBuffer();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    @Override
    @Operation
    public void manuallyBundleSelection(@QueryParam(VIZUUID_PARAM) String vizUuid,
                                        @QueryParam(DVUUID_PARAM) String dvUuid, @QueryParam(BUNDLE_NAME) String bundleName)
            throws CentrifugeException {
        if (bundleName == null) {
            throw new CentrifugeException("Bundle name is required.");
        }

        GraphOperation operation = new GraphOperation();
        CsiMap<String, String> props = new CsiMap<String, String>();
        props.put("name", bundleName);
        props.put("currentLayout", "");
        props.put("includeBundleAnimation", "false");

        operation.operation = ACTION_BUNDLE;
        operation.type = OPERATION_SELECTION_MODEL;
        operation.parameters = props;
        operateOn(vizUuid, dvUuid, operation);
    }

    @Override
    public PlunkedNode plunkNewNode(PlunkNodeDTO plunkNodeDTO) throws CentrifugeException {
        checkReadOnly(plunkNodeDTO.getVizUuid());

        return new NodePlunker().plunk(plunkNodeDTO);
    }

    @Override
    public PlunkedLink plunkLink(PlunkLinkDTO plunkLinkDTO) throws CentrifugeException {
        checkReadOnly(plunkLinkDTO.getVizUuid());
        return new LinkPlunker().plunk(plunkLinkDTO);
    }

    @Override
    public PlunkedItemsToDeleteDTO deletePlunkedItem(String vizUuid, String itemKey, String objectType)
            throws CentrifugeException {
        checkReadOnly(vizUuid);
        removeAnnotation(vizUuid, itemKey);
        return new PlunkDeleter().delete(vizUuid, itemKey, OPERATION_EDGE_COMPLEX.equals(objectType));
    }

    @Override
    public void deleteAllPlunkedItems(String vizUuid) throws CentrifugeException {
        checkReadOnly(vizUuid);
        try {
            RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
            List<String> keysToRemove = new PlunkDeleter().deleteAll(relGraphViewDef);
            removeAnnotations(relGraphViewDef, keysToRemove);

            CsiPersistenceManager.merge(relGraphViewDef);
        } catch (Exception exception) {
            LOG.error("There was an error when attempting to delete a plunked node/link", exception);
        }
    }

    @Override
    public void savePlunkedNode(String vizUuid, PlunkedNode plunkedNode) throws CentrifugeException {
        checkReadOnly(vizUuid);
        plunkedNode.setHasBeenEdited(true);
        CsiPersistenceManager.merge(plunkedNode);
        new NodePlunker().editPlunkedNode(vizUuid, plunkedNode);
    }

    @Override
    public void savePlunkedLink(String vizUuid, PlunkedLink plunkedLink) throws CentrifugeException {
        checkReadOnly(vizUuid);
        CsiPersistenceManager.merge(plunkedLink);
        new LinkPlunker().editPlunkedLink(vizUuid, plunkedLink);
    }

    /**
     * Will Throw CentrifugeException if user doesn't have access to a Data View
     * that houses the viz
     *
     * @param vizUuid - the uuid of the viz
     * @throws CentrifugeException - "Access denied. Not authorized to edit dataview."
     */
    private void checkReadOnly(String vizUuid) throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext != null) {
            if (!CsiSecurityManager.isAuthorized(graphContext.getDvUuid(), AclControlType.EDIT)) {
                throw new CentrifugeException("Access denied.  Not authorized to edit dataview.");
            }
        }
    }

    @Override
    public List<String> getNodeTypes(String uuid) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);
        return new ArrayList<String>(graphContext.getNodeLegend().keySet());
    }

    @Override
    public boolean isDuplicate(String vizUuid, String name, String type) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        Graph graph = graphContext.getVisibleGraph();

        Iterator<Node> nodes = graph.nodes();

        // A node must be found twice to be a duplicate.
        int foundCount = 0;
        while (nodes.hasNext()) {
            Node node = nodes.next();
            NodeStore nodeDetails = GraphManager.getNodeDetails(node);
            if (!type.equals(nodeDetails.getType())) {
               continue;
            }
            String label = nodeDetails.getLabel();
            if (label.equals(name)) {
                if (++foundCount > 1) {
                  return true;
               }
            }
        }
        return false;
    }

    @Override
    public List<String> getLinkTypes(String uuid) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);
        return new ArrayList<String>(graphContext.getLinkLegend().keySet());
    }

   @Override
   public Boolean ensureViewport(String uuid, int clientWidth, int clientHeight) {
      Boolean result = Boolean.FALSE;
      GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);
//
//        System.out.println("should");
//        System.out.println(clientWidth);
//        System.out.println(clientHeight);
      if (graphContext != null) {
//		    System.out.println("context is valid");
         Display display = graphContext.getDisplay();
         double dcw = clientWidth;
         double dch = clientHeight;
         double ddw = display.getWidth();
         double ddh = display.getHeight();
         double heightRatio = dch / ddh;
         double widthRatio = dcw / ddw;
         double zoom = 1;

         if ((heightRatio > 1) && (widthRatio > 1)) {
            //grow until one of the ratios would be one
            zoom = Math.min(heightRatio, widthRatio);
         } else if ((heightRatio < 1) && (widthRatio < 1)) {
            //shrink until one of the ratios would be one
            zoom = Math.max(heightRatio, widthRatio);
         }
         if (BigDecimal.valueOf(zoom).compareTo(BigDecimal.ONE) != 0) {
            double zoomThatMakeSenseToServer = 0;

            if (zoom > 1) {
               zoomThatMakeSenseToServer = zoom - 1;
            } else if (zoom < 1) {
               zoomThatMakeSenseToServer = (-1D / (zoom)) + 1D;
            }
            graphContext.zoomPercent(zoomThatMakeSenseToServer);
         }
         //anchor to center
         graphContext.pan((int) -(ddw - dcw) / 2, (int) -(ddh - dch) / 2);

         Rectangle2D bounds = display.getVisualization().getBounds(ObjectAttributes.NODES_OBJECT_TYPE);
         double scaledBoundsWidth = bounds.getWidth() * display.getScale();
         double scaledBoundsHeight = bounds.getHeight() * display.getScale();
         result = Boolean.valueOf(boundsWithinViewport(clientWidth, clientHeight, scaledBoundsWidth, scaledBoundsHeight));
      }
      return result;
   }

    private boolean boundsWithinViewport(int width, int height, double scaledBoundsWidth, double scaledBoundsHeight) {
        return (scaledBoundsWidth < width) && (scaledBoundsHeight < height);
    }

    @Override
    public void manuallyBundleNodesById(String vizUuid, ArrayList<Integer> nodeIds, String bundleName) {
        checkArgument(!Strings.isNullOrEmpty(bundleName), "Bundle name is required.");
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        Graph graph = graphContext.getVisibleGraph();
        VisualGraph vGraph = graphContext.getVisualGraph();

        boolean hideLabels = true;

        for (Integer nodeId : nodeIds) {
            Node node = vGraph.getNode(nodeId);
            NodeStore nodeDetails = GraphManager.getNodeDetails(node);
            if (nodeDetails.isBundled()) {
                return;
                // throw new CentrifugeException(
                // "Bundling can only be performed against nodes that are not
                // already a member of a bundle");
            }
            hideLabels = hideLabels && nodeDetails.isHideLabels();
        }

        SelectionModel nodesToBundle = new SelectionModel();
        nodesToBundle.nodes.addAll(nodeIds);
        Node bundleNode = GraphManager.createBundle(graph, bundleName, nodesToBundle);

        List<Node> children = new ArrayList<Node>();
        NodeStore details = GraphManager.getNodeDetails(bundleNode);
        details.setHideLabels(hideLabels);
        for (AbstractGraphObjectStore child : details.getChildren()) {
            children.add(graphContext.getNodeFromDetails((NodeStore) child));
        }

        Collection<Point2D> points = new ArrayList<Point2D>(children.size());

        for (Node n : children) {
            NodeItem ni = (NodeItem) vGraph.getNode(n.getRow());
            points.add(new Point2D.Double(ni.getX(), ni.getY()));
            hideNodeAndEdges(vGraph, n);
        }

        Point2D centroid = GeometryUtil.centroidOf(points);
        NodeItem bundleNI = (NodeItem) vGraph.getNode(bundleNode.getRow());
        PrefuseLib.setX(bundleNI, null, centroid.getX());
        PrefuseLib.setY(bundleNI, null, centroid.getY());
        graphContext.showNodes(new ArrayList<Node>(Arrays.asList(bundleNode)).iterator());

        CsiMap<String, Object> results = new CsiMap<String, Object>();
        if (bundleNode instanceof VisualItem) {
            populatePropertyDetails((VisualItem) bundleNode, results, true);
        }

        BundleMetrics metrics = new BundleMetrics();
        NodeStore bundle = GraphManager.getNodeDetails(bundleNode);
        metrics = GraphManager.computeBundleIconSize(vGraph, bundle, metrics);
        bundle.setRelativeSize(metrics.computeSize());
        if (metrics.bySize) {
            bundle.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE);
        } else {
            if (metrics.byTransparency) {
                // bundle.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_TRANSPARENCY);
            }
        }
        VisualItem vi = (VisualItem) vGraph.getNode(bundleNode.getRow());
        vi.setSize(bundle.getRelativeSize());
    }

    @Override
    @Operation
    public boolean hasSelection(@QueryParam(value = VIZUUID_PARAM) String vizUuid) throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

        if (graphContext == null) {
            return false;
        }

        synchronized (graphContext) {

            SelectionModel selectionModel = graphContext.getSelection(GraphManager.DEFAULT_SELECTION);
            return (selectionModel != null) && (!selectionModel.nodes.isEmpty() || !selectionModel.links.isEmpty());
        }
    }

    @Override
    @Operation
    public FindPathResponse findPaths(@QueryParam(value = VIZUUID_PARAM) String vizId,
                                      @PayloadParam FindPathRequest request) throws CentrifugeException {

        if (vizId == null) {
            throw new CentrifugeException(VIZUUID_PARAM + " is required for findPaths.");
        }

        if ((request.selectedNodes == null)
                || ((request.selectedNodes != null) && request.selectedNodes.isEmpty())) {
            throw new CentrifugeException("Both a start and an end node are required for findPaths.");
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizId);

        if (context == null) {
            throw new CentrifugeException("Unable to find visualization with ID " + vizId);
        }

        List<PathPairs> searchPaths = new ArrayList<PathPairs>();
        Integer[] nodeIds = new Integer[request.selectedNodes.size()];
        request.selectedNodes.toArray(nodeIds);
        for (int i = 0; i < nodeIds.length; i++) {
            for (int j = i + 1; j < nodeIds.length; j++) {
                searchPaths.add(new PathPairs(nodeIds[i].intValue(), nodeIds[j].intValue()));
                if (request.includeDirection) {
                    searchPaths.add(new PathPairs(nodeIds[j].intValue(), nodeIds[i].intValue()));
                }
            }
        }
        List<Path> allPaths = new ArrayList<Path>();
        FindPathResponse response = new FindPathResponse();
        int pathSearchCount = 1;
        int maxPathSearch = searchPaths.size();
        int totalPaths = 0;
        synchronized (context) {
            Graph g = context.getGraphData();
            Node startNode = null;
            Node endNode = null;
            int pathId = 0;
            for (PathPairs pairs : searchPaths) {
                startNode = g.getNode(pairs.source);
                endNode = g.getNode(pairs.target);

                if (startNode == null) {
                    throw new CentrifugeException("Couldn't find starting node with ID " + pairs.source);
                }

                if (endNode == null) {
                    throw new CentrifugeException("Couldn't find ending node with ID " + pairs.target);
                }

                TaskHelper.checkForCancel();

                String msg = "Searching for " + request.numPaths + " path";
                if (request.numPaths > 1) {
                    msg += "s";
                }
                TaskHelper.reportProgress(msg, 5);
                YenTopKShortestPathsAlg algorithm = new YenTopKShortestPathsAlg(g, request.includeDirection, false);
                List<Path> paths = algorithm.get_shortest_paths(startNode, endNode, request.numPaths, request.minLength,
                        request.maxLength, pathSearchCount++, totalPaths, maxPathSearch, pathId);
                if ((paths != null) && !paths.isEmpty()) {
                    pathId = Integer.parseInt(paths.get(paths.size() - 1).getId());
                }
                TaskHelper.checkForCancel();
                allPaths.addAll(paths);
                totalPaths = allPaths.size();

            }
            allPaths = removeDups(allPaths);
            allPaths = filterPaths(allPaths, request);
            if ((allPaths != null) && !allPaths.isEmpty()) {
                int count = 0;
                for (Path p : allPaths) {
                    PathMeta meta = new PathMeta();
                    meta.id = p.getId();
                    meta.name = "Path " + (++count);
                    meta.pathNodes = getNodeInfo(p.get_vertices());
                    meta.length = p.get_vertices().size() - 1;
                    meta.source = meta.pathNodes.get(0);
                    meta.waypoints = meta.pathNodes.stream().filter(Objects::nonNull).collect(Collectors.joining(" \u2015 "));
                    meta.target = meta.pathNodes.get(meta.pathNodes.size() - 1);
                    response.foundPaths.add(meta);
                }
            }
            Collections.sort(response.foundPaths, PathMeta.COMPARE_UI_PATHLIST_ORDER);

            context.setPaths(allPaths);
            TaskHelper.reportProgress("Finished searching.", 100);
            return response;
        }

    }

    private List<Path> removeDups(List<Path> paths) {
        for (Path spath : paths) {
            for (Path dpath : paths) {
                if (spath.getId().equals(dpath.getId())) {
                    continue;
                }
                if (spath.equals(dpath)) {
                    paths.remove(dpath);
                }
            }
        }
        return paths;
    }

    private List<Path> filterPaths(List<Path> paths, FindPathRequest request) {
        if (request.matchNodes > 0) {
            List<Path> pathsToRemove = new ArrayList<Path>();
            for (Path path : paths) {
                List<Node> route = path.get_vertices();
                int startNode = route.get(0).getRow();
                int endNode = route.get(route.size() - 1).getRow();
                if (!pathContainsNode(route, request.selectedNodes, request.matchNodes, startNode, endNode)) {
                    pathsToRemove.add(path);
                }
            }
            for (Path path : pathsToRemove) {
                paths.remove(path);
            }
        }

        // For some reason, all the above code doesn't seem to limit return.
        // From what it looks like, the algorithm
        // being used actually limits the amount of paths per node..
        if (paths.size() > request.numPaths) {

            paths = paths.subList(0, request.numPaths);

        }

        return paths;
    }

    private boolean pathContainsNode(List<Node> nodes, List<Integer> selectNodes, int needMatch, int startNode,
                                     int endNode) {
        boolean rc = false;
        int mustHave = needMatch;
        int count = 0;
        for (Integer id : selectNodes) {
//            int nodeid = id.intValue();
            for (Node node : nodes) {
                int curNode = node.getRow();
                if ((startNode == curNode) || (endNode == curNode)) {
                    continue;
                }
                if (node.getRow() == id) {
                    count++;
                }
                if (count >= mustHave) {
                    rc = true;
                    break;
                }
            }
        }
        return rc;
    }

    private List<String> getNodeInfo(List<Node> vertices) {
        List<String> allInfo = new ArrayList<String>();
        for (Node node : vertices) {
            NodeStore details = GraphManager.getNodeDetails(node);
            NodeInfo info = new NodeInfo();
            info.label = details.getLabel();
            info.id = node.getRow();
            info.type = getAllTypes(details);
            allInfo.add(info.label);
        }
        return allInfo;
    }

    private String getAllTypes(NodeStore details) {
        Map<String, Integer> typeMap = details.getTypes();
        List<String> types = new ArrayList<String>(typeMap.size());
        types.addAll(typeMap.keySet());
        Collections.sort(types);
        return types.stream().collect(Collectors.joining(", "));
    }

   @Override
   @Operation
   @Interruptable
   public List<NodeInfo> findAllNodesMeta(@PayloadParam FindAllNodesMetaRequest request) throws CentrifugeException {
      List<NodeInfo> nodesInfo = new ArrayList<NodeInfo>();

      for (Integer nodeid : request.findPathNodes) {
         CsiMap map = findNodeMeta(nodeid.toString(), null, null, request.vizUUID);
         NodeInfo info = new NodeInfo();
         info.id = new Integer((String) map.get("id"));
         info.label = (String) map.get("label");

         if ((map.get("available") != null) && Boolean.parseBoolean((String) map.get("available"))) {
            nodesInfo.add(info);
         }
      }
      return nodesInfo;
   }

    @Override
    @Operation
    @Interruptable
    public CsiMap<String, String> findNodeMeta(@QueryParam(value = "nodeId") String idStr,
                                               @QueryParam(value = "x") String xStr, @QueryParam(value = "y") String yStr,
                                               @QueryParam(value = VIZUUID_PARAM) String vizUuid) throws CentrifugeException {
        int x = -1;
        int y = -1;
        int id = -1;
        CsiMap<String, String> prop = new CsiMap<String, String>();

        if ((idStr == null) && (xStr == null) && (yStr == null)) {
            throw new CentrifugeException("FindNodeMeta requires either a node ID or x and y positions.");
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizUuid);
        if (context == null) {
            return prop;
        }

        if (idStr != null) {
            try {
                id = Integer.valueOf(idStr);
            } catch (NumberFormatException e) {
                throw new CentrifugeException("Unable to parse node id.", e);
            }
        }

        if (xStr != null) {
            try {
                x = Double.valueOf(xStr).intValue();
            } catch (NumberFormatException e) {
                throw new CentrifugeException("Unable to parse x position.", e);
            }
        }

        if (yStr != null) {
            try {
                y = Double.valueOf(yStr).intValue();
            } catch (NumberFormatException e) {
                throw new CentrifugeException("Unable to parse y position.", e);
            }
        }

        synchronized (context) {
            Visualization viz = getVisualization(vizUuid);

            if (viz != null) {
               synchronized (viz) {
                  Display display = viz.getDisplay(0);

                  synchronized (display) {
                     Node node = null;

                     if (idStr != null) {
                        try {
                            node = context.getGraphData().getNode(id);
                        } catch (IllegalArgumentException e) {
                        }
                     }
                     if ((node == null) && (xStr != null) && (yStr != null)) {
                        VisualItem vi = display.findItem(new Point(x, y));

                        if ((vi != null) && (vi.getSourceTuple() instanceof Node)) {
                            node = (Node) vi.getSourceTuple();
                        }
                     }
                     if (node != null) {
                        NodeStore ns = GraphManager.getNodeDetails(node);

                        prop.put("id", Integer.toString(node.getRow()));
                        prop.put("label", ns.getLabel());
                        boolean nodeVisible = ns.isDisplayable() && !ns.isBundled() && !ns.isHidden();
                        prop.put("available", Boolean.toString(nodeVisible));
                     }
                  }
               }
            }
        }
        return prop;
    }

    @Override
    @Operation
    /**
     * @return true if the graph has been modified and requires a refresh, false
     *         otherwise.
     */
    public boolean highlightPaths(@QueryParam(value = VIZUUID_PARAM) String vizId, @PayloadParam List<String> pathIds)
            throws CentrifugeException {
        if (vizId == null) {
            throw new CentrifugeException(VIZUUID_PARAM + " is required for highlightPaths.");
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizId);
        if (context == null) {
            if ((pathIds == null) || pathIds.isEmpty()) {
                // If we can't find the graph and we're trying to clear
                // highlights, just
                // return false. Otherwise, we end up printing out scary and
                // unnecessary messages.
                return false;
            } else {
                throw new CentrifugeException("Unable to find visualization with ID " + vizId);
            }
        }

        if ((pathIds == null) || pathIds.isEmpty()) {
            SelectionModel selectionModel = getSelectionModel(GraphConstants.PATH_HIGHLIGHT, vizId);
            if ((selectionModel.nodes.isEmpty()) && (selectionModel.links.isEmpty())) {
                return false;
            }
        }

        /*SelectionModel highlighted = */
           selectPaths(vizId, pathIds, false, context, GraphConstants.PATH_HIGHLIGHT, true, true);

        return true;
    }

    @Override
    @Operation
    public SelectionModel selectPaths(@QueryParam(value = VIZUUID_PARAM) String vizId,
                                      @PayloadParam List<String> pathIds, @QueryParam("addToSelection") String addStr,
                                      @QueryParam("selectNodes") String selNode, @QueryParam("selectLinks") String selLink)
            throws CentrifugeException {
        boolean addToSelection = false;
        boolean selectNodes = false;
        boolean selectLinks = false;

        if (vizId == null) {
            throw new CentrifugeException(VIZUUID_PARAM + " is required for selectPaths.");
        }

        if ((pathIds == null) || pathIds.isEmpty()) {
            throw new CentrifugeException("One or more pathIds are required for selectPaths.");
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizId);
        if (context == null) {
            throw new CentrifugeException("Unable to find visualization with ID " + vizId);
        }

        if (addStr != null) {
            addToSelection = Boolean.parseBoolean(addStr);
        }

        if (selNode != null) {
            selectNodes = Boolean.parseBoolean(selNode);
        }

        if (selLink != null) {
            selectLinks = Boolean.parseBoolean(selLink);
        }

        SelectionModel selectionModel = selectPaths(vizId, pathIds, addToSelection, context, DEFAULT_SELECTION, selectNodes, selectLinks);
        GraphHelper.removeHiddenFromSelection(selectionModel);
        return selectionModel;
    }

   private SelectionModel selectPaths(String vizId, List<String> pathIds, boolean addToSelection, GraphContext context,
                                      String selectionName, boolean selectNodes, boolean selectLinks) {
      synchronized (context) {
         SelectionModel selectionModel = getSelectionModel(selectionName, vizId);

         if (!addToSelection) {
            selectionModel.nodes.clear();
            selectionModel.links.clear();
         }
         Visualization vis = getVisualization(vizId);

         if (vis != null) {
            synchronized (vis) {
               List<Path> paths = context.getPaths();
               List<Path> selectedPaths = new ArrayList<Path>();

               if (paths != null) {
                  for (Path p : paths) {
                     for (String id : pathIds) {
                        if (p.getId().equals(id)) {
                           selectedPaths.add(p);
                        }
                     }
                  }
                  for (Path path : selectedPaths) {
                     Node last = null;

                     for (Node n : path.get_vertices()) {
                        if (selectNodes) {
                           selectionModel.nodes.add(n.getRow());
                        }
                        if (last != null) {
                           Edge e = context.getGraphData().getEdge(n, last);

                           if (e == null) {
                              e = context.getGraphData().getEdge(last, n);
                           }
                           if ((e != null) && selectLinks) {
                              selectionModel.links.add(e.getRow());
                           }
                        }
                        last = n;
                     }
                  }
               }
            }
         }
         if (context.isPlayerRunning()) {
            context.updateDisplay();
         }
         return selectionModel;
      }
   }

   @Override
   public void fitToRegion(String vizUUID, int x, int y, int w, int h) {
      GraphContext context = GraphServiceUtil.getGraphContext(vizUUID);

      if (context != null) {
         synchronized (context) {
            Rectangle2D bounds = new Rectangle(x - 25, y - 25, w + 50, h + 50);
            // context.computeSoftMinimumBounds(bounds);
            // expand with 15% border to ensure nothing gets clipped on the
            // edges of the display
            double dim = Math.max(bounds.getWidth(), bounds.getHeight());
            // dim = Math.max(dim, 50);

            GraphicsLib.expand(bounds, dim * .15);
            context.fitToRegion(bounds);
         }
      }
   }

    @Override
    public String getNodeAsImage(String imageLoc, ShapeType shape, int color, int size, double iconScale) {
        // checkArgument((color >= 0) && (color <= 16777215), "Must provide a
        // valid color");
        if (((color < 0) || (color > 16777215))) {
            color = 0;
        }
        checkArgument(size > 0, "Cannot render size < 0");
        NodeRenderer nodeRenderer = new NodeRenderer();
        Color shapeColor = new Color(color);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) img.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if ((shape != null) && (shape instanceof ShapeType) && (shape != ShapeType.NONE)) {
            nodeRenderer.renderShape(graphics, shapeColor, shape, false, false, false, size / 2F, size / 2F,
                    size - TWICE_THE_PADDING_FOR_RENDERED_NODE);
        } else if (!Strings.isNullOrEmpty(imageLoc)) {
            ImageLocation imageURL;
            // File f = new File("webapps" + imageLoc.replace('\\', '/'));
            imageURL = new ImageLocation(imageLoc);
            // FIXME: This conditional is designed to support iconScales greater
            // than 1, without cropping the image.
            nodeRenderer.renderImage(graphics, imageURL, size, (size * (iconScale > 1 ? 1 : iconScale)));
        } else {
            // We didn't render a shape or icon, so we need SOMETHING

        }

        return ImageUtil.toBase64String(img);
    }

    @Override
    public String getNodeAsImageNew(String iconId, ShapeType shape, int color, int size, double iconScale)
            throws CentrifugeException {
        return getNodeAsImageNew(iconId, false, shape, color, 1.0f, false, false, false, size, iconScale, 1, false, false, false);
    }

    @Override
    public String getBundleIcon(int size, double iconScale) throws CentrifugeException {

        try {

            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = (Graphics2D) img.getGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            NodeRenderer nodeRenderer = new NodeRenderer();
            nodeRenderer.renderBundleImage(graphics, size, (size * (iconScale > 1 ? 1 : iconScale)));

            return ImageUtil.toBase64String(img);
        } catch (Exception e) {
            LOG.error("Unable to render bundle icon", e);
        }

        return NO_SHAPE_ICON;

    }

    @Override
    public String getNodeAsImageNew(String iconId, boolean isMap, ShapeType shape, int color, float alpha, boolean isSelected,
                                    boolean isHighlighted, boolean isCombined, int size, double iconScale, int strokeSize, boolean useSummary,
                                    boolean isNew, boolean isUpdated) throws CentrifugeException {
        if (((color < 0) || (color > 16777215))) {
            color = 0;
        }
        checkArgument(size > 0, "Cannot render size < 0");
        NodeRenderer nodeRenderer = new NodeRenderer();
        Image image = null;
        if ((iconId != null) && (iconId.length() > 5)) {
            if (useSummary) {
                if (shape == ShapeType.NONE) {
                  shape = ShapeType.CIRCLE;
               }
            } else {
//				ImageFactory imageFactory = new ImageFactory(512,512);
//				image = imageFactory.getImage(new ImageLocation(iconId), size,size);
//				image = getImage(iconId, nodeRenderer);

                if (iconId.contains(":") || iconId.contains("/") || iconId.contains("\\")) {
                    ImageFactory imageFactory = new ImageFactory(512, 512);
                    image = imageFactory.getImage(new ImageLocation(iconId), size, size);
                } else {
                    String icon = iconActionsService.getBase64Image(iconId);
                    try {
                        image = nodeRenderer.renderImage(icon);
                    } catch (IOException e) {
                        ImageFactory imageFactory = new ImageFactory(512, 512);
                        image = imageFactory.getImage(new ImageLocation(iconId), size, size);
                    }
                }

                if ((shape == ShapeType.NONE) && (image == null)) {
                    if (isMap) {
                        String brokenImageUrl = Configuration.getInstance().getWebApplicationContext().getServletContext().getRealPath("h5/" + GraphActionsService.NO_SHAPE_ICON);
                        try {
                            image = ImageIO.read(new File(brokenImageUrl));
                        } catch (IOException e) {
                            return BROKEN_IMAGE;
                        }
                    } else {
                        return BROKEN_IMAGE;
                    }
                }
            }
        }
        if (((shape != ShapeType.NONE) || isSelected || isHighlighted || isCombined || isNew || isUpdated) &&
            (image != null) && (BigDecimal.valueOf(alpha).compareTo(BigDecimal.ONE) != 0)) {
           alpha = alpha / 2;
        }
        int colorInt = ((((int) (alpha * 255)) & 0xFF) << 24) | color;
        Color shapeColor = new Color(colorInt, true);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) img.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if ((shape != ShapeType.NONE) || isSelected || isHighlighted || isCombined || isNew || isUpdated) {
            nodeRenderer.renderShape(graphics, isMap, shapeColor, shape, isSelected, isHighlighted, isCombined,
                    useSummary, isNew, isUpdated, size / 2F, size / 2F, size - TWICE_THE_PADDING_FOR_RENDERED_NODE,
                    size - TWICE_THE_PADDING_FOR_RENDERED_NODE, strokeSize);
            if (image != null) {
                // FIXME: This conditional is designed to support iconScales
                // greater than 1, without cropping the image.
                int newSize = size;
                if (isMap) {
                    newSize -= 4;
                    if (isSelected || isNew || isUpdated) {
                        if (isSelected && isNew && isUpdated) {
                            newSize -= 12;
                        } else if ((isSelected && isNew) || (isSelected && isUpdated) || (isNew && isUpdated)) {
                            newSize -= 10;
                        } else {
                            newSize -= 8;
                        }
                    } else {
                        if (isHighlighted) {
                            newSize -= 6;
                        }
                    }
                    if (isCombined) {
                        newSize -= 2;
                    }
                }
                Composite orig = graphics.getComposite();
                float alphaValue = alpha;
                {//sanitize alpha value
                    alphaValue = Math.max(0, alphaValue);
                    alphaValue = Math.min(1, alphaValue);
                }
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue);
                graphics.setComposite(alphaComposite);
                nodeRenderer.renderImage(graphics, iconId, image, size, (newSize * (iconScale > 1 ? 1 : iconScale)));
                graphics.setComposite(orig);
            }
        } else if (image != null) {
            // FIXME: This conditional is designed to support iconScales greater
            // than 1, without cropping the image.
            Composite orig = graphics.getComposite();
            float alphaValue = alpha;
            {//sanitize alpha value
                alphaValue = Math.max(0, alphaValue);
                alphaValue = Math.min(1, alphaValue);
            }
            AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue);
            graphics.setComposite(alphaComposite);
            nodeRenderer.renderImage(graphics, iconId, image, size, (size * (iconScale > 1 ? 1 : iconScale)));
            graphics.setComposite(orig);
        } else {
            return NO_SHAPE_ICON;
        }
        return ImageUtil.toBase64String(img);
    }

    private Image getImage(String iconId, NodeRenderer nodeRenderer) throws CentrifugeException {
        Image image;
        if (iconId.contains(":") || iconId.contains("/") || iconId.contains("\\")) {
            image = ImageFactory.readImageFromLocation(iconId);
        } else {
            String icon = iconActionsService.getBase64Image(iconId);
            try {
                image = nodeRenderer.renderImage(icon);
            } catch (IOException e) {
                image = ImageFactory.readImageFromLocation(iconId);
            }
        }
        return image;
    }

    private BufferedImage getBufferedImage(Image image) {
        BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    @Override
    public void revealNeighborsOfSelectedNodes(String uuid) {
        SelectionModel selection = getSelection(uuid, DEFAULT_SELECTION);
        TreeSet<Integer> selectedNodes = selection.nodes;
        ArrayList<Integer> selectNodesNeighbors = new ArrayList<Integer>();
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);

        VisualGraph visibleGraph = graphContext.getVisualGraph();

        for (Integer nodeId : selectedNodes) {
            Node node = visibleGraph.getNode(nodeId);
            Iterator<Edge> edges = node.edges();
            while (edges.hasNext()) {
                Edge next = edges.next();
                Node adjacentNode = next.getAdjacentNode(node);
                selectNodesNeighbors.add(adjacentNode.getRow());
            }
            // selectNodesNeighbors.addAll(nodeNeighbors(uuid, nodeId));
        }
        unhideNodeById(uuid, selectNodesNeighbors);
    }

    @Override
    public void showOnlyPaths(String vizUUID, List<String> pathIds) {
        try {
            Selection stashedSelection;
            GraphContext context;
            {
                // save selection
                context = GraphServiceUtil.getGraphContext(vizUUID);
                SelectionModel selection = context.getSelection(DEFAULT_SELECTION);
                stashedSelection = selection.copy();
            }
            selectPaths(vizUUID, pathIds, "false", "true", "true");
            invertSelection(null, vizUUID);
            hideSelection(null, vizUUID);
            {
                // restore selection
                SelectionModel selection = context.getSelection(DEFAULT_SELECTION);
                selection.setFromSelection(stashedSelection);
            }
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PatternResultSet findPatterns(String uuid, GraphPattern pattern) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);

        try {
            PatternResultSet resultSet = (new GraphDatabaseHelper()).findPattern(pattern, graphContext.getDvUuid(),
                    graphContext);
            return resultSet;
            // Set<TreeSet<String>> treeSets = (new
            // GraphDatabaseHelper()).findPattern(vGraph, pattern);
            // String selectionName = DEFAULT_SELECTION;
            // SelectionModel selectionModel = getSelectionModel(selectionName,
            // uuid);
            // selectionModel.clearSelection();
            //
            // for (TreeSet<String> results : treeSets) {
            // for (String s : results) {
            // selectionModel.nodes.add(Integer.parseInt(s));
            // }
            // }
        } catch (URISyntaxException ignored) {
        }

        return null;
    }

    @Override
    public void highlightPatterns(String uuid, List<PatternHighlightRequest> patternHighlightRequests) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);
        Graph graph = graphContext.getVisibleGraph();
        List<PatternSelection> highlights = new ArrayList<PatternSelection>();
        for (PatternHighlightRequest patternHighlightRequest : patternHighlightRequests) {
            SelectionModel selectionModel = new SelectionModel();
            for (PatternMeta selectedPattern : patternHighlightRequest.getSelectedPatterns()) {
                for (String nodeRows : selectedPattern.getPattern().getNodes()) {
                    try {
                        int i = Integer.parseInt(nodeRows);
                        selectionModel.nodes.add(i);
                    } catch (NumberFormatException ignored) {

                    }
                }

                for (String patternEdge : selectedPattern.getPattern().getLinks()) {
                    try {
                        selectionModel.links.add(getLinkRow(graph, patternEdge));
                    } catch (Exception e) {// this should not happen but
                        // probably not a big deal.
                        e.printStackTrace();
                    }
                }
            }
            PatternSelection patternSelection = new PatternSelection();
            patternSelection.setSelectionModel(selectionModel);
            patternSelection.setColor(patternHighlightRequest.getColor());
            highlights.add(patternSelection);
        }
        graphContext.setPatternHighlights(highlights);
    }

    @Override
    public Boolean validateBundleName(String graphUuid, String suggestedName) {
        if (Strings.isNullOrEmpty(suggestedName)) {
            return false;
        }
        GraphContext graphContext = GraphServiceUtil.getGraphContext(graphUuid);
        VisualGraph vGraph = graphContext.getVisualGraph();
        Iterator nodes = vGraph.nodes();
        while (nodes.hasNext()) {
            VisualItem visualItem = (VisualItem) nodes.next();
            NodeStore detail = GraphManager.getNodeDetails(visualItem);
            if (detail.getLabel().equals(suggestedName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void showOnlyPatterns(String uuid, List<PatternMeta> patterns) {
        SelectionModel selectionModel = getSelectionModel(DEFAULT_SELECTION, uuid);
        // backup old selection
        SelectionModel copyOriginalSelection = new SelectionModel();
        copyOriginalSelection.merge(selectionModel);
        // clear selection so that we can hide items
        selectionModel.clearSelection();
        // add the items in thea pattern to the selection
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);
        Graph graph = graphContext.getVisibleGraph();

        for (PatternMeta selectedPattern : patterns) {
            for (String nodeRows : selectedPattern.getPattern().getNodes()) {
                try {
                    int i = Integer.parseInt(nodeRows);
                    selectionModel.nodes.add(i);
                } catch (NumberFormatException ignored) {

                }
            }

            for (String patternEdge : selectedPattern.getPattern().getLinks()) {
                selectionModel.links.add(getLinkRow(graph, patternEdge));

            }
        }
        invertSelection(null, uuid);
        try {
            hideSelection(null, uuid);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        selectionModel.clearSelection();
        // restore selection
//		selectionModel.merge(copyOriginalSelection);
        for (Integer node : copyOriginalSelection.nodes) {
            Node e = graphContext.getVisualGraph().getNode(node);
            NodeStore details = GraphManager.getNodeDetails(e);
            if (details.isHidden()) {
                selectionModel.nodes.add(node);
            }
        }
        for (Integer link : copyOriginalSelection.links) {
            Edge e = graphContext.getVisualGraph().getEdge(link);
            LinkStore details = GraphManager.getEdgeDetails(e);
            if (details.isHidden()) {
                selectionModel.links.add(link);
            }
        }
    }

    @Override
    public void addSelectPatterns(String uuid, List<PatternMeta> patterns, boolean selectNodes, boolean selectLinks) {
        SelectionModel selectionModel = getSelectionModel(DEFAULT_SELECTION, uuid);
        GraphContext graphContext = GraphServiceUtil.getGraphContext(uuid);
        Graph graph = graphContext.getVisibleGraph();

        for (PatternMeta selectedPattern : patterns) {
            if (selectNodes) {
                for (String nodeRows : selectedPattern.getPattern().getNodes()) {
                    try {
                        int i = Integer.parseInt(nodeRows);
                        selectionModel.nodes.add(i);
                    } catch (NumberFormatException ignored) {

                    }
                }
            }
            if (selectLinks) {
                for (String patternEdge : selectedPattern.getPattern().getLinks()) {
                    selectionModel.links.add(getLinkRow(graph, patternEdge));

                }
            }
        }
    }

    @Override
    public void selectPatterns(String uuid, List<PatternMeta> selectedPatterns, boolean selectNodes,
                               boolean selectLinks) {
        SelectionModel selectionModel = getSelectionModel(DEFAULT_SELECTION, uuid);
        selectionModel.clearSelection();
        addSelectPatterns(uuid, selectedPatterns, selectNodes, selectLinks);
    }

    private Integer getLinkRow(Graph graph, String edgeKey) {
        Map<String, Edge> edgeMap = (Map<String, Edge>) graph.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        return edgeMap.get(edgeKey).getRow();
    }

    @Override
    public Annotation addAnnotation(AnnotationDTO annotationDTO) throws CentrifugeException {

        checkReadOnly(annotationDTO.getVizUuid());

        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class,
                annotationDTO.getVizUuid());
        Annotation newAnnotation = null;
        List<Annotation> annotations = relGraphViewDef.getAnnotations();

        // check list for this annotation
        for (Annotation annotation : annotations) {
            if (annotation.getParentKey().equals(annotationDTO.getParentId())) {
                annotation.setHtmlString(annotationDTO.getHtmlString());
                newAnnotation = annotation;
                break;
            }
        }

        // Add new annotation if it's not in list
        if (newAnnotation == null) {
            newAnnotation = new Annotation();
            newAnnotation.setHtmlString(annotationDTO.getHtmlString());
            newAnnotation.setParentKey(annotationDTO.getParentId());
            relGraphViewDef.getAnnotations().add(newAnnotation);
        }

        if (!annotationDTO.getParentId().equals("-1")) {
            GraphContext graphContext = GraphServiceUtil.getGraphContext(annotationDTO.getVizUuid());
//            AbstractGraphObjectStore store;
            Node node = graphContext.getNodeKeyIndex().get(annotationDTO.getParentId());
//TODO: store is not used
            if (node == null) {
                Edge edge = graphContext.getEdgeKeyIndex().get(annotationDTO.getParentId());
                /*store = */GraphManager.getEdgeDetails(edge);
            } else {
                /*store = */GraphManager.getNodeDetails(node);
            }
        }
        return newAnnotation;
    }

    public Map<String, Object> createTooltip(GraphContext graphContext, CsiMap<String, Object> infoMap, VisualItem item,
                                             Map<String, Object> oldtips, boolean isEdge) throws CentrifugeException {

        Connection conn = null;
        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class,
                graphContext.getVizUuid());
        DataView dataview = CsiPersistenceManager.findObject(DataView.class, graphContext.getDvUuid());
        Map<String, List<Integer>> rows = null;
        NodeStore nodeStore = null;

        if (isEdge) {
            LinkStore linkStore = GraphManager.getEdgeDetails(item);
            rows = linkStore.getRows();
        } else {
            nodeStore = GraphManager.getNodeDetails(item);
            rows = nodeStore.getRows();
        }

        CsiMap<String, Object> newtips = new CsiMap<String, Object>();
        if ((nodeStore != null) && nodeStore.hasChildren()) {

            List<Map<String, String>> labels = new ArrayList<Map<String, String>>(nodeStore.getChildren().size());
            for (AbstractGraphObjectStore child : nodeStore.getChildren()) {

                if (!(child instanceof NodeStore)) {
                    continue;
                }
                if (child.hasChildren()) {
                    Map<String, String> label = new TreeMap<String, String>();
                    label.put("label", child.getLabel());
                    labels.add(label);
                } else {

                    Set<String> types = child.getRows().keySet();

                    Map<String, String> label = new TreeMap<String, String>();
                    ResultSet resultSet = null;
                    label.put("label", child.getLabel());
                    for (String defType : types) {
                        try {

                            Set<AttributeDef> attributes = new HashSet<AttributeDef>();
                            Map<String, Property> propertyMap;
                            CsiMap<String, Object> toolTipProps = new CsiMap<String, Object>();

                            Set<Integer> rowsOfNode = new HashSet<Integer>();
                            rowsOfNode.addAll(rows.get(defType));
                            conn = CsiPersistenceManager.getCacheConnection();
                            DataCacheHelper cacheHelper = new DataCacheHelper();
                            String filter = cacheHelper.getTooltipFilter(graphContext.getDvUuid(), relGraphViewDef,
                                    rowsOfNode);

                            String query = String.format(CacheCommands.SELECT_ALL_QUERY,
                                    CacheUtil.getQuotedCacheTableName(graphContext.getDvUuid()));
                            query += " WHERE " + filter;

                            resultSet = QueryHelper.executeSingleQuery(conn, query, null);

                            // Retrive correct NodeDef(s) (bundles mean possibly many)
                            for (NodeDef nodeDef : relGraphViewDef.getNodeDefs()) {

                                if ((null != nodeDef.getName()) && nodeDef.getName().equals(defType)) {
                                    attributes.addAll(nodeDef.getAttributeDefs());
                                }
                            }

                            // Creates properties and computes aggregates
                            GraphManager.getInstance().createTooltipProperties(dataview, resultSet, attributes, child);

                            // Begin Converting tooltip properties to infomap
                            propertyMap = child.getAttributes();

                            Set<Map.Entry<String, Property>> entrySet = propertyMap.entrySet();
                            for (Map.Entry<String, Property> entry : entrySet) {
                                String entryKey = entry.getKey();
                                Property value = entry.getValue();

                                // TODO: where to get this?
                                // tooltipOrderMap.put(entryKey,
                                // value.getTooltipOrdinal());

                                if (isMetricProperty(value)
                                        || entryKey.equals(GraphMetrics.SUBGRAPH_PROP_NAME.getLocalPart())) {
                                    continue;
                                }

                                // Use simple name here
                                entryKey = value.getName();
                                if (!value.isIncludeInTooltip()) {
                                    continue;
                                }
                                List<Object> values = value.getValues();

                                List<String> valuesWithCounts = getValuesWithCounts(values,
                                        value.isHideEmptyInTooltip());

                                if (entryKey.contains(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                                    values.remove(null);
                                    infoMap.put(entryKey, valuesWithCounts);
                                } else {
                                    if (value.isHideEmptyInTooltip()) {
                                        while (values.indexOf(null) != -1) {
                                            values.remove(null);
                                        }

                                        while (values.contains("")) {
                                            values.remove("");
                                        }
                                    }

                                    if (!values.isEmpty()) {
                                        toolTipProps.put(entryKey, valuesWithCounts);
                                    }
                                }
                            }

                            for (String childKey : child.getAttributes().keySet()) {
                                if (!childKey.contains(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                                    if (child.getAttributes().get(childKey) instanceof Property) {
                                        Property property = child.getAttributes().get(childKey);
                                        label.put(childKey, property.getValues().toString());
                                    } else {
                                        label.put(childKey, child.getAttributes().get(childKey).toString());
                                    }
                                }
                            }

                            // TODO: creation of label and type need to be
                            // pushed to this method
                            // newtips.put("Label", infoMap.get(DISPLAY_LABEL));
                            // newtips.put("Type",
                            // infoMap.get(VISUAL_ITEM_TYPE));
                            // if (infoMap.get(MEMBER_TYPES) != null) {
                            // newtips.put("Member Types",
                            // infoMap.get(MEMBER_TYPES));
                            // }
                            //
                            // newtips.putAll(toolTipProps);

                        } catch (Exception e) {
                            throw new CentrifugeException(e);
                        } finally {
                            SqlUtil.quietCloseConnection(conn);
                            SqlUtil.quietCloseResulSet(resultSet);
                        }
                    }

                    labels.add(label);
                }
            }
            newtips.put("Label", infoMap.get(DISPLAY_LABEL));
            newtips.put("Type", infoMap.get(VISUAL_ITEM_TYPE));
            if (infoMap.get(MEMBER_TYPES) != null) {
                newtips.put("Member Types", infoMap.get(MEMBER_TYPES));
            }
            newtips.put("Contents", labels);
        } else {

            Set<String> types = rows.keySet();

            for (String defType : types) {
                try {

                    Set<Integer> rowsOfNode = new HashSet<Integer>();
                    rowsOfNode.addAll(rows.get(defType));
                    conn = CsiPersistenceManager.getCacheConnection();
                    DataCacheHelper cacheHelper = new DataCacheHelper();
                    String filter = cacheHelper.getTooltipFilter(graphContext.getDvUuid(), relGraphViewDef, rowsOfNode);

                    String query = String.format(CacheCommands.SELECT_ALL_QUERY,
                            CacheUtil.getQuotedCacheTableName(graphContext.getDvUuid()));
                    query += " WHERE " + filter;

                    ResultSet resultSet = QueryHelper.executeSingleQuery(conn, query, null);

                    CsiMap<String, Object> toolTipProps = createDynamicTooltip(infoMap, item, relGraphViewDef, dataview,
                            newtips, resultSet, rows, defType);

                    // TODO: creation of label and type need to be pushed to
                    // this method
                    newtips.put("Label", infoMap.get(DISPLAY_LABEL));
                    newtips.put("Type", infoMap.get(VISUAL_ITEM_TYPE));
                    if (infoMap.get(MEMBER_TYPES) != null) {
                        newtips.put("Member Types", infoMap.get(MEMBER_TYPES));
                    }

                    newtips.putAll(toolTipProps);

                } catch (Exception e) {
                    throw new CentrifugeException(e);
                } finally {
                    SqlUtil.quietCloseConnection(conn);
                }
            }
        }
        newtips.putAll(oldtips);
        infoMap.put(TOOLTIPS_TYPES, newtips);
        return newtips;
    }

    public CsiMap<String, Object> createDynamicTooltip(CsiMap<String, Object> infoMap, VisualItem item,
                                                       RelGraphViewDef relGraphViewDef, DataView dataview, CsiMap<String, Object> newtips, ResultSet resultSet,
                                                       Map<String, List<Integer>> rows, String defType)
            throws InstantiationException, IllegalAccessException, SQLException {
        String key;

        Set<AttributeDef> attributes = new HashSet<AttributeDef>();
        Map<String, Property> propertyMap;
        CsiMap<String, Object> toolTipProps = new CsiMap<String, Object>();

        // Check if link or node, then get key from appropriate store.
        if (infoMap.get("csi.object.type").equals("graph.edges")) {
            LinkStore linkStore = GraphManager.getEdgeDetails(item);
            propertyMap = linkStore.getAttributes();
            key = linkStore.getKey();

            // Retrive correct NodeDef(s) (bundles mean possibly many)
            for (LinkDef linkDef : relGraphViewDef.getLinkDefs()) {

                if ((null != linkDef.getName()) && linkDef.getName().equals(defType)) {
                    attributes.addAll(linkDef.getAttributeDefs());
                }
            }

            // Creates properties and computes aggregates
            GraphManager.getInstance().createTooltipProperties(dataview, resultSet, attributes, linkStore);

            List<String> labels = linkStore.getLabels();
            if ((labels != null) && !labels.isEmpty()) {
                infoMap.put(DISPLAY_LABEL, labels.get(0));
            }
            infoMap.put(ALL_LABELS, labels);
            populateTypeLabels(infoMap, linkStore);

            /*
             * Populate the map with direction-related information, for the
             * client to display in the tooltip
             */
            infoMap.put(DIRECTION, directionAggregator.aggregateDirectionTypeStrings(linkStore));
            infoMap.put(COMPUTED, directionAggregator.aggregateComputedFields(linkStore));

            infoMap.put(PROPERTY_SIZE, item.getSize());
            infoMap.put(PLUNKED, linkStore.isPlunked());
            Set<Map.Entry<String, Property>> entrySet = propertyMap.entrySet();
            String entryKey;
            for (Map.Entry<String, Property> entry : entrySet) {
                entryKey = entry.getKey();
                Property value = entry.getValue();

                if (!value.isIncludeInTooltip()) {
                    continue;
                }

                if (isDirectionValue(entryKey)) {
                    continue;
                }

                if (value instanceof AggregateProperty) {
                    continue;
                }

                List<Object> values = value.getValues();

                List<String> valuesWithCounts = getValuesWithCounts(values, value.isHideEmptyInTooltip());

                if (entryKey.contains(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                    infoMap.put(entryKey, valuesWithCounts);
                } else if (!valuesWithCounts.isEmpty() || !value.isHideEmptyInTooltip()) {
                    toolTipProps.put(entryKey, valuesWithCounts);
                }
            }
            infoMap.put("toolTipProps", toolTipProps);
        } else {
            NodeStore nodeStore = GraphManager.getNodeDetails(item);
            key = nodeStore.getKey();

            //Retrive correct NodeDef(s) (bundles mean possibly many)
            for (NodeDef nodeDef : relGraphViewDef.getNodeDefs()) {

                if ((null != nodeDef.getName()) && nodeDef.getName().equals(defType)) {
                    attributes.addAll(nodeDef.getAttributeDefs());
                }
            }

            //Creates properties and computes aggregates
            GraphManager.getInstance().createTooltipProperties(dataview, resultSet, attributes, nodeStore);

            //Begin Converting tooltip properties to infomap
            propertyMap = nodeStore.getAttributes();
            populateInfoWithTooltips(infoMap, propertyMap, toolTipProps);

            // if (nodeStore.isBundle()) {
            // populateBundleContents(toolTipProps, nodeStore);
            // }

            populateSNAMetrics(toolTipProps, nodeStore);

        }

        List<Annotation> annotations = relGraphViewDef.getAnnotations();
        List<String> itemAnnotations = new ArrayList<String>();

        for (Annotation annotation : annotations) {
            if (annotation.getParentKey().equals(key)) {
                itemAnnotations.add(annotation.getHtmlString());
            }
        }

        if (!itemAnnotations.isEmpty()) {
            newtips.put(COMMENTS, itemAnnotations);

            Map<String, Integer> tooltipOrderMap = (Map<String, Integer>) infoMap.get(TOOLTIP_ORDER);
            if (tooltipOrderMap == null) {
                tooltipOrderMap = new HashMap<String,Integer>();
            }

            tooltipOrderMap.put(COMMENTS, Annotation.tooltipOrder);

        }
        return toolTipProps;
    }

    public void populateInfoWithTooltips(CsiMap<String, Object> infoMap, Map<String, Property> propertyMap,
                                         CsiMap<String, Object> toolTipProps) {
        Set<Map.Entry<String, Property>> entrySet = propertyMap.entrySet();
        for (Map.Entry<String, Property> entry : entrySet) {
            String entryKey = entry.getKey();
            Property value = entry.getValue();

            // TODO: where to get this?
            // tooltipOrderMap.put(entryKey, value.getTooltipOrdinal());

            if (isMetricProperty(value) || entryKey.equals(GraphMetrics.SUBGRAPH_PROP_NAME.getLocalPart())) {
                continue;
            }

            // Use simple name here
            entryKey = value.getName();
            if (!value.isIncludeInTooltip()) {
                continue;
            }
            List<Object> values = value.getValues();

            List<String> valuesWithCounts = getValuesWithCounts(values, value.isHideEmptyInTooltip());

            if (entryKey.contains(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                values.remove(null);
                infoMap.put(entryKey, valuesWithCounts);
            } else {
                if (value.isHideEmptyInTooltip()) {
                    while (values.indexOf(null) != -1) {
                        values.remove(null);
                    }

                    while (values.contains("")) {
                        values.remove("");
                    }
                }

                if (!values.isEmpty()) {
                    toolTipProps.put(entryKey, valuesWithCounts);
                }
            }
        }
    }

    @Override
    public Annotation retrieveAnnotation(AnnotationDTO annotationDTO) {
        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class,
                annotationDTO.getVizUuid());
        Annotation foundAnnotation = null;
        List<Annotation> annotations = relGraphViewDef.getAnnotations();

        // check list for this annotation
        for (Annotation annotation : annotations) {

            if (annotation.getParentKey().equals(annotationDTO.getParentId())) {

                foundAnnotation = annotation;
                break;
            }
        }

        return foundAnnotation;
    }

    @Override
    public void removeAnnotation(AnnotationDTO annotationDTO) throws CentrifugeException {
        removeAnnotation(annotationDTO.getVizUuid(), annotationDTO.getParentId());
    }

    public void removeAnnotation(String vizUuid, String itemKey) throws CentrifugeException {
        checkReadOnly(vizUuid);

        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
        Annotation foundAnnotation = null;
        List<Annotation> annotations = relGraphViewDef.getAnnotations();

        // check list for this annotation
        for (Annotation annotation : annotations) {

            if (annotation.getParentKey().equals(itemKey)) {

                foundAnnotation = annotation;
                break;
            }
        }

        annotations.remove(foundAnnotation);

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
//        AbstractGraphObjectStore store;
        Node node = graphContext.getNodeKeyIndex().get(itemKey);
//TODO: store is not used
        if (node == null) {
            Edge edge = graphContext.getEdgeKeyIndex().get(itemKey);
            /*store = */GraphManager.getEdgeDetails(edge);
        } else {
            /*store = */GraphManager.getNodeDetails(node);
        }
        CsiPersistenceManager.merge(relGraphViewDef);
    }

    private void removeAnnotations(RelGraphViewDef relGraphViewDef, List<String> keysToRemove) {
        List<Annotation> annotationsToRemove = new ArrayList<Annotation>();
        List<Annotation> annotations = relGraphViewDef.getAnnotations();

        // check list for this annotation
        for (Annotation annotation : annotations) {

            if (keysToRemove.contains(annotation.getParentKey())) {

                annotationsToRemove.add(annotation);

                GraphContext graphContext = GraphServiceUtil.getGraphContext(relGraphViewDef.getUuid());
//                AbstractGraphObjectStore store;
                Node node = graphContext.getNodeKeyIndex().get(annotation.getParentKey());
//TODO: store is not used
                if (node == null) {
                    Edge edge = graphContext.getEdgeKeyIndex().get(annotation.getParentKey());
                    /*store = */GraphManager.getEdgeDetails(edge);
                } else {
                    /*store = */GraphManager.getNodeDetails(node);
                }
            }
        }
        annotations.removeAll(annotationsToRemove);

    }

    @Override
    public GraphCachedState saveLegendCache(String vizUuid, List<String> itemOrderList, List<GraphNodeLegendItem> list,
                                            List<GraphLinkLegendItem> list2) throws CentrifugeException {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext != null) {
            RelGraphViewDef viewDef = graphContext.getVisualizationDef();
            if (!graphContext.isInvalidated() && (viewDef != null)) {
                GraphCachedState state = viewDef.getState();

                if (state == null) {
                    state = new GraphCachedState();
                    viewDef.setState(state);
                }

                if (state.getNodeLegendDecorations() == null) {

                    state.setNodeLegendDecorations(new LinkedHashMap<String, ArrayList>());
                }

                if (state.getLinkLegendDecorations() == null) {

                    state.setLinkLegendDecorations(new LinkedHashMap<String, ArrayList>());
                }

                state.setLegendOrder((ArrayList) itemOrderList);
                if (list != null) {
                    for (GraphNodeLegendItem item : list) {
                        ArrayList typeInfo = new ArrayList();
                        typeInfo.add(item.shape);
                        typeInfo.add(item.color);
                        state.getNodeLegendDecorations().put(item.key, typeInfo);
                    }
                }
                if (list2 != null) {
                    for (GraphLinkLegendItem item : list2) {
                        ArrayList typeInfo = new ArrayList();
                        typeInfo.add(ShapeType.NONE);
                        typeInfo.add(item.color);
                        state.getLinkLegendDecorations().put(item.key, typeInfo);
                    }
                }
                CsiPersistenceManager.merge(viewDef);
                return state;
            }
        }
        return null;
    }

    @Override
    public CsiPair<Boolean, Boolean> showLastLinkupHighlights(String vizUuid, String dvUuid) {

        CsiPair<Boolean, Boolean> pair = new CsiPair<Boolean, Boolean>(false, false);
        DataView dataview = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (!graphContext.isInvalidated()) {
            Connection conn = null;
            ResultSet rs = null;
            try {
                conn = CsiPersistenceManager.getCacheConnection();
                String[] linkupTableNames = dataview.getLinkups().split("\\|");
                String linkupRowStartQuery = DataCacheHelper.buildLinkupFirstRowQuery(linkupTableNames[linkupTableNames.length - 1]).toString();
                rs = QueryHelper.executeSingleQuery(conn, linkupRowStartQuery, null);
                Integer linkupRowId = null;
                if (rs.next()) {
                    linkupRowId = rs.getInt(1);
                } else {
                    return pair;
                }

                SelectionModel newNodes = graphContext.getSelection(GraphConstants.NEW_GENERATION);
                if (newNodes == null) {
                    newNodes = new SelectionModel();
                    graphContext.addSelectionModel(GraphConstants.NEW_GENERATION, newNodes);
                } else {
                    newNodes.clearSelection();
                }

                SelectionModel updatedNodes = graphContext.getSelection(GraphConstants.UPDATED_GENERATION);
                if (updatedNodes == null) {
                    updatedNodes = new SelectionModel();
                    graphContext.addSelectionModel(GraphConstants.UPDATED_GENERATION, updatedNodes);
                } else {
                    updatedNodes.clearSelection();
                }
                {

                    //Retrieve nodeIds that have a linkup rowId
                    Multimap<Integer, Integer> nodesByRow = graphContext.getNodesByRow();
                    Set<Integer> linkupNodes = new HashSet<Integer>();
                    for (Integer row : nodesByRow.keySet()) {
                        if (row >= linkupRowId) {
                           linkupNodes.addAll(nodesByRow.get(row));
                        }
                    }

                    Graph graph = graphContext.getGraphData();
                    Iterator<Node> nodes = graph.nodes();

                    //Go through nodes and find which ones are new vs in common
                    boolean isNew;

                    while (nodes.hasNext()) {
                        isNew = true;
                        Node node = nodes.next();
                        int id = node.getRow();
                        if (linkupNodes.contains(id)) {
                            NodeStore details = GraphManager.getNodeDetails(node);
                            Map<String, List<Integer>> rows = details.getRows();
                            Collection<List<Integer>> vals = rows.values();

                            for (List<Integer> list : vals) {

                                if ((list != null) && !list.isEmpty()) {
                                    if (list.get(0) < linkupRowId) {
                                        isNew = false;
                                        break;
                                    }
                                }
                            }

                            if (isNew) {
                                newNodes.nodes.add(id);
                            } else {
                                updatedNodes.nodes.add(id);
                            }
                        }

                    }
                }

                {
                    //Retrieve linkIds that have a linkup rowId
                    Multimap<Integer, Integer> linksByRow = graphContext.getLinksByRow();
                    Set<Integer> linkupLinks = new HashSet<Integer>();
                    for (Integer row : linksByRow.keySet()) {
                        if (row >= linkupRowId) {
                           linkupLinks.addAll(linksByRow.get(row));
                        }
                    }
                    Graph graph = graphContext.getGraphData();
                    Iterator<Edge> edges = graph.edges();

                    //Go through links and find which ones are new vs in common
                    boolean isNew;

                    while (edges.hasNext()) {
                        isNew = true;
                        Edge edge = edges.next();
                        int id = edge.getRow();
                        if (linkupLinks.contains(id)) {
                            LinkStore details = GraphManager.getEdgeDetails(edge);
                            Map<String, List<Integer>> rows = details.getRows();
                            Collection<List<Integer>> vals = rows.values();

                            for (List<Integer> list : vals) {

                                if ((list != null) && !list.isEmpty()) {
                                    if (list.get(0) < linkupRowId) {
                                        isNew = false;
                                        break;
                                    }
                                }
                            }

                            if (isNew) {
                                newNodes.links.add(id);
                            } else {
                                updatedNodes.links.add(id);
                            }
                        }

                    }
                }
                pair = new CsiPair(newNodes.isCleared(), updatedNodes.isCleared());


            } catch (Exception e) {
                LOG.error("Failed to highlight last linkup:", e);
                return pair;
            } finally {
                SqlUtil.quietCloseResulSet(rs);
                SqlUtil.quietCloseConnection(conn);
            }

        }
        return pair;
    }

    public SQLFactory getSqlFactory() {
        return sqlFactory;
    }

    public FilterActionsService getFilterActionsService() {
        return filterActionsService;
    }

    class PathPairs {

        int source;
        int target;

        public PathPairs(int source, int target) {
            this.source = source;
            this.target = target;
        }
    }

}

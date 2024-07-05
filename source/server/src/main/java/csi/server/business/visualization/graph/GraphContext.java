package csi.server.business.visualization.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.AbstractColumn;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.util.force.GravitationalForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.server.business.service.GraphNavigationObserver;
import csi.server.business.service.visualization.theme.ThemeManager;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.graph.data.GraphDataManager;
import csi.server.business.visualization.graph.grouping.UnGroupNodesCommand;
import csi.server.business.visualization.graph.layout.CsiForceDirectedLayout;
import csi.server.business.visualization.graph.layout.CsiRungeKuttaIntegrator;
import csi.server.business.visualization.graph.paths.Path;
import csi.server.business.visualization.graph.pattern.selection.PatternSelection;
import csi.server.business.visualization.graph.player.GraphPlayer;
import csi.server.business.visualization.graph.renderers.EdgeRenderer;
import csi.server.business.visualization.graph.renderers.NodeRenderer;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.GraphStateFlags;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.visualization.graph.GraphCachedState;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.GraphConstants.eLayoutAlgorithms;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;

/*
 * Notes:
 * resetIds seems to be the hook for post-processing after XStream. Their doc indicates that
 * they follow Java Serialization; but apparently they don't. Otherwise we could
 * hook into the Serializable.readObject & ObjectInputValidator.validateObject for de-serialization
 * processing/validation.
 */
public class GraphContext extends Observable {
   private static final Logger LOG = LogManager.getLogger(GraphContext.class);

    public static final String VISUAL_GRAPH = "visualGraph";
    public static final String ROOT_GRAPH_ID = "root.graph.id";
    public static final String IS_VISUALIZED = "isVisualized";
    private static final double MIN_AUTOFIT_DIMENSION = 300.0d;
    public static ThreadLocal<GraphContext> Current = new ThreadLocal<GraphContext>();
    /*
     * TODO: Longer term the graphData will be a facade around the persisted
     * graph in our cache; while the visibleGraph represents what is currently
     * rendered. Doesn't seem to be a clean path for avoiding duplicate Graph
     * objects currently. So make visibleGraph as light as possible.
     */
    private Graph graphData;

    private VisualGraph visualGraph;

    private Visualization visualization;

    private GraphTheme theme = null;

    private boolean muteTheme = false;

    private Display display;

    private Map<String, SelectionModel> selections;

    private String dvUuid;

    private String vizUuid;

    private boolean subnetsDirty;

    private Multimap<Integer, Integer> nodesByRow;

    private Multimap<Integer, Integer> linksByRow;

    // private Map<String, Node> keyIndex;

    private GraphPlayer player;
    private Collection<BufferedImage> layers;

    private ImageProvider imageProvider;

    private boolean invalidated = false;
    private boolean isPlayerRunning = false;
    private boolean hideNodeLabels = false;

    private List<Path> paths;

    private TypeInfo multiTypes;
    private TypeInfo linkMultiTypes;
    private List<PatternSelection> patternHighlights;
    private String themeUuid;

    public GraphContext(String dvUuid, String vizUuid, String themeUuid) {
        this.invalidated = false;
        this.dvUuid = dvUuid;
        this.vizUuid = vizUuid;
        this.themeUuid = themeUuid;
        boolean directed = false;
        graphData = new Graph(directed);
        subnetsDirty = true;

        this.imageProvider = new DefaultImageProvider(false);

        Table nodeTable = graphData.getNodeTable();
        nodeTable.addColumn(IS_VISUALIZED, boolean.class, Boolean.TRUE);
        nodeTable.addColumn(GraphConstants.NODE_DETAIL, NodeStore.class);
        nodeTable.addColumn(GraphConstants.DOC_ID, Object.class);

        Table edgeTable = graphData.getEdgeTable();
        edgeTable.addColumn(IS_VISUALIZED, boolean.class, Boolean.TRUE);
        edgeTable.addColumn(GraphConstants.LINK_DETAIL, LinkStore.class);
        edgeTable.addColumn(GraphConstants.DOC_ID, Object.class);

        final int nodeDetailColumn = graphData.getNodeTable().getColumnNumber(GraphConstants.NODE_DETAIL);
        final int linkDetailColumn = graphData.getEdgeTable().getColumnNumber(GraphConstants.LINK_DETAIL);

        final Map<String, Node> idNodeIndex = new HashMap<String, Node>();
        // keyIndex = idNodeIndex;
        HashMap<String, Edge> edgeMap = new HashMap<String, Edge>();
        final Map<String, TypeInfo> nodeLegendData = new HashMap<String, TypeInfo>();
        final Map<String, GraphLinkLegendItem> linkLegendData = new HashMap<String, GraphLinkLegendItem>();
        selections = Maps.newConcurrentMap();

        nodesByRow = HashMultimap.<Integer, Integer>create();
        linksByRow = HashMultimap.<Integer, Integer>create();

        graphData.putClientProperty(NodeStore.NODE_LEGEND_INFO, nodeLegendData);
        graphData.putClientProperty(LinkStore.LINK_LEGEND_INFO, linkLegendData);
        graphData.putClientProperty(GraphManager.NODE_HASH_TABLE, idNodeIndex);
        graphData.putClientProperty(GraphManager.EDGE_HASH_TABLE, edgeMap);
        graphData.putClientProperty(GraphManager.DV_UUID, dvUuid);
        graphData.putClientProperty(GraphManager.VIEWDEF_UUID, vizUuid);
        graphData.putClientProperty(GraphConstants.SELECTIONS, selections);
        graphData.putClientProperty(GraphManager.NODES_BY_INTERNAL_ROW, nodesByRow);
        graphData.putClientProperty(GraphManager.LINKS_BY_INTERNAL_ROW, linksByRow);

        graphData.addGraphModelListener(new GraphModelListener(nodeLegendData, nodeDetailColumn, idNodeIndex));
        graphData.addGraphModelListener(new GraphLinkListener(linkLegendData, linkDetailColumn, edgeMap));
        createVisualization();
        createDisplay();

//        RelGraphViewDef rgDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
//        String optionSetName = rgDef.getOptionSetName();
//        if (optionSetName == null) {
//            optionSetName = csi.config.Configuration.instance().getGraphConfig().getDefaultTheme();
//        }
//
//        setOptionSetName(optionSetName);
    }

    public GraphContext(String dvUuid, String vizUuid, Graph graph, Visualization viz, VisualGraph visGraph,
            Map<String, TypeInfo> types, Map<String, GraphLinkLegendItem> linkTypes,
            Map<String, SelectionModel> selections, Map<String, Node> nodeIndex, Map<String, Edge> edgeIndex,
            String optionSetName, Multimap<Integer, Integer> nodesByRow, Multimap<Integer, Integer> linksByRow) {

        this.dvUuid = dvUuid;
        this.vizUuid = vizUuid;
        this.selections = selections;
        this.graphData = graph;
        this.visualGraph = visGraph;
        this.visualization = viz;
        this.display = viz.getDisplay(0);
        this.subnetsDirty = true;
        this.nodesByRow = nodesByRow;
        this.linksByRow = linksByRow;

        graphData.putClientProperty(GraphManager.DV_UUID, dvUuid);
        graphData.putClientProperty(GraphManager.VIEWDEF_UUID, vizUuid);
        graphData.putClientProperty(GraphConstants.SELECTIONS, selections);
        graphData.putClientProperty(NodeStore.NODE_LEGEND_INFO, types);
        graphData.putClientProperty(LinkStore.LINK_LEGEND_INFO, linkTypes);
        graphData.putClientProperty(GraphManager.OPTION_SET_NAME, optionSetName);
        graphData.putClientProperty(VISUAL_GRAPH, visualGraph);
        graphData.putClientProperty(GraphManager.NODES_BY_INTERNAL_ROW, nodesByRow);
        graphData.putClientProperty(GraphManager.LINKS_BY_INTERNAL_ROW, linksByRow);

        Map<String, Node> keyIndex = (Map<String, Node>) graphData.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Map<String, Edge> linkIndex = (Map<String, Edge>) graphData.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        final int detailColumn = graphData.getNodeTable().getColumnNumber(GraphConstants.NODE_DETAIL);
        final int linkDetailColumn = graphData.getEdgeTable().getColumnNumber(GraphConstants.LINK_DETAIL);
        graphData.addGraphModelListener(new GraphModelListener(types, detailColumn, keyIndex));
        graphData.addGraphModelListener(new GraphLinkListener(linkTypes, linkDetailColumn, linkIndex));

        graphData.addPropertyChangeListener(GraphConstants.STORAGE, new PropertyCopyListener(visualGraph));
    }

    public static Map<String, Object> buildAttributeMap(NodeDef nodeDef) {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<AttributeDef> attrs = nodeDef.getAttributeDefs();
        for (AttributeDef attr : attrs) {
            AttributeKind kind = attr.getKind();
            FieldDef fieldDef = attr.getFieldDef();
            if (fieldDef != null) {
                if ((kind == AttributeKind.STATIC) || (fieldDef.getFieldType() == FieldType.STATIC)) {
                    String value = fieldDef.getStaticText();
                    map.put(attr.getName(), value);
                }
            }
        }
        return map;
    }

    public static Map<String, Object> buildAttributeMap(LinkDef linkDef) {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<AttributeDef> attrs = linkDef.getAttributeDefs();
        for (AttributeDef attr : attrs) {
            AttributeKind kind = attr.getKind();
            FieldDef fieldDef = attr.getFieldDef();
            if ((kind == AttributeKind.STATIC) || ((fieldDef != null) && (fieldDef.getFieldType() == FieldType.STATIC))) {
                String value = fieldDef.getStaticText();
                map.put(attr.getName(), value);
            }
        }
        return map;

    }

    public static Map<String, TypeInfo> getNodeLegend(Graph graph) {
        return (Map<String, TypeInfo>) graph.getClientProperty(NodeStore.NODE_LEGEND_INFO);
    }

    public static void setNodeLegend(Graph graph, Map legend) {
        graph.putClientProperty(NodeStore.NODE_LEGEND_INFO, legend);
    }

    /**
     * Update the Legend's link entries with pre-canned types based off of the link definitions
     * <p>
     *     If these result in a zero count, they should be pruned after processing the links.
     * </p>
     */
    public void bootStrapLinkLegend() {

        RelGraphViewDef rgDef = getVisualizationDef();
        GraphTheme theme = getTheme();

        GraphManager GM = GraphManager.getInstance();
        Map<String, Object> empty = Collections.emptyMap();
        Map<String, GraphLinkLegendItem> linkLegend = getLinkLegend();
        List<LinkDef> linkDefs = rgDef.getLinkDefs();
        HashMap<String, ArrayList> linkLegendDecorations;
        if(rgDef.getState() == null) {
            rgDef.setState(new GraphCachedState());
        }
        if(rgDef.getState().getLinkLegendDecorations() == null) {
            linkLegendDecorations = new LinkedHashMap<String, ArrayList>();
            rgDef.getState().setLinkLegendDecorations(linkLegendDecorations);
        } else {
            linkLegendDecorations = rgDef.getState().getLinkLegendDecorations();
        }

        for (LinkDef linkDef : linkDefs) {
            String name = linkDef.getName();
            if (linkLegend.containsKey(name)) {
                continue;
            }
            Object o = GM.getLinkType(linkDef, empty);
            String linkType = ((o == null) || o.equals("")) ? "Link" : o.toString();

            Map<String, Object> attrs = buildAttributeMap(linkDef);

            //Need this or else non-edited links are always dynamic
            boolean isDynamic = false;
            if ((o != null) && !o.equals("")) {
                GraphManager.isDynamicType(linkDef);
            }

            if(linkLegend.containsKey(linkType)){
                continue;
            }



            GraphLinkLegendItem legendEntry = GraphDataManager.initializeLinkLegendItem(linkType, attrs, isDynamic, theme);
            if(linkLegendDecorations.containsKey(linkType)) {
                legendEntry.color = (Integer) linkLegendDecorations.get(linkType).get(1);
                //typeInfo.shape = ShapeType.valueOf((String) (legendDecorations.get(typeInfo.key)).get(0));
            }
            linkLegend.put(linkType, legendEntry);
        }

    }

    /**
     * Update the Legend's entries with theme colors, if applicable
     */
   public void applyLinkThemeToLegend() {
      GraphTheme theme = getTheme();

      if (theme != null){
         for (Map.Entry<String,GraphLinkLegendItem> entry : getLinkLegend().entrySet()) {
            String linkType = StringUtils.isEmpty(entry.getKey()) ? "Link" : entry.getKey();
            GraphLinkLegendItem linkLegendItem = entry.getValue();
            LinkStyle style = null;

            if ((linkLegendItem != null) && !linkLegendItem.colorOverride) {
               style = theme.findLinkStyle(linkType);

               if (style != null) {
                  linkLegendItem.color = style.getColor().longValue();
               }
            }
         }
      }
   }

    /**
     * Update the Legend's node entries with pre-canned values based off of the
     * node definitions in the provided RG definition.
     * <p>
     *     The caller should prune any entries after processing all nodes where the
     *     total count is zero.
     * </p>
     */
    public void bootStrapNodeLegend() {
        Map<String, TypeInfo> nodeLegend = getNodeLegend();
        RelGraphViewDef rgDef = getVisualizationDef();
        HashMap<String, ArrayList> legendDecorations;
//        LinkedHashMap<String, ArrayList> linkLegendDecorations;


        if(rgDef.getState() == null) {
            rgDef.setState(new GraphCachedState());
        }

        if(rgDef.getState().getNodeLegendDecorations() == null) {
            legendDecorations = new LinkedHashMap<String, ArrayList>();
            rgDef.getState().setNodeLegendDecorations(legendDecorations);
        } else {
            legendDecorations = rgDef.getState().getNodeLegendDecorations();
        }
//
//        if(rgDef.getState().getLinkLegendDecorations() == null) {
//            linkLegendDecorations = new LinkedHashMap<String, ArrayList>();
//            rgDef.getState().setLinkLegendDecorations(legendDecorations);
//        } else {
//            linkLegendDecorations = rgDef.getState().getLinkLegendDecorations();
//        }

        List<NodeDef> nodeDefs = rgDef.getNodeDefs();
        Map<String, Object> empty = Collections.emptyMap();
        for (NodeDef nodeDef : nodeDefs) {
            String name = nodeDef.getName();
            if (nodeLegend.containsKey(name)) {
                continue;
            }

            Object nodeType = GraphManager.getNodeType(nodeDef, empty);
            Map<String, Object> attrs = buildAttributeMap(nodeDef);
            boolean isDynamic = GraphManager.isDynamicType(nodeDef);
            TypeInfo typeInfo = TypeInfo.initializeTypeInfo(nodeType.toString(), attrs, isDynamic, false, theme);


            if(legendDecorations.containsKey(typeInfo.key)) {
                typeInfo.color = (Integer) (legendDecorations.get(typeInfo.key)).get(1);
                typeInfo.shape = ShapeType.valueOf((String) (legendDecorations.get(typeInfo.key)).get(0));
            }
            nodeLegend.put(typeInfo.name, typeInfo);
        }

        CsiPersistenceManager.merge(rgDef);

        multiTypes = new TypeInfo();
        multiTypes.name = "Multi-typed Nodes";
        multiTypes.key = GraphConstants.MultiTypeKey;
        multiTypes.totalCount = 0;
        multiTypes.visible = 0;


        linkMultiTypes = new TypeInfo();
        linkMultiTypes.name = "Multi-typed Links";
        linkMultiTypes.key = GraphConstants.MultiTypeLinkKey;
        linkMultiTypes.totalCount = 0;
        linkMultiTypes.visible = 0;
        //        nodeLegend.put(GraphConstants.MultiTypeKey, multiTypes );
    }

    public TypeInfo getMultiTypeNodeLegendEntry() {
        return multiTypes;
    }

    public TypeInfo getMultiTypeLinkLegendEntry() {
        return linkMultiTypes;
    }

    /**
     * Remove all entries from the legend that have a total count of zero.
     */
    public void pruneEmptyLegendEntries() {
        pruneEmptyNodeLegendEntries();
        pruneEmptyLinkLegendEntries();
    }

    /*
     * Optimization: right now the conditional is if totalCount ==0.  If for
     * so reason this needs to be changed, then we need to introduce a new
     * Function parameter, so that we can pass in the predicate to test
     */
    public void pruneEmptyLinkLegendEntries() {
        Map<String, GraphLinkLegendItem> linkLegend = getLinkLegend();
        Iterator<Entry<String, GraphLinkLegendItem>> li = linkLegend.entrySet().iterator();
        while (li.hasNext()) {
            Entry<String, GraphLinkLegendItem> entry = li.next();
            GraphLinkLegendItem item = entry.getValue();
            if ((item == null) || (item.totalCount == 0)) {
                li.remove();
            }
        }
    }

    /*
     * Optimization: right now the conditional is if totalCount ==0.  If for
     * so reason this needs to be changed, then we need to introduce a new
     * Function parameter, so that we can pass in the predicate to test
     */
    public void pruneEmptyNodeLegendEntries() {
        Map<String, TypeInfo> nodeLegend = getNodeLegend();
        Iterator<Entry<String, TypeInfo>> ni = nodeLegend.entrySet().iterator();
        while (ni.hasNext()) {
            Entry<String, TypeInfo> entry = ni.next();
            TypeInfo item = entry.getValue();
            if ((item == null) || (item.totalCount == 0)) {
                ni.remove();
            }
        }
    }

    private void createVisualization() {
        visualization = new Visualization();
        Renderer nr = new NodeRenderer();
        // Renderer nr = new CompositeRenderer();
        EdgeRenderer er = new EdgeRenderer();
        DefaultRendererFactory rendererFactory = new DefaultRendererFactory(nr, er);

        visualization.setRendererFactory(rendererFactory);

        // associate the VisualGraph with our tracking of the visibleGraph.

        visualGraph = visualization.addGraph("graph", graphData);
        Table vnt = visualGraph.getNodeTable();
        AbstractColumn column = (AbstractColumn) vnt.getColumn(VisualItem.VISIBLE);
        column.setDefaultValue(false);

        column = (AbstractColumn) visualGraph.getEdgeTable().getColumn(VisualItem.VISIBLE);
        column.setDefaultValue(false);

        graphData.putClientProperty(VISUAL_GRAPH, visualGraph);

        graphData.addPropertyChangeListener(GraphConstants.STORAGE, new PropertyCopyListener(visualGraph));
    }

    private void createDisplay() {
        display = new Display(visualization);
        display.setBackground(Color.WHITE);
        display.setHighQuality(true);
        Dimension viewDimension = new Dimension(600, 400);
        display.setSize(viewDimension);
    }

    public void cleanup() {
        display = null;
        visualization = null;
        selections = null;
        graphData = null;

    }

    public Graph getGraphData() {
        return graphData;
    }

    public void setGraphData(Graph graphData) {
        this.graphData = graphData;

        // HACK: temporary
        Map<String, TypeInfo> legendData = (Map<String, TypeInfo>) graphData
                .getClientProperty(NodeStore.NODE_LEGEND_INFO);

        Map<String, TypeInfo> visibleLegendData = new HashMap<String, TypeInfo>();
        for (Entry<String, TypeInfo> entry : legendData.entrySet()) {
            TypeInfo ti = entry.getValue();
            TypeInfo vti = new TypeInfo();
            vti.copyFrom(ti);
            vti.totalCount = 0;
            visibleLegendData.put(vti.name, vti);
        }

        graphData.putClientProperty(NodeStore.NODE_LEGEND_INFO, visibleLegendData);

        createVisualization();
        createDisplay();
        createSelections();
    }

    public VisualGraph getVisualGraph() {
        return visualGraph;
    }

   public void validateSelection(String name) {
      SelectionModel selection = getSelection(name);

      if (selection != null) {
         Graph graph = graphData;

         pruneInvalid(graph.getNodeTable(), selection.nodes);
         pruneInvalid(graph.getEdgeTable(), selection.links);
      }
   }

   private static void pruneInvalid(Table table, Collection<Integer> source) {
      if ((table != null) && (source != null)) {
         Set<Integer> workingCopy = new HashSet<Integer>();

         workingCopy.addAll(source);

         for (Integer id : workingCopy) {
            if (!table.isValidRow(id)) {
               source.remove(id);
            }
         }
      }
   }

    private void createSelections() {
        if (graphData != null) {
            selections = new HashMap<String, SelectionModel>();
            graphData.putClientProperty("selections", selections);
        }
    }

    public Graph getVisibleGraph() {
        return graphData;
    }

    public Visualization getVisualization() {
        return visualization;
    }

    public Display getDisplay() {
        return visualization.getDisplay(0);
    }

    public String getDvUuid() {
        return dvUuid;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public boolean isSubnetsDirty() {
        return subnetsDirty;
    }

    public void setSubnetsDirty(boolean value) {
        this.subnetsDirty = value;
    }

    /**
     * Helper methods for isolating the arbitrariness of client properties on
     * the graph
     */

    public void addSelectionModel(String name, SelectionModel model) {
        if (selections != null) {
            selections.put(name, model);
        }
    }

    public SelectionModel getSelection(String name) {
        if (selections != null) {
            SelectionModel selectionModel = selections.get(name);
            if (selectionModel != null) {
                return selectionModel;
            }
            selectionModel = new SelectionModel();
            selections.put(name, selectionModel);
            return selectionModel;
        }
        return null;
    }

    public void removeSelectionModel(String name) {
        if (selections != null) {
            selections.remove(name);
        }
    }

    public SelectionModel getOrCreateSelection(String name) {
        SelectionModel selection = getSelection(name);
        if (selection == null) {
            selection = new SelectionModel();
            selections.put(name, selection);
        }

        return selection;
    }

    public RelGraphViewDef getVisualizationDef() {
        return CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
    }

    public GraphTheme getTheme() {
        if(muteTheme){
            return theme;
        }
        if((theme == null) || ((themeUuid != null) && !themeUuid.equals(theme.getUuid()))){
            theme = ThemeManager.getGraphTheme(themeUuid);
        }

        muteTheme = true;
        return theme;
    }

    /**
     * Add the provided nodes and edges into the visible graph.
     * <p>
     * NB: These nodes are the ones from the graphData aka root graph.
     *
     * @param nodes
     */
    public void showItems(Iterable<Node> nodes) {
        showItems(nodes, true);
    }

    /*
     * Mark the items as visualized, but honor the node's hidden state.
     */
    public void visualizeItems(Collection<Node> initialNodes, boolean doLayout) {

        for (Node node : initialNodes) {

            NodeItem ni = (NodeItem) visualGraph.getNode(node.getRow());
            ni.setBoolean(IS_VISUALIZED, true);
            NodeStore details = Functions.GetNodeDetails.apply(node);

            ni.setVisible(!details.isHidden());
        }

    }

    public void showItems(Iterable<Node> nodes, boolean doLayout) {

        // int nodeCount = visualGraph.getNodeCount();
        int nodeCount = 0;

        for (Iterator<Node> graphNodes = visualGraph.nodes(); graphNodes.hasNext();) {
           if (Predicates.IsVisualizedAndDisplayable.test(graphNodes.next())) {
              nodeCount++;
           }
        }
        boolean requireAutoSize = (nodeCount == 0);

        if (doLayout) {
            Iterator<VisualItem> vnodes = visualGraph.nodes();
            while (vnodes.hasNext()) {
                TaskHelper.checkForCancel();

                VisualItem item = vnodes.next();
                if (item.getBoolean(IS_VISUALIZED)) {
                    item.setFixed(true);
                }
            }
        }

        Collection<Edge> checkEdges = new HashSet<Edge>();
        for (Node node : nodes) {
            TaskHelper.checkForCancel();
            if (!isVisualized(node) || !isNodeVisible(node)) {
                subnetsDirty = true;
            }
            showNode(node);

            for (Iterator<Edge> edges = node.edges(); edges.hasNext();) {
               checkEdges.add(edges.next());
            }
        }

        // Post-processing. We've tracked the set of edges representative of the nodes
        // we're adding. For each edge add it to the visual graph if the endpoints are
        // visualized...of course accounting for the fact that it might already
        // be added.
        for (Edge edge : checkEdges) {
            TaskHelper.checkForCancel();

            Node sourceNode = edge.getSourceNode();
            Node targetNode = edge.getTargetNode();
            if (isVisualized(sourceNode) && isVisualized(targetNode) && isNodeVisible(sourceNode)
                    && isNodeVisible(targetNode)) {
                showEdge(edge);
                subnetsDirty = true;
            }

        }

        if (requireAutoSize) {
            TaskHelper.checkForCancel();

            GraphManager manager = GraphManager.getInstance();
            manager.computeComponents(graphData);
            manager.computeComponentRegions(this);
            manager.runPlacement(graphData, eLayoutAlgorithms.forceDirected);

            fitToSize();

            Rectangle2D bounds = visualization.getBounds("graph");
            computeSoftMinimumBounds(bounds);
            fitToRegion(bounds);
        } else if (doLayout) {
            TaskHelper.checkForCancel();

            CsiForceDirectedLayout layout = new CsiForceDirectedLayout("graph", false, true);
            layout.getForceSimulator().addForce(new GravitationalForce());
            layout.setVisualization(getVisualization());
            layout.setIterations(50);
            layout.getForceSimulator().setIntegrator(new CsiRungeKuttaIntegrator());
            layout.setLayoutBounds(new Rectangle2D.Double(0, 0, 10000, 10000));
            layout.setLayoutAnchor(new Point2D.Double(5000, 5000));
            layout.run(0.0);

            Iterator<VisualItem> vnodes = visualGraph.nodes();
            while (vnodes.hasNext()) {
                TaskHelper.checkForCancel();

                VisualItem item = vnodes.next();
                item.setFixed(false);
            }

            // FIXME: need to recompute components?
        }

        updateVisibleNodeLegend();
        updateVisibleLinkLegend();
    }

    private boolean isNodeVisible(Node node) {
        NodeStore details = Functions.GetNodeDetails.apply(node);
        boolean results = !details.isHidden();
        return results;
    }

    private boolean isVisualized(Tuple tuple) {
        boolean results = tuple.getBoolean(IS_VISUALIZED);

        return results;
    }

    public void showNodes(Iterable<Node> nodes) {
        showItems(nodes);
    }

    public void showNodes(Iterator<Node> nodes) {
        Collection<Node> c = new ArrayList<Node>();

        for (Iterator<Node> nodeIterator = nodes; nodeIterator.hasNext();) {
           c.add(nodeIterator.next());
        }
        showItems(c);
    }

   private void showNode(Node node) {
      NodeStore details = GraphManager.getNodeDetails(node);

      if ((details != null) && !details.isBundled()) {
         node.setBoolean(IS_VISUALIZED, true);

         details.setHidden(false);
         details.setVisualized(true);

         NodeItem ni = (NodeItem) visualGraph.getNode(node.getRow());

         ni.setVisible(!details.isHidden());
         ni.setSize(details.getRelativeSize());
         // details.setHidden( false );
      }
   }

    private void showEdge(Edge edge) {
        EdgeItem ve = (EdgeItem) visualGraph.getEdge(edge.getRow());
        ve.setBoolean(IS_VISUALIZED, true);

        LinkStore details = GraphManager.getEdgeDetails(edge);
        if ((details != null) && details.isDisplayable()) {
            ve.setVisible(true);
            details.setVisualized(true);
            ve.setSize(details.getWidth());
        }
    }

    private Node getVisualizedNode(Node node) {
        return node;

    }

    /**
     * Represents nodes and edges in the visibleGraph.
     *
     * FYI -- edges not used right now.
     *
     * @param nodes
     * @param edges
     */
    public void retractVisualItems(Collection<Integer> nodes, Collection<Integer> edges) {

        Graph graph = graphData;

        Table nodeTable = graphData.getNodeTable();

        SelectionModel selection = getSelection(GraphManager.DEFAULT_SELECTION);
        if (selection == null) {
            selection = new SelectionModel();
        }

        // CTWO-4714: any retracted node is removed from the selection model as well.

        for (Integer id : nodes) {
            // make sure we're actually displaying the requested node
            if (!nodeTable.isValidRow(id)) {
                continue;
            }
            Node dataNode = graph.getNode(id);
            retractNode(dataNode);
            // Node vnode = graphData.getNode( id );
            // int r_id = vnode.getInt( ROOT_GRAPH_ID );
            //
            //
            // if( r_id != -1 && graphDataNT.isValidRow( r_id ) ) {
            // Node dataNode = graphData.getNode( r_id );
            // } else {
            // // handle Visual only instances...
            // visibleGraph.removeNode( vnode );
            // }
        }

        if (LOG.isDebugEnabled()) {
           LOG.debug("Removing nodes from default selection.");
        }

        selection.nodes.removeAll(nodes);
        selection.links.removeAll(edges);

        subnetsDirty = true;

        updateVisibleNodeLegend();
        updateVisibleLinkLegend();

    }

    public void retractDataItems(Collection<Integer> nodes, Collection<Integer> edges) {
        for (Integer id : nodes) {
            Node node = graphData.getNode(id);
            retractNode(node);
        }
    }

    private void retractNode(Node dataNode) {

        dataNode.setBoolean(IS_VISUALIZED, false);
        NodeItem ni = (NodeItem) visualGraph.getNode(dataNode.getRow());
        ni.setVisible(false);

        NodeStore details = Functions.GetNodeDetails.apply(dataNode);
        details.setHidden(true);
        details.setVisualized(false);

        if (LOG.isDebugEnabled()) {
           LOG.debug("Removed node(" + dataNode.getRow() + ") from graph canvas.");
        }

        Iterator<EdgeItem> edges = ni.edges();
        while (edges.hasNext()) {
            EdgeItem ei = edges.next();
            ei.setBoolean(IS_VISUALIZED, false);
            GraphManager.getEdgeDetails(ei).setVisualized(false);
            ei.setVisible(false);
        }

        if (details.isBundle()) {
            Collection<Node> children = new ArrayList<Node>();
            BundleUtil.findChildNodes(dataNode, children);

            try {
                UnGroupNodesCommand ungroup = new UnGroupNodesCommand();
                ungroup.setGraph(getVisibleGraph());
                ungroup.setNodes(Collections.singletonList(dataNode));
                ungroup.call();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            for (Node node : children) {
                retractNode(node);
            }

        }
    }

    public Map<String, TypeInfo> getNodeLegend() {
        return (Map<String, TypeInfo>) graphData.getClientProperty(NodeStore.NODE_LEGEND_INFO);
    }

    public Map<String, GraphLinkLegendItem> getLinkLegend() {
        return (Map<String, GraphLinkLegendItem>) graphData.getClientProperty(LinkStore.LINK_LEGEND_INFO);
    }

    public void initLinkLegend() {
        if (getLinkLegend() == null) {
            final Map<String, GraphLinkLegendItem> linkLegendData = new HashMap<String, GraphLinkLegendItem>();
            graphData.putClientProperty(LinkStore.LINK_LEGEND_INFO, linkLegendData);
        }
    }

//    public OptionSet getOptionSet() {
//        String optionName = getOptionSetName();
//        if (optionName == null) {
//            return null;
//        }
//
//        try {
//            return OptionSetManager.getOptionSet(optionName);
//        } catch (CentrifugeException e) {
//            log.warn("Error retrieving option set: " + optionName);
//            return null;
//        }
//    }

    public String getOptionSetName() {
        return (String) graphData.getClientProperty(GraphManager.OPTION_SET_NAME);
    }

    public void setOptionSetName(String name) {
        if (name != null) {
            graphData.putClientProperty(GraphManager.OPTION_SET_NAME, name);
        }
    }

    public void retractVisualItems(SelectionModel selection) {

        ArrayList<Integer> nodeCopy = new ArrayList<Integer>(selection.nodes);
        ArrayList<Integer> linkCopy = new ArrayList<Integer>(selection.links);
        retractVisualItems(nodeCopy, linkCopy);
    }

    public Multimap<Integer, Integer> getNodesByRow() {
        return nodesByRow;
    }

    public void setNodesByRow(Multimap<Integer, Integer> nodesByRow) {
        this.nodesByRow = nodesByRow;
    }

    public Multimap<Integer, Integer> getLinksByRow() {
        return linksByRow;
    }

    public void setLinksByRow(Multimap<Integer, Integer> linksByRow) {
        this.linksByRow = linksByRow;
    }

    public void initializeRowMaps() {
        Graph graph = getGraphData();
        setNodesByRow(HashMultimap.<Integer, Integer>create());
        setLinksByRow(HashMultimap.<Integer, Integer>create());
        graph.putClientProperty(GraphManager.NODES_BY_INTERNAL_ROW, getNodesByRow());
        graph.putClientProperty(GraphManager.LINKS_BY_INTERNAL_ROW, getLinksByRow());
    }

    public List<PatternSelection> getPatternHighlights() {
        return patternHighlights;
    }

    public void setPatternHighlights(List<PatternSelection> patternHighlights) {
        this.patternHighlights = patternHighlights;
    }

    /*
     * reset legend counts and recalculate.
     */
    public void updateVisibleNodeLegend() {
        Map<String, TypeInfo> legendData = getNodeLegend();
        // CTWO-4678 -- ensure we reset both counts!
        List<String> oldKeys = new ArrayList<String>();

        for (Map.Entry<String, TypeInfo> entry : legendData.entrySet()) {
            TypeInfo info = entry.getValue();

            if (!info.key.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                info.totalCount = 0;
                info.visible = 0;
            } else {
                oldKeys.add(entry.getKey());
            }
        }

        for (String oldKey : oldKeys) {
            legendData.remove(oldKey);
        }

        // ctwo-7259  in some instances we may have an empty legend that
        // we need to rebuild.  prime the legend with 'types' corresponding
        // to node defs
        bootStrapNodeLegend();

        TypeInfo multiTypes = getMultiTypeNodeLegendEntry();

        for (Iterator<Node> nodes = graphData.nodes(); nodes.hasNext();) {
           Node node = nodes.next();

           if (Predicates.IsNodeVisualized.test(node)) {
              TaskHelper.checkForCancel();

              if (node.getBoolean(IS_VISUALIZED)) {
                 NodeStore details = GraphManager.getNodeDetails(node);
                 boolean visible = (!details.isHidden() && !details.isBundled());

                 if (details.isBundle()) {
                    incrementNodeLegendCount(legendData, GraphConstants.BUNDLED_NODES, visible, true);
                 } else {
                    Map<String, Integer> types = details.getTypes();
                    int typeCount = 0;

                    for (String type : types.keySet()) {
                       typeCount++;
                       incrementNodeLegendCount(legendData, type, visible, false);
                    }
                    if (typeCount > 1) {
                       multiTypes.totalCount++;

                       if (visible) {
                          multiTypes.visible++;
                       }
                    }
                 }
              }
           }
        }
        pruneEmptyNodeLegendEntries();
    }

    /*
     * reset legend counts and recalculate.
     */
    public void updateVisibleLinkLegend() {
        Map<String,GraphLinkLegendItem> legendData = getLinkLegend();
        if (legendData != null) {
            List<String> oldKeys = new ArrayList<String>();
            // ensure we reset both counts!
            for (Map.Entry<String,GraphLinkLegendItem> entry : legendData.entrySet()) {
                GraphLinkLegendItem info = entry.getValue();

                if (!info.key.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
                    info.totalCount = 0;
                    info.count = 0;
                } else {
                    oldKeys.add(entry.getKey());
                }
            }

            for (String oldKey : oldKeys) {
                legendData.remove(oldKey);
            }

            // ctwo-7259  in some instances we may have an empty legend that
            // we need to rebuild.  prime the legend with 'types' corresponding
            // to node defs
            //bootStrapLinkLegend();

            Iterator<Edge> edges = graphData.edges();
            TaskHelper.checkForCancel();
            while (edges.hasNext()) {
                Edge edge = edges.next();
                boolean rendered = edge.getBoolean(IS_VISUALIZED);

                if (!rendered) {
                    continue;
                }

                LinkStore details = GraphManager.getEdgeDetails(edge);
                NodeStore sourceDetails = GraphManager.getNodeDetails(edge.getSourceNode());
                NodeStore targetDetails = GraphManager.getNodeDetails(edge.getTargetNode());

                // The link might be hidden, or one of the source or target nodes might be hidden.
                boolean visible = !details.isHidden() && !sourceDetails.isHidden() && !targetDetails.isHidden();

                // If either end is a bundle, treat this as though it is a bundled link.
                if (sourceDetails.isBundle() || targetDetails.isBundle()) {
                    // If neither end is bundled, this is currently a top-level link and should be counted.
                    // If one end is bundled, this link is inside another higher-level link and should be ignored.
                    if (!targetDetails.isBundled() && !sourceDetails.isBundled()) {
                        incrementLinkLegendCount(legendData, GraphConstants.BUNDLED_LINKS, visible, true);
                    }
                } else {
                    // If one end is a bundle or one end is bundled, then this isn't visible.
                    if (sourceDetails.isBundled() || targetDetails.isBundled() || sourceDetails.isBundle()
                            || targetDetails.isBundle()) {
                        visible = false;
                    }

                    Map<String, Integer> types = details.getTypes();
                    int typeCount = 0;
                    for (String type : types.keySet()) {
                        typeCount++;
                        incrementLinkLegendCount(legendData, type, visible, false);
                    }

                    if (typeCount > 1) {
                        linkMultiTypes.totalCount++;
                        if (visible) {
                            linkMultiTypes.visible++;
                        }
                    }
                }
            }

            pruneEmptyLegendEntries();
        }
    }

    private void incrementNodeLegendCount(Map<String, TypeInfo> map, String type, boolean visible, boolean internalType) {
        String mapKey = type;
        if (internalType) {
            mapKey = GraphConstants.CSI_INTERNAL_NAMESPACE + "." + type;
        }

        TypeInfo info = map.get(mapKey);

        if (info == null) {
            info = TypeInfo.createSimpleTypeInfo(type, mapKey);
            map.put(mapKey, info);
        }

        info.totalCount++;

        if (visible) {
            info.visible++;
        }

    }

    private void incrementLinkLegendCount(Map<String, GraphLinkLegendItem> map, String type, boolean visible,
            boolean internalType) {

        String mapKey = type;
        if (internalType) {
            mapKey = GraphConstants.CSI_INTERNAL_NAMESPACE + "." + type;
        }

        GraphLinkLegendItem item = map.get(mapKey);

        if (item == null) {
            item = new GraphLinkLegendItem();
            item.totalCount = 0;
            item.count = 0;
            item.key = mapKey;
            item.typeName = type;
            map.put(mapKey, item);
        }

        item.totalCount++;

        if (visible) {
            item.count++;
        }
    }

    public Rectangle getPatchBounds() {
        Rectangle region = (Rectangle) graphData.getClientProperty(GraphConstants.PATCH_BOUNDS);
        return region;
    }

    public boolean hasType(Node node, String type) {
        NodeStore details = GraphManager.getNodeDetails(node);
        Map<String, Integer> types = details.getTypes();
        Set<String> typeNames = types.keySet();

        return typeNames.contains(type);
    }

   public void updateVisualProperties(RelGraphViewDef rgDef) {
      GenericProperties settings = rgDef.getSettings();

      if (settings != null) {
         Map<String, String> propertiesMap = settings.getPropertiesMap();

         if (propertiesMap != null) {
            String value = propertiesMap.get("csi.relgraph.backgroundColor");

            if (value != null) {
               try {
                  int color = Integer.parseInt(value);

                  if (color == 16777215) { //TODO:looks like this matters
                     color = 16711422;
                  }
                  Color c = new Color(color);
                  display.setBackground(c);
               } catch (NumberFormatException e) {
                  LOG.debug("Invalid value provided for background color, using default value");
               }
            }
         }
      }
   }

    public boolean isInvalidated() {
        return invalidated;
    }

    public void setInvalidated(boolean invalidated) {
        this.invalidated = invalidated;
    }

    public int getVisibleNodeCount() {

        @SuppressWarnings("unchecked")
        Iterator<Node> nodes = visualGraph.nodes();
        int count = 0;

        while (nodes.hasNext()) {
            Node node = nodes.next();

            if (node.getBoolean(IS_VISUALIZED) && Predicates.IsNodeDisplayable.test(node)) {
                count++;
            }
        }

        return count;

    }

    public int getVisibleDisplayCount() {
        int visibleItemCount = display.getVisibleItemCount();
        return visibleItemCount;
    }

    public Node getNodeFromDetails(NodeStore child) {
        if (child == null) {
            throw new IllegalArgumentException();
        }
        Node node = getNodeKeyIndex().get(child.getKey());
        return node;
    }

    public Map<String, Node> getNodeKeyIndex() {
        return (Map<String, Node>) graphData.getClientProperty(GraphManager.NODE_HASH_TABLE);
    }

    public Map<String, Edge> getEdgeKeyIndex() {
        Map<String, Edge> linkIndex = (Map<String, Edge>) graphData.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        return linkIndex;
    }

    public void computeSoftMinimumBounds(Rectangle2D bounds) {

        double min = Math.min(bounds.getWidth(), bounds.getHeight());
        if (min < 100) {
            // we want the size to be at least 300 pixels -- determine the
            // scaling to get to that size...

            double scaling = MIN_AUTOFIT_DIMENSION / min;
            double cx = bounds.getCenterX();
            double cy = bounds.getCenterY();

            double rx = (bounds.getWidth() / 2.0d) * scaling;
            double ry = (bounds.getHeight() / 2.0d) * scaling;

            if (Double.isNaN(rx) || Double.isNaN(ry)) {
                rx = 100.0d;
                ry = 100.0d;
            }

            bounds.setFrameFromCenter(cx, cy, (cx + rx), (cy + ry));
        }

    }

    public void applySoftBounds() {
        Rectangle2D bounds = visualization.getBounds("graph");
        computeSoftMinimumBounds(bounds);

        fitToRegion(bounds);
    }

   public void showNode(Integer id) {
      NodeItem node = (NodeItem) visualGraph.getNode(id);

      if (node == null) {
         LOG.trace("Show for non-existent node.");
      } else {
         NodeStore details = Functions.GetNodeDetails.apply(node);

         details.setHidden(false);

         if (!details.isBundled()) {
            node.setVisible(true);
         }
         Iterator<Edge> edges = node.edges();

         while (edges.hasNext()) {
            Edge edge = edges.next();
            // Use internal call so that we can maintain the state
            // of the edge's hidden nature -- i.e. if a edge
            // was explicitly hidden, we don't automatically render it.
            internal_ShowLink(edge.getRow());
         }
         setSubnetsDirty(true);
      }
   }

    private void internal_ShowLink(Integer id) {
        EdgeItem edge = (EdgeItem) visualGraph.getEdge(id);
        LinkStore details = Functions.GetLinkDetails.apply(edge);

        edge.setVisible(details.isDisplayable());
        setSubnetsDirty(details.isDisplayable());
    }

   public void hideNode(Integer id) {
      NodeItem node = (NodeItem) visualGraph.getNode(id);

      if (node == null) {
         LOG.trace("Hide for non-existent node.");
      } else {
         node.setVisible(false);

         NodeStore details = Functions.GetNodeDetails.apply(node);

         details.setHidden(true);

         Iterator<EdgeItem> edges = node.edges();

         while (edges.hasNext()) {
            EdgeItem edge = edges.next();
            edge.setVisible(false);
            // hideLink( edge.getRow() );
         }
         setSubnetsDirty(true);
      }
   }

    public void showLink(Integer id) {
        EdgeItem edge = (EdgeItem) visualGraph.getEdge(id);
        edge.setVisible(true);

        LinkStore details = Functions.GetLinkDetails.apply(edge);
        details.setHidden(false);

        setSubnetsDirty(true);

    }

    public void hideLink(Integer id) {
        EdgeItem edge = (EdgeItem) visualGraph.getEdge(id);

        edge.setVisible(false);
        LinkStore details = Functions.GetLinkDetails.apply(edge);
        details.setHidden(true);

        setSubnetsDirty(true);

    }

   public int getDataNodeCount() {
      int nodeCount = 0;

      for (Iterator<Node> nodes = graphData.nodes(); nodes.hasNext();) {
         Node node = nodes.next();

         if (Predicates.IsNodeDisplayable.test(node)) {
            nodeCount++;
         }
      }
      return nodeCount;
   }

    public void markAllVisualized(boolean enable) {

        Iterator<Node> nodes = graphData.nodes();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            node.setBoolean(IS_VISUALIZED, enable);
        }

        Iterator<Edge> edges = graphData.edges();
        while (edges.hasNext()) {
            Edge edge = edges.next();
            edge.setBoolean(IS_VISUALIZED, enable);
        }

        if (!enable) {
            Iterator<NodeItem> it = visualGraph.nodes();
            while (it.hasNext()) {
                NodeItem nodeItem = it.next();
                nodeItem.setVisible(false);
            }

            Iterator<EdgeItem> edgeItems = visualGraph.edges();
            while (edgeItems.hasNext()) {
                EdgeItem edgeItem = edgeItems.next();
                edgeItem.setVisible(false);
            }
        }

        // do we need edges visualized as well?

    }

    public synchronized GraphStateFlags getGraphStateFlags() {
        GraphStateFlags flags = new GraphStateFlags();
        SelectionModel selection = null;
        if ((selections != null) && !selections.isEmpty()) {
            selection = selections.get("default.selection");
        }

        for (Iterator<Node> nodes = visualGraph.nodes(); nodes.hasNext(); ) {
            Node node = nodes.next();
            NodeStore details = GraphManager.getNodeDetails(node);
            if (node.getBoolean(IS_VISUALIZED)) {
                if (details.isHidden()) {
                    flags.hasHiddenItems = true;
                    if ((selection != null) && (selection.nodes != null) && selection.nodes.contains(node.getRow())) {
                        flags.hasHiddenItemsInSelection = true;
                    }
                } else {
                    flags.hasVisibleItems = true;

                }

                if (details.isBundle()) {
                    flags.hasBundles = true;
                }
            }

            if (flags.hasBundles && flags.hasVisibleItems && flags.hasHiddenItems && flags.hasHiddenItemsInSelection) {
                return flags;
            }
        }

        if (!flags.hasHiddenItems || !flags.hasHiddenItemsInSelection) {
            for (Iterator<Edge> edges = visualGraph.edges(); edges.hasNext(); ) {
                Edge edge = edges.next();
                LinkStore details = GraphManager.getEdgeDetails(edge);
                if (edge.getBoolean(IS_VISUALIZED) && details.isHidden()
                        && !details.getFirstEndpoint().isBundled()
                        && !details.getSecondEndpoint().isBundled()) {
                    flags.hasHiddenItems = true;
                    if ((selection != null) && (selection.links != null) && selection.links.contains(edge.getRow())) {
                        flags.hasHiddenItemsInSelection = true;
                    }
                }

                if (flags.hasBundles && flags.hasVisibleItems && flags.hasHiddenItems
                        && flags.hasHiddenItemsInSelection) {
                    return flags;
                }
            }
        }

        return flags;
    }

   public boolean hasHiddenItems() {
      boolean hasHidden = false;

      for (Iterator<Node> nodes = visualGraph.nodes(); nodes.hasNext();) {
         Node node = nodes.next();

         if (Predicates.IsNodeVisualized.test(node) && Functions.GetNodeDetails.apply(node).isHidden()) {
            hasHidden = true;
            break;
         }
      }
      if (!hasHidden) {
         for (Iterator<Edge> edges = visualGraph.edges(); edges.hasNext();) {
            Edge edge = edges.next();

            if (Predicates.IsEdgeVisualized.test(edge)) {
               LinkStore details = Functions.GetLinkDetails.apply(edge);

               if (details.isHidden() && !details.getFirstEndpoint().isBundled() &&
                   !details.getSecondEndpoint().isBundled()) {
                  hasHidden = true;
                  break;
               }
            }
         }
      }
      return hasHidden;
   }

    public VisualItem getDisplayableFor(NodeItem item) {
        if (item == null) {
            return null;
        }

        NodeStore details = GraphManager.getNodeDetails(item);
        if (details != null) {
            NodeStore root = details;
            while (root.getParent() != null) {
                root = (NodeStore) root.getParent();
            }

            details = root;
        }
        Node displayed = getNodeFromDetails(details);

        return (displayed == null) ? null : (VisualItem) visualGraph.getNode(displayed.getRow());
    }

    public Map<String, SelectionModel> getSelections() {
        return selections;
    }

    public void setSelections(Map<String, SelectionModel> selections) {
        this.selections = selections;
    }

    // public void markItemsVisible( boolean flag ) {
    // Iterator<NodeItem> nodes = visualGraph.nodes();
    // while( nodes.hasNext() ) {
    // NodeItem nodeItem = nodes.next();
    // nodeItem.setVisible( flag );
    // }
    // }

    // public void setKeyIndex(Map<String, Node> keyIndex) {
    // this.keyIndex = keyIndex;
    // }
    //
    // public Map<String, Node> getKeyIndex() {
    // return keyIndex;
    // }
    public void resetPlayer() {
        if (player != null) {
            player.destroy();
        }

        layers = Collections.emptySet();
        player = null;

        isPlayerRunning = false;

        deleteObservers();
    }

    public void createPlayer() {
        player = new GraphPlayer(vizUuid);

        // just to ensure we're not leaking here...
        deleteObservers();

        addObserver(new GraphNavigationObserver());

        emitNavigationUpdate();

        // player.initialize();
        // player.start();
    }

    private void emitNavigationUpdate() {
        setChanged();
        notifyObservers();
    }

    public GraphPlayer getPlayer() {
        return player;
    }

    public void resetIds(String dvUuid, String vizUuid) {
        this.dvUuid = dvUuid;
        this.vizUuid = vizUuid;

        graphData.putClientProperty(GraphManager.DV_UUID, dvUuid);
        graphData.putClientProperty(GraphManager.VIEWDEF_UUID, vizUuid);

        // HACK:
        // this is not the place for it, but is the only hook we have right now to
        // capture when we've XStream unmarshalled a GraphContext. If XStream really
        // followed their doc, we could use a Serializable.readObject & ObjectInputValidator.
        this.imageProvider = new DefaultImageProvider(false);
    }

    public void toggleHideLabels() {
        hideNodeLabels = !hideNodeLabels;
        setHideLabels(hideNodeLabels);
    }

    public void setHideLabels(boolean flag) {
        hideNodeLabels = flag;
        DefaultRendererFactory factory = (DefaultRendererFactory) visualization.getRendererFactory();
        Renderer renderer = factory.getDefaultRenderer();
        if ((renderer != null) && (renderer instanceof AbstractShapeRenderer)) {
            AbstractShapeRenderer cr = (AbstractShapeRenderer) renderer;
            cr.setHideLabels(flag);
        }

    }

    public Collection<BufferedImage> getImageLayers() {
        if (layers == null) {
            return Collections.emptySet();
        }

        return layers;

    }

    public void setImageLayers(Collection<BufferedImage> value) {
        layers = new LinkedList<BufferedImage>(value);
    }

    public boolean isPlayerRunning() {
        boolean flag = isPlayerRunning && (player != null);
        if (isPlayerRunning && (player == null)) {
           LOG.trace("RelGraph recovering from inconsistent state (was: play in progress, now: stopped)");
            isPlayerRunning = false;
        }
        return flag;
    }

    public void setPlayerRunning(boolean isPlayerRunning) {
        this.isPlayerRunning = isPlayerRunning;
    }

    public ImageProvider getImageProvider() {
        return imageProvider;
    }

    public void setImageProvider(ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }

    public void fitToRegion(Rectangle2D bounds) {
        GraphManager graphManager = GraphManager.getInstance();
        graphManager.fitToRegion(visualization, bounds);

        emitNavigationUpdate();
    }

    public void fitToSize() {
        GraphManager graphManager = GraphManager.getInstance();
        graphManager.fitToSize(visualization);
        emitNavigationUpdate();
    }

    public void zoomToRegion(Rectangle2D bounds) {
        synchronized (this) {
            GraphManager gm = GraphManager.getInstance();
            gm.zoomToRegion(visualization, bounds);
            emitNavigationUpdate();
        }

    }

   public void pan(int x, int y) {
      synchronized (this) {
         synchronized (visualization) {
            Display display = visualization.getDisplay(0);

            synchronized (display) {
               display.pan(x, y);
            }
         }
         emitNavigationUpdate();
      }
   }

   public void zoom(double scale) {
      synchronized (this) {
         synchronized (visualization) {
            Display display = visualization.getDisplay(0);

            synchronized (display) {
               Dimension viewDim = display.getSize();
               Point2D center = new Point2D.Double((viewDim.getWidth() / 2), (viewDim.getHeight() / 2));

               display.zoom(center, scale);
               emitNavigationUpdate();
            }
         }
         emitNavigationUpdate();
      }
   }

   public void zoomPercent(double percent) {
      if (BigDecimal.valueOf(percent).compareTo(BigDecimal.ZERO) != 0) {
         synchronized (this) {
            synchronized (visualization) {
               Display display = visualization.getDisplay(0);

               synchronized (display) {
                  Dimension viewDim = display.getSize();
                  Point2D center = new Point2D.Double((viewDim.getWidth() / 2), (viewDim.getHeight() / 2));
                  double scale = 1;

                  if (percent > 0) {
                     scale = (1 + percent);
                  } else {
                     scale = 1 / (1 + Math.abs(percent));
                  }
                  display.zoom(center, scale);
                  emitNavigationUpdate();
               }
            }
         }
      }
   }

    public void updateDisplay() {
        emitNavigationUpdate();
    }

    public void copySupportingRowMaps(Graph graph) {
        setNodesByRow((Multimap<Integer, Integer>) graph.getClientProperty(GraphManager.NODES_BY_INTERNAL_ROW));
        setLinksByRow((Multimap<Integer, Integer>) graph.getClientProperty(GraphManager.LINKS_BY_INTERNAL_ROW));

    }
//
//    public void saveSelection() {
//        RelGraphViewDef rgDef = getVisualizationDef();
//
//        // yes this looks wierd, but we've harbored a ref to the selection.
//        // & we need to get it into the current transactional context to
//        // be persisted properly.
//        SelectionModel selection = getOrCreateSelection(GraphManager.DEFAULT_SELECTION);
//        selection = CsiPersistenceManager.merge(selection);
//
//        if (selection != null) {
//            rgDef.setOldSelection(selection);
//            migrateToKeys(selection);
//        }
//    }

    public void loadSelection() {
        RelGraphViewDef rgDef = getVisualizationDef();
        SelectionModel selection = rgDef.getOldSelection();
        if (selection != null) {
            migrateFromKeys(selection);
            addSelectionModel(GraphManager.DEFAULT_SELECTION, selection);
        }
    }

    public List<Path> getPaths() {
        return this.paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

    // convert internal integer ids over to the keys of the objects
    // this way they survive across loads
    private void migrateToKeys(SelectionModel selection) {

        selection.resetKeys();
        Graph graphData = getGraphData();

        for (Integer id : selection.nodes) {
            Node node = graphData.getNode(id);
            NodeStore details = GraphManager.getNodeDetails(node);
            String key = details.getKey();
            selection.nodeKeys.add(key);
        }

        for (Integer id : selection.links) {
            Edge edge = graphData.getEdge(id);
            LinkStore details = GraphManager.getEdgeDetails(edge);
            String key = details.getKey();
            selection.linkKeys.add(key);
        }

    }

    private void migrateFromKeys(SelectionModel selection) {
        selection.reset();

        if (selection.nodeKeys == null) {
            selection.nodeKeys = new TreeSet<String>();
        }

        if (selection.linkKeys == null) {
            selection.linkKeys = new TreeSet<String>();
        }

        Map<String, Node> nodeIndex = getNodeKeyIndex();
        for (String key : selection.nodeKeys) {
            Node node = nodeIndex.get(key);
            if (node != null) {
                int row = node.getRow();
                selection.nodes.add(row);
            }
        }

        Map<String, Edge> edgeIndex = getEdgeKeyIndex();
        for (String key : selection.linkKeys) {
            Edge edge = edgeIndex.get(key);
            if (edge != null) {
                int row = edge.getRow();
                selection.links.add(row);
            }
        }

    }

    public static class Functions {

        public static Function<Node, NodeStore> GetNodeDetails = new Function<Node, NodeStore>() {

            @Override
            public NodeStore apply(Node node) {
                NodeStore details = GraphManager
                        .getNodeDetails(node);
                return details;
            }
        };
        public static Function<Edge, LinkStore> GetLinkDetails = new Function<Edge, LinkStore>() {

            @Override
            public LinkStore apply(Edge edge) {
                LinkStore details = GraphManager
                        .getEdgeDetails(edge);
                return details;
            }
        };

        public static Function<Integer, Node> mapIdToNodeFunction(final Graph graph) {
            return new Function<Integer, Node>() {

                @Override
                public Node apply(Integer id) {
                    return graph.getNode(id.intValue());
                }
            };
        }

        public static Function<Node, Node> mapNodeToWorkspace(final Graph workspace) {
            return new Function<Node, Node>() {

                @Override
                public Node apply(Node visible) {
                    int w_id = visible.getInt(ROOT_GRAPH_ID);
                    Node wNode = workspace.getNode(w_id);
                    return wNode;
                }

            };

        }

        public static Function<Node, Integer> mapNodeToId() {
            return new Function<Node, Integer>() {

                @Override
                public Integer apply(Node node) {
                    return node.getRow();
                }
            };
        }

        public static Function<Node, CsiMap<String, Integer>> mapNodeToNeighborCount() {
            return new Function<Node, CsiMap<String, Integer>>() {
                @Override
                public CsiMap<String, Integer> apply(Node node) {
                    CsiMap<String, Integer> props = new CsiMap<String, Integer>();
                    props.put("id", node.getRow());

                    int neighborCount = 0;

                    for (Iterator<Node> nodes = node.neighbors(); nodes.hasNext();) {
                       if (GraphContext.Predicates.IsVisualizedAndDisplayable.test(nodes.next())) {
                          neighborCount++;
                       }
                    }
                    props.put("count", neighborCount);
                    return props;
                }
            };
        }

//        public static Function<Integer, Node> mapVisibleIdToWorkspaceNode(GraphContext context) {
//            Function<Integer, Node> phaseOne = mapIdToNodeFunction(context.getVisibleGraph());
//            Function<Node, Node> phaseTwo = mapNodeToWorkspace(context.getGraphData());
//            Function<Integer, Node> compose = com.google.common.base.Functions.compose(phaseTwo, phaseOne);
//            return compose;
//        }
    }

   public static class Predicates {
      public static Predicate<Node> NoParents = new Predicate<Node>() {
         @Override
         public boolean test(Node node) {
            return (node.getParent() == null) || (node.getParent().getChildIndex(node) != -1);
         }
      };

      public static Predicate<Node> IsNodeDisplayable = new Predicate<Node>() {
         @Override
         public boolean test(Node node) {
            return GraphManager.getNodeDetails(node).isDisplayable();
         }
      };

      public static Predicate<Node> IsNodeHidden = new Predicate<Node>() {
         @Override
         public boolean test(Node node) {
            return GraphManager.getNodeDetails(node).isHidden();
         }
      };

      public static Predicate<Node> IsNodeVisualized = new Predicate<Node>() {
         @Override
         public boolean test(Node node) {
            return node.getBoolean(IS_VISUALIZED);
         }
      };

      public static Predicate<Edge> IsEdgeVisualized = new Predicate<Edge>() {
         @Override
         public boolean test(Edge edge) {
            //return GraphManager.getEdgeDetails(edge).isDisplayable();
            return GraphManager.getEdgeDetails(edge).isVisualized();
         }
      };

      public static Predicate<Edge> IsEdgeVisualizedAndDisplayable = new Predicate<Edge>() {
         @Override
         public boolean test(Edge edge) {
            return IsVisualizedAndDisplayable.test(edge.getSourceNode()) &&
                   IsVisualizedAndDisplayable.test(edge.getTargetNode());
         }
      };

      public static Predicate<Edge> IsEdgeDisplayable = new Predicate<Edge>() {
         @Override
         public boolean test(Edge edge) {
            return GraphManager.getEdgeDetails(edge).isDisplayable();
         }
      };

      public static Predicate<Node> IsVisualizedAndDisplayable = IsNodeVisualized.and(IsNodeDisplayable);

      public static Predicate<Node> IsBundled = new Predicate<Node>() {
         @Override
         public boolean test(Node node) {
            return (node != null) && GraphManager.getNodeDetails(node).isBundled();
         }
      };

      public static Predicate<Node> IsVisualizedAndUnbundled = IsBundled.negate().and(IsNodeVisualized);
   }

   public String getThemeUuid() {
      return themeUuid;
   }

   public void setThemeUuid(String themeUuid) {
      this.themeUuid = themeUuid;
   }
}

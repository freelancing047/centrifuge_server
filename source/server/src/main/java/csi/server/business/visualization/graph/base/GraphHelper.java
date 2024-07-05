package csi.server.business.visualization.graph.base;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.EventConstants;
import prefuse.data.event.GraphListener;
import prefuse.util.collections.IntIterator;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.config.Configuration;
import csi.server.business.service.FilterActionsService;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.LayoutHelper;
import csi.server.business.visualization.graph.data.GraphDataManager;
import csi.server.common.dto.CsiMap;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.task.exception.TaskAbortedException;
import csi.server.util.sql.SQLFactory;

public class GraphHelper {
   private static final Logger LOG = LogManager.getLogger(GraphHelper.class);

    public static final String DEFAULT_SELECTION = "default.selection";

    private static final String NAME_PARAMETER = "name";

    private static Map<String, Set<Integer>> getGraphSelectionMap(Graph graph) {
        graph.getClientProperty(GraphConstants.SELECTIONS);
        List<Node> selectedNodes = (List<Node>) graph.getClientProperty(GraphConstants.SELECTED_NODES);
        List<Edge> selectedEdges = (List<Edge>) graph.getClientProperty(GraphConstants.SELECTED_LINKS);

        return getGraphSelectionMap(selectedNodes, selectedEdges);
    }

    private static Map<String, Set<Integer>> getGraphSelectionMap(List<Node> nodes, List<Edge> edges) {
        Map<String, Set<Integer>> selectionMap = new HashMap<String, Set<Integer>>();

        if ((nodes != null) && !nodes.isEmpty()) {
            updateSelectionMap(selectionMap, nodes, GraphConstants.NODE_DETAIL);
        }

        if ((edges != null) && !edges.isEmpty()) {
            updateSelectionMap(selectionMap, edges, GraphConstants.LINK_DETAIL);
        }

        return selectionMap;
    }

    public static RelGraphViewDef getVisualizationDef(Graph graph) {
        String vizId = (String) graph.getClientProperty(GraphManager.VIEWDEF_UUID);
        RelGraphViewDef def = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizId);
        return def;
    }

    private static void updateSelectionMap(Map<String, Set<Integer>> selectionMap, List<? extends Tuple> selectedNodes,
            String storeName) {
        for (Tuple t : selectedNodes) {
            AbstractGraphObjectStore store = (AbstractGraphObjectStore) t.get(storeName);

            updateSelectionMap(selectionMap, store);
        }
    }

   private static void updateSelectionMap(Map<String, Set<Integer>> selectionMap, AbstractGraphObjectStore store) {
      Map<String,List<Integer>> rows = store.getRows();

      for (Map.Entry<String,List<Integer>> entry : rows.entrySet()) {
         String nodeDefName = entry.getKey();
         List<Integer> supportRows = entry.getValue();

         if ((supportRows != null) && !supportRows.isEmpty()) {
            Set<Integer> set = selectionMap.get(nodeDefName);

            if (set == null) {
               set = new HashSet<Integer>();
               selectionMap.put(nodeDefName, set);
            }
            set.addAll(rows.get(nodeDefName));
         }
      }
      // include bundled nodes
      List<AbstractGraphObjectStore> children = store.getChildren();

      if (children != null) {
         for (AbstractGraphObjectStore child : children) {
            updateSelectionMap(selectionMap, child);
         }
      }
   }

    public static Graph getSubComponent(Graph graph, int componentIndex) {
        List<Graph> subgraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);
        Graph component = null;
        if ((subgraphs != null) && (subgraphs.size() > componentIndex)) {
            component = subgraphs.get(componentIndex);
        }

        return component;
    }

   private static Predicate<Node> alwaysTrue =
      new Predicate<Node>() {
         public boolean test(Node node) {
            return true;
         }
   };

    public static void initializeGraphContext(GraphContext graphContext, RelGraphViewDef rgDef, Dimension vdim,
            boolean externalAction, SQLFactory sqlFactory, FilterActionsService filterActionsService) throws CentrifugeException {

        GraphContext.Current.set(graphContext);

        LOG.debug("Creating graph");
        GraphManager mgr = GraphManager.getInstance();
        mgr.createGraph(graphContext);
        Graph graph = graphContext.getGraphData();
        Display display = graphContext.getDisplay();
        display.setSize(vdim);

        TaskHelper.checkForCancel();
        if (externalAction) {
            TaskHelper.reportProgress("Creating graph", 20);
        }

        LOG.debug("Creating nodes and links");

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        mgr.createNodesAndLinks(graph, sqlFactory, filterActionsService);
        mgr.initSubgraphData(graph, false);
        graphContext.copySupportingRowMaps(graph);
        stopWatch.stop();

        LOG.debug("Graph data processing took: " + stopWatch.getTime());

        TaskHelper.checkForCancel();
        graphContext.updateVisualProperties(rgDef);

        if (BundleUtil.shouldBundle(graphContext.getGraphData(), rgDef)) {
            TaskHelper.checkForCancel();
            if (externalAction) {
                TaskHelper.reportProgress("Bundling graph", 40);
            }

            graphContext.markAllVisualized(true);
            try {
                String selection = GraphManager.DEFAULT_SELECTION;
                performBundleBySpec(graphContext, selection, null, false, true, null, alwaysTrue,
                        false);
            } finally {
                graphContext.markAllVisualized(false);
                // graphContext.markItemsVisible( false );
            }
        }

        int limit = getRenderThresholdLimit(rgDef);
        int totalNodes = graphContext.getDataNodeCount();
        //
        if (totalNodes < limit) {
            TaskHelper.checkForCancel();
            if (externalAction) {
                TaskHelper.reportProgress("Creating initial graph view", 30);
            }

            graphContext.markAllVisualized(true);
            graphContext.setSubnetsDirty(true);

            Collection<Node> initialNodes = new ArrayList<Node>();

            for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
               Node node = nodes.next();

               if (GraphContext.Predicates.IsNodeDisplayable.test(node)) {
                  initialNodes.add(node);
               }
            }
            graphContext.showItems(initialNodes, false);

            if (!mgr.hasComponentizedLayout(graphContext)) {
                TaskHelper.checkForCancel();
                if (externalAction) {
                    TaskHelper.reportProgress("Running placement", 50);
                }
                mgr.computeAndLayoutComponents(graphContext);
            }

            // GraphManager.fitToRegion( graphContext.getVisualization(),
            // graphContext.getPatchBounds() );
            graphContext.fitToRegion(graphContext.getPatchBounds());

            graphContext.updateVisibleNodeLegend();
            graphContext.updateVisibleLinkLegend();
        } else {
            graphContext.markAllVisualized(false);
            graphContext.updateVisibleNodeLegend();
            graphContext.updateVisibleLinkLegend();
        }

        if (externalAction) {
            TaskHelper.reportProgress("Saving graph", 95);
        }
        GraphDataManager.saveGraphData(graphContext);

        // only register the context after the graph is in a valid state
        GraphServiceUtil.setGraphContext(graphContext);
        rgDef.getOldSelection().clearSelection();
        restoreSelectionFromVizDef(rgDef);

//        if(false) {
//            if (rgDef.isBroadcastListener()) {
//                Selection selection = SelectionBroadcastCache.getInstance().getSelection(rgDef.getUuid());
//                if (!(selection instanceof GraphInternalIdSelection)) {
//                    updateOldSelectionAndRestore(rgDef, selection);
//                    return;
//                }
//
//                GraphInternalIdSelection graphInternalIdSelection = (GraphInternalIdSelection) selection;
//                SelectionModel selectionModel = graphInternalIdSelection.createSelectionModel(graph);
//                updateOldSelectionAndRestore(rgDef, selectionModel);
//            }
//        }
    }

//    private static void updateOldSelectionAndRestore(RelGraphViewDef rgDef, Selection selectionModel) {
//        rgDef.getOldSelection().setFromSelection(selectionModel);
//        CsiPersistenceManager.flush();
//        restoreSelectionFromVizDef(rgDef);
//    }

    public static void performBundleBySpec(GraphContext graphContext, String selectionName, CsiMap results,
            boolean withAnimation, boolean entireGraph, String animationLayout, Predicate<Node> nodeFilter,
            boolean externalAction) throws CentrifugeException {

        Graph graph = graphContext.getVisibleGraph();
        List<CsiMap> positionList = new ArrayList<CsiMap>();

        SelectionModel currentSelection = graphContext.getSelection(selectionName);

        // Capture positions before animation for delta.
        if (withAnimation) {
            positionList = captureGraphBundleNodeLayout(graphContext);
        }
        if (externalAction) {
            TaskHelper.reportProgress(20);
        }

        int listSize = (entireGraph) ? graph.getNodeCount() : currentSelection.nodes.size();
        List<Node> targetNodes = new ArrayList<Node>(listSize);

        if (entireGraph) {
           for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
              Node node = nodes.next();

              if (nodeFilter.test(node)) {
                 targetNodes.add(node);
              }
           }
        } else {
           for (Integer nodeId : currentSelection.nodes) {
              Node node = graph.getNode(nodeId);

              targetNodes.add(node);
           }
        }

        try {
            // TODO: add event listener to track newly added (top-level) nodes.
            // this will feed into an animation for rendering updates.

            final Collection<Node> added = new ArrayList<Node>();
            GraphListener graphListener = new GraphListener() {

                Map<Integer, Node> possibleDelete = new HashMap<Integer, Node>();

                @Override
                public void graphChanged(Graph g, String table, int start, int end, int col, int type) {
                    if ((type == EventConstants.INSERT) && table.equals(Graph.NODES)) {
                        if (start != end) {
                            if (LOG.isTraceEnabled()) {
                               LOG.trace("Multi-row insert?");
                            }
                        } else {
                            Node node = g.getNode(start);
                            added.add(node);
                            possibleDelete.put(start, node);
                        }
                    } else if ((type == EventConstants.DELETE) && table.equals(Graph.NODES)) {
                        Node node = possibleDelete.remove(start);
                        added.remove(node);

                    }
                }
            };

            graph.addGraphModelListener(graphListener);
            BundleByObject bundleOperation = new BundleByObject(graphContext.getDvUuid(), graphContext.getVizUuid(),
                    graph);
            bundleOperation.setSelection(targetNodes);
            bundleOperation.setNodeFilter(nodeFilter);
            bundleOperation.run();

            graph.removeGraphModelListener(graphListener);

//            Map<String, Node> idMapper = graphContext.getNodeKeyIndex();

//            Set<Integer> components = new HashSet<Integer>();

            for (Node bundleNode : added) {
                bundleNode.setBoolean(GraphContext.IS_VISUALIZED, true);

                NodeStore bundleDetails = GraphManager.getNodeDetails(bundleNode);
                boolean hideLabels = true;

                List<NodeStore> children = new ArrayList<NodeStore>();
                BundleUtil.findLeafChildren(bundleDetails, children);
                for (NodeStore leaf : children) {
                    hideLabels = hideLabels && leaf.isHideLabels();
                }
                bundleDetails.setHideLabels(hideLabels);
            }

            // Need a recomputation of the components for layout.
            // Need to figure out how to mark the current positions
            // and the positions after the layout
            GraphManager graphManager = GraphManager.getInstance();
            graphManager.computeAndLayoutComponents(graphContext);
            graphContext.updateVisibleNodeLegend();
            graphContext.updateVisibleLinkLegend();

            if (currentSelection != null) {
                currentSelection.reset();
            }

        } catch (SQLException e) {
            throw new CentrifugeException(e);
        }

        // Capture positions after bundling for delta.
        if (withAnimation) {
            layoutAndMergeBundleBySpecNodes(positionList, animationLayout, graphContext.getVizUuid(), targetNodes);
            results.put("animationLayoutList", positionList);
        }

        if (externalAction) {
            TaskHelper.reportProgress(75);
        }
    }

    public static int getRenderThresholdLimit(RelGraphViewDef def) {
        int threshold = Configuration.getInstance().getGraphConfig().getRenderThreshold();
        CsiMap properties = def.getClientProperties();
        if (properties != null) {
            Object value = properties.get(RelGraphViewDef.PROPERTY_RENDER_THRESHOLD);
            if (value != null) {
                if (value instanceof Number) {
                    threshold = ((Number) value).intValue();
                } else {
                    try {
                        threshold = Integer.parseInt(value.toString());
                    } catch (Throwable t) {
                       LOG.trace("Failed parsing threshold value of : " + value.toString());
                    }
                }
            }
        }

        return threshold;
    }

    public static void restoreSelectionFromVizDef(VisualizationDef vizDef) {
        RelGraphViewDef viewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizDef.getUuid());
        SelectionModel selectionModel = viewDef.getOldSelection();

        if (selectionModel == null) {
            return;
        }

        GraphContext context = GraphServiceUtil.getGraphContext(vizDef.getUuid());
        String selectionName = DEFAULT_SELECTION;
        SelectionModel graphSelectionModel = context.getOrCreateSelection(selectionName);

        if (graphSelectionModel == null) {
            return;
        }

        IntIterator nodeIterator = context.getGraphData().nodeRows();
        // Check to see if the oldSelectionModel still applies to the new context
        while (nodeIterator.hasNext()) {
            Integer currentNode = nodeIterator.nextInt();
            if (selectionModel.nodes.contains(currentNode)) {
                graphSelectionModel.nodes.add(currentNode);
            }
        }

        IntIterator linkIterator = context.getGraphData().edgeRows();
        // Check to see if the oldSelectionModel still applies to the new context
        while (linkIterator.hasNext()) {
            Integer currentLink = linkIterator.nextInt();
            if (selectionModel.links.contains(currentLink)) {
                graphSelectionModel.links.add(currentLink);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public static List<CsiMap> captureGraphBundleNodeLayout(GraphContext graphContext) {

        Graph graph = graphContext.getVisibleGraph();
        VisualGraph vGraph = graphContext.getVisualGraph();
        Display display = graphContext.getDisplay();
        Iterator nodes = graph.nodes();
        List<CsiMap> nodePositions = new ArrayList<CsiMap>();
        Point2D itemPoint;
        Point2D displayPoint;

        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();
            VisualItem vizNode = (VisualItem) vGraph.getNode(node.getRow());

            if (vizNode.isVisible()) {
                itemPoint = new Point2D.Double(vizNode.getX(), vizNode.getY());
                displayPoint = toDisplayPoint(display, itemPoint);

                CsiMap<String, Object> prop = new CsiMap<String, Object>();
                prop.put("ID", vizNode.getRow());
                prop.put("beforeX", displayPoint.getX());
                prop.put("beforeY", displayPoint.getY());
                nodePositions.add(prop);
            }
        }

        return nodePositions;
    }

    @SuppressWarnings("unchecked")
    public static List<CsiMap> layoutAndMergeBundleBySpecNodes(List<CsiMap> startingNodePosition,
            String animationLayout, String vizuuid, Collection<Node> nodesOfInterest) {

//        List<CsiMap> mergedList = new ArrayList<CsiMap>();
//        List<Node> bundles = new ArrayList<Node>();
        Visualization vis = getVisualization(vizuuid);

        if (vis != null) {
           Graph graph = (Graph) vis.getSourceData("graph");
           Map<String,Node> idToNodeMapper = (Map<String,Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
           Display display = vis.getDisplay(0);
//        Point2D itemPoint;
           Point2D displayPoint = new Point2D.Double(0, 0);
           /* SelectionModel selection = */getSelection(vizuuid, DEFAULT_SELECTION);

           // Run layout
           GraphConstants.eLayoutAlgorithms layout = LayoutHelper.getLayout(animationLayout);
           GraphManager gm = GraphManager.getInstance();

           gm.runPlacement(graph, layout);

           VisualGraph vGraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);
           // Iterator nodes = graph.nodes();

           for (CsiMap nodeInfo : startingNodePosition) {

              int nodeId = ((Number) nodeInfo.get("ID")).intValue();
              NodeItem node = (NodeItem) vGraph.getNode(nodeId);

              NodeStore details = GraphManager.getNodeDetails(node);
              if (details.isBundled() && nodesOfInterest.contains(node)) {
                 NodeStore root = findRootParent(details);
                 Node rootNode = idToNodeMapper.get(root.getKey());
                 NodeItem visualRootNode = (NodeItem) vGraph.getNode(rootNode.getRow());

                 // swap current reference for the visual node to be the 'root'
                 node = visualRootNode;
                 details = GraphManager.getNodeDetails(node);
              }

              // don't handle nodes already bundled!
              if (details.isBundled()) {
                 continue;
              }

              if (!details.isHidden()) {
                 nodeInfo.put("afterX", node.getX());
                 nodeInfo.put("afterY", node.getY());
                 nodeInfo.put("height", node.getBounds().getHeight());
                 nodeInfo.put("width", node.getBounds().getWidth());
                 nodeInfo.put("afterX", displayPoint.getX());
                 nodeInfo.put("afterY", displayPoint.getY());

                 // get the label
                 List<String> labels = details.getLabels();
                 if ((labels != null) && !labels.isEmpty()) {
                    nodeInfo.put(NAME_PARAMETER, labels.get(0));
                 }
                 nodeInfo.put("type", details.getType());
              }
           }

           Double currentScale = Double.valueOf(display.getScale());
           CsiMap<String,Object> prop = new CsiMap<String,Object>();
           prop.put("currentScale", currentScale);
           startingNodePosition.add(0, prop);
        }
        return startingNodePosition;
    }

    public static Point2D toDisplayPoint(Display display, Point2D absPoint) {
        AffineTransform transform = display.getTransform();

        Point2D doublePoint = (transform == null ? absPoint : transform.transform(absPoint, null));

        Point point = new Point();
        point.setLocation(doublePoint);

        return point;
    }

    public static Visualization getVisualization(String vizUuid) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        Visualization visualization = null;
        if (graphContext != null) {
            if (graphContext.isInvalidated()) {
                throw new TaskAbortedException("Graph has been invalidated.");
            }
            visualization = graphContext.getVisualization();
        }
        return visualization;
    }

    public static SelectionModel getSelection(String vizUuid, String id) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        if (graphContext == null) {
            return new SelectionModel();
        }
        synchronized (graphContext) {
            if (id == null) {
                id = "default.selection";
            }
            SelectionModel selection = graphContext.getOrCreateSelection(id);
            return selection;
        }
    }

    private static NodeStore findRootParent(NodeStore details) {
        NodeStore root = details;
        NodeStore temp = (NodeStore) root.getParent();
        while (temp != null) {
            root = temp;
            temp = (NodeStore) root.getParent();
        }

        return root;
    }

    public static void removeHiddenFromSelection(SelectionModel selection) {
        GraphContext context = GraphContext.Current.get();
        if (context == null) {
            return;
        }
        VisualGraph visualGraph = context.getVisualGraph();
        if (visualGraph == null) {
            return;
        }
        for (Integer nodeId : new ArrayList<Integer>(selection.nodes)) {
            NodeItem node = (NodeItem) visualGraph.getNode(nodeId);
            NodeStore nodeDetails = GraphManager.getNodeDetails(node);
            if (nodeDetails.isHidden()) {
                selection.nodes.remove(nodeId);
            }
        }
        for (Integer linkid : new ArrayList<Integer>(selection.links)) {
            EdgeItem edge = (EdgeItem) visualGraph.getEdge(linkid);
            LinkStore details = GraphManager.getEdgeDetails(edge);
            if (details.isHidden()) {
                selection.links.remove(linkid);
            }
        }
    }
}

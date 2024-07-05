package csi.server.business.visualization.graph.base;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.service.visualization.theme.ThemeManager;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.util.BundleMetrics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.dao.CsiPersistenceManager;

public class BundleByObject extends AbstractBundling {
   protected static final Logger LOG = LogManager.getLogger(BundleByObject.class);

   private static final String SUPPORTING_QUERY = "select * from {0} where \"internal_id\" = ?";

    protected DataCacheHelper cacheHelper;

    protected ResultSet scrollingData;

    protected DataView dataview;

    protected DataModelDef modelDef;

    protected RelGraphViewDef graphDef;

    protected GraphTheme theme;

    // Note: always assumes we have a non-null selection. guards in
    // setter to ensure non-null list is passed.
    protected List<Node> selection = Collections.emptyList();

    private Collection<Node> bundleNodes = new HashSet<Node>();
    private Collection<Edge> bundleEdges = new HashSet<Edge>();

    private Map<String, Node> nodeKeyMap;

    private Predicate<Node> nodeFilter;

   public BundleByObject(String dvUuid, String vizUuid, Graph graph) throws SQLException, CentrifugeException {
      super(dvUuid, vizUuid, graph);

      cacheHelper = new DataCacheHelper();
      nodeFilter = new Predicate<Node>() {
         @Override
         public boolean test(Node node) {
            return true;
         }
      };
   }

    protected void initialize() {
        dataview = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        modelDef = dataview.getMeta().getModelDef();
        graphDef = (RelGraphViewDef) modelDef.findVisualizationByUuid(vizUuid);
        theme = ThemeManager.getGraphTheme(graphDef.getThemeUuid());
        nodeKeyMap = (Map<String, Node>) this.graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
    }

    public Predicate<Node> getNodeFilter() {
        return nodeFilter;
    }

    public void setNodeFilter(Predicate<Node> nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

    public synchronized void setSelection(List<Node> selection) {
        if (selection != null) {
            // TODO: determine whether we need to make a copy to avoid
            // external modification of this list...
            this.selection = selection;
        }
    }

   protected Iterator<Node> getFilteredNodes() {
      Iterator<Node> filter = graph.nodes();

      if (nodeFilter != null) {
         Collection<Node> filteredNodes = new ArrayList<Node>();

         for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
            Node node = nodes.next();

            if (nodeFilter.test(node)) {
               filteredNodes.add(node);
            }
            filter = filteredNodes.iterator();
         }
      }
      return filter;
   }

    public void run() {
        initialize();
        execute();
    }

   protected void execute() {
      try {
         BundleDef bundlingDef = null;

         for (BundleDef bdef : graphDef.getBundleDefs()) {
            bundlingDef = bdef;
            break;
         }
         // TODO: move this to setup
         if ((bundlingDef == null) || (bundlingDef.getOperations() == null) || bundlingDef.getOperations().isEmpty()) {
            LOG.warn("No grouping operations defined by individual object");
            LOG.info("Skipping bundle by objects -- no configuration found");
            return;
         }
         Iterator<Node> iterator;

         if (selection.isEmpty()) {
            // apply bundling to all nodes....
            iterator = getFilteredNodes();
         } else {
            iterator = selection.iterator();
         }
         try (Connection connection = CsiPersistenceManager.getCacheConnection();
              ResultSet rs = cacheHelper.getCacheData(connection, dvUuid, true)) {
            while (iterator.hasNext()) {
               Node node = iterator.next();
               NodeStore store = GraphManager.getNodeDetails(node);

               try {
                  applyBundleSpecs2(node, bundlingDef, modelDef.getFieldDefs(), rs);
               } catch (Exception e) {
                  LOG.info("Could not properly apply bundles for " + store.getLabel(), e);
               }
            }
            // we have our bundles, apply desktop logic
            // to prune bundles of 1; then we need to fix up the graph
            // by creating any new links required.
            // TODO: remove the pruning for longer term, Groups of One
            pruneBundles();
            createBundleLinks();

            if (hasVisualGraph()) {
                applyVisualProperties();
            }
            VisualGraph vGraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);
            BundleMetrics metrics = new BundleMetrics();

            for (Node n : bundleNodes) {
            	NodeStore details = GraphManager.getNodeDetails(n);
            	metrics = GraphManager.computeBundleIconSize(vGraph, details, metrics);

            	details.setRelativeSize(metrics.computeSize());

            	if (metrics.bySize) {
            		details.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE);
            	} else {
            		if (metrics.byTransparency) {
//            			details.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_TRANSPARENCY);
            		}
            	}
            	VisualItem vi = (VisualItem) vGraph.getNode(n.getRow());

            	vi.setSize(details.getRelativeSize());
            }
            getGraph().putClientProperty("dirty", Boolean.TRUE);
         }
      } catch (Exception e) {
         LOG.warn("Failed during grouping of the graph", e);
      } finally {
         CsiPersistenceManager.releaseCacheConnection();
      }
   }

    private boolean hasVisualGraph() {
        return graph.getClientProperty(GraphConstants.ROOT_GRAPH) != null;
    }

    private void applyVisualProperties() {

        VisualGraph vGraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);

        for (Node bundle : bundleNodes) {

            NodeItem nodeItem = (NodeItem) vGraph.getNode(bundle.getRow());
            NodeStore details = GraphManager.getNodeDetails(bundle);
            boolean canDisplay = details.isDisplayable();
            nodeItem.setVisible(canDisplay);

            @SuppressWarnings("unchecked")
            Iterator<EdgeItem> edges = vGraph.edges(nodeItem);
            while (edges.hasNext()) {
                EdgeItem edge = edges.next();
                edge.setVisible(canDisplay);
            }

        }

        for (Node bundle : bundleNodes) {
//            NodeItem nodeItem = (NodeItem) vGraph.getNode(bundle.getRow());
            NodeStore details = GraphManager.getNodeDetails(bundle);
            if (details.isBundle()) {
                markChildrenHidden(vGraph, details);
            }

        }

//        for (Edge edge : bundleEdges) {
//            EdgeItem edgeItem = (EdgeItem) vGraph.getEdge(edge.getRow());
//        }

    }

   private void markChildrenHidden(VisualGraph vGraph, NodeStore bundle) {
      List<AbstractGraphObjectStore> children = bundle.getChildren();

      if ((children != null) && !children.isEmpty()) {
         for (AbstractGraphObjectStore graphObjectStore : children) {
            NodeStore child = (NodeStore) graphObjectStore;
            Node node = nodeKeyMap.get(child.getKey());

            if (node != null) {
                GraphManager.hideNodeAndEdges(vGraph, node);
            }
            markChildrenHidden(vGraph, child);
         }
      }
   }

    protected void applyBundleSpecs2(Node node, BundleDef bundleDef, List<FieldDef> fieldDefs, ResultSet rs) throws Exception {
        NodeStore store = GraphManager.getNodeDetails(node);

        if (!store.isDisplayable() || store.isBundle()) {
            return;
        }

        Map<String, List<Integer>> supportingRows = store.getRows();
        List<Integer> specRows = supportingRows.get(store.getSpecID());
        if ((specRows == null) || specRows.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Grouping skipped, no supporting rows for node: " + store.getLabel() + "<" + store.getType() + ">");
            }
        }

        // used to build hierarchy of grouping from top --> down. This node is
        // added to the last parentBundle
        // once we're complete (unless they're the same node!)
        NodeStore parentBundle = null;

        // // TODO: do we need to pass all values or just the first row?
        // // this really needs to be based on data of the node and not the
        // // original data anyway....
        if (specRows != null) {
           rs.absolute(specRows.get(0).intValue());
        }
        CacheRowSet cacheRowSet = new CacheRowSet(fieldDefs, rs);

        String primarySpecID = store.getSpecID();

        List<BundleOp> operations = bundleDef.getOperations();
        for (BundleOp bundleOp : operations) {
            if (!primarySpecID.equals(bundleOp.getNodeDef().getName())) {
                continue;
            }

            if (!supportingRows.containsKey(bundleOp.getNodeDef().getName())) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Grouping operation " + bundleOp.getNodeDef().getName() + " does not apply for node: " + store.getLabel());
                }
                continue;
            }

            Object results = cacheRowSet.get(bundleOp.getField());
            String bundleCaption = (results == null) ? null : results.toString();
            if ((bundleCaption == null) || (bundleCaption.length() == 0)) {
                continue;
            }

            String parentID = (parentBundle == null) ? "None" : parentBundle.getKey();
            String bundleKey = new StringBuilder().append(bundleCaption).append("<Parent ").append(parentID).toString();
            Node bundle = nodeKeyMap.get(bundleKey);
            NodeStore bundleStore;

            if (bundle == null) {
                bundleStore = createBundleStore(store, parentBundle, bundleCaption, bundleKey);
            } else {
                bundleNodes.add(bundle);
                bundleStore = GraphManager.getNodeDetails(bundle);
//                String bundleType = bundleStore.getType();
//                String nodeType = store.getType();

//                bundleType = (bundleType == null) ? "" : bundleType;
//                nodeType = (nodeType == null) ? "" : nodeType;

//                if (!bundleType.equals(nodeType)) {
                    // dealing with a mixed-type bundle -- remove typing
                    // information for the bundle
                    // NB: passing empty type reverts to unspecified. The count
                    // for
                    // the previous contributions of the homogeneous bundle type
                    // is still maintained
                    // just add in the new type for the current node to maintain
                    // 'sub-type' counts.
//                    bundleStore.setType("");
//                    bundleStore.addType(nodeType);
//                }

            }

            if (parentBundle != null) {
                bundleStore.setParent(parentBundle);
                parentBundle.addChild(bundleStore);
            }

            parentBundle = bundleStore;

        }

        if (parentBundle != null) {
            if (parentBundle == store) {
                LOG.info(String.format("Ignoring recursive bundle spec found for node %s", store.getKey()));
            } else {
                store.setParent(parentBundle);
                parentBundle.addChild(store);
            }
        }
    }

    /**
     * Clean up any nodes that were created as a bundle, but only have a single
     * child. This requires a single pass thru all nodes of the graph to flag
     * bundles, a pass thru bundle nodes discovered 'rectify' themselves, and
     * the final pass determines whether a bundle had nodes 'promoted' out of
     * the bundle (i.e. there was only one node in the bundle).
     * <p>
     * NB: This will change once we implement <i>Brian's</i> notion of multiple
     * membership i.e. a node can be a member of multiple bundles/groups _AND_ a
     * bundle can contain only one node.
     */
    @SuppressWarnings("unchecked")
    private void pruneBundles() {
        Node node;
        NodeStore store;
        List<Node> potentialDelete = new ArrayList<Node>();

        potentialDelete.addAll(bundleNodes);

        Iterator<Node> iterator = potentialDelete.iterator();
        while (iterator.hasNext()) {
            node = iterator.next();
            store = GraphManager.getNodeDetails(node);
            store.rectifyBundle();
        }

        Table nodeTable = graph.getNodeTable();
        iterator = potentialDelete.iterator();
        while (iterator.hasNext()) {
            node = iterator.next();
            if (!nodeTable.isValidRow(node.getRow())) {
                // normally we would provide a trace of indicating that this node
                // is no longer valid;but attempting to get any information about
                // the node will cause a IllegalStateException to be thrown....
                // get rid of the node from further processing though
                bundleNodes.remove(node);
                continue;
            }

            store = GraphManager.getNodeDetails(node);
            if (store.getChildren().isEmpty()) {
                getGraph().removeNode(node);
                nodeKeyMap.remove(store.getKey());
                bundleNodes.remove(node);
            }

        }
    }

    private NodeStore createBundleStore(NodeStore store, NodeStore parentBundle, String bundleLabel, String bundleKey) {

        NodeStore bundleStore = new NodeStore();
        bundleStore.setDocId(ObjectId.get());

        bundleStore.setBundle(true);
        bundleStore.setKey(bundleKey);
        bundleStore.addLabel(bundleLabel);
        bundleStore.setSpecID(store.getSpecID());
        bundleStore.addSpecRow(store.getSpecID(), 0);
        bundleStore.setType(GraphConstants.BUNDLED_NODES);

        Node newBundle = getGraph().addNode();
        GraphManager.setNodeDetails( newBundle, bundleStore);

        bundleNodes.add(newBundle);
        nodeKeyMap.put(bundleKey, newBundle);
        return bundleStore;
    }

    /**
     * Cycles through all concrete edges in our graph. For each end-point of a
     * link we determine if either end-point is now part of a bundle. If either
     * end-point is bundled, we find the root of each node's hierarchy (only
     * applicable if both nodes are bundled) and create a new <i>meta</i> link
     * to represent the relation between the nodes due to the bundling
     * operation.
     */
    @SuppressWarnings("unchecked")
    protected void createBundleLinks() {
        // use own hash table for new derived links
        Map<String, Edge> linkBundleTable = new HashMap<String, Edge>();
        Iterator<Edge> links = getGraph().edges();
        Predicate<Node> IsVisualizedAndNotHidden =
            GraphContext.Predicates.IsNodeHidden.negate().and(GraphContext.Predicates.IsNodeVisualized);

        while (links.hasNext()) {
            Edge e = links.next();

            if (!IsVisualizedAndNotHidden.test(e.getSourceNode()) || !IsVisualizedAndNotHidden.test(e.getTargetNode())) {
                continue;
            }

            LinkStore linkStore = GraphManager.getEdgeDetails(e);
            // no need to consider a link that has been created due to bundling.
            if (linkStore.isBundle()) {
                continue;
            }

            NodeStore nodeStore1 = linkStore.getFirstEndpoint();
            NodeStore nodeStore2 = linkStore.getSecondEndpoint();

            if (nodeStore1.isBundled() || nodeStore2.isBundled()) {
                while (nodeStore1.getParent() != null) {
                  nodeStore1 = (NodeStore) nodeStore1.getParent();
               }
                while (nodeStore2.getParent() != null) {
                  nodeStore2 = (NodeStore) nodeStore2.getParent();
               }

                // end-points bundled in the same node don't require a link.
                // if the nodes are unbundled (via manual un-bundling the links
                // are created at that time)
                if (nodeStore1 == nodeStore2) {
                  continue;
               }

                Node bundle1 = nodeKeyMap.get(nodeStore1.getKey());
                Node bundle2 = nodeKeyMap.get(nodeStore2.getKey());

                String nodeKey1 = nodeStore1.getKey();
                String nodeKey2 = nodeStore2.getKey();
                String bundleKey = nodeKey1 + "+" + nodeKey2;

                //true if the bundle link reverse target with source
                boolean reverse = false;
                Edge link = graph.getEdge(bundle1, bundle2);
                if (link == null) {
                    link = graph.getEdge(bundle2, bundle1);
                    reverse = true;
                }

                LinkStore bundleStore = null;

                if (link == null) {
                    link = getGraph().addEdge(bundle1, bundle2);
                    link.setBoolean(GraphContext.IS_VISUALIZED, true);
                    reverse = false;
                    bundleEdges.add(link);
                    linkBundleTable.put(bundleKey, link);

                    bundleStore = new LinkStore();
                    bundleStore.setDocId(ObjectId.get());

                    bundleStore.setKey(bundleKey);
                    bundleStore.setFirstEndpoint(nodeStore1);
                    bundleStore.setSecondEndpoint(nodeStore2);
                    bundleStore.setType(GraphConstants.BUNDLED_LINKS);

                    GraphManager.setLinkDetails(link, bundleStore);

                } else {
                    bundleStore = GraphManager.getEdgeDetails(link);
                }

                // establish the link's color if not already set.
                if (bundleStore.color == null) {
                    bundleStore.color = linkStore.getColor();
                }

                // set parent and child pointers
                linkStore.setParent(bundleStore);
                bundleStore.addChild(linkStore);

                BundleUtil.mergeLinkDirection(bundleStore, linkStore, reverse);
            }
        }

    }

}

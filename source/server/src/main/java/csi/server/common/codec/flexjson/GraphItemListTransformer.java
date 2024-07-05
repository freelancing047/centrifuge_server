//package csi.server.common.codec.flexjson;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import prefuse.data.Edge;
//import prefuse.data.Graph;
//import prefuse.data.Node;
//import prefuse.data.Tuple;
//
//import com.google.common.base.Predicate;
//import com.google.common.base.Predicates;
//import com.google.common.collect.Iterators;
//
//import csi.server.business.visualization.graph.GraphContext;
//import csi.server.business.visualization.graph.GraphManager;
//import csi.server.business.visualization.graph.base.GraphObjectStore;
//import csi.server.business.visualization.graph.base.LinkStore;
//import csi.server.business.visualization.graph.base.NodeStore;
//import csi.server.common.dto.graph.EdgeListing;
//import csi.server.common.dto.graph.NodeListing;
//import flexjson.JSONContext;
//import flexjson.TypeContext;
//import flexjson.transformer.AbstractTransformer;
//
//public class GraphItemListTransformer extends AbstractTransformer {
//
//    public GraphItemListTransformer() {
//    }
//
//    public void transform(Object o) {
//
//        if (o == null) {
//            return;
//        }
//
//        Graph graph = null;
//
//        Iterator items;
//        if (o instanceof NodeListing) {
//            NodeListing listing = (NodeListing) o;
//            graph = listing.getGraph();
//            Iterator<Node> unfiltered = graph.nodes();
//            items = Iterators.filter(unfiltered, listing.getFilter());
//        } else {
//            EdgeListing listing = (EdgeListing) o;
//            graph = listing.getGraph();
//            Iterator<Edge> edges = graph.edges();
//            items = edges;
//        }
//
//        TypeContext typeContext = getContext().writeOpenArray();
//
//        while (items.hasNext()) {
//            Tuple tuple = (Tuple) items.next();
//            if (shouldEmitTuple(tuple)) {
//                if (typeContext.isFirst()) {
//                    typeContext.setFirst(false);
//                } else {
//                    getContext().writeComma();
//                }
//                writeTuple(getContext(), tuple, true);
//            }
//
//        }
//
//        getContext().writeCloseArray();
//    }
//
//    private boolean shouldEmitTuple(Tuple tuple) {
//        if (tuple instanceof Node) {
//            return shouldEmitNode((Node) tuple);
//        } else {
//            return shouldEmitEdge((Edge) tuple);
//        }
//    }
//
//    private boolean shouldEmitNode(Node node) {
//
//        if (node == null) {
//            return false;
//        }
//
//        NodeStore nodeStore = GraphManager.getNodeDetails(node);
//
//        boolean include = (nodeStore != null && false == nodeStore.isBundled());
//        return include;
//    }
//
//    private boolean shouldEmitEdge(Edge edge) {
//        LinkStore details = GraphManager.getEdgeDetails(edge);
//        boolean emitEdge = !(details.getFirstEndpoint().isBundled() || details.getSecondEndpoint().isBundled());
//        return emitEdge;
//    }
//
//    private void writeTuple(JSONContext ctx, Tuple tuple, boolean deep) {
//        GraphObjectStore details = null;
//        String sitemtype = null;
//        if (tuple instanceof Node) {
//            details = GraphManager.getNodeDetails((Node) tuple);
//            sitemtype = "node";
//        } else {
//            details = GraphManager.getEdgeDetails((Edge) tuple);
//            sitemtype = "link";
//        }
//
//        ctx.writeOpenObject();
//
//        writeItem(ctx, "itemType", sitemtype);
//        ctx.writeComma();
//
//        writeItem(ctx, "itemId", tuple.getRow());
//        ctx.writeComma();
//
//        writeItem(ctx, "itemKey", details.getKey());
//        ctx.writeComma();
//
//        writeItem(ctx, "displayType", details.getType());
//        ctx.writeComma();
//
//        writeItem(ctx, "displayLabel", details.getLabel());
//        ctx.writeComma();
//
//        writeItem(ctx, "hidden", details.isHidden());
//        ctx.writeComma();
//
//        ctx.writeName("types");
//        writeTypes(ctx, details.getTypes());
//        ctx.writeComma();
//
//        if (tuple instanceof Node) {
//            writeItem(ctx, "bundle", details.hasChildren());
//
//            if (deep) {
//                ctx.writeComma();
//
//                int nestedLevel = getNestedLevel((NodeStore) details);
//                writeItem(ctx, "nestedLevel", nestedLevel);
//                ctx.writeComma();
//
//                int visibleNeighbors = getVisibleNeighbors((Node) tuple);
//                writeItem(ctx, "visibleNeighbors", visibleNeighbors);
//
//                if (details.hasChildren()) {
//                    ctx.writeComma();
//                    ctx.writeName("children");
//                    ctx.writeOpenArray();
//
//                    List<GraphObjectStore> children = details.getChildren();
//                    Map<String, Node> idNodeIndex = (Map<String, Node>) ((Node) tuple).getGraph().getClientProperty(GraphManager.NODE_HASH_TABLE);
//                    for (int i = 0; i < children.size(); i++) {
//                        GraphObjectStore store = children.get(i);
//                        Node child = idNodeIndex.get(store.getKey());
//
//                        if (i > 0) {
//                            ctx.writeComma();
//                        }
//                        writeTuple(ctx, child, true);
//                    }
//
//                    ctx.writeCloseArray();
//                }
//            }
//        } else {
//
//            Edge edge = (Edge) tuple;
//            ctx.writeName("source");
//            writeTuple(ctx, edge.getSourceNode(), false);
//            ctx.writeComma();
//
//            ctx.writeName("target");
//            writeTuple(ctx, edge.getTargetNode(), false);
//
//        }
//        ctx.writeCloseObject();
//
//    }
//
//    private int getVisibleNeighbors(Node node) {
//        Iterator<Edge> edges = node.edges();
//        Predicate<Edge> notHidden = new Predicate<Edge>() {
//
//            @Override
//            public boolean apply(Edge edge) {
//                boolean flag = (edge != null && GraphManager.getEdgeDetails(edge).isHidden() == false);
//                return flag;
//            }
//        };
//        Predicate<Edge> condition = Predicates.and(GraphContext.Predicates.IsEdgeVisualizedAndDisplayable, notHidden);
//        int visibleNeighbors = Iterators.size(Iterators.filter(edges, condition));
//        return visibleNeighbors;
//    }
//
//    private void writeTypes(JSONContext ctx, Map<String, Integer> types) {
//        Iterator<Map.Entry<String, Integer>> iter = types.entrySet().iterator();
//        TypeContext typesCtx = ctx.writeOpenArray();
//        while (iter.hasNext()) {
//            Map.Entry<String, Integer> entry = iter.next();
//            if (typesCtx.isFirst()) {
//                typesCtx.setFirst(false);
//            } else {
//                ctx.writeComma();
//            }
//            ctx.writeOpenObject();
//            writeItem(ctx, "name", entry.getKey());
//            ctx.writeComma();
//
//            writeItem(ctx, "count", entry.getValue());
//            ctx.writeCloseObject();
//        }
//        ctx.writeCloseArray();
//    }
//
//    private int getNestedLevel(NodeStore details) {
//        int nestedLevel = 0;
//        GraphObjectStore myParent = details.getParent();
//        while (myParent != null) {
//            nestedLevel += 1;
//            myParent = myParent.getParent();
//        }
//        return nestedLevel;
//    }
//
//    private void writeItem(JSONContext ctx, String name, Object value) {
//        ctx.writeName(name);
//        if (value == null) {
//            return;
//        }
//
//        if (value instanceof String) {
//            ctx.writeQuoted((String) value);
//        } else {
//            ctx.write(String.valueOf(value));
//        }
//    }
//
//}
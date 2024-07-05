package csi.server.business.visualization.graph.data;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.config.Configuration;
import csi.graph.AbstractStorageService;
import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.service.theme.ThemeActionsService;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.GraphConstants.eLayoutAlgorithms;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.exception.TaskAbortedException;

public class GraphDataManager {
   private static final Logger LOG = LogManager.getLogger(GraphDataManager.class);

    private static File    graphDir = new File("vizdata");
    private static XStream codec;
    static {
        codec = new XStream();
        codec.processAnnotations(new Class[] {});
        codec.setMode(XStream.ID_REFERENCES);

        Mapper mapper = codec.getMapper();
        codec.registerConverter(new GraphContextConverter(mapper));

        XStreamHelper.initSetConverter(codec);

    }

    public static void saveGraphContext(GraphContext gc)
            throws CentrifugeException {
        if (!graphDir.exists()) {
            graphDir.mkdirs();
        }

        File f = new File(graphDir, getVizFileName(gc.getVizUuid()));

        synchronized (gc) {
            try (OutputStream outs = new GZIPOutputStream(new FileOutputStream(f));
                 Writer writer = new OutputStreamWriter(outs, "UTF-8")) {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");

                codec.toXML(gc, writer);
            } catch (Exception e) {
                try {
                    f.delete();
                } catch (Exception e1) {
                   LOG.error("Failed to delete graph data", e1);
                }
                throw new CentrifugeException("Failed to save graph data", e);
            }
        }
    }

    // TODO: For now we need the dvuuid for now since the file may have be copied during publish
    // or spinoff so we can't trust the content of the file to contain the correct uuids.
    // This will be unnecessary once we store the ids separate from the data
    public static GraphContext loadGraphContext(String dvuuid, String vizuuid)
            throws CentrifugeException {

        AbstractStorageService service = AbstractStorageService.instance();
        if (!service.hasVisualizationData(vizuuid)) {
            return null;
        }

        RelGraphViewDef rgDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizuuid);
        // String optionSetName = rgDef.getOptionSetName();

        // ok then. we need to set about reconstructing a bunch of crap! This constitutes
        // what we capture in the serialized graph context file for the visualization. Until
        // we get all data persisted related to the graph, we have to reconstruct it here.
        //
        // this includes but is not limited to the node id map, link id map, type count, legend info
        //

        ThemeActionsService.updateOptionSet(rgDef);
        GraphContext context = new GraphContext(dvuuid, vizuuid, rgDef.getThemeUuid());
        Map<String, TypeInfo> nodeLegend = context.getNodeLegend();

        context.bootStrapNodeLegend();

        Map<String, Node> nodeIndex = context.getNodeKeyIndex();

        // map to perform lookup of nodes when constructing the edges
        Map<Object, Node> docIdIndex = new HashMap<Object, Node>();

        service.initializeData(vizuuid);
        Graph graph = context.getGraphData();
        VisualGraph visualGraph = context.getVisualGraph();

        GraphStorage graphStorage = service.getGraphStorage(vizuuid);
        if(graphStorage == null){
            return null;
        }

        Collection<DBObject> vertices = graphStorage.getVertices();

        //        OptionSet optionSet = context.getOptionSet();

        Iterator<DBObject> iterator = vertices.iterator();

        DataToNodeTransformer nodeTransformer = new DataToNodeTransformer();
        nodeTransformer.setGraphStorage(graphStorage);
        nodeTransformer.setGraph(graph);
        nodeTransformer.setIndex(nodeIndex);

        HashMap<String, ArrayList> legendDecorations = null;
        if((rgDef.getState() != null) && (rgDef.getState().getNodeLegendDecorations() != null)) {
            legendDecorations = rgDef.getState().getNodeLegendDecorations();
        }
        while (iterator.hasNext()) {
            DBObject v = iterator.next();

            Object id = Helper.getId(v);
            String key = (String) v.get(Helper.APP_ID);
            Node node;
            NodeStore details;

            // if already in the index, it's because the node was referenced by another
            // e.g. a child node of a bundle.  skip the steps to add it to graph, but
            // still update the subsequent UI/visual aspects.
            if( !nodeIndex.containsKey(key)) {
                node = graph.addNode();
                node.set(GraphConstants.DOC_ID, id);
                nodeIndex.put(key, node);
                details = nodeTransformer.apply(v);
                GraphManager.setNodeDetails(node, details);
            } else {
                node = nodeIndex.get(key);
                details = GraphManager.getNodeDetails(node);
            }

            Point position = details.getPosition(eLayoutAlgorithms.forceDirected);

            updateLegendEntry(details, nodeLegend, rgDef, context.getTheme(), legendDecorations);

            NodeItem ni = (NodeItem) visualGraph.getNode(node.getRow());
            ni.setBoolean(GraphContext.IS_VISUALIZED, details.isVisualized());
            ni.setVisible(details.isDisplayable() && details.isVisualized());
            ni.setSize(details.getRelativeSize());
            if (position != null) {
                // calling lib function to ensure x, start:x, and end:x are all set
                // @see ctwo-7210
                PrefuseLib.setX(ni, null, position.getX());
                PrefuseLib.setY(ni, null, position.getY());
            }

            setVisualizedFlag(node, v);
            docIdIndex.put(id, node);
        }

        if((context.getLinkLegend() != null) && context.getLinkLegend().isEmpty()) {
         context.bootStrapLinkLegend();
      }


        Map<String, GraphLinkLegendItem> linkLegend = context.getLinkLegend();
        DataToEdgeTransformer edgeTransformer = new DataToEdgeTransformer();
        edgeTransformer.setGraphStorage(graphStorage);

        Collection<DBObject> edges = graphStorage.getEdges();


        LinkedHashMap<String, ArrayList> linkLegendDecorations = null;
        if((rgDef.getState() != null) && (rgDef.getState().getLinkLegendDecorations() != null)) {
            legendDecorations = rgDef.getState().getLinkLegendDecorations();
        }
        for (DBObject e : edges) {
            Object id = Helper.getId(e);
            ObjectId source = Helper.getEdgeSource(e);
            ObjectId target = Helper.getEdgeTarget(e);

            Node sn = docIdIndex.get(source);
            Node tn = docIdIndex.get(target);

            if ((sn == null) || (tn == null)) {
               LOG.info("Invalid edge encountered.  One or both endpoints do not exist.");
                continue;
            }

            Edge edge = graph.addEdge(sn, tn);
            edge.set(GraphConstants.DOC_ID, id);

            LinkStore linkDetails = edgeTransformer.apply(e);
            linkDetails.setFirstEndpoint(GraphManager.getNodeDetails(sn));
            linkDetails.setSecondEndpoint(GraphManager.getNodeDetails(tn));
            GraphManager.setLinkDetails(edge, linkDetails);

            EdgeItem ei = (EdgeItem) visualGraph.getEdge(edge.getRow());

            ei.setBoolean(GraphContext.IS_VISUALIZED, linkDetails.isVisualized());
            ei.setVisible(linkDetails.isDisplayable());

            setVisualizedFlag(edge, e);

            String linkType = linkDetails.getType();
            GraphLinkLegendItem legendItem = linkLegend.get(linkType);

            //Integer detailsColor = linkDetails.getColor();

            List<LinkDef> linkDefs = rgDef.getLinkDefs();

            if (legendItem == null) {

                LinkDef primaryDef = null;
                int howMany = linkDefs.size();

                for (int i = 0; i < howMany; i++) {
                    LinkDef ld = linkDefs.get(i);
                    if(ld.getAttributeDefs() != null){
                        Object type = ld.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
                        if((type != null) && (((AttributeDef)type).getFieldDef() != null)
                                && linkType.equals(((AttributeDef) type).getFieldDef().getStaticText())){
                            primaryDef = ld;
                            break;
                        }
                    }
                    if (ld.getName().equals(linkDetails.getSpecID())) {
                        primaryDef = ld;
                    }
                }
                boolean isDynamic = false;
                Map<String, Object> attrs = Collections.<String, Object>emptyMap();
                if(primaryDef != null){
                    isDynamic = GraphManager.isDynamicType(primaryDef);
                    attrs = GraphContext.buildAttributeMap(primaryDef);
                }

                legendItem = initializeLinkLegendItem(linkType, attrs, isDynamic, context.getTheme());
                if((linkLegendDecorations != null) && linkLegendDecorations.containsKey(legendItem)) {
                    legendItem.color = (long) linkLegendDecorations.get(linkType).get(1);
                }

                linkLegend.put(linkType, legendItem);
            }

            legendItem.count++;
        }

        if (linkLegend != null) {
            for (Map.Entry<String, GraphLinkLegendItem> entry : linkLegend.entrySet()) {
                GraphLinkLegendItem value = entry.getValue();
                if (!value.colorOverride) {
                     GraphManager.getInstance().preventColorsCloseToBackground(rgDef, value);
                }
            }
        }

        Set<String> keys = new HashSet<String>();
        keys.addAll(linkLegend.keySet());
        for (String key : keys) {
            GraphLinkLegendItem legendItem = linkLegend.get(key);
            if (legendItem.count == 0) {
                linkLegend.remove(key);
            }
        }

        // CTWO-7105 Graph does not retain size when reloaded.
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add(Helper.DOC_ID, "dimension");
        DBObject property = graphStorage.getVisualProperty(builder.get());
        if (property != null) {
            Dimension dimension = new Dimension();
            dimension.setSize((Double) property.get("width"), (Double) property.get("height"));
            context.getDisplay().setSize(dimension);
            builder = BasicDBObjectBuilder.start();
            builder.add(Helper.DOC_ID, "transform");
            property = graphStorage.getVisualProperty(builder.get());
            double[] parts = new double[6];
            parts[0] = (Double) property.get("m00");
            parts[1] = (Double) property.get("m01");
            parts[2] = (Double) property.get("m02");
            parts[3] = (Double) property.get("m10");
            parts[4] = (Double) property.get("m11");
            parts[5] = (Double) property.get("m12");
            try {
                context.getDisplay().setTransform(new AffineTransform(parts));
            } catch (NoninvertibleTransformException nte) {

            }
        }

        context.loadSelection();
        context.updateVisualProperties(rgDef);
        return context;
    }


    private static void setVisualizedFlag(Tuple tuple, DBObject val) {
        Data.setVisualizationFlag( tuple, val);

    }

    private static void updateLegendEntry(NodeStore details, Map<String, TypeInfo> nodeLegend, RelGraphViewDef rgDef, GraphTheme graphTheme, HashMap<String, ArrayList> legendDecorations) {
        String nodeType = details.getType();


        if (!nodeLegend.containsKey(nodeType)) {
            //            Options options = optionSet.getOptions(OptionSet.NODE_TYPE, nodeType);

            Map<String, Object> ndAttrs = null;
            boolean isDynamic = false;

            if (rgDef != null) {
                List<NodeDef> nodeDefs = rgDef.getNodeDefs();
                NodeDef primaryDef = null;
                int howMany = nodeDefs.size();

                for (int i = 0; (i < howMany) && (primaryDef == null); i++) {
                    NodeDef nd = nodeDefs.get(i);
                    if (nd.getName().equals(details.getSpecID())) {
                        primaryDef = nd;
                    }
                }

                if (primaryDef != null) {
                    isDynamic = GraphManager.isDynamicType(primaryDef);
                    ndAttrs = GraphContext.buildAttributeMap(primaryDef);
                }

                if(details.isPlunked()){
                    isDynamic = true;
                }

            }

            if (ndAttrs == null) {
                ndAttrs = new HashMap();
            }

            TypeInfo info = TypeInfo.initializeTypeInfo(nodeType, ndAttrs, isDynamic, false, graphTheme);
            if((legendDecorations != null) && legendDecorations.containsKey(nodeType)) {
                info.color = (Integer) legendDecorations.get(info.key).get(1);
                info.shape = ShapeType.valueOf((String) (legendDecorations.get(info.key)).get(0));
            }
            nodeLegend.put(nodeType, info);
        }

        TypeInfo typeInfo = nodeLegend.get(nodeType);
        typeInfo.totalCount++;
    }

    public static String getVizFileName(String vizuuid) {
        return vizuuid + ".dat";
    }

    public static File getGraphDir() {
        return graphDir;
    }

    public static void copyGraphData(String srcVizUuid, String targetVizUuid)
            throws CentrifugeException {
        File srcf = new File(graphDir, getVizFileName(srcVizUuid));
        File targetf = new File(graphDir, getVizFileName(targetVizUuid));

        if (srcf.exists()) {
            try (InputStream in = new BufferedInputStream(new FileInputStream(srcf));
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(targetf))) {
                IOUtils.copy(in, out);
            } catch (Exception e) {
                try {
                    targetf.delete();
                } catch (Exception e1) {
                   LOG.error("Failed to delete graph data", e1);
                }
                throw new CentrifugeException("Failed to copy graph data", e);
            }
        }

        AbstractStorageService storage = AbstractStorageService.instance();
        if (storage.hasVisualizationData(srcVizUuid)) {
            storage.copy(srcVizUuid, targetVizUuid);
        }
    }

    public static void deleteContext(String vizuuid) {
        //We have to invalidate context, or else load may try to use a context that has been deleted.
        //This results in bad behavior
        AbstractStorageService.instance().resetData(vizuuid);
        try{
            GraphContext curContext = GraphServiceUtil.getGraphContext(vizuuid);
            synchronized (curContext) {
                if(LOG.isDebugEnabled()) {
                  LOG.debug("Invalidating existing graph");
               }
                curContext.setInvalidated(true);
            }
        } catch(TaskAbortedException e){
           LOG.error("Unable to remove graph context", e);
        } catch(NullPointerException npe){
            //Isn't a big deal that I know of, means the context was removed already
            if(LOG.isDebugEnabled()) {
               LOG.debug("Current Context is already null", npe);
            }
        }
    }

    public static void saveGraphData(final GraphContext context) {
        //context.saveSelection();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {

                    Preconditions.checkNotNull(context, "GraphContext not available");

                    AbstractStorageService service = AbstractStorageService.instance();
                    //service.resetData(context.getVizUuid());
                    GraphStorage storage = service.getGraphStorage(context.getVizUuid());

                    storage = service.createEmptyStorage(context.getVizUuid());

                    // we'll be working on visual items
                    VisualGraph vgraph = context.getVisualGraph();
                    if (GraphContext.getNodeLegend(vgraph) == null) {
                        GraphContext.setNodeLegend(vgraph, context.getNodeLegend());
                    }

                    NodeToDataTransformer nodeTransform = new NodeToDataTransformer();
                    nodeTransform.setGraphStorage(storage);
                    Iterator<NodeItem> nodes = vgraph.nodes();
                    while (nodes.hasNext()) {
                        NodeItem nodeItem = nodes.next();
                        NodeStore nodeDetails = GraphManager.getNodeDetails(nodeItem);

                        nodeDetails.setVisualized(nodeItem.getBoolean(GraphContext.IS_VISUALIZED));

                        DBObject data = nodeTransform.apply(nodeItem);
                        storage.addOrUpdateVertex(data);

                        Object docId = Helper.getId(data);
                        nodeItem.set(GraphConstants.DOC_ID, docId);
                        nodeDetails.setDocId(docId);
                    }

                    EdgeToDataTransformer edgeTransform = new EdgeToDataTransformer();
                    edgeTransform.setGraphStorage(storage);

                    Iterator<EdgeItem> edges = vgraph.edges();
                    while (edges.hasNext()) {
                        EdgeItem edgeItem = edges.next();
//                        LinkStore details = GraphManager.getEdgeDetails(edgeItem);

                        DBObject data = edgeTransform.apply(edgeItem);
                        DBObject sourceRef = buildEdgeNodeRef(edgeItem.getSourceNode());
                        DBObject targetRef = buildEdgeNodeRef(edgeItem.getTargetNode());
                        EdgeType edgeType = edgeItem.isDirected() ? EdgeType.DIRECTED : EdgeType.UNDIRECTED;

                        storage.addOrUpdateEdge(data, new Pair<DBObject>(sourceRef, targetRef), edgeType);
                        Object id = Helper.getId(data);

                        edgeItem.set(GraphConstants.DOC_ID, id);
                        GraphManager.getEdgeDetails(edgeItem).setDocId(id);
                    }

                    // CTWO-7105 Graph does not retain size when reloaded.
                    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
                    builder.add(Helper.DOC_ID, "dimension");
                    Dimension dimension = context.getDisplay().getSize();
                    builder.add("height", dimension.getHeight());
                    builder.add("width", dimension.getWidth());
                    storage.addVisualProperty(builder.get());
                    builder = BasicDBObjectBuilder.start();
                    builder.add(Helper.DOC_ID, "transform");
                    AffineTransform transform = context.getDisplay().getTransform();
                    double[] parts = new double[6];
                    transform.getMatrix(parts);
                    builder.add("m00", parts[0]);
                    builder.add("m01", parts[1]);
                    builder.add("m02", parts[2]);
                    builder.add("m10", parts[3]);
                    builder.add("m11", parts[4]);
                    builder.add("m12", parts[5]);
                    storage.addVisualProperty(builder.get());
                    service.saveGraphStorage(context.getVizUuid(), storage);
                    saveJobs.remove(context.getVizUuid());

                } finally {

                    CsiPersistenceManager.releaseCacheConnection();
                }
            }
        };

        boolean persistAsync = Configuration.getInstance().getGraphAdvConfig().isPersistAsync();
        if (persistAsync) {
            RelGraphViewDef visualizationDef = context.getVisualizationDef();
            Future<?> oldSave = saveJobs.put(visualizationDef.getUuid(), Executors.newSingleThreadExecutor().submit(runnable));
            if (oldSave != null) {
                oldSave.cancel(true);
            }
        } else {
            runnable.run();
        }

    }

    public static Map<String, Future<?> > saveJobs = Maps.newConcurrentMap();

    private static DBObject buildEdgeNodeRef(Node sourceNode) {
        ObjectId id = (ObjectId) sourceNode.get(GraphConstants.DOC_ID);
        return Helper.getIdQuery(id);
    }

    /**
     * Builds a GraphLinkLegendItem object.
     * @param edgeType      the link type.
     * @param attributes    map of atributes from linkstore, where the link could have saved its color, or other atributes.
     * @param isDynamic
     * @return              new GraphLinkLegendItem object
     */
    public static GraphLinkLegendItem initializeLinkLegendItem(String edgeType, Map<String, Object> attributes, boolean isDynamic, GraphTheme theme) {
        GraphLinkLegendItem linkLegendItem = new GraphLinkLegendItem();

        linkLegendItem.typeName = edgeType;

        Object color = attributes.get(ObjectAttributes.CSI_INTERNAL_COLOR);

        if (isDynamic) {
            linkLegendItem.color = 16777216;
        } else if (color != null) {
            if (color instanceof Number) {
                linkLegendItem.color = ((Number) color).intValue();
            } else {
                try {
                    linkLegendItem.color = Integer.parseInt(color.toString());
                } catch (Throwable ex) {
                   LOG.warn(String.format("Failure parsing value '%s' for node '%s'", color, linkLegendItem.typeName));
                }
            }
        }
        Object colorOverrideObject = attributes.get(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);

        if (colorOverrideObject != null) {
           linkLegendItem.colorOverride = true;

           if (color != null) {
              if (color instanceof Number) {
                 linkLegendItem.color = ((Number) color).intValue();
              } else {
                 try {
                    linkLegendItem.color = Integer.parseInt(color.toString());
                 } catch (Throwable ex) {
                    LOG.warn(String.format("Failure parsing value '%s' for node '%s'", color, linkLegendItem.typeName));
                 }
              }
           }
        }

//        if(theme != null){
//            Object colorOverrideObject = attributes.get(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
//            LinkStyle style = null;
//            if(colorOverrideObject == null &&  linkLegendItem.typeName != null){
//                style = theme.findLinkStyle(linkLegendItem.typeName);
//                if(style != null)
//                    linkLegendItem.color = style.getColor();
//            }
//
//
//
//        }

        //        else {
        //            linkLegendItem.color = 0L;
        //        }

        linkLegendItem.key = edgeType;
        linkLegendItem.shape = ShapeType.LINE.toString();
        return linkLegendItem;
    }
}

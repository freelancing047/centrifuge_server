package csi.server.ws.actions;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.display.Clip;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.GraphConstants.eLayoutAlgorithms;
import csi.server.dao.CsiPersistenceManager;
import csi.server.ws.ResponseHelper;
import csi.server.ws.rest.JSONActionRestlet;
import csi.server.ws.rest.services.GraphServiceUtil;
import csi.server.ws.support.GraphBuilder;
import csi.server.ws.support.ImageResolver;
import csi.server.ws.support.JsonGraphConstants;
import csi.server.ws.support.Utility;
import csi.server.ws.support.WrappedServletRequest;

public class GraphActionsJSON extends JSONActionRestlet implements JsonGraphConstants {
   private static final Logger LOG = LogManager.getLogger(GraphActionsJSON.class);

    private static final long serialVersionUID = 1L;

    private static final String ICONMAPPING_FILE = "iconmap.properties";
    private static final String PROPERTIES_FILE = "/forecastlink.properties";
    private static final String RIGHT = "r";
    private static final String BOTTOM = "b";
    private static final String LEFT = "l";
    private static final String TOP = "t";
    private static final String OPTION_LABELS = "showLabels";

    private static final String GRAPH_IMAGE_OPERATION = "/services/relgraphactions/actions/getDisplay";
    static String X = "x";
    static String Y = "y";

    static String HEIGHT = "vh";
    static String WIDTH = "vw";

    static String ZOOM = "zoom";

    private Properties imageAlias;

    public GraphActionsJSON() {
        // fixme: change this to read from a properties file.
        imageAlias = new Properties();

        loadProperties();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            CsiPersistenceManager.begin();
            
            Object result = invokeAction(request);

            CsiPersistenceManager.flush();

            if (result != null) {
                responseHelper.returnResult(response, ResponseHelper.CENTRIFUGE_SUCCESS, (result != null) ? result : "", null);
            }
            
            if (CsiPersistenceManager.isRollbackOnly()) {
                CsiPersistenceManager.rollback();
            } else {
                CsiPersistenceManager.commit();
            }

        } catch (Throwable e) {

            CsiPersistenceManager.rollback();

            if (!(e instanceof CentrifugeException) || !((CentrifugeException) e).hasLoggedConditional()) {

               LOG.error("Failure executing " + extractMethodName(request), e);
            }

            handleFault(response, e);
        } finally {
            try {
                CsiPersistenceManager.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return;
    }

    private void loadProperties() {
        ClassLoader loader = this.getClass().getClassLoader();
        try {
            InputStream stream = loader.getResourceAsStream(PROPERTIES_FILE);
            if (stream == null) {

                if (LOG.isDebugEnabled()) {
                   LOG.debug("Using default settings for ForecastLink icons");
                }
                stream = this.getClass().getResourceAsStream(ICONMAPPING_FILE);
            }
            imageAlias.load(stream);
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Failed loading override properties for ForecastLink support.  Please verify you have properly installed this extension.", e);
            }
        }

    }

    public void fitToSize(Map<String, Object> payload) {
        String uuid = getIdentifier(payload);
        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        if (payload.containsKey(WIDTH)) {
            int width = coerceToInt(payload.get(WIDTH));
            int height = coerceToInt(payload.get(HEIGHT));

            Visualization viz = context.getVisualization();
            Display display = viz.getDisplay(0);
            display.setSize(width, height);
        }

        context.fitToSize();
    }

    public void options(Map<String, Object> payload) {
        String uuid = getIdentifier(payload);
        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        if (payload.containsKey(OPTION_LABELS)) {
            boolean flag = getBoolean(payload.get(OPTION_LABELS));
            context.setShowLabels(flag);
        }
    }

    private boolean getBoolean(Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }

    public Map<String, Object> hitTest(Map<String, Object> payload) {
        String uuid = getIdentifier(payload);
        Number x = (Number) payload.get(X);
        Number y = (Number) payload.get(Y);
        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        Map<String, Object> results = new HashMap<String, Object>();

        Visualization viz = context.getVisualization();
        synchronized (viz) {
            Display display = viz.getDisplay(0);
            VisualItem item = display.findItem(new Point(x.intValue(), y.intValue()));
            if (item instanceof NodeItem) {
                populateNodeData(results, (NodeItem) item);
            }
        }

        return results;
    }

    public void pan(Map<String, Object> payload) {
        String uuid = getIdentifier(payload);
        Number x = coerceToNumber(payload.get(X));
        Number y = coerceToNumber(payload.get(Y));

        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        Visualization viz = context.getVisualization();

        synchronized (viz) {
            Display display = viz.getDisplay(0);
            synchronized (display) {
                display.pan(x.doubleValue(), y.doubleValue());
            }
        }

    }

    @SuppressWarnings( { "unchecked", "rawtypes" })
    public Collection<Map> positions(String uuid) throws Exception {
        Collection<Map> results = new LinkedList<Map>();

        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        Display display = context.getDisplay();

        if (display == null) {
            throw new CentrifugeException("No display present for the request graph");
        }

        AffineTransform transform = display.getTransform();
        Dimension dimension = display.getSize();

        Clip virtual = new Clip();
        virtual.setClip(0, 0, dimension.getWidth(), dimension.getHeight());
        virtual.transform(display.getInverseTransform());

        double pixel = 1.0 + 1.0 / display.getScale();

        VisualGraph visualGraph = context.getVisualGraph();

        Iterator<NodeItem> nodes = visualGraph.nodes();
        while (nodes.hasNext()) {
            NodeItem item = nodes.next();

            Rectangle2D bounds = item.getBounds();

            if (virtual.intersects(bounds, pixel)) {

                NodeStore details = GraphManager.getNodeDetails(item);

                Map nodeData = new HashMap();
                nodeData.put(NODE_ID, details.getKey());

                Point2D p = getTranslated(transform, bounds.getMinX(), bounds.getMinY());
                nodeData.put(TOP, p.getY());
                nodeData.put(LEFT, p.getX());

                p = getTranslated(transform, bounds.getMaxX(), bounds.getMaxY());
                nodeData.put(BOTTOM, p.getY());
                nodeData.put(RIGHT, p.getX());
                results.add(nodeData);
            }
        }
        return results;
    }

    public void generateReport(Map graphPayload) throws ServletException, IOException {

        String dataId = UUID.randomUUID().toString();
        GraphServiceUtil.removeGraphContext(this.getSession(), dataId);
        GraphBuilder builder = new GraphBuilder(imageAlias);
        GraphContext context = builder.buildGraphContext(dataId);

        GraphServiceUtil.setGraphContext(this.getSession(), context);

        builder.fromJson(context, graphPayload);

        runComponentizedLayout(context);

        Dimension dim = getDimension(graphPayload);
        if (dim != null) {
            fitToSize(context, dim);
        }

        // now forward to our rendering operation
        Map<String, String> params = new HashMap<String, String>();
        params.put("vduuid", dataId);
        params.put("vw", (String) graphPayload.get("vw"));
        params.put("vh", (String) graphPayload.get("vh"));
        HttpServletRequest wrapped = new WrappedServletRequest(this.getHttpRequest(), params);
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(GRAPH_IMAGE_OPERATION);
        dispatcher.forward(wrapped, getHttpResponse());

        GraphServiceUtil.removeGraphContext(this.getSession(), dataId);
    }

    private void fitToSize(GraphContext context, Dimension dim) {
        Display display = context.getDisplay();
        display.setSize(dim);
        context.fitToSize();
    }

    private Dimension getDimension(Map graphPayload) {

        String wVal = (String) graphPayload.get("vw");
        String hVal = (String) graphPayload.get("vh");

        if (wVal != null || hVal != null) {

            try {
                int width = Integer.parseInt(wVal);
                int height = Integer.parseInt(hVal);

                return new Dimension(width, height);
            } catch (Throwable t) {

            }
        }

        return null;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" })
    public Map uploadGraph(Map graphPayload) {
        Map results = new HashMap();

        String dataId = getIdentifier(graphPayload);

        GraphServiceUtil.removeGraphContext(this.getSession(), dataId);
        GraphBuilder builder = new GraphBuilder(imageAlias);
        GraphContext context = builder.buildGraphContext(dataId);

        GraphServiceUtil.setGraphContext(this.getSession(), context);

        builder.fromJson(context, graphPayload);

        runComponentizedLayout(context);

        return results;
    }

    public void zoom(Map<String, Object> payload) {
        String uuid = getIdentifier(payload);
        Number zoom = coerceToNumber(payload.get(ZOOM));

        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        Visualization viz = context.getVisualization();
        synchronized (viz) {
            Display display = viz.getDisplay(0);
            synchronized (display) {
                Rectangle dims = new Rectangle(display.getSize());
                Point2D center = new Point2D.Double(dims.getCenterX(), dims.getCenterY());
                display.zoom(center, zoom.doubleValue());
            }

        }
    }

    public void updateNode(Map<String, Object> payload) {
        String uuid = getIdentifier(payload);
        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        if (!payload.containsKey(NODE)) {
           LOG.debug("Node data  not provided; skipping updates");
            return;
        }

        Map<String, Node> nodeIndex = context.getNodeKeyIndex();
        @SuppressWarnings("unchecked")
        Map<String, Object> updates = (Map<String, Object>) payload.get(NODE);
        String id = getNodeId(updates);

        if (id == null) {
           LOG.debug("Node identifier not provided; skipping data update");
            return;
        }

        Node node = nodeIndex.get(id);
        updateNodeWithData(node, updates);

    }

    private void updateNodeWithData(Node node, Map<String, Object> updates) {
        NodeStore details = GraphManager.getNodeDetails(node);
        if (updates.containsKey(SIZE)) {

            double size = Double.parseDouble((String) updates.get(SIZE));
            setRelativeSize(details, size, DEFAULT_NODE_SIZE);

        }

        if (updates.containsKey(COLOR)) {
            Utility.addProperty(details, STATUS_COLOR, updates.get(COLOR));

            new ImageResolver(imageAlias).resolve(details);

        }

    }

    private String getNodeId(Map<String, Object> data) {
        return (String) data.get(ID);
    }

    public void regionZoom(Map<String, Object> payload) {
        String uuid = getIdentifier(payload);
        double top = coerceToDouble(payload.get("top"));
        double left = coerceToDouble(payload.get("left"));
        double width = coerceToDouble(payload.get("width"));
        double height = coerceToDouble(payload.get("height"));

        GraphContext context = GraphServiceUtil.getGraphContext(this.getSession(), uuid);

        Rectangle2D bounds = new Rectangle2D.Double();
        bounds.setFrame(left, top, width, height);
        context.zoomToRegion(bounds);
    }

    private Number coerceToNumber(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else {
            return 0;
        }
    }

    private double coerceToDouble(Object value) {
        if (value == null) {
            return 0.0;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            return 0.0;
        }
    }

    private int coerceToInt(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof String) {
            try {
                return (int) Double.parseDouble((String) value);
            } catch (Throwable t) {

            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return 0;

    }

    private void setRelativeSize(NodeStore details, double value, double ref) {
        double size = value / ref;
        details.setRelativeSize(size);
    }

    private String getIdentifier(Map<String, Object> map) {
        if (map.containsKey(DATA_ID)) {
            return (String) map.get(DATA_ID);
        } else if (map.containsKey(GRAPH_ID)) {
            return (String) map.get(GRAPH_ID);
        } else {
            return null;
        }
    }

    private Point2D getTranslated(AffineTransform transform, double x, double y) {

        Point2D orig = new Point2D.Double(x, y);

        Point2D translated = transform.transform(orig, null);
        return translated;
    }

    //
    // Navigational Operations
    //

    private void populateNodeData(Map<String, Object> results, NodeItem item) {
        NodeStore details = GraphManager.getNodeDetails(item);
        results.put(ID, details.getKey());
        results.put(NAME, details.getLabel());
    }

    private void runComponentizedLayout(GraphContext context) {
        GraphManager manager = GraphManager.getInstance();
        Graph graph = context.getGraphData();
        manager.computeComponents(graph);
        manager.computeComponentRegions(graph);
        manager.runPlacement(graph, eLayoutAlgorithms.forceDirected, context);
        context.fitToSize();
    }

}

package csi.server.business.service.export.anx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

/*
 * This is a basic servlet to generate ANX files based on our graph structure. There is a considerable part of the ANX
 * schema that we never touch so we are probably under-utilizing some of the ANX features. The intent is to generate a
 * simple version of our graph that may be opened with an ANX reader.
 *
 * Because a good chunk of the files contain the same basic, template, information, we load in a file that already has
 * this information (ANX_BASE_FILE). We then add to this file by walking the nodes and links in our graph. This
 * primarily consists of creating ChartItem elements in the ANX lexicon.
 *
 * Although this is a subclass of FlexActionRestlet, method generateANX writes directly to the response's output stream,
 * and does not wrap the file output in a packet.
 */
@SuppressWarnings("serial")
public class ANXWriterActions {
   private static final Logger LOG = LogManager.getLogger(ANXWriterActions.class);

    private static final String ATTRIBUTE_CLASS_COLLECTION = "AttributeClassCollection";

    private static final String CHART_COLLECTION = "ChartItemCollection";

    private static final String LAYOUT = "layout";

    private static final String SCALING = "scaling";

    public static final String ANX_BASE_FILE = "webapps/Centrifuge/resources/ANX/ANXBase.xml";

    /*
     * This is pretty arbitrary scaling but seems to yield decent looking charts given our sample data.
     */
    public static final int ANX_X_SCALE = 22;

    public static final int ANX_Y_SCALE = 12;

    /*
     * attributeClasses vs attributeClassCollection is a little confusing - As we walk the nodes we keep a list of
     * distinct attributes in the Array attributeClasses. We also add them to the XML element attributeClassCollection.
     * The reason for using two structures is that we need to lookup each attribute against the list and see if it is
     * new. Rather than making this an expensive Xpath operation against the DOM element, we just keep a parallet
     * ArrayList.
     */

    private static class DataContext {

        Graph graph;

        Document document;

        List<String> attributeClasses = new ArrayList<String>();

        double x_scaling;

        double y_scaling;
    }

    public static String generateANX(GraphContext graphContext) throws IOException, CentrifugeException {

//        String dvUuid = graphContext.getDvUuid();
//        String vizId = graphContext.getVizUuid();

//        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);

        //This is bad.
//        RelGraphViewDef viewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizId);

        //TODO:
//        String value = getHttpRequest().getParameter(SCALING);
        String value = "";
        double scaling = 1;
//        if (value != null && value.length() > 0) {
            try {
                scaling = Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                scaling = 1;
            }
//        }


        DataContext context = new DataContext();

        context.x_scaling = ANX_X_SCALE;
        context.y_scaling = ANX_Y_SCALE;
        context.x_scaling *= scaling;
        context.y_scaling *= scaling;
        context.x_scaling = 2.0d;
        context.y_scaling = 1.5d;

        context.graph = graphContext.getVisibleGraph();
        context.document = getANXTemplate();

/*
        HttpServletResponse httpResponse = getHttpResponse();
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("application/xml");

        // This is how we get it to do a File download so the user will get a
        // File Save dialog
        String name = getDocumentName(dv, viewDef);
        httpResponse.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(name, "UTF-8") + ".anx");
*/

        buildANXHolders(context);

        /*
         * Broken out for readability
         */
        writeNodes(context);
        writeLinks(context);

        String s = "";
        try {
//            StringWriter sw = new StringWriter(10000);
            XMLSerializer serializer = new XMLSerializer();
            OutputFormat outputFormat = new OutputFormat();
            outputFormat.setIndent(2);
            outputFormat.setEncoding((String) null);
            serializer.setOutputFormat(outputFormat);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.setOutputByteStream(baos);
            serializer.serialize(context.document);
            s = baos.toString();

            /*File file = new File("test.xml");
            try {
                Files.asByteSink(file).write(s.getBytes());
            } catch (IOException ignored) {
            }

*//*
            serializer.setOutputByteStream(httpResponse.getOutputStream());
            serializer.serialize(context.document);
            httpResponse.getOutputStream().flush();
*/

        } catch (Exception e) {
           LOG.error(e.toString());
        }
        return s;
    }

    private String getDocumentName(DataView dv, RelGraphViewDef viewDef) {
        if ((viewDef != null) && (viewDef.getName() != null)) {
            return viewDef.getName();
        } else if ((dv != null) && (dv.getName() != null)) {
            return dv.getName();
        } else {
            return "Unnamed Document";
        }
    }

    private static Element buildANXHolders(DataContext context) {
        // Add a ChartCollection to the base ANX file
        Element root = context.document.getDocumentElement();

        Element element = context.document.createElement(ATTRIBUTE_CLASS_COLLECTION);
        root.appendChild(element);

        element = context.document.createElement(CHART_COLLECTION);
        root.appendChild(element);
        return root;
    }

    private static Element getElement(DataContext context, String name) {
        Document doc = context.document;
        NodeList list = doc.getElementsByTagName(name);

        Element searched = null;
        if ((list != null) && (list.getLength() > 0)) {
            searched = (Element) list.item(0);
        }

        return searched;
    }

    /*
     * A link in ANX looks like this - <ChartItem> <Link End1Id="106" End2Id="107"/> </ChartItem>
     */
    @SuppressWarnings("unchecked")
    private static void writeLinks(DataContext context) {
        Document document = context.document;
        Iterator edges = context.graph.edges();

        Element itemContainer = getElement(context, CHART_COLLECTION);

        if (itemContainer != null) {
           while (edges.hasNext()) {
              Element chartItem = document.createElement("ChartItem");
              Edge e = (Edge) edges.next();
              Node sourceNode = e.getSourceNode();
              Node targetNode = e.getTargetNode();
//            NodeStore sourceNodeStore = GraphManager.getNodeDetails(sourceNode);
//            NodeStore targetNodeStore = GraphManager.getNodeDetails(targetNode);
              if (!isValidNode(sourceNode) || !isValidNode(targetNode)) {
                 continue;
              }
              LinkStore edgeDetails = GraphManager.getEdgeDetails(e);

              if (edgeDetails.isHidden()) {
                 continue;
              }
              Element link = document.createElement("Link");

              link.setAttribute("End1Id", String.valueOf(sourceNode.getRow()));
              link.setAttribute("End2Id", String.valueOf(targetNode.getRow()));
              chartItem.appendChild(link);
              itemContainer.appendChild(chartItem);
           }
        }
    }

    /*
     * A node in ANX looks like this - <ChartItem Label="SV 21 1/23/2001 2001-01-23 (23) Tue"> <End X="1650" Y="2112">
     * <Entity EntityId="27" Identity="27" LabelIsIdentity="false"> <Icon> <IconStyle Type="Flight Info"/> </Icon>
     * </Entity> </End> <AttributeCollection> <Attribute AttributeClass="DOB" Value="1972-08-11"/> <Attribute
     * AttributeClass="PORT OF DEPARTURE" Value="JFK"/> </AttributeCollection> </ChartItem>
     */
    @SuppressWarnings("unchecked")
    private static void writeNodes(DataContext context) {

        Document document = context.document;
        Element attributeContainer = getElement(context, ATTRIBUTE_CLASS_COLLECTION);
        Element itemContainer = getElement(context, CHART_COLLECTION);

        if ((attributeContainer != null) && (itemContainer != null)) {
        Iterator nodes = context.graph.nodes();

        VisualGraph visualGraph = (VisualGraph) context.graph.getClientProperty(GraphConstants.ROOT_GRAPH);

        while (nodes.hasNext()) {
            Node n = (Node) nodes.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(n);

            if (!isValidNode(n)) {
               continue;
            }

            Element chartItem = document.createElement("ChartItem");
            // String key = nodeStore.getKey();
            String label = getNodeLabel(nodeStore, n);
            String id = getId(nodeStore, n);
            chartItem.setAttribute("Label", label);

            Element end = document.createElement("End");
            // Point point = nodeStore.getPosition( context.layout );
            VisualItem visualItem = (VisualItem) visualGraph.getNode(n.getRow());
            int xval = (int) (visualItem.getX() * context.x_scaling);
            int yval = (int) (visualItem.getY() * context.y_scaling);

            chartItem.setAttribute("XPosition", Integer.toString(xval));
            end.setAttribute("X", ((Integer) xval).toString());
            end.setAttribute("Y", ((Integer) yval).toString());

            Element entity = document.createElement("Entity");
            entity.setAttribute("EntityId", String.valueOf(n.getRow()));
            entity.setAttribute("Identity", id);
            entity.setAttribute("LabelIsIdentity", "false");

            Element icon = document.createElement("Icon");
            Element iconStyle = document.createElement("IconStyle");

            iconStyle.setAttribute("Type", nodeStore.getType());
            icon.appendChild(iconStyle);
            entity.appendChild(icon);
            end.appendChild(entity);
            chartItem.appendChild(end);
            Element attributeCollection = document.createElement("AttributeCollection");

            // Iterate through the attributes for this node
            Map<String, Property> attributes = nodeStore.getAttributes();
            for (Entry<String, Property> entry : attributes.entrySet()) {
                Property prop = entry.getValue();
                List<Object> values = prop.getValues();
                for (Object o : values) {
                    if (o != null) {
                        Element attribute = document.createElement("Attribute");
                        String attributeKey = entry.getKey();
                        // Don't write out any 'csi.internal' attributes
                        if (attributeKey.startsWith("csi.internal")) {
                           continue;
                        }

                        attribute.setAttribute("AttributeClass", attributeKey);

                        // If this is a new attribute, we need to add it to the
                        // AttributeClassCollection
                        if (!context.attributeClasses.contains(attributeKey)) {
                            Element attributeClass = document.createElement("AttributeClass");
                            attributeClass.setAttribute("Name", attributeKey);
                            attributeClass.setAttribute("Type", "AttText");
                            attributeClass.setAttribute("Visible", "false");
                            attributeContainer.appendChild(attributeClass);
                            context.attributeClasses.add(attributeKey);
                        }
                        attribute.setAttribute("Value", o.toString());
                        attributeCollection.appendChild(attribute);
                    }
                }
            }
            chartItem.appendChild(attributeCollection);
            itemContainer.appendChild(chartItem);
        }
        }
    }

    private static String getId(NodeStore details, Node n) {
        String id = details.getKey();
        if ((id == null) || id.isEmpty()) {
            // use id
            id = String.valueOf(n.getRow());
        }
        return id;
    }

    private static String getNodeLabel(NodeStore details, Node n) {
        String label = "";
        if (!details.isBundle()) {
            label = details.getLabel();
            if ((label == null) || label.isEmpty()) {
                //use id
                label = String.valueOf(n.getRow());
            }
        } else {
            label = BundleUtil.buildBundleLabel(details);
        }
        return label;
    }

    /*
     * Don't try to show nodes that are either explicitly hidden or that are currently bundled.
     */
   private static boolean isValidNode(Node n) {
      NodeStore details = GraphManager.getNodeDetails(n);

      return n.getBoolean(GraphContext.IS_VISUALIZED) && !details.isHidden() && !details.isBundled();
   }

    /*
     * We will ship with a 'base' ANX file that contains only an EntityTypeCollection. This is a collection of Entity
     * types that ANB will understand. The value of putting this in an external file is that users will be able to add
     * to it. If an entity is listed in the graph but not in the EntityTypeCollection, it will not show up.
     */
    private static Document getANXTemplate() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
//            ServletContext context = getHttpRequest().getSession().getServletContext();

            // TODO: getResourceasStream would be better here as this won't work
            // if
            // ever get deployed in a war.
//            InputStream stream = context.getResourceAsStream(ANX_BASE_FILE);
            File file = new File(ANX_BASE_FILE);
//            log.error(file.getAbsoluteFile());
            CharSource charSource = Files.asCharSource(file, Charset.defaultCharset());
            Document dom = db.parse(new InputSource(new StringReader(charSource.read())));
            dom.getDocumentElement().normalize();
            return dom;

        } catch (Exception e) {
           LOG.error("Could not parse ANX base file.");
            return null;
        }
    }
}
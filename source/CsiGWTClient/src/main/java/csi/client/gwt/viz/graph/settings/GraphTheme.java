//package csi.client.gwt.viz.graph.settings;
//
//import static com.google.common.base.Preconditions.checkArgument;
//
//import java.util.List;
//import java.util.Map;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.gwt.core.client.Scheduler;
//import com.google.gwt.core.client.Scheduler.ScheduledCommand;
//import com.google.gwt.xml.client.Document;
//import com.google.gwt.xml.client.NamedNodeMap;
//import com.google.gwt.xml.client.Node;
//import com.google.gwt.xml.client.NodeList;
//import com.google.gwt.xml.client.XMLParser;
//
//import csi.client.gwt.WebMain;
//import csi.client.gwt.viz.graph.node.NodeProxy;
//import csi.client.gwt.vortex.AbstractVortexEventHandler;
//import csi.client.gwt.vortex.VortexFuture;
//import csi.client.gwt.widget.boot.ErrorDialog;
//import csi.server.common.dto.SelectionListData.ResourceBasics;
//import csi.server.common.exception.CentrifugeException;
//import csi.server.common.graphics.shapes.ShapeType;
//import csi.server.common.model.visualization.VisualizationType;
//import csi.server.common.service.api.FileActionsServiceProtocol;
//import csi.server.common.service.api.ThemeActionsServiceProtocol;
//
//public class GraphTheme {
//
//    private static final String OPTION_SETS = "OptionSets"; //NON-NLS
//    private Map<String, NodeTheme> nodeThemes;
//    private Map<String, LinkTheme> linkThemes;
//    private List<ShapeType> shapes;
//    private String comment;
//    private BundleTheme bundleNodeTheme;
//
//    private double iconScale = .75;
//
//    private String iconRoot = "Baseline"; //NON-NLS
//
//    private boolean drawColorInIconPicker;
//
//    private String version;
//
//    private String name;
//
//    private GraphTheme(String name) {
//        this.name = name;
//        nodeThemes = Maps.newTreeMap();
//        linkThemes = Maps.newTreeMap();
//        shapes = Lists.newArrayList();
//    }
//
//    public void addNodeTheme(NodeTheme nodeTheme) {
//        nodeThemes.put(nodeTheme.type, nodeTheme);
//    }
//
//    public void addLinkTheme(LinkTheme linkTheme) {
//        linkThemes.put(linkTheme.type, linkTheme);
//    }
//
//    public void addShape(ShapeType graphThemeShapes) {
//        shapes.add(graphThemeShapes);
//    }
//
//    public static GraphTheme create(String result, String name) {
//        Document doc = XMLParser.parse(result);
//        GraphTheme graphTheme = new GraphTheme(name);
//        NodeList childNodes = doc.getDocumentElement().getChildNodes();
//        for (int i = 0; i < childNodes.getLength(); i++) {
//            Node item = childNodes.item(i);
//            String nodeName = item.getNodeName();
//            if (nodeName.equals("Version")) {//NON-NLS
//                graphTheme.version = item.getChildNodes().item(0).getNodeValue();
//            }
//            if (nodeName.equals("DrawColorInIconPicker")) {//NON-NLS
//                if (item.getChildNodes().item(0).getNodeValue().equals("1")) {//NON-NLS
//                    graphTheme.drawColorInIconPicker = true;
//                }
//            }
//            if (nodeName.equals("IconRoot")) {//NON-NLS
//                graphTheme.iconRoot = item.getChildNodes().item(0).getNodeValue();
//            }
//            if (nodeName.equals("IconScale")) {//NON-NLS
//                graphTheme.iconScale = Double.parseDouble(item.getChildNodes().item(0).getNodeValue());
//            }
//            if (nodeName.equals("Shapes")) {//NON-NLS
//                NodeList shapes = item.getChildNodes();
//                for (int j = 0; j < shapes.getLength(); j++) {
//                    Node subitem = item.getChildNodes().item(j);
//                    if (subitem.getNodeName().equals("Shape")) {
//                        graphTheme.addShape(ShapeType.getShape(subitem.getChildNodes().item(0).getNodeValue()));
//                    }
//                }
//            }
//            if (nodeName.equals("BundleNode")) {//NON-NLS
//                NamedNodeMap attributes = item.getAttributes();
//                Node namedItem;
//                namedItem = attributes.getNamedItem("icon");//NON-NLS
//                String icon = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("color");//NON-NLS
//                String color = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("shape");//NON-NLS
//                String shape = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("overlayScale");//NON-NLS
//                String overlayScale = namedItem.getNodeValue();
//                graphTheme.bundleNodeTheme = new BundleTheme(icon, color, shape, overlayScale);
//            }
//            if (nodeName.equals("Comment")) {//NON-NLS
//                graphTheme.comment = item.getChildNodes().item(0).getNodeValue();
//            }
//            if (nodeName.equals("LinkType")) {//NON-NLS
//                NamedNodeMap attributes = item.getAttributes();
//                Node namedItem = attributes.getNamedItem("linktype");//NON-NLS
//                String type = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("style");
//                String style = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("width");//NON-NLS
//                String width = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("color");
//                String color = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("subtype");//NON-NLS
//                String subtype = namedItem.getNodeValue();
//                graphTheme.addLinkTheme(new LinkTheme(type, style, width, color, subtype));
//            }
//            if (nodeName.equals("NodeType")) {//NON-NLS
//
//                NamedNodeMap attributes = item.getAttributes();
//                Node namedItem = attributes.getNamedItem("nodetype");//NON-NLS
//                String type = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("icon");//NON-NLS
//                String icon = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("color");//NON-NLS
//                String color = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("shape");//NON-NLS
//                String shape = namedItem.getNodeValue();
//                namedItem = attributes.getNamedItem("subtype");//NON-NLS
//                String subtype = namedItem.getNodeValue();
//                graphTheme.addNodeTheme(new NodeTheme(type, icon, color, shape, subtype));
//                // logger.severe(namedItem.getNodeValue());
//            }
//        }
//        return graphTheme;
//    }
//
//    public static class LinkTheme {
//
//        public String type;
//        public String style;
//        public String width;
//        public String color;
//        @Deprecated
//        public String subtype;
//
//        public LinkTheme(String type, String style, String width, String color, String subtype) {
//            this.type = type;
//            this.style = style;
//            this.width = width;
//            this.color = color;
//            this.subtype = subtype;
//        }
//    }
//
//    public static class NodeTheme {
//
//        public String type;
//        public String icon;
//        public String color;
//        public String shape;
//        @Deprecated
//        public String subtype;
//
//        public NodeTheme(String type, String icon, String color, String shape, String subtype) {
//            this.type = type;
//            this.icon = icon;
//            this.color = color;
//            this.shape = shape;
//            this.subtype = subtype;
//        }
//    }
//
//    public static class BundleTheme extends NodeTheme {
//
//        public String overlayScale;
//
//        public BundleTheme(String icon, String color, String shape, String overlayScale) {
//            super(null, icon, color, shape, null);
//        }
//    }
//
//    public BundleTheme getBundleNodeTheme() {
//        return bundleNodeTheme;
//    }
//
//    public String getComment() {
//        return comment;
//    }
//
//    public boolean isDrawColorInIconPicker() {
//        return drawColorInIconPicker;
//    }
//
//    public String getIconRoot() {
//        return iconRoot;
//    }
//
//    public Map<String, LinkTheme> getLinkThemes() {
//        return linkThemes;
//    }
//
//    public Map<String, NodeTheme> getNodeThemes() {
//        return nodeThemes;
//    }
//
//    public List<ShapeType> getShapes() {
//        if (shapes.isEmpty()) {
//            return Lists.newArrayList(ShapeType.nodeShapeWheel);
//        }
//        return shapes;
//    }
//
//    public String getVersion() {
//        return version;
//    }
//
//    public double getIconScale() {
//        return iconScale;
//    }
//
//    private static int nextIndex = 0;
//
//    private static Map<String, GraphTheme> graphThemeMap = Maps.newTreeMap();
//
//    public void apply(NodeProxy nodeProxy) {
//        String nodeType = nodeProxy.getType();
//        if (nodeType == null) {
//            return;
//        }
//        NodeTheme nodeTheme = nodeThemes.get(nodeType);
//        nodeProxy.setScale(iconScale);
//        if (nodeTheme != null) {
//            nodeProxy.setShape(ShapeType.getShape(nodeTheme.shape));
//            nodeProxy.setColor(nodeTheme.color);
//            if (!Strings.isNullOrEmpty(nodeTheme.icon)) {
//                nodeProxy.setIconURI("/Centrifuge/resources/icons/" + iconRoot + "/" + nodeTheme.icon);//NON-NLS
//            }
//        } else {
//            // is there as shape policy
//            if (!shapes.isEmpty()) {
//                ShapeType shape = nodeProxy.getShape();
//                // is this node's shape valid
//                if (!shapes.contains(shape)) {
//                    // assign new shape
//                    if (nextIndex >= shapes.size()) {
//                        nextIndex = 0;
//                    }
//                    nodeProxy.setShape(shapes.get(nextIndex));
//                    nextIndex = nextIndex + 1;
//                }
//            }
//            // does the icon belong to this theme
//            // I cannot keep my icon if it is not in the current theme. I check this by testing my icon url against the current IconRoot.
//            String iconURI = nodeProxy.getIconURI();
//            if (!Strings.isNullOrEmpty(iconURI)) {
//                if (!iconURI.contains(iconRoot)) {
//                    nodeProxy.setIconURI("");
//                }
//            }
//        }
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public static VortexFuture<GraphTheme> get(final String themeName) {
//        checkArgument(!Strings.isNullOrEmpty(themeName), "Theme names cannot be empty or null");//NON-NLS asserts are not enabled in production.
//        // NOTE: the futureTheme is final, so that we can pass it into the other future's handler.
//        final VortexFuture<GraphTheme> futureTheme = WebMain.injector.getVortex().createFuture();
//        // Here we check to see if we already have that theme you wanted.
//        GraphTheme graphTheme = graphThemeMap.get(themeName);
//        if (graphTheme == null) {// in this case we go to the server
//            VortexFuture<String> futureXml = WebMain.injector.getVortex().createFuture();
//            futureXml.execute(FileActionsServiceProtocol.class).getApplicationResource(
//                    //FIXME: magic strings
//                    "OptionSets/" + themeName + ".xml");//NON-NLS
//            futureXml.addEventHandler(new AbstractVortexEventHandler<String>() {
//
//                // When we get back the xml we will create a GraphTheme and then give it to you by firing success on the the VortexFuture the get() method returns.
//                @Override
//                public void onSuccess(String result) {
//                    GraphTheme newTheme = GraphTheme.create(result, themeName);
//                    graphThemeMap.put(themeName, newTheme);
//                    GraphTheme.populateFuture(futureTheme, newTheme);
//                }
//            });
//        } else {// I have the theme you are looking for, but I will let you attach handlers first
//            GraphTheme.populateFuture(futureTheme, graphTheme);
//        }
//        return futureTheme;
//    }
//
//    private static void populateFuture(final VortexFuture<GraphTheme> futureTheme, final GraphTheme graphTheme) {
//        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//
//            @Override
//            public void execute() {
//                futureTheme.fireSuccess(graphTheme);
//            }
//        });
//    }
//
////    public static VortexFuture<List<String>> getThemeNames() {
////        VortexFuture<List<String>> vortexFuture = WebMain.injector.getVortex().createFuture();
////        vortexFuture.execute(FileActionsServiceProtocol.class).getApplicationResourceFiles(OPTION_SETS);
////        return vortexFuture;
////    }
//
//    public static VortexFuture<List<ResourceBasics>> getThemeNames() {
//        VortexFuture<List<ResourceBasics>> vortexFuture = WebMain.injector.getVortex().createFuture();
//        try {
//            vortexFuture.execute(ThemeActionsServiceProtocol.class).listThemesByType(VisualizationType.RELGRAPH_V2);
//        } catch (CentrifugeException e) {
//            ErrorDialog.showError("Failed Theme Query", e.getMessage());
//        }
//        return vortexFuture;
//    }
//    
//    public boolean isMatch(String type) {
//        return nodeThemes.get(type) != null;
//    }
//}

package csi.server.business.visualization.graph.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.CascadedTable;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.column.AbstractColumn;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphDataActions;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.graph.renderers.EdgeRenderer;
import csi.server.business.visualization.graph.renderers.NodeRenderer;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.selection.SelectionModel;

public class GraphContextConverter implements Converter {

    protected Mapper _mapper;

    public GraphContextConverter(Mapper _mapper) {
        this._mapper = _mapper;
    }

    public boolean canConvert(Class type) {
        return type.equals(GraphContext.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        GraphContext gc = (GraphContext) source;

        writer.addAttribute("dvuuid", gc.getDvUuid());
        writer.addAttribute("vuuid", gc.getVizUuid());
        writer.addAttribute("dirty", Boolean.toString(gc.isSubnetsDirty()));

        if (gc.getOptionSetName() != null) {
            writer.addAttribute("optionset", gc.getOptionSetName());
        }

        Map<String, SelectionModel> selections = gc.getSelections();
        if (selections != null) {
            writer.startNode("selections");
            context.convertAnother(selections);
            writer.endNode();
        }

        Multimap<Integer, Integer> nodesByRow = gc.getNodesByRow();
        if (nodesByRow != null) {
            writer.startNode("nodesByRow");
            context.convertAnother(nodesByRow);
            writer.endNode();
        }

        Multimap<Integer, Integer> linksByRow = gc.getLinksByRow();
        if (linksByRow != null) {
            writer.startNode("linksByRow");
            context.convertAnother(linksByRow);
            writer.endNode();
        }

        Map<String, TypeInfo> types = gc.getNodeLegend();
        if (types != null) {
            writer.startNode("typeInfo");
            context.convertAnother(types);
            writer.endNode();
        }

        Map<String, GraphLinkLegendItem> linkLegendItems = gc.getLinkLegend();
        if (linkLegendItems != null) {
            writer.startNode("graphLinkLegendItem");
            context.convertAnother(linkLegendItems);
            writer.endNode();
        }

        Display display = gc.getDisplay();
        writer.startNode("display");
        marshalDisplay(display, writer, context);
        writer.endNode();

        Graph graph = gc.getGraphData();
        if (graph != null) {
            writer.startNode("graph");
            marshalGraph(graph, false, writer, context);
            writer.endNode();
        }

        Graph visGraph = gc.getVisualGraph();
        if (visGraph != null) {
            writer.startNode("vgraph");
            marshalGraph(visGraph, true, writer, context);
            writer.endNode();
        }
    }

    private void marshalDisplay(Display display, HierarchicalStreamWriter writer, MarshallingContext context) {
        Color background = display.getBackground();
        if (background != null) {
            writer.startNode("backgroundColor");
            context.convertAnother(background);
            writer.endNode();
        }

        Dimension size = display.getSize();
        if (size != null) {
            writer.startNode("size");
            context.convertAnother(size);
            writer.endNode();
        }

        AffineTransform transform = display.getTransform();
        if (transform != null) {
            writer.startNode("transform");
            context.convertAnother(transform);
            writer.endNode();
        }
    }

    private void marshalGraph(Graph graph, boolean includeData, HierarchicalStreamWriter writer, MarshallingContext context) {

        Table nodeTable = graph.getNodeTable();
        Table edgeTable = graph.getEdgeTable();
        if (nodeTable != null) {
            writer.startNode("nodes");
            marshalTable(nodeTable, includeData, writer, context);
            writer.endNode();
        }

        if (edgeTable != null) {
            writer.startNode("edges");
            marshalTable(edgeTable, includeData, writer, context);
            writer.endNode();
        }
    }

    private void marshalTable(Table table, boolean includeData, HierarchicalStreamWriter writer, MarshallingContext context) {
        int maxRows = table.getMaximumRow() + 1;
        int columnCount = table.getColumnCount();
        writer.addAttribute("nrows", Integer.toString(maxRows));
        writer.addAttribute("ncols", Integer.toString(table.getColumnCount()));

        writer.startNode("columns");
        for (int i = 0; i < columnCount; ++i) {
            String columnName = table.getColumnName(i);
            if( columnName.equalsIgnoreCase(GraphConstants.DOC_ID)) {
                continue;
            }

            writer.startNode("column");
            writer.addAttribute("name", table.getColumnName(i));
            writer.addAttribute("index", Integer.toString(i));

            writer.startNode("type");
            Class columnType = table.getColumnType(i);
            writer.startNode(_mapper.serializedClass(Class.class));
            context.convertAnother(columnType);
            writer.endNode();
            writer.endNode();

            Object defaultVal = table.getDefault(table.getColumnName(i));
            if (defaultVal != null) {
                writer.startNode("default");

                writer.startNode(_mapper.serializedClass(Class.class));
                context.convertAnother(defaultVal.getClass());
                writer.endNode();

                writer.startNode(_mapper.serializedClass(defaultVal.getClass()));
                context.convertAnother(defaultVal);
                writer.endNode();

                writer.endNode();
            }
            writer.endNode();
        }
        writer.endNode();

        if (includeData) {
            writer.startNode("data");
            for (int i = 0; i < maxRows; ++i) {
                writer.startNode("row");
                writer.addAttribute("rowid", Integer.toString(i));
                if (!table.isValidRow(i)) {
                    writer.addAttribute("valid", "false");
                } else {
                    writer.addAttribute("valid", "true");
                    for (int j = 0; j < columnCount; ++j) {
                        Object val = table.get(i, j);
                        Object colDefault = table.getColumn(j).getDefaultValue();
                        if ((val != null) && (val != colDefault)) {
                            writer.startNode("value");
                            writer.addAttribute("colid", Integer.toString(j));
                            writer.addAttribute("colname", table.getColumnName(j));

                            writer.startNode(_mapper.serializedClass(Class.class));
                            context.convertAnother(val.getClass());
                            writer.endNode();

                            writer.startNode(_mapper.serializedClass(val.getClass()));
                            context.convertAnother(val);
                            writer.endNode();

                            writer.endNode();
                        }
                    }
                }
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String dvuuid = reader.getAttribute("dvuuid");
        String vizuuid = reader.getAttribute("vuuid");
        String optionSetName = reader.getAttribute("optionset");

        Map<String, SelectionModel> selections = null;
        Map<String, TypeInfo> types = null;
        Map<String, GraphLinkLegendItem> linkLegendItems = null;
        Map<String, Node> nodeIndex = null;
        Map<String, Edge> edgeIndex = null;

        Multimap<Integer, Integer> nodesByRow = null;
        Multimap<Integer, Integer> linksByRow = null;

        Visualization viz = new Visualization();
        Renderer nr = new NodeRenderer();
        //Renderer nr = new CompositeRenderer();
        EdgeRenderer er = new EdgeRenderer();
        DefaultRendererFactory rendererFactory = new DefaultRendererFactory(nr, er);
        viz.setRendererFactory(rendererFactory);

        Display display = new Display(viz);
        display.setHighQuality(true);
        // Dimension viewDimension = new Dimension( 600, 400 );
        // display.setSize( viewDimension );

        Graph graph = new Graph(false);
        VisualGraph visGraph = viz.addGraph("graph", graph);

        AbstractColumn column = (AbstractColumn) visGraph.getNodeTable().getColumn(VisualItem.VISIBLE);
        column.setDefaultValue(false);

        column = (AbstractColumn) visGraph.getEdgeTable().getColumn(VisualItem.VISIBLE);
        column.setDefaultValue(false);

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String nodeName = reader.getNodeName();
            if (nodeName.equals("selections")) {
                selections = (Map<String, SelectionModel>) context.convertAnother(null, HashMap.class);
            } else if (nodeName.equals("nodesByRow")) {
                nodesByRow = (Multimap<Integer, Integer>) context.convertAnother(null, HashMultimap.class);
            } else if (nodeName.equals("linksByRow")) {
                linksByRow = (Multimap<Integer, Integer>) context.convertAnother(null, HashMultimap.class);
            } else if (nodeName.equals("typeInfo")) {
                types = (Map<String, TypeInfo>) context.convertAnother(null, HashMap.class);

            } else if (nodeName.equals("graph")) {
                populateGraph(graph, reader, context);

            } else if (nodeName.equals("vgraph")) {
                populateGraph(visGraph, reader, context);

            } else if (nodeName.equals("display")) {
                populateDisplay(display, reader, context);
            } else if (nodeName.equals("graphLinkLegendItem")){
                linkLegendItems = (Map<String, GraphLinkLegendItem>) context.convertAnother(null, HashMap.class);
            }

            reader.moveUp();
        }

        return new GraphContext(dvuuid, vizuuid, graph, viz, visGraph, types, linkLegendItems, selections, nodeIndex, edgeIndex, optionSetName, nodesByRow, linksByRow);
    }

    private void populateDisplay(Display display, HierarchicalStreamReader reader, UnmarshallingContext context) {

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String nodeName = reader.getNodeName();
            if (nodeName.equals("backgroundColor")) {
                Color bg = (Color) context.convertAnother(null, Color.class);
                display.setBackground(bg);

            } else if (nodeName.equals("size")) {
                Dimension size = (Dimension) context.convertAnother(null, Dimension.class);
                display.setSize(size);

            } else if (nodeName.equals("transform")) {
                AffineTransform transform = (AffineTransform) context.convertAnother(null, AffineTransform.class);
                try {
                    display.setTransform(transform);
                } catch (NoninvertibleTransformException e) {

                }
            }

            reader.moveUp();
        }
    }

    private void populateGraph(Graph graph, HierarchicalStreamReader reader, UnmarshallingContext context) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String tname = reader.getNodeName();
            if (tname.equals("nodes")) {
                populateTable(graph, "nodes", reader, context);

            } else if (tname.equals("edges")) {
                populateTable(graph, "edges", reader, context);

            }
            reader.moveUp();
        }
    }

    private Table populateTable(Graph graph, String type, HierarchicalStreamReader reader, UnmarshallingContext context) {
        Integer nrows = Integer.valueOf(reader.getAttribute("nrows"));
//        Integer ncols = Integer.valueOf(reader.getAttribute("ncols"));

        Graph dataGraph = graph;
        if (dataGraph instanceof VisualGraph) {
            dataGraph = (Graph) ((VisualGraph) graph).getVisualization().getSourceData("graph");
        }

        Map<String, Object> itemIndex = new HashMap<String, Object>();
        HashMultimap<String, Node> nodesByType = HashMultimap.create();

        Table table = null;
        if (type.equals("nodes")) {
            table = graph.getNodeTable();
            dataGraph.putClientProperty(GraphManager.NODE_HASH_TABLE, itemIndex);
            dataGraph.putClientProperty(GraphDataActions.NODE_TYPES_LIST, nodesByType);
        } else {
            table = graph.getEdgeTable();
            dataGraph.putClientProperty(GraphManager.EDGE_HASH_TABLE, itemIndex);
        }

        if (!(table instanceof CascadedTable)) {
            int curMaxRows = table.getMaximumRow() + 1;
            if (nrows > curMaxRows) {
                table.addRows(nrows - curMaxRows);
            }
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String nodeName = reader.getNodeName();
            if (nodeName.equals("columns")) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    String colname = reader.getAttribute("name");
                    Class coltype = null;
                    Object colDefault = null;

                    while (reader.hasMoreChildren()) {
                        reader.moveDown();

                        String name = reader.getNodeName();
                        if (name.equals("type")) {
                            reader.moveDown();
                            coltype = (Class) context.convertAnother(null, Class.class);
                            reader.moveUp();
                        } else if (name.equals("default")) {
                            reader.moveDown();
                            Class valclass = (Class) context.convertAnother(null, Class.class);
                            reader.moveUp();

                            reader.moveDown();
                            colDefault = context.convertAnother(null, valclass);
                            reader.moveUp();
                        }

                        int colidx = table.getColumnNumber(colname);
                        if (colidx == -1) {
                            table.addColumn(colname, coltype, colDefault);
                        }

                        reader.moveUp();
                    }
                    reader.moveUp();
                }
            } else if (nodeName.equals("data")) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();

                    Integer row = Integer.valueOf(reader.getAttribute("rowid"));
                    String validval = reader.getAttribute("valid");

                    if ((validval != null) && Boolean.parseBoolean(validval)) {
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();

//                            Integer col = Integer.valueOf(reader.getAttribute("colid"));
                            String colname = reader.getAttribute("colname");

                            reader.moveDown();
                            Class valclass = (Class) context.convertAnother(null, Class.class);
                            reader.moveUp();

                            reader.moveDown();
                            Object val = context.convertAnother(null, valclass);
                            reader.moveUp();

                            table.set(row, colname, val);
                            if (GraphConstants.NODE_DETAIL.equals(colname)) {
                                NodeStore ns = (NodeStore) val;
                                String key = ns.getKey();
                                Node item = dataGraph.getNode(row);
                                itemIndex.put(key, item);

                                Map<String, Integer> types = ns.getTypes();
                                for (String ntype : types.keySet()) {
                                    nodesByType.put(ntype, item);
                                }
                            } else if (GraphConstants.LINK_DETAIL.equals(colname)) {
                                LinkStore ns = (LinkStore) val;
                                String key = ns.getKey();
                                Edge item = dataGraph.getEdge(row);
                                itemIndex.put(key, item);
                            }

                            reader.moveUp();
                        }
                    } else {
                        if (type.equals("nodes")) {
                            dataGraph.removeNode(row);
                        } else {
                            dataGraph.removeEdge(row);
                        }
                    }

                    reader.moveUp();
                }
            }

            reader.moveUp();
        }

        return table;
    }
}

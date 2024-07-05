/*
 * @(#) GraphSupportingRows.java,  18.02.2010
 *
 */
package csi.server.business.visualization.graph.base;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.SupportingRows;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.common.model.DimensionField;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.Property;
import csi.server.common.model.chart.ChartDimension;
import csi.server.common.model.chart.ChartField;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;

public class GraphSupportingRows {
    // NB: this is potentially recursive -- depending on the bundling depth.
    // Longer term this needs to be unrolled! i.e. better support for concept of
    // a
    // subgraph and in-place expansion/retrieval of contained nodes
    public static void addSupportingRows(AbstractGraphObjectStore node, Set<Integer> rows) {
        if (node == null) {
            return;
        }

        Iterator<List<Integer>> iterator = node.getRows().values().iterator();
        while (iterator.hasNext()) {
            rows.addAll(iterator.next());
        }

        List<AbstractGraphObjectStore> children = node.getChildren();
        if (children != null) {
            for (AbstractGraphObjectStore child : children) {
                addSupportingRows(child, rows);
            }
        }
    }

    public static Collection<Integer> getSupportingRows(List<Node> nodes, List<Edge> edges) {
        Set<Integer> supportingRows = new TreeSet<Integer>();
        if (nodes != null) {
            for (Node node : nodes) {
                NodeStore nodeStore = GraphManager.getNodeDetails(node);
                addSupportingRows(nodeStore, supportingRows);
            }
        }
        if (edges != null) {
            for (Edge edge : edges) {
                LinkStore linkStore = GraphManager.getEdgeDetails(edge);
                addSupportingRows(linkStore, supportingRows);
            }
        }

        return supportingRows;
    }

    /**
     * Determines all supporting rows for the given graph. The elementType
     * parameter is used to determine whether nodes, links, or nodes and links
     * are used for identifying the supporting rows. The list of currently
     * selected nodes and links associated w/ the graph (via properties) is
     * used.
     *
     * Valid values for elementType: nodes, links, nodes+links. An empty or null
     * value defaults to nodes.
     *
     *
     * @param graph
     * @param elementType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getSupportingRows(Graph graph, String elementType) {
        if ((elementType == null) || (elementType.length() == 0)) {
            elementType = "nodes";
        }

        Set<Integer> supportingRows = new TreeSet<Integer>();
        if (elementType.indexOf("nodes") != -1) {
            List<Node> selectedNodes = (List<Node>) graph.getClientProperty(GraphConstants.SELECTED_NODES);
            if (selectedNodes != null) {
                for (Node node : selectedNodes) {
                    NodeStore nodeStore = GraphManager.getNodeDetails(node);
                    addSupportingRows(nodeStore, supportingRows);
                }
            }
        }

        if (elementType.indexOf("links") != -1) {
            List<Edge> selectedEdges = (List<Edge>) graph.getClientProperty(GraphConstants.SELECTED_LINKS);
            if (selectedEdges != null) {
                for (Edge edge : selectedEdges) {
                    LinkStore linkStore = GraphManager.getEdgeDetails(edge);
                    addSupportingRows(linkStore, supportingRows);
                }
            }
        }
        return supportingRows.stream().map(i -> i.toString()).collect(Collectors.joining(","));
    }

    /*
     * From the Relationship Graph, pass in the selected items from the node
     * list along with the Graph.
     *
     * @param nodeIdIterator IDs of selected nodes in the RG node list. @param
     *
     * @return graph A @returns List of internal_ids of the rows.
     */
    public static List<Integer> getSupportingRows(Graph graph, Iterator<Integer> nodeIdIterator) {
        Set<Integer> supportingRows = new TreeSet<Integer>();

        while (nodeIdIterator.hasNext()) {
           Node node = graph.getNode(nodeIdIterator.next().intValue());
           NodeStore nodeStore = GraphManager.getNodeDetails(node);

            if (nodeStore != null) {
               addSupportingRows(nodeStore, supportingRows);
            }
        }

        return new ArrayList<Integer>(supportingRows);
    }

    public static String getFieldExpression(String bundleFunction, String fieldName, String[] params, boolean quote) {
        StringBuilder rtnBuff = new StringBuilder();
        if ((fieldName == null) || (fieldName.length() == 0)) {
            SupportingRows.LOG.error("No field provided for constructing an expression");
            return "";
        }

        String safeFieldName = quote ? SqlUtil.quote(fieldName) : fieldName;

        if ((null == bundleFunction) || (bundleFunction.length() == 0)) {
            rtnBuff.append(safeFieldName);
        } else if (SupportingRows.LEFT.equals(bundleFunction)) {
            String pattern = "substring( {0} for {1} )";
            String expr = MessageFormat.format(pattern, safeFieldName, params[0]);
            rtnBuff.append(expr);
        } else if (SupportingRows.RIGHT.equals(bundleFunction)) {

            String pattern = "substring( {0} from length( {0} ) - {1} + 1 ) ";
            String expr = MessageFormat.format(pattern, safeFieldName, params[0]);
            rtnBuff.append(expr);
        } else if (SupportingRows.LENGTH.equals(bundleFunction)) {

            // rtnBuff.append( "length( " ).append( safeFieldName ).append( " )" );
            rtnBuff.append("length( trim( both from ").append(safeFieldName).append(" ) )");

        } else if (SupportingRows.SUBSTRING.equals(bundleFunction)) {

            rtnBuff.append("substring( ");
            rtnBuff.append(safeFieldName);

            if ((params[0] != null) && (params[0].length() != 0)) {
                rtnBuff.append(" from ").append(params[0]);
            }

            if ((params[1] != null) && (params[1].length() > 0)) {
                rtnBuff.append(" for ").append(params[1]);
            }

            rtnBuff.append(" )");

        } else if (SupportingRows.YEAR.equals(bundleFunction)) {
            rtnBuff.append("cast(date_part( 'year', ");
            rtnBuff.append(safeFieldName);
            rtnBuff.append(" ) as integer)");

        } else if (SupportingRows.MONTH.equals(bundleFunction)) {
            rtnBuff.append("cast(date_part( 'month', ");
            rtnBuff.append(safeFieldName);
            rtnBuff.append(" ) as integer)");

        } else if (SupportingRows.DAY.equals(bundleFunction)) {
            rtnBuff.append("cast(date_part( 'day', ");
            rtnBuff.append(safeFieldName);
            rtnBuff.append(" ) as integer)");

        } else if (SupportingRows.HOUR.equals(bundleFunction)) {
            rtnBuff.append("cast(date_part( 'hour', ");
            rtnBuff.append(safeFieldName);
            rtnBuff.append(" ) as integer)");

        } else if (SupportingRows.DATE.equals(bundleFunction)) {
            rtnBuff.append("date( ").append(safeFieldName).append(" )");

        } else if (SupportingRows.MINUTE.equals(bundleFunction)) {
            rtnBuff.append("cast(date_part( 'minute', ");
            rtnBuff.append(safeFieldName);
            rtnBuff.append(" ) as integer)");

        } else if (SupportingRows.CEILING.equals(bundleFunction)) {

            rtnBuff.append("ceiling( ").append(safeFieldName).append(" )");

        } else if (SupportingRows.FLOOR.equals(bundleFunction)) {
            rtnBuff.append("floor( ").append(safeFieldName).append(" )");

        } else if (SupportingRows.YEAR_MONTH.equals(bundleFunction)) {

            // Extract month and year from date, and format it as
            // a char, yyyy-mm. E.g., "2008-06".
            // Force a leading zero for Jan - Aug, so char sort is
            // also chronological sort.
            // N.B., the doc claims that there is a TRIM function, but
            // Derby did not recognize it.
            //
            rtnBuff.append("to_char( ");
            rtnBuff.append(safeFieldName);
            rtnBuff.append(", 'YYYY-MM' )");

        } else {

            SupportingRows.LOG.warn(String.format("Unrecognized bundle-function name '%s', default to bare field '%s'", bundleFunction, fieldName));

            rtnBuff.append(safeFieldName);

        }

        return rtnBuff.toString();
    }

    public static String[] extractParams(GenericProperties bundleParams) {
        if (bundleParams == null) {
            return new String[0];
        }

        List<Property> properties = bundleParams.getProperties();
        if ((properties == null) || properties.isEmpty()) {
            return new String[0];
        }

        String[] params = new String[properties.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = properties.get(i).getValue();

        }
        return params;
    }

    public static String getFieldExpression(ChartField field, boolean quote) {
        String cast = CacheUtil.makeCastExpression(field.getDimension());
        String[] params = extractParams(field.getBundleParams());
        return getFieldExpression(field.getBundleFunction(), cast, params, false);
    }

    public static String getFieldExpression(ChartDimension dimension, int fieldIndex) {
        DimensionField field = dimension.getDimensionFields().get(fieldIndex);
        String cast = CacheUtil.makeCastExpression(field.getFieldDef());
        String[] params = extractParams(dimension.getBundleParams());
        return getFieldExpression(dimension.getBundleFunction(), cast, params, false);
    }

    public static String getFieldExpression(String bundleFunction, String fieldName, String[] params) {
        return getFieldExpression(bundleFunction, fieldName, params, true);
    }

    public static Set<Integer> getSupportingRows(Graph graph, SelectionModel selectionModel, boolean excludeBroadcast) {
        return getSupportingRows(graph, selectionModel, null, excludeBroadcast);
    }

    public static Set<Integer> getSupportingRows(Graph graph, String defName, Integer visualItemId, boolean isEdge){


        Set<Integer> allRows = new HashSet<Integer>();

        AbstractGraphObjectStore details;
        if(isEdge){
            Edge edge = graph.getEdge(visualItemId.intValue());
            details = GraphManager.getEdgeDetails(edge);
        } else {
            Node node = graph.getNode(visualItemId.intValue());
            details = GraphManager.getNodeDetails(node);
        }


        Map<String, List<Integer>> rows = details.getRows();
        if (rows == null) {
            return allRows;
        }

        if (defName != null) {
            List<Integer> list = rows.get(defName);
            if (list != null) {
                allRows.addAll(list);
            }
        } else {
            Iterator<List<Integer>> iterator = rows.values().iterator();
            while (iterator.hasNext()) {
                List<Integer> subSet = iterator.next();
                allRows.addAll(subSet);
            }
        }

        return allRows;
    }

    public static Set<Integer> getSupportingRows(Graph graph, SelectionModel selectionModel, String defName, boolean excludeBroadcast) {
        if (selectionModel == null) {
            throw new IllegalArgumentException();
        }

        Set<Integer> allRows = new HashSet<Integer>();
        // cycle through all unique nodes and links. the id stored represents
        // the object id. use that to lookup the object instance to retrieve
        // all the supporting rows for that object.

        // NB: do not check against the node type! While in most cases a node's
        // type is the same as
        // its definition; we are not guaranteed that is the case.

        for (Integer id : selectionModel.nodes) {

            Node node = graph.getNode(id.intValue());
            NodeStore details = GraphManager.getNodeDetails(node);
            Map<String, List<Integer>> rows = details.getRows();
            if (rows == null) {
                continue;
            }

            if (defName != null) {
                List<Integer> list = rows.get(defName);
                if (list != null) {
                    allRows.addAll(list);
                }
            } else {
                Iterator<List<Integer>> iterator = rows.values().iterator();
                while (iterator.hasNext()) {
                    List<Integer> subSet = iterator.next();
                    allRows.addAll(subSet);
                }
            }
        }

        for (Integer id : selectionModel.links) {
            Edge edge = graph.getEdge(id.intValue());
            LinkStore details = GraphManager.getEdgeDetails(edge);
            Map<String, List<Integer>> rows = details.getRows();
            if (rows == null) {
                continue;
            }

            if (defName != null) {
                List<Integer> list = rows.get(defName);
                if (list != null) {
                    allRows.addAll(list);
                }
            } else {
                Iterator<List<Integer>> iterator = rows.values().iterator();
                while (iterator.hasNext()) {
                    List<Integer> subSet = iterator.next();
                    allRows.addAll(subSet);
                }
            }
        }

        return allRows;

    }

}

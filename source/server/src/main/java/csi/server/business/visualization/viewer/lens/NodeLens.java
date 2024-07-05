package csi.server.business.visualization.viewer.lens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterators;

import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.NodeLensDef;
import csi.server.common.model.visualization.viewer.NodeObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.LensImage.NodeLensImage;

public class NodeLens implements Lens {
    @Override
    public LensImage focus(LensDef lensDef, Objective objective) {
        NodeLensImage nodeLensImage = new NodeLensImage();
        if (objective instanceof NodeObjective) {
            NodeObjective nodeObjective = (NodeObjective) objective;

            GraphContext graphContext = GraphServiceUtil.getGraphContext(objective.getVisualizationUuid());

            Node node = graphContext.getGraphData().getNode(nodeObjective.getFindItemDTO().getID());
            NodeStore nodeDetails = GraphManager.getNodeDetails(node);

            nodeLensImage.setImage("");

            {//labels
                ArrayList<String> labels = new ArrayList<String>(Collections.unmodifiableCollection(nodeDetails.getLabels()));
                Collections.sort(labels);
                nodeLensImage.setLabels(labels);
            }
//
            {
                //types
                ArrayList<String> types = new ArrayList<String>();
                ArrayList<String> legendOrder = graphContext.getVisualizationDef().getState().getLegendOrder();
                for (String s : legendOrder) {

                    for (Map.Entry<String, Integer> stringIntegerEntry1 : nodeDetails.getTypes().entrySet()) {
                        String type = s.substring("graph.nodes".length());
                        if (type.equals(stringIntegerEntry1.getKey())) {

                            types.add(stringIntegerEntry1.getKey() + " (" + stringIntegerEntry1.getValue() + ")");
                        }
                    }
                }
                nodeLensImage.setTypes(types);
            }
            //
            {
                Map<String, Integer> types = nodeDetails.getTypes();
                int occurrences = 0;
                for (Map.Entry<String, Integer> stringIntegerEntry : types.entrySet()) {
                    occurrences += stringIntegerEntry.getValue();
                }
                nodeLensImage.setOccurrences(occurrences);
            }
            //
            {
                nodeLensImage.setNumberOfNeighbors(Iterators.size(node.neighbors()));
            }
            //
            {
                Map<String, Property> attributes = nodeDetails.getAttributes();
                for (java.util.Map.Entry<String, Property> entry : attributes.entrySet()) {
                    Property property = entry.getValue();
                    if (property.isIncludeInTooltip() && property.hasValues()) {

                        Object o = property.getValues().get(0);
                        if (o instanceof Number) {
                            Number n = (Number) o;
                            if ((n != null) && !Double.isNaN(n.doubleValue())) {
                                switch (entry.getValue().getName()) {
                                    case "BETWEENNESS":
                                        nodeLensImage.setBetweenness(n.doubleValue());
                                        break;
                                    case "CLOSENESS":
                                        nodeLensImage.setCloseness(n.doubleValue());
                                        break;
                                    case "EIGENVECTOR":
                                        nodeLensImage.setEigenvector(n.doubleValue());
                                        break;

                                }
                            }
                        }
                    }
                }
            }

            {
                ArrayList<String> neighborLabels = new ArrayList<String>();
                ArrayList<Integer> neighborLabelCounts = new ArrayList<Integer>();
                ArrayList<String> neighborTypes = new ArrayList<String>();
                ArrayList<Integer> neighborTypeCounts = new ArrayList<Integer>();

                Iterator<Node> neighbors = node.neighbors();
                nodeIteratorToLists(neighborLabels, neighborLabelCounts, neighborTypes, neighborTypeCounts, neighbors);

                nodeLensImage.setNeighborLabels(neighborLabels);
                nodeLensImage.setNeighborLabelCounts(neighborLabelCounts);
                nodeLensImage.setNeighborTypes(neighborTypes);
                nodeLensImage.setNeighborTypeCounts(neighborTypeCounts);
            }
            if (((NodeObjective) objective).getFindItemDTO().bundle.booleanValue()) {
               //TODO:Bundle Info
//             List<AbstractGraphObjectStore> iterable = nodeDetails.getChildren();
               ArrayList<String> labels = new ArrayList<String>();
               nodeLensImage.setBundleLabels(labels);
               ArrayList<Integer> labelCounts = new ArrayList<Integer>();
               nodeLensImage.setBundleLabelCounts(labelCounts);
               ArrayList<String> types = new ArrayList<String>();
               nodeLensImage.setBundleTypes(types);
               ArrayList<Integer> typeCounts = new ArrayList<Integer>();
               nodeLensImage.setBundleTypeCounts(typeCounts);
               nodeIteratorToLists(labels, labelCounts, types, typeCounts, null);
            } else {
               nodeLensImage.setBundleLabels(new ArrayList<String>());
               nodeLensImage.setBundleLabelCounts(new ArrayList<Integer>());
               nodeLensImage.setBundleTypes(new ArrayList<String>());
               nodeLensImage.setBundleTypeCounts(new ArrayList<Integer>());
            }
        }
        return nodeLensImage;
    }

    @Override
    public List<List<?>> focus(LensDef lensDef, Objective objective, String token) {
        if ((objective instanceof NodeObjective) && (lensDef instanceof NodeLensDef)) {
            NodeObjective nodeObjective = (NodeObjective) objective;

            List<List<?>> results = new ArrayList<List<?>>();

            GraphContext graphContext = GraphServiceUtil.getGraphContext(objective.getVisualizationUuid());

            Node node = graphContext.getGraphData().getNode(nodeObjective.getFindItemDTO().getID());
            NodeStore nodeDetails = GraphManager.getNodeDetails(node);
            int i = 0;
            if (token.equals("Type")) {
                {
                    //types
                    ArrayList<String> legendOrder = graphContext.getVisualizationDef().getState().getLegendOrder();
                    for (String s : legendOrder) {
                        for (Map.Entry<String, Integer> stringIntegerEntry1 : nodeDetails.getTypes().entrySet()) {
                            String type = s.substring("graph.nodes".length());
                            if (type.equals(stringIntegerEntry1.getKey())) {
                                List<Object> list = new ArrayList<Object>();
                                list.add(Integer.valueOf(i++));
                                list.add(stringIntegerEntry1.getKey());
                                list.add(stringIntegerEntry1.getValue());
                                results.add(list);
                            }
                        }
                    }

                }
            }
            return results;
        }
        return null;
    }

    @Override
    public ViewerGridConfig getGridConfig() {
        return null;
    }

    private void nodeIteratorToLists(ArrayList<String> labels, ArrayList<Integer> labelCounts, ArrayList<String> _types,
                                     ArrayList<Integer> typeCounts, Iterator<Node> neighbors) {
       if (neighbors != null) {
          while (neighbors.hasNext()) {
             Node adjacent = neighbors.next();
             NodeStore details = GraphManager.getNodeDetails(adjacent);

             if (adjacent.getBoolean(GraphContext.IS_VISUALIZED) && !details.isHidden()) {
                String label = details.getLabel();

                if (labels.contains(label)) {
                   int i = labels.indexOf(label);
                   Integer integer = labelCounts.get(i);
                   labelCounts.add(i, integer + 1);
                } else {
                   labels.add(label);
                   labelCounts.add(1);
                }
                Map<String,Integer> types = details.getTypes();

                for (Map.Entry<String,Integer> stringIntegerEntry : types.entrySet()) {
                   String type = stringIntegerEntry.getKey();
                   if (_types.contains(type)) {
                      int i = _types.indexOf(type);
                      Integer integer = typeCounts.get(i);
                      typeCounts.add(i, integer + 1);
                   } else {
                      _types.add(type);
                      typeCounts.add(1);
                   }
                }
             }
          }
       }
    }
}

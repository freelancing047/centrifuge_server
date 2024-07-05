package csi.server.business.visualization.viewer.lens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Doubles;

import prefuse.data.Edge;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.business.visualization.viewer.dto.ViewerGridHeader;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.NodeNeighborLabelLensDef;
import csi.server.common.model.visualization.viewer.NodeObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.gwt.viz.graph.LinkDirection;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.LensImage.NodeNeighborLabelLensImage;

public class NodeNeighborLabelLens implements Lens {
   @Override
   public LensImage focus(LensDef lensDef, Objective objective) {
      NodeNeighborLabelLensImage lensImage = null;

      if ((objective instanceof NodeObjective) && (lensDef instanceof NodeNeighborLabelLensDef)) {
         lensImage = new NodeNeighborLabelLensImage();
         NodeObjective nodeObjective = (NodeObjective) objective;
         GraphContext graphContext = GraphServiceUtil.getGraphContext(objective.getVisualizationUuid());
         Node node = graphContext.getGraphData().getNode(nodeObjective.getFindItemDTO().getID());
         ArrayList<String> neighborLabels = new ArrayList<String>();
         Map<String,Integer> neighborLabelCounts = new HashMap<String,Integer>();
         Map<String,Integer> neighborLabelOccurs = new HashMap<String,Integer>();
         Collection<Node> neighborList =
            filterNeighbors(node.neighbors(), (NodeNeighborLabelLensDef) lensDef, node, neighborLabelOccurs);

         nodeIteratorToLists(neighborLabels, neighborLabelCounts, neighborLabelOccurs, neighborList.iterator());
         sortNeighbors(neighborLabels, neighborLabelCounts, neighborLabelOccurs);
         lensImage.setDistinctCount(neighborList.size());
         truncateNeighbors(neighborLabels);
         lensImage.setNeighborLabels(neighborLabels);
         lensImage.setNeighborLabelsCounts(neighborLabelCounts);
         lensImage.setNeighborLabelsOccurrances(neighborLabelOccurs);
      }
      return lensImage;
   }

   private void truncateNeighbors(ArrayList<String> neighborLabels) {
      for (int i = 10; i < neighborLabels.size();) {
         neighborLabels.remove(i);
      }
   }

   private void sortNeighbors(ArrayList<String> neighborLabels, Map<String,Integer> neighborLabelCounts,
                              Map<String,Integer> neighborLabelOccurs) {
       neighborLabels.sort(new Comparator<String>() {
         @Override
         public int compare(String left, String right) {
            if (left == null) {
               return Integer.MIN_VALUE;
            }
            if (right == null) {
               return Integer.MAX_VALUE;
            }
            String leftSub = left;

            if (left.indexOf(';') > 0) {
               leftSub = left.substring(0, left.indexOf(';'));
            }
            String rightSub = right;

            if (right.indexOf(';') > 0) {
               rightSub = right.substring(0, right.indexOf(';'));
            }
            return ComparisonChain.start()
                      .compare(-neighborLabelCounts.get(left).intValue(), -neighborLabelCounts.get(right).intValue())
                      .compare(-neighborLabelOccurs.get(left).intValue(), -neighborLabelOccurs.get(right).intValue())
                      .compare(Doubles.tryParse(leftSub) != null ? Doubles.tryParse(leftSub).doubleValue() : Double.MAX_VALUE,
                               Doubles.tryParse(rightSub) != null ? Doubles.tryParse(rightSub).doubleValue() : Double.MAX_VALUE)
                      .compare(leftSub, rightSub)
                      .result();
         }
      });
   }

   public Collection<Node> filterNeighbors(Iterator<Node> neighbors, NodeNeighborLabelLensDef lensDef,
                                           final Node objectiveNode, Map<String,Integer> neighborLabelOccurs) {
      Collection<Node> result = Collections.emptyList();

      if (neighbors != null) {
         result = new ArrayList<Node>();

         for (Iterator<Node> neighborNodes = neighbors; neighborNodes.hasNext();) {
            Node neighborNode = neighborNodes.next();
            NodeStore nodeDetails = GraphManager.getNodeDetails(neighborNode);
            boolean out = false; //default to include

            //TODO: short curcuit on success
            if (nodeDetails.isBundle() && !nodeDetails.isBundled()) {//for bundles that are not bundled
               out |= !lensDef.isIncludeBundles();
            } else if (nodeDetails.isBundled()) {
               out |= !lensDef.isIncludeBundled();
            }
            if (nodeDetails.isHidden()) {
               out |= !lensDef.isIncludeHidden();
            } else {
               out |= !lensDef.isIncludeVisible();
            }
            for (Iterator<Edge> allEdges = objectiveNode.edges(); allEdges.hasNext();) {
               Edge e = allEdges.next();
               boolean reverse = false;
               Node otherSide = e.getTargetNode();

               if (otherSide == objectiveNode) {
                  otherSide = e.getSourceNode();
                  reverse = true;
               }
               if (otherSide.getRow() != neighborNode.getRow()) {
                  continue;
               }
               LinkStore linkDetails = (LinkStore) e.get(GraphConstants.LINK_DETAIL);
               LinkDirection edgeDirection = linkDetails.getDirection();
               boolean isTarget = false;
               boolean isSource = false;
               boolean isBoth = false;
               boolean isNeither = false;

               if (reverse) {
                  if (edgeDirection == LinkDirection.REVERSE) {
                     isTarget = true;
                  }
                  if (edgeDirection == LinkDirection.FORWARD) {
                     isSource = true;
                  }
               } else {
                  if (edgeDirection == LinkDirection.REVERSE) {
                     isSource = true;
                  }
                  if (edgeDirection == LinkDirection.FORWARD) {
                     isTarget = true;
                  }
               }
               if (edgeDirection == LinkDirection.BOTH) {
                  isBoth = true;
               }
               if (edgeDirection == LinkDirection.NONE) {
                  isNeither = true;
               }
               if (isTarget) {
                  out |= !lensDef.isIncludeSources();
               }
               if (isSource) {
                  out |= !lensDef.isIncludeIncoming();
               }
               if (isBoth) {
                  out |= !lensDef.isIncludeBidirectional();
               }
               if (isNeither) {
                  out |= !lensDef.isIncludeNoDirection();
               }
               int rows = 0;

               for (List<Integer> stringListEntry : linkDetails.getRows().values()) {
                  rows += stringListEntry.size();
               }
               if (neighborLabelOccurs.get(nodeDetails.getLabel()) == null) {
                  neighborLabelOccurs.put(nodeDetails.getLabel(), rows);
               } else {
                  Integer occurs = neighborLabelOccurs.get(nodeDetails.getLabel());

                  neighborLabelOccurs.put(nodeDetails.getLabel(), occurs + rows);
               }
            }
            if (nodeDetails.isPlunked()) {
               out |= !lensDef.isIncludeUserNodes();
            } else {
               out |= !lensDef.isIncludeDataNodes();
            }
            if (!out) {
               result.add(neighborNode);
            }
         }
      }
      return result;
   }

   private void nodeIteratorToLists(ArrayList<String> labels, Map<String,Integer> labelCounts,
                                    Map<String,Integer> labelOccurs, Iterator<Node> neighbors) {
      while (neighbors.hasNext()) {
         Node adjacent = neighbors.next();
         NodeStore details = GraphManager.getNodeDetails(adjacent);

         if (adjacent.getBoolean(GraphContext.IS_VISUALIZED)) {
            String label = details.getLabel();

            if (labels.contains(label)) {
               Integer integer = labelCounts.get(label);

               labelCounts.put(label, Integer.valueOf(integer.intValue() + 1));
            } else {
               labels.add(label);
               labelCounts.put(label, Integer.valueOf(1));
            }
         }
      }
   }

    @Override
    public List<List<?>> focus(LensDef lensDef, Objective objective, String token) {
        List<List<?>> lists = new ArrayList<List<?>>();
        NodeNeighborLabelLensImage lensImage = new NodeNeighborLabelLensImage();
        NodeObjective nodeObjective;
        if ((objective instanceof NodeObjective) && (lensDef instanceof NodeNeighborLabelLensDef)) {
            nodeObjective = (NodeObjective) objective;
        } else {
            return null;
        }
        GraphContext graphContext = GraphServiceUtil.getGraphContext(objective.getVisualizationUuid());
        Node node = graphContext.getGraphData().getNode(nodeObjective.getFindItemDTO().getID());
        {
            ArrayList<String> neighborLabels = new ArrayList<String>();
            Map<String, Integer> neighborLabelCounts = new HashMap<String,Integer>();
            Map<String, Integer> neighborLabelOccurs = new HashMap<String,Integer>();
            Collection<Node> neighborList =
               filterNeighbors(node.neighbors(), (NodeNeighborLabelLensDef) lensDef, node, neighborLabelOccurs);
            nodeIteratorToLists(neighborLabels, neighborLabelCounts, neighborLabelOccurs, neighborList.iterator());
            sortNeighbors(neighborLabels, neighborLabelCounts, neighborLabelOccurs);
            lensImage.setDistinctCount(neighborList.size());
            lensImage.setNeighborLabels(neighborLabels);
            lensImage.setNeighborLabelsCounts(neighborLabelCounts);
            lensImage.setNeighborLabelsOccurrances(neighborLabelOccurs);
            for (String neighborLabel : neighborLabels) {
                ArrayList<Object> l = new ArrayList<>();
                lists.add(l);
                l.add(neighborLabel);
                l.add(neighborLabel);
                l.add(neighborLabelCounts.get(neighborLabel));
                l.add(neighborLabelOccurs.get(neighborLabel));
            }
        }
        return lists;
    }

    @Override
    public ViewerGridConfig getGridConfig() {
        ViewerGridConfig viewerGridConfig = new ViewerGridConfig();
        ArrayList<ViewerGridHeader> headers = new ArrayList<ViewerGridHeader>();
        {
            ViewerGridHeader e = new ViewerGridHeader();
            FieldDef fieldDef = new FieldDef("key", FieldType.STATIC, CsiDataType.Integer);
            fieldDef.setUuid("1");
            CsiPersistenceManager.detachEntity(fieldDef);
            e.setFieldDef(fieldDef);
            e.setVisible(false);
            headers.add(e);
        }
        {
            ViewerGridHeader e = new ViewerGridHeader();
            FieldDef fieldDef = new FieldDef("Label", FieldType.STATIC, CsiDataType.String);
            fieldDef.setUuid("2");
            CsiPersistenceManager.detachEntity(fieldDef);
            e.setFieldDef(fieldDef);
            headers.add(e);
        }
        {
            ViewerGridHeader e = new ViewerGridHeader();
            FieldDef fieldDef = new FieldDef("Count", FieldType.STATIC, CsiDataType.Integer);
            fieldDef.setUuid("3");
            CsiPersistenceManager.detachEntity(fieldDef);
            e.setFieldDef(fieldDef);
            headers.add(e);
        }
        {
            ViewerGridHeader e = new ViewerGridHeader();
            FieldDef fieldDef = new FieldDef("Occurrences", FieldType.STATIC, CsiDataType.Integer);
            fieldDef.setUuid("4");
            CsiPersistenceManager.detachEntity(fieldDef);
            e.setFieldDef(fieldDef);
            headers.add(e);
        }
        viewerGridConfig.setHeaders(headers);
        return viewerGridConfig;
    }
}

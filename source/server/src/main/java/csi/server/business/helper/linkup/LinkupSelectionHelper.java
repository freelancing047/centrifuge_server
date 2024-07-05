package csi.server.business.helper.linkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.GraphSupportingRows;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.linkup.LinkupHelper;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.model.visualization.table.TableViewDef;

public class LinkupSelectionHelper {
   private GraphContext _context;
   private SelectionModel _selectionModel;
   private Graph _graphData;
   private TreeSet<Integer> _nodes;
   private TreeSet<Integer> _edges;

   public LinkupSelectionHelper(RelGraphViewDef visualizationIn) {
      _context = GraphServiceUtil.getGraphContext(visualizationIn.getUuid());

      if (_context != null) {
         _selectionModel = _context.getOrCreateSelection(GraphManager.DEFAULT_SELECTION);
         _graphData = _context.getGraphData();

         if (_selectionModel != null) {
            _nodes = _selectionModel.nodes;
            _edges = _selectionModel.links;
         }
      }
   }

   public LinkupSelectionHelper(TableViewDef visualizationIn) {
   }

   public List<LinkupHelper> identifySupportingRows(List<LinkupHelper> parameterBuilderListIn) throws CentrifugeException {
      List<LinkupHelper> myParameterBuilderList = new ArrayList<LinkupHelper>();

      if (_context != null) {
         List<LinkupHelper> myNodeLinkups = new ArrayList<LinkupHelper>();
         List<LinkupHelper> myEdgeLinkups = new ArrayList<LinkupHelper>();
         Collection<Integer> myGeneralRows = null;

         for (LinkupHelper myHelper : parameterBuilderListIn) {
            if (myHelper.getNodeDefId() != null) {
               myNodeLinkups.add(myHelper);
            } else if (myHelper.getEdgeDefId() != null) {
               myEdgeLinkups.add(myHelper);
            } else {
               if (myGeneralRows == null) {
                  myGeneralRows = identifyGeneralSupportingRows();
               }
               myHelper.addRows(myGeneralRows);
            }
         }
         if (!myNodeLinkups.isEmpty()) {
            identifyNodeSupportingRows(myNodeLinkups);
         }
         if (!myEdgeLinkups.isEmpty()) {
            identifyEdgeSupportingRows(myEdgeLinkups);
         }
         for (LinkupHelper myHelper : parameterBuilderListIn) {
            if (myHelper.getRowCount() > 0) {
               myParameterBuilderList.add(myHelper);
            }
         }
      }
      return myParameterBuilderList;
   }

   private void identifyNodeSupportingRows(List<LinkupHelper> parameterBuilderListIn) throws CentrifugeException {
      if (_nodes != null) {
         for (Integer myId : _nodes) {
            Node myNode = _graphData.getNode(myId);
            NodeStore myStore = GraphManager.getNodeDetails(myNode);

            for (LinkupHelper myhelper : parameterBuilderListIn) {
               String myNodeId = myhelper.getNodeDefId();

               if ((myNodeId != null) && myStore.incorporatesNodeDef(myNodeId)) {
                  myhelper.addRows(myStore.getRows().get(myNodeId));
               }
            }
         }
      }
   }

   private void identifyEdgeSupportingRows(List<LinkupHelper> parameterBuilderListIn) throws CentrifugeException {
      if (_edges != null) {
         for (Integer myId : _edges) {
            Edge myEdge = _graphData.getEdge(myId);
            LinkStore myStore = GraphManager.getEdgeDetails(myEdge);

            for (LinkupHelper myhelper : parameterBuilderListIn) {
               String myEdgeId = myhelper.getEdgeDefId();

               if ((myEdgeId != null) && myStore.incorporatesNodeDef(myEdgeId)) {
                  myhelper.addRows(myStore.getRows().get(myEdgeId));
               }
            }
         }
      }
   }

   private Collection<Integer> identifyGeneralSupportingRows() throws CentrifugeException {
      Collection<Integer> mySupportRows = null;

      if (_context != null) {
         List<Node> myNodes = null;
         List<Edge> myEdges = null;

         if ((_nodes != null) && !_nodes.isEmpty()) {
            myNodes = new ArrayList<Node>();

            for (Integer myId : _nodes) {
               myNodes.add(_graphData.getNode(myId));
            }
         }
         if ((_edges != null) && !_edges.isEmpty()) {
            myEdges = new ArrayList<Edge>();

            for (Integer myId : _edges) {
               myEdges.add(_graphData.getEdge(myId));
            }
         }
         mySupportRows = GraphSupportingRows.getSupportingRows(myNodes, myEdges);
      }
      return mySupportRows;
   }
}

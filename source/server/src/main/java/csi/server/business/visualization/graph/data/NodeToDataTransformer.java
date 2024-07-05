package csi.server.business.visualization.graph.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import prefuse.data.Node;
import prefuse.visual.NodeItem;

import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.NodeStore;

public class NodeToDataTransformer implements Function<Node,DBObject> {
   private static final BigDecimal BIG_DECIMAL_ONE_HUNDRED = BigDecimal.valueOf(100);

    private GraphStorage graphStorage;

    @Override
    public DBObject apply(Node node) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        NodeStore details = GraphManager.getNodeDetails(node);

        if( details.getDocId() != null ) {
            builder.add(Helper.DOC_ID, details.getDocId());
        }

        // start off with non-internal attributes.
        Data.addAttributes(builder, details, false, graphStorage);

        String appId = Data.resolveAppId(details);
        builder.add(Helper.APP_ID, appId);

        if (details.getLabel() != null) {
            builder.append(Data.Label, details.getLabel());
        }

        Data.buildTypes(builder, details, graphStorage);

        builder.push(Helper.INTERNAL);
        {
            Data.addDefCounts(builder, details, graphStorage);
            addInternals(builder, details);
            addPosition( builder, node );
            if( node instanceof NodeItem ) {
                addVisuals(builder, (NodeItem)node);
            }

            builder.add(Data.PrimaryType, details.getType());

            boolean flag = node.getBoolean(GraphContext.IS_VISUALIZED);
            if( !flag ) {
                builder.add(Data.Visualized, Boolean.valueOf(flag));
            }

            builder.add(Data.Plunked, Boolean.valueOf(details.isPlunked()));
        }

        DBObject data = builder.get();
        return data;
    }

    private void addVisuals(BasicDBObjectBuilder builder, NodeItem node) {
//        Graph graph = node.getGraph();
//        Map<String, TypeInfo> nodeLegend = GraphContext.getNodeLegend(graph);

        NodeStore details = GraphManager.getNodeDetails(node);

        // shape, icon, size...
//        if( node.getSize() != 1.0d) {
//            builder.add(Data.Size, node.getSize() );
//        }
        if( details.getSizeMode() != 0) {
        	builder.add(Data.SizeMode, Integer.valueOf(details.getSizeMode()));
        }
    }

    private void addPosition(BasicDBObjectBuilder builder, Node node) {
        if( node instanceof NodeItem ) {
            NodeItem ni = (NodeItem) node;
            double x = ni.getX();
            double y = ni.getY();

            builder.push(Data.Position);
            builder.add(Data.X, x);
            builder.add(Data.Y, y);
            builder.pop();
        }
    }

   private void addInternals(BasicDBObjectBuilder builder, NodeStore details) {
      Data.addAttributes(builder, details, true, graphStorage);

      // clean up noisy stuff -- internal attributes like type
      // are not efficient.  remove these since we capture them elsewheere
      Data.removeRedundantAttributes( builder );

      if (details.getIcon() != null) {
         builder.add(Data.Icon, details.getIcon());
      }
      if (details.getShape() != null) {
         builder.add(Data.Shape, details.getShape());
      }
      if (BigDecimal.valueOf(details.getRelativeSize()).compareTo(BigDecimal.ONE) != 0) {
         builder.add(Data.RelativeSize, Double.valueOf(details.getRelativeSize()));
      }
      if (details.isAnchored()) {
         builder.add(Data.Anchored, Boolean.TRUE);
      }
      if (details.isHideLabels()) {
         builder.add(Data.HideLabels, Boolean.TRUE);
      }
      if (details.isHidden()) {
         builder.add(Data.Hidden, Boolean.TRUE);
      }
      if (details.getColor() != null) {
         builder.add(Data.Color, details.getColor());
      }
      if (details.getParent() != null) {
         AbstractGraphObjectStore parent = details.getParent();
         Object docId = parent.getDocId();

         if (docId == null) {
            docId = ObjectId.get();
            parent.setDocId(docId);
         }
         builder.add(Data.Parent, docId);
      }
      List<AbstractGraphObjectStore> children = details.getChildren();

      if ((children != null) && !children.isEmpty()) {
         List childIds = new ArrayList(children.size());

         for (AbstractGraphObjectStore child : children) {
            if (child.getDocId() == null) {
               // typically dealing with an intermediate bundle node...
               child.setDocId(ObjectId.get());
            }
            childIds.add(child.getDocId());
         }
         builder.add(Data.Children, childIds);
      }
      if (details.getScale() != 1) {
         builder.add(Data.Scale, Integer.valueOf(details.getScale()));
      }
      if (BigDecimal.valueOf(details.getTransparency()).compareTo(BIG_DECIMAL_ONE_HUNDRED) != 0) {
         builder.add(Data.Transparency, Double.valueOf(details.getTransparency()));
      }
      if (details.getSpecID() != null) {
         builder.add(Data.SpecId, details.getSpecID());
      }
      if (!details.isVisualized()) {
         builder.add(Data.Visualized, Boolean.FALSE);
      }
   }

   public void setGraphStorage(GraphStorage graphStorage) {
      this.graphStorage = graphStorage;
   }
}

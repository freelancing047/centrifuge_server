package csi.server.business.visualization.graph.data;

import java.math.BigDecimal;
import java.util.function.Function;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import edu.uci.ics.jung.graph.util.EdgeType;

import prefuse.data.Edge;
import prefuse.data.Node;

import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.shared.gwt.viz.graph.LinkDirection;

public class EdgeToDataTransformer implements Function<Edge,DBObject> {
   private static final BigDecimal BIG_DECIMAL_ONE_HUNDRED = BigDecimal.valueOf(100);

   private GraphStorage graphStorage;

    @Override
    public DBObject apply(Edge edge) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        LinkStore details = GraphManager.getEdgeDetails(edge);

        Data.addAttributes(builder, details, false, graphStorage);

        String appId = resolveLinkKey(details);
        builder.add(Helper.APP_ID, appId);

        if (details.getLabel() != null) {
            builder.append(Data.Label, details.getLabel());
        }

        Data.buildTypes(builder, details, graphStorage);

        builder.push(Helper.INTERNAL);
        {
            Data.addDefCounts(builder, details, graphStorage);
            addInternals(builder, details);
            addEdgeStructure(builder, edge);

            builder.add(Data.PrimaryType, details.getType());

            boolean flag = edge.getBoolean(GraphContext.IS_VISUALIZED);
            if( !flag ) {
                builder.add(Data.Visualized, flag);
            }
            builder.add(Data.Plunked, details.isPlunked());
        }
        builder.pop();
        return builder.get();
    }

    private String resolveLinkKey(LinkStore details) {
        String key = details.getKey();

        if ((details.getKey() == null) || details.getKey().isEmpty()) {
            String sourceKey = details.getFirstEndpoint().getKey();
            String targetKey = details.getSecondEndpoint().getKey();
            key = sourceKey + "+" + targetKey;
        }
        return key;
    }

    private void addEdgeStructure(BasicDBObjectBuilder builder, Edge edge) {
        Node sn = edge.getSourceNode();
        Node tn = edge.getTargetNode();

        ObjectId sourceId = Data.getNodeDocId(sn);
        ObjectId targetId = Data.getNodeDocId(tn);

        builder.add(Helper.SOURCE, sourceId);
        builder.add(Helper.TARGET, targetId);

        boolean directed = edge.isDirected();
        if (directed) {
            builder.add(Helper.EDGE_TYPE, EdgeType.DIRECTED.toString());
        } else {
            builder.add(Helper.EDGE_TYPE, EdgeType.UNDIRECTED.toString());
        }

    }

   private void addInternals(BasicDBObjectBuilder builder, LinkStore details) {
      Data.addAttributes(builder, details, true, graphStorage);
      Data.removeRedundantAttributes(builder);

      /* -- start LinkStore specific attributes -- */
      if (details.getStyle() != null) {
         builder.add(Data.Style, details.getStyle());
      }
      if (BigDecimal.valueOf(details.getWidth()).compareTo(BigDecimal.ONE) != 0) {
         builder.add(Data.Width, details.getWidth());
      }
      if (details.getScale() != 1) {
         builder.add(Data.Scale, details.getScale());
      }
      if (BigDecimal.valueOf(details.getTransparency()).compareTo(BIG_DECIMAL_ONE_HUNDRED) != 0) {
         builder.add(Data.Transparency, details.getTransparency());
      }
      if (details.getDirection() != LinkDirection.NONE) {
         builder.add(Data.EdgeDirection, details.getDirection().toString());
      }
      if (details.getCountForward() != 0) {
         builder.add(Data.CountForward, details.getCountForward());
      }
      if (details.getCountReverse() != 0) {
         builder.add(Data.CountReverse, details.getCountReverse());
      }
      if (details.getCountNone() != 0) {
         builder.add(Data.CountNone, details.getCountNone());
      }
      /* -- end LinkStore specific attributes -- */

      if (details.getColor() != null) {
         builder.add(Data.Color, details.getColor());
      }
      if (details.isHidden()) {
         builder.add(Data.Hidden, true);
      }
      if (details.getSpecID() != null) {
         builder.add(Data.SpecId, details.getSpecID());
      }
      if (details.getSizeMode() != 0) {
         builder.add(Data.SizeMode, details.getSizeMode());
      }
      if (details.isHideLabels()) {
         builder.add(Data.HideLabels, true);
      }
   }

   public void setGraphStorage(GraphStorage graphStorage) {
      this.graphStorage = graphStorage;
   }
}

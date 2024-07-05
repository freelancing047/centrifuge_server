package csi.server.business.visualization.graph.metrics.centrality;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.NOPTransformer;

import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Graph;

import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphMetrics;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class VertexScoreHelper<V, E> {

    protected RelGraphViewDef def;

    protected Map<String, NodeDef> nodeDefMap;

    public VertexScoreHelper(RelGraphViewDef viewDef) {
        if (viewDef == null) {
            throw new IllegalArgumentException();
        }

        def = viewDef;

        buildMap();
    }

    private void buildMap() {
        nodeDefMap = new HashMap<String, NodeDef>();
        for (NodeDef nodeDef : def.getNodeDefs()) {
            nodeDefMap.put(nodeDef.getName(), nodeDef);
        }
    }

    public List<Ranking<V>> getRankings(VertexScorer<V, ? extends Number> scorer, Collection<V> vertices) {

        return getRankings(scorer, vertices, NOPTransformer.<Number> getInstance());
    }

   public List<Ranking<V>> getRankings(VertexScorer<V, ? extends Number> scorer, Collection<V> vertices, double normal) {
      if (BigDecimal.valueOf(normal).compareTo(BigDecimal.ZERO) == 0) {
         throw new IllegalArgumentException("normal");
      }
      Transformer<Number, Number> transformer = getNormalizedTransformer(normal);
      return getRankings(scorer, vertices, transformer);
   }

    private Transformer<Number, Number> getNormalizedTransformer(final double normal) {
        Transformer<Number, Number> transformer = new Transformer<Number, Number>() {

            @Override
            public Number transform(Number value) {
                return (value.doubleValue() / normal);
            }
        };
        return transformer;
    }

    public List<Ranking<V>> getRankings(VertexScorer<V, ? extends Number> scorer, Collection<V> vertices, Transformer<Number, Number> transform) {
        List<Ranking<V>> rankings = new ArrayList<Ranking<V>>();

        for (V v : vertices) {
            Number score = scorer.getVertexScore(v);
            score = transform.transform(score);
            rankings.add(new Ranking<V>(0, score.doubleValue(), v));
        }

        Collections.sort(rankings);
        return rankings;
    }

    public Map<V, Object> getRankingsAsMap(VertexScorer<V, ? extends Number> scorer, Collection<V> vertices, double normal) {
        Transformer<Number, Number> xform = getNormalizedTransformer(normal);
        HashMap<V, Object> map = new HashMap<V, Object>();

        for (V v : vertices) {
            Number score = scorer.getVertexScore(v);
            score = xform.transform(score);
            map.put(v, score);
        }

        return map;
    }

   public void updateVertexScores(Graph<V,E> graph, Map<String,Node> nodeIDMap,
                                  VertexScorer<V,? extends Number> scorer, QName propertyName, int subGraphId) {
      // do not use nodeMap to calculate normal value because in case of bundle, it
      // contains both bundled node and the nodes that were selected to form that
      // bundled node.
      double normal = (graph.getVertices().size() - 1) * (graph.getVertices().size() - 2);
      // This only happens if vertices.size() is 2, in which case we don't need to
      // normalize

      if (BigDecimal.valueOf(normal).compareTo(BigDecimal.ZERO) == 0) {
         normal = 1;
      }
      String simpleName = propertyName.getLocalPart();
      Transformer<Number,Number> xform = getNormalizedTransformer(normal);

      for (V vkey : graph.getVertices()) {
         Node node = nodeIDMap.get(vkey);
         NodeStore data = GraphManager.getNodeDetails(node);
         Map<String,Property> attributes = data.getAttributes();

         String specID = data.getSpecID();
         Property property = getOrCreateProperty(attributes, propertyName, simpleName, specID);
         Property subGraphIdProp = getOrCreateProperty(attributes, GraphMetrics.SUBGRAPH_PROP_NAME, "subGraphId", specID);
         Number vertexScore = scorer.getVertexScore(vkey);
         vertexScore = xform.transform(vertexScore);

         // property.getValues().add( vertexScore.doubleValue() );
         property.getValues().clear();
         property.getValues().add(vertexScore.doubleValue());
         subGraphIdProp.getValues().clear();
         subGraphIdProp.getValues().add(subGraphId);
      }
   }

    private Property getOrCreateProperty(Map<String, Property> attributes, QName propertyName, String simpleName, String specID) {
        Property property = attributes.get(simpleName);
        if (property == null) {
            // property = new Property( propertyName );
            property = new Property(propertyName.getLocalPart());

            if (specID != null) {
                AttributeDef attr = findAttributeDef(specID, simpleName);
                if (attr != null) {
                    property.setIncludeInTooltip(attr.isIncludeInTooltip());
                    property.setHideEmptyInTooltip(attr.isHideEmptyInTooltip());
                }
            }

            attributes.put(simpleName, property);
        }
        return property;
    }

    private AttributeDef findAttributeDef(String specID, String simpleName) {
        AttributeDef attribute = null;

        NodeDef nodeDef = nodeDefMap.get(specID);
        if (nodeDef != null) {
            attribute = nodeDef.getAttributeDef(simpleName);
        }

        return attribute;
    }

    public void clearScore(Map<String, Property> attributes, QName propName, String specID) {
        Property property = getOrCreateProperty(attributes, propName, propName.getLocalPart(), specID);
        property.getValues().clear();
    }

}

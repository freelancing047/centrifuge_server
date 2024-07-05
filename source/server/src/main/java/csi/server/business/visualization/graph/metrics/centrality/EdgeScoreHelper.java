package csi.server.business.visualization.graph.metrics.centrality;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.scoring.EdgeScorer;
import edu.uci.ics.jung.graph.Graph;

import prefuse.data.Edge;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class EdgeScoreHelper<V, E> {

    protected RelGraphViewDef def;

    protected Map<String, LinkDef> linkDefMap;

    public EdgeScoreHelper(RelGraphViewDef viewDef) {
        if (viewDef == null) {
            throw new IllegalArgumentException();
        }

        def = viewDef;

        buildMap();
    }

    private void buildMap() {
        linkDefMap = new HashMap<String, LinkDef>();
        for (LinkDef linkDef : def.getLinkDefs()) {
            linkDefMap.put(linkDef.getName(), linkDef);
        }
    }

    @SuppressWarnings("unchecked")
    public void updateEdgeScores(Graph<V, E> subGraph, Map<String, Edge> edgeIDMap, EdgeScorer<E, ? extends Number> scorer, QName property) {
        //do not use edgeMap to calculate normal value because in case of bundle, the bundled edges are listed twice.
//        double normal = (subGraph.getEdges().size() - 1) * (subGraph.getEdges().size() - 2);
//        Transformer<Number, Number> xform = getNormalizedTransformer(normal);

        String simpleName = property.getLocalPart();

        for (E eKey : subGraph.getEdges()) {
            //assume E is String
            String key = (String) eKey;
            Edge edge = edgeIDMap.get(key);
            if (edge == null) {
                // We have a reversed key, avoid Null Pointer Exception
                edge = edgeIDMap.get(GraphManager.invertLinkID(key));
            }

            LinkStore data = GraphManager.getEdgeDetails(edge);
            String specID = data.getSpecID();

            Map<String, Property> attributes = data.getAttributes();
            Property prop = getOrCreateProperty(attributes, property, simpleName, specID);
            Number score = scorer.getEdgeScore(eKey);

            Number previous = null;
            if(!prop.getValues().isEmpty()){
                previous = (Number)prop.getValues().get(0);
            }
            if (previous != null) {
                //Betweenness is calculated by summing the number on both direction
                score = score.doubleValue()  +  previous.doubleValue();
            }
            prop.getValues().clear();
            prop.getValues().add(score);
            GraphManager.setLinkDetails(edge, data);
        }

    }

    @SuppressWarnings("unchecked")
    public void clearScores(Graph<V, E> subGraph, Map<String, Edge> edgeIDMap, QName property) {

        String simpleName = property.getLocalPart();

        for (E eKey : subGraph.getEdges()) {
            //assume E is String
            String key = (String) eKey;
            Edge edge = edgeIDMap.get(key);
            if (edge == null) {
                // We have a reversed key, avoid Null Pointer Exception
                edge = edgeIDMap.get(GraphManager.invertLinkID(key));
            }

            LinkStore data = GraphManager.getEdgeDetails(edge);
            String specID = data.getSpecID();

            Map<String, Property> attributes = data.getAttributes();
            Property prop = getOrCreateProperty(attributes, property, simpleName, specID);

            prop.getValues().clear();
        }

    }

    private Property getOrCreateProperty(Map<String, Property> attributes, QName propertyName, String simpleName, String specID) {
        //TODO: Simple property not working with the change to Postgres, not sure why though. Might have to do with Data.java, but that seems unchanged.
        Property property = attributes.get(simpleName);
        if (property == null) {
            property = new Property(propertyName.getLocalPart());

            AttributeDef attribute = findAttributeDef(specID, simpleName);
            if (attribute != null) {
                property.setIncludeInTooltip(attribute.isIncludeInTooltip());
                property.setHideEmptyInTooltip(attribute.isHideEmptyInTooltip());
            }

            attributes.put(simpleName, property);
        }
        return property;
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

    private AttributeDef findAttributeDef(String specID, String simpleName) {
        AttributeDef attribute = null;

        LinkDef linkDef = linkDefMap.get(specID);
        if (linkDef != null) {
            attribute = linkDef.getAttributeDef(simpleName);
        }

        return attribute;
    }

}

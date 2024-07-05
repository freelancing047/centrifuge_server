package csi.server.business.visualization.graph.stat;

import java.util.Set;

import prefuse.data.Edge;
import prefuse.data.Node;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.stat.GraphStatisticalPopulation.TypePopulation;
import csi.server.common.model.visualization.graph.GraphConstants;

public interface GraphObjectProcessor {

    public GraphStatisticalEntity process(Object o);

    public class NodeTypeObjectProcessor implements GraphObjectProcessor {

        private final TypePopulation population;


        public NodeTypeObjectProcessor(TypePopulation population) {
            this.population = population;
        }

        @Override
        public GraphStatisticalEntity process(Object o) {
            if (o instanceof Node) {
                Node input = (Node) o;

                NodeStore ns = (NodeStore) input.get(GraphConstants.NODE_DETAIL);
                Set<String> keys = ns.getTypes().keySet();
                keys.iterator();
                for (String type : keys) {
                    if (type.equals(population.getType())) {
                        return ns;

                    }
                }
            }
            return null;
        }

    }

    public class LinkObjectProcessor implements GraphObjectProcessor {

        public LinkObjectProcessor() {
        }

        @Override
        public GraphStatisticalEntity process(Object o) {
            if (o instanceof Edge) {
                Edge input = (Edge) o;
                return (LinkStore) input.get(GraphConstants.LINK_DETAIL);
            }
            return null;
        }

    }    public class NodeObjectProcessor implements GraphObjectProcessor {

        public NodeObjectProcessor() {
        }

        @Override
        public GraphStatisticalEntity process(Object o) {
            if (o instanceof Node) {
                Node input = (Node) o;
                return (NodeStore) input.get(GraphConstants.NODE_DETAIL);
            }
            return null;
        }

    }


    public class LinkTypeObjectProcessor implements GraphObjectProcessor {

        private final GraphStatisticalPopulation.LinkTypePopulation population;


        public LinkTypeObjectProcessor(GraphStatisticalPopulation.LinkTypePopulation population) {
            this.population = population;
        }



        @Override
        public GraphStatisticalEntity process(Object o) {
            if (o instanceof Edge) {
                Edge input = (Edge) o;
                LinkStore store = (LinkStore) input.get(GraphConstants.LINK_DETAIL);
                Set<String> keys = store.getTypes().keySet();
                keys.iterator();
                for (String type : keys) {
                    if (type.equals(population.getType())) {
                        return store;
                    }
                }
            }
            return null;
        }

    }
}

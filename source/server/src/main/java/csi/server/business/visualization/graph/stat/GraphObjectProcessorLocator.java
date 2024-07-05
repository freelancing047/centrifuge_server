package csi.server.business.visualization.graph.stat;

import csi.server.business.visualization.graph.stat.GraphObjectProcessor.NodeTypeObjectProcessor;
import csi.server.business.visualization.graph.stat.GraphStatisticalPopulation.TypePopulation;

public class GraphObjectProcessorLocator {

    public static GraphObjectProcessor get(GraphStatisticalPopulation population) {
        if (population instanceof TypePopulation) {
            return new NodeTypeObjectProcessor((TypePopulation) population);
        }
        if(population instanceof GraphStatisticalPopulation.NodePopulation){
            return new GraphObjectProcessor.NodeObjectProcessor();
        }
        if (population instanceof GraphStatisticalPopulation.LinkPopulation) {
            return new GraphObjectProcessor.LinkObjectProcessor();
        }
        if (population instanceof GraphStatisticalPopulation.LinkTypePopulation) {
            return new GraphObjectProcessor.LinkTypeObjectProcessor((GraphStatisticalPopulation.LinkTypePopulation) population);
        }
        return null;
    }
    
    
    private GraphObjectProcessorLocator() {
        // No-OP
    }
}

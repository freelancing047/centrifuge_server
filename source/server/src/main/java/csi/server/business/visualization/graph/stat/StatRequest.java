package csi.server.business.visualization.graph.stat;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class StatRequest implements IsSerializable {

    private ArrayList<GraphStatistic> statistics;
    private ArrayList<GraphStatisticalPopulation> populations;

    public StatRequest(ArrayList<GraphStatistic> statistics, ArrayList<GraphStatisticalPopulation> populations) {
        this.statistics = statistics;
        this.populations = populations;
    }

    public ArrayList<GraphStatisticalPopulation> getPopulations() {
        return populations;
    }

    public ArrayList<GraphStatistic> getStatistics() {
        return statistics;
    }
    
    
    public StatRequest() {
    }

}

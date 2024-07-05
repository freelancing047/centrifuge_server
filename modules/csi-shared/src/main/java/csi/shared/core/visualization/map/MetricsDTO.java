package csi.shared.core.visualization.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MetricsDTO implements Serializable {
    List<MetricPair> metrics;

    public MetricsDTO(){
        metrics = new ArrayList<MetricPair>();
    }

    public void add(String name, String value){
        metrics.add(new MetricPair(name, value));
    }

    public void add(String name, Integer value){
        add(name, Integer.toString(value));
    }

    public List<MetricPair> getMetrics() {
        return metrics;
    }
}

package csi.shared.gwt.viz.viewer.LensImage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class NodeNeighborLabelLensImage implements LensImage {
    private List<String> neighborLabels = Lists.newArrayList();
    private Map<String,Integer> neighborLabelsCounts = Maps.newHashMap();
    private Map<String,Integer> neighborLabelsOccurrances = Maps.newHashMap();
    private int distinctCount;

    public Map<String, Integer> getNeighborLabelsCounts() {
        return neighborLabelsCounts;
    }

    public void setNeighborLabelsCounts(Map<String, Integer> neighborLabelsCounts) {
        this.neighborLabelsCounts = neighborLabelsCounts;
    }

    public Map<String, Integer> getNeighborLabelsOccurrances() {
        return neighborLabelsOccurrances;
    }

    public void setNeighborLabelsOccurrances(Map<String, Integer> neighborLabelsOccurrances) {
        this.neighborLabelsOccurrances = neighborLabelsOccurrances;
    }

    public List<String> getNeighborLabels() {
        return neighborLabels;
    }

    public void setNeighborLabels(List<String> neighborLabels) {
        this.neighborLabels = neighborLabels;
    }
    private String lensDef;

    @Override
    public String getLensDef() {
        return lensDef;
    }

    public void setLensDef(String lensDef) {
        this.lensDef = lensDef;
    }

    @Override
    public String getLabel() {
        return "Neighbor Labels";
    }

    @Override
    public void setLabel(String s) {

    }

    public void setDistinctCount(int distinctCount) {
        this.distinctCount = distinctCount;
    }

    public int getDistinctCount() {
        return distinctCount;
    }
}

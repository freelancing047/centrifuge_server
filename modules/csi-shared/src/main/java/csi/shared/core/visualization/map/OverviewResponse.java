package csi.shared.core.visualization.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class OverviewResponse implements Serializable {

    /**
     * Colors for the overview chart.
     */
    private List<Integer> overviewColors = new ArrayList<Integer>();

    private int totalCategories = -1;

    /**
     * The number of categories represented by 1 pixel in the overview chart, defaults to 1.
     */
    private int overviewBinSize = 1;

    public List<Integer> getOverviewColors() {
        return overviewColors;
    }

    public void setOverviewColors(List<Integer> overviewColors) {
        this.overviewColors = overviewColors;
    }

    public int getOverviewBinSize() {
        return overviewBinSize;
    }

    public void setOverviewBinSize(int overviewBinSize) {
        this.overviewBinSize = overviewBinSize;
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(int totalCategories) {
        this.totalCategories = totalCategories;
    }
}

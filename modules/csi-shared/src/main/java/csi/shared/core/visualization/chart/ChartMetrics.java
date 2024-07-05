package csi.shared.core.visualization.chart;

import java.io.Serializable;

public class ChartMetrics  implements Serializable {
    private Number max, min;
    private long categoryCount;
    String seriesName;

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public long getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }
}

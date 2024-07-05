package csi.shared.core.visualization.chart;

import java.io.Serializable;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class OverviewRequest implements Serializable {

    private int currentWidth;
    private List<Number> seriesData;
    private String vizUuid;

    public int getCurrentWidth() {
        return currentWidth;
    }

    public void setCurrentWidth(int currentWidth) {
        this.currentWidth = currentWidth;
    }

    public List<Number> getSeriesData() {
        return seriesData;
    }

    public void setSeriesData(List<Number> seriesData) {
        this.seriesData = seriesData;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }
}

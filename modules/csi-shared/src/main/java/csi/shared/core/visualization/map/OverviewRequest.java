package csi.shared.core.visualization.map;

import java.io.Serializable;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class OverviewRequest implements Serializable {

    private int currentWidth;
    private String vizUuid;
    private String dvUuid;

    public int getCurrentWidth() {
        return currentWidth;
    }

    public void setCurrentWidth(int currentWidth) {
        this.currentWidth = currentWidth;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }

    public String getDvUuid() {
        return dvUuid;
    }

    public void setDvUuid(String dvUuid) {
        this.dvUuid = dvUuid;
    }
}

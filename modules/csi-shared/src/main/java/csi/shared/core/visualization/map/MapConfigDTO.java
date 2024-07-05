package csi.shared.core.visualization.map;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.List;

public class MapConfigDTO implements Serializable, IsSerializable {
    private List<String> boundaryLayerIds;
    private int pointLimit;
    private int linkLimit;
    private int typeLimit;
    private String defaultBasemapOwner;
    private String defaultBasemapId;
    private int frontendToggleThreshold;
    private int frontendZoomThreshold;
    private int minPlaceSize;
    private int maxPlaceSize;
    private int minTrackWidth;
    private int maxTrackWidth;
    private String defaultThemeName;
    private int detailLevel;
    private String locatorUrl;

    public MapConfigDTO() {
    }

    public List<String> getBoundaryLayerIds() {
        return boundaryLayerIds;
    }

    public void setBoundaryLayerIds(List<String> boundaryLayerIds) {
        this.boundaryLayerIds = boundaryLayerIds;
    }

    public int getPointLimit() {
        return pointLimit;
    }

    public void setPointLimit(int pointLimit) {
        this.pointLimit = pointLimit;
    }

    public int getLinkLimit() {
        return linkLimit;
    }

    public void setLinkLimit(int linkLimit) {
        this.linkLimit = linkLimit;
    }

    public int getTypeLimit() {
        return typeLimit;
    }

    public void setTypeLimit(int typeLimit) {
        this.typeLimit = typeLimit;
    }

    public String getDefaultBasemapOwner() {
        return defaultBasemapOwner;
    }

    public void setDefaultBasemapOwner(String defaultBasemapOwner) {
        this.defaultBasemapOwner = defaultBasemapOwner;
    }

    public String getDefaultBasemapId() {
        return defaultBasemapId;
    }

    public void setDefaultBasemapId(String defaultBasemapId) {
        this.defaultBasemapId = defaultBasemapId;
    }

    public int getFrontendToggleThreshold() {
        return frontendToggleThreshold;
    }

    public void setFrontendToggleThreshold(int frontendToggleThreshold) {
        this.frontendToggleThreshold = frontendToggleThreshold;
    }

    public int getFrontendZoomThreshold() {
        return frontendZoomThreshold;
    }

    public void setFrontendZoomThreshold(int frontendZoomThreshold) {
        this.frontendZoomThreshold = frontendZoomThreshold;
    }

    public int getMinPlaceSize() {
        return minPlaceSize;
    }

    public void setMinPlaceSize(int minPlaceSize) {
        this.minPlaceSize = minPlaceSize;
    }

    public int getMaxPlaceSize() {
        return maxPlaceSize;
    }

    public void setMaxPlaceSize(int maxPlaceSize) {
        this.maxPlaceSize = maxPlaceSize;
    }

    public int getMinTrackWidth() {
        return minTrackWidth;
    }

    public void setMinTrackWidth(int minTrackWidth) {
        this.minTrackWidth = minTrackWidth;
    }

    public int getMaxTrackWidth() {
        return maxTrackWidth;
    }

    public void setMaxTrackWidth(int maxTrackWidth) {
        this.maxTrackWidth = maxTrackWidth;
    }

    public String getDefaultThemeName() {
        return this.defaultThemeName;
    }

    public void setDefaultThemeName(String name) {
        this.defaultThemeName = name;
    }

    public int getDetailLevel() {
        return detailLevel;
    }

    public void setDetailLevel(int detailLevel) {
        this.detailLevel = detailLevel;
    }

    public String getLocatorUrl() {
        return locatorUrl;
    }

    public void setLocatorUrl(String locatorUrl) {
        this.locatorUrl = locatorUrl;
    }
}

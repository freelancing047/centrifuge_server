package csi.config;

import java.util.Map;

public class MapConfig extends AbstractConfigurationSettings {
    private Map<String, String> baseMaps = null;
    private Map<String, String> boundaryLayers = null;
    private int maxLabelLength = 20;
    private int pointLimit = 20000;
    private int linkLimit = 10000;
    private int typeLimit = 1000;
    private String defaultBasemapOwner;
    private String defaultBasemapId;
    private int frontendToggleThreshold = 1000;
    private int frontendZoomThreshold = 20000;
    private int minPlaceSize = 8;
    private int maxPlaceSize = 36;
    private int minTrackWidth = 1;
    private int maxTrackWidth = 10;
    private int mapCacheMaxSize = 1000;
    private int mapCacheMaxIdleTimeForQueue = 1;
    private String mapCacheTimeUnitForMaxIdleTimeForQueue = "HOURS";
    private String defaultThemeName = "";
    private int outOfBoundResourcesSize = 10;
    private boolean allowBlankBasemap = false;
    private int detailLevel = 6;
    private String locatorUrl = "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer";

    public MapConfig() {
    }

    public Map<String, String> getBaseMaps() {
        return baseMaps;
    }

    public void setBaseMaps(Map<String, String> baseMapsIn) {
        baseMaps = baseMapsIn;
    }

    public Map<String, String> getBoundaryLayers() {
        return boundaryLayers;
    }

    public void setBoundaryLayers(Map<String, String> boundaryLayers) {
        this.boundaryLayers = boundaryLayers;
    }

    public int getMaxLabelLength() {
        return maxLabelLength;
    }

    public void setMaxLabelLength(int maxLabelLength) {
        this.maxLabelLength = maxLabelLength;
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

    public int getMapCacheMaxSize() {
        return mapCacheMaxSize;
    }

    public void setMapCacheMaxSize(int mapCacheMaxSize) {
        this.mapCacheMaxSize = mapCacheMaxSize;
    }

    public int getMapCacheMaxIdleTimeForQueue() {
        return mapCacheMaxIdleTimeForQueue;
    }

    public void setMapCacheMaxIdleTimeForQueue(int mapCacheMaxIdleTimeForQueue) {
        this.mapCacheMaxIdleTimeForQueue = mapCacheMaxIdleTimeForQueue;
    }

    public String getMapCacheTimeUnitForMaxIdleTimeForQueue() {
        return mapCacheTimeUnitForMaxIdleTimeForQueue;
    }

    public void setMapCacheTimeUnitForMaxIdleTimeForQueue(String mapCacheTimeUnitForMaxIdleTimeForQueue) {
        this.mapCacheTimeUnitForMaxIdleTimeForQueue = mapCacheTimeUnitForMaxIdleTimeForQueue;
    }

    public String getDefaultThemeName() {
        return defaultThemeName;
    }

    public void setDefaultThemeName(String defaultThemeName) {
        this.defaultThemeName = defaultThemeName;
    }

    public int getOutOfBoundResourcesSize() {
        return outOfBoundResourcesSize;
    }

    public void setOutOfBoundResourcesSize(int outOfBoundResourcesSize) {
        this.outOfBoundResourcesSize = outOfBoundResourcesSize;
    }

    public boolean isAllowBlankBasemap() {
        return allowBlankBasemap;
    }

    public void setAllowBlankBasemap(boolean allowBlankBasemap) {
        this.allowBlankBasemap = allowBlankBasemap;
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
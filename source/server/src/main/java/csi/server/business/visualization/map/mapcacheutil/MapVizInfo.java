package csi.server.business.visualization.map.mapcacheutil;

import com.google.common.collect.Maps;
import csi.server.business.visualization.map.TrackmapNodeInfo;
import csi.server.common.model.map.Extent;
import csi.shared.core.visualization.map.MapSettingsDTO;

import java.util.Map;

public class MapVizInfo {
    private boolean legendEnabled = false;
    private boolean multiTypeDecoratorEnabled = false;
    private MapSettingsDTO mapSettings;
    private int coarsestMapSummaryPrecision;
    private boolean placeTypeLimitReached = false;
    private boolean trackTypeLimitReached = false;
    private boolean pointLimitReached = false;
    private boolean linkLimitReached = false;
    private Extent initialExtent;
    private Map<Integer, Boolean> useHome = Maps.newHashMap();
    private CurrentInfo homeInfo = new CurrentInfo();
    private Map<Integer, CurrentInfo> currentInfos = Maps.newHashMap();
    private HomeStatus homeStatus = HomeStatus.NOT_LOADED;

    MapVizInfo() {
    }

    public MapSettingsDTO getMapSettings() {
        return mapSettings;
    }

    public void setMapSettings(MapSettingsDTO mapSettings) {
        this.mapSettings = mapSettings;
    }

    public int getCoarsestMapSummaryPrecision() {
        return coarsestMapSummaryPrecision;
    }

    public void setCoarsestMapSummaryPrecision(int coarsestMapSummaryPrecision) {
        this.coarsestMapSummaryPrecision = coarsestMapSummaryPrecision;
    }

    public boolean isPlaceTypeLimitReached() {
        return placeTypeLimitReached;
    }

    public void setPlaceTypeLimitReached(boolean placeTypeLimitReached) {
        this.placeTypeLimitReached = placeTypeLimitReached;
    }

    public boolean isTrackTypeLimitReached() {
        return trackTypeLimitReached;
    }

    public void setTrackTypeLimitReached(boolean trackTypeLimitReached) {
        this.trackTypeLimitReached = trackTypeLimitReached;
    }

    public boolean isPointLimitReached() {
        return pointLimitReached;
    }

    public void setPointLimitReached(boolean pointLimitReached) {
        this.pointLimitReached = pointLimitReached;
    }

    public boolean isLinkLimitReached() {
        return linkLimitReached;
    }

    public void setLinkLimitReached(boolean linkLimitReached) {
        this.linkLimitReached = linkLimitReached;
    }

    public boolean isLegendEnabled() {
        return legendEnabled;
    }

    public void setLegendEnabled(boolean legendEnabled) {
        this.legendEnabled = legendEnabled;
    }

    public boolean isMultiTypeDecoratorEnabled() {
        return multiTypeDecoratorEnabled;
    }

    public void setMultiTypeDecoratorEnabled(boolean multiTypeDecoratorEnabled) {
        this.multiTypeDecoratorEnabled = multiTypeDecoratorEnabled;
    }

    public void setUseHome(int sequenceNumber, boolean useHome) {
        this.useHome.put(sequenceNumber, useHome);
    }

    public Extent getInitialExtent() {
        return initialExtent;
    }

    public void setInitialExtent(Extent initialExtent) {
        this.initialExtent = initialExtent;
    }

    public void invalidateInitialExtent() {
        setInitialExtent(null);
    }

    public TrackmapNodeInfo getHomeTrackmapNodeInfo() {
        return homeInfo.getTrackmapNodeInfo();
    }

    public void invalidateHomeItemsInViz() {
        homeInfo.setItemsInViz(null);
    }

    public CurrentInfo getCurrentInfo(int sequenceNumber) {
        if (isUseHome(sequenceNumber)) {
            return homeInfo;
        } else {
            CurrentInfo currentInfo = null;
            if (currentInfos.containsKey(sequenceNumber)) {
                currentInfo = currentInfos.get(sequenceNumber);
            }
            if (currentInfo == null) {
                currentInfo = new CurrentInfo();
                currentInfos.put(sequenceNumber, currentInfo);
            }
            return currentInfo;
        }
    }

    public boolean isUseHome(int sequenceNumber) {
        if (!useHome.containsKey(sequenceNumber)) {
            return true;
        }
        return useHome.get(sequenceNumber);
    }

    public void invalidateMapNodeInfoAndMapLinkInfo(int sequenceNumber) {
        CurrentInfo currentInfo = getCurrentInfo(sequenceNumber);
        if (currentInfo != null) {
            currentInfo.setMapNodeInfo(null);
            currentInfo.setMapLinkInfo(null);
        }
    }

    public void clearStaleCaches(Integer sequenceNumber) {
        CurrentInfo latest = currentInfos.get(sequenceNumber);
        currentInfos.clear();
        currentInfos.put(sequenceNumber, latest);
    }

    public boolean isHomeLoaded() {
        return homeStatus == HomeStatus.LOADED;
    }

    public boolean isHomeLoading() {
        return homeStatus == HomeStatus.LOADING;
    }

    public void setHomeStatus(HomeStatus homeStatus) {
        this.homeStatus = homeStatus;
    }

    public enum HomeStatus {
        NOT_LOADED, LOADING, LOADED
    }
}

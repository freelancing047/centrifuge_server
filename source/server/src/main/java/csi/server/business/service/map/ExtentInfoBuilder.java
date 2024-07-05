package csi.server.business.service.map;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.ExtentInfo;

import java.util.Objects;

public class ExtentInfoBuilder {
    private MapCacheHandler mapCacheHandler;
    private ExtentInfo extentInfo;
    private Extent initialExtent;
    private Extent extent;

    private ExtentInfoBuilder(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
        this.extent = mapCacheHandler.getExtent();
    }

    public static ExtentInfo getExtentInfo(MapCacheHandler mapCacheHandler) {
        ExtentInfoBuilder builder = new ExtentInfoBuilder(mapCacheHandler);
        builder.build();
        return builder.getExtentInfo();
    }

    public void build() {
        init();
        if (canCreateExtentInfo())
            createExtentInfo();
    }

    private void init() {
        extentInfo = null;
        initialExtent = mapCacheHandler.getInitialExtent();
    }

    private boolean canCreateExtentInfo() {
        return initialExtent != null;
    }

    private void createExtentInfo() {
        extentInfo = new ExtentInfo();
        if (extent == null || isExtentsEqual()) {
            extentInfo.setNewExtent(true);
            extentInfo.setExtent(null);
        } else {
            extentInfo.setNewExtent(false);
            extentInfo.setExtent(extent);
        }
        extentInfo.setInitialExtent(initialExtent);
    }

    private boolean isExtentsEqual() {
        return Objects.equals(extent.getXmax(), initialExtent.getXmax()) &&
                Objects.equals(extent.getXmin(), initialExtent.getXmin()) &&
                Objects.equals(extent.getYmax(), initialExtent.getYmax()) &&
                Objects.equals(extent.getYmin(), initialExtent.getYmin());
    }

    private ExtentInfo getExtentInfo() {
        return extentInfo;
    }
}
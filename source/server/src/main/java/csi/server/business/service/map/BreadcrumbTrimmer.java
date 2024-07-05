package csi.server.business.service.map;

import com.google.common.collect.Lists;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.Crumb;
import csi.shared.core.visualization.map.MapBundleDefinitionDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;

import java.util.List;

public class BreadcrumbTrimmer {
    private MapCacheHandler mapCacheHandler;
    private String drillFieldName;
    private List<MapBundleDefinitionDTO> mapBundleDefinitions;
    private List<Crumb> oldBreadcrumb;
    private List<Crumb> newBreadcrumb;
    private int drillLevel;

    public BreadcrumbTrimmer(MapCacheHandler mapCacheHandler, String drillFieldName) {
        this.mapCacheHandler = mapCacheHandler;
        this.drillFieldName = drillFieldName;
    }

    public void trim() {
        initControlVariables();
        useOldBreadcrumbToBuildNewBreadcrumb();
        mapCacheHandler.addBreadcrumb(newBreadcrumb);
    }

    private void initControlVariables() {
        MapSettingsDTO mapSettings = mapCacheHandler.getMapSettings();
        mapBundleDefinitions = mapSettings.getMapBundleDefinitions();
        oldBreadcrumb = mapCacheHandler.getBreadcrumb();
        newBreadcrumb = Lists.newArrayList();
        drillLevel = 0;
    }

    private void useOldBreadcrumbToBuildNewBreadcrumb() {
        while (drillLevel < oldBreadcrumb.size()) {
            Crumb crumb = oldBreadcrumb.get(drillLevel);
            if (endAtCurrentDrillLevel()) {
                mapCacheHandler.setExtentIfMapNotPinned(crumb.getPreviousExtent());
                mapCacheHandler.setInitialExtent(crumb.getPreviousInitialExtent());
                break;
            }
            newBreadcrumb.add(crumb);
            drillLevel++;
        }
    }

    private boolean endAtCurrentDrillLevel() {
        MapBundleDefinitionDTO mapBundleDefinition = mapBundleDefinitions.get(drillLevel);
        return mapBundleDefinition.getFieldName().equals(drillFieldName);
    }

}
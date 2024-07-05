package csi.server.business.visualization.map.cacheloader.outofbandresources;

import java.util.List;
import java.util.concurrent.Semaphore;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.PointMapSummaryGrid;
import csi.server.business.visualization.map.storage.AbstractMapStorageService;
import csi.server.business.visualization.map.storage.OutOfBandResources;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class PointmapOutOfBandResourcesLoader extends AbstractOutOfBandResourcesLoader {
    public PointmapOutOfBandResourcesLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, List<MapSummaryExtent> mapSummaryExtents, OutOfBandResources resources, Semaphore semaphore) {
        super(mapCacheHandler, dataView, mapViewDef, mapSummaryExtents, resources, semaphore);
    }

    @Override
    public void run() {
        try {
            loadNodeSizeCalculator();
            loadMapSummaryGrid();
            loadMapLinks();
            cacheResult();
        } finally {
            saveJobs.remove(mapCacheHandler.getVizUuid());
            semaphore.release();
            CsiPersistenceManager.releaseCacheConnection();
        }
    }

    private void loadNodeSizeCalculator() {
        NodeSizeCalculatorLoader loader = new NodeSizeCalculatorLoader(mapCacheHandler, dataView, mapViewDef, mapSummaryExtents);
        loader.setRegistry(resources.getRegistry());
        loader.load();
    }

    private void loadMapSummaryGrid() {
        PointmapMapSummaryGridLoader loader = new PointmapMapSummaryGridLoader(mapCacheHandler, dataView, mapViewDef);
        PointMapSummaryGrid mapSummaryGrid = (PointMapSummaryGrid) resources.getMapSummaryGrid();
        mapSummaryGrid.setNewestInternalTypeId(dataView.getNextLinkupId() - 1);
        loader.setMapSummaryGrid(mapSummaryGrid);
        loader.load();
    }

    private void cacheResult() {
        AbstractMapStorageService storageService = AbstractMapStorageService.instance();
        storageService.save(mapCacheHandler.getVizUuid(), resources);
    }

    private void loadMapLinks() {
        MapLinksLoader loader = new MapLinksLoader(mapCacheHandler, dataView, mapViewDef);
        loader.setLinks(resources.getLinks());
        loader.load();
    }
}

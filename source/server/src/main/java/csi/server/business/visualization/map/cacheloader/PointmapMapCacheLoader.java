package csi.server.business.visualization.map.cacheloader;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.cacheloader.outofbandresources.AbstractOutOfBandResourcesLoader;
import csi.server.business.visualization.map.cacheloader.outofbandresources.PointmapOutOfBandResourcesLoader;
import csi.server.business.visualization.map.cacheloader.pointcounter.PointCounterByCountGrid;
import csi.server.business.visualization.map.cacheloader.pointmap.AbstractDetailPointCacheLoader;
import csi.server.business.visualization.map.cacheloader.pointmap.SummaryPointCacheLoader;
import csi.server.business.visualization.map.storage.OutOfBandResources;
import csi.server.business.visualization.map.storage.PointmapOutOfBandResources;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.UBox;

public class PointmapMapCacheLoader extends AbstractMapCacheLoader {
    private MapTheme mapTheme;
    private UBox uBox;

    public PointmapMapCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) throws CentrifugeException {
        super(mapCacheHandler, dataView, mapViewDef);
        this.mapTheme = mapTheme;
        this.uBox = uBox;
    }

    @Override
    public void load() throws CentrifugeException {
        if (!mapCacheHandler.isPlaceLimitOrTrackTypeLimitReached()) {
            setSummaryInformation(calculateCoarsestPrecision());
            Semaphore semaphore = AbstractOutOfBandResourcesLoader.acquireSemaphore(mapCacheHandler.getVizUuid());
            loadOutOfBandResources(semaphore);
            if (shouldLoadDetailCache()) {
                loadDetailMap();
            } else {
                loadSummaryMap();
            }
        }
    }

    private boolean shouldLoadDetailCache() {
        return mapCacheHandler.isCurrentlyAtDetailLevel();
    }

    private void setSummaryInformation(int coarsestPrecision) {
        mapCacheHandler.setCoarsestMapSummaryPrecision(coarsestPrecision);
        mapCacheHandler.setCurrentMapSummaryPrecision(coarsestPrecision);
    }

    private int calculateCoarsestPrecision() {
        PointCounterByCountGrid counter = new PointCounterByCountGrid(mapCacheHandler, dataView, mapViewDef);
        counter.calculatePrecision();
        return counter.getCoarsestPrecision();
    }

    private void loadSummaryMap() throws CentrifugeException {
        SummaryPointCacheLoader mapCacheLoader = new SummaryPointCacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        mapCacheLoader.setPrecision(mapCacheHandler.getCoarsestMapSummaryPrecision());
        mapCacheLoader.load();
    }

    private void loadOutOfBandResources(Semaphore semaphore) {
        OutOfBandResources resources = createAndRegisterOutOfBandResources();

        CsiPersistenceManager.detachEntity(dataView);
        CsiPersistenceManager.detachEntity(mapViewDef);

        List<MapSummaryExtent> mapSummaryExtents = mapCacheHandler.calculateMapSummaryExtents(uBox);
        AbstractOutOfBandResourcesLoader loader = new PointmapOutOfBandResourcesLoader(mapCacheHandler, dataView, mapViewDef, mapSummaryExtents, resources, semaphore);
        Future<?> oldSave = AbstractOutOfBandResourcesLoader.saveJobs.put(mapCacheHandler.getVizUuid(), Executors.newSingleThreadExecutor().submit(loader));
        if (oldSave != null) {
            oldSave.cancel(true);
        }
    }

    private OutOfBandResources createAndRegisterOutOfBandResources() {
        OutOfBandResources resources = new PointmapOutOfBandResources();
        mapCacheHandler.addOutOfBandResources(resources);
        return resources;
    }

    private void loadDetailMap() throws CentrifugeException {
        AbstractMapCacheLoader mapCacheLoader = AbstractDetailPointCacheLoader.make(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        mapCacheLoader.load();
    }
}

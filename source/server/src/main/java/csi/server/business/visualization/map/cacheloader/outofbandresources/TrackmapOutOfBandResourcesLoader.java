package csi.server.business.visualization.map.cacheloader.outofbandresources;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.business.visualization.map.storage.OutOfBandResources;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.TrackmapSelection;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class TrackmapOutOfBandResourcesLoader extends AbstractOutOfBandResourcesLoader {
   public TrackmapOutOfBandResourcesLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef,
                                           List<MapSummaryExtent> mapSummaryExtents, OutOfBandResources resources,
                                           Semaphore semaphore) {
      super(mapCacheHandler, dataView, mapViewDef, mapSummaryExtents, resources, semaphore);
   }

   @Override
   public void run() {
      try {
         loadNodeSizeCalculator();
         loadMapSummaryGrid();
      } finally {
         saveJobs.remove(mapCacheHandler.getVizUuid());
         semaphore.release();
         cullSelection();
         CsiPersistenceManager.releaseCacheConnection();
      }
   }

   private void loadNodeSizeCalculator() {
      NodeSizeCalculatorLoader loader = new NodeSizeCalculatorLoader(mapCacheHandler, dataView, mapViewDef,
            mapSummaryExtents);
      loader.setRegistry(resources.getRegistry());
      loader.load();
   }

   private void loadMapSummaryGrid() {
      TrackmapMapSummaryGridLoader loader = new TrackmapMapSummaryGridLoader(mapCacheHandler, dataView, mapViewDef);
      MapSummaryGrid mapSummaryGrid = resources.getMapSummaryGrid();
      mapSummaryGrid.setNewestInternalTypeId(dataView.getNextLinkupId() - 1);
      loader.setMapSummaryGrid(mapSummaryGrid);
      loader.load();
   }

   private void cullSelection() {
      TrackMapSummaryGrid trackMapSummaryGrid = mapCacheHandler.getTrackMapSummaryGrid();

      if (trackMapSummaryGrid != null) {
         TrackmapSelection trackMapSelection = mapCacheHandler.getTrackMapSelection();

         if ((trackMapSelection != null) && !trackMapSelection.isCleared()) {
            Collection<LinkGeometry> allLinks = trackMapSummaryGrid.getAllLinks();
            Set<LinkGeometry> selectedLinks = trackMapSelection.getLinks();
            Set<LinkGeometry> culled = selectedLinks.stream().filter(allLinks::contains).collect(Collectors.toSet());

            trackMapSelection.setLinks(culled);
         }
      }
   }
}

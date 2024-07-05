package csi.server.business.visualization.map.cacheloader.outofbandresources;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import com.google.common.collect.Maps;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.storage.OutOfBandResources;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.MapSummaryExtent;

public abstract class AbstractOutOfBandResourcesLoader implements Runnable {
   public static final Map<String,Future<?>> saveJobs = Maps.newConcurrentMap();
   private static Map<String,Semaphore> semaphoreMap = Maps.newConcurrentMap();
   protected DataView dataView;
   protected MapViewDef mapViewDef;
   protected List<MapSummaryExtent> mapSummaryExtents;
   protected OutOfBandResources resources;
   protected MapCacheHandler mapCacheHandler;
   Semaphore semaphore;

   public AbstractOutOfBandResourcesLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef,
                                           List<MapSummaryExtent> mapSummaryExtents, OutOfBandResources resources,
                                           Semaphore semaphore) {
      this.mapCacheHandler = mapCacheHandler;
      this.dataView = dataView;
      this.mapViewDef = mapViewDef;
      this.mapSummaryExtents = mapSummaryExtents;
      this.resources = resources;
      this.semaphore = semaphore;
   }

   public static Semaphore acquireSemaphore(String mapViewDefUuid) {
      Semaphore semaphore = semaphoreMap.get(mapViewDefUuid);
      if (semaphore == null) {
         semaphore = new Semaphore(1, true);
         semaphoreMap.put(mapViewDefUuid, semaphore);
         semaphore = semaphoreMap.get(mapViewDefUuid);
      }
      try {
         semaphore.acquire();
      } catch (InterruptedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return semaphore;
   }
}

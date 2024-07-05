package csi.server.business.visualization.map.storage;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.server.business.visualization.map.storage.postgres.PostgresMapStorageService;

public abstract class AbstractMapStorageService {
   private static final AbstractMapStorageService SINGLETON = new PostgresMapStorageService();

   public OutOfBandResourcesLRUCache outOfBandResourcesCache;

   public AbstractMapStorageService() {
      MapConfig mapConfig = Configuration.getInstance().getMapConfig();
      int outOfBoundResourcesSize = mapConfig.getOutOfBoundResourcesSize();
      outOfBandResourcesCache = new OutOfBandResourcesLRUCache(outOfBoundResourcesSize);
   }

   public static AbstractMapStorageService instance() {
      return SINGLETON;
   }

   public abstract void save(String vizuuid, OutOfBandResources resources);

   public abstract boolean hasVisualizationData(String vizuuid);

   public abstract OutOfBandResources get(String vizuuid);

   public abstract void resetData(String vizUuid);

   public void invalidate(String vizUuid) {
      outOfBandResourcesCache.remove(vizUuid);
      resetData(vizUuid);
   }

   public void addOutOfBandResources(String mapViewDefUuid, OutOfBandResources outOfBandResources) {
      outOfBandResourcesCache.set(mapViewDefUuid, outOfBandResources);
   }

   public OutOfBandResources getOutOfBandResources(String mapViewDefUuid) {
      return outOfBandResourcesCache.get(mapViewDefUuid);
   }

   public void invalidateOutOfBandResources(String mapViewDefUuid) {
      outOfBandResourcesCache.remove(mapViewDefUuid);
   }
}

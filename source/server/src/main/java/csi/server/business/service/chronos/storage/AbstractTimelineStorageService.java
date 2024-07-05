package csi.server.business.service.chronos.storage;

import com.mongodb.DBObject;

import csi.server.business.service.chronos.storage.postgres.PostgresTimelineStorageService;

public abstract class AbstractTimelineStorageService {
   private static final AbstractTimelineStorageService SINGLETON = new PostgresTimelineStorageService();

   public static AbstractTimelineStorageService instance() {
      return SINGLETON;
   }

   public abstract void initializeData(String vizUuid);

   public abstract void resetData(String vizUuid);

   public abstract void resetDataAt(String vizUuid);

   public abstract boolean hasVisualizationData(String vizuuid);

   public abstract TimelineStorage<DBObject> getTimelineStorage(String vizuuid);

   public abstract void save(String vizuuid, TimelineStorage<DBObject> TimelineStorage);

   public abstract TimelineStorage<DBObject> createEmptyStorage(String vizUuid);

   public abstract boolean hasVisualizationDataAt(String vizUuid);
}

package csi.graph;

import com.mongodb.DBObject;

import csi.graph.postgres.PostgresStorageService;

public abstract class AbstractStorageService {
   private static final AbstractStorageService SINGLETON = new PostgresStorageService();

   public static AbstractStorageService instance() {
      return SINGLETON;
   }

   public abstract void initializeData(String vizUuid);

   public abstract void resetData(String vizUuid);

   public abstract boolean hasVisualizationData(String vizuuid);

   public abstract GraphStorage<DBObject,DBObject> getGraphStorage(String vizuuid);

   public abstract void saveGraphStorage(String vizuuid, GraphStorage<DBObject,DBObject> graphStorage);

   public abstract boolean copy(String srcVizUuid, String targetVizUuid);

   public abstract GraphStorage<DBObject,DBObject> createEmptyStorage(String vizUuid);
}

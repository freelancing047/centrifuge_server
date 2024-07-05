package csi.server.business.service.matrix.storage;

import com.mongodb.DBObject;

import csi.server.business.service.matrix.storage.postgres.PostgresMatrixCacheStorageService;

public abstract class AbstractMatrixCacheStorageService {
   private static final AbstractMatrixCacheStorageService SINGLETON = new PostgresMatrixCacheStorageService();

   public static AbstractMatrixCacheStorageService instance() {
      return SINGLETON;
   }

//  public abstract void initializeData(String vizUuid);
   public abstract void resetData(String vizUuid);

   public abstract boolean hasVisualizationData(String vizuuid);

   public abstract MatrixCacheStorage<DBObject> getMatrixCacheStorage(String vizuuid);

   public abstract void saveMatrixCacheStorage(String vizuuid, MatrixCacheStorage<DBObject> matrixCacheStorage);

   public abstract MatrixCacheStorage<DBObject> createEmptyStorage(String vizUuid);
}

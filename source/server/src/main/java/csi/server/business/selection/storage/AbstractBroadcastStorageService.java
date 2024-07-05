package csi.server.business.selection.storage;

import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.postgres.PostgresBroadcastStorageService;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;

public abstract class AbstractBroadcastStorageService {
   private static final AbstractBroadcastStorageService SINGLETON = new PostgresBroadcastStorageService();

   public static AbstractBroadcastStorageService instance() {
      return SINGLETON;
   }

   public abstract void addBroadcast(String vizUuid, IntegerRowsSelection selection, boolean excludeRows);

   public abstract BroadcastResult getBroadcast(String vizUuid);

   public abstract void clearBroadcast(String vizUuid);

   public abstract void copy(String vizUuid, String targetUuid);

   public abstract boolean hasBroadcast(String vizUuid);
}

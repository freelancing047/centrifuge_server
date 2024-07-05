package csi.server.business.selection.storage.postgres;

import java.util.function.Function;

import com.mongodb.DBObject;

import csi.server.business.selection.cache.BroadcastResult;

public class DataToBroadcastTransformer implements Function<Object,BroadcastResult> {
   public BroadcastResult apply(Object object) {
      BroadcastResult result = null;

      if (object instanceof DBObject) {
         DBObject dbObject = (DBObject) object;
         result = (BroadcastResult) dbObject.get(PostgresBroadcastStorageService.BROADCAST_KEY);
      }
      return result;
   }
}

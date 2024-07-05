package csi.server.business.selection.storage.postgres;

import java.util.function.Function;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import csi.server.business.selection.cache.BroadcastResult;

public class BroadcastToDataTransformer implements Function<BroadcastResult,DBObject> {
   public DBObject apply(BroadcastResult broadcast) {
      BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

      builder.append(PostgresBroadcastStorageService.BROADCAST_KEY, broadcast);
      return builder.get();
   }
}

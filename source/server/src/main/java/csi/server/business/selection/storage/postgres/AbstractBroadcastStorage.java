package csi.server.business.selection.storage.postgres;

import com.mongodb.DBObject;

public abstract class AbstractBroadcastStorage<T> {
   public abstract void addResult(DBObject object);

   public abstract DBObject getResult();
}

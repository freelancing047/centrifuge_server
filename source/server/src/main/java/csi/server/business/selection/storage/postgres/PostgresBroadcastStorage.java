package csi.server.business.selection.storage.postgres;

import com.mongodb.DBObject;

public class PostgresBroadcastStorage extends AbstractBroadcastStorage<DBObject> {
   private PostgresBroadcastStorageBlob blob;

   public PostgresBroadcastStorage(PostgresBroadcastStorageBlob storageBlob) {
      this.blob = storageBlob;
   }

   public PostgresBroadcastStorage() {
      this.blob = new PostgresBroadcastStorageBlob();
   }

   @Override
   public void addResult(DBObject result) {
      blob.setBroadcastResult(result);
   }

   public PostgresBroadcastStorageBlob getBlob() {
      return blob;
   }

   @Override
   public DBObject getResult() {
      return blob.getBroadcastResult();
   }
}

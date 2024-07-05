package csi.server.business.selection.storage.postgres;

import java.io.Serializable;

import com.mongodb.DBObject;

public class PostgresBroadcastStorageBlob implements Serializable {
    protected DBObject result;
    public void setBroadcastResult(DBObject result) {
        this.result = result;
    }

    public DBObject getBroadcastResult() {
        return result;
    }

}

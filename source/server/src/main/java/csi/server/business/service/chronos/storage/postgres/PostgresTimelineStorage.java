package csi.server.business.service.chronos.storage.postgres;

import com.mongodb.DBObject;

import csi.server.business.service.chronos.storage.TimelineStorage;

public class PostgresTimelineStorage implements TimelineStorage<DBObject> {

    private PostgresTimelineStorageBlob blob;

    public PostgresTimelineStorage(PostgresTimelineStorageBlob storageBlob) {
        this.blob = storageBlob;
    }

    public PostgresTimelineStorage() {
        this.blob = new PostgresTimelineStorageBlob();
    }

    @Override
    public void addResult(DBObject result) {
        blob.setDetailedTimelineResult(result);
    }

    public PostgresTimelineStorageBlob getBlob() {
        return blob;
    }

    @Override
    public DBObject getResult() {
        
        return blob.getDetailedTimelineResult();
    }


   


}

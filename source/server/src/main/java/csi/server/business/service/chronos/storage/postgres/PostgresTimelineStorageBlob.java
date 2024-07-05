package csi.server.business.service.chronos.storage.postgres;

import java.io.Serializable;

import com.mongodb.DBObject;

public class PostgresTimelineStorageBlob implements Serializable{
    protected DBObject detailedTimelineResult;

    public DBObject getDetailedTimelineResult() {
        return detailedTimelineResult;
    }

    public void setDetailedTimelineResult(DBObject detailedTimelineResult) {
        this.detailedTimelineResult = detailedTimelineResult;
    }

}

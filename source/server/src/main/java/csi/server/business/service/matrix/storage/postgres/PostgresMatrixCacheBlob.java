package csi.server.business.service.matrix.storage.postgres;

import com.mongodb.DBObject;

import java.io.Serializable;


public class PostgresMatrixCacheBlob implements Serializable {
    protected DBObject cachedMatrixData;


    public DBObject getCachedMatrixData(){
        return cachedMatrixData;
    }

    public void setCachedMatrixData(DBObject cachedMatrixData) {
        this.cachedMatrixData= cachedMatrixData;
    }

}

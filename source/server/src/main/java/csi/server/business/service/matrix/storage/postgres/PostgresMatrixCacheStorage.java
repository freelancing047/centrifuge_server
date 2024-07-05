package csi.server.business.service.matrix.storage.postgres;

import com.mongodb.DBObject;
import csi.server.business.service.matrix.storage.MatrixCacheStorage;


public class PostgresMatrixCacheStorage implements MatrixCacheStorage<DBObject> {

    private PostgresMatrixCacheBlob blob;

    public PostgresMatrixCacheStorage(PostgresMatrixCacheBlob b){
        this.blob = b;
    }

    public PostgresMatrixCacheStorage(){
        this.blob = new PostgresMatrixCacheBlob();
    }


    public PostgresMatrixCacheBlob getBlob(){
        return blob;
    }


    @Override
    public void setResult(DBObject result) {
        blob.setCachedMatrixData(result);
    }

    @Override
    public DBObject getResult() {
        return blob.getCachedMatrixData();
    }
}

package csi.server.business.service.chart.storage.postgres;

import com.mongodb.DBObject;

import csi.server.business.service.chart.storage.ChartStorage;

public class PostgresChartStorage implements ChartStorage<DBObject> {

    private PostgresChartStorageBlob blob;

    public PostgresChartStorage(PostgresChartStorageBlob storageBlob) {
        this.blob = storageBlob;
    }

    public PostgresChartStorage() {
        this.blob = new PostgresChartStorageBlob();
    }

    @Override
    public void addResult(DBObject result) {
        blob.setTableResult(result);
    }

    public PostgresChartStorageBlob getBlob() {
        return blob;
    }

    @Override
    public DBObject getResult() {
        
        return blob.getTableResult();
    }
}

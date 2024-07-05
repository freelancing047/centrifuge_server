package csi.server.business.service.chart.storage.postgres;

import java.io.Serializable;

import com.mongodb.DBObject;

public class PostgresChartStorageBlob implements Serializable{
    protected DBObject tableResult;

    public DBObject getTableResult() {
        return tableResult;
    }

    public void setTableResult(DBObject tableResult) {
        this.tableResult = tableResult;
    }
}

package csi.server.business.service.chart.storage;

import java.util.List;

import com.mongodb.DBObject;

import csi.server.business.service.chart.storage.postgres.PostgresChartStorageService;

public abstract class AbstractChartStorageService {
    private static final AbstractChartStorageService SINGLETON = new PostgresChartStorageService();

    public static AbstractChartStorageService instance() {
        return SINGLETON;
    }

    public abstract void initializeData(String vizUuid);

    public abstract void resetData(String vizUuid);

    public abstract void resetDataAt(String vizUuid, Integer drillKey);

    public abstract boolean hasVisualizationData(String vizuuid);

    public abstract ChartStorage<DBObject> getChartStorage(String vizuuid, Integer drillKey);

    public abstract void save(String vizuuid, Integer drillKey, ChartStorage<DBObject> chartStorage);

    public abstract ChartStorage<DBObject> createEmptyStorage(String vizUuid);

    public abstract boolean hasVisualizationDataAt(String vizUuid, Integer drillKey);

    public abstract int createDrillKey(List<String> drillDimensions);
}

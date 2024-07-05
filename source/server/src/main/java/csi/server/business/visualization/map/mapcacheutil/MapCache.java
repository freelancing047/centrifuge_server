package csi.server.business.visualization.map.mapcacheutil;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import csi.config.Configuration;
import csi.config.MapConfig;

public class MapCache {
    private static MapCache instance = null;
    private Cache<String, MapVizInfo> mapVizInfoCache;

    public MapCache(int cacheMaxSize, int maxIdleTimeForQueue, TimeUnit timeUnitForMaxIdleTimeForQueue) {
        mapVizInfoCache = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterAccess(maxIdleTimeForQueue, timeUnitForMaxIdleTimeForQueue).build();
    }

    public static MapCache getInstance() {
        if (instance == null) {
            MapConfig mapConfig = Configuration.getInstance().getMapConfig();
            int cacheMaxSize = mapConfig.getMapCacheMaxSize();
            int maxIdleTimeForQueue = mapConfig.getMapCacheMaxIdleTimeForQueue();
            TimeUnit timeUnit = TimeUnit.MINUTES;
            switch (mapConfig.getMapCacheTimeUnitForMaxIdleTimeForQueue()) {
                case "DAYS":
                    timeUnit = TimeUnit.DAYS;
                    break;
                case "HOURS":
                    timeUnit = TimeUnit.HOURS;
                    break;
                case "MINUTES":
                    timeUnit = TimeUnit.MINUTES;
                    break;
                case "SECONDS":
                    timeUnit = TimeUnit.SECONDS;
                    break;
                default:
                    break;
            }
            instance = new MapCache(cacheMaxSize, maxIdleTimeForQueue, timeUnit);
        }
        return instance;
    }

    public MapVizInfo getMapVizInfo(String vizUuid) {
        MapVizInfo mapVizInfo = mapVizInfoCache.getIfPresent(vizUuid);
        if (mapVizInfo == null) {
            mapVizInfo = new MapVizInfo();
            mapVizInfoCache.put(vizUuid, mapVizInfo);
        }
        return mapVizInfo;
    }

    public void invalidate(String vizUuid) {
        mapVizInfoCache.invalidate(vizUuid);
    }
}

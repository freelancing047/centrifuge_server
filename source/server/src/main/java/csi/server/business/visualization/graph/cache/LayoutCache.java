package csi.server.business.visualization.graph.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import csi.security.CsiSecurityManager;
import csi.server.business.selection.cache.SessionAndVizKey;
import csi.server.common.model.visualization.graph.GraphConstants;

/**
 * @author Centrifuge Systems, Inc.
 */
public class LayoutCache {

    private static LayoutCache instance = null;
    public static LayoutCache getInstance(){
        if(instance == null)
            return new LayoutCache(1000, 6, TimeUnit.HOURS);

        return instance;
    }

    private Cache<SessionAndVizKey, GraphConstants.eLayoutAlgorithms> layoutAlgorithmCache;

    public LayoutCache(int cacheMaxSize, int maxIdleTimeForQueue, TimeUnit timeUnitForMaxIdleTimeForQueue) {
        layoutAlgorithmCache = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterAccess(maxIdleTimeForQueue, timeUnitForMaxIdleTimeForQueue).build();
        instance = this;
    }

    public void addLayout(String vizUuid, GraphConstants.eLayoutAlgorithms layoutAlgorithm){
        SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(CsiSecurityManager.getUserName(), vizUuid);
        layoutAlgorithmCache.put(sessionAndVizKey, layoutAlgorithm);
    }

    public GraphConstants.eLayoutAlgorithms getLayout(String vizUuid){
        GraphConstants.eLayoutAlgorithms layout = layoutAlgorithmCache.getIfPresent(new SessionAndVizKey(CsiSecurityManager.getUserName(), vizUuid));
        if(layout == null)
            return null;
        return layout;
    }
}

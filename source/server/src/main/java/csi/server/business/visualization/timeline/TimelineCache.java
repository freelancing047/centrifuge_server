package csi.server.business.visualization.timeline;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import csi.security.Authorization;
import csi.server.business.selection.cache.SessionAndVizKey;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.TimelineState;

public class TimelineCache {

	private static TimelineCache instance = null;

	public static TimelineCache getInstance(){
		if(instance == null)
			return new TimelineCache(1000, 10, TimeUnit.MINUTES);

		return instance;
	}

	private Cache<SessionAndVizKey, Selection> selectionCache;
	private Cache<SessionAndVizKey, DetailedTimelineResult> resultsCache;
	private Cache<SessionAndVizKey, Authorization> sessionCache;
	private Cache<SessionAndVizKey, TimelineState> stateCache;

	public TimelineCache(int cacheMaxSize, int maxIdleTimeForQueue, TimeUnit timeUnitForMaxIdleTimeForQueue) {
		selectionCache = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterAccess(24, TimeUnit.HOURS).build();
		resultsCache = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterAccess(maxIdleTimeForQueue, timeUnitForMaxIdleTimeForQueue).build();
		sessionCache = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterAccess(maxIdleTimeForQueue, timeUnitForMaxIdleTimeForQueue).build();
		//Allowing states to stick around a bit longer, looks a little better on the UI that way.
		stateCache = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterAccess(24,  TimeUnit.HOURS).build();
		instance = this;
	}

	public void addSelection(String vizUuid, Selection selection){
		SessionAndVizKey sessionAndVizKey = new SessionAndVizKey("", vizUuid);
		selectionCache.put(sessionAndVizKey, selection);
	}
	
	public Selection getSelection(String vizUuid){
		Selection selection = selectionCache.getIfPresent(new SessionAndVizKey("", vizUuid));
		if(selection == null)
			return NullSelection.instance;
		return selection;
	}

	public void addResult(String identifier, String vizUuid, DetailedTimelineResult result){
		SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(identifier, vizUuid);
		resultsCache.put(sessionAndVizKey, result);
	}

	public DetailedTimelineResult getResult(String identifier, String vizUuid){
		DetailedTimelineResult result = resultsCache.getIfPresent(new SessionAndVizKey(identifier, vizUuid));
		return result;
	}

	public void clearResult(String identifier, String vizUuid) {
		SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(identifier, vizUuid);
		resultsCache.invalidate(sessionAndVizKey);
	}
	
	public void addState(String vizUuid, TimelineState state){
		SessionAndVizKey sessionAndVizKey = new SessionAndVizKey("", vizUuid);
		stateCache.put(sessionAndVizKey, state);
	}
	
	public TimelineState getState(String vizUuid){
		TimelineState result = stateCache.getIfPresent(new SessionAndVizKey("", vizUuid));
		return result;
	}
	
	public void addSession(String identifier, String vizUuid, Authorization result){
		SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(identifier, vizUuid);
		sessionCache.put(sessionAndVizKey, result);
	}

	public Authorization getSession(String identifier, String vizUuid){
		Authorization result = sessionCache.getIfPresent(new SessionAndVizKey(identifier, vizUuid));
		return result;
	}

	public void clearSession(String identifier, String vizUuid) {
		SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(identifier, vizUuid);
		sessionCache.invalidate(sessionAndVizKey);
	}
}

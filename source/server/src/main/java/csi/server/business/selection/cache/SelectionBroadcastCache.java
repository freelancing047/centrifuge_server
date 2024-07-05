package csi.server.business.selection.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

/**
 * Holds broadcasts and selections for the current uesr in caches, which are externally configurable.
 * @author Centrifuge Systems, Inc.
 */
public class SelectionBroadcastCache{

    private static SelectionBroadcastCache instance = null;
    public static SelectionBroadcastCache getInstance(){
        if(instance == null){
            //Time is ignored currently for the selections but not broadcasts
            return new SelectionBroadcastCache(1000, 48, TimeUnit.HOURS);
        }
        return instance;
    }

    private Cache<SessionAndVizKey, Selection> selections;
    private Cache<SessionAndVizKey, BroadcastResult> broadcasts;

    public SelectionBroadcastCache(int cacheMaxSize, int maxIdleTimeForQueue, TimeUnit timeUnitForMaxIdleTimeForQueue) {
        selections = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).build();
        //broadcasts = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterAccess(maxIdleTimeForQueue, timeUnitForMaxIdleTimeForQueue).build();
        instance = this;
    }

    public void addSelection(String vizUuid, Selection selection){
            SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(null, vizUuid);
        if (selection != null) {
            selections.put(sessionAndVizKey, selection);
        }else {
            selections.invalidate(sessionAndVizKey);
        }
    }

    public Selection getSelection(String vizUuid){
        Selection selection = selections.getIfPresent(new SessionAndVizKey(null, vizUuid));
        if(selection == null)
            return NullSelection.instance;
        return selection;
    }
    
    public void removeSelection(String vizUuid){
        selections.invalidate(new SessionAndVizKey(null, vizUuid));
//        if(selection != null) {
//        }
//            selection.clearSelection();
    }
    
    public void clearSelection(String vizUuid) {
        Selection selection = selections.getIfPresent(new SessionAndVizKey(null, vizUuid));
        if(selection != null)
            selection.clearSelection();
    }

//    public void addBroadcast(String vizUuid, IntegerRowsSelection selection, boolean excludeRows){
//        SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(null, vizUuid);
//        BroadcastResult broadcastResult;
//        if (selection.isCleared()) {
//            broadcastResult = BroadcastResult.EMPTY_BROADCAST_RESULT;
//        }else {
//            broadcastResult = new BroadcastResult(selection, excludeRows);
//        }
//        broadcasts.put(sessionAndVizKey, broadcastResult);
//    }

//    public BroadcastResult getBroadcast(String vizUuid){
//        BroadcastResult result = broadcasts.getIfPresent(new SessionAndVizKey(null, vizUuid));
//        if(result == null)
//            return BroadcastResult.NULL_BROADCAST;
//        return result;
//    }

//    public void clearBroadcast(String vizUuid) {
//        SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(null, vizUuid);
//        broadcasts.invalidate(sessionAndVizKey);
//    }
//
//	public void copy(String vizUuid, String targetUuid) {
//		addMapSelection(targetUuid, getSelection(vizUuid).copy());
//		SessionAndVizKey sessionAndVizKey = new SessionAndVizKey(null, targetUuid);
//        BroadcastResult broadcastResult = getBroadcast(vizUuid);
//        if (broadcastResult != BroadcastResult.EMPTY_BROADCAST_RESULT)
//        	broadcasts.put(sessionAndVizKey, broadcastResult.copy());
//	}

//    public void persist() {
//        File selectionCacheFile = new File("webapps/Centrifuge/resources/selectionCacheFile.ser");
//        try (
//                FileOutputStream file = new FileOutputStream(selectionCacheFile);
//                BufferedOutputStream buffer = new BufferedOutputStream(file);
//                ObjectOutput output = new ObjectOutputStream(buffer);
//        ) {
//            Map<SessionAndVizKey, Selection> selectionMap = selections.asMap();
//            Map<SessionAndVizKey, Selection> map = new HashMap<SessionAndVizKey,Selection>();
//            for (SessionAndVizKey key : selectionMap.keySet()) {
//                map.put(key, selectionMap.get(key));
//            }
//            output.writeObject(map);
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        File broadcastCacheFile = new File("webapps/Centrifuge/resources/broadcastCacheFile.ser");
//        try (
//                FileOutputStream file = new FileOutputStream(broadcastCacheFile);
//                BufferedOutputStream buffer = new BufferedOutputStream(file);
//                ObjectOutput output = new ObjectOutputStream(buffer);
//        ){
//            Map<SessionAndVizKey, BroadcastResult> broadcastMap = broadcasts.asMap();
//            Map<SessionAndVizKey, BroadcastResult> map = new HashMap<SessionAndVizKey,BroadcastResult>();
//            for (SessionAndVizKey key : broadcastMap.keySet()) {
//                map.put(key, broadcastMap.get(key));
//            }
//            output.writeObject(map);
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public void restore() {
//        File selectionCacheFile = new File("webapps/Centrifuge/resources/selectionCacheFile.ser");
//        if (selectionCacheFile.exists()) {
//            try (InputStream file = new FileInputStream(selectionCacheFile);
//                    InputStream buffer = new BufferedInputStream(file);
//                    ObjectInput input = new ObjectInputStream(buffer);
//            ) {
//                Map<SessionAndVizKey, Selection> selectionMap = (Map<SessionAndVizKey, Selection>) input.readObject();
//                for (SessionAndVizKey key : selectionMap.keySet()) {
//                    selections.put(key, selectionMap.get(key));
//                }
//            } catch (ClassNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        File broadcastCacheFile = new File("webapps/Centrifuge/resources/broadcastCacheFile.ser");
//        if (broadcastCacheFile.exists()) {
//            try (InputStream file = new FileInputStream(broadcastCacheFile);
//                    InputStream buffer = new BufferedInputStream(file);
//                    ObjectInput input = new ObjectInputStream(buffer);
//            ) {
//                Map<SessionAndVizKey, BroadcastResult> broadcastMap = (Map<SessionAndVizKey, BroadcastResult>) input.readObject();
//                for (SessionAndVizKey key : broadcastMap.keySet()) {
//                    broadcasts.put(key, broadcastMap.get(key));
//                }
//            } catch (ClassNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
}

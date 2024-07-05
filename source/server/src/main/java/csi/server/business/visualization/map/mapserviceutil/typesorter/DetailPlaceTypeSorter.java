package csi.server.business.visualization.map.mapserviceutil.typesorter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.common.model.map.PlaceidTypenameDuple;

public class DetailPlaceTypeSorter extends AbstractPlaceTypeSorter {
    private MapNodeInfo mapNodeInfo;
    private Set<PlaceidTypenameDuple> mapNodeInfoTypeNames;

    public DetailPlaceTypeSorter(String mapViewDefUuid, List<PlaceidTypenameDuple> keys) {
        super(keys);
        mapNodeInfo = MapCacheUtil.getMapNodeInfo(mapViewDefUuid);
        dynamicTypeInfo = MapServiceUtil.getPlaceDynamicTypeInfo(mapViewDefUuid);
    }

    @Override
    public void sort() {
        init();
        applyToTypenameCache(keys.iterator());
        applyToTypenameCache(mapNodeInfoTypeNames.iterator());
        applyToDynamicTypenameCache(dynamicTypeInfoTypeNames.iterator());
    }

    private void init() {
        initControlVariables();
        clearTypenameCache();
    }

    private void initControlVariables() {
        mapNodeInfoTypeNames = new TreeSet<PlaceidTypenameDuple>();
        dynamicTypeInfoTypeNames = new TreeSet<PlaceidTypenameDuple>();
        for (PlaceidTypenameDuple key : dynamicTypeInfo.getTypenameToId().keySet()) {
            if (key != null) {
               dynamicTypeInfoTypeNames.add(key);
            }
        }
        for (PlaceidTypenameDuple key : mapNodeInfo.getTypenameToColor().keySet()) {
            if (key != null) {
                mapNodeInfoTypeNames.add(key);
                dynamicTypeInfoTypeNames.remove(key);
            }
        }
        mapNodeInfoTypeNames.removeAll(keys);
        id = 0;
    }

    private void clearTypenameCache() {
        MapServiceUtil.clearTypeInfo(mapNodeInfo);
        MapServiceUtil.clearTypeInfo(dynamicTypeInfo);
    }

    private void applyToTypenameCache(Iterator<PlaceidTypenameDuple> keys) {
        while (keys.hasNext()) {
            PlaceidTypenameDuple key = keys.next();
            MapServiceUtil.applyTypenameToTypeInfo(mapNodeInfo, key, id);
            MapServiceUtil.applyTypenameToTypeInfo(dynamicTypeInfo, key, id);
            id++;
        }
    }
}

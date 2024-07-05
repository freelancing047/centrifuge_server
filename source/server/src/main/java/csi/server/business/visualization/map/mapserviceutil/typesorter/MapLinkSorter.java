package csi.server.business.visualization.map.mapserviceutil.typesorter;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapLinkInfo;
import csi.shared.core.visualization.map.AssociationSettingsDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;

import java.util.List;

public class MapLinkSorter {
    private MapLinkInfo mapLinkInfo;
    private List<String> typeNames;
    private MapCacheHandler mapCacheHandler;

    public MapLinkSorter(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
    }

    public void sort() {
        init();
        applyToTypenameCache();
    }

    private void applyToTypenameCache() {
        for (int id = typeNames.size() - 1; id >= 0; id--)
            applyTo(id, typeNames.get(id));
    }

    private void applyTo(int id, String typename) {
        mapLinkInfo.getTypenameToId().put(typename, id);
        mapLinkInfo.getTypeidToName().put(id, typename);
    }

    private void init() {
        mapLinkInfo = mapCacheHandler.getMapLinkInfo();
        MapSettingsDTO mapSettings = mapCacheHandler.getMapSettings();
        typeNames = Lists.newArrayList();
        for (AssociationSettingsDTO associationSettings : mapSettings.getAssociationSettings())
            typeNames.add(associationSettings.getName());
    }
}

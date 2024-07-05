package csi.server.business.visualization.map.cacheloader.pointmap;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.UBox;

class DetailPointMapCacheLoader extends AbstractDetailPointCacheLoader {
   DetailPointMapCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef,
                             MapTheme mapTheme, UBox uBox)
         throws CentrifugeException {
      super(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox, false);
   }

   void init() {
      if (mapCacheHandler.getMapNodeInfo() == null) {
         mapCacheHandler.invalidateExtentIfMapNotPinned();
         mapCacheHandler.invalidateInitialExtent();
         mapCacheHandler.initializeMapNodeInfoAndMapLinkInfo();
      }
   }
}

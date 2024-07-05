package csi.server.business.visualization.map.cacheloader.pointmap;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.UBox;

public class SummaryPointMapCacheLoader extends AbstractDetailPointCacheLoader {
   public SummaryPointMapCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef,
                                     MapTheme mapTheme, UBox uBox)
         throws CentrifugeException {
      super(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox, true);
   }

   public void init() {
      if (mapCacheHandler.getMapNodeInfo() == null) {
         mapCacheHandler.initializeMapNodeInfoAndMapLinkInfo();
      }
   }
}

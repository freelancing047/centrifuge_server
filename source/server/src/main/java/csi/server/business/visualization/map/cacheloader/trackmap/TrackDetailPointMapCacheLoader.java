package csi.server.business.visualization.map.cacheloader.trackmap;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.UBox;

public class TrackDetailPointMapCacheLoader extends AbstractTrackDetailPointCacheLoader {
   public TrackDetailPointMapCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef,
                                         MapTheme mapTheme, UBox uBox)
         throws CentrifugeException {
      super(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
   }

   void init() {
      if (mapCacheHandler.getMapNodeInfo() == null) {
         mapCacheHandler.initializeMapNodeInfoAndMapTrackInfo();
      }
   }
}

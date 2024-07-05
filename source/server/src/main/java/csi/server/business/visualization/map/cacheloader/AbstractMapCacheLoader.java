package csi.server.business.visualization.map.cacheloader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import csi.server.business.helper.DataCacheHelper;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.MapSettingsDTO;

public abstract class AbstractMapCacheLoader {
   protected MapCacheHandler mapCacheHandler;
   protected DataView dataView;
   protected MapViewDef mapViewDef;
   protected MapSettingsDTO mapSettings;
   protected String filterString;
   protected String selectedItemsString;
   String groupByString = "";

   public AbstractMapCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef)
         throws CentrifugeException {
      this.mapCacheHandler = mapCacheHandler;
      this.dataView = dataView;
      if (dataView == null) {
         throw new CentrifugeException("DataView not found.");
      }
      this.mapViewDef = mapViewDef;
      mapSettings = mapCacheHandler.getMapSettings();
   }

   protected static void addToSetIfNotNull(Set<String> set, String value) {
      if (value != null) {
         set.add(value);
      }
   }

   public abstract void load() throws CentrifugeException;

   protected void formSelectedItemsString(Set<String> selectedColumnNames) {
      List<String> quotedColumnNames = new ArrayList<String>();

      for (String columnName : selectedColumnNames) {
         quotedColumnNames.add(new StringBuilder("\"").append(columnName).append("\"").toString());
      }
      selectedItemsString = quotedColumnNames.stream().collect(Collectors.joining(", "));
   }

   protected ResultSet getData(Connection connection) throws CentrifugeException {
      return DataCacheHelper.getCacheData(connection, dataView.getUuid(), null, selectedItemsString, filterString,
                                          groupByString, null, -1, -1, true);
   }
}

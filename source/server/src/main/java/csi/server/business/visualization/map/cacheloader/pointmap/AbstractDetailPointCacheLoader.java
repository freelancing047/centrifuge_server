package csi.server.business.visualization.map.cacheloader.pointmap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.cacheloader.AbstractMapCacheLoader;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.MapTooltipFieldDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;
import csi.shared.core.visualization.map.UBox;

public abstract class AbstractDetailPointCacheLoader extends AbstractMapCacheLoader {
   private static final Logger LOG = LogManager.getLogger(AbstractDetailPointCacheLoader.class);

   private MapTheme mapTheme;
    private List<PlaceSettingsDTO> placeSettings;
    private List<MapSummaryExtent> mapSummaryExtents;
    private boolean waitForRegistry;

    public AbstractDetailPointCacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox, boolean waitForRegistry) throws CentrifugeException {
        super(mapCacheHandler, dataView, mapViewDef);
        this.mapTheme = mapTheme;
        placeSettings = mapSettings.getPlaceSettings();
        mapSummaryExtents = mapCacheHandler.calculateMapSummaryExtents(uBox);
        this.waitForRegistry = waitForRegistry;
    }

    public static AbstractDetailPointCacheLoader make(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) throws CentrifugeException {
        return new SummaryPointMapCacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
    }

    @Override
    public void load() {
        init();
        exec();
    }

    abstract void init();

   private void exec() {
      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         generateQuery();
         populateMapCache(getData(conn));
      } catch (CentrifugeException | SQLException e) {
         LOG.error(e);
      }
   }

    private void generateQuery() {
        generateSelectedItemsString();
        filterString = mapCacheHandler.generateFilterString(dataView, mapViewDef, mapSummaryExtents);
    }

    private void generateSelectedItemsString() {
        Set<String> selectedColumnNames = gatherSelectedColumns();
        formSelectedItemsString(selectedColumnNames);
    }

    private Set<String> gatherSelectedColumns() {
        Set<String> selectedColumnNames = gatherMinimumSelectedColumns();
        for (int placeId = 0; placeId < placeSettings.size(); placeId++) {
         gatherSelectedColumnsForPlaceId(selectedColumnNames, placeId);
      }
        return selectedColumnNames;
    }

    private Set<String> gatherMinimumSelectedColumns() {
        Set<String> selectedColumnNames = new TreeSet<String>();

        addToSetIfNotNull(selectedColumnNames, CacheUtil.INTERNAL_ID_NAME);
        addToSetIfNotNull(selectedColumnNames, CacheUtil.INTERNAL_STATEID);

        for (PlaceSettingsDTO placeSetting : placeSettings) {
            addToSetIfNotNull(selectedColumnNames, placeSetting.getLatColumn());
            addToSetIfNotNull(selectedColumnNames, placeSetting.getLongColumn());
        }
        return selectedColumnNames;
    }

    private void gatherSelectedColumnsForPlaceId(Set<String> selectedColumnNames, int placeId) {
        addToSetIfNotNull(selectedColumnNames, placeSettings.get(placeId).getSizeColumn());
        addToSetIfNotNull(selectedColumnNames, placeSettings.get(placeId).getTypeColumn());
        addToSetIfNotNull(selectedColumnNames, placeSettings.get(placeId).getLabelColumn());
        addToSetIfNotNull(selectedColumnNames, placeSettings.get(placeId).getIconColumn());

        for (MapTooltipFieldDTO tooltipField : mapSettings.getTooltipFields().get(placeId)) {
         addToSetIfNotNull(selectedColumnNames, tooltipField.getFieldColumn());
      }
    }

    private void populateMapCache(ResultSet rs) throws SQLException {
        ResultSetProcessor processor = new ResultSetProcessor(mapCacheHandler, dataView, mapSettings, mapTheme, rs, waitForRegistry);
        processor.process();
    }
}

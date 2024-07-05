package csi.server.business.selection.toselection;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.service.FilterActionsService;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.BundleMapNode;
import csi.server.business.visualization.map.FilterStringGenerator;
import csi.server.business.visualization.map.LinkGeometryPlus;
import csi.server.business.visualization.map.MapBundleQueryBuilder;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapLink;
import csi.server.business.visualization.map.MapNode;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.business.visualization.map.MapSummaryQueryBuilder;
import csi.server.business.visualization.map.QueryExecutor;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SummaryMapSelection;
import csi.server.common.model.visualization.selection.TrackmapSelection;
import csi.server.util.CacheUtil;
import csi.server.util.sql.SQLFactory;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.TrackSettingsDTO;

public class MapRowsToSelectionConverter implements RowsToSelectionConverter {
   private static final Logger LOG = LogManager.getLogger(MapRowsToSelectionConverter.class);

   private static String ROW_ID = "__row_id__";
   private static String TRACK_ID = "__track_id__";
   private static String TRACK_NAME = "__track_name__";
   private static String SEQUENCE = "__sequence__";

   private final MapViewDef visualizationDef;
   private final DataView dataView;
   private final SQLFactory sqlFactory;
   private final FilterActionsService filterActionsService;

   public MapRowsToSelectionConverter(DataView dataView, MapViewDef visualizationDef, SQLFactory sqlFactory,
                                      FilterActionsService filterActionsService) {
      this.dataView = dataView;
      this.visualizationDef = visualizationDef;
      this.sqlFactory = sqlFactory;
      this.filterActionsService = filterActionsService;
   }

   private static LinkGeometryPlus getLinkGeometryPlus(MapLink mapLink) {
      LinkGeometry linkGeometry = mapLink.getLinkGeometry();
      LinkGeometryPlus linkGeometryPlus = new LinkGeometryPlus(linkGeometry.getLinkType(),
                                                               linkGeometry.getNode1Geometry(),
                                                               linkGeometry.getNode2Geometry());

      for (int rowId : mapLink.getRowIds()) {
         linkGeometryPlus.addRowId(rowId);
      }
      return linkGeometryPlus;
   }

   @Override
   public Selection toSelection(Set<Integer> rowIds) {
      String dvUuid = dataView.getUuid();
      String mapUuid = visualizationDef.getUuid();
      AbstractMapSelection mapSelection;

      if (MapServiceUtil.isUseTrack(dvUuid, mapUuid)) {
         mapSelection = new TrackmapSelection(mapUuid);
      } else {
         mapSelection = new SummaryMapSelection(mapUuid);
      }
      if (rowIds != null) {
         if (MapServiceUtil.isHandleBundle(dataView.getUuid(), visualizationDef.getUuid())) {
            handleBundle(rowIds, mapUuid, mapSelection);
         } else if (MapServiceUtil.isUseTrack(dataView.getUuid(), visualizationDef.getUuid())) {
            handleTrack(rowIds, mapUuid, mapSelection);
         } else {
            handlePointSummary(rowIds, mapSelection);
         }
      }
      return mapSelection;
   }

   private void handleBundle(Set<Integer> rowIds, String mapUuid, AbstractMapSelection mapSelection) {
      MapBundleQueryBuilder mapQueryBuilder = new MapBundleQueryBuilder();
      mapQueryBuilder.setDataView(dataView);
      mapQueryBuilder.setViewDef(visualizationDef);
      mapQueryBuilder.setSqlFactory(sqlFactory);
      mapQueryBuilder.setFilterActionsService(filterActionsService);

      List<Integer> idsAsList = new ArrayList<>(rowIds);

      if (!idsAsList.isEmpty()) {
         List<String> categories = mapQueryBuilder.rowIdsToSelectionInfo(idsAsList);
         Map<Geometry,AugmentedMapNode> mapNodeByGeometryMap = MapCacheUtil.getMapNodeByGeometryMap(mapUuid);

         if (mapNodeByGeometryMap != null) {
            for (Map.Entry<Geometry,AugmentedMapNode> entry : mapNodeByGeometryMap.entrySet()) {
               MapNode mapNode = entry.getValue();

               if (mapNode != null) {
                  BundleMapNode bundleMapNode = (BundleMapNode) mapNode;

                  if (categories.contains(bundleMapNode.getBundleValue())) {
                     mapSelection.addNode(mapNode.getGeometry());
                  }
               }
            }
         }
      }
      handleLinks(rowIds, mapSelection, mapUuid);
   }

   private void handleLinks(Set<Integer> rowIds, AbstractMapSelection mapSelection, String mapViewDefUuid) {
      Map<Long,MapLink> mapLinkByIdMap = MapServiceUtil.getMapLinkByIdMap(mapViewDefUuid);

      if (mapLinkByIdMap != null) {
         for (Map.Entry<Long,MapLink> entry : mapLinkByIdMap.entrySet()) {
            MapLink mapLink = entry.getValue();

            if (mapLink != null) {
               for (Integer rowId : mapLink.getRowIds()) {
                  if (rowIds.contains(rowId)) {
                     mapSelection.addLink(getLinkGeometryPlus(mapLink));
                     break;
                  }
               }
            }
         }
      }
   }

   private void handleTrack(Set<Integer> rowIds, String mapUuid, AbstractMapSelection mapSelection) {
      handleTrackLinks(rowIds, mapSelection, mapUuid);
   }

   private void handleTrackLinks(Set<Integer> rowIds, AbstractMapSelection mapSelection, String mapViewDefUuid) {
      TrackMapRowToSelectionConverter converter =
         new TrackMapRowToSelectionConverter(mapViewDefUuid, rowIds, mapSelection);

      converter.convert();
   }

   private void handlePointSummary(Set<Integer> rowIds, AbstractMapSelection mapSelection) {
      MapSummaryQueryBuilder mapQueryBuilder = new MapSummaryQueryBuilder();

      mapQueryBuilder.setDataView(dataView);
      mapQueryBuilder.setViewDef(visualizationDef);
      mapQueryBuilder.setSqlFactory(sqlFactory);
      mapQueryBuilder.setFilterActionsService(filterActionsService);

      List<Integer> idsAsList = new ArrayList<>(rowIds);

      if (!idsAsList.isEmpty()) {
         MapServiceUtil.selectionAddNodes(mapQueryBuilder.rowIdsToSelectionInfo(idsAsList), mapSelection);
      }
   }

   class TrackMapRowToSelectionConverter {
      private String mapViewDefUuid;
      private Set<Integer> rowIds;
      private AbstractMapSelection mapSelection;
      private MapSettingsDTO mapSettingsDTO;
      private int trackId;
      private TrackSettingsDTO trackSettingsDTO;
      private TrackMapSummaryGrid mapSummaryGrid;

      public TrackMapRowToSelectionConverter(String mapViewDefUuid, Set<Integer> rowIds,
                                             AbstractMapSelection mapSelection) {
         this.mapViewDefUuid = mapViewDefUuid;
         this.rowIds = rowIds;
         this.mapSelection = mapSelection;
      }

      void convert() {
         mapSettingsDTO = MapCacheUtil.getMapSettings(mapViewDefUuid);

         if (mapSettingsDTO != null) {
            trackId = 0;
            trackSettingsDTO = mapSettingsDTO.getTrackSettings().get(trackId);
            mapSummaryGrid = MapCacheUtil.getTrackMapSummaryGrid(mapViewDefUuid);

            QueryExecutor.execute(LOG, generateQuery(), (ResultSet rs) -> {
               while (rs.next()) {
                  int trackId = rs.getInt(TRACK_ID);
                  String trackName = rs.getString(TRACK_NAME);
                  TrackidTracknameDuple key = new TrackidTracknameDuple(trackId, trackName);
                  int rowId = rs.getInt(ROW_ID);
                  Object sequenceValue = rs.getObject(SEQUENCE);
                  Set<LinkGeometry> linkGeometries = mapSummaryGrid.getLinkGeometries(key, sequenceValue, rowId);
                  mapSelection.addLinks(linkGeometries);
               }
            });
         }
      }

      private String generateQuery() {
         if (rowIds.size() < 100) {
            return getSelectClause() + " " + getFromClause() + " " + getWhereClause();
         } else {
            return getSelectClause() + " " + getValuesFromClause() + " " + getValuesWhereClause();
         }
      }

      private String getSelectClause() {
         Set<String> selectedColumnNames = new TreeSet<String>();
         selectedColumnNames.add(CacheUtil.INTERNAL_ID_NAME + " " + ROW_ID);
         selectedColumnNames.add(trackId + " " + TRACK_ID);
         if (trackSettingsDTO.getIdentityColumn() == null) {
            selectedColumnNames.add("'" + trackSettingsDTO.getIdentityName() + "' " + TRACK_NAME);
         } else {
            selectedColumnNames.add("\"" + trackSettingsDTO.getIdentityColumn() + "\" " + TRACK_NAME);
         }
         selectedColumnNames.add("\"" + trackSettingsDTO.getSequenceColumn() + "\" " + SEQUENCE);
         return selectedColumnNames.stream().collect(Collectors.joining(", ", "SELECT ", ""));
      }

      private String getFromClause() {
         return "FROM " + CacheUtil.getQuotedCacheTableName(dataView.getUuid());
      }

      private String getValuesFromClause() {
         List<String> rowIdStrs = new ArrayList<String>();

         rowIds.forEach(rowId -> rowIdStrs.add("(" + rowId + ")"));

         return new StringBuilder("FROM ").append(CacheUtil.getQuotedCacheTableName(dataView.getUuid()))
                          .append(" JOIN (VALUES ").append(rowIdStrs.stream().collect(Collectors.joining(", ")))
                          .append(") ex(ex_" + CacheUtil.INTERNAL_ID_NAME + ") ON (" +
                                  CacheUtil.INTERNAL_ID_NAME + " = ex_" + CacheUtil.INTERNAL_ID_NAME + ")")
                          .toString();
      }

      private String getWhereClause() {
         String filterString = FilterStringGenerator.generateFilterString(dataView, visualizationDef, mapSettingsDTO);
         String idString = CacheUtil.INTERNAL_ID_NAME + " IN (" +
                           rowIds.stream().map(i -> i.toString()).collect(Collectors.joining(", ")) + ")";
         String whereClause = "WHERE " + idString;
         whereClause += " AND \"" + trackSettingsDTO.getSequenceColumn() + "\" IS NOT NULL";
         if ((filterString != null) && !filterString.isEmpty()) {
            whereClause += " AND " + filterString;
         }
         return whereClause;
      }

      private String getValuesWhereClause() {
         String filterString = FilterStringGenerator.generateFilterString(dataView, visualizationDef, mapSettingsDTO);
         String whereClause = "WHERE \"" + trackSettingsDTO.getSequenceColumn() + "\" IS NOT NULL";
         if ((filterString != null) && !filterString.isEmpty()) {
            whereClause += " AND " + filterString;
         }
         return whereClause;
      }
   }
}

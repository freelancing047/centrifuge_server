package csi.server.business.helper.field;

import java.util.List;
import java.util.Set;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.chart.SortDefinition;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapBundleDefinition;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.common.model.visualization.matrix.MatrixSortDefinition;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSortField;
import csi.server.common.model.visualization.timeline.TimelineEventDefinition;
import csi.server.common.model.visualization.timeline.TimelineField;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.shared.core.field.FieldReferences;

/**
 * Determines whether the DataView references the passed in Field in any place other than a direct reference in it's FieldList.
 * Builds a FieldReferences object that contains locations of the referenced field.
 * @author Centrifuge Systems, Inc.
 */
public class FieldReferencesFromDataView {

    private final DataViewDef meta;
    private final FieldListAccess model;
    private final FieldDef fieldDef;
    private final FieldReferences fieldReferences;

    public FieldReferencesFromDataView(DataViewDef metaIn, FieldDef fieldDefIn) {
        this.meta = metaIn;
        this.model = meta.getModelDef().getFieldListAccess();
        this.fieldDef = fieldDefIn;
        this.fieldReferences = new FieldReferences();
    }

    public FieldReferences buildFieldReferences() {
        addVisualizations();
        addFilters();
        addLinkups();
        addFields();

        return fieldReferences;
    }

    private void addVisualizations() {
        for (VisualizationDef visualizationDef : meta.getModelDef().getVisualizations()) {
            if (doesVisualizationHaveFieldDef(visualizationDef)) {
                fieldReferences.addVisualization(visualizationDef.getName());
            }
        }
    }

    public boolean doesVisualizationHaveFieldDef(VisualizationDef visualizationDef) {
        if (visualizationDef instanceof TableViewDef) {
            return isInTableDef((TableViewDef) visualizationDef);
        }
        if (visualizationDef instanceof DrillChartViewDef) {
            return isInChartDef((DrillChartViewDef) visualizationDef);
        }
        if (visualizationDef instanceof MatrixViewDef) {
            return isInMatrixDef((MatrixViewDef) visualizationDef);
        }
        if (visualizationDef instanceof RelGraphViewDef) {
            return isInGraphDef((RelGraphViewDef) visualizationDef);
        }
        if (visualizationDef instanceof MapViewDef) {
            return isInMapDef((MapViewDef) visualizationDef);
        }
        if (visualizationDef instanceof TimelineViewDef) {
            return isInTimelineDef((TimelineViewDef)visualizationDef);
        }

        return false;
    }

    private boolean isInTimelineDef(TimelineViewDef visualizationDef) {
        for(TimelineEventDefinition event: visualizationDef.getTimelineSettings().getEvents()) {
            if((event.getStartField() != null) && fieldDef.equals(event.getStartField().getFieldDef())){
                return true;
            }
            if((event.getEndField() != null) && fieldDef.equals(event.getEndField().getFieldDef())) {
                return true;
            }
            if(fieldDef.equals(event.getLabelField())) {
                return true;
            }
        }

        if((visualizationDef.getTimelineSettings().getGroupByField() != null)
                && visualizationDef.getTimelineSettings().getGroupByField().equals(fieldDef)) {
            return true;
        }
        if((visualizationDef.getTimelineSettings().getColorByField() != null)
                && visualizationDef.getTimelineSettings().getColorByField().equals(fieldDef)) {
            return true;
        }
        if((visualizationDef.getTimelineSettings().getDotSize() != null)
                && visualizationDef.getTimelineSettings().getDotSize().equals(fieldDef)) {
            return true;
        }

        if(visualizationDef.getTimelineSettings().getFieldList() != null) {
            for(TimelineField field: visualizationDef.getTimelineSettings().getFieldList()) {
                if(field.getFieldDef().equals(fieldDef)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isInGraphDef(RelGraphViewDef relGraphViewDef) {
        if (isInPlayerSettings(relGraphViewDef.getPlayerSettings())) {
            return true;
        }

        for (NodeDef nodeDef : relGraphViewDef.getNodeDefs()) {
            if (isInAttributeDef(nodeDef.getAttributeDefs())) {
               return true;
            }
        }
        for (LinkDef linkDef : relGraphViewDef.getLinkDefs()) {
            if (isInAttributeDef(linkDef.getAttributeDefs())) {
               return true;
            }

            if ((linkDef.getDirectionDef() != null) && fieldDef.equals(linkDef.getDirectionDef().getFieldDef())) {
                return true;
            }
        }
        for (BundleDef bundleDef : relGraphViewDef.getBundleDefs()) {
            for (BundleOp bundleOp : bundleDef.getOperations()) {
                if (fieldDef.equals(bundleOp.getField())) {
                    return true;
                }
                if (isInAttributeDef(bundleOp.getNodeDef().getAttributeDefs())) {
                    return true;
                }
            }
        }

        return false;
    }

   private boolean isInPlayerSettings(GraphPlayerSettings playerSettings) {
      return (fieldDef.equals(playerSettings.startField) || fieldDef.equals(playerSettings.endField));
   }

    private boolean isInAttributeDef(Set<AttributeDef> attributeDefs) {
        for (AttributeDef attributeDef : attributeDefs) {
            if (fieldDef.equals(attributeDef.getFieldDef())) {
                return true;
            }
            if (fieldDef.equals(attributeDef.getTooltipLinkFeildDef())) {
                return true;
            }
        }
        return false;
    }

   private boolean isInMatrixDef(MatrixViewDef matrixViewDef) {
      for (MatrixCategoryDefinition matrixCategoryDefinition : matrixViewDef.getMatrixSettings().getAxisCategories()) {
         if (fieldDef.equals(matrixCategoryDefinition.getFieldDef())) {
            return true;
         }
      }
      for (MatrixSortDefinition matrixSortDefinition : matrixViewDef.getMatrixSettings().getAxisSortDefinitions()) {
         if (fieldDef.equals(matrixSortDefinition.getCategoryDefinition() != null ? matrixSortDefinition.getCategoryDefinition().getFieldDef() : null)) {
            return true;
         }
      }
      return fieldDef.equals((matrixViewDef.getMatrixSettings().getMatrixMeasureDefinition() == null)
                                 ? null
                                 : matrixViewDef.getMatrixSettings().getMatrixMeasureDefinition().getFieldDef());
   }

    private boolean isInChartDef(DrillChartViewDef drillChartViewDef) {
        for (CategoryDefinition categoryDefinition : drillChartViewDef.getChartSettings().getCategoryDefinitions()) {
            if (fieldDef.equals(categoryDefinition.getFieldDef())) {
                return true;
            }
        }
        for (MeasureDefinition measureDefinition : drillChartViewDef.getChartSettings().getMeasureDefinitions()) {
            if (fieldDef.equals(measureDefinition.getFieldDef())) {
                return true;
            }
        }
        for (SortDefinition sortDefinition : drillChartViewDef.getChartSettings().getSortDefinitions()) {
            if (fieldDef.equals(sortDefinition.getCategoryDefinition() != null ? sortDefinition.getCategoryDefinition().getFieldDef() : null)) {
                return true;
            }
            if (fieldDef.equals(sortDefinition.getMeasureDefinition() != null ? sortDefinition.getMeasureDefinition().getFieldDef() : null)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInTableDef(TableViewDef visualizationDef) {
        for (FieldDef def : visualizationDef.getTableViewSettings().getVisibleFieldDefs(meta.getModelDef())) {
            if (def.equals(fieldDef)) {
                return true;
            }
        }
        for (TableViewSortField field : visualizationDef.getTableViewSettings().getSortFields()) {
            if (fieldDef.equals(field.getFieldDef(meta.getModelDef()))) {
                return true;
            }
        }

        return false;
    }

    private boolean isInMapDef(MapViewDef visualizationDef) {
        MapSettings mySettings = visualizationDef.getMapSettings();
        if (null != mySettings) {
            if (fieldDef.equals(mySettings.getWeightField())) {
                return true;
            }
            List<MapBundleDefinition> myBundles = mySettings.getMapBundleDefinitions();
            if ((myBundles != null) && !myBundles.isEmpty()) {
                for (MapBundleDefinition myBundle : myBundles) {
                    if (fieldDef.equals(myBundle.getFieldDef())) {
                        return true;
                    }
                }
            }
            List<MapPlace> myPlaces = mySettings.getMapPlaces();
            if ((myPlaces != null) && !myPlaces.isEmpty()) {
                for (MapPlace myPlace : myPlaces) {
                    if (fieldDef.equals(myPlace.getIconField())) {
                        return true;
                    }
                    if (fieldDef.equals(myPlace.getLabelField())) {
                        return true;
                    }
                    if (fieldDef.equals(myPlace.getLatField())) {
                        return true;
                    }
                    if (fieldDef.equals(myPlace.getLongField())) {
                        return true;
                    }
                    if (fieldDef.equals(myPlace.getSizeField())) {
                        return true;
                    }
                    if (fieldDef.equals(myPlace.getTypeField())) {
                        return true;
                    }
                    List<String> myToolTipIds = myPlace.getTooltipFieldIds();
                    if ((myToolTipIds != null) && !myToolTipIds.isEmpty()) {
                        String myLocalId = fieldDef.getLocalId();
                        if (null != myLocalId) {
                            for (String myToolTipId : myToolTipIds) {
                                if (myLocalId.equals(myToolTipId)) {
                                    return true;
                                }
                            }
                        }
                    }
                    /* Alternate method for tooltips
                    List<FieldDef> myToolTips = myPlace.getTooltipFields(model);
                    if ((myToolTips != null) && !myToolTips.isEmpty()) {
                        for (FieldDef myToolTip : myToolTips) {
                            if (fieldDef.equals(myToolTip)) {
                                return true;
                            }
                        }
                    }
                    */
                }
            }
        }
        return false;
    }

    private void addFilters() {
        for (Filter filter : meta.getFilters()) {
            for (FieldDef def : filter.getReferencedFields()) {
                if (def.equals(fieldDef)) {
                    fieldReferences.addFilter(filter.getName());
                }
            }
        }
    }

    private void addLinkups() {
        for (LinkupMapDef linkupMapDef : meta.getLinkupDefinitions()) {
            if (linkupMapDef.isIdRequired(fieldDef.getLocalId())) {
                fieldReferences.addLinkup(linkupMapDef.getLinkupName());
            }
        }
    }

    private void addFields() {
        List<FieldDef> allDefs = meta.getModelDef().getFieldListAccess().getDependentFieldDefs();
        for (FieldDef def : allDefs) {
            //Ignore the case where a field is directly referenced in the DataView's field list.
            if(def.equals(fieldDef)) {
               continue;
            }

            FieldCycleDetector fieldCycleDetector = new FieldCycleDetector(model);
            fieldCycleDetector.detectCycle(def);
            Set<FieldDef> dependencies = fieldCycleDetector.getEncounteredFields();
            if (dependencies.contains(fieldDef)) {
               fieldReferences.addField(def.getFieldName());
            }
        }
    }
}

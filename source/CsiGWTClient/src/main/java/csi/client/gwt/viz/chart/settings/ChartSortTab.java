/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.chart.settings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.misc.ReactsToVisibilityChange;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.client.gwt.widget.ui.form.SortOrderCell;
import csi.server.common.model.FieldType;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.chart.SortDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartSortTab extends ChartSettingsComposite implements ReactsToVisibilityChange {

    private ChartCategoriesTab categoriesTab;
    private ChartMeasuresTab measuresTab;

    private Grid<SortDefinition> grid;
    @UiField
    GridContainer gridContainer;
    @UiField
    SortDefinitionComboBox fieldList;

    interface SortDefinitionPropertyAccess extends PropertyAccess<SortDefinition> {

        @Path("uuid")
        public ModelKeyProvider<SortDefinition> key();

        @Path("displayName")
        public ValueProvider<SortDefinition, String> name();

        public ValueProvider<SortDefinition, SortOrder> sortOrder();
    }

    interface SpecificUiBinder extends UiBinder<Widget, ChartSortTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static SortDefinitionPropertyAccess propertyAccess = GWT.create(SortDefinitionPropertyAccess.class);

    public ChartSortTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        initGrid();
    }

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    @SuppressWarnings("unchecked")
    private void initGrid() {
        GridComponentManager<SortDefinition> manager = WebMain.injector.getGridFactory().create(propertyAccess.key());

        ColumnConfig<SortDefinition, String> dragCol = manager.create(propertyAccess.name(), 20, _constants.chartSortTab_dragCol(), false, true);
        dragCol.setCell(DragCell.<String> create());
        dragCol.setResizable(false);

        IdentityValueProvider<SortDefinition> identity = new IdentityValueProvider<SortDefinition>();
        ColumnConfig<SortDefinition, SortDefinition> nameCol = manager.create(identity, 150, _constants.chartSortTab_nameCol(), false, true);
        nameCol.setCell(new SortDefinitionNameCell());

        ColumnConfig<SortDefinition, SortOrder> sortOrder = manager.create(propertyAccess.sortOrder(), 50, _constants.chartSortTab_sortOrder(),
                false, true);
        sortOrder.setCell(new SortOrderCell());

        List<ColumnConfig<SortDefinition, ?>> columns = manager.getColumnConfigList();
        ColumnModel<SortDefinition> cm = new ColumnModel<SortDefinition>(columns);
        ListStore<SortDefinition> gridStore = manager.getStore();

        grid = new ResizeableGrid<SortDefinition>(gridStore, cm);
        grid.getView().setAutoExpandColumn(nameCol);
        GridHelper.setDraggableRowsDefaults(grid);

        gridContainer.setGrid(grid);
    }

    public void setCategoriesTab(ChartCategoriesTab categoriesTab) {
        this.categoriesTab = categoriesTab;
    }

    public void setMeasuresTab(ChartMeasuresTab measuresTab) {
        this.measuresTab = measuresTab;
    }

    @Override
    public void updateViewFromModel() {
        grid.getStore().clear();
        for (SortDefinition sd : getDrillChartSettings().getSortDefinitions()) {
            grid.getStore().add(sd);
        }
    }

    @Override
    public void updateModelWithView() {
        updateCurrentSelections();
        grid.getStore().commitChanges();
        getDrillChartSettings().getSortDefinitions().clear();
        int i = 0;
        for(SortDefinition sd : grid.getStore().getAll()) {
            sd.setListPosition(i++);
            getDrillChartSettings().getSortDefinitions().add(sd);
        }
    }

    @UiHandler("buttonAddSortField")
    public void handleAddSortField(ClickEvent event) {
        SortDefinition sd = fieldList.getCurrentValue();
        grid.getStore().add(sd);
        fieldList.getStore().remove(sd);
        updateAvailableSelections();
    }

    @UiHandler("buttonDeleteSortField")
    public void handleDeleteSortField(ClickEvent event) {
        List<SortDefinition> list = grid.getSelectionModel().getSelectedItems();
        for (SortDefinition sd : list) {
            fieldList.getStore().add(sd);
            grid.getStore().remove(sd);
        }
        updateAvailableSelections();
    }

    @Override
    public void onShow() {
        updateCurrentSelections();
        updateAvailableSelections();
    }

    /**
     * Update field list to reflect only those elements that are currently selected in categories and measures and
     * not in the current selection.
     */
    private void updateAvailableSelections() {
        Set<SortDefinition> availableSelections = new HashSet<SortDefinition>();

        for (CategoryDefinition categoryDef : categoriesTab.getCurrentCategories()) {
            if (categoryDef.getFieldDef().getFieldType() != FieldType.STATIC) {
                availableSelections.add(new SortDefinition(categoryDef));
            }
        }

        //check if count(*) is in there as a sort measure.
        if (getDrillChartSettings().isUseCountStarForMeasure() || getDrillChartSettings().getMeasureDefinitions().size() == 0) {
            SortDefinition sortDefinition = new SortDefinition();
            sortDefinition.setCountStar(true);
            availableSelections.add(sortDefinition);
        } else {
            // for all measures under the measures tab, create a sort def
            for (MeasureDefinition md : measuresTab.getCurrentMeasures()) {
            	SortDefinition sortDefinition = new SortDefinition(md);
            	if(availableSelections.contains(sortDefinition)){
            		availableSelections.remove(sortDefinition);
            	}
                availableSelections.add(new SortDefinition(md));
            }
        }
        availableSelections.removeAll(grid.getStore().getAll());

        fieldList.getStore().clear();
        fieldList.getStore().addAll(availableSelections);
        if (availableSelections.size() > 0) {
            fieldList.setSelectedIndex(0);
        }
    }

    private void updateCurrentSelections() {
        List<CategoryDefinition> categories = categoriesTab.getCurrentCategories();
        List<MeasureDefinition> measures = measuresTab.getCurrentMeasures();

        List<SortDefinition> definitionsToRemove = new ArrayList<SortDefinition>();

        for (SortDefinition sd : grid.getStore().getAll()) {
            boolean found = false;
            if (sd.isCountStar() && (getDrillChartSettings().isUseCountStarForMeasure() || getDrillChartSettings().getMeasureDefinitions().size() == 0)) {
                found = true;
            } else if (sd.isCategory()) {
                for (CategoryDefinition cd : categories) {
                    if (cd.getUuid().equals(sd.getCategoryDefinition() != null ? sd.getCategoryDefinition().getUuid() : null)) {
                        found = true;
                        break;
                    }
                }
            } else {
                for (MeasureDefinition md : measures) {
                    if (md.getUuid().equals(sd.getMeasureDefinition() != null ? sd.getMeasureDefinition().getUuid() : null)) {
                        found = true;
                        break;
                    }
                }
            } // end else for if (sd.isCategory) ...
            if (!found) {
                definitionsToRemove.add(sd);
            }
        } // end for over SortDefinitions
        for (SortDefinition sortDefinition : definitionsToRemove) {
            grid.getStore().remove(sortDefinition);
        }

        grid.getView().refresh(false);
    }


    @Override
    public void onHide() {
        // noop
    }
}

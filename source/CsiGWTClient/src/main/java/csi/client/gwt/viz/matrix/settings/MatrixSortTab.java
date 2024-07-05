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
package csi.client.gwt.viz.matrix.settings;

import java.util.Comparator;
import java.util.List;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
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
import csi.client.gwt.widget.misc.AttributeDefinitionNameCell;
import csi.client.gwt.widget.misc.ReactsToVisibilityChange;
import csi.client.gwt.widget.ui.form.SortOrderButton;
import csi.client.gwt.widget.ui.form.SortOrderCell;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.matrix.Axis;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixSortDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixSortTab extends MatrixSettingsComposite implements ReactsToVisibilityChange {

    private MatrixCategoriesTab categoriesTab;
    private MatrixMeasuresTab measuresTab;

    private Grid<MatrixSortDefinition> axisGrid;
    
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    @UiField
    GridContainer axisGridContainer;
    @UiField
    RadioButton sortTypeAxis, sortTypeMeasure;
    @UiField
    FlowPanel measureFieldName;
    @UiField
    SortOrderButton measureSortOrder;
    
    SortOrder xOrder = null;
    SortOrder yOrder = null;

    interface SortDefinitionPropertyAccess extends PropertyAccess<MatrixSortDefinition> {

        @Path("id")
        public ModelKeyProvider<MatrixSortDefinition> key();

        public ValueProvider<MatrixSortDefinition, String> axisName();

        public ValueProvider<MatrixSortDefinition, SortOrder> sortOrder();

        public ValueProvider<MatrixSortDefinition, MatrixCategoryDefinition> categoryDefinition();
    }

    interface SpecificUiBinder extends UiBinder<Widget, MatrixSortTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static SortDefinitionPropertyAccess propertyAccess = GWT.create(SortDefinitionPropertyAccess.class);

    public MatrixSortTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        initAxisGrid();
        initHandler();
    }

    private void initHandler() {

        
    }

    @SuppressWarnings("unchecked")
    private void initAxisGrid() {
        GridComponentManager<MatrixSortDefinition> manager = WebMain.injector.getGridFactory().create(propertyAccess.key());

        manager.create(propertyAccess.axisName(), 50, _constants.matrixSortTab_axis(), false, true);

        ColumnConfig<MatrixSortDefinition, MatrixCategoryDefinition> nameCol = manager.create(
                propertyAccess.categoryDefinition(), 150, _constants.matrixSortTab_field(), false, true);
        AttributeDefinitionNameCell<MatrixCategoryDefinition> cell = new AttributeDefinitionNameCell<MatrixCategoryDefinition>();
        cell.setIncludeItemDefinition(false);
        nameCol.setCell(cell);

        ColumnConfig<MatrixSortDefinition, SortOrder> sortOrder = manager.create(propertyAccess.sortOrder(), 50, _constants.matrixSortTab_sort(),
                false, true);
        sortOrder.setCell(new SortOrderCell());

        List<ColumnConfig<MatrixSortDefinition, ?>> columns = manager.getColumnConfigList();
        ColumnModel<MatrixSortDefinition> cm = new ColumnModel<MatrixSortDefinition>(columns);
        ListStore<MatrixSortDefinition> gridStore = manager.getStore();

        axisGrid = new ResizeableGrid<MatrixSortDefinition>(gridStore, cm);
        axisGrid.getView().setAutoExpandColumn(nameCol);
        GridHelper.setDefaults(axisGrid);

        // Ensure x-axis is first
        axisGrid.getStore().addSortInfo(new StoreSortInfo<MatrixSortDefinition>(new Comparator<MatrixSortDefinition>() {

            @Override
            public int compare(MatrixSortDefinition o1, MatrixSortDefinition o2) {
                return o1.getAxisName().compareTo(o2.getAxisName());
            }
        }, SortDir.ASC));

        axisGridContainer.setGrid(axisGrid);
    }

    public void setCategoriesTab(MatrixCategoriesTab categoriesTab) {
        this.categoriesTab = categoriesTab;
    }

    public void setMeasuresTab(MatrixMeasuresTab measuresTab) {
        this.measuresTab = measuresTab;
    }

    @Override
    public void updateViewFromModel() {
        
        sortTypeAxis.setValue(getMatrixSettings().isSortByAxis());
        sortTypeMeasure.setValue(!getMatrixSettings().isSortByAxis());


        for (MatrixSortDefinition sd : getMatrixSettings().getAxisSortDefinitions()) {
            axisGrid.getStore().add(sd);
        }

        if (getMatrixSettings().getMatrixMeasureDefinition() != null) {
            measureFieldName.getElement().setInnerSafeHtml(
                    AttributeDefinitionNameCell.toHtml(getMatrixSettings().getMatrixMeasureDefinition(), false));
        }
        measureSortOrder.setValue(getMatrixSettings().getMeasureSortOrder());
    }

    @Override
    public void updateModelWithView() {
        updateGrids();
        getMatrixSettings().setSortByAxis(sortTypeAxis.getValue());

        getMatrixSettings().getAxisSortDefinitions().clear();

        for (MatrixSortDefinition sd : axisGrid.getStore().getAll()) {
            getMatrixSettings().getAxisSortDefinitions().add(sd);
        }
        getMatrixSettings().setMeasureSortOrder(measureSortOrder.getValue());
    }

    @Override
    public void onShow() {
        updateGrids();
        if (measuresTab.isUseCountForMeasure()) {
            measureFieldName.getElement().setInnerSafeHtml(SafeHtmlUtils.fromSafeConstant("Count (*)"));
        } else {
            MatrixMeasureDefinition mmd = measuresTab.getCurrentMeasure(getMatrixSettings());
            if (mmd != null) {
                measureFieldName.getElement().setInnerSafeHtml(AttributeDefinitionNameCell.toHtml(mmd, false));
            }
        }
    }
    
   
    /**
     * Update the grids to show the categories & measures that are currently active
     */
    private void updateGrids() {

        //Need to commit first to get proper sort orders
        axisGrid.getStore().commitChanges();
        
        for (MatrixSortDefinition sd : axisGrid.getStore().getAll()) {
            if(sd.getCategoryDefinition().getAxis() == Axis.X){
                xOrder = sd.getSortOrder();
            } else {
                yOrder = sd.getSortOrder();
            }
        }
        

        axisGrid.getStore().clear();
        
        
        for (MatrixCategoryDefinition cd : categoriesTab.getCurrentCategories()) {
            if (cd.getAxis() == Axis.X) {
                MatrixSortDefinition sd = new MatrixSortDefinition();
                sd.setCategoryDefinition(cd);
                if(xOrder != null)
                    sd.setSortOrder(SortOrder.valueOf(xOrder.name()));
                axisGrid.getStore().add(sd);
            } else if (cd.getAxis() == Axis.Y) {
                MatrixSortDefinition sd = new MatrixSortDefinition();
                sd.setCategoryDefinition(cd);
                if(yOrder != null)
                    sd.setSortOrder(SortOrder.valueOf(yOrder.name()));
                axisGrid.getStore().add(sd);
            }
        }

        axisGrid.getStore().commitChanges();
        axisGrid.getView().refresh(true);
    }

    @Override
    public void onHide() {
        for (MatrixSortDefinition sd : axisGrid.getStore().getAll()) {
            if(sd.getCategoryDefinition().getAxis() == Axis.X){
                xOrder = sd.getSortOrder();
            } else {
                yOrder = sd.getSortOrder();
            }
        }
    }

    public void removeCategory() {
        updateGrids();
    }


}

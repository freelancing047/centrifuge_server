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
package csi.client.gwt.viz.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.RowClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowExpander;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.cells.readonly.FieldDefValueCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.bundle.BundleFunctionDisplayCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.util.ValuePair;
import csi.server.util.sql.api.BundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FilterDisplayWidget extends ResizeComposite {

    private Grid<FilterExpression> grid;

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    @UiField
    InlineLabel filterName;
    @UiField
    FluidRow referencedBy;
    @UiField
    GridContainer gridContainer;

    private ReferencedFilterDialog dialog;

    private final int MAXIMUM_STRING_LENGTH = 70;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    interface FilterExpressionPropertyAccess extends PropertyAccess<FilterExpression> {

        @Path("uuid")
        public ModelKeyProvider<FilterExpression> key();

        @Path("expressionIdLabel")
        public ValueProvider<FilterExpression, String> reference();

        @Path("fieldDef")
        public ValueProvider<FilterExpression, FieldDef> field();

        @Path("fieldDefValue")
        public ValueProvider<FilterExpression, ValuePair<Boolean, FieldDef>> fieldDefValue();



        public ValueProvider<FilterExpression, Boolean> isSelectionFilter();

        public ValueProvider<FilterExpression, BundleFunction> bundleFunction();

        public ValueProvider<FilterExpression, List<BundleFunctionParameter>> bundleFunctionParameters();

        public ValueProvider<FilterExpression, String> negatedDescription();

        public ValueProvider<FilterExpression, String> operatorDescription();
    }

    private static FilterExpressionPropertyAccess propertyAccess = GWT.create(FilterExpressionPropertyAccess.class);

    interface SpecificUiBinder extends UiBinder<Widget, FilterDisplayWidget> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public FilterDisplayWidget() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        initGrid();
    }

    @SuppressWarnings("unchecked")
    private void initGrid() {
        final GridComponentManager<FilterExpression> manager = WebMain.injector.getGridFactory().create(
                propertyAccess.key());

        RowExpander<FilterExpression> expander = new RowExpander<FilterExpression>(
                new IdentityValueProvider<FilterExpression>(), new AbstractCell<FilterExpression>() {

            @Override
            public void render(Context context, FilterExpression value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<p style='margin: 5px;'>" + value.getDescription() + "</p>");
            }
        });

        manager.create(propertyAccess.reference(), 40, _constants.filterDisplayWidget_referencedBy(), false, true);
        ColumnConfig<FilterExpression, ValuePair<Boolean, FieldDef>> nameCol = manager.create(propertyAccess.fieldDefValue(), 150, _constants.filter_field(), false,
                true);
        nameCol.setCell(new FieldDefValueCell());
        ColumnConfig<FilterExpression, BundleFunction> bundle = manager.create(propertyAccess.bundleFunction(), 100,
                _constants.filter_bundle(), false, true);
        bundle.setCell(new BundleFunctionDisplayCell(manager.getStore(), propertyAccess.isSelectionFilter()));

        manager.create(propertyAccess.negatedDescription(), 40, _constants.filterDisplayWidget_negatedDes(), false, true);
        manager.create(propertyAccess.operatorDescription(), 110, _constants.filterDisplayWidget_operatorDes(), false, true);
        manager.create(new FilterValueProvider(), 150, _constants.filterDisplayWidget_valueDefDes(), false, true);

        List<ColumnConfig<FilterExpression, ?>> columns = manager.getColumnConfigList();
        columns.add(0, expander);

        ColumnModel<FilterExpression> cm = new ColumnModel<FilterExpression>(columns);
        ListStore<FilterExpression> gridStore = manager.getStore();

        grid = new ResizeableGrid<FilterExpression>(gridStore, cm);
        grid.getView().setAutoExpandColumn(nameCol);
        grid.addRowClickHandler(new RowClickEvent.RowClickHandler() {
            @Override
            public void onRowClick(RowClickEvent rowClickEvent) {

                grid.getSelectionModel().selectAll();
            }
        });
        expander.initPlugin(grid);
        GridHelper.setDefaults(grid);

        gridContainer.setGrid(grid);
    }

    public void display(Filter selectedFilter) {
        referencedBy.clear();
        if (selectedFilter != null) {
            String referencedByText = "";
            List<String> visualizationsWithCurrentFilter = new ArrayList<String>();
            Multimap<String, String> worksheetVizWithCurrentFilter = ArrayListMultimap.create();
            List<WorksheetDef> worksheets = WebMain.injector.getMainPresenter().getDataViewPresenter(false).getDataView().getMeta().getModelDef().getWorksheets();
            List<VisualizationDef> visualizations = WebMain.injector.getMainPresenter().getDataViewPresenter(false).getDataView().getMeta().getModelDef().getVisualizations();

            for (VisualizationDef visualization : visualizations) {
                if (visualization.getFilter() != null) {
                    if (visualization.getFilter().getUuid().equals(selectedFilter.getUuid())) {
                        //                        WebMain.injector.getMainPresenter().getDataViewPresenter(false).getDataView().getMeta().getModelDef().getWorksheets().get(0).getVisualizations().get(0).getName()
                       visualizationsWithCurrentFilter.add(visualization.getName());
                    }
                }
            }

            for(WorksheetDef worksheet : worksheets) {
                for (VisualizationDef worksheetVisualization : worksheet.getVisualizations()) {
                    if (visualizationsWithCurrentFilter.contains(worksheetVisualization.getName())) {
                        worksheetVizWithCurrentFilter.put(worksheet.getWorksheetName(), worksheetVisualization.getName());
                    }
                }
            }

            String mapText = worksheetVizWithCurrentFilter.toString();
            if (!mapText.isEmpty()) {
                mapText = mapText.replaceAll("\\{", "");
                mapText = mapText.replaceAll("}", "");
            }
            String fullText = mapText;
            dialog = new ReferencedFilterDialog(_constants.filterDisplayWidget_referencedByListTitle(), fullText);

            if (mapText.length() > MAXIMUM_STRING_LENGTH) {
                referencedByText = fullText.substring(0, MAXIMUM_STRING_LENGTH-1) + "...";
            } else if (mapText.length() == 0) {
                referencedByText = _constants.filterDisplayWidget_none();
            } else {
                referencedByText = fullText;
            }

            InlineLabel referencedByList = new InlineLabel();
            referencedByList.getElement().getStyle().setPosition(Style.Position.RELATIVE);
            referencedByList.getElement().getStyle().setTop(5, Style.Unit.PX);
            referencedByList.setText(referencedByText);

            Button referencedByListMore = new Button();
            referencedByListMore.setIcon(IconType.LIST_OL);
            referencedByListMore.setSize(ButtonSize.SMALL);
            referencedByListMore.getElement().getStyle().setFloat(Style.Float.RIGHT);


            referencedByListMore.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    dialog.show();
                }
            });
            referencedBy.add(referencedByList);
            referencedBy.add(referencedByListMore);

            filterName.setText(selectedFilter.getName());

            grid.getStore().clear();
            for (FilterExpression fe : selectedFilter.getFilterDefinition().getFilterExpressions()) {
                grid.getStore().add(fe);
            }
            grid.getSelectionModel().selectAll();

        }
    }
}

package csi.client.gwt.viz.table.settings;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.table.settings.ColumnsTab.FieldProperties;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.ui.form.SortOrderCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.table.TableViewSortField;

public class SortTab extends TableSettingsComposite {

    @UiField
    Button addSortFieldButton;
    @UiField
    Button deleteSortFieldButton;
    @UiField(provided = true)
    Grid<TableViewSortField> sortFieldsGrid;
    @UiField(provided = true)
    ListView<FieldDef, FieldDef> fieldslist;

    private List<TableViewSortField> tableViewSortFields;
    private ListStore<TableViewSortField> sortStore;
    private ListStore<FieldDef> fieldDefListStore;

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    private static SortTabUiBinder uiBinder = GWT.create(SortTabUiBinder.class);

    interface SortTabUiBinder extends UiBinder<Widget, SortTab> {
    }

    interface SortFieldProperties extends PropertyAccess<TableViewSortField> {

        ModelKeyProvider<TableViewSortField> uuid();

        LabelProvider<TableViewSortField> fieldName();

        @Path("fieldDef")
        ValueProvider<TableViewSortField, FieldDef> fieldDef();

        ValueProvider<TableViewSortField, SortOrder> sortOrder();
    }

    public SortTab() {
        FieldProperties fieldProps = GWT.create(FieldProperties.class);
        GridSelectionModel<TableViewSortField> sm = new GridSelectionModel<TableViewSortField>();
        SortFieldProperties sortProps = GWT.create(SortFieldProperties.class);
        sortStore = new ListStore<TableViewSortField>(sortProps.uuid());
        ColumnConfig<TableViewSortField, FieldDef> fname = new ColumnConfig<TableViewSortField, FieldDef>(
                sortProps.fieldDef(), 150, i18n.sortTabFieldNameTitle()); //$NON-NLS-1$
        ColumnConfig<TableViewSortField, SortOrder> order = new ColumnConfig<TableViewSortField, SortOrder>(
                sortProps.sortOrder(), 300, i18n.sortTabSortTitle()); //$NON-NLS-1$
        
        order.setCell(new SortOrderCell());
        fname.setCell(new FieldDefNameCell());

        List<ColumnConfig<TableViewSortField, ?>> colList = new ArrayList<ColumnConfig<TableViewSortField, ?>>();
        colList.add(fname);
        colList.add(order);
        ColumnModel<TableViewSortField> cm = new ColumnModel<TableViewSortField>(colList);

        sortFieldsGrid = new ResizeableGrid<TableViewSortField>(sortStore, cm);
        sortFieldsGrid.getView().setAutoExpandColumn(fname);
        sortFieldsGrid.getView().setColumnLines(true);
        sortFieldsGrid.getView().setStripeRows(true);
        sortFieldsGrid.setWidth("450px");
        
        sortFieldsGrid.setBorders(false);
        sortFieldsGrid.setSelectionModel(sm);

        fieldDefListStore = new ListStore<FieldDef>(fieldProps.uuid());
        fieldslist = new ListView<FieldDef, FieldDef>(fieldDefListStore, new IdentityValueProvider<FieldDef>());
        fieldslist.setWidth("75%"); //$NON-NLS-1$
        fieldslist.setHeight(300);
        fieldslist.setCell(new FieldDefNameCell());

        initWidget(uiBinder.createAndBindUi(this));
        
        addSortFieldButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                List<FieldDef> selectedFields = fieldslist.getSelectionModel().getSelectedItems();
                for (FieldDef def : selectedFields) {
                    if(sortStore.hasRecord(new TableViewSortField(def))){
                        continue;
                    }
                    TableViewSortField srtFld = new TableViewSortField();
                    srtFld.setFieldDef(def);
                    srtFld.setSortOrder(SortOrder.ASC);
                    sortStore.add(srtFld);
                    fieldDefListStore.remove(def);
                }
            }
        });

        deleteSortFieldButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                List<TableViewSortField> delFlds = sortFieldsGrid.getSelectionModel().getSelectedItems();
                if (delFlds.size() > 0) {
                    List<TableViewSortField> srtFlds = new ArrayList<TableViewSortField>();
                    srtFlds.addAll(sortFieldsGrid.getStore().getAll());
                    srtFlds.removeAll(delFlds);
                    sortFieldsGrid.getStore().replaceAll(srtFlds);
                    populateListBox();
                }
            }
        });
    }

    @Override
    public void updateViewFromModel() {
        populateListBox();
        tableViewSortFields = getTableViewSettings().getSortFieldDefs(getDataModelDef());
        sortStore.clear();
        sortStore.addAll(tableViewSortFields);

    }

    @Override
    public void updateModelWithView() {
        List<TableViewSortField> sortFields = new ArrayList<TableViewSortField>();
        sortStore.commitChanges();
        List<TableViewSortField> sortStoreFields = sortStore.getAll();
        for (int i = 0; i < sortStoreFields.size(); i++) {
            TableViewSortField uisFld = sortStoreFields.get(i);
            uisFld.setListPosition(i);
            sortFields.add(uisFld);
        }

        getTableViewSettings().setSortFields(sortFields);

    }

    private void populateListBox() {
        List<FieldDef> sortFields = new ArrayList<FieldDef>();
        fieldDefListStore.clear();
        // get the current selected sort fields
        for (TableViewSortField field : sortStore.getAll()) {
            sortFields.add(field.getFieldDef(getDataModelDef()));
        }

        // exclude the current sort fields from the listbox
        List<FieldDef> t = new ArrayList<FieldDef>();
        t.addAll(getAllColumns());
        java.util.Collections.sort(t, TableSettingsPresenter.COMPARE_BY_NAME);
        for (FieldDef fld : t) {
            if (!sortFields.contains(fld)) {
                fieldDefListStore.add(fld);
            }
        }
    }

}

package csi.client.gwt.viz.table.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.*;
import com.sencha.gxt.widget.core.client.form.DualListFieldTwo;
import csi.client.gwt.WebMain;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.viz.shared.CsiDualListFieldTwo;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.table.VisibleTableField;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ColumnsTab extends TableSettingsComposite {

    private static final Comparator<? super FieldDef> COMPARE_BY_ORDINAL = new Comparator<FieldDef>() {
        @Override
        public int compare(FieldDef o1, FieldDef o2) {
            return Integer.valueOf(o1.getOrdinal()).compareTo(o2.getOrdinal());
        }
    };

    private static ColumnsTabUiBinder uiBinder = GWT
            .create(ColumnsTabUiBinder.class);

    @UiField(provided = true)
    CsiDualListFieldTwo<FieldDef, FieldDef> dualListField;
    @UiField
    Button sortAlphaButton;
    @UiField
    Button sortNaturalButton;

    private final int COLUMN_WIDTH = 310;

    private ListStore<FieldDef> availColumns;
    private ListStore<FieldDef> selectedColumns;


    /**
     * Constructor,
     */
    public ColumnsTab() {
        FieldProperties props = GWT.create(FieldProperties.class);

        availColumns = new ListStore<FieldDef>(props.uuid());
        selectedColumns = new ListStore<FieldDef>(props.uuid());


        //configs te dual list field and sets its size properties
        configureDualListField();

        initWidget(uiBinder.createAndBindUi(this));

        sortAlphaButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                availColumns.clearSortInfo();
                Store.StoreSortInfo<FieldDef> sortInfo = new Store.StoreSortInfo<FieldDef>(FieldDefUtils.SORT_ALPHABETIC, SortDir.ASC);
                availColumns.addSortInfo(sortInfo);

            }
        });
        sortNaturalButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                availColumns.clearSortInfo();
                Store.StoreSortInfo<FieldDef> sortInfo = new Store.StoreSortInfo<FieldDef>(FieldDefUtils.SORT_ORDINAL, SortDir.ASC);
                availColumns.addSortInfo(sortInfo);
            }
        });

    }

    private List<FieldDef> getAllAvailableColumns(){
        List<FieldDef> fieldDefs = new ArrayList<FieldDef>();
        fieldDefs.addAll(availColumns.getAll());
        return fieldDefs;
    }


    private void configureDualListField(){
        dualListField = new CsiDualListFieldTwo<FieldDef, FieldDef>(availColumns, selectedColumns,
                new IdentityValueProvider<FieldDef>(),
                new FieldDefNameCell());

        //sort the store to get in proper order for this.
        Store.StoreSortInfo<FieldDef> sortInfo = new Store.StoreSortInfo<FieldDef>(
                WebMain.getClientStartupInfo().isSortAlphabetically() ? FieldDefUtils.SORT_ALPHABETIC : FieldDefUtils.SORT_ORDINAL, SortDir.ASC);
        availColumns.addSortInfo(sortInfo);
//        dualListField.getFromView().getSelectionModel().addSelectionHandler()
        dualListField.setEnableDnd(true);
        dualListField.setMode(DualListFieldTwo.Mode.INSERT);
        dualListField.setWidth("100%");
        dualListField.getFromView().setWidth(COLUMN_WIDTH);
        dualListField.getToView().setWidth(COLUMN_WIDTH);
        dualListField.setHeight("200px");
    }


    /**
     * Widget updates its state from the model.
     */
    @Override
    public void updateViewFromModel() {
        availColumns.addAll(getTableViewSettings().getVisibleFieldDefs(getDataModelDef()));

        List<FieldDef> allColumns = getAllColumns();
        List<FieldDef> visibleColumns = getTableViewSettings()
                .getVisibleFieldDefs(getDataModelDef());

        List<FieldDef> fieldDefs = new ArrayList<FieldDef>();
        fieldDefs.addAll(allColumns);

        fieldDefs.removeAll(visibleColumns);

        availColumns.clear();
        availColumns.addAll(fieldDefs);
        selectedColumns.clear();
        selectedColumns.addAll(visibleColumns);
    }

    /**
     * Widget pushes its state to the model.
     */
    @Override
    public void updateModelWithView() {
        List<VisibleTableField> visibleTableFields = new ArrayList<VisibleTableField>();
        for (int i = 0; i < selectedColumns.size(); i++) {
            VisibleTableField tableField = new VisibleTableField();
            tableField.setFieldDef(selectedColumns.get(i));
            tableField.setListPosition(i);
            visibleTableFields.add(tableField);
        }

        getTableViewSettings().setVisibleFields(visibleTableFields);

    }

    public int getSelectedColumnCount() {
        return selectedColumns.size();
    }


    /**
     *
     */
    interface ColumnsTabUiBinder extends UiBinder<Widget, ColumnsTab> {}

    /**
     * This is used to get uuid...
     */
    interface FieldProperties extends PropertyAccess<FieldDef> {

        ModelKeyProvider<FieldDef> uuid();

        LabelProvider<FieldDef> fieldName();
    }
}

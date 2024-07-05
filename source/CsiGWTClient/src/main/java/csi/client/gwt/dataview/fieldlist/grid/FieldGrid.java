package csi.client.gwt.dataview.fieldlist.grid;

import java.util.*;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sencha.gxt.cell.core.client.NumberCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.dataview.fieldlist.DeleteCommand;
import csi.client.gwt.dataview.fieldlist.FieldCommand;
import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.resources.SortResource;
import csi.client.gwt.util.GenericCallback;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.cells.CsiCompositeCell;
import csi.client.gwt.widget.cells.context_menu.DataTypeCell;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.form.DownButton;
import csi.client.gwt.widget.ui.form.UpButton;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;


/**
 * @author Centrifuge Systems, Inc.
 *         This grid appears in the FieldListDialog
 */
public class FieldGrid extends Composite {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final int DATA_TYPE_WIDTH = 110;
    private static final int FIELD_TYPE_WIDTH = 90;
    private static final int EDIT_DELETE_WIDTH = 115;
    private static final int ORDINAL_WIDTH = 40;
    private static final int NAME_WIDTH = 220;

    private final FieldCommand editFieldCommand;
    private final FieldCommand deleteFieldCommand;
    private final FieldList fieldList;
    private final ColumnModel<FieldGridModel> columnModel;
    private final ListStore<FieldGridModel> listStore;
    private final Grid<FieldGridModel> grid;
    private final Validator duplicateNameInGridValidator;
    private Integer _fieldCount = 0;

    interface FieldProperties extends PropertyAccess<FieldGridModel> {
        ModelKeyProvider<FieldGridModel> uuid();

        ValueProvider<FieldGridModel, Integer> ordinal();

        ValueProvider<FieldGridModel, String> name();

        ValueProvider<FieldGridModel, CsiDataType> dataType();

        ValueProvider<FieldGridModel, String> fieldType();

        ValueProvider<FieldGridModel, Boolean> deletable();
    }

    private static final FieldProperties properties = GWT.create(FieldProperties.class);

    public FieldGrid(FieldList fieldListIn) {
        this.fieldList = fieldListIn;
        this.editFieldCommand = new EditFieldCommand(fieldList);
        this.deleteFieldCommand = new DeleteCommand(fieldList);
        listStore = createListStore(fieldList.getFieldDefs());
        columnModel = createColumnModel();
        grid = new Grid<FieldGridModel>(listStore, columnModel);
        //TODO: Remove inline editing temporarily
        //createEditor();
        grid.getSelectionModel().setSelectionMode(com.sencha.gxt.core.client.Style.SelectionMode.SINGLE);
        wrapGridInPanel();
        duplicateNameInGridValidator = createDuplicateNameInGridValidator();
    }

    public void addOrUpdateRow(FieldDef fieldDef) {

        FieldGridModel myRow = listStore.findModelWithKey(fieldDef.getUuid());

        if (null != myRow) {
            listStore.update(fromFieldDef(fieldDef));
        } else {
            fieldDef.setOrdinal(_fieldCount);
            myRow = fromFieldDef(fieldDef);
            listStore.add(_fieldCount++, myRow);
        }
        updateOrdinals();
        grid.getView().ensureVisible(myRow);
    }

    public void selectAndEnsureRowVisible(String keyIn) {

        FieldGridModel myRow = listStore.findModelWithKey(keyIn);

        if (null != myRow) {

            grid.getSelectionModel().select(myRow, false);
            grid.getView().ensureVisible(myRow);
        }
    }

    public void deleteFieldDef(String uuidIn) {
        FieldGridModel model = listStore.findModelWithKey(uuidIn);
        listStore.remove(model);
    }

    public void refresh(List<FieldDef> listIn) {

        if (null != listStore) {

            listStore.clear();
            refreshListStore(listStore, listIn);
            listStore.commitChanges();
        }
    }

    public String getSelection() {

        FieldGridModel myRow = grid.getSelectionModel().getSelectedItem();

        return (null != myRow) ? myRow.getUuid() : null;
    }

    private ClickHandler handleUpClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            FieldGridModel myRow = grid.getSelectionModel().getSelectedItem();

            if (null != myRow) {

                int myIndex = grid.getStore().indexOf(myRow) - 1;

                if (0 <= myIndex) {

                    grid.getStore().remove(myRow);
                    grid.getStore().add(myIndex, myRow);
                    grid.getSelectionModel().select(myIndex, true);
                }
                updateOrdinals();
                grid.getView().ensureVisible(myRow);
            }
        }
    };

    private ClickHandler handleDownClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            FieldGridModel myRow = grid.getSelectionModel().getSelectedItem();

            if (null != myRow) {

                int myIndex = grid.getStore().indexOf(myRow) + 1;

                if (grid.getStore().size() > myIndex) {

                    grid.getStore().remove(myRow);
                    grid.getStore().add(myIndex, myRow);
                    grid.getSelectionModel().select(myIndex, true);
                }
                updateOrdinals();
                grid.getView().ensureVisible(myRow);
            }
        }
    };

    private void updateOrdinals() {

        ListStore myStore = grid.getStore();

        for (int myIndex = 0; myStore.size() > myIndex; myIndex++) {

            FieldGridModel myRow = (FieldGridModel)myStore.get(myIndex);

            myRow.setOrdinal(myIndex);
        }
        grid.getView().refresh(false);
    }

    private Validator createDuplicateNameInGridValidator() {
        return new Validator() {
            @Override
            public boolean isValid() {
                Set<String> names = new HashSet<String>();
                for(int i=0; i < listStore.size(); i++){
                    if(!names.add(grid.getView().getCell(i,0).getInnerText().trim().toLowerCase())){
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private FieldGridModel fromFieldDef(FieldDef def) {
        final FieldGridModel model = new FieldGridModel();
        model.setUuid(def.getUuid());
        model.setOrdinal(def.getOrdinal());
        model.setName(def.getFieldName() != null ? def.getFieldName().trim() : ""); //$NON-NLS-1$
        model.setDataType(def.getValueType());
        model.setFieldType(def.getFieldType().getLabel());
        model.setDeletable(isDeletable(def));

        return model;
    }

    private boolean isDeletable(FieldDef fieldIn) {

        return isDeletable(fieldIn.getFieldType(), fieldIn.getUuid());
    }

    private boolean isDeletable(FieldType typeIn, String uuidIn) {

        boolean myOkFlag = false;

        if (FieldGridModel.isFieldTypeDeletable(typeIn)) {

            if (!fieldList.inUse(uuidIn)) {

                myOkFlag = true;
            }
        }

        return myOkFlag;
    }

    private void wrapGridInPanel() {
        FullSizeLayoutPanel myContainer = new FullSizeLayoutPanel();
        FullSizeLayoutPanel mySortingPanel = new FullSizeLayoutPanel();
        ContentPanel myGridPanel = new ContentPanel();
        ContentPanel myWidgetPanel = new ContentPanel();
        Button myHeader = new Button("Reorder");
        UpButton myUpArrow = new UpButton(handleUpClick);
        DownButton myDownArrow = new DownButton(handleDownClick);

        myHeader.setPixelSize(60, 20);
        myHeader.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        myHeader.getElement().getStyle().setPaddingTop(1, Style.Unit.PX);
        myHeader.getElement().getStyle().setPaddingLeft(0, Style.Unit.PX);

        myGridPanel.setPixelSize(595, 355);
        myGridPanel.add(grid);
        myGridPanel.setHeaderVisible(false);

        mySortingPanel.add(myHeader);
        mySortingPanel.setWidgetTopHeight(myHeader, 0, Style.Unit.PX, 20, Style.Unit.PX);
        mySortingPanel.setWidgetLeftRight(myHeader, 0, Style.Unit.PX, 0, Style.Unit.PX);

        mySortingPanel.add(myUpArrow);
        mySortingPanel.setWidgetTopHeight(myUpArrow, 120, Style.Unit.PX, 64, Style.Unit.PX);
        mySortingPanel.setWidgetLeftRight(myUpArrow, 9, Style.Unit.PX, 9, Style.Unit.PX);

        mySortingPanel.add(myDownArrow);
        mySortingPanel.setWidgetBottomHeight(myDownArrow, 100, Style.Unit.PX, 64, Style.Unit.PX);
        mySortingPanel.setWidgetLeftRight(myDownArrow, 9, Style.Unit.PX, 9, Style.Unit.PX);

        myWidgetPanel.add(mySortingPanel);
        myWidgetPanel.setPixelSize(60, 355);
        myWidgetPanel.setHeaderVisible(false);

        myContainer.setPixelSize(595, 355);

        myContainer.add(myGridPanel);
        myContainer.setWidgetTopBottom(myGridPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        myContainer.setWidgetLeftRight(myGridPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);

//        myContainer.add(myWidgetPanel);
//        myContainer.setWidgetTopBottom(myWidgetPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
//        myContainer.setWidgetLeftRight(myWidgetPanel, 595, Style.Unit.PX, 0, Style.Unit.PX);

        initWidget(myContainer);
    }

    private ListStore<FieldGridModel> createListStore(Collection<FieldDef> fieldDefsIn) {
        ListStore<FieldGridModel> myListStore = new ListStore<FieldGridModel>(properties.uuid());

        refreshListStore(myListStore, fieldDefsIn);
        return myListStore;
    }

    private void refreshListStore(ListStore<FieldGridModel> listStoreIn, Collection<FieldDef> fieldDefsIn) {

        Map<Integer, FieldDef> myMap = new TreeMap<Integer, FieldDef>();
        List<FieldDef> myCollisions = new ArrayList<FieldDef>();

        listStoreIn.clear();
        for (FieldDef myField : fieldDefsIn) {

            if (myMap.containsKey(myField.getOrdinal())) {

                myCollisions.add(myField);

            } else {

                myMap.put(myField.getOrdinal(), myField);
            }
        }
        _fieldCount = 0;
        for (FieldDef myField : myMap.values()) {

            myField.setOrdinal(_fieldCount++);
            listStoreIn.add(fromFieldDef(myField));
        }
        for (FieldDef myField : myCollisions) {

            myField.setOrdinal(_fieldCount++);
            listStoreIn.add(fromFieldDef(myField));
        }
    }

    public List<String> sychonizeFieldOrder() {

        Map<String, FieldDef> myMapIn = fieldList.getFieldMap();
        Map<Integer, String> myMapOut = new TreeMap<Integer, String>();

        for (int i = 0; listStore.size() > i; i++ ) {

            FieldGridModel myEntry = listStore.get(i);

            if (null != myEntry) {

                FieldDef myField = myMapIn.get(myEntry.getUuid());

                if (null != myField) {

                    Integer myOrdinal = myEntry.getOrdinal() - 1;

                    myField.setOrdinal(myOrdinal);
                    myMapOut.put(myOrdinal, myField.getUuid());
                }
            }
        }
        return new ArrayList<String>(myMapOut.values());
    }

    private ColumnModel<FieldGridModel> createColumnModel() {
        List<ColumnConfig<FieldGridModel, ?>> columnConfig = new ArrayList<ColumnConfig<FieldGridModel, ?>>();
        columnConfig.add(createOrdinalColumn());
        columnConfig.add(createNameColumn());
        columnConfig.add(createDataTypeColumn());
        columnConfig.add(createFieldTypeColumn());
        columnConfig.add(createEditDeleteColumn());
        return new ColumnModel<FieldGridModel>(columnConfig);
    }

    private ColumnConfig<FieldGridModel, Integer> createOrdinalColumn() {
        ColumnConfig<FieldGridModel, Integer> myConfig
                = new ColumnConfig<FieldGridModel, Integer>(properties.ordinal(), ORDINAL_WIDTH, "Col.");
        myConfig.setCell(new NumberCell<>());
        myConfig.setSortable(false);
        return myConfig;
    }

    private ColumnConfig<FieldGridModel, String> createNameColumn() {
        ColumnConfig<FieldGridModel, String> myConfig
                = new ColumnConfig<FieldGridModel, String>(properties.name(),
                            NAME_WIDTH, i18n.fieldList_GridTitle_Name());
        myConfig.setCell(new CsiTitleCell());
        myConfig.setSortable(true);
        return myConfig;
    }

    private ColumnConfig<FieldGridModel, CsiDataType> createDataTypeColumn() {

        ColumnConfig<FieldGridModel, CsiDataType> myConfig
                                            = new ColumnConfig<FieldGridModel, CsiDataType>(properties.dataType(),
                                                        DATA_TYPE_WIDTH, i18n.fieldList_GridTitle_DataType());
        myConfig.setCell(new DataTypeCell(true, true));
        myConfig.setSortable(true);
        return myConfig;
    }

    private ColumnConfig<FieldGridModel, String> createFieldTypeColumn() {

        ColumnConfig<FieldGridModel, String> myConfig
                                            = new ColumnConfig<FieldGridModel, String>(properties.fieldType(),
                                                        FIELD_TYPE_WIDTH, i18n.fieldList_GridTitle_FieldType());
        myConfig.setSortable(true);
        return myConfig;
    }

    private ColumnConfig<FieldGridModel, Boolean> createEditDeleteColumn() {
        ColumnConfig<FieldGridModel, Boolean> config = new ColumnConfig<FieldGridModel, Boolean>(properties.deletable(), EDIT_DELETE_WIDTH, "Edit/Delete Field");

        List<HasCell<Boolean, ?>> cells = new ArrayList<HasCell<Boolean, ?>>();
        cells.add(new HasEditButtonCell(listStore, editFieldCommand));
        cells.add(new HasDeleteButtonCell(listStore, deleteFieldCommand));

        CsiCompositeCell<Boolean> compositeCell = new CsiCompositeCell<Boolean>(cells, 0, 2);
        config.setCell(compositeCell);
        config.setSortable(false);
        return config;
    }

}

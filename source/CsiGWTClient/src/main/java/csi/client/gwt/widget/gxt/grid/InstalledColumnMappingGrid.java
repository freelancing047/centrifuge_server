package csi.client.gwt.widget.gxt.grid;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.event.*;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;

import csi.client.gwt.dataview.linkup.SelectionChangeResponder;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.resources.ApplicationResources;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.cells.GridCellAssist;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.model.FieldDef;


/**
 * Created by centrifuge on 9/24/2018.
 */
public abstract class InstalledColumnMappingGrid implements GridCellAssist<FieldDef> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public class MyValueProvider<S> implements ValueProvider<S, String> {

        /**
         * Returns the property value of the given object.
         *
         * @param object the target object
         * @return the property value
         */
        public String getValue(S object) {

            return ""; //$NON-NLS-1$
        }

        /**
         * Sets the value of the given object
         *
         * @param object
         * @param value
         */
        public void setValue(S object, String value) {

        }

        /**
         * Returns the path that this ValueProvider makes available, from the object,
         * to the value.
         *
         * @return the path from the object to the value
         */
        public String getPath() {

            return ""; //$NON-NLS-1$
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected FixedSizeGrid<ColumnMappingDataItem> dataGrid;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    protected final int _helpIconWidth = 22;

    protected final String _txtIncludeCheckBoxHeader = _constants.linkupGridMapper_IncludeCheckBoxHeader();
    protected String _txtColumnHeader = _constants.linkupGridMapper_TemplateFieldHeader();
    protected String _txtFieldHeader = _constants.linkupGridMapper_DataViewFieldHeader();

    private GridInlineEditing<ColumnMappingDataItem> _gridEditor = null;

    protected ApplicationResources resources = GWT.create(ApplicationResources.class);
    private ColumnMappingDataItem _listStore = null;
    private SelectionChangeResponder _parent;
    private int _width;
    private int _height;
    private String _helpKey;
    private FieldDef _selectedField = null;
    private int _clickCount = 0;
    private int _activeRow = -1;
    private boolean _isBlocked = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstalledColumnMappingGrid(SelectionChangeResponder parentIn, int widthIn, int heightIn, String helpFileNameIn) {

        _parent = parentIn;
        _width = widthIn;
        _height = heightIn;
        _helpKey = helpFileNameIn;
    }

    public InstalledColumnMappingGrid(SelectionChangeResponder parentIn, int widthIn, int heightIn,
                                      String helpFileNameIn, String columnHeaderIn, String FieldHeaderIn) {

        _parent = parentIn;
        _width = widthIn;
        _height = heightIn;
        _helpKey = helpFileNameIn;
        if (null != columnHeaderIn) {

            _txtColumnHeader = columnHeaderIn;
        }
        if (null != FieldHeaderIn) {

            _txtFieldHeader = FieldHeaderIn;
        }
    }

    public ListStore<ColumnMappingDataItem> getListStore() {

        return (null != dataGrid) ? dataGrid.getStore() : null;
    }

    public void selectionChange(Object dataRowIn) {

        if (null != _parent) {
            _parent.selectionChange(dataRowIn);
        }
    }

    public void rowComplete(Object dataRowIn) {

        if (null != _parent) {
            _parent.rowComplete(dataRowIn);
        }
    }

    public void forceRedraw(Integer rowIn, Integer columnIn) {
        ColumnMappingDataItem myRowData = getRowData(rowIn);

        if (null != myRowData){
            getListStore().update(myRowData);
        }
    }

    public void forceRedraw(Integer rowIn) {
        ColumnMappingDataItem myRowData = getRowData(rowIn);

        if (null != myRowData){
            getListStore().update(myRowData);
        }
    }

    public String getStyle(Integer rowIn, Integer columnIn) {
        return null;
    }

    public void forceUpdate(FieldDef valueIn, Integer rowIn, Integer columnIn){
        ColumnMappingDataItem myRowData = getRowData(rowIn);

        if (null != myRowData){
            myRowData.setColumnData(columnIn, valueIn);
        }
    }

    public void reportTextChange(String valueIn, Integer rowIn, Integer columnIn) {
        if ((null == valueIn) || (0 == valueIn.length())) {
            forceUpdate(null, rowIn, columnIn);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void createGrid() {


    }

    protected ColumnMappingDataItem getRowData(Integer rowIn) {

        int myStoreSize = (null != dataGrid) ? dataGrid.getStore().size() : 0;

        if ((null != rowIn) && (0 <= rowIn) && (myStoreSize > rowIn)) {

            return getListStore().get(rowIn);
        }
        return null;
    }

    protected FieldDefNameCell createFieldDefComboBoxCell(ListStore listStoreIn, Collection<FieldDef> fieldListIn) {

        FieldDefComboBox myFieldComboBox = new FieldDefComboBox();
        refreshFieldDefComboBox(myFieldComboBox, fieldListIn);
        FieldDefNameCell myCell = new FieldDefNameCell(listStoreIn, myFieldComboBox,
                                                        _constants.linkupGridMapperFieldInstructions());
        myCell.setNullText("Provide null values as data");
        return myCell;
    }

    protected void createGrid(GridComponentManager<ColumnMappingDataItem> managerIn,
                              GridView<ColumnMappingDataItem> gridViewIn) {

        if ((null != managerIn) && (null != gridViewIn)) {

            dataGrid = new FixedSizeGrid<ColumnMappingDataItem>(managerIn.getStore(),
                        new ColumnModel<ColumnMappingDataItem>(managerIn.getColumnConfigList()), gridViewIn);
            dataGrid.setWidth(_width);
            dataGrid.setHeight(_height);
            getListStore().setAutoCommit(true);
            dataGrid.getView().setColumnLines(true);
            dataGrid.getView().setStripeRows(true);
            dataGrid.setBorders(true);
            dataGrid.setView(gridViewIn);
        }
    }

    protected void refreshFieldDefComboBox(FieldDefComboBox fieldDefComboBoxIn, Collection<FieldDef> fieldListIn) {

        fieldDefComboBoxIn.getStore().clear();
        if ((null != fieldListIn) && (0 < fieldListIn.size())) {

            fieldDefComboBoxIn.getStore().addAll(fieldListIn);
        }
    }

    protected void applyComboBoxSelection(FieldDefNameCell fieldDefCellIn) {

        if ((null != fieldDefCellIn) && (0 <= _activeRow)) {

            FieldDefComboBox myComboBox = fieldDefCellIn.getComboBox();

            if (1 == _clickCount) {

                if (myComboBox.isExpanded()) {

                    _clickCount = 2;
                }
            }

            if (2 == _clickCount) {

                if (!myComboBox.isExpanded()) {

                    ListView<FieldDef, ?> myListView = myComboBox.getListView();

                    if (null != myListView) {

                        try {

                            FieldDef mySelection = myListView.getSelectionModel().getSelectedItem();

                            if (null == mySelection) {

                                myComboBox.setValue(mySelection);
                            }

                        } catch (Exception myException) {

                            Display.error(myException.getMessage());
                        }
                    }
                    _clickCount = 0;
                    _gridEditor.completeEditing();
                }
            }
        }
    }

    private GridInlineEditing<ColumnMappingDataItem> createEditor() {

        GridInlineEditing<ColumnMappingDataItem> myEditor = null;

        if (null != dataGrid) {

            myEditor = new GridInlineEditing<ColumnMappingDataItem>(dataGrid);

            // Add handler to identify row in edit mode
            myEditor.addStartEditHandler(new StartEditEvent.StartEditHandler<ColumnMappingDataItem>() {
                @Override
                public void onStartEdit(StartEditEvent<ColumnMappingDataItem> eventIn) {

                    _activeRow = _gridEditor.getActiveCell().getRow();
                    _clickCount = 0;
                    _isBlocked = true;
                    _selectedField = null;
                }
            });
            myEditor.addCancelEditHandler(new CancelEditEvent.CancelEditHandler<ColumnMappingDataItem>() {
                @Override
                public void onCancelEdit(CancelEditEvent<ColumnMappingDataItem> tCancelEditEvent) {

                    _activeRow = -1;
                    _clickCount = 0;
                    Scroll scroll = dataGrid.getView().getScroller().getScroll();
                    dataGrid.getView().refresh(false);
                    dataGrid.getView().getScroller().setScrollTop(scroll.getScrollTop());
                    _isBlocked = false;
                }
            });
            // Add handler to remove row from edit mode
            myEditor.addCompleteEditHandler(new CompleteEditEvent.CompleteEditHandler<ColumnMappingDataItem>() {
                @Override
                public void onCompleteEdit(CompleteEditEvent<ColumnMappingDataItem> eventIn) {

                    _activeRow = -1;
                    _clickCount = 0;
                    Scroll scroll = dataGrid.getView().getScroller().getScroll();
                    dataGrid.getView().refresh(false);
                    dataGrid.getView().getScroller().setScrollTop(scroll.getScrollTop());
                    _isBlocked = false;
                }
            });
            // Add handler to block edit when row has not been selected.
            myEditor.addBeforeStartEditHandler(new BeforeStartEditEvent.BeforeStartEditHandler<ColumnMappingDataItem>() {
                @Override
                public void onBeforeStartEdit(BeforeStartEditEvent<ColumnMappingDataItem> eventIn) {

                    ListStore<ColumnMappingDataItem> myListStore = getListStore();

                    if (null != myListStore) {

                        Grid.GridCell myCell = eventIn.getEditCell();

                        if (null != myCell) {

                            int myRow = myCell.getRow();

                            if ((0 <= myRow) && (myListStore.size() > myRow)) {

                                ColumnMappingDataItem myRowData = getListStore().get(myRow);

                                if (!myRowData.isSelected()) {

                                    eventIn.setCancelled(true);
                                }
                            }
                        }
                    }
                    _clickCount = 0;
                    _selectedField = null;
                }
            });
        }
        return myEditor;
    }

    protected void addFieldEditor(ColumnConfig<ColumnMappingDataItem, FieldDef> columnIn) {

        FieldDefNameCell myCell = (FieldDefNameCell)columnIn.getCell();

        FieldDefComboBox myComboBox = myCell.getComboBox();

        if (null != myComboBox) {

            myComboBox.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    _clickCount = 1;
                }
            }, ClickEvent.getType() );

            if (null == _gridEditor) {

                _gridEditor = createEditor();
            }
            _gridEditor.addEditor(columnIn, myComboBox);
        }
    }
}

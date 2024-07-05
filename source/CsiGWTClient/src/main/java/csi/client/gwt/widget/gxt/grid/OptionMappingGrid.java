package csi.client.gwt.widget.gxt.grid;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
import com.sencha.gxt.widget.core.client.event.CancelEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.StartEditEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.OptionControl;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.dataview.linkup.SelectionChangeResponder;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.cells.readonly.OptionCell;
import csi.client.gwt.widget.combo_boxes.ImportOptionComboBox;
import csi.client.gwt.widget.list_boxes.CsiListBox;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.list_boxes.CsiStringStoreItem;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.ConflictResolution;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.TrippleDisplay;
import csi.server.common.model.FieldDef;


/**
 * Created by centrifuge on 9/24/2018.
 */
public class OptionMappingGrid<S extends TrippleDisplay, T extends OptionControl<S>> extends AbstractInputWidget {


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

    protected FixedSizeGrid<T> dataGrid;
    protected ImportOptionComboBox comboBox;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    protected String _txtItemHeader = "Item";
    protected String _txtOptionHeader = "Option";
    protected int[][] _optionLimits = null;
    protected int _width;
    protected int _height;

    private GridInlineEditing<T> _gridEditor = null;
    private SelectionChangeResponder _parent;
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

    public OptionMappingGrid(SelectionChangeResponder parentIn, int widthIn, int heightIn, String helpFileNameIn) {

        _parent = parentIn;
        _width = widthIn - 2;
        _height = heightIn - 30;
        _helpKey = helpFileNameIn;
    }

    public OptionMappingGrid(SelectionChangeResponder parentIn, int widthIn, int heightIn,
                             String helpFileNameIn, String itemHeaderIn, String optionHeaderIn) {

        _parent = parentIn;
        _width = widthIn - 2;
        _height = heightIn - 30;
        _helpKey = helpFileNameIn;
        if (null != itemHeaderIn) {

            _txtItemHeader = itemHeaderIn;
        }
        if (null != optionHeaderIn) {

            _txtOptionHeader = optionHeaderIn;
        }
    }

    public ListStore<T> getListStore() {

        return (null != dataGrid) ? dataGrid.getStore() : null;
    }

    public void selectionChange(Object dataRowIn) {

        if (null != _parent) {
            _parent.selectionChange(dataRowIn);
        }
    }

    public String getStyle(Integer rowIn, Integer columnIn) {
        return null;
    }
    @Override
    public String getText() throws CentrifugeException {
        return null;
    }

    @Override
    public void resetValue() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean atReset() {
        return false;
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public int getRequiredHeight() {
        return 0;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void layoutDisplay() {

    }

    protected void createGrid() {


    }

    protected T getRowData(Integer rowIn) {

        int myStoreSize = (null != dataGrid) ? dataGrid.getStore().size() : 0;

        if ((null != rowIn) && (0 <= rowIn) && (myStoreSize > rowIn)) {

            return getListStore().get(rowIn);
        }
        return null;
    }

    protected void createGrid(GridComponentManager<T> managerIn,
                              GridView<T> gridViewIn) {

        if ((null != managerIn) && (null != gridViewIn)) {

            dataGrid = new FixedSizeGrid<T>(managerIn.getStore(),
                    new ColumnModel<T>(managerIn.getColumnConfigList()), gridViewIn);
            dataGrid.setWidth(_width);
            dataGrid.setHeight(_height);
            getListStore().setAutoCommit(true);
            dataGrid.getView().setColumnLines(true);
            dataGrid.getView().setStripeRows(true);
            dataGrid.setBorders(true);
            dataGrid.setView(gridViewIn);
        }
    }

    protected void createGrid(GridComponentManager<T> managerIn) {

        if (null != managerIn) {

            dataGrid = new FixedSizeGrid<T>(managerIn.getStore(), new ColumnModel<T>(managerIn.getColumnConfigList()));
            dataGrid.setWidth(_width);
            dataGrid.setHeight(_height);
            getListStore().setAutoCommit(true);
            dataGrid.getView().setColumnLines(true);
            dataGrid.getView().setStripeRows(true);
            dataGrid.setBorders(true);
        }
    }

    protected void addOptionEditor(ColumnConfig<T, S> columnIn) {

        final OptionCell myCell = (OptionCell)columnIn.getCell();

        comboBox = myCell.getComboBox();

        if (null != comboBox) {

            if (null == _gridEditor) {

                _gridEditor = createEditor();
            }
            _gridEditor.addEditor(columnIn, comboBox);
        }
    }

    private ClickHandler myNameHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            Display.success("Hello");
        }
    };

    private GridInlineEditing<T> createEditor() {

        GridInlineEditing<T> myEditor = null;

        if (null != dataGrid) {

            myEditor = new GridInlineEditing<T>(dataGrid);

            // Add handler to identify row in edit mode
            myEditor.addStartEditHandler(new StartEditEvent.StartEditHandler<T>() {
                @Override
                public void onStartEdit(StartEditEvent<T> eventIn) {

                    _activeRow = _gridEditor.getActiveCell().getRow();
                    _clickCount = 0;
                    _isBlocked = true;
                    _selectedField = null;
                    loadComboBox();
                }
            });
            myEditor.addCancelEditHandler(new CancelEditEvent.CancelEditHandler<T>() {
                @Override
                public void onCancelEdit(CancelEditEvent<T> tCancelEditEvent) {

                    _activeRow = -1;
                    _clickCount = 0;
                    Scroll scroll = dataGrid.getView().getScroller().getScroll();
                    dataGrid.getView().refresh(false);
                    dataGrid.getView().getScroller().setScrollTop(scroll.getScrollTop());
                    _isBlocked = false;
                }
            });
            // Add handler to remove row from edit mode
            myEditor.addCompleteEditHandler(new CompleteEditEvent.CompleteEditHandler<T>() {
                @Override
                public void onCompleteEdit(CompleteEditEvent<T> eventIn) {

                    _activeRow = -1;
                    _clickCount = 0;
                    Scroll scroll = dataGrid.getView().getScroller().getScroll();
                    dataGrid.getView().refresh(false);
                    dataGrid.getView().getScroller().setScrollTop(scroll.getScrollTop());
                    _isBlocked = false;
                }
            });
            // Add handler to block edit when row has not been selected, or there is no conflicts
            myEditor.addBeforeStartEditHandler(new BeforeStartEditEvent.BeforeStartEditHandler<T>() {
                @Override
                public void onBeforeStartEdit(BeforeStartEditEvent<T> eventIn) {

                    ListStore<T> myListStore = getListStore();

                    if (null != myListStore) {

                        Grid.GridCell myCell = eventIn.getEditCell();

                        if (null != myCell) {

                            int myRow = myCell.getRow();

                            if ((0 <= myRow) && (myListStore.size() > myRow)) {

                                T myRowData = getListStore().get(myRow);

                                if ((!myRowData.getSelected()) || (!myRowData.getConflicts())) {

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

    private void loadComboBox() {

        if ((null != _optionLimits) && (0 <= _activeRow) && (_optionLimits.length > _activeRow)) {

            int[] myLimits = _optionLimits[_activeRow];
            ListStore<ConflictResolution> myStore = comboBox.getStore();

            myStore.clear();
            if (null != myLimits) {

                for (int i = myLimits[0]; myLimits[1] > i; i++) {

                    myStore.add(ConflictResolution.values()[i]);
                }
            }
        }
    }
}

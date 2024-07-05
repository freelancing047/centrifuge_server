package csi.client.gwt.csiwizard.panels;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.etc.DataPairDisplay;
import csi.client.gwt.etc.IDataPairDisplay;
import csi.client.gwt.etc.MappingSupport;
import csi.client.gwt.events.MappingChangeEvent;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.buttons.MiniCyanButton;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.cells.readonly.DisplayListCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.gxt.grid.paging.GroupingView;
import csi.server.common.dto.SelectionListData.ExtendedInfo;
import csi.server.common.exception.CentrifugeException;


public class MappingPanel<S extends ExtendedInfo, T extends ExtendedInfo> extends AbstractWizardPanel {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    GridContainer leftContainer;
    GridContainer rightContainer;
    GridContainer mappingContainer;
    Button buttonAdd;
    Button buttonDelete;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private Grid<S> _leftGrid = null;
    private Grid<T> _rightGrid = null;
    private Grid<DataPairDisplay<S, T>> _mappingGrid;
    
    private GridSelectionModel<S> _leftSelectionModel = null;
    private GridSelectionModel<T> _rightSelectionModel = null;
    private GridSelectionModel<DataPairDisplay<S, T>> _mappingSelectionModel = null;
    
    private MappingSupport<S, T> _mappingSupport;
    
    private boolean _hasLeftGroup = false;
    private boolean _hasRightGroup = false;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SelectionChangedHandler<S> handleLeftSelectionChange
    = new SelectionChangedHandler<S>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<S> event) {
            buttonAdd.setEnabled(isSelectionOK());
        }
    };

    private SelectionChangedHandler<T> handleRightSelectionChange
    = new SelectionChangedHandler<T>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<T> event) {
            buttonAdd.setEnabled(isSelectionOK());
        }
    };

    private ClickHandler handleAddClick
    = new ClickHandler() {
        
        @Override
        public void onClick(ClickEvent event) {

            List<S> myLeftSelection = _leftSelectionModel.getSelectedItems();
            List<T> myRightSelection = _rightSelectionModel.getSelectedItems();

            if ((null != myLeftSelection) && (0 < myLeftSelection.size())
                    && (null != myRightSelection) && (0 < myRightSelection.size())) {
                
                for (S myLeftItem : myLeftSelection) {
                    
                    for (T myRightItem : myRightSelection) {
                        
                        DataPairDisplay<S, T> myItem = _mappingSupport.mapPair(myLeftItem, myRightItem);

                        if (null != myItem) {
                            
                            fireEvent(new MappingChangeEvent<S, T>(myItem, true));
                        }
                    }
                }
                _leftSelectionModel.deselectAll();
                _rightSelectionModel.deselectAll();

            } else {

                Dialog.showInfo("No Selection", "Cannot create mapping without selecting two columns.");
            }
        }
    };
    private ClickHandler handleDeleteClick
    = new ClickHandler() {
        
        @Override
        public void onClick(ClickEvent event) {
            
            List<DataPairDisplay<S, T>> mySelection = _mappingSelectionModel.getSelectedItems();

            if ((null != mySelection) && (0 < mySelection.size())) {

                for (DataPairDisplay<S, T> myItem : mySelection) {

                    _mappingSupport.unmapPair(myItem);

                    fireEvent(new MappingChangeEvent<S, T>(myItem, false));
                }
                buttonDelete.setEnabled(_mappingGrid.getStore().getAll().size() > 0);

            } else {

                Dialog.showInfo("Delete", "Please select mapping rows to deselect.");
            }
        }
    };

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public MappingPanel(CanBeShownParent parentDialogIn, String leftGroupHeaderIn, String leftValueHeaderIn, String rightGroupHeaderIn,
            String rightValueHeaderIn, boolean leftSingleIn, boolean rightSingleIn,
            boolean leftRequireAllIn, boolean rightRequireAllIn) throws CentrifugeException {

        super(parentDialogIn);
        
        _hasLeftGroup = (null != leftGroupHeaderIn);
        _hasRightGroup = (null != rightGroupHeaderIn);
        
        _leftGrid = initSourceGrid(leftGroupHeaderIn, leftValueHeaderIn, leftSingleIn);
        _rightGrid = initSourceGrid(rightGroupHeaderIn, rightValueHeaderIn, rightSingleIn);
        _mappingGrid = initMappingGrid(leftGroupHeaderIn, leftValueHeaderIn, rightGroupHeaderIn, rightValueHeaderIn);

        _leftSelectionModel = _leftGrid.getSelectionModel();
        _rightSelectionModel = _rightGrid.getSelectionModel();
        _mappingSelectionModel = _mappingGrid.getSelectionModel();

        _mappingSupport = new MappingSupport<S, T>(_leftGrid.getStore(), _rightGrid.getStore(), _mappingGrid.getStore(),
                                            leftSingleIn, rightSingleIn, leftRequireAllIn, rightRequireAllIn, false, false);

        buttonDelete.setEnabled(_mappingGrid.getStore().getAll().size() > 0);
    }

    @Override
    public String getText() throws CentrifugeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void grabFocus() {

        _leftGrid.focus();
    }

    @Override
    public void destroy() {

        suspendMonitoring();
        _mappingSupport = null;
        clear();
        removeFromParent();
    }

    @Override
    public void enableInput() {
        
        _leftGrid.setEnabled(true);
        _rightGrid.setEnabled(true);
        _mappingGrid.setEnabled(true);
    }

    @Override
    public boolean isOkToLeave() {
        
        boolean myOk = !isRequired();

        if (myOk) {

            S myLeftSelection = _leftSelectionModel.getSelectedItem();
            T myRightSelection = _rightSelectionModel.getSelectedItem();
            
            if ((null == myLeftSelection) && (null == myRightSelection)) {
                
                myOk = _mappingSupport.isReady();
                
            } else {
                
                myOk = false;
            }
        }
        return myOk;
    }

    @Override
    public void suspendMonitoring() {
        
    }
    
    @Override
    public void beginMonitoring() {
        
        fireEvent(new ValidityReportEvent(isOkToLeave()));
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

        leftContainer = new GridContainer();
        leftContainer.setGrid(_leftGrid);
        add(leftContainer);
        
        rightContainer = new GridContainer();
        rightContainer.setGrid(_rightGrid);
        add(rightContainer);
        
        mappingContainer = new GridContainer();
        mappingContainer.setGrid(_mappingGrid);
        add(mappingContainer);
        
        buttonAdd = new MiniCyanButton(Dialog.txtMapButton, handleAddClick);
        buttonAdd.setEnabled(false);
        add(buttonAdd);

        buttonDelete = new MiniRedButton(Dialog.txtUnmapButton, handleDeleteClick);
        buttonDelete.setEnabled(false);
        add(buttonDelete);
    }

    @Override
    protected void layoutDisplay() throws CentrifugeException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void wireInHandlers() {

        _leftSelectionModel.addSelectionChangedHandler(handleLeftSelectionChange);
        _rightSelectionModel.addSelectionChangedHandler(handleRightSelectionChange);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private <R extends ExtendedInfo> Grid<R> initSourceGrid(String groupHeaderIn, String valueHeaderIn, boolean singleSelectionIn) {
        
        ModelKeyProvider<R> myKeyProvider = new ExtendedInfoKeyProvider<R>();
        ValueProvider<R, String> myGroupProvider = (null != groupHeaderIn)
                                                    ? new ExtendedInfoGroupProvider<R>()
                                                    : null;
        ValueProvider<R, String> myValueProvider = new ExtendedInfoValueProvider<R>();
        GroupingView<R> myView = null;
        
        final GridComponentManager<R> myManager = WebMain.injector.getGridFactory().create(myKeyProvider);
        ListStore<R> gridStore = myManager.getStore();
        
        if (null != groupHeaderIn) {
            
            ColumnConfig<R, String> myGroup
            = myManager.create(myGroupProvider, 150, groupHeaderIn, false, true);
            myGroup.setCell(new GroupCell<R>(gridStore));
            
            myView = new GroupingView<R>();
            myView.setShowGroupedColumn(false);
            myView.setForceFit(true);
            myView.groupBy(myGroup);
        }

        ColumnConfig<R, String> myValue = myManager.create(myValueProvider, 150, valueHeaderIn, false, true);
        myValue.setCell(new DisplayListCell<R>(gridStore));

        List<ColumnConfig<R, ?>> myList = myManager.getColumnConfigList();
        ColumnModel<R> myModel = new ColumnModel<R>(myList);

        Grid<R> myGrid = new ResizeableGrid<R>(gridStore, myModel);

        if (null != myView) {
            
            myGrid.setView(myView);
        }

        GridHelper.setReorderableRowsDefaults(myGrid);

        if (singleSelectionIn) {
            
            myGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            
        } else {
            
            myGrid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
        }
        
        return myGrid;
    }

    @SuppressWarnings("unchecked")
    private Grid<DataPairDisplay<S, T>> initMappingGrid(String leftGroupHeaderIn, String leftValueHeaderIn,
            String rightGroupHeaderIn, String rightValueHeaderIn) {

        final GridComponentManager myManager
        = WebMain.injector.getGridFactory().create(new ExtendedPairKeyProvider());

        ListStore<DataPairDisplay<S, T>> myGridStore = myManager.getStore();

        if (_hasLeftGroup) {
            
            ColumnConfig<DataPairDisplay<S, T>, String> myGroupOne
            = myManager.create(new ExtendedPairGroupProviderOne<DataPairDisplay<S, T>, String>(),
                    70, leftGroupHeaderIn, false, true);
            myGroupOne.setCell(new ExtendedPairGroupOneCell<DataPairDisplay<S, T>>(myGridStore));
        }

        ColumnConfig<DataPairDisplay<S, T>, String> myItemOne
        = myManager.create(new ExtendedPairValueProviderOne<DataPairDisplay<S, T>, String>(),
                70, leftValueHeaderIn, false, true);
        myItemOne.setCell(new ExtendedPairItemOneCell<DataPairDisplay<S, T>>(myGridStore));

        if (_hasRightGroup) {
            
            ColumnConfig<DataPairDisplay<S, T>, String> myGroupTwo
            = myManager.create(new ExtendedPairGroupProviderTwo<DataPairDisplay<S, T>, String>(),
                    70, rightGroupHeaderIn, false, true);
            myGroupTwo.setCell(new ExtendedPairGroupTwoCell<DataPairDisplay<S, T>>(myGridStore));
        }

        ColumnConfig<DataPairDisplay<S, T>, String> myItemTwo
        = myManager.create(new ExtendedPairValueProviderTwo<DataPairDisplay<S, T>, String>(),
                70, rightValueHeaderIn, false, true);
        myItemTwo.setCell(new ExtendedPairItemTwoCell<DataPairDisplay<S, T>>(myGridStore));

        List<ColumnConfig<DataPairDisplay<S, T>, ?>> myColumns = myManager.getColumnConfigList();
        ColumnModel<DataPairDisplay<S, T>> myModel = new ColumnModel<DataPairDisplay<S, T>>(myColumns);

        Grid<DataPairDisplay<S, T>> myGrid = new ResizeableGrid<DataPairDisplay<S, T>>(myGridStore, myModel);
        GridHelper.setDraggableRowsDefaults(myGrid);
        myGrid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
        
        return myGrid;
    }
    
    private boolean isSelectionOK() {
        
        boolean myOk = false;

        List<S> myLeftSelection = _leftSelectionModel.getSelectedItems();
        List<T> myRightSelection = _rightSelectionModel.getSelectedItems();

        if ((null != myLeftSelection) && (0 < myLeftSelection.size())
                && (null != myRightSelection) && (0 < myRightSelection.size())) {

            if ((1 < myLeftSelection.size()) || (1 < myRightSelection.size())) {
                
                for (S myLeftItem : myLeftSelection) {
                    
                    for (T myRightItem : myRightSelection) {
                        
                        if (_mappingSupport.isOkToMap(myLeftItem, myRightItem)) {
                            
                            myOk = true;
                            break;
                        }
                    }
                }
            }
        }
        return myOk;
    }
}


////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                                     Support Classes                                    //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

class ExtendedInfoGroupProvider<T extends ExtendedInfo> implements ValueProvider<T, String> {

    @Override
    public String getValue(T objectIn) {
    
        return objectIn.getParentString();
    }
    
    // Unsupported operation
    @Override
    public void setValue(T objectIn, String valueIn) {
    }
    
    // Unsupported operation
    @Override
    public String getPath() {
        return null;
    }
}

class ExtendedInfoValueProvider<T extends ExtendedInfo> implements ValueProvider<T, String> {

    @Override
    public String getValue(T objectIn) {
    
        return objectIn.getDisplayString();
    }
    
    // Unsupported operation
    @Override
    public void setValue(T objectIn, String valueIn) {
    }
    
    // Unsupported operation
    @Override
    public String getPath() {
        return null;
    }
}

class ExtendedInfoKeyProvider<T extends ExtendedInfo> implements ModelKeyProvider<T> {

    @Override
    public String getKey(T objectIn) {
    
        return objectIn.getKey();
    }
}

class ExtendedPairKeyProvider implements ModelKeyProvider<IDataPairDisplay> {

    @Override
    public String getKey(IDataPairDisplay item) {

        return item.getKey();
    }
}

class ExtendedPairGroupProviderOne<R extends IDataPairDisplay, Q> implements ValueProvider<R, String> {

    @Override
    public String getValue(R objectIn) {
    
        return objectIn.getGroupValueOne();
    }
    
    // Unsupported operation
    @Override
    public void setValue(R objectIn, String valueIn) {
    }
    
    // Unsupported operation
    @Override
    public String getPath() {
        return null;
    }
}

class ExtendedPairValueProviderOne<R extends IDataPairDisplay, Q> implements ValueProvider<R, String> {

    @Override
    public String getValue(R objectIn) {
    
        return objectIn.getItemValueOne();
    }
    
    // Unsupported operation
    @Override
    public void setValue(R objectIn, String valueIn) {
    }
    
    // Unsupported operation
    @Override
    public String getPath() {
        return null;
    }
}

class ExtendedPairGroupProviderTwo<R extends IDataPairDisplay, Q> implements ValueProvider<R, String> {

    @Override
    public String getValue(R objectIn) {
    
        return objectIn.getGroupValueTwo();
    }
    
    // Unsupported operation
    @Override
    public void setValue(R objectIn, String valueIn) {
    }
    
    // Unsupported operation
    @Override
    public String getPath() {
        return null;
    }
}

class ExtendedPairValueProviderTwo<R extends IDataPairDisplay, Q> implements ValueProvider<R, String> {

    @Override
    public String getValue(R objectIn) {
    
        return objectIn.getItemValueTwo();
    }
    
    // Unsupported operation
    @Override
    public void setValue(R objectIn, String valueIn) {
    }
    
    // Unsupported operation
    @Override
    public String getPath() {
        return null;
    }
}

class GroupCell<R extends ExtendedInfo> extends DisplayListCell<R> {
    
    public GroupCell(ListStore<R> listStoreIn) {
        
        super(listStoreIn);
    }

    @Override
    protected void formatDisplayRequest(SafeHtmlBuilder htmlBuilderIn, R itemIn) {
        
        render(htmlBuilderIn, itemIn.getParentString(), null, null, itemIn.getDisplayMode());
    }
}

class ExtendedPairGroupOneCell<R extends IDataPairDisplay> extends DisplayListCell<R> {
    
    public ExtendedPairGroupOneCell(ListStore<R> listStoreIn) {
        
        super(listStoreIn);
    }

    @Override
    protected void formatDisplayRequest(SafeHtmlBuilder htmlBuilderIn, R itemIn) {
        
        render(htmlBuilderIn, itemIn.getGroupValueOne(), null, null, itemIn.getDisplayMode());
    }
}

class ExtendedPairItemOneCell<R extends IDataPairDisplay> extends DisplayListCell<R> {
    
    public ExtendedPairItemOneCell(ListStore<R> listStoreIn) {
        
        super(listStoreIn);
    }

    @Override
    protected void formatDisplayRequest(SafeHtmlBuilder htmlBuilderIn, R itemIn) {
        
        render(htmlBuilderIn, itemIn.getItemValueOne(), null, null, itemIn.getDisplayMode());
    }
}

class ExtendedPairGroupTwoCell<R extends IDataPairDisplay> extends DisplayListCell<R> {
    
    public ExtendedPairGroupTwoCell(ListStore<R> listStoreIn) {
        
        super(listStoreIn);
    }

    @Override
    protected void formatDisplayRequest(SafeHtmlBuilder htmlBuilderIn, R itemIn) {
        
        render(htmlBuilderIn, itemIn.getGroupValueTwo(), null, null, itemIn.getDisplayMode());
    }
}

class ExtendedPairItemTwoCell<R extends IDataPairDisplay> extends DisplayListCell<R> {
    
    public ExtendedPairItemTwoCell(ListStore<R> listStoreIn) {
        
        super(listStoreIn);
    }
    
    @Override
    protected void formatDisplayRequest(SafeHtmlBuilder htmlBuilderIn, R itemIn) {
        
        render(htmlBuilderIn, itemIn.getItemValueTwo(), null, null, itemIn.getDisplayMode());
    }
}

package csi.client.gwt.widget.gxt.grid;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.ImportNamingDialog;
import csi.client.gwt.csi_resource.OverWrite;
import csi.client.gwt.dataview.linkup.SelectionChangeResponder;
import csi.client.gwt.events.GridCellClick;
import csi.client.gwt.events.GridCellClickHandler;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.cells.ColorProvider;
import csi.client.gwt.widget.cells.CsiTextCell;
import csi.client.gwt.widget.cells.readonly.OptionCell;
import csi.client.gwt.widget.combo_boxes.ImportOptionComboBox;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.resource.ExportImportHelper;
import csi.server.common.dto.resource.ImportItem;
import csi.server.common.dto.resource.ImportRequest;
import csi.server.common.dto.resource.ResourceConflictInfo;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ConflictResolution;
import csi.client.gwt.csi_resource.ResourceConflictDisplay;
import csi.server.common.model.FieldDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by centrifuge on 4/22/2019.
 */
public class ImportOptionMapper
        extends OptionMappingGrid<ConflictResolution, ResourceConflictDisplay>
        implements ColorProvider {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public class CustomProvider implements ValueProvider<ResourceConflictDisplay, Boolean> {


        @Override
        public String getPath() {
            // TODO Auto-generated method stub
            return "selected";
        }

        @Override
        public Boolean getValue(ResourceConflictDisplay object) {

            return object.getSelected();
        }

        @Override
        public void setValue(ResourceConflictDisplay object, Boolean value) {
            object.setSelected(value);
        }
    }

    interface ParameterMapProperties extends PropertyAccess<ResourceConflictDisplay> {
        ModelKeyProvider<ResourceConflictDisplay> key();

        @Editor.Path("type")
        ValueProvider<ResourceConflictDisplay, String> type();
        @Editor.Path("name")
        ValueProvider<ResourceConflictDisplay, String> name();
        @Editor.Path("owner")
        ValueProvider<ResourceConflictDisplay, String> owner();
        @Editor.Path("conflicts")
        ValueProvider<ResourceConflictDisplay, Boolean> conflicts();
        @Editor.Path("resolution")
        ValueProvider<ResourceConflictDisplay, ConflictResolution> resolution();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    OptionCell<ConflictResolution, ResourceConflictDisplay> _optionCell;
    private NameChangeAccess _nameChanger = null;
    ColumnConfig<ResourceConflictDisplay, String> gridTypeColumn = null;
    ColumnConfig<ResourceConflictDisplay, String> gridNameColumn = null;
    ColumnConfig<ResourceConflictDisplay, String> gridOwnerColumn = null;
    ColumnConfig<ResourceConflictDisplay, ConflictResolution> gridOptionColumn = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private MainPresenter _mainPresenter = null;

    private List<ResourceConflictInfo> _itemList;
    private ListStore<ResourceConflictDisplay> _dataStore;
    private CsiCheckboxImportSelectionModel _selectionModel = null;
    private Map<String, List<OverWrite>> _overWrite;
    private List<OverWrite> _userOverWriteList;
    private boolean _iAmOwner;
    private int[] gridColumnidth = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private GridCellClickHandler clickHandler = new GridCellClickHandler() {
        @Override
        public void onGridCellClick(GridCellClick eventIn) {

            int myRowId = eventIn.getRow();
            ResourceConflictDisplay myRow = ((0 <= myRowId) && (_dataStore.size() > myRowId))
                    ? _dataStore.get(myRowId)
                    : null;

            if (null != myRow) {

                List<String> myLocalList = null;
                String myName = myRow.getName();
                String myOwner = myRow.getOwner();
                List<OverWrite> myOverWriteList = _iAmOwner
                                                    ? _userOverWriteList
                                                    : (((null != _overWrite) && (null != myOwner))
                                                            ? _overWrite.get(myOwner) : null);

                if (null != myOverWriteList) {

                    if ((0 < myOverWriteList.size()) && AclResourceType.DATAVIEW.getDescriptor().equals(myRow.getType())) {

                        myLocalList = myOverWriteList.get(0).getLocalList(myName);

                    } else if ((1 < myOverWriteList.size()) && AclResourceType.TEMPLATE.getDescriptor().equals(myRow.getType())) {

                        myLocalList = myOverWriteList.get(1).getLocalList(myName);

                    } else if ((2 < myOverWriteList.size()) && AclResourceType.MAP_BASEMAP.getDescriptor().equals(myRow.getType())) {

                        myLocalList = myOverWriteList.get(2).getLocalList(myName);
                    }
                }
                _nameChanger.inputNameChange(myRow, myLocalList);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ImportOptionMapper(SelectionChangeResponder parentIn, int widthIn, int heightIn, String helpFileNameIn,
                              List<ResourceConflictInfo> itemListIn,  Map<String, List<OverWrite>> overWriteIn,
                              String columnHeaderIn, String FieldHeaderIn, boolean iAmOwnerIn) {

        super(parentIn, widthIn, heightIn, helpFileNameIn, columnHeaderIn, FieldHeaderIn);
        initializeAll(itemListIn, overWriteIn, iAmOwnerIn);
    }

    public void setNameChanger(NameChangeAccess nameChangerIn) {

        _nameChanger = nameChangerIn;
    }

    private void initializeAll(List<ResourceConflictInfo> itemListIn,
                               Map<String, List<OverWrite>> overWriteIn, boolean iAmOwnerIn) {

        _iAmOwner = iAmOwnerIn;
        if ((null != itemListIn) && (0 < itemListIn.size())) {

            _itemList = itemListIn;
            _overWrite = overWriteIn;
            _userOverWriteList = _overWrite.get("");
            _optionLimits = new int[itemListIn.size()][];
            createDataGrid(_width);

            for (int i = 0; _itemList.size() > i; i++) {

                ResourceConflictInfo myItem = _itemList.get(i);
                AclResourceType myType = myItem.getType();

               if (AclResourceType.ICON.equals(myType)) {

                    _dataStore.add(new ResourceConflictDisplay(i, null, myType.getDescriptor(),
                                                                myItem.getName(), myItem.getOwner(), null,
                                                                ConflictResolution.MERGE_KEEP, true, false));

                } else {
/*
                    if ((null != myItem.getConflictId())
                            && (!(AclResourceType.DATAVIEW.equals(myType)
                            || AclResourceType.TEMPLATE.equals(myType)
                            || AclResourceType.MAP_BASEMAP.equals(myType)))) {
*/
                   if ((null != myItem.getConflictId()) & (!AclResourceType.DATAVIEW.equals(myType))) {
                        if (myItem.getAuthorized()) {

                            _optionLimits[i] = ConflictResolution.getLimits(myType);
                            _dataStore.add(new ResourceConflictDisplay(i, myItem.getUuid(), myType.getDescriptor(),
                                                                        myItem.getName(), myItem.getOwner(),
                                                                        myItem.getRemarks(), null, true, false));

                        } else {

                            _dataStore.add(new ResourceConflictDisplay(i, myItem.getUuid(), myType.getDescriptor(),
                                                                        myItem.getName(), myItem.getOwner(),
                                                                        myItem.getRemarks(), ConflictResolution.IMPORT_NEW,
                                                                        true, false));
                        }

                    } else {

                        _dataStore.add(new ResourceConflictDisplay(i, myItem.getUuid(), myType.getDescriptor(),
                                                                    myItem.getName(), myItem.getOwner(),
                                                                    myItem.getRemarks(), null, false, false));
                    }
                    addLocal(myItem);
                }
            }
        }
    }

    public List<ImportItem> getImportRequestList() {

        List<ImportItem> myRequestList = new ArrayList<ImportItem>();
        String myForceOwner = _iAmOwner ? getMainPresenter().getUserName() : null;

        for (int i = 0; _itemList.size() > i; i++) {

            ResourceConflictInfo myItem = _itemList.get(i);
            AclResourceType myType = myItem.getType();
            ResourceConflictDisplay myDisplay = _dataStore.get(i);
            String myFile = myItem.getFileName();

            if (myDisplay.getSelected()) {

                String myOwner = (null != myForceOwner) ? myForceOwner : myDisplay.getOwner();
                ImportItem myRequest = new ImportItem(myType, myDisplay.getName(), myDisplay.getRemarks(), myOwner,
                                                        myDisplay.getUuid(), myFile, myDisplay.getResolution());

                myRequestList.add(myRequest);
            }
        }
        return (0 < myRequestList.size()) ? myRequestList : null;
    }

    public FixedSizeGrid<ResourceConflictDisplay> getGrid() {

        return dataGrid;
    }

    public String getColor(int rowIn, int columnIn) {

        String myColor = Dialog.txtLabelColor;

        if (2 == columnIn) {

            ResourceConflictDisplay myRow = ((0 <= rowIn) && (_dataStore.size() > rowIn))
                    ? _dataStore.get(rowIn)
                    : null;

            if ((null != myRow) && myRow.getSelected()) {

                String myName = myRow.getName();
                String myOwner = myRow.getOwner();
                String myType = myRow.getType();
                List<OverWrite> myOverWriteList = _iAmOwner
                        ? _userOverWriteList
                        : (((null != _overWrite) && (null != myOwner))
                        ? _overWrite.get(myOwner) : null);

                if (null != myOverWriteList) {

                    if ((0 < myOverWriteList.size()) && AclResourceType.DATAVIEW.getDescriptor().equals(myType)) {

                        myColor = myOverWriteList.get(0).getColor(myName);

                    } else if ((1 < myOverWriteList.size()) && AclResourceType.TEMPLATE.getDescriptor().equals(myType)) {

                        myColor = myOverWriteList.get(1).getColor(myName);

                    } else if ((2 < myOverWriteList.size()) && AclResourceType.MAP_BASEMAP.getDescriptor().equals(myType)) {

                        myColor = myOverWriteList.get(2).getColor(myName);
                    }
                }
            }
        }
        return myColor;
    }

    public void refreshOverWrite() {

        clearLocalList();

        for (ResourceConflictDisplay myStore : dataGrid.getStore().getAll()) {

            if (myStore.getSelected()) {

                addLocal(myStore);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    protected GridComponentManager<ResourceConflictDisplay> createDataGrid(int widthIn) {

        int mySmallDelta = 20;
        int myLargeDelta = 30;
        int myAverageWidth = (widthIn - 22) / 4;
        ParameterMapProperties myProperties = GWT.create(ParameterMapProperties.class);
        CsiTextCell myClickableCell = new CsiTextCell(clickHandler, this);

        gridColumnidth = new int[] {myAverageWidth - myLargeDelta, myAverageWidth + mySmallDelta,
                                    myAverageWidth - mySmallDelta, myAverageWidth + myLargeDelta,
                                    myAverageWidth + (mySmallDelta / 2)};

        final GridComponentManager<ResourceConflictDisplay> myManager
                = (GridComponentManager<ResourceConflictDisplay>) WebMain.injector.getGridFactory().create(myProperties.key());

        // Create check box column
        createSelectionModel();
        myManager.getColumnConfigList().add(_selectionModel.getColumn());

        // Create Resource type column

        gridTypeColumn = myManager.create(myProperties.type(), gridColumnidth[0], "Resource Type", false, true);
        gridTypeColumn.setHideable(false);
        gridTypeColumn.setWidth(gridColumnidth[0]);
        gridTypeColumn.setCell(new TextCell());

        // Create Resource name column
        gridNameColumn = myManager.create(myProperties.name(), gridColumnidth[1], "Resource Name", false, true);
        gridNameColumn.setHideable(false);
        gridNameColumn.setWidth(gridColumnidth[1]);
        gridNameColumn.setCell(myClickableCell);

        // Create Resource owner column
        gridOwnerColumn = myManager.create(myProperties.owner(), gridColumnidth[2], "Resource Owner", false, true);
        gridOwnerColumn.setHideable(false);
        gridOwnerColumn.setWidth(gridColumnidth[2]);
        gridOwnerColumn.setCell(new TextCell());

        // Import option column column
        gridOptionColumn = myManager.create(myProperties.resolution(), gridColumnidth[3], "Import Option", false, true);
        gridOptionColumn.setWidth(gridColumnidth[3]);
        _optionCell = createComboBoxCell();
        gridOptionColumn.setCell(_optionCell);

        // Create the grid so that it will be available for linking with individual cells
        createGrid(myManager);
        dataGrid.setSelectionModel(_selectionModel);
        _dataStore = dataGrid.getStore();
        _optionCell.setStore(_dataStore);
        myClickableCell.addStore(_dataStore);

        addOptionEditor(gridOptionColumn);

        return myManager;
    }

    protected OptionCell<ConflictResolution, ResourceConflictDisplay> createComboBoxCell() {

        ImportOptionComboBox myComboBox = new ImportOptionComboBox();
        OptionCell myCell = new OptionCell<ConflictResolution, ResourceConflictDisplay>(myComboBox, "No conflicting IDs.");

        myCell.setNullText(ConflictResolution.NO_CHANGE.getLabel());
        return myCell;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void createSelectionModel() {

        _selectionModel = new CsiCheckboxImportSelectionModel(new CustomProvider());

        _selectionModel.addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<ResourceConflictDisplay>(){

            @Override
            public void onSelectionChanged(SelectionChangedEvent<ResourceConflictDisplay> event) {

                List<ResourceConflictDisplay> list = event.getSelection();
                XElement myScroller = dataGrid.getView().getScroller();
                int myScrollTop = -1;

                if (null != myScroller) {

                    myScrollTop = myScroller.getScrollTop();
                }
                for(ResourceConflictDisplay store: list){

                    if(!store.getSelected()){

                        store.setSelected(true);
                    }
                }

                for(ResourceConflictDisplay store: dataGrid.getStore().getAll()){
                    if(store.getSelected() && !list.contains(store)){
                        store.setSelected(false);
                    }
                }
                refreshOverWrite();
                gridOptionColumn.setWidth(gridColumnidth[4]);
                dataGrid.getView().refresh(false);
                if (0 <= myScrollTop) {

                    myScroller.setScrollTop(myScrollTop + 1);
                }
            }
        });
        _selectionModel.setShowSelectAll(true);
        _selectionModel.setSelectionMode(com.sencha.gxt.core.client.Style.SelectionMode.MULTI);
    }

    private void clearLocalList() {

        for (List<OverWrite> myOverWriteList : _overWrite.values()) {

            for (OverWrite myOverWrite : myOverWriteList) {

                myOverWrite.clearLocalList();
            }
        }
    }

    private void addLocal(ResourceConflictDisplay displayIn) {

        if (null != displayIn) {

            addLocal(AclResourceType.getTypeFromDescriptor(displayIn.getType()), displayIn.getName(), displayIn.getOwner());
        }
    }

    private void addLocal(ResourceConflictInfo itemIn) {

        if (null != itemIn) {

            addLocal(itemIn.getType(), itemIn.getName(), itemIn.getOwner());
        }
    }

    private void addLocal(AclResourceType typeIn, String nameIn, String ownerIn) {

        if (null != nameIn) {

            List<OverWrite> myAdminList = _overWrite.get(ownerIn);

            if (AclResourceType.DATAVIEW.equals(typeIn)) {

                if (null != myAdminList) {

                    OverWrite myAdminOverWrite = myAdminList.get(0);

                    if (null != myAdminOverWrite) {

                        myAdminOverWrite.addLocal(nameIn);
                    }
                }
                _userOverWriteList.get(0).addLocal(nameIn);

            } else if (AclResourceType.TEMPLATE.equals(typeIn)) {

                if (null != myAdminList) {

                    OverWrite myAdminOverWrite = myAdminList.get(1);

                    if (null != myAdminOverWrite) {

                        myAdminOverWrite.addLocal(nameIn);
                    }
                }
                _userOverWriteList.get(1).addLocal(nameIn);

            } else if (AclResourceType.MAP_BASEMAP.equals(typeIn)) {

                if (null != myAdminList) {

                    OverWrite myAdminOverWrite = myAdminList.get(2);

                    if (null != myAdminOverWrite) {

                        myAdminOverWrite.addLocal(nameIn);
                    }
                }
                _userOverWriteList.get(2).addLocal(nameIn);
            }
        }
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}

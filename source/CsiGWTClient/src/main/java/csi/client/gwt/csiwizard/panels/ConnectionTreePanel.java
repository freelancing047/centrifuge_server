package csi.client.gwt.csiwizard.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.IconProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.SourceListChangeMonitor;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.IntegerInputWidget;
import csi.client.gwt.csiwizard.dialogs.DataSourceDialog;
import csi.client.gwt.csiwizard.support.ConnectionItemType;
import csi.client.gwt.csiwizard.support.ConnectionTreeItem;
import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.edit_sources.dialogs.common.QueryNameMapProvider;
import csi.client.gwt.edit_sources.dialogs.query.CustomQueryDialog;
import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.events.HoverEventHandler;
import csi.client.gwt.events.TransferCompleteEvent;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.events.TreeSelectionEvent;
import csi.client.gwt.events.TreeSelectionEventHandler;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.LogonDialog;
import csi.server.common.util.ConnectorSupport;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.server.common.util.AuthorizationObject;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.buttons.*;
import csi.client.gwt.widget.cells.ExtendedDisplays;
import csi.client.gwt.widget.gxt.drag_n_drop.DragAndDropTree;
import csi.client.gwt.widget.input_boxes.FilteredIntegerInput;
import csi.client.gwt.widget.ui.uploader.FileSelectorDialog;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.Response;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.UUID;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.service.api.TestActionsServiceProtocol;


public class ConnectionTreePanel extends AbstractWizardPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public interface TableCallBack {

        public void handleResponse(SqlTableDef tableIn);
    }

    interface singleArg extends XTemplates {

        SafeHtml displayEntry(String listStringIn);
    }

    interface specialSingle extends singleArg {

        @XTemplates.XTemplate("<div style=\"color:blue;\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn);
    }

    class HighlightingStringCell extends ClickableTextCell {

        TreeStore<ConnectionTreeItem> _dataStore;

        public HighlightingStringCell(TreeStore<ConnectionTreeItem> dataStoreIn) {

            _dataStore = dataStoreIn;
        }

        @Override
        public void render(Context contextIn, String valueIn, SafeHtmlBuilder builderIn) {

            ConnectionTreeItem myItem = _dataStore.findModelWithKey((String) contextIn.getKey());
            DisplayMode myMode = (myItem.isSpecial()) ? DisplayMode.SPECIAL : DisplayMode.NORMAL;

            builderIn.append(ExtendedDisplays.displayEntry(myMode, valueIn));
        }
    }

    class ConnectionTreeValueProvider implements ValueProvider<ConnectionTreeItem, String> {

        @Override
        public String getValue(ConnectionTreeItem object) {
            // TODO Auto-generated method stub
            return object.getName();
        }

        @Override
        public void setValue(ConnectionTreeItem object, String value) {
            //do nothing?
        }

        @Override
        public String getPath() {
            return "name"; //$NON-NLS-1$
        }
    }

    class ConnectionTreeModelKeyProvider implements ModelKeyProvider<ConnectionTreeItem> {


        @Override
        public String getKey(ConnectionTreeItem item) {
            return item.key;
        }
    }

    class ConnectionTreeIconProvider implements IconProvider<ConnectionTreeItem> {

        @Override
        public ImageResource getIcon(ConnectionTreeItem itemIn) {

            ImageResource myImageResource = null;

            switch (itemIn.type) {

                case CONNECTION_DEF:
                case DATA_SOURCE_DEF:

                    myImageResource = DataSourceClientUtil.get(itemIn.getConnectionDef(), false);
                    break;

                case TABLE:

                    myImageResource = DataSourceClientUtil.getTableImageResource();
                    break;

                case COLUMN:

                    myImageResource = DataSourceClientUtil.get(itemIn.getColumnDef());
                    break;

                case CUSTOM_QUERY:

                    myImageResource = DataSourceClientUtil.getCustomQueryIcon();
                    break;

                case INSTALLER:

                    myImageResource = DataSourceClientUtil.getInstallerIcon();
                    break;

                default:

                    myImageResource = DataSourceClientUtil.getFolderImageResource();
                    break;
            }
            return myImageResource;
        }
    }

    /**
     * A container class for items represented in the Connection tree.  Can hold a TableDef, ConnectionDef, or a CollumnDef
     *
     * @author bmurray
     */

    class MapValue {

        public ConnectionTreeItem item;
        public Map<String, MapValue> map;

        public MapValue(ConnectionTreeItem itemIn, Map<String, MapValue> mapIn) {

            item = itemIn;
            map = mapIn;
        }
    }

    class SciConnectionTree extends DragAndDropTree<ConnectionTreeItem> {

        public SciConnectionTree(ConnectionTreePanel parentIn, TreeStore<ConnectionTreeItem> storeIn,
                                 ConnectionTreeValueProvider valueProviderIn,
                                 ClickHandler selectionHandlerIn) {

            super(parentIn, storeIn, valueProviderIn, selectionHandlerIn, false, true);
        }

        @Override
        public void scrollIntoView(ConnectionTreeItem modelIn) {

            TreeNode<ConnectionTreeItem> myNode = findNode(modelIn);

            if (myNode != null) {

                XElement myContainer = (XElement) myNode.getElementContainer();

                if (myContainer != null) {

                    myContainer.scrollIntoView(getElement(), false);
                    focusEl.setXY(myContainer.getXY());
                    focus();
                }
            }
        }

        @Override
        public boolean isLeaf(ConnectionTreeItem modelIn) {

            boolean myLeafFlag = true;

            if ((null != modelIn) && (null != modelIn.type)) {

                switch (modelIn.type) {

                    case DATA_SOURCE_DEF:
                    case CATALOG:
                    case SCHEMA:
                    case TABLE_TYPE:
                    case TABLE:

                        myLeafFlag = false;
                        break;

                    default:

                        myLeafFlag = super.isLeaf(modelIn);
                }
            }
            return myLeafFlag;
        }

        @Override
        protected void onDoubleClick(Event eventIn) {
            TreeNode<ConnectionTreeItem> node = findNode(eventIn.getEventTarget().<Element>cast());
            if (node != null) {

                super.onDoubleClick(eventIn);
                _selectionHandler.onClick(null);
            }
        }

        @Override
        protected void onExpand(ConnectionTreeItem modelIn, TreeNode<ConnectionTreeItem> nodeIn, boolean deepIn) {

            if (modelIn.hasAllChildren) {

                super.onExpand(modelIn, nodeIn, deepIn);

            } else {

                ((ConnectionTreePanel) _parent).retrieveChildren(modelIn);
            }
        }

        public void deleteChildren(ConnectionTreeItem modelIn) {

            store.removeChildren(modelIn);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private WatchBoxInterface watchBox = WatchBox.getInstance();
    private LayoutPanel topPanel = null;
    private HorizontalPanel topButtonPanel = null;
    private SciConnectionTree tree;
    LayoutPanel selectionBox = null;
    InlineLabel rowLimitLabel = null;
    FilteredIntegerInput rowLimitTextBox = null;
    Label selectionName = null;
    MiniRedButton deleteButton = null;
    MiniBlueButton addButton = null;
    MiniBlueButton editButton = null;
    MiniBlueButton newButton = null;

    private ValidatingDialog identityPromptDialog = null;
    private IntegerInputWidget identityWidget = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String PX = "px"; //$NON-NLS-1$
    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final ConnectionItemType[] _namePathType = {ConnectionItemType.CATALOG,
            ConnectionItemType.SCHEMA,
            ConnectionItemType.TABLE_TYPE};
    protected static final String _txtFailureDialogTitle = _constants.serverRequest_FailureDialogTitle();
    protected static final String _txtWaitingForTableList = _constants.serverRequest_WaitingForTableList();
    protected static final String _txtWaitingForColumnList = _constants.serverRequest_WaitingForColumnList();
    protected static final String _txtAddButtonHint = _constants.dataSourceEditor_AddButtonHint();
    protected static final String _txtNewButtonHint = _constants.dataSourceEditor_NewButtonHint();
    protected static final String _txtEditButtonHint = _constants.dataSourceEditor_EditButtonHint();
    protected static final String _txtDeleteButtonHint = _constants.dataSourceEditor_DeleteButtonHint();
    protected static final String _txtNewCustomQuery = _constants.connectionTreePanelNewQuery();
    protected static final String _txtNewInstalledTable = _constants.connectionTreePanelNewTable();

    private IconProvider<ConnectionTreeItem> _iconProvider = new ConnectionTreeIconProvider();

    private ConnectionTreeValueProvider _valueProvider = new ConnectionTreeValueProvider();
    private ConnectionTreeModelKeyProvider _keyProvider = new ConnectionTreeModelKeyProvider();

    private TreeStore<ConnectionTreeItem> _store = null;

    private TreeSelectionEventHandler<ConnectionTreeItem, SqlTableDef, DataSourceDef> _addButtonHandler = null;
    private boolean _validFlag = false;

    QueryNameMapProvider _queryNameMapProvider = null;

    private ConnectionTreeItem _activeSelection = null;
    private ConnectionTreeItem _processedItem = null;
    private ConnectionTreeItem _itemSelection = null;
    private ConnectionTreeItem _sourceSelection = null;
    private DataSourceEditorModel _model = null;
    private List<DataSourceDef> _dataSourceList = null;
    private Map<String, ConnectionTreeItem> _treeItemMap = null;
    private List<QueryParameterDef> _dataSetParameters = null;
    private ParameterPresenter _parameterPresenter;
    private Map<String, AuthDO> _authorizationMap = null;
    private Map<ConnectionTreeItem, ConnectionTreeItem> _queryItems
            = new HashMap<ConnectionTreeItem, ConnectionTreeItem>();

    private int _buttonWidth = 75;

    private CustomQueryDialog _queryDialog = null;
    private int _prefix = 0;
    private String _activePrefix = "  "; //$NON-NLS-1$
    private int _requestCount = 0;
    private boolean _editMode = true;
    private boolean _isNewTable = false;
    private boolean _installingTables = false;
    private HoverEventHandler _buttonMonitor = null;
    private SourceListChangeMonitor _sourceListChangeMonitor = null;
    private TableCallBack _tableCallBack = null;
    private ClickHandler _browseHandler = null;
    ConnectionTreeItem _focusRequest = null;
    ConnectionTreeItem _firstTable = null;
    private List<String> _highLightList = null;
    private boolean _safeMode = false;
    private boolean _displayButtons = false;
    private UserSecurityInfo userInfo = WebMain.injector.getMainPresenter().getUserInfo();
    private WizardInterface _this;
    private boolean _monitoring = false;



    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle response to request for column list
    //
    private VortexEventHandler<Response<String, List<ColumnDef>>> handleColumnSelectionResponse
            = new AbstractVortexEventHandler<Response<String, List<ColumnDef>>>() {
        @Override
        public boolean onError(Throwable myException) {

            enableInput();

            Dialog.showException(myException);

            return false;
        }

        @Override
        public void onSuccess(Response<String, List<ColumnDef>> responseIn) {

            String myKey = responseIn.getKey();

            watchBox.hide();

            if (responseIn.isAuthorizationRequired()) {

                try {

                    ConnectionTreeItem myItem = _treeItemMap.get(myKey);
                    DataSourceDef mySource = getDataSource(myItem);

                    Dialog.showLogon(new AuthorizationObject(_authorizationMap, mySource), processColumnLogon, myItem);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }

            } else if (ResponseHandler.isSuccess(responseIn)) {

                List<ColumnDef> myList = responseIn.getResult();
                SqlTableDef myTable = getSqlTable();

                myTable.setColumns(myList);

                _tableCallBack.handleResponse(myTable);
            }
        }
    };
    /*
        private ClickHandler handleIdentityFieldContinueButtonClick
                = new ClickHandler() {
            @Override
            public void onClick(ClickEvent eventIn) {

                if (null != identityPromptDialog) {

                    if (null != identityWidget) {

                        try {

                            _keyField = Integer.decode(identityWidget.getText());

                        } catch (Exception myException) {

                            Display.error(myException.getMessage());
                        }
                    }
                    identityPromptDialog.destroy();
                    identityPromptDialog = null;
                }

                if (null != _queryDialog) {

                    processQueryRequest();

                } else {

                    processTableRequest();
                }
            }
        };

        private ClickHandler handleIdentityFieldCancelButtonClick
                = new ClickHandler() {
            @Override
            public void onClick(ClickEvent eventIn) {

                if (null != identityPromptDialog) {

                    identityPromptDialog.destroy();
                    identityPromptDialog = null;
                }
            }
        };
    */
    //
    // Handle processing table after response to request for column list
    //
    private VortexEventHandler<Response<String, List<ColumnDef>>> handleAddingTableToDisplay
            = new AbstractVortexEventHandler<Response<String, List<ColumnDef>>>() {
        @Override
        public boolean onError(Throwable myException) {

            enableInput();

            Dialog.showException(myException);

            return false;
        }

        @Override
        public void onSuccess(Response<String, List<ColumnDef>> responseIn) {

            String myKey = responseIn.getKey();

            watchBox.hide();

            if (responseIn.isAuthorizationRequired()) {

                try {

                    ConnectionTreeItem myItem = _treeItemMap.get(myKey);
                    DataSourceDef mySource = getDataSource(myItem);

                    showLogon(mySource, processTableLogon, myItem);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }

            } else if (ResponseHandler.isSuccess(responseIn)) {

                ConnectionTreeItem mySelection = _treeItemMap.get(myKey);

                handleColumnListResponse.onSuccess(responseIn);

                processSelection(mySelection);
            }
        }
    };

    //
    // Handle response to request for column list
    //
    private VortexEventHandler<Response<String, List<ColumnDef>>> handleColumnListResponse
            = new AbstractVortexEventHandler<Response<String, List<ColumnDef>>>() {
        @Override
        public boolean onError(Throwable myException) {

            enableInput();

            Dialog.showException(myException);

            return false;
        }

        @Override
        public void onSuccess(Response<String, List<ColumnDef>> responseIn) {

            String myKey = responseIn.getKey();

            watchBox.hide();

            _processedItem = _treeItemMap.get(myKey);

            _processedItem.retrievingChildren = false;
            if (!_isNewTable) {

                enableInput();
            }

            if (responseIn.isAuthorizationRequired()) {

                try {

                    DataSourceDef mySource = getDataSource(_processedItem);

                    showLogon(mySource, processColumnLogon, _processedItem);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }

            } else if (ResponseHandler.isSuccess(responseIn)) {

                try {

                    List<ColumnDef> myList = responseIn.getResult();
                    SqlTableDef myTable = _processedItem.getSqlTableDef();

                    myTable.setColumns(myList);

                    if ((null != myList) && (0 < myList.size())) {

                        tree.deleteChildren(_processedItem);
                        for (ColumnDef myColumn : myList) {

                            ConnectionTreeItem myItem = new ConnectionTreeItem(myColumn, ConnectionItemType.COLUMN);
                            _store.add(_processedItem, myItem.insertParent(_processedItem));
                        }
                    }
                    _processedItem.hasAllChildren = true;
                    expandLatest();

                } catch (CentrifugeException myException) {

                    enableInput();
                    Dialog.showException(myException);
                }
            }
        }
    };

    private UserInputEventHandler<String> processLogon
            = new UserInputEventHandler<String>() {

        public void onUserInput(UserInputEvent<String> eventIn) {

            if (eventIn.isCanceled()) {

            } else {

                String myKey = eventIn.getKey();
                ConnectionTreeItem myItem = _treeItemMap.get(myKey);
                DataSourceDef mySource = myItem.getDataSourceDef();

                if (null != mySource) {

                    myItem.retrievingChildren = true;
                    retrieveCatalogList(mySource, myKey);
                }
            }
        }
    };

    private UserInputEventHandler<ConnectionTreeItem> processTableLogon
            = new UserInputEventHandler<ConnectionTreeItem>() {

        public void onUserInput(UserInputEvent<ConnectionTreeItem> eventIn) {

            if (eventIn.isCanceled()) {

            } else {

                ConnectionTreeItem myKey = eventIn.getKey();

                loadTableChildren(myKey, handleAddingTableToDisplay);
            }
        }
    };

    private UserInputEventHandler<ConnectionTreeItem> processColumnLogon
            = new UserInputEventHandler<ConnectionTreeItem>() {

        public void onUserInput(UserInputEvent<ConnectionTreeItem> eventIn) {

            if (eventIn.isCanceled()) {

            } else {

                ConnectionTreeItem myKey = eventIn.getKey();

                loadTableChildren(myKey, handleColumnListResponse);
            }
        }
    };

    //
    // Handle response to request for catalog list
    //
    private VortexEventHandler<Response<String, List<String>>> handleCatalogListResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        @Override
        public boolean onError(Throwable myException) {

            _requestCount = enableInput(_requestCount);

            Dialog.showException(myException);

            return false;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            String myKey = responseIn.getKey();
            ConnectionTreeItem myItem = _treeItemMap.get(myKey);
            DataSourceDef mySource = (null != myItem) ? myItem.getDataSourceDef() : null;

            watchBox.hide();

            if (null != mySource) {

                _requestCount = enableInput(_requestCount);

                if (responseIn.isAuthorizationRequired()) {

                    showLogon(mySource, processLogon, myKey);

                } else if (ResponseHandler.isSuccess(responseIn)) {

                    try {

                        List<String> myList = responseIn.getResult();

                        if (null != myList) {

                            addChildren(myItem, ConnectionItemType.CATALOG, myList);

                        } else {

                            retrieveSchemaList(mySource, null, myKey);
                        }

                    } catch (Exception myException) {

                        Dialog.showException(myException);
                    }
                }
            }
        }
    };

    //
    // Handle response to request for catalog list
    //
    private VortexEventHandler<Response<String, List<String>>> handleSchemaListResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        @Override
        public boolean onError(Throwable myException) {

            _requestCount = enableInput(_requestCount);

            Dialog.showException(myException);

            return false;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            String myKey = responseIn.getKey();
            ConnectionTreeItem myItem = _treeItemMap.get(myKey);
            DataSourceDef mySource = (null != myItem) ? myItem.getDataSourceDef() : null;

            watchBox.hide();

            myItem.retrievingChildren = false;

            if (null != mySource) {

                _requestCount = enableInput(_requestCount);

                if (responseIn.isAuthorizationRequired()) {

                    showLogon(mySource, processLogon, myKey);

                } else if (ResponseHandler.isSuccess(responseIn)) {

                    try {

                        List<String> myList = responseIn.getResult();

                        if (null != myList) {

                            addChildren(myItem, ConnectionItemType.SCHEMA, myList);

                        } else {

                            retrieveTableTypeList(mySource, myItem.getCatalog(), null, myKey);
                        }

                    } catch (Exception myException) {

                        Dialog.showException(myException);
                    }
                }
            }
        }
    };

    //
    // Handle response to request for catalog list
    //
    private VortexEventHandler<Response<String, List<String>>> handleTableTypeListResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        @Override
        public boolean onError(Throwable myException) {

            _requestCount = enableInput(_requestCount);

            Dialog.showException(myException);

            return false;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            String myKey = responseIn.getKey();
            ConnectionTreeItem myItem = _treeItemMap.get(myKey);
            DataSourceDef mySource = (null != myItem) ? myItem.getDataSourceDef() : null;

            watchBox.hide();

            myItem.retrievingChildren = false;

            if (null != mySource) {

                _requestCount = enableInput(_requestCount);

                if (responseIn.isAuthorizationRequired()) {

                    showLogon(mySource, processLogon, myKey);

                } else if (ResponseHandler.isSuccess(responseIn)) {

                    try {

                        List<String> myList = responseIn.getResult();

                        if (null != myList) {

                            addChildren(myItem, ConnectionItemType.TABLE_TYPE, myList);

                        } else {

                            retrieveTableList(mySource, myItem.getCatalog(), myItem.getSchema(), myItem.getTableType(), myKey);
                        }

                    } catch (Exception myException) {

                        Dialog.showException(myException);
                    }
                }
            }
        }
    };

    //
    // Handle response to request for catalog list
    //
    private VortexEventHandler<Response<String, List<SqlTableDef>>> handleTableListResponse
            = new AbstractVortexEventHandler<Response<String, List<SqlTableDef>>>() {
        @Override
        public boolean onError(Throwable myException) {

            _requestCount = enableInput(_requestCount);

            Dialog.showException(myException);

            return false;
        }

        @Override
        public void onSuccess(Response<String, List<SqlTableDef>> responseIn) {

            String myKey = responseIn.getKey();
            ConnectionTreeItem myItem = _treeItemMap.get(myKey);
            DataSourceDef mySource = (null != myItem) ? myItem.getDataSourceDef() : null;

            watchBox.hide();

            myItem.retrievingChildren = false;

            if (null != mySource) {

                _requestCount = enableInput(_requestCount);

                if (responseIn.isAuthorizationRequired()) {

                    showLogon(mySource, processLogon, myKey);

                } else if (ResponseHandler.isSuccess(responseIn)) {

                    try {

                        List<SqlTableDef> myList = responseIn.getResult();

                        addChildren(myItem, myList);

                    } catch (Exception myException) {

                        Dialog.showException(myException);
                    }
                }
            }
        }
    };

    //
    //
    //
    private SelectionChangedHandler<ConnectionTreeItem> handleSelectionChangeEvent
            = new SelectionChangedHandler<ConnectionTreeItem>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<ConnectionTreeItem> eventIn) {

            validateSelection(eventIn.getSelection());
        }
    };

    private ClickHandler handleNewDataSourceRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            //
            // Disable the Add Data Source button
            //
            enableNewButton(false);
            (new DataSourceDialog(handleNewDataSourceResponse)).show();
        }
    };

    private ClickHandler handleDataSourceEditRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            DataSourceDef myDataSource = _sourceSelection.getDataSourceDef();

            if (null != myDataSource) {

                storePrefix(myDataSource.getName());
                (new DataSourceDialog(handleDataSourceEditResponse, myDataSource)).show();
            }
        }
    };

    private ClickHandler handleCancelDataSourceEditRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            enableNewButton(true);
        }
    };

    private ClickHandler handleDataSourceEditButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            DataSourceDef myDataSource = _sourceSelection.getDataSourceDef();

            if (null != myDataSource) {

                if (ConnectorSupport.getInstance().canEditConnection(myDataSource)) {

                    //
                    // Disable the Add Data Source button
                    //
                    enableNewButton(false);
                    if ((!_safeMode) && myDataSource.hasChildren()) {

                        Dialog.showContinueDialog(i18n.dataSourceEditor_EditSourceTitle(),
                                i18n.dataSourceEditor_EditSourceMessage(),
                                handleDataSourceEditRequest, handleCancelDataSourceEditRequest);

                    } else {

                        handleDataSourceEditRequest.onClick(null);
                    }

                } else {

                    Display.error(i18n.security_NotAuthorized());
                }
            }
        }
    };

    private ClickHandler handleDeleteDataSourceRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            if ((null != _dataSourceList) && (null != _sourceSelection)) {

                deleteBranch(_sourceSelection);
            }
            //
            // Disable the Delete Data Source button
            //
            enableDeleteButton(false);
            enableEditButton(false);
            _sourceSelection = null;
            _processedItem = null;
            sourceListChanged();
        }
    };

    private ClickHandler handleCancelDeleteDataSourceRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            enableDeleteButton(true);
            enableEditButton(true);
        }
    };

    private ClickHandler handleDeleteDataSourceButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            if ((null != _dataSourceList) && (null != _sourceSelection)) {

                DataSourceDef myDataSource = _sourceSelection.getDataSourceDef();

                enableDeleteButton(false);
                enableEditButton(false);
                if ((!_safeMode) && myDataSource.hasChildren()) {

                    Dialog.showContinueDialog(i18n.dataSourceEditor_RemoveSourceTitle(),
                                                i18n.dataSourceEditor_RemoveSourceMessage1(),
                            handleDeleteDataSourceRequest, handleCancelDeleteDataSourceRequest);

                } else {

                    Dialog.showContinueDialog(i18n.dataSourceEditor_RemoveSourceTitle(),
                            i18n.dataSourceEditor_RemoveSourceMessage2(myDataSource.getName()),
                            handleDeleteDataSourceRequest, handleCancelDeleteDataSourceRequest);
                }
            }
        }
    };

    private DataChangeEventHandler handleNewDataSourceResponse
            = new DataChangeEventHandler() {
        @Override
        public void onDataChange(DataChangeEvent eventIn) {

            Object myObject = eventIn.getData();

            if ((null != myObject) && (myObject instanceof DataSourceDef)) {

                DataSourceDef mySource = (DataSourceDef) myObject;

                mySource.setName(getPrefix() + mySource.getName());

                _processedItem = null;

                tree.collapseAll();
                tree.getSelectionModel().deselectAll();
                selectionName.setText(null);

                DeferredCommand.add(new Command() {
                    public void execute() {
                        tree.collapseAll();
                    }
                });

                disableInput();

                if (_validFlag) {

                    _validFlag = false;

                    fireEvent(new ValidityReportEvent(_validFlag, null));
                }
                addConnection(mySource);
                sourceListChanged();

            } else {

                enableInput();
            }
        }
    };

    private DataChangeEventHandler handleDataSourceEditResponse
            = new DataChangeEventHandler() {
        @Override
        public void onDataChange(DataChangeEvent eventIn) {

            Object myObject = eventIn.getData();

            if ((null != myObject) && (myObject instanceof DataSourceDef)) {

                DataSourceDef mySource = (DataSourceDef) myObject;

                _processedItem = null;

                disableInput();

                if (_validFlag) {

                    _validFlag = false;

                    fireEvent(new ValidityReportEvent(_validFlag, null));
                }
                replaceConnection(mySource);
                sourceListChanged();

            } else {

                enableInput();
            }
        }
    };

    ClickHandler handleAddItemRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            processSelection();
        }
    };

    ClickHandler handleCreateQuerySuccess
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            DataSourceDef myDataSource = _queryDialog.getDataSource();
/*
            if (myDataSource.isInPlace() && (null == _keyField)) {

                _activeSelection = null;
                promptForIdentityField();

            } else {

                processQueryRequest();
            }
*/
            processQueryRequest();
        }
    };

    private ClickHandler installTableHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                suspendMonitoring();
                (new FileSelectorDialog(_this, handleInstallComplete, null)).show();

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
    };

    private TransferCompleteEventHandler handleInstallComplete
            = new TransferCompleteEventHandler() {

        @Override
        public void onTransferComplete(TransferCompleteEvent eventIn) {

            try {

                reformatDataSource(eventIn.getItemList());
                resumeMonitoring();

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // currently used by the data source editor
    //
    public ConnectionTreePanel(WizardInterface parentDialogIn, DataSourceEditorModel modelIn,
                               TreeSelectionEventHandler<ConnectionTreeItem, SqlTableDef, DataSourceDef> handlerIn,
                               boolean displayButtonsIn) throws CentrifugeException {

        super(parentDialogIn);

        try {

            _this = parentDialogIn;
            _displayButtons = displayButtonsIn;
            _editMode = true;
            _model = modelIn;
            _dataSourceList = _model.getDataSources();
            _treeItemMap = new HashMap<String, ConnectionTreeItem>();
            _addButtonHandler = handlerIn;
            _authorizationMap = WebMain.injector.getMainPresenter().getAuthorizationMap();
            _browseHandler = installTableHandler;

            initializePrefix();

            initializeObject();

        } catch (Exception myException) {

            Display.error(myException.getMessage());
        }
    }

    //
    // Currently used by the wizard
    //
    public ConnectionTreePanel(WizardInterface parentDialogIn, final DataSourceDef dataSourceIn,
                               List<QueryParameterDef> dataSetParametersIn, ClickHandler browseHandlerIn,
                               List<String> highLightListIn) throws CentrifugeException {

        super(parentDialogIn);

        try {

            _this = parentDialogIn;
            dataSourceIn.setName("01 " + dataSourceIn.getName()); //$NON-NLS-1$
            reformatPanel(dataSourceIn, dataSetParametersIn, browseHandlerIn, highLightListIn);

        } catch (Exception myException) {

            Display.error(myException.getMessage());
        }
    }

    public ConnectionTreePanel releasePanel() {

        _model = null;
        return (ConnectionTreePanel)super.releasePanel();
    }

    public void addDataSource() {

        handleNewDataSourceRequest.onClick(null);
    }

    public void editDataSource() {

        handleDataSourceEditButtonClick.onClick(null);
    }

    public void setSourceListChangeMonitor(SourceListChangeMonitor monitorIn) {

        _sourceListChangeMonitor = monitorIn;
    }

    public void removeDataSource() {

        handleDeleteDataSourceButtonClick.onClick(null);
    }

    public void dropItem(ConnectionTreeItem itemIn) {

        if (ConnectionItemType.TABLE.equals(itemIn.type)
                || ConnectionItemType.CUSTOM_QUERY.equals(itemIn.type)) {

            _itemSelection = itemIn;
            processSelection();
        }
    }

    public void reformatPanel(final DataSourceDef dataSourceIn, List<QueryParameterDef> dataSetParametersIn,
                              ClickHandler browseHandlerIn, List<String> highLightListIn) throws CentrifugeException {

        try {

            resetAll();

            _editMode = false;
//            dataSourceIn.setName("01 " + dataSourceIn.getName()); //$NON-NLS-1$
            _dataSourceList = new ArrayList<DataSourceDef>();
            _treeItemMap = new HashMap<String, ConnectionTreeItem>();

            _dataSourceList.add(dataSourceIn);
            _dataSetParameters = dataSetParametersIn;
            if (null != browseHandlerIn) {

                _browseHandler = browseHandlerIn;

            } else {

                _browseHandler = installTableHandler;
            }

            _highLightList = highLightListIn;

            initializePrefix();

            initializeObject();

        } catch (Exception myException) {

            Display.error(myException.getMessage());
        }
    }

    public void hide() {

        watchBox.hide();
    }

    public boolean querySelected() {

        return ((null != _itemSelection) && (ConnectionItemType.CUSTOM_QUERY.equals(_itemSelection.type)));
    }

    public boolean tableSelected() {

        return ((null != _itemSelection) && (ConnectionItemType.TABLE.equals(_itemSelection.type)));
    }

    public boolean installerSelected() {

        return ((null != _itemSelection) && (ConnectionItemType.INSTALLER.equals(_itemSelection.type)));
    }

    public SqlTableDef getSqlTable(ConnectionTreeItem selectionIn) {

        return (null != selectionIn) ? selectionIn.getSqlTableDef() : null;
    }

    public SqlTableDef getSqlTable() {

        return getSqlTable(_itemSelection);
    }

    public SqlTableDef getOnlySqlTable() {

        SqlTableDef myTable = null;

        if (tableSelected()) {

            SqlTableDef myTableTemplate = getSqlTable();
            DataSourceDef myDataSource = getDataSource();

            if ((null != myTableTemplate) && (null != myDataSource)) {

                myTable = (SqlTableDef) myTableTemplate.clone(true);

                myTable.setSource(myDataSource);
                myTable.setLocalId(UUID.randomUUID());
            }
        }

        return myTable;
    }

    public void getSqlTableWithColumns(TableCallBack callBackIn) {

        SqlTableDef myTable = getSqlTable();
        DataSourceDef myDataSource = getDataSource();
        List<ColumnDef> myColumns = myTable.getColumns();
/*
        if (((null != myColumns) && (0 < myColumns.size()))
                || (!ConnectorSupport.canEditDataSource(myDataSource, myTable))) {
*/
        myTable.setSource(myDataSource);
        if ((null != myColumns) && (0 < myColumns.size())) {

            callBackIn.handleResponse(myTable);

        } else {

            _tableCallBack = callBackIn;

            loadTableChildren(_itemSelection, handleColumnSelectionResponse);
        }
    }

    public DataSourceDef getDataSource(ConnectionTreeItem selectionIn) {

        DataSourceDef myDataSource = null;

        for (ConnectionTreeItem mySelection = selectionIn; null != mySelection; mySelection = mySelection.parent) {

            myDataSource = mySelection.getDataSourceDef();

            if (null != myDataSource) {

                break;
            }
        }

        return myDataSource;
    }

    public DataSourceDef getDataSource() {

        return getDataSource(_itemSelection);
    }

    public ConnectionDef getConnection(ConnectionTreeItem selectionIn) {

        DataSourceDef myDataSource = getDataSource(selectionIn);
        ConnectionDef myConnection = (null != myDataSource) ? myDataSource.getConnection() : null;

        if (null == myConnection) {

            for (ConnectionTreeItem mySelection = selectionIn; null != mySelection; mySelection = mySelection.parent) {

                myConnection = mySelection.getConnectionDef();

                if (null != myConnection) {

                    break;
                }
            }
        }

        return myConnection;
    }

    public ConnectionDef getConnection() {

        return getConnection(_itemSelection);
    }

    public void expandLatest() {

        DeferredCommand.add(new Command() {
            public void execute() {
                expandItem(_processedItem);
            }
        });
    }

    @Override
    public String getText() throws CentrifugeException {

        return selectionName.getText();
    }

    @Override
    public void grabFocus() {

        if (null != _focusRequest) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    focusItem(_focusRequest);
                }
            });

        } else if (null != _processedItem) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    focusItem(_processedItem);
                }
            });

        } else if (null != _firstTable) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    focusItem(_firstTable);
                }
            });
        }
    }

    @Override
    public boolean isOkToLeave() {

        return _validFlag;
    }

    @Override
    public void suspendMonitoring() {

    }

    @Override
    public void beginMonitoring() {

        _monitoring = true;
        restrictInput();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void enableInput() {

        watchBox.hide();

        validateSelection(tree.getSelectionModel().getSelection());

        tree.setEnabled(true);
        enableAddButton(null != _itemSelection);
        enableNewButton(true);
        enableEditButton(null != _sourceSelection);
        enableDeleteButton(null != _sourceSelection);
    }

    @Override
    public void handleCarriageReturn() {

        if (_validFlag) {

            super.handleCarriageReturn();
        }
    }
    //
    // Request the list of columns belonging to the identified table
    //
    private void loadTableChildren(ConnectionTreeItem selectionIn,
                                   VortexEventHandler<Response<String, List<ColumnDef>>> callBackIn) {

        DataSourceDef mySource = getDataSource(selectionIn);
        ConnectionDef myConnectionDef = (null != mySource) ? mySource.getConnection() : null;
        SqlTableDef myTable = getSqlTable(selectionIn);

        myTable.setSource(mySource);

//        if (ConnectorSupport.canEditDataSource(mySource, myTable)) {

        if (null != myConnectionDef) {

            VortexFuture<Response<String, List<ColumnDef>>> myVortexFuture = WebMain.injector.getVortex().createFuture();
            String myKey = selectionIn.key;

            watchBox.show(_txtWaitingForColumnList);
            myVortexFuture.addEventHandler(callBackIn);

            try {
                AuthorizationObject.updateConnectionCredentials(mySource, _authorizationMap);
                myVortexFuture.execute(TestActionsServiceProtocol.class).listTableColumns(myConnectionDef, myTable, myKey);

            } catch (Exception myException) {

                watchBox.hide();
                Dialog.showException(myException);
            }
        }
//        }
    }

    public List<QueryParameterDef> getParameterList() {

        return _dataSetParameters;
    }

    public void retrieveChildren(ConnectionTreeItem selectionIn) {

        if ((!selectionIn.hasAllChildren) && (!selectionIn.retrievingChildren)) {

            String myKey = selectionIn.key;
            ConnectionItemType myType = selectionIn.type;
            DataSourceDef myDataSource = selectionIn.getDataSourceDef();

            selectionIn.retrievingChildren = true;

            if (null != myDataSource) {

                switch (myType) {

                    case DATA_SOURCE_DEF:

                        retrieveCatalogList(myDataSource, myKey);
                        break;

                    case CATALOG:

                        retrieveSchemaList(myDataSource, selectionIn.getCatalog(), myKey);
                        break;

                    case SCHEMA:

                        retrieveTableTypeList(myDataSource, selectionIn.getCatalog(), selectionIn.getSchema(), myKey);
                        break;

                    case TABLE_TYPE:

                        retrieveTableList(myDataSource, selectionIn.getCatalog(), selectionIn.getSchema(), selectionIn.getTableType(), myKey);
                        break;

                    case TABLE:

                        SqlTableDef myTable = getSqlTable(selectionIn);

                        if (ConnectorSupport.getInstance().canEditDataSource(myDataSource, myTable)) {

                            loadTableChildren(selectionIn, handleColumnListResponse);

                        } else {

                            selectionIn.retrievingChildren = false;
                            Display.error(i18n.security_NotAuthorized());
                        }
                        break;

                    default:

                        selectionIn.retrievingChildren = false;
                        break;
                }
            }
        }
    }

    public void addButtonMonitor(HoverEventHandler handlerIn) {

        _buttonMonitor = handlerIn;

        if (null != _buttonMonitor) {

            if (null != addButton) {

                addButton.addHoverEventHandler(_buttonMonitor, _txtAddButtonHint);
            }
            if (null != newButton) {

                newButton.addHoverEventHandler(_buttonMonitor, _txtNewButtonHint);
            }
            if (null != editButton) {

                editButton.addHoverEventHandler(_buttonMonitor, _txtEditButtonHint);
            }
            if (null != deleteButton) {

                deleteButton.addHoverEventHandler(_buttonMonitor, _txtDeleteButtonHint);
            }
        }
    }

    public void clearSelection() {

        tree.getSelectionModel().deselectAll();
        clearSelectionHandles();
    }

    public Integer getRowLimit() {

        finalCheck();

        if (null != rowLimitTextBox) {

            String myText = rowLimitTextBox.getText();

            return ((null != myText) && (0 < myText.length())) ? Integer.decode(myText) : null;
        }
        return null;
    }

    public void setRowLimit(Integer rowLimitIn) {

        rowLimitTextBox.setText((null != rowLimitIn) ? rowLimitIn.toString() : null);
        beginMonitoring();
    }

    public void finalCheck() {

        _monitoring = false;
        restrictInput();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

        _store = new TreeStore<ConnectionTreeItem>(_keyProvider);
        tree = new SciConnectionTree(this, _store, _valueProvider, handleAddItemRequest);

        selectionBox = new LayoutPanel();
        selectionBox.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        selectionBox.getElement().getStyle().setBorderWidth(1, Unit.PX);
        selectionBox.getElement().getStyle().setBorderColor("#DDDDDD");
        selectionName = new Label();
        selectionBox.add(selectionName);

        tree.setIconProvider(_iconProvider);
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tree.setCell(new HighlightingStringCell(_store));

        if ((null != _dataSourceList) &&(0 < _dataSourceList.size())) {

            for (DataSourceDef myDataSource : _dataSourceList) {

                ConnectionTreeItem myItem = addConnectionTreeBase(myDataSource);
                String myKey = myItem.key;

                _treeItemMap.put(myKey, myItem);
            }
        }

        add(tree);
        add(selectionBox);

        if (_displayButtons && (null != _addButtonHandler)) {

            addButton = new MiniBlueButton();
            addButton.setText(i18n.connectionTreePanelAddButtonText()); //$NON-NLS-1$
            enableAddButton(false);
            if (null != _buttonMonitor) {

                addButton.addHoverEventHandler(_buttonMonitor, _txtAddButtonHint);
            }
            add(addButton);

            topPanel = new LayoutPanel();
            topPanel.getElement().getStyle().setBackgroundColor("#F5F5F5");
            topButtonPanel = new HorizontalPanel();
            topPanel.add(topButtonPanel);
            add(topPanel);

            newButton = new MiniBlueButton();
            newButton.setText(i18n.connectionTreePanelNewSourceText()); //$NON-NLS-1$
            enableNewButton(true);
            if (null != _buttonMonitor) {

                newButton.addHoverEventHandler(_buttonMonitor, _txtNewButtonHint);
            }
            topButtonPanel.add(newButton);

            editButton = new MiniBlueButton();
            editButton.setText(i18n.connectionTreePanelEditSourceText()); //$NON-NLS-1$
            enableEditButton(false);
            if (null != _buttonMonitor) {

                editButton.addHoverEventHandler(_buttonMonitor, _txtEditButtonHint);
            }
            topButtonPanel.add(editButton);

            deleteButton = new MiniRedButton();
            deleteButton.setText(i18n.connectionTreePanelRemoveText()); //$NON-NLS-1$
            enableDeleteButton(false);
            if (null != _buttonMonitor) {

                deleteButton.addHoverEventHandler(_buttonMonitor, _txtDeleteButtonHint);
            }
            topButtonPanel.add(deleteButton);

        } else {

            int defaultRowLimit = WebMain.getClientStartupInfo().getDefaultRowCountLimit();

            rowLimitLabel = new InlineLabel(_constants.mapper_LimitRowCount());
            rowLimitTextBox = new FilteredIntegerInput();
            rowLimitTextBox.setText(defaultRowLimit == 0 ? "" : String.valueOf(defaultRowLimit));
            add(rowLimitLabel);
            add(rowLimitTextBox);
        }
    }

    @Override
    protected void layoutDisplay() throws CentrifugeException {

        int myWidth = _width - (2 * Dialog.intMargin);
        int myWidthUnit = myWidth / 24;
        int myTop = (null != topButtonPanel) ? (Dialog.intButtonHeight + (2 * Dialog.intMargin)) : 0;

        tree.setWidth(Integer.toString(myWidth) + PX); //$NON-NLS-1$
        setWidgetTopBottom(tree, myTop, Unit.PX, Dialog.intTextBoxHeight + (2 * Dialog.intMargin), Unit.PX);
        setWidgetLeftRight(tree, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        if (null != addButton) {

            selectionBox.setPixelSize(((myWidth - ((Dialog.intMargin * 3) + _buttonWidth))),
                                        (Dialog.intMiniButtonHeight - 2)); //$NON-NLS-1$
            setWidgetBottomHeight(selectionBox, Dialog.intMargin, Unit.PX, Dialog.intMiniButtonHeight, Unit.PX);
            setWidgetLeftRight(selectionBox, Dialog.intMargin, Unit.PX, (Dialog.intMargin * 3) + _buttonWidth, Unit.PX);

            addButton.setWidth(Integer.toString(_buttonWidth) +"PX"); //$NON-NLS-1$
            setWidgetBottomHeight(addButton, Dialog.intMargin, Unit.PX, Dialog.intMiniButtonHeight, Unit.PX);
            setWidgetLeftRight(addButton, (myWidth - _buttonWidth), Unit.PX, Dialog.intMargin, Unit.PX);

        } else {

            selectionBox.setPixelSize((myWidthUnit * 12), (Dialog.intTextBoxHeight)); //$NON-NLS-1$
            setWidgetBottomHeight(selectionBox, 0, Unit.PX, (Dialog.intTextBoxHeight + 4), Unit.PX);
            setWidgetLeftWidth(selectionBox, Dialog.intMargin, Unit.PX, (myWidthUnit * 14), Unit.PX);

            rowLimitLabel.setPixelSize((myWidthUnit * 6), (Dialog.intLabelHeight));
            setWidgetBottomHeight(rowLimitLabel, 9, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetRightWidth(rowLimitLabel, (myWidthUnit * 5), Unit.PX, (myWidthUnit * 6), Unit.PX);
            rowLimitTextBox.setWidth("70px");
            setWidgetBottomHeight(rowLimitTextBox, 0, Unit.PX, (Dialog.intTextBoxHeight + 4), Unit.PX);
            setWidgetRightWidth(rowLimitTextBox, 0, Unit.PX, (myWidthUnit * 5), Unit.PX);
        }
        selectionBox.setWidgetTopBottom(selectionName, 4, Unit.PX, 4, Unit.PX);
        selectionBox.setWidgetLeftRight(selectionName, Dialog.intMiniMargin, Unit.PX, Dialog.intMiniMargin, Unit.PX);

        if (null != topPanel) {

            setWidgetTopHeight(topPanel, 0, Unit.PX, 35, Unit.PX);
            setWidgetLeftRight(topPanel, 0, Unit.PX, 0, Unit.PX);
            topPanel.setWidgetTopBottom(topButtonPanel, 0, Unit.PX, 0, Unit.PX);
            topPanel.setWidgetLeftRight(topButtonPanel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

            if (null != newButton) {

                newButton.setWidth(Integer.toString(_buttonWidth) + "PX"); //$NON-NLS-1$
//                newButton.setPixelSize(_buttonWidth, Dialog.intButtonHeight); //$NON-NLS-1$
                newButton.getElement().getStyle().setMarginRight(5, Unit.PX); //$NON-NLS-1$
                newButton.getElement().getStyle().setMarginTop(5, Unit.PX); //$NON-NLS-1$
            }

            if (null != editButton) {

                editButton.setWidth(Integer.toString(_buttonWidth) + "PX"); //$NON-NLS-1$
//                editButton.setPixelSize(_buttonWidth, Dialog.intButtonHeight); //$NON-NLS-1$
                editButton.getElement().getStyle().setMarginRight(5, Unit.PX); //$NON-NLS-1$
                editButton.getElement().getStyle().setMarginTop(5, Unit.PX); //$NON-NLS-1$
            }

            if (null != deleteButton) {

                deleteButton.setWidth(Integer.toString(_buttonWidth) + "PX"); //$NON-NLS-1$
//                deleteButton.setPixelSize(_buttonWidth, Dialog.intButtonHeight); //$NON-NLS-1$
                deleteButton.getElement().getStyle().setMarginRight(5, Unit.PX); //$NON-NLS-1$
                deleteButton.getElement().getStyle().setMarginTop(5, Unit.PX); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected void wireInHandlers() {

        tree.getSelectionModel().addSelectionChangedHandler(handleSelectionChangeEvent);
        if (null != addButton) {

            addButton.addClickHandler(handleAddItemRequest);
        }
        if (null != newButton) {

            newButton.addClickHandler(handleNewDataSourceRequest);
        }
        if (null != editButton) {

            editButton.addClickHandler(handleAddItemRequest);
        }
        if (null != deleteButton) {

            deleteButton.addClickHandler(handleDeleteDataSourceButtonClick);
        }
    }

    protected void disableInput() {

        tree.setEnabled(false);
        enableAddButton(false);
        enableNewButton(false);
        enableEditButton(false);
        enableDeleteButton(false);
    }

    protected void restrictInput() {

        if (null != rowLimitTextBox) {

            rowLimitTextBox.restrictValue();
            if (_monitoring) {

                DeferredCommand.add(new Command() {
                    public void execute() {
                        restrictInput();
                    }
                });
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void addConnection(DataSourceDef dataSourceIn) {

        _dataSourceList.add(dataSourceIn);
        ConnectionTreeItem myItem = addConnectionTreeBase(dataSourceIn);
        String myKey = myItem.key;

        _treeItemMap.put(myKey, myItem);

        myItem.retrievingChildren = true;
        retrieveCatalogList(dataSourceIn, myKey);
    }

    private void replaceConnection(DataSourceDef dataSourceIn) {

        DataSourceDef myDataSource = _sourceSelection.getDataSourceDef();

        myDataSource.setName(recoverPrefix() + myDataSource.getName());
        myDataSource.setConnection(dataSourceIn.getConnection());

        replaceDataSourceTables(_sourceSelection);
    }

    private void replaceDataSourceTables(ConnectionTreeItem parentItemIn) {

        DataSourceDef myDataSource = parentItemIn.getDataSourceDef();
        String myKey = parentItemIn.key;

        clearSelectionHandles();

        _store.removeChildren(parentItemIn);

        try {

            if (_installingTables) {

                _installingTables = false;
                _processedItem = new ConnectionTreeItem(genEmptyTable(_txtNewInstalledTable), ConnectionItemType.INSTALLER);
                _store.add(parentItemIn, _processedItem.insertParent(parentItemIn));
            }

            if (_editMode) {
                if ((!JdbcDriverType.WEB.isBaseFor(myDataSource.getConnection().getType()))
                        && ConnectorSupport.getInstance().canExecuteQuery(myDataSource)
                        && ConnectorSupport.getInstance().canEditQuery(myDataSource)) {
                    _processedItem = new ConnectionTreeItem(genEmptyCustomQuery(_txtNewCustomQuery), ConnectionItemType.CUSTOM_QUERY);
                    _store.add(parentItemIn, _processedItem.insertParent(parentItemIn));
                    _queryItems.put(parentItemIn, _processedItem);
                }
            }
        } catch (Exception myException) {

            Dialog.showException(myException);
        }
        parentItemIn.retrievingChildren = true;
        retrieveCatalogList(myDataSource, myKey);
    }

    private void clearSelectionHandles() {

        _firstTable = null;
        _focusRequest = null;
        _activeSelection = null;
        _processedItem = null;
        _itemSelection = null;
        _sourceSelection = null;
    }

    private ConnectionTreeItem addConnectionTreeBase(DataSourceDef dataSourceIn) {

        if (null != dataSourceIn) {

            String myConnectionType = DataSourceClientUtil.getConnectionTypeName(dataSourceIn);

            try {

                ConnectionTreeItem mySource = new ConnectionTreeItem(myConnectionType, ConnectionItemType.CONNECTION_TYPE);
                ConnectionTreeItem myConnection = new ConnectionTreeItem(dataSourceIn, ConnectionItemType.DATA_SOURCE_DEF);

                if (null != myConnection) {

                    _store.add(myConnection.insertParent(mySource));

                    if (DataSourceClientUtil.isInstalledTable(dataSourceIn)) {

                        _processedItem = new ConnectionTreeItem(genEmptyTable(_txtNewInstalledTable), ConnectionItemType.INSTALLER);
                        _store.add(myConnection, _processedItem.insertParent(myConnection));
                    }

                    if (_editMode) {

                        if ((!JdbcDriverType.WEB.isBaseFor(dataSourceIn.getConnection().getType()))
                                && ConnectorSupport.getInstance().canExecuteQuery(dataSourceIn)
                                && ConnectorSupport.getInstance().canEditQuery(dataSourceIn)) {

                            _processedItem = new ConnectionTreeItem(genEmptyCustomQuery(_txtNewCustomQuery), ConnectionItemType.CUSTOM_QUERY); //$NON-NLS-1$

                            _store.add(myConnection, _processedItem.insertParent(myConnection));

                            _queryItems.put(myConnection, _processedItem);
                        }
                    }
                }

                return myConnection;

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
        return null;
    }

    private SqlTableDef genEmptyCustomQuery(String tableNameIn) {

        SqlTableDef myEmptyTable = new SqlTableDef();

        myEmptyTable.setTableName(tableNameIn);
        myEmptyTable.setIsCustom(true);

        return myEmptyTable;
    }

    private SqlTableDef genEmptyTable(String tableNameIn) {

        SqlTableDef myEmptyTable = new SqlTableDef();

        myEmptyTable.setTableName(tableNameIn);
        myEmptyTable.setIsCustom(false);

        return myEmptyTable;
    }

    private void retrieveCatalogList(DataSourceDef dataSourceIn, String keyIn) {

        ConnectionDef myConnection = (null != dataSourceIn) ? dataSourceIn.getConnection() : null;

        if (null != myConnection) {

            VortexFuture<Response<String, List<String>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            _requestCount++;
            watchBox.show(_txtWaitingForTableList);

            try {
                //
                // Request a list of tables within the data source from the server
                //
                AuthorizationObject.updateConnectionCredentials(dataSourceIn, _authorizationMap);
                myVortexFuture.execute(TestActionsServiceProtocol.class).listCatalogs(myConnection, keyIn);
                myVortexFuture.addEventHandler(handleCatalogListResponse);

            } catch (Exception myException) {

                watchBox.hide();
                Dialog.showException(myException);
            }
        }
    }

    private void retrieveSchemaList(DataSourceDef dataSourceIn, String catalogIn, String keyIn) {

        ConnectionDef myConnection = (null != dataSourceIn) ? dataSourceIn.getConnection() : null;

        if (null != myConnection) {

            VortexFuture<Response<String, List<String>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            _requestCount++;
            watchBox.show(_txtWaitingForTableList);

            try {
                //
                // Request a list of tables within the data source from the server
                //
                AuthorizationObject.updateConnectionCredentials(dataSourceIn, _authorizationMap);
                myVortexFuture.execute(TestActionsServiceProtocol.class).listSchemas(myConnection, catalogIn, keyIn);
                myVortexFuture.addEventHandler(handleSchemaListResponse);

            } catch (Exception myException) {

                watchBox.hide();
                Dialog.showException(myException);
            }
        }
    }

    private void retrieveTableTypeList(DataSourceDef dataSourceIn, String catalogIn, String schemaIn, String keyIn) {

        ConnectionDef myConnection = (null != dataSourceIn) ? dataSourceIn.getConnection() : null;

        if (null != myConnection) {

            VortexFuture<Response<String, List<String>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            _requestCount++;
            watchBox.show(_txtWaitingForTableList);

            try {
                //
                // Request a list of tables within the data source from the server
                //
                AuthorizationObject.updateConnectionCredentials(dataSourceIn, _authorizationMap);
                myVortexFuture.execute(TestActionsServiceProtocol.class).listTableTypes(myConnection, catalogIn,
                        schemaIn, keyIn);
                myVortexFuture.addEventHandler(handleTableTypeListResponse);

            } catch (Exception myException) {

                watchBox.hide();
                Dialog.showException(myException);
            }
        }
    }

    private void retrieveTableList(DataSourceDef dataSourceIn, String catalogIn, String schemaIn, String typeIn, String keyIn) {

        ConnectionDef myConnection = (null != dataSourceIn) ? dataSourceIn.getConnection() : null;

        if (null != myConnection) {

            VortexFuture<Response<String, List<SqlTableDef>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            _requestCount++;
            watchBox.show(_txtWaitingForTableList);

            try {
                //
                // Request a list of tables within the data source from the server
                //
                AuthorizationObject.updateConnectionCredentials(dataSourceIn, _authorizationMap);
                myVortexFuture.execute(TestActionsServiceProtocol.class).listTableDefs(myConnection, catalogIn,
                        schemaIn, typeIn, keyIn);
                myVortexFuture.addEventHandler(handleTableListResponse);

            } catch (Exception myException) {

                watchBox.hide();
                Dialog.showException(myException);
            }
        }
    }

    private void updateParameters(List<QueryParameterDef> parametersIn) {

        if (null != _dataSetParameters) {

            _dataSetParameters = parametersIn;

        } else if (null != _model) {

            _model.replaceQueryParameters(parametersIn);
        }
    }

    private void processSelection(ConnectionTreeItem selectionIn) {

        _activeSelection = selectionIn;

        DataSourceDef myDataSource = getDataSource(_activeSelection);
/*
        if (myDataSource.isInPlace() && (null == _keyField)) {

            _queryDialog = null;
            promptForIdentityField();

        } else {

        }
*/
        processTableRequest();
    }

    private void validateSelection(List<ConnectionTreeItem> listIn) {

        boolean myValidItemButton = false;
        boolean myValidItemFlag = false;
        boolean myLimitedSourceFlag = false;
        boolean myValidSourceFlag = false;
        ConnectionTreeItem myItem = null;
        DataSourceDef myDataSource;

        if ((null != listIn) && (0 < listIn.size())) {

            myItem = listIn.get(0);
            myDataSource = myItem.getDataSourceDef();

            myValidItemButton = ConnectionItemType.INSTALLER.equals(myItem.type);

            myValidItemFlag = (ConnectionItemType.TABLE.equals(myItem.type))
                    || (ConnectionItemType.CUSTOM_QUERY.equals(myItem.type));

            myLimitedSourceFlag = (null != _addButtonHandler)
                    && (ConnectionItemType.DATA_SOURCE_DEF.equals(myItem.type))
                    && (null != myDataSource)
                    && (null != myDataSource.getConnection());

            myValidSourceFlag = myLimitedSourceFlag && ((!_safeMode) || (!myDataSource.hasChildren()));
        }

        if (myValidItemFlag || myValidSourceFlag || myLimitedSourceFlag) {

            _itemSelection = myItem;
            selectionName.setText(_itemSelection.getName());

        } else if (myValidItemButton) {

            _itemSelection = myItem;

        } else {

            _itemSelection = null;
            selectionName.setText(null);
        }

        if (myLimitedSourceFlag) {

            _sourceSelection = myItem;

        } else {

            _sourceSelection = null;
        }

        if (null != addButton) {

            enableAddButton(myValidItemFlag);
            addButton.setText(i18n.connectionTreePanelAddButtonText()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        enableEditButton(myValidSourceFlag);
        enableDeleteButton(myValidSourceFlag);

        if (myValidItemFlag != _validFlag) {

            _validFlag = myValidItemFlag;

            fireEvent(new ValidityReportEvent(_validFlag, myItem));
        }
    }

    private void initializePrefix() {

        if ((null != _dataSourceList) &&(0 < _dataSourceList.size())) {

            for (DataSourceDef mySource : _dataSourceList) {

                Integer myPrefix = extractPrefix(mySource.getName());

                if ((null != myPrefix) && (myPrefix > _prefix)) {

                    _prefix = myPrefix;
                }
            }
        }
    }

    private Integer extractPrefix(String nameIn) {

        Integer myValue = null;

        if ((null != nameIn) && (2 < nameIn.length() && (' ' == nameIn.charAt(2))
                && ('0' <= nameIn.charAt(0)) && ('9' >= nameIn.charAt(0))
                && ('0' <= nameIn.charAt(1)) && ('9' >= nameIn.charAt(1)))) {

            myValue = ((nameIn.charAt(0) - '0') * 10) + (nameIn.charAt(1) - '0');
        }
        return myValue;
    }

    private String getPrefix() {

        _prefix++;
        return Integer.toString(_prefix / 10) + Integer.toString(_prefix % 10) + " "; //$NON-NLS-1$
    }

    private void storePrefix(String nameIn) {

        if ((null != nameIn) && (2 < nameIn.length())) {

            _activePrefix = nameIn.substring(0, 3);

        } else {

            _activePrefix = "   "; //$NON-NLS-1$
        }
    }

    private String recoverPrefix() {

        return _activePrefix;
    }

    private int enableInput(int counterIn) {

        int myCounter = (0 < counterIn) ? (counterIn - 1) : 0;

        if ((!watchBox.active()) && (0 == myCounter)) {

            enableInput();
        }
        return myCounter;
    }

    private void processQueryRequest() {

        SqlTableDef myTable = _queryDialog.getTable();

        if (null != myTable) {

            DataSourceDef myDataSource = _queryDialog.getDataSource();

            updateParameters(_queryDialog.getDataSetParameters());

            if (null != _addButtonHandler) {

                _addButtonHandler.onTreeSelection(new TreeSelectionEvent<ConnectionTreeItem,
                        SqlTableDef, DataSourceDef>(_itemSelection, myTable, myDataSource));
            }

            _queryDialog.destroy();

        } else {

            _queryDialog = null;
            enableInput();
        }
    }

    private void processTableRequest() {

        SqlTableDef myTable = getSqlTable(_activeSelection);
        DataSourceDef myDataSource = getDataSource(_activeSelection);

        myTable = (SqlTableDef) myTable.clone(true);

        myTable.setSource(myDataSource);
        myTable.setLocalId(UUID.randomUUID());

        if (null != _addButtonHandler) {

            _addButtonHandler.onTreeSelection(new TreeSelectionEvent<ConnectionTreeItem,
                    SqlTableDef, DataSourceDef>(_activeSelection, myTable, myDataSource));
        } else {

            enableInput();
        }
        _processedItem = _activeSelection;
    }

    /*
        private void promptForIdentityField() {

            try {

                Button myActionButton;
                Button myCancelButton;
                IntegerInputWidget myWidget = new IntegerInputWidget(i18n.connectionTreePanelInstructionText(), "0", true); //$NON-NLS-1$ //$NON-NLS-2$
                List<AbstractInputWidget> myList = new ArrayList<AbstractInputWidget>();

                identityPromptDialog = new ValidatingDialog(myList);

                SingleEntryWizardPanel myPanel = new SingleEntryWizardPanel(identityPromptDialog, "", myWidget, ""); //$NON-NLS-1$ //$NON-NLS-2$

                myPanel.setPixelSize(500,300);
                myList.add(myWidget);

                identityWidget = myWidget;

                identityPromptDialog.defineHeader("Identify Identity Field", (String)null, true); //$NON-NLS-1$
                identityPromptDialog.add(myPanel);
                identityPromptDialog.getActionButton().setText(Dialog.txtCreateButton);

                myActionButton = identityPromptDialog.getActionButton();
                myCancelButton = identityPromptDialog.getCancelButton();
                myActionButton.setText(Dialog.txtContinueButton);
                myActionButton.addClickHandler(handleIdentityFieldContinueButtonClick);
                myCancelButton.addClickHandler(handleIdentityFieldCancelButtonClick);

                identityPromptDialog.show(70);


            } catch (Exception myException) {

            }
        }
    */
    private void resetAll() {

        clear();

        topButtonPanel = null;
        tree = null;
        selectionBox = null;
        deleteButton = null;
        addButton = null;
        newButton = null;

        identityPromptDialog = null;
        identityWidget = null;

        _iconProvider = new ConnectionTreeIconProvider();

        _valueProvider = new ConnectionTreeValueProvider();
        _keyProvider = new ConnectionTreeModelKeyProvider();


        _store = null;

        _addButtonHandler = null;
        _validFlag = false;

        _queryNameMapProvider = null;

        _firstTable = null;
        _focusRequest = null;
        _activeSelection = null;
        _processedItem = null;
        _itemSelection = null;
        _sourceSelection = null;
        _model = null;
        _dataSourceList = null;
        _dataSetParameters = null;
        _parameterPresenter = null;
        _authorizationMap = null;
        _queryItems = new HashMap<ConnectionTreeItem, ConnectionTreeItem>();

        _buttonWidth = 70;

        _queryDialog = null;
        _prefix = 0;
        _activePrefix = "  "; //$NON-NLS-1$
        _requestCount = 0;
        _editMode = true;
        _isNewTable = false;
        _buttonMonitor = null;
        _tableCallBack = null;
        _browseHandler = null;
        _highLightList = null;

        watchBox.hide();
    }

    private void focusItem(final ConnectionTreeItem itemIn) {

        if (null != itemIn) {

            ConnectionTreeItem myParent = itemIn.getParent();

            if (null != myParent) {

                tree.setExpanded(myParent, true, false);
            }
            tree.getSelectionModel().select(itemIn, false);

            DeferredCommand.add(new Command() {
                public void execute() {
                    tree.scrollIntoView(itemIn);
                }
            });
        }
    }

    private void expandItem(final ConnectionTreeItem itemIn) {

        if (null != itemIn) {

            if (itemIn.canHaveChildren()) {

                tree.setExpanded(itemIn, true, false);
            }

            tree.getSelectionModel().select(itemIn, false);

            DeferredCommand.add(new Command() {
                public void execute() {
                    tree.scrollIntoView(itemIn);
                }
            });
        }
    }

    private void reformatDataSource(List<String> highLightListIn) {

        if ((null != highLightListIn) && (null != _itemSelection)) {

            _installingTables = installerSelected();
            ConnectionTreeItem mySourceSelection = _itemSelection.getParent();

            if (null != mySourceSelection) {

                try {

                    _highLightList = highLightListIn;
                    replaceDataSourceTables(mySourceSelection);

                } catch (Exception myException) {

                    Display.error(myException.getMessage());
                }
            }
        }
    }

    private void addChildren(final ConnectionTreeItem parentIn, ConnectionItemType childTypeIn, List<String> childListIn) {

        if ((null != childListIn) && (0 < childListIn.size())) {

            try {

                for (String myName : childListIn) {

                    ConnectionTreeItem myItem = new ConnectionTreeItem(myName, childTypeIn);
                    _treeItemMap.put(myItem.key, myItem);
                    _store.add(parentIn, myItem.insertParent(parentIn));
                }

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
        parentIn.hasAllChildren = true;
        parentIn.retrievingChildren = false;
        displayChildren(parentIn);
    }

    private void addChildren(ConnectionTreeItem parentIn, List<SqlTableDef> tableListIn) {

        if ((null != tableListIn) && (0 < tableListIn.size())) {

            try {

                for (SqlTableDef myTable : tableListIn) {

                    ConnectionTreeItem myItem = new ConnectionTreeItem(myTable, ConnectionItemType.TABLE);
                    _treeItemMap.put(myItem.key, myItem);
                    _store.add(parentIn, myItem.insertParent(parentIn));
                }

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
        parentIn.hasAllChildren = true;
        parentIn.retrievingChildren = false;
        displayChildren(parentIn);
    }

    private void deleteBranch(ConnectionTreeItem itemIn) {

        DataSourceDef mySource = itemIn.getDataSourceDef();
        String myKey = itemIn.key;

        // Remove children first

        // remove item last
        _queryItems.remove(itemIn);
        _store.remove(itemIn);

        if (null != mySource) {

            _dataSourceList.remove(mySource);
        }
    }

    private void displayChildren(final ConnectionTreeItem parentIn) {

        if (null != parentIn) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    expandItem(parentIn);
                }
            });
        }
    }

    private void processSelection() {

        //
        // Disable the add button
        //
        enableAddButton(false);

        if (null != _sourceSelection) {

            handleDataSourceEditButtonClick.onClick(null);

        } else if (null != _itemSelection) {

            if (ConnectionItemType.INSTALLER.equals(_itemSelection.type)) {

                // Install one or more new tables
                if (null != _browseHandler) {

                    _browseHandler.onClick(null);
                }

            } else if (ConnectionItemType.TABLE.equals(_itemSelection.type)) {

                SqlTableDef myTable = getSqlTable();
                List<ColumnDef> myList = myTable.getColumns();

                if ((null != myList) && (0 < myList.size())) {

                    watchBox.show(_txtWaitingForColumnList);

                    DeferredCommand.add(new Command() {
                        public void execute() {
                            processSelection(_itemSelection);
                        }
                    });

                } else {

                    loadTableChildren(_itemSelection, handleAddingTableToDisplay);
                }

            } else if (ConnectionItemType.CUSTOM_QUERY.equals(_itemSelection.type)) {

                DataSourceDef myDataSource = getDataSource();

                if (ConnectorSupport.getInstance().canExecuteQuery(myDataSource) && ConnectorSupport.getInstance().canEditQuery(myDataSource)) {

                    Map<String, Boolean> myNameMap;

                    if (null != _model) {

                        myNameMap = _model.getQueryNameMap();
                        _parameterPresenter = _model.createParameterPresenter();

                    } else {

                        myNameMap = new HashMap<String, Boolean>();
                        _parameterPresenter = new ParameterPresenter(_dataSetParameters, null, null);
                    }
                    _queryDialog = new CustomQueryDialog(_authorizationMap, myDataSource, genEmptyCustomQuery("Query"), //$NON-NLS-1$
                            _parameterPresenter, handleCreateQuerySuccess, "Create", myNameMap); //$NON-NLS-1$
                    _queryDialog.show();

                } else {

                    Display.error(_constants.dataSourceEditor_CustomQuery(), _constants.blockingCustomQuery());
                }

            } else if (ConnectionItemType.INSTALLER.equals(_itemSelection.type)) {
            }
        }
    }

    private void enableDeleteButton(boolean isEnabledIn) {

        if (null != deleteButton) {

            deleteButton.setEnabled(isEnabledIn);
        }
    }

    private void enableEditButton(boolean isEnabledIn) {

        if (null != editButton) {

            editButton.setEnabled(isEnabledIn);
        }
    }

    private void enableAddButton(boolean isEnabledIn) {

        if (null != addButton) {

            addButton.setEnabled(isEnabledIn);
        }
    }

    private void enableNewButton(boolean isEnabledIn) {

        if (null != newButton) {

            newButton.setEnabled(isEnabledIn);
        }
    }

    private void sourceListChanged() {

        if (null != _sourceListChangeMonitor) {

            _sourceListChangeMonitor.onSourceListChange();
        }
    }

    private <T> void showLogon(DataSourceDef dataSourceIn, UserInputEventHandler<T> handlerIn, T itemIn) {

        if (ConnectorSupport.getInstance().hasHiddenParameters(dataSourceIn)) {

            Dialog.showError(_constants.restrictedCredentialsTitle(), _constants.restrictedCredentialsInfo());

        } else {

            AuthorizationObject myCredential = new AuthorizationObject(_authorizationMap, dataSourceIn);

            Dialog.showLogon(myCredential, handlerIn, itemIn);
        }
    }
}
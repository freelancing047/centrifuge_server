package csi.client.gwt.edit_sources.dialogs.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataSourceUtilities;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.edit_sources.dialogs.common.QueryParameterDefReference;
import csi.client.gwt.edit_sources.dialogs.common.QueryParameterNameCell;
import csi.client.gwt.edit_sources.dialogs.parameter.InputParameterListDialog;
import csi.client.gwt.csiwizard.wizards.ParameterWizard;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.lib.ace.src.AceEditor;
import csi.client.gwt.lib.ace.src.AceEditorMode;
import csi.client.gwt.lib.ace.src.AceEditorTheme;
import csi.server.common.util.ConnectorSupport;
import csi.client.gwt.util.Display;
import csi.server.common.util.AuthorizationObject;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.boot.SuccessDialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.CustomQueryDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.TestQueryResponse;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.service.api.TestActionsServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 */
public final class CustomQueryDialog {

    interface CustomQueryDialogUiBinder extends UiBinder<ValidatingDialog, CustomQueryDialog> {
    }

    interface QueryParameterDefPropertyAccess extends PropertyAccess<QueryParameterDefReference> {

        @Path("parameter.uuid")
        public ModelKeyProvider<QueryParameterDefReference> key();

        public ValueProvider<QueryParameterDefReference, QueryParameterDef> parameter();

        public ValueProvider<QueryParameterDefReference, String> prompt();

        public ValueProvider<QueryParameterDefReference, String> type();

    }
    

    // GridFactory needs a void ValueProvider if a Grid doesn't correspond to a row.  
    // It cannot be created via PropertyAccess<QueryParameterDefReference> unless a 
    // This method is used for creating the icon to insert the parameter into the text editor box.
    private ValueProvider<QueryParameterDefReference, Void> voidValueProvider = new ValueProvider<QueryParameterDefReference, Void>(){

        @Override
        public Void getValue(QueryParameterDefReference object) {
            return null;
        }

        @Override
        public void setValue(QueryParameterDefReference object, Void value) {
            //no op
            
        }

        @Override
        public String getPath() {
            return "";
        }
        
    };

    private boolean _forceTest = true;
    private String _testedQuery =  null;
    private boolean _testSuccessful = false;

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final static String _txtParameterTestTitle = _constants.parameterTestTitle();
    private final static String _txtParameterTestHelp = _constants.parameterTestHelpTarget();
    private final static String _txtParameterTestButton = Dialog.txtTestButton;
    private final static String _txtParameterSaveButton = Dialog.txtSaveButton;
    private final static String _txtMissingParameters = _constants.parameterMissingList();

    private static final QueryParameterDefPropertyAccess propertyAccess = GWT
            .create(QueryParameterDefPropertyAccess.class);
    private static final CustomQueryDialogUiBinder uiBinder = GWT.create(CustomQueryDialogUiBinder.class);
    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private Grid<QueryParameterDefReference> grid;
    private ValidatingDialog dialog;
    private HasText sqlEditor;
    private List<LaunchParam> _launchParameters = null;
    private ParameterPresenter _parameterPresenter = null;
    private Map<String, AuthDO> _authorizationMap = null;
    private ConnectionDef _connection = null;
    private QueryDef _query = new QueryDef();
    private Map<String, Boolean> _nameMap;
    
    private DataSourceDef _dataSource;
    private SqlTableDef _table = null;
    
    private boolean _exitProcess = false;
    private WatchBoxInterface watchBox = WatchBox.getInstance();
    private ClickHandler _handler = null;
    private List<QueryParameterDef> _testParameters =  null;
    private boolean _allowQuery = false;
    private boolean _hadQuery = false;
    private QueryDef _priorQuery = null;

    @UiField
    GridContainer gridContainer;

    @UiField
    SimplePanel editorContainer;

    @UiField
    Button inputParametersButton;

    @UiField
    FilteredTextBox queryName;

    @UiField
    Button testQueryButton;
    @UiField
    TextArea instructionTextArea;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    UserInputEventHandler<List<LaunchParam>> callBackHandler
    = new UserInputEventHandler<List<LaunchParam>>() {
        
        public void onUserInput(UserInputEvent<List<LaunchParam>> eventIn) {

            try {

                if (eventIn.isCanceled()) {

                    watchBox.hide();
                    _exitProcess = false;

                } else {

                    _launchParameters = eventIn.getKey();
                    testQuery();
                }

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 1, myException);
            }
        }
    };

    private ClickHandler handleCancelButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                _table.setIsCustom(_hadQuery);
                _table.setCustomQuery(_priorQuery);
                _parameterPresenter.addTableItems(_table);
                _table = null;
                _handler.onClick(null);

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 2, myException);
            }
        }
    };

    private ClickHandler handleTestButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                _exitProcess = false;
                watchBox.show(_constants.testingCustomQuery());
                _launchParameters = null;

                verifyParameters();

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 3, myException);
            }
        }
    };

    private ClickHandler handleCreateButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if (_forceTest && ((!_testSuccessful) || (!sqlEditor.getText().equals(_testedQuery)))) {

                    _exitProcess = true;
                    watchBox.show(_constants.verifyingCustomQuery());
                    _launchParameters = null;

                    verifyParameters();

                } else {

                    _table = DataSourceUtilities.finalizeSqlTable(_dataSource, _table);
                    _parameterPresenter.addTableItems(_table);
                    if (null != _handler) {

                        _handler.onClick(null);
                    }
                }

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 4, myException);
            }
        }
    };
    
    private VortexEventHandler<List<String>> handleParameterListResponse
    = new AbstractVortexEventHandler<List<String>>() {
        @Override
        public boolean onError(Throwable myException) {
            
            watchBox.hide();
            _exitProcess = false;
            
            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(List<String> listIn) {

            try {

                _query.setParameters(null);
                _testParameters = null;

                if ((null != listIn) && (0 < listIn.size())) {

                    if (mergeParameters(listIn)) {

                        retrieveParameterValues(_testParameters);
                    }

                } else {

                    testQuery();
                }

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 5, myException);
            }
        }
    };
    
    private UserInputEventHandler<Integer> processLogon
    = new UserInputEventHandler<Integer>() {
        
        public void onUserInput(UserInputEvent<Integer> eventIn) {

            try {

                if (!eventIn.isCanceled()) {

                    testQuery();
                }

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 6, myException);
            }
        }
    };

    private VortexEventHandler<TestQueryResponse> handleQueryTestResponse
            = new AbstractVortexEventHandler<TestQueryResponse>() {
        @Override
        public boolean onError(Throwable myException) {

            watchBox.hide();
            _exitProcess = false;

            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(TestQueryResponse responseIn) {

            try {

                watchBox.hide();

                if (responseIn.isRequiresAuth()) {

                    if (ConnectorSupport.getInstance().hasHiddenParameters(_dataSource)) {

                        Dialog.showError(_constants.restrictedCredentialsTitle(), _constants.restrictedCredentialsInfo());

                    } else {

                        Dialog.showLogon(new AuthorizationObject(_authorizationMap, _dataSource), processLogon);
                    }

                } else if (responseIn.getSuccess()) {

                    _testedQuery = sqlEditor.getText();
                    _table = DataSourceUtilities.finalizeSqlTable(_dataSource, responseIn.getTableDef());
                    if (_exitProcess) {

                        _parameterPresenter.addTableItems(_table);
                        if (null != _handler) {

                            _handler.onClick(null);
                        }

                    } else {

                        (new SuccessDialog(i18n.customQueryDialog_TestQuery_Success_Title(),
                                i18n.customQueryDialog_TestQuery_Success_Message())).show();
                    }
                    _exitProcess = false;

                } else {

                    (new ErrorDialog(i18n.customQueryDialog_TestQuery_Failed_Title(),
                            i18n.customQueryDialog_TestQuery_Failed_Message(responseIn.getErrorMsg()))).show();
                    _exitProcess = false;
                }

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 7, myException);
            }
        }
    };

    private VortexEventHandler<QueryDef> handleCreateQueryResponse
            = new AbstractVortexEventHandler<QueryDef>() {
        @Override
        public boolean onError(Throwable myException) {

            watchBox.hide();
            _exitProcess = false;

            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(QueryDef responseIn) {

            try {

                watchBox.hide();
                _query = responseIn;
                if (_query != null) {
                    sqlEditor.setText(_query.getSql());
                }

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 8, myException);
            }
        }
    };

    private AttachEvent.Handler handleInitialAttachment
    = new AttachEvent.Handler() {
        @Override
        public final void onAttachOrDetach(final AttachEvent event) {

            try {

                // only run on attach.
                if (event.isAttached()) {
                    final AceEditor editor = new AceEditor();
                    sqlEditor = editor;

                    editorContainer.add(editor);
                    editor.startEditor();
                    editor.setHeight("100%");
                    editor.setHScrollBarAlwaysVisible(true);
                    editor.setShowGutter(false);
                    editor.setShowPrintMargin(false);
                    editor.setMode(AceEditorMode.SQL);
                    editor.setTheme(AceEditorTheme.SOLARIZED_LIGHT);
                    if (_query != null) {
                        sqlEditor.setText(_query.getQueryText());
                    }

                    editor.setRequired(true);
                    dialog.addObject(editor, true);
                }

            } catch (Exception myException) {

                Display.error("CustomQueryDialog", 9, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CustomQueryDialog(Map<String, AuthDO> authorizationMapIn,
                        DataSourceDef dataSourceIn, SqlTableDef tableIn,
                        ParameterPresenter parameterPresenterIn,
                        ClickHandler handlerIn, String buttonTextIn, Map<String, Boolean> mapIn) {

        try {

            _allowQuery = ConnectorSupport.getInstance().canExecuteQuery(dataSourceIn) && ConnectorSupport.getInstance().canEditQuery(dataSourceIn);

            if (_allowQuery) {

                _authorizationMap = authorizationMapIn;
                _dataSource = dataSourceIn;
                _table = tableIn;
                _parameterPresenter = parameterPresenterIn;
                _nameMap = mapIn;
                _hadQuery = _table.getIsCustom();
                _priorQuery = _table.getCustomQuery();

                init(dataSourceIn.getConnection(), _table.getCustomQuery(), handlerIn, buttonTextIn);

                parameterPresenterIn.removeTableItems(_table);

                if (!_table.getIsCustom()) {

                    buildInitialQuery();
                }
            }

        } catch (Exception myException) {

            Display.error("CustomQueryDialog", 10, myException);
        }
    }


    
    public Button getExecuteButton() {
        
        return dialog.getActionButton();
    }
    
    public void destroy() {
        
        dialog.hide();
    }
    
    public DataSourceDef getDataSource() {
        
        return _dataSource;
    }
    
    public SqlTableDef getTable() {
        /*
        if (null !=  _table) {
            
            _table.setCustomQuery(getQuery());
            _table.setTableName(queryName.getText());
        }
        */
        return _table;
    }

    public List<QueryParameterDef> getDataSetParameters() {
        
        return _parameterPresenter.getParameters();
    }

    /**
     * Common Initialization
     * 
     */
    private final void init(ConnectionDef connectionIn, QueryDef existingQuery, ClickHandler handlerIn, String buttonTextIn) {

        _connection = connectionIn;
        _query = existingQuery;
        _handler = handlerIn;

        dialog = uiBinder.createAndBindUi(this);
        dialog.hideOnCancel();

        instructionTextArea.setReadOnly(true);
        instructionTextArea.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        instructionTextArea.getElement().getStyle().setProperty("resize", "none");
        instructionTextArea.getElement().getStyle().setBackgroundColor("white");
        instructionTextArea.getElement().getStyle().setBorderColor("white");
        instructionTextArea.getElement().getStyle().setColor(Dialog.txtInfoColor);
        instructionTextArea.setText(i18n.customQueryInstructions(i18n.inputParameters(), "{:param}", "{:p1}"));

        if (null == _query) {
            dialog.defineHeader(i18n.customQueryDialog_CreateTitle(), i18n.customQueryDialog_CreateHelpTarget(), true);
        } else {
            String myName = _query.getName();
            dialog.defineHeader(i18n.customQueryDialog_EditTitle(), i18n.customQueryDialog_EditHelpTarget(), true);
            queryName.setText(myName);
            if (null != myName) {

                String myTestName = myName.toLowerCase();
                if ((null != _nameMap) && (_nameMap.containsKey(myTestName))) {

                    _nameMap.remove(myTestName);
                }
            }
        }
        dialog.getActionButton().setText(buttonTextIn);

        queryName.setRejectionMap(_nameMap);
        queryName.setMode(ValidityCheckCapable.Mode.LOWERCASE);
        queryName.setRequired(true);
        dialog.addObject(this.queryName, true);

        createGrid();
        addHandlers();
    }

    /**
     * Create & Initialize the Query Input Parameters Grid
     */
    private final void createGrid() {
        @SuppressWarnings("unchecked")
        final GridComponentManager<QueryParameterDefReference> manager = WebMain.injector.getGridFactory().create(
                propertyAccess.key());

        final ColumnConfig<QueryParameterDefReference, QueryParameterDef> nameCol = manager.create(
                propertyAccess.parameter(), 125, i18n.customQueryDialog_GridCol_Name(), true, true);
        nameCol.setCell(new QueryParameterNameCell());
        nameCol.setComparator(QueryParameterDefReference.getComparator());

        final ColumnConfig<QueryParameterDefReference, String> promptCol = manager.create(propertyAccess.prompt(), 260,
                i18n.customQueryDialog_GridCol_Prompt(), true, true);
        promptCol.setCell(new CsiTitleCell());

        final ColumnConfig<QueryParameterDefReference, String> typeCol = manager.create(propertyAccess.type(), 60,
                i18n.customQueryDialog_GridCol_Type(), true, true);
        typeCol.setCell(new CsiTitleCell());

        final ColumnConfig<QueryParameterDefReference, Void> pasteCol = manager.create(voidValueProvider, 15, "",
                false, true);
        pasteCol.setCell(new IconCell(IconType.PASTE));
        pasteCol.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.customQueryDialog_GridCol_Paste_Tooltip())
                .toSafeHtml());
        pasteCol.setResizable(false);

        final List<ColumnConfig<QueryParameterDefReference, ?>> columns = manager.getColumnConfigList();
        final ColumnModel<QueryParameterDefReference> cm = new ColumnModel<QueryParameterDefReference>(columns);
        final ListStore<QueryParameterDefReference> gridStore = manager.getStore();

        grid = new ResizeableGrid<QueryParameterDefReference>(gridStore, cm);
        grid.getView().setAutoExpandColumn(nameCol);
        grid.getStore().setAutoCommit(true);
        GridHelper.setDraggableRowsDefaults(grid);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.addCellDoubleClickHandler(new CellDoubleClickHandler() {

            @Override
            public final void onCellClick(CellDoubleClickEvent event) {
                handleGridClickEvent(event.getRowIndex(), event.getCellIndex(), false, pasteCol);
            }
        });
        /*
        grid.addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                handleGridClickEvent(event.getRowIndex(), event.getCellIndex(), true, pasteCol);
            }
        });
        */
        gridContainer.setGrid(grid);

        for (QueryParameterDef param : _parameterPresenter.getParameters()) {
            grid.getStore().add(new QueryParameterDefReference().setParameter(param));
        }
    }

    /**
     * Handle Click Events on the Query Input Parameters Grid
     * @param rowIndex
     * @param colIndex
     * @param enforceColumn
     * @param pasteCol
     */
    private void handleGridClickEvent(final int rowIndex, final int colIndex, final boolean enforceColumn,
            final ColumnConfig<QueryParameterDefReference, Void> pasteCol) {
        if (enforceColumn) {
            if (colIndex != grid.getColumnModel().indexOf(pasteCol)) {
                return;
            }
        }

        final QueryParameterDefReference currentRow = grid.getStore().get(rowIndex);
        final StringBuilder sqlQueryText = new StringBuilder(sqlEditor.getText());
        if (sqlQueryText.length() > 0) {
            sqlQueryText.append(" ");
        }
        sqlQueryText.append("{:").append(currentRow.getParameter().getName()).append("}");
        sqlEditor.setText(sqlQueryText.toString());
    }
    
    private void addHandlers() {

        testQueryButton.addClickHandler(handleTestButtonClick);
        dialog.getActionButton().addClickHandler(handleCreateButtonClick);
        dialog.getCancelButton().addClickHandler(handleCancelButtonClick);
        
        // Ace Editor can only be initialized once it has been attached to the DOM tree.
        dialog.addAttachHandler(handleInitialAttachment);
    }

    public QueryDef getQuery() {
        String name = queryName.getValue();
        String sqlText = sqlEditor.getText();

        // generate a default name for the string
        if (name == null || name.isEmpty()) {
            if (15 < sqlText.length()) {
                
                name = sqlText.substring(0, 15);
                
            } else {
                
                name = sqlText;
            }
        }
        if (null == _query) {
            
            _query = new QueryDef();
        }
        _query.setName(name);
        _query.setQueryText(sqlText);

        return _query;
    }

    public void show() {

        if (_allowQuery) {

            dialog.show();
            dialog.setFocus(queryName);

        } else {

            Display.error(_constants.dataSourceEditor_CustomQuery(),
                    _constants.blockingCustomQuery(), handleCancelButtonClick);
        }
    }

    @UiHandler("inputParametersButton")
    public void handleInputParameterClick(final ClickEvent event) {
        final ParameterPresenter myChildPresenter = new ParameterPresenter(_parameterPresenter);
        final InputParameterListDialog inputParameterListDialog = new InputParameterListDialog(myChildPresenter);
        inputParameterListDialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                _parameterPresenter.replaceAll(myChildPresenter);
                grid.getStore().clear();
                for (QueryParameterDef param : _parameterPresenter.getParameters()) {
                    grid.getStore().add(new QueryParameterDefReference().setParameter(param));
                }
            }
        });
        inputParameterListDialog.show();
    }
    
    private void verifyParameters() {
        
        VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            //
            // Request a list of parameters used by the query from the server
            //
            myVortexFuture.addEventHandler(handleParameterListResponse);
            myVortexFuture.execute(TestActionsServiceProtocol.class).listQueryParameters(getQuery());

        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }
    
    private boolean mergeParameters(List<String> listIn) {
        
        List<String> myFailureList = new ArrayList<String>();

        if ((null != listIn) && (0 < listIn.size())) {

            for (String myTest : listIn) {
                
                if (!matchParameter(myTest)) {
                    
                    myFailureList.add(myTest);
                }
            }
        }
        if (0 < myFailureList.size()){
            
            StringBuilder myBuffer = new StringBuilder();
            
            myBuffer.append(_txtMissingParameters);
            
            for (int i = 0; myFailureList.size() > i; i++) {
                
                myBuffer.append("\n" + myFailureList.get(i));
            }
            
            Display.error(myBuffer.toString());
            
            watchBox.hide();
            _exitProcess = false;
        }
        return (0 == myFailureList.size());
    }
    
    private void testQuery() {

        VortexFuture<TestQueryResponse> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            
            CustomQueryDO myRequest = new CustomQueryDO(_connection, getQuery(), _testParameters, _launchParameters);
            //
            // Request a list of parameters used by the query from the server
            //
            myVortexFuture.addEventHandler(handleQueryTestResponse);
            myVortexFuture.execute(TestActionsServiceProtocol.class).testCustomQuery(myRequest);

        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }
    
    private boolean matchParameter(String parameterNameIn) {
        
        boolean myFound = false;
        
        for (QueryParameterDef myTest : _parameterPresenter.getParameters()) {
            
            if (parameterNameIn.equals(myTest.getName())) {
                
                _query.getParameters().add(myTest.getLocalId());
                getTestParameters().add(myTest);
                myFound = true;
                break;
            }
        }
        return myFound;
    }
    
    private List<QueryParameterDef> getTestParameters() {
        
        if (null == _testParameters) {
            
            _testParameters = new ArrayList<QueryParameterDef>();
        }
        return _testParameters;
    }
    
    private void retrieveParameterValues(List<QueryParameterDef> listIn) {
        
        try {
            
            (new ParameterWizard(_txtParameterTestTitle, _txtParameterTestHelp, (_exitProcess ? _txtParameterSaveButton : _txtParameterTestButton), listIn, callBackHandler)).show();
            
        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }

    private void buildInitialQuery() {

        VortexFuture<QueryDef> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handleCreateQueryResponse);
            myVortexFuture.execute(TestActionsServiceProtocol.class).createCustomQuery(_table, new ArrayList<String>(),
                                                                                    _parameterPresenter.getParameters());

            watchBox.show(_constants.creatingCustomQuery());

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}

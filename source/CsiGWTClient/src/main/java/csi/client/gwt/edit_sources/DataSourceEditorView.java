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
package csi.client.gwt.edit_sources;

import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.SourceListChangeMonitor;
import csi.client.gwt.csiwizard.panels.WizardPanelInterfacePanel;
import csi.client.gwt.edit_sources.center_panel.ConfigurationPresenter;
import csi.client.gwt.edit_sources.center_panel.shapes.WienzoComposite;
import csi.client.gwt.edit_sources.center_panel.shapes.WienzoUnion;
import csi.client.gwt.edit_sources.center_panel.shapes.WienzoJoin;
import csi.client.gwt.edit_sources.center_panel.shapes.WienzoTable;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.edit_sources.right_panel.*;
import csi.client.gwt.csiwizard.support.ConnectionTreeItem;
import csi.client.gwt.csiwizard.panels.ConnectionTreePanel;
import csi.client.gwt.events.HoverEvent;
import csi.client.gwt.events.HoverEventHandler;
import csi.client.gwt.events.TreeSelectionEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.CsiDisplay;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.Instructions;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.buttons.*;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataSourceEditorView extends ResizeComposite implements SourceListChangeMonitor {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      Interfaces                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<Widget, DataSourceEditorView> {
    }

    private enum RightColumn {

        PROPERTIES, FIELD_MAP, FIELD_LIST, PARAMETERS, INFORMATION
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    MiniRedButton deleteObject;
    @UiField
    MiniBlueButton replaceObject;
    @UiField
    SimpleButton buttonPrevious;
    @UiField
    BlueButton buttonNext;
    @UiField
    GreenButton buttonSave;
    @UiField
    SimpleButton buttonCancel;
    @UiField
    MiniBlueButton buttonProperties;
    @UiField
    MiniBlueButton buttonParameters;
    @UiField
    MiniButton buttonMap;
    @UiField
    MiniBlueButton buttonFields;
    @UiField
    SplitLayoutPanel rootPanel;
    @UiField
    ScrollPanel centerPanel;
    @UiField
    SimpleLayoutPanel rightPanel;
    @UiField
    WizardPanelInterfacePanel leftPanel;
    @UiField
    Label statusLabel;

    @UiField
    RadioButton largeRadioButton;
    @UiField
    RadioButton smallRadioButton;
    @UiField
    CheckBox hideConnectors;

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @UiField(provided = true)
    String buttonParametersText = Dialog.txtParametersButton;

    @UiField(provided = true)
    String buttonFieldsText = Dialog.txtFieldsButton;

    @UiField(provided = true)
    String buttonMapText = Dialog.txtMapButton;

    @UiField(provided = true)
    String buttonPreviousText = Dialog.txtPreviousButton;

    @UiField(provided = true)
    String buttonPropertiesText = Dialog.txtPropertiesButton;

    @UiField(provided = true)
    String buttonNextText = Dialog.txtNextButton;

    @UiField(provided = true)
    String buttonSaveText = Dialog.txtCreateButton;

    @UiField(provided = true)
    String buttonCancelText = Dialog.txtCancelButton;

    @UiField(provided = true)
    String buttonDeleteText = Dialog.txtDeleteButton;

    @UiField(provided = true)
    String buttonReplaceText = Dialog.txtReplaceButton;

    @UiField(provided = true)
    String buttonEditText = Dialog.txtEditButton;

    @UiField(provided = true)
    Double westWidth = 450.0;

    @UiField(provided = true)
    Double eastWidth = 410.0;

    private Widget currentRightPanelWidget = null;

    private Instructions instructionPanel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final String _classKey = "edit_sources.EditorView";
    private static final String _westKey = "west";
    private static final String _eastKey = "east";

    private static final String _txtDeleteObjectHint = _constants.dataSourceEditor_DeleteObjectHint();
    private static final String _txtReplaceObjectHint = _constants.dataSourceEditor_ReplaceObjectHint();

    private static final String _txtPreviousButtonHint = _constants.wizard_PreviousButtonHint();
    private static final String _txtNextButtonHint = _constants.wizard_NextButtonHint();
    private static final String _txtCancelButtonHint = _constants.wizard_CancelButtonHint();
    private static final String _txtParametersButtonHint = _constants.dataSourceEditor_ParametersButtonHint();
    private static final String _txtFieldButtonHint = _constants.dataSourceEditor_EditFieldsButtonHint();
    private static final String _txtMapButtonHint = _constants.dataSourceEditor_MapButtonHint();

    private static final boolean _replaceSupported = true;
    private static final boolean _fullReplaceSupported = false;

    private DataSourceEditorPresenter _presenter;
    private DataSourceEditorModel _model;

    private ConfigurationPresenter _configurationPresenter = null;
    private ConnectionTreePanel _treePanel = null;
    private ClickHandler _cancelProcess = null;
    private ClickHandler _stepForward = null;
    private ClickHandler _stepBack = null;

    private TreeSelectionEventHandler<ConnectionTreeItem, SqlTableDef, DataSourceDef> _selectionHandler;
    
    private String[] _messageList = new String[DataSourceEditorState.values().length + 1];
    private String[] _messageColors = new String[DataSourceEditorState.values().length + 1];
    private boolean _displayButtons = true;
    private boolean _badDataFlag = true;
    private boolean _goodDataFlag = false;
    private boolean _missingFields = true;
    private boolean _noFields = true;
    private boolean _badDataType = true;
    private RightColumn _rightColumnDisplay = null;
    private Integer _rowLimit = null;
    private MainPresenter _mainPresenter = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleLargeDisplayClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            ConfigurationPresenter.changeDisplayFormat(true);
            if (null != _configurationPresenter) {

                _configurationPresenter.refresh();
            }
        }
    };

    private ClickHandler handleSmallDisplayClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            ConfigurationPresenter.changeDisplayFormat(false);
            if (null != _configurationPresenter) {

                _configurationPresenter.refresh();
            }
        }
    };

    private ClickHandler handleConnectorCheckBoxClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            ConfigurationPresenter.hideConnectors(hideConnectors.getValue());
            if (null != _configurationPresenter) {

                _configurationPresenter.refresh();
            }
        }
    };

    private ClickHandler handleDeleteObjectConfirmation =  new ClickHandler()  {

        @Override
        public void onClick(final ClickEvent event) {

            if (null != _configurationPresenter) {
                _configurationPresenter.deleteSelectedObject();
            }
        }
    };

    private ClickHandler handleDeleteObjectClick =  new ClickHandler()  {

        @Override
        public void onClick(final ClickEvent event) {

            Dialog.showYesNoDialog(i18n.dataSourceEditor_DeleteObjectDialogTitle(),
                    i18n.dataSourceEditor_DeleteObjectDialogPrompt(),
                    handleDeleteObjectConfirmation);
        }
    };

    ConnectionTreePanel.TableCallBack replaceTableCallBack = new ConnectionTreePanel.TableCallBack() {
        @Override
        public void handleResponse(SqlTableDef tableIn) {

            if (null != tableIn) {

                SqlTableDef myNewTable = _treePanel.getOnlySqlTable();

                _configurationPresenter.replaceSelectedObject(myNewTable);
            }
        }
    };

    private ClickHandler handleReplaceObjectClick =  new ClickHandler()  {

        @Override
        public void onClick(final ClickEvent event) {

            if (_replaceSupported) {

                if (_treePanel.tableSelected()) {

                    _treePanel.getSqlTableWithColumns(replaceTableCallBack);
                }
            }
        }
    };

    private ClickHandler handleEditObjectClick =  new ClickHandler()  {

        @Override
        public void onClick(final ClickEvent event) {

            _configurationPresenter.editSelectedObject();
        }
    };

    private ClickHandler handleParameterButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(final ClickEvent event) {

            showParameters();
        }
    };

    private ClickHandler handleParameterSaveClick =  new ClickHandler()  {

        @Override
        public void onClick(final ClickEvent event) {
            _model.replaceQueryParameters();
        }
    };

    private ClickHandler handleMapButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(ClickEvent event) {
            showFieldMapper();
        }
    };

    private ClickHandler handleFieldsButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(ClickEvent event) {
            showFieldList();
        }
    };

    private ClickHandler handlePropertiesButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(ClickEvent event) {

            WienzoComposite myItem = _configurationPresenter.getSelectedItem();

            if (null != myItem) {

                if (myItem instanceof WienzoTable) {

                    showTableEditor((WienzoTable) myItem);
                }
                if (myItem instanceof WienzoJoin) {

                    showJoinEditor((WienzoJoin) myItem);
                }
                if (myItem instanceof WienzoUnion) {

                    showAppendEditor((WienzoUnion)myItem);
                }
            }
        }
    };

    private ClickHandler handlePreviousButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(ClickEvent event) {
            //display a warning message before invoking the handler provided
            WarningDialog dialog = new WarningDialog(_constants.dataSourceEditor_DiscardDialogTitle(), //$NON-NLS-1$
                    _constants.dataSourceEditor_DiscardDialogMessage()); //$NON-NLS-1$
            dialog.addClickHandler(_stepBack);
            dialog.show();
        }
    };

    private ClickHandler handleNextButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(ClickEvent eventIn) {
            _stepForward.onClick(eventIn);
        }
    };

    private ClickHandler handleCancelButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(ClickEvent event) {
            //display a warning message before invoking the handler provided
            WarningDialog dialog = new WarningDialog(_constants.dataSourceEditor_DiscardDialogTitle(), //$NON-NLS-1$
                    _constants.dataSourceEditor_DiscardDialogMessage()); //$NON-NLS-1$
            dialog.addClickHandler(handleRevertButtonClick);
            dialog.show();
        }
    };

    private ClickHandler handleRevertButtonClick =  new ClickHandler()  {

        @Override
        public void onClick(ClickEvent eventIn) {
            //call the handler provided as as argument
            _cancelProcess.onClick(eventIn);
        }
    };

    private HoverEventHandler handleHoverEvent = new HoverEventHandler() {
        
        public void onHoverChange(HoverEvent eventIn) {
         
            if (eventIn.isOver()) {
                
                displayToolTip(eventIn.getDisplayMessage());
                
            } else {
                
                clearToolTip();
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataSourceEditorView(DataSourceEditorPresenter presenterIn,
            TreeSelectionEventHandler<ConnectionTreeItem, SqlTableDef, DataSourceDef> handlerIn) {
        super();

        MainPresenter myMainPresenter = getMainPresenter();

        westWidth = myMainPresenter.getDialogPreference(_classKey, _westKey, (double)410.0);
        eastWidth = myMainPresenter.getDialogPreference(_classKey, _eastKey, (double)450.0);

        initWidget(uiBinder.createAndBindUi(this));

        _presenter = presenterIn;
        _selectionHandler = handlerIn;
        _model = _presenter.getModel();
        _rowLimit = _presenter.getRowLimit();
        enableButtons(true);
        buttonFields.setVisible(false);
    }

    public DataSourceEditorView close() {

        _treePanel = _treePanel.releasePanel();
        _configurationPresenter = _configurationPresenter.releaseDisplay();
        _presenter = null;
        _model = null;
        removeFromParent();
        return null;
    }

    public void addDataSource() {

        if (null != _treePanel) {

            _treePanel.addDataSource();
        }
    }

    public void editDataSource() {

        if (null != _treePanel) {

            _treePanel.editDataSource();
        }
    }

    public void removeDataSource() {

        if (null != _treePanel) {

            _treePanel.removeDataSource();
        }
    }

    public void mapFields() {

        handleMapButtonClick.onClick(null);
    }

    public void editFields() {

    }

    public void editParameters() {

        handleParameterButtonClick.onClick(null);
    }

    public void dropItem(ConnectionTreeItem itemIn) {

        _treePanel.dropItem(itemIn);
    }

    public void finalizeDisplay() throws Exception {

        _configurationPresenter = new ConfigurationPresenter(_presenter, this);
        _configurationPresenter.addViewToPanel(centerPanel);
        _treePanel = new ConnectionTreePanel(null, _model, _selectionHandler, _displayButtons);
        _treePanel.setSourceListChangeMonitor(this);
        leftPanel.add(_treePanel);

        for (int i = 0; DataSourceEditorState.values().length > i; i++) {

            _messageList[i] = null;
            _messageColors[i] = DataSourceEditorState.values()[i].getColor();
        }
        _messageList[DataSourceEditorState.values().length] = null;
        _messageColors[DataSourceEditorState.values().length] = Dialog.txtInfoColor;

        identifyMajorButtons();

        DeferredCommand.add(new Command() {
            public void execute() {
                finalResize();
            }
        });
    }

    private void finalResize() {


        DeferredCommand.add(new Command() {
            public void execute() {
                leftPanel.onResize();
                updateConfigButtons();
            }
        });
    }

    public void onSourceListChange() {

        _model.setChanged();
    }

    public void addTable(SqlTableDef tableIn, DataSourceDef dataSourceIn) {

        _configurationPresenter.addTable(tableIn, dataSourceIn);
    }

    /**
     * For some reason reason this returns HasClickHandlers instead of Button.
     * @return
     */
    public HasClickHandlers getSaveButton() {
        return buttonSave;
    }

    public void treeSelectionComplete() {

        _treePanel.enableInput();
    }

    public void clearAnyEditor() {

        _rightColumnDisplay = null;
    }

    public void showTableEditor(WienzoTable objectIn) {

        _rightColumnDisplay = RightColumn.PROPERTIES;
        TableDetailEditor editor = new TableDetailEditor(objectIn, _model);
        swapRightPanel(editor);
    }

    public void showJoinEditor(WienzoJoin objectIn) {

        _rightColumnDisplay = RightColumn.PROPERTIES;
        JoinMappingEditor editor = new JoinMappingEditor(objectIn, _model);
        swapRightPanel(editor);
    }

    public void showAppendEditor(WienzoUnion objectIn) {

        _rightColumnDisplay = RightColumn.PROPERTIES;
        AppendMappingEditor editor = new AppendMappingEditor(objectIn, _model);
        swapRightPanel(editor);
    }

    public void showFieldMapper() {

        _rightColumnDisplay = RightColumn.FIELD_MAP;
        FieldColumnMappingEditor editor = new FieldColumnMappingEditor(_model);
        swapRightPanel(editor);
    }

    public void showFieldList() {

        _rightColumnDisplay = RightColumn.FIELD_LIST;
        swapRightPanel(new Label("Field List"));
        showFieldListEditor();

    }

    public void showParameters() {

        _rightColumnDisplay = RightColumn.PARAMETERS;
        swapRightPanel(new ParameterListEditor(_model.createParameterPresenter()));
    }

    public void clearSourceTreeSelection() {

        if (null != _treePanel) {

            _treePanel.clearSelection();
        }
    }

    public void enableButtons(boolean enabledIn) {

        buttonPrevious.setEnabled(_presenter.isNew());
        buttonNext.setEnabled(enabledIn && (! _presenter.isFinal()));
        buttonSave.setEnabled(enabledIn && _presenter.isFinal());
        buttonCancel.setEnabled(true);
    }

    public void adjustButtons(List<DataSourceDef> dataSourcesIn, List<DataSetOp> dataListIn,
                              boolean missingFieldsIn, boolean noFieldsIn, boolean badDataTypesIn) {

        _goodDataFlag = (null != dataListIn) && (1 == dataListIn.size());
        _badDataFlag = (null == dataListIn) || (0 == dataListIn.size());
        _missingFields = missingFieldsIn;
        _noFields = noFieldsIn;
        _badDataType = badDataTypesIn;

        clearState();
        
        enableButtons(okToSave(dataSourcesIn, dataListIn));
        enableMapperButton();

        if (_badDataFlag) {
            
            clearAnyEditor();
        }
    }
    
    public void disableAll() {

        buttonPrevious.setEnabled(false);
        buttonNext.setEnabled(false);
        buttonSave.setEnabled(false);
        buttonCancel.setEnabled(false);
        buttonMap.setEnabled(false);
        buttonFields.setEnabled(false);
        buttonParameters.setEnabled(false);
        buttonProperties.setEnabled(false);
        clearState();
        clearAnyEditor();
    }

    public void setNextClickHandler(final ClickHandler handlerIn) {

        _stepForward = handlerIn;
    }

    public void setPreviousClickHandler(final ClickHandler handlerIn) {

        _stepBack = handlerIn;
    }

    public void setCancelClickHandler(final ClickHandler handlerIn) {

        _cancelProcess = handlerIn;
    }

    private boolean okToSave(List<DataSourceDef> dataSourcesIn, List<DataSetOp> dataListIn) {

        if (_noFields) {

            displayState(DataSourceEditorState.NOFIELDS);
        }
        if (_badDataType) {

            displayState(DataSourceEditorState.BADTYPE);
        }
        if (_missingFields) {

            displayState(DataSourceEditorState.BADFIELD);
        }
        if (null != dataListIn) {
            
            if (1 > dataListIn.size()) {
                
                displayState(DataSourceEditorState.NOTREE);

            } else if (1 < dataListIn.size()) {
                
                displayState(DataSourceEditorState.XTRATREE);
                
            } else if (!dataListIn.get(0).isOkTree(dataSourcesIn)) {
                    
                displayState(DataSourceEditorState.BADTREE);
            }
        }
        
        checkMappings(dataListIn);
        
        return !hasErrorMessages();
    }

    public void enableMapperButton() {

        if (_missingFields || _badDataType || _noFields) {

            buttonMap.setType(ButtonType.WARNING);

        } else {

            buttonMap.setType(ButtonType.PRIMARY);
        }
        buttonMap.setEnabled(_goodDataFlag && (!RightColumn.FIELD_MAP.equals(_rightColumnDisplay)));
    }

    public void displayState(DataSourceEditorState stateIn) {
        
        _messageList[stateIn.getOrdinal()] = stateIn.getLabel();
        displayActiveMessage();
    }
    
    public void clearState(DataSourceEditorState stateIn) {
        
        _messageList[stateIn.getOrdinal()] = null;
        displayActiveMessage();
    }
    
    public void clearState() {
        
        for (int i = 0; (_messageList.length - 1) > i; i++) {
            
            _messageList[i] = null;
        }
        displayActiveMessage();
    }

    public void displayToolTip(String toolTipIn, String colorIn) {

        _messageList[_messageList.length - 1] = toolTipIn;
        _messageColors[_messageList.length - 1] = colorIn;
        displayActiveMessage();
    }

    public void displayToolTip(String toolTipIn) {

        displayToolTip(toolTipIn, Dialog.txtInfoColor);
    }

    public void clearToolTip() {

        _messageList[_messageList.length - 1] = null;
        displayActiveMessage();
    }

    public boolean hasErrorMessages() {
        
        boolean myHasErrors = false;
        
        for (int i = _messageList.length - 2; 0 <= i; i--) {
            
            if (null != _messageList[i]) {
                
                if (Dialog.txtErrorColor.equals(_messageColors[i])) {
                    
                    myHasErrors = true;
                }
                break;

            } else if (!Dialog.txtErrorColor.equals(_messageColors[i])) {
                
                break;
            }
        }
        
        if (!myHasErrors) {
            
            displayState(DataSourceEditorState.READY);
        }
        return myHasErrors;
    }

    public boolean sourceTableSelected() {

        return _treePanel.tableSelected();
    }

    public SqlTableDef sourceTableSelection() {

        return _treePanel.getSqlTable();
    }

    public void recordDisplayState() {

        MainPresenter myMainPresenter = getMainPresenter();

        myMainPresenter.setDialogPreference(_classKey, _westKey, (double)leftPanel.getOffsetWidth());
        myMainPresenter.setDialogPreference(_classKey, _eastKey, (double)rightPanel.getOffsetWidth());
    }

    public Integer getRowLimit() {

        forceUpdates();
        return _rowLimit;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void updateConfigButtons() {

        boolean myCenterItemSelected = _configurationPresenter.itemSelected();
        boolean myCenterTableSelected = _configurationPresenter.tableSelected();

        deleteObject.setEnabled(myCenterItemSelected);
        replaceObject.setEnabled((_fullReplaceSupported && (myCenterItemSelected
//                && (_treePanel.querySelected() || _treePanel.tableSelected())))
                && _treePanel.tableSelected()))
                || (_replaceSupported && (myCenterTableSelected
//                && (_treePanel.querySelected() || _treePanel.tableSelected()))));
                && _treePanel.tableSelected())));

        enableMapperButton();
        buttonFields.setEnabled(!RightColumn.FIELD_LIST.equals(_rightColumnDisplay));
        buttonParameters.setEnabled(!RightColumn.PARAMETERS.equals(_rightColumnDisplay));
        buttonProperties.setEnabled(myCenterItemSelected && (!RightColumn.PROPERTIES.equals(_rightColumnDisplay)));

        DeferredCommand.add(new Command() {
            public void execute() {
                updateConfigButtons();
            }
        });
    }

    private void identifyMajorButtons() {

        buttonSaveText = _presenter.isNew() ? Dialog.txtCreateButton : Dialog.txtUpdateButton;

        deleteObject.addClickHandler(handleDeleteObjectClick);
        replaceObject.addClickHandler(handleReplaceObjectClick);

        buttonParameters.addClickHandler(handleParameterButtonClick);
        largeRadioButton.addClickHandler(handleLargeDisplayClick);
        smallRadioButton.addClickHandler(handleSmallDisplayClick);
        hideConnectors.addClickHandler(handleConnectorCheckBoxClick);
        buttonMap.addClickHandler(handleMapButtonClick);
        buttonFields.addClickHandler(handleFieldsButtonClick);
        buttonProperties.addClickHandler(handlePropertiesButtonClick);

        deleteObject.addHoverEventHandler(handleHoverEvent, _txtDeleteObjectHint);
        replaceObject.addHoverEventHandler(handleHoverEvent, _txtReplaceObjectHint);
        buttonParameters.addHoverEventHandler(handleHoverEvent, _txtParametersButtonHint);
        buttonFields.addHoverEventHandler(handleHoverEvent, _txtFieldButtonHint);
        buttonMap.addHoverEventHandler(handleHoverEvent, _txtMapButtonHint);
        buttonProperties.addHoverEventHandler(handleHoverEvent, "Display properties in the right panel for the center panel item selected.");

        buttonPrevious.addClickHandler(handlePreviousButtonClick);
        buttonPrevious.addHoverEventHandler(handleHoverEvent, _txtPreviousButtonHint);

        buttonNext.addClickHandler(handleNextButtonClick);
        buttonNext.addHoverEventHandler(handleHoverEvent, _txtNextButtonHint);

        buttonCancel.addClickHandler(handleCancelButtonClick);
        buttonCancel.addHoverEventHandler(handleHoverEvent, _txtCancelButtonHint);

        buttonSave.setText(buttonSaveText);
        buttonSave.addHoverEventHandler(handleHoverEvent,
                                        _constants.wizard_FinalizeButtonHint(buttonSaveText,
                                                                            _presenter.getResourceType().getLabel()));
        _treePanel.addButtonMonitor(handleHoverEvent);
    }

    private void checkMappings(List<DataSetOp> dataListIn) {
        
        for (DataSetOp myDso : dataListIn) {
            
            if (myDso.hasBadMappings()) {
                
                displayState(DataSourceEditorState.NOJMAP);
            }
        }
        
        for (DataSetOp myDso : dataListIn) {
            
            if (myDso.hasWeekMappings()) {
                
                displayState(DataSourceEditorState.NOAMAP);
            }
        }
    }

    private void displayActiveMessage() {
        
        for (int i = _messageList.length - 1; 0 <= i; i--) {
            
            if (null != _messageList[i]) {
                
                statusLabel.getElement().getStyle().setColor(_messageColors[i]);
                statusLabel.setText(_messageList[i]);
                break;
            }
        }
    }

    private void showFieldListEditor() {

        Display.error("Field List Editor",
                "Not currently available within the Data Source Editor");
    }

    private void forceUpdates() {

        if ((null != currentRightPanelWidget) && (currentRightPanelWidget instanceof FieldColumnMappingEditor)) {

            _rowLimit = ((FieldColumnMappingEditor)currentRightPanelWidget).getRowLimit();
        }
    }

    private void initializePanel() {

        if ((null != currentRightPanelWidget) && (currentRightPanelWidget instanceof FieldColumnMappingEditor)) {

            ((FieldColumnMappingEditor)currentRightPanelWidget).setRowLimit(_rowLimit);
        }
    }

    private void swapRightPanel(Widget widgetIn) {

        forceUpdates();
        rightPanel.setWidget(widgetIn);
        currentRightPanelWidget = widgetIn;
        initializePanel();
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}

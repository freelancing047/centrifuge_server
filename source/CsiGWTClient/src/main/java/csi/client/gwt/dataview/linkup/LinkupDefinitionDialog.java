/** 
 *  Copyright (c) 2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.dataview.linkup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.TemplateSelector;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.export.kml.mapping.multiselector.DualListBoxDialog;
import csi.client.gwt.dataview.staging.LinkupExtenderStage;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.*;
import csi.client.gwt.widget.buttons.AmberButton;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.buttons.MiniCyanButton;
import csi.client.gwt.widget.buttons.MiniSimpleButton;
import csi.client.gwt.widget.buttons.RedButton;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.DataOperation;
import csi.server.common.enumerations.ResourceChoiceCriteria;
import csi.server.common.linkup.LinkupDataTransfer;
import csi.server.common.linkup.TemplateResponse;
import csi.server.common.model.FieldDef;
import csi.server.common.model.UUID;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupExtender;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.linkup.LooseMapping;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.util.SynchronizeChanges;
import csi.server.common.util.ValuePair;

public class LinkupDefinitionDialog extends WatchingParent implements CanBeShownParent, SelectionChangeResponder, ValidityCheck {

    private com.github.gwtbootstrap.client.ui.Button linkinButton;

    public void setLinkinValue(String linkinValue) {
        this.linkinValue = linkinValue;
        updateLinkinButtonText();

    }

    public void updateLinkinButtonText() {
        int length = 0;
        try {
            if(!Strings.isNullOrEmpty(linkinValue)) {
                length = linkinValue.split(",").length;
            }

        } catch (Exception e) {

        }
        linkinButton.setText(_constants.linkinFields()+" ("+ length +")");
    }

    private String linkinValue = "";


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface LinkupDefinitionDialogUiBinder extends UiBinder<Widget, LinkupDefinitionDialog> {
    }

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">{name}</span>")
        SafeHtml display(String name);

    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @UiField
    ValidatingDialog dialog;
        
    @UiField
    FullSizeLayoutPanel topPanel;
    @UiField
    TextBox newLinkupName;

    @UiField
    RadioButton newLinkup;
    @UiField
    RadioButton existingLinkup;
    @UiField
    RadioButton allRows;
    @UiField
    RadioButton uniqueRows;
    @UiField
    InlineLabel dataModeLabel;

    @UiField
    MiniCyanButton newFieldButton;
    @UiField
    MiniCyanButton renameButton;
    @UiField
    MiniSimpleButton restoreButton;

    @UiField(provided = true)
    CsiStringListBox availableLinkups;

    @UiField(provided = true)
    CsiStringListBox availableTemplates;

    @UiField(provided = true)
    Grid<FieldToLabelMapStore> templateParms;
    /* if (_fieldParmsSupported) {
    @UiField(provided = true)
    Grid<?> fieldParms;
    } */
    @UiField(provided = true)
    Grid<FieldToFieldMapStore> fieldMap;

    @UiField
    HorizontalPanel templateDropDownPanel;
    @UiField
    HorizontalPanel linkupNamePanel;
    @UiField
    HorizontalPanel availableLinkupsDropDownPanel;

    @UiField
    Label availableLinkupLabel;
    @UiField
    Label availableTemplatesLabel;
    @UiField
    Label linkupNameLabel;
    @UiField
    Label templateParmsLabel;
    /* if (_fieldParmsSupported) {
    @UiField
    Label fieldParmsLabel;
    } */
    @UiField
    Label fieldMapLabel;
    
//    @UiField
//    Label optionalParms;
    @UiField
    Label fieldMapError;

//    @UiField
//    HorizontalPanel noParametersPanel;

    @UiField
    CheckBox noNullsCheckBox;
    @UiField
    CheckBox promptCheckBox;
    @UiField
    CheckBox editOkCheckBox;
    @UiField
    CheckBox monitorCheckBox;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static LinkupDefinitionDialogUiBinder uiBinder = GWT.create(LinkupDefinitionDialogUiBinder.class);

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final String _txtDialogTitle = _constants.linkupDefinitionPanel_DialogTitle();
    private final String _txtServerRequestSuccessTitle = _constants.linkupDefinitionPanel_ServerRequestSuccessTitle();
    private final String _txtServerRequestSuccessMessage = _constants.linkupDefinitionPanel_ServerRequestSuccessMessage();
    private final String _txtServerDeleteFailureTitle = _constants.linkupDefinitionPanel_ServerDeleteFailureTitle();
    private final String _txtServerUpdateFailureTitle = _constants.linkupDefinitionPanel_ServerUpdateFailureTitle();
    private final String _txtMappingMissing = _constants.linkupDefinitionPanel_MappingMissing();
    private final String _txtMappingConflict = _constants.linkupDefinitionPanel_MappingConflict();
    private final String _txtDelayedInputOption = _constants.linkupDefinitionPanel_DelayedInputOption();
    private final String _txtMappingInstructions = _constants.linkupDefinitionPanel_MappingInstructions();
    private final String _txtLinkupNameLabel = _constants.linkupDefinitionPanel_LinkupNameLabel();
    private final String _txtAvailableTemplatesLabel = _constants.linkupDefinitionPanel_AvailableTemplatesLabel();
    private final String _txtCreateLinkupLabel = _constants.linkupDefinitionPanel_CreateLinkupLabel();
    private final String _txtEditLinkupLabel = _constants.linkupDefinitionPanel_EditLinkupLabel();
    private final String _txtAdvancedButton = _constants.dialog_AdvancedButton();
    private final String _txtDeleteButton = _constants.dialog_DeleteButton();
    private final String _txtExitButton = _constants.dialog_CloseButton();
    private final String _txtNeedSelection = _constants.linkupDefinitionPanel_NeedSelection();
    private final String _txtUpdateButton = _constants.dialog_UpdateButton();
    private final String _txtAddButton = _constants.dialog_AddButton();
    private final String _txtFillParameterGridLabel = _constants.linkupDefinitionPanel_FillParameterGridLabel();
    private final String _txtIgnoreParameterGridLabel = _constants.linkupDefinitionPanel_IgnoreParameterGridLabel();
    private final String _txtFieldGridLabel = _constants.linkupDefinitionPanel_FieldGridLabel();
    private final String _txtNoParametersLabel = _constants.linkupDefinitionPanel_NoParametersLabel();
    private final String _txtHelpTarget = null;//_constants.linkupDefinitionPanel_HelpTarget();
    private final String _txtTemplateListError = _constants.error_RetrieveTemplateList_Title();
    private final String _txtTemplateDataError = _constants.error_RetrieveTemplateData_Title();
    private final String _txtLinkupDataError = _constants.error_RetrieveLinkupData_Title();

    private final String _txtSelectLinkupTemplate = _constants.linkupDefinitionPanel_SelectTemplate();
    private final String _txtEnterLinkupName = _constants.linkupDefinitionPanel_EnterName();
    private final String _txtSelectLinkup = _constants.linkupDefinitionPanel_SelectLinkup();
    private final String _txtReadOnly = _constants.linkupDefinitionPanel_ReadOnly();
    private final String _txtIgnoreNullParms = _constants.linkupSelectionDialog_IgnoreNullsCheckBox();
    private final String _txtPromptToVerify = _constants.linkupDefinitionPanel_PromptToVerify();
    private final String _txtOkToChange = _constants.linkupDefinitionPanel_OkToChange();
    private final String _txtMonitorProgress = _constants.linkupDefinitionPanel_MonitorProgress();

    private final String _txtReturnAllRows = _constants.linkupDefinitionPanel_ReturnAllRows();
    private final String _txtReturnUniqueRows = _constants.linkupDefinitionPanel_ReturnUniqueRows();
    private final String _txtDataModeLabel = _constants.linkupDefinitionPanel_DataModeLabel();

    private Label noParametersLabel = null;

    private Map<String, ResourceBasics> _templateNameUuidMap = new TreeMap<String, ResourceBasics>();
    private AbstractDataViewPresenter _dvPresenter;
    private DataViewDef _templateDef;
    private AmberButton advancedButton;
    private RedButton deleteButton;
    private LinkupMapDef _selectedLinkup;
    private Map<String, LinkupMapDef> _linkupNameMap = new TreeMap<String, LinkupMapDef>();
    private boolean  _initialized = false;
    private LinkupFieldMapper _fieldMapper;
    private LinkupParameterMapper _parameterMapper;
    /* if (_fieldParmsSupported) {
    private LinkupConditionalFieldMapper _conditionalMapper;
    } */
    private TemplateSelector _templateSelector = null;
    private HandlerRegistration _linkupCallbackHandler;
    private AdvancedLinkupParameterDialog _advancedDialog = null;
    private String _templateName = null;
    private String _linkupName = null;
    private boolean _templateValid = false;
    private boolean _selectionValid = false;
    private boolean _linkupNameValid = false;
    private boolean _dialogButtonsActive = false;
    private boolean _hasParameters = false;
    private List<LinkupExtender> _extenderList = null;
    private int _nextOrdinal = 0;
    private String _autoName = null;
    private String _templateOwner = null;
    private boolean _isNew = true;
    private boolean _returnAllRows = false;

    public static Comparator<String> itemComparator = new Comparator<String>() {
        @Override
        public int compare(String stringOneIn, String stringTwoIn) {
            if (null != stringOneIn) {
                if (null != stringTwoIn) {
                    return stringOneIn.compareTo(stringTwoIn);
                } else {
                    return 1;
                }
            } else if (null != stringTwoIn) {
                return -1;
            }
            return 0;
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle response from server when creating, updating, or deleting a linkup definition
    //
    private DataChangeEventHandler handleLinkupResponse
    = new DataChangeEventHandler() {

        @Override
        public void onDataChange(DataChangeEvent eventIn) {

            try {

                if (!eventIn.isSuccess()) {
                    enableAll(true);
                    if (eventIn.isDelete()) {

                        (new ErrorDialog(_txtServerDeleteFailureTitle,
                                eventIn.getError().getMessage())).show();
                    } else {

                        (new ErrorDialog(_txtServerUpdateFailureTitle,
                                eventIn.getError().getMessage())).show();
                    }
                } else {
                    if ((null != eventIn.getData()) && (eventIn.getData() instanceof LinkupDataTransfer)) {
                        LinkupDataTransfer myLinkupResponse = (LinkupDataTransfer)eventIn.getData();
                        LinkupMapDef myLinkupMapDef = myLinkupResponse.getObjectOfInterest();
                        String myLinkupName = myLinkupMapDef.getLinkupName();
//                    DataViewDef myMeta = _dvPresenter.getDataView().getMeta();

                        _extenderList = null;
                        recordAvailableLinkups();
                        availableLinkups.setSelectedValue(myLinkupName);
                        newLinkupName.setText(myLinkupName);
                        //initLinkupFields(_linkupNameMap.get(eventIn.getDataString()));
                        if (eventIn.isDelete()) {
                            _isNew = true;
                            newLinkup.setValue(true);
                            newLinkup.setEnabled(false);
                            existingLinkup.setEnabled(true);
                            deleteButton.setEnabled(false);
                            deleteButton.setVisible(false);
                            initializeNewValues();
                        } else {

                            _isNew = false;
                            existingLinkup.setValue(true);
                            newLinkup.setEnabled(true);
                            existingLinkup.setEnabled(false);
                            deleteButton.setVisible(true);
                            deleteButton.setEnabled(true);
                            initializeExistingValues();
                        }
                    }
                    enableAll(true);
                    (new SuccessDialog(_txtServerRequestSuccessTitle, _txtServerRequestSuccessMessage + " "
                            + eventIn.getRequestString() + " " + eventIn.getDataString())).show(); //$NON-NLS-1$
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 1, myException);
            }
        }
    };

    //
    // Handle valid free text entry or selection from template selection combobox drop-down
    //
    private SelectionChangedHandler<String> handleTemplateSelection
            = new SelectionChangedHandler<String>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            try {

                String mySelection = availableTemplates.getSelectedText();

                if ((null != mySelection) && (0 < mySelection.length())) {

                    String myName = newLinkupName.getText();

                    _templateName = mySelection;
                    availableTemplates.setEmptyText(_templateName);
                    resetTemplateValues();
                    enableAll(false);
                    initTemplateFields(_templateNameUuidMap.get(_templateName));

                    if ((null == myName) || (0 == myName.length()) || myName.equals(_autoName)) {

                        generateLinkupName();
                    }
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 2, myException);
            }
        }
    };

    //
    // Handle valid free text entry or selection from linkup selection combobox drop-down
    //
    private SelectionChangedHandler<String> handleLinkupSelection
            = new SelectionChangedHandler<String>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            try {

                String myString = availableLinkups.getSelectedText();

                if ((null != myString) && (0 < myString.trim().length())) {
                    resetTemplateValues();

                    _selectedLinkup = _linkupNameMap.get(myString.trim());
                    _linkupName = _selectedLinkup.getLinkupName();
                    availableLinkups.setEmptyText(_linkupName);
                    promptCheckBox.setValue(_selectedLinkup.getPrompt());
                    editOkCheckBox.setValue(_selectedLinkup.getEditOk());
                    monitorCheckBox.setValue(_selectedLinkup.getMonitor());
                    setLinkinValue(_selectedLinkup.getLinkin());
                    enableAll(false);
                    initLinkupFields();
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 3, myException);
            }
        }
    };

    //
    // Handle radio button selection for creating new linkup definition
    //
    private ClickHandler handleNewLinkupRadioButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                _isNew = true;
                newLinkup.setValue(true);
                newLinkup.setEnabled(false);
                existingLinkup.setEnabled(true);
                initializeNewValues();
                enableAll(true);

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 4, myException);
            }
        }
    };

    //
    // Handle radio button selection for accessing existing linkup definition
    //
    private ClickHandler handleExistingLinkupRadioButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if(_initialized) {

                    if(availableLinkups.getStore().size() == 0){
                        newLinkup.setValue(true, false);
                        return;
                    }
                    _isNew = false;
                    existingLinkup.setValue(true);
                    newLinkup.setEnabled(true);
                    existingLinkup.setEnabled(false);
                    initializeExistingValues();
                    enableAll(true);
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 5, myException);
            }
        }
    };

    //
    // Handle clicking the advanced button
    //
    private ClickHandler handleAdvancedButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if(_initialized) {

                    if (null == _advancedDialog) {

                        if (null == _extenderList) {

                            List<LinkupExtender> myList = (null != _selectedLinkup) ? _selectedLinkup.linkupExtenders : null;

                            _extenderList = new ArrayList<LinkupExtender>();

                            if ((null != myList) && (0 < myList.size())) {

                                for (int i = 0; myList.size() > i; i++) {

                                    _extenderList.add(new LinkupExtenderStage(myList.get(i)));
                                }
                            }
                        }
                        _advancedDialog = new AdvancedLinkupParameterDialog(_dvPresenter, _templateDef, _extenderList);
                    }
                    if (null != _advancedDialog) {

                        _advancedDialog.show();
                    }
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 6, myException);
            }
        }
    };

    //
    // Handle clicking the delete button
    //
    private ClickHandler handleDeleteButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if(_initialized) {

                    if (_selectedLinkup != null) {
                        Dialog.showYesNoDialog(_constants.warningDialog_DeleteResourceTitle("Linkup"),
                                _constants.warningDialog_DeleteResourceMessage(1, "Linkup"),
                                handleDeleteAuthorizedClick);
                    }
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 7, myException);
            }
        }
    };

    //
    // Handle clicking the delete button
    //
    private ClickHandler handleDeleteAuthorizedClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if(_initialized) {

                    if (_selectedLinkup != null) {
                        removeLinkup();
                    }
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 8, myException);
            }
        }
    };

    //
    // Handle clicking the action button which may be labeled as either "Add" or "Update"
    //
    private ClickHandler handleRenameButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if(_initialized) {

                    linkupNamePanel.setVisible(true);
                    linkupNameLabel.setVisible(true);
                    newLinkupName.setVisible(true);
                    newLinkupName.setEnabled(true);
                    restoreButton.setVisible(true);

                    newLinkupName.setText(availableLinkups.getText());

                    availableLinkupsDropDownPanel.setVisible(false);
                    availableLinkupLabel.setVisible(false);
                    availableLinkups.setEnabled(false);
                    availableLinkups.setVisible(false);
                    renameButton.setVisible(false);

                    newLinkupName.setText(availableLinkups.getText());
                    newLinkupName.setFocus(true);
                    newLinkupName.selectAll();
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 9, myException);
            }
        }
    };

    //
    // Handle clicking the action button which may be labeled as either "Add" or "Update"
    //
    private ClickHandler handleRestoreButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if(_initialized) {

                    linkupNamePanel.setVisible(false);
                    linkupNameLabel.setVisible(false);
                    newLinkupName.setVisible(false);
                    newLinkupName.setEnabled(false);
                    restoreButton.setVisible(false);

                    newLinkupName.setText("");

                    availableLinkupsDropDownPanel.setVisible(true);
                    availableLinkupLabel.setVisible(true);
                    availableLinkups.setEnabled(true);
                    availableLinkups.setVisible(true);
                    renameButton.setVisible(true);

                    _linkupNameValid = true;
                    clearMessageDisplay();
                    setActionButtons(true);
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 10, myException);
            }
        }
    };

    //
    // Handle clicking the action button which may be labeled as either "Add" or "Update"
    //
    private ClickHandler handleActionButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if (_isNew) {
                    addLinkup();
                } else {
                    updateLinkup();
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 11, myException);
            }
        }
    };

    //
    // Handle clicking the cancel button
    //
    private ClickHandler handleCancelButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                _linkupCallbackHandler.removeHandler();
                dialog.removeFromParent();
                dialog.hide();

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 12, myException);
            }
        }
    };

    //
    // Monitor typing into the "newLinkupName" text field
    // and continually check for name conflicts
    //
    private KeyUpHandler handleNewLinkupNameKeyUp
    = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent eventIn) {

            try {

                checkLinkupName(newLinkupName.getText());
                setActionButtons();

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 13, myException);
            }
       }
    };

    //
    // Check for name conflicts whenever a text drag object
    // is dropped onto the "newLinkupName" text field
    //
    private DropHandler handleNewLinkupNameDrop
    = new DropHandler() {
        @Override
        public void onDrop(DropEvent eventIn) {

            try {

                checkLinkupName(eventIn.getData("text/plain")); //$NON-NLS-1$
                setActionButtons();

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 14, myException);
            }
       }
    };

    //
    // Monitor any change to the "newLinkupName" text field
    // and continually check for name conflicts
    //
    private ChangeHandler handleNewLinkupNameChange
    = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent eventIn) {

            try {

                checkLinkupName(newLinkupName.getText());
                setActionButtons();

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 15, myException);
            }
        }
    };

    //
    // Initialize the list of available templates with data from the server
    //
    private VortexEventHandler<List<ResourceBasics>> handleInitAvailableTemplatesServerResponse
    = new AbstractVortexEventHandler<List<ResourceBasics>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            (new ErrorDialog(_txtTemplateListError, exceptionIn.getMessage())).show();
            return true;
        }

        @Override
        public void onSuccess(List<ResourceBasics> listIn) {

            try {

                availableTemplates.clear();
                for (ResourceBasics myItem : listIn) {
                    ResourceBasics myResource = myItem;
                    availableTemplates.addItem(myResource.getDisplayString());
                    _templateNameUuidMap.put(myResource.getDisplayString(), myResource);
                }
                availableTemplates.setValue(null);

                _templateDef = null;
                _templateName = null;
                _templateOwner = null;
                _templateValid = false;

                grantUserAccess();

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 16, myException);
            }
        }
    };

    //
    // Load values from the server into mapping grid after selecting a linkup to edit
    //
    private VortexEventHandler<Response<String, TemplateResponse>> handleInitLinkupFieldsServerResponse
    = new AbstractVortexEventHandler<Response<String, TemplateResponse>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            // Display error message.
            Display.error("LinkupDefinitionDialog", 17, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, TemplateResponse> responseIn) {

            try {

                _templateValid = false;
                _selectionValid = false;
                _linkupNameValid = true;
                clearMessageDisplay();
                displayGrids();
                enableAll(true);
                renameButton.setVisible(true);

                if (ResponseHandler.isSuccess(responseIn)) {

                    TemplateResponse myReport = responseIn.getResult();
                    List<ValuePair<DataViewDef, Integer>> myList = myReport.getTemplateList();
                    DataViewDef myTemplate = myReport.getTemplate();
                    Integer myCriteria = myReport.getCriteriaMask();

                    if (((null != myList) && (1 < myList.size())) || ((null != myTemplate) && (!ResourceChoiceCriteria.UUID.isSet(myCriteria)))) {

                        _templateSelector = new TemplateSelector(_this, myList, myTemplate, myCriteria);
                        _templateSelector.show(AclResourceType.TEMPLATE, null, "Select the template to use for the linkup", "Use");

                    } else if (null != myTemplate) {

                        applyTemplateSelection(myTemplate);

                    } else {

                        Display.error("LinkupDefinitionDialog_18", _constants.locateTemplate_Failure());
                    }
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 19, myException);
            }
        }
    };

    //
    // Load values from the server into mapping grid after selecting a template for linkup
    //
    private VortexEventHandler<Response<String, TemplateResponse>> handleInitTemplateFieldsServerResponse
            = new AbstractVortexEventHandler<Response<String, TemplateResponse>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            // Display error message.
            Display.error("LinkupDefinitionDialog", 20, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, TemplateResponse> responseIn) {

            try {

                _templateValid = false;
                _selectionValid = false;
                displayGrids();
                enableAll(true);

                if (ResponseHandler.isSuccess(responseIn)) {

                    TemplateResponse myReport = responseIn.getResult();

                    DataViewDef myTemplate = (null != myReport) ? myReport.getTemplate() : null;

                    if (null != myTemplate) {

                        _extenderList = null;
                        _templateDef = myTemplate;
                        availableTemplates.setSelectedValue(_templateDef.getName());
                        _fieldMapper.initGridFields(_templateDef, _dvPresenter.getDataView().getMeta());
                        _parameterMapper.initGridFields(myTemplate);
                        showWarning(_txtNeedSelection);
                        _templateValid = true;
                        _selectionValid = false;
                        displayGrids();
                        enableAll(true);

                        showReadOnlyMessageIfNeeded();

                    } else {

                        Display.error("LinkupDefinitionDialog_21", _constants.locateTemplate_Failure());
                    }
                }

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 22, myException);
            }
        }
    };

    private ClickHandler handleNewFieldClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new AddLinkupFieldDialog(_fieldMapper)).show();

            } catch(Exception myException) {

                Display.error("LinkupDefinitionDialog", 23, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public LinkupDefinitionDialog(AbstractDataViewPresenter dvPresenterIn) {

        try {

            // Provide access to local copy of the DataView
            _dvPresenter = dvPresenterIn;

            //
            // Create object to handle linkup field mapping
            // and set up the field mapping grid
            //
            _fieldMapper = new LinkupFieldMapper(this, _dvPresenter, 460, 380, _txtHelpTarget + "#FieldDisplay"); //$NON-NLS-1$
            fieldMap = _fieldMapper.getGrid();
        /*
        newFieldButton = new MiniCyanButton();
        newFieldButton.setVisible(true);
        newFieldButton.setEnabled(true);
        newFieldButton.setWidth("422px");

        _fieldMapper.addGridButton(newFieldButton);
        */
         /* if (_fieldParmsSupported) {

            //
            // Create object to handler linkup parameter mapping
            // and set up the parameter mapping grid
            //
            _parameterMapper = new LinkupParameterMapper(_dvPresenter, 405, 180, _txtHelpTarget + "#ParameterGrid");
            templateParms = _parameterMapper.getGrid();

            //
            // Create object to handler linkup conditional field mapping
            // and set up the conditional field mapping grid
            //
            _conditionalMapper = new LinkupConditionalFieldMapper(_dvPresenter, 405, 130, _txtHelpTarget + "#FieldGrid");
            fieldParms = _conditionalMapper.getGrid();

        } else { */

            //
            // Create object to handler linkup parameter mapping
            // and set up the parameter mapping grid
            //

            _parameterMapper = new LinkupParameterMapper(this, _dvPresenter, 425, 200, _txtHelpTarget + "#ParameterGrid"); //$NON-NLS-1$
            templateParms = _parameterMapper.getGrid();
        /* } */

            // Identify callback handler for server requests involving linkups (add, update, delete)
            _linkupCallbackHandler = _dvPresenter.addHandler(handleLinkupResponse, DataChangeEvent.type);

            //
            // Set up available templates combobox
            //
            availableTemplates = setUpListBox(handleTemplateSelection);
            initAvailableTemplates();

            //
            // Set up available linkups combobox
            //
            availableLinkups = setUpListBox(handleLinkupSelection);
            recordAvailableLinkups();

            //
            // Link UI XML code to this file and all GWT to create remaining components
            //
            uiBinder.createAndBindUi(this);

            noNullsCheckBox.setValue(true);
            noNullsCheckBox.setText(_txtIgnoreNullParms);
            //
            //
            promptCheckBox.setValue(true);
            promptCheckBox.setText(_txtPromptToVerify);
            //
            //
            monitorCheckBox.setValue(true);
            monitorCheckBox.setText(_txtMonitorProgress);
            //
            //
            editOkCheckBox.setValue(false);
            editOkCheckBox.setText(_txtOkToChange);
            //
            // Initialize transient labels
            //
//        optionalParms.getElement().getStyle().setColor(Dialog.txtInfoColor);
//        optionalParms.getElement().setInnerText(_txtDelayedInputOption);
            fieldMapLabel.getElement().setInnerText(_txtMappingInstructions);
            //templateParmsLabel.getElement().setInnerText(_txtFillParameterGridLabel);
        /* if (_fieldParmsSupported) {
        fieldParmsLabel.getElement().setInnerText(_txtFieldGridLabel);
        } */

            //
            // Initialize permanent labels
            //
            availableLinkupLabel.getElement().setInnerText(_txtLinkupNameLabel);
            availableLinkupLabel.setVisible(false);
            linkupNameLabel.getElement().setInnerText(_txtLinkupNameLabel);
            linkupNameLabel.setVisible(false);
            linkupNamePanel.setVisible(false);
            availableTemplatesLabel.getElement().setInnerText(_txtAvailableTemplatesLabel);
            availableTemplatesLabel.setVisible(true);

            //
            // Disable the error/warning message for now
            //
            clearMessageDisplay();

            //--------------------------------------------------------------------------------
            // Effort being performed -- creating a new linkup or updating an existing one
            //
            // Set up the first radio button of a pair (create a new linkup)
            //
            newLinkup.setText(_txtCreateLinkupLabel);
            newLinkup.setVisible(true);
            newLinkup.addClickHandler(handleNewLinkupRadioButtonClick);
            newLinkup.setValue(true);
            newLinkup.setEnabled(false);

            //
            // Set up the second radio button of a pair (update or delete an existing linkup)
            //
            existingLinkup.setText(_txtEditLinkupLabel);
            existingLinkup.setVisible(true);
            existingLinkup.addClickHandler(handleExistingLinkupRadioButtonClick);
            existingLinkup.setValue(false);
            existingLinkup.setEnabled(false);
            //--------------------------------------------------------------------------------
            // Mode in which data is returned for merging -- all rows or set of unique rows
            //
            // Set up the first radio button of a pair (create a new linkup)
            //
            allRows.setText(_txtReturnAllRows);
            allRows.setVisible(true);
            allRows.setValue(_returnAllRows);
            allRows.setEnabled(true);
            //
            // Set up the second radio button of a pair (update or delete an existing linkup)
            //
            uniqueRows.setText(_txtReturnUniqueRows);
            uniqueRows.setVisible(true);
            uniqueRows.setValue(!_returnAllRows);
            uniqueRows.setEnabled(true);
            //--------------------------------------------------------------------------------
            dataModeLabel.setText(_txtDataModeLabel);
            _isNew = true;


            //
            // Set up the advanced button (visible only when a template has been selected)
            //
            advancedButton = new AmberButton(_txtAdvancedButton);
            advancedButton.addClickHandler(handleAdvancedButtonClick);
            if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isShowAdvancedParameter()){
                advancedButton.setVisible(true);
                advancedButton.setEnabled(false);
            } else {
                advancedButton.setVisible(false);
                advancedButton.setEnabled(false);
            }
            dialog.addRightControl(advancedButton);


            renameButton.addClickHandler(handleRenameButtonClick);
            restoreButton.addClickHandler(handleRestoreButtonClick);

            //
            // Set up the delete button (visible only when viewing an existing linkup)
            //
            deleteButton = new RedButton(_txtDeleteButton);
            deleteButton.addClickHandler(handleDeleteButtonClick);
            deleteButton.setVisible(false);
            deleteButton.setEnabled(false);
            dialog.addRightControl(deleteButton);


            //
            // Set up the action button for either "Add" -- new linkup
            // or "Update" -- existing linkup
            //
            dialog.getActionButton().setText(actionButtonText());
            dialog.getActionButton().addClickHandler(handleActionButtonClick);
            dialog.getActionButton().setEnabled(false);

            //
            // Set up the cancel button -- it is always active
            //
            dialog.getCancelButton().setText(_txtExitButton);
            dialog.getCancelButton().addClickHandler(handleCancelButtonClick);
            dialog.getCancelButton().setEnabled(true);

            //
            // Set up handlers to capture changes in the linkup name
            // in order to recognize conflicts with existing linkups
            // when naming a new linkup map
            //
            newLinkupName.addKeyUpHandler(handleNewLinkupNameKeyUp);
            newLinkupName.addDropHandler(handleNewLinkupNameDrop);
            newLinkupName.addChangeHandler(handleNewLinkupNameChange);

            //
            // Set up handler for creating new linkup field
            //
            newFieldButton.addClickHandler(handleNewFieldClick);

            dialog.defineHeader(_txtDialogTitle, _txtHelpTarget, true);

            // If the template list has been initialized from the server
            // enable the controls, otherwise continue to wait
            grantUserAccess();

            //LinkIn values
            {
                int length = 0;
                try {
                    if(!Strings.isNullOrEmpty(linkinValue)) {
                        length = linkinValue.split(",").length;
                    }
                } catch (Exception e) {

                }
                linkinButton = new com.github.gwtbootstrap.client.ui.Button("Linkin Fields"+"("+ length +")");
                {
                    linkinButton.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            final ArrayList<FieldDef> fieldDefs = Lists.newArrayList(_dvPresenter.getResource().getFieldList());
                            fieldDefs.removeIf(new Predicate<FieldDef>() {
                                @Override
                                public boolean test(FieldDef fieldDef) {
                                    switch (fieldDef.getFieldType()) {

                                        case COLUMN_REF:
                                            break;
                                        case SCRIPTED:
                                            return true;
                                        case STATIC:
                                            return true;
                                        case LINKUP_REF:
                                            break;
                                        case DERIVED:
                                            return true;
                                        case UNMAPPED:
                                            return true;
                                    }
                                    if (_selectedLinkup != null) {
                                        for (LooseMapping looseMapping : _selectedLinkup.fieldsMap) {
                                            if (looseMapping.getMappedLocalId() == fieldDef.getLocalId()) {
                                                return true;
                                            }
                                        }
                                    }
                                    return false;
                                }
                            });
                            if (Strings.isNullOrEmpty(linkinValue)) {
                                linkinValue = "";
                            }
                            FieldDefComboBox fdbc = new FieldDefComboBox();
                            final DualListBoxDialog dualListBoxDialog = new DualListBoxDialog();
                            dualListBoxDialog.show();

                            List<String> split = Splitter.on(",").splitToList(linkinValue);

                            for (FieldDef fieldDef : fieldDefs) {
                                if (!split.contains(fieldDef.getLocalId())) {
                                    dualListBoxDialog.addAvailableField(fieldDef);
                                }else{
                                    dualListBoxDialog.addSelectedField(fieldDef);

                                }
                            }
                            dualListBoxDialog.getActionButton().addClickHandler(new ClickHandler() {
                                @Override
                                public void onClick(ClickEvent event) {
                                    ArrayList<FieldDef> selectedFieldDefs = Lists.newArrayList(dualListBoxDialog.getSelectedFieldDefs());
                                    ArrayList<String> strings = Lists.newArrayList();
                                    for (FieldDef selectedFieldDef : selectedFieldDefs) {
                                        strings.add(selectedFieldDef.getLocalId());
                                    }
                                    Joiner joiner = Joiner.on(",");
                                    linkinValue = joiner.join(strings);
                                    dualListBoxDialog.close();
                                    if (Splitter.on(",").splitToList(linkinValue).size()>1) {
                                        MultiFieldDef multiFieldDef = new MultiFieldDef();
                                        multiFieldDef.setFieldName(_constants.multipleValues());
                                        fdbc.setValue(multiFieldDef);
                                    }
                                    updateLinkinButtonText();
                                }
                            });
                            dualListBoxDialog.getCancelButton().addClickHandler(new ClickHandler() {
                                @Override
                                public void onClick(ClickEvent event) {
                                    dualListBoxDialog.close();
                                }
                            });

                            dualListBoxDialog.show();
                            com.github.gwtbootstrap.client.ui.Button clearButton = new com.github.gwtbootstrap.client.ui.Button();
                            clearButton.setText(_constants.clear());
                        }
                    });
                }
                ((ValidatingDialog) dialog).addLeftControl(linkinButton);

            }

        } catch(Exception myException) {

            Display.error("LinkupDefinitionDialog", 24, myException);
        }
    }

    public void hide() {

        dialog.hide();
    }

    @Override
    public void showWithResults(KnowsParent childIn) {

        if (null != _templateSelector) {

            retrieveTemplateSelection();
            _templateSelector.destroy();
        }
        show();
    }

    @Override
    public void selectionChange(Object rowDataIn) {

        try {

            if ((null == rowDataIn) || (rowDataIn instanceof FieldToFieldMapStore)) {

                int myCount = _fieldMapper.getSelectionCount();

                _selectionValid = _fieldMapper.haveValidSelection();

                if (0 < myCount) {

                    if (_selectionValid) {

                        clearMessageDisplay();

                        showReadOnlyMessageIfNeeded();

                    } else {

                        showError(_txtMappingMissing);
                    }
                } else {

                    showWarning(_txtNeedSelection);
                }

                if (null != rowDataIn) {

                    setActionButtons();
                }
            }

            if ((null == rowDataIn) || (rowDataIn instanceof FieldToLabelMapStore)) {

                String myErrorString = _parameterMapper.checkIntegrity();

                if ((null != rowDataIn) || _selectionValid) {

                    if (null != myErrorString) {

                        _selectionValid = false;
                        showError(myErrorString);

                    } else {

                        _selectionValid = true;
                        clearMessageDisplay();

                        showReadOnlyMessageIfNeeded();
                    }
                }

                setActionButtons();
            }

        } catch(Exception myException) {

            Display.error("LinkupDefinitionDialog", 25, myException);
        }
    }

    public void rowComplete(Object dataRowIn) {

    }

    //
    //
    //
    public void show() {

        try {

            dialog.setCallBack(this);
            dialog.show(70);

        } catch(Exception myException) {

            Display.error("LinkupDefinitionDialog", 26, myException);
        }
    }

    //
    //
    //
    public void checkValidity() {

        if (_isNew) {

            checkLinkupName(newLinkupName.getText());
        }

        if (_linkupNameValid && _templateValid) {

            selectionChange(null);
        }
        setActionButtons();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Remove any handlers
    //
    protected void finalize( ) {

        if (null != _linkupCallbackHandler) {
            _linkupCallbackHandler.removeHandler();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Check for conflict between presumably new linkup name and existing linkups
    //
    private void checkLinkupName(String nameIn) {

        _linkupNameValid = false;

        if ((null != nameIn) && (0 < nameIn.length())) {
            if (_linkupNameMap.containsKey(nameIn)) {
                showError(_txtEnterLinkupName);
                newLinkupName.getElement().getStyle().setColor(Dialog.txtErrorColor);
            } else {
                _linkupNameValid = true;
                newLinkupName.getElement().getStyle().setColor(Dialog.txtLabelColor);

                showReadOnlyMessageIfNeeded();
            }
        } else {

            showError(_txtEnterLinkupName);
        }
    }

    //
    // Initialize widgets after selecting the "new linkup" radio button
    //
    private void initializeNewValues() {
        resetMode();
        checkLinkupName(newLinkupName.getText());
        setActionButtons(true);
        if(availableLinkups.getStore().size() == 0){
            existingLinkup.setEnabled(false);
        } else {
            existingLinkup.setEnabled(true);
        }
    }

    //
    // Initialize widgets after selecting the "existing linkup" radio button
    //
    private void initializeExistingValues() {
        String myLinkupName = newLinkupName.getValue();

        // If the free text name entered while in "new linkup" mode
        // matches an existing linkup definition
        // initialize the proper widgets and data stores
        if (isExistingLinkup(myLinkupName)) {

            _selectedLinkup = _linkupNameMap.get(myLinkupName);
            _linkupNameValid = true;
            noNullsCheckBox.setValue(_selectedLinkup.getNoNulls());
            promptCheckBox.setValue(_selectedLinkup.getPrompt());
            monitorCheckBox.setValue(_selectedLinkup.getMonitor());
            editOkCheckBox.setValue(_selectedLinkup.getEditOk());
            linkinValue = _selectedLinkup.getLinkin();
            enableAll(false);
            _linkupName = myLinkupName;
            availableLinkups.setSelectedValue(myLinkupName);
            initLinkupFields();
        } else {
            if(availableLinkups.getStore().size() > 0){

                myLinkupName = null;
                availableLinkups.setValue(null);
                _linkupName = null;
                _selectedLinkup = null;
                _linkupNameValid = false;
                noNullsCheckBox.setValue(true);
                promptCheckBox.setValue(true);
                monitorCheckBox.setValue(true);
                editOkCheckBox.setValue(false);
                setLinkinValue("");
/*
                myLinkupName = availableLinkups.getItemText(0);
                availableLinkups.setSelectedValue(myLinkupName);
                _linkupName = myLinkupName;
                _selectedLinkup = _linkupNameMap.get(myLinkupName);
                _linkupNameValid = true;
                noNullsCheckBox.setValue(_selectedLinkup.getnoNulls());
                promptCheckBox.setValue(_selectedLinkup.getPrompt());
                editOkCheckBox.setValue(_selectedLinkup.getEditOk());
*/
                enableAll(false);
                // initLinkupFields();

            } else {
                availableLinkups.clear();
            }
        }
        // Swap combobox for textbox and enable GUI controls
        adjustDisplayForMode(true);
        setActionButtons(true);
    }

    private void resetMode() {

        newLinkupName.setText(""); //$NON-NLS-1$
        _autoName = null;
        noNullsCheckBox.setValue(true);
        promptCheckBox.setValue(true);
        monitorCheckBox.setValue(true);
        editOkCheckBox.setValue(false);
        adjustDisplayForMode(true);
    }

    //
    // Reset template dependent widgets and data stores
    //
    private void resetTemplateValues() {
        availableTemplates.setValue(null);
        if (null != templateParms) {

            templateParms.getStore().clear();
        }
        if (null != fieldMap) {

            fieldMap.getStore().clear();
        }
        _fieldMapper.resetGrid();
        setLinkinValue("");

        /* if (_fieldParmsSupported) {

            fieldParms.getStore().clear();
        } */
    }
    
    //
    // Enable GUI controls after second call
    // (Called by both constructor and 
    //
    private void grantUserAccess() {
        if (_initialized) {
            enableAll(true);
            
            //
            // Give the focus to the text box for entering the new linkup name
            //
            newLinkupName.setFocus(true);
            newLinkupName.selectAll();
        }
        _initialized = true;
    }
    
    //
    //
    //
    private void enableAll(boolean stateIn) {

        if ((null != _linkupNameMap) && (0 < _linkupNameMap.size())) {

            existingLinkup.setEnabled(stateIn);

        } else {

            existingLinkup.setEnabled(false);
            existingLinkup.setValue(false);
            newLinkup.setValue(true);
        }
        if (null != templateParms) {

            templateParms.setEnabled(stateIn && _hasParameters);
        }
        if (null != fieldMap) {

            fieldMap.setEnabled(stateIn && notReadOnly());
        }
        newFieldButton.setEnabled(stateIn && notReadOnly());
        deleteButton.setEnabled(stateIn && notReadOnly());
        setActionButtons(stateIn);

        adjustDisplayForMode(stateIn);

        /* if (_fieldParmsSupported) {

            fieldParms.setEnabled(stateIn);
        } */
    }

    private void adjustDisplayForMode(boolean stateIn) {

        clearMessageDisplay();
        allRows.setValue(_returnAllRows);
        uniqueRows.setValue(!_returnAllRows);

        if (_isNew) {

            String myName = newLinkupName.getText();

            availableTemplates.setEnabled(stateIn);
            linkupNameLabel.setVisible(true);
            linkupNamePanel.setVisible(true);
            newLinkupName.setVisible(true);

            availableLinkupsDropDownPanel.setVisible(false);
            availableLinkupLabel.setVisible(false);
            availableLinkups.setEnabled(false);
            availableLinkups.setVisible(false);
            renameButton.setVisible(false);
            restoreButton.setVisible(false);

            if ((null == _templateName) || (0 == _templateName.length())) {

                newLinkupName.setEnabled(false);
                showError(_txtSelectLinkupTemplate);

            } else {

                newLinkupName.setEnabled(stateIn);

                if ((null == myName) || (0 == myName.length())) {

                    showError(_txtEnterLinkupName);

                } else {

                    showReadOnlyMessageIfNeeded();
                }
            }
            templateDropDownPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

        } else {

            String myName = availableLinkups.getSelectedText();

            availableTemplates.setEnabled(stateIn && notReadOnly());
            linkupNameLabel.setVisible(false);
            linkupNamePanel.setVisible(false);
            newLinkupName.setVisible(false);
            newLinkupName.setEnabled(false);

            availableLinkupsDropDownPanel.setVisible(true);
            availableLinkupLabel.setVisible(true);
            availableLinkups.setEnabled(stateIn);
            availableLinkups.setVisible(true);

            if ((null == myName) || (0 == myName.length())) {

                showError(_txtSelectLinkup);

            } else {

                showReadOnlyMessageIfNeeded();
            }
            if (null != _selectedLinkup) {

                allRows.setValue(_selectedLinkup.getReturnAll());
                uniqueRows.setValue(!_selectedLinkup.getReturnAll());
            }
            templateDropDownPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        }
    }

    //
    //
    //
    private LinkupDataTransfer buildMapDef(DataOperation modeIn) {
        
        LinkupDataTransfer myResults = null;

        LinkupDataTransfer myLinkupData = new LinkupDataTransfer(_dvPresenter.getUuid(), _selectedLinkup);
        
        if (_fieldMapper.extractGridData(myLinkupData))
        {
            if ((!_hasParameters) || (DataOperation.DELETE == modeIn)
                    || _parameterMapper.extractGridData(myLinkupData.getObjectOfInterest()))
            {
                /* if (fieldParamsSupported) {
                if ((DataChangeEvent.DELETE == modeIn)
                        || (_conditionalMapper.extractGridData(myLinkupData.getObjectOfInterest()))
                {
                /* } */
                //
                // Extract linkup definition from the request data
                //
                LinkupMapDef myLinkupMap = myLinkupData.getObjectOfInterest();
                List<LinkupExtender> myList = null;

                if (DataOperation.CREATE == modeIn) {
                    myLinkupMap.setUuid(UUID.uuid());
                    myLinkupMap.setOrdinal(_nextOrdinal);
                    myLinkupMap.setUseCount(0);
                } else {
                    myLinkupMap.setUuid(_selectedLinkup.getUuid());
                    myLinkupMap.setOrdinal(_selectedLinkup.getOrdinal());
                    myList = _selectedLinkup.getLinkupExtenders();
                    myLinkupMap.setUseCount(_selectedLinkup.getUseCount());
                }
                if (newLinkupName.isEnabled()) {

                    myLinkupMap.setLinkupName(newLinkupName.getValue());

                } else {

                    myLinkupMap.setLinkupName(_selectedLinkup.getLinkupName());
                }
                myLinkupMap.setReturnAll(allRows.getValue());

                //
                // Extract extender data from staging
                //
                if (null != _extenderList) {

                    if (0 < _extenderList.size()) {

                        if (null == myList) {

                            myList = new ArrayList<LinkupExtender>();
                        }

                        for (int i = 0; _extenderList.size() > i; i++) {

                            ((LinkupExtenderStage)_extenderList.get(i)).finalize(myList);
                        }

                        if (0 == myList.size()) {

                            myList = null;
                        }
                    } else {

                        myList = null;
                    }
                }

                myLinkupMap.setLinkupExtenders(myList);

                //
                // Finish building out linkup definition
                //
                myLinkupMap.setNoNulls(noNullsCheckBox.getValue());
                myLinkupMap.setPrompt(promptCheckBox.getValue());
                myLinkupMap.setMonitor(monitorCheckBox.getValue());
                myLinkupMap.setEditOk(editOkCheckBox.getValue());
                myLinkupMap.setTemplateOwner(_templateOwner);
                if (null != _templateDef) {
                    myLinkupMap.setTemplateUuid(_templateDef.getUuid());
                    myLinkupMap.setTemplateName(_templateDef.getName());
                }
                if (linkinValue != null) {
                    myLinkupMap.setLinkin(linkinValue);
                }
                else{
                    myLinkupMap.setLinkin("");
                }

                myResults = myLinkupData;

                /* if (fieldParamsSupported) {
                } else {
                    myLinkupData = null;
                }
                } */
            } else {
                myLinkupData = null;
            }
        } else {
            myLinkupData = null;
        }
        
        return myResults;
    }
    
    //
    //
    //
    private void addLinkup() {
        
        LinkupDataTransfer myLinkupData = buildMapDef(DataOperation.CREATE);

        if (null != myLinkupData) {

            enableAll(false);
            _dvPresenter.addLinkup(myLinkupData);
        }
    }

    //
    //
    //
    private void removeLinkup() {

        LinkupDataTransfer myLinkupData = buildMapDef(DataOperation.DELETE);

        if (null != myLinkupData) {

            enableAll(false);
            _dvPresenter.removeLinkup(myLinkupData);

        }
    }

    //
    //
    //
    private void updateLinkup() {
        
        LinkupDataTransfer myLinkupData = buildMapDef(DataOperation.UPDATE);

        if (null != myLinkupData) {

            enableAll(false);
            _dvPresenter.updateLinkup(myLinkupData);
        }
    }

    //
    //
    //
    private CsiStringListBox setUpListBox(SelectionChangedHandler<String> selectionHandlerIn) {

        CsiStringListBox myListBox = new CsiStringListBox();

        myListBox.sortAscending();
        if (null != selectionHandlerIn) {
            myListBox.addSelectionChangedHandler(selectionHandlerIn);
        }
        myListBox.setEnabled(false);

        return myListBox;
    }

    //
    // Request data from the server to initialize the list of available templates
    //
    private void initAvailableTemplates() {

        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).listLinkupTemplateBasics();
            myVortexFuture.addEventHandler(handleInitAvailableTemplatesServerResponse);
            
        } catch (Exception myException) {

            Display.error("LinkupDefinitionDialog", 27, myException);
        }
    }
    
    //
    // Initialize various structures with the list of
    // linkup definitions belonging to the current dataview
    //
    private void recordAvailableLinkups() {

        List<LinkupMapDef> myLinkups = _dvPresenter.getDataView().getMeta().getLinkupDefinitions();
        availableLinkups.clear();
        _linkupNameMap.clear();
        _nextOrdinal = 0;
        for (LinkupMapDef myLinkup :myLinkups) {
            String myName = myLinkup.getLinkupName().trim();
            int myOrdinal = myLinkup.getOrdinal();
            availableLinkups.addItem(myName);
            _linkupNameMap.put(myName, myLinkup);
            if (myOrdinal >= _nextOrdinal) {
                _nextOrdinal = myOrdinal + 1;
            }
        }
        availableLinkups.setValue(null);
    }

    //
    // Request data from the server to initialize the grids after selecting a linkup to edit
    //
    private void initLinkupFields() {
        
        VortexFuture<Response<String, TemplateResponse>> vortexFuture = WebMain.injector.getVortex().createFuture();

        // Invalidate current information
        _templateValid = false;
        _selectionValid = false;
        _extenderList = null;
        _templateOwner = _selectedLinkup.getTemplateOwner();
        
        // Disable grid data -- it is being replaced
        disableGrids();

        try {
            vortexFuture.addEventHandler(handleInitLinkupFieldsServerResponse);
            vortexFuture.execute(DataViewDefActionsServiceProtocol.class).getLinkupTemplate(_selectedLinkup);

        } catch (Exception myException) {

            Display.error("LinkupDefinitionDialog", 28, myException);
        }
    }
    
    //
    // Load values into mapping grid after selecting a template for linkup
    //
    private void initTemplateFields(ResourceBasics resourceIn) {
        
        VortexFuture<Response<String, TemplateResponse>> vortexFuture = WebMain.injector.getVortex().createFuture();

        // Invalidate current information
        _templateValid = false;
        _selectionValid = false;
        _templateOwner = resourceIn.getOwner();
        
        // Disable grid data -- it is being replaced
        disableGrids();

        try {
            vortexFuture.execute(DataViewDefActionsServiceProtocol.class).getLinkupTemplate(resourceIn.getUuid());
            vortexFuture.addEventHandler(handleInitTemplateFieldsServerResponse);
            
        } catch (Exception myException) {

            Display.error("LinkupDefinitionDialog", 29, myException);
        }
    }
    
    //
    // Force the Delete and Update/New buttons to a particular state
    //
    private void setActionButtons(boolean stateIn) {
        
        _dialogButtonsActive = stateIn;
        setActionButtons();

    }
    
    //
    // Determine the proper states for each 
    // the delete button (visibility and enabled status)
    // the action button (display text ["And" or "Update"] and enabled status)
    // based upon the states of other widgets
    //
    // The cancel button is always visible and enabled
    //
    private void setActionButtons() {

        Button myActionButton = dialog.getActionButton();
        boolean myDeleteVisible = existingLinkup.getValue();
        boolean myDeleteState = _dialogButtonsActive && isExistingLinkup() && notReadOnly();
//        boolean myUpdateState = _dialogButtonsActive && _linkupNameValid && _templateValid && _selectionValid && notBlocked() && notReadOnly();
        boolean myUpdateState = _dialogButtonsActive && _linkupNameValid && _templateValid && _selectionValid && notBlocked();
        boolean myAdvancedState = _dialogButtonsActive && _templateValid;
       
        myActionButton.setText(actionButtonText());
        myActionButton.setEnabled(myUpdateState);

        if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isShowAdvancedParameter()){
            advancedButton.setEnabled(myAdvancedState);
        } else {
            advancedButton.setEnabled(false);
            advancedButton.setVisible(false);
        }
        deleteButton.setEnabled(myDeleteState);
        deleteButton.setVisible(myDeleteVisible);
    }

    private boolean notBlocked() {

        return (!(_fieldMapper.isBlocked() || _parameterMapper.isBlocked()));
    }

    private boolean notReadOnly() {

        return true;
//        return (_isNew || ((null != _selectedLinkup) && (!_selectedLinkup.isInUse())));
    }

    //
    // Determine the display text for the action button based
    // upon whether a new linkup definition is being created
    // or an existing linkup definition is being accessed
    //
    private String actionButtonText() {
        return existingLinkup.getValue() ? _txtUpdateButton : _txtAddButton; 
    }
    
    //
    // Check to see if a linkup exists with the identified name
    //
    private boolean isExistingLinkup(String nameIn) {
         return (null != nameIn)
                 && (_linkupNameMap.containsKey(nameIn));
    }
    
    //
    // Check to see if the combobox contains a valid selection
    //
    private boolean isExistingLinkup() {
        return (null != _linkupName) && (0 < _linkupName.length());
    }
    
    //
    //
    //
    private void disableGrids() {

        if (null != templateParms) {

            templateParms.setEnabled(false);
        }
        if (null != fieldMap) {

            fieldMap.setEnabled(false);
        }
        newFieldButton.setEnabled(false);

        /* if (_fieldParmsSupported) {

            fieldParms.setEnabled(false);
        } */
    }

    //
    //
    //
    private void displayGrids() {

        _hasParameters = false;

        noNullsCheckBox.setVisible(true);
        promptCheckBox.setVisible(true);
        monitorCheckBox.setVisible(true);
        editOkCheckBox.setVisible(true);
        newFieldButton.setText(Dialog.txtNewFieldButton);

        if (null != _templateDef) {

            List<QueryParameterDef> myList = _templateDef.getDataSetParameters();

            _hasParameters = ((null != myList) && (0 < myList.size()));
        }

        if (_hasParameters) {

            removeNoParametersNotification();
            templateParmsLabel.setVisible(false);
            //templateParmsLabel.getElement().setInnerText(_txtFillParameterGridLabel);

            /* if (_fieldParmsSupported) {

                templateParms.setHeight(180);
                fieldParms.setHeight(120);

            } else { */

            if (null != templateParms) {

                templateParms.setHeight(200);
            }
            /* } */

            
        } else {

            displayNoParametersNotification();
            templateParmsLabel.getElement().setInnerText(_txtIgnoreParameterGridLabel);
            templateParmsLabel.setVisible(true);

            /* if (_fieldParmsSupported) {

                templateParms.setHeight(80);
                fieldParms.setHeight(220);

            } else { */

            if (null != templateParms) {

                templateParms.setHeight(180);            /* } */
            }


        }
        if(fieldMap != null){
            ListStore<FieldToFieldMapStore> store = fieldMap.getStore();
            List<FieldToFieldMapStore> selection = new ArrayList<FieldToFieldMapStore>();
            for(FieldToFieldMapStore item : store.getAll()){
                if(item.getMappingField() != null && item.getMappingField().getLocalId() != null){
                    item.mapField = true;
                    selection.add(item);
                } else {
                    item.mapField = false;
                }
            }

            fieldMap.getSelectionModel().select(selection, false);
        }
        if(null != templateParms){
            ListStore<FieldToLabelMapStore> store = templateParms.getStore();
            List<FieldToLabelMapStore> selection = new ArrayList<FieldToLabelMapStore>();
            for(FieldToLabelMapStore item : store.getAll()){
                if(item.getMappingField() != null && item.getMappingField().getLocalId() != null){
                    item.mapField = true;
                    selection.add(item);
                } else {
                    item.mapField = false;
                }
            }

            templateParms.getSelectionModel().select(selection, false);
            templateParms.setVisible(true);
            templateParms.setEnabled(_hasParameters);
        }
//        templateParmsLabel.setVisible(true);
        //optionalParms.setVisible(true);
        newFieldButton.setVisible(true);
        newFieldButton.setEnabled(true);

        /* if (_fieldParmsSupported) {

            fieldParmsLabel.setVisible(true);
            fieldParms.setVisible(true);
            fieldParms.setEnabled(true);

        }

            fieldParmsLabel.setVisible(false);
        } */
    }
    
    private void displayNoParametersNotification() {
        
//        if (null == noParametersPanel) {
//            
//            noParametersPanel = new HorizontalPanel();
//            noParametersPanel.setWidth("420px"); //$NON-NLS-1$
//            noParametersPanel.setHeight("60px"); //$NON-NLS-1$
//            topPanel.add(noParametersPanel);
//            topPanel.setWidgetLeftWidth(noParametersPanel, 20, Unit.PX, 405, Unit.PX);
//            topPanel.setWidgetTopHeight(noParametersPanel, 160, Unit.PX, 60, Unit.PX);
//            noParametersLabel = new Label(_txtNoParametersLabel);
//            noParametersPanel.add(noParametersLabel);
//            noParametersPanel.setCellHorizontalAlignment(noParametersLabel, HasHorizontalAlignment.ALIGN_CENTER);
//            noParametersPanel.setCellVerticalAlignment(noParametersLabel, HasVerticalAlignment.ALIGN_MIDDLE);
//        }
    }
    
    private void removeNoParametersNotification() {
        
//        if (null != noParametersPanel) {
//            
//            topPanel.remove(noParametersPanel);
//            noParametersPanel = null;
//            noParametersLabel = null;
//        }
    }

    private void showWarning(String messageIn) {

        fieldMapError.getElement().getStyle().setColor(Dialog.txtWarningColor);
        fieldMapError.getElement().setInnerText(messageIn);
        fieldMapLabel.setVisible(false);
        fieldMapError.setVisible(true);
    }

    private void showError(String messageIn) {

        fieldMapError.getElement().getStyle().setColor(Dialog.txtErrorColor);
        fieldMapError.getElement().setInnerText(messageIn);
        fieldMapLabel.setVisible(false);
        fieldMapError.setVisible(true);
    }

    private void showMessage(String messageIn) {

        fieldMapError.getElement().getStyle().setColor(Dialog.txtInfoColor);
        fieldMapError.getElement().setInnerText(messageIn);
        fieldMapLabel.setVisible(false);
        fieldMapError.setVisible(true);
    }

    private void clearMessageDisplay() {

        fieldMapError.setVisible(false);
        fieldMapLabel.setVisible(true);
    }


    private void showReadOnlyMessageIfNeeded() {

        if (!notReadOnly()) {

            showWarning(_txtReadOnly);
        }
    }

    private void generateLinkupName() {

        if (_linkupNameMap.containsKey(_templateName)) {

            _autoName = SynchronizeChanges.generateName(_templateName + " {:0}", _linkupNameMap, false); //$NON-NLS-1$

        } else {

            _autoName = _templateName;
        }
        newLinkupName.setText(_autoName);
        newLinkupName.setFocus(true);
        newLinkupName.selectAll();
    }
    private class MultiFieldDef extends FieldDef {

    }

    private void applyTemplateSelection(DataViewDef templateIn) {

        _templateDef = templateIn;
        _extenderList = null;
        _templateName = _selectedLinkup.getTemplateName();
        availableTemplates.setSelectedValue(_templateName);
        availableTemplates.setEmptyText(_templateName);
        newLinkupName.setValue(_linkupName);
        _fieldMapper.resetFieldList();
        _fieldMapper.initGridFields(_selectedLinkup, _templateDef, _dvPresenter.getDataView().getMeta());
        _parameterMapper.resetFieldList();
        _parameterMapper.initGridFields(_selectedLinkup, _templateDef);
        _templateValid = true;
        _selectionValid = true;
        _linkupNameValid = true;
        clearMessageDisplay();
        displayGrids();
        enableAll(true);
        generateLinkupName();
        showReadOnlyMessageIfNeeded();
    }

    private void retrieveTemplateSelection() {

        DataViewDef myTemplate = _templateSelector.getTemplate();

        if (null != myTemplate) {

            applyTemplateSelection(myTemplate);

        } else {

            Display.error("LinkupDefinitionDialog_30", _constants.locateTemplate_Failure());
        }
    }
}

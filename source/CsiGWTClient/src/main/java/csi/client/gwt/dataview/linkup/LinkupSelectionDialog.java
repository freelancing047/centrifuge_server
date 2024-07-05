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
package csi.client.gwt.dataview.linkup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.event.HideHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.event.RefreshEvent;
import com.sencha.gxt.widget.core.client.event.RefreshEvent.RefreshHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.wizards.ParameterWizard;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.staging.LinkupExtenderStage;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.LinkupMode;
import csi.server.common.enumerations.ResourceChoiceCriteria;
import csi.server.common.linkup.LinkupHelper;
import csi.server.common.linkup.LinkupRequest;
import csi.server.common.linkup.LinkupResponse;
import csi.server.common.linkup.LinkupValidationReport;
import csi.server.common.linkup.TemplateResponse;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupExtender;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;

/**
 * @author Centrifuge Systems, Inc.
 */
public class LinkupSelectionDialog extends WatchingParent implements SelectionChangeResponder, ValidityCheck {
   private static final Logger LOG = Logger.getLogger(LinkupSelectionDialog.class.getName());
   
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface LinkupSelectionDialogUiBinder extends UiBinder<Widget, LinkupSelectionDialog> {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField
    ValidatingDialog dialog;

    @UiField
    FullSizeLayoutPanel topPanel;
    @UiField
    AbsolutePanel greyPanel;
    @UiField
    AbsolutePanel parameterSetSelectionPanel;
    @UiField
    VerticalPanel parameterSetDataPanel;

    @UiField
    Label templateParmsLabel;
    /* if (_fieldParmsSupported) {
    @UiField
    Label fieldParmsLabel;
    } */
    @UiField
    Label linkupDefNameLabel;
    @UiField
    Label templateNameLabel;
    @UiField
    Label nothingSavedLabel;
    @UiField
    Label noMappingLabel;
    @UiField
    Label noExtendersLabel;

    @UiField
    InlineLabel linkupDefName;
    @UiField
    InlineLabel templateName;
    @UiField
    InlineLabel newDataviewNameLabel;

    @UiField
    TextBox newDataviewName;

    @UiField
    RadioButton useDefault;
    @UiField
    RadioButton useExtenders;
    @UiField
    RadioButton mergeData;
    @UiField
    RadioButton spinOff;
    @UiField
    RadioButton spinUp;

    @UiField
    CheckBox setDisabledCheckBox;

    @UiField(provided = true)
    Grid<LinkupFieldMapDisplayStore> fieldMap;

    @UiField(provided = true)
    Grid<FieldToLabelMapStore> templateParms;
    /* if (_fieldParmsSupported) {
    @UiField(provided = true)
    Grid<?> fieldParms;
    } */

    CheckBox ignoreNullValues;
    CheckBox miniIgnoreNulls;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static LinkupSelectionDialogUiBinder uiBinder = GWT.create(LinkupSelectionDialogUiBinder.class);

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final Integer _zero = new Integer(0);

    private final String _txtDialogTitle = _constants.linkupSelectionDialog_DialogTitle();
    private final String _txtFailureDialogTitle = _constants.linkupSelectionDialog_FailureDialogTitle();
    private final String _txtErrorDialogTitle = _constants.linkupSelectionDialog_ErrorDialogTitle();
    private final String _txtFillParameterGridLabel = _constants.linkupSelectionDialog_FillParameterGridLabel();
    private final String _txtIgnoreParameterGridLabel = _constants.linkupSelectionDialog_IgnoreParameterGridLabel();
//    private final String _txtFieldGridLabel = _constants.linkupSelectionDialog_FieldGridLabel();
    private final String _txtLinkupNameLabel = _constants.linkupSelectionDialog_LinkupNameLabel();
    private final String _txtTemplateNameLabel = _constants.linkupSelectionDialog_TemplateNameLabel();
    private final String _txtNewDataviewNameLabel = _constants.linkupSelectionDialog_NewDataviewNameLabel();
    private final String _txtDisabledCheckBox = _constants.linkupSelectionDialog_DisabledCheckBox();
    private final String _txtExecuteButton = _constants.dialog_ExecuteButton();
    private final String _txtUseDefaultRadioButton = _constants.linkupSelectionDialog_UseDefaultRadioButton();
    private final String _txtUseNodeEdgeRadioButton = _constants.linkupSelectionDialog_UseNodeEdgeRadioButton();
    private final String _txtMergeRadioButton = _constants.linkupSelectionDialog_MergeRadioButton();
    private final String _txtSpinOffRadioButton = _constants.linkupSelectionDialog_SpinOffRadioButton();
    private final String _txtSpinUpRadioButton = _constants.linkupSelectionDialog_SpinUpRadioButton();
    private final String _txtDataSelectionRequired = _constants.linkupSelectionDialog_DataSelectionRequired();
    private final String _txtNoLinkupInfo = _constants.linkupSelectionDialog_NoLinkupInfo();
    private final String _txtSuccessResponseTitle = _constants.linkupSelectionDialog_SuccessResponseTitle();
    private final String _txtNoChangesSaved = _constants.linkupSelectionDialog_NoChangesSaved();
    private final String _txtNoParametersSet = _constants.linkupSelectionDialog_NoParametersSet();
    private final String _txtNoExtendersLabel = _constants.linkupSelectionDialog_NoExtendersLabel();
    private final String _txtNoMappingLabel = _constants.linkupSelectionDialog_NoMappingLabel();
    private final String _txtNoParametersLabel = _constants.linkupSelectionDialog_NoParametersLabel();
    private final String _txtNoMappingsLabel = _constants.linkupSelectionDialog_NoMappingsLabel();
    private final String _txtHelpTarget = _constants.linkupSelectionDialog_HelpTarget();
    private final String _txtEverythingDisabled = _constants.linkupSelectionDialog_EverythingDisabled();
    private final String _txtNoDataviewNameTitle = _constants.linkupSelectionDialog_NoDataviewNameTitle();
    private final String _txtNoDataviewNameMessage = _constants.linkupSelectionDialog_NoDataviewNameMessage(Dialog.txtContinueButton, Dialog.txtCancelButton);
    private final String _txtTemplateDataError = _constants.error_RetrieveTemplateData_Title();
    private final String _txtDataviewListError = _constants.error_RetrieveDataviewList_Title();
    private final String _txtIgnoreNullValues = _constants.linkupSelectionDialog_IgnoreNullsCheckBox();
    private final static String _txtParameterTitle = _constants.parameterLinkupTitle();
    private final static String _txtParameterHelp = _constants.parameterLinkupHelpTarget();
    private final static String _txtParameterButton = Dialog.txtExecuteButton;
    private static final String _txtChoiceDialogNewButtonString = _constants.openDataviewDialog_Choice_NewButtonString();
    private static final String _txtChoiceDialogCurrentButtonString = _constants.openDataviewDialog_Choice_CurrentButtonString();
    private static final String _txtChoiceDialogTitle = _constants.openDataviewDialog_Choice_DialogTitle();
    private static final String _txtChoiceInfoString = _constants.openDataviewDialog_Choice_InfoString(_txtChoiceDialogNewButtonString, _txtChoiceDialogCurrentButtonString);

    private HorizontalPanel noParametersPanel = null;
    private HorizontalPanel noMappingsPanel = null;
    private Label noParametersLabel = null;
    private Label noMappingsLabel = null;
    private TabPanel parameterSetTabPanel = null;
    private Button executeButton;
    private Button cancelButton;

    DecisionDialog miniDialog = null;
    LayoutPanel miniInputPanel = null;
    RadioButton miniMergeData = null;
    RadioButton miniSpinOff = null;
    RadioButton miniSpinUp = null;
//    InlineLabel miniLabel = null;
    TextBox miniDataViewName = null;

    private HandlerRegistration _selectionHandler = null;

    private AbstractDataViewPresenter _dvPresenter;
    private DataViewDef _template = null;
    private LinkupMapDef _linkupMap;
    private List<LinkupExtender> _linkupExtenders = null;
    private List<LinkupExtender> _defaultExtenders = null;
    private List<LinkupExtender> _advancedExtenders = null;
    private List<Integer> _selectedRows = null;
    private Selection _selectedItems = null;
    private Visualization _visualization = null;
    private LinkupRequest _request = null;
    private LinkupFieldMapDisplay _fieldMapper = null;
    private LinkupParameterMapper _parameterMapper = null;
    /* if (_fieldParmsSupported) {
    private LinkupConditionalFieldMapper _conditionalMapper = null;
    } */
    private boolean _hasParameters = false;
    private boolean _hasExtenders = false;
    private boolean _hasMapping = false;
    private boolean _isReady = false;
    private boolean _usingExtenders = false;
    private LinkupMode _mode = LinkupMode.SPINOFF;
    private VisualizationDef _vizdef = null;
    private int _accessIndex = -1;
    private boolean _doFinalInit = false;
    private boolean _isDisabled = false;
    private boolean _swapCheckBoxValue = true;
    private boolean _newDataviewNameValid = false;
    private Map<String, Integer> _dataviewNameMap = null;
    private boolean _initialized = false;
    private DataView _newDataView = null;
    private String _dataViewUuid = null;
    private String _dataViewName = null;
    private boolean _moreData = false;
    private long _rowCount = 0L;

    private CanBeShownParent _this;
    private boolean _visible = false;

    private static final List<ButtonDef> _buttonList1a = Arrays.asList(

            new ButtonDef(i18n.linkupSelectionDialogExecuteButton(), ButtonType.SUCCESS),
            new ButtonDef(i18n.linkupSelectionDialogModifyButton(), ButtonType.PRIMARY)
    );

    private static final List<ButtonDef> _buttonList1b = Arrays.asList(

            new ButtonDef(i18n.linkupSelectionDialogExecuteButton(), ButtonType.SUCCESS)
    );

    private static final List<ButtonDef> _buttonList2 = Arrays.asList(

            new ButtonDef(_txtChoiceDialogCurrentButtonString),
            new ButtonDef(_txtChoiceDialogNewButtonString)
    );


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle response to request for current dataview list
    //
    private VortexEventHandler<List<String>> handleListDataviewNamesResponse = new AbstractVortexEventHandler<List<String>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            Dialog.showException(_txtDataviewListError, exceptionIn);
            return true;
        }
        @Override
        public void onSuccess(List<String> dataviewListIn) {

            try {

                _dataviewNameMap = new HashMap<String, Integer>();

                if ((null != dataviewListIn) && (0 < dataviewListIn.size())) {

                    for (String myDataviewName : dataviewListIn) {

                        _dataviewNameMap.put(myDataviewName, _zero);
                    }
                }
                if (_initialized) {
                    checkSetName(newDataviewName, newDataviewName.getText());
                    validateButtons();
                }

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 1, myException);
            }
        }
    };

    //
    // Monitor typing into the "newDataviewName" text field
    // and continually check for name conflicts
    //
    private KeyUpHandler handleNewDataviewNameKeyUp
            = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent eventIn) {

            try {

                checkSetName(newDataviewName, newDataviewName.getText());
                validateButtons();

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 2, myException);
            }
        }
    };

    //
    // Check for name conflicts whenever a text drag object
    // is dropped onto the "newDataviewName" text field
    //
    private DropHandler handleNewDataviewNameDrop
            = new DropHandler() {
        @Override
        public void onDrop(DropEvent eventIn) {

            try {

                checkSetName(newDataviewName, eventIn.getData("text/plain")); //$NON-NLS-1$
                validateButtons();

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 3, myException);
            }
        }
    };

    //
    // Monitor any change to the "newDataviewName" text field
    // and continually check for name conflicts
    //
    private ChangeHandler handleNewDataviewNameChange
            = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent eventIn) {

            try {

                checkSetName(newDataviewName, newDataviewName.getText());
                validateButtons();

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 4, myException);
            }
        }
    };

    //
    // Monitor typing into the "newDataviewName" text field
    // and continually check for name conflicts
    //
    private KeyUpHandler handleMiniNewDataviewNameKeyUp
            = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent eventIn) {

            try {

                checkSetName(miniDataViewName, miniDataViewName.getText());
                miniDialog.enableButton(1, (miniMergeData.getValue() || _newDataviewNameValid));

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 5, myException);
            }
        }
    };

    //
    // Check for name conflicts whenever a text drag object
    // is dropped onto the "newDataviewName" text field
    //
    private DropHandler handleMiniNewDataviewNameDrop
            = new DropHandler() {
        @Override
        public void onDrop(DropEvent eventIn) {

            try {

                checkSetName(miniDataViewName, eventIn.getData("text/plain")); //$NON-NLS-1$
                miniDialog.enableButton(1, (miniMergeData.getValue() || _newDataviewNameValid));

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 6, myException);
            }
        }
    };

    //
    // Monitor any change to the "newDataviewName" text field
    // and continually check for name conflicts
    //
    private ChangeHandler handleMiniNewDataviewNameChange
            = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent eventIn) {

            try {

                checkSetName(miniDataViewName, miniDataViewName.getText());
                miniDialog.enableButton(1, (miniMergeData.getValue() || _newDataviewNameValid));

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 7, myException);
            }
        }
    };

    private UserInputEventHandler<Integer> processLogon
    = new UserInputEventHandler<Integer>() {

        @Override
        public void onUserInput(UserInputEvent<Integer> eventIn) {

            try {

                if (!eventIn.isCanceled()) {

                    _request.refreshAuthorizationList();
                    executeRequest();
                }

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 8, myException);
            }
        }
    };

    //
    // Handle response to request to execute linkup
    private VortexEventHandler<Response<String, LinkupResponse>> handleLinkupResponse
    = new AbstractVortexEventHandler<Response<String, LinkupResponse>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            // Display error message.
            hideWatchBox();
            Dialog.showException(_txtFailureDialogTitle, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, LinkupResponse> responseIn) {

            try {

                hideWatchBox();
                if (responseIn.isAuthorizationRequired()) {

                    dialog.showCredentialDialogs(_request.getAuthorizationMap(), responseIn.getAuthorizationList(), processLogon);

                } else if (ResponseHandler.isSuccess(responseIn)) {

                    LinkupResponse myRespoonse = responseIn.getResult();
                    LinkupValidationReport myReport = (null != myRespoonse) ? myRespoonse.getValidationReport() : null;
                    boolean myOkFlag = (null != myReport) ? myReport.isOK() : false;

                    _rowCount = responseIn.getCount();
                    _moreData = responseIn.getLimitedData();

                    if (myOkFlag) {

                        if (LinkupMode.MERGE.equals(_request.getMode())) {

                            // Update client version of the DataView
                            //
                            try{
                                myRespoonse.updateDataView(_dvPresenter.getDataView());
                                _dvPresenter.onDataChange();
                                // Signal visualizations to refresh
                                //
                                for (Visualization myVisualization : _dvPresenter.getVisualizations()) {

                                    try{
                                        if (myVisualization instanceof Graph) {
                                            Graph graph = (Graph) myVisualization;
                                            if(graph.isLoaded()){
                                                graph.getGraphSurface().refresh();
                                                graph.getLegend().load();
                                                graph.getGraphSurface().getToolTipManager().removeAllToolTips();
                                                graph.hideIfEmpty();
                                            }
                                        } else {
                                            if (myVisualization instanceof MapPresenter) {
                                            	((MapPresenter)myVisualization).handleLinkup();
                                            } else {
                                                myVisualization.saveViewStateToVisualizationDef();
                                                Selection selection = myVisualization.getVisualizationDef().getSelection();
                                                if(!(selection instanceof NullSelection)) {
                                                    myVisualization.saveOldSelection(selection);
                                                }
                                                myVisualization.reload();
                                            }
                                        }
                                    } catch(Exception myException){
                                        LOG.warning("Visualization failed to load: "
                                                + myVisualization.getName() + myException.getMessage());
                                    }
                                }
                                if (_linkupMap.getMonitor()) {

                                    if (_moreData) {

                                        Dialog.showWarning(_constants.abstractDataViewPresenter_MoreDataTitle(),
                                                _constants.abstractDataViewPresenter_MoreDataMessage(_rowCount));

                                    } else {

                                        Dialog.showSuccess(_txtSuccessResponseTitle,
                                                _constants.linkupSelectionDialog_SuccessResponseMessage(_rowCount));
                                    }
                                }

                            } catch(Exception myException){
                               LOG.warning("Caught exception updating DataView after linkup" + myException.getMessage());
                            }
                            dialog.destroy();

                        } else {

                            _newDataView = myRespoonse.getNewDataView();
                            WebMain.injector.getMainPresenter().beginOpenDataView(_newDataView);
                        }

                    } else if (null != myReport) {

                        StringBuilder myBuffer = new StringBuilder();

                        myBuffer.append(_constants.templateValidation_Unsuccessful());

                        if (myReport.isTemplateMissing()) {

                            myBuffer.append("\n");
                            myBuffer.append(_constants.templateValidation_TemplateNotFound(Format.value(myReport.getMissingTemplateName()),
                                    Format.value(myReport.getMissingTemplateUser())));

                        } else {

                            List<ValuePair<String, String>> myMissingFieldList = myReport.getMissingFieldList();
                            List<String> myMissingParameterList = myReport.getMissingParameterList();
                            List<String> myMissingParmDataList = myReport.getMissingParmDataList();

                            if ((null != myMissingFieldList) && (0 < myMissingFieldList.size())) {

                                myBuffer.append("\n");
                                myBuffer.append(_constants.templateValidation_FieldNotFound());
                                myBuffer.append("\n");

                                for (ValuePair<String, String> myField : myMissingFieldList) {

                                    myBuffer.append(Format.value(myField.getValue1()));
                                    myBuffer.append(", ");
                                }
                                myBuffer.setLength(myBuffer.length() - 2);
                                myBuffer.append(".");
                            }

                            if ((null != myMissingParmDataList) && (0 < myMissingParmDataList.size())) {

                                myBuffer.append("\n");
                                myBuffer.append(_constants.templateValidation_ParameterDataNotFound());
                                myBuffer.append("\n");

                                for (String myParameter : myMissingParmDataList) {

                                    myBuffer.append(Format.value(myParameter));
                                    myBuffer.append(", ");
                                }
                                myBuffer.setLength(myBuffer.length() - 2);
                                myBuffer.append(".");
                            }

                            if ((null != myMissingParameterList) && (0 < myMissingParameterList.size())) {

                                myBuffer.append("\n");
                                myBuffer.append(_constants.templateValidation_ParameterNotFound());
                                myBuffer.append("\n");

                                for (String myParameter : myMissingParameterList) {

                                    myBuffer.append(Format.value(myParameter));
                                    myBuffer.append(", ");
                                }
                                myBuffer.setLength(myBuffer.length() - 2);
                                myBuffer.append(".");
                            }
                        }
                        _visible = _linkupMap.getEditOk();
                        Display.error(_txtFailureDialogTitle, myBuffer.toString(), _this);

                    } else {

                        Display.error(_txtFailureDialogTitle, _constants.unexpectedError());
                    }

                } else {

                    Display.error(_txtFailureDialogTitle, responseIn.getException());
                }

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 9, myException);
            }
        }
    };

    private ClickHandler handleExecuteRequest = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                LinkupMode myMode = mergeData.getValue()
                                        ? LinkupMode.MERGE
                                        : (spinOff.getValue()
                                                ? LinkupMode.SPINOFF
                                                : LinkupMode.SPINUP);

                if (buildRequest(newDataviewName.getText(), myMode, ignoreNullValues.getValue())) {

                    finalizeRequest();
                }

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 11, myException);
            }
        }
    };

    private UserInputEventHandler<List<LaunchParam>> handleParameterLoadingComplete
            = new UserInputEventHandler<List<LaunchParam>>() {

        @Override
        public void onUserInput(UserInputEvent<List<LaunchParam>> eventIn) {

            try {


            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 12, myException);
            }
            if (!eventIn.isCanceled()) {

                _request.setParameterValues(eventIn.getKey());

                executeRequest();
            }
        }
    };

    //
    // Handle "Continue" button click event from Dataview Name dialog
    // by setting _continue value to true
    //
    private ClickHandler handleContinueButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                executeRequest();

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 13, myException);
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
            Display.error(_txtTemplateDataError, exceptionIn.getMessage());
            return true;
        }
        @Override
        public void onSuccess(Response<String, TemplateResponse> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    TemplateResponse myReport = responseIn.getResult();

                    if (myReport.isOK(ResourceChoiceCriteria.OWNER)) {

                        _template = myReport.getTemplate();
                        finalizeInitialization();

                    } else {

                        Display.error(_constants.locateTemplate_Failure());
                    }
                }

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 14, myException);
            }
        }
    };

    //
    // Handle radio button selection for using default parameter set
    //
    private ClickHandler handleUseDefaultRadioButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                switchParameterMode(false);

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 15, myException);
            }
        }
    };

    //
    // Handle radio button selection for using extended parameter sets
    //
    private ClickHandler handleUseExtendersRadioButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                switchParameterMode(true);

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 16, myException);
            }
        }
    };

    //
    // Handle radio button selection for creating new dataview
    //
    private ClickHandler handleNewDataviewRadioButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                newDataviewNameLabel.setVisible(true);
                newDataviewName.setVisible(true);
                newDataviewName.setEnabled(true);
                newDataviewName.setFocus(true);

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 17, myException);
            }
        }
    };

    //
    // Handle radio button selection for merging into current dataview
    //
    private ClickHandler handleMergeDataRadioButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                newDataviewNameLabel.setVisible(false);
                newDataviewName.setVisible(false);
                newDataviewName.setEnabled(false);

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 18, myException);
            }
        }
    };

    //
    // Handle parameter grid refresh -- check grid integrity
    //
    private RefreshHandler handleParameterGridRefresh
            = new RefreshHandler() {
        @Override
        public void onRefresh(RefreshEvent event) {

            try {

                validateButtons();

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 19, myException);
            }
        }
    };

    //
    // Handle loading data before displaying a tab
    //
    private BeforeSelectionHandler<Widget> handleBeforeSelectingTab
            = new BeforeSelectionHandler<Widget>() {
        @Override
        public void onBeforeSelection(BeforeSelectionEvent<Widget> eventIn) {

            try {

                Widget myWidget = eventIn.getItem();

                if (myWidget instanceof HorizontalPanel) {

                    //
                    // Extract values from previously displayed tab panel
                    //
                    extractActiveValues();
                    //
                    // Establish link from new panel to data source, use an index base of 1
                    //  -- index value of zero is reserved for default parameters
                    //
                    _accessIndex = (_usingExtenders) ? parameterSetTabPanel.getWidgetIndex(myWidget) : 0;
                    //
                    // Load values into to be displayed panel
                    //
                    loadActiveValues();
                    validateButtons();
                }

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 20, myException);
            }
        }
    };

    //
    // Handle change event for disable parameter set check box
    //
    private ChangeHandler handleDisableParameterSetChangeEvent
            = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {

            try {

                if (_swapCheckBoxValue) {
                    _isDisabled = !_isDisabled;
                }
                validateButtons();

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 21, myException);
            }
        }
    };

    //
    // Handle choice being made between modifying and executing the linkup
    //
    private ChoiceMadeEventHandler handleChoiceMadeEvent
            = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            try {

                int _choice = eventIn.getChoice();

                switch (_choice) {

                    case 0:

                        dialog.hide();

                        break;

                    case 1:
                    {
                        LinkupMode myMode = miniMergeData.getValue()
                                ? LinkupMode.MERGE
                                : (miniSpinOff.getValue()
                                ? LinkupMode.SPINOFF
                                : LinkupMode.SPINUP);

                        if (buildRequest(miniDataViewName.getText(), myMode, miniIgnoreNulls.getValue())) {

                            finalizeRequest();
                        }
                        break;
                    }
                    case 2:

                        newDataviewName.setText(miniDataViewName.getText());
                        spinOff.setValue(miniSpinOff.getValue());
                        spinUp.setValue(miniSpinUp.getValue());
                        mergeData.setValue(miniMergeData.getValue());
                        ignoreNullValues.setValue(miniIgnoreNulls.getValue());
                        newDataviewName.setVisible(spinOff.getValue() || spinUp.getValue());
                        newDataviewName.setEnabled(spinOff.getValue() || spinUp.getValue());
                        checkSetName(newDataviewName, newDataviewName.getText());
                        finalizeGrids();
                        validateButtons();
                        dialog.show(70);

                        break;
                }

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 22, myException);
            }
        }
    };

    //
    // Handle mini dialog radio button selection doing merge
    //
    private ClickHandler handleMiniMergeRadioButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                miniInputPanel.setVisible(false);
                miniDialog.enableButton(1);

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 23, myException);
            }
        }
    };

    //
    // Handle mini dialog radio button selection for creating new dataview
    //
    private ClickHandler handleMiniNewDataviewRadioButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                miniInputPanel.setVisible(true);

            } catch(Exception myException) {

                Dialog.showException("LinkupSelectionDialog", 24, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public LinkupSelectionDialog(AbstractDataViewPresenter _dataViewPresenter, Visualization visualizationIn, String selectionIn) {

        try {

            // Provide access to local copy of the DataView
            _dvPresenter = _dataViewPresenter;
            _dataViewUuid = _dvPresenter.getUuid();
            //_vizControl = vizControlIn;
            _this = this;

            if ((null != _dataViewPresenter) && (null != visualizationIn) && (null != selectionIn) && (0 < selectionIn.length())) {

                List<LinkupMapDef> myLinkupMaps = _dvPresenter.getDataView().getMeta().getLinkupDefinitions();

                visualizationIn.saveViewStateToVisualizationDef();
                _visualization = visualizationIn;
                _vizdef = _visualization.getVisualizationDef();
                initDataviewNameList();

                for (LinkupMapDef myLinkupMap : myLinkupMaps) {

                    if (selectionIn.equals(myLinkupMap.getLinkupName())) {

                        _linkupMap = myLinkupMap;

                        if (createRequest()) {

                            initComponents();
                        }
                        return;
                    }
                }
            }
            // Display error message.
            Display.error(_txtErrorDialogTitle, _txtNoLinkupInfo);

        } catch(Exception myException) {

            Dialog.showException("LinkupSelectionDialog", 25, myException);
        }
    }

    public HandlerRegistration addHideHandler(HideHandler handler) {

        try {

            return dialog.addHideHandler(handler);

        } catch(Exception myException) {

            Dialog.showException("LinkupSelectionDialog", 26, myException);
        }
        return null;
    }

    public void selectionChange(Object dataRowIn) {

        try {

            validateButtons();

        } catch(Exception myException) {

            Dialog.showException("LinkupSelectionDialog", 27, myException);
        }
    }

    public void rowComplete(Object dataRowIn) {

    }

    public void checkValidity() {

        validateButtons();
    }

    public void show() {

        try {

            if (_visible) {

                _visible = false;
                dialog.show();
            }

        } catch(Exception myException) {

            Dialog.showException("LinkupSelectionDialog", 28, myException);
        }
    }

    public void hide() {

        dialog.hide();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initComponents() {

        _hasMapping = _linkupMap.hasMappedFields();
        _mode = _hasMapping ? LinkupMode.MERGE : LinkupMode.SPINUP;

        //
        // Identify extenders refering to the visualization of interest
        //
        filterExtenders();
        _usingExtenders = _hasExtenders;
        _linkupExtenders = _usingExtenders ? _advancedExtenders : _defaultExtenders;

        //
        // Request template field information from the server
        //
        initTemplateFields(_linkupMap);

        //
        // Create object to display linkup parameter mapping
        // and set up the parameter mapping grid -- readonly grid!
        //
        _fieldMapper = new LinkupFieldMapDisplay(_dvPresenter, 410, 220, _txtHelpTarget + "#FieldDisplay"); //$NON-NLS-1$
        fieldMap = _fieldMapper.getGrid();
//        fieldMap.setVisible(true);

        /* if (_fieldParmsSupported) {

            //
            // Create object to handler linkup parameter mapping
            // and set up the parameter mapping grid
            //
            _parameterMapper = new LinkupParameterMapper(this, _dvPresenter, 410, 170, _txtHelpTarget + "#ParameterGrid");
            templateParms = _parameterMapper.getGrid();
            templateParms.setVisible(false);
            templateParms.addRefreshHandler(handleParameterGridRefresh);

            //
            // Create object to handler linkup conditional field mapping
            // and set up the conditional field mapping grid
            //
            _conditionalMapper = new LinkupConditionalFieldMapper(this, _dvPresenter, 410, 80, _txtHelpTarget + "#FieldGrid");
            fieldParms = _conditionalMapper.getGrid();
            fieldParms.setVisible(false);
            fieldParms.addRefreshHandler(handleParameterGridRefresh);

        } else { */

            //
            // Create object to handler linkup parameter mapping
            // and set up the parameter mapping grid
            //
            _parameterMapper = new LinkupParameterMapper(this, _dvPresenter, 410, 380, _txtHelpTarget + "#ParameterGrid"); //$NON-NLS-1$
            templateParms = _parameterMapper.getGrid();
//            templateParms.setVisible(false);
            templateParms.addRefreshHandler(handleParameterGridRefresh);
        /* } */

        uiBinder.createAndBindUi(this);

        //
        // set the colors for the tab panel we are creating
        //
        greyPanel.getElement().getStyle().setBackgroundColor(Dialog.txtBorderColor);
        parameterSetSelectionPanel.getElement().getStyle().setBackgroundColor("#ffffff"); //$NON-NLS-1$

        //
        // Set the values for permanent labels
        //
        linkupDefNameLabel.getElement().setInnerText(_txtLinkupNameLabel);
        templateNameLabel.getElement().setInnerText(_txtTemplateNameLabel);
        newDataviewNameLabel.getElement().setInnerText(_txtNewDataviewNameLabel);
        templateParmsLabel.getElement().setInnerText(_txtFillParameterGridLabel);
        templateParmsLabel.setVisible(false);

        /* if (_fieldParmsSupported) {

        fieldParmsLabel.getElement().setInnerText(_txtFieldGridLabel);
        fieldParmsLabel.setVisible(false);
        } */

        nothingSavedLabel.setText(_txtNoChangesSaved);
        nothingSavedLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
        nothingSavedLabel.setVisible(true);

        noMappingLabel.setText(_txtNoMappingLabel);
        noMappingLabel.getElement().getStyle().setColor(Dialog.txtWarningColor);
        noMappingLabel.setVisible(!_hasMapping);

        noExtendersLabel.setText(_txtNoExtendersLabel);
        noExtendersLabel.getElement().getStyle().setColor(Dialog.txtWarningColor);
        noExtendersLabel.setVisible(!_hasExtenders);

        //
        // Set up handlers to capture changes in the dataview name
        // in order to recognize conflicts with existing dataview
        // when naming a new dataview to be generated via linkup
        //
        newDataviewName.addKeyUpHandler(handleNewDataviewNameKeyUp);
        newDataviewName.addDropHandler(handleNewDataviewNameDrop);
        newDataviewName.addChangeHandler(handleNewDataviewNameChange);

        //
        // Set up the dialog cancel button
        //
        cancelButton = dialog.getCancelButton();
        cancelButton.setText(Dialog.txtCancelButton);
        cancelButton.setVisible(true);
        cancelButton.setEnabled(true);
        dialog.hideOnCancel();

        //
        // Set up the dialog execute button
        //
        executeButton = dialog.getActionButton();
        executeButton.setText(_txtExecuteButton);
        executeButton.setVisible(true);
        executeButton.setEnabled(false);
        executeButton.addClickHandler(handleExecuteRequest);

        linkupDefName.setText(_linkupMap.getLinkupName());

        templateName.setText(_linkupMap.getTemplateName());
        useDefault.setText(_txtUseDefaultRadioButton);
        useDefault.setValue(!_usingExtenders);
        useDefault.addClickHandler(handleUseDefaultRadioButtonClick);

        useExtenders.setText(_txtUseNodeEdgeRadioButton);
        useExtenders.setValue(_usingExtenders);
        useExtenders.setEnabled(_hasExtenders);
        useExtenders.addClickHandler(handleUseExtendersRadioButtonClick);

        spinOff.setText(_txtSpinOffRadioButton);
        spinOff.setTitle(_constants.linkupSelectionDialog_SpinOffRadioButtonExplanation());
        spinOff.setValue(LinkupMode.SPINOFF.equals(_mode));
        spinOff.addClickHandler(handleNewDataviewRadioButtonClick);
        spinOff.setEnabled(_hasMapping);

        spinUp.setText(_txtSpinUpRadioButton);
        spinUp.setTitle(_constants.linkupSelectionDialog_SpinUpRadioButtonExplanation());
        spinUp.setValue(LinkupMode.SPINUP.equals(_mode));
        spinUp.addClickHandler(handleNewDataviewRadioButtonClick);
        spinUp.setEnabled(true);

        mergeData.setText(_txtMergeRadioButton);
        mergeData.setTitle(_constants.linkupSelectionDialog_MergeRadioButtonExplanation());
        mergeData.setValue(LinkupMode.MERGE.equals(_mode));
        mergeData.addClickHandler(handleMergeDataRadioButtonClick);
        mergeData.setEnabled(_hasMapping);

        newDataviewNameLabel.setVisible(!_hasMapping);
        newDataviewName.setVisible(!_hasMapping);
        newDataviewName.setEnabled(!_hasMapping);
        newDataviewName.setFocus(!_hasMapping);

        //
        // Set up the check box for disabling a parameter set
        //
        setDisabledCheckBox.setBoxLabel(_txtDisabledCheckBox);
        setDisabledCheckBox.setVisible(true);
        setDisabledCheckBox.setEnabled(true);
        setDisabledCheckBox.addChangeHandler(handleDisableParameterSetChangeEvent);
        forceDisabled(_isDisabled);

        dialog.defineHeader(_txtDialogTitle, _txtHelpTarget, true);

        ignoreNullValues = new CheckBox();
        ignoreNullValues.setBoxLabel(_txtIgnoreNullValues);
        ignoreNullValues.setValue((null != _linkupMap) ? _linkupMap.getNoNulls() : false);
        dialog.addLeftControl(ignoreNullValues);

        finalizeInitialization();
    }

    //
    // Request data from the server to initialize the list dataviews to prevent naming conflicts
    //
    private void initDataviewNameList() {
        VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            //
            // !! Not currently bringing back names of templates we cannot access !!
            //
            myVortexFuture.execute(DataViewActionServiceProtocol.class).listDataViewNames(AclControlType.READ);
            myVortexFuture.addEventHandler(handleListDataviewNamesResponse);

        } catch (Exception myException) {

            Dialog.showException(_txtFailureDialogTitle, myException);
        }
    }

    private void finalizeInitialization() {


        if (_doFinalInit) {

            _fieldMapper.initGridFields(_linkupMap, _template);
            displayGrids();
            //
            // Set up the tab bar for selecting a parameter set
            //
            refreshTabPanel();
            _initialized = true;
            validateButtons();

            if (executeButton.isEnabled()) {

                InlineLabel myLabel = new InlineLabel(i18n.linkupSelectionDialogNewDataviewLabel()); //$NON-NLS-1$
                String myMessage = i18n.linkupSelectionDialogExecutingLinkupMessage(_linkupMap.getLinkupName(), _template.getName(), _visualization.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                miniMergeData = new RadioButton("class1", _txtMergeRadioButton); //$NON-NLS-1$
                miniMergeData.setTitle(_constants.linkupSelectionDialog_MergeRadioButtonExplanation());
                miniMergeData.setValue(LinkupMode.MERGE.equals(_mode));
                miniMergeData.setEnabled(_hasMapping);
                miniMergeData.addClickHandler(handleMiniMergeRadioButtonClick);

                miniSpinOff = new RadioButton("class1", _txtSpinOffRadioButton); //$NON-NLS-1$
                miniSpinOff.setTitle(_constants.linkupSelectionDialog_SpinOffRadioButtonExplanation());
                miniSpinOff.setValue(LinkupMode.SPINOFF.equals(_mode));
                miniSpinOff.setEnabled(_hasMapping);
                miniSpinOff.addClickHandler(handleMiniNewDataviewRadioButtonClick);

                miniSpinUp = new RadioButton("class1", _txtSpinUpRadioButton); //$NON-NLS-1$
                miniSpinUp.setTitle(_constants.linkupSelectionDialog_SpinUpRadioButtonExplanation());
                miniSpinUp.setValue(LinkupMode.SPINUP.equals(_mode));
                miniSpinUp.setEnabled(true);
                miniSpinUp.addClickHandler(handleMiniNewDataviewRadioButtonClick);

//                miniLabel = new InlineLabel("DataView");

                miniDataViewName = new TextBox();
                miniDataViewName.setWidth("175px"); //$NON-NLS-1$
                miniDataViewName.addKeyUpHandler(handleMiniNewDataviewNameKeyUp);
                miniDataViewName.addDropHandler(handleMiniNewDataviewNameDrop);
                miniDataViewName.addChangeHandler(handleMiniNewDataviewNameChange);

                miniIgnoreNulls = new CheckBox();
                miniIgnoreNulls.setBoxLabel(_txtIgnoreNullValues);
                miniIgnoreNulls.setValue(ignoreNullValues.getValue());

                if ((!_hasMapping) || _linkupMap.getPrompt()) {

                    List<ButtonDef> myButtonList = _linkupMap.getEditOk() ? _buttonList1a : _buttonList1b;
                    LayoutPanel myRadioPanel = new LayoutPanel();
                    myRadioPanel.setPixelSize(450, 30);
                    myRadioPanel.add(miniMergeData);
                    myRadioPanel.add(miniSpinOff);
                    myRadioPanel.add(miniSpinUp);
//                    myRadioPanel.add(miniLabel);
                    myRadioPanel.setWidgetLeftWidth(miniMergeData, 20, Unit.PX, 140, Unit.PX);
                    myRadioPanel.setWidgetLeftWidth(miniSpinOff, 165, Unit.PX, 140, Unit.PX);
                    myRadioPanel.setWidgetLeftWidth(miniSpinUp, 310, Unit.PX, 140, Unit.PX);
//                    myRadioPanel.setWidgetLeftRight(miniLabel, 375, Unit.PX, 0, Unit.PX);
/*
                    HorizontalPanel myTextBoxPanel = new HorizontalPanel();
                    myTextBoxPanel.setSpacing(20);
                    myTextBoxPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
                    myTextBoxPanel.add(myLabel);
                    myTextBoxPanel.add(miniDataViewName);
*/
                    miniInputPanel = new LayoutPanel();
                    miniInputPanel.setPixelSize(450, 40);
                    miniInputPanel.add(myLabel);
                    miniInputPanel.add(miniDataViewName);
                    miniInputPanel.setWidgetRightWidth(myLabel, 210, Unit.PX, 130, Unit.PX);
                    miniInputPanel.setWidgetRightWidth(miniDataViewName, 20, Unit.PX, 190, Unit.PX);
                    miniInputPanel.setVisible(!_hasMapping);

                    LayoutPanel myCheckBoxPanel = new LayoutPanel();
                    myCheckBoxPanel.setPixelSize(450, 30);
                    myCheckBoxPanel.add(miniIgnoreNulls);
                    myCheckBoxPanel.setWidgetRightWidth(miniIgnoreNulls, 0, Unit.PX, 400, Unit.PX);

                    miniDialog = new DecisionDialog(i18n.linkupSelectionDialogExecuteLinkupTitle(), myMessage,
                                                    myButtonList, handleChoiceMadeEvent, 60); //$NON-NLS-1$
                    miniDialog.setWidth("460px"); //$NON-NLS-1$
                    if (!_hasMapping) {

                        miniDialog.addWarning(_constants.linkup_NoMappingsCanNotMerge_Warning());
                    }
                    miniDialog.addWidget(myRadioPanel);
                    miniDialog.addWidget(miniInputPanel);
                    miniDialog.addWidget(myCheckBoxPanel);

                    miniDialog.show(70);

                } else {

                    LinkupMode myMode = miniMergeData.getValue()
                            ? LinkupMode.MERGE
                            : (miniSpinOff.getValue()
                            ? LinkupMode.SPINOFF
                            : LinkupMode.SPINUP);

                    if (buildRequest(miniDataViewName.getText(), myMode, miniIgnoreNulls.getValue())) {

                        finalizeRequest();

                    } else {

                        Display.error(i18n.linkupSelectionDialogLinkupErrorMessage()); //$NON-NLS-1$
                    }
                }

            } else if (_linkupMap.getEditOk()) {

                finalizeGrids();
                dialog.setCallBack(this);
                dialog.show(70);
            }

        } else {

            _doFinalInit = true;
        }
    }

    private void finalizeGrids() {

        if(templateParms != null){
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
        }
    }

    private boolean createRequest() {
        LinkupRequest myRequest = new LinkupRequest();
        String myMessage = null;

        try {
            myRequest.setVisualizationId(_visualization.getUuid());
//            if (_visualization.hasSelection()) {

                try {

                    myRequest.setSessionId(_dvPresenter.getUuid());
                    myRequest.setLinkupUuid(_linkupMap.getUuid());
                    getClientSelection();
                    _request = myRequest;

                } catch (Exception myException) {

                    Dialog.showException(_txtErrorDialogTitle, 1, myException);
                }

//            } else {
//                myMessage = _txtDataSelectionRequired;
//            }

        } catch (Exception myException) {

            Dialog.showException(_txtErrorDialogTitle, 2, myException);
        }

        if (null != myMessage) {
            Display.error(_txtErrorDialogTitle + " 3", myMessage);
        }

        return (null != _request);
    }

    private void getClientSelection() {

        if(_vizdef instanceof RelGraphViewDef ||
                _vizdef instanceof MapViewDef)
        {
            return;
        }

        _selectedItems = _vizdef.getSelection();

        if (null != _selectedItems) {

            if (_vizdef instanceof TableViewDef) {

                IntegerRowsSelection selection = new IntegerRowsSelection();
//                HashSet<Integer> rows = new HashSet<Integer>();
//                for(Integer item : selection.getSelectedItems()){
//                    rows.add(item);
//                }
                selection.setFromSelection(_selectedItems);
                _selectedRows = selection.getSelectedItems();
            }
        }
    }

    //
    // Load values into mapping grid after selecting a template for linkup
    //
    private void initTemplateFields(LinkupMapDef linkupIn) {

        VortexFuture<Response<String, TemplateResponse>> vortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            vortexFuture.execute(DataViewDefActionsServiceProtocol.class).getLinkupTemplate(linkupIn);
            vortexFuture.addEventHandler(handleInitTemplateFieldsServerResponse);

        } catch (Exception myException) {

            Dialog.showException(_txtFailureDialogTitle, myException);
        }
    }

    //
    //
    //
    private void addParameterSetTab(LinkupExtender extenderIn) {

        if (null == parameterSetTabPanel) {

            createTabPanel();
        }

        String myLabel = extenderIn.getName();
        TabItemConfig myItemConfig = new TabItemConfig((null != myLabel) ? myLabel : "-- ? --", false); //$NON-NLS-1$

        parameterSetTabPanel.add(new HorizontalPanel(), myItemConfig);

        parameterSetDataPanel.setVisible(true);
    }

    //
    // Set up the tab bar for selecting a parameter set
    //
    private void createTabPanel() {

        //
        // Release previos selection handler
        //
        if (null != _selectionHandler) {

            _selectionHandler.removeHandler();
            _selectionHandler = null;
        }

        parameterSetTabPanel = new TabPanel();
        parameterSetTabPanel.addStyleName("senchaTab"); //$NON-NLS-1$
        parameterSetTabPanel.setAnimScroll(true);
        parameterSetTabPanel.setTabScroll(true);
        parameterSetTabPanel.setCloseContextMenu(false);
        parameterSetTabPanel.setPixelSize(430, 0);

        //
        // Attach handlers for extracting and loading parameter data
        //
        _selectionHandler = parameterSetTabPanel.addBeforeSelectionHandler(handleBeforeSelectingTab);

        parameterSetSelectionPanel.add(parameterSetTabPanel);
    }

    //
    //
    //
    private void refreshTabPanel() {

        if (null == parameterSetTabPanel) {

            createTabPanel();
        }
        if (null != parameterSetTabPanel) {

            clearTabPanel();
        }
        if (_usingExtenders) {

            for (int i = 0; _linkupExtenders.size() > i; i++) {

                addParameterSetTab(_linkupExtenders.get(i));
            }

        } else {

            TabItemConfig myItemConfig = new TabItemConfig(i18n.linkupSelectionDialogTabTitle(), false); //$NON-NLS-1$

            parameterSetTabPanel.add(new HorizontalPanel(), myItemConfig);
        }
        _accessIndex = 0;
        loadActiveValues();
        parameterSetTabPanel.setActiveWidget(parameterSetTabPanel.getWidget(0));
    }

    private void clearTabPanel() {

        if (null != parameterSetTabPanel) {

            while (0 < parameterSetTabPanel.getWidgetCount()) {

                parameterSetTabPanel.remove(0);
            }
            parameterSetTabPanel.clear();
        }
    }

    //
    //
    //
    private LinkupExtenderStage getDataItem() {

        return getDataItem(_accessIndex);
    }

    //
    //
    //
    private LinkupExtenderStage getDataItem(int indexIn) {

        LinkupExtenderStage myDataItem = null;

        if ((null != _linkupExtenders) && (0 <= indexIn) && (_linkupExtenders.size() > indexIn)) {

            myDataItem = (LinkupExtenderStage)_linkupExtenders.get(indexIn);
        }
        return myDataItem;
    }

    private void clearActiveValues() {

        forceDisabled(false);

        clearGridValues();
    }

    private void loadActiveValues() {

        LinkupExtenderStage myDataItem = getDataItem();

        clearActiveValues();

        if (null != myDataItem) {

            loadGridValues(myDataItem);
            forceDisabled(myDataItem.getIsDisabled());
        }
    }

    private void extractActiveValues() {

        LinkupExtenderStage myDataItem = getDataItem();

        if (null != myDataItem) {

            myDataItem.setParameterList(extractGridValues());
            myDataItem.setIsDisabled(_isDisabled);
        }
    }

    private void clearGridValues() {

        if (_hasParameters) {

            _parameterMapper.initGridFields(_template);
        }
        /* if (_fieldParmsSupported) {

            _conditionalMapper.initGridFields(_template);
        } */
    }

    private void loadGridValues(LinkupExtenderStage extenderIn) {

        if (null != extenderIn) {

            List<ParamMapEntry> myList = extenderIn.getParameterList() ;

            if (_hasParameters) {

                _parameterMapper.initGridFields(_template, myList);
            }
            /* if (_fieldParmsSupported) {

                _conditionalMapper.initGridFields(_template, myList);
            } */
        }
    }

    private List<ParamMapEntry> extractGridValues() {

        List<ParamMapEntry> myList = null ;

        if (_hasParameters) {

            myList = _parameterMapper.extractGridData(myList);
        }
        /* if (_fieldParmsSupported) {

            myList = _conditionalMapper.extractGridData(myList);
        } */
        return myList;
    }

    //
    //
    //
    private void displayGrids() {

        if (null != _template) {

            List<QueryParameterDef> myList = _template.getDataSetParameters();

            _hasParameters = ((null != myList) && (0 < myList.size()));
        }
        if (_hasMapping) {

            removeNoMappingsNotification();
            fieldMap.setHeight(220);

        } else {

            displayNoMappingsNotification();
            fieldMap.setHeight(120);
        }

        /* if (_fieldParmsSupported) {

        if (_hasParameters) {

            removeNoParametersNotification();
            templateParmsLabel.getElement().setInnerText(_txtFillParameterGridLabel);

            if (_usingExtenders) {

                templateParms.setHeight(230);

            } else {

                templateParms.setHeight(260);
            }

            fieldParms.setHeight(80);

        } else {

            displayNoParametersNotification();
            templateParmsLabel.getElement().setInnerText(_txtIgnoreParameterGridLabel);

            if (_usingExtenders) {

                templateParms.setHeight(60);
                fieldParms.setHeight(160);

            } else {

                templateParms.setHeight(60);
                fieldParms.setHeight(190);
            }
        }

        } else { */

        removeNoParametersNotification();
        templateParmsLabel.getElement().setInnerText(_txtFillParameterGridLabel);
        templateParms.setHeight(260);

        /* } */

        noMappingLabel.setVisible(!_hasMapping);
        templateParmsLabel.setVisible(true);
/*
        fieldMap.setVisible(true);
        templateParms.setVisible(true);
        fieldMap.setEnabled(_hasMapping);
        templateParms.setEnabled(_hasParameters);
*/
        /* if (_fieldParmsSupported) {

            fieldParmsLabel.setVisible(true);
            fieldParms.setVisible(true);
            fieldParms.setEnabled(true);

        } else {

            fieldParmsLabel.setVisible(false);
        } */
        setDisabledCheckBox.setVisible(true);
        setDisabledCheckBox.setEnabled(true);
    }

    private void displayNoParametersNotification() {

        if (null == noParametersPanel) {

            noParametersPanel = new HorizontalPanel();
            noParametersPanel.setWidth("420px"); //$NON-NLS-1$
            noParametersPanel.setHeight("60px"); //$NON-NLS-1$
            topPanel.add(noParametersPanel);
            topPanel.setWidgetLeftWidth(noParametersPanel, 470, Unit.PX, 420, Unit.PX);
            topPanel.setWidgetTopHeight(noParametersPanel, 100, Unit.PX, 60, Unit.PX);
            noParametersLabel = new Label(_txtNoParametersLabel);
            noParametersPanel.add(noParametersLabel);
            noParametersPanel.setCellHorizontalAlignment(noParametersLabel, HasHorizontalAlignment.ALIGN_CENTER);
            noParametersPanel.setCellVerticalAlignment(noParametersLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        }
    }

    private void removeNoParametersNotification() {

        if (null != noParametersPanel) {

            noParametersPanel.remove(noParametersLabel);
            topPanel.remove(noParametersPanel);
            noParametersPanel = null;
            noParametersLabel = null;
        }
    }

    private void displayNoMappingsNotification() {

        if (null == noMappingsPanel) {

            noMappingsPanel = new HorizontalPanel();
            noMappingsPanel.setWidth("420px"); //$NON-NLS-1$
            noMappingsPanel.setHeight("120px"); //$NON-NLS-1$
            topPanel.add(noMappingsPanel);
            topPanel.setWidgetLeftWidth(noMappingsPanel, 20, Unit.PX, 410, Unit.PX);
            topPanel.setWidgetBottomHeight(noMappingsPanel, 10, Unit.PX, 120, Unit.PX);
            noMappingsLabel = new Label(_txtNoMappingsLabel);
            noMappingsPanel.add(noMappingsLabel);
            noMappingsPanel.setCellHorizontalAlignment(noMappingsLabel, HasHorizontalAlignment.ALIGN_CENTER);
            noMappingsPanel.setCellVerticalAlignment(noMappingsLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        }
    }

    private void removeNoMappingsNotification() {

        if (null != noMappingsPanel) {

            noMappingsPanel.remove(noMappingsLabel);
            topPanel.remove(noMappingsPanel);
            noMappingsPanel = null;
            noMappingsLabel = null;
        }
    }

    private void executeRequest() {
        VortexFuture<Response<String, LinkupResponse>> vortexFuture = WebMain.injector.getVortex().createFuture();

        if (_linkupMap.getMonitor()) {

            showWatchBox(_constants.linkupSelectionDialogMaskMessage()); //$NON-NLS-1$
        }
        try {
            _dvPresenter.resetTableCaching();
            vortexFuture.addEventHandler(handleLinkupResponse);
            vortexFuture.execute(DataViewActionServiceProtocol.class).executeLinkup(_request);
            executeButton.setEnabled(false);
        } catch (Exception myException) {
            hideWatchBox();
            Dialog.showException(_txtFailureDialogTitle, myException);
        }
    }

    //
    //
    //
    private void filterExtenders() {

        List<LinkupExtender> myFullExtendersList = _linkupMap.getLinkupExtenders();
        String myVizdefId = _vizdef.getLocalId();
        LinkupExtender myDefault = new LinkupExtenderStage();

        myDefault.setParameterList(_linkupMap.getLinkupParms());
        myDefault.setIsDisabled(false);

        _defaultExtenders = new ArrayList<LinkupExtender>();
        _advancedExtenders = new ArrayList<LinkupExtender>();

        _defaultExtenders.add(myDefault);

        if ((null != myFullExtendersList) && (0 < myFullExtendersList.size())) {

            for (LinkupExtender myExtender : myFullExtendersList) {

                String myKey = myExtender.getVizDefId();

                if (myVizdefId.equalsIgnoreCase(myKey)) {

                    _advancedExtenders.add(new LinkupExtenderStage(myExtender));
                }
            }
        }
        _hasExtenders = (0 < _advancedExtenders.size());
    }

    private void switchParameterMode(boolean useExtendersIn) {

        extractActiveValues();

        _usingExtenders = useExtendersIn;
        _linkupExtenders = _usingExtenders ? _advancedExtenders : _defaultExtenders;

        _accessIndex = 0;
        loadActiveValues();

        displayGrids();
        refreshTabPanel();
        validateButtons();
    }

    private boolean buildRequest(String nameIn, LinkupMode modeIn, boolean ignoreNullsIn) {

        if (checkParameterIntegrity()) {

            //
            // Extract values from previously displayed tab panel
            //
            extractActiveValues();

            List<LinkupHelper> _myHelperList = new ArrayList<LinkupHelper>();

            //
            // Gather data for parameter set that are not disabled
            // -- _linkupExtenders points to either default or advanced based upon the user selection
            //
            for (LinkupExtender myExtender : _linkupExtenders) {

                if (!myExtender.getIsDisabled()) {

                    _myHelperList.add(new LinkupHelper(myExtender, _selectedRows));
                }
            }

            _request.setParameterBuilderList(_myHelperList);
            _request.setMode(modeIn);
            _request.setDiscardNulls(ignoreNullsIn);
            _request.setNewDataViewName(((!LinkupMode.MERGE.equals(modeIn)) && _newDataviewNameValid) ? nameIn : null);
            _request.setSelection(_selectedItems);
        }

        return _isReady;
    }

    private boolean checkParameterIntegrity() {
/*
        String myErrorP = _parameterMapper.checkIntegrity(true);
        //if (_fieldParmsSupported) {
        //String myErrorC = _conditionalMapper.checkIntegrity(true);
        //}

        int myExtenderCount = 0;

        if (!_isDisabled) {

            myExtenderCount = 1;

            //if (_fieldParmsSupported) {
            //if ((null != myErrorP) || (null != myErrorC)) {
            //} else {
            if (null != myErrorP) {
            //}

                _isReady = false;
                nothingSavedLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
                //if (_fieldParmsSupported) {
                //nothingSavedLabel.setText((null != myErrorP) ? myErrorP : myErrorC);
                //} else {
                nothingSavedLabel.setText(myErrorP);
                //}/

            } else {

                //if (_fieldParmsSupported) {
                //int myRowCount = _parameterMapper.rowCount() + _conditionalMapper.rowCount();
                //} else {
                int myRowCount = _parameterMapper.rowCount();
                //}

                if (0 < myRowCount) {

                    _isReady = true;
                    nothingSavedLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
                    nothingSavedLabel.setText(_txtNoChangesSaved);

                } else {

                    _isReady = false;
                    nothingSavedLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
                    nothingSavedLabel.setText(_txtNoParametersSet);
                }
            }

        } else {

            _isReady = true;
        }

        if (_isReady) {

            for (int i =0; _linkupExtenders.size() > i; i++) {

                if (i != _accessIndex) {

                    LinkupExtender myExtender = _linkupExtenders.get(i);

                    if (!myExtender.getIsDisabled()) {

                        myExtenderCount++;

                        if (!_linkupExtenders.get(i).isReady()) {
                            _isReady = false;
                            break;
                        }
                    }
                }
            }
            if (0 == myExtenderCount) {

                _isReady = false;
                nothingSavedLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
                nothingSavedLabel.setText(_txtEverythingDisabled);
            }
        }
        return _isReady;
*/
        _isReady = true;
        return true;
    }

    //
    // Check for conflict between presumably new parameter set name and existing names
    //
    private void checkSetName(TextBox textBoxIn, String nameIn) {

        _newDataviewNameValid = false;

        if ((null != nameIn) && (0 < nameIn.length())) {
            if ((null != _dataviewNameMap) && _dataviewNameMap.containsKey(nameIn)) {
                textBoxIn.getElement().getStyle().setColor(Dialog.txtErrorColor);
            } else {
                _newDataviewNameValid = true;
                textBoxIn.getElement().getStyle().setColor(Dialog.txtLabelColor);
            }
        }
    }

    private void forceDisabled(boolean valueIn) {

        _swapCheckBoxValue = false;
        _isDisabled = valueIn;
        setDisabledCheckBox.setValue(_isDisabled);
        _swapCheckBoxValue = true;
    }

    private void validateButtons() {

        executeButton.setEnabled(checkParameterIntegrity());
    }

    private void finalizeRequest() {

        List<QueryParameterDef> myParmList = identifyRequiredParameters();

        try {

            if ((null != myParmList) && (0 < myParmList.size())) {

                (new ParameterWizard(_txtParameterTitle, _txtParameterHelp, _txtParameterButton, myParmList, handleParameterLoadingComplete)).show();

            } else {

                performLinkup();
            }

        } catch(Exception myException) {

            Dialog.showException(_txtErrorDialogTitle, 4, myException);
        }
    }

    private void performLinkup() {

        if (LinkupMode.MERGE.equals(_request.getMode())
                || ((null != _request.getNewDataViewName())
                    && (0 < _request.getNewDataViewName().length()))) {

            executeRequest();

        } else {

            Dialog.showContinueDialog(_txtNoDataviewNameTitle, _txtNoDataviewNameMessage, handleContinueButtonClick);
        }
    }

    private List<QueryParameterDef> identifyRequiredParameters() {

        // Search to find any parameters that were not satisfied by every request
        List<QueryParameterDef> myRequiredParameters = new ArrayList<QueryParameterDef>();
        List<LinkupHelper> myExtenderList = _request.getParameterBuilderList();
        List<QueryParameterDef> myParameterList = _template.getDataSetParameters();
        int myParameterCount = myParameterList.size();
        int myExtenderCount = myExtenderList.size();
        int[] myCount = new int[myParameterList.size()];

        for (int i = 0; myParameterCount > i; i++) {

            String myParameterId = myParameterList.get(i).getLocalId();

            for (int j = 0; myExtenderCount > j; j++) {

                List<ParamMapEntry> myCoverage = myExtenderList.get(j).getParameterList();

                if (null != myCoverage) {

                    for (ParamMapEntry myEntry : myCoverage) {

                        if (myEntry.getParamId() == myParameterId) {

                            myCount[i]++;
                        }
                    }
                }
            }
        }

        for (int i = 0; myParameterCount > i; i++) {

            if (myExtenderCount > myCount[i]) {

                myRequiredParameters.add(myParameterList.get(i));
            }
        }
        return myRequiredParameters;
    }
}

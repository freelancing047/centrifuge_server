package csi.client.gwt.csi_resource;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.events.ResourceSelectionEvent;
import csi.client.gwt.events.ResourceSelectionEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.resource.ExportImportConstants;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.service.api.ExportActionsServiceProtocol;
import csi.server.common.service.api.ModelActionsServiceProtocol;


public class ExportDialog extends WatchingParent {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ExportDialogUiBinder extends UiBinder<IsWidget, ExportDialog> {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    Dialog dialog;

    @UiField
    FullSizeLayoutPanel controlPanel;
    @UiField(provided = true)
    ResourceSelectorPanel<ResourceBasics> selectorWidget;
    @UiField
    TextArea instructionTextArea;
    @UiField
    RadioButton dataviewRadioButton;
    @UiField
    RadioButton templateRadioButton;
    @UiField
    RadioButton themeRadioButton;
    @UiField
    CheckBox multiCheckBox;
    @UiField
    CheckBox rawXmlCheckBox;
    @UiField
    CheckBox themesCheckBox;
    @UiField
    CheckBox iconsCheckBox;
    @UiField
    CheckBox mapsCheckBox;

    private Button exportButton;
    private Button exitButton;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtFailureDialogTitle = _constants.serverRequest_ErrorDialogTitle();

    private static final String _txtExitButton = Dialog.txtExitButton;
    private static final String _txtExportButton = Dialog.txtExportButton;
    private static final String _txtDialogTitle = _constants.exportDialog_Title();
    private static final String _txtHelpTarget = _constants.exportDialog_HelpTarget();
    private static final String _txtInstructions = _constants.exportDialog_Instructions();

    private static ExportDialogUiBinder uiBinder = GWT.create(ExportDialogUiBinder.class);
    private static String _classKey = "mainapp.ExportDialog";
    private static String _dataKey = "resourceType";

    private MainPresenter _mainPresenter = null;
    private AclResourceType _resourceType = null;

    private List<ResourceBasics> _dataviewList = null;
    private List<ResourceBasics> _templateList = null;
    private List<ResourceBasics> _themeList = null;
    private List<ResourceBasics> _activeList = null;

    private String _fileToken = null;
    private String _fileName = null;
    private String _fileExtension = null;
    private boolean _selectionValid = false;
    private boolean _checkBoxValid = false;
    private boolean _includeThemes = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle response to request for dataview list
    //
    protected VortexEventHandler<List<ResourceBasics>> handleDataviewListRequestResponse = new AbstractVortexEventHandler<List<ResourceBasics>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                Dialog.showException(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
            return false;
        }

        @Override
        public void onSuccess(List<ResourceBasics> listIn) {

            try {

                _dataviewList = listIn;

                if (dataviewRadioButton.getValue()) {

                    listDataviews();
                }

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle response to request for template list
    //
    protected VortexEventHandler<List<ResourceBasics>> handleTemplateListRequestResponse = new AbstractVortexEventHandler<List<ResourceBasics>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            try {

                Dialog.showException(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
            return false;
        }

        @Override
        public void onSuccess(List<ResourceBasics> listIn) {
            try {

                _templateList = listIn;

                if (templateRadioButton.getValue()) {

                    listTemplates();
                }

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle response to request for template list
    //
    protected VortexEventHandler<List<ResourceBasics>> handleThemeListRequestResponse = new AbstractVortexEventHandler<List<ResourceBasics>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            try {

                Dialog.showException(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
            return false;
        }

        @Override
        public void onSuccess(List<ResourceBasics> listIn) {
            try {

                _themeList = listIn;

                if (themeRadioButton.getValue()) {

                    listThemes();
                }

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle clicking the Exit button
    //
    protected ClickHandler handleExitButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            try {

                getMainPresenter().setDialogPreference(_classKey, _dataKey, (long) _resourceType.ordinal());
                //
                // Hide dialog
                //
                dialog.hide();

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle selection made from resource list
    //
    protected ResourceSelectionEventHandler<ResourceBasics> handleResourceSelection
            = new ResourceSelectionEventHandler<ResourceBasics>() {
        @Override
        public void onResourceSelection(ResourceSelectionEvent<ResourceBasics> eventIn) {

            exportButton.setEnabled(false);

            try {
                //
                // Call the proper execute method to export the resource
                //
                if (rawXmlCheckBox.getValue()) {

                    ResourceBasics mySelection = eventIn.getSelection();
                    createXmlExport(mySelection.getUuid());
                } else {

                    List<String> myExportList = new ArrayList<String>();
                    List<ResourceBasics> mySelection = selectorWidget.getSelectionList();
                    if ((null != mySelection) && (0 < mySelection.size())) {
                        for (ResourceBasics myItem : mySelection) {
                            myExportList.add(myItem.getUuid());
                        }
                    }
                    createExport(myExportList);
                }
                //
                // Re-enable the resource selector
                //
                selectorWidget.enableInput();

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle clicking the DataView radio button
    //
    private ClickHandler handleDataViewButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            try {

                listDataviews();
                DeferredCommand.add(new Command() {
                    public void execute() {
                        selectorWidget.clearSelection();
                    }
                });

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle clicking the Template Radio button
    //
    private ClickHandler handleTemplateButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            try {

                listTemplates();
                DeferredCommand.add(new Command() {
                    public void execute() {
                        selectorWidget.clearSelection();
                    }
                });

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle clicking the Theme Radio button
    //
    private ClickHandler handleThemeButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            try {

                listThemes();
                DeferredCommand.add(new Command() {
                    public void execute() {
                        selectorWidget.clearSelection();
                    }
                });

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    //
    // Handle results from validity check
    //
    protected ValidityReportEventHandler handleValidityReportEvent
            = new ValidityReportEventHandler() {
        @Override
        public void onValidityReport(ValidityReportEvent eventIn) {
            try {

                _selectionValid = eventIn.getValidFlag();
                exportButton.setEnabled(_selectionValid || _checkBoxValid);
                labelCheckBoxes();

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    private ClickHandler cancelCallBack = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            try {

                if (null != _fileToken) {

                    try {

                        WebMain.injector.getVortex().execute(ExportActionsServiceProtocol.class).destroyDownload(_fileToken);

                    } catch (Exception myException) {

                        Dialog.showException(myException);
                    }
                }

            } catch (Exception myException) {

                Dialog.showException("ExportDialog", myException);
            }
        }
    };

    private Callback<Response<String, String>> downloadCallback = new Callback<Response<String, String>>() {

        @Override
        public void onSuccess(Response<String, String> responseIn) {

            hideWatchBox();
            if (ResponseHandler.isSuccess(responseIn)) {
                try {
                    String mySuffix = _fileExtension;
                    _fileToken = responseIn.getResult();
                    (new DownloadHelper(_fileName, mySuffix, _fileToken)).execute(cancelCallBack);
                } catch (Exception myException) {
                    Display.error("ExportDialog", myException);
                }
            }
        }
    };

    private ValueChangeHandler handleCheckBoxChange = new ValueChangeHandler() {
        @Override
        public void onValueChange(ValueChangeEvent eventIn) {

            if (eventIn.getSource().equals(multiCheckBox)) {

                selectorWidget.setMultiSelection(multiCheckBox.getValue());

            } else if (eventIn.getSource().equals(themesCheckBox)) {

                _includeThemes = themesCheckBox.getValue();
            }
            enableDisableCheckBoxes();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ExportDialog() {

        this(null, null);
    }

    public ExportDialog(String resourceIn, AclResourceType resourceTypeIn) {
        try {

            _resourceType = resourceTypeIn;
            initializeObject();
            wireInHandlers();
            if (null != resourceIn) {

                selectorWidget.setPreSelect(resourceIn);
            }

        } catch (Exception myException) {

            Dialog.showException("ExportDialog", myException);
        }
    }

    public void show() {
        try {

            initializeDisplay();
            dialog.show(60);

        } catch (Exception myException) {

            Dialog.showException("ExportDialog", myException);
        }
    }

    public void hide() {
        try {

            dialog.hide();

        } catch (Exception myException) {

            Dialog.showException("ExportDialog", myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    private void initializeObject() {

        if (null == _resourceType) {

            long myResourceTypeOrdinal = getMainPresenter().getDialogPreference(_classKey, _dataKey,
                    (long) AclResourceType.TEMPLATE.ordinal());

            _resourceType = AclResourceType.values()[(int) myResourceTypeOrdinal];
        }
        selectorWidget = new ResourceSelectorPanel<ResourceBasics>(this, SelectorMode.READ_ONLY, false);

        //
        // Link UI XML code to this file and all GWT to create remaining components
        //
        uiBinder.createAndBindUi(this);

        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtDialogTitle, _txtHelpTarget, true);

        //
        // Set up the dialog cancel button
        //
        exitButton = dialog.getCancelButton();
        exitButton.setText(_txtExitButton);
        exitButton.setVisible(true);
        exitButton.setEnabled(true);
        //
        // Set up the dialog export button
        //
        exportButton = dialog.getActionButton();
        exportButton.setText(_txtExportButton);
        exportButton.setVisible(true);
        exportButton.setEnabled(false);
        //
        // Initially set up for exporting templates unless requested otherwise
        //
        if (AclResourceType.DATAVIEW.equals(_resourceType)) {

            themeRadioButton.setValue(false);
            templateRadioButton.setValue(false);
            dataviewRadioButton.setValue(true);

        } else if (AclResourceType.TEMPLATE.equals(_resourceType)) {

            themeRadioButton.setValue(false);
            dataviewRadioButton.setValue(false);
            templateRadioButton.setValue(true);

        } else {

            dataviewRadioButton.setValue(false);
            templateRadioButton.setValue(false);
            themeRadioButton.setValue(true);
        }
        mapsCheckBox.setValue(false, false);
        iconsCheckBox.setValue(false, false);
        themesCheckBox.setValue(false, false);
        multiCheckBox.setValue(false);
//        multiCheckBox.setVisible(false);
        rawXmlCheckBox.setValue(false);

        mapsCheckBox.addValueChangeHandler(handleCheckBoxChange);
        iconsCheckBox.addValueChangeHandler(handleCheckBoxChange);
        themesCheckBox.addValueChangeHandler(handleCheckBoxChange);
        multiCheckBox.addValueChangeHandler(handleCheckBoxChange);
        rawXmlCheckBox.addValueChangeHandler(handleCheckBoxChange);
        enableDisableCheckBoxes();

        instructionTextArea.setReadOnly(true);
        instructionTextArea.getElement().getStyle().setBorderStyle(BorderStyle.NONE);
        instructionTextArea.getElement().getStyle().setProperty("resize", "none");
        instructionTextArea.getElement().getStyle().setBackgroundColor("white");
        instructionTextArea.getElement().getStyle().setBorderColor("white");
        instructionTextArea.getElement().getStyle().setColor(Dialog.txtInfoColor);
        instructionTextArea.setText(_txtInstructions);
        //
        // Retrieve resource lists
        //
        retrieveTemplateDisplayList();
        retrieveDataviewDisplayList();
        retrieveThemeDisplayList();

        labelCheckBoxes();
    }

    private void wireInHandlers() {

        exitButton.addClickHandler(handleExitButtonClick);

        exportButton.addClickHandler(selectorWidget.handleSelectButtonClick);

        dataviewRadioButton.addClickHandler(handleDataViewButtonClick);
        templateRadioButton.addClickHandler(handleTemplateButtonClick);
        themeRadioButton.addClickHandler(handleThemeButtonClick);

        selectorWidget.addResourceSelectionEventHandler(handleResourceSelection);
        selectorWidget.addValidityReportEventHandler(handleValidityReportEvent);
    }

    //
    //
    //
    private void initializeDisplay() {

        //
        // Set up the selection widget
        //
        if (null != _activeList) {

            selectorWidget.resetDisplay(_resourceType, "", _activeList);

        } else {

            selectorWidget.initializeDisplay(_resourceType, "");
        }
        enableDisableCheckBoxes();
        dialog.hideTitleCloseButton();
    }

    //
    // Request data from the server to initialize the list dataviews available for exporting
    //
    private void retrieveDataviewDisplayList() {

        if (null == _dataviewList) {

            VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                ResourceFilter myFilter = WebMain.injector.getMainPresenter().getDefaultResourceFilter();

                myVortexFuture.addEventHandler(handleDataviewListRequestResponse);
                myVortexFuture.execute(ModelActionsServiceProtocol.class).getFilteredResourceList(
                        AclResourceType.DATAVIEW, myFilter, AclControlType.READ);
                showWatchBox();

            } catch (Exception myException) {

                hideWatchBox();
                Dialog.showException(myException);
            }
        }
    }

    //
    // Request data from the server to initialize the list dataviews available for exporting
    //
    private void retrieveTemplateDisplayList() {

        if (null == _templateList) {

            VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                ResourceFilter myFilter = WebMain.injector.getMainPresenter().getDefaultResourceFilter();

                myVortexFuture.addEventHandler(handleTemplateListRequestResponse);
                myVortexFuture.execute(ModelActionsServiceProtocol.class).getFilteredResourceList(
                        AclResourceType.TEMPLATE, myFilter, AclControlType.READ);
                showWatchBox();

            } catch (Exception myException) {

                hideWatchBox();
                Dialog.showException(myException);
            }
        }
    }

    //
    // Request data from the server to initialize the list dataviews available for exporting
    //
    private void retrieveThemeDisplayList() {

        if (null == _themeList) {

            VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                ResourceFilter myFilter = WebMain.injector.getMainPresenter().getDefaultResourceFilter();

                myVortexFuture.addEventHandler(handleThemeListRequestResponse);
                myVortexFuture.execute(ModelActionsServiceProtocol.class).getFilteredResourceList(
                        AclResourceType.THEME, myFilter, AclControlType.READ);
                showWatchBox();

            } catch (Exception myException) {

                hideWatchBox();
                Dialog.showException(myException);
            }
        }
    }

    //
    //
    //
    private void createXmlExport(String uuidIn) {

        if (null != uuidIn) {

            showWatchBox();

            VortexFuture<String> myVortexFuture = WebMain.injector.getVortex().createFuture();
            try {

                myVortexFuture.addEventHandler(new VortexEventHandler<String>() {
                    @Override
                    public void onSuccess(String result) {

                        _fileName = result;
                        _fileExtension = ExportImportConstants.XML_SUFFIX;
                        if (AclResourceType.DATAVIEW.equals(_resourceType)) {

                            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportDataView(uuidIn);

                        } else if (AclResourceType.TEMPLATE.equals(_resourceType)) {

                            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportTemplate(uuidIn);

                        } else if (AclResourceType.THEME.equals(_resourceType)
                                || AclResourceType.GRAPH_THEME.equals(_resourceType)
                                || AclResourceType.MAP_THEME.equals(_resourceType)) {

                            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportTheme(uuidIn);
                        }
                    }

                    @Override
                    public boolean onError(Throwable t) {
                        hideWatchBox();
                        Dialog.showException(t);
                        return false;
                    }

                    @Override
                    public void onUpdate(int taskProgess, String taskMessage) {
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                myVortexFuture.execute(ExportActionsServiceProtocol.class).createExportName(_resourceType, uuidIn, WebMain.getClientStartupInfo().getExportFileNameComponentOrder());
            } catch (Exception myException) {

                hideWatchBox();
                Dialog.showException(myException);
            }
        }
    }


    private void createExport(List<String> listIn) {
        showWatchBox();

        VortexFuture<String> myVortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            myVortexFuture.addEventHandler(new VortexEventHandler<String>() {
                @Override
                public void onSuccess(String result) {
                    _fileName = result;
                    _fileExtension = ExportImportConstants.EXPORT_SUFFIX;
                    if ((null != listIn) && (0 < listIn.size())) {

                        if (AclResourceType.DATAVIEW.equals(_resourceType)) {

                            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportDataViews(
                                    listIn, themesCheckBox.getValue(), iconsCheckBox.getValue(), mapsCheckBox.getValue(), false);

                        } else if (AclResourceType.TEMPLATE.equals(_resourceType)) {

                            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportTemplates(
                                    listIn, themesCheckBox.getValue(), iconsCheckBox.getValue(), mapsCheckBox.getValue());

                        } else if (AclResourceType.THEME.equals(_resourceType)
                                || AclResourceType.GRAPH_THEME.equals(_resourceType)
                                || AclResourceType.MAP_THEME.equals(_resourceType)) {

                            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportThemes(
                                    listIn, iconsCheckBox.getValue(), mapsCheckBox.getValue());
                        }

                    } else if (themesCheckBox.getValue() || iconsCheckBox.getValue() || mapsCheckBox.getValue()) {

                        _resourceType = themesCheckBox.getValue()
                                ? AclResourceType.THEME
                                : (iconsCheckBox.getValue()
                                ? AclResourceType.ICON
                                : (mapsCheckBox.getValue()
                                ? AclResourceType.MAP_BASEMAP
                                : AclResourceType.UNKNOWN));
                        WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportSupportingResources(
                                themesCheckBox.getValue(), iconsCheckBox.getValue(), mapsCheckBox.getValue());
                    }

                }

                @Override
                public boolean onError(Throwable t) {
                    hideWatchBox();
                    Dialog.showException(t);
                    return false;
                }

                @Override
                public void onUpdate(int taskProgess, String taskMessage) {

                }

                @Override
                public void onCancel() {
                }
            });
            myVortexFuture.execute(ExportActionsServiceProtocol.class).createExportName(_resourceType, listIn.get(0), WebMain.getClientStartupInfo().getExportFileNameComponentOrder());
        } catch (Exception myException) {
            hideWatchBox();
            Dialog.showException(myException);
        }


    }

    private void listDataviews() {

        _resourceType = AclResourceType.DATAVIEW;
        _activeList = _dataviewList;
        themesCheckBox.setValue(_includeThemes);
        initializeDisplay();
    }

    private void listTemplates() {

        _resourceType = AclResourceType.TEMPLATE;
        _activeList = _templateList;
        themesCheckBox.setValue(_includeThemes);
        initializeDisplay();
    }

    private void listThemes() {

        _resourceType = AclResourceType.THEME;
        _activeList = _themeList;
        _includeThemes = themesCheckBox.getValue();
        initializeDisplay();
    }

    private void enableDisableCheckBoxes() {

        iconsCheckBox.setEnabled(getMainPresenter().isIconAdmin() && (!rawXmlCheckBox.getValue()));
        mapsCheckBox.setEnabled(!rawXmlCheckBox.getValue());
        themesCheckBox.setEnabled(!rawXmlCheckBox.getValue());
        multiCheckBox.setEnabled(!rawXmlCheckBox.getValue());
        rawXmlCheckBox.setEnabled(!(AclResourceType.THEME.equals(_resourceType) || multiCheckBox.getValue()
                || mapsCheckBox.getValue() || iconsCheckBox.getValue() || themesCheckBox.getValue()));
        _checkBoxValid = mapsCheckBox.getValue() || iconsCheckBox.getValue() || themesCheckBox.getValue();

        exportButton.setEnabled(_selectionValid || _checkBoxValid);

        labelCheckBoxes();
    }

    private void labelCheckBoxes() {

        List<ResourceBasics> mySelection = selectorWidget.getSelectionList();

        if ((null != mySelection) && (0 < mySelection.size())) {

            iconsCheckBox.setText(_constants.export_RequiredIcons());

            if (themeRadioButton.getValue()) {

                themesCheckBox.setText(_constants.export_SelectedThemes());
                themesCheckBox.setValue(true);
                themesCheckBox.setEnabled(false);
                mapsCheckBox.setText(_constants.export_AllMaps());

            } else {

                themesCheckBox.setValue(_includeThemes);
                themesCheckBox.setEnabled(true);
                themesCheckBox.setText(_constants.export_RequiredThemes());
                mapsCheckBox.setText(_constants.export_RequiredMaps());
            }

        } else {

            themesCheckBox.setText(_constants.export_AllThemes());
            iconsCheckBox.setText(_constants.export_AllIcons());
            mapsCheckBox.setText(_constants.export_AllMaps());
        }
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}

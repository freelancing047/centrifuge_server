package csi.client.gwt.dataview.linkup;

import java.util.*;

import com.github.gwtbootstrap.client.ui.*;

import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.csiwizard.widgets.CsiTabWidget;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.staging.LinkupExtenderStage;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.*;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.buttons.CyanButton;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.list_boxes.CsiListBox;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupExtender;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;


public class AdvancedLinkupParameterDialog implements SelectionChangeResponder, ValidityCheck {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface AdvancedLinkupParameterDialogUiBinder extends UiBinder<Widget, AdvancedLinkupParameterDialog> {
    }

    private class VizXfer {

        CsiTabWidget csiTab = null;
        TabLink tabLink = null;
        TextBox nameInput = null;
        VisualizationDef vizDef = null;
        Set<String> nameSet = new HashSet<String>();
        List<CsiTabWidget> tabList = new ArrayList<CsiTabWidget>();

        VizXfer(VisualizationDef vizDefIn) {

            vizDef = vizDefIn;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @UiField
    ValidatingDialog dialog;
    
    @UiField
    AbsolutePanel greyPanel;
    @UiField
    AbsolutePanel tabPanel;
    @UiField
    AbsolutePanel whitePanel;

    @UiField(provided = true)
    CsiListBox<VisualizationDef> availableVisualizations;
    @UiField
    CyanButton setAddButton;
    
    @UiField
    Label availableVisualizationsLabel;
    @UiField
    Label parameterNotice;

    private CsiTabPanel parameterSetTabPanel = null;
    private Button applyButton;
    private Button cancelButton;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static AdvancedLinkupParameterDialogUiBinder uiBinder = GWT.create(AdvancedLinkupParameterDialogUiBinder.class);

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final String _txtDialogTitle = _constants.advancedLinkupParameterDialog_DialogTitle();
    private final String _txtHelpTarget = null;//_constants.advancedLinkupParameterDialog_HelpTarget();
    private final String _txtRequiredParameterSetIdentification = _constants.advancedLinkupParameterDialog_RequiredParameterSetIdentification();
    private final String _txtAvailableVisualizationsLabel = _constants.advancedLinkupParameterDialog_AvailableVisualizationsLabel();
    private final String _txtSetAddButton = _constants.advancedLinkupParameterDialog_SetAddButton();
    private final String _txtApplyButton = _constants.dialog_ApplyButton();
    private final String _txtSelectVisualization = _constants.advancedLinkupParameterDialog_SelectVisualization();
    private final String _txtAddParameterSet = _constants.advancedLinkupParameterDialog_AddParameterSet();
    private final String _txtCancelButton = _constants.dialog_CancelButton();

    private HandlerRegistration _selectionHandler = null;

    private AbstractDataViewPresenter _dvPresenter;
    private DataViewDef _template;
    private List<LinkupExtender> _extenders;
    private Map<String, VisualizationDef> _vizMap;
    private Map<String, VizXfer> _sourceMap;
    List<LinkupExtenderStage> _deletedSource;
    private VizXfer _activeSource;
    private VizXfer _noViz;
    private String _priorTabLabel = null;
    private Integer _nextId = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle clicking the Add Parameter Set button
    //
    private ClickHandler handleSetAddButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                dialog.suspendMonitoring();
                createParameterSet();

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 01", myException);
            }
            dialog.beginMonitoring();
        }
    };
    
    //
    // Handle clicking the Delete Parameter Set button
    //
    ClickHandler handlesetDeleteButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                dialog.suspendMonitoring();
                deleteParameterSet();

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 02", myException);
            }
            dialog.beginMonitoring();
        }
    };
    
    //
    // Handle clicking the ? (help) button for selecting a visualization
    //
    private ClickHandler handleVizHelpButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                HelpWindow.display(_txtHelpTarget + "#Visualizations"); //$NON-NLS-1$

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 03", myException);
            }
        }
    };
    
    //
    // Handle clicking the ? (help) button
    //
    private ClickHandler handleParmHelpButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                HelpWindow.display(_txtHelpTarget + "#Parameters"); //$NON-NLS-1$

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 04", myException);
            }
        }
    };

    //
    // Handle clicking the Apply button
    //
    private ClickHandler handleApplyButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                dialog.suspendMonitoring();
                finalizeChanges();

                //
                // Hide dialog
                //
                dialog.removeFromParent();
                dialog.hide();

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 05", myException);
            }
        }
    };

    //
    // Handle clicking the Cancel button
    //
    private ClickHandler handleCancelButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                dialog.suspendMonitoring();
                //
                // Hide dialog
                //
                dialog.removeFromParent();
                dialog.hide();

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 06", myException);
            }
        }
    };

    //
    // Handle valid selection from visualization selection combobox drop-down
    //
    private SelectionChangedEvent.SelectionChangedHandler<VisualizationDef> handleVisualizationSelection
    = new SelectionChangedEvent.SelectionChangedHandler<VisualizationDef>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<VisualizationDef> eventIn) {

            List<VisualizationDef> myList = eventIn.getSelection();
            VisualizationDef myVizDef = ((null != myList) && (0 < myList.size())) ? myList.get(0) : null;

            try {

                dialog.suspendMonitoring();
                if ((null != _activeSource) && (null != _activeSource.nameSet)
                        && (null != _priorTabLabel) && (0 < _priorTabLabel.trim().length())) {

                    _activeSource.nameSet.add(_priorTabLabel.trim());
                }
                if (null != myVizDef) {

                    _activeSource = _sourceMap.get(myVizDef.getLocalId());
                    if ((null != _activeSource) && (null != _activeSource.nameSet)
                            && (null != _activeSource.tabLink) && (null != _activeSource.tabLink)) {

                        String myLabel = _activeSource.tabLink.getText().trim();
                        if ((null != myLabel) && (0 < myLabel.trim().length())) {

                            _activeSource.nameSet.remove(myLabel.trim());
                            _activeSource.nameInput.getElement().getStyle().setColor(Dialog.txtLabelColor);
                            _activeSource.nameInput.setFocus(true);
                        }
                    }

                } else {

                    _activeSource = _noViz;
                }
                prepareParameterSetAccess();
                _priorTabLabel = null;

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 12", myException);
            }
            dialog.beginMonitoring();
        }
    };

    private TabPanel.ShowEvent.Handler handllePreProcessTab
            = new TabPanel.ShowEvent.Handler() {

        @Override
        public void onShow(TabPanel.ShowEvent shownEvent) {

            dialog.suspendMonitoring();
            if ((null != _activeSource) && (null != _activeSource.nameSet)
                    && (null != _priorTabLabel) && (0 < _priorTabLabel.trim().length())) {

                _activeSource.nameSet.add(_priorTabLabel.trim());
            }
            _priorTabLabel = null;
        }
    };

    private TabPanel.ShownEvent.Handler handllePostProcessTab
            = new TabPanel.ShownEvent.Handler() {

        @Override
        public void onShow(TabPanel.ShownEvent shownEvent) {

            DeferredCommand.add(new Command() {
                public void execute() {

                    _activeSource.tabLink = parameterSetTabPanel.getActiveLink();
                    _activeSource.csiTab = (CsiTabWidget)parameterSetTabPanel.getActiveTab();
                    if (null != _activeSource.csiTab) {

                        _activeSource.nameInput
                                = ((AdvancedLinkupParameterPanel)_activeSource.csiTab.getWidget()).setNameTextBox;
                    }
                    _priorTabLabel = _activeSource.tabLink.getText().trim();
                    if ((null != _activeSource.nameSet)
                            && (null != _priorTabLabel) && (0 < _priorTabLabel.trim().length())) {

                        _activeSource.nameSet.remove(_priorTabLabel.trim());
                        _activeSource.nameInput.getElement().getStyle().setColor(Dialog.txtLabelColor);
                        _activeSource.nameInput.setFocus(true);
                    }
                    dialog.beginMonitoring();
                }
            });
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AdvancedLinkupParameterDialog(AbstractDataViewPresenter dvPresenterIn,
                                         DataViewDef templateIn, List<LinkupExtender> extendersIn) {

        //
        // Provide access to local copy of the DataView and linkup definition
        //
        _dvPresenter = dvPresenterIn;
        _template = templateIn;
        _extenders = extendersIn;
        _noViz = new VizXfer(null);
        _activeSource = _noViz;
        _deletedSource = new ArrayList<LinkupExtenderStage>();

        //
        //Initialize the visualization selection combo box and the visualization input panel map
        //
        initializeVisualizationList(_dvPresenter.getVizDefs());

        //
        // Link UI XML code to this file and all GWT to create remaining components
        //
        uiBinder.createAndBindUi(this);
        
        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtDialogTitle, _txtHelpTarget, true);

        //
        // set the colors for the tab panel we are creating
        //
        greyPanel.getElement().getStyle().setBackgroundColor(Dialog.txtBorderColor);
        tabPanel.getElement().getStyle().setBackgroundColor(Dialog.txtBorderColor); //$NON-NLS-1$
        whitePanel.getElement().getStyle().setBackgroundColor("#ffffff"); //$NON-NLS-1$

        availableVisualizationsLabel.setText(_txtAvailableVisualizationsLabel);
        availableVisualizationsLabel.setVisible(true);
        
        parameterNotice.getElement().getStyle().setColor(Dialog.txtInfoColor);
        parameterNotice.setText(_txtRequiredParameterSetIdentification);
        parameterNotice.setVisible(true);

        //
        // Set up the parameter set add button
        //
        setAddButton.setText(_txtSetAddButton);
        setAddButton.setVisible(true);
        setAddButton.setEnabled(true);
        setAddButton.addClickHandler(handleSetAddButtonClick);

        //
        // Set up the dialog cancel button
        //
        cancelButton = dialog.getCancelButton();
        cancelButton.setText(_txtCancelButton);
        cancelButton.setVisible(true);
        cancelButton.setEnabled(true);
        cancelButton.addClickHandler(handleCancelButtonClick);
        
        //
        // Set up the dialog save button
        //
        applyButton = dialog.getActionButton();
        applyButton.setText(_txtApplyButton);
        applyButton.setVisible(true);
        applyButton.setEnabled(false);
        applyButton.addClickHandler(handleApplyButtonClick);

        initializeData();
        //
        // Attach handlers to help buttons
        //
        /*vizHelpButton.addClickHandler(handleVizHelpButtonClick);
        vizHelpButton.addClickHandler(handleParmHelpButtonClick);*/
        prepareParameterSetAccess();
    }

    public void show() {

        try {

            dialog.setCallBack(this);
            dialog.show(70);

        } catch (Exception myException) {

            Dialog.showException("AdvancedLinkupParameterDialog 15", myException);
        }
    }

    public void checkValidity() {

        try {

            boolean myOk = false;

            if (null != _activeSource.csiTab) {

                boolean myNameOk = (null != _activeSource.nameInput) ? checkSetName(_activeSource.nameInput.getText().trim()) : false;

                parameterNotice.getElement().getStyle().setColor(Dialog.txtInfoColor);
                parameterNotice.setText(_txtRequiredParameterSetIdentification);

                if (null != availableVisualizations.getCurrentValue()) {

                    myOk = okToChange(myNameOk);

                } else {

                    parameterNotice.getElement().getStyle().setColor(Dialog.txtWarningColor);
                    parameterNotice.setText(_txtSelectVisualization);
                    availableVisualizations.setEnabled(true);
                    setAddButton.setEnabled(false);
                }
                applyButton.setEnabled(myOk);
            }

       } catch (Exception myException) {

            Dialog.showException("AdvancedLinkupParameterDialog 16", myException);
        }
    }

    @Override
    public void selectionChange(Object dataRowIn) {
    }

    @Override
    public void rowComplete(Object dataRowIn) {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeData() {

        //
        // Create all the panels for all the visualizations and add them to the map created above
        //
        createAllPanels();
        //
        // Perform the initial button validation
        //
        availableVisualizations.setEnabled(true);
        dialog.beginMonitoring();
    }

    private void createAllPanels() {

        int myErrorCount = 0;

        for (LinkupExtender myExtender : _extenders) {

            String myVizId = myExtender.getVizDefId();

            if (null != myVizId) {

                VizXfer myMapping = _sourceMap.get(myVizId);
                if (null != myMapping) {

                    Set<String> mySet =  myMapping.nameSet;
                    List<CsiTabWidget> myList =  myMapping.tabList;

                    if (null != myList) {

                        AdvancedLinkupParameterPanel myPanel
                                = new AdvancedLinkupParameterPanel(this, _dvPresenter, _template, myMapping.vizDef,
                                                                    (LinkupExtenderStage)myExtender,
                                                                    _txtHelpTarget, _nextId++);
                        CsiTabWidget myWidget = new CsiTabWidget(myPanel, myPanel.getLabel(), null);
                        myWidget.setTopMargin(0);
                        myList.add(myWidget);
                        mySet.add(myPanel.getLabel());

                    } else {

                        myErrorCount++;
                    }
                }
            }
        }
        if (0 < myErrorCount) {

            Display.error("Advanced Linkup Parameter Dialog",
                    _constants.advancedLinkupParameterDialog_MissingReferencedViz(Integer.toString(myErrorCount)));
        }
    }

    private void addParameterSetTab(CsiTabWidget sourceIn) {

        if (null == parameterSetTabPanel) {

            createTabPanel();
        }
        if (null != parameterSetTabPanel) {

            parameterSetTabPanel.add(sourceIn);
        }
    }

    //
    // Set up the tab bar for selecting a parameter set
    //
    private void createTabPanel() {

        if (null == parameterSetTabPanel) {

            parameterSetTabPanel = new CsiTabPanel();
            parameterSetTabPanel.addShowHandler(handllePreProcessTab);
            parameterSetTabPanel.addShownHandler(handllePostProcessTab);
            tabPanel.add(parameterSetTabPanel);
        }
    }

    private void refreshTabPanel() {
        
        if (null == parameterSetTabPanel) {
            
            createTabPanel();
        }
        if (null != parameterSetTabPanel) {
            
            clearTabPanel();
        }

        if ((null != _activeSource.tabList) && (0 < _activeSource.tabList.size())) {

            for (CsiTabWidget mySource : _activeSource.tabList) {

                addParameterSetTab(mySource);
            }
            parameterSetTabPanel.selectTab(0);
        }
    }

    private void clearTabPanel() {
        
        if (null != parameterSetTabPanel) {

            parameterSetTabPanel.clear();
        }
    }

    private void createParameterSet() {

        if (null != _activeSource.tabList) {

            AdvancedLinkupParameterPanel myPanel
                    = new AdvancedLinkupParameterPanel(this, _dvPresenter, _template, _activeSource.vizDef,
                                                        null, _txtHelpTarget, _nextId++);
            CsiTabWidget myWidget = new CsiTabWidget(myPanel, myPanel.getLabel(), null);
            myWidget.setTopMargin(0);
            _activeSource.tabList.add(myWidget);
            addParameterSetTab(myWidget);
            parameterSetTabPanel.selectTab(myWidget);
            myPanel.setVisible(true);
        }
    }

    private void deleteParameterSet() {

        if ((null != parameterSetTabPanel) && (null != _activeSource)) {

            AdvancedLinkupParameterPanel myPanel = (AdvancedLinkupParameterPanel)_activeSource.csiTab.getWidget();
            LinkupExtenderStage myExtender = myPanel.getExtender();

            if (null != _activeSource.csiTab) {

                parameterSetTabPanel.remove(_activeSource.csiTab);
            }
            if (!myExtender.wasCreated()) {

                _deletedSource.add(myExtender);
            }
            _activeSource.tabList.remove(_activeSource.csiTab);
            _activeSource.tabLink = null;
            _activeSource.nameInput = null;
            _activeSource.csiTab = null;
            _priorTabLabel = null;
            enableAll();
            if (0 < parameterSetTabPanel.getWidgetCount()) {

                parameterSetTabPanel.selectTab(0);
            }
        }
    }

    private void prepareParameterSetAccess() {
        
        refreshTabPanel();
    }

    private void finalizeChanges() {

        //
        // Transfer staging information
        //
        if ((null != _sourceMap) && (0 < _sourceMap.size())) {

            for (VizXfer myMapping : _sourceMap.values()) {

                if ((null != myMapping.tabList) && (0 < myMapping.tabList.size())) {

                    CsiTabWidget[] myTabArray = myMapping.tabList.toArray(new CsiTabWidget[0]);

                    for (int i = 0; myTabArray.length > i; i++) {

                        AdvancedLinkupParameterPanel myPanel = (AdvancedLinkupParameterPanel)myTabArray[i].getWidget();

                        if (null != myPanel) {

                            myPanel.finalize(_extenders);
                        }
                    }
                }
            }
        }
        if ((null != _deletedSource) && (0 < _deletedSource.size())) {

            for (LinkupExtenderStage myExtender : _deletedSource) {

                myExtender.finalize(_extenders);
            }
            _deletedSource.clear();
        }
    }
    private void initializeVisualizationList(List<VisualizationDef> listIn) {

        _sourceMap = new HashMap<String, VizXfer>();
        _vizMap = new HashMap<String, VisualizationDef>();
        availableVisualizations = new CsiListBox<VisualizationDef>();
        availableVisualizations.addSelectionChangedHandler(handleVisualizationSelection);

        for (VisualizationDef myViz : listIn) {

            if (VisualizationType.RELGRAPH_V2 == myViz.getType()) {

                String myName = myViz.getName();
                String myId = myViz.getLocalId();

                _sourceMap.put(myId, new VizXfer(myViz));
                _vizMap.put(myId, myViz);
                availableVisualizations.addItem(myName, myViz);
            }
        }
        availableVisualizations.setValue(null);
    }

    //
    // Check for conflict between presumably new parameter set name and existing names
    //
    private boolean checkSetName(String nameIn) {

        boolean myOK = true;

        if ((null != _activeSource.nameInput) && (null != _activeSource.tabLink)) {
            
            myOK = false;
            if (null != nameIn) {

                if (!nameIn.equals(_priorTabLabel)) {

                    // Copy name to tab
                    changeTabLabel(nameIn);
                    _priorTabLabel = nameIn;
                }
            }

            if ((null != nameIn) && (0 < nameIn.length())) {

                if ((null != _activeSource.nameSet) && (_activeSource.nameSet.contains(nameIn))) {
                    _activeSource.nameInput.getElement().getStyle().setColor(Dialog.txtErrorColor);
                } else {
                    _activeSource.nameInput.getElement().getStyle().setColor(Dialog.txtLabelColor);
                    myOK = true;
                }
            }
        }
        return myOK;
    }

    private boolean okToChange(boolean nameOkIn) {

        boolean myOk;

        if ((null == parameterSetTabPanel) || (0 == parameterSetTabPanel.getWidgetCount())) {

            myOk = true;
            parameterNotice.setText(_txtAddParameterSet);

        } else {

            myOk = okToAdd(nameOkIn);
        }
        availableVisualizations.setEnabled(myOk);
        setAddButton.setEnabled(myOk);
        parameterSetTabPanel.enableTabs(myOk);

        return myOk;
    }

    private boolean okToAdd(boolean nameOkIn) {

        AdvancedLinkupParameterPanel myPanel = (null != _activeSource.csiTab)
                                                    ? (AdvancedLinkupParameterPanel)_activeSource.csiTab.getWidget()
                                                    : null;
        String myCaution = (null != myPanel) ? myPanel.okToAdd(nameOkIn) : null;

        if (null != myCaution) {

            parameterNotice.getElement().getStyle().setColor(Dialog.txtWarningColor);
            parameterNotice.setText(myCaution);
        }
        return (null == myCaution);
    }

    private void changeTabLabel(String labelIn) {

        if ((null != parameterSetTabPanel) && (null != labelIn) && (0 < labelIn.length())){

            if ((null != labelIn) && (null != _activeSource.tabLink)) {

                _activeSource.tabLink.setText(labelIn);
                if (null != _activeSource.csiTab) {

                    _activeSource.csiTab.setLabel(labelIn);
                }
            }
        }
    }

    private void enableAll() {

        if (null != parameterSetTabPanel) {

            parameterSetTabPanel.enableTabs();
        }
        applyButton.setEnabled(true);
        setAddButton.setEnabled(true);
        availableVisualizations.setEnabled(true);
    }
}

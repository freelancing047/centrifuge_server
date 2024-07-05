package csi.client.gwt.dataview.linkup;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.grid.Grid;

import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.staging.LinkupExtenderStage;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.RedButton;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupExtender;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

import java.util.*;


public class AdvancedLinkupParameterPanel extends Composite {
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface AdvancedLinkupParameterPanelUiBinder extends UiBinder<Widget, AdvancedLinkupParameterPanel> {
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    AbsolutePanel parameterSetSelectionPanel;
    @UiField
    VerticalPanel parameterSetControlPanel;
    @UiField
    VerticalPanel parameterSetDataPanel;

    @UiField(provided = true)
    CsiStringListBox availableNodes;
    @UiField(provided = true)
    CsiStringListBox availableLinks;

    @UiField
    RedButton setDeleteButton;
//    @UiField
//    Button vizHelpButton;
//    @UiField
//    Button parmHelpButton;
    @UiField
    Label setNameLabel;
    @UiField
    Label setDescriptionLabel;
    @UiField
    Label optionalParms;
    @UiField
    Label templateParmsLabel;
    /* if (_fieldParmsSupported) {
    @UiField
    Label fieldParmsLabel;
    } */
    @UiField
    Label notRelGraphLabel;
    @UiField
    Label noNodesLabel;
    @UiField
    Label noEdgesLabel;
   
    @UiField
    TextBox setNameTextBox;
    @UiField
    TextBox setDescriptionTextBox;

    @UiField
    RadioButton useAllRadioButton;
    @UiField
    RadioButton useNodeRadioButton;
    @UiField
    RadioButton useLinkRadioButton;

    @UiField
    CheckBox setDisabledCheckBox;
    
    @UiField(provided = true)
    Grid<FieldToLabelMapStore> templateParms;
    /* if (_fieldParmsSupported) {
    @UiField(provided = true)
    Grid<?> fieldParms;
    } */
    private HorizontalPanel noParametersPanel = null;
    private Label noParametersLabel = null;
    private LinkupExtenderStage _extender;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static AdvancedLinkupParameterPanelUiBinder uiBinder = GWT.create(AdvancedLinkupParameterPanelUiBinder.class);

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final String _txtSetDeleteButton = _constants.advancedLinkupParameterDialog_SetDeleteButton();
    private final String _txtSetNameLabel = _constants.advancedLinkupParameterDialog_SetNameLabel();
    private final String _txtSetDescriptionLabel = _constants.advancedLinkupParameterDialog_SetDescriptionLabel();
    private final String _txtUseAllRadioButton = _constants.advancedLinkupParameterDialog_UseAllRadioButton();
    private final String _txtUseNodeRadioButton = _constants.advancedLinkupParameterDialog_UseNodeRadioButton();
    private final String _txtUseLinkRadioButton = _constants.advancedLinkupParameterDialog_UseLinkRadioButton();
    private final String _txtNotRelGraphLabel = _constants.advancedLinkupParameterDialog_NotRelGraphLabel();
    private final String _txtNoNodesLabel = _constants.advancedLinkupParameterDialog_NoNodesLabel();
    private final String _txtNoEdgesLabel = _constants.advancedLinkupParameterDialog_NoEdgesLabel();
    private final String _txtDelayedInputOption = _constants.advancedLinkupParameterDialog_DelayedInputOption();
    private final String _txtFillParameterGridLabel = _constants.advancedLinkupParameterDialog_FillParameterGridLabel();
    private final String _txtNoParametersLabel = _constants.advancedLinkupParameterDialog_NoParametersLabel();
    private final String _txtDisabledCheckBox = _constants.advancedLinkupParameterDialog_DisabledCheckBox();
    private final String _txtEnterParameterSetName = _constants.advancedLinkupParameterDialog_EnterParameterSetName();
    private final String _txtSelectNode = _constants.advancedLinkupParameterDialog_SelectNode();
    private final String _txtSelectEdge = _constants.advancedLinkupParameterDialog_SelectEdge();
//    private final String _txtIgnoreParameterGridLabel = _constants.advancedLinkupParameterDialog_IgnoreParameterGridLabel();
//    private final String _txtFieldGridLabel = _constants.advancedLinkupParameterDialog_FieldGridLabel();

    AdvancedLinkupParameterDialog _parent;
    String _txtHelpTarget;
    AbstractDataViewPresenter _dvPresenter;
    DataViewDef _template;
    LinkupParameterMapper _parameterMapper;
    /* if (_fieldParmsSupported) {
    private LinkupConditionalFieldMapper _conditionalMapper;
    } */
    private Map<String, NodeDef> _nodeMap = null;
    private Map<String, LinkDef> _linkMap = null;
    private List<NodeDef> _nodeList;
    private List<LinkDef> _linkList;
    private String _vizDefId = null;
    private boolean _hasParameters = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle clicking of the "useAllRadioButton" radio button.
    //
    private ClickHandler handleuseAllRadioButtonClicked
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                availableNodes.setEnabled(false);
                availableLinks.setEnabled(false);

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 09", myException);
            }
        }
    };

    //
    // Handle clicking of the "useNodeRadioButton" radio button.
    //
    private ClickHandler handleuseNodeRadioButtonClicked
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                availableNodes.setEnabled(true);
                availableLinks.setEnabled(false);

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 10", myException);
            }
        }
    };

    //
    // Handle clicking of the "useAllRadioButton" radio button.
    //
    private ClickHandler handleuseLinkRadioButtonClicked
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                availableNodes.setEnabled(false);
                availableLinks.setEnabled(true);

            } catch (Exception myException) {

                Dialog.showException("AdvancedLinkupParameterDialog 11", myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AdvancedLinkupParameterPanel(AdvancedLinkupParameterDialog parentIn, AbstractDataViewPresenter dvPresenterIn,
                                        DataViewDef templateIn, VisualizationDef vizDefIn,
                                        LinkupExtenderStage extenderIn, String txtHelpTargetIn, int idIn) {

        String myRadioGroup = "restrictions_" + Integer.toString(idIn);

        _parent = parentIn;
        _dvPresenter = dvPresenterIn;
        _extender = (null != extenderIn)
                        ? new LinkupExtenderStage(extenderIn)
                        : new LinkupExtenderStage(vizDefIn.getLocalId());
        _txtHelpTarget = txtHelpTargetIn;

        //
        // Set up available nodes combobox
        //
        availableNodes = setUpListBox(null);

        //
        // Set up available edges combobox
        //
        availableLinks = setUpListBox(null);

        /* if (_fieldParmsSupported) {

            //
            // Create object to handler linkup parameter mapping
            // and set up the parameter mapping grid
            //
            _parameterMapper = new LinkupParameterMapper(_dvPresenter, 420, 180, _txtHelpTarget + "#ParameterGrid");
            templateParms = _parameterMapper.getGrid();

            //
            // Create object to handler linkup conditional field mapping
            // and set up the conditional field mapping grid
            //
            _conditionalMapper = new LinkupConditionalFieldMapper(_dvPresenter, 420, 120, _txtHelpTarget + "#FieldGrid");
            fieldParms = _conditionalMapper.getGrid();
        } else { */

        //
        // Create object to handler linkup parameter mapping
        // and set up the parameter mapping grid
        //
        _parameterMapper = new LinkupParameterMapper(_parent, _dvPresenter, 420, 330,
                                                        _txtHelpTarget + "#ParameterGrid"); //$NON-NLS-1$
        templateParms = _parameterMapper.getGrid();

        //
        // Link UI XML code to this file and all GWT to create remaining components
        //
        initWidget(uiBinder.createAndBindUi(this));

        parameterSetSelectionPanel.getElement().getStyle().setBackgroundColor("#ffffff"); //$NON-NLS-1$

        //
        // Initialize transient labels
        //
        optionalParms.getElement().getStyle().setColor(Dialog.txtInfoColor);
        optionalParms.setText(_txtDelayedInputOption);
        templateParmsLabel.setText(_txtFillParameterGridLabel);
        /* if (_fieldParmsSupported) {
        fieldParmsLabel.setText(_txtFieldGridLabel);
        */
        notRelGraphLabel.getElement().getStyle().setColor(Dialog.txtWarningColor);
        notRelGraphLabel.setText(_txtNotRelGraphLabel);
        notRelGraphLabel.setVisible(false);
        noNodesLabel.getElement().getStyle().setColor(Dialog.txtWarningColor);
        noNodesLabel.setText(_txtNoNodesLabel);
        noNodesLabel.setVisible(false);
        noEdgesLabel.getElement().getStyle().setColor(Dialog.txtWarningColor);
        noEdgesLabel.setText(_txtNoEdgesLabel);
        noEdgesLabel.setVisible(false);

        //
        // Set up the check box for disabling a parameter set
        //
        setDisabledCheckBox.setBoxLabel(_txtDisabledCheckBox);
        setDisabledCheckBox.setValue(false);
        setDisabledCheckBox.setVisible(true);
        setDisabledCheckBox.setEnabled(true);

        //
        // Set up the parameter set delete button
        //
        setDeleteButton.setText(_txtSetDeleteButton);
        setDeleteButton.setVisible(true);
        setDeleteButton.setEnabled(true);
        setDeleteButton.addClickHandler(_parent.handlesetDeleteButtonClick);

        //
        // Set up labels for parameter set panel components
        //
        setNameLabel.setText(_txtSetNameLabel);

        //
        // Set up labels for parameter set panel components
        //
        setDescriptionLabel.setText(_txtSetDescriptionLabel);

        //
        // Set up labels for parameter set panel components
        //
        useAllRadioButton.setName(myRadioGroup);
        useAllRadioButton.setText(_txtUseAllRadioButton);
        useAllRadioButton.addClickHandler(handleuseAllRadioButtonClicked);
        useNodeRadioButton.setName(myRadioGroup);
        useNodeRadioButton.setText(_txtUseNodeRadioButton);
        useNodeRadioButton.addClickHandler(handleuseNodeRadioButtonClicked);
        useLinkRadioButton.setName(myRadioGroup);
        useLinkRadioButton.setText(_txtUseLinkRadioButton);
        useLinkRadioButton.addClickHandler(handleuseLinkRadioButtonClicked);

        _template = templateIn;
        _hasParameters = false;

        if (null != _template) {

            List<QueryParameterDef> myList = _template.getDataSetParameters();

            _hasParameters = ((null != myList) && (0 < myList.size()));
        }
        initializeParameterTab(vizDefIn);

        //
        // Initialize with template data
        //
        _parameterMapper.initGridFields(_template);
        /* if (_fieldParmsSupported) {

            _conditionalMapper.initGridFields(_template);
        } */
        loadValues(_extender);

        //
        // Adjust the grids size and accessibility
        //
        displayGrids();
    }

    public TabItemConfig getConfig() {

        return getConfig(getLabel());
    }

    public TabItemConfig getConfig(String labelIn) {

        return new TabItemConfig(getLabel(labelIn), false);
    }

    public String getLabel(String labelIn) {

        return (null != labelIn) ? labelIn : "-- ? --";
    }

    public String getLabel() {

        return (null != getName()) ? getName() : "-- ? --";
    }

    public String getName() {

        return (null != _extender) ? _extender.getName() : null;
    }

    public LinkupExtenderStage getExtender() {

        return _extender;
    }

    public void finalize(List<LinkupExtender> extenderListIn) {

        extractData(_extender);
        _extender.finalize(extenderListIn);
    }

    void initializeParameterTab(VisualizationDef vizdefIn) {

        availableNodes.clear();
        availableLinks.clear();
        availableNodes.setEnabled(false);
        availableLinks.setEnabled(false);
        useAllRadioButton.setEnabled(false);
        useNodeRadioButton.setEnabled(false);
        useLinkRadioButton.setEnabled(false);
        _nodeList = null;
        _linkList = null;
        notRelGraphLabel.setVisible(false);
        noNodesLabel.setVisible(false);
        noEdgesLabel.setVisible(false);

        if (null != vizdefIn) {

            _vizDefId = vizdefIn.getLocalId();
            useAllRadioButton.setEnabled(true);

            if (VisualizationType.RELGRAPH_V2 == vizdefIn.getType()) {
                _nodeList = ((RelGraphViewDef)vizdefIn).getNodeDefs();
                _linkList = ((RelGraphViewDef)vizdefIn).getLinkDefs();

                if ((null != _nodeList) && (0 < _nodeList.size())) {
                    initializeNodeList(_nodeList);
                    useNodeRadioButton.setEnabled(true);
                } else {
                    noNodesLabel.setVisible(true);
                }
                if ((null != _linkList) && (0 < _linkList.size())) {
                    initializeLinkList(_linkList);
                    useLinkRadioButton.setEnabled(true);
                } else {
                    noEdgesLabel.setVisible(true);
                }
            } else {
                notRelGraphLabel.setVisible(true);
            }
        }
        useAllRadioButton.setValue(true, true);
    }

    String checkIntegrity() {

        return _parameterMapper.checkIntegrity();
    }

    void loadValues(LinkupExtender extenderIn) {

        if (_hasParameters) {

            _parameterMapper.initGridFields(_template);
        }
        if (null != extenderIn) {

            List<ParamMapEntry> myList = extenderIn.getParameterList() ;

            if (_hasParameters) {

                _parameterMapper.initGridFields(_template, myList);

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
            /* if (_fieldParmsSupported) {

                _conditionalMapper.initGridFields(_template, myList);
            } */
            setNameTextBox.setText(extenderIn.getName());
            setDescriptionTextBox.setText(extenderIn.getDescription());
            availableLinks.setValue(null);
            availableNodes.setValue(null);

            if ((null != extenderIn.getNodeDefId())) {

                try {

                    availableNodes.setSelectedValue(extenderIn.getNodeDefId());

                } catch (Exception myException) {

                    Dialog.showException("Caught exception trying to initialize Node selection.", myException);
                }
                useNodeRadioButton.setValue(true);
                availableNodes.setEnabled(true);

            } else if (null != extenderIn.getLinkDefId()) {

                try {

                    availableLinks.setSelectedValue(extenderIn.getLinkDefId());

                } catch (Exception myException) {

                    Dialog.showException("Caught exception trying to initialize Node selection.", myException);
                }
                useLinkRadioButton.setValue(true);
                availableLinks.setEnabled(true);

            } else {

                useAllRadioButton.setValue(true);
            }
            setDisabledCheckBox.setValue(extenderIn.getIsDisabled());
        }
    }

    List<ParamMapEntry> extractGridValues() {

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
    void displayGrids() {

        if (_hasParameters) {

            removeNoParametersNotification();

            /* if (_fieldParmsSupported) {

                templateParms.setHeight(180);
                fieldParms.setHeight(120);

            } else { */
            templateParms.setHeight(320);
            /* } */
            templateParmsLabel.getElement().setInnerText(_txtFillParameterGridLabel);
        } else {

            displayNoParametersNotification();

            /* if (_fieldParmsSupported) {

                templateParms.setHeight(80);
                fieldParms.setHeight(220);
                templateParmsLabel.getElement().setInnerText(_txtIgnoreParameterGridLabel);

            } else { */

            templateParms.setHeight(320);
            /* } */
        }

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
        templateParmsLabel.setVisible(true);
        optionalParms.setVisible(true);
//        templateParms.setVisible(true);
//        templateParms.setEnabled(_hasParameters);

        /* if (_fieldParmsSupported) {

            fieldParmsLabel.setVisible(true);
            fieldParms.setVisible(true);
            fieldParms.setEnabled(true);

        } else {

            fieldParmsLabel.setVisible(false);
        } */
    }
    void displayNoParametersNotification() {

        if (null == noParametersPanel) {

            noParametersPanel = new HorizontalPanel();
            noParametersPanel.setWidth("420px"); //$NON-NLS-1$
            noParametersPanel.setHeight("60px"); //$NON-NLS-1$
            parameterSetSelectionPanel.add(noParametersPanel, 470, 90);
            noParametersLabel = new Label(_txtNoParametersLabel);
            noParametersPanel.add(noParametersLabel);
            noParametersPanel.setCellHorizontalAlignment(noParametersLabel, HasHorizontalAlignment.ALIGN_CENTER);
            noParametersPanel.setCellVerticalAlignment(noParametersLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        }
        noParametersPanel.setVisible(parameterSetControlPanel.isVisible());
    }

    void removeNoParametersNotification() {

        if (null != noParametersPanel) {

            noParametersPanel.remove(noParametersLabel);
            parameterSetSelectionPanel.remove(noParametersPanel);
            noParametersPanel = null;
            noParametersLabel = null;
        }
    }

    private void initializeNodeList(List<NodeDef> listIn) {

        availableNodes.clear();

        for (NodeDef myNode : listIn) {

            availableNodes.addItem(myNode.getName());
        }
        availableNodes.setSelectedValue(null);
    }

    private void initializeLinkList(List<LinkDef> listIn) {

        availableLinks.clear();

        for (LinkDef myLink : listIn) {

            availableLinks.addItem(myLink.getDisplayName());
        }
        availableLinks.setSelectedValue(null);
    }

    String okToAdd(boolean nameOkIn) {

        String myCaution = _txtEnterParameterSetName;

        if (!nameOkIn) {

            myCaution = _txtEnterParameterSetName;

        } else if (useNodeRadioButton.getValue()
                && (null == availableNodes.getCurrentValue())) {

            myCaution = _txtSelectNode;

        } else if (useLinkRadioButton.getValue()
                && (null == availableLinks.getCurrentValue())) {

            myCaution = _txtSelectEdge;

        } else {

            myCaution = checkIntegrity();

        }
        return myCaution;
    }

    //
    //
    //
    CsiStringListBox setUpListBox(SelectionChangedEvent.SelectionChangedHandler<?> selectionHandlerIn) {

        CsiStringListBox myListBox = new CsiStringListBox();

        myListBox.sortAscending();
        if (null != selectionHandlerIn) {
            myListBox.addSelectionChangedHandler(selectionHandlerIn);
        }
        myListBox.setEnabled(false);

        return myListBox;
    }

    void extractData(LinkupExtender extenderIn) {

        extenderIn.setVizDefId(_vizDefId);
        extenderIn.setName(setNameTextBox.getText());
        extenderIn.setDescription(setDescriptionTextBox.getText());
        extenderIn.setIsDisabled(setDisabledCheckBox.getValue());
        extenderIn.setNodeDefId(null);
        extenderIn.setLinkDefId(null);
        if (useNodeRadioButton.getValue()) {

            extenderIn.setNodeDefId(availableNodes.getSelectedValue());

        } else if (useLinkRadioButton.getValue()) {

            extenderIn.setLinkDefId(availableLinks.getSelectedValue());
        }
        extenderIn.setParameterList(extractGridValues());
    }
}

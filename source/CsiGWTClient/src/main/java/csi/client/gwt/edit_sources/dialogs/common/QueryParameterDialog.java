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
package csi.client.gwt.edit_sources.dialogs.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.csiwizard.PanelDialog;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.support.ParameterPanelSupport;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.CsiListView;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.Instructions;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.MiniCyanButton;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Format;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class QueryParameterDialog extends WatchingParent {

    public enum Mode {
        CREATE, EDIT;
    }

    public interface SpecificUiBinder extends UiBinder<ValidatingDialog, QueryParameterDialog> {
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    ValidatingDialog dialog;
    @UiField
    ControlGroup nameControlGroup;
    @UiField
    FilteredTextBox nameField;
    @UiField
    TextBox promptField;
    @UiField
    TextArea descriptionField;
    @UiField
    CheckBox neverPromptField;
    @UiField
    CheckBox alwaysFillField;
    @UiField
    CheckBox alwaysPromptField;
    @UiField
    CheckBox listParameter;
    @UiField
    CheckBox trim;
    @UiField
    CsiStringListBox typeField;
    @UiField(provided = true)
    CsiListView<StringEntry> defaultValuesListView;
    @UiField
    MiniCyanButton editDefaultsButton;
    @UiField
    MiniRedButton clearDefaultsButton;
    @UiField
    ControlLabel nameLabel;
    @UiField
    ControlLabel descriptionLabel;
    @UiField
    ControlLabel promptLabel;
    @UiField
    ControlLabel typeLabel;
    @UiField
    ControlLabel defaultsLabel;
    @UiField
    Instructions instructionTextArea;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtDefaultsCreateTitle = _constants.parameterDefaults_CreateTitle();
    private static final String _txtDefaultsHelpTarget = _constants.parameterDefaults_HelpTarget();

    private static final String _txtCreateTitle = _constants.parameterEditor_CreateTitle();
    private static final String _txtUpdateTitle = _constants.parameterEditor_UpdateTitle();
    private static final String _txtHelpTarget = _constants.parameterEditor_HelpTarget();

    private QueryParameterDef queryParam;
    private ClickHandler clickHandler;
    private Map<String, QueryParameterDef> _nameMap;
    private ListStore<StringEntry> _defaultValuesDataStore;
    private SingleEntryWizardPanel activePanel = null;

    private CsiDataType _initialType = null;
    private CsiDataType _shadowType = null;
    List<String> _defaultShadowValues = null;

    WatchingParent _parent = null;
    WatchingParent _instance = this;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleCreateButtonClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (Strings.isNullOrEmpty(nameField.getValue())) {

                nameControlGroup.setType(ControlGroupType.ERROR);
                event.stopPropagation();

            } else {

                bindToObject(queryParam);

                if (!queryParam.getType().equals(_shadowType)) {

                    queryParam.getValues().clear();
                }

                if (!queryParam.getListParameter()) {

                    truncateList(queryParam.getValues());
                    truncateList(queryParam.getDefaultValues());
                }
                clickHandler.onClick(event);
            }
            if (null != _parent) {

                _parent.show();
            }
        }
    };

    private ClickHandler handleCancelButtonClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (null != _parent) {

                _parent.show();
            }
        }
    };

    private ClickHandler handleNeverPromptClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            alwaysFillField.setValue(false);
            alwaysPromptField.setValue(false);
        }
    };

    private ClickHandler handleAlwaysPromptClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            alwaysFillField.setValue(false);
            neverPromptField.setValue(false);
        }
    };

    private ClickHandler handleAlwaysFillClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            alwaysPromptField.setValue(false);
            neverPromptField.setValue(false);
        }
    };

    private ClickHandler handleSaveDefaultsButtonClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            List<String> myDefaults = null;

            try {

                _shadowType = CsiDataType.getValue(typeField.getSelectedValue());
                formatDefaultValues(activePanel.getList());

            } catch(Exception myException) {

                Display.error(myException.getMessage());
            }
            activePanel = null;
        }
    };

    private ClickHandler handleEditDefaultsButtonClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            QueryParameterDef myParameter = bindToObject(new QueryParameterDef());
            String myInfo = ParameterPanelSupport.getParameterInfo(myParameter);

            activePanel = ParameterPanelSupport.buildParameterPanel(_instance, myParameter, false, true);

            PanelDialog myDialog = new PanelDialog(activePanel, _txtDefaultsCreateTitle, _txtDefaultsHelpTarget, myInfo, handleSaveDefaultsButtonClick);

            myDialog.getActionButton().setText(Dialog.txtApplyButton);

            myDialog.show(60);
        }
    };

    private ClickHandler handleClearDefaultsButtonClick
            = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            clearDefaultValues();
        }
    };

    private SelectionChangedHandler<String> handleTypeChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            CsiDataType myType = CsiDataType.getValue(typeField.getSelectedValue());

            _defaultValuesDataStore.clear();
//            trim.setVisible(CsiDataType.String.equals(myType));

            if ((null != _defaultShadowValues) && (null != myType) && (myType.equals(_shadowType))) {

                displayValues();
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public QueryParameterDialog(Mode modeIn, QueryParameterDef queryParamIn, Map<String, QueryParameterDef> nameMapIn) {
        super();

        _nameMap = nameMapIn;
        queryParam = queryParamIn;

        defaultValuesListView = new CsiListView<StringEntry>();
        _defaultValuesDataStore = defaultValuesListView.getStore();

        uiBinder.createAndBindUi(this);
        
        dialog.hideOnCancel();

        editDefaultsButton.addClickHandler(handleEditDefaultsButtonClick);
        clearDefaultsButton.addClickHandler(handleClearDefaultsButtonClick);
        neverPromptField.addClickHandler(handleNeverPromptClick);
        alwaysPromptField.addClickHandler(handleAlwaysPromptClick);
        alwaysFillField.addClickHandler(handleAlwaysFillClick);

        //
        // Make description text area not resizeable
        //
        descriptionField.getElement().getStyle().setProperty("resize", "none"); //$NON-NLS-1$ //$NON-NLS-2$

        if (Mode.CREATE == modeIn) {

            dialog.defineHeader(_txtCreateTitle, _txtHelpTarget, true);
            dialog.getActionButton().setText(Dialog.txtCreateButton);
            nameField.setEnabled(true);
            
        } else {

            dialog.defineHeader(_txtUpdateTitle, _txtHelpTarget, true);
            dialog.getActionButton().setText(Dialog.txtSaveButton);
            nameField.setEnabled(false);
        }

        dialog.getActionButton().addClickHandler(handleCreateButtonClick);
        dialog.getCancelButton().addClickHandler(handleCancelButtonClick);

        for (CsiDataType csiDataType : CsiDataType.sortedValuesByLabel()) {
            typeField.addItem(csiDataType.getLabel());
        }
        if ((null != queryParam) && (null != queryParam.getType())) {

            typeField.setSelectedValue(queryParam.getType().getLabel());
        }
        typeField.addSelectionChangedHandler(handleTypeChange);
        trim.setValue(true);
//        trim.setVisible(CsiDataType.String.equals(CsiDataType.getValue(typeField.getSelectedValue())));
        bindToFields();
        establishMonitoring();
    }

    public QueryParameterDef getQueryParameter() {
        return queryParam;
    }

    public void setSaveClickHandler(ClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    public void show(WatchingParent parentIn) {

        _parent = parentIn;
        dialog.show(60);
        dialog.setFocus(nameField);
        if (null != _parent) {

            _parent.hide();
        }
    }

    public void show() {
        dialog.show(60);
        dialog.setFocus(nameField);
    }

    public void hide() {
        dialog.hide();
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private QueryParameterDef bindToObject(QueryParameterDef parameterIn) {
        parameterIn.setName(nameField.getValue());
        parameterIn.setDescription(descriptionField.getValue());
        parameterIn.setPrompt(promptField.getValue());
        //queryParam.getListParameter(); Is this field used?
        parameterIn.setNeverPrompt(neverPromptField.getValue());
        parameterIn.setAlwaysPrompt(alwaysPromptField.getValue());
        parameterIn.setAlwaysFill(alwaysFillField.getValue());
        parameterIn.setListParameter(listParameter.getValue());
        parameterIn.setType(CsiDataType.getValue(typeField.getSelectedValue()));
        parameterIn.setDefaultValues(getDefaultValues());

        if (!parameterIn.getType().equals(_initialType)) {

            parameterIn.getValues().clear();
        }
        if (parameterIn.getNeverPrompt()) {

            ArrayList<String> myList = getDefaultValues();

            if ((null != myList) && (0 < myList.size())) {

                parameterIn.setValues(myList);
            }
        }
        parameterIn.setTrimValues(trim.getValue());

        return parameterIn;
    }

    private void bindToFields() {
        _initialType =queryParam.getType();
        _shadowType = _initialType;
        nameField.setValue(queryParam.getName());
        descriptionField.setValue(queryParam.getDescription());
        promptField.setValue(queryParam.getPrompt());
        neverPromptField.setValue(queryParam.getNeverPrompt());
        alwaysPromptField.setValue(queryParam.getAlwaysPrompt());
        alwaysFillField.setValue(queryParam.getAlwaysFill());
        listParameter.setValue(queryParam.getListParameter());
        queryParam.setListParameter(true);
        if ((null != _shadowType) && (!CsiDataType.Unsupported.equals(_shadowType))) {
            typeField.setSelectedValue(queryParam.getType().getLabel());
        }
        formatDefaultValues(queryParam.getDefaultValues());
        trim.setValue(queryParam.getTrimValues());
    }
    
    private void establishMonitoring() {

        nameField.setRejectionMap(_nameMap);
        nameField.setMode(ValidityCheckCapable.Mode.LOWERCASE);
        nameField.setRequired(true);
        nameField.setColorChangingLabel(nameLabel);
        dialog.addObject(nameField, true);
    }

    private void formatDefaultValues(List<String> defaultValuesIn) {

        _defaultShadowValues = new ArrayList<String>();
        _defaultValuesDataStore.clear();
        defaultValuesListView.refresh();

        if (null != defaultValuesIn){

            _defaultShadowValues.addAll(defaultValuesIn);
            displayValues();
        }
        defaultValuesListView.refresh();
        if (0 < _defaultValuesDataStore.size()) {

            neverPromptField.setEnabled(true);

        } else {

            neverPromptField.setValue(false);
            neverPromptField.setEnabled(false);
        }
    }

    private void displayValues() {

        if (CsiDataType.String.equals(_shadowType)) {

            for (String myDefault : _defaultShadowValues) {

                _defaultValuesDataStore.add(new StringEntry(Format.value(myDefault), true));
            }

        } else {

            for (String myDefault : _defaultShadowValues) {

                _defaultValuesDataStore.add(new StringEntry(myDefault, true));
            }
        }
    }

    private void clearDefaultValues() {

        _defaultShadowValues = new ArrayList<String>();
        _defaultValuesDataStore.clear();
        defaultValuesListView.refresh();
        neverPromptField.setValue(false);
        neverPromptField.setEnabled(false);
    }

    private ArrayList<String> getDefaultValues() {

        ArrayList<String> myDefaults = new ArrayList<String>();

        for (StringEntry myDefault : _defaultValuesDataStore.getAll()) {

            myDefaults.add(Format.stripQuotes(myDefault.getValue()));
        }

        return myDefaults;
    }

    private void truncateList(List<String> listIn) {

        if ((null != listIn) && (1 < listIn.size())) {

            String myValue = listIn.get(0);

            listIn.clear();
            listIn.add(myValue);
        }
    }
}

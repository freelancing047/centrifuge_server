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
package csi.client.gwt.edit_sources.dialogs.column;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.csiwizard.widgets.DateTimeInputWidget;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.edit_sources.dialogs.common.QueryParameterDialog;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.MiniBlueButton;
import csi.client.gwt.widget.buttons.MiniCyanButton;
import csi.client.gwt.widget.buttons.MiniGreenButton;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.combo_boxes.ColumnDefComboBox;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.ui.TagInput;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.UUID;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.FilterOperatorType;
import csi.server.common.model.query.QueryParameterDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnFilterDialog extends WatchingParent {

    public enum ColumnFilterMode {
        CREATE, EDIT
    };

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    @UiField(provided = true)
	String excludeLabel = i18n.columnFilterDialogexcludeLabel(); //$NON-NLS-1$
    @UiField(provided = true)
	String operatorLabel = i18n.columnFilterDialogoperatorLabel(); //$NON-NLS-1$
    @UiField(provided = true)
	String operandLabel = i18n.columnFilterDialogoperandLabel(); //$NON-NLS-1$
    @UiField(provided = true)
	String addDateLabel = i18n.columnFilterDialogaddDateLabel(); //$NON-NLS-1$

    private static final String myDateFormatString = "yyyy-MM-dd"; //$NON-NLS-1$
    private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat(myDateFormatString);

    private Dialog dialog;

    private TagInput staticValues = new TagInput();
    private ColumnDefComboBox columnDefList = new ColumnDefComboBox();
    private CsiStringListBox inputParameters = new CsiStringListBox();
    private MiniGreenButton newQueryParameter;
    private MiniBlueButton editQueryParamter;
    private MiniRedButton deleteQueryParameter;

    ListStore<ColumnFilter> _gridStore;
    private ColumnFilter _columnFilter;
    private ClickHandler _actionClickHandler;
    private CsiDataType _colType;
    private SqlTableDef _table;
    private ColumnDef _column;
    private List<String> _localIdList = null;
    private ParameterPresenter _parentPresenter;
    private ParameterPresenter _parameterPresenter;
    private String _oldParameterId = null;
    private ColumnFilterMode _mode;
    
    private String _recentParameterId = null;

    @UiField
    CsiStringListBox operandField;
    @UiField
    CsiStringListBox operatorField;
    @UiField
    CheckBox excludeField;
    @UiField
    ControlLabel valueLabel;
    @UiField
    Controls valueControls;
    @UiField
    ControlGroup valueControlGroup;
    @UiField
    DateTimeInputWidget datePicker;
    @UiField
    MiniCyanButton addDateButton;

    interface SpecificUiBinder extends UiBinder<Dialog, ColumnFilterDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    
    public ColumnFilterDialog(ColumnFilterMode modeIn, ColumnFilter columnFilterIn, ListStore<ColumnFilter> gridStoreIn,
                                SqlTableDef tableIn, ColumnDef columnIn, ParameterPresenter parameterPresenterIn) {
        super();

        _mode = modeIn;
        _columnFilter = columnFilterIn;
        _gridStore = gridStoreIn;
        _parentPresenter = parameterPresenterIn;
        _parameterPresenter = (new ParameterPresenter(null, null, null)).replaceAll(_parentPresenter);
        _table = tableIn;
        _column = columnIn;

        _oldParameterId = (FilterOperandType.PARAMETER.equals(_columnFilter.getOperandType()))
                            ? _columnFilter.getParamLocalId() : null;

        dialog = uiBinder.createAndBindUi(this);
        dialog.setTitle((ColumnFilterMode.CREATE == _mode) ? i18n.columnFilterDialogCreateTitle() : i18n.columnFilterDialogEditTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.getActionButton().setText(modeIn == ColumnFilterMode.CREATE ? i18n.columnFilterDialogCreateButton() : i18n.columnFilterDialogSaveButton()); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.hideOnCancel();

        init();
    }

    public void setActionClickHandler(ClickHandler actionClickHandlerIn) {
        _actionClickHandler = actionClickHandlerIn;
    }

    @UiHandler("addDateButton")
    public void processAddDateButtonClick(ClickEvent event) {

        staticValues.addTag(datePicker.getText());
        datePicker.resetValue();
    }

    private ValidityReportEventHandler handleDateTimeValidityReport = new ValidityReportEventHandler() {
        @Override
        public void onValidityReport(ValidityReportEvent eventIn) {

            addDateButton.setEnabled(eventIn.getValidFlag());
        }
    };

    private void init() {

        _colType = _column.getCsiType();
        excludeField.setValue(_columnFilter.getExclude());
        List<FilterOperatorType> operators = FilterOperatorType.getApplicableOperators(_colType);
/*
        Collections.sort(operators, new Comparator<FilterOperatorType>() {

            @Override
            public int compare(FilterOperatorType o1, FilterOperatorType o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
*/
        for (FilterOperatorType foType : operators) {
            operatorField.addItem(foType.getLabel(), foType.name());
        }
        operatorField.setSelectedValue(_columnFilter.getOperator().name());
        setupAvailableOperands();
        if (operandField.getItemCount() > 0) {
            operandField.setSelectedValue(_columnFilter.getOperandType().name());
        }

        operatorField.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                setupAvailableOperands();
            }
        });

        initParameterButtons();
        datePicker.addValidityReportEventHandler(handleDateTimeValidityReport);
        setupValueField();
        operandField.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                setupValueField();
            }
        });

        columnDefList.addStyleName("bootstrapComboBox"); //$NON-NLS-1$

        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                validateAndSave(event);
            }
        });

        if (null != _actionClickHandler) {
            dialog.getCancelButton().addClickHandler(_actionClickHandler);
        }
    }

    protected void validateAndSave(ClickEvent event) {
 
        FilterOperandType operandType = operandField.getItemCount() > 0
                ? FilterOperandType.valueOf(operandField.getSelectedValue()) : null;

        if (operandType == FilterOperandType.STATIC && staticValues.getTags().size() == 0) {
            staticValues.addStyleName("tagError"); //$NON-NLS-1$
        } else if (operandType == FilterOperandType.PARAMETER && inputParameters.getItemCount() == 0) {
            valueControlGroup.setType(ControlGroupType.ERROR);
        } else if (operandType == FilterOperandType.COLUMN && columnDefList.getValue() == null) {
            valueControlGroup.setType(ControlGroupType.ERROR);
        } else {
            _columnFilter.setExclude(excludeField.getValue());
            _columnFilter.setOperator(FilterOperatorType.valueOf(operatorField.getSelectedValue()));
            _columnFilter.setOperandType(operandType);

            if (operandType == null) {
                _columnFilter.setStaticValues(new ArrayList<String>());
                _columnFilter.setLocalColumnId(null);
                _columnFilter.setParamLocalId(null);
            } else {
                switch (operandType) {
                    case STATIC: {
                        _columnFilter.setStaticValues(new ArrayList<String>(staticValues.getTags()));
                        _columnFilter.setLocalColumnId(null);
                        _columnFilter.setParamLocalId(null);
                        break;
                    }
                    case COLUMN: {
                        _columnFilter.setStaticValues(new ArrayList<String>());
                        _columnFilter.setLocalColumnId(columnDefList.getValue().getLocalId());
                        _columnFilter.setParamLocalId(null);
                        break;
                    }
                    case PARAMETER: {
                        _columnFilter.setStaticValues(new ArrayList<String>());
                        _columnFilter.setLocalColumnId(null);
                        _columnFilter.setParamLocalId(inputParameters.getSelectedValue());
                        break;
                    }
                } // end switch (operand) ...
            }
            finalizeParameters();
            if (ColumnFilterMode.CREATE == _mode) {
                
                _gridStore.add(_columnFilter);
                
            } else {
                
                _gridStore.update(_columnFilter);
            }
            if (_actionClickHandler != null) {
                _actionClickHandler.onClick(event);
            }
            dialog.hide();
        } // end else
    }
    
    private void finalizeParameters() {
        
        String newParameterId = (FilterOperandType.PARAMETER.equals(_columnFilter.getOperandType())) ? _columnFilter.getParamLocalId() : null;
        
        if (_oldParameterId != newParameterId) {
            
            if (null != _oldParameterId) {
                
                _parameterPresenter.removeSourceItem(_oldParameterId);
            }
            
            if (null != newParameterId) {
                
                _parameterPresenter.addSourceItem(newParameterId);
            }
        }
        _parentPresenter.replaceAll(_parameterPresenter);
    }

    private void initParameterButtons() {
        newQueryParameter = new MiniGreenButton(i18n.columnFilterDialogNewButton()); //$NON-NLS-1$
        newQueryParameter.getElement().getStyle().setMarginLeft(10, Unit.PX);
        newQueryParameter.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                handleNewQueryParameterClick();
            }
        });

        editQueryParamter = new MiniBlueButton(i18n.columnFilterDialogEditButton()); //$NON-NLS-1$
        editQueryParamter.getElement().getStyle().setMarginLeft(5, Unit.PX);
        editQueryParamter.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                handleEditQueryParameterClick();
            }

        });

        deleteQueryParameter = new MiniRedButton(i18n.columnFilterDialogDeleteButton()); //$NON-NLS-1$
        deleteQueryParameter.getElement().getStyle().setMarginLeft(5, Unit.PX);
        deleteQueryParameter.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                handleDeleteQueryParameterClick();
            }
        });
    }

    private void handleNewQueryParameterClick() {
        QueryParameterDef def = new QueryParameterDef();
        def.setLocalId(UUID.randomUUID());
        def.setType(_colType);
        _recentParameterId = def.getLocalId();

        final QueryParameterDialog qpDialog = new QueryParameterDialog(QueryParameterDialog.Mode.CREATE, def, _parameterPresenter.getCurrentNames());
        qpDialog.setSaveClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                QueryParameterDef qpd = qpDialog.getQueryParameter();
                _parameterPresenter.addParameter(qpd);
                setupValueField();
                qpDialog.hide();
            }
        });
        qpDialog.show(this);
    }

    private void handleEditQueryParameterClick() {
        QueryParameterDef param = null;
        for (QueryParameterDef qpd : _parameterPresenter.getParameters()) {
            if (qpd.getLocalId().equals(inputParameters.getValue().getValue())) {
                param = qpd;
                _recentParameterId = param.getLocalId();
                break;
            }
        }
        if (null == param) {

            param = new QueryParameterDef();
            param.setLocalId(UUID.randomUUID());
            param.setType(_colType);
        }
        final QueryParameterDialog dialog = new QueryParameterDialog(QueryParameterDialog.Mode.EDIT, param, _parameterPresenter.getCurrentNames());
        dialog.setSaveClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setupValueField();
                dialog.hide();
            }
        });
        dialog.show(this);
    }

    private void handleDeleteQueryParameterClick() {
        String paramName = inputParameters.getItemText(inputParameters.getSelectedIndex());
        WarningDialog dialog = new WarningDialog(i18n.columnFilterDialogDeleteDialogTitle() + paramName, i18n.columnFilterDialogDeleteMessageConfirmation(paramName)); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.show();
        dialog.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Iterator<QueryParameterDef> iterator = _parameterPresenter.getParameters().iterator();
                QueryParameterDef myParameter = null;
                while (iterator.hasNext()) {
                    QueryParameterDef myTest = iterator.next();
                    if (myTest.getLocalId().equals(inputParameters.getValue().getValue())) {
                        myParameter = myTest;
                        break;
                    }
                }
                if (null != myParameter) {

                    _parameterPresenter.removeParameter(myParameter);
                    ColumnFilterDialog.this.setupValueField();
                    inputParameters.setSelectedIndex(-1);
                }
            }
        });
    }

    private void setupAvailableOperands() {
        FilterOperatorType selectedOperator = FilterOperatorType.valueOf(operatorField.getSelectedValue());
        List<FilterOperandType> myOperands = selectedOperator.getApplicableOperands();
        operandField.clear();
        if (myOperands.size() == 0) {
            operandField.setEnabled(false);
        } else {
            for (FilterOperandType operandType : myOperands) {
                operandField.addItem(operandType.getLabel(), operandType.name());
            }
            operandField.setEnabled(true);
        }
        setupValueField();
    }

    private void setupValueField() {
        valueControls.clear();
        valueControlGroup.setType(ControlGroupType.NONE);
        hideDateControls();
        if (operandField.getItemCount() > 0) {
            _setupValueField();
        } else {
            valueLabel.getElement().setInnerHTML(""); //$NON-NLS-1$
        }
    }

    private void hideDateControls() {
        datePicker.setVisible(false);
        addDateButton.setVisible(false);
        datePicker.suspendMonitoring();
    }

    private void showDateControls() {
        datePicker.setVisible(true);
        addDateButton.setVisible(true);
        datePicker.beginMonitoring();
    }

    private void _setupValueField() {
        FilterOperandType foType = FilterOperandType.valueOf(operandField.getSelectedValue());
        switch (foType) {
            case STATIC: {
                _setupStaticValueField();
                break;
            }
            case COLUMN: {
                _setUpColumnValueField();
                break;
            }
            case PARAMETER: {
                _setupParamterDataField();
                break;
            }
        }
    }

    private void _setupParamterDataField() {
        valueLabel.getElement().setInnerHTML(i18n.columnFilterDialogParameterLabel()); //$NON-NLS-1$
        inputParameters.clear();
        // This list is purely to get around the limitation of ListBox in defining the selected value by
        // index only.
        _localIdList = new ArrayList<String>();
        for (QueryParameterDef qpd : _parameterPresenter.getParameters()) {
            if ((null != _colType) && (_colType.equals(qpd.getType()))) {

                inputParameters.addItem(qpd.getName(), qpd.getLocalId());
                _localIdList.add(qpd.getLocalId());
            }
        }
        valueControls.add(inputParameters);

        valueControls.add(newQueryParameter);
        valueControls.add(editQueryParamter);
        valueControls.add(deleteQueryParameter);
        
        int index = (null != _recentParameterId)
                        ? _localIdList.indexOf(_recentParameterId)
                        : (!Strings.isNullOrEmpty(_columnFilter.getParamLocalId()))
                            ? _localIdList.indexOf(_columnFilter.getParamLocalId())
                            : -1;
        
        if (0 <= index) {
            inputParameters.setSelectedIndex(index);
        }
        _recentParameterId = null;
        
        inputParameters.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                adjustButtons();
            }
        });
        
        adjustButtons();
    }

    private void adjustButtons() {
        
        int myIndex = inputParameters.getSelectedIndex();
        boolean myEnableFlag = false;

        if ((null != _localIdList) && (null != _columnFilter) && (0 <= myIndex)){
            
            myEnableFlag = _parameterPresenter.parameterHasNoUser(_localIdList.get(myIndex));
        }

        deleteQueryParameter.setEnabled(myEnableFlag);
        editQueryParamter.setEnabled(0 <= myIndex);
    }
    
    private void _setUpColumnValueField() {
        valueLabel.getElement().setInnerHTML(i18n.columnFilterDialogColumnLabel()); //$NON-NLS-1$
        // Filter out this column from the list.
        columnDefList.getStore().clear();
        Map<String, ColumnDef> columnDefsByLocalId = Maps.newHashMap();
        for (ColumnDef cd : _table.getColumns()) {
            if (!cd.equals(_column)) {
                columnDefList.getStore().add(cd);
                columnDefsByLocalId.put(cd.getLocalId(), cd);
            }
        }
        valueControls.add(columnDefList);
        if (!Strings.isNullOrEmpty(_columnFilter.getLocalColumnId())) {
            columnDefList.setValue(columnDefsByLocalId.get(_columnFilter.getLocalColumnId()));
        }
    }

    private void _setupStaticValueField() {
        ScrollPanel myPanel = new ScrollPanel();

        switch (_colType) {

            case DateTime:

                datePicker.configureDateTimeMode("Enter date/time");
                showDateControls();
                break;

            case Date:

                datePicker.configureDateMode("Enter date");
                showDateControls();
                break;

            case Time:

                datePicker.configureTimeMode("Enter time");
                showDateControls();
                break;

            default :

                break;
        }
        valueLabel.getElement().setInnerHTML(i18n.columnFilterDialogStaticLabel()); //$NON-NLS-1$
        myPanel.setHeight("50px"); //$NON-NLS-1$
        myPanel.add(staticValues);
        valueControls.add(myPanel);
        staticValues.setTags(_columnFilter.getStaticValues());
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

}

package csi.client.gwt.dataview.fieldlist.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.BooleanInputWidget;
import csi.client.gwt.csiwizard.widgets.DateInputWidget;
import csi.client.gwt.csiwizard.widgets.DateTimeInputWidget;
import csi.client.gwt.csiwizard.widgets.IntegerInputWidget;
import csi.client.gwt.csiwizard.widgets.TextInputWidget;
import csi.client.gwt.csiwizard.widgets.TimeInputWidget;
import csi.client.gwt.csiwizard.widgets.ValueInputWidget;
import csi.client.gwt.dataview.fieldlist.editor.derived.DerivedFieldDialog;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorView;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.misc.ScrollingString;
import csi.config.Configuration;
import csi.server.common.dto.CompilationResult;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.SqlTokenValueCallback;
import csi.server.common.interfaces.TokenExecutionValue;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.SqlTokenTreeItem;
import csi.server.common.model.SqlTokenTreeItemList;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.worksheet.WorksheetDef;

/**
 * @author Centrifuge Systems, Inc.
 * Widget that shows the form for editing a field.
 */
public class FieldEditorView extends Composite {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    public static final String SCRIPTED = i18n.fieldList_FieldTypeText_Scripted();
    public static final String DERIVED = i18n.fieldList_FieldTypeText_Derived();
    public static final String STATIC = i18n.fieldList_FieldTypeText_Static();

    private static final CsiDataType DEFAULT_DATA_TYPE = CsiDataType.String;
    private static final FieldType DEFAULT_FIELD_TYPE = FieldType.STATIC;
    private final int MAXIMUM_STRING_LENGTH = 70;

    private static final Map<CsiDataType, String> _infoMap;

    static {
        _infoMap = new HashMap<CsiDataType, String>();
        _infoMap.put(CsiDataType.String,  i18n.parameterInfo_String());
        _infoMap.put(CsiDataType.Boolean,  i18n.parameterInfo_Boolean());
        _infoMap.put(CsiDataType.Integer,  i18n.parameterInfo_Integer());
        _infoMap.put(CsiDataType.Number,  i18n.parameterInfo_Number());
        _infoMap.put(CsiDataType.DateTime,  i18n.parameterInfo_DateTime());
        _infoMap.put(CsiDataType.Date,  i18n.parameterInfo_Date());
        _infoMap.put(CsiDataType.Time,  i18n.parameterInfo_Time());
    }

    private static final String _txtPreCacheButton = i18n.fieldList_FieldEditor_PreCacheButton();
    private static final String _txtReCalculateButton = i18n.fieldList_FieldEditor_ReCalculateButton();
    private static final String _txtCacheChangeButton = "cache change";
    private static final String _txtRuntimeCastButton = "runtime cast";
    private static final String _txtDerivedFieldInfoWithPrecache = i18n.fieldList_FieldEditor_DerivedInfoWithPrecache(_txtPreCacheButton, _txtReCalculateButton);
    private static final String _txtDerivedFieldInfo = i18n.fieldList_FieldEditor_DerivedInfo();
    private static final String _txtExpressionPrompt = i18n.fieldList_FieldEditor_ExpressionPrompt();
    private static final String _txtMissingToken = i18n.sqlTokenTreeLabel_MissingValue();

    private static Map<String, FieldType> fieldTypeMap = new HashMap<String, FieldType>();

    static {

        fieldTypeMap.put(STATIC, FieldType.STATIC);
        fieldTypeMap.put(DERIVED, FieldType.DERIVED);
        fieldTypeMap.put(SCRIPTED, FieldType.SCRIPTED);
    }

    private final VerticalPanel topContainer = new VerticalPanel();
    private final HorizontalPanel basicContainer = new HorizontalPanel();
    private final VerticalPanel scriptedContainer = new VerticalPanel();
    private final VerticalPanel dialogContainer = new VerticalPanel();
    private final VerticalPanel infoContainer = new VerticalPanel();
    private DataViewDef _meta;
    private FieldListAccess _dataModel;
    private FieldModel _model;
    private final ScriptedFunctionsEditorView scriptedEditorView;

    private ControlLabel nameLabel;
    private FieldEditorViewNameTextBox nameTextBox = new FieldEditorViewNameTextBox();
    private CsiStringListBox dataTypeDropdown = new CsiStringListBox();
    private CsiStringListBox fieldTypeDropdown = new CsiStringListBox();

//    private TextBox staticValueTextBox = new TextBox();
    private AbstractInputWidget staticValueInput = null;
    private TextArea infoDisplay = new TextArea();
    private CsiStringListBox dynamicFunctionsDropdown = new CsiStringListBox();

    private Column staticValueColumn = new Column(3);

    private ScrollingString sqlExpression;
    private Row derivedValueRow;
    private Row staticValueRow;
    private Row scriptedValueRow;
    private Row emptyRow;
    private FluidRow inUseViz;
    private HorizontalPanel derivedRadioButtons;
    private RadioButton cacheRadioButton;
    private RadioButton calculateRadioButton;
    private HorizontalPanel castingRadioButtons;
    private RadioButton cacheChangeRadioButton;
    private RadioButton runtimeCastRadioButton;
    private UsedVisualizationDialog usedVisualizationDialog;
    private MainPresenter _mainPresenter = null;

    private MultiValidator _validator = null;
    private FieldType _mode = FieldType.STATIC;
    private CsiDataType _type = CsiDataType.String;
    private Map<String, FieldDef> _map;
    private SqlTokenTreeItemList _sqlShadowTree = null;
    private CsiDataType _derivedShadowType = null;
    private String _staticShadowValue = null;
    private CsiDataType _staticShadowType = null;
    private boolean _preCacheDerived = false;
    private boolean _isNew = false;
    private List<String> _inUseVizList;

    private DerivedFieldDialog _derivedFieldDialog;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ValueChangeHandler<String> handleNameChange = new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> eventIn) {

            scriptedEditorView.handleNameChange(eventIn.getValue());
        }
    };

    private SelectionChangedHandler<String> handleDataTypeChange
            = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            if (FieldType.STATIC.equals(_mode)) {

                try {

                    recordStaticInfo(_type, staticValueInput.getText());

                } catch(Exception myException) {

                    // ignore
                }
            }
            _type = CsiDataType.getValue(dataTypeDropdown.getSelectedValue());

            if (FieldType.COLUMN_REF.equals(_mode) || FieldType.LINKUP_REF.equals(_mode)) {

                castingRadioButtons.setVisible(isTypeChange(_type));
            }
            handleStaticValueRow();
            displayExpression();
        }
    };

    private boolean isTypeChange(CsiDataType typeIn) {

        return (null != typeIn) && ((!typeIn.equals(_model.getStorageType())) || (!typeIn.equals(_model.getDataType())));
    }

    private SelectionChangedHandler<String> handleFieldTypeChange = new SelectionChangedHandler<String>() {

        @Override
        public void  onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            FieldType myFieldType = fieldTypeMap.get(fieldTypeDropdown.getSelectedValue());
            CsiDataType myDataType = CsiDataType.getValue(dataTypeDropdown.getSelectedValue());

            if (FieldType.STATIC.equals(_mode)) {

                try {

                    recordStaticInfo(_type, staticValueInput.getText());

                } catch(Exception myException) {

                    // ignore
                }
            }

            setRowVisibilities(myFieldType, myDataType);
            displayExpression();
        }
    };

    private ClickHandler handleDerivedFieldApply = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            // Extract values from dialog
            recordDerivedInfo(_derivedFieldDialog.generateSqlExpression());
            displayExpression();

            // Destroy dialog
            _derivedFieldDialog = _derivedFieldDialog.destroy();
        }
    };

    private ClickHandler handleDerivedFieldCancel = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            FieldType myFieldType = fieldTypeMap.get(fieldTypeDropdown.getValue());
            CsiDataType myDataType = CsiDataType.getValue(dataTypeDropdown.getSelectedValue());

            displayExpression();

            // Destroy dialog
            _derivedFieldDialog = _derivedFieldDialog.destroy();
        }
    };

    private ClickHandler handleExpressionClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            CsiDataType myType = CsiDataType.getValue(dataTypeDropdown.getSelectedValue());
            SqlTokenTreeItemList myExpression = getExpression();

            try {

                _derivedFieldDialog = new DerivedFieldDialog(_meta, myType, myExpression,
                handleDerivedFieldApply,
                        handleDerivedFieldCancel);

                _derivedFieldDialog.show();

            } catch (Exception myException) {

                Display.error("FieldEditorView", 1, myException);
            }
        }
    };

    private ClickHandler handleCacheRadioButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            _preCacheDerived = true;
        }
    };

    private ClickHandler handleCalculateRadioButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            _preCacheDerived = false;
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SqlTokenValueCallback _slqTokenValueCallback = new SqlTokenValueCallback() {
        @Override
        public String getFieldDisplayValue(String valueIn) {

            FieldDef myField = _dataModel.getFieldDefByLocalId(valueIn);

            return (null != myField) ? myField.getFieldName() : null;
        }

        @Override
        public TokenExecutionValue getFieldExecutionValue(String valueIn) {

            return null;
        }

        @Override
        public String getParameterDisplayValue(String valueIn) {

            QueryParameterDef myParameter = _meta.getParameterById(valueIn);

            return (null != myParameter) ? myParameter.getName() : null;
        }

        @Override
        public TokenExecutionValue getParameterExecutionValue(String valueIn) {

            return null;
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldEditorView(FieldModel modelIn, DataViewDef metaIn,
                           ScriptedFunctionsEditorView scriptedEditorView, boolean inuseIn, boolean isNewIn, List<String> inUseVizList) {
        _meta = metaIn;
        _model = modelIn;
        _dataModel = _meta.getModelDef().getFieldListAccess();
        this._map = _dataModel.getFieldMapByName();
        this.scriptedEditorView = scriptedEditorView;
        _isNew = isNewIn;
        _inUseVizList = inUseVizList;

        infoDisplay.setReadOnly(true);
        infoDisplay.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        infoDisplay.getElement().getStyle().setProperty("resize", "none"); //$NON-NLS-1$ //$NON-NLS-2$
        infoDisplay.getElement().getStyle().setBackgroundColor("white"); //$NON-NLS-1$
        infoDisplay.getElement().getStyle().setBorderColor("white"); //$NON-NLS-1$
        infoDisplay.getElement().getStyle().setColor(Dialog.txtInfoColor);
        infoDisplay.setWidth("270px"); //$NON-NLS-1$
        infoDisplay.setHeight("150px"); //$NON-NLS-1$

        topContainer.setWidth("615px"); //$NON-NLS-1$
        basicContainer.setWidth("615px"); //$NON-NLS-1$
        scriptedContainer.setWidth("615px"); //$NON-NLS-1$
        dialogContainer.setWidth("325px"); //$NON-NLS-1$
        infoContainer.setWidth("290px"); //$NON-NLS-1$
        infoContainer.add(infoDisplay);

        basicContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        basicContainer.add(dialogContainer);
        basicContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        basicContainer.add(infoContainer);

        topContainer.add(basicContainer);
        topContainer.add(scriptedContainer);

        buildUI();

        initWidget(topContainer);
        setDropdownOptions();
        setUIFromModel();
        nameTextBox.setRejectionMap(_map);
        nameTextBox.setRequired(true);
        setRowVisibilities(_model.getFieldType(), _model.getDataType());

        _mainPresenter = getMainPresenter();
        boolean isAdmin = _mainPresenter.isFieldListAdmin();

        if (inuseIn && !isAdmin) {

            dataTypeDropdown.setEnabled(false);

        } else {

            dataTypeDropdown.addSelectionChangedHandler(handleDataTypeChange);
            dataTypeDropdown.setEnabled(true);
        }
    }

    public FieldModel storeViewIntoModel() {

        try {
            _model.setName(nameTextBox.getText().trim());
            _model.setDataType(CsiDataType.getValue(dataTypeDropdown.getSelectedValue()));
            _model.setFieldType(_mode);
            _model.setPreCalculated(_preCacheDerived);

            switch (_mode) {

                case  STATIC:

                    _model.setScriptedFunctionsModel(null);
                    _model.setSqlExpression(null);

                    extractStaticValue();
                    break;

                case DERIVED:

                    _model.setStaticValue(null);
                    _model.setScriptedFunctionsModel(null);
                    _model.setPreCalculated(_preCacheDerived);

                    _model.setSqlExpression(getExpression());
                    break;

                case SCRIPTED:

                    _model.setStaticValue(null);
                    _model.setSqlExpression(null);

                    ScriptedFunctionsEditorModel dynamicModel = scriptedEditorView.getScriptedValues();
                    _model.setScriptedFunctionsModel(dynamicModel);
                    break;

                default:

                    _model.setStaticValue(null);
                    _model.setSqlExpression(null);
                    _model.setScriptedFunctionsModel(null);
                    _model.setPreCalculated(cacheChangeRadioButton.getValue());
                    break;
            }

        } catch(Exception myException) {

            Display.error(myException.getMessage());
        }

        return _model;
    }

    public void addScriptedFunctionsDropdownChangeHandler(SelectionChangedHandler handlerIn) {
        dynamicFunctionsDropdown.addSelectionChangedHandler(handlerIn);
    }

    public String getScriptedFunctionsDropdownValue() {
        return dynamicFunctionsDropdown.getSelectedValue();
    }

    public void displaySuccessOrFailure(CompilationResult result) {
        scriptedEditorView.displaySuccessOrFailure(result);
    }

    public void addValidator(MultiValidator validatorIn) {

        _validator = validatorIn;
    }

    public boolean checkValidity() {
        
        boolean myValidFlag = true;

        switch (_mode) {

            case STATIC:

                if ((null == staticValueInput) || (!staticValueInput.isValid())) {

                    myValidFlag = false;
                }
                break;

            case DERIVED:

                if (null == getExpression()) {

                    myValidFlag = false;
                }
                break;

            case SCRIPTED:

                if (null != scriptedEditorView) {

                    myValidFlag = scriptedEditorView.isValid();
                }
                break;

            default:

                break;
        }

        if (!nameTextBox.isValid()) {

            myValidFlag = false;
        }
        return myValidFlag;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void setUIFromModel() {
        CsiDataType myDataType = (null != _model.getDataType()) ? _model.getDataType() : DEFAULT_DATA_TYPE;
        nameTextBox.setInitialValue(_model.getName());
        dataTypeDropdown.setSelectedValue(myDataType.getLabel());

        if (_model.isPreCalculated()) {

            cacheRadioButton.setValue(true, true);
            cacheChangeRadioButton.setValue(true, true);

        } else {

            calculateRadioButton.setValue(true, true);
            runtimeCastRadioButton.setValue(true, true);
        }

        if(_model.getFieldType() == FieldType.STATIC){
            _staticShadowValue = _model.getStaticValue();
            fieldTypeDropdown.setSelectedValue(STATIC);
            recordStaticInfo(_type, _model.getStaticValue());
        }
        else if(_model.getFieldType() == FieldType.DERIVED){
            fieldTypeDropdown.setSelectedValue(DERIVED);
            recordDerivedInfo(_model.getSqlExpression());
        }
        else if(_model.getFieldType() == FieldType.SCRIPTED){
            fieldTypeDropdown.setSelectedValue(SCRIPTED);
            dynamicFunctionsDropdown.setSelectedValue(_model.getScriptedFunctionsModel().getFunctionType().getTitle());
//            dynamicFunctionsDropdown.setSelectedValue(ScriptedFunctions.ADVANCED_FUNCTION.getTitle());
            scriptedEditorView.setUIFromModel(_model.getScriptedFunctionsModel());
        }
    }

    private void buildUI() {

        nameLabel = new ControlLabel(i18n.fieldList_FieldEditor_Name());
        nameTextBox.setColorChangingLabel(nameLabel);
        nameTextBox.addValueChangeHandler(handleNameChange);

        addDialogRow(nameLabel, nameTextBox);

        addDialogRow(i18n.fieldList_GridTitle_DataType(), dataTypeDropdown);
        addDialogRow(i18n.fieldList_GridTitle_FieldType(), createFieldTypeWidget());
        addDialogRow(i18n.fieldList_GridTitle_UsedVisualizations(), createUsedVizWidget());
        setFieldSourceLabels();

        emptyRow = createRow(new ControlLabel(""), new InlineLabel(""), 10); //$NON-NLS-1$ //$NON-NLS-2$
        addDialogRow(emptyRow);

        sqlExpression = new ScrollingString(handleExpressionClick);
        sqlExpression.setWidth("225px"); //$NON-NLS-1$
        addCastingRadioButtons();
        derivedValueRow = createRow(new ControlLabel(i18n.fieldList_FieldEditor_EditDerivedFieldPrompt()),
                                    sqlExpression.asWidget(), 40);
        addDialogRow(derivedValueRow);
        addDerivedFieldRadioButtons();

        staticValueRow = createProtoRow(i18n.fieldList_FieldEditor_Value() + " *", staticValueColumn); //$NON-NLS-1$
        addDialogRow(staticValueRow);

        scriptedValueRow = createRow(i18n.fieldList_FieldEditor_SelectFunction(), dynamicFunctionsDropdown);
        addScriptedRow(scriptedValueRow);
        addScriptedRow(scriptedEditorView);
    }

    private void setRowVisibilities(FieldType fieldTypeIn, CsiDataType dataTypeIn) {

        _mode = fieldTypeIn;
        _type = dataTypeIn;

        switch (_mode) {
            
            case STATIC:

                infoDisplay.setText(_infoMap.get(dataTypeIn));
                emptyRow.setVisible(false);
                derivedValueRow.setVisible(false);
                derivedRadioButtons.setVisible(false);
                castingRadioButtons.setVisible(false);
                staticValueRow.setVisible(true);
                scriptedValueRow.setVisible(false);
                scriptedEditorView.setVisible(false);
                break;
            
            case DERIVED:

                infoDisplay.setText(isPrecacheEnabled() ? _txtDerivedFieldInfoWithPrecache : _txtDerivedFieldInfo);
                emptyRow.setVisible(true);
                derivedValueRow.setVisible(true);
                derivedRadioButtons.setVisible(true);
                castingRadioButtons.setVisible(false);
                staticValueRow.setVisible(false);
                scriptedValueRow.setVisible(false);
                scriptedEditorView.setVisible(false);
                displayExpression();
                break;

            case SCRIPTED:

                emptyRow.setVisible(false);
                derivedValueRow.setVisible(false);
                derivedRadioButtons.setVisible(false);
                castingRadioButtons.setVisible(false);
                staticValueRow.setVisible(false);
                scriptedValueRow.setVisible(true);
                scriptedEditorView.setVisible(true);
                break;
            
            default:

                emptyRow.setVisible(false);
                derivedValueRow.setVisible(false);
                derivedRadioButtons.setVisible(false);
                castingRadioButtons.setVisible(true);
                staticValueRow.setVisible(false);
                scriptedValueRow.setVisible(false);
                scriptedEditorView.setVisible(false);
                break;
        }
        handleStaticValueRow();
    }

    private void setFieldSourceLabels() {
        for (String key : _model.getClientProperties().keySet()) {
            String displayKey = convertFromCamelCase(key);

            addDialogRow(displayKey, new ControlLabel(_model.getClientProperties().get(key)));
        }
    }

    private static String convertFromCamelCase(String key) {
        int index = 0;

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < key.length(); i++){
            if(Character.isUpperCase(key.charAt(i))){
                sb.append(" "); //$NON-NLS-1$
                sb.append(String.valueOf(key.charAt(index)).toUpperCase());
                sb.append(key.substring(index+1, i));
                index = i;
            }
        }
        sb.append(" "); //$NON-NLS-1$
        sb.append(String.valueOf(key.charAt(index)).toUpperCase());
        sb.append(key.substring(index+1));
        return sb.toString().trim();
    }

    private Widget createFieldTypeWidget() {
//        if (_model.getFieldType() == FieldType.COLUMN_REF) {
          if (!_isNew) {
            return new ControlLabel(_model.getFieldType().getLabel());
        }
        return fieldTypeDropdown;
    }

    private Widget createUsedVizWidget() {
        inUseViz = new FluidRow();
        inUseViz.clear();

        String inUseText = "";

        Multimap<String, String> worksheetVizWithCurrentFilter = ArrayListMultimap.create();
        List<WorksheetDef> worksheets = WebMain.injector.getMainPresenter().getDataViewPresenter(false).getDataView().getMeta().getModelDef().getWorksheets();


        for(WorksheetDef worksheet : worksheets) {
            for (VisualizationDef worksheetVisualization : worksheet.getVisualizations()) {
                if (_inUseVizList.contains(worksheetVisualization.getName())) {
                    worksheetVizWithCurrentFilter.put(worksheet.getWorksheetName(), worksheetVisualization.getName());
                }
            }
        }

        String mapText = worksheetVizWithCurrentFilter.toString();
        if (!mapText.isEmpty()) {
            mapText = mapText.replaceAll("\\{", "");
            mapText = mapText.replaceAll("}", "");
        }


        String fullText = mapText;
        usedVisualizationDialog = new UsedVisualizationDialog(i18n.fieldList_fieldEditor_inUseViz_dialogTitle(), fullText);

        if (mapText.length() > MAXIMUM_STRING_LENGTH) {
            inUseText = fullText.substring(0, MAXIMUM_STRING_LENGTH-1) + "...";
        } else if (mapText.length() == 0) {
            inUseText = i18n.filterDisplayWidget_none();
        } else {
            inUseText = fullText;
        }


        InlineLabel inUseVizListLabel = new InlineLabel();
        inUseVizListLabel.getElement().getStyle().setPosition(Style.Position.RELATIVE);
        inUseVizListLabel.getElement().getStyle().setTop(5, Style.Unit.PX);
        inUseVizListLabel.setText(inUseText);

        Button inUseVizListMore = new Button();
        inUseVizListMore.setIcon(IconType.LIST_OL);
        inUseVizListMore.setSize(ButtonSize.SMALL);
        inUseVizListMore.getElement().getStyle().setFloat(Style.Float.RIGHT);
        inUseVizListMore.getElement().getStyle().setLineHeight(16, Style.Unit.PX);
        inUseVizListMore.getElement().getStyle().setMarginTop(-30, Style.Unit.PX);

        inUseVizListMore.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                usedVisualizationDialog.show();
            }
        });

        inUseViz.add(inUseVizListLabel);
        inUseViz.add(inUseVizListMore);

        return inUseViz;
    }

    private void addDialogRow(String label, Widget control) {
        addDialogRow(createRow(label, control));
    }

    private void addDialogRow(ControlLabel labelIn, Widget controlIn) {
        addDialogRow(createRow(labelIn, controlIn));
    }

    private void addDialogRow(Row row) {
        dialogContainer.add(row);
    }

    private void addDerivedFieldRadioButtons() {

        derivedRadioButtons = new HorizontalPanel();
        cacheRadioButton = new RadioButton("DerivedRadioSet", _txtPreCacheButton); //$NON-NLS-1$ //$NON-NLS-2$
        calculateRadioButton = new RadioButton("DerivedRadioSet", _txtReCalculateButton); //$NON-NLS-1$ //$NON-NLS-2$
        cacheRadioButton.getElement().getStyle().setPadding(20, Style.Unit.PX);
        calculateRadioButton.getElement().getStyle().setPadding(20, Style.Unit.PX);
        derivedRadioButtons.add(cacheRadioButton);
        derivedRadioButtons.add(calculateRadioButton);
        if (isPrecacheEnabled()) {
            addDialogRow("", derivedRadioButtons); //$NON-NLS-1$

            cacheRadioButton.addClickHandler(handleCacheRadioButtonClick);
            calculateRadioButton.addClickHandler(handleCalculateRadioButtonClick);
        } else {

            derivedRadioButtons.setVisible(false);
        }
    }

    private void addCastingRadioButtons() {

        castingRadioButtons = new HorizontalPanel();
        cacheChangeRadioButton = new RadioButton("CastingRadioSet", _txtCacheChangeButton); //$NON-NLS-1$ //$NON-NLS-2$
        runtimeCastRadioButton = new RadioButton("CastingRadioSet", _txtRuntimeCastButton); //$NON-NLS-1$ //$NON-NLS-2$
        cacheChangeRadioButton.getElement().getStyle().setPaddingRight(20, Style.Unit.PX);
        runtimeCastRadioButton.getElement().getStyle().setPaddingLeft(20, Style.Unit.PX);
        castingRadioButtons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        castingRadioButtons.add(cacheChangeRadioButton);
        castingRadioButtons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        castingRadioButtons.add(runtimeCastRadioButton);
        if (isPrecacheEnabled()) {
            addDialogRow("", castingRadioButtons); //$NON-NLS-1$
        } else {

            castingRadioButtons.setVisible(false);
        }
    }

    private void addScriptedRow(Row row) {
        scriptedContainer.add(row);
    }

    private Row createProtoRow(String labelIn, Column columnIn) {

        return createProtoRow(new ControlLabel(labelIn), columnIn);
    }

    private Row createProtoRow(ControlLabel labelIn, Column valueColumnIn) {

        Row myRow = new Row();
        Column myLabelColumn = new Column(1);

        myLabelColumn.add(labelIn);

        myRow.add(myLabelColumn);
        myRow.add(valueColumnIn);

        return myRow;
    }

    private Row createRow(String labelIn, Widget controlIn) {

        return createRow(new ControlLabel(labelIn), controlIn);
    }

    private Row createRow(ControlLabel labelIn, Widget controlIn) {

        return createRow(labelIn, controlIn, 30);
    }

    private Row createRow(ControlLabel labelIn, Widget controlIn, int heightIn) {

        Column myColumn = new Column(3);

        myColumn.add(controlIn);
        myColumn.getElement().getStyle().setHeight(heightIn, Style.Unit.PX);

        return createProtoRow(labelIn, myColumn);
    }

    private void setDropdownOptions() {
        setDataTypeOptions();
        setStaticFunctionsOptions();
        setScriptedFunctionsOptions();
    }

    private void setDataTypeOptions() {
        dataTypeDropdown.clear();
        for (CsiDataType dataType : CsiDataType.values()) {
            if(dataType == CsiDataType.Unsupported)
                continue;

            dataTypeDropdown.addItem(dataType.getLabel());
        }
        dataTypeDropdown.setAllowBlank(false);
        dataTypeDropdown.setTypeAhead(false);
        dataTypeDropdown.setForceSelection(true);
   }

    private void setStaticFunctionsOptions() {
        fieldTypeDropdown.clear();
        fieldTypeDropdown.addItem(STATIC);
        fieldTypeDropdown.addItem(DERIVED);
        if(javascriptingEnabled())
            fieldTypeDropdown.addItem(SCRIPTED);

        fieldTypeDropdown.addSelectionChangedHandler(handleFieldTypeChange);
        fieldTypeDropdown.setAllowBlank(false);
        fieldTypeDropdown.setTypeAhead(false);
        fieldTypeDropdown.setForceSelection(true);
    }

    private void setScriptedFunctionsOptions() {
        dynamicFunctionsDropdown.clear();
        for (ScriptedFunctions df : ScriptedFunctions.values()) {
            dynamicFunctionsDropdown.addItem(df.getTitle());
        }
//        dynamicFunctionsDropdown.addItem(ScriptedFunctions.ADVANCED_FUNCTION.getTitle());
        dynamicFunctionsDropdown.setSelectedValue(ScriptedFunctions.ADVANCED_FUNCTION.getTitle());
        dynamicFunctionsDropdown.setAllowBlank(false);
        dynamicFunctionsDropdown.setTypeAhead(false);
        dynamicFunctionsDropdown.setForceSelection(true);
    }

    private boolean javascriptingEnabled() {
        return WebMain.getClientStartupInfo().getFeatureConfigGWT().isScriptingEnabled();
    }

    private boolean isPrecacheEnabled() {
        return WebMain.getClientStartupInfo().getFeatureConfigGWT().isPrecacheEnabled();
    }

    private void handleStaticValueRow() {

        if (staticValueRow.isVisible()) {

            showHelpText("150px"); //$NON-NLS-1$
            createStaticValueInputWidget(_type);

        } else {

            if (derivedValueRow.isVisible()) {

                showHelpText("190px"); //$NON-NLS-1$

            } else {

                hideHelpText();
            }
            destroyStaticValueInputWidget();
        }
    }

    private void showHelpText(String heightIn) {

        //dialogContainer.setWidth("325px");
        //infoContainer.setWidth("290px");
        infoDisplay.setHeight(heightIn);
        infoContainer.setVisible(true);
    }

    private void hideHelpText() {

        //dialogContainer.setWidth("615px");
        //infoContainer.setWidth("0px");
        infoContainer.setVisible(false);
    }

    private void createStaticValueInputWidget(CsiDataType dataTypeIn) {

        String myPrompt = " "; //$NON-NLS-1$
        String myDefault = _model.getStaticValue();
        int myWidth = 220;
        CsiDataType myDataType = (null != dataTypeIn) ? dataTypeIn : CsiDataType.String;

        if ((null != staticValueInput) && (!myDataType.equals(_staticShadowType))) {

            destroyStaticValueInputWidget();

            if (myDataType.equals(_staticShadowType)) {

                myDefault = _staticShadowValue;

            } else {

                myDefault = null;
            }
        }

        if (null == staticValueInput) {

            try {

                switch (myDataType) {

                    case String :

                        staticValueInput = new TextInputWidget(myPrompt, myDefault, true);
                        break;

                    case Boolean :

                        staticValueInput = new BooleanInputWidget(myPrompt, myDefault, true);
                        break;

                    case Integer :

                        staticValueInput = new IntegerInputWidget(myPrompt, myDefault, true);
                        break;

                    case Number :

                        staticValueInput = new ValueInputWidget(myPrompt, myDefault, true);
                        break;

                    case DateTime :

                        staticValueInput = new DateTimeInputWidget(myPrompt, myDefault, true);
                        break;

                    case Date :

                        staticValueInput = new DateInputWidget(myPrompt, myDefault, true);
                        break;

                    case Time :

                        staticValueInput = new TimeInputWidget(myPrompt, myDefault, true);
                        break;

                    case Unsupported :
                    default:

                        myDataType = CsiDataType.String;
                        staticValueInput = new TextInputWidget(myPrompt, myDefault, true);
                        break;
                }

            } catch(Exception myException) {

                myDataType = CsiDataType.String;
                infoDisplay.setText(_infoMap.get(myDataType));
                staticValueInput = new TextInputWidget(myPrompt, myDefault, true);
            }
            staticValueInput.setPixelSize(myWidth, staticValueInput.getRequestedHeight());
            infoDisplay.setText(_infoMap.get(myDataType));
            staticValueColumn.add(staticValueInput);
            _staticShadowType = myDataType;
        }
    }

    private void destroyStaticValueInputWidget() {

        if (null != staticValueInput) {

            staticValueColumn.remove(staticValueInput);
            staticValueInput = null;
        }
    }

    private void extractStaticValue() {

        String myValue = null;

        if (null != staticValueInput) {

            _model.setFieldType(FieldType.STATIC);

            try {

                myValue = staticValueInput.getText();

            } catch (Exception myException) {

                Display.error(myException.getMessage());
            }
        }
        _model.setStaticValue(myValue);
    }

    private SqlTokenTreeItemList getExpression() {

        SqlTokenTreeItemList mySqlExpression = null;

        if ((null != _derivedShadowType) && (_derivedShadowType.equals(_type))) {

            mySqlExpression = _sqlShadowTree;
        }
        return mySqlExpression;
    }

    private void displayExpression() {

        if (FieldType.DERIVED.equals(_mode)) {

            SqlTokenTreeItemList mySqlExpression = getExpression();

            if (null != mySqlExpression) {

                try {

                    sqlExpression.setText(mySqlExpression.get(0).format(_slqTokenValueCallback, false), Dialog.txtSuccessColor);

                } catch(Exception myException) {

                    sqlExpression.setText(_txtExpressionPrompt, Dialog.txtErrorColor);
                    Dialog.showException(myException);
                }

            } else {

                sqlExpression.setText(_txtExpressionPrompt, Dialog.txtErrorColor);
            }
        }
    }

    private void recordDerivedInfo(SqlTokenTreeItemList tokenTreeIn) {

        _derivedShadowType = CsiDataType.getValue(dataTypeDropdown.getSelectedValue());
        _sqlShadowTree = tokenTreeIn;

        if ((null != _sqlShadowTree) && (0 < _sqlShadowTree.size())) {

            SqlTokenTreeItem myTopItem = _sqlShadowTree.get(0);

            if (null != myTopItem) {

                myTopItem.setRequiredDataType(_derivedShadowType.getMask());
            }
        }
    }

    private void recordStaticInfo(CsiDataType typeIn, String textIn) {

        _staticShadowType = typeIn;
        _staticShadowValue = textIn;
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}

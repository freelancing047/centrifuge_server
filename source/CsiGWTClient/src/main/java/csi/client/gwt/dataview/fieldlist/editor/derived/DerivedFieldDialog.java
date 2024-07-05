package csi.client.gwt.dataview.fieldlist.editor.derived;

import java.util.Map;
import java.util.TreeMap;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.csiwizard.ConstantDataEntryDialog;
import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.CsiListView;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.DialogInfoTextArea;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.buttons.CyanButton;
import csi.client.gwt.widget.buttons.MiniAmberButton;
import csi.client.gwt.widget.buttons.MiniBlueButton;
import csi.client.gwt.widget.buttons.RedButton;
import csi.client.gwt.widget.display_list_widgets.ComponentLabel;
import csi.client.gwt.widget.display_list_widgets.ComponentTreeHelper;
import csi.client.gwt.widget.display_list_widgets.ComponentTreePanel;
import csi.client.gwt.widget.display_list_widgets.DisplayList;
import csi.client.gwt.widget.display_list_widgets.SegmentedStringWidget;
import csi.client.gwt.widget.display_list_widgets.SelectionCallback;
import csi.client.gwt.widget.display_list_widgets.SqlTokenItemBuilder;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.SqlToken;
import csi.server.common.enumerations.SqlTokenType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SqlTokenTreeItem;
import csi.server.common.model.SqlTokenTreeItemList;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryParameterDef;

/**
 * Created by centrifuge on 2/18/2015.
 */
public class DerivedFieldDialog implements ValidityCheck {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<ValidatingDialog, DerivedFieldDialog> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ValidatingDialog dialog;

    @UiField
    FullSizeLayoutPanel topPanel;
    @UiField
    HorizontalPanel valuePanel;

    @UiField
    CyanButton undoButton;
    @UiField
    RedButton deleteButton;
    @UiField
    MiniBlueButton valueButton;
    @UiField
    MiniBlueButton parameterButton;
    @UiField
    MiniAmberButton fieldButton;

    @UiField(provided = true)
    SegmentedStringWidget<SqlToken> expressionWidget;
    @UiField
    ComponentTreePanel treeWidget;

    @UiField
    RadioButton radio1;
    @UiField
    RadioButton radio2;
    @UiField
    RadioButton radio3;
    @UiField
    RadioButton radio4;
    @UiField
    RadioButton radio5;
    @UiField
    RadioButton radio6;
    @UiField
    RadioButton radio7;

    @UiField
    MiniAmberButton group1Label;
    @UiField
    MiniAmberButton group2Label;
    @UiField
    MiniAmberButton group3Label;
    @UiField
    MiniAmberButton group4Label;

    @UiField
    DialogInfoTextArea instructionTextArea;

    @UiField(provided = true)
    CsiListView<SqlTokenSelectionItem> group1;
    @UiField(provided = true)
    CsiListView<SqlTokenSelectionItem> group2;
    @UiField(provided = true)
    CsiListView<SqlTokenSelectionItem> group3;
    @UiField(provided = true)
    CsiListView<SqlTokenSelectionItem> group4;
    @UiField(provided = true)
    CsiListView<DataFieldSelectionItem> fieldList;
    @UiField(provided = true)
    CsiListView<ParameterSelectionItem> parameterList;

    private MiniAmberButton[] _SelectionListLabels = null;
    private RadioButton[] _radioButtons = null;
    private CsiListView<SqlTokenSelectionItem>[] selectionList = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String _txtTitle = _constants.derivedFieldDialog_Title();
    private static String _txtHelpPath = _constants.derivedFieldDialog_HelpTarget();

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private static final int _groupCount = 4;

    private static final String _txtMissingPrompt = _constants.sqlTokenTreeLabel_MissingValue();
    private static final String _txtOptionalPrompt = _constants.sqlTokenTreeLabel_OptionalValueValue();
    private static final String _txtDataFieldHeader = _constants.derivedFieldDialog_DataFieldHeader();
    private static final String _txtParameterHeader = _constants.derivedFieldDialog_DataViewParameterHeader();
    private static final String _txtUserInputPrompt = _constants.derivedFieldDialog_UserInputHeader();
    private static final String _txtBasicInstructions = _constants.derivedFieldDialog_Instructions(_txtMissingPrompt,
                                                                            _txtOptionalPrompt, _txtDataFieldHeader,
                                                                            _txtParameterHeader, _txtUserInputPrompt);

    private static final String[][] _groupLabelText
    = {
        {
                _constants.derivedFieldDialog_group_1_String(),
                _constants.derivedFieldDialog_group_1_Boolean(),
                _constants.derivedFieldDialog_group_1_Integer(),
                _constants.derivedFieldDialog_group_1_Number(),
                _constants.derivedFieldDialog_group_1_DateTime(),
                _constants.derivedFieldDialog_group_1_Date(),
                _constants.derivedFieldDialog_group_1_Time()
        },
        {
                _constants.derivedFieldDialog_group_2_String(),
                _constants.derivedFieldDialog_group_2_Boolean(),
                _constants.derivedFieldDialog_group_2_Integer(),
                _constants.derivedFieldDialog_group_2_Number(),
                _constants.derivedFieldDialog_group_2_DateTime(),
                _constants.derivedFieldDialog_group_2_Date(),
                _constants.derivedFieldDialog_group_2_Time()
        },
        {
                _constants.derivedFieldDialog_group_3_String(),
                _constants.derivedFieldDialog_group_3_Boolean(),
                _constants.derivedFieldDialog_group_3_Integer(),
                _constants.derivedFieldDialog_group_3_Number(),
                _constants.derivedFieldDialog_group_3_DateTime(),
                _constants.derivedFieldDialog_group_3_Date(),
                _constants.derivedFieldDialog_group_3_Time()
        },
        {
                _constants.derivedFieldDialog_group_4_String(),
                _constants.derivedFieldDialog_group_4_Boolean(),
                _constants.derivedFieldDialog_group_4_Integer(),
                _constants.derivedFieldDialog_group_4_Number(),
                _constants.derivedFieldDialog_group_4_DateTime(),
                _constants.derivedFieldDialog_group_4_Date(),
                _constants.derivedFieldDialog_group_4_Time()
        }
    };
//    private static final String _blueText = "#216893";
//    private static final String _orangeText = "#fbb450";
    private static final String _blueText = "white";
    private static final String _orangeText = "white";

    private ListStore<SqlTokenSelectionItem>[] _dataStore = null;
    
    private Map<CsiDataType, RadioButton> _radioButtonMap = null;

    private DataViewDef _meta;
    private FieldListAccess _dataModel;
    private CsiDataType _activeDataType = null;
    private CsiDataType _initialDataType;
    private SqlTokenTreeItem _sourceTree;
    private DisplayList<ComponentLabel, SqlToken> _displayList;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SelectionCallback handleSelectionChange = new SelectionCallback() {

        @Override
        public void segmentSelected(Integer parentKeyIn, Integer keyIn, Integer slotIn, boolean forwardIn) {

            resetDataTypes(parentKeyIn, slotIn);
        }

        @Override
        public void valueSelected(Integer parentKeyIn, Integer keyIn, Integer slotIn, boolean forwardIn) {

            resetDataTypes(parentKeyIn, slotIn);
        }

        @Override
        public void emptyValueSelected(Integer parentKeyIn, Integer keyIn, Integer slotIn, boolean forwardIn) {

            resetDataTypes(parentKeyIn, slotIn);
        }
    };

    private ComponentTreeHelper<SqlToken> treeHelper = new ComponentTreeHelper<SqlToken>() {

        @Override
        public String getLabel(SqlToken itemIn) {

            return SqlTokenDisplayHelper.getTreeLabel(itemIn);
        }

        @Override
        public ImageResource getIcon(SqlToken itemIn) {

            return DataSourceClientUtil.get(itemIn.getType());
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CsiListView.HoverCallBack<SqlTokenSelectionItem> handleFunctionHover
            = new CsiListView.HoverCallBack<SqlTokenSelectionItem>() {

        public void onHoverChange(SqlTokenSelectionItem targetIn, boolean isOverIn) {

            if (isOverIn) {

                SqlToken myToken = targetIn.getToken();
                String myHelpDisplay = SqlTokenDisplayHelper.getHelp(myToken);

                if (null != myHelpDisplay) {

                    instructionTextArea.setText(myHelpDisplay);

                } else {

                    instructionTextArea.setText(_txtBasicInstructions);
                }

            } else {

                instructionTextArea.setText(_txtBasicInstructions);
            }
        }
    };

    private ClickHandler handleUndoClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            expressionWidget.undo();
            updateControls(false);
        }
    };

    private ClickHandler handleDeleteClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            boolean myAddOnFlag = SqlTokenDisplayHelper.isAddOn(_displayList);

            expressionWidget.deleteSelected(myAddOnFlag);
            if (myAddOnFlag) {

                resetSelectionLists(true);

            } else {

                updateControls(true);
            }
        }
    };

    private ClickHandler handleValueClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
/*
            valueButton.setType(ButtonType.WARNING);
            parameterButton.setType(ButtonType.PRIMARY);
            fieldButton.setType(ButtonType.PRIMARY);
            valuePanel.setVisible(true);
            parameterList.setVisible(false);
            fieldList.setVisible(false);
*/
            dialog.suspendMonitoring();

            new ConstantDataEntryDialog(_activeDataType, null, handleUserInput);
        }
    };

    private ClickHandler handleParameterClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            valueButton.setType(ButtonType.PRIMARY);
            parameterButton.setType(ButtonType.WARNING);
            fieldButton.setType(ButtonType.PRIMARY);
            valueButton.getElement().getStyle().setColor(_orangeText);
            parameterButton.getElement().getStyle().setColor(_blueText);
            fieldButton.getElement().getStyle().setColor(_orangeText);
            valueButton.setTextSize(11);
            parameterButton.setTextSize(11);
            fieldButton.setTextSize(11);
            valuePanel.setVisible(false);
            parameterList.setVisible(true);
            fieldList.setVisible(false);
        }
    };

    private ClickHandler handleFieldClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            valueButton.setType(ButtonType.PRIMARY);
            parameterButton.setType(ButtonType.PRIMARY);
            fieldButton.setType(ButtonType.WARNING);
            valueButton.getElement().getStyle().setColor(_orangeText);
            parameterButton.getElement().getStyle().setColor(_orangeText);
            fieldButton.getElement().getStyle().setColor(_blueText);
            valueButton.setTextSize(11);
            parameterButton.setTextSize(11);
            fieldButton.setTextSize(11);
            valuePanel.setVisible(false);
            parameterList.setVisible(false);
            fieldList.setVisible(true);
        }
    };

    private UserInputEventHandler<Integer> handleUserInput = new UserInputEventHandler<Integer>() {

        @Override
        public void onUserInput(UserInputEvent<Integer> eventIn) {

            if (!eventIn.isCanceled()) {

                SqlToken myToken = SqlToken.getDataValueToken(_activeDataType);
                SqlTokenTreeItem myTreeItem = new SqlTokenTreeItem(eventIn.getInput());
                expressionWidget.replaceSelection(myTreeItem, myToken);
                updateControls(false);
            }
            dialog.beginMonitoring();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DerivedFieldDialog(DataViewDef metaIn, CsiDataType dataTypeIn, SqlTokenTreeItemList currentValueIn,
                              ClickHandler applyHandlerIn, ClickHandler cancelHandlerIn) throws CentrifugeException {

        CsiDataType[] myDataTypeList = CsiDataType.sortedValuesByLabel().toArray(new CsiDataType[0]);

        _meta = metaIn;
        _dataModel = _meta.getModelDef().getFieldListAccess();
        _activeDataType = (_initialDataType = dataTypeIn);
        _sourceTree = (null != currentValueIn) ? currentValueIn.get(0) : null;
        _radioButtonMap = new TreeMap<CsiDataType, RadioButton>();

        group1 = new CsiListView<SqlTokenSelectionItem>();
        group1.setHoverCallBack(handleFunctionHover);
        group2 = new CsiListView<SqlTokenSelectionItem>();
        group2.setHoverCallBack(handleFunctionHover);
        group3 = new CsiListView<SqlTokenSelectionItem>();
        group3.setHoverCallBack(handleFunctionHover);
        group4 = new CsiListView<SqlTokenSelectionItem>();
        group4.setHoverCallBack(handleFunctionHover);
        fieldList = new CsiListView<DataFieldSelectionItem>();
        parameterList = new CsiListView<ParameterSelectionItem>();

        selectionList = new CsiListView[]{group1, group2, group3, group4};
        _dataStore = new ListStore[_groupCount];

        for (int i = 0; _groupCount > i; i++) {

            _dataStore[i] = selectionList[i].getStore();
        }

        expressionWidget = new SegmentedStringWidget<SqlToken>(metaIn, SqlTokenDisplayHelper.getTokenToLabelMap(), handleSelectionChange);
        expressionWidget.setWidth("595px"); //$NON-NLS-1$
        expressionWidget.setBaseLabel(_constants.derivedFieldDialog_ExpressionPrompt());

        dialog = uiBinder.createAndBindUi(this);

        _radioButtons = new RadioButton[] {radio1, radio2, radio3, radio4, radio5, radio6, radio7};
        _SelectionListLabels = new MiniAmberButton[] {group1Label, group2Label, group3Label, group4Label};

        if (myDataTypeList.length != _radioButtons.length) {

            throw new CentrifugeException("FATAL ERROR: Data Type Labels not configured correctly!");
        }

        for (int i = 0; myDataTypeList.length > i; i++) {

            CsiDataType myDataType = myDataTypeList[i];
            RadioButton myRadioButton = _radioButtons[i];

            myRadioButton.setText(myDataType.getLabel());
            _radioButtonMap.put(myDataType, myRadioButton);
        }
        undoButton.setText(Dialog.txtUndoButton);
        deleteButton.setText(Dialog.txtDeleteButton);
        valueButton.setText(_constants.derivedFieldDialog_UserInputHeader());
        parameterButton.setText(_constants.derivedFieldDialog_DataViewParameterHeader());
        fieldButton.setText(_constants.derivedFieldDialog_DataFieldHeader());
        instructionTextArea.setText(_txtBasicInstructions);
        valueButton.getElement().getStyle().setColor(_orangeText);
        parameterButton.getElement().getStyle().setColor(_orangeText);
        fieldButton.getElement().getStyle().setColor(_blueText);
        group1Label.getElement().getStyle().setColor(_blueText);
        group2Label.getElement().getStyle().setColor(_blueText);
        group3Label.getElement().getStyle().setColor(_blueText);
        group4Label.getElement().getStyle().setColor(_blueText);
        valueButton.setTextSize(11);
        parameterButton.setTextSize(11);
        fieldButton.setTextSize(11);
        group1Label.setTextSize(11);
        group2Label.setTextSize(11);
        group3Label.setTextSize(11);
        group4Label.setTextSize(11);

        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtTitle, _txtHelpPath, true);

        attachHandlers(applyHandlerIn, cancelHandlerIn);
    }

    public void checkValidity() {

        if (null != _displayList) {

            dialog.getActionButton().setEnabled(_displayList.isValid());

        } else {

            dialog.getActionButton().setEnabled(false);
        }
    }

    public void show() {

        expressionWidget.connect(treeWidget, treeHelper);
        _displayList = expressionWidget.initializeDisplay(_sourceTree);

        resetCheckBoxes(new CsiDataType[]{_initialDataType});

        dialog.setCallBack(this);

        dialog.show(60);

        expressionWidget.refresh();
    }

    public DerivedFieldDialog destroy() {

        dialog.hide();
        dialog.removeFromParent();

        return null;
    }

    public SqlTokenTreeItemList generateSqlExpression() {

        SqlTokenTreeItem myResults = expressionWidget.getResults(new SqlTokenItemBuilder());

        return (null != myResults) ? new SqlTokenTreeItemList(myResults) : null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void attachHandlers(ClickHandler applyHandlerIn, ClickHandler cancelHandlerIn) {

        CsiDataType[] myDataTypeList = CsiDataType.sortedValuesByLabel().toArray(new CsiDataType[0]);

        selectionList[0].getSelectionModel().addSelectionHandler(new SelectionHandler<SqlTokenSelectionItem>() {
            @Override
            public void onSelection(SelectionEvent<SqlTokenSelectionItem> eventIn) {

                final SqlTokenSelectionItem mySelection = eventIn.getSelectedItem();
                SqlToken myToken = mySelection.getToken();
                SqlTokenType myType = (null != myToken) ? myToken.getTokenType() : null;

                switch (myType) {

                    case CONDITIONAL :

                        expressionWidget.replaceSelection(new SqlTokenTreeItem(), myToken);
                        expressionWidget.selectChild(1);
                        expressionWidget.replaceSelection(new SqlTokenTreeItem(), myToken.getConditionalComponent());
                        expressionWidget.clearUndo();
                        resetSelectionLists(false);
                        break;

                    case CONDITIONAL_COMPONENT_1 :

                        expressionWidget.prependToSelection(new SqlTokenTreeItem(), myToken);
                        resetSelectionLists(false);
                        break;

                    case CONDITIONAL_COMPONENT_2 :

                        expressionWidget.appendToSelection(new SqlTokenTreeItem(), myToken);
                        resetSelectionLists(false);
                        break;

                    case CONDITIONAL_DEFAULT :

                        expressionWidget.addToSelectedParent(new SqlTokenTreeItem(), myToken);
                        resetSelectionLists(false);
                        break;

                    default :

                        expressionWidget.replaceSelection(new SqlTokenTreeItem(), myToken);
                        DeferredCommand.add(new Command() {
                            public void execute() {
                                selectionList[0].getSelectionModel().deselect(mySelection);
                            }
                        });
                        break;
                }
                updateControls(false);
            }
        });

        for (int i = 1; _groupCount > i; i++) {

            final CsiListView<SqlTokenSelectionItem> myList = selectionList[i];

            myList.getSelectionModel().addSelectionHandler(new SelectionHandler<SqlTokenSelectionItem>() {
                @Override
                public void onSelection(SelectionEvent<SqlTokenSelectionItem> eventIn) {

                    final SqlTokenSelectionItem mySelection = eventIn.getSelectedItem();
                    SqlToken myToken = mySelection.getToken();
                    SqlTokenType myType = (null != myToken) ? myToken.getTokenType() : null;

                    expressionWidget.replaceSelection(new SqlTokenTreeItem(), myToken);
                    DeferredCommand.add(new Command() {
                        public void execute() {
                            myList.getSelectionModel().deselect(mySelection);
                        }
                    });
                    updateControls(false);
                }
            });
        }

        fieldList.getSelectionModel().addSelectionHandler(new SelectionHandler<DataFieldSelectionItem>() {
            @Override
            public void onSelection(SelectionEvent<DataFieldSelectionItem> eventIn) {

                final DataFieldSelectionItem mySelection = eventIn.getSelectedItem();
                FieldDef myDataField = mySelection.getDataField();
                SqlToken myToken = SqlToken.getDataFieldToken(myDataField.getValueType());
                SqlTokenTreeItem myTreeItem = new SqlTokenTreeItem(myDataField.getFieldName());
                expressionWidget.replaceSelection(myTreeItem, myToken);
                updateControls(false);
                DeferredCommand.add(new Command() {
                    public void execute() {
                        fieldList.getSelectionModel().deselect(mySelection);
                    }
                });
            }
        });

        parameterList.getSelectionModel().addSelectionHandler(new SelectionHandler<ParameterSelectionItem>() {
            @Override
            public void onSelection(SelectionEvent<ParameterSelectionItem> eventIn) {

                final ParameterSelectionItem mySelection = eventIn.getSelectedItem();
                QueryParameterDef myParameter = mySelection.getDataField();
                SqlToken myToken = SqlToken.getParameterToken(myParameter.getType());
                SqlTokenTreeItem myTreeItem = new SqlTokenTreeItem(myParameter.getName());
                expressionWidget.replaceSelection(myTreeItem, myToken);
                updateControls(false);
                DeferredCommand.add(new Command() {
                    public void execute() {
                        parameterList.getSelectionModel().deselect(mySelection);
                    }
                });
            }
        });

        for (int i = 0; myDataTypeList.length > i; i++) {

            final CsiDataType myDataType = myDataTypeList[i];
            final RadioButton myRadioButton = _radioButtons[i];

            myRadioButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent eventIn) {

                    recordDataTypeChange(myDataType);
                }
            });
        }

        undoButton.addClickHandler(handleUndoClick);
        deleteButton.addClickHandler(handleDeleteClick);
        valueButton.addClickHandler(handleValueClick);
        parameterButton.addClickHandler(handleParameterClick);
        fieldButton.addClickHandler(handleFieldClick);
        dialog.getActionButton().addClickHandler(applyHandlerIn);
        dialog.getCancelButton().addClickHandler(cancelHandlerIn);
    }

    private void resetDataTypes(Integer parentKeyIn, Integer slotIn) {

        if ((null != parentKeyIn) && (null != slotIn)) {

            CsiDataType[] myDataTypes = SqlTokenDisplayHelper.getArgumentDataTypes(_displayList, parentKeyIn, slotIn);

            if (null != myDataTypes) {

                resetCheckBoxes(myDataTypes);

            } else if (0 == slotIn) {

                resetCheckBoxes(new CsiDataType[] {_initialDataType});
            }
        }
        updateControls(false);
    }

    private void resetCheckBoxes(CsiDataType[] dataTypesIn) {

        if ((null != dataTypesIn) && (0 < dataTypesIn.length)) {

            CsiDataType[] myDataTypeList = CsiDataType.sortedValuesByLabel().toArray(new CsiDataType[0]);

            boolean mySelected = true;

            // Make all radio buttons invisible
            for (RadioButton myRadioButton : _radioButtons) {

                myRadioButton.setVisible(false);
            }

            // Make visible only radio buttons corresponding to allowed data types
            for (int i = 0; dataTypesIn.length > i; i++) {

                CsiDataType myDataType = dataTypesIn[i];
                RadioButton myRadioButton = _radioButtonMap.get(myDataType);

                if (null != myRadioButton) {

                    myRadioButton.setVisible(true);
                }
            }

            // Select first visible radio button
            for (int i = 0; myDataTypeList.length > i; i++) {

                RadioButton myRadioButton = _radioButtons[i];

                if (myRadioButton.isVisible()) {

                    _activeDataType = myDataTypeList[i];
                    myRadioButton.setValue(true);
                    break;
                }
            }
            resetSelectionLists(false);
        }
        updateControls(false);
    }

    private void resetSelectionLists(boolean isDeleteIn) {

        if ((!isDeleteIn) &&(SqlTokenDisplayHelper.isConditional(_displayList))) {

            _dataStore[0].clear();
            _dataStore[0].addAll(SqlTokenSelectionItem.getList(_activeDataType, SqlToken.getBaseGroup(),
                    _displayList.getSelectedObject(), SqlTokenDisplayHelper.hasConditionalDefault(_displayList)));
            selectionList[0].setEnabled(expressionWidget.hasSelection());
            selectionList[0].refresh();

            for (int i = 1, j = SqlToken.getBaseGroup() + 1; _groupCount > i; i++, j++) {

                _dataStore[i].clear();
                selectionList[i].refresh();
            }

            fieldList.getStore().clear();
            fieldList.refresh();
            parameterList.getStore().clear();
            parameterList.refresh();

        } else {

            for (int i = 0, j = SqlToken.getBaseGroup(); _groupCount > i; i++, j++) {

                _dataStore[i].clear();
                _dataStore[i].addAll(SqlTokenSelectionItem.getList(_activeDataType, j));
                selectionList[i].setEnabled(expressionWidget.hasSelection());
                selectionList[i].refresh();
            }

            fieldList.getStore().clear();
            fieldList.getStore().addAll(DataFieldSelectionItem.getResetList(_dataModel, _activeDataType));
            fieldList.setEnabled(expressionWidget.hasSelection());
            fieldList.refresh();
            parameterList.getStore().clear();
            parameterList.getStore().addAll(ParameterSelectionItem.getResetList(_meta, _activeDataType));
            parameterList.setEnabled(expressionWidget.hasSelection());
            parameterList.refresh();
        }
        for (int i = 0; _groupCount > i; i++) {

            String myLabel = _groupLabelText[i][_activeDataType.ordinal()];

            _SelectionListLabels[i].setText(((null != myLabel) && (0 < myLabel.length())) ? myLabel : "-  -  -");
        }
    }

    private void updateControls(boolean isDeleteIn) {

        SqlToken myToken = expressionWidget.getSelectedObject();
        String myLabel = (null != myToken) ? SqlTokenDisplayHelper.getLabel(myToken) : ""; //$NON-NLS-1$

        expressionWidget.setDeltaLabel(myLabel, Dialog.txtPatternColor);
        undoButton.setEnabled(expressionWidget.canUndo());
        deleteButton.setEnabled(SqlTokenDisplayHelper.canDelete(_displayList));
    }

    private void recordDataTypeChange(CsiDataType dataTypeIn) {

        _activeDataType = dataTypeIn;
        resetSelectionLists(false);
    }

    private ImageResource getIcon(CsiDataType dataTypeIn) {

        return DataSourceClientUtil.get(dataTypeIn);
    }
}

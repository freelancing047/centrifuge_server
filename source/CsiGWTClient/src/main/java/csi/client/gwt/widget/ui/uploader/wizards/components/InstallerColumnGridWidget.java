package csi.client.gwt.widget.ui.uploader.wizards.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.dom.client.Style;
//import com.google.gwt.event.dom.client.ChangeEvent;
//import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.InstructionOverlay;
import csi.client.gwt.csiwizard.widgets.GridWidget;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.DialogInfoTextArea;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.uploader.wizards.support.FormatValue;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.enumerations.DisplayMode;

/**
 * Created by centrifuge on 8/18/2015.
 */
public class InstallerColumnGridWidget extends GridWidget<InstallerColumnDisplay> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private HorizontalPanel leftPanel = null;
    private HorizontalPanel rightPanel = null;
    private CheckBox hideColumns = null;
    private InlineLabel operationLabel = null;
    private CsiStringListBox operationSelection = null;
    private InstructionOverlay dialogOverlay = null;
    private FullSizeLayoutPanel overlayPanel = null;
    private DialogInfoTextArea overlayPrompt = null;
    private DialogInfoTextArea overlayInstructions = null;
    private Label overlayError = null;
    private FilteredTextBox overlayTextBox = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _txtRenameInstructions = _constants.installFileWizard_RenameColumn_Instructions();
    private static final String _txtSelectOperationLabel = _constants.installFileWizard_ChoseOperation();
    private static final String _txtChangeTypeMenuSubstring = _constants.installFileWizard_TypeEquals();

    private boolean _inOverlayMode = false;
    private boolean _namesValidFlag = true;
    private CsiFileType _fileType;
    private ColumnGridInfo _gridInfo;
    private ListStore<InstallerColumnDisplay> _gridStore;
    private List<InstallerColumnDisplay> _backupList;
    private UserSecurityInfo _userInfo;
    private ClickHandler _renameHandler = null;
    private int _renameIndex = 0;
    private FormatValue _formatter = null;

    private Map<Integer, Integer> _gridMap = null;
    private Map<String, Integer> _columnNameMap = null;

    private int _debugValue = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ValueChangeHandler handleHideOrShowColumns = new ValueChangeHandler() {
        @Override
        public void onValueChange(ValueChangeEvent event) {

            if (hideColumns.getValue()) {

                removeDisplayColumns();
                
            } else {

                restoreDisplayColumns();
            }
        }
    };

    private HeaderClickEvent.HeaderClickHandler handleGridHeaderClick = new HeaderClickEvent.HeaderClickHandler() {
        @Override
        public void onHeaderClick(HeaderClickEvent event) {

            operationSelection.setSelectedIndex(0);
        }
    };

    private CellClickEvent.CellClickHandler handleGridCellClick = new CellClickEvent.CellClickHandler() {
        @Override
        public void onCellClick(CellClickEvent event) {

            operationSelection.setSelectedIndex(0);
        }
    };

    private SelectionChangedHandler<String> handleOperationSelection =  new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            List<InstallerColumnDisplay> mySelection = grid.getSelectionModel().getSelection();

            if ((null != mySelection) && (0 < mySelection.size())) {

                // Create undo context

                int myChoice = operationSelection.getSelectedIndex();

                if (0 != myChoice) {

                    if (1 == myChoice) {

                        renameColumns(mySelection);

                    } else if (5 > myChoice) {

                        boolean myFlag = (4 == myChoice);

                        if ((!myFlag) && hideColumns.getValue()) {

                            ListStore<InstallerColumnDisplay> myStore = grid.getStore();

                            for (InstallerColumnDisplay myItem : mySelection) {

                                myItem.setInclude(myFlag);
                                _backupList.set(myItem.getColumnNumber() - 1, myItem);
                                myStore.remove(myItem);
                            }

                        } else {

                            for (InstallerColumnDisplay myItem : mySelection) {

                                myItem.setInclude(myFlag);
                            }
                        }
                        grid.getSelectionModel().deselectAll();

                    } else if (5 < myChoice) {

                        String myDataTypeName = operationSelection.getSelectedValue().substring(_txtChangeTypeMenuSubstring.length() + 1);
                        CsiDataType myDataType = CsiDataType.getValue(myDataTypeName);

                        for (InstallerColumnDisplay myItem : mySelection) {

                            myItem.setDataType(myDataTypeName);
                            myItem.setFirstValue(_formatter.onDataTypeChange(myItem.getColumnNumber(), myDataType));
                        }
                        grid.getSelectionModel().deselectAll();
                    }
                }
            }
            refresh();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstallerColumnGridWidget(ColumnGridInfo gridInfoIn, CsiFileType typeIn, String nameIn,
                                     List<InstallerColumnDisplay> listIn, FormatValue formatterIn, boolean forceIn) {

        super(gridInfoIn.createGrid(), new HorizontalPanel(), new HorizontalPanel());

        _gridInfo = gridInfoIn;
        _gridStore = grid.getStore();
        _gridMap = new TreeMap<Integer, Integer>();
        _columnNameMap = new TreeMap<String, Integer>();
        _formatter = formatterIn;

        _backupList = listIn;
        restoreList();

        _userInfo = WebMain.injector.getMainPresenter().getUserInfo();
        _fileType = typeIn;

        buildTopPanel(typeIn, nameIn, forceIn);
        buildBottomPanel();
        grid.addHeaderClickHandler(handleGridHeaderClick);
        grid.addCellClickHandler(handleGridCellClick);
        operationSelection.addSelectionChangedHandler(handleOperationSelection);
        grid.getView().refresh(true);
    }

    public List<InstallerColumnDisplay> getSelectionList() {

        return grid.getSelectionModel().getSelection();
    }

    public void scrollTo(InstallerColumnDisplay selectionIn) {

        Integer myIndex = _gridMap.get(selectionIn.getColumnNumber());

        if (null != myIndex) {

            grid.getView().focusRow(myIndex);
        }
    }

    public boolean isValid() {

        boolean myValidFlag = false;

        if (_inOverlayMode) {

            overlayTextBox.isValid();

        } else {

            // Adjust selectability of Operation menu.
            enableOperationSelection(true);
        }
        myValidFlag = _namesValidFlag && super.isValid();

        return myValidFlag;
    }

    public List<InstallerColumnDisplay> getGridData() {

        backupList();

        return _backupList;
    }

    @Override
    public void handleCarriageReturn() {

        if ((null != dialogOverlay) && (overlayTextBox.isValid())) {

            try {

                final List<InstallerColumnDisplay> mySelection = grid.getSelectionModel().getSelection();

                if ((null != mySelection) && (0 < mySelection.size())) {

                    if (mySelection.size() > _renameIndex) {

                        InstallerColumnDisplay myItem = mySelection.get(_renameIndex);

                        myItem.setName(overlayTextBox.getText());
                        addColumnName(myItem.getName());

                        if (mySelection.size() > ++_renameIndex) {

                            refreshOverlay(mySelection);

                        } else {

                            dialogOverlay.hide();
                            dialogOverlay = null;
                            grid.getSelectionModel().deselectAll();
                            refresh();

                            DeferredCommand.add(new Command() {
                                public void execute() {

                                    exitOverlayMode();
                                }
                            });
                        }
                    }
                }

            } catch(Exception myException) {

                Dialog.showException(myException);
            }

        } else {

            super.handleCarriageReturn();
        }
    }

    @Override
    public void handleEscapeKey() {

        if (null != dialogOverlay) {

            dialogOverlay.hide();
            dialogOverlay = null;

            DeferredCommand.add(new Command() {
                public void execute() {

                    refresh();
                    exitOverlayMode();
                }
            });

        } else {

            super.handleEscapeKey();
        }
    }

    public void refresh() {

        validateNames();
        //We are doing this keep position on the grid, otherwise it will scroll up to the top.
        Integer scrollHeight = grid.getView().getEditorParent().getScrollTop();
        grid.getView().refresh(false);
        grid.getView().getEditorParent().setScrollTop(scrollHeight);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void refreshOverlay(List<InstallerColumnDisplay> selectionIn) {

        if (null != dialogOverlay) {

            try {

                InstallerColumnDisplay myItem = selectionIn.get(_renameIndex);
                String myName = myItem.getName();

                refresh();

                removeColumnName(myName);
                overlayPrompt.setText(_constants.installFileWizard_RenameColumn_Prompt(myItem.getColumnNumber(),
                                                                            myItem.getFirstValue(), myName));
                overlayTextBox.setInitialValue(myName);
                overlayTextBox.setRejectionMap(_columnNameMap, true);
                overlayTextBox.selectAll();
                overlayTextBox.setFocus(true);

            } catch(Exception myException) {

                Dialog.showException(myException);
            }
        }
    }

    private void buildTopPanel(CsiFileType typeIn, String nameIn, boolean forceIn) {

        HorizontalPanel myInterimPanel = new HorizontalPanel();

        myInterimPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        topPanel.add(myInterimPanel);
    }

    private void buildBottomPanel() {

        InlineLabel mySpacer = new InlineLabel("++");

        mySpacer.getElement().getStyle().setColor("white");

        leftPanel = new HorizontalPanel();
        rightPanel = new HorizontalPanel();

        hideColumns = new CheckBox(_constants.gridWidget_HideColumns());
        hideColumns.addValueChangeHandler(handleHideOrShowColumns);

        leftPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        leftPanel.add(hideColumns);

        bottomPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        bottomPanel.add(leftPanel);

        rightPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        operationLabel = new InlineLabel(_txtSelectOperationLabel);
        operationSelection = buildOperationSelector();
        rightPanel.add(operationLabel);
        rightPanel.add(mySpacer);
        rightPanel.add(operationSelection);

        bottomPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        bottomPanel.add(rightPanel);
    }

    private CsiStringListBox buildOperationSelector() {

        CsiStringListBox mySelector = new CsiStringListBox();

        mySelector.setWidth("130px");
        mySelector.setHeight("24px");
        mySelector.addItem(" ");
        mySelector.addItem(_constants.gridWidget_Rename());
        mySelector.addItem("---------------------------", DisplayMode.COMPONENT);
        mySelector.addItem(_constants.gridWidget_Exclude());
        mySelector.addItem(_constants.gridWidget_Include());
        mySelector.addItem("---------------------------", DisplayMode.COMPONENT);
        for (CsiDataType myType : CsiDataType.values()) {

            String myLabel = _txtChangeTypeMenuSubstring + " " + myType.getLabel();

            if (null != myLabel) {

                mySelector.addItem(myLabel);
            }
        }
        // TODO: Support disabled entries in drop-down

        // TODO: Support disabled entries in drop-down

        // TODO: Support disabled entries in drop-down

        // TODO: Support disabled entries in drop-down

        // TODO: Support disabled entries in drop-down

//        mySelector.getElement().getElementsByTagName("option").getItem(2).setAttribute("disabled", "disabled");
//        mySelector.getElement().getElementsByTagName("option").getItem(5).setAttribute("disabled", "disabled");
        return mySelector;
    }

    private void removeDisplayColumns() {

        ListStore<InstallerColumnDisplay> myStore = grid.getStore();
        List<InstallerColumnDisplay> myDiscards = new ArrayList<InstallerColumnDisplay>(myStore.size());

        backupList();

        for (int i = 0; _gridStore.size() > i; i++) {

            InstallerColumnDisplay myItem = _gridStore.get(i);

            if (!myItem.getInclude()) {

                myDiscards.add(myItem);
            }
        }

        for (int i = 0; myDiscards.size() > i; i++) {

            myStore.remove(myDiscards.get(i));
        }
    }
    private void restoreDisplayColumns() {

        restoreList();
    }

    private void backupList() {

        for (int i = 0; _gridStore.size() > i; i++) {

            InstallerColumnDisplay myItem = _gridStore.get(i);

            _backupList.set(myItem.getColumnNumber() - 1, myItem);
        }
    }

    private void createUndo() {

        for (int i = 0; _gridStore.size() > i; i++) {

            InstallerColumnDisplay myItem = _gridStore.get(i);

            _backupList.set(myItem.getColumnNumber() - 1, myItem);
        }
    }

    private void restoreList() {

        boolean myValidFlag = true;
        List<InstallerColumnDisplay> mySelection = (null != grid) ? grid.getSelectionModel().getSelection() : null;

        _gridStore.clear();
        _gridMap.clear();
        _columnNameMap.clear();

        for (int i = 0; _backupList.size() > i; i++) {

            InstallerColumnDisplay myItem = _backupList.get(i);

            if (myItem.getInclude()) {

                if (!addColumnName(myItem.getName())) {

                    myValidFlag = false;
                }
            }

            _gridStore.add(myItem);
            _gridMap.put(myItem.getColumnNumber(), i);
            setNamesValidFlag(myValidFlag);
        }
        if (null != mySelection) {

            grid.getSelectionModel().setSelection(mySelection);
        }
    }

    private void enterOverlayMode() {

        _inOverlayMode = true;
        disableDialog();
        getPanel().enterOverlayMode();
    }

    private void exitOverlayMode() {

        enableDialog();
        getPanel().exitOverlayMode();
        _inOverlayMode = false;
    }

    private void disableDialog() {

        if ((null != hideColumns) && hideColumns.isVisible()) hideColumns.setEnabled(false);
        enableOperationSelection(false);
    }

    private void enableDialog() {

        if ((null != hideColumns) && hideColumns.isVisible()) hideColumns.setEnabled(true);
        enableOperationSelection(true);
    }

    private void validateNames() {

        if (null != _columnNameMap) {

            boolean myValidFlag = true;

            _columnNameMap.clear();

            for (int i = 0; _gridStore.size() > i; i++) {

                InstallerColumnDisplay myItem = _gridStore.get(i);

				myItem.setMode(DisplayMode.NORMAL);

                if (myItem.getInclude()) {

                    if (!addColumnName(myItem.getName())) {

                        myValidFlag = false;

                        mark(i);
                    }
                }
            }
            setNamesValidFlag(myValidFlag);
        }
    }

    private void mark(int limitIn) {

        InstallerColumnDisplay myTargetItem = _gridStore.get(limitIn);
        String myTargetName = myTargetItem.getName();

        myTargetItem.setMode(DisplayMode.ERROR);

        for (int i = 0; limitIn > i; i++) {

            InstallerColumnDisplay myItem = _gridStore.get(i);

            if (myTargetName.equals(myItem.getName())) {

                if (myItem.getInclude()) {

                    myItem.setMode(DisplayMode.ERROR);
                }
            }
        }
    }

    private void removeColumnName(String nameIn) {

        if ((null != nameIn) && (0 < nameIn.length())) {

            Integer myCount = _columnNameMap.get(nameIn);

            if (null != myCount) {

                if (1 < myCount) {

                    _columnNameMap.put(nameIn, myCount - 1);

                } else {

                    _columnNameMap.remove(nameIn);
                }
            }
        }
    }

    private boolean addColumnName(String nameIn) {

        boolean myValidFlag = false;

        if ((null != nameIn) && (0 < nameIn.length())) {

            Integer myCount = _columnNameMap.get(nameIn);

            if (null != myCount) {

                _columnNameMap.put(nameIn, myCount + 1);

            } else {

                _columnNameMap.put(nameIn, 1);
                myValidFlag = true;
            }
        }
        return myValidFlag;
    }

    private void setNamesValidFlag(boolean isValidIn) {

        if (isValidIn) {

            clearAlert();

        } else {

            setAlert(_constants.gridWidget_Column_Naming_Error(), Dialog.txtErrorColor);
        }
        _namesValidFlag = isValidIn;
    }

    private void renameColumns(final List<InstallerColumnDisplay> columnsIn) {

        if ((null != columnsIn) && (0 < columnsIn.size())) {

            enterOverlayMode();

            _renameIndex = 0;
            overlayPanel = new FullSizeLayoutPanel();
            overlayPanel.setPixelSize(288, 314);
            overlayPanel.getElement().getStyle().setBackgroundColor("transparent");

            overlayPrompt = new DialogInfoTextArea();
            overlayPrompt.setPixelSize(273, 108);
            overlayPanel.add(overlayPrompt);
            overlayPanel.setWidgetLeftRight(overlayPrompt, 15, Style.Unit.PX, 0, Style.Unit.PX);
            overlayPanel.setWidgetTopHeight(overlayPrompt, 0, Style.Unit.PX, 108, Style.Unit.PX);

            overlayError = new Label(_constants.gridWidget_Column_Naming_Error());
            overlayError.getElement().getStyle().setColor(Dialog.txtPanelColor);
            overlayPanel.add(overlayError);
            overlayPanel.setWidgetLeftRight(overlayError, 20, Style.Unit.PX, 0, Style.Unit.PX);
            overlayPanel.setWidgetTopHeight(overlayError, 108, Style.Unit.PX, 22, Style.Unit.PX);

            overlayTextBox = new FilteredTextBox();
            overlayTextBox.setRequired(true);
            overlayTextBox.setColorChangingLabel(overlayError, Dialog.txtPanelColor);
            overlayTextBox.getElement().getStyle().setMarginLeft(10, Style.Unit.PX);
            overlayPanel.add(overlayTextBox);
            overlayPanel.setWidgetLeftRight(overlayTextBox, 10, Style.Unit.PX, 10, Style.Unit.PX);
            overlayPanel.setWidgetTopHeight(overlayTextBox, 130, Style.Unit.PX, 40, Style.Unit.PX);

            overlayInstructions = new DialogInfoTextArea();
            overlayPrompt.setPixelSize(273, 90);
            overlayInstructions.setText(_txtRenameInstructions);
            overlayPanel.add(overlayInstructions);
            overlayPanel.setWidgetLeftRight(overlayInstructions, 15, Style.Unit.PX, 0, Style.Unit.PX);
            overlayPanel.setWidgetTopHeight(overlayInstructions, 170, Style.Unit.PX, 90, Style.Unit.PX);

            dialogOverlay = new InstructionOverlay(overlayPanel, HasVerticalAlignment.ALIGN_MIDDLE);
            dialogOverlay.show();

            DeferredCommand.add(new Command() {
                public void execute() {

                    refreshOverlay(columnsIn);
                }
            });
        }
    }

    private void enableOperationSelection(boolean isOkIn) {

        boolean isEnabled = isOkIn && (null != grid) && (0 < grid.getSelectionModel().getSelection().size());

        if (null != operationSelection) operationSelection.setEnabled(isEnabled);
        if (null != operationLabel) operationLabel.setVisible(isEnabled);
    }
}

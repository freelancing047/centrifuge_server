package csi.client.gwt.csiwizard.widgets;

import java.util.List;

import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.list_boxes.BasicStringListBox;
import csi.client.gwt.widget.list_boxes.CsiDynamicStringListBox;
import csi.client.gwt.widget.list_boxes.CsiOverlayTextBox;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.ui.StringGrid;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.ValuePair;


public class FileParsingWidget extends AbstractInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    StringGrid grid = null;
    Widget titleWidget = null;
//    GridContainer gridPanel = null;
    ScrollPanel gridPanel = null;
    HorizontalPanel dropPanel_1 = null;
    HorizontalPanel dropPanel_2 = null;
    CsiStringListBox columnNames = null;
    CsiStringListBox dataStart = null;
    Label dropLabel[] = null;
    CsiDynamicStringListBox[] dropDown = null;
    boolean[] dropDownRequired = null;
    CsiStringListBox _extraRequired = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtSelectionPrompt = _constants.fileInputWidget_Button();
    private static final String _txtFirstRowLabel = _constants.fileInputWidget_FirstRow();
    private static final int _minColWidth = 100;

    private boolean _monitoring = false;
    private ChoiceMadeEventHandler _callback;
    private int _dropDownCount = 0;
    private String _txtColumnNamesLabel = _constants.fileParsingWidget_ColumnNamesRowNumberLabel();
    private String _txtDataStartLabel = _constants.fileParsingWidget_DataStartRowNumberLabel();
    private String _txtNotApplicable = _constants.tableDetailEditor_NotApplicableAbreviation();
    private String _errorString = _constants.fileParsingWidget_TooManyDropDowns();
    private int _rowCount_1 = 2;
    private int _rowCount_2 = 0;
    private int _displayHeight = 0;
    private int _displayWidth = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SelectionChangedHandler<String> handleHeaderRowChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> event) {

            int myChoice = columnNames.getSelectedIndex();

            reloadDataStart();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FileParsingWidget(Widget titleWidgetIn, int dropCountIn)
            throws CentrifugeException {

        super(true);

        //
        // Initialize the display objects
        //
        initializeObject(titleWidgetIn, dropCountIn);
    }

    public FileParsingWidget(int dropCountIn) throws CentrifugeException {

        this(null,dropCountIn);
    }

    public void displayGrid(List<String[]> dataIn, int columnCountIn) {

        clearGrid();

        if (1 < columnCountIn) {

            int[] myWidth;
            String[] myHeaders = new String[columnCountIn];

            myHeaders[0] = _constants.gridColumnTitle_RowNumber();
            if (2 < columnCountIn) {

                myWidth = new int[] {50, Math.max(_minColWidth, (getWidth() - 70) / (columnCountIn - 1))};

                for (int i = 1; columnCountIn > i; i++) {

                    myHeaders[i] = _constants.gridColumnTitle_FieldDefault(Integer.toString(i));
                }

            } else {

                int myMaxLength = 0;

                for (int i = 0; dataIn.size() > i; i++) {

                    String[] myRow = dataIn.get(i);

                    if ((null != myRow) && (1 < myRow.length)) {

                        String myString = myRow[1];

                        if (null != myString) {

                            myMaxLength = Math.max(myMaxLength, myString.length());
                        }
                    }
                }

                myWidth = new int[] {50, Math.max((getWidth() - 70), (myMaxLength * 6))};

                myHeaders[1] = "Decoded Row Data";
            }

            grid = new StringGrid(dataIn, myHeaders, myWidth);
            gridPanel.add(grid);

        } else {

            if ((null != dataIn) && (0 < dataIn.size())) {

                String[] myData = dataIn.get(0);

                if ((null != myData) && (0 < myData.length)) {


                }
            }
        }
    }

    public void clearGrid() {

        if (null != grid) {

            grid.asWidget().removeFromParent();
            grid = null;
        }
    }

    public void initializeDropDown(int dropDownIn, String promptIn, String[] optionsIn, SelectionChangedHandler handlerIn, boolean requiredIn) {

        if (dropDown.length > dropDownIn) {

            Label myLabel = dropLabel[dropDownIn];
            CsiDynamicStringListBox myDropDown = dropDown[dropDownIn];

            myDropDown.clear();

            if (null != handlerIn) {

                myDropDown.addSelectionChangedHandler(handlerIn);
            }

            myLabel.setText((null != promptIn) ? promptIn : "");
            myDropDown.addAll(optionsIn);
            myDropDown.setSelectedIndex(0);

            if (requiredIn) {

                if (null == dropDownRequired) {

                    dropDownRequired = new boolean[dropDown.length];
                }
                dropDownRequired[dropDownIn] = true;
            }
        }
    }

    public void initializeDropDown(int dropDownIn, String promptIn, List<ValuePair<String, String>> optionsIn,
                                   CsiOverlayTextBox.ValidationMode validationModeIn,
                                   SelectionChangedHandler handlerIn, boolean requiredIn) {

        if (dropDown.length > dropDownIn) {

            Label myLabel = dropLabel[dropDownIn];
            CsiDynamicStringListBox myDropDown = dropDown[dropDownIn];

            myDropDown.clear();

            if (null != handlerIn) {

                myDropDown.addSelectionChangedHandler(handlerIn);
            }

            myLabel.setText((null != promptIn) ? promptIn : "");
            myDropDown.initializeDropdown(validationModeIn, optionsIn);

            if (requiredIn) {

                if (null == dropDownRequired) {

                    dropDownRequired = new boolean[dropDown.length];
                }
                dropDownRequired[dropDownIn] = true;
            }
        }
    }

    public void requireDropDown(CsiStringListBox dropDownIn) {

        _extraRequired = dropDownIn;
    }

    public void resetDropDowns() {

        for (CsiDynamicStringListBox myListBox : dropDown) {

            myListBox.setSelectedIndex(0);
        }
    }

    public void setColumnNames(Integer rowIndexIn) {

        if (null != rowIndexIn) {

            columnNames.setSelectedIndex(rowIndexIn + 1);

        } else {

            columnNames.setSelectedIndex(0);
        }
        reloadDataStart();
    }

    public int getColumnNames() {

        return columnNames.getSelectedIndex();
    }

    public void setDataStart(Integer rowIndexIn) {

        int myChoice = (null != rowIndexIn)
                ? Math.min(Math.max(rowIndexIn - columnNames.getSelectedIndex() - 1, 0), dataStart.getItemCount())
                : 0;

        dataStart.setSelectedIndex(myChoice);
    }

    public int getDataStart() {

        return dataStart.getSelectedIndex() + columnNames.getSelectedIndex() + 1;
    }

    public void setDropDownChoice(int dropDownIn, int choiceIn) {

        if (dropDown.length > dropDownIn) {

            dropDown[dropDownIn].setSelectedIndex(choiceIn);
        }
    }

    public void setDropDownChoice(int dropDownIn, Character choiceIn) {

        String myChoice = (null != choiceIn) ? choiceIn.toString() : null;

        if (dropDown.length > dropDownIn) {

            dropDown[dropDownIn].setSelectedValue(myChoice);
        }
    }

    public void setDropDownChoice(int dropDownIn, String choiceIn) {

        if (dropDown.length > dropDownIn) {

            dropDown[dropDownIn].setSelectedValue(choiceIn);
        }
    }

    public String getDropDownSelection(int dropDownIn) {

        String mySelection = null;

        if (dropDown.length > dropDownIn) {

            mySelection = dropDown[dropDownIn].getSelectedValue();
        }
        return mySelection;
    }

    public Character getDropDownSelectionCharacter(int dropDownIn) {

        String mySelection = getDropDownSelection(dropDownIn);

        return ((null != mySelection) && (0 < mySelection.length())) ? mySelection.charAt(0) : null;
    }

    public int getDropDownSelectedIndex(int dropDownIn) {

        int mySelection = 0;

        if (dropDown.length > dropDownIn) {

            mySelection = dropDown[dropDownIn].getSelectedIndex();
        }
        return mySelection;
    }

    public void enableDropDown(int dropDownIn) {

        if (dropDown.length > dropDownIn) {

            dropDown[dropDownIn].setEnabled(true);
        }
    }

    public void disableDropDown(int dropDownIn) {

        if (dropDown.length > dropDownIn) {

            dropDown[dropDownIn].setEnabled(false);
        }
    }

    public boolean isValid() {

        boolean myValidFlag = true;

        if ((null != _extraRequired) && (1 > _extraRequired.getSelectedIndex())) {

            myValidFlag = false;

        } else {

            if (null != dropDownRequired) {

                for (int i = 0; dropDown.length > i; i++) {

                    if (dropDownRequired.length <= i) {

                        break;

                    } else if (dropDownRequired[i] && (1 > dropDown[i].getSelectedIndex())) {

                        myValidFlag = false;
                        break;
                    }
                }
            }
        }

        return myValidFlag;
    }

    public void grabFocus() {

//        fileSelector.setFocus(true);
    }

    @Override
    public String getText() throws CentrifugeException {

        return null;
    }

    @Override
    public void resetValue() {

//        fileSelector.setText(_default);
//        reportValidity(checkIntegrity(fileSelector.getText()));
    }

    public int getRequiredHeight() {

        return getTitleHeight(0) + getGridPanelHeight() + getDropPanelReserve(0);
    }

    @Override
    public int getRequestedHeight() {

        return getTitleHeight(Dialog.intMargin) + getGridPanelHeight() + getDropPanelReserve(Dialog.intMargin);
    }

    public boolean atReset() {

        return !isValid();
    }

    @Override
    public void suspendMonitoring() {

        _monitoring = false;
    }

    @Override
    public void beginMonitoring() {

        if (! _monitoring) {

            _monitoring = true;
            checkValidity();
        }
    }

    @Override
    public void setValue(String valueIn) {

//        fileSelector.setText(valueIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected void initializeObject(Widget titleWidgetIn, int dropCountIn)
            throws CentrifugeException {

        _validator = null;

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(titleWidgetIn, dropCountIn);

        //
        // Wire in the handlers
        //
        wireInHandlers();
    }

    protected void wireInHandlers() {

        columnNames.addSelectionChangedHandler(handleHeaderRowChange);
    }
/*

    private int getTitleHeight(int marginIn) {

        return (null != titleWidget) ? (Dialog.intTextBoxHeight + marginIn) : 0;
    }

    private int getGridPanelHeight() {

        return Dialog.intTextBoxHeight * 7;
    }

    private int getDropPanelHeight() {

        return Dialog.intLabelHeight + Dialog.intTextBoxHeight * ((null != dropPanel_2) ? 2 : 1);
    }

    private int getButtonPanelHeight(int marginIn) {

        return (null != titleWidget) ? (Dialog.intButtonHeight + marginIn) : 0;
    }

 */
    protected void layoutDisplay() {

        int myTop = 0;
        int myWidth = getWidth();
        int myWidthUnit = (null != dropPanel_2) ? (myWidth - 30) / 5 : (myWidth - 50) / 6;
        int myTitleHeight = getTitleHeight(_margin);
        int myDropPanelHeight = getSingleDropPanelHeight();
        int myDropPanelReserve = getDropPanelReserve(_margin);
        int myGridPanelHeight = getHeight() - (myTitleHeight + myDropPanelReserve);
        String myDropWidth = Integer.toString(2 * myWidthUnit) + "px";
        String mySmallDropWidth = Integer.toString(myWidthUnit) + "px";

        columnNames.setWidth(mySmallDropWidth);
        dataStart.setWidth(mySmallDropWidth);

        if (0 < myTitleHeight) {

            setWidgetTopHeight(titleWidget, myTop, Unit.PX, Dialog.intTextBoxHeight, Unit.PX);
            setWidgetLeftRight(titleWidget, 0, Unit.PX, 0, Unit.PX);
            myTop += myTitleHeight;
        }

        gridPanel.setHeight(Integer.toString(myGridPanelHeight) + "px");
        setWidgetTopHeight(gridPanel, myTop, Unit.PX, myGridPanelHeight, Unit.PX);
        setWidgetLeftRight(gridPanel, 0, Unit.PX, 0, Unit.PX);
        myTop += (myGridPanelHeight + _margin);

        setWidgetTopHeight(dropPanel_1, myTop, Unit.PX, myDropPanelHeight, Unit.PX);
        setWidgetLeftRight(dropPanel_1, 0, Unit.PX, 0, Unit.PX);
        myTop += (myDropPanelHeight + _margin);

        if (null != dropPanel_2) {

            setWidgetTopHeight(dropPanel_2, myTop, Unit.PX, myDropPanelHeight, Unit.PX);
            setWidgetLeftRight(dropPanel_2, 0, Unit.PX, 0, Unit.PX);
        }

        for (int i = 0; _dropDownCount > i; i++) {

            CsiDynamicStringListBox myDropDown = dropDown[i];

            if (null != myDropDown) {

                myDropDown.setWidth(myDropWidth);
            }
        }
    }

    protected boolean checkIntegrity() {

        return true;
    }

    protected boolean hideIntegrityCheckBox() {

        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void createWidgets(Widget titleWidgetIn, int dropCountIn)
            throws CentrifugeException{

        titleWidget = titleWidgetIn;
        _rowCount_1 = (2 < dropCountIn) ? dropCountIn / 2 : dropCountIn;
        _rowCount_2 = dropCountIn - _rowCount_1;

        setMarginCount((0 < _rowCount_2) ? ((null != titleWidget) ? 3 : 2) : ((null != titleWidget) ? 2 : 1));

        if (3 > _rowCount_2) {

            _dropDownCount = dropCountIn;

            dropLabel = new Label[_dropDownCount];
            dropDown = new CsiDynamicStringListBox[_dropDownCount];
            dropPanel_1 = new HorizontalPanel();

            dropPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
            addColumnNamesDropBox(dropPanel_1);

            if (0 < _rowCount_2) {

                dropPanel_2 = new HorizontalPanel();
                dropPanel_2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
                addDataStartDropBox(dropPanel_2, false);

                for (int i = _rowCount_1; _dropDownCount > i; i++) {

                    addUserDropPanel(dropPanel_2, i);
                }
                add(dropPanel_2);

            } else {

                addDataStartDropBox(dropPanel_1, true);
            }

            for (int i = 0; _rowCount_1 > i; i++) {

                addUserDropPanel(dropPanel_1, i);
            }
            add(dropPanel_1);
//            gridPanel = new GridContainer();
            gridPanel = new ScrollPanel();
            add(gridPanel);
            if (null != titleWidgetIn) {

                add(titleWidgetIn);
            }

        } else {

            throw new CentrifugeException(_errorString);
        }
    }

    private void addUserDropPanel(HorizontalPanel panelIn, int indexIn) {

        dropLabel[indexIn] = new Label(".");
        dropDown[indexIn] = new CsiDynamicStringListBox();

        addDropPanel(panelIn, dropLabel[indexIn], dropDown[indexIn], true);
    }

    private void addColumnNamesDropBox(HorizontalPanel panelIn) {

        columnNames = new CsiStringListBox();

        columnNames.addItem(_txtNotApplicable);

        for (int i = 1; 6 > i; i++) {

            columnNames.addItem(Integer.toString(i));
        }
        columnNames.setSelectedIndex(0);

        addDropPanel(panelIn, new Label(_txtColumnNamesLabel), columnNames, false);
    }

    private void addDataStartDropBox(HorizontalPanel panelIn, boolean addSpacerIn) {

        dataStart = new CsiStringListBox();

        for (int i = 1; 7 > i; i++) {

            dataStart.addItem(Integer.toString(i));
        }
        dataStart.setSelectedIndex(0);

        addDropPanel(panelIn, new Label(_txtDataStartLabel), dataStart, addSpacerIn);
    }

    private void addDropPanel(HorizontalPanel panelIn, Label labelIn, BasicStringListBox listBoxIn, boolean addSpacerIn) {

        VerticalPanel myPanel = new VerticalPanel();

        myPanel.add(labelIn);
        myPanel.add(listBoxIn.getWidget());

        if (addSpacerIn) {

            InlineLabel mySpacer = new InlineLabel(_constants.plusplus());
            mySpacer.getElement().getStyle().setColor(Dialog.txtDefaultBackground);

            panelIn.add(mySpacer);
        }
        panelIn.add(myPanel);
    }

    private void reloadDataStart() {

        String myValue = dataStart.getSelectedValue();
        int mySelection = (null != myValue) ? Integer.parseInt(myValue) : 0;
        int myBase = columnNames.getSelectedIndex() + 1;
        int myTop = myBase + 6;

        dataStart.clear();

        for (int i = myBase; myBase + 6 > i; i++) {

            dataStart.addItem(Integer.toString(i));
        }
        mySelection = Math.min((myTop - 1), Math.max(0, (mySelection - myBase)));

        dataStart.setSelectedIndex(mySelection);
    }

    private void checkValidity() {

        reportValidity();

        if (_monitoring ) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }

    private int getTitleHeight(int marginIn) {

        return (null != titleWidget) ? (Dialog.intTextBoxHeight + marginIn) : 0;
    }

    private int getGridPanelHeight() {

        return Dialog.intTextBoxHeight * ((null != dropPanel_2) ? 5 : 6);
    }

    private int getDropPanelReserve(int marginIn) {

        return (getSingleDropPanelHeight() + _margin) * ((null != dropPanel_2) ? 2 : 1);
    }

    private int getSingleDropPanelHeight() {

        return Dialog.intLabelHeight + Dialog.intTextBoxHeight;
    }
}

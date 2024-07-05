package csi.client.gwt.csiwizard.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vectomatic.file.File;
import org.vectomatic.file.FileList;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.SimpleButton;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.misc.ScrollingString;
import csi.client.gwt.widget.ui.uploader.wizards.components.CsiFileSelector;
import csi.server.common.exception.CentrifugeException;


public class FileInputWidget extends AbstractInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    VerticalPanel verticalPanel = null;
    HorizontalPanel horizontalPanel_1 = null;
    HorizontalPanel horizontalPanel_2 = null;
    HorizontalPanel horizontalPanel_3 = null;
    TextBox fileName = null;
    SimpleButton button = null;
    ScrollingString sampleData = null;
    Label dropLabel[] = null;
    CsiStringListBox[] dropDown = null;
    boolean[] dropDownRequired = null;

    CsiFileSelector fileSelector = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtSelectionPrompt = _constants.fileInputWidget_Button();
    private static final String _txtFirstRowLabel = _constants.fileInputWidget_FirstRow();

    List<File> _fileList = null;
    private boolean _monitoring = false;
    private ChoiceMadeEventHandler _callback;
    private int _dropDownCount = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle selection of a file
    //
    private ChangeHandler handleFileSelectionComplete
            = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {

            FileList myFiles = fileSelector.getFiles();
            Iterator<File> myIterator = myFiles.iterator();

            if (myIterator.hasNext()) {

                _fileList = new ArrayList<File>();

                while (myIterator.hasNext()) {

                    _fileList.add(myIterator.next());
                }
                fileName.setText(_fileList.get(0).getName());
            }
            if (null != _callback) {

                _callback.onChoiceMade(new ChoiceMadeEvent(0));
            }
        }
    };

    //
    // Handle "Select File" button click
    //
    private ClickHandler handleSelectButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            if (null != fileSelector) {

                fileName.setText("");
                _fileList = null;
                sampleData.asWidget().setVisible(false);
                horizontalPanel_2.setVisible(false);
                horizontalPanel_3.setVisible(false);

                fileSelector.activate();
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FileInputWidget(boolean isMultipleIn, int dropCountIn, ChoiceMadeEventHandler callbackIn, boolean isVisible)
            throws CentrifugeException {

        super(true);


        _callback = callbackIn;

        //
        // Initialize the display objects
        //
        initializeObject(isMultipleIn, dropCountIn, isVisible);
    }

    public void setSampleData(String sampleDataIn) {

        sampleData.asWidget().setVisible(true);
        horizontalPanel_2.setVisible(true);
        horizontalPanel_3.setVisible(false);
        sampleData.setText(sampleDataIn);
    }

    public void setSampleColor(int indexIn, String colorIn) {

        sampleData.setColor(indexIn, colorIn);
    }

    public void setSampleColors(String[] colorsIn) {

        sampleData.setColors(colorsIn);
    }

    public void initializeDropDown(int dropDownIn, String promptIn, String[] optionsIn, SelectionChangedHandler<String> handlerIn, boolean requiredIn) {

        if (dropDown.length > dropDownIn) {

            Label myLabel = dropLabel[dropDownIn];
            CsiStringListBox myDropDown = dropDown[dropDownIn];

            myDropDown.clear();

            if (null != handlerIn) {

                myDropDown.addSelectionChangedHandler(handlerIn);
            }

            myLabel.setText((null != promptIn) ? promptIn : "");
            for (int i = 0; optionsIn.length > i; i++) {

                myDropDown.addItem(optionsIn[i]);
            }

            if (requiredIn) {

                if (null == dropDownRequired) {

                    dropDownRequired = new boolean[dropDown.length];
                }
                dropDownRequired[dropDownIn] = true;
            }
        }
    }

    public void resetDropDowns() {

        for (CsiStringListBox myListBox : dropDown) {

            myListBox.setSelectedIndex(0);
        }
    }

    public void setDropDownChoice(int dropDownIn, int choiceIn) {

        if (dropDown.length > dropDownIn) {

            sampleData.asWidget().setVisible(true);
            horizontalPanel_2.setVisible(true);
            horizontalPanel_3.setVisible(true);
            dropDown[dropDownIn].setSelectedIndex(choiceIn);
        }
    }

    public void setDropDownChoice(int dropDownIn, String choiceIn) {

        if (dropDown.length > dropDownIn) {

            sampleData.asWidget().setVisible(true);
            horizontalPanel_2.setVisible(true);
            horizontalPanel_3.setVisible(true);
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

    public File getFile() {

        return ((null != _fileList) && (0 < _fileList.size())) ? _fileList.get(0) : null;
    }

    public List<File> getList() {

        return _fileList;
    }

    public boolean isValid() {

        boolean myValidFlag = false;

        if ((null != _fileList) && (0 < _fileList.size())) {

            myValidFlag = true;

            if (null != dropDownRequired) {

                for (int i = 0; dropDown.length > i; i++) {

                    if (dropDownRequired.length <= i) {

                        break;

                    } else if (dropDownRequired[i] && (0 == dropDown[i].getSelectedIndex())) {

                        myValidFlag = false;
                        break;
                    }
                }
            }
        }

        return myValidFlag;
    }

    public boolean hasColumnNames() {

        return (null != sampleData) ? sampleData.hasColumnNames() : false;
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

        return (3 * (Dialog.intTextBoxHeight + Dialog.intMargin)) + Dialog.intScollingStringHeight;
    }

    @Override
    public int getRequestedHeight() {

        return (3 * (Dialog.intTextBoxHeight + Dialog.intMargin)) + Dialog.intScollingStringHeight + Dialog.intLabelHeight;
    }

    public void activate() {

        fileSelector.activate();
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
    protected void initializeObject(boolean isMultipleIn, int dropCountIn, boolean isVisibleIn)
            throws CentrifugeException {

        _validator = null;

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(isMultipleIn, dropCountIn, isVisibleIn);

        //
        // Wire in the handlers
        //
        wireInHandlers();
    }

    protected void wireInHandlers() {

        button.addClickHandler(handleSelectButtonClick);
        fileSelector.addChangeHandler(handleFileSelectionComplete);
    }

    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myHeight = getHeight();
        String myDropWidth = Integer.toString((myWidth - 30) / 3) + "px";

        verticalPanel.setPixelSize(myWidth, myHeight);
        setWidgetTopBottom(verticalPanel, 0, Unit.PX, 0, Unit.PX);
        setWidgetLeftRight(verticalPanel, 0, Unit.PX, 0, Unit.PX);

        sampleData.setWidth(Integer.toString(myWidth - 4) + "px");
        fileSelector.setWidth(Integer.toString(myWidth - 14) + "px"); //$NON-NLS-1$

        for (int i = 0; _dropDownCount > i; i++) {

            dropDown[i].setWidth(myDropWidth);
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

    private void createWidgets(boolean isMultipleIn, int dropCountIn, boolean isVisibleIn)
            throws CentrifugeException{

        int myCount_1 = (4 == dropCountIn) ? 2 : Math.min(3, dropCountIn);
        int myCount_2 = dropCountIn - myCount_1;

        if (4 > myCount_2) {

            InlineLabel mySpacer1 = new InlineLabel(_constants.plusplus());

            _dropDownCount = dropCountIn;

            dropLabel = new Label[_dropDownCount];
            dropDown = new CsiStringListBox[_dropDownCount];
            verticalPanel = new VerticalPanel();
            horizontalPanel_1 = new HorizontalPanel();
            horizontalPanel_2 = new HorizontalPanel();
            horizontalPanel_3 = new HorizontalPanel();
            fileName = new TextBox();
            button = new SimpleButton(_txtSelectionPrompt);
            sampleData = new ScrollingString("", _txtFirstRowLabel);
            sampleData.displayUsage();
            fileSelector = new CsiFileSelector(isMultipleIn, isVisibleIn);

            mySpacer1.getElement().getStyle().setColor(Dialog.txtDefaultBackground);

            horizontalPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
            horizontalPanel_1.add(fileName);
            horizontalPanel_1.add(mySpacer1);
            horizontalPanel_1.add(button);

            verticalPanel.setSpacing(Dialog.intMargin);
            verticalPanel.add(horizontalPanel_1);
            verticalPanel.add(sampleData);

            if (0 < myCount_1) {

                horizontalPanel_2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

                for (int i = 0; myCount_1 > i; i++) {

                    VerticalPanel myPanel = new VerticalPanel();

                    dropLabel[i] = new Label();
                    dropDown[i] = new CsiStringListBox();

                    if (0 != i) {

                        InlineLabel mySpacer2 = new InlineLabel(_constants.plusplus());
                        mySpacer2.getElement().getStyle().setColor(Dialog.txtDefaultBackground);

                        horizontalPanel_2.add(mySpacer2);
                    }
                    myPanel.add(dropLabel[i]);
                    myPanel.add(dropDown[i]);
                    horizontalPanel_2.add(myPanel);
                }
                verticalPanel.add(horizontalPanel_2);

                if (0 < myCount_2) {

                    Label mySpacer3 = new Label(_constants.plusplus());
                    mySpacer3.getElement().getStyle().setColor(Dialog.txtDefaultBackground);

                    verticalPanel.add(mySpacer3);

                    horizontalPanel_3.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

                    for (int i = myCount_1; _dropDownCount > i; i++) {

                        VerticalPanel myPanel = new VerticalPanel();

                        dropLabel[i] = new Label();
                        dropDown[i] = new CsiStringListBox();

                        if (myCount_1 != i) {

                            InlineLabel mySpacer4 = new InlineLabel(_constants.plusplus());
                            mySpacer4.getElement().getStyle().setColor(Dialog.txtDefaultBackground);

                            horizontalPanel_3.add(mySpacer4);
                        }
                        myPanel.add(dropLabel[i]);
                        myPanel.add(dropDown[i]);
                        horizontalPanel_3.add(myPanel);
                    }
                    verticalPanel.add(horizontalPanel_3);
                }
            }
            verticalPanel.add(fileSelector);

            add(verticalPanel);

            sampleData.asWidget().setVisible(false);
            horizontalPanel_2.setVisible(false);
            horizontalPanel_3.setVisible(false);
            fileName.setEnabled(false);

        } else {

            throw new CentrifugeException("Too many drop down boxes requested!");
        }
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
}

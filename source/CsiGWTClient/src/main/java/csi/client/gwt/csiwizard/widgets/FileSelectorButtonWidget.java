package csi.client.gwt.csiwizard.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vectomatic.file.File;
import org.vectomatic.file.FileList;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.SimpleButton;
import csi.client.gwt.widget.ui.uploader.wizards.components.CsiFileSelector;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 10/26/2015.
 */
public class FileSelectorButtonWidget extends AbstractInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    VerticalPanel radioPanel = null;
    TextBox fileName = null;
    SimpleButton button = null;
    AbstractInputWidget finalizingWidget = null;
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

            resetValue();

            if (null != fileSelector) {

                fileSelector.activate();
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FileSelectorButtonWidget(ChoiceMadeEventHandler callbackIn, boolean isMultipleIn, boolean isVisibleIn)
            throws CentrifugeException {

        super(true);

        _callback = callbackIn;

        //
        // Initialize the display objects
        //
        initializeObject(isMultipleIn, isVisibleIn);
    }

    public FileSelectorButtonWidget(boolean isMultipleIn, boolean isVisibleIn)
            throws CentrifugeException {

        super(true);

        //
        // Initialize the display objects
        //
        initializeObject(isMultipleIn, isVisibleIn);
    }

    public FileSelectorButtonWidget(ChoiceMadeEventHandler callbackIn)
            throws CentrifugeException {

        super(true);

        _callback = callbackIn;

        //
        // Initialize the display objects
        //
        initializeObject(false, true);
    }

    public FileSelectorButtonWidget() throws CentrifugeException {

        super(true);

        //
        // Initialize the display objects
        //
        initializeObject(false, true);
    }

    public void addSelectionHandler(ChoiceMadeEventHandler callbackIn) {

        _callback = callbackIn;
    }

    public File getFile() {

        return ((null != _fileList) && (0 < _fileList.size())) ? _fileList.get(0) : null;
    }

    public List<File> getList() {

        return _fileList;
    }

    public boolean isValid() {

        return (null != getFile());
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

        fileName.setText(null);
        _fileList = null;
        radioPanel.setVisible(false);
    }

    public int getRequiredHeight() {

        return (3 * (Dialog.intTextBoxHeight + Dialog.intMargin)) + Dialog.intScollingStringHeight;
    }

    @Override
    public int getRequestedHeight() {

        return (3 * (Dialog.intTextBoxHeight + Dialog.intMargin)) + Dialog.intScollingStringHeight + Dialog.intLabelHeight;
    }

    public boolean atReset() {

        return !isValid();
    }

    public void activate() {

        fileSelector.activate();
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

    public void replaceFinalizingWidget(AbstractInputWidget widgetIn) {

        if (null != finalizingWidget) {

            finalizingWidget.removeFromParent();
        }
        finalizingWidget = widgetIn;
        if (null != finalizingWidget) {

            add(finalizingWidget);
        }
        layoutDisplay();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected void initializeObject(boolean isMultipleIn, boolean isVisibleIn) {

        _validator = null;

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(isMultipleIn);

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
        int mySelectorHeight = Dialog.intMargin;
        int myPanelHeight = Dialog.intTextBoxHeight;
        int myTop = 0;

        setWidgetTopHeight(button, myTop, Style.Unit.PX, myPanelHeight, Style.Unit.PX);
        setWidgetLeftWidth(button, 0, Style.Unit.PX, 80, Style.Unit.PX);

        myTop += myPanelHeight;
        myTop += mySelectorHeight;
        setWidgetTopHeight(fileName, myTop, Style.Unit.PX, myPanelHeight, Style.Unit.PX);
        setWidgetLeftRight(fileName, 0, Style.Unit.PX, 0, Style.Unit.PX);

        fileName.setWidth(Integer.toString(myWidth - 14) + "px");

        myTop += myPanelHeight;
        setWidgetTopHeight(fileSelector, myTop, Style.Unit.PX, mySelectorHeight, Style.Unit.PX);
        setWidgetLeftRight(fileSelector, 0, Style.Unit.PX, 0, Style.Unit.PX);

        myTop += mySelectorHeight;
        if (null != finalizingWidget) {

            setWidgetTopBottom(finalizingWidget, myTop, Style.Unit.PX, 0, Style.Unit.PX);
            setWidgetLeftRight(finalizingWidget, 0, Style.Unit.PX, 0, Style.Unit.PX);

            finalizingWidget.setPixelSize(myWidth, myHeight - myTop);
            finalizingWidget.layoutDisplay();
        }
        fileSelector.setWidth(Integer.toString(myWidth - 14) + "px"); //$NON-NLS-1$
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

    private void createWidgets(boolean isMultipleIn) {

        radioPanel = new VerticalPanel();
        fileName = new TextBox();
        button = new SimpleButton(_txtSelectionPrompt);
        fileSelector = new CsiFileSelector(isMultipleIn, false);

        add(fileSelector);
        add(button);
        add(fileName);
        fileName.setEnabled(false);
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

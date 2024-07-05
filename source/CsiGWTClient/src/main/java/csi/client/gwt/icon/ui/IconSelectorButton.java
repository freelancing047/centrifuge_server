package csi.client.gwt.icon.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vectomatic.file.File;
import org.vectomatic.file.FileList;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;

import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.uploader.wizards.components.CsiFileSelector;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 10/26/2015.
 */
public class IconSelectorButton {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    HorizontalPanel selectorPanel = null;
    String fileName;
    Button button = null;
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
                fileName = (_fileList.get(0).getName());
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

                fileSelector.activate();
            }
        }
    };


   
    public IconSelectorButton() throws CentrifugeException {


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

    

    public boolean atReset() {

        return isEmpty();
    }

    public void activate() {

        fileSelector.activate();
    }

    public boolean isEmpty() {

        return !isValid();
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
        selectorPanel = new HorizontalPanel();
        
        button = new Button(_constants.iconPanelAddButton());
        button.setIcon(IconType.PLUS);
        button.setHeight("25px");
        button.setType(ButtonType.LINK);
        fileSelector = new CsiFileSelector(false, false);
        //InputElement element = fileSelector.getElement().cast();
        //element.setAccept(".gif,.jpg,.jpeg,.png");

        selectorPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        selectorPanel.add(button);

        selectorPanel.add(fileSelector);
    }

    public void cancelProgressDialog() {
        // TODO Auto-generated method stub
        
    }
    
    public Panel getView(){
        return selectorPanel;
    }

    public Button getButton() {
        // TODO Auto-generated method stub
        return button;
    }

}

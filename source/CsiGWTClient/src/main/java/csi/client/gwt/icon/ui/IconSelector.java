package csi.client.gwt.icon.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vectomatic.file.File;
import org.vectomatic.file.FileList;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;

import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.uploader.wizards.components.CsiFileSelector;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 10/26/2015.
 */
public class IconSelector {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    AbsolutePanel selectorPanel = null;
    String fileName;
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
    private boolean enabled = true;


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


            if (null != fileSelector && enabled) {

                fileSelector.activate();
            }
        }
    };


   
    public IconSelector() throws CentrifugeException {


        //
        // Initialize the display objects
        //
        initializeObject(false, true);
    }

    public void addSelectionHandler(ChoiceMadeEventHandler callbackIn) {

        if(_callback == null)
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

        //selectorPanel.addHandler(handleSelectButtonClick, ClickEvent.getType());
        selectorPanel.addDomHandler(handleSelectButtonClick, ClickEvent.getType());
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
        selectorPanel = new AbsolutePanel();
        
        
        fileSelector = new CsiFileSelector(false, false);
        //InputElement element = fileSelector.getElement().cast();
        //element.setAccept(".gif,.jpg,.jpeg,.png");

        selectorPanel.add(fileSelector);
    }

    public void cancelProgressDialog() {
        // TODO Auto-generated method stub
        
    }
    
    public AbsolutePanel getView(){
        return selectorPanel;
    }

    public ClickHandler getHandleSelectButtonClick() {
        return handleSelectButtonClick;
    }

    public void setHandleSelectButtonClick(ClickHandler handleSelectButtonClick) {
        this.handleSelectButtonClick = handleSelectButtonClick;
    }

    public void disable() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
    }


}

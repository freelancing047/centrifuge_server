package csi.client.gwt.csiwizard.panels;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.LayoutPanel;

import csi.client.gwt.csiwizard.Wizard;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.events.CarriageReturnEvent;
import csi.client.gwt.events.CarriageReturnEventHandler;
import csi.client.gwt.events.EscapeKeyEvent;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;


////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                          Basic text entry panel for CsiWizard                          //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

public abstract class AbstractWizardPanel extends LayoutPanel  {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected WatchBoxInterface watchBox = null;
    protected CanBeShownParent parentDialog;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    protected final int _buttonWidth = 100;

    protected int _height = 335;
    protected int _width = 480;
    private boolean _processingRequest = false;

    private Wizard _wizard;
    private HandlerManager _handlerManager;
    private boolean _required = true;
    private String _name = null;
    private boolean _overlayMode = false;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public abstract String getText() throws CentrifugeException;
    public abstract void grabFocus();
    public abstract void destroy();
    public abstract void enableInput();

    protected abstract void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn);
    protected abstract void layoutDisplay() throws CentrifugeException;
    protected abstract void wireInHandlers();

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AbstractWizardPanel() {

        initializeValues(null, null, true);
    }

    public AbstractWizardPanel(CanBeShownParent parentDialogIn) {
        
        initializeValues(parentDialogIn, null, true);
    }
    
    public AbstractWizardPanel(CanBeShownParent parentDialogIn, boolean requiredIn) {
        
        initializeValues(parentDialogIn, null, requiredIn);
    }
    
    public AbstractWizardPanel(CanBeShownParent parentDialogIn, String nameIn) {
        
        initializeValues(parentDialogIn, nameIn, true);
    }
    
    public AbstractWizardPanel(CanBeShownParent parentDialogIn, String nameIn, boolean requiredIn) {
        
        initializeValues(parentDialogIn, nameIn, requiredIn);
    }

    public AbstractWizardPanel releasePanel() {

        parentDialog = null;
        return null;
    }

    public void setParentDialog(CanBeShownParent parentDialogIn) {

        parentDialog = parentDialogIn;
        if (null != parentDialog) {

            WatchBoxInterface myWatchBox = parentDialogIn.getWatchBox();

            if (null != myWatchBox) {

                watchBox = myWatchBox;
            }
        }
    }

    public CanBeShownParent getParentDialog() {

        return parentDialog;
    }

    public HandlerManager getEventManager() {
        
        return _handlerManager;
    }
    
    public String getPanelName() {
        
        return _name;
    }

    public boolean isRequired() {

        return _required;
    }

    public void setRequired(boolean requiredIn) {

        _required = requiredIn;
    }

    public boolean isOkToLeave() {
        
        return !isRequired();
    }

    public void setWizard(Wizard wizardIn) {

        _wizard = wizardIn;
    }

    public void enterOverlayMode() {

        _overlayMode = true;
        _wizard.hideInstructions();
        _wizard.disableControlButtons();
    }

    public void exitOverlayMode() {

        try {

            _overlayMode = false;
            _wizard.enableControlButtons();
            _wizard.showInstructions();

        } catch(Exception myException) {

            Dialog.showException(this.getClass().getSimpleName(), 1, myException);
        }
    }

    public void suspendMonitoring() {

    }

    public void resumeMonitoring() {

    }

    public void beginMonitoring() {
        
        fireEvent(new ValidityReportEvent(isOkToLeave()));
    }

    public HandlerRegistration addValidityReportEventHandler(
            ValidityReportEventHandler handler) {
        HandlerRegistration myRegistration =  _handlerManager.addHandler(ValidityReportEvent.type, handler);
        fireEvent(new ValidityReportEvent(isOkToLeave()));
        return myRegistration;
    }

    public HandlerRegistration addCarriageReturnEventHandler(
            CarriageReturnEventHandler handler) {
        return _handlerManager.addHandler(CarriageReturnEvent.type, handler);
    }

    public void handleCarriageReturn() {

        fireEvent(new CarriageReturnEvent(isOkToLeave()));
    }

    public void handleEscapeKey() {

        fireEvent(new EscapeKeyEvent());
    }

    public int getPanelWidth() {

        return _width;
    }

    //
    // Fire requested event
    //
    @Override
    public void fireEvent(GwtEvent<?> eventIn) {
        _handlerManager.fireEvent(eventIn);
    }

    @Override
    public void setHeight(String stringIn) {
        try{
            if (_processingRequest) {

                super.setHeight(stringIn);
            } else {

                forceDimensions(_width, Dialog.decode(stringIn));
            }
        } catch (Exception myException) {

            Dialog.showException(this.getClass().getSimpleName(), 2, myException);
        }
    }
    
    @Override
    public void setWidth(String stringIn) {
        try{
            if (_processingRequest) {

                super.setWidth(stringIn);
            } else {

                forceDimensions(Dialog.decode(stringIn), _height);
            }
        } catch (Exception myException) {

            Dialog.showException(this.getClass().getSimpleName(), 3, myException);
        }
    }
    
    @Override
    public void onResize() {
        // IG: remove this, because we were grabbing the parent size and setting the size here based on the parent,
        //  vs the one needed.
        /*
            Widget myParent = this.getParent();
            int myWidth = myParent.getOffsetWidth();
            int myHeight = myParent.getOffsetHeight();
         */

        forceDimensions(_width, _height);
    }
    
    @Override
    public void setPixelSize(int widthIn, int heightIn) {
        
        if (_processingRequest) {
            
            super.setPixelSize(widthIn, heightIn);
        } else {
            
            forceDimensions(widthIn, heightIn);
        }
    }

    public void clearAlert() {

        if (null != _wizard) {

            _wizard.clearAlert();
        }
    }

    public void setAlert(String textIn, String colorIn) {

        if (null != _wizard) {

            _wizard.setAlert(textIn, colorIn);
        }
    }

    public AbstractInputWidget getInputWidget() {

        return null;
    }

    public void suspendParentMonitoring() {

    }

    public void resumeParentMonitoring() {

    }

    public String getPanelTitle() {

        return null;
    }

    public String getDialogTitle() {

        return null;
    }

    public String getInstructions(String buttonIn) {

        return null;
    }

    public void showWatchBox(String titleIn, String messageIn) {

        watchBox.show(titleIn, messageIn);
    }

    public void showWatchBox(String messageIn) {

        watchBox.show(messageIn);
    }

    public void showWatchBox() {

        watchBox.show();
    }

    public void hideWatchBox() {

        watchBox.hide();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected void initializeObject() throws CentrifugeException {
        
        initializeObject(null, null);
    }
    
    //
    //
    //
    protected void initializeObject(String descriptionIn) throws CentrifugeException {
        
        initializeObject(descriptionIn, null);
    }
    
    //
    //
    //
    protected void initializeObject(String descriptionIn, AbstractInputWidget inputCellIn) throws CentrifugeException {
    
        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(descriptionIn, inputCellIn);

        //
        // Size the display
        //
        setPixelSize(_width, _height);

        //
        // Place the widgets on the panel
        //
        resizeDisplay();
        
        //
        // Wire in the handlers
        //
        wireInHandlers();
    }

    protected boolean inOverlayMode() {

        return _overlayMode;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    private void initializeValues(CanBeShownParent parentDialogIn, String nameIn, boolean requiredIn) {

        parentDialog = parentDialogIn;
        watchBox = (null != parentDialog) ? parentDialog.getWatchBox() : WatchBox.getInstance();
        _handlerManager = new HandlerManager(this);
        _name = nameIn;
        _required = requiredIn;
    }
    
    private void forceDimensions(int widthIn, int heightIn) {

        _processingRequest = true;
        _width  = widthIn;
        _height = heightIn;
        super.setPixelSize(_width, _height);
        _processingRequest = false;
        
        resizeDisplay();
    }
    
    private void resizeDisplay() {
        
        if ((0 < _width) && (0 < _height)) {
            
            try {

                layoutDisplay();

            } catch (Exception myException) {
                
                Dialog.showException(this.getClass().getSimpleName(), 4, myException);
            }
        }
    }
}

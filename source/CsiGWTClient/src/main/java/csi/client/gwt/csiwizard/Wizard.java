package csi.client.gwt.csiwizard;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.events.CarriageReturnEvent;
import csi.client.gwt.events.CarriageReturnEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.*;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;


public abstract class Wizard extends WatchBoxSource implements WizardInterface {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface DataCreationWizardUiBinder extends UiBinder<IsWidget, Wizard> {
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @UiField
    protected Dialog dialog;
        
    @UiField
    FullSizeLayoutPanel controlPanel;
    @UiField
    DialogInfoTextArea instructionTextArea;

    private Button finalButton;
    private Button cancelButton;
    private Button previousButton;
    private Button nextButton;

    private WizardInterface priorDialog = null;

    protected Label wizardAlert;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static DataCreationWizardUiBinder uiBinder = GWT.create(DataCreationWizardUiBinder.class);

    protected boolean _chainForward = false;
    protected int _activePanelIndex = -1;
    protected int _finalDisplayIndex = -1;
    protected AbstractWizardPanel _activePanel = null;
    protected Integer _buttonWidth = Dialog.intWizardButtonWidth;
    protected boolean _hiding = false;

    private final String _txtFinalizeButton = Dialog.txtCreateButton;
    private final String _txtCancelButton = Dialog.txtCancelButton;
    private final String _txtNextButton = Dialog.txtNextButton;
    private final String _txtPreviousButton = Dialog.txtPreviousButton;
    private final String _txtPleaseWait = _constants.pleaseWait();

    private List<String> _instructionList = null;
    private List<AbstractWizardPanel> _panelList = null;
    private List<Boolean> _stateList = null;
    private boolean _isVisible = false;
    private String _title = null;
    private String _helpTarget = null;
    private Callback<CsiModal> _callBack = null;
    private int _basePanel = 0;
    private boolean _blocked = false;
    private boolean _final = false;
    private boolean _previousShadow = false;
    private boolean _wizardDisabled = false;
    private boolean _waitingForReturn = false;
    private boolean _wasFinal = false;
    private int _destroyFlag = 0;

    private HandlerRegistration _keyBoardHandlerRegistration1 = null;
    private HandlerRegistration _keyBoardHandlerRegistration2 = null;
    private HandlerRegistration _keyBoardHandlerRegistration3 = null;
    private HandlerRegistration _validityReportEventHandlerRegistration = null;
    private HandlerRegistration _carriageReturnEventHandlerRegistration = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected abstract void execute(AbstractWizardPanel activePanelIn, ClickEvent eventIn);

    //
    //
    //
    protected abstract void cancel(AbstractWizardPanel activePanelIn, ClickEvent eventIn);

    //
    //
    //
    protected abstract void displayNewPanel(int indexIn, ClickEvent eventIn);

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle results from validity check
    //
    protected ValidityReportEventHandler handleValidityReportEvent
    = new ValidityReportEventHandler() {
        @Override
        public void onValidityReport(ValidityReportEvent eventIn) {

            try{

                nextButton.setEnabled(false);
                finalButton.setEnabled(false);

                if (!_wizardDisabled) {

                    if (_final || ((!_chainForward) && (0 <= _finalDisplayIndex)
                                    && (_finalDisplayIndex == _activePanelIndex))) {

                        enableFinish(eventIn.getValidFlag(), false);

                    } else {

                        enableNext(eventIn.getValidFlag(), false);

                    }
                }

            } catch (Exception myException) {

                Display.error("Wizard", 1, myException);
            }
        }
    };

    //
    // Handle recognized carriage return event
    //
    protected CarriageReturnEventHandler handleCariageReturnEvent
    = new CarriageReturnEventHandler() {
        @Override
        public void onCarriageReturn(CarriageReturnEvent eventIn) {

            try{

                nextButton.setEnabled(false);
                finalButton.setEnabled(false);

                if (!_wizardDisabled) {

                    if (_final || ((0 <= _finalDisplayIndex) && (_finalDisplayIndex == _activePanelIndex))) {

                        if (_chainForward) {

                            execute(_panelList.get(_panelList.size() - 1), null);

                        } else {

                            enableFinish(eventIn.getValidFlag(), true);
                        }

                    } else {

                        enableNext(eventIn.getValidFlag(), true);

                    }

                    if (_blocked) {

                        _blocked = false;
                        showWatchBox(_txtPleaseWait);

                    } else if (watchBoxShowing()) {

                    }
                }

            } catch (Exception myException) {

                Display.error("Wizard", 2, myException);
            }
        }
    };

    //
    // Monitor all typing to check for a carriage return
    //
    private KeyDownHandler handlePanelKeyDown
            = new KeyDownHandler() {

        @Override
        public void onKeyDown(KeyDownEvent eventIn) {

            try{

                if (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode()) {

                    eventIn.stopPropagation();

                    if ((null != _panelList) && (_basePanel <= _activePanelIndex)
                            && (_panelList.size() > _activePanelIndex)) {

                        AbstractWizardPanel myPanel = _panelList.get(_activePanelIndex);

                        if (null != myPanel) {

                            myPanel.handleEscapeKey();
                        }
                    }
                    eventIn.stopPropagation();
                }

            } catch (Exception myException) {

                Display.error("Wizard", 3, myException);
            }
        }
    };

    //
    // Monitor all typing to check for a carriage return
    //
    private KeyUpHandler handlePanelKeyUp
            = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent eventIn) {

            try{

                if (KeyCodes.KEY_ENTER == eventIn.getNativeKeyCode()) {

                    if ((null != _panelList) && (_basePanel <= _activePanelIndex)
                            && (_panelList.size() > _activePanelIndex)) {

                        AbstractWizardPanel myPanel = _panelList.get(_activePanelIndex);

                        if (null != myPanel) {

                            myPanel.handleCarriageReturn();
                        }
                    }

                } else if (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode()) {

                    eventIn.stopPropagation();
                }

            } catch (Exception myException) {

                Display.error("Wizard", 4, myException);
            }
        }
    };

    //
    // Monitor all typing to check for a carriage return
    //
    private KeyPressHandler handlePanelKeyPress
            = new KeyPressHandler() {

        @Override
        public void onKeyPress(KeyPressEvent eventIn) {

            try{

                if (KeyCodes.KEY_ESCAPE == eventIn.getCharCode()) {

                    eventIn.stopPropagation();
                }

            } catch (Exception myException) {

                Display.error("Wizard", 5, myException);
            }
        }
    };

    //
    // Handle clicking the Create button
    //
    private ClickHandler handleCreateButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try{

                //
                // Disable the final button
                //
                finalButton.setEnabled(false);

                //
                // Call the execute method supplied by the derived class
                //
                execute(_panelList.get(_panelList.size() - 1), eventIn);

            } catch (Exception myException) {

                Display.error("Wizard", 6, myException);
            }
        }
    };

    //
    // Handle clicking the Cancel button
    //
    private ClickHandler handleCancelButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            cancel();
        }
    };

    //
    // Handle clicking the Next button
    //
    private ClickHandler handleNextButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try{

                //
                // Disable the next button
                //
                nextButton.setEnabled(false);

                if (_chainForward && (0 <= _finalDisplayIndex) && (_finalDisplayIndex <= _activePanelIndex)) {

                    //
                    // Call the execute method supplied by the derived class
                    //
                    execute(_panelList.get(_panelList.size() - 1), eventIn);

                } else {

                    //
                    // Call the execute method supplied by the derived class
                    //
                    displayNewPanel(_panelList.size(), eventIn);
                    adjustTitle();
                }

            } catch (Exception myException) {

                Display.error("Wizard", 7, myException);
            }
        }
    };

    //
    // Handle clicking the Previous button
    //
    private ClickHandler handlePreviousButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try{

                if (1 < _panelList.size()) {

                    removePanel(eventIn);

                } else if (null != priorDialog) {

                    removeLastPanel(eventIn);
                    priorDialog.show();
                    destroy();
                }
                adjustTitle();

            } catch (Exception myException) {

                Display.error("Wizard", 8, myException);
            }
        }
    };

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void setReadyToExecute(boolean finalFlagIn) {

        _final = finalFlagIn;
    }

    @Override
    public void showWithResults(KnowsParent childIn) {

        show();
    }

    @Override
    public void show() {

        dialog.show();
        _hiding = false;
        //
        // resume monitoring
        //
        resumeMonitoring();
        if(null != _activePanel) {

            _activePanel.resumeMonitoring();
        }
/*
        if (_waitingForReturn){

            _waitingForReturn = false;
            redisplayLastPanel(_wasFinal);

        } else if (null != _activePanel) {

            _activePanel.resumeMonitoring();
        }
*/
    }

    @Override
    public void hide() {

        try {

            //
            // release the monitors
            //
            suspendMonitoring();
            if (null != _panelList) {

                for (int i = _basePanel; _panelList.size() > i; i++) {

                    AbstractWizardPanel myPanel = _panelList.get(i);
                    if (null != myPanel) {

                        myPanel.suspendMonitoring();
                    }
                }
            }
            _hiding = true;
            dialog.hide();

        } catch (Exception myException) {

            Display.error("Wizard", 9, myException);
        }
    }

    public void exit() {

        destroy(priorDialog);
    }

    @Override
    public void destroy() {

        destroy(null);
    }

     public void destroy(WizardInterface priorDialogIn) {

        try{

            final WizardInterface myPriorDialog = priorDialogIn;

            switch  (_destroyFlag) {

                case 0:

                    _destroyFlag = 1;

                    hide();

                    DeferredCommand.add(new Command() {
                        public void execute() {
                            destroy(myPriorDialog);
                        }
                    });
                    break;

                case 1:

                    _destroyFlag = 2;

                    //
                    // release the monitors
                    //
                    suspendMonitoring();

                    if (null != _panelList) {

                        for (int i = _basePanel; _panelList.size() > i; i++) {

                            AbstractWizardPanel myPanel = _panelList.get(i);
                            if (null != myPanel) {

                                myPanel.suspendMonitoring();
                                myPanel.destroy();
                            }
                        }
                    }

                    DeferredCommand.add(new Command() {
                        public void execute() {
                            destroy(myPriorDialog);
                        }
                    });
                    break;

                default:

                    //
                    // Hide dialog
                    //
                    if (null != dialog) {

                        dialog.removeFromParent();
                    }
                    if (null != myPriorDialog) {

                        myPriorDialog.show();
                    }
                    dialog = null;
                    controlPanel = null;
                    instructionTextArea = null;
                    finalButton = null;
                    cancelButton = null;
                    previousButton = null;
                    nextButton = null;

                    _instructionList = null;
                    _panelList = null;
                    _stateList = null;
                    _activePanelIndex = -1;
                    _isVisible = false;
                    break;
            }

        } catch (Exception myException) {

            Display.error("Wizard", 10, myException);
        }
    }

    public void hideInstructions() {

        instructionTextArea.setVisible(false);
    }

    public void showInstructions() {

        instructionTextArea.setVisible(true);
    }

    public void replaceInstructions(String instructionsIn) {

        instructionTextArea.setText(instructionsIn);
    }

    public void setButtonWidth(Integer widthIn) {
        
        _buttonWidth = widthIn;
    }

    public void disableControlButtons() {

        _previousShadow = previousButton.isEnabled();
        _wizardDisabled = true;
        previousButton.setEnabled(null != priorDialog);
        nextButton.setEnabled(false);
        finalButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    public void enableControlButtons() {

        _wizardDisabled = false;
        previousButton.setEnabled(_previousShadow);
        cancelButton.setEnabled(true);
    }

    public void clearAlert() {

        wizardAlert.setText("");
        wizardAlert.setVisible(false);
    }

    public void setAlert(String textIn, String colorIn) {

        wizardAlert.setText(textIn);
        wizardAlert.getElement().getStyle().setColor(colorIn);
        wizardAlert.setVisible(true);
    }

    public void setPriorDialog(WizardInterface dialogIn) {

        priorDialog = dialogIn;
        setParent(priorDialog);
        if (null != previousButton) {

            previousButton.setEnabled((null != priorDialog) || ((_basePanel + 1) < _panelList.size()));
        }
    }

    public void suspendMonitoring() {

        try{

            //
            // release the keyboard monitors
            //
            if (null != _keyBoardHandlerRegistration1) {

                _keyBoardHandlerRegistration1.removeHandler();
                _keyBoardHandlerRegistration1 = null;
            }
            if (null != _keyBoardHandlerRegistration2) {

                _keyBoardHandlerRegistration2.removeHandler();
                _keyBoardHandlerRegistration2 = null;
            }
            if (null != _keyBoardHandlerRegistration3) {

                _keyBoardHandlerRegistration3.removeHandler();
                _keyBoardHandlerRegistration3 = null;
            }
            suspendEventMonitoring();

        } catch (Exception myException) {

            Display.error("Wizard", 11, myException);
        }
    }

    public void resumeMonitoring() {

        try{

            if (null == _keyBoardHandlerRegistration1) {

                _keyBoardHandlerRegistration1 = RootPanel.get().addDomHandler(handlePanelKeyDown, KeyDownEvent.getType());
            }
            if (null == _keyBoardHandlerRegistration2) {

                _keyBoardHandlerRegistration2 = RootPanel.get().addDomHandler(handlePanelKeyUp, KeyUpEvent.getType());
            }
            if (null == _keyBoardHandlerRegistration3) {

                _keyBoardHandlerRegistration3 = RootPanel.get().addDomHandler(handlePanelKeyPress, KeyPressEvent.getType());
            }
            resumeEventMonitoring();

        } catch (Exception myException) {

            Display.error("Wizard", 12, myException);
        }
    }

    public void cancel() {

        try{

            int myPanelCount = _panelList.size();
            //
            // Call the cleanup method supplied by the derived class
            //
            if (_basePanel < myPanelCount) {

                cancel(_panelList.get(myPanelCount - 1), null);

            } else {

                cancel(null, null);
            }
            if(null != priorDialog) {

                priorDialog.cancel();
                priorDialog = null;
            }
        } catch (Exception myException) {

            Display.error("Wizard", 13, myException);
        }
        //
        // free resources and leave
        //
        hideWatchBox();
        destroy();
        CsiModal.clearAll();
    }

    public void clickPrior() {

        handlePreviousButtonClick.onClick(null);
    }

    public void clickNext() {

        handleNextButtonClick.onClick(null);
    }

    public void clickCancel() {

        handleCancelButtonClick.onClick(null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void abortWizard(Exception exceptionIn) {

        Display.error("Wizard", 14, exceptionIn);
        destroy();
    }

    //
    // Override this method to get access to the
    // panel before discarding it when stepping backwards
    //
    protected void accessOldPanel(AbstractWizardPanel oldPanelIn, int indexIn, ClickEvent eventIn) {

    }

    //
    // Override this method to get access to the
    // panel before redisplaying it when stepping backwards
    //
    protected void accessNewPanel(AbstractWizardPanel oldPanelIn, int indexIn, ClickEvent eventIn) {

    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn, String finalizeButtonIn, int basePanelIn) {

        super(null);
        _basePanel = basePanelIn;
        initializeObject(titleIn, helpTargetIn, finalizeButtonIn, null);
    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn, String finalizeButtonIn) {

        super(null);
        initializeObject(titleIn, helpTargetIn, finalizeButtonIn, null);
    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn, int basePanelIn) {

        super(null);
        _basePanel = basePanelIn;
        initializeObject(titleIn, helpTargetIn, _txtFinalizeButton, null);
    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn) {

        super(null);
        initializeObject(titleIn, helpTargetIn, _txtFinalizeButton, null);
    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn,
                     String finalizeButtonIn, int basePanelIn, WizardInterface previousDialogIn) {

        super(previousDialogIn);
        _basePanel = basePanelIn;
        initializeObject(titleIn, helpTargetIn, finalizeButtonIn, previousDialogIn);
    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn, String finalizeButtonIn, WizardInterface previousDialogIn) {

        super(previousDialogIn);
        initializeObject(titleIn, helpTargetIn, finalizeButtonIn, previousDialogIn);
    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn, int basePanelIn, WizardInterface previousDialogIn) {

        super(previousDialogIn);
        _basePanel = basePanelIn;
        initializeObject(titleIn, helpTargetIn, _txtFinalizeButton, previousDialogIn);
    }

    //
    //
    //
    protected Wizard(String titleIn, String helpTargetIn, WizardInterface previousDialogIn) {

        super(previousDialogIn);
        initializeObject(titleIn, helpTargetIn, _txtFinalizeButton, previousDialogIn);
    }

    protected void unlockDisplay() {
        
    }
    
    protected void addHelp(Callback<CsiModal> callbackIn) {
         
        //
        // Set up the dialog title bar with help button
        //
        _callBack = callbackIn;
        dialog.defineHeader(_title, _callBack, true);
    }

    protected int getBasePanel() {

        return _basePanel;
    }

    protected void displayPanel(AbstractWizardPanel panelIn, String instructionsIn) {

        displayPanel(panelIn, instructionsIn, false);
    }

    protected void displayPanel(AbstractWizardPanel panelIn, String instructionsIn, boolean finalIn) {

        displayPanel(panelIn, instructionsIn, finalIn, false);
    }

    protected void displayPanel(AbstractWizardPanel panelIn, String[] instructionsIn, boolean finalIn) {

        displayPanel(panelIn, instructionsIn, "\n", finalIn);
    }

    protected void displayPanel(AbstractWizardPanel panelIn, String[] instructionsIn, String delimeterIn, boolean finalIn) {

        try{

            if ((null != instructionsIn) && (0 < instructionsIn.length)) {

                StringBuilder myInstructions = new StringBuilder();

                for (int i = 0; instructionsIn.length > i; i++) {

                    if (0 != i) {

                        myInstructions.append(delimeterIn);
                    }
                    myInstructions.append(instructionsIn[i]);
                }

                displayPanel(panelIn, myInstructions.toString(), finalIn, false);

            } else {

                displayPanel(panelIn, (String)null, finalIn, false);
            }
            adjustTitle();

        } catch (Exception myException) {

            Display.error("Wizard", 15, myException);
        }
    }

    protected void displayPanel(AbstractWizardPanel panelIn, String instructionsIn, boolean finalIn, boolean blockNextIn) {

        try{

            _final = finalIn;

            if (null != panelIn) {

                cancelButton.setEnabled(true);
                finalButton.setEnabled(false);
                nextButton.setEnabled(false);
                previousButton.setEnabled((null != priorDialog) || (_basePanel < _panelList.size()));

                _activePanel = panelIn;
                rewireHandlers();

                if (_basePanel < _panelList.size()) {

                    AbstractWizardPanel myCurrentPanel = _panelList.get(_panelList.size() - 1);

                    myCurrentPanel.suspendMonitoring();

                    myCurrentPanel.asWidget().setVisible(false);
                    controlPanel.remove(myCurrentPanel);

                    //
                    // Save the state of the Create button prior to leaving this panel
                    //
                    _stateList.add(finalButton.isEnabled());
                }

                _panelList.add(_activePanel);
                _instructionList.add(instructionsIn);

                addControlPanel(_activePanel);

                _activePanelIndex = _panelList.size() - 1;

                instructionTextArea.setText(instructionsIn);

                if (!_isVisible) {
                    _isVisible = true;
                    dialog.show(_buttonWidth);
                }
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {

                        if(null != _activePanel) {

                            _activePanel.grabFocus();
                            _activePanel.beginMonitoring();
                        }
                    }
                });
                _blocked = blockNextIn;
            }
            adjustTitle();

        } catch (Exception myException) {

            Display.error("Wizard", 16, myException);
        }
    }

    protected void removeLastPanel(ClickEvent eventIn) {

        try{

            if (0 < _panelList.size()) {

                int myIndex = _panelList.size() - 1;

                _final = false;

                AbstractWizardPanel myCurrentPanel = _panelList.get(myIndex);

                myCurrentPanel.suspendMonitoring();
                accessOldPanel(myCurrentPanel, myIndex, eventIn);
            }
            adjustTitle();

        } catch (Exception myException) {

            Display.error("Wizard", 17, myException);
        }
    }

    protected void redisplayLastPanel(boolean isFinal) {


        try{

            if (1 < _panelList.size()) {

                _activePanelIndex = _panelList.size() - 1;
                _activePanel = _panelList.get(_activePanelIndex);
                String myInstructions = _instructionList.get(_activePanelIndex);
                rewireHandlers();

                _final = isFinal;
                accessNewPanel(_activePanel, _activePanelIndex, null);
                addControlPanel(_activePanel);
                instructionTextArea.setText(myInstructions);
                previousButton.setEnabled((null != priorDialog) || ((_basePanel + 1) < _panelList.size()));
                //
                // Restore the state of the Create button
                // to what it was prior to leaving the panel
                //
                _activePanel.enableInput();
                finalButton.setEnabled(_stateList.get(_activePanelIndex));
                nextButton.setEnabled(true);

                DeferredCommand.add(new Command() {
                    public void execute() {
                        _activePanel.grabFocus();
                        _activePanel.beginMonitoring();
                    }
                });
            }
            adjustTitle();

        } catch (Exception myException) {

            Display.error("Wizard", 18, myException);
        }
    }

    protected void removePanel(ClickEvent eventIn) {

        try{

            if (1 < _panelList.size()) {

                int myIndex = _panelList.size() - 1;

                _final = false;

                AbstractWizardPanel myCurrentPanel = _panelList.get(myIndex);

                myCurrentPanel.suspendMonitoring();
                accessOldPanel(myCurrentPanel, myIndex, eventIn);

                _activePanelIndex = myIndex - 1;
                _activePanel = _panelList.get(_activePanelIndex);
                String myInstructions = _instructionList.get(_activePanelIndex);

                if (null != _activePanel) {

                    _panelList.set(_activePanelIndex, _activePanel);

                } else {

                    _activePanel = _panelList.get(_activePanelIndex);
                }
                rewireHandlers();

                myCurrentPanel.asWidget().setVisible(false);
                controlPanel.remove(myCurrentPanel);
                _panelList.remove(myIndex);
                _instructionList.remove(myIndex);

                accessNewPanel(_activePanel, _activePanelIndex, eventIn);
                addControlPanel(_activePanel);

                instructionTextArea.setText(myInstructions);

                previousButton.setEnabled((null != priorDialog) || ((_basePanel + 1) < _panelList.size()));

                //
                // Restore the state of the Create button
                // to what it was prior to leaving the panel
                //
                _activePanel.enableInput();
                finalButton.setEnabled(_stateList.get(_activePanelIndex));
                nextButton.setEnabled(true);

                DeferredCommand.add(new Command() {
                    public void execute() {
                        _activePanel.grabFocus();
                        _activePanel.beginMonitoring();
                    }
                });
            }
            adjustTitle();

        } catch (Exception myException) {

            Display.error("Wizard", 19, myException);
        }
    }

    //
    //
    //
    protected void disablePrevious() {

        previousButton.setEnabled(null != priorDialog);
    }

    //
    //
    //
    protected void enablePrevious() {

        previousButton.setEnabled(true);
    }

    //
    //
    //
    protected void disableNext() {

        nextButton.setEnabled(false);
    }

    //
    //
    //
    protected void enableNext() {

        nextButton.setEnabled(true);
    }

    //
    //
    //
    private void enableNext(boolean isEnabledIn, boolean executeIn) {

        enableButton(nextButton, isEnabledIn, executeIn);
    }

    //
    //
    //
    protected void relabelFinish(String textIn) {
        
        finalButton.setText(textIn);
    }
    
    //
    //
    //
    protected void disableFinish() {
        
        finalButton.setEnabled(false);
    }
    
    //
    //
    //
    protected void enableFinish() {
        
        finalButton.setEnabled(true);
    }
    
    //
    //
    //
    protected void enableFinish(boolean isEnabledIn, boolean executeIn) {

        enableButton(finalButton, isEnabledIn, executeIn);
    }
    
    //
    //
    //
    protected void replaceActivePanel(AbstractWizardPanel panelIn) {

        try{

            if (_basePanel <= _activePanelIndex) {

                _activePanel.suspendMonitoring();

                _activePanel = panelIn;
                rewireHandlers();
                _panelList.set(_activePanelIndex, _activePanel);

                addControlPanel(_activePanel);

                _stateList.set(_activePanelIndex, false);
                finalButton.setEnabled(false);
                nextButton.setEnabled(false);

                DeferredCommand.add(new Command() {
                    public void execute() {
                        _activePanel.grabFocus();
                        _activePanel.beginMonitoring();
                    }
                });
            }
            adjustTitle();

        } catch (Exception myException) {

            Display.error("Wizard", 20, myException);
        }
    }
    
    //
    //
    //
    protected void replaceActivePanel(AbstractWizardPanel panelIn, String instructionsIn) {

        try{

            if (_basePanel <= _activePanelIndex) {

                _activePanel.suspendMonitoring();

                _activePanel = panelIn;
                rewireHandlers();
                _panelList.set(_activePanelIndex, _activePanel);
                _instructionList.set(_activePanelIndex, instructionsIn);

                addControlPanel(_activePanel);

                instructionTextArea.setText(instructionsIn);
                instructionTextArea.getElement().getStyle().setColor(Dialog.txtInfoColor);

                _stateList.set(_activePanelIndex, false);
                finalButton.setEnabled(false);
                nextButton.setEnabled(false);

                DeferredCommand.add(new Command() {
                    public void execute() {
                        _activePanel.grabFocus();
                        _activePanel.beginMonitoring();
                    }
                });
            }
            adjustTitle();

        } catch (Exception myException) {

            Display.error("Wizard", 21, myException);
        }
    }
    
    //
    // Retrieve a panel by index
    //
    protected AbstractWizardPanel getPanel(int indexIn) {
        
        AbstractWizardPanel myPanel = null;
        int myLimit = _panelList.size();
        
        if ((_basePanel <= indexIn) && (myLimit > indexIn)) {
            
            myPanel = _panelList.get(indexIn);
        }
        return myPanel;
    }

    protected WizardInterface getPriorDialog() {

        return priorDialog;
    }

    protected void adjustTitle() {

        String mySubTitle = null;

        if (null != _activePanel) {

            mySubTitle = _activePanel.getPanelTitle();
        }

        if (null != mySubTitle) {

            dialog.setTitle(_title + " -- " + mySubTitle);

        } else {

            dialog.setTitle(_title);
        }
    }

    protected void savePreviousContext(boolean wasFinalIn) {

        _waitingForReturn = true;
        _wasFinal = wasFinalIn;
    }

    protected String getTitle() {

        return _title;
    }

    protected String getHelp() {

        return _helpTarget;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    private void initializeObject(String titleIn, String helpTargetIn,
                                  String finalizeButtonIn, WizardInterface priorDialogIn) {

        try{

            _instructionList = new ArrayList<String>();
            _panelList =  new ArrayList<AbstractWizardPanel>();
            _stateList =  new ArrayList<Boolean>();
            priorDialog = priorDialogIn;
            setParent(priorDialog);

            for (int i = 0; _basePanel > i; i++) {

                _instructionList.add("");
                _panelList.add(null);
                _stateList.add(false);
            }

            //
            // Link UI XML code to this file and all GWT to create remaining components
            //
            uiBinder.createAndBindUi(this);

            //
            // Set up the dialog title bar with help button
            //
            _title = titleIn;
            _helpTarget = helpTargetIn;
            dialog.defineHeader(_title, _helpTarget, (null != _helpTarget));

            //
            // Set up the dialog cancel button
            //
            cancelButton = dialog.getCancelButton();
            cancelButton.setText(_txtCancelButton);
            cancelButton.setVisible(true);
            cancelButton.setEnabled(true);
            cancelButton.addClickHandler(handleCancelButtonClick);

            //
            // Set up the dialog action button
            //
            finalButton = dialog.getActionButton();
            finalButton.setText((null != finalizeButtonIn) ? finalizeButtonIn : Dialog.txtFinishButton);
            finalButton.setVisible(true);
            finalButton.setEnabled(false);
            finalButton.addClickHandler(handleCreateButtonClick);

            //
            // Set up the Next button
            //
            nextButton = new Button(_txtNextButton);
            nextButton.addClickHandler(handleNextButtonClick);
            nextButton.setVisible(true);
            nextButton.setEnabled(false);
            nextButton.setType(ButtonType.PRIMARY);
            dialog.addRightControl(nextButton);

            //
            // Set up the Previous button
            //
            previousButton = new Button(_txtPreviousButton);
            previousButton.addClickHandler(handlePreviousButtonClick);
            previousButton.setVisible(true);
            previousButton.setEnabled(null != priorDialog);
            dialog.addRightControl(previousButton);

            resumeMonitoring();

            wizardAlert = new Label();
            clearAlert();
            dialog.addLeftControl(wizardAlert);

            dialog.hideTitleCloseButton();

        } catch (Exception myException) {

            Display.error("Wizard", 22, myException);
        }
    }
    
    //
    //
    //
    private void addControlPanel(AbstractWizardPanel panelIn) {

        panelIn.setWizard(this);
        controlPanel.add(panelIn);
        controlPanel.setWidgetLeftWidth(panelIn, 10, Style.Unit.PX, panelIn.getPanelWidth(), Style.Unit.PX);
        controlPanel.setWidgetTopBottom(panelIn, 10, Style.Unit.PX, 0, Style.Unit.PX);
        panelIn.asWidget().setVisible(true);
    }

    private void rewireHandlers() {

        suspendEventMonitoring();
        resumeEventMonitoring();
    }

    private void suspendEventMonitoring() {

        if (null != _validityReportEventHandlerRegistration) {

            _validityReportEventHandlerRegistration.removeHandler();
            _validityReportEventHandlerRegistration = null;
        }
        if (null != _carriageReturnEventHandlerRegistration) {

            _carriageReturnEventHandlerRegistration.removeHandler();
            _carriageReturnEventHandlerRegistration = null;
        }
    }

    private void resumeEventMonitoring() {

        if (null != _activePanel) {

            if (null == _validityReportEventHandlerRegistration) {

                _validityReportEventHandlerRegistration = _activePanel.addValidityReportEventHandler(handleValidityReportEvent);
            }
            if (null == _carriageReturnEventHandlerRegistration) {

                _carriageReturnEventHandlerRegistration = _activePanel.addCarriageReturnEventHandler(handleCariageReturnEvent);
            }
        }
    }

    //
    //
    //
    private void enableButton(Button buttonIn, boolean isEnabledIn, boolean executeIn) {

        if (isEnabledIn) {

            if (_blocked) {

                _blocked = false;
                showWatchBox(_txtPleaseWait);
/*
            } else if (!watchBoxShowing()) {

                buttonIn.setEnabled(true);

                if (executeIn) {

                    buttonIn.click();
                }

            } else {

                buttonIn.setEnabled(false);
            }
*/
            } else {

                buttonIn.setEnabled(true);

                if (executeIn) {

                    buttonIn.click();
                }
            }

        } else {

            buttonIn.setEnabled(false);
        }
    }
}

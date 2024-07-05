package csi.client.gwt.csiwizard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.RootPanel;

import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.events.CarriageReturnEvent;
import csi.client.gwt.events.CarriageReturnEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.*;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 1/15/2015.
 */
public class PanelDialog extends WatchingParent implements KnowsParent, ValidityCheck {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface PanelDialogUiBinder extends UiBinder<ValidatingDialog, PanelDialog> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    protected ValidatingDialog dialog;

    @UiField
    FullSizeLayoutPanel controlPanel;
    @UiField
    DialogInfoTextArea instructionTextArea;

    private AbstractWizardPanel _activePanel = null;
    private List<WizardButton> _buttonList = new ArrayList<WizardButton>();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private String _txtCreateTitle = null;
    private String _txtHelpTarget = null;
    private String _txtInstructions = null;

    private static PanelDialogUiBinder uiBinder = GWT.create(PanelDialogUiBinder.class);

    private ClickHandler _executeHandler = null;
    private ClickHandler _cancelHandler = null;

    private boolean _executionBlocked = false;

    private HandlerRegistration _validityReportEventHandlerRegistration = null;
    private HandlerRegistration _keyBoardHandlerRegistration1 = null;
    private HandlerRegistration _keyBoardHandlerRegistration2 = null;
    private HandlerRegistration _keyBoardHandlerRegistration3 = null;
    private HandlerRegistration _carriageReturnEventHandlerRegistration = null;

    private boolean _forceValidityUpdate = true;
    private boolean _validPanelData = false;
    private int _executeButton = 0;
    private int _cancelButton = 1;
    private int _destroyFlag = 0;

    protected PanelDialog _this;


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

            try {

                boolean myValidFlag = eventIn.getValidFlag();

                if (_forceValidityUpdate || (_validPanelData != myValidFlag)) {

                    setValidity(myValidFlag);
                    _forceValidityUpdate = false;
                }

            } catch (Exception myException) {

                Display.error("PanelDialog", 1, myException);
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

            try {

                executeRequest();

            } catch (Exception myException) {

                Display.error("PanelDialog", 2, myException);
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

            try {

                if (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode()) {

                    eventIn.stopPropagation();

                    if (null != _activePanel) {

                        _activePanel.handleEscapeKey();
                    }
                }

            } catch (Exception myException) {

                Display.error("PanelDialog", 3, myException);
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

            try {

                if (KeyCodes.KEY_ENTER == eventIn.getNativeKeyCode()) {

                    if (null != _activePanel) {

                        _activePanel.handleCarriageReturn();
                    }

                } else if (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode()) {

                    eventIn.stopPropagation();
                }

            } catch (Exception myException) {

                Display.error("PanelDialog", 4, myException);
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

            try {

                if (KeyCodes.KEY_ESCAPE == eventIn.getCharCode()) {

                    eventIn.stopPropagation();
                }

            } catch (Exception myException) {

                Display.error("PanelDialog", 5, myException);
            }
        }
    };

    //
    // Handle clicking the Cancel button
    //
    protected ClickHandler handleCancelButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                //
                // Notify caller
                //
                if (null != _cancelHandler) {

                    _cancelHandler.onClick(eventIn);
                }

                //
                // Destroy dialog
                //
                destroy();

            } catch (Exception myException) {

                Display.error("PanelDialog", 6, myException);
            }
        }
    };

    //
    // Handle clicking the Cancel button
    //
    protected ClickHandler handleExecuteButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                executeRequest();

                //
                // Destroy dialog
                //
                destroy();

            } catch (Exception myException) {

                Display.error("PanelDialog", 7, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public PanelDialog() {

        this(null, null, null, null, null, null);
    }

    public PanelDialog(AbstractWizardPanel panelIn, String titleIn, String infoIn) {

        this(panelIn, titleIn, null, infoIn, null, null);
    }

    /*
        Used by WizardDialog
     */
    public PanelDialog(AbstractWizardPanel panelIn, String titleIn, String helpIn, String infoIn) {

        this(panelIn, titleIn, helpIn, infoIn, null, null);
    }

    /*
        Used by WizardDialog
     */
    public PanelDialog(AbstractWizardPanel panelIn, String titleIn, String helpIn, String[] infoIn, String delimiterIn) {

        this(panelIn, titleIn, helpIn, Format.value(infoIn, delimiterIn), null, null);
    }

    /*
        Used by QueryParameterDialog
     */
    public PanelDialog(AbstractWizardPanel panelIn, String titleIn, String helpIn, String infoIn, ClickHandler executeHandlerIn) {

        this(panelIn, titleIn, helpIn, infoIn, executeHandlerIn, null);
    }

    public PanelDialog(AbstractWizardPanel panelIn, String titleIn, String infoIn, ClickHandler executeHandlerIn) {

        this(panelIn,titleIn, null, infoIn, executeHandlerIn, null);
    }

    /*
        Used by DataSourceEditorView
     */
    public PanelDialog(AbstractWizardPanel panelIn, String titleIn, String infoIn, ClickHandler executeHandlerIn, ClickHandler cancelHandlerIn) {

        this(panelIn,titleIn, null, infoIn, executeHandlerIn, cancelHandlerIn);
    }

    /*
        Used directly by ConstantDataEntryDialog
     */
    public PanelDialog(AbstractWizardPanel panelIn, String titleIn, String helpIn, String infoIn, ClickHandler executeHandlerIn, ClickHandler cancelHandlerIn) {

        try {

            _this = this;

            _txtCreateTitle = titleIn;
            _txtHelpTarget = helpIn;
            _txtInstructions = infoIn;

            //
            // Link UI XML code to this file and all GWT to create remaining components
            //
            dialog = uiBinder.createAndBindUi(this);

            dialog.setCallBack(this);

            _executeHandler = executeHandlerIn;
            _cancelHandler = cancelHandlerIn;

            _buttonList = new ArrayList<WizardButton>();

            _buttonList.add(new WizardButton(dialog.getActionButton(), Dialog.txtCreateButton, handleExecuteButtonClick, true, true));
            _buttonList.add(new WizardButton(dialog.getCancelButton(), null, handleCancelButtonClick, true, false));

            dialog.defineHeader(_txtCreateTitle, _txtHelpTarget, true);

            if (null != panelIn) {

                addControlPanel(panelIn, infoIn);
            }

        } catch (Exception myException) {

            Display.error("PanelDialog", 8, myException);
        }
    }

    public void setRequestHandler(ClickHandler handlerIn) {

        _executeHandler = handlerIn;
    }

    public void setCancelHandler(ClickHandler handlerIn) {

        _cancelHandler = handlerIn;
    }

    public AbstractWizardPanel getCurrentPanel() {

        return _activePanel;
    }

    public void show() {

        try {

            updateHandlers(true);
            dialog.defineHeader(_txtCreateTitle, _txtHelpTarget, true);
            dialog.show();
            forceValidityUpdate();

        } catch (Exception myException) {

            Display.error("PanelDialog", 9, myException);
        }
    }

    public void show(int buttonWidthIn) {

        try {

            updateHandlers(true);
            dialog.defineHeader(_txtCreateTitle, _txtHelpTarget, true);
            dialog.show(buttonWidthIn);
            forceValidityUpdate();

        } catch (Exception myException) {

            Display.error("PanelDialog", 10, myException);
        }
    }

    public void hide() {

        try {

//            hideWatchBox();
            updateHandlers(false);
            dialog.hide();

        } catch (Exception myException) {

            Display.error("PanelDialog", 11, myException);
        }
    }

    public void destroy() {

        try{

//            hideWatchBox();
            switch  (_destroyFlag) {

                case 0:

                    _destroyFlag = 1;

                    hide();

                    DeferredCommand.add(new Command() {
                        public void execute() {
                            destroy();
                        }
                    });
                    break;

                case 1:

                    _destroyFlag = 2;

                    //
                    // release the monitors
                    //
                    updateHandlers(false);
                    if (null != _activePanel) {

                        _activePanel.suspendMonitoring();
                        _activePanel.destroy();
                    }
                    DeferredCommand.add(new Command() {
                        public void execute() {
                            destroy();
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
                    replaceButtons(null);

                    dialog = null;
                    controlPanel = null;
                    instructionTextArea = null;
                    break;
            }

        } catch (Exception myException) {

            Display.error("PanelDialog", 12, myException);
        }
    }

    public Button getActionButton() {

        if ((null != _buttonList) && (_buttonList.size() > _executeButton)) {

            return _buttonList.get(_executeButton).getButton();

        } else {

            return dialog.getActionButton();
        }
    }

    public Button getCancelButton() {

        if ((null != _buttonList) && (_buttonList.size() > _cancelButton)) {

            return _buttonList.get(_cancelButton).getButton();

        } else {

            return dialog.getCancelButton();
        }
    }

    public String getText() {

        String myResult = null;

        try {

            if (null != _activePanel) {

                try {

                    myResult = _activePanel.getText();

                } catch(Exception myException) {

                    Display.error(myException);
                }
            }

        } catch (Exception myException) {

            Display.error("PanelDialog", 13, myException);
        }
        return myResult;
    }

    //
    //
    //
    public void addControlPanel(AbstractWizardPanel panelIn, String[] infoIn, String delimiterIn) {

        addControlPanel(panelIn, Format.value(infoIn, delimiterIn));
    }

    //
    //
    //
    public void addControlPanel(AbstractWizardPanel panelIn, String infoIn) {

        try {

            if (null != _activePanel) {

                controlPanel.remove(_activePanel);
            }
            _activePanel = panelIn;
            controlPanel.add(_activePanel);
            controlPanel.setWidgetLeftRight(_activePanel, 10, Style.Unit.PX, 312, Style.Unit.PX);
            controlPanel.setWidgetTopBottom(_activePanel, 10, Style.Unit.PX, 0, Style.Unit.PX);
            _activePanel.asWidget().setVisible(true);
            instructionTextArea.setText(infoIn);

            setValidity(false);

            DeferredCommand.add(new Command() {
                public void execute() {

                    try {

                        _activePanel.grabFocus();
                        _activePanel.beginMonitoring();
                        forceValidityUpdate();

                    } catch (Exception myException) {

                        Display.error("PanelDialog", 14, myException);
                    }
                }
            });

        } catch (Exception myException) {

            Display.error("PanelDialog", 15, myException);
        }
    }

    public void addInstructions(String infoIn) {

        instructionTextArea.setText(infoIn);
    }

    public void replaceButtons(List<WizardButton> listIn) {

        replaceButtons(listIn, _executeButton, _cancelButton);
    }

    public void replaceButtons(List<WizardButton> listIn, int actionButtonIn, int cancelButtonIn) {

        try {

            if (null != _buttonList) {

                for (int i = 0; _buttonList.size() > i; i++) {

                    WizardButton myButtonDef = _buttonList.get(i);

                    if (null != myButtonDef) {

                        myButtonDef.replaceHandler(null);

                        Button myButton = myButtonDef.getButton();

                        if (null != myButton) {

                            myButton.removeFromParent();
                        }
                    }
                }
            }
            _buttonList = null;

            if (null != listIn) {

                _buttonList = new ArrayList<WizardButton>(listIn.size());

                for (int i = 0; listIn.size() > i; i++) {

                    WizardButton myButtonDef = listIn.get(i);

                    if (null != myButtonDef) {

                        Button myButton = myButtonDef.getButton();

                        if (null != myButton) {

                            dialog.addRightControl(myButton);
                            _buttonList.add(myButtonDef);
                        }
                    }
                }
            }
            _executeButton = actionButtonIn;
            _cancelButton = cancelButtonIn;
            forceValidityUpdate();

        } catch (Exception myException) {

            Display.error("PanelDialog", 16, myException);
        }
    }

    public void forceValidityUpdate() {

        _forceValidityUpdate = true;
    }

    @Override
    public void checkValidity() {

    }

    public void suspendMonitoring() {

        try {

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

            Display.error("PanelDialog", 17, myException);
        }
    }

    public void resumeMonitoring() {

        try {

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

            Display.error("PanelDialog", 18, myException);
        }
    }

    public String getDialogTitle() {

        return _txtCreateTitle;
    }

    public String getDialogHelp() {

        return _txtHelpTarget;
    }

    public void setExecuteText(String textIn) {

        Button myActionButton = getActionButton();

        if (null != myActionButton) {

            myActionButton.setText(textIn);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void abortDialog(Exception exceptionIn) {

        Display.error("Dialog Encountered an Exception", exceptionIn);
        destroy();
    }

    protected void updateHandlers(boolean connectIn) {

        suspendMonitoring();

        if (connectIn) {

            resumeMonitoring();
        }
    }

    protected void setDialogInstructions(String infoIn) {

        if (null != infoIn) {

            instructionTextArea.setText(infoIn);
        }
    }

    protected String getDialogInstructions() {

        return _txtInstructions;
    }

    protected void setValidity(boolean validFlagIn) {

        _validPanelData = validFlagIn;

        if (null != _buttonList) {

            for (int i = 0; _buttonList.size() > i; i++) {

                _buttonList.get(i).recognizeValidity(_validPanelData);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void executeRequest() {

        if ((null != _executeHandler) && (!_executionBlocked) && (null != _buttonList)
                && (_buttonList.size() > _executeButton) && _buttonList.get(_executeButton).isEnabled()) {

            _executionBlocked = true;

            _executeHandler.onClick(null);
        }
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
}

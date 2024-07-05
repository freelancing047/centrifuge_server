package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;


public class TextInputWidget extends AbstractInputWidget {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    protected enum Integrity {

        BROKEN,
        OK_SO_FAR,
        COMPLETE
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    Label parameterFormat = null;
    TextBox parameterInput = null;
    CheckBox suppressValidityCheck = null;
    CheckBox returnEmptyString = null;
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected String _default = null;
    protected String _errorText = ""; //$NON-NLS-1$
    protected String _formatText = ""; //$NON-NLS-1$
    protected boolean _trim = false;
    private boolean _monitoring = false;
    private CsiDataType _dataType = CsiDataType.String;

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle clicking the "Add" button placed onto a parent dialog!!
    //
    public ClickHandler handleCheckBoxClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            
            reportValidity(checkIntegrity(getUserInput()), atReset());
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TextInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn, boolean canDoEmptyStringIn) {

        super(requiredIn);

        //
        // Initialize the display objects
        //
        initializeObject(promptIn, formatIn, defaultIn, validatorIn, canDoEmptyStringIn);
    }

    public TextInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {

        this(promptIn, formatIn, defaultIn, validatorIn, requiredIn, true);
    }

    public TextInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn) {
        
        this(promptIn, formatIn, defaultIn, validatorIn, true, true);
    }
    
    public TextInputWidget(String promptIn, String formatIn, String defaultIn, boolean requiredIn) {
        
        this(promptIn, formatIn, defaultIn, null, requiredIn, true);
    }
    
    public TextInputWidget(String promptIn, ParameterValidator validatorIn, boolean requiredIn) {

        this(promptIn, null, null, validatorIn, requiredIn, true);
    }
    
    public TextInputWidget(String promptIn, boolean requiredIn) {

        this(promptIn, null, null, null, requiredIn, true);
    }
    
    public TextInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {

        this(promptIn, null, defaultIn, validatorIn, requiredIn, true);
    }
    
    public TextInputWidget(String promptIn, String defaultIn, boolean requiredIn) {

        this(promptIn, null, defaultIn, null, requiredIn, true);
    }
    
    public TextInputWidget(String promptIn, String formatIn, String defaultIn) {
        
        this(promptIn, formatIn, defaultIn, null, true, true);
    }
    
    public TextInputWidget(String promptIn, ParameterValidator validatorIn) {

        this(promptIn, null, null, validatorIn, true, true);
    }
    
    public TextInputWidget(String promptIn) {

        this(promptIn, null, null, null, true, true);
    }
    
    public TextInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn) {

        this(promptIn, null, defaultIn, validatorIn, true, true);
    }
    
    public TextInputWidget(String promptIn, String defaultIn) {

        this(promptIn, null, defaultIn, null, true, true);
    }
    
    public boolean isValid() {
        
        return checkIntegrity(getUserInput());
    }

    public void grabFocus() {
        
        parameterInput.setFocus(true);
    }

    @Override
    public String getText() throws CentrifugeException {

        String myResult = getUserInput();

        return ((null == myResult) || (0 == myResult.length()))
                ? ((null != returnEmptyString)
                ? (returnEmptyString.getValue() ? "" : null)
                : null)
                : myResult;
    }
    
    @Override
    public void resetValue() {
        
        parameterInput.setText(_default);
        if (null != returnEmptyString) {

            returnEmptyString.setValue(false);
        }
        reportValidity(checkIntegrity(getUserInput()), atReset());
    }
    
    public int getRequiredHeight() {
        
        int myRequiredHeight = Dialog.intLabelHeight + Dialog.intTextBoxHeight;
                
        if ((null != suppressValidityCheck) || (null != returnEmptyString)) {
            
            myRequiredHeight += Dialog.intLabelHeight;
        }
        return myRequiredHeight;
    }
    
    @Override
    public int getRequestedHeight() {
        
        int myRequestedHeight = super.getRequestedHeight();

        if ((null != suppressValidityCheck) || (null != returnEmptyString)) {
            
            myRequestedHeight += Dialog.intMargin;
        }
        return myRequestedHeight;
    }

    public boolean atReset() {
        
        return (isEmpty()) && (!((null != returnEmptyString) && returnEmptyString.getValue()));
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
        
        parameterInput.setText(valueIn);
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected void initializeObject(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn, boolean canDoEmptyStringIn) {
        
        _errorText = ""; //$NON-NLS-1$
        _formatText = (null != validatorIn) ? validatorIn.getFormat() : formatIn;
        _validator = validatorIn;
        _default = defaultIn;
        
        if (null == _formatText) {
            
            _formatText = formatIn;
        }
        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(promptIn, canDoEmptyStringIn);
        
        //
        // Wire in the handlers
        //
        wireInHandlers();
    }
    
    //
    // Dummy routine for checking validity of the user input when it changes
    //
    // -- overwrite this method and return false to signify an error
    //
    protected Integrity checkInput(String userInputIn) {

        return checkInput(userInputIn, 32);
    }

    protected Integrity checkInput(String userInputIn, int minValueIn) {

        Integrity myIntegrity = returnEmptyString.getValue() ? Integrity.COMPLETE : Integrity.OK_SO_FAR;

        if ((null != userInputIn) && (0 < userInputIn.length())) {

            myIntegrity = Integrity.COMPLETE;

            for (int i = 0; userInputIn.length() > i; i++) {

                char myCharacter = userInputIn.charAt(i);

                if (minValueIn > myCharacter) {

                    myIntegrity = Integrity.BROKEN;
                    _errorText = (32 == myCharacter)
                                    ? i18n.stringInputblankException()
                                    : i18n.stringInputcontrolException(); //$NON-NLS-1$
                }
            }
        }
        return myIntegrity;
    }

    protected void wireInHandlers() {
        
        //
        // Set up handlers to capture changes in the resource name
        // in order to recognize conflicts with existing resources
        // when naming a new resource
        //
        //parameterInput.addKeyPressHandler(handleParameterInputKeyPress);
        //parameterInput.addKeyUpHandler(handleParameterInputKeyUp);
        //parameterInput.addDropHandler(handleParameterInputDrop);
        //parameterInput.addChangeHandler(handleParameterInputChange);
        
        if (null != suppressValidityCheck) {
            
            suppressValidityCheck.addClickHandler(handleCheckBoxClick);
        }
    }
    
    protected void layoutDisplay() {
        
        int myWidth = getWidth();
        int  myRightMargin = getRightMargin();
        int myInputTop = ((null != parameterPrompt) || (null != parameterFormat))
                ? Dialog.intLabelHeight : 0;
        
        if (null != parameterFormat) {
            
            setWidgetTopHeight(parameterFormat, 0, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetRightWidth(parameterFormat, myRightMargin, Unit.PX, myWidth, Unit.PX);
        }
        
       if (null != parameterPrompt) {
           
           setWidgetTopHeight(parameterPrompt, 0, Unit.PX, Dialog.intLabelHeight, Unit.PX);
           setWidgetLeftWidth(parameterPrompt, 0, Unit.PX, myWidth, Unit.PX);
       }

        parameterInput.setWidth(Integer.toString(myWidth - 14) + "px"); //$NON-NLS-1$
        setWidgetTopHeight(parameterInput, myInputTop, Unit.PX, Dialog.intTextBoxHeight, Unit.PX);
        setWidgetLeftWidth(parameterInput, 0, Unit.PX, myWidth, Unit.PX);

        if (null != suppressValidityCheck) {

            int myCheckBoxTop = myInputTop + Dialog.intTextBoxHeight + _margin;

            setWidgetTopHeight(suppressValidityCheck, myCheckBoxTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetLeftWidth(suppressValidityCheck, 0, Unit.PX, myWidth, Unit.PX);
        }

        if (null != returnEmptyString) {

            int myCheckBoxTop = myInputTop + Dialog.intTextBoxHeight + _margin;

            setWidgetTopHeight(returnEmptyString, myCheckBoxTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetRightWidth(returnEmptyString, 0, Unit.PX, myWidth, Unit.PX);
        }
    }

    protected boolean checkIntegrity() {
        
        return true;
    }

    protected boolean hideIntegrityCheckBox() {
        
        return true;
    }

    protected boolean hideValue() {
        
        return false;
    }
    
    protected String filterDrop(String dataIn) {
        
        StringBuilder myBuffer = new StringBuilder();
        
        for (char myCharacter : dataIn.toCharArray()) {
            
            if (31 < myCharacter) {
                
                myBuffer.append(myCharacter);
                
            } else {
                
                myBuffer.append(' ');
            }
        }
        return myBuffer.toString();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private void createWidgets(String promptIn, boolean canDoEmptyStringIn) {

        if ((null != promptIn) && (0 < promptIn.length())) {
            
            parameterPrompt = new Label();
            parameterPrompt.setText(promptIn);
            add(parameterPrompt);
            
            parameterFormat = new Label();
            parameterFormat.asWidget().getElement().getStyle().setColor(Dialog.txtPatternColor);
            parameterFormat.asWidget().getElement().getStyle().setTextAlign(TextAlign.RIGHT);
            
            if (null == _formatText) {
                
                _formatText = ""; //$NON-NLS-1$
            }
            parameterFormat.setText(_formatText);
            add(parameterFormat);
        }
        
        if (hideValue()) {

            parameterInput = new PasswordTextBox();

        } else {
            
            parameterInput = new TextBox();
        }
        if ((null != _default) && (0 < _default.length())) {
            
            parameterInput.setText(_default);
        }
        add(parameterInput);
        
        if (checkIntegrity() && (!hideIntegrityCheckBox())) {
            
            suppressValidityCheck = new CheckBox();
            suppressValidityCheck.setText(i18n.textInputDisableValidityMessage()); //$NON-NLS-1$
            add(suppressValidityCheck);
        }
        
        if (canDoEmptyStringIn) {

            returnEmptyString = new CheckBox();
            returnEmptyString.setText(i18n.returnEmptyString()); //$NON-NLS-1$
            add(returnEmptyString);
        }
    }
    
    private void checkValidity() {

        if (_monitoring) {

            reportValidity();
        }

        if (null != returnEmptyString) {

            if (isEmpty()) {

                returnEmptyString.setVisible(true);

            } else {

                returnEmptyString.setVisible(false);
                returnEmptyString.setValue(false);
            }
        }

        if (_monitoring || (null != returnEmptyString)) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }

    private boolean isEmpty() {

        String myData = getUserInput();

        return (null == myData) || (0 == myData.length());
    }
    
    private boolean checkIntegrity(String userInputIn) {
        
        boolean myReadyFlag; // true indicates "Next" or "Add" may be selected in the wizard
        String myTextColor1 = Dialog.txtLabelColor;
        String myTextColor2 = Dialog.txtPatternColor;
        
        parameterInput.getElement().getStyle().setColor(Dialog.txtLabelColor);

        if (((null != userInputIn) && (0 < userInputIn.length()))
                || ((null != returnEmptyString) && returnEmptyString.getValue())) {
            
            myReadyFlag = true;
            
            if (checkIntegrity() && (hideIntegrityCheckBox() || (!suppressValidityCheck.getValue()))) {
                
                Integrity myIntegrity = checkInput(userInputIn);

                if (null != _validator) {
                    
                    _errorText = _validator.checkInput(userInputIn);
                    
                    if (null != _errorText) {
                        
                        myIntegrity = Integrity.BROKEN;
                    }
                }

                if (Integrity.BROKEN == myIntegrity) {

                    myTextColor1 = Dialog.txtErrorColor;
                    myTextColor2 = Dialog.txtErrorColor;
                }
                myReadyFlag = (Integrity.COMPLETE == myIntegrity);
            }

        } else {

            myReadyFlag = false;
        }

        parameterInput.getElement().getStyle().setColor(myTextColor1);
        
        if (null != parameterPrompt) {
            
            parameterPrompt.getElement().getStyle().setColor(myTextColor1);
        }
        if (null != parameterFormat) {
            
            parameterFormat.setText(((null != _errorText) && (0 < _errorText.length())) ? _errorText : _formatText);
            parameterFormat.getElement().getStyle().setColor(myTextColor2);
        }
        return myReadyFlag;
    }

    private String getUserInput() {

        String myResult = (null != parameterInput) ? parameterInput.getText() : null;

        return ((null != myResult) && _trim) ? myResult.trim() : myResult;
    }
}

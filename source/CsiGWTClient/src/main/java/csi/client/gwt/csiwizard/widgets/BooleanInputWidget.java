package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.widget.boot.Dialog;


public class BooleanInputWidget extends AbstractInputWidget {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    RadioButton trueButton = null;
    RadioButton falseButton = null;
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private boolean _default = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle clicking the True radio button
    //
    private ClickHandler handleRadioButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            fireEvent(new ValidityReportEvent(true, atReset()));
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public BooleanInputWidget(String promptIn, String defaultIn, boolean requiredIn) {
        
        super(requiredIn);
        
        //
        // Initialize the display objects
        //
        initializeObject(promptIn, defaultIn);
    }
    
    public BooleanInputWidget(String promptIn, String defaultIn) {
        
        //
        // Initialize the display objects
        //
        initializeObject(promptIn, defaultIn);
    }

    @Override
    public String getText() {
        return trueButton.getValue() ? "true" : "false";
    }
    
    @Override
    public void resetValue() {
        
        falseButton.setValue(!_default);
        trueButton.setValue(_default);
    }
    
    public boolean isValid() {
        
        return trueButton.getValue() || falseButton.getValue();
    }
    
    public void grabFocus() {
        
        trueButton.setFocus(true);
    }
    
    public int getRequiredHeight() {
        
        return Dialog.intLabelHeight;
    }

    public boolean hasValidData() {

        return (true);
    }

    public boolean atReset() {

        return (true);
    }

    @Override
    public void beginMonitoring() {

        reportValidity(isValid());
    }

    @Override
    public void setValue(String valueIn) {
        
        if (null != valueIn){
            
            _default = (valueIn.equalsIgnoreCase("true") || valueIn.equalsIgnoreCase("yes"));
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected void initializeObject(String promptIn, String defaultIn) {
        
        //
        // Identify default value
        //
        if (null != defaultIn) {
                
            _default = (defaultIn.equalsIgnoreCase("true") || defaultIn.equalsIgnoreCase("yes"));
        }
        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(promptIn);
        
        //
        // Wire in the handlers
        //
        wireInHandlers();
        
        reportValidity(isValid());
    }
    
    protected void wireInHandlers() {
        
        trueButton.addClickHandler(handleRadioButtonClick);
        falseButton.addClickHandler(handleRadioButtonClick);
    }
    
    protected void layoutDisplay() {
        
        int myWidth = getWidth();
        int myHalfWidth = myWidth / 2;
        int myTop = (getHeight() - (2 * _margin)) / 2;
         
        if (null != parameterPrompt) {
            
            myTop -= _margin;
            setWidgetTopHeight(parameterPrompt, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetLeftWidth(parameterPrompt, 0, Unit.PX, myWidth, Unit.PX);
            myTop += Dialog.intLabelHeight;
        }
        
        setWidgetTopHeight(trueButton, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftWidth(trueButton, 0, Unit.PX, myHalfWidth, Unit.PX);
        
        setWidgetTopHeight(falseButton, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftWidth(falseButton, myHalfWidth, Unit.PX, myHalfWidth, Unit.PX);
        
        if (null != addButton) {
        
            centerAddButton();
        }
    }
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private void createWidgets(String promptIn) {

        String myTrueLabel = Dialog.txtTrueButton;
        String myFalseLabel = Dialog.txtFalseButton;
        
        if (null != promptIn) {
            
            String myPrompt = promptIn.trim();

            if (0 < myPrompt.length()) {
                
                if ('?' == myPrompt.charAt(myPrompt.length() - 1)) {
                    
                    myTrueLabel = Dialog.txtYesButton;
                    myFalseLabel = Dialog.txtNoButton;
                }
                
                parameterPrompt = new Label();
                parameterPrompt.setText(myPrompt);
                add(parameterPrompt);
            }
        }

        trueButton = new RadioButton("TrueFalse", myTrueLabel);
        trueButton.setValue(_default);
        add(trueButton);
        falseButton = new RadioButton("TrueFalse", myFalseLabel);
        falseButton.setValue(!_default);
        add(falseButton);
    }
}

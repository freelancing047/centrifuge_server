package csi.client.gwt.csiwizard;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;

/**
 * Created by centrifuge on 10/20/2015.
 */
public class WizardButton {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private Button _button;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private HandlerRegistration _registration;
    private boolean _useValidity;
    private boolean _isEnabled;
    private boolean _isValid = true;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public WizardButton(Dialog dialogIn, Button buttonIn, String labelIn,
                        ClickHandler handlerIn, boolean isEnabledIn, boolean useValidityIn) {

        this(buttonIn, labelIn, handlerIn, isEnabledIn, useValidityIn);

        if (null != dialogIn) {

            dialogIn.addRightControl(_button);
        }
    }

    public WizardButton(Button buttonIn, String labelIn, ClickHandler handlerIn,
                        boolean isEnabledIn, boolean useValidityIn) {

        _button = buttonIn;
        _isEnabled = isEnabledIn;
        _useValidity = useValidityIn;
        _isValid = true;
        replaceLabel(labelIn);
        replaceHandler(handlerIn);
        updateDisplay();
    }

    public Button getButton() {

        return _button;
    }

    public boolean isEnabled() {

        return _button.isEnabled();
    }

    public void enable() {

        _isEnabled = true;
        updateDisplay();
    }

    public void disable() {

        _isEnabled = false;
        updateDisplay();
    }

    public void recognizeValidity(boolean isValidIn) {

        if (_useValidity) {

            _isValid = isValidIn;
            updateDisplay();
        }
    }

    public void replaceLabel(String labelIn) {

        if (null != labelIn) {

            _button.setText(labelIn);
        }
    }

    public void replaceHandler(ClickHandler handlerIn) {

        if (null != _registration) {

            _registration.removeHandler();
        }
        _registration = (null != handlerIn) ? _button.addClickHandler(handlerIn) : null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void updateDisplay() {

        _button.setEnabled(_isValid & _isEnabled);
    }
}

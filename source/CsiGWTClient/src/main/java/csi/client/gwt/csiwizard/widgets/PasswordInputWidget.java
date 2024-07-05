package csi.client.gwt.csiwizard.widgets;

import csi.client.gwt.csiwizard.support.ParameterValidator;


public class PasswordInputWidget extends StringInputWidget {
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private static final String _txtDefaultPrompt = _constants.connectorParameterPrompt_Password();

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public PasswordInputWidget(String promptIn, ParameterValidator validatorIn, boolean requiredIn) {

       super((null != promptIn) ? promptIn : _txtDefaultPrompt, validatorIn, requiredIn);
    }
    
    public PasswordInputWidget(String promptIn, boolean requiredIn) {

       super((null != promptIn) ? promptIn : _txtDefaultPrompt, requiredIn);
    }
    
    public PasswordInputWidget(String promptIn, ParameterValidator validatorIn) {

       super((null != promptIn) ? promptIn : _txtDefaultPrompt, validatorIn);
    }
    
    public PasswordInputWidget(String promptIn) {

       super((null != promptIn) ? promptIn : _txtDefaultPrompt);
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected boolean hideValue() {
        
        return true;
    }
}

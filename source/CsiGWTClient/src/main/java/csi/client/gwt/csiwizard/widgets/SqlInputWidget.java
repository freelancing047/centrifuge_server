package csi.client.gwt.csiwizard.widgets;

import csi.client.gwt.csiwizard.support.ParameterValidator;



public class SqlInputWidget extends TextInputWidget {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SqlInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {
        
        super(promptIn, null, defaultIn, validatorIn, requiredIn);
    }
    
    public SqlInputWidget(String promptIn, String defaultIn, boolean requiredIn) {
        
        super(promptIn, null, defaultIn, requiredIn);
    }

    public SqlInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn) {
        
        super(promptIn, null, defaultIn, validatorIn);
    }
    
    public SqlInputWidget(String promptIn, String defaultIn) {
        
        super(promptIn, null, defaultIn);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    // Replace this code with the proper validity check
    //
    protected Integrity checkInput(String userInputIn) {
        return super.checkInput(userInputIn);
    }
}

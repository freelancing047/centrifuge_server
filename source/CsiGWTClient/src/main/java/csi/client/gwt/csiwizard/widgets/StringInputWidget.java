package csi.client.gwt.csiwizard.widgets;

import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;


public class StringInputWidget extends TextInputWidget {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public StringInputWidget(String promptIn, ParameterValidator validatorIn, boolean requiredIn) {

       super(promptIn, validatorIn, requiredIn);
    }
    
    public StringInputWidget(String promptIn, boolean requiredIn) {

       super(promptIn, requiredIn);
    }
    
    public StringInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {

       super(promptIn, defaultIn, validatorIn, requiredIn);
    }
    
    public StringInputWidget(String promptIn, String defaultIn, boolean requiredIn) {

       super(promptIn, defaultIn, requiredIn);
    }
    
    public StringInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {

       super(promptIn, formatIn, defaultIn, validatorIn, requiredIn);
    }
    
    public StringInputWidget(String promptIn, String formatIn, String defaultIn, boolean requiredIn) {

       super(promptIn, formatIn, defaultIn, requiredIn);
    }
    
    
    
    public StringInputWidget(String promptIn, ParameterValidator validatorIn) {

       super(promptIn, validatorIn);
    }
    
    public StringInputWidget(String promptIn) {

       super(promptIn);
    }
    
    public StringInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn) {

       super(promptIn, defaultIn, validatorIn);
    }
    
    public StringInputWidget(String promptIn, String defaultIn) {

       super(promptIn, defaultIn);
    }
    
    public StringInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn) {

       super(promptIn, formatIn, defaultIn, validatorIn);
    }
    
    public StringInputWidget(String promptIn, String formatIn, String defaultIn) {

       super(promptIn, formatIn, defaultIn);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // routine for checking validity of the user input when it changes
    //
    @Override
    protected Integrity checkInput(String userInputIn) {
        
        return checkInput(userInputIn, 33);
    }
}

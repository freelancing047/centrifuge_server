package csi.client.gwt.csiwizard.widgets;

import java.util.Comparator;

import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.JdbcDriverParameterValidationType;


public class IntegerInputWidget extends TextInputWidget {

    private class ValueComparator implements Comparator<String> {

        @Override
        public int compare(String stringOneIn, String stringTwoIn) {

            Integer myValueOne = null;
            Integer myValueTwo = null;

            if (null != stringOneIn) {

                try {

                    myValueOne = Integer.valueOf(stringOneIn.trim());

                } catch (Exception myException) { }
            }
            if (null != stringTwoIn) {

                try {

                    myValueTwo = Integer.valueOf(stringTwoIn.trim());

                } catch (Exception myException) { }
            }
            if ((null == myValueOne) || (null == myValueTwo)) {

                Display.error(_constants.comparatorError_Title(CsiDataType.Integer.getLabel()),
                        _constants.comparatorError_Message());
            }

            return ((null != myValueOne) && (null != myValueTwo)) ? Integer.compare(myValueOne, myValueTwo) : 0;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public IntegerInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {
        
        super(promptIn, formatIn, defaultIn,
                (null != validatorIn)
                    ? validatorIn
                    : new ParameterValidator(JdbcDriverParameterValidationType.ISNUMBER, "true"),
                requiredIn, false);
        _trim = true;
        if (null != validatorIn) {
            
            validatorIn.add(JdbcDriverParameterValidationType.ISNUMBER, "true");
        }
    }

    public IntegerInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {
        
        this(promptIn, null, defaultIn, validatorIn, requiredIn);
    }
    
    public IntegerInputWidget(String promptIn, String formatIn, String defaultIn, boolean requiredIn) {
        
        this(promptIn, formatIn, defaultIn, null, requiredIn);
    }
    
    public IntegerInputWidget(String promptIn, String defaultIn, boolean requiredIn) {
        
        this(promptIn, null, defaultIn, null, requiredIn);
    }
    
    public IntegerInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn) {
        
        this(promptIn, formatIn, defaultIn, validatorIn, true);
    }

    public IntegerInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn) {
        
        this(promptIn, null, defaultIn, validatorIn, true);
    }
    
    public IntegerInputWidget(String promptIn, String formatIn, String defaultIn) {
        
        this(promptIn, formatIn, defaultIn, null, true);
    }
    
    public IntegerInputWidget(String promptIn, String defaultIn) {
        
        this(promptIn, null, defaultIn, null, true);
    }

    @Override
    public Comparator<String> getComparator() {

        return new ValueComparator();
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    @Override
    protected Integrity checkInput(String userInputIn) {
        
        Integrity myIntegrity = Integrity.OK_SO_FAR;

        if ((null != userInputIn) && (0 < userInputIn.length())) {
            
            String myMessage = _validator.checkInput(userInputIn);
            
            if (null == myMessage) {
                
                char myTest = userInputIn.charAt(0);
                boolean mySignFlag = (('+' == myTest) || ('-' == myTest));
                    
                int myLimit = userInputIn.length();
                int myRequiredCount = mySignFlag ? 2 : 1;
                    
                if (myRequiredCount <= myLimit) {
                    
                    myIntegrity = Integrity.COMPLETE;
                }
                
            } else {
                
               _errorText = myMessage;
               myIntegrity = Integrity.BROKEN;
            }
        }
        
        return myIntegrity;
    }
}

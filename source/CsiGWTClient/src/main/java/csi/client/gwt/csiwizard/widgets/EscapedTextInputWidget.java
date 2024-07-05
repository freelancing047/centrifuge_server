package csi.client.gwt.csiwizard.widgets;

import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.exception.CentrifugeException;


public class EscapedTextInputWidget extends TextInputWidget {


    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public EscapedTextInputWidget(String promptIn, ParameterValidator validatorIn) {

       super(promptIn, validatorIn);
    }
    
    public EscapedTextInputWidget(String promptIn) {

       super(promptIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn) {

       super(promptIn, defaultIn, validatorIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String defaultIn) {

       super(promptIn, defaultIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn) {

       super(promptIn, formatIn, defaultIn, validatorIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String formatIn, String defaultIn) {

       super(promptIn, formatIn, defaultIn);
    }
    
    public EscapedTextInputWidget(String promptIn, ParameterValidator validatorIn, boolean requiredIn) {

       super(promptIn, validatorIn, requiredIn);
    }
    
    public EscapedTextInputWidget(String promptIn, boolean requiredIn) {

       super(promptIn, requiredIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {

       super(promptIn, defaultIn, validatorIn, requiredIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String defaultIn, boolean requiredIn) {

       super(promptIn, defaultIn, requiredIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String formatIn, String defaultIn, ParameterValidator validatorIn, boolean requiredIn) {

       super(promptIn, formatIn, defaultIn, validatorIn, requiredIn);
    }
    
    public EscapedTextInputWidget(String promptIn, String formatIn, String defaultIn, boolean requiredIn) {

       super(promptIn, formatIn, defaultIn, requiredIn);
    }

    @Override
    public String getText() throws CentrifugeException {
        
        String mySource = super.getText();
        StringBuilder myBuffer = new StringBuilder();

        for (int i = 0; mySource.length() > i; ) {
            
            char myCharacter = mySource.charAt(i++);
            
            if ('\\' == myCharacter) {

            
                switch (myCharacter) {
                    
                    case 'b' :
                        
                        myBuffer.append('\b');
                        break;
                        
                    case 'f' :
                        
                        myBuffer.append('\f');
                        break;
                        
                    case 'n' :
                        
                        myBuffer.append('\n');
                        break;
                        
                    case 'r' :
                        
                        myBuffer.append('\r');
                        break;
                        
                    case 't' :
                        
                        myBuffer.append('\t');
                        break;
                        
                    case '\\' :
                        
                        myBuffer.append('\\');
                        break;
                        
                    default :
                        
                        myBuffer.append(getEscapeValue(mySource, (i - 1)));
                        i += 2;
                        break;
                }
                
            } else {
                
                myBuffer.append(myCharacter);
            }
        }
        return myBuffer.toString();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private char getEscapeValue(String sourceIn, int offsetIn) throws CentrifugeException {
        
        int myValue = 0;
        
        try {
            
            for (int i = offsetIn; offsetIn + 3 > i; i++) {
                
                char myCharacter = sourceIn.charAt(i++);
                
                if (('0' <= myCharacter) && ('7' >= myCharacter)) {

                    myValue = (8 * myValue) + myCharacter - '0';

                } else {
                    
                    throw new CentrifugeException(i18n.escapedTextInputException()); //$NON-NLS-1$
                }
            }
            return (char)myValue;
            
        } catch (Exception myException) {
            
            throw new CentrifugeException(i18n.escapedTextInputException()); //$NON-NLS-1$
        }
    }
}

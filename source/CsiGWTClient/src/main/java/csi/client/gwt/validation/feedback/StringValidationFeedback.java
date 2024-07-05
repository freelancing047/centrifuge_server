package csi.client.gwt.validation.feedback;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * @author Centrifuge Systems, Inc.
 */
public class StringValidationFeedback extends NoOpValidationFeedback {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final String error;
    private static StringValidationFeedback DUPLICATE_VISUALIZATION_FEEDBACK;
    private static StringValidationFeedback EMPTY_VISUALIZATION_FEEDBACK;

    public StringValidationFeedback(String error){
        this.error = error;
    }

    @Override
    public String getError() {
        return error;
    }
    
    public static StringValidationFeedback getDuplicateVisualizationFeedback(){
    	if(DUPLICATE_VISUALIZATION_FEEDBACK == null){
    		DUPLICATE_VISUALIZATION_FEEDBACK = new StringValidationFeedback(i18n.stringValidationFeedbackVisualizationsUniqueMessage()); //$NON-NLS-1$
    	}
    	return DUPLICATE_VISUALIZATION_FEEDBACK;
    }
    
    public static StringValidationFeedback getEmptyVisualizationFeedback(){
    	if(EMPTY_VISUALIZATION_FEEDBACK == null){
    		EMPTY_VISUALIZATION_FEEDBACK = new StringValidationFeedback(i18n.stringValidationFeedbackVisualizationEmptyNameValidation()); //$NON-NLS-1$
    	}
    	return EMPTY_VISUALIZATION_FEEDBACK;

    }
}

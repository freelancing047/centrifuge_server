package csi.client.gwt.validation.feedback;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NumberValidationFeedback extends NoOpValidationFeedback {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final String error;
    private static NumberValidationFeedback VALUE_NOT_DOUBLE_FEEDBACK;

    public NumberValidationFeedback(String error){
        this.error = error;
    }

    @Override
    public String getError() {
        return error;
    }
    
    public static NumberValidationFeedback getValueNotDoubleFeedback(){
    	if(VALUE_NOT_DOUBLE_FEEDBACK == null){
    		VALUE_NOT_DOUBLE_FEEDBACK = new NumberValidationFeedback(i18n.numberValidationFeedbackValueNotDoubleMessage()); //$NON-NLS-1$
    	}
    	return VALUE_NOT_DOUBLE_FEEDBACK;
    }
}

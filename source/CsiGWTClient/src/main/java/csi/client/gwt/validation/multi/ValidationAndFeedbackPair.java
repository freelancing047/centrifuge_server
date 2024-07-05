package csi.client.gwt.validation.multi;

import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.validator.Validator;

/**
 * Matches a Validator to it's feedback. Not meant to be sub-classed.
 * @author Centrifuge Systems, Inc.
 */
public final class ValidationAndFeedbackPair {
    private final Validator validator;
    private final ValidationFeedback validationFeedback;

    public ValidationAndFeedbackPair(Validator validator, ValidationFeedback validationFeedback) {
        this.validator = validator;
        this.validationFeedback = validationFeedback;
    }

    public Validator getValidator() {
        return validator;
    }

    public ValidationFeedback getValidationFeedback() {
        return validationFeedback;
    }

    public boolean validateWithFeedback(){
        boolean valid = getValidator().isValid();

        if(valid)
            getValidationFeedback().hideValidationFeedback();
        else
            getValidationFeedback().showValidationFeedback();

        return valid;
    }
}

package csi.client.gwt.validation.multi;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for aggregating multiple validation/feedback pairs.
 * @author Centrifuge Systems, Inc.
 */
public abstract class MultiValidator {

    private final List<ValidationAndFeedbackPair> validationAndFeedbackPairs;

    public MultiValidator() {
        this.validationAndFeedbackPairs = new ArrayList<ValidationAndFeedbackPair>();
    }

    public void addValidationAndFeedback(ValidationAndFeedbackPair validationAndFeedbackPair) {
        this.validationAndFeedbackPairs.add(validationAndFeedbackPair);
    }

    public abstract boolean validate();

    public List<ValidationAndFeedbackPair> getValidationAndFeedbackPairs() {
        return validationAndFeedbackPairs;
    }
}

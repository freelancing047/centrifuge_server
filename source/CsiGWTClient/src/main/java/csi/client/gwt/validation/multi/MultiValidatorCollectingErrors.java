package csi.client.gwt.validation.multi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MultiValidatorCollectingErrors extends MultiValidator {

    private final List<String> errors = new ArrayList<String>();

    @Override
    public boolean validate() {
        boolean allValid = true;
        errors.clear();
        for (ValidationAndFeedbackPair pair : getValidationAndFeedbackPairs()) {
            if (!pair.validateWithFeedback()) {
                errors.add(pair.getValidationFeedback().getError());
                allValid = false;
            }
        }
        return allValid;
    }

    public List<String> getErrors() {
        return errors;
    }
}

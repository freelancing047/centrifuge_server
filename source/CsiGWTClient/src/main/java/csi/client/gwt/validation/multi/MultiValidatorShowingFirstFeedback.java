package csi.client.gwt.validation.multi;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MultiValidatorShowingFirstFeedback extends MultiValidator {

    public boolean validate(){
        for (ValidationAndFeedbackPair pair : getValidationAndFeedbackPairs()) {
            if (!pair.validateWithFeedback()) {
                return false;
            }
        }
        return true;
    }
}

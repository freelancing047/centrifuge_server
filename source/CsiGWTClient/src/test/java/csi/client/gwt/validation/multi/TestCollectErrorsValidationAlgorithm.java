package csi.client.gwt.validation.multi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import csi.client.gwt.validation.ValidationJunitUtils;
import csi.client.gwt.validation.feedback.NoOpValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;

/**
 * Shows how to use the MultiValidatorCollectingErrors.
 * @author Centrifuge Systems, Inc.
 */
public class TestCollectErrorsValidationAlgorithm {

    static final String duplicateNameError = "Visualizations must have a unique name."; //$NON-NLS-1$
    static final String missingNameError = "Visualizations must have a name."; //$NON-NLS-1$

    public class ValidationFeedbackTestCase extends NoOpValidationFeedback {

        private final String error;

        public ValidationFeedbackTestCase(String errorStringIn) {

            error = errorStringIn;
        }

        @Override
        public String getError() {
            return error;
        }
    }

    @Test
    public void testCollectErrorsValidationAlgorithm(){

        MultiValidatorCollectingErrors validationAlgorithm = new MultiValidatorCollectingErrors();

        ValidationFeedback firstFeedback = new ValidationFeedbackTestCase(duplicateNameError);
        ValidationFeedback secondFeedback = new ValidationFeedbackTestCase(missingNameError);

        ValidationJunitUtils.setupTwoInvalidValidatorsForValidation(validationAlgorithm, firstFeedback, secondFeedback);

        assertFalse(validationAlgorithm.validate());
        assertTrue(validationAlgorithm.getErrors().size() == 2);

        assertTrue(validationAlgorithm.getErrors().contains(duplicateNameError));
        assertTrue(validationAlgorithm.getErrors().contains(missingNameError));

    }
}

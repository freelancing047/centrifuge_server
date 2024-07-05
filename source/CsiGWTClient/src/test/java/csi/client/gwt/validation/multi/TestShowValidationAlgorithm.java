package csi.client.gwt.validation.multi;

import static csi.client.gwt.validation.ValidationJunitUtils.createTakesValue;
import static csi.client.gwt.validation.ValidationJunitUtils.setupTwoInvalidValidatorsForValidation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import csi.client.gwt.validation.ValidationJunitUtils.WhatMethodIsCalledValidationFeedback;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.Validator;

/**
 * Tests different validation situations hide/show the correct feedback.
 * @author Centrifuge Systems, Inc.
 */
public class TestShowValidationAlgorithm {

    @Test
    public void testValidHidesFeedback(){
        MultiValidator multiValidator = new MultiValidatorShowingFirstFeedback();
        WhatMethodIsCalledValidationFeedback feedback = new WhatMethodIsCalledValidationFeedback();
        setupValidSituationForValidation(multiValidator, feedback);

        assertTrue(multiValidator.validate());
        assertHideIsCalled(feedback);
    }

    @Test
    public void testInvalidShowsFeedback(){
        MultiValidator multiValidator = new MultiValidatorShowingFirstFeedback();
        WhatMethodIsCalledValidationFeedback feedback = new WhatMethodIsCalledValidationFeedback();
        setupInvalidSituationForValidation(multiValidator, feedback);

        assertFalse(multiValidator.validate());
        assertShowIsCalled(feedback);
    }

    @Test
    public void testTwoInvalidsOnlyShowsTheFirstFeedback(){
        MultiValidator multiValidator = new MultiValidatorShowingFirstFeedback();
        WhatMethodIsCalledValidationFeedback firstFeedback = new WhatMethodIsCalledValidationFeedback();
        WhatMethodIsCalledValidationFeedback secondFeedback = new WhatMethodIsCalledValidationFeedback();
        setupTwoInvalidValidatorsForValidation(multiValidator, firstFeedback, secondFeedback);

        assertFalse(multiValidator.validate());
        assertShowIsCalled(firstFeedback);
        assertNeitherHideOrShowCalled(secondFeedback);

    }

    private void setupValidSituationForValidation(MultiValidator multiValidator, WhatMethodIsCalledValidationFeedback feedback) {
        Validator notBlankValidator = new NotBlankValidator(createTakesValue("hi"));

        ValidationAndFeedbackPair pair = new ValidationAndFeedbackPair(notBlankValidator, feedback);
        multiValidator.addValidationAndFeedback(pair);
    }

    private void setupInvalidSituationForValidation(MultiValidator multiValidator, WhatMethodIsCalledValidationFeedback feedback) {
        Validator notBlankValidator = new NotBlankValidator(createTakesValue(" "));

        ValidationAndFeedbackPair pair = new ValidationAndFeedbackPair(notBlankValidator, feedback);
        multiValidator.addValidationAndFeedback(pair);
    }

    private void assertHideIsCalled(WhatMethodIsCalledValidationFeedback feedback) {
        assertTrue(feedback.isHideCalled());
        assertFalse(feedback.isShowCalled());
    }

    private void assertShowIsCalled(WhatMethodIsCalledValidationFeedback feedback) {
        assertFalse(feedback.isHideCalled());
        assertTrue(feedback.isShowCalled());
    }

    private void assertNeitherHideOrShowCalled(WhatMethodIsCalledValidationFeedback feedback) {
        assertFalse(feedback.isHideCalled());
        assertFalse(feedback.isShowCalled());
    }


}

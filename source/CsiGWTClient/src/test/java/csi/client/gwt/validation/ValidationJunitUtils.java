package csi.client.gwt.validation;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.TakesValue;

import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.NotEmptyCollectionValidator;
import csi.client.gwt.validation.validator.Validator;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ValidationJunitUtils {

    public static TakesValue<String> createTakesValue(final String value) {
        return new TakesValue<String>() {
            @Override
            public void setValue(String value) {

            }

            @Override
            public String getValue() {
                return value;
            }
        };
    }

    public static void setupTwoInvalidValidatorsForValidation(MultiValidator multiValidator, ValidationFeedback firstFeedback, ValidationFeedback secondFeedback) {
        Validator notBlankValidator = new NotBlankValidator(createTakesValue("  "));

        Validator notEmptyCollectionValidator = new NotEmptyCollectionValidator(Lists.newArrayList());

        ValidationAndFeedbackPair pair1 = new ValidationAndFeedbackPair(notBlankValidator, firstFeedback);
        ValidationAndFeedbackPair pair2 = new ValidationAndFeedbackPair(notEmptyCollectionValidator, secondFeedback);

        multiValidator.addValidationAndFeedback(pair1);
        multiValidator.addValidationAndFeedback(pair2);
    }

    public static class WhatMethodIsCalledValidationFeedback implements ValidationFeedback {

        private boolean showCalled = false;
        private boolean hideCalled = false;

        public boolean isShowCalled() {
            return showCalled;
        }

        public boolean isHideCalled() {
            return hideCalled;
        }

        @Override
        public void showValidationFeedback() {
            showCalled = true;
        }

        @Override
        public void hideValidationFeedback() {
            hideCalled = true;
        }

        @Override
        public String getError(){
            return "";
        }
    }
}

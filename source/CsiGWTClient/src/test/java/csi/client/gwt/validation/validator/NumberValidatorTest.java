package csi.client.gwt.validation.validator;

import static org.junit.Assert.assertTrue;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NumberValidatorTest extends NumericalValidatorBase {

    @Override
    protected Validator createValidator(TakesValue<String> takesValue) {
        return new NumberValidator(takesValue);
    }

    @Override
    protected void assertNonInteger(Validator validator) {
        assertTrue(validator.isValid());
    }

    @Override
    protected void assertInteger(Validator validator) {
        assertTrue(validator.isValid());
    }

    @Override
    protected void assertNegativeInteger(Validator validator) {
        assertTrue(validator.isValid());
    }
}

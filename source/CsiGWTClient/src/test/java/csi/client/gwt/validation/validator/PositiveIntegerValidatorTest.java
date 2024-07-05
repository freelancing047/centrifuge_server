package csi.client.gwt.validation.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PositiveIntegerValidatorTest extends NumericalValidatorBase {
    @Override
    protected Validator createValidator(TakesValue<String> takesValue) {
        return new PositiveIntegerValidator(takesValue);
    }

    @Override
    protected void assertNonInteger(Validator validator) {
        assertFalse(validator.isValid());
    }

    @Override
    protected void assertInteger(Validator validator) {
        assertTrue(validator.isValid());
    }

    @Override
    protected void assertNegativeInteger(Validator validator) {
        assertFalse(validator.isValid());
    }
}

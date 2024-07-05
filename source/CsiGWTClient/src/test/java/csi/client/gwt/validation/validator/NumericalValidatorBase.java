package csi.client.gwt.validation.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gwt.user.client.TakesValue;

import csi.client.gwt.validation.ValidationJunitUtils;

/**
 * @author Centrifuge Systems, Inc.
 */
public abstract class NumericalValidatorBase {

    @Test
    public void testInteger(){
        TakesValue<String> valid = ValidationJunitUtils.createTakesValue("3");
        Validator validator = createValidator(valid);

        assertInteger(validator);
    }

    @Test
    public void testNonInteger(){
        TakesValue<String> valid = ValidationJunitUtils.createTakesValue("3.8");
        Validator validator = createValidator(valid);

        assertNonInteger(validator);
    }

    @Test
    public void testNegativeInteger(){
        TakesValue<String> valid = ValidationJunitUtils.createTakesValue("-2");
        Validator validator = createValidator(valid);

        assertNegativeInteger(validator);
    }

    @Test
    public void testStringValue(){
        TakesValue<String> valid = ValidationJunitUtils.createTakesValue("s");
        Validator validator = createValidator(valid);

        assertFalse(validator.isValid());
    }

    @Test
    public void testNull(){
        TakesValue<String> valid = ValidationJunitUtils.createTakesValue(null);
        Validator validator = createValidator(valid);

        assertTrue(validator.isValid());
    }

    @Test
    public void testEmpty(){
        TakesValue<String> valid = ValidationJunitUtils.createTakesValue("");
        Validator validator = createValidator(valid);

        assertTrue(validator.isValid());
    }

    protected abstract Validator createValidator(TakesValue<String> takesValue);
    protected abstract void assertNonInteger(Validator validator);
    protected abstract void assertInteger(Validator validator);
    protected abstract void assertNegativeInteger(Validator validator);

}

package csi.client.gwt.validation.validator;

import static csi.client.gwt.validation.ValidationJunitUtils.createTakesValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NotBlankValidatorTest {

    @Test
    public void testNullIsInvalid(){
        TakesValue<String> nullTakesValue = createTakesValue(null);

        NotBlankValidator notBlankValidator = new NotBlankValidator(nullTakesValue);
        assertFalse(notBlankValidator.isValid());
    }

    @Test
    public void testEmptyIsInvalid(){
        TakesValue<String> nullTakesValue = createTakesValue("");

        NotBlankValidator notBlankValidator = new NotBlankValidator(nullTakesValue);
        assertFalse(notBlankValidator.isValid());
    }

    @Test
    public void testSpaceIsInvalid(){
        TakesValue<String> nullTakesValue = createTakesValue("   ");

        NotBlankValidator notBlankValidator = new NotBlankValidator(nullTakesValue);
        assertFalse(notBlankValidator.isValid());
    }

    @Test
    public void testStringIsValid(){
        TakesValue<String> nullTakesValue = createTakesValue("hi");

        NotBlankValidator notBlankValidator = new NotBlankValidator(nullTakesValue);
        assertTrue(notBlankValidator.isValid());
    }

}

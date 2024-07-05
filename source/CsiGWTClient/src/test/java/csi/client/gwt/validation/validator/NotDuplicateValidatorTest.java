package csi.client.gwt.validation.validator;

import static csi.client.gwt.validation.ValidationJunitUtils.createTakesValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NotDuplicateValidatorTest {

    //Don't pass in null to either argument..
    @Test(expected = NullPointerException.class)
    public void testNulls(){
        TakesValue<String> takesValue = createTakesValue("Hi");
        NotDuplicateValidatorTrimAndIgnoreCase validator = new NotDuplicateValidatorTrimAndIgnoreCase(takesValue, null);
        validator.isValid();
    }
    @Test(expected = NullPointerException.class)
    public void testNullTakesValue(){
        TakesValue<String> nullString = createTakesValue(null);
        NotDuplicateValidatorTrimAndIgnoreCase validator = new NotDuplicateValidatorTrimAndIgnoreCase(nullString, Lists.newArrayList("hello"));
        validator.isValid();
    }

    @Test
    public void testInCollection(){
        TakesValue<String> helloString = createTakesValue("hello");
        NotDuplicateValidatorTrimAndIgnoreCase validator = new NotDuplicateValidatorTrimAndIgnoreCase(helloString, Lists.newArrayList("hello"));

        assertFalse(validator.isValid());
    }

    @Test
    public void testInCollectionWithTrimAndCase(){
        TakesValue<String> helloString = createTakesValue("hello");
        NotDuplicateValidatorTrimAndIgnoreCase validator = new NotDuplicateValidatorTrimAndIgnoreCase(helloString, Lists.newArrayList("  hELLo "));

        assertFalse(validator.isValid());
    }

    @Test
    public void testNotInCollection(){
        TakesValue<String> helloString = createTakesValue("hello");
        NotDuplicateValidatorTrimAndIgnoreCase validator = new NotDuplicateValidatorTrimAndIgnoreCase(helloString, Lists.newArrayList("helloo", "hi", "world"));

        assertTrue(validator.isValid());
    }


}

package csi.client.gwt.validation.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NotEmptyCollectionValidatorTest {

    @Test
    public void testNull(){
        NotEmptyCollectionValidator validator = new NotEmptyCollectionValidator(null);
        assertFalse(validator.isValid());
    }

    @Test
    public void testEmpty(){
        NotEmptyCollectionValidator validator = new NotEmptyCollectionValidator(Lists.newArrayList());
        assertFalse(validator.isValid());
    }

    @Test
    public void testNonEmpty(){
        NotEmptyCollectionValidator validator = new NotEmptyCollectionValidator(Sets.newHashSet("Hi"));
        assertTrue(validator.isValid());
    }
}

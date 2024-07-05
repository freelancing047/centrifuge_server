package csi.client.gwt.viz.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.google.common.collect.Lists;

import csi.client.gwt.util.name.UniqueNameUtil;

/**
 * @author Centrifuge Systems, Inc.
 */
public class UniqueNameUtilTest {

    private static final String VALUE = "value";

    @Test(expected = NullPointerException.class)
    public void testNullThrowsNPE(){
        UniqueNameUtil.getDistinctName(null, null);
    }

    @Test
    public void testEmptyListNullString(){
        String name = UniqueNameUtil.getDistinctName(new ArrayList<String>(), null);
        assertTrue(name == null);
    }

    @Test
    public void testEmptyListAndString(){
        String name = UniqueNameUtil.getDistinctName(new ArrayList<String>(), "");
        assertEquals("", name);
    }

    @Test
    public void testStringValueWithEmptyList(){
        String name = UniqueNameUtil.getDistinctName(new ArrayList<String>(), VALUE);
        assertEquals(VALUE, name);
    }

    @Test
    public void testStringValueWithValueInsideList(){
        String name = UniqueNameUtil.getDistinctName(Lists.newArrayList(VALUE), VALUE);
        assertEquals(VALUE + " (1)", name);
    }

    @Test
    public void testStringValueWithTwoValues(){
        ArrayList<String> strings = Lists.newArrayList(VALUE, VALUE+"(1)");
        String name = UniqueNameUtil.getDistinctName(strings, VALUE);
        assertEquals(VALUE + " (1)", name);

        strings.add(name);
        name = UniqueNameUtil.getDistinctName(strings, VALUE);
        assertEquals(VALUE + " (2)", name);
    }

    @Test
    public void testStringValueWithThreeValues(){
        ArrayList<String> strings = Lists.newArrayList(VALUE);
        String name = UniqueNameUtil.getDistinctName(strings, VALUE);
        assertEquals(VALUE + " (1)", name);

        strings.add(name);
        name = UniqueNameUtil.getDistinctName(strings, VALUE);
        assertEquals(VALUE + " (2)", name);

        strings.add(name);
        name = UniqueNameUtil.getDistinctName(strings, VALUE);
        assertEquals(VALUE + " (3)", name);

    }

    @Test
    public void testStringValueWithSpacedValue(){
        ArrayList<String> strings = Lists.newArrayList(VALUE, VALUE + " (1)");
        String name = UniqueNameUtil.getDistinctName(strings, VALUE);
        assertEquals(VALUE + " (2)", name);
    }


}

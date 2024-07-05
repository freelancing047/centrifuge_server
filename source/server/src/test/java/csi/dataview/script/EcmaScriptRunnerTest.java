package csi.dataview.script;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import javax.script.Bindings;

import org.junit.Before;
import org.junit.Test;

import csi.config.Configuration;
import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;


public class EcmaScriptRunnerTest {

    CsiScriptRunner runner = new EcmaScriptRunner();

    @Before
    public void setUp() throws Exception {
        Configuration.setInstance(new Configuration());
        Configuration.getInstance().getFeatureToggleConfig().setScriptingEnabled(true);
    }

    /* 
     * Basic tests of general script engine functionality
     * 
     */
    // this also test invoking a global function
    @Test
    public void testBeautifyScript() throws Exception {
        String script = runner.beautifyScript("var x=3; if (x==1 ) { x=2;} else {x = 3;}");
        String expected = "var x = 3;\n" + "if (x == 1) {\n" + "    x = 2;\n" + "} else {\n" + "    x = 3;\n" + "}";
        assertEquals(expected, script);
    }

    // this also test using global instance of helper
    @Test
    public void testEvalExpressionNoBinding() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.trim('  testtext  '); "), bindings);
        assertEquals("testtext", ret);
    }

    // test using binding variable
    @Test
    public void testEvalExpressionWithBinding() throws Exception {
        Bindings bindings = runner.createBindings();
        bindings.put("x", "  testtext  ");

        String ret = (String) runner.evalExpression(("CSI.trim(x); "), bindings);
        assertEquals("testtext", ret);
    }

    // test invoking method on a scripted object
    @Test
    public void testInvokeMethod() throws Exception {
        // get the global helper instance as the target object
        // to invoke the trim method on
        Bindings bindings = runner.getGlobalBindings();
        Object helper = bindings.get("CSI");
        String ret = (String) runner.invokeMethod(helper, "trim", "  testtext  ");

        assertEquals("testtext", ret);
    }

    /* 
     * 
     * Test helper functions
     * 
     */
    @Test
    public void testLength() throws Exception {
        Bindings bindings = runner.createBindings();
        Integer ret = (Integer) runner.evalExpression(("CSI.length('1234'); "), bindings);
        assertEquals(Integer.valueOf(4), ret);
    }

    @Test
    public void testReverseString() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.reverseString('testtext'); "), bindings);
        assertEquals("txettset", ret);
    }

    @Test
    public void testTrim() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.trim('  testtext  '); "), bindings);
        assertEquals("testtext", ret);
    }

    @Test
    public void testLTrim() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.ltrim('  testtext  '); "), bindings);
        assertEquals("testtext  ", ret);
    }

    @Test
    public void testRTrim() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.rtrim('  testtext  '); "), bindings);
        assertEquals("  testtext", ret);
    }

    @Test
    public void testIndex() throws Exception {
        Bindings bindings = runner.createBindings();
        Integer ret = (Integer) runner.evalExpression(("CSI.index('testtext', 'ex'); "), bindings);
        assertEquals(Integer.valueOf(5), ret);
    }

    @Test
    public void testLeft() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.left('testtext', 4); "), bindings);
        assertEquals("test", ret);
    }

    @Test
    public void testRight() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.right('testtext', 4); "), bindings);
        assertEquals("text", ret);
    }

    @Test
    public void testSubstr() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.substr('testtext', 3,2); "), bindings);
        assertEquals("st", ret);
    }

    @Test
    public void testToken() throws Exception {
        Bindings bindings = runner.createBindings();

        String ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', '2', '|'); "), bindings);
        assertEquals("b", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', '2-4', '|'); "), bindings);
        assertEquals("bcd", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', '-3', '|'); "), bindings);
        assertEquals("b", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', '-2', '|'); "), bindings);
        assertEquals("c", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', 2, '|'); "), bindings);
        assertEquals("b", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', 2-4, '|'); "), bindings);
        assertEquals("c", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', -3, '|'); "), bindings);
        assertEquals("b", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', -2, '|'); "), bindings);
        assertEquals("c", ret);

        ret = (String) runner.evalExpression(("CSI.token('a|b|c|d', 'fake', '|'); "), bindings);
        assertEquals(null, ret);

        ret = (String) runner.evalExpression(("CSI.token('a,b,c,d', '2', ','); "), bindings);
        assertEquals("b", ret);

        ret = (String) runner.evalExpression(("CSI.token('a-b-c-d', '2-4', '-'); "), bindings);
        assertEquals("bcd", ret);
    }

    @Test
    public void testTokenCount() throws Exception {
        Bindings bindings = runner.createBindings();
        Double ret = (Double) runner.evalExpression(("CSI.tokenCount('a,b,c', ',');"), bindings);
        assertEquals(Double.valueOf(3), ret);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testYear() throws Exception {
        Bindings bindings = runner.createBindings();
        Integer ret = (Integer) runner.evalExpression(("CSI.year('12/12/1969'); "), bindings);
        assertEquals(Integer.valueOf(1969), ret);

        Integer ret2 = (Integer) runner.evalExpression(("CSI.year(" + Date.parse("12/12/1969") + "); "), bindings);
        assertEquals(Integer.valueOf(1969), ret2);
    }

    @Test
    public void testMonthYear() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.monthYear(" + Date.parse("08/12/1999") + "); "), bindings);
        assertEquals("08/1999", ret);
    }

    @Test
    public void testLike() throws Exception {
        Bindings bindings = runner.createBindings();
        Boolean ret = (Boolean) runner.evalExpression(("CSI.like('blahhelloblah', '*hello'); "), bindings);
        assertEquals(Boolean.TRUE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.like('zhello?', '[z]hello?'); "), bindings);
        assertEquals(Boolean.TRUE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.like('ghello', '[!z]hello'); "), bindings);
        assertEquals(Boolean.TRUE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.like('zhello', '[!z]hello'); "), bindings);
        assertEquals(Boolean.FALSE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.like('a2a', 'a#a'); "), bindings);
        assertEquals(Boolean.TRUE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.like('axa', 'a#a'); "), bindings);
        assertEquals(Boolean.FALSE, ret);
    }

    @Test
    public void testBetween() throws Exception {
        Bindings bindings = runner.createBindings();
        Boolean ret = (Boolean) runner.evalExpression(("CSI.between(6, 5, 7); "), bindings);
        assertEquals(Boolean.TRUE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.between(6, 7, 9); "), bindings);
        assertEquals(Boolean.FALSE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.between('rst', 'rat', 'rxt'); "), bindings);
        assertEquals(Boolean.TRUE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.between(new Date('06/16/1999'), '07/08/1998', '07/08/2000'); "), bindings);
        assertEquals(Boolean.TRUE, ret);
    }

    @Test
    public void testIsIn() throws Exception {
        Bindings bindings = runner.createBindings();
        Boolean ret = (Boolean) runner.evalExpression(("CSI.isIn('a', 'x, y, a, b'); "), bindings);
        assertEquals(Boolean.TRUE, ret);

        ret = (Boolean) runner.evalExpression(("CSI.isIn(2, '1, 2, 3, 4'); "), bindings);
        assertEquals(Boolean.TRUE, ret);
    }

    @Test
    public void testIfOp() throws Exception {
        Bindings bindings = runner.createBindings();
        String ret = (String) runner.evalExpression(("CSI.ifOp('a', 'a', '==', 'hello', 'goodbye' ); "), bindings);
        assertEquals("hello", ret);

        ret = (String) runner.evalExpression(("CSI.ifOp('a', 'b', '==', 'hello', 'goodbye' ); "), bindings);
        assertEquals("goodbye", ret);

        ret = (String) runner.evalExpression(("CSI.ifOp('a', 'b', '>', 'hello', 'goodbye' ); "), bindings);
        assertEquals("goodbye", ret);

        ret = (String) runner.evalExpression(("CSI.ifOp('a', 'b', '<', 'hello', 'goodbye' ); "), bindings);
        assertEquals("hello", ret);

        Double iret = (Double) runner.evalExpression(("CSI.ifOp(1, 1, '==', 10, 20 ); "), bindings);
        assertEquals(new Double(10.0).intValue(), iret.intValue());
    }

    @Test
    public void testAbs() throws Exception {
        Bindings bindings = runner.createBindings();
        Double ret = (Double) runner.evalExpression(("CSI.abs(-34); "), bindings);
        assertEquals(new Double(34), ret);
    }

    @Test
    public void testLoge() throws Exception {
        Bindings bindings = runner.createBindings();
        double ret = (Double) runner.evalExpression(("CSI.loge(1.53); "), bindings);
        assertEquals(Math.log(1.53), ret, 0.2);
    }

    @Test
    public void testLog10() throws Exception {
        Bindings bindings = runner.createBindings();
        double ret = (Double) runner.evalExpression(("CSI.log10(1.53); "), bindings);
        assertEquals(Math.log(1.53) / Math.log(10), ret, 0.2);
    }

    @Test
    public void testRound() throws Exception {
        Bindings bindings = runner.createBindings();
        Long ret = (Long) runner.evalExpression(("CSI.round(1.53); "), bindings);
        assertEquals(new Long(2), ret);
    }

    @Test
    public void testCeil() throws Exception {
        Bindings bindings = runner.createBindings();
        Double ret = (Double) runner.evalExpression(("CSI.ceil(1.13); "), bindings);
        assertEquals(new Double(2), ret);
    }

    @Test
    public void testFloor() throws Exception {
        Bindings bindings = runner.createBindings();
        Double ret = (Double) runner.evalExpression(("CSI.floor(1.13); "), bindings);
        assertEquals(new Double(1), ret);
    }

    @Test
    public void testUpdateReferences() throws Exception {
        String text = "yasdf dsa(dff(())+sfdfdsfs(csiRow.get   (  '   hello  '  )  )bleah blah";

        String s = runner.updateFieldReferences(text, "HELLO", "goodbye");
        String expected = "yasdf dsa(dff(())+sfdfdsfs(csiRow.get('goodbye')  )bleah blah";
        //System.out.println(s);
        assertEquals(expected, s);

    }
}

package csi.server.business.helper.field;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.OrderedField;
import csi.server.common.model.functions.ConcatFunction;
import csi.server.common.model.functions.MathFunction;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.common.model.operator.OperatorType;

/**
 * @author Centrifuge Systems, Inc.
 */
public class FieldCycleDetectorTest {

    private static final String SCRIPT_FIELD_NAME = "Script Name";
    private static final String CONCAT_FIELD_NAME = "Concat";

    @Test
    public void testNull(){
        //FieldCycleDetector fieldCycleDetector = new FieldCycleDetector(new DataModelDef());
        //assertFalse(fieldCycleDetector.detectCycle(null));
    }

    @Test
    public void testNonScript(){
        FieldDef fieldDef = createStaticFieldDef();

        assertCycles(Lists.newArrayList(fieldDef), false);
    }

    @Test
    public void testSimpleScript(){
        FieldDef fieldDef = createSimpleScriptFieldDef("var csiResult = '5'");

        assertCycles(Lists.newArrayList(fieldDef), false);
    }

    @Test
    public void testSelfReference(){
        FieldDef fieldDef = createSimpleScriptFieldDef("var csiResult = csiRow.get('" + SCRIPT_FIELD_NAME + "')");

        assertCycles(Lists.newArrayList(fieldDef), true);
    }

    @Test
    public void testConcatScript(){
        FieldDef fieldDef1 = createSimpleScriptFieldDef("var csiResult = '5'");
        FieldDef fieldDef2 = createSimpleScriptFieldDef("var csiResult = 'E'");
        FieldDef fieldDef = createConcatFieldDef(fieldDef1, fieldDef2);

        assertCycles(Lists.newArrayList(fieldDef, fieldDef1, fieldDef2), false);
    }

    @Test
    public void testConcatScriptDuplicate(){
        FieldDef fieldDef1 = createSimpleScriptFieldDef("var csiResult = '5'");
        FieldDef fieldDef = createConcatFieldDef(fieldDef1, fieldDef1);

        assertCycles(Lists.newArrayList(fieldDef, fieldDef1), false);
    }

    @Test
    public void testConcatScriptWithScriptCycle(){
        FieldDef fieldDef1 = createSimpleScriptFieldDef("var csiResult = csiRow.get('" + CONCAT_FIELD_NAME + "')");
        FieldDef fieldDef2 = createSimpleScriptFieldDef("var csiResult = 'E'");
        FieldDef fieldDef = createConcatFieldDef(fieldDef1, fieldDef2);

        fieldDef.setFieldName(CONCAT_FIELD_NAME);
        fieldDef1.setFieldName("fieldDef1");
        fieldDef2.setFieldName("fieldDef2");

        assertCycles(Lists.newArrayList(fieldDef, fieldDef1, fieldDef2), true);
    }

    @Test
    public void testMathFunction(){
        FieldDef fieldDef1 = createStaticFieldDef();
        FieldDef fieldDef2 = createStaticFieldDef();
        FieldDef fieldDef3 = createMathFieldDef(fieldDef1, fieldDef2);
        FieldDef fieldDef4 = createMathFieldDef(fieldDef3, fieldDef1);

        fieldDef3.setFieldName(CONCAT_FIELD_NAME + 1);
        fieldDef4.setFieldName(CONCAT_FIELD_NAME + 2);

        assertCycles(Lists.newArrayList(fieldDef1, fieldDef2, fieldDef3, fieldDef4), false);
    }

    private void assertCycles(List<FieldDef> fieldDefs, boolean shouldCyclesExist) {
        boolean cycleDetected = false;
        DataModelDef myModel = new DataModelDef();
        myModel.setFieldDefs(fieldDefs);
        for (FieldDef fieldDef : fieldDefs) {
//            FieldCycleDetector fieldCycleDetector = new FieldCycleDetector(myModel);
//            if(!shouldCyclesExist){
//                assertFalse("Cycle detected on field: " + fieldDef.getFieldName(), fieldCycleDetector.detectCycle(fieldDef));
//            }
//            else{
//                if(fieldCycleDetector.detectCycle(fieldDef)) {
//                    cycleDetected = true;
//                    System.out.println(fieldCycleDetector.getEncounteredFields());
//                    assertFalse(fieldCycleDetector.getEncounteredFields().size() == 0);
//                }
//            }
        }
        assertTrue("A cycle was not found", cycleDetected == shouldCyclesExist);
    }

    @Test
    public void testFakeCycle(){
        FieldDef fieldDef1 = createStaticFieldDef();
        FieldDef fieldDef2 = createStaticFieldDef();
        FieldDef fieldDef3 = createMathFieldDef(fieldDef1, fieldDef2);
        FieldDef fieldDef4 = createMathFieldDef(fieldDef3, fieldDef1);
        FieldDef fieldDef5 = createMathFieldDef(fieldDef3, fieldDef4);

        fieldDef3.setFieldName("fieldDef3");
        fieldDef4.setFieldName("fieldDef4");
        fieldDef5.setFieldName("fieldDef5");

        assertCycles(Lists.newArrayList(fieldDef1, fieldDef2, fieldDef3, fieldDef4, fieldDef5), false);
    }

    private FieldDef createConcatFieldDef(FieldDef fieldDef1, FieldDef fieldDef2) {
        FieldDef fd = createSimpleScriptFieldDef("");
        ConcatFunction concatFunction = new ConcatFunction();

        OrderedField orderedField1 = new OrderedField();
        orderedField1.setFieldDef(fieldDef1);
        orderedField1.setOrdinal(0);

        OrderedField orderedField2 = new OrderedField();
        orderedField2.setFieldDef(fieldDef2);
        orderedField2.setOrdinal(1);

        concatFunction.setFields(Lists.newArrayList(orderedField1, orderedField2));

        fd.setFunctions(Lists.newArrayList((ScriptFunction)concatFunction));
        return fd;
    }

    private FieldDef createMathFieldDef(FieldDef fieldDef1, FieldDef fieldDef2) {
        FieldDef fd = createSimpleScriptFieldDef("");
        MathFunction mathFunction = new MathFunction();

        mathFunction.setField1(fieldDef1);
        mathFunction.setField2(fieldDef2);
        mathFunction.setOperator(OperatorType.ADD);

        fd.setFunctions(Lists.newArrayList((ScriptFunction)mathFunction));
        return fd;
    }

    private FieldDef createStaticFieldDef() {
        FieldDef fieldDef = new FieldDef();
        fieldDef.setFieldName("Static Name");
        fieldDef.setFieldType(FieldType.STATIC);
        fieldDef.setStaticText("Static Value");
        return fieldDef;
    }

    private FieldDef createSimpleScriptFieldDef(String scriptText) {
        FieldDef fieldDef = new FieldDef();
        fieldDef.setFieldName(SCRIPT_FIELD_NAME);
        fieldDef.setFieldType(FieldType.SCRIPTED);
        fieldDef.setScriptText(scriptText);
        return fieldDef;
    }
}

package csi.server.business.helper.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.OrderedField;
import csi.server.common.model.functions.ConcatFunction;
import csi.server.common.model.functions.DurationFunction;
import csi.server.common.model.functions.MathFunction;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.common.model.functions.SubstringFunction;
import csi.server.common.util.ValuePair;

/**
 * @author Centrifuge Systems, Inc.
 * Detects when a FieldDef has a cycle, meaning it references fields which reference itself.
 * encounteredFields will describe how the cycle was found, if any exist.
 */
public class FieldCycleDetector {

    private FieldListAccess model;
    private FieldDef fieldDefFailure = null;
    private FieldDef fieldDefUnderTest = null;
    private final Set<FieldDef> encounteredFields = new HashSet<FieldDef>();
    private final List<FieldDef> allFieldDefs;

    public static FieldDef validateAllFields(FieldListAccess modelIn) {

        FieldDef myFailure = null;

        for (FieldDef myField : modelIn.getDependentFieldDefs()) {

            FieldCycleDetector myDetector = new FieldCycleDetector(modelIn);

            if (myDetector.detectCycle(myField)) {

                myFailure = myDetector.getFieldDefFailure();
            }
        }
        return myFailure;
    }

    public static int markUncachedFields(FieldListAccess modelIn) {

        int myLimit = 1;
        Map<String, ValuePair<FieldDef, Map<String, FieldDef>>>
                myDependentFields = getDerivedFieldInterDependencies(modelIn);
        Collection<ValuePair<FieldDef, Map<String, FieldDef>>> listOne = myDependentFields.values();
        List<ValuePair<FieldDef, Map<String, FieldDef>>> listTwo
                = new ArrayList<ValuePair<FieldDef, Map<String, FieldDef>>>();

        for (FieldDef myField : modelIn.getFieldDefList()) {

            myField.setLevel(0);
        }
        for (ValuePair<FieldDef, Map<String, FieldDef>> myPair : myDependentFields.values()) {

            myPair.getValue1().setLevel(10000);
        }
        for (int i = 0; myDependentFields.size() >= i; i++) {

            for (ValuePair<FieldDef, Map<String, FieldDef>> myPair : listOne) {

                if (allSatisfied(myPair.getValue2().values(), i)) {

                    myPair.getValue1().setLevel(i);

                } else {

                    listTwo.add(myPair);
                }
            }
            listOne = listTwo;
            listTwo = new ArrayList<ValuePair<FieldDef, Map<String, FieldDef>>>();
            myLimit++;
            if (0 == listOne.size()) {

                break;
            }
        }
        for (FieldDef myField : modelIn.getOrderedActiveFieldDefs()) {

            myField.invertLevel(myLimit - 1);
        }
        return myLimit;
    }

    public FieldCycleDetector(FieldListAccess modelIn){
        model = modelIn;
        allFieldDefs = (null != model) ? model.getFieldDefList() : null;
    }

    public FieldDef getFieldDefFailure() {

        return fieldDefFailure;
    }

    public boolean detectCycle(FieldDef fieldDef) {

        if(fieldDefHasNoDependencies(fieldDef)) {
            return false;
        }

        encounteredFields.add(fieldDef);
        if (fieldDef.equals(fieldDefUnderTest)) {
            fieldDefFailure = fieldDefUnderTest;
            return true;
        }

        if(fieldDefUnderTest == null){
            fieldDefUnderTest = fieldDef;
        }

        for (FieldDef child : getDependencies(fieldDef)) {
            if (detectCycle(child)) {
                return true;
            }
        }
        return false;
    }

    public List<FieldDef> getOrderedDirtyScriptedFieldDefs() {

        Map<String, FieldDef> myDirtyMap = new TreeMap<String, FieldDef>();
        Set<String> myDirtySet = new TreeSet<String>();
        EcmaScriptRunner myScriptRunner = new EcmaScriptRunner();

        for (FieldDef myField : model.getOrderedFieldSet()) {

            if (myField.isDirty()) {

                myDirtySet.add(myField.getName());

                if (FieldType.SCRIPTED.equals(myField.getFieldType())) {

                    myDirtyMap.put(myField.getName(), myField);
                }

            } else if (FieldType.SCRIPTED.equals(myField.getFieldType())) {
//                        && isDependent(myScriptRunner, myField, myDirtySet)) {

                myDirtyMap.put(myField.getName(), myField);
            }
        }
        return new ArrayList<FieldDef>(myDirtyMap.values());
    }

    public boolean isDependent(EcmaScriptRunner scriptRunnerIn, FieldDef fieldIn, Set<String> changeSetIn) {

        if ((null != changeSetIn) && (0 < changeSetIn.size())) {

            List<ScriptFunction> myFunctions = (null != fieldIn) ? fieldIn.getFunctions() : null;
            String myScript = fieldIn.getScriptText();

            if ((null != myFunctions) && (0 < myFunctions.size())) {

                for(ScriptFunction myFunction : myFunctions) {

                    if (hasDependency(myFunction, changeSetIn)) {

                        return true;
                    }
                }
            }
            if ((null != myScript) && (0 < myScript.length())) {

                return hasDependency(scriptRunnerIn, myScript, changeSetIn);
            }
        }
        return false;
    }

    public Set<FieldDef> getEncounteredFields() {
        return encounteredFields;
    }

    private static boolean allSatisfied(Collection<FieldDef> dependencyListIn, int levelIn) {

        for (FieldDef myField : dependencyListIn) {

            if (myField.getLevel() >= levelIn) {

                return false;
            }
        }
        return true;
    }

    private static Map<String, ValuePair<FieldDef, Map<String, FieldDef>>>
    getDerivedFieldInterDependencies(FieldListAccess modelIn) {

        Map<String, ValuePair<FieldDef, Map<String, FieldDef>>> myResult
                = new TreeMap<String, ValuePair<FieldDef, Map<String, FieldDef>>>();

        for (FieldDef myField : modelIn.getOrderedActiveFieldDefs()) {

            Map<String, FieldDef> myMap = myField.getSqlExpression().mapRequiredFields(null, modelIn);

            if ((null != myMap) && (0 < myMap.size())) {

                Map<String, FieldDef> myDependencyMap = new TreeMap<String, FieldDef>();

                for (Map.Entry<String, FieldDef> myPair : myMap.entrySet()) {

                    String myKey = myPair.getKey();
                    FieldDef myDependency = myPair.getValue();

                    if ((FieldType.DERIVED.equals(myDependency.getFieldType()))
                            && (!myDependency.isPreCalculated())) {

                        myDependencyMap.put(myKey, myDependency);
                    }
                }
                if (0 < myDependencyMap.size()) {

                    myResult.put(myField.getLocalId(),
                            new ValuePair<FieldDef, Map<String, FieldDef>>(myField, myDependencyMap));
                }
            }
        }
        return myResult;
    }

    private Set<FieldDef> getDependencies(FieldDef fieldDef) {
        Set<FieldDef> children = new HashSet<FieldDef>();

        if(fieldDefHasNoDependencies(fieldDef))
            return children;

        if (FieldType.SCRIPTED.equals(fieldDef.getFieldType())) {

            if(fieldDef.getFunctions() != null) {
                for (ScriptFunction scriptFunction : fieldDef.getFunctions()) {
                    children.addAll(getFieldsWithin(scriptFunction));
                }
            }
            children.addAll(findDependenciesInScriptText(fieldDef.getScriptText()));

        } else if (FieldType.DERIVED.equals(fieldDef.getFieldType())) {

            Map<String, FieldDef> myMap = fieldDef.getSqlExpression().mapRequiredFields(null, model);

            if ((null != myMap) && (0 < myMap.size())) {

                children.addAll(myMap.values());
            }
        }
        return children;
    }

    private List<FieldDef> findDependenciesInScriptText(String scriptText) {
        List<FieldDef> fieldsWithin = new ArrayList<FieldDef>();
        if(StringUtils.isEmpty(scriptText)){
            return fieldsWithin;
        }

        EcmaScriptRunner scriptRunner = new EcmaScriptRunner();
        for(FieldDef fieldDef : allFieldDefs){
            if(scriptRunner.referencesField(scriptText, fieldDef.getFieldName())){
                fieldsWithin.add(fieldDef);
            }
        }

        return fieldsWithin;
    }

    private List<FieldDef> getFieldsWithin(ScriptFunction scriptFunction) {
        if(scriptFunction instanceof ConcatFunction){
            return fieldsWithinConcatFunction((ConcatFunction) scriptFunction);
        }
        if(scriptFunction instanceof SubstringFunction){
            return fieldsWithinSubstringFunction((SubstringFunction) scriptFunction);
        }
        if(scriptFunction instanceof DurationFunction){
            return fieldsWithinDurationFunction((DurationFunction) scriptFunction);
        }
        if(scriptFunction instanceof MathFunction){
            return fieldsWithinMathFunction((MathFunction) scriptFunction);
        }

        return new ArrayList<FieldDef>();
    }

    private List<FieldDef> fieldsWithinConcatFunction(ConcatFunction scriptFunction) {
        List<FieldDef> fieldsWithin = new ArrayList<FieldDef>();
        for (OrderedField orderedField : scriptFunction.getFields()) {
            fieldsWithin.add(orderedField.getFieldDef(model));
        }
        return fieldsWithin;
    }

    private List<FieldDef> fieldsWithinSubstringFunction(SubstringFunction scriptFunction) {
        List<FieldDef> fieldsWithin = new ArrayList<FieldDef>();
        fieldsWithin.add(scriptFunction.getField(model));
        return fieldsWithin;
    }

    private List<FieldDef> fieldsWithinDurationFunction(DurationFunction scriptFunction) {
        List<FieldDef> fieldsWithin = new ArrayList<FieldDef>();
        fieldsWithin.add(scriptFunction.getStartField(model));
        fieldsWithin.add(scriptFunction.getEndField(model));
        return fieldsWithin;
    }

    private List<FieldDef> fieldsWithinMathFunction(MathFunction scriptFunction) {
        List<FieldDef> fieldsWithin = new ArrayList<FieldDef>();
        fieldsWithin.add(scriptFunction.getField1(model));
        fieldsWithin.add(scriptFunction.getField2(model));
        return fieldsWithin;
    }

    private boolean hasDependency(ConcatFunction functionIn, Set<String> fieldListIn) {

        List<OrderedField> myFieldList = (null != functionIn) ? functionIn.getFields() : null;

        if ((null != fieldListIn) && (0 < fieldListIn.size()) && (null != myFieldList) && (0 < myFieldList.size())) {

            for (OrderedField orderedField : myFieldList) {

                FieldDef myField = (null != orderedField) ? orderedField.getFieldDef(model) : null;
                String myName = (null != myField) ? myField.getName() : null;

                for (String myFieldIn : fieldListIn) {

                    if (myFieldIn.equals(myName)) {

                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasDependency(SubstringFunction functionIn, Set<String> fieldListIn) {

        if ((null != functionIn) && (null != fieldListIn) && (0 < fieldListIn.size())) {

            FieldDef myField = functionIn.getField(model);
            String myName = (null != myField) ? myField.getName() : null;

            for (String myFieldIn : fieldListIn) {

                if (myFieldIn.equals(myName)) {

                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDependency(DurationFunction functionIn, Set<String> fieldListIn) {

        if ((null != functionIn) && (null != fieldListIn) && (0 < fieldListIn.size())) {

            FieldDef myStartField = functionIn.getStartField(model);
            FieldDef myEndField = functionIn.getEndField(model);
            String myStartName = (null != myStartField) ? myStartField.getName() : null;
            String myEndName = (null != myEndField) ? myEndField.getName() : null;

            for (String myFieldIn : fieldListIn) {

                if (myFieldIn.equals(myStartName) || myFieldIn.equals(myEndName)) {

                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDependency(MathFunction functionIn, Set<String> fieldListIn) {

        if ((null != functionIn) && (null != fieldListIn) && (0 < fieldListIn.size())) {

            FieldDef myField1 = functionIn.getField1(model);
            FieldDef myField2 = functionIn.getField2(model);
            String myName1 = (null != myField1) ? myField1.getName() : null;
            String myName2 = (null != myField2) ? myField2.getName() : null;

            for (String myFieldIn : fieldListIn) {

                if ((null != myFieldIn) && myFieldIn.equals(myName1) || myFieldIn.equals(myName2)) {

                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDependency(ScriptFunction functionIn, Set<String> fieldListIn) {

        if(functionIn instanceof ConcatFunction){
            return hasDependency((ConcatFunction)functionIn, fieldListIn);
        }
        if(functionIn instanceof SubstringFunction){
            return hasDependency((SubstringFunction)functionIn, fieldListIn);
        }
        if(functionIn instanceof DurationFunction){
            return hasDependency((DurationFunction)functionIn, fieldListIn);
        }
        if(functionIn instanceof MathFunction){
            return hasDependency((MathFunction)functionIn, fieldListIn);
        }
        return false;
    }

    private boolean hasDependency(EcmaScriptRunner scriptRunnerIn, String scriptTextIn, Set<String> fieldListIn) {

        if ((null != scriptTextIn) && (null != fieldListIn) && (0 < fieldListIn.size())) {

            for (String myFieldIn : fieldListIn) {

                if (scriptRunnerIn.referencesField(scriptTextIn, myFieldIn)) {

                    return true;
                }
            }
        }
        return false;
    }

    private boolean fieldDefHasNoDependencies(FieldDef fieldDef) {
        return fieldDef == null || (!fieldDef.getFieldType().isDependent());
    }
}

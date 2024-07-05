package csi.server.common.model.functions;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.annotations.GwtIncompatible;

import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.ModelObject;
import csi.server.common.model.FieldDef;
import csi.server.common.model.operator.OperatorType;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MathFunction extends ScriptFunction {

    private static final String MATH_TEMPLATE = "var csiResult = Number(csiRow.get('%1$s')) %2$s Number(csiRow.get('%3$s'));";

    private static Map<OperatorType, String> opTypeMap = new HashMap<OperatorType, String>();
    static {
        opTypeMap.put(OperatorType.ADD, "+");
        opTypeMap.put(OperatorType.DIVIDE, "/");
        opTypeMap.put(OperatorType.MULTIPLY, "*");
        opTypeMap.put(OperatorType.SUBTRACT, "-");
    }

    /*
     * For example: field1 - field2 field1 + field2 field1 field2 field1 /
     * field2
     */
    protected OperatorType operator;

    protected String fieldId1;

    protected String fieldId2;

    public MathFunction() {
        super();
    }

    public OperatorType getOperator() {
        return operator;
    }

    public void setOperator(OperatorType operator) {
        this.operator = operator;
    }

    public String getFieldId1() {
        return fieldId1;
    }

    public void setFieldId1(String fieldId1In) {
        this.fieldId1 = fieldId1In;
    }

    public String getFieldId2() {
        return fieldId2;
    }

    public void setFieldId2(String fieldId2In) {
        this.fieldId2 = fieldId2In;
    }

    public FieldDef getField1(FieldListAccess modelIn) {
        return (null != fieldId1) ? modelIn.getFieldDefByLocalId(fieldId1) : null;
    }

    public void setField1(FieldDef field1In) {
        this.fieldId1 = (null != field1In) ? field1In.getLocalId() : null;
    }

    public FieldDef getField2(FieldListAccess modelIn) {
        return (null != fieldId2) ? modelIn.getFieldDefByLocalId(fieldId2) : null;
    }

    public void setField2(FieldDef field2In) {
        this.fieldId2 = (null != field2In) ? field2In.getLocalId() : null;
    }

    @GwtIncompatible("String.format")
    public String generateScript(FieldListAccess modelIn) {
        return String.format(MATH_TEMPLATE, getField1(modelIn).getFieldName(), opTypeMap.get(getOperator()), getField2(modelIn)
                .getFieldName());
    }

    @Override
    public MathFunction copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String,Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (MathFunction) copyOfThis;
            }
        }
        MathFunction copy = new MathFunction();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        copy.uuid = this.uuid;
        copy.fieldId1 = getFieldId1();
        copy.fieldId2 = getFieldId2();
        copy.operator = getOperator();
        return copy;
    }

    @Override
    public <T extends ModelObject> MathFunction clone(Map<String,T> fieldMapIn){
        if (fieldMapIn == null) {
            fieldMapIn = new HashMap<String,T>();
        }
        {
            Object copyOfThis = fieldMapIn.get(this.getUuid());
            if (copyOfThis != null) {
                return (MathFunction) copyOfThis;
            }
        }
        MathFunction copy = new MathFunction();
        fieldMapIn.put(getUuid(), (T) copy);
        super.copyComponents(copy);
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        copy.fieldId1 = getFieldId1();
        copy.fieldId2 = getFieldId2();
        copy.operator = getOperator();
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MathFunction fullClone() {

        MathFunction myClone = new MathFunction();

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MathFunction clone() {

        MathFunction myClone = new MathFunction();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        cloneContents((MathFunction)cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        cloneContents((MathFunction)cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateInPlace(ScriptFunction sourceIn) {

        super.updateInPlace(sourceIn);

        setFieldId1(((MathFunction)sourceIn).getFieldId1());
        setFieldId2(((MathFunction)sourceIn).getFieldId2());
        setOperator(((MathFunction)sourceIn).getOperator());
    }

    private void cloneContents(MathFunction cloneIn) {

        cloneIn.setFieldId1(getFieldId1());
        cloneIn.setFieldId2(getFieldId2());
        cloneIn.setOperator(getOperator());
    }
}

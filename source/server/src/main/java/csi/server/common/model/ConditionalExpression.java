package csi.server.common.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.dto.CsiMap;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ConditionalExpression extends ModelObject implements DeepCopiable<ConditionalExpression>, InPlaceUpdate<ConditionalExpression> {

    @Column(length = 8192)
    protected String expression;

    public ConditionalExpression() {
        super();
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public ConditionalExpression copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String, Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (ConditionalExpression) copyOfThis;
            }
        }
        ConditionalExpression copy = new ConditionalExpression();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.expression = this.expression;
        copy.uuid = this.uuid;
        return copy;
    }

    public <T extends ModelObject> ConditionalExpression trueCopy(Map<String, T> copies) {
        if (copies == null) {
            copies = new HashMap<String, T>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (ConditionalExpression) copyOfThis;
            }
        }
        ConditionalExpression copy = new ConditionalExpression();
        copies.put(getUuid(), (T) copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.expression = this.expression;
        return copy;
    }

    public void updateInPlace(ConditionalExpression sourceIn) {

        setExpression(sourceIn.getExpression());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConditionalExpression fullClone() {

        ConditionalExpression myClone = new ConditionalExpression();

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConditionalExpression clone() {

        ConditionalExpression myClone = new ConditionalExpression();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        ((ConditionalExpression)cloneIn).setExpression(getExpression());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        ((ConditionalExpression)cloneIn).setExpression(getExpression());
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, expression, indentIn, "expression");
    }
}

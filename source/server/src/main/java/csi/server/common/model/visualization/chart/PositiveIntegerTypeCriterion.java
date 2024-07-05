package csi.server.common.model.visualization.chart;

import csi.server.common.model.ModelObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PositiveIntegerTypeCriterion extends ChartCriterion implements Serializable {
    public Integer testValue;

    public PositiveIntegerTypeCriterion() { super(); }

    public PositiveIntegerTypeCriterion(int columnIndex, String columnHeader, String operatorString) {
        super(columnIndex, columnHeader, operatorString);
    }

    public Integer getTestValue() { return testValue; }

    public void setTestValue(Integer testValue) { this.testValue = testValue; }

    @Override
    public <T extends ModelObject> PositiveIntegerTypeCriterion clone(Map<String, T> fieldMapIn) {
        PositiveIntegerTypeCriterion myClone = new PositiveIntegerTypeCriterion();

        super.cloneComponents(myClone, fieldMapIn);

        myClone.setTestValue(getTestValue());

        return myClone;
    }

    @Override
    public <T extends ModelObject> PositiveIntegerTypeCriterion copy(Map<String, T> fieldMapIn) {
        if(fieldMapIn.containsKey(this.getUuid())) {
            return (PositiveIntegerTypeCriterion) fieldMapIn.get(this.getUuid());
        }
        PositiveIntegerTypeCriterion myCopy = new PositiveIntegerTypeCriterion();
        super.copyComponents(myCopy);
        myCopy.setTestValue(getTestValue());
        fieldMapIn.put(this.getUuid(), (T) myCopy);

        return myCopy;
    }
}

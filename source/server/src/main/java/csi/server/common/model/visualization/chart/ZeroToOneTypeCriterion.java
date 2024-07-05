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
public class ZeroToOneTypeCriterion extends ChartCriterion implements Serializable {
    public Double testValue;

    public ZeroToOneTypeCriterion() { super(); }

    public ZeroToOneTypeCriterion(int columnIndex, String columnHeader, String operatorString) {
        super(columnIndex, columnHeader, operatorString);
    }

    public Double getTestValue() { return testValue; }

    public void setTestValue(Double testValue) { this.testValue = testValue ;}

    @Override
    public <T extends ModelObject> ZeroToOneTypeCriterion clone(Map<String, T> fieldMapIn) {
        ZeroToOneTypeCriterion myClone = new ZeroToOneTypeCriterion();

        super.cloneComponents(myClone, fieldMapIn);

        myClone.setTestValue(getTestValue());

        return myClone;
    }

    @Override
    public <T extends ModelObject> ZeroToOneTypeCriterion copy(Map<String, T> fieldMapIn) {
        if(fieldMapIn.containsKey(this.getUuid())){
            return (ZeroToOneTypeCriterion) fieldMapIn.get(this.getUuid());
        }
        ZeroToOneTypeCriterion myCopy = new ZeroToOneTypeCriterion();

        super.copyComponents(myCopy);
        myCopy.setTestValue(getTestValue());
        fieldMapIn.put(this.getUuid(), (T) myCopy);

        return myCopy;
    }
}

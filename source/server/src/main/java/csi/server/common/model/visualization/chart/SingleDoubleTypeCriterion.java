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
public class SingleDoubleTypeCriterion extends ChartCriterion implements Serializable  {
	private Double testValue;
	public SingleDoubleTypeCriterion() {
		super();
	}
	public SingleDoubleTypeCriterion(int columnIndex, String columnHeader, String operatorString) {
		super(columnIndex, columnHeader, operatorString);
	}
	public Double getTestValue() {
		return testValue;
	}
	public void setTestValue(Double testValue) {
		this.testValue = testValue;
	}
	@Override
    public <T extends ModelObject> SingleDoubleTypeCriterion clone(Map<String, T> fieldMapIn) {
		SingleDoubleTypeCriterion myClone = new SingleDoubleTypeCriterion();
        
        super.cloneComponents(myClone, fieldMapIn);
        
        myClone.setTestValue(getTestValue());
        
        return myClone;
    }
    public <T extends ModelObject> SingleDoubleTypeCriterion copy(Map<String, T> fieldMapIn) {
    	if(fieldMapIn.containsKey(this.getUuid())){
    		return (SingleDoubleTypeCriterion) fieldMapIn.get(this.getUuid());
    	}
    	SingleDoubleTypeCriterion myCopy = new SingleDoubleTypeCriterion();
        
        super.copyComponents(myCopy);
        
        myCopy.setTestValue(getTestValue());
        fieldMapIn.put(this.getUuid(), (T) myCopy);
        
        return myCopy;
    }
}
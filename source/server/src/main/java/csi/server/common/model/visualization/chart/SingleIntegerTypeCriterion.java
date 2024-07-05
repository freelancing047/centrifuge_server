package csi.server.common.model.visualization.chart;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SingleIntegerTypeCriterion extends ChartCriterion implements Serializable {
	private Integer testValue;
	public SingleIntegerTypeCriterion() {
		super();
	}
	public SingleIntegerTypeCriterion(int columnIndex, String columnHeader, String operatorString) {
		super(columnIndex, columnHeader, operatorString);
	}
	public Integer getTestValue() {
		return testValue;
	}
	public void setTestValue(Integer testValue) {
		this.testValue = testValue;
	}
	@Override
    public <T extends ModelObject> SingleIntegerTypeCriterion clone(Map<String, T> fieldMapIn) {
		SingleIntegerTypeCriterion myClone = new SingleIntegerTypeCriterion();
        
        super.cloneComponents(myClone, fieldMapIn);
        
        myClone.setTestValue(getTestValue());
        
        return myClone;
    }
    public <T extends ModelObject> SingleIntegerTypeCriterion copy(Map<String, T> fieldMapIn) {
    	if(fieldMapIn.containsKey(this.getUuid())){
    		return (SingleIntegerTypeCriterion) fieldMapIn.get(this.getUuid());
    	}
    	SingleIntegerTypeCriterion myCopy = new SingleIntegerTypeCriterion();
        
        super.copyComponents(myCopy);
        
        myCopy.setTestValue(getTestValue());
        fieldMapIn.put(this.getUuid(), (T) myCopy);
        
        return myCopy;
    }
}

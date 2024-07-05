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
public class TwoDoubleTypeCriterion extends ChartCriterion implements Serializable {
	private Double minValue;
	private Double maxValue;
	public TwoDoubleTypeCriterion() {
        super();
    }
	public TwoDoubleTypeCriterion(int columnIndex, String columnHeader, String operatorString) {
		super(columnIndex, columnHeader, operatorString);
	}
	public Double getMinValue() {
		return minValue;
	}
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}
	public Double getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}
	@Override
    public <T extends ModelObject> TwoDoubleTypeCriterion clone(Map<String, T> fieldMapIn) {
		TwoDoubleTypeCriterion myClone = new TwoDoubleTypeCriterion();
        
        super.cloneComponents(myClone, fieldMapIn);
        
        myClone.setMinValue(getMinValue());
        myClone.setMaxValue(getMaxValue());
        
        return myClone;
    }
    public <T extends ModelObject> TwoDoubleTypeCriterion copy(Map<String, T> fieldMapIn) {
    	if(fieldMapIn.containsKey(this.getUuid())){
    		return (TwoDoubleTypeCriterion) fieldMapIn.get(this.getUuid());
    	}
    	TwoDoubleTypeCriterion myCopy = new TwoDoubleTypeCriterion();
        
        super.copyComponents(myCopy);
        
        myCopy.setMinValue(getMinValue());
        myCopy.setMaxValue(getMaxValue());
        fieldMapIn.put(this.getUuid(), (T) myCopy);
        
        return myCopy;
    }
}

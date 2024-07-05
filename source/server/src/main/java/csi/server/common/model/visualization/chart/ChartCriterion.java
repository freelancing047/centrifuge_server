package csi.server.common.model.visualization.chart;

import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class ChartCriterion extends ModelObject {
	private int columnIndex;
	private String columnHeader;
	private String operatorString;
	private int listPosition;
	public ChartCriterion() {
		super();
	}
	public ChartCriterion(int columnIndex, String columnHeader, String operatorString) {
		super();
		this.columnIndex = columnIndex;
		this.columnHeader = columnHeader;
		this.operatorString = operatorString;
	}
	public int getColumnIndex() {
		return columnIndex;
	}
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	public String getColumnHeader() {
		return columnHeader;
	}
	public void setColumnHeader(String columnHeader) {
		this.columnHeader = columnHeader;
	}
	public String getOperatorString() {
		return operatorString;
	}
	public void setOperatorString(String operatorString) {
		this.operatorString = operatorString;
	}
	public int getListPosition() {
		return listPosition;
	}
	public void setListPosition(int listPosition) {
		this.listPosition = listPosition;
	}
	@SuppressWarnings("unchecked")
    protected <T extends ModelObject> void cloneComponents(ChartCriterion cloneIn, Map<String, T> fieldMapIn) {
        if (null != cloneIn) {
            super.cloneComponents(cloneIn);
            cloneIn.setColumnIndex(getColumnIndex());
            cloneIn.setColumnHeader(getColumnHeader());
            cloneIn.setOperatorString(getOperatorString());
        }
    }
    @SuppressWarnings("unchecked")
    protected <T extends ModelObject> void copyComponents(ChartCriterion copyIn) {
        if (null != copyIn) {
            
            super.copyComponents(copyIn);
            
            copyIn.setColumnIndex(getColumnIndex());
            copyIn.setColumnHeader(getColumnHeader());
            copyIn.setOperatorString(getOperatorString());
        }
    }
    abstract public <T extends ModelObject> ChartCriterion clone(Map<String, T> fieldMapIn);
    abstract public <T extends ModelObject> ChartCriterion copy(Map<String, T> fieldMapIn);
}

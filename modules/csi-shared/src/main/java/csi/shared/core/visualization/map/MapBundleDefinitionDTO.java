package csi.shared.core.visualization.map;

public class MapBundleDefinitionDTO {
	private String fieldName;
	private String fieldColumn;
	private String shapeString;
	private String colorString;
	private boolean showLabel;
	private boolean allowNulls = true;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldColumn() {
		return fieldColumn;
	}

	public void setFieldColumn(String fieldColumn) {
		this.fieldColumn = fieldColumn;
	}

	public String getShapeString() {
		return shapeString;
	}

	public void setShapeString(String shapeString) {
		this.shapeString = shapeString;
	}

	public String getColorString() {
		return colorString;
	}

	public void setColorString(String colorString) {
		this.colorString = colorString;
	}

	public boolean isShowLabel() {
		return showLabel;
	}

	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}
	
	public boolean isAllowNulls() {
		return allowNulls;
	}
	
	public void setAllowNulls(boolean allowNulls) {
		this.allowNulls = allowNulls;
	}
}

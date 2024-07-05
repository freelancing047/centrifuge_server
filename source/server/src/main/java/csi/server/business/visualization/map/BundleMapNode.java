package csi.server.business.visualization.map;

public class BundleMapNode extends AugmentedMapNode {
	private String bundleColumn;
	private String bundleValue;
	private String label = null;
	private String drillString;
	private Integer childrenCount = null;

	public BundleMapNode(String vizUuid, Long nodeId, String bundleColumn, String bundleValue, String drillString) {
		super(vizUuid, nodeId);
		this.bundleColumn = bundleColumn;
		this.bundleValue = bundleValue;
		this.label = bundleValue;
		this.drillString = drillString;
	}

	public String getBundleColumn() {
		return bundleColumn;
	}

	public void setBundleColumn(String bundleColumn) {
		this.bundleColumn = bundleColumn;
	}

	public String getBundleValue() {
		return bundleValue;
	}

	public void setBundleValue(String bundleValue) {
		this.bundleValue = bundleValue;
	}

	public String getDrillLevel() {
		return drillString;
	}

	public String getLabel() {
		if (label == null) {
			return super.getLabel();
		} else {
			return label;
		}
	}

	public Integer getChildrenCount() {
		return childrenCount;
	}

	public void setChildrenCount(Integer childrenCount) {
		this.childrenCount = childrenCount;
	}
}

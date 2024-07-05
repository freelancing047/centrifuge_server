package csi.server.common.model.map;

public class Crumb {
	private String criterion;
	private Extent previousExtent;
	private Extent previousInitialExtent;
	public Crumb() {
	}
	public String getCriterion() {
		return criterion;
	}
	public void setCriterion(String criterion) {
		this.criterion = criterion;
	}
	public Extent getPreviousExtent() {
		return previousExtent;
	}
	public void setPreviousExtent(Extent previousExtent) {
		this.previousExtent = previousExtent;
	}
	public Extent getPreviousInitialExtent() {
		return previousInitialExtent;
	}
	public void setPreviousInitialExtent(Extent previousInitialExtent) {
		this.previousInitialExtent = previousInitialExtent;
	}
}

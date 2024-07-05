package csi.server.business.visualization.map.mapserviceutil;

import java.util.List;

import csi.server.common.model.map.Crumb;

public class MapBundleInfo {
    private List<Crumb> breadcrumb = null;
    private boolean showLeaves = false;
	public List<Crumb> getBreadcrumb() {
		return breadcrumb;
	}
    public void setBreadcrumb(List<Crumb> breadcrumb) {
		this.breadcrumb = breadcrumb;
	}
    public boolean isShowLeaves() {
		return showLeaves;
	}
    public void setShowLeaves(boolean showLeaves) {
		this.showLeaves = showLeaves;
	}
}

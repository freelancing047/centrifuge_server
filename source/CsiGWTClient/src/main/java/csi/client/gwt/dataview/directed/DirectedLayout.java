package csi.client.gwt.dataview.directed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import csi.client.gwt.events.LayoutType;

@SuppressWarnings("serial")
public class DirectedLayout implements Serializable {

	private LayoutType layoutType;
	private Map<String, Integer> params = new HashMap<String, Integer>();
	
	public Map<String, Integer> getParams() {
		return params;
	}
	public void setParams(Map<String, Integer> params2) {
		this.params = params2;
	}
	public LayoutType getLayoutType() {
		return layoutType;
	}
	public void setLayoutType(LayoutType layoutType) {
		this.layoutType = layoutType;
	}
	
}

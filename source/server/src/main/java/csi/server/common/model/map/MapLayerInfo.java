package csi.server.common.model.map;

public class MapLayerInfo {
	private String id;
	private String key;
	private String url;
	private String type;
	private String layername;
	private boolean visible;
	private int opacity;

	public MapLayerInfo() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLayername() {
		return layername;
	}

	public void setLayername(String layername) {
		this.layername = layername;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getOpacity() {
		return opacity;
	}

	public void setOpacity(int opacity) {
		this.opacity = opacity;
	}
}

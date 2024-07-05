package csi.shared.core.visualization.map;

public class AssociationSettingsDTO {
	private String lineStyle;
	private int width;
	private String colorString;
	private String name;
	private String source;
	private String destination;
	private boolean showDirection;

	public String getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(String lineStyle) {
		this.lineStyle = lineStyle;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getColorString() {
		return colorString;
	}

	public void setColorString(String colorString) {
		this.colorString = colorString;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public boolean isShowDirection() {
		return showDirection;
	}

	public void setShowDirection(boolean showDirection) {
		this.showDirection = showDirection;
	}

}

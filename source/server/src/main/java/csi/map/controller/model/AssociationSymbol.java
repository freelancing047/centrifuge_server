package csi.map.controller.model;

public class AssociationSymbol {
	private int id;
	private String lineStyle;
	private int width;
	private String color;
	private boolean showDirection;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
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
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public boolean isShowDirection() {
		return showDirection;
	}
	public void setShowDirection(boolean showDirection) {
		this.showDirection = showDirection;
	}
}
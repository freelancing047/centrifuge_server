package csi.client.gwt.viz.map.settings;

import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ValueBasedIconCell extends AbstractCell<Boolean> {
	private IconType iconType;
	private IconSize iconSize;
	private String tooltip;

	public ValueBasedIconCell(IconType iconType) {
		this(iconType, IconSize.DEFAULT);
	}

	public ValueBasedIconCell(IconType iconType, IconSize iconSize) {
		super();
		this.iconType = iconType;
		this.iconSize = iconSize;
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
		if (value)
			sb.appendHtmlConstant("<i" + (tooltip == null ? "" : " title=\"" + tooltip + "\"") + " class=\"" + iconType.get() + " " + iconSize.get() + "\"></i>");
		else
			sb.appendHtmlConstant("");
	}

	public IconType getIconType() {
		return iconType;
	}

	public void setIconType(IconType iconType) {
		this.iconType = iconType;
	}

	public IconSize getIconSize() {
		return iconSize;
	}

	public void setIconSize(IconSize iconSize) {
		this.iconSize = iconSize;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}

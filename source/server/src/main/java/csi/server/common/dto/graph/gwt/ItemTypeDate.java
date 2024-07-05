package csi.server.common.dto.graph.gwt;

import java.util.Date;

public class ItemTypeDate extends AbstractItemTypeBase {
	public Date item;

	public ItemTypeDate() {
	}

	public ItemTypeDate(Date item) {
		this.item = item;
	}

	@Override
	public String getAsString() {
		return item.toString();
	}

	@Override
	public boolean isDate() {
		return true;
	}

	@Override
	public boolean isDouble() {
		return false;
	}

	@Override
	public boolean isInteger() {
		return false;
	}

	@Override
	public boolean isString() {
		return false;
	}

	@Override
	public boolean isLong() {
		return false;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	public Date getItem() {
		return item;
	}

	public void setItem(Date item) {
		this.item = item;
	}
}

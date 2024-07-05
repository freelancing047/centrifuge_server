package csi.server.common.dto.graph.gwt;

public class ItemTypeInteger extends AbstractItemTypeBase {
	public Integer item;

	public ItemTypeInteger() {
	}

	public ItemTypeInteger(Integer item) {
		this.item = item;
	}

	@Override
	public String getAsString() {
		return item.toString();
	}

	@Override
	public boolean isDate() {
		return false;
	}

	@Override
	public boolean isDouble() {
		return false;
	}

	@Override
	public boolean isInteger() {
		return true;
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

	public Integer getItem() {
		return item;
	}

	public void setItem(Integer item) {
		this.item = item;
	}
}

package csi.server.common.dto.graph.gwt;

public class ItemTypeString extends AbstractItemTypeBase {
	public String item;

	public ItemTypeString() {
	}

	public ItemTypeString(String item) {
		this.item = item;
	}

	@Override
	public String getAsString() {
		return item;
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
		return false;
	}

	@Override
	public boolean isString() {
		return true;
	}

	@Override
	public boolean isLong() {
		return false;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}
}

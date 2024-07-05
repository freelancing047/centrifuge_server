package csi.server.common.dto.graph.gwt;


public class ItemTypeBoolean extends AbstractItemTypeBase {
	public Boolean item;

	public ItemTypeBoolean() {
	}

	public ItemTypeBoolean(Boolean item) {
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
		return true;
	}

	public Boolean getItem() {
		return item;
	}

	public void setItem(Boolean item) {
		this.item = item;
	}
}

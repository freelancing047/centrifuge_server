package csi.server.common.dto.graph.gwt;

public class ItemTypeLong extends AbstractItemTypeBase {
	public Long item;

	public ItemTypeLong() {
	}

	public ItemTypeLong(Long item) {
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
		return true;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	public Long getItem() {
		return item;
	}

	public void setItem(Long item) {
		this.item = item;
	}
}

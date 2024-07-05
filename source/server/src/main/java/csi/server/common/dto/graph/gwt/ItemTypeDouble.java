package csi.server.common.dto.graph.gwt;

public class ItemTypeDouble extends AbstractItemTypeBase {
	public Double item;

	public ItemTypeDouble() {
	}

	public ItemTypeDouble(Double item) {
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
		return true;
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

	public Double getItem() {
		return item;
	}

	public void setItem(Double item) {
		this.item = item;
	}
}

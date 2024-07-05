package csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue;

public class CountTypeSizeValue implements TypeSizeValue {
	private int count = 0;

	public void incrementCount() {
		count++;
	}

	public void incrementCount(int hits) {
		count += hits;
	}

	@Override
	public double getValue() {
		return count;
	}
}

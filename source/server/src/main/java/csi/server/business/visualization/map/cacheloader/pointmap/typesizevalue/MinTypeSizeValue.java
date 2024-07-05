package csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue;

public class MinTypeSizeValue implements TypeSizeValue {
	private double minvalue = Double.MAX_VALUE;
	
	public void addValue(double value) {
		if (value < minvalue)
			minvalue = value;
	}

	@Override
	public double getValue() {
		return minvalue;
	}
}

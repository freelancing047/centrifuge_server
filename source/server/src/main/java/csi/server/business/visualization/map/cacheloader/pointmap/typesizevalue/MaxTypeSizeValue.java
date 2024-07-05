package csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue;

public class MaxTypeSizeValue implements TypeSizeValue {
	private double maxvalue = Double.MIN_VALUE;
	
	public void addValue(double value) {
		if (value > maxvalue)
			maxvalue = value;
	}

	@Override
	public double getValue() {
		return maxvalue;
	}
}

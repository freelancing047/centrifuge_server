package csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue;

public class AbsSumTypeSizeValue implements TypeSizeValue {
	private double sum = 0;
	
	public void addValue(double value) {
		sum += Math.abs(value);
	}
	
	@Override
	public double getValue() {
		return sum;
	}
}

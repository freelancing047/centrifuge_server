package csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue;

public class SumTypeSizeValue implements TypeSizeValue {
	private double sum = 0;
	
	public void addValue(double value) {
		sum += value;
	}
	
	@Override
	public double getValue() {
		return sum;
	}
}

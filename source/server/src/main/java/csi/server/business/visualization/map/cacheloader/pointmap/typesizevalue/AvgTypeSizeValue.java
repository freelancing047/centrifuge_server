package csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue;

public class AvgTypeSizeValue implements TypeSizeValue {
	private int count = 0;
	private double sum = 0;
	
	public void incrementCount() {
		count++;
	}
	
	public void addValue(double value) {
		sum += value;
	}
	
	@Override
	public double getValue() {
		if (count == 0)
			return 0;
		else 
			return sum / (double)count;
	}
}

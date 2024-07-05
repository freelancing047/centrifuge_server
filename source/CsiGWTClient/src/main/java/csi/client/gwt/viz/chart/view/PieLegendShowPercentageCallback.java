package csi.client.gwt.viz.chart.view;

import java.util.Map;

import com.google.gwt.i18n.client.NumberFormat;

public class PieLegendShowPercentageCallback {
	private Map<String, Double> pointPercentages;
	
	public void setPointPercentages(Map<String,Double> pointPercentages) {
		this.pointPercentages = pointPercentages;
	}
	
	public String execute(String pointName) {
		NumberFormat fmt = NumberFormat.getFormat("0.00");
		String percentage = fmt.format(pointPercentages.get(pointName) * 100);
		return percentage;
	}
}

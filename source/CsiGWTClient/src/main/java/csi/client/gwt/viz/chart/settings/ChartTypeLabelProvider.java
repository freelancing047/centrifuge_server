package csi.client.gwt.viz.chart.settings;

import com.sencha.gxt.data.shared.LabelProvider;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.core.visualization.chart.ChartType;

public class ChartTypeLabelProvider implements LabelProvider<ChartType> {

    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	@Override
	public String getLabel(ChartType item) {
		String label = "";
		
		switch(item){
			case AREA: label = i18n.chartTypeArea(); break;
			case AREA_SPLINE: label = i18n.chartTypeSpline(); break;
			case BAR: label = i18n.axisLabelsY(); break;
			case COLUMN: label = i18n.axisLabelsX(); break;
			case DONUT: label = i18n.chartTypeDonut(); break;
			case LINE: label = i18n.chartTypeLine(); break;
			case PIE: label = i18n.chartTypePie(); break;
			case POLAR: label = i18n.chartTypePolar(); break;
			case SPIDER: label = i18n.chartTypeSpider(); break;
		}
		return label;
	}

}

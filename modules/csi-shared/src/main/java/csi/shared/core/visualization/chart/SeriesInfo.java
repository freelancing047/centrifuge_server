package csi.shared.core.visualization.chart;

import com.google.common.html.HtmlEscapers;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public class SeriesInfo implements Serializable {

    private String metricName;
    private String hexColor;
    private MeasureChartType measureChartType;

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = HtmlEscapers.htmlEscaper().escape(metricName);
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public MeasureChartType getMeasureChartType() {
        return measureChartType;
    }

    public void setMeasureChartType(MeasureChartType measureChartType) {
        this.measureChartType = measureChartType;
    }
}

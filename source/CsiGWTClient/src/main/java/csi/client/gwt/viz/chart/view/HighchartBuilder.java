package csi.client.gwt.viz.chart.view;

import java.util.ArrayList;
import java.util.List;

import com.google.common.html.HtmlEscapers;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.sencha.gxt.core.client.util.TextMetrics;
import org.moxieapps.gwt.highcharts.client.*;
import org.moxieapps.gwt.highcharts.client.events.PointClickEvent;
import org.moxieapps.gwt.highcharts.client.events.PointClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.PointSelectEventHandler;
import org.moxieapps.gwt.highcharts.client.events.PointUnselectEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesHideEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesShowEventHandler;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.DataLabels;
import org.moxieapps.gwt.highcharts.client.labels.DataLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.DataLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.Labels;
import org.moxieapps.gwt.highcharts.client.labels.LegendLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.LegendLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.PieDataLabels;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaSplinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.BarPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.ColumnPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.PiePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.PlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.i18n.client.NumberFormat;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.model.CsiChartLoadHandler;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.presenter.DrillSelectionCallback;
import csi.shared.core.visualization.chart.ChartType;
import csi.shared.core.visualization.chart.MeasureChartType;
import csi.shared.core.visualization.chart.SeriesInfo;

/**
 * @author Centrifuge Systems, Inc.
 */
public class HighchartBuilder {
    public static final double DONUT_INNER_SIZE = 0.5;
    private static final float VERTICAL_LABLE_ANGLE = 70.0f;
    private static final int measureNameCharLimit = 50;
	private static final int measureNameMetricLimit = 150;
    
	private static void setOptions(PlotOptions<?> plotOptions) {
		// See http://stackoverflow.com/questions/15619519
		plotOptions.setOption(HighchartConstants.POINT_RANGE.toString(), 1.0);
		plotOptions.setAnimation(new Animation().setDuration(ResizeableHighchart.ANIMATION_DURATION).setEasing(Animation.Easing.SWING));
		plotOptions.setAllowPointSelect(true);
		plotOptions.setCursor(PlotOptions.Cursor.POINTER);
	}

	private static LinePlotOptions createDefaultLinePlotOptions() {
		LinePlotOptions linePlotOptions = new LinePlotOptions();
		setOptions(linePlotOptions);
		return linePlotOptions;
	}

	private static ColumnPlotOptions createDefaultColumnPlotOptions() {
		ColumnPlotOptions columnPlotOptions = new ColumnPlotOptions();
		columnPlotOptions.setGroupPadding(0.1);
		setOptions(columnPlotOptions);
		return columnPlotOptions;
	}

	private static BarPlotOptions createDefaultBarPlotOptions() {
		BarPlotOptions plotOptions = new BarPlotOptions();
		plotOptions.setGroupPadding(0.1);
		setOptions(plotOptions);
		return plotOptions;
	}

	private static AreaPlotOptions createDefaultAreaPlotOptions() {
		AreaPlotOptions plotOptions = new AreaPlotOptions();
		setOptions(plotOptions);
		return plotOptions;
	}

	private static AreaSplinePlotOptions createDefaultAreaSplinePlotOptions() {
		AreaSplinePlotOptions plotOptions = new AreaSplinePlotOptions();
		setOptions(plotOptions);
		return plotOptions;
	}

	private static PiePlotOptions createDefaultPiePlotOptions(boolean containsPieMeasure, ChartType chartType, PieDataLabels pieDataLabels) {
		PiePlotOptions plotOptions = new PiePlotOptions();
		setOptions(plotOptions);


		if (chartType == ChartType.DONUT) {
			plotOptions.setInnerSize(DONUT_INNER_SIZE);
		}

		if (chartType == ChartType.PIE || containsPieMeasure) {
			plotOptions.setPieDataLabels(pieDataLabels);
		}

		plotOptions.setShowInLegend(true);

		plotOptions.setOption(HighchartConstants.SLICED_OFFSET.toString(), 20);
		return plotOptions;
	}

	private PieDataLabels createPieDataLabel() {
		PieDataLabels pieDataLabels = new PieDataLabels();
		if (pieLabelEnabled) {
			pieDataLabels.setEnabled(true);

			DataLabelsFormatter dataLabelsFormatter = new DataLabelsFormatter() {
				public String format(DataLabelsData dataLabelsData) {
					if (dataLabelsData.getPercentage() < pieLabelThreshold) {
						return null;
					} else {
						StringBuffer sb = new StringBuffer();
						sb.append("<b>" + dataLabelsData.getPointName() + "</b>");
						if (pieLabelShowValue) {
							sb.append(" : " + dataLabelsData.getYAsString() + " / " + dataLabelsData.getTotal());
						}
						if (pieLabelShowPercentage) {
							NumberFormat fmt = NumberFormat.getFormat("0.00");
							String percentage = fmt.format(dataLabelsData.getPercentage());
							sb.append(" : " + percentage + " %");
						}
						return sb.toString();
					}
				}
			};

			pieDataLabels.setFormatter(dataLabelsFormatter);
		} else {
			pieDataLabels.setEnabled(false);
		}
		return pieDataLabels;
	}

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private String title;
    private String description;
    private String xAxisTitle;
    private ChartModel chartModel;
    private ChartType chartType;
    private int seriesCount;
    private DrillSelectionCallback drillSelectionCallback;
    private AxisLabelsFormatter axisLabelsFormatter;

    private boolean pieLabelEnabled;
    private boolean pieLabelShowPercentage;
    private boolean pieLabelShowValue;
    private double pieLabelThreshold;

    private boolean pieLegendEnabled;
    private boolean pieLegendShowPercentage;
    private boolean pieLegendShowValue;

    private boolean polar;
    private boolean spider;


    private PointUnselectEventHandler pointUnselectEventHandler;
    private PointSelectEventHandler pointSelectEventHandler;
    private SeriesHideEventHandler seriesHideEvent;
    private SeriesShowEventHandler seriesShowEvent;

	public SeriesHideEventHandler getSeriesHideEvent() {
		return seriesHideEvent;
	}

	public HighchartBuilder setSeriesHideEvent(SeriesHideEventHandler seriesHideEvent) {
		this.seriesHideEvent = seriesHideEvent;
		return this;
	}

	public SeriesShowEventHandler getSeriesShowEvent() {
		return seriesShowEvent;
	}

	public HighchartBuilder setSeriesShowEvent(SeriesShowEventHandler seriesShowEvent) {
		this.seriesShowEvent = seriesShowEvent;
		return this;
	}

	private boolean containsPieMeasure;

    public HighchartBuilder titleDescription(String title, String description){
		title = title.replaceAll("&amp", "&").replaceAll("gt", ">");
		description = description
				.replaceAll("&amp;", "&")
				.replaceAll("&gt;", ">")
				.replaceAll("&lt;", "<")
				.replaceAll("&#39;", "'")
				.replaceAll("&quot;", "\"");
	    this.title = description.length() > 0 ? title + " \u2014 " +  description : title;
//	    this.description = description;
	    return this;
	}

	public HighchartBuilder xAxisTitle(String xAxisTitle){
	    this.xAxisTitle = xAxisTitle;
	    return this;
	}

	ChartPresenter pres;

	public HighchartBuilder chartPresenter(ChartPresenter chartPresenter){
	    chartModel = chartPresenter.getChartModel();
	    this.pres = chartPresenter;
	    chartType = chartModel.getInitialChartData().getChartType();
	    containsPieMeasure = false;
	            
	    for(SeriesInfo seriesInfo: chartModel.getHighchartResponse().getSeriesInfos()) {
	        MeasureChartType measureChartType = seriesInfo.getMeasureChartType();
	        if (measureChartType == MeasureChartType.PIE || measureChartType == MeasureChartType.DONUT) {
	            containsPieMeasure = true;
	        }
	    }
	            
	    return this;
	}

	public HighchartBuilder seriesCount(int seriesCount) {
	    this.seriesCount = seriesCount;
	    return this;
	}

	public HighchartBuilder drillSelectionCallback(DrillSelectionCallback drillSelectionCallback){
	    this.drillSelectionCallback = drillSelectionCallback;
	    return this;
	}

	public HighchartBuilder axisLabelsFormatter(AxisLabelsFormatter axisLabelsFormatter) {
	    this.axisLabelsFormatter = axisLabelsFormatter;
	    return this;
	}

	public HighchartBuilder pieLabelEnabled(boolean pieLabelEnabled){
	    this.pieLabelEnabled = pieLabelEnabled;
	    return this;
	}

	public HighchartBuilder pieLabelShowPercentage(boolean pieLabelShowPercentage) {
		this.pieLabelShowPercentage = pieLabelShowPercentage;
		return this;
	}

	public HighchartBuilder pieLabelShowValue(boolean pieLabelShowValue) {
		this.pieLabelShowValue = pieLabelShowValue;
		return this;
	}

	public HighchartBuilder pieLabelPercentageThreshold(double pieLabelThreshold){
	    this.pieLabelThreshold = pieLabelThreshold;
	    return this;
	}

	public HighchartBuilder pieLegendEnabled(boolean pieLegendEnabled) {
		this.pieLegendEnabled = pieLegendEnabled;
		return this;
	}

	public HighchartBuilder pieLegendShowPercentage(boolean pieLegendShowPercentage) {
		this.pieLegendShowPercentage = pieLegendShowPercentage;
		return this;
	}

	public HighchartBuilder pieLegendShowValue(boolean pieLegendShowValue) {
		this.pieLegendShowValue = pieLegendShowValue;
		return this;
	}
	
    public HighchartBuilder setPointUnselectEventHandler(PointUnselectEventHandler pointUnselectEventHandler) {
		this.pointUnselectEventHandler = pointUnselectEventHandler;
		return this;
	}

    public HighchartBuilder setPointSelectEventHandler(PointSelectEventHandler pointSelectEventHandler) {
		this.pointSelectEventHandler = pointSelectEventHandler;
		return this;
	}

    public ResizeableHighchart build(PieLegendShowPercentageCallback callback, CsiChartLoadHandler csiChartLoadHandler)
    {
        validate();
		ResizeableHighchart chart = new ResizeableHighchart();
		chart.setSeriesPlotOptions(createClickHandlersForSeries());
		chart.addLoadHandler(csiChartLoadHandler);
		initYAxises(chart);
		setHighchartType(chart);
		setHighchartLabels(chart, callback);
		setHighchartPlotOptions(chart);
		// i think what's happening here is that we the options and then wipe them out with new PlotOptions() in the callbacks

		if (chartType == ChartType.DONUT && pieLabelEnabled) {
			chart.setOption("/plotOptions/series/dataLabels", createPieDataLabel());
			chart.setOption("/plotOptions/series/dataLabels/enabled", true);
		}
		return chart;
    }

	private void validate() {
		if (chartType == null)
			throw new RuntimeException(_constants.highchartBuilder_chartTypeNull());
		if (seriesCount <= 0)
			throw new RuntimeException(_constants.highchartBuilder_seriesCountNegative());
	}

	private void initYAxises(ResizeableHighchart chart) {
		// Without this, the y axises do not get initialized and we cannot render multiple series.
		if (chartModel.getInitialChartData().isAlignAxes()) {
			chart.getYAxis(0);
		} else {
			chart.getYAxis(seriesCount - 1);
		}
		if (chartType == ChartType.BAR)
			chart.getYAxis().setShowLastLabel(false);
	}

	private void setHighchartType(ResizeableHighchart chart) {
		chart.setType(createSeriesType());
		chart.setPolar(polar);
		if (spider) {
			chart.getYAxis().setOption(
					HighchartConstants.GRID_LINE_INTERPOLATION.toString(),
					_constants.highchartBuilder_polygon());
		}
	}

	private Series.Type createSeriesType() {
		switch (chartType) {
		case AREA:
			return Series.Type.AREA;
		case AREA_SPLINE:
			return Series.Type.AREA_SPLINE;
		case BAR:
			return Series.Type.BAR;
		case LINE:
			return Series.Type.LINE;
		case PIE:
		case DONUT:
			return Series.Type.PIE;
		case SPIDER:
			spider = true;
		case POLAR:
			polar = true;
		default:
			return Series.Type.COLUMN;
		}
	}

	private void setHighchartLabels(ResizeableHighchart chart, PieLegendShowPercentageCallback callback) {
//		chart.setTitle(title, description, seriesCount > 1 ? ChartTitle.Align.LEFT : ChartTitle.Align.CENTER);
		chart.setTitle(title, null, ChartTitle.Align.CENTER);
		chart.getXAxis().setAxisTitle(new AxisTitle().setText(xAxisTitle));
		chart.getXAxis().setLabels(createXAxisLabels());
		chart.setToolTip(createTooltip(chart));
		chart.setLegend(createLegend(callback, title));
	}

	private XAxisLabels createXAxisLabels() {
		XAxisLabels xAxisLables = new XAxisLabels();
		setLabelType(xAxisLables);
		setLabelFormatter(xAxisLables);
		return xAxisLables;
	}

	private void setLabelType(XAxisLabels xAxisLables) {
		switch (chartType) {
			// Vertical traditional charts
			case AREA:
			case AREA_SPLINE:
			case COLUMN:
			case LINE:
				xAxisLables.setRotation(VERTICAL_LABLE_ANGLE);
				xAxisLables.setAlign(Labels.Align.LEFT);
				break;
			case POLAR:
			case SPIDER:
				break;
			// Horizontal traditional charts
			case BAR:
				xAxisLables.setAlign(Labels.Align.RIGHT);
				break;
			// pie based charts
			case PIE:
			case DONUT:
				xAxisLables.setEnabled(false);
				break;
			default:
				throw new RuntimeException(_constants.highchartBuilder_dontKnowAbout() + " " + chartType);
		}
	}

	private void setLabelFormatter(XAxisLabels xAxisLables) {
		if (axisLabelsFormatter != null)
			xAxisLables.setFormatter(axisLabelsFormatter);
	}

	private ToolTip createTooltip(ResizeableHighchart chart) {
		ToolTip tooltip = new ToolTip();

        tooltip.setOption("distance", 10);
        tooltip.setOption("shape", "square");
//        tooltip.setFollowPointer(true);
        tooltip.setShadow(false);
		tooltip.setFormatter(new ToolTipFormatter() {
			@Override
			public String format(ToolTipData toolTipData) {
				// This is a workaround for toolTipData.getXAsString being null when a measure is set to type PIE
				// but the chart itself is say of column type.
				String xLabel = toolTipData.getXAsString();
				if (xLabel == null) {
					xLabel = toolTipData.getPointName();
				}
				if(chart.getSeries(toolTipData.getSeriesId()).isVisible()) {
					return buildLabel(toolTipData, xLabel);
				}else{
					return "";
				}
			}

			private String buildLabel(ToolTipData toolTipData, String xLabel) {
				String yValue = String.valueOf(toolTipData.getYAsDouble());
				if (toolTipData.getPoint().getUserData() != null && toolTipData.getPoint().getUserData().get(HighchartConstants.NEGATIVE.toString()) != null) {
					yValue = String.valueOf(toolTipData.getYAsDouble() * -1);
				}
				String toolTip = toolTipData.getSeriesName().
						replaceAll("&amp;", "&").
						replaceAll("&lt;", "<").
						replaceAll("&gt;", ">").
						replaceAll("&#39;", "'").
						replaceAll("&quot;", "\"");
				return xLabel + " : " + yValue + " : " + toolTip;
			}
		});
		return tooltip;
	}

	private Legend createLegend(final PieLegendShowPercentageCallback callback, String title) {
		Legend legend = new Legend();
		legend.setBorderWidth(0);

		if (chartType == ChartType.PIE || chartType == ChartType.DONUT || containsPieMeasure) {
			if (pieLegendEnabled) {

				legend.setEnabled(true);
				legend.setAlign(Legend.Align.RIGHT);
				legend.setVerticalAlign(Legend.VerticalAlign.MIDDLE);
				legend.setLayout(Legend.Layout.VERTICAL);
				legend.setLabelsFormatter(new LegendLabelsFormatter() {

					@Override
					public String format(LegendLabelsData legendLabelsData) {
						StringBuffer sb = new StringBuffer();
						sb.append("<b>" + legendLabelsData.getPointName() + "</b>");
						if (pieLegendShowValue) {
							sb.append(" : " + legendLabelsData.getPoint().getY());
						}
						if (pieLegendShowPercentage) {
							String percentage = callback.execute(legendLabelsData.getPointName());
							sb.append(" : " + percentage + " %");
						}
						return sb.toString();
					}

				});

			} else {
				legend.setEnabled(false);
			}
		} else {
			if (seriesCount > 1) {
				legend.setEnabled(true);
				legend.setAlign(Legend.Align.CENTER);
				legend.setVerticalAlign(Legend.VerticalAlign.TOP);
				legend.setLayout(Legend.Layout.HORIZONTAL);
				legend.setLabelsFormatter(new LegendLabelsFormatter() {
					@Override
					public String format(LegendLabelsData legendLabelsData) {
						String measureName = legendLabelsData.getPointName();
						measureName = measureName.replace("&amp;amp;", "&");
						measureName = measureName.replace("&amp;lt;", "<");
						measureName = measureName.replace("&amp;gt;", ">");
						measureName = measureName.replace("&amp;#39;", "'");
						String truncatedMeasureName = "";
						if (TextMetrics.get().getWidth(measureName) > measureNameMetricLimit) {
							truncatedMeasureName = measureName.substring(0, measureNameCharLimit) + "…";
						} else {
							return measureName;
						}

						String vizUuid = pres.getUuid();
						String formattedVizUuid = "main-layout-panel-Optional.of(" + vizUuid + ")";

						String finalTruncatedMeasureName = truncatedMeasureName;
						String finalMeasureName = measureName;
						Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
							@Override
							public boolean execute() {
								NodeList<Element> element = Document.get().getElementById(formattedVizUuid).getElementsByTagName("g");
								for (int i = 0; i < element.getLength(); i++) {
									String gElementClassName = element.getItem(i).getAttribute("class");
									if (gElementClassName.equals("highcharts-legend-item")) {
										Element legendTextElem = element.getItem(i).getElementsByTagName("text").getItem(0);
										String measureDisplayName = legendTextElem.getElementsByTagName("tspan").getItem(0).getInnerHTML();
										if (legendTextElem.getElementsByTagName("title").getLength() < 1 && measureDisplayName.equals(finalTruncatedMeasureName)) {
											legendTextElem.setInnerHTML(legendTextElem.getInnerHTML() + "<title>" + finalMeasureName + "</title>");
										}
									}
								}
								return false;
							}
						}, 500);

						return truncatedMeasureName;
					}
				});


			} else {
				legend.setEnabled(false);
			}
		}

		return legend;
	}

	private void setHighchartPlotOptions(ResizeableHighchart chart) {
		chart.setLinePlotOptions(createDefaultLinePlotOptions());
		chart.setColumnPlotOptions(createDefaultColumnPlotOptions());
		chart.setBarPlotOptions(createDefaultBarPlotOptions());
		chart.setAreaPlotOptions(createDefaultAreaPlotOptions());
		chart.setAreaSplinePlotOptions(createDefaultAreaSplinePlotOptions());
		chart.setPiePlotOptions(createDefaultPiePlotOptions(containsPieMeasure, chartType, createPieDataLabel()));
	}

	public SeriesPlotOptions createClickHandlersForSeries() {
		SeriesPlotOptions seriesOptions = new SeriesPlotOptions();
		if (drillSelectionCallback != null && chartModel != null) {
			seriesOptions.setPointClickEventHandler(new PointClickEventHandler() {
				public boolean onClick(PointClickEvent e) {
					if (!(e.isControlKeyDown() || e.isShiftKeyDown())) {
						List<String> drillCategories = new ArrayList<String>(chartModel.getDrillSelections());
						drillCategories.add(e.getPoint().getName());
						drillSelectionCallback.drillCategorySelected(drillCategories);

						return false;
					} else {
						return true;
					}
				}
			});
		}

		seriesOptions.setPointSelectEventHandler(pointSelectEventHandler);

		seriesOptions.setPointUnselectEventHandler(pointUnselectEventHandler);

		seriesOptions.setSeriesHideEventHandler(seriesHideEvent);

		seriesOptions.setSeriesShowEventHandler(seriesShowEvent);

		seriesOptions.setCursor(PlotOptions.Cursor.POINTER);
		if (!(chartType == ChartType.PIE || containsPieMeasure)) {
            seriesOptions.setDataLabels(new DataLabels().setEnabled(false));
		}

		return seriesOptions;
	}

}

/**

 */
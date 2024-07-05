package csi.client.gwt.viz.chart.view;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.user.client.ui.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.AbstractMetricsView;
import csi.client.gwt.viz.shared.MetricsDisplay;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.ChartActionsServiceProtocol;
import csi.shared.core.visualization.chart.ChartMetrics;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.shared.core.visualization.chart.HighchartPagingRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartMetricsView extends AbstractMetricsView<ChartMetrics>{
    private ChartPresenter presenter;

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private String WINDOW_TITLE = i18n.chart_metrics_title();
    private String WINDOW_TITLE_SHORT = i18n.chart_metrics_title_short();
    private String METRICS_MIN = i18n.chart_metrics_min();
    private String METRICS_MAX = i18n.chart_metrics_max();
    private String METRICS_TOTAL_COUNT = i18n.chart_metrics_total();

    public ChartMetricsView(ChartPresenter presenter) {
        super(presenter);
        this.presenter = presenter;

        isView = false;
        loadViewMetrics();
    }

    @Override
    protected ScrollPanel buildView(List<ChartMetrics> metrics) {
        CellTable<MetricsDisplay> table = new CellTable<>();

        table.addColumn(getNameValueProvider());
        table.addColumn(getValueProvider());

        List<MetricsDisplay> data = new ArrayList<>();

        for (ChartMetrics seriesMetrics : metrics) {
            data.add(new MetricsDisplay(seriesMetrics.getSeriesName(), -1));
            data.add(new MetricsDisplay(METRICS_MIN, seriesMetrics.getMin()));
            data.add(new MetricsDisplay(METRICS_MAX, seriesMetrics.getMax()));
            data.add(new MetricsDisplay(METRICS_TOTAL_COUNT, seriesMetrics.getCategoryCount()));
        }

        table.setRowData(data);
        table.setCondensed(true);
        table.setStriped(true);
        table.setBordered(true);

        ScrollPanel panel = GWT.create(ScrollPanel.class);
        panel.add(table);
        return panel;
    }
    List<ChartMetrics> chartMetricsForView;
    @Override
    protected void loadViewMetrics(){
        if(chartMetricsForView != null) {
            if(chartMetricsForView != presenter.getChartMetricsForView()){
                chartMetricsForView = presenter.getChartMetricsForView();
            }
        }else{
            chartMetricsForView = presenter.getChartMetricsForView();
        }

        setViewData(buildView(chartMetricsForView));
    }

    @Override
    protected void loadFullMetrics() {
        isView = false;
        if(globalMetrics == null) {
            HighchartPagingRequest requestForHighchartData = presenter.createRequestForHighchartData(presenter.getChartModel().getDrillSelections());

            VortexFuture<List<ChartMetrics>> future = presenter.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<List<ChartMetrics>>() {
                @Override
                public void onSuccess(List<ChartMetrics> allMetrics) {
                    globalMetrics = allMetrics;
                    setViewData(buildView(allMetrics));
                    ResizeEvent.fire(display, display.getOffsetWidth(), display.getOffsetHeight());
                }
            });

            future.execute(ChartActionsServiceProtocol.class).getChartMetrics(requestForHighchartData);
        }else{
            setViewData(buildView(globalMetrics));
            ResizeEvent.fire(display, display.getOffsetWidth(), display.getOffsetHeight());
        }
    }

    @Override
    protected String getWindowTitleShort() {
        return WINDOW_TITLE_SHORT;
    }

    @Override
    protected String getWindowTitle(){
        return WINDOW_TITLE;
    }

}

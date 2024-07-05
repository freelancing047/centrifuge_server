package csi.client.gwt.viz.timeline.view.metrics;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.AbstractMetricsView;
import csi.client.gwt.viz.shared.MetricsDisplay;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.enumerations.TimelineMetricsType;
import csi.server.common.service.api.ChronosActionsServiceProtocol;
import csi.shared.core.visualization.map.MetricPair;
import csi.shared.core.visualization.map.MetricsDTO;
import csi.shared.gwt.viz.timeline.TimelineRequest;

import java.util.ArrayList;
import java.util.List;

public class TimelineMetricsView extends AbstractMetricsView<MetricsDTO> {

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    TimelinePresenter pres;

    public TimelineMetricsView(TimelinePresenter presenter) {
        super(presenter);
        pres = presenter;
        loadFullMetrics();
        setViewMetricsEnabled(true);
    }

    @Override
    protected String getWindowTitleShort() {
        return i18n.timeline_showMetrics_shortTitle();
    }

    @Override
    protected String getWindowTitle() {
        return i18n.timeline_showMetrics_timelineMetrics();
    }

    @Override
    protected ScrollPanel buildView(List<MetricsDTO> metrics) {
        if (metrics.isEmpty()) {
            return new ScrollPanel();
        }
        MetricsDTO dto = metrics.get(0);
        CellTable<MetricsDisplay> table = new CellTable<>();


        table.addColumn(getNameValueProvider());
        table.addColumn(getValueProvider());


        List<MetricsDisplay> data = new ArrayList<>();

        for (MetricPair mapMetric : dto.getMetrics()) {
            data.add(new MetricsDisplay(mapMetric.getName(), Double.parseDouble(mapMetric.getValue())));
        }


        table.setRowData(data);

        table.setCondensed(true);
        table.setStriped(true);
        table.setBordered(true);
//        cont.add(table);

        ScrollPanel panel = new ScrollPanel();
        panel.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        panel.add(table);

        return panel;
    }

    @Override
    protected void loadViewMetrics() {
        showLoading();
        isView = true;

        if (getPresenter() instanceof TimelinePresenter) {
            TimelinePresenter pres = (TimelinePresenter) getPresenter();
            if (pres.getMode() == TimelineView.ViewMode.DETAILED) {
                List<MetricsDTO> ret = new ArrayList<>();
                ret.add(pres.getViewMetrics());
                setViewData(buildView(ret));
                ResizeEvent.fire(display, display.getOffsetWidth(), display.getOffsetHeight());
            } else {
                TimelineRequest request = pres.buildTimelineRequest();
                request.setStartTime(pres.getCurrentInterval().start);
                request.setEndTime(pres.getCurrentInterval().end);
                VortexFuture<MetricsDTO> future = pres.getVortex().createFuture();
                future.addEventHandler(new AbstractVortexEventHandler<MetricsDTO>() {
                    @Override
                    public void onSuccess(MetricsDTO result) {
                        List<MetricsDTO> ret = new ArrayList<>();
                        ret.add(result);
                        setViewData(buildView(ret));
                        ResizeEvent.fire(display, display.getOffsetWidth(), display.getOffsetHeight());
                    }
                });

                List<String> visGroups = null;

                if (pres.getVisualizationDef().getTimelineSettings().getGroupByField() != null) {
                    visGroups = pres.getVisibleTracks();
                }

                if (pres.getMode() == TimelineView.ViewMode.SUMMARY) {
                    visGroups = null;
                }
                future.execute(ChronosActionsServiceProtocol.class).getViewMetrics(request, visGroups);
            }
        }

    }

    @Override
    protected void loadFullMetrics() {
        showLoading();
        isView = false;
        VortexFuture<MetricsDTO> future = pres.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<MetricsDTO>() {
            @Override
            public void onSuccess(MetricsDTO result) {
                List<MetricsDTO> ret = new ArrayList<>();
                List<MetricPair> metricPairs = result.getMetrics();
                metricPairs.forEach(metricPair -> {
                    String metricName = metricPair.getName();
                    TimelineMetricsType metricsType = TimelineMetricsType.valueOf(metricName.toUpperCase());
                    metricPair.setName(metricsType.getLabel());
                });
                ret.add(result);
                setViewData(buildView(ret));
                ResizeEvent.fire(display, display.getOffsetWidth(), display.getOffsetHeight());
            }
        });

        TimelineRequest request = pres.buildTimelineRequest();

        if (request.getTrackName() != null) {
            future.execute(ChronosActionsServiceProtocol.class).getTotalMetrics(pres.getVisualizationDef().getUuid(), request.getTrackName());
        } else {
            future.execute(ChronosActionsServiceProtocol.class).getTotalMetrics(pres.getVisualizationDef().getUuid(), null);
        }


    }

    @Override
    public void show() {
        display.setVisible(true);
        display.setSize("220px", "100px");

        Widget parent = display.getParent();
        ((AbsolutePanel) parent).setWidgetPosition(display, parent.getElement().getOffsetWidth() - 290, 15);
    }
}

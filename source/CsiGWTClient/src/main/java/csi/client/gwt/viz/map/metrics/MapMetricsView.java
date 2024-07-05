package csi.client.gwt.viz.map.metrics;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.map.legend.MouseEventResponder;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.matrix.HideMetricsEvent;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.shared.AbstractMetricsView;
import csi.client.gwt.viz.shared.MetricsDisplay;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.enumerations.MapMetricsType;
import csi.server.common.service.api.MapActionsServiceProtocol;
import csi.shared.core.visualization.map.MapConstants;
import csi.shared.core.visualization.map.MetricPair;
import csi.shared.core.visualization.map.MetricsDTO;

import java.util.ArrayList;
import java.util.List;

public class MapMetricsView extends AbstractMetricsView<MetricsDTO> implements MouseEventResponder {
    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private MapPresenter pres;
    private UpdateTimer viewTimer;
    private List<MetricsDTO> prevViewMetrics;
    private boolean mouseSensitive = true;

    public MapMetricsView(MapPresenter presenter) {
        super(presenter);
        pres = presenter;
        isView = false;
        loadFullMetrics();

        EVENT_BUS.addHandler(HideMetricsEvent.TYPE, event -> {
            if (!event.getVisUuid().equals(pres.getUuid())) {
                return;
            }
            if (display != null) {
                display.setVisible(false);
            }
        });
    }

    @Override
    protected String getWindowTitleShort() {
        return i18n.map_metrics_title_short();
    }

    @Override
    protected String getWindowTitle() {
        return i18n.map_metrics_title();
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
            // for null types
            if (mapMetric.getName().equals(MapConstants.NULL_TYPE_NAME)) {
                mapMetric.setName(CentrifugeConstantsLocator.get().null_label());
            }
            data.add(new MetricsDisplay(mapMetric.getName(), Double.parseDouble(mapMetric.getValue())));
        }

        table.setRowData(data);
        table.setStriped(true);
        table.setCondensed(true);
        table.setBordered(true);

        ScrollPanel panel = GWT.create(ScrollPanel.class);
        panel.setPixelSize(display.getOffsetWidth(), display.getOffsetHeight());
        panel.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        panel.add(table);

        return panel;
    }

    @Override
    protected void loadViewMetrics() {
        isView = true;
        showLoading();
        // force refresh will go get the metrics regardless
        if (isForceRefresh()) {
            prevViewMetrics = null;
        }

        if (viewTimer != null) {
            viewTimer.cancel();
            viewTimer = null;
        }

        viewTimer = new UpdateTimer();
        viewTimer.schedule(1000);
    }

    private void requestViewMetrics() {
        VortexFuture<MetricsDTO> future = pres.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<MetricsDTO>() {
            @Override
            public void onSuccess(MetricsDTO result) {
                // reseeeeet
                prevViewMetrics = new ArrayList<>();
                if (result == null || result.getMetrics().isEmpty()) {
                    setViewData(new ScrollPanel());
                    return;
                }
                List<MetricPair> metricPairs = result.getMetrics();
                updateMetricsTypeForI18n(metricPairs);
                prevViewMetrics.add(result);
                setViewData(buildView(prevViewMetrics));
            }
        });
        future.execute(MapActionsServiceProtocol.class).getViewMetrics(pres.getVisualizationDef().getUuid(), pres.getDataViewUuid());
    }

    @Override
    protected void loadFullMetrics() {
        showLoading();
        isView = false;

        if (viewTimer != null) {
            viewTimer.cancel();
            viewTimer = null;
        }
        if (forceRefresh) {
            globalMetrics = null;
        }

        if (globalMetrics == null || (globalMetrics.size() > 0 && globalMetrics.get(0).getMetrics().size() > 0 && globalMetrics.get(0).getMetrics().get(0).getValue().equals("0")) || globalMetrics.get(0).getMetrics().size() == 0) {
            VortexFuture<Boolean> future = pres.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        getMapTotalMetrics();
                    }
                }
            });
            future.execute(MapActionsServiceProtocol.class).isMetricsReady(pres.getDataViewUuid(), pres.getVisualizationDef().getUuid());
        } else {
            //load cached.
            setViewData(buildView(globalMetrics));
            ResizeEvent.fire(display, display.getOffsetWidth(), display.getOffsetHeight());
        }
    }

    private void getMapTotalMetrics() {
        VortexFuture<MetricsDTO> future = pres.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<MetricsDTO>() {
            @Override
            public void onSuccess(MetricsDTO result) {
                List<MetricsDTO> ret = new ArrayList<>();
                List<MetricPair> metricPairs = result.getMetrics();
                updateMetricsTypeForI18n(metricPairs);
                ret.add(result);
                globalMetrics = ret;
                setViewData(buildView(ret));

            }
        });
        future.execute(MapActionsServiceProtocol.class).getMapTotalMetrics(pres.getVisualizationDef().getUuid(), pres.getDataViewUuid());
    }

    private void updateMetricsTypeForI18n(List<MetricPair> metricPairs) {
        metricPairs.forEach(metricPair -> {
            String metricName = metricPair.getName();
            MapMetricsType metricsType = null;
            try {
                metricsType = MapMetricsType.valueOf(metricName.toUpperCase());
            } catch (IllegalArgumentException ignore) {
            }
            if (metricsType != null) {
                metricPair.setName(metricsType.getLabel());
            }
        });
    }

    @Override
    public void revertToAnchoredPosition() {
        anchored = true;
        Widget parent = display.getParent();
        ((AbsolutePanel) parent).setWidgetPosition(display, 25, 25);
    }

    @Override
    public void show() {
        display.setVisible(true);
        display.setSize("220px", "180px");
        // because we don't update in the background - this will update;
        EVENT_BUS.fireEvent(new UpdateEvent(pres.getUuid()));
        anchored = true;
        Widget parent = display.getParent();
        ((AbsolutePanel) parent).setWidgetPosition(display, 25, 25);
    }

    @Override
    public void sensitize() {
        if (!mouseSensitive) {
            com.google.gwt.dom.client.Style style = display.getElement().getStyle();
            style.setProperty("pointerEvents", "all");
            mouseSensitive = true;
        }
    }

    @Override
    public void desensitize() {
        if (mouseSensitive) {
            com.google.gwt.dom.client.Style style = display.getElement().getStyle();
            style.setProperty("pointerEvents", "none");
            mouseSensitive = false;
        }
    }

    public class UpdateTimer extends Timer {
        UpdateTimer() {
        }

        @Override
        public void run() {
            VortexFuture<Boolean> future = pres.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        requestViewMetrics();
                    }
                }
            });
            future.execute(MapActionsServiceProtocol.class).isMetricsReady(pres.getDataViewUuid(), pres.getVisualizationDef().getUuid());
        }
    }
}

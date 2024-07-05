package csi.client.gwt.viz.matrix;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.Divider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.RefreshEvent;
import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.AbstractMetricsView;
import csi.client.gwt.viz.shared.MetricsDisplay;
import csi.client.gwt.viz.timeline.events.ResizeEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixMetrics;
import csi.shared.core.visualization.matrix.MatrixWrapper;

import java.util.ArrayList;
import java.util.List;

public class MatrixMetricsView extends AbstractMetricsView<MatrixMetrics> {
    private MatrixPresenter presenter;

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private String WINDOW_TITLE = i18n.matrix_metrics_title();
    private String WINDOW_TITLE_SHORT = i18n.matrix_metrics_title_short();
    private String METRICS_MIN = i18n.matrix_metrics_min();
    private String METRICS_MAX = i18n.matrix_metrics_max();
    private String METRICS_TOTAL = i18n.matrix_metrics_totalCells();
    private String METRICS_X_COUNT = i18n.matrix_metrics_x_count();
    private String METRICS_Y_COUNT = i18n.matrix_metrics_y_count();

    public MatrixMetricsView(MatrixPresenter presenter) {
        super(presenter);
        this.presenter = presenter;

        isView = false;
        loadViewMetrics();
    }

    @Override
    public ScrollPanel buildView(List<MatrixMetrics> met){
        MatrixMetrics metrics = met.get(0);

        CellTable<MetricsDisplay> table = new CellTable<>();

        table.addColumn(getNameValueProvider());
        table.addColumn(getValueProvider());

        List<MetricsDisplay> data = new ArrayList<>();
        data.add(new MetricsDisplay(METRICS_MIN, metrics.getMinValue()));
        data.add(new MetricsDisplay(METRICS_MAX, metrics.getMaxValue()));
        data.add(new MetricsDisplay(METRICS_TOTAL, metrics.getTotalCells()));

        data.add(new MetricsDisplay(METRICS_X_COUNT,  metrics.getAxisXCount()));
        data.add(new MetricsDisplay(METRICS_Y_COUNT,  metrics.getAxisYCount()));

        table.setRowData(data);

        table.setCondensed(true);
        table.setStriped(true);
        table.setBordered(true);

        ScrollPanel panel = new ScrollPanel();
        panel.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        panel.add(table);

        return panel;
    }

    boolean isRequestingViewMetrics = false;
    MatrixDataRequest prevRequest = null;
    List<MatrixMetrics> viewMetrics = null;
    @Override
    protected void loadViewMetrics() {
        prevRequest = isForceRefresh() ? null : prevRequest;
        if(display != null && !display.isVisible()){
            return;
        }


        if(presenter.getModel().isSummary()){
            getSummaryMetrics();
        }else{
            setViewData(buildView(presenter.getModel().calculateViewMetrics()));
        }

    }

    private void getSummaryMetrics() {
        MatrixDataRequest viewMetricsRequest = presenter.getView().getViewMetricsRequest();

        if(prevRequest == null){
            showLoading();
            prevRequest = viewMetricsRequest;
        }else{
            // don't request if we have the same view port and metrics.
            if(prevRequest.equals(viewMetricsRequest) && viewMetrics != null) {
                setViewData(buildView(viewMetrics));
                return;
            }else{
                prevRequest = viewMetricsRequest;
            }
        }

        VortexFuture<MatrixMetrics> vortexFuture = getPresenter().getVortex().createFuture();
        showLoading();
        vortexFuture.addEventHandler(new VortexEventHandler<MatrixMetrics>() {
            @Override
            public void onSuccess(MatrixMetrics result) {
                if(result != null && isRequestingViewMetrics){
                    viewMetrics = new ArrayList<>();
                    viewMetrics.add(result);
                    setViewData(buildView(viewMetrics));
                }
                isRequestingViewMetrics = false;
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {

            }

            @Override
            public void onCancel() {

            }
        });


        isRequestingViewMetrics = true;
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getViewMetrics(viewMetricsRequest);
    }

    @Override
    protected void loadFullMetrics() {
        isRequestingViewMetrics = false;
        ArrayList<MatrixMetrics> matrixMetrics = new ArrayList<>();
        matrixMetrics.add(presenter.getModel().getMetrics());
        setViewData(buildView(matrixMetrics));
    }

    @Override
    public void show(){
        display.setSize("200px","185px");
        revertToAnchoredPosition();
        display.setVisible(true);


        EVENT_BUS.fireEvent(new UpdateEvent(presenter.getUuid()));
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

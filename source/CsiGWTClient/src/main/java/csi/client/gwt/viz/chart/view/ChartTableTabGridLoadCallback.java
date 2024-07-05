package csi.client.gwt.viz.chart.view;

import java.util.ArrayList;
import java.util.List;

import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

import csi.client.gwt.widget.gxt.grid.paging.LoadCallback;
import csi.server.common.service.api.ChartActionsServiceProtocol;
import csi.shared.core.visualization.chart.HighchartResponse;
import csi.shared.core.visualization.chart.HighchartSeriesData;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartTableTabGridLoadCallback implements LoadCallback<ChartActionsServiceProtocol, ChartGridRow> {

    final HighchartResponse response;

    public ChartTableTabGridLoadCallback(HighchartResponse response){
        this.response = response;
    }

    @Override
    public PagingLoadResult<ChartGridRow> onLoadCallback(ChartActionsServiceProtocol vortexService, FilterPagingLoadConfig loadConfig) {
        List<ChartGridRow> list = buildRowListFromResponse();
        return createPagingLoadResultBean(list);
    }

    private List<ChartGridRow> buildRowListFromResponse() {
        List<ChartGridRow> list = new ArrayList<ChartGridRow>();
        for (int i = 0; i < response.getCategorySize(); i++) {
            ChartGridRow row = createChartGridRow(i);
            list.add(row);
        }
        return list;
    }

    private ChartGridRow createChartGridRow(int i) {
        ChartGridRow row = new ChartGridRow();
        String dimension = response.getCategoryNames().get(i);
        dimension = dimension.replace("&quot;", "\"").replace("&#39;", "\'").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
        row.setDimension(dimension);
        for (HighchartSeriesData seriesData : response.getSeriesData()) {
            row.getMetricValues().add(seriesData.getData().get(i));
        }
        return row;
    }

    private PagingLoadResultBean<ChartGridRow> createPagingLoadResultBean(List<ChartGridRow> list) {
        PagingLoadResultBean<ChartGridRow> result = new PagingLoadResultBean<ChartGridRow>();
        result.setOffset(0);
        result.setTotalLength(response.getCategorySize());
        result.setData(list);
        return result;
    }
}

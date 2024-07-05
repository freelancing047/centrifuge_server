package csi.shared.core.visualization.chart;

public class HighchartPagingResponse extends HighchartResponse {
    private OverviewResponse overviewResponse;
    private int start;
    private int limit;

    public OverviewResponse getOverviewResponse() {
        return overviewResponse;
    }

    public void setOverviewResponse(OverviewResponse overviewResponse) {
        this.overviewResponse = overviewResponse;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public HighchartPagingResponse copy() {
        HighchartPagingResponse response = super.copy();
        response.setStart(start);
        response.setLimit(limit);
        response.setOverviewResponse(overviewResponse);
        return response;
    }

}

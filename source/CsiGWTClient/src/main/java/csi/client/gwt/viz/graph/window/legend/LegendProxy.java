package csi.client.gwt.viz.graph.window.legend;

import java.util.List;

import com.google.gwt.user.client.Random;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.CsiEvent;
import csi.client.gwt.events.CsiEventCommander;
import csi.client.gwt.events.CsiEventHandler;
import csi.client.gwt.events.CsiEventHeader;
import csi.client.gwt.vortex.Callback;
import csi.server.business.visualization.graph.base.GraphLegendNodeSummary;
import csi.server.business.visualization.legend.GraphLegendInfo;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class LegendProxy {

    private List<GraphNodeLegendItem> nodeLegendItems;
    private List<GraphLinkLegendItem> linkLegendItems;
    private GraphLegendNodeSummary graphLegendNodeSummary;
    private CsiEventHeader myEventHeaders;
    private String vizUuid;
    Callback<GraphLegendInfo> legendDataCallback = new Callback<GraphLegendInfo>() {

        @Override
        public void onSuccess(GraphLegendInfo result) {
            linkLegendItems = result.getLinkLegendItems();
            nodeLegendItems = result.getNodeLegendItems();
            graphLegendNodeSummary = result.graphLegendNodeSummary;
            new CsiEvent(myEventHeaders).fire();
        }
    };


    public LegendProxy(String vizUuid) {
        this.vizUuid = vizUuid;
        myEventHeaders = new CsiEventHeader();
        myEventHeaders.addHeader("LegendProxy", "" + Random.nextDouble());

        try {
            WebMain.injector.getVortex().execute(legendDataCallback, GraphActionServiceProtocol.class)
                    .legendData(vizUuid);
        } catch (CentrifugeException ignored) {

        }
    }


    public List<GraphNodeLegendItem> getNodeLegendItems() {
        return nodeLegendItems;
    }


    public List<GraphLinkLegendItem> getLinkLegendItems() {
        return linkLegendItems;
    }


    public GraphLegendNodeSummary getGraphLegendNodeSummary() {
        return graphLegendNodeSummary;
    }


    public void addLoadHandler(CsiEventHandler csiEventHandler) {
        CsiEventCommander.getInstance().addHandler(myEventHeaders, csiEventHandler);
    }


    public String getVizUuid() {
        return vizUuid;
    }


    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }
}
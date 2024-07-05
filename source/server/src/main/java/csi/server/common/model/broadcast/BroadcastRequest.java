package csi.server.common.model.broadcast;

import java.io.Serializable;

import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.Selection;

/**
 * Request made by visualizations listening to a broadcast.
 * Also used to clear broadcasts.
 */
public class BroadcastRequest implements Serializable {

    private String dataViewUuid;

    private String broadcasterVizUuid;
    private Selection broadcasterSelection;
    private VisualizationDef broadcastingViz;
    private BroadcastRequestType broadcastRequestType;
    private BroadcastSet broadcastSet;

    private String listeningVizUuid;
    private Selection listeningVizSelection;


    public String getDataViewUuid() {
        return dataViewUuid;
    }

    public void setDataViewUuid(String dataViewUuid) {
        this.dataViewUuid = dataViewUuid;
    }

    public String getBroadcasterVizUuid() {
        return broadcasterVizUuid;
    }

    public void setBroadcasterVizUuid(String broadcasterVizUuid) {
        this.broadcasterVizUuid = broadcasterVizUuid;
    }

    public Selection getBroadcasterSelection() {
        return broadcasterSelection;
    }

    public void setBroadcasterSelection(Selection broadcasterSelection) {
        this.broadcasterSelection = broadcasterSelection;
    }

    public String getListeningVizUuid() {
        return listeningVizUuid;
    }

    public void setListeningVizUuid(String listeningVizUuid) {
        this.listeningVizUuid = listeningVizUuid;
    }

    public Selection getListeningVizSelection() {
        return listeningVizSelection;
    }

    public void setListeningVizSelection(Selection listeningVizSelection) {
        this.listeningVizSelection = listeningVizSelection;
    }

    public BroadcastRequestType getBroadcastRequestType() {
        return broadcastRequestType;
    }

    public void setBroadcastRequestType(BroadcastRequestType broadcastRequestType) {
        this.broadcastRequestType = broadcastRequestType;
    }

    public BroadcastSet getBroadcastSet() {
        return broadcastSet;
    }

    public void setBroadcastSet(BroadcastSet broadcastSet) {
        this.broadcastSet = broadcastSet;
    }

    public VisualizationDef getBroadcastingViz() {
        return broadcastingViz;
    }

    public void setBroadcastingViz(VisualizationDef broadcastingViz) {
        this.broadcastingViz = broadcastingViz;
    }
}

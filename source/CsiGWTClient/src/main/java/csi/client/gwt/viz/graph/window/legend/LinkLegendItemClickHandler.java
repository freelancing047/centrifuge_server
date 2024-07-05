package csi.client.gwt.viz.graph.window.legend;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.GraphActionServiceProtocol;

/**
 * Created by Patrick on 8/21/2014.
 */
public class LinkLegendItemClickHandler implements ClickHandler {

    private String vizuuid;
    private String dvuuid;
    private String linkKey;
    private Graph graph;


    public LinkLegendItemClickHandler(Graph graph, String key) {
        this.graph = graph;
        vizuuid = graph.getUuid();
        dvuuid = graph.getDataviewUuid();
        this.linkKey = key;
    }


    @SuppressWarnings("unused")
    @Override
    public void onClick(ClickEvent event) {
        SelectionOperation selectionOperation;
        if (event.getNativeEvent().getShiftKey() && event.getNativeEvent().getCtrlKey()) {
            selectionOperation = SelectionOperation.SELECTION_OPERATION_DESELECT;
        } else if (event.getNativeEvent().getCtrlKey() || event.getNativeEvent().getShiftKey()) {
            selectionOperation = SelectionOperation.SELECTION_OPERATION_APPEND;
        } else {
            selectionOperation = SelectionOperation.SELECTION_OPERATION_CLEAR;
        }
        // TODO: this should probably move to graph's model??
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).toggleLinkSelectionByType(vizuuid, dvuuid, linkKey,
                    selectionOperation.toString());
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        graph.getGraphSurface().refresh(vortexFuture);
        

        event.stopPropagation();
        event.preventDefault();
    }

    enum SelectionOperation {

        SELECTION_OPERATION_APPEND {
            @Override
            public String toString() {
                return "selectionOperationAppend";
            }//NON-NLS
        },
        SELECTION_OPERATION_CLEAR {
            @Override
            public String toString() {
                return "selectionOperationClear";
            }//NON-NLS
        },
        SELECTION_OPERATION_DESELECT {
            @Override
            public String toString() {
                return "selectionOperationDeselect";
            }//NON-NLS
        }
    }

    ;
}

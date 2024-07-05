package csi.client.gwt.viz.map.legend;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class UpdatedPlaceLegendItemClickHandler implements ClickHandler {
	private MapPresenter mapPresenter;
	
	public UpdatedPlaceLegendItemClickHandler(MapPresenter mapPresenter) {
		this.mapPresenter = mapPresenter;
    }

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
        mapPresenter.updatedPlaceClicked(selectionOperation.toString());
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
    };
}

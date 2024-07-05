package csi.client.gwt.widget.gxt.drag_n_drop;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sencha.gxt.dnd.core.client.DND;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DropTarget;

import csi.client.gwt.edit_sources.DataSourceEditorView;
import csi.client.gwt.csiwizard.support.ConnectionItemType;
import csi.client.gwt.csiwizard.support.ConnectionTreeItem;
import csi.client.gwt.events.CsiDropEvent;
import csi.server.common.model.SqlTableDef;

/**
 * Created by centrifuge on 8/1/2016.
 */
public class LienzoPanelDropTarget extends DropTarget {

    private ResizeableLienzoPanel _panel;
    private DataSourceEditorView _totalView;

    public LienzoPanelDropTarget(ResizeableLienzoPanel panelIn, DataSourceEditorView totalViewIn) {

        super(panelIn);

        _panel = panelIn;
        _totalView = totalViewIn;

        setOperation(DND.Operation.COPY);
        setFeedback(DND.Feedback.BOTH);
    }

    @Override
    protected void onDragDrop(final DndDropEvent eventIn) {

        final Object myDataDrop = eventIn.getData();
        final SqlTableDef myDataTarget = _totalView.sourceTableSelected()
                ? _totalView.sourceTableSelection()
                : null;

        DeferredCommand.add(new Command() {

            public void execute() {

                _panel.fireEvent(new CsiDropEvent(myDataDrop, myDataTarget));
            }
        });
    }

    @Override
    protected void showFeedback(DndDragMoveEvent eventIn) {

        boolean myAcceptanceFlag = false;
        Object myObject = eventIn.getData();
        ConnectionTreeItem myConnectionItem = null;

        if ((null != myObject) && (myObject instanceof ConnectionTreeItem)) {

            myConnectionItem = (ConnectionTreeItem) myObject;

            myAcceptanceFlag = ConnectionItemType.TABLE.equals(myConnectionItem.type)
                    || ConnectionItemType.CUSTOM_QUERY.equals(myConnectionItem.type);
        }
        eventIn.getStatusProxy().setStatus(myAcceptanceFlag);
    }
}

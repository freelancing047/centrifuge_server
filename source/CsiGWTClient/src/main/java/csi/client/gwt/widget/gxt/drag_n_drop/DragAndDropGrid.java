package csi.client.gwt.widget.gxt.drag_n_drop;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.dnd.core.client.DND;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.events.CsiDropEvent;
import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.common.model.CsiUUID;

/**
 * Created by centrifuge on 3/23/2016.
 */
public class DragAndDropGrid<T> extends ResizeableGrid<T> {

    private String _id = CsiUUID.randomUUID();
    private GridDropTarget<T> _target;
    private GridDragSource<T> _source;

    public DragAndDropGrid(ListStore<T> store, ColumnModel<T> cm, GridView<T> view) {
        super(store, cm, view);
        init();
    }

    public DragAndDropGrid(ListStore<T> store, ColumnModel<T> cm) {
        super(store, cm);
        init();
    }

    public DragAndDropGrid(ListStore<T> store, ColumnModel<T> cm, boolean showBorderIn) {
        super(store, cm, showBorderIn);
        init();
    }

    public String getGridId() {

        return _id;
    }

    protected void setGridId(String idIn) {

        _id = idIn;
    }

    public HandlerRegistration addCsiDropEventHandler(CsiDropEventHandler handlerIn) {

        return addHandler(handlerIn, CsiDropEvent.type);
    }

    private void init() {

        autoResizeColumns(true);

        _target = new GridDropTarget<T>(this) {

            @Override
            protected void onDragDrop(final DndDropEvent eventIn) {

                final Object myDataDrop = eventIn.getData();
                final T myDataTarget = grid.getSelectionModel().getSelectedItem();

                DeferredCommand.add(new Command() {

                    public void execute() {

                        fireEvent(new CsiDropEvent(myDataDrop, myDataTarget));
                    }
                });
            }

            @Override
            protected void showFeedback(DndDragMoveEvent eventIn) {

                EventTarget target = eventIn.getDragMoveEvent().getNativeEvent().getEventTarget();
                Element row = grid.getView().findRow(Element.as(target)).cast();
                int idx = (row != null) ? grid.getView().findRowIndex(row) : -1;

                activeItem = ((0 <= idx) && (grid.getStore().size() > idx)) ? grid.getStore().get(idx) : null;
                if (null != activeItem) {
                    grid.getSelectionModel().select(idx, false);
                    eventIn.getStatusProxy().setStatus(true);
                } else {
                    grid.getSelectionModel().deselectAll();
                    eventIn.getStatusProxy().setStatus(false);
                }
            }
        };
        _source = new GridDragSource<T>(this) {

            @Override
            protected void onDragDrop(DndDropEvent event) {

            }
        };
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);
    }
}

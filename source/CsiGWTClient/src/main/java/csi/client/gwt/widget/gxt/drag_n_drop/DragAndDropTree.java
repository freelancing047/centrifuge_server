package csi.client.gwt.widget.gxt.drag_n_drop;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Panel;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.dnd.core.client.DND;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.TreeDragSource;
import com.sencha.gxt.dnd.core.client.TreeDropTarget;
import com.sencha.gxt.widget.core.client.tree.Tree;

import csi.client.gwt.events.CsiDropEvent;
import csi.client.gwt.events.CsiDropEventHandler;
import csi.server.common.model.CsiUUID;

/**
 * Created by centrifuge on 8/1/2016.
 */
public class DragAndDropTree<T> extends Tree<T, String> {

    protected String _id = CsiUUID.randomUUID();
    protected TreeDropTarget<T> _target;
    protected TreeDragSource<T> _source;
    protected Panel _parent;
    protected ClickHandler _selectionHandler;

    public DragAndDropTree(Panel parentIn, TreeStore<T> storeIn,
                           ValueProvider<T, String> valueProviderIn,
                           ClickHandler selectionHandlerIn,
                           boolean supportTargetIn, boolean supportSourceIn) {

        super(storeIn, valueProviderIn);

        _parent = parentIn;
        _selectionHandler = selectionHandlerIn;

        getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);

        if (supportTargetIn) {

            initTarget();
        }
        if (supportSourceIn) {

            initSource();
        }
    }

    public String getTreeId() {

        return _id;
    }

    protected void setTreeId(String idIn) {

        _id = idIn;
    }

    public HandlerRegistration addCsiDropEventHandler(CsiDropEventHandler handlerIn) {

        return addHandler(handlerIn, CsiDropEvent.type);
    }

    private void initTarget() {

        _target = new TreeDropTarget<T>(this) {

            @Override
            protected void onDragDrop(final DndDropEvent eventIn) {

                final Object myDataDrop = eventIn.getData();
                final T myDataTarget = getSelectionModel().getSelectedItem();

                DeferredCommand.add(new Command() {

                    public void execute() {

                        fireEvent(new CsiDropEvent(myDataDrop, myDataTarget));
                    }
                });
            }
        };
        _target.setAllowSelfAsSource(true);
        _target.setAllowDropOnLeaf(true);
        _target.setAddChildren(true);
        _target.setAutoExpand(true);
        _target.setAutoScroll(true);
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);
    }

    private void initSource() {

        _source = new TreeDragSource<T>(this) {

            @Override
            protected void onDragStart(DndDragStartEvent eventIn) {

                T mySelection = getWidget().getSelectionModel().getSelectedItem();

                if (null == mySelection) {
                    eventIn.setCancelled(true);
                    return;
                }
                eventIn.setData(mySelection);
                eventIn.getStatusProxy().update(SafeHtmlUtils.fromString(mySelection.toString())); //http://docs.sencha.com/gxt/4.x/guides/ui/SafeHtml.html
            }
        };
    }
}

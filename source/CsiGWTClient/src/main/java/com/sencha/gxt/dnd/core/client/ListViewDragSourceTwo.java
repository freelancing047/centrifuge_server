package com.sencha.gxt.dnd.core.client;

import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DragSource;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.dnd.core.client.DND.Operation;
import com.sencha.gxt.messages.client.DefaultMessages;
import java.util.Iterator;
import java.util.List;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.widget.core.client.ListViewTwo;

public class ListViewDragSourceTwo<M> extends DragSource {
    protected ListViewTwo<M, ?> listView;
    protected ListViewDragSourceTwo.ListViewDragSourceMessages messages;

    public ListViewDragSourceTwo(ListViewTwo<M, ?> listView) {
        super(listView);
        this.listView = listView;
    }

    public ListViewTwo<M, ?> getListView() {
        return this.listView;
    }

    public ListViewDragSourceTwo.ListViewDragSourceMessages getMessages() {
        if (this.messages == null) {
            this.messages = new ListViewDragSourceTwo.DefaultListViewDragSourceMessages();
        }

        return this.messages;
    }

    public void setMessages(ListViewDragSourceTwo.ListViewDragSourceMessages messages) {
        this.messages = messages;
    }

    protected void onDragDrop(DndDropEvent event) {
        if (event.getOperation() == Operation.MOVE) {
            Object data = event.getData();
            if (data instanceof List) {
                List<M> list = (List)data;
                Iterator i$ = list.iterator();

                while(i$.hasNext()) {
                    M item = (M) i$.next();
                    this.listView.getStore().remove(item);
                }
            }

            super.data = null;
        }

    }

    protected void onDragStart(DndDragStartEvent event) {
        Element r = this.listView.findElement(event.getDragStartEvent().getStartElement());
        if (r == null) {
            event.setCancelled(true);
        } else {
            List<M> sel = this.listView.getSelectionModel().getSelectedItems();
            if (sel.size() > 0) {
                event.setCancelled(false);
                event.setData(sel);
                if (this.getStatusText() == null) {
                    event.getStatusProxy().update(SafeHtmlUtils.fromString(this.getMessages().itemsSelected(sel.size())));
                } else {
                    event.getStatusProxy().update(SafeHtmlUtils.fromString(Format.substitute(this.getStatusText(), sel.size())));
                }
            }

        }
    }

    protected class DefaultListViewDragSourceMessages implements ListViewDragSourceTwo.ListViewDragSourceMessages {
        protected DefaultListViewDragSourceMessages() {
        }

        public String itemsSelected(int items) {
            return DefaultMessages.getMessages().listField_itemsSelected(items);
        }
    }

    public interface ListViewDragSourceMessages {
        String itemsSelected(int var1);
    }
}

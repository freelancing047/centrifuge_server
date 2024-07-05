package com.sencha.gxt.dnd.core.client;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Element;
import com.sencha.gxt.core.client.GXTLogConfiguration;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Rectangle;
import com.sencha.gxt.widget.core.client.ListViewTwo;
import com.sencha.gxt.widget.core.client.event.XEvent;

public class ListViewDropTargetTwo<M> extends DropTarget {
   private static final Logger LOG = Logger.getLogger(ListViewDropTarget.class.getName());

    protected ListViewTwo<M, ?> listView;
    protected M activeItem;
    protected int insertIndex;
    protected boolean before;
    private boolean autoSelect;

    public ListViewDropTargetTwo(ListViewTwo<M, ?> listView) {
        super(listView);
        this.listView = listView;
    }

    public ListViewTwo<M, ?> getListView() {
        return this.listView;
    }

    public boolean isAutoSelect() {
        return this.autoSelect;
    }

    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    protected void onDragDrop(DndDropEvent event) {
        super.onDragDrop(event);
        Object data = event.getData();
        List<M> models = (List<M>) this.prepareDropData(data, true);
        if (models.size() > 0) {
            if (this.feedback == DND.Feedback.APPEND) {
                this.listView.getStore().addAll(models);
            } else {
                this.listView.getStore().addAll(this.insertIndex, models);
            }
        }

        this.insertIndex = -1;
        this.activeItem = null;
    }

    protected void onDragEnter(DndDragEnterEvent event) {
        super.onDragEnter(event);
        event.setCancelled(true);
        event.getStatusProxy().setStatus(true);
    }

    protected void onDragLeave(DndDragLeaveEvent event) {
        super.onDragLeave(event);
        Insert insert = Insert.get();
        insert.setVisible(false);
    }

    protected void onDragMove(DndDragMoveEvent event) {
        XElement target = this.getElementFromEvent(event.getDragMoveEvent().getNativeEvent());
        if (!this.listView.getElement().isOrHasChild(target)) {
            event.setCancelled(true);
            event.getStatusProxy().setStatus(false);
        } else {
            event.setCancelled(false);
            event.getStatusProxy().setStatus(true);
        }

    }

    protected void showFeedback(DndDragMoveEvent event) {
        event.getStatusProxy().setStatus(true);
        Element target = this.getElementFromEvent(event.getDragMoveEvent().getNativeEvent());
        if (this.feedback == DND.Feedback.INSERT) {
            Element row = target != null ? this.listView.findElement(target) : null;
            if (row == null && this.listView.getStore().size() > 0) {
                row = (Element)this.listView.getElement(this.listView.getStore().size() - 1).cast();
            }

            if (row != null) {
                int height = row.getOffsetHeight();
                int mid = height / 2;
                mid += row.getAbsoluteTop();
                int y = ((XEvent)event.getDragMoveEvent().getNativeEvent().cast()).getXY().getY();
                this.before = y < mid;
                int idx = this.listView.findElementIndex(row);
                this.activeItem = this.listView.getStore().get(idx);
                this.insertIndex = this.adjustIndex(event, idx);
                this.showInsert(event, row);
            } else {
                this.insertIndex = 0;
            }
        }

    }

    private int adjustIndex(DndDragMoveEvent event, int index) {
        Object data = event.getData();
        int i = index;
        List<M> models = (List<M>) this.prepareDropData(data, true);
        Iterator i$ = models.iterator();

        while(true) {
            while(true) {
                int idx;
                do {
                    if (!i$.hasNext()) {
                        return this.before ? i : i + 1;
                    }

                    M m = (M) i$.next();
                    idx = this.listView.getStore().indexOf(m);
                } while(idx <= -1);

                if (this.before) {
                    if (idx < index) {
                        break;
                    }
                } else if (idx <= index) {
                    break;
                }
            }

            --i;
        }
    }

    private void showInsert(DndDragMoveEvent event, Element row) {
        Insert insert = Insert.get();
        insert.show(row.getParentElement());
        Rectangle rect = ((XElement)row.cast()).getBounds();
        int y = rect.getY() - 2;
        if (!this.before) {
            y = rect.getY() + rect.getHeight() - 4;
        }

        insert.getElement().makePositionable(true);
        insert.getElement().setBounds(rect.getX(), y, rect.getWidth(), 6);
        if (GXTLogConfiguration.loggingIsEnabled()) {
           LOG.finest("showInsert: y=" + y + " before=" + this.before + " rect=" + rect);
        }

    }
}

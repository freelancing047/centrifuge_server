package com.sencha.gxt.widget.core.client.form;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.dnd.core.client.DND.Feedback;
import com.sencha.gxt.dnd.core.client.ListViewDragSourceTwo;
import com.sencha.gxt.dnd.core.client.ListViewDropTargetTwo;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.ListViewTwo;
import com.sencha.gxt.widget.core.client.button.IconButton;
import com.sencha.gxt.widget.core.client.button.IconButton.IconConfig;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DualListFieldTwo<M, T>  extends AdapterField<List<M>> {
    protected DualListFieldTwo.Mode mode;
    protected ListViewDragSourceTwo<M> sourceFromField;
    protected ListViewDragSourceTwo<M> sourceToField;
    protected ListViewDropTargetTwo<M> targetFromField;
    protected ListViewDropTargetTwo<M> targetToField;
    protected IconButton up;
    protected IconButton allRight;
    protected IconButton right;
    protected IconButton left;
    protected IconButton allLeft;
    protected IconButton down;
    private DualListFieldTwo.DualListFieldMessages messages;
    private VerticalPanel buttonBar;
    private ListViewTwo<M, T> fromView;
    private ListViewTwo<M, T> toView;
    private ListStore<M> fromStore;
    private ListStore<M> toStore;
    private final DualListField.DualListFieldAppearance appearance;
    private String dndGroup;
    private boolean enableDnd;

    public DualListFieldTwo(ModelKeyProvider<? super M> keyProvider, ValueProvider<? super M, T> valueProvider, Cell<T> cell) {
        this(new ListStore(keyProvider), new ListStore(keyProvider), valueProvider, cell);
    }

    @UiConstructor
    public DualListFieldTwo(ListStore<M> fromStore, ListStore<M> toStore, ValueProvider<? super M, T> valueProvider, Cell<T> cell) {
        this(fromStore, toStore, valueProvider, cell, (DualListField.DualListFieldAppearance)GWT.create(DualListField.DualListFieldAppearance.class));
    }

    public DualListFieldTwo(ListStore<M> fromStore, ListStore<M> toStore, ValueProvider<? super M, T> valueProvider, Cell<T> cell, DualListField.DualListFieldAppearance appearance) {
        super(new HorizontalPanel());
        this.mode = DualListFieldTwo.Mode.APPEND;
        this.enableDnd = true;
        this.appearance = appearance;
        this.fromStore = fromStore;
        this.toStore = toStore;
        HorizontalPanel panel = (HorizontalPanel)this.getWidget();
        this.buttonBar = new VerticalPanel();
        this.fromView = new ListViewTwo(this.fromStore, valueProvider);
        this.fromView.setCell(cell);
        this.fromView.setWidth(125);
        this.toView = new ListViewTwo(this.toStore, valueProvider);
        this.toView.setCell(cell);
        this.toView.setWidth(125);
        this.buttonBar.setSpacing(3);
        this.buttonBar.getElement().getStyle().setProperty("margin", "7px");
        this.buttonBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        this.up = new IconButton(appearance.up());
        this.up.setToolTip(this.getMessages().moveUp());
        this.up.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                DualListFieldTwo.this.onUp();
            }
        });
        this.allRight = new IconButton(appearance.allRight());
        this.allRight.setToolTip(this.getMessages().addAll());
        this.allRight.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                DualListFieldTwo.this.onAllRight();
            }
        });
        this.right = new IconButton(appearance.right());
        this.right.setToolTip(this.getMessages().addSelected());
        this.right.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                DualListFieldTwo.this.onRight();
            }
        });
        this.left = new IconButton(appearance.left());
        this.left.setToolTip(this.getMessages().removeSelected());
        this.left.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                DualListFieldTwo.this.onLeft();
            }
        });
        this.allLeft = new IconButton(appearance.allLeft());
        this.allLeft.setToolTip(this.getMessages().removeAll());
        this.allLeft.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                DualListFieldTwo.this.onAllLeft();
            }
        });
        this.down = new IconButton(appearance.down());
        this.down.setToolTip(this.getMessages().moveDown());
        this.down.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                DualListFieldTwo.this.onDown();
            }
        });
        this.buttonBar.add(this.up);
        this.buttonBar.add(this.allRight);
        this.buttonBar.add(this.right);
        this.buttonBar.add(this.left);
        this.buttonBar.add(this.allLeft);
        this.buttonBar.add(this.down);
        panel.add(this.fromView);
        panel.add(this.buttonBar);
        panel.add(this.toView);
        this.setMode(this.mode);
        this.setPixelSize(200, 125);
    }

    public DualListField.DualListFieldAppearance getAppearance() {
        return this.appearance;
    }

    public String getDndGroup() {
        return this.dndGroup;
    }

    public ListViewDragSourceTwo<M> getDragSourceFromField() {
        return this.sourceFromField;
    }

    public ListViewDragSourceTwo<M> getDragSourceToField() {
        return this.sourceToField;
    }

    public ListViewDropTargetTwo<M> getDropTargetFromField() {
        return this.targetFromField;
    }

    public ListViewDropTargetTwo<M> getDropTargetToField() {
        return this.targetToField;
    }

    public ListViewTwo<M, T> getFromView() {
        return this.fromView;
    }

    public ListStore<M> getFromStore() {
        return this.fromStore;
    }

    public DualListFieldTwo.DualListFieldMessages getMessages() {
        if (this.messages == null) {
            this.messages = new DualListFieldTwo.DualListFieldDefaultMessages();
        }

        return this.messages;
    }

    public DualListFieldTwo.Mode getMode() {
        return this.mode;
    }

    public ListViewTwo<M, T> getToView() {
        return this.toView;
    }

    public ListStore<M> getToStore() {
        return this.toStore;
    }

    public List<M> getValue() {
        return this.toStore.getAll();
    }

    public IconButton getLeftButton() {
        return this.left;
    }

    public IconButton getAllLeftButton() {
        return this.allLeft;
    }

    public IconButton getRightButton() {
        return this.right;
    }

    public IconButton getAllRightButton() {
        return this.allRight;
    }

    public IconButton getUpButton() {
        return this.up;
    }

    public IconButton getDownButton() {
        return this.down;
    }

    public boolean isEnableDnd() {
        return this.enableDnd;
    }

    public void setDndGroup(String group) {
        if (group == null) {
            group = this.getId() + "-group";
        }

        this.dndGroup = group;
        if (this.sourceFromField != null) {
            this.sourceFromField.setGroup(this.dndGroup);
        }

        if (this.sourceToField != null) {
            this.sourceToField.setGroup(this.dndGroup);
        }

        if (this.targetFromField != null) {
            this.targetFromField.setGroup(this.dndGroup);
        }

        if (this.targetToField != null) {
            this.targetToField.setGroup(this.dndGroup);
        }

    }

    public void setEnableDnd(boolean enableDnd) {
        if (enableDnd) {
            if (this.sourceFromField == null) {
                this.sourceFromField = new ListViewDragSourceTwo(this.fromView);
                this.sourceToField = new ListViewDragSourceTwo(this.toView);
                this.targetFromField = new ListViewDropTargetTwo(this.fromView);
                this.targetFromField.setAutoSelect(true);
                this.targetToField = new ListViewDropTargetTwo(this.toView);
                this.targetToField.setAutoSelect(true);
                if (this.mode == DualListFieldTwo.Mode.INSERT) {
                    this.targetToField.setAllowSelfAsSource(true);
                    this.targetFromField.setFeedback(Feedback.INSERT);
                    this.targetToField.setFeedback(Feedback.INSERT);
                }

                this.setDndGroup(this.dndGroup);
            }
        } else {
            if (this.sourceFromField != null) {
                this.sourceFromField.release();
                this.sourceFromField = null;
            }

            if (this.sourceToField != null) {
                this.sourceToField.release();
                this.sourceToField = null;
            }

            if (this.targetFromField != null) {
                this.targetFromField.release();
                this.targetFromField = null;
            }

            if (this.targetToField != null) {
                this.targetToField.release();
                this.targetToField = null;
            }
        }

        this.enableDnd = enableDnd;
    }

    public void setMessages(DualListFieldTwo.DualListFieldMessages messages) {
        this.messages = messages;
    }

    public void setMode(DualListFieldTwo.Mode mode) {
        this.mode = mode;
        switch(mode) {
            case APPEND:
                this.up.setVisible(false);
                this.down.setVisible(false);
                break;
            case INSERT:
                this.up.setVisible(true);
                this.down.setVisible(true);
        }

    }

    public void setValue(List<M> value) {
        if (value != null && !value.isEmpty()) {
            value = new ArrayList(value);
            List<M> nonSelectedItems = new ArrayList(this.toStore.getAll());
            nonSelectedItems.addAll(this.fromStore.getAll());
            value.retainAll(nonSelectedItems);
            nonSelectedItems.removeAll(value);
            this.fromStore.replaceAll(nonSelectedItems);
            this.toStore.replaceAll(value);
        } else {
            this.onAllLeft();
        }
    }

    protected void onAllLeft() {
        List<M> sel = this.toStore.getAll();
        this.fromStore.addAll(sel);
        this.toStore.clear();
    }

    protected void onAllRight() {
        List<M> sel = this.fromStore.getAll();
        this.toStore.addAll(sel);
        this.fromStore.clear();
    }

    protected void onDisable() {
        super.onDisable();
        this.fromView.disable();
        this.toView.disable();
        this.allLeft.disable();
        this.allRight.disable();
        this.right.disable();
        this.left.disable();
        this.up.disable();
        this.down.disable();
    }

    protected void onDown() {
        this.toView.moveSelectedDown();
    }

    protected void onEnable() {
        super.onEnable();
        this.fromView.enable();
        this.toView.enable();
        this.allLeft.enable();
        this.allRight.enable();
        this.right.enable();
        this.left.enable();
        this.up.enable();
        this.down.enable();
    }

    protected void onLeft() {
        List<M> sel = this.toView.getSelectionModel().getSelectedItems();
        Iterator i$ = sel.iterator();

        while(i$.hasNext()) {
            M m = (M) i$.next();
            this.toStore.remove(m);
        }

        this.fromStore.addAll(sel);
        this.fromView.getSelectionModel().select(sel, false);
    }

    protected void onResize(int width, int height) {
        super.onResize(width, height);
        int w = (width - (this.buttonBar.getOffsetWidth() + 14)) / 2;
        this.fromView.setPixelSize(w, height);
        this.toView.setPixelSize(w, height);
    }

    protected void onRight() {
        List<M> sel = this.fromView.getSelectionModel().getSelectedItems();
        Iterator i$ = sel.iterator();

        while(i$.hasNext()) {
            M m = (M) i$.next();
            this.fromStore.remove(m);
        }

        this.toStore.addAll(sel);
        this.toView.getSelectionModel().select(sel, false);
    }

    protected void onUp() {
        this.toView.moveSelectedUp();
    }

    protected class DualListFieldDefaultMessages implements DualListFieldTwo.DualListFieldMessages {
        protected DualListFieldDefaultMessages() {
        }

        public String addAll() {
            return DefaultMessages.getMessages().listField_addAll();
        }

        public String addSelected() {
            return DefaultMessages.getMessages().listField_addSelected();
        }

        public String moveDown() {
            return DefaultMessages.getMessages().listField_moveSelectedDown();
        }

        public String moveUp() {
            return DefaultMessages.getMessages().listField_moveSelectedUp();
        }

        public String removeAll() {
            return DefaultMessages.getMessages().listField_removeAll();
        }

        public String removeSelected() {
            return DefaultMessages.getMessages().listField_removeSelected();
        }
    }

    public static enum Mode {
        APPEND,
        INSERT;

        private Mode() {
        }
    }

    public interface DualListFieldMessages {
        String addAll();

        String addSelected();

        String moveDown();

        String moveUp();

        String removeAll();

        String removeSelected();
    }

    public interface DualListFieldAppearance {
        IconConfig allLeft();

        IconConfig allRight();

        IconConfig down();

        IconConfig left();

        IconConfig right();

        IconConfig up();
    }
}

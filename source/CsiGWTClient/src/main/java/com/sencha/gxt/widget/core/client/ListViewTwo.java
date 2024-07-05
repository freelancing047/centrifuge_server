package com.sencha.gxt.widget.core.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.GXTLogConfiguration;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.CompositeElement;
import com.sencha.gxt.core.client.dom.DomHelper;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.gestures.LongPressOrTapGestureRecognizer;
import com.sencha.gxt.core.client.gestures.PointerEventsSupport;
import com.sencha.gxt.core.client.gestures.ScrollGestureRecognizer;
import com.sencha.gxt.core.client.gestures.TouchData;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.Util;
import com.sencha.gxt.core.shared.event.GroupingHandlerRegistration;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import com.sencha.gxt.data.shared.event.StoreDataChangeEvent;
import com.sencha.gxt.data.shared.event.StoreFilterEvent;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreSortEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent;
import com.sencha.gxt.data.shared.loader.Loader;
import com.sencha.gxt.widget.core.client.cell.DefaultHandlerManagerContext;
import com.sencha.gxt.widget.core.client.event.RefreshEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.XEvent;
import com.sencha.gxt.widget.core.client.tips.QuickTip;

public class ListViewTwo<M, N> extends Component implements RefreshEvent.HasRefreshHandlers {
   private static final Logger LOG = Logger.getLogger(ListViewTwo.class.getName());

    private final ListView.ListViewAppearance<M> appearance;
    protected XElement focusEl;
    protected final FocusImpl focusImpl;
    protected int rowSelectorDepth;
    protected ListStore<M> store;
    protected final ValueProvider<? super M, N> valueProvider;
    private CompositeElement all;
    private Cell<N> cell;
    private boolean enableQuickTip;
    private HandlerRegistration storeHandlersRegistration;
    private GroupingHandlerRegistration loadHandlerRegistration;
    private SafeHtml loadingIndicator;
    private XElement overElement;
    private QuickTip quickTip;
    private boolean selectOnHover;
    private ListViewSelectionModelTwo<M> sm;
    private StoreHandlers<M> storeHandlers;
    private boolean trackMouseOver;

    public ListViewTwo(ListStore<M> store, ValueProvider<? super M, N> valueProvider) {
        this(store, valueProvider, (ListView.ListViewAppearance) GWT.create(ListView.ListViewAppearance.class));
    }

    public ListViewTwo(ListStore<M> store, ValueProvider<? super M, N> valueProvider, Cell<N> cell) {
        this(store, valueProvider, (ListView.ListViewAppearance)GWT.create(ListView.ListViewAppearance.class));
        this.setCell(cell);
    }

    public ListViewTwo(final ListStore<M> store, ValueProvider<? super M, N> valueProvider, ListView.ListViewAppearance<M> appearance) {
        this.focusImpl = FocusImpl.getFocusImplForPanel();
        this.rowSelectorDepth = 5;
        this.enableQuickTip = true;
        this.loadingIndicator = SafeHtmlUtils.EMPTY_SAFE_HTML;
        this.trackMouseOver = GXT.isMSEdge() || !GXT.isTouch();
        this.appearance = appearance;
        this.valueProvider = valueProvider;
        this.setSelectionModel(new ListViewSelectionModelTwo());
        SafeHtmlBuilder markupBuilder = new SafeHtmlBuilder();
        appearance.render(markupBuilder);
        this.setElement(XDOM.create(markupBuilder.toSafeHtml()));
        this.all = new CompositeElement();
        this.setAllowTextSelection(false);
        this.storeHandlers = new StoreHandlers<M>() {
            public void onAdd(StoreAddEvent<M> event) {
                ListViewTwo.this.onAdd(event.getItems(), event.getIndex());
            }

            public void onClear(StoreClearEvent<M> event) {
                ListViewTwo.this.refresh();
            }

            public void onDataChange(StoreDataChangeEvent<M> event) {
                ListViewTwo.this.refresh();
            }

            public void onFilter(StoreFilterEvent<M> event) {
                ListViewTwo.this.refresh();
            }

            public void onRecordChange(StoreRecordChangeEvent<M> event) {
                if (ListViewTwo.this.valueProvider == event.getProperty()) {
                    ListViewTwo.this.onUpdate(event.getRecord().getModel(), store.indexOf(event.getRecord().getModel()));
                }

            }

            public void onRemove(StoreRemoveEvent<M> event) {
                ListViewTwo.this.onRemove(event.getItem(), event.getIndex());
            }

            public void onSort(StoreSortEvent<M> event) {
                ListViewTwo.this.refresh();
            }

            public void onUpdate(StoreUpdateEvent<M> event) {
                List<M> items = event.getItems();

                for(int i = 0; i < items.size(); ++i) {
                    M item = items.get(i);
                    ListViewTwo.this.onUpdate(item, store.indexOf(item));
                }

            }
        };
        this.setStore(store);
        this.ensureFocusElement();
        this.sinkEvents(255);
        this.addGestureRecognizer(new LongPressOrTapGestureRecognizer() {
            protected void onLongPress(TouchData touch) {
                ListViewTwo.this.onLongPress(touch);
                super.onLongPress(touch);
            }

            protected void onTap(TouchData touch) {
                ListViewTwo.this.onTap(touch);
                super.onTap(touch);
            }

            protected void handlePreventDefault(NativeEvent event) {
                if (ListViewTwo.this.sm == null || !ListViewTwo.this.sm.isInput((Element)event.getEventTarget().cast())) {
                    event.preventDefault();
                }
            }
        });
        this.addGestureRecognizer(new ScrollGestureRecognizer(this.getElement(), ScrollGestureRecognizer.ScrollDirection.BOTH));
    }

    public HandlerRegistration addRefreshHandler(RefreshEvent.RefreshHandler handler) {
        return this.addHandler(handler, RefreshEvent.getType());
    }

    public Element findElement(Element element) {
        return this.appearance.findElement((XElement)element.cast());
    }

    public int findElementIndex(Element element) {
        Element elem = this.findElement(element);
        return elem != null ? this.indexOf(elem) : -1;
    }

    public void focus() {
        if (GXT.isGecko()) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                public void execute() {
                    if (ListViewTwo.this.focusEl != null) {
                        ListViewTwo.this.focusImpl.focus(ListViewTwo.this.focusEl);
                    }

                }
            });
        } else if (this.focusEl != null) {
            this.focusImpl.focus(this.focusEl);
        }

    }

    public ListView.ListViewAppearance<M> getAppearance() {
        return this.appearance;
    }

    public Cell<N> getCell() {
        return this.cell;
    }

    public XElement getElement(int index) {
        return (XElement)this.all.getElement(index).cast();
    }

    public List<Element> getElements() {
        return this.all.getElements();
    }

    public int getItemCount() {
        return this.store == null ? 0 : this.store.size();
    }

    public SafeHtml getLoadingIndicator() {
        return this.loadingIndicator;
    }

    public QuickTip getQuickTip() {
        return this.quickTip;
    }

    public ListViewSelectionModelTwo<M> getSelectionModel() {
        return this.sm;
    }

    public boolean getSelectOnOver() {
        return this.selectOnHover;
    }

    public ListStore<M> getStore() {
        return this.store;
    }

    public int indexOf(Element element) {
        if (element == null) {
            return -1;
        } else {
            return element.getPropertyString("viewIndex") != null ? Util.parseInt(element.getPropertyString("viewIndex"), -1) : this.all.indexOf(element);
        }
    }

    public boolean isEnableQuickTips() {
        return this.enableQuickTip;
    }

    public boolean isTrackMouseOver() {
        return this.trackMouseOver;
    }

    public void moveSelectedDown() {
        List<M> sel = this.getSelectionModel().getSelectedItems();
        Collections.sort(sel, new Comparator<M>() {
            public int compare(M o1, M o2) {
                return ListViewTwo.this.store.indexOf(o1) < ListViewTwo.this.store.indexOf(o2) ? 1 : 0;
            }
        });
        Iterator i$ = sel.iterator();

        while(i$.hasNext()) {
            M m = (M) i$.next();
            int idx = this.store.indexOf(m);
            if (idx < this.store.size() - 1) {
                this.store.remove(m);
                this.store.add(idx + 1, m);
            }
        }

        this.getSelectionModel().select(sel, false);
    }

    public void moveSelectedUp() {
        List<M> sel = this.getSelectionModel().getSelectedItems();
        Collections.sort(sel, new Comparator<M>() {
            public int compare(M o1, M o2) {
                return ListViewTwo.this.store.indexOf(o1) > ListViewTwo.this.store.indexOf(o2) ? 1 : 0;
            }
        });
        Iterator i$ = sel.iterator();

        while(i$.hasNext()) {
            M m = (M) i$.next();
            int idx = this.store.indexOf(m);
            if (idx > 0) {
                this.store.remove(m);
                this.store.add(idx - 1, m);
            }
        }

        this.getSelectionModel().select(sel, false);
    }

    public void onBrowserEvent(Event event) {
        if (this.isEnabled()) {
            if (this.cell != null) {
                CellWidgetImplHelper.onBrowserEvent(this, event);
            }

            this.handleEventForCell(event);
            super.onBrowserEvent(event);
            switch(event.getTypeInt()) {
                case 4:
                    if (!PointerEventsSupport.impl.isSupported()) {
                        this.onMouseDown(event);
                    }
                    break;
                case 16:
                    this.onMouseOver(event);
                    break;
                case 32:
                    this.onMouseOut(event);
            }

        }
    }

    public void refresh() {
        if (this.isOrWasAttached()) {
            this.getElement().setInnerSafeHtml(SafeHtmlUtils.EMPTY_SAFE_HTML);
            this.getElement().repaint();
            List<M> models = this.store == null ? new ArrayList() : this.store.getAll();
            this.all.removeAll();
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            this.bufferRender((List)models, sb);
            SafeHtml markup = sb.toSafeHtml();
            this.getElement().setInnerSafeHtml(markup);
            SafeHtmlBuilder esb = new SafeHtmlBuilder();
            this.appearance.renderEnd(esb);
            DomHelper.insertHtml("beforeend", this.getElement(), esb.toSafeHtml());
            List<Element> elems = this.appearance.findElements(this.getElement());
            this.all = new CompositeElement(elems);
            this.updateIndexes(0, -1);
            this.ensureFocusElement();
            this.fireEvent(new RefreshEvent());
        }
    }

    public void refreshNode(int index) {
        this.onUpdate(this.store.get(index), index);
    }

    public void setAllowTextSelection(boolean enable) {
        this.allowTextSelection = enable;
        this.getElement().setClassName(CommonStyles.get().unselectableSingle(), !enable);
    }

    public void setCell(Cell<N> cell) {
        this.cell = cell;
        if (cell != null) {
            CellWidgetImplHelper.sinkEvents(this, cell.getConsumedEvents());
        }

    }

    public void setEnableQuickTips(boolean enableQuickTip) {
        this.assertPreRender();
        this.enableQuickTip = enableQuickTip;
    }

    public void setLoader(Loader<?, ?> loader) {
        if (this.loadHandlerRegistration != null) {
            this.loadHandlerRegistration.removeHandler();
            this.loadHandlerRegistration = null;
        }

        if (loader != null) {
            if (this.loadHandlerRegistration == null) {
                this.loadHandlerRegistration = new GroupingHandlerRegistration();
            }

            this.loadHandlerRegistration.add(loader.addBeforeLoadHandler(new BeforeLoadEvent.BeforeLoadHandler() {
                public void onBeforeLoad(BeforeLoadEvent event) {
                    ListViewTwo.this.onBeforeLoad();
                }
            }));
            this.loadHandlerRegistration.add(loader.addLoadExceptionHandler(new LoadExceptionEvent.LoadExceptionHandler() {
                public void onLoadException(LoadExceptionEvent event) {
                    ListViewTwo.this.refresh();
                    ListViewTwo.this.onLoadError(event);
                }
            }));
        }

    }

    public void setLoadingIndicator(SafeHtml html) {
        this.loadingIndicator = html;
    }

    public void setLoadingIndicator(String text) {
        this.loadingIndicator = SafeHtmlUtils.fromString(text);
    }

    public void setSelectionModel(ListViewSelectionModelTwo<M> sm) {
        if (this.sm != null) {
            this.sm.bindList((ListViewTwo)null);
        }

        this.sm = sm;
        if (sm != null) {
            sm.bindList(this);
        }

    }

    public void setSelectOnOver(boolean selectOnHover) {
        this.selectOnHover = selectOnHover;
    }

    public void setStore(ListStore<M> store) {
        if (this.store != null) {
            this.storeHandlersRegistration.removeHandler();
        }

        if (store != null) {
            this.storeHandlersRegistration = store.addStoreHandlers(this.storeHandlers);
        }

        this.store = store;
        this.sm.bindList(this);
        if (store != null) {
            this.refresh();
        }

    }

    public void setTrackMouseOver(boolean trackMouseOver) {
        this.trackMouseOver = trackMouseOver;
    }

    protected void bufferRender(List<M> models, SafeHtmlBuilder sb) {
        int i = 0;

        for(int len = models.size(); i < len; ++i) {
            M m = models.get(i);
            SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
            N v = this.getValue(m);
            if (this.cell == null) {
                String text = null;
                if (v != null) {
                    text = v.toString();
                }

                cellBuilder.append(Util.isEmptyString(text) ? Util.NBSP_SAFE_HTML : SafeHtmlUtils.fromString(text));
            } else {
                Cell.Context context = new Cell.Context(i, 0, this.store.getKeyProvider().getKey(m));
                this.cell.render(context, v, cellBuilder);
            }

            this.appearance.renderItem(sb, cellBuilder.toSafeHtml());
        }

    }

    protected boolean cellConsumesEventType(Cell<N> cell, String eventType) {
        Set<String> consumedEvents = cell.getConsumedEvents();
        return consumedEvents != null && consumedEvents.contains(eventType);
    }

    public void focusItem(int index) {
        XElement elem = this.getElement(index);
        if (elem != null) {
            elem.scrollIntoView(this.getElement(), false);
            this.focusEl.setXY(elem.getXY());
        }

        this.focus();
    }

    protected N getValue(M m) {
        Object value;
        if (this.store.hasRecord(m)) {
            value = this.store.getRecord(m).getValue(this.valueProvider);
        } else {
            value = this.valueProvider.getValue(m);
        }

        return (N) value;
    }

    protected void handleEventForCell(Event event) {
        EventTarget eventTarget = event.getEventTarget();
        if (this.cell != null && Element.is(eventTarget)) {
            XElement target = (XElement)event.getEventTarget().cast();
            int rowIndex = this.findElementIndex(this.appearance.findElement(target));
            final M m = this.getStore().get(rowIndex);
            Element cellParent = this.appearance.findCellParent(this.getElement(rowIndex));
            if (m != null && cellParent != null) {
                DefaultHandlerManagerContext context = new DefaultHandlerManagerContext(rowIndex, 0, this.getStore().getKeyProvider().getKey(m), ComponentHelper.ensureHandlers(this));
                if (this.cellConsumesEventType(this.cell, event.getType())) {
                    this.cell.onBrowserEvent(context, cellParent, this.getValue(m), event, new ValueUpdater<N>() {
                        public void update(N value) {
                            ListViewTwo.this.getStore().getRecord(m).addChange(ListViewTwo.this.valueProvider, value);
                        }
                    });
                }
            }

        }
    }

    protected void onAdd(List<M> models, int index) {
        if (this.isOrWasAttached()) {
            boolean empty = this.all.getCount() == 0;
            if (empty && models.size() == this.store.size()) {
                this.refresh();
            } else {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                this.bufferRender(models, sb);
                Element d = Document.get().createDivElement();
                d.setInnerSafeHtml(sb.toSafeHtml());
                List<Element> list = this.appearance.findElements((XElement)d.cast());
                Element ref = index == 0 ? null : this.all.getElement(index - 1);
                Element n = ref == null ? null : ref.getParentElement();

                for(int i = list.size() - 1; i >= 0; --i) {
                    Element e = (Element)list.get(i);
                    if (index == 0) {
                        this.getElement().insertFirst(e);
                    } else {
                        Node next = ref == null ? null : ref.getNextSibling();
                        if (next == null) {
                            n.appendChild(e);
                        } else {
                            n.insertBefore(e, next);
                        }
                    }
                }

                this.all.insert(Util.toElementArray(list), index);
                this.updateIndexes(index, -1);
            }
        }
    }

    protected void onAfterFirstAttach() {
        super.onAfterFirstAttach();
        if (this.enableQuickTip) {
            this.quickTip = new QuickTip(this);
        }

        this.refresh();
    }

    protected void onBeforeLoad() {
        if (this.loadingIndicator != SafeHtmlUtils.EMPTY_SAFE_HTML) {
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant("<div class='loading-indicator'>");
            sb.append(this.loadingIndicator);
            sb.appendHtmlConstant("</div>");
            this.getElement().setInnerSafeHtml(sb.toSafeHtml());
            this.all.removeAll();
        }

    }

    protected void onLoadError(LoadExceptionEvent<?> event) {
        if (GXTLogConfiguration.loggingIsEnabled()) {
            LOG.severe("The ListView had a load exception");
        }

    }

    protected void onFocus(Event event) {
        super.onFocus(event);
    }

    public void onHighlightRow(int index, boolean highLight) {
        XElement e = this.getElement(index);
        if (e != null) {
            e.setClassName("x-view-highlightrow", highLight);
        }

    }

    protected void onLongPress(TouchData touchData) {
        this.onTap(touchData);
    }

    protected void onMouseDown(Event e) {
        if (justSelected == false) {
            int index = this.indexOf((Element) e.getEventTarget().cast());
            if (index != -1) {
                this.fireEvent(new SelectEvent());
            }
        } else {
            getSelectionModel().doNotClear();
            justSelected = false;
        }
    }

    protected void onMouseOut(Event ce) {
        if (this.overElement != null && !((XEvent)ce.cast()).within(this.overElement, true)) {
            this.appearance.onOver(this.overElement, false);
            this.overElement = null;
        }

    }

    protected void onMouseOver(Event ce) {
        if (this.selectOnHover || this.trackMouseOver) {
            Element target = (Element)ce.getEventTarget().cast();
            target = this.findElement(target);
            if (target != null) {
                int index = this.indexOf(target);
                if (index != -1) {
                    if (this.selectOnHover) {
                        this.sm.select(index, false);
                    } else if (target != this.overElement) {
                        this.appearance.onOver((XElement)target.cast(), true);
                        this.overElement = (XElement)target.cast();
                    }
                }
            }
        }

    }

    protected void onRemove(M data, int index) {
        if (this.all != null) {
            Element e = this.getElement(index);
            if (e != null) {
                this.appearance.onOver((XElement)e.cast(), false);
                if (this.overElement == e) {
                    this.overElement = null;
                }

                this.getSelectionModel().deselect(index);
                e.removeFromParent();
                this.all.remove(index);
                this.updateIndexes(index, -1);
            }
        }

    }

    protected void onResize(int width, int height) {
        super.onResize(width, height);
        this.constrainFocusElement();
    }

    boolean justSelected = false;
    public void onSelectChange(M model, boolean select) {
        if (this.all != null) {
            int index = this.store.indexOf(model);
            if (index != -1 && index < this.all.getCount()) {
                XElement e = this.getElement(index);
                if (e != null) {
                    this.appearance.onSelect(e, select);
                    this.appearance.onOver((XElement)e.cast(), false);
                }
            }
        }
        justSelected = true;
    }

    protected void onTap(TouchData touch) {
        Event event = (Event)touch.getLastNativeEvent().cast();
        this.onMouseOut(event);
        if (this.sm != null && !this.sm.isLocked() && !this.sm.isInput((Element)event.getEventTarget().cast())) {
            this.getSelectionModel().onMouseDown(event);
            this.getSelectionModel().onMouseClick(event);
            this.onMouseOver(event);
            this.onMouseDown(event);
        }
    }

    protected void onUpdate(M model, int index) {
        Element original = this.all.getElement(index);
        if (original != null) {
            List<M> list = Collections.singletonList(model);
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            this.bufferRender(list, sb);
            DomHelper.insertBefore(original, sb.toSafeHtml());
            this.all.replaceElement(original, Element.as(original.getPreviousSibling()));
            original.removeFromParent();
        }

        this.sm.onRowUpdated(model);
    }

    protected void updateIndexes(int startIndex, int endIndex) {
        List<Element> elems = this.all.getElements();
        endIndex = endIndex == -1 ? elems.size() - 1 : endIndex;

        for(int i = startIndex; i <= endIndex; ++i) {
            ((Element)elems.get(i)).setPropertyInt("viewIndex", i);
        }

    }

    private void constrainFocusElement() {
        int scrollLeft = this.getElement().getScrollLeft();
        int scrollTop = this.getElement().getScrollTop();
        int left = this.getElement().getWidth(true) / 2 + scrollLeft;
        int top = this.getElement().getHeight(true) / 2 + scrollTop;
        this.focusEl.setLeftTop(left, top);
    }

    private void ensureFocusElement() {
        if (this.focusEl != null) {
            this.focusEl.removeFromParent();
        }

        this.focusEl = (XElement)this.getElement().appendChild(this.focusImpl.createFocusable());
        this.focusEl.addClassName(CommonStyles.get().noFocusOutline());
        if (this.focusEl.hasChildNodes()) {
            this.focusEl.getFirstChildElement().addClassName(CommonStyles.get().noFocusOutline());
            Style focusElStyle = this.focusEl.getFirstChildElement().getStyle();
            focusElStyle.setBorderWidth(0.0D, Style.Unit.PX);
            focusElStyle.setFontSize(1.0D, Style.Unit.PX);
            focusElStyle.setPropertyPx("lineHeight", 1);
        }

        this.focusEl.setLeft(0);
        this.focusEl.setTop(0);
        this.focusEl.makePositionable(true);
        this.focusEl.addEventsSunk(6144);
    }

    public interface ListViewAppearance<M> {
        Element findCellParent(XElement var1);

        Element findElement(XElement var1);

        List<Element> findElements(XElement var1);

        void onOver(XElement var1, boolean var2);

        void onSelect(XElement var1, boolean var2);

        void render(SafeHtmlBuilder var1);

        void renderEnd(SafeHtmlBuilder var1);

        void renderItem(SafeHtmlBuilder var1, SafeHtml var2);
    }
}

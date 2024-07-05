package csi.client.gwt.viz.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.event.BodyScrollEvent;
import com.sencha.gxt.widget.core.client.event.BodyScrollEvent.BodyScrollHandler;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.shared.BroadcastAlert;
import csi.client.gwt.viz.table.CellHoverEvent.CellHoverHandler;
import csi.client.gwt.viz.table.grid.TableVizGrid;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.server.common.dto.CustomPagingResultBean;
import csi.server.common.dto.TableDataHeader;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.table.ColumnState;
import csi.server.common.model.visualization.table.TableCachedState;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.common.service.api.TableActionsServiceProtocol;
import csi.shared.core.util.IntCollection;

@SuppressWarnings("rawtypes")
public class TableView extends ResizeComposite {
    private static final int UPDATE_DELAY = 100;
    private static final int SCROLL_DELAY = 250;
    //Needed for minimum/initial
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static TableViewUiBinder uiBinder = GWT.create(TableViewUiBinder.class);
    private static final String DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
    @UiField(provided = true)
    GridContainer gridContainer;
    private boolean isNoData = false;
    private TablePresenter presenter;
    private RemoteLoadingGridComponentManager<List<?>> gridComponentManager;
    private TableVizGrid<List<?>> tableGrid;
    private int lastScrollPosition;
    private int lastScrollLeft = 0;
    private CustomScrollHandler bodyScrollHandler;
    private int lastHeight = 0;
    private int totalHeight = 1;
    private boolean loadComplete = true;
    private com.github.gwtbootstrap.client.ui.Icon spinnerIcon = new com.github.gwtbootstrap.client.ui.Icon(IconType.SPINNER);
    private SearchCellHighlightEvent lastHighlight;
    private Element lastHighlightedCell;
    private ScrollingToolBar toolBar;
    private List<FieldDef> visibleFields;
    private Map<String, FieldDef> columnFields;
    private ArrayList<TableValueProvider> valueProviderList;
    private TableIntCheckboxSelectionModel selectionModel;
    private CancellableCommand scrollCommand;
    private boolean init = false;
    private LoadExceptionEvent.LoadExceptionHandler handleLoadException = new LoadExceptionEvent.LoadExceptionHandler() {
        @Override
        public void onLoadException(LoadExceptionEvent eventIn) {
            presenter.displayLoadingError(eventIn.getException());
        }
    };
    private CancelRepeatingCommand command;
    private boolean doSearch = false;
    private String searchText;
    private CopyCellHighlightEvent lastCopyEvent;

    TableView(TablePresenter presenter) {
        gridContainer = new GridContainer();
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
        initialize(0);
        attachSpinner();
        initSpinner();
        Scheduler.get().scheduleDeferred(() -> {
            positionSpinner();
            showLoadingSpinner();
        });
    }

    private static void forceScrollTopIe() {
      /*-{
        element.scrollTop = scrollTop;
      }-*/
    }

    void displayNoData() {
        String message = "No Results Found";
        isNoData = true;
        presenter.getChrome().addFullScreenWindow(message, IconType.INFO_SIGN);
        if (gridContainer != null) {
            gridContainer.setVisible(false);
        }
    }

    @SuppressWarnings("unchecked")
    public void initialize(long totalRecords) {
        lastScrollPosition = 0;
        lastScrollLeft = 0;
        initFields();
        initGridComponentManager();
        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> myLoader = gridComponentManager.getLoader();
        myLoader.addLoadExceptionHandler(handleLoadException);
        this.selectionModel = createSelectionModel(myLoader);
        List<ColumnConfig<List<?>, ?>> colList = initColumns();
        initTableGrid(colList);
        initSelectionInfo();
        initToolBar();
        gridContainer.setGrid(tableGrid);
        bodyScrollHandler = new CustomScrollHandler() {
            private boolean disabled = false;

            public void disable() {
                disabled = true;
            }

            @Override
            public void onBodyScroll(BodyScrollEvent event) {
                if (disabled) {
                    disabled = false;
                    return;
                }
                XElement scroller = tableGrid.getView().getScroller();
                if (lastScrollPosition == scroller.getScrollTop()) {
                    return;
                }
                if (scrollCommand != null) {
                    scrollCommand.cancel();
                }
                showLoadingSpinner();
                int rowHeight = tableGrid.getRowHeight();
                int tableHeight = tableGrid.getStore().size() * rowHeight;
                int pageHeight = tableGrid.getView().getScroller().getClientHeight();
                totalHeight = (int) (totalRecords * rowHeight);
                int rowOffset = lastScrollPosition % rowHeight;
                int topMargin = lastScrollPosition - rowOffset - rowHeight;
                if (topMargin < 0) {
                    topMargin = 0;
                }
                int bottomMargin = totalHeight - (topMargin + tableHeight);
                toolBar.updateInfo(lastScrollPosition, totalHeight,
                        pageHeight, (int) totalRecords, rowHeight,
                        (bottomMargin == 0) && ((lastScrollPosition + pageHeight) >= (totalHeight - rowOffset)));
                scrollCommand = new CancellableCommand() {
                    @Override
                    public void command() {
                        updateAndLoad();
                    }
                };
                Scheduler.get().scheduleFixedDelay(scrollCommand, SCROLL_DELAY);
                lastScrollLeft = scroller.getScrollLeft();
                lastScrollPosition = scroller.getScrollTop();
            }
        };
        tableGrid.addBodyScrollHandler(bodyScrollHandler);
        tableGrid.addHandler(new CellHoverHandler() {
            private Element lastCopyCellHover = null;

            @Override
            public void onCellHover(CellHoverEvent event) {
                Element cell = tableGrid.getView().getCell(event.getRowIndex(), event.getCellIndex());
                if ((lastCopyCellHover != null) && (lastCopyCellHover != cell)) {
                    lastCopyCellHover.removeClassName("table-copy-hover");
                }
                if (TextMetrics.get().getWidth(cell.getInnerText()) > cell.getClientWidth()) {
                    String msg;
                    //force a line break in the tooltip if \n is in the data
                    msg = cell.getInnerText().replace("\n", "<br/>");
                    SafeHtmlBuilder b = new SafeHtmlBuilder();
                    b.appendHtmlConstant("<div style=\"overflow:auto; max-height: 200px; max-width: 300px; display:block\">" + msg + "</div>");
                    ToolTipConfig toolTipConfig = new ToolTipConfig();
                    toolTipConfig.setAutoHide(true);
                    toolTipConfig.setTitle("");
                    tableGrid.setToolTipConfig(toolTipConfig);
                    tableGrid.setToolTip(b.toSafeHtml());
                } else {
                    tableGrid.hideToolTip();
                }
                if (presenter.getMode() == TableMode.COPY) {
                    cell.addClassName("table-copy-hover");
                    lastCopyCellHover = cell;
                }
            }
        }, CellHoverEvent.getType());
        tableGrid.addHandler(event -> {
            clearLastHighlight();
            lastHighlight = event;
            lastHighlightedCell = tableGrid.getView().getCell(event.getRowIndex(), event.getCellIndex());
            lastHighlightedCell.addClassName("table-search-highlight");
            lastHighlightedCell.scrollIntoView();
        }, SearchCellHighlightEvent.getType());
        tableGrid.addHandler(event -> highlightCopyCells(event, false), CopyCellHighlightEvent.getType());
        tableGrid.addRefreshHandler(event -> {
            XElement scroller = tableGrid.getView().getScroller();
            int rowHeight = tableGrid.getRowHeight();
            int tableHeight = tableGrid.getStore().size() * rowHeight;
            int pageHeight = tableGrid.getView().getScroller().getClientHeight();
            //Means UI is not ready, return because resize will fire after
            if (tableGrid.getView() == null) {
                return;
            }
            //Means UI is there, but previous results aren't displayed
            if ((totalRecords > 0) && (rowHeight == 0) && verifyTableHeight()) {
                Scheduler.get().scheduleDeferred(this::updateAndLoad);
                return;
            }
            hideLoadingSpinner();
            //Means we have wrong stuff, update immediately
            if ((tableGrid.getStore().size() < totalRecords) && (tableHeight < pageHeight)) {
                updateAndLoad();
                return;
            }
            bodyScrollHandler.disable();
            totalHeight = (int) (totalRecords * rowHeight);
            int rowOffset = lastScrollPosition % rowHeight;
            int topMargin = lastScrollPosition - rowOffset - rowHeight;
            if (topMargin < 0) {
                topMargin = 0;
            }
            int bottomMargin = totalHeight - (topMargin + tableHeight);
            tableGrid.forceTop(topMargin);
            tableGrid.forceBottom(bottomMargin);
            toolBar.updateInfo(lastScrollPosition, totalHeight,
                    pageHeight, (int) totalRecords, rowHeight,
                    (bottomMargin == 0) || ((lastScrollPosition + pageHeight) >= (totalHeight - rowOffset)));
            scroller.setScrollLeft(lastScrollLeft);
            scroller.setScrollTop(lastScrollPosition);
            forceScrollTopIe();
        });
    }

    public void disable() {
        tableGrid.setSelectionModel(null);
    }

    public void enable() {
        if ((tableGrid.getSelectionModel() == null) && (selectionModel != null)) {
            tableGrid.setSelectionModel(selectionModel);
            IntCollection savedSelection = new IntCollection();
            savedSelection.addAll(selectionModel.getAllSelectedIds());
            selectionModel.setSelectionByIds(savedSelection);
        }
    }

    private void highlightCopyCells(CopyCellHighlightEvent event, boolean clear) {
        this.lastCopyEvent = event;
        if (event == null) {
            return;
        }
        int startRowIndex = event.getStartRowIndex();
        int startCellIndex = event.getStartCellIndex();
        int endRowIndex = event.getEndRowIndex();
        int endCellIndex = event.getEndCellIndex();
        if (startRowIndex > endRowIndex) {
            int temp = startRowIndex;
            startRowIndex = endRowIndex;
            endRowIndex = temp;
        }
        if (startCellIndex > endCellIndex) {
            int temp = startCellIndex;
            startCellIndex = endCellIndex;
            endCellIndex = temp;
        }
        int rowHeight = tableGrid.getRowHeight();
        int currentTopRow = lastScrollPosition / rowHeight;
        startRowIndex = startRowIndex - currentTopRow;
        endRowIndex = endRowIndex - currentTopRow;
        int currentRowIndex = startRowIndex;
        if (endRowIndex < 0) {
            return;
        }
        if (currentRowIndex < 0) {
            currentRowIndex = 0;
        }
        while (currentRowIndex <= endRowIndex) {
            Element row = tableGrid.getView().getRow(currentRowIndex);
            int currentIndex = startCellIndex;
            Element highlightCell;
            if ((row == null) || !row.hasChildNodes()) {
            } else if (!tableGrid.getView().isEnableRowBody()) {
                while (currentIndex <= endCellIndex) {
                    highlightCell = (Element) row.getChildNodes().getItem(currentIndex);
                    if (clear) {
                        removeCopyHighlight(highlightCell);
                    } else {
                        highlightCopyCell(highlightCell, currentIndex == startCellIndex, currentIndex == endCellIndex, currentRowIndex == startRowIndex, currentRowIndex == endRowIndex);
                    }
                    currentIndex++;
                }
            } else {
                Element cellContainer = row.getFirstChildElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().getNextSiblingElement().getFirstChildElement();
                while (currentIndex <= endCellIndex) {
                    highlightCell = (Element) cellContainer.getChild(currentIndex);
                    if (clear) {
                        removeCopyHighlight(highlightCell);
                    } else {
                        highlightCopyCell(highlightCell, currentIndex == startCellIndex, currentIndex == endCellIndex, currentRowIndex == startRowIndex, currentRowIndex == endRowIndex);
                    }
                    currentIndex++;
                }
            }
            currentRowIndex++;
        }
    }

    private void highlightCopyCell(Element highlightCell, boolean left, boolean right, boolean top, boolean bottom) {
        highlightCell.removeClassName("table-copy-highlight-full");
        if (left) {
            if (bottom) {
                if (right) {
                    if (top) {
                        highlightCell.addClassName("table-copy-highlight-full");
                    } else {
                        highlightCell.addClassName("table-copy-highlight-full");
                    }
                } else if (top) {
                    highlightCell.addClassName("table-copy-highlight-full");
                } else {
                    highlightCell.addClassName("table-copy-highlight-bottom-left");
                }
            } else if (top) {
                if (right) {
                    highlightCell.addClassName("table-copy-highlight-full");
                } else {
                    highlightCell.addClassName("table-copy-highlight-top-left");
                }
            }
            if (right) {
                highlightCell.addClassName("table-copy-highlight-full");
            } else {
                highlightCell.addClassName("table-copy-highlight-left");
            }
        } else if (right) {
            if (bottom) {
                if (top) {
                    highlightCell.addClassName("table-copy-highlight-full");
                } else {
                    highlightCell.addClassName("table-copy-highlight-bottom-right");
                }
            } else if (top) {
                highlightCell.addClassName("table-copy-highlight-top-right");
            } else {
                highlightCell.addClassName("table-copy-highlight-right");
            }
        } else if (top && bottom) {
            highlightCell.addClassName("table-copy-highlight-full");
        } else if (top) {
            highlightCell.addClassName("table-copy-highlight-top");
        } else if (bottom) {
            highlightCell.addClassName("table-copy-highlight-bottom");
        }
        highlightCell.addClassName("table-copy-background-highlight");
    }

    private void removeCopyHighlight(Element highlightCell) {
        highlightCell.removeClassName("table-copy-background-highlight");
        highlightCell.removeClassName("table-copy-highlight-full");
        highlightCell.removeClassName("table-copy-highlight-right-full");
        highlightCell.removeClassName("table-copy-highlight-left-full");
        highlightCell.removeClassName("table-copy-highlight-top-full");
        highlightCell.removeClassName("table-copy-highlight-bottom-full");
        highlightCell.removeClassName("table-copy-highlight-left");
        highlightCell.removeClassName("table-copy-highlight-right");
        highlightCell.removeClassName("table-copy-highlight-bottom");
        highlightCell.removeClassName("table-copy-highlight-top");
        highlightCell.removeClassName("table-copy-highlight-bottom-right");
        highlightCell.removeClassName("table-copy-highlight-top-right");
        highlightCell.removeClassName("table-copy-highlight-bottom-left");
        highlightCell.removeClassName("table-copy-highlight-top-left");
    }

    void scrollToIndex(int index) {
        XElement scroller = tableGrid.getView().getScroller();
        int rowHeight = tableGrid.getRowHeight();
        int tableHeight = tableGrid.getStore().size() * rowHeight;
        int scrollPosition = index * rowHeight;
        if (scrollPosition > (totalHeight - tableHeight)) {
            scrollPosition = totalHeight - tableHeight;
        }
        scroller.setScrollTop(scrollPosition);
        forceScrollTopIe();
    }

    private void triggerSearch() {
        this.doSearch = false;
        presenter.searchText(searchText);
    }

    void markForSearch(String text) {
        this.searchText = text;
        this.doSearch = true;
    }

    void unmarkForSearch() {
        this.searchText = null;
        this.doSearch = false;
    }

    private boolean verifyTableHeight() {
        return ((tableGrid != null) && (tableGrid.getView() != null) && (getOffsetHeight() > 1));
    }

    private void updateAndLoad() {
        if (loadComplete) {
            loadComplete = false;
            showLoadingSpinner();
            gridComponentManager.getLoader().load();
            scrollCommand = null;
        } else {
            if (command != null) {
                command.cancel();
            }
            command = new CancelRepeatingCommand() {
                @Override
                public boolean execute() {
                    if (loadComplete) {
                        updateAndLoad();
                        return false;
                    } else {
                        return !isCancel();
                    }
                }
            };
            Scheduler.get().scheduleFixedDelay(command, UPDATE_DELAY);
        }
    }

    public void setScrollTop(final int position) {
        command = new CancelRepeatingCommand() {
            @Override
            public boolean execute() {
                if (!loadComplete) {
                    setScrollTop(position);
                    return false;
                } else {
                    XElement scroller = tableGrid.getView().getScroller();
                    scroller.setScrollTop(position);
                    forceScrollTopIe();
                    return false;
                }
            }
        };
        Scheduler.get().scheduleFixedDelay(command, UPDATE_DELAY);
    }

    public Scroll getScroll() {
        return tableGrid.getView().getScroller().getScroll();
    }

    public void refresh() {
        if (toolBar == null) {
            return;
        }
        initToolBar();
        initSelectionInfo();
    }

    void saveSelectionsToModel() {
        if (selectionModel == null) {
            return;
        }
        TableViewDef def = presenter.getVisualizationDef();
        List<Integer> selections = new ArrayList<>();
        selections.addAll(selectionModel.getAllSelectedIds());
        IntPrimitiveSelection tableSelection = def.getSelection();
        tableSelection.makeSelectionStateForRows(selections);
        tableSelection.getSelectedItems().deDupe();
    }

    public void selectAll(int[] ids) {
        selectionModel.setSelectionByIds(ids);
    }

    void deSelectAll() {
        if (presenter.getMode() == TableMode.COPY) {
            presenter.endCopy();
            presenter.switchMode(TableMode.NORMAL);
        } else {
            selectionModel.deselectAll();
            selectionModel.setSelectionByIds(new ArrayList<String>());
        }
    }

    public List<String> getSelection() {
        return new ArrayList<>();
    }

    public boolean hasSelection() {
        return selectionModel.hasSelection();
    }

    @Override
    public void onResize() {
        super.onResize();
        if (isNoData) {
            return;
        }
        positionSpinner();
        if (lastHeight != getOffsetHeight()) {
            updateAndLoad();
        }
        lastHeight = getOffsetHeight();
        if (toolBar != null) {
            gridContainer.setPager(toolBar);
            toolBar.forceLayout();
            if (lastScrollPosition != 0) {
                setScrollTop(lastScrollPosition);
            }
        }
    }

    public void broadcastNotify(String text) {
        gridContainer.add(new BroadcastAlert(text));
    }

    private void initSelectionInfo() {
        IntPrimitiveSelection selInfo = presenter.getVisualizationDef().getSelection();
        HashSet<Integer> ids = new HashSet<>();
        if (!selInfo.isCleared()) {
            ids.addAll(selInfo.getSelectedItems());
        }
        selectionModel.setSelectionByIds(ids);
    }

    private List<ColumnConfig<List<?>, ?>> initColumns() {
        List<ColumnConfig<List<?>, ?>> colList = new ArrayList<>();
        int idx = 0;
        valueProviderList = Lists.newArrayList();
        // don't show SelectionModel in context menu in column header
        selectionModel.getColumn().setHideable(false);
        HashMap<Integer, ColumnConfig<List<?>, ?>> indexToColumnMap = new HashMap<>();
        for (FieldDef def : visibleFields) {
            ColumnState currentColumnState = null;
            int width = 100;
            TableCachedState state = presenter.getVisualizationDef().getState();
            if (state != null) {
                List<ColumnState> columnStates = state.getColumnStates();
                if (columnStates != null) {
                    for (ColumnState columnState : columnStates) {
                        if (def.getUuid().equals(columnState.getFieldDef().getUuid())) {
                            width = columnState.getWidth();
                            currentColumnState = columnState;
                            break;
                        }
                    }
                }
            }
            TableValueProvider<Object> valueProvider = new TableValueProvider<>(def);
            valueProviderList.add(valueProvider);
            ColumnConfig<List<?>, Object> col = new ColumnConfig<>(valueProvider, width, def.getFieldName());
            col.setResizable(true);
            col.setToolTip(def.getFieldName());
            if (CsiDataType.Integer.equals(def.getValueType()) || CsiDataType.Number.equals(def.getValueType())) {
                col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            }
            if (currentColumnState == null) {
                colList.add(idx++, col);
            } else {
                indexToColumnMap.put(currentColumnState.getIndex(), col);
            }
        }
        List<Integer> keys = new ArrayList<>(indexToColumnMap.keySet());
        Collections.sort(keys);
        List<ColumnConfig<List<?>, ?>> sortedList = new ArrayList<>();
        sortedList.add(0, selectionModel.getColumn());
        for (Integer key : keys) {
            sortedList.add(indexToColumnMap.get(key));
        }
        sortedList.addAll(colList);
        return sortedList;
    }

    private TableIntCheckboxSelectionModel createSelectionModel(PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> loader) {
        IdentityValueProvider<List<?>> identity = new IdentityValueProvider<>();
        return new TableIntCheckboxSelectionModel(identity, loader, presenter);
    }

    private void initFields() {
        List<VisibleTableField> visibleTableFields = presenter.getVisualizationDef().getTableViewSettings().getVisibleFields();
        visibleFields = new ArrayList<>();
        int idx = 0;
        for (VisibleTableField visibleTableField : visibleTableFields) {
            visibleFields.add(idx++, visibleTableField.getFieldDef(presenter.getDataModel()));
        }
        columnFields = new HashMap<>();
        for (FieldDef def : visibleFields) {
            columnFields.put(def.getFieldName(), def);
        }
    }

    private void initTableGrid(List<ColumnConfig<List<?>, ?>> colList) {
        ListStore<List<?>> rowStore = gridComponentManager.getStore();
        ColumnModel<List<?>> cm = new ColumnModel<>(colList);
        tableGrid = new TableVizGrid<>(rowStore, cm, this);
        tableGrid.setSelectionModel(selectionModel);
        initTableGridLoader();
        setTableGridSettings();
    }

    private void setTableGridSettings() {
        tableGrid.setLazyRowRender(0);
        tableGrid.setId("table-grid");
        tableGrid.setBorders(false);
        tableGrid.setColumnReordering(true);
        tableGrid.setColumnResize(true);
        tableGrid.setLoadMask(false);
        //view
        tableGrid.getView().setStripeRows(true);
        tableGrid.getView().setColumnLines(true);
        tableGrid.getView().setAdjustForHScroll(true);
        tableGrid.getView().setTrackMouseOver(true);
    }

    private void initTableGridLoader() {
        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> loader = gridComponentManager.getLoader();
        tableGrid.setSortLoader(loader);
    }

    private void initToolBar() {
        if (toolBar == null) {
            toolBar = gridComponentManager.getScrollingToolbar(DEFAULT_PAGE_SIZE);
            toolBar.addPageHandler(new PageEventHandler() {
                @Override
                public void onPage(PageEvent event) {
                    int page = event.getActivePage();
                    if (tableGrid.getView() == null) {
                        return;
                    }
                    int pageHeight = tableGrid.getView().getScroller().getClientHeight();
                    int updatedScrollPosition = (page * pageHeight) - pageHeight;
                    int maxScrollPosition = totalHeight - pageHeight;
                    updatedScrollPosition = Math.min(updatedScrollPosition, maxScrollPosition);
                    bodyScrollHandler.disable();
                    setScrollTop(updatedScrollPosition);
                }
            }, PageEvent.type);
        }
        toolBar.setActivePage(1);
        gridContainer.setPager(toolBar);
    }

    private void initGridComponentManager() {
        GridComponentFactory componentFactory = WebMain.injector.getGridFactory();
        gridComponentManager = componentFactory.createRemoteLoading(new TableModelKeyProvider(),
                TableActionsServiceProtocol.class,
                (vortexService, loadConfig) -> {
                    try {
                        return vortexService.gwtGetTableDataList(presenter.getDataViewUuid(), presenter.getUuid(), loadConfig);
                    } catch (Exception myException) {
                        presenter.displayLoadingError(myException);
                    }
                    return null;
                }
        );
        gridComponentManager.getLoader().addBeforeLoadHandler(event -> {
            int limit = 1;
            int offset = 0;
            if ((tableGrid.getView().getScroller() != null) && (tableGrid.getRowHeight() > 0)) {
                XElement scroller = tableGrid.getView().getScroller();
                if ((presenter.getPreviousState() == null) && (presenter.getVisualizationDef().getState() != null)) {
                    limit = (scroller.getOffsetHeight() / tableGrid.getRowHeight()) + 2;
                    gridComponentManager.getLoader().setLimit(limit);
                    TableCachedState state = presenter.getVisualizationDef().getState();
                    lastScrollPosition = state.getVerticalScrollPosition();
                    lastScrollLeft = (state.getHorizontalScrollPosition());
                    offset = calculateCurrentOffset();
                    presenter.setPreviousState(state);
                } else {
                    limit = (tableGrid.getView().getScroller().getOffsetHeight() / tableGrid.getRowHeight()) + 2;
                    gridComponentManager.getLoader().setLimit(limit);
                    offset = calculateCurrentOffset();
                }
            }
            if (offset < 0) {
                offset = 0;
            }
            showLoadingSpinner();
            event.getLoadConfig().setOffset(offset);
            event.getLoadConfig().setLimit(limit);
        });
        gridComponentManager.getLoader().addLoadHandler(event -> {
            if (event.getLoadResult() != null) {
                CustomPagingResultBean<List<?>> result = (CustomPagingResultBean<List<?>>) event.getLoadResult();
                List<TableDataHeader> headers = result.getHeaders();
                int position = 0;
                for (TableDataHeader header : headers) {
                    for (TableValueProvider provider : valueProviderList) {
                        if (provider.getFieldDefUuid().equals(header.getColId())) {
                            provider.setPosition(position);
                            break;
                        }
                    }
                    position++;
                }
            }
            lastHighlightedCell = null;
            if (doSearch) {
                triggerSearch();
            } else {
                if (lastHighlight != null) {
                    List<List<?>> clientData = getGridComponentManager().getStore().getAll();
                    for (int ii = 0; ii < clientData.size(); ii++) {
                        List<?> row = clientData.get(ii);
                        Object o = row.get(0);
                        int value = (int) o;
                        if (value == lastHighlight.getRowId()) {
                            tableGrid.fireEvent(new SearchCellHighlightEvent(ii, lastHighlight.getCellIndex(), value));
                            break;
                        }
                    }
                }
                if (lastCopyEvent != null) {
                    highlightCopyCells(lastCopyEvent, false);
                }
            }
            loadComplete = true;
        });
    }

    void forceGridVisible() {
        presenter.getChrome().removeFullScreenWindow();
        isNoData = false;
        if (gridContainer != null) {
            gridContainer.setVisible(true);
            if (toolBar != null) {
                toolBar.onResize();
            }
        }
    }

    TableVizGrid getTableGrid() {
        return tableGrid;
    }

    Map<String, FieldDef> getColumnFields() {
        return columnFields;
    }

    void showLoadingSpinner() {
        if (!isNoData) {
            spinnerIcon.setVisible(true);
        }
    }

    void hideLoadingSpinner() {
        if (!init) {
            return;
        }
        spinnerIcon.setVisible(false);
    }

    private void positionSpinner() {
        if (spinnerIcon == null) {
            return;
        }
        int height = spinnerIcon.getOffsetHeight();
        int width = spinnerIcon.getOffsetWidth();
        Style style = spinnerIcon.getElement().getStyle();
        style.setMarginLeft((gridContainer.getOffsetWidth() / 2) - width, Unit.PX);
        style.setMarginTop((gridContainer.getOffsetHeight() / 2) - height - 29, Unit.PX);
        style.setPosition(Position.ABSOLUTE);
    }

    private void initSpinner() {
        Scheduler.get().scheduleFixedDelay(this::attachSpinner, 50);
    }

    private boolean attachSpinner() {
        spinnerIcon.setIconSize(IconSize.FOUR_TIMES);
        spinnerIcon.setSpin(true);
        spinnerIcon.addStyleName("csi-icon-spinner"); //$NON-NLS-1$
        if (gridContainer != null) {
            gridContainer.getParent().getElement().appendChild(spinnerIcon.getElement());
            positionSpinner();
            spinnerIcon.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
            return false;
        }
        return true;
    }

    public void trackHorizontalPosition() {
        XElement scroller = tableGrid.getView().getScroller();
        lastScrollLeft = scroller.getScrollLeft();
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    List<TableValueProvider> getValueProviders() {
        return valueProviderList;
    }

    RemoteLoadingGridComponentManager<List<?>> getGridComponentManager() {
        // TODO Auto-generated method stub
        return gridComponentManager;
    }

    Element getLastHighlight() {
        // TODO Auto-generated method stub
        return lastHighlightedCell;
    }

    private void clearLastHighlight() {
        if (lastHighlightedCell != null) {
            lastHighlightedCell.removeClassName("table-search-highlight");
            lastHighlightedCell = null;
            lastHighlight = null;
        }
    }

    void clearHighlights() {
        clearLastHighlight();
        clearCopyHighlight();
    }

    private void clearCopyHighlight() {
        highlightCopyCells(lastCopyEvent, true);
        lastCopyEvent = null;
    }

    public HandlerRegistration addCellClickHandler(CellClickHandler cellClickHandler) {
        // TODO Auto-generated method stub
        return getTableGrid().addCellClickHandler(cellClickHandler);
    }

    private int calculateCurrentOffset() {
        int offset = lastScrollPosition / tableGrid.getRowHeight();
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

    int calculateGridOffset() {
        int offset = lastScrollPosition / tableGrid.getRowHeight();
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

    interface TableViewUiBinder extends UiBinder<Widget, TableView> {
    }

    private interface CustomScrollHandler extends BodyScrollHandler {
        void disable();
    }

    public abstract class CancellableCommand implements RepeatingCommand {
        private boolean doCancel = false;

        @Override
        public boolean execute() {
            if (doCancel) {
                return false;
            } else {
                command();
                return false;
            }
        }

        public abstract void command();

        public void cancel() {
            doCancel = true;
        }
    }

    public static String getDateTimeFormatString(String s, String dateTimeFormat) {
        Date date = null;
        if (s != null) {
            try {
                if (s.contains(".")) {
                    s = s.substring(0, s.indexOf("."));
                }
                date = new Date(s);
            } catch (Exception ignored) {
            }
        }
        if (date != null) {
            return DateTimeFormat.getFormat(dateTimeFormat).format(date);
        } else {
            return s;
        }
    }

    static class TableValueProvider<T> implements ValueProvider<List<?>, T> {
        private Function resolveType;
        private int position = -1;
        private String defUuid;

        TableValueProvider(FieldDef def) {
            defUuid = def.getUuid();
            CsiDataType valueType = def.getValueType();
            if (CsiDataType.DateTime.equals(valueType)) {
                resolveType = ((Function<String, String>) s -> getDateTimeFormatString(s, DATE_TIME_FORMAT));
            }
            if (CsiDataType.Integer.equals(valueType)) {
                resolveType = ((Function<String, Double>) s -> {
                    Double value = null;
                    if (s != null) {
                        try {
                            value = new Double(s);
                        } catch (Exception ignored) {
                        }
                    }
                    return value;
                });
            }
            if (CsiDataType.Number.equals(valueType)) {
                resolveType = ((Function<String, String>) s -> {
                    Double value = null;
                    if (s != null) {
                        try {
                            value = new Double(s);
                        } catch (Exception ignored) {
                        }
                    }
                    if (value != null) {
                        if ((value % 1) == 0) {
                            NumberFormat format = NumberFormat.getFormat("0.0#");
                            return format.format(value);
                        } else {
                            // should we add custom formatters to every field?
                            return value.toString();
                        }
                    } else {
                        return "";
                    }
                });
            }
            if (resolveType == null) {
                resolveType = ((Function<T, String>) s -> {
                    if (s == null) {
                        return "";
                    }
                    return s.toString();
                });
            }
        }

        public T getValue(List<?> object) {
            if (position >= 0) {
                return (T) resolveType.apply(object.get(position));
            } else {
                return null;
            }
        }

        public void setValue(List<?> object, T value) {
        }

        String getFieldDefUuid() {
            return defUuid;
        }

        public int getPosition() {
            return this.position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getPath() {
            return defUuid;
        }
    }

    private static class TableModelKeyProvider implements ModelKeyProvider<List<?>> {
        @Override
        public String getKey(List<?> item) {
            return item.get(0).toString();
        }
    }
}

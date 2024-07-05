package csi.client.gwt.viz.table;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextArea;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.filter.FilterCapableVisualizationPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.viz.shared.search.Searchable;
import csi.client.gwt.viz.table.TableView.TableValueProvider;
import csi.client.gwt.viz.table.grid.TableVizGrid;
import csi.client.gwt.viz.table.menu.TableMenuManager;
import csi.client.gwt.viz.timeline.view.SearchBox;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.table.ColumnState;
import csi.server.common.model.visualization.table.TableCachedState;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.common.service.api.TableActionsServiceProtocol;
import csi.server.ws.actions.PagingInfo;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.gwt.viz.table.TableSearchRequest;

import java.io.IOException;
import java.util.*;

public class TablePresenter extends AbstractVisualizationPresenter<TableViewDef, TableView>
        implements FilterCapableVisualizationPresenter, Searchable {

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private TableCachedState previousState;
    private boolean saveCache = true;

    private SearchBox<TablePresenter> searchBox;
    private CopyCellPresenter copyCellPresenter = new CopyCellPresenter(this);
    
    private List<HandlerRegistration> copyHandlers = Lists.newArrayList();

    private ClickHandler closeSearchHandler = event -> hideFind();

    private ClickHandler rightSearchHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            searchNext(searchBox.getText());
            // findNext(searchEvent, searchTrackName);
            //
            // if(searchEvent != null && searchEvent.getTrack() != null &&
            // searchEvent.getTrack().isCollapsed()){
            // searchEvent.getTrack().setCollapsed(false);
            // adjustGroups();
            // }
            //
            // scrollToNextEvent(searchEvent);
            // panToNextEvent(searchEvent);
        }
    };

    private ClickHandler leftSearchHandler = event -> {
        // findPrevious(searchEvent, searchTrackName);
        //
        // if(searchEvent != null && searchEvent.getTrack() != null &&
        // searchEvent.getTrack().isCollapsed()){
        // searchEvent.getTrack().setCollapsed(false);
        // adjustGroups();
        // }
        // scrollToPreviousEvent(searchEvent);
        // panToPreviousEvent(searchEvent);
    };

    private String oldText;
    private TableMode mode;

    public TablePresenter(AbstractDataViewPresenter dvPresenterIn, TableViewDef visualizationDef) {
        super(dvPresenterIn, visualizationDef);
//        createSearchWindow();
    }

    public TablePresenter(TableViewDef visualizationDef) {
        super(visualizationDef);
//        createSearchWindow();
    }

    @Override
    public boolean hasSelection() {
        return getView().hasSelection();
    }

    @Override
    public void saveViewStateToVisualizationDef() {
        getView().saveSelectionsToModel();
    }

    @Override
    public ImagingRequest getImagingRequest() {
        return null;
    }

    @Override
    public void reload() {
        rememberState();
        super.reload();
    }

    private void rememberState() {
        updateCache();
        previousState = getVisualizationDef().getState();
        // previousState.setVerticalScrollPosition(0);
    }

    @Override
    public void loadVisualization() {

        this.appendNotificationText(NotificationLabel.FILTER, getVisualizationDef().getFilter() != null);
        switchMode(TableMode.NORMAL);

        VortexFuture<PagingInfo> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(TableActionsServiceProtocol.class).getRowCount(getDataViewUuid(),
                    getVisualizationDef().getUuid());
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<PagingInfo>() {
                @Override
                public void onSuccess(PagingInfo info) {

                    if (info.totalRecords == 0) {
                        getView().setInit(true);
                        getView().displayNoData();
                        return;
                    } else {
                        getView().forceGridVisible();
                    }

                    getView().initialize(info.totalRecords);
                    getView().setInit(true);

                    if (hasOldSelection())
                        applySelection(popOldSelection());

                    // updateCache();
                }

                @Override
                public boolean onError(Throwable t) {
                    new ErrorDialog(i18n.tablePresenterErrorDialog()).show(); // $NON-NLS-1$
                    return super.onError(t);
                }
            });
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }

    }

//    private void createSearchWindow() {
//        ScheduledCommand cmd = new ScheduledCommand() {
//
//            @Override
//            public void execute() {
//                if (getView() == null || getView().getTableGrid() == null) {
//                    Scheduler.get().scheduleDeferred(this);
//                } else {
//                    if (searchBox == null) {
//                        searchBox = new SearchBox<TablePresenter>(TablePresenter.this);
//                        getChrome().addSearchBox(searchBox);
//                        searchBox.addCloseClickHandler(closeSearchHandler);
//                        searchBox.addLeftButtonClickHandler(leftSearchHandler);
//                        searchBox.addRightButtonClickHandler(rightSearchHandler);
//                        searchBox.disableLeftButton();
//
//                        // getMenuManager().hide(MenuKey.HIDE_SEARCH);
//                        hideFind();
//                    }
//                }
//            }
//
//        };
//        Scheduler.get().scheduleDeferred(cmd);
//    }

    public void hideFind() {

//        if (searchBox != null && getMenuManager() != null) {
        if (getMenuManager() != null) {
            getMenuManager().hide(MenuKey.HIDE_SEARCH);
            getMenuManager().enable(MenuKey.SHOW_SEARCH);
            oldText = searchBox.getText();
            searchBox.setText("");
            searchBox.setVisible(false);

            getView().clearHighlights();
            // searchText(searchBox.getText());
            searchBox = null;
        }

    }

    public void showFind() {
        searchBox = new SearchBox<>(TablePresenter.this);
        getChrome().addSearchBox(searchBox);
        searchBox.addCloseClickHandler(closeSearchHandler);
        searchBox.addLeftButtonClickHandler(leftSearchHandler);
        searchBox.addRightButtonClickHandler(rightSearchHandler);
        searchBox.disableLeftButton();
        searchBox.setVisible(false);
        if (oldText != null) {
            //searchBox.setText(oldText);
            oldText = null;
        }
        getMenuManager().enable(MenuKey.HIDE_SEARCH);
        getMenuManager().hide(MenuKey.SHOW_SEARCH);

        Scheduler.get().scheduleDeferred(() -> {
            Style style = searchBox.getElement().getStyle();
            style.clearLeft();
            style.clearRight();
            style.clearTop();
            style.clearBottom();
            searchBox.setVisible(true);
            searchBox.focus();
        });
    }

    private void searchGrid(String searchText, boolean next) {

        TableView tableView = getView();
        TableVizGrid grid = tableView.getTableGrid();
        if(searchText == null || searchText.isEmpty()) {
            tableView.clearHighlights();
            return;
        }
        List<List<?>> clientData = tableView.getGridComponentManager().getStore().getAll();
        List<TableValueProvider> valueProviders = tableView.getValueProviders();
        int startingRowIndex = 0;
        int startingColumnIndex = 1;

        Element lastHighlight = tableView.getLastHighlight();
        if (next && lastHighlight != null) {
            int rowIndex = grid.getView().findRowIndex(lastHighlight);

            if (rowIndex >= 0) {
                startingRowIndex = rowIndex;
                int colIndex = grid.getView().findCellIndex(lastHighlight, null);
                if (colIndex > 0) {

//                    ColumnConfig column = grid.getColumnModel().getColumn(colIndex);
//                    ValueProvider valueProvider = column.getValueProvider();
//                    Object object = valueProvider.getValue(clientData.get(startingRowIndex));
//                    if (object != null) {
//                        String value = object.toString();
//                        if (value.contains(searchText)) {
//                            // If they are typing and the same value still fits,
//                            // don't move
//                            return;
//                        }
//                    }

                    // Move right
                    startingColumnIndex = colIndex + 1;
                }
            }
        }
        for (int jj = startingRowIndex; jj < clientData.size(); jj++) {
            List<?> row = clientData.get(jj);

            for (int ii = startingColumnIndex; ii < grid.getColumnModel().getColumnCount(); ii++) {
                ColumnConfig column = grid.getColumnModel().getColumn(ii);
                ValueProvider valueProvider = column.getValueProvider();
                Object object = valueProvider.getValue(row);
                if (object != null) {
                    String value = object.toString();
                    if (value.contains(searchText)) {

                        Object o = row.get(0);
                        int val = (int) o;
                        grid.fireEvent(new SearchCellHighlightEvent(jj, ii, val));
                        return;
                    }
                }

            }

            // Reset after first row is done
            startingColumnIndex = 1;

        }

        TableSearchRequest searchRequest = new TableSearchRequest();
        searchRequest.searchText = searchText;
        searchRequest.dataViewUuid = getDataViewUuid();
        searchRequest.visualizationUuid = getUuid();
        searchRequest.offset = tableView.getGridComponentManager().getLoader().getOffset()
                + tableView.getGridComponentManager().getLoader().getLimit();
        searchRequest.limit = tableView.getGridComponentManager().getLoader().getLimit();
        FilterPagingLoadConfig config = tableView.getGridComponentManager().getLoader().getLastLoadConfig();

        VortexFuture<Integer> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            //Can't search between loads
            if(config == null) {
                return;
            }
            vortexFuture.execute(TableActionsServiceProtocol.class).searchTable(searchRequest, config);
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<Integer>() {

                @Override
                public void onSuccess(Integer result) {
                    if (result >= 0) {
                        if (result > 1) {
                            int searchBuffer = 2;
                            result = result - searchBuffer;
                        }
                        tableView.markForSearch(searchText);
                        tableView.scrollToIndex(result);
                        // searchGrid(searchText);
                    } else {
                        if(lastHighlight != null) {
                            tableView.markForSearch(searchText);
                            tableView.scrollToIndex(0);
                            tableView.clearHighlights();
                        } else {
                            tableView.unmarkForSearch();
                            //No hits found
                            tableView.clearHighlights();
                        }
                        
                        //InfoDialog dialog = new InfoDialog(searchText, searchText);
                    }
                }
            });
        } catch (CentrifugeException | IOException ignored) {

        }
    }

    public void updateCache() {
        TableCachedState state = new TableCachedState();
        TableVizGrid grid = getView().getTableGrid();

        if (grid != null) {
            ColumnModel columnModel = grid.getColumnModel();

            List<ColumnConfig> columns = columnModel.getColumns();
            List<ColumnState> columnStates = new ArrayList<>();

            Set<String> localIds = new HashSet<>();
            if (getVisualizationDef() != null && getVisualizationDef().getTableViewSettings() != null) {
                for (VisibleTableField tableField : getVisualizationDef().getTableViewSettings().getVisibleFields()) {
                    String localId = tableField.getFieldId();
                    localIds.add(localId);
                }
            }

            int index = 0;
            for (ColumnConfig column : columns) {
                ColumnState columnState = new ColumnState();
                columnState.setWidth(column.getWidth());
                columnState.setIndex(index++);
                FieldDef fieldDef = getDataModel().getFieldListAccess()
                        .getFieldDefByUuid(column.getValueProvider().getPath());
                if (fieldDef != null && localIds.contains(fieldDef.getLocalId())) {
                    columnState.setFieldDef(fieldDef);
                    columnStates.add(columnState);
                }
            }

            state.setColumnStates(columnStates);

            if (getView().getTableGrid() != null && getView().getTableGrid().getView() != null
                    && getView().getTableGrid().getView().getScroller() != null) {
                Scroll scroll = getView().getTableGrid().getView().getScroller().getScroll();
                if (scroll != null) {
                    state.setHorizontalScrollPosition(scroll.getScrollLeft());
                    state.setVerticalScrollPosition(scroll.getScrollTop());
                }
            }

            // state.setVerticalScrollPosition(getView().getScroll().getScrollTop());
            updateHorizontalScroll();
            // state.setPage(getView().getPageOffset());
            getVisualizationDef().setState(state);
        }
    }

    private void updateHorizontalScroll() {
        
        if(getView() == null) {
            return;
        }
        
        TableVizGrid grid = getView().getTableGrid();
        Map<String, FieldDef> fieldMap = getView().getColumnFields();

        if (grid != null) {
            ColumnModel columnModel = grid.getColumnModel();

            TableCachedState state = getVisualizationDef().getState();
            if (state == null) {
                state = new TableCachedState();
                getVisualizationDef().setState(state);
            }

            if (getView().getTableGrid() != null && getView().getTableGrid().getView() != null && getView().getTableGrid().getView().getBody() != null) {
                Scroll scroll = getView().getTableGrid().getView().getScroller().getScroll();
                if(scroll != null) {
                    state.setHorizontalScrollPosition(scroll.getScrollLeft());
                }
            }

        }
    }

    @Override
    public void applySelection(Selection selection) {
        getVisualizationDef().getSelection().setFromSelection(selection);
        getView().refresh();
    }

    @Override
    public <V extends Visualization> AbstractMenuManager<V> createMenuManager() {
        return (AbstractMenuManager<V>) new TableMenuManager(this);
    }

    @Override
    public TableView createView() {
        return new TableView(this);
    }

    @Override
    public void setChrome(VizChrome vizChrome) {
        super.setChrome(vizChrome);
        vizChrome.hideControlLayer();
    }
    
 

    @Override
    public VortexFuture<Void> saveSettings(final boolean refreshOnSuccess, final boolean isStructural) {

        if (isSaveCache()) {
            updateCache();
        } else {
            setSaveCache(true);
        }

        return super.saveSettings(refreshOnSuccess, isStructural);
    }

    public void selectAll() {
        VortexFuture<int[]> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(TableActionsServiceProtocol.class).getAllIds(getDataViewUuid(), getUuid());
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<int[]>() {
                @Override
                public void onSuccess(int[] ids) {
                    getView().selectAll(ids);
                }

                @Override
                public boolean onError(Throwable t) {
                    new ErrorDialog(i18n.tablePresenterErrorDialog()).show(); // $NON-NLS-1$
                    return super.onError(t);
                }
            });
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
    }

    TableCachedState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(TableCachedState tableCachedState) {
        this.previousState = tableCachedState;
    }

    @Override
    public void broadcastNotify(String text) {
        getView().broadcastNotify(text);
        appendBroadcastIcon();
    }

    private boolean isSaveCache() {
        return saveCache;
    }

    public void setSaveCache(boolean saveCache) {
        this.saveCache = saveCache;
    }

    public void deselectAll() {
        getView().deSelectAll();
    }

    @Override
    public void searchText(String text) {
        searchGrid(text, false);
    }

    private void searchNext(String text) {
        searchGrid(text, true);
    }

    public void startCopy() {
        if(this.mode!=TableMode.COPY) {
            //go back in
            switchMode(TableMode.COPY);
        }
    }

    void switchMode(TableMode mode) {
        this.mode = mode;
        //In case they did something mid copy
        endCopy();
        if(getView() != null && getView().getTableGrid() != null)
        switch(mode) {
        case COPY:
            getView().getTableGrid().getElement().getStyle().setCursor(Cursor.CROSSHAIR);
            copyCellPresenter.startCopyMode();
            break;
        case NORMAL:
        case SEARCH:
            getView().getTableGrid().getElement().getStyle().setCursor(Cursor.DEFAULT);
            break;
        default:
            getView().getTableGrid().getElement().getStyle().setCursor(Cursor.DEFAULT);
            break;
        
        }
    }

    void lockFocusToCopy(TextArea hiddenTextBox) {
        getView().disable();
        copyHandlers.add(getView().getTableGrid().addHandler(event -> {
            hiddenTextBox.getElement().focus();
            hiddenTextBox.selectAll();
            event.stopPropagation();
        }, com.google.gwt.event.dom.client.ClickEvent.getType()));
        
        copyHandlers.add(getView().getTableGrid().addHandler(event -> {
            hiddenTextBox.getElement().focus();
            hiddenTextBox.selectAll();
            event.stopPropagation();
        }, com.google.gwt.event.dom.client.FocusEvent.getType()));

        copyHandlers.add(getView().getTableGrid().addHandler(event -> {
            hiddenTextBox.getElement().focus();
            hiddenTextBox.selectAll();

        }, com.google.gwt.event.dom.client.MouseOverEvent.getType()));
    }
    
    public void showLoading() {

        getView().showLoadingSpinner();
    }
    
    void hideLoading() {
        getView().hideLoadingSpinner();
    }
    
    

    void endCopy() {
        hideLoading();
        getView().clearHighlights();
        getView().enable();
        
        for(HandlerRegistration register: copyHandlers) {
            register.removeHandler();
        }
        copyCellPresenter.reset();
        copyHandlers.clear();
    }

    void highlightCellRange(int startRowIndex, int startCellIndex, int endRowIndex, int endCellIndex) {
        TableView tableView = getView();
        TableVizGrid grid = tableView.getTableGrid();
        grid.fireEvent(new CopyCellHighlightEvent(startRowIndex, startCellIndex, endRowIndex, endCellIndex));
    }
    public TableMode getMode() {
        return this.mode;
    }

}

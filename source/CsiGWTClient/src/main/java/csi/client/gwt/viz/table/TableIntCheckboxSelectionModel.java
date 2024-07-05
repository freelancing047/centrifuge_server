package csi.client.gwt.viz.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;

import com.sencha.gxt.widget.core.client.grid.Grid;
import csi.client.gwt.widget.gxt.grid.CsiCheckboxSelectionModel;
import csi.client.gwt.widget.gxt.grid.paging.GroupingView;
import csi.shared.core.util.IntCollection;



public class TableIntCheckboxSelectionModel<T> extends CsiCheckboxSelectionModel<List<T>> {

    private IntCollection allSelectedIds = new IntCollection();
    private IntCollection possibleIdsOnThisPage = new IntCollection();


    private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<T>>> pagingLoader;
    private int lastRowSelected;
    private TablePresenter presenter;

    public TableIntCheckboxSelectionModel(IdentityValueProvider<List<T>> identity,
            PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<T>>> loader, TablePresenter presenter) {
        super(identity);
        this.pagingLoader = loader;
        this.presenter = presenter;
        addPagingHandler();
    }

    private void addPagingHandler() {
        pagingLoader.addBeforeLoadHandler(new BeforeLoadEvent.BeforeLoadHandler<FilterPagingLoadConfig>() {
            @Override
            public void onBeforeLoad(BeforeLoadEvent<FilterPagingLoadConfig> event) {
                getAllSelectedIds();
            }
        });

        pagingLoader.addLoadHandler(new LoadHandler<FilterPagingLoadConfig, PagingLoadResult<List<T>>>() {
            @Override
            public void onLoad(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<List<T>>> event) {
                updatePossibleIds(event);
                applySelectionOnThisPage(event);
            }
        });
    }

    private void updatePossibleIds(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<List<T>>> event) {
        possibleIdsOnThisPage.clear();
        possibleIdsOnThisPage.addAll(getIdsFromData(event.getLoadResult().getData()));
    }

    private void applySelectionOnThisPage(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<List<T>>> event) {
        List<List<T>> selectedRows = getSelectedRows(event.getLoadResult().getData());
        select(selectedRows, true);
    }

    private List<List<T>> getSelectedRows(List<List<T>> data) {
        List<List<T>> selectedRows = new ArrayList<List<T>>();
        allSelectedIds.sort();

        List<List<T>> dataCopy = Lists.newArrayList(data);
        Collections.sort(dataCopy, (o1, o2) -> {
            if(o1 == null){
                return -1;
            }
            if(o2 == null){
                return 1;
            }

            return ((Integer) o1.get(0)).compareTo((Integer) o2.get(0));
        });

        int firstIndex = 0;
        int secondIndex = 0;
        while(firstIndex < allSelectedIds.size()){
            Integer firstRowId = allSelectedIds.get(firstIndex);
            while(secondIndex < dataCopy.size() && firstIndex < allSelectedIds.size()){
                Integer secondRowId = (Integer) dataCopy.get(secondIndex).get(0);
                if(firstRowId.equals(secondRowId)){
                    selectedRows.add(dataCopy.get(secondIndex));
                    secondIndex++;
                    break;
                } else if(firstRowId < secondRowId){
                    break;
                } else {
                    secondIndex++;
                }
            }
            firstIndex++;
        }

        return selectedRows;
    }

    @Override
    protected void doMultiSelect(List<List<T>> modelsIn, boolean keepExistingIn, boolean suppressEventIn) {

        if (locked) return;

        boolean myChange = false;

        boolean isGrouped = grid != null && grid.getView() != null && grid.getView() instanceof GroupingView;
        if(isGrouped){
            ((GroupingView) grid.getView()).cacheTableRows();
        }

        if (!keepExistingIn && (selected.size() != 0)) {

            myChange = true;
            doDeselect(new ArrayList<List<T>>(selected), true);
        }



        for (List<T> myModel : modelsIn) {

            boolean isSelected = isSelected(myModel);

            if (!suppressEventIn && !isSelected) {

                BeforeSelectionEvent<List<T>> evt = BeforeSelectionEvent.fire(this, myModel);

                if (evt != null && evt.isCanceled()) {

                    continue;
                }
            }
            //
            // IMPORTANT: Only select item if not previously selected
            //
            if (!isSelected) {
                if(modelsIn.size() == 1){
                    //               int toIndex  = (int) myModel.get(0);
                }

                myChange = true;
                lastSelected = myModel;
                selected.add(myModel);
                if (lastSelected != null) {
                    setLastFocused(lastSelected);
                }
                onSelectChange(myModel, true);
                if (!suppressEventIn) {

                    SelectionEvent.fire(this, myModel);
                }
            }
        }

        if(isGrouped){
            ((GroupingView) grid.getView()).invalidateTableRows();
        }

        if (myChange && !suppressEventIn) {
            fireSelectionChange();
        }
    }

    @Override
    protected void doSelect(List<List<T>> models, boolean keepExisting, boolean suppressEvent) {
        if (locked) return;
        if(keepExisting){

        } else {
            //allSelectedIds.clear();
            possibleIdsOnThisPage.clear();
        }
        if (selectionMode == SelectionMode.SINGLE) {
            List<T> m = models.size() > 0 ? models.get(0) : null;
            if (m != null) {
                doSingleSelect(m, suppressEvent);
            }
        } else {
            doMultiSelect(models, keepExisting, suppressEvent);
        }
    }

    @Override
    public void refresh() {
        List<List<T>> sel = new ArrayList<List<T>>();
        boolean change = false;
        for (List<T> m : selected) {
            List<T> storeModel= store.findModel(m);
            if (storeModel != null) {
                sel.add(storeModel);
            }
        }
        if (sel.size() != selected.size()) {
            change = true;
        }
        selected.clear();
        if(lastSelected!= null){
            //            lastRowSelected = (int) lastSelected.get(0);
        }
        lastSelected = null;
        setLastFocused(null);
        doSelect(sel, true, true);
        if (change) {
            fireSelectionChange();
        }
    }
    
    @Override
    protected void handleHeaderClick(HeaderClickEvent eventIn) {
        int myColumnIndex = eventIn.getColumnIndex();
        boolean left = eventIn.getEvent().getButton() == Event.BUTTON_LEFT;

        if (left && (0 == myColumnIndex)) {
          
            //super.handleHeaderClick(eventIn);
            if(presenter.hasSelection()) {
                presenter.deselectAll();
            } else {
                presenter.selectAll();
            }
        }
    }

    @Override
    public void select(int start, int end, boolean keepExisting) {
        if (store instanceof ListStore) {
            ListStore<T> ls = (ListStore<T>) store;
            List<List<T>> sel = new ArrayList<List<T>>();
            if (start <= end) {
                for (int i = start; i <= end; i++) {
                    sel.add((List<T>) ls.get(i));
                }
            } else {
                for (int i = start; i >= end; i--) {
                    sel.add((List<T>) ls.get(i));
                }
            }

            if(!keepExisting){
                clearSelectionState();
            }
            doSelect(sel, keepExisting, false);
        }
    }

    @Override
    protected void onRowMouseDown(RowMouseDownEvent event) {
        boolean left = event.getEvent().getButton() == Event.BUTTON_LEFT;

        if (left && event.getColumnIndex() == grid.getColumnModel().getColumns().indexOf(getColumn())) {
            List<T> model = listStore.get(event.getRowIndex());
            if (model != null) {
                if (isSelected(model)) {
                    deselect(model);
                } else {
                    select(model, true);
                }
            }
        } else {
            if(!event.getEvent().getCtrlKey() && (lastSelected == null || !event.getEvent().getShiftKey())){
                clearSelectionState();
            }
            super.onRowMouseDown(event);
        }
    }

    public void clearSelectionState() {
        allSelectedIds.clear();
        doDeselect(new ArrayList<List<T>>(selected), true);
        selected.clear();
        lastSelected = null;
    }


    public void setSelectionByIds(int[] newSelection) {

        allSelectedIds.clear();
        allSelectedIds.addAll(newSelection);

        List<List<T>> selectedRows = getSelectedRows(getGrid().getStore().getAll());
        setSelection(selectedRows);
    }

    public void setSelectionByIds(Collection<Integer> newSelection) {

        allSelectedIds.clear();
        allSelectedIds.addAll(newSelection);

        List<List<T>> selectedRows = getSelectedRows(getGrid().getStore().getAll());
        setSelection(selectedRows);
    }

    public IntCollection getAllSelectedIds() {
        Grid<List<T>> grid = getGrid();
        if (grid != null) {
            allSelectedIds.removeAll(getIdsFromData(grid.getStore().getAll()));
        }
        List<Integer> selectedIdsOnThisPage = getIdsFromData(getSelection());
        allSelectedIds.addAllSorted(selectedIdsOnThisPage);
        return allSelectedIds;
    }

    private List<Integer> getIdsFromData(List<List<T>> data){
        List<Integer> ids = new ArrayList<Integer>();
        for(List<T> row : data){
            ids.add((Integer) row.get(0));
        }
        return ids;
    }

    public boolean hasSelection() {
        return allSelectedIds.size() > 0;
    }

}

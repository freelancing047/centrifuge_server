package com.sencha.gxt.data.shared;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.event.StoreAddEvent.StoreAddHandler;
import com.sencha.gxt.data.shared.event.StoreClearEvent.StoreClearHandler;
import com.sencha.gxt.data.shared.event.StoreDataChangeEvent.StoreDataChangeHandler;
import com.sencha.gxt.data.shared.event.StoreFilterEvent.StoreFilterHandler;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent.StoreRecordChangeHandler;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent.StoreRemoveHandler;
import com.sencha.gxt.data.shared.event.StoreSortEvent.StoreSortHandler;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent.StoreUpdateHandler;

import csi.client.gwt.viz.chart.view.ChartGridRow;

public class ChartTableListStore extends ListStore<ChartGridRow> {
	private ListStore<ChartGridRow> listStore;
	private Map<String, Integer> keyIndexMap = new HashMap<String, Integer>();

	public ChartTableListStore(ModelKeyProvider<? super ChartGridRow> keyProvider) {
		super(keyProvider);
	}

	public ChartTableListStore(ListStore<ChartGridRow> listStoreToWrap) {
		this(listStoreToWrap.getKeyProvider());
		listStore = listStoreToWrap;
	}

	public void add(int index, ChartGridRow item) {
		listStore.add(index, item);
		keyIndexMap.clear();
	}

	public void add(ChartGridRow item) {
		int i = size();
		add(i, item);
	}

	public boolean addAll(Collection<? extends ChartGridRow> items) {
		return addAll(size(), items);
	}

	public boolean addAll(int index, Collection<? extends ChartGridRow> items) {
		boolean retVal = listStore.addAll(index, items);
		keyIndexMap.clear();
		return retVal;
	}

	public void addFilter(StoreFilter<ChartGridRow> filter) {
		listStore.addFilter(filter);
	}

	public void addSortInfo(int index, StoreSortInfo<ChartGridRow> info) {
		listStore.addSortInfo(index, info);
		keyIndexMap.clear();
	}

	public void addSortInfo(StoreSortInfo<ChartGridRow> info) {
		listStore.addSortInfo(info);
		keyIndexMap.clear();
	}

	@Override
	public HandlerRegistration addStoreAddHandler(StoreAddHandler<ChartGridRow> handler) {
		return listStore.addStoreAddHandler(handler);
	}

	@Override
	public HandlerRegistration addStoreClearHandler(StoreClearHandler<ChartGridRow> handler) {
		return listStore.addStoreClearHandler(handler);
	}

	@Override
	public HandlerRegistration addStoreDataChangeHandler(StoreDataChangeHandler<ChartGridRow> handler) {
		return listStore.addStoreDataChangeHandler(handler);
	}

	@Override
	public HandlerRegistration addStoreFilterHandler(StoreFilterHandler<ChartGridRow> handler) {
		return listStore.addStoreFilterHandler(handler);
	}

	@Override
	public HandlerRegistration addStoreHandlers(StoreHandlers<ChartGridRow> handlers) {
		return listStore.addStoreHandlers(handlers);
	}

	@Override
	public HandlerRegistration addStoreRecordChangeHandler(StoreRecordChangeHandler<ChartGridRow> handler) {
		return listStore.addStoreRecordChangeHandler(handler);
	}

	@Override
	public HandlerRegistration addStoreRemoveHandler(StoreRemoveHandler<ChartGridRow> handler) {
		return listStore.addStoreRemoveHandler(handler);
	}

	@Override
	public HandlerRegistration addStoreSortHandler(StoreSortHandler<ChartGridRow> handler) {
		return listStore.addStoreSortHandler(handler);
	}

	@Override
	public HandlerRegistration addStoreUpdateHandler(StoreUpdateHandler<ChartGridRow> handler) {
		return listStore.addStoreUpdateHandler(handler);
	}

	@Override
	protected void applyFilters() {
		listStore.applyFilters();
		keyIndexMap.clear();
	}

	@Override
	public void applySort(boolean suppressEvent) {
		listStore.applySort(suppressEvent);
		keyIndexMap.clear();
	}

	protected Comparator<ChartGridRow> buildFullComparator() {
		return listStore.buildFullComparator();
	}

	@Override
	public void clear() {
		listStore.clear();
		keyIndexMap.clear();
	}

	public void clearSortInfo() {
		listStore.clearSortInfo();
		keyIndexMap.clear();
	}

	public void commitChanges() {
		listStore.commitChanges();
	}

	protected HandlerManager ensureHandlers() {
		return listStore.ensureHandlers();
	}

	@Override
	public ChartGridRow findModelWithKey(String key) {
		if (listStore.size() > 200) {
			if (keyIndexMap.size() == 0) {
				populateKeyIndexMap();
			}
			int index = keyIndexMap.get(key);
			return listStore.get(index);
		} else {
			return listStore.findModelWithKey(key);
		}
	}

	public void fireEvent(GwtEvent<?> event) {
		listStore.fireEvent(event);
	}

	protected void fireSortedAddEvents(Collection<? extends ChartGridRow> items) {
		listStore.fireSortedAddEvents(items);
	}

	public ChartGridRow get(int index) {
		return listStore.get(index);
	}

	@Override
	public List<ChartGridRow> getAll() {
		return listStore.getAll();
	}

	public LinkedHashSet<StoreFilter<ChartGridRow>> getFilters() {
		return listStore.getFilters();
	}

	public ModelKeyProvider<? super ChartGridRow> getKeyProvider() {
		return listStore.getKeyProvider();
	}

	public Collection<Store<ChartGridRow>.Record> getModifiedRecords() {
		return listStore.getModifiedRecords();
	}

	public Record getRecord(ChartGridRow data) {
		return listStore.getRecord(data);
	}

	public List<StoreSortInfo<ChartGridRow>> getSortInfo() {
		return listStore.getSortInfo();
	}

	public boolean hasMatchingKey(ChartGridRow model1, ChartGridRow model2) {
		return listStore.hasMatchingKey(model1, model2);
	}

	public boolean hasRecord(ChartGridRow data) {
		return listStore.hasRecord(data);
	}

	public int indexOf(ChartGridRow item) {
		if (listStore.size() > 200) {
			if (keyIndexMap.size() == 0) {
				populateKeyIndexMap();
			}
			return keyIndexMap.get(item.getDimension());
		} else {
			return listStore.indexOf(item);
		}
	}

	private void populateKeyIndexMap() {
		keyIndexMap.clear();
		for (int i = 0; i < listStore.size(); i++) {
			keyIndexMap.put(listStore.get(i).getDimension(), i);
		}
	}

	public boolean isAutoCommit() {
		return listStore.isAutoCommit();
	}

	public boolean isEnableFilters() {
		return listStore.isEnableFilters();
	}

	public boolean isFiltered() {
		return listStore.isFiltered();
	}

	protected boolean isFilteredOut(ChartGridRow item) {
		return listStore.isFilteredOut(item);
	}

	protected boolean isSorted() {
		return listStore.isSorted();
	}

	public void rejectChanges() {
		listStore.rejectChanges();
	}

	public ChartGridRow remove(int index) {
		ChartGridRow retVal = listStore.remove(index);
		keyIndexMap.clear();
		return retVal;
	}

	@Override
	public boolean remove(ChartGridRow model) {
		boolean retVal = listStore.remove(model);
		if (retVal)
			keyIndexMap.clear();
		return retVal;
	}

	public void removeFilter(StoreFilter<ChartGridRow> filter) {
		listStore.removeFilter(filter);
	}

	public void removeFilters() {
		listStore.removeFilters();
	}

	public void replaceAll(List<? extends ChartGridRow> newItems) {
		listStore.replaceAll(newItems);
		populateKeyIndexMap();
	}

	public void setAutoCommit(boolean isAutoCommit) {
		listStore.setAutoCommit(isAutoCommit);
	}

	public void setEnableFilters(boolean enableFilters) {
		listStore.setEnableFilters(enableFilters);
	}

	public int size() {
		return listStore.size();
	}

	public List<ChartGridRow> subList(int start, int end) {
		return listStore.subList(start, end);
	}

	public void update(ChartGridRow item) {
		listStore.update(item);
	}

	public <V> ValueProvider<? super ChartGridRow, V> wrapRecordValueProvider(final ValueProvider<? super ChartGridRow, V> valueProvider) {
		return listStore.wrapRecordValueProvider(valueProvider);
	}
}
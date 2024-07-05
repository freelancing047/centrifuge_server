package com.sencha.gxt.widget.core.client.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.viz.chart.view.ChartGridRow;

public class ChartTableSelectionModel extends GridSelectionModel<ChartGridRow> {
	private Set<String> keyList = new HashSet<String>();

	@Override
	public boolean isSelected(ChartGridRow item) {
		return keyList.contains(item.getDimension());
	}

	@Override
	public void refresh() {
		clearKeyList();
		super.refresh();
	}

	private void clearKeyList() {
		keyList.clear();
	}

	protected void doDeselect(List<ChartGridRow> models, boolean suppressEvent) {
		if (locked)
			return;
		for (ChartGridRow m : models) {
			if (!isSelected(m))
				continue;
			if (selected.remove(m)) {
				removeFromKeyList(m);
				if (lastSelected == m) {
					lastSelected = selected.size() > 0 ? selected.get(selected.size() - 1) : null;
				}
				onSelectChange(m, false);
			}
		}
		if (!suppressEvent) {
			fireSelectionChange();
		}
	}

	private void removeFromKeyList(ChartGridRow item) {
		keyList.remove(item.getDimension());
	}

	protected void doMultiSelect(List<ChartGridRow> models, boolean keepExisting, boolean suppressEvent) {
		if (locked)
			return;
		if (!keepExisting && selected.size() > 0) {
			doDeselect(new ArrayList<ChartGridRow>(selected), true);
		}

		for (ChartGridRow m : models) {
			boolean isSelected = isSelected(m);
			lastSelected = m;

			if (!isSelected) {
				selected.add(m);
				addToKeyList(m);
				onSelectChange(m, true);
			}
		}
		if (!suppressEvent) {
			fireSelectionChange();
		}
	}

	private void addToKeyList(ChartGridRow item) {
		keyList.add(item.getDimension());
	}

	protected void doSingleSelect(ChartGridRow model, boolean suppressEvent) {
		if (locked)
			return;

		int index = -1;
		if (store instanceof ListStore) {
			ListStore<ChartGridRow> ls = (ListStore<ChartGridRow>) store;
			index = ls.indexOf(model);
		}
		if (store instanceof TreeStore) {
			TreeStore<ChartGridRow> ls = (TreeStore<ChartGridRow>) store;
			index = ls.indexOf(model);
		}
		if (index == -1 || isSelected(model)) {
			return;
		}

		if (selected.size() > 0 && !isSelected(model)) {
			doDeselect(Collections.singletonList(lastSelected), true);
		}

		lastSelected = model;
		setLastFocused(lastSelected);

		selected.add(model);
		addToKeyList(model);
		onSelectChange(model, true);
		if (!suppressEvent) {
			fireSelectionChange();
		}
	}

	protected void onClear(StoreClearEvent<ChartGridRow> event) {
		clearKeyList();
		super.onClear(event);
	}

	protected void onRemove(ChartGridRow model) {
		if (locked)
			return;
		if (!isSelected(model))
			return;
		if (selected.remove(model)) {
			removeFromKeyList(model);
			if (lastSelected == model) {
				lastSelected = null;
			}
			if (getLastFocused() == model) {
				setLastFocused(null);
			}
			fireSelectionChange();
		}
	}

	protected void onUpdate(ChartGridRow model) {
		if (locked)
			return;
		for (int i = 0; i < selected.size(); i++) {
			ChartGridRow m = selected.get(i);
			if (store.hasMatchingKey(model, m)) {
				if (m != model) {
					selected.remove(m);
					removeFromKeyList(m);
					selected.add(i, model);
				}
				if (lastSelected == m) {
					lastSelected = model;
				}
				break;
			}
		}
		if (getLastFocused() != null && model != getLastFocused() && store.hasMatchingKey(model, getLastFocused())) {
			setLastFocused(model);
		}
	}
	
	public void setSelected(List<String> keys) {
		List<ChartGridRow> models = new ArrayList<ChartGridRow>();
		for (String key : keys) {
			models.add(store.findModelWithKey(key));
		}
		doMultiSelect(models, false, true);
	}
	
	public void onSort() {
		grid.getView().refresh(false);
//		grid.getView().onDataChanged(null);
	}
	
	@Override
	protected void fireSelectionChange() {
		if (mouseDown) {
			fireSelectionChangeOnClick = true;
		}
		fireEvent(new SelectionChangedEvent<ChartGridRow>(selected));
	}
}

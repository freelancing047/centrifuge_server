package csi.client.gwt.viz.shared.filter;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;

import csi.server.common.model.filter.FilterExpression;

public class FilterCheckBoxCell extends CheckBoxCell {
	private ListStore<FilterExpression> gridStore;
	private ValueProvider<FilterExpression, Boolean> isSelectionValueProvider;

	public FilterCheckBoxCell(ListStore<FilterExpression> gridStore,
			ValueProvider<FilterExpression, Boolean> isSelectionValueProvider) {
		super();
		this.gridStore = gridStore;
		this.isSelectionValueProvider = isSelectionValueProvider;
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
		boolean isSelectionFilter = (Boolean) gridStore.getRecord(gridStore.findModelWithKey(context.getKey().toString())).getValue(isSelectionValueProvider);
		if (!isSelectionFilter) {
			super.render(context, value, sb);
		}
	}
}

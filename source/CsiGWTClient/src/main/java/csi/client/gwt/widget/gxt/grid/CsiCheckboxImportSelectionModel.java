package csi.client.gwt.widget.gxt.grid;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.event.RowClickEvent;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import csi.client.gwt.widget.gxt.grid.paging.GroupingView;

import java.util.ArrayList;
import java.util.List;

public class CsiCheckboxImportSelectionModel<M> extends CheckBoxSelectionModel<M> {

    private int _lastRowSelected = -1;

    /**
     * Creates a CheckBoxSelectionModel that will operate on the row itself. To customize the row
     * it is acting on, use a constructor that lets you specify a ValueProvider, to customize how
     * each row is drawn, use a constructor that lets you specify an appearance instance.
     */
    public CsiCheckboxImportSelectionModel() {
        super();
    }

    /**
     * Creates a CheckBoxSelectionModel with a custom ValueProvider instance.
     *
     * @param valueProvider the ValueProvider to use when constructing a ColumnConfig
     */
    public CsiCheckboxImportSelectionModel(ValueProvider<M, M> valueProvider) {
        super(valueProvider);
    }

    /**
     * Creates a CheckBoxSelectionModel with a custom appearance instance.
     *
     * @param appearance the appearance that should be used to render and update the checkbox
     */
    public CsiCheckboxImportSelectionModel(CheckBoxColumnAppearance appearance) {
        super(appearance);
    }

    /**
     * Creates a CheckBoxSelectionModel with a custom ValueProvider and appearance.
     *
     * @param valueProvider the ValueProvider to use when constructing a ColumnConfig
     * @param appearance the appearance that should be used to render and update the checkbox
     */
    public CsiCheckboxImportSelectionModel(ValueProvider<M, M> valueProvider, final CheckBoxColumnAppearance appearance) {
        super(valueProvider, appearance);
    }

    @Override
    protected void handleHeaderClick(HeaderClickEvent eventIn) {
        int myColumnIndex = eventIn.getColumnIndex();
        boolean left = eventIn.getEvent().getButton() == Event.BUTTON_LEFT;

        if (left && (0 == myColumnIndex)) {

            super.handleHeaderClick(eventIn);
        }
    }

    //
    // Routine for selecting multiple rows using
    // the shift key with the left mouse button
    //
//    private void selectGroup(int myRowIndex) {
//
//        int myFirst = Math.min(myRowIndex, _lastRowSelected);
//        int myLast = Math.max(myRowIndex, _lastRowSelected);
//
//        select(myFirst, myLast, true);
//    }

    //
    // Fixes problem of double-selecting
    //
    @Override
    protected void doMultiSelect(List<M> modelsIn, boolean keepExistingIn, boolean suppressEventIn) {

        if (locked) return;

        boolean myChange = false;

        boolean isGrouped = grid.getView() != null && grid.getView() instanceof GroupingView;
        if(isGrouped){
            ((GroupingView) grid.getView()).cacheTableRows();
        }

        if (!keepExistingIn && (selected.size() != 0)) {

            myChange = true;
            doDeselect(new ArrayList<M>(selected), true);
        }

        for (M myModel : modelsIn) {

            boolean isSelected = isSelected(myModel);

            if (!suppressEventIn && !isSelected) {

                BeforeSelectionEvent<M> evt = BeforeSelectionEvent.fire(this, myModel);

                if (evt != null && evt.isCanceled()) {

                    continue;
                }
            }
            //
            // IMPORTANT: Only select item if not previously selected
            //
            if (!isSelected) {

                myChange = true;
                lastSelected = myModel;
                selected.add(myModel);
                setLastFocused(lastSelected);
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
    public void onRowMouseDown(RowMouseDownEvent event) {
        boolean left = event.getEvent().getButton() == 1;
        if (left && event.getColumnIndex() == this.grid.getColumnModel().getColumns().indexOf(this.getColumn())) {
            M model = this.listStore.get(event.getRowIndex());
            if (model != null) {
                if (this.isSelected(model)) {
                    this.deselect(model);
                } else {
                    this.select(model, true);
                }
            }
        }
    }

    @Override
    public void onRowClick(RowClickEvent event) {

    }

    @Override
    public void deselectAll() {
        boolean isGrouped = grid.getView() != null && grid.getView() instanceof GroupingView;

        if(isGrouped){
            ((GroupingView) grid.getView()).cacheTableRows();
        }

        doDeselect(new ArrayList<M>(selected), false);

        if(isGrouped){
            ((GroupingView) grid.getView()).invalidateTableRows();
        }
    }
}

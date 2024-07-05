package csi.client.gwt.widget.gxt.grid;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.event.RowClickEvent;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;

/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/

public class CsiCheckboxSingleSelectionModel<M> extends CheckBoxSelectionModel<M> {

    private int _lastRowSelected = -1;
    
    /**
     * Creates a CheckBoxSelectionModel that will operate on the row itself. To customize the row
     * it is acting on, use a constructor that lets you specify a ValueProvider, to customize how
     * each row is drawn, use a constructor that lets you specify an appearance instance.
     */
    public CsiCheckboxSingleSelectionModel() {
      super();
    }

    /**
     * Creates a CheckBoxSelectionModel with a custom ValueProvider instance.
     *
     * @param valueProvider the ValueProvider to use when constructing a ColumnConfig
     */
    public CsiCheckboxSingleSelectionModel(ValueProvider<M, M> valueProvider) {
      super(valueProvider);
    }

    /**
     * Creates a CheckBoxSelectionModel with a custom appearance instance.
     *
     * @param appearance the appearance that should be used to render and update the checkbox
     */
    public CsiCheckboxSingleSelectionModel(CheckBoxColumnAppearance appearance) {
      super(appearance);
    }

    /**
     * Creates a CheckBoxSelectionModel with a custom ValueProvider and appearance.
     *
     * @param valueProvider the ValueProvider to use when constructing a ColumnConfig
     * @param appearance the appearance that should be used to render and update the checkbox
     */
    public CsiCheckboxSingleSelectionModel(ValueProvider<M, M> valueProvider, final CheckBoxColumnAppearance appearance) {
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
    private void selectGroup(int myRowIndex) {
        
        int myFirst = Math.min(myRowIndex, _lastRowSelected);
        int myLast = Math.max(myRowIndex, _lastRowSelected);
        
        select(myFirst, myLast, true);
    }
    
    @Override
    protected void onRowClick(RowClickEvent event) {
      if (event.getColumnIndex() == grid.getColumnModel().getColumns().indexOf(getColumn())) {
        return;
      }
    }
    
    @Override
    protected void onRowMouseDown(RowMouseDownEvent event) {
      boolean left = event.getEvent().getButton() == Event.BUTTON_LEFT;

      if (left && event.getColumnIndex() == grid.getColumnModel().getColumns().indexOf(getColumn())) {
        M model = listStore.get(event.getRowIndex());
        if (model != null) {
          if (isSelected(model)) {
            deselect(model);
          } else {
            select(model, true);
          }
        }
      } 
    }

    //
    // Fixes problem of double-selecting
    //
    @Override
    protected void doMultiSelect(List<M> modelsIn, boolean keepExistingIn, boolean suppressEventIn) {
        
        if (locked) return;
        
        boolean myChange = false;
        
        if (!keepExistingIn && (0 < selected.size())) {
            
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

        if (myChange && !suppressEventIn) {
            
            fireSelectionChange();
        }
    }
}

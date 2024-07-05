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
package csi.client.gwt.viz.shared.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.widget.core.client.event.CellSelectionEvent;

import csi.client.gwt.widget.boot.CsiModal;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.NullValueDefinition;
import csi.server.common.model.filter.ValueDefinition;
import csi.server.common.enumerations.OperandCardinality;
import csi.server.common.enumerations.RelationalOperator;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RelationalOperatorCell extends ComboBoxCell<RelationalOperator> {
    private ListStore<FilterExpression> gridStore;
    private ValueProvider<FilterExpression, ValueDefinition> valueDefinitionValueProvider;
    private ValueProvider<FilterExpression, Boolean> isSelectionValueProvider;
    private ValueUpdater<RelationalOperator> valueUpdater;

//    private List<RelationalOperator> regularList = new ArrayList<RelationalOperator>();
    private List<RelationalOperator> regularList = RelationalOperator.getFullOperatorList();
    private List<RelationalOperator> selectionFilterList = new ArrayList<RelationalOperator>();

    public RelationalOperatorCell(ListStore<FilterExpression> gridStoreIn,
            ValueProvider<FilterExpression, ValueDefinition> valueDefinitionValueProviderIn,
            ValueProvider<FilterExpression, Boolean> isSelectionValueProviderIn, int widthIn) {
        super(new ListStore<RelationalOperator>(new ModelKeyProvider<RelationalOperator>() {
            @Override
            public String getKey(RelationalOperator item) {
                return item.name();
            }
        }), new LabelProvider<RelationalOperator>() {
            @Override
            public String getLabel(RelationalOperator item) {
            	if(item == null){
            		return "";
            	} 
            	RelationalOperatorLabelProvider labelProvider = new RelationalOperatorLabelProvider();
                return labelProvider.getLabel(item);
            }
        });
        this.gridStore = gridStoreIn;
        this.valueDefinitionValueProvider = valueDefinitionValueProviderIn;
        this.isSelectionValueProvider = isSelectionValueProviderIn;
        setWidth(Math.max(110, (widthIn - 20)));
        selectionFilterList.add(RelationalOperator.INCLUDED);
        selectionFilterList.add(RelationalOperator.EXCLUDED);

        setTriggerAction(TriggerAction.ALL);
        setHideTrigger(true);
        setAllowBlank(false);
        setForceSelection(true);
        setSelectOnFocus(true);

        addSelectionHandler();
        
   		getStore().clear();
        getStore().addAll(selectionFilterList);
		getStore().addAll(regularList);
    }

    private void addSelectionHandler() {
        this.addSelectionHandler(new SelectionHandler<RelationalOperator>() {
            @Override
            public void onSelection(SelectionEvent<RelationalOperator> e) {
                // If the change to the field is incompatible with the current value definition, we set the value definition to null.
                CellSelectionEvent<RelationalOperator> event = (CellSelectionEvent<RelationalOperator>) e;

                FilterExpression fe = RelationalOperatorCell.this.gridStore.findModelWithKey(event.getContext().getKey().toString());
                Store<FilterExpression>.Record record = RelationalOperatorCell.this.gridStore.getRecord(fe);
                ValueDefinition vd = record.getValue(RelationalOperatorCell.this.valueDefinitionValueProvider);
                if (vd != null) {
                    if (vd.getCardinality() != event.getSelectedItem().getCardinality() || vd.getCardinality() == OperandCardinality.NONE) {
                        record.addChange(RelationalOperatorCell.this.valueDefinitionValueProvider, new NullValueDefinition());
                    }
                }
                // Note: Not making this call leaves the cursor in the edit field and other cells that depend on the
                // current value of this cell will fail to reflect the right value (and the record change marker doesn't
                // show upon selection).
                valueUpdater.update(event.getSelectedItem());
            }
        });
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, RelationalOperator value, NativeEvent event, ValueUpdater<RelationalOperator> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        this.valueUpdater = valueUpdater;
        if (BrowserEvents.MOUSEOVER.equals(event.getType())) {

        } else if (BrowserEvents.CLICK.equals(event.getType())) {
            super.onTriggerClick(context, (XElement)parent.cast(), event, value, valueUpdater);
            final FilterExpression fe = gridStore.findModelWithKey(context.getKey().toString());
            final CsiDataType dataType;
            if (fe.isSelectionFilter()) {
                dataType = CsiDataType.Integer;
            } else {
                dataType = fe.getFieldDef().getValueType();
            }

            // TODO: establish selection list

            getStore().removeFilters();
            getStore().addFilter(new StoreFilter<RelationalOperator>() {
                @Override
                public boolean select(Store<RelationalOperator> store, RelationalOperator parent, RelationalOperator item) {
                	boolean isSelectionOperator = selectionFilterList.contains(item);
                	boolean isSelectionFilter = fe.isSelectionFilter(); 
               		if (isSelectionOperator && isSelectionFilter) {
               			return true;
           			} else if (isSelectionOperator && !isSelectionFilter) {
           				return false;
           			} else if (!isSelectionOperator && isSelectionFilter) { 
           				return false;
           			} else {
           				return item.isApplicable(dataType);
           			}
                }
            });

            // TODO:

        }
    }
}
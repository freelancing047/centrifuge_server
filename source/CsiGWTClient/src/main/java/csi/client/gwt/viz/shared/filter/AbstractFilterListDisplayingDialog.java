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

import java.util.Comparator;
import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiField;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;

import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.KnowsParent;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterDefinition;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.OperandTypeAndValue;
import csi.server.common.model.filter.ScalarValueDefinition;
import csi.server.common.model.filter.ValueDefinition;
import csi.server.common.util.StringUtil;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractFilterListDisplayingDialog extends WatchingParent implements KnowsParent {

    protected abstract Dialog getDialog();

    private String dataViewUuid;
    private boolean isInitialized = false;
    private boolean initialized = false;

    protected Dialog dialog = null;

    @UiField(provided = true)
    SimpleComboBox<Filter> availableFilters;

    public AbstractFilterListDisplayingDialog(CanBeShownParent parentIn, String dataViewUuid) {
        super(parentIn);
        this.dataViewUuid = dataViewUuid;

        availableFilters = new SimpleComboBox<Filter>(new LabelProvider<Filter>() {

            @Override
            public String getLabel(Filter item) {
                return item.getName();
            }
        });
        availableFilters.getStore().addSortInfo(new StoreSortInfo<Filter>(new Comparator<Filter>() {

            @Override
            public int compare(Filter o1, Filter o2) {
                return o1.getName().compareTo(o2.getName());
            }
        }, SortDir.ASC));

        availableFilters.setTriggerAction(TriggerAction.ALL);
        availableFilters.setForceSelection(true);
        availableFilters.setEditable(false);

        availableFilters.addValueChangeHandler(new ValueChangeHandler<Filter>() {

            @Override
            public void onValueChange(ValueChangeEvent<Filter> event) {
                onAvailableFilterValueChange(event.getValue());
            }
        });
        availableFilters.addSelectionHandler(new SelectionHandler<Filter>() {

            @Override
            public void onSelection(SelectionEvent<Filter> event) {
                onAvailableFilterValueChange(event.getSelectedItem());
            }
        });
        dialog = getDialog();
        dialog.hideOnCancel();
    }

    public SimpleComboBox<Filter> getAvailableFilters() {
        return availableFilters;
    }

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    /**
     * Called when the available filters value has changed.
     * @param filter Currently selected filter.
     */
    protected abstract void onAvailableFilterValueChange(Filter filter);

    protected void populateAvailableFilters() {
        availableFilters.getStore().clear();
        List<Filter> filters = DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getFilters();
//        adjustFilter(filters, false);
        availableFilters.getStore().addAll(filters);
        onFilterPopulation(filters);
    }

    /**
     * Called after available filter list is loaded.
     * @param result Filters available
     */
    protected abstract void onFilterPopulation(List<Filter> result);

    @Override
    public void show() {

        if (!initialized) {

            // Get known filters.
            populateAvailableFilters();
        }
        if (null != getParent()) {

            getParent().hide();
        }
        dialog.show();
    }

    @Override
    public void hide() {

        dialog.hide();
    }

    @Override
    public void destroy() {

        if (null != getParent()) {

            getParent().show();
        }
        dialog.hide();
    }

   public abstract Filter getSelectedFilter();

   protected List<Filter> adjustFilter(List<Filter> filterListIn, boolean toSqlIn) {
      if ((isInitialized == toSqlIn) && (filterListIn != null) && !filterListIn.isEmpty()) {
         int howMany = availableFilters.getStore().size();

         for (int i = 0; i < howMany; i++) {
            Filter filter = availableFilters.getStore().get(i);
            FilterDefinition definition = filter.getFilterDefinition();
            List<FilterExpression> expressionList = (definition == null) ? null : definition.getFilterExpressions();

            if (expressionList != null) {
               for (FilterExpression expression : expressionList) {
                  if ((expression != null) && RelationalOperator.LIKE.equals(expression.getOperator())) {
                     ValueDefinition valueDef = expression.getValueDefinition();
                     OperandTypeAndValue<?> value = (valueDef instanceof ScalarValueDefinition)
                                                       ? ((ScalarValueDefinition) valueDef).getValue()
                                                       : null;

                     if (value != null) {  //TODO: only for OperandType.SCALAR, add columns
                        if (toSqlIn) {
                           ((ScalarValueDefinition) valueDef).getValue().setValue(StringUtil.patternToSql((String) value.getValue()));
                        } else {
                           ((ScalarValueDefinition) valueDef).getValue().setValue(StringUtil.patternFromSql((String) value.getValue()));
                        }
                     }
                  }
               }
            }
         }
         isInitialized = !isInitialized;
      }
      return filterListIn;
   }
}

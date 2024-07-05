/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.chart.settings;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.visualization.AbstractMeasureDefinition;
import csi.server.util.sql.api.AggregateFunction;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 */
public class AggregateFunctionComboCell extends ComboBoxCell<AggregateFunction> {

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private ListStore<? extends AbstractMeasureDefinition> gridStore;
    private ValueUpdater<AggregateFunction> valueUpdater;

    AggregateFunctionComboCell(ListStore<? extends AbstractMeasureDefinition> gridStore) {

        super(new ListStore<>(Enum::name), item -> {
            String label = ""; //$NON-NLS-1$
            switch (item) {
                case COUNT:
                    label = i18n.count(); //$NON-NLS-1$
                    break;
                case STD_DEV:
                    label = i18n.stdDev(); //$NON-NLS-1$
                    break;
                case VARIANCE:
                    label = i18n.variance(); //$NON-NLS-1$
                    break;
                case MINIMUM:
                    label = i18n.min(); //$NON-NLS-1$
                    break;
                case MAXIMUM:
                    label = i18n.max(); //$NON-NLS-1$
                    break;
                case SUM:
                    label = i18n.sum(); //$NON-NLS-1$
                    break;
                case AVERAGE:
                    label = i18n.average(); //$NON-NLS-1$
                    break;
                case COUNT_DISTINCT:
                    label = i18n.countDistinct(); //$NON-NLS-1$
                    break;
                case UNITY:
                    label = i18n.unity(); //$NON-NLS-1$
                    break;
                case MEDIAN:
                    label = i18n.median(); //$NON-NLS-1$
                    break;
            }
            return label;
        });

        this.gridStore = gridStore;
        List<AggregateFunction> list = Lists.newArrayList(AggregateFunction.values());
        list.sort(Comparator.comparing(AggregateFunction::getLabel));
        setWidth(120);
        getStore().addAll(list);
        setTriggerAction(TriggerAction.ALL);
        setAllowBlank(false);
        setForceSelection(true);

        addSelectionHandler();
    }

    private void addSelectionHandler() {
        this.addSelectionHandler(event -> {
            // Note: Not making this call leaves the cursor in the edit field and other cells that depend on the
            // current value of this cell will fail to reflect the right value (and the record change marker doesn't
            // show upon selection).
            valueUpdater.update(event.getSelectedItem());
        });
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, AggregateFunction value, NativeEvent event,
                               ValueUpdater<AggregateFunction> valueUpdater) {

        this.valueUpdater = valueUpdater;

        String eventType = event.getType();
        if ("click".equals(eventType)) {
            AbstractMeasureDefinition acd = gridStore.findModelWithKey(context.getKey().toString());
            CsiDataType dataType = acd.getFieldDef().getValueType();
            final Set<AggregateFunction> allowed = new HashSet<>(AggregateFunction.forType(dataType));
            getStore().removeFilters();
            getStore().addFilter((store, parent1, item) -> allowed.contains(item));
        }
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }
}

/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.widget.gxt.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.shared.core.util.HasComparator;
import csi.shared.core.util.HasLabel;

/**
 * Creates combo-box-cell that shows strings.
 * @author Centrifuge Systems, Inc.
 *
 */
public class ComboBoxFactory {

    public static ComboBoxCell<String> cellFrom(List<String> values) {
        List<String> list = new ArrayList<String>(values);
        Collections.sort(list);
        ListStore<String> store = new ListStore<String>(new ModelKeyProvider<String>() {

            public String getKey(String item) {
                return item;
            }

            ;
        });
        store.addAll(list);
        store.addSortInfo(new StoreSortInfo<String>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        }, SortDir.ASC));
        ComboBoxCell<String> cell = new SelectingComboBoxCell<String>(store, new LabelProvider<String>() {

            @Override
            public String getLabel(String item) {
                return item;
            }
        });
        cell.setTriggerAction(TriggerAction.ALL);
        cell.setAllowBlank(false);
        cell.setForceSelection(true);
        return cell;
    }

    public static ComboBoxCell<String> cellFrom(String[] values) {
        return cellFrom(Lists.newArrayList(values));
    }

    public static <T extends Enum<?>> ComboBoxCell<String> cellFrom(T[] enums) {
        List<String> values = new ArrayList<String>();
        for (T e : enums) {
            values.add(e.name());
        }
        return cellFrom(values);
    }

    @SuppressWarnings("unchecked")

    public static <T extends Enum<?>> SelectingComboBoxCell<T> typedEnumCellFromWithWidth(T[] enums, final LabelProvider<T> labelProvider, int width) {
        SelectingComboBoxCell<T> combo = typedEnumCellFrom(enums, labelProvider);
        combo.setWidth(width);
        return combo;
    }


    /**
     * Creates a combobox from an enumeration, if labelProvider is null, attempts to use HasLabel interface, if present
     *
     * @param enums the enumeration you wish to use
     * @param labelProvider Used for i18n
     * @return
     */
    public static <T extends Enum<?>> SelectingComboBoxCell<T> typedEnumCellFrom(T[] enums, final LabelProvider<T> labelProvider) {
        List<T> list = Lists.newArrayList(enums);

        if (enums[0] instanceof HasComparator<?>) {
            Collections.sort(list, ((HasComparator<T>) enums[0]).getComparator());
        }
        final ListStore<T> store = new ListStore<T>(item -> item.name());


        store.addAll(list);

        SelectingComboBoxCell<T> cell = new SelectingComboBoxCell<T>(store, new LabelProvider<T>() {

            @Override
            public String getLabel(T item) {

                if (labelProvider != null) {
                    return labelProvider.getLabel(item);
                }
                if (item instanceof HasLabel) {
                    return ((HasLabel) item).getLabel();
                } else {
                    return item.name();
                }
            }
        });
        cell.setTriggerAction(TriggerAction.ALL);
        cell.setAllowBlank(false);
        cell.setForceSelection(true);
        return cell;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> ComboBox<T> typedEnumFrom(T[] enums, final LabelProvider<T> labelProvider) {
        List<T> list = Lists.newArrayList(enums);
        if (enums[0] instanceof HasComparator<?>) {
            Collections.sort(list, ((HasComparator<T>) enums[0]).getComparator());
        }
        final ListStore<T> store = new ListStore<T>(new ModelKeyProvider<T>() {

            public String getKey(T item) {
                return item.name();
            }

            ;
        });
        store.addAll(list);

        ComboBox<T> cell = new ComboBox<T>(store, new LabelProvider<T>() {

            @Override
            public String getLabel(T item) {
                if (labelProvider != null) {
                    return labelProvider.getLabel(item);
                }
                if (item instanceof HasLabel) {
                    return ((HasLabel) item).getLabel();
                } else {
                    return item.name();
                }
            }
        });
        cell.setTriggerAction(TriggerAction.ALL);
        cell.setAllowBlank(false);
        cell.setForceSelection(true);
        return cell;
    }
}

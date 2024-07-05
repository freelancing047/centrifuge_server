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
package csi.client.gwt.widget.combo_boxes;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.model.column.ColumnDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnDefComboBox extends ComboBox<ColumnDef> {

    static interface ColumnDefProperty extends PropertyAccess<ColumnDef> {

        ModelKeyProvider<ColumnDef> uuid();
    }

    private static ColumnDefProperty columnDefProperty = GWT.create(ColumnDefProperty.class);

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}</span>")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    public ColumnDefComboBox() {
        super(new ListStore<ColumnDef>(columnDefProperty.uuid()), new LabelProvider<ColumnDef>() {

            @Override
            public String getLabel(ColumnDef item) {
                return item.getColumnName();
            }
        }, new AbstractSafeHtmlRenderer<ColumnDef>() {

            public SafeHtml render(ColumnDef item) {
                SafeUri dataUri = FieldDefUtils.getColumnDataTypeImage(item).getSafeUri();
                return comboBoxTemplates.html(dataUri, item.getColumnName());
            }
        });
        addStyleName("string-combo-style");
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }

    public int getItemCount() {
        return getStore().size();
    }

    public int getSelectedIndex() {
        return getStore().indexOf(getCurrentValue());
    }

    public void setSelectedIndex(int i) {
        setValue(getStore().get(i));
    }

}

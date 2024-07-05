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

import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.server.common.model.SqlTableDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TableDefComboBox extends ComboBox<SqlTableDef> {

    static interface TableDefProperty extends PropertyAccess<SqlTableDef> {

        ModelKeyProvider<SqlTableDef> uuid();
    }

    private static TableDefProperty tableDefProperty = GWT.create(TableDefProperty.class);

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}</span>")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    public TableDefComboBox() {
        super(new ListStore<SqlTableDef>(tableDefProperty.uuid()), new LabelProvider<SqlTableDef>() {

            @Override
            public String getLabel(SqlTableDef item) {
                return item.getTableName();
            }
        }, new AbstractSafeHtmlRenderer<SqlTableDef>() {

            public SafeHtml render(SqlTableDef item) {
                SafeUri dataUri = DataSourceClientUtil.get(item, false).getSafeUri();
                return comboBoxTemplates.html(dataUri, item.getTableName());
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

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
package csi.client.gwt.viz.chart.settings;

import java.util.Comparator;

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
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.model.visualization.AbstractAttributeDefinition;
import csi.server.common.model.visualization.chart.SortDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SortDefinitionComboBox extends ComboBox<SortDefinition> {

    static interface FieldDefProperty extends PropertyAccess<SortDefinition> {

        ModelKeyProvider<SortDefinition> uuid();
    }

    private static FieldDefProperty fieldDefProperty = GWT.create(FieldDefProperty.class);

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span class=\"attributeComboType\">[{type}]</span>&nbsp;"
                + "<img width=\"16\" height=\"15\" src=\"{fieldUri}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\">&nbsp;&nbsp;{name}")
        SafeHtml field(SafeUri fieldUri, SafeUri dataUri, String name, String type);

        @XTemplate("<span class=\"attributeComboType\">[{measure}]</span>&nbsp;{countStar}")
        SafeHtml countStar(String measure, String countStar);
    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    public SortDefinitionComboBox() {
        super(new ListStore<SortDefinition>(fieldDefProperty.uuid()), new LabelProvider<SortDefinition>() {

            @Override
            public String getLabel(SortDefinition item) {
                return item.getDisplayName();
            }
        }, new AbstractSafeHtmlRenderer<SortDefinition>() {

            public SafeHtml render(SortDefinition item) {
            	
            	AttributeDefinitionLabelProvider labelProvider = new AttributeDefinitionLabelProvider();
            	
                if (item.isCountStar()) {
                    return comboBoxTemplates.countStar(CentrifugeConstantsLocator.get().measure(), CentrifugeConstantsLocator.get().countStar());
                } else {
                    AbstractAttributeDefinition cad = item.getChartAttributeDefinition();
                    SafeUri fieldUri = FieldDefUtils.getFieldTypeImage(cad.getFieldDef().getFieldType())
                            .getSafeUri();
                    SafeUri dataUri = FieldDefUtils.getDataTypeImage(cad.getDerivedType()).getSafeUri();
                    return comboBoxTemplates.field(fieldUri, dataUri, cad.getComposedName(), labelProvider.getLabel(cad));
                }
            }
        });
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(300);

        getStore().addSortInfo(new StoreSortInfo<SortDefinition>(new Comparator<SortDefinition>() {

            @Override
            public int compare(SortDefinition o1, SortDefinition o2) {
                return o1.getTypeQualifiedDisplayName().compareTo(o2.getTypeQualifiedDisplayName());
            }
        }, SortDir.ASC));
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

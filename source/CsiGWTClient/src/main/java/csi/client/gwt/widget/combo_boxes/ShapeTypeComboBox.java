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
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.server.common.graphics.shapes.ShapeType;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ShapeTypeComboBox extends ComboBox<ShapeType> {

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT
            .create(ComboBoxTemplates.class);

    public ShapeTypeComboBox() {
        super(new ListStore<ShapeType>(new ModelKeyProvider<ShapeType>(){

            @Override
            public String getKey(ShapeType item) {
                return item.name();
            }}),

        new LabelProvider<ShapeType>() {

            @Override
            public String getLabel(ShapeType item) {
                return item.toString();
            }
        });
        addStyleName("string-combo-style");
        setTypeAhead(false);
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
        

        for (ShapeType df : ShapeType.values()) {
            getStore().add(df);
        }
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

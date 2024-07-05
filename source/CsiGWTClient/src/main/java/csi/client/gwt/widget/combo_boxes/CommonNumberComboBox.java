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

import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.widget.cells.ComboBoxEditCell;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CommonNumberComboBox extends ComboBox<String> {
    private final int COMBO_BOX_WIDTH = 75;

    public CommonNumberComboBox() {
        super(
            new ListStore<String>(new ModelKeyProvider<String>(){

                @Override
                public String getKey(String item) {
                    return item.toString();
                }}),
            new LabelProvider<String>() {

                @Override
                public String getLabel(String item) {
                    return item;
                }
            }
        );

        //TODO: externalize i18n? 
        getStore().add("10");
        getStore().add("100");
        getStore().add("250");
        getStore().add("500");
        getStore().add("1000");
        setForceSelection(true);
        setSelectedIndex(1);
        initialize();
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
    
    public String getCurrentCellText() {
        
        return ((ComboBoxEditCell<String>)getCell()).getSelectionText();
    }

    private void initialize() {
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(COMBO_BOX_WIDTH);
        getStore().applySort(true);
    }

}

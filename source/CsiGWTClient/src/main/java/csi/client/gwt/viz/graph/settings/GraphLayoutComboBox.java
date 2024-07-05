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
package csi.client.gwt.viz.graph.settings;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.core.visualization.graph.GraphLayout;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class GraphLayoutComboBox extends ComboBox<GraphLayout> {

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    public GraphLayoutComboBox() {
        super(new ListStore<GraphLayout>(new ModelKeyProvider<GraphLayout>() {

            @Override
            public String getKey(GraphLayout item) {
                return item.getName();
            }
        }), new LabelProvider<GraphLayout>() {

            @Override
            public String getLabel(GraphLayout graphLayout) {
                switch (graphLayout) {
                case circular:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_circular();
                case centrifuge:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_centrifuge();
                case forceDirected:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_force_directed();
                case treeNodeLink:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_linear_hierarchy();
                case treeRadial:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_radial();
                case scramble:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_scramble_and_place();
                case applyForce:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_apply_force();
                case grid:
                    return CentrifugeConstantsLocator.get().menuKeyConstants_grid();
                default:
                    return "";
                }
            }
        });
        addStyleName("string-combo-style");
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
        getStore().remove(GraphLayout.scramble);
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

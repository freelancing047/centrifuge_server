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
package csi.client.gwt.edit_sources.right_panel;

import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.edit_sources.center_panel.shapes.WienzoComposite;
import csi.client.gwt.events.GridClickEvent;
import csi.client.gwt.events.GridClickEventHandler;
import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.operator.OpJoinType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class JoinMappingEditor extends ColumnColumnMappingEditor {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public JoinMappingEditor(WienzoComposite parentIn, DataSourceEditorModel modelIn) {

        super(parentIn, modelIn);

        _parent = parentIn;
        _dso = _parent.getDso();
        _model = modelIn;

        getPanelTitle().setText(getDso().getName());

        // Join type
        getDropDown().setVisible(true);
        for (OpJoinType opjoin : OpJoinType.values()) {
            getDropDown().addItem(opjoin.getLabel(), opjoin.name());
        }
        getDropDown().setSelectedValue(getDso().getJoinType().name());
        initAll();
        addHandlers();
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();

        getDropDown().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                getDso().setJoinType(OpJoinType.valueOf(getDropDown().getSelectedValue()));
                _model.setChanged();
                _parent.updateInfo();
                
            }
        });
    }
    
}

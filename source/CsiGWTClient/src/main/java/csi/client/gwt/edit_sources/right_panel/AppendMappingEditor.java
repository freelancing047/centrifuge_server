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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                                     Public Methods                                     //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

public class AppendMappingEditor extends ColumnColumnMappingEditor {

    public AppendMappingEditor(WienzoComposite parentIn, DataSourceEditorModel modelIn) {

        super(parentIn, modelIn);

        getPanelTitle().setText(getDso().getName());

        // Append type
        getDropDown().setVisible(true);
        getDropDown().addItem(_constants.unionType_Unique());
        getDropDown().addItem(_constants.unionType_All());

        getDropDown().setSelectedValue(getDso().getAppendAll()
                ? _constants.unionType_All()
                : _constants.unionType_Unique());
        initAll("||", "mapped together");
        addHandlers();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void addHandlers() {
        super.addHandlers();

        getDropDown().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                getDso().setAppendAll(1 == getDropDown().getSelectedIndex());
                _model.setChanged();
                _parent.updateInfo();
            }
        });
    }
}

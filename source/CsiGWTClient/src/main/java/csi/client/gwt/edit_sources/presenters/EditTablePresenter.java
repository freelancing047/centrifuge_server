package csi.client.gwt.edit_sources.presenters;

import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.AdHocDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 2/25/2019.
 */
public class EditTablePresenter extends EditAdHocDataPresenter {


    List<String> _fieldIdList = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public EditTablePresenter(AdHocDataSource requestIn, TransferCompleteEventHandler handlerIn) {

        super(requestIn, handlerIn);
    }

    //
    // Request the list of required fields from the server
    //
    @Override
    public void retrieveRequiredFieldList(VortexEventHandler<List<String>> handlerIn) {

        handlerIn.onSuccess(getFieldIds());
    }

    //
    // Request the list of required fields from the server
    //
    @Override
    public void retrieveRequiredCoreFieldList(VortexEventHandler<List<String>> handlerIn) {

        handlerIn.onSuccess(getFieldIds());
    }

    List<String> getFieldIds() {

        if (null == _fieldIdList) {

            for (FieldDef myField : _resource.getFieldList()) {

                _fieldIdList.add(myField.getUuid());
            }
        }
        return _fieldIdList;
    }
}

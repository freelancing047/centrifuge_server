package csi.client.gwt.edit_sources.right_panel;

import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.operator.OpMapItem;

/**
 * Created by centrifuge on 3/28/2016.
 */
////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                                     Public Methods                                     //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

public class ColumnMappingSet extends SelectionPair<ColumnDisplay, ColumnDisplay> {

    OpMapItem _result;

    public ColumnMappingSet(String idIn, ColumnDisplay leftDisplayIn, ColumnDisplay rightDisplayIn, OpMapItem resultIn) {

        super(idIn, leftDisplayIn, rightDisplayIn);

        _result = resultIn;
        if (null != _result) {

            CsiDataType myCurrentType = _result.getCastToType();
            ComparingToken myCurrentToken = _result.getComparingToken();

            if (null != myCurrentType) {

                setCastToType(myCurrentType);
            }
            if (null != myCurrentToken) {

                setComparingToken(myCurrentToken);
            }
        }
    }

    public OpMapItem getResult() {

        return _result;
    }
}

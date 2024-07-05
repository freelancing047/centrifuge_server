package csi.client.gwt.dataview.fieldlist.editor.derived;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.DisplayableObject;

/**
 * Created by centrifuge on 3/16/2015.
 */
public class ParameterSelectionItem extends DisplayableObject implements ExtendedDisplayInfo {

    private QueryParameterDef _parameter;

    public ParameterSelectionItem() {

        super(DisplayMode.NORMAL);
    }

    public ParameterSelectionItem(QueryParameterDef parameterIn) {

        super(DisplayMode.NORMAL);
        _parameter = parameterIn;
    }

    public void setParameter(QueryParameterDef parameterIn) {

        _parameter = parameterIn;
    }

    public QueryParameterDef getParameter() {

        return _parameter;
    }

    public static List<ParameterSelectionItem> getResetList(DataViewDef metaIn, CsiDataType dataTypeIn) {

        List<ParameterSelectionItem> myListOut = new ArrayList<ParameterSelectionItem>();

        if (null != dataTypeIn) {

            Collection<QueryParameterDef> myListIn = metaIn.getOrderedFullParameterList(dataTypeIn);

            if ((null != myListIn) && (0 < myListIn.size())) {

                for (QueryParameterDef myParameter : myListIn) {

                    myListOut.add(new ParameterSelectionItem(myParameter));
                }
            }
        }

        return myListOut;
    }

    public QueryParameterDef getDataField() {

        return _parameter;
    }

    public void resetFlags() {

        setDisplayMode(DisplayMode.NORMAL);
    }

    @Override
    public String getKey() {

        return _parameter.getLocalId();
    }

    @Override
    public String getParentString() {

        return null;
    }

    @Override
    public String getDisplayString() {

        return _parameter.getName();
    }

    @Override
    public String getTitleString() {

        return _parameter.getPrompt();
    }

    @Override
    public String getDescriptionString() {

        return _parameter.getDescription();
    }

    private static List<DataFieldSelectionItem> resetList(List<DataFieldSelectionItem> listIn) {

        for (DataFieldSelectionItem myItem : listIn) {

            myItem.resetFlags();
        }
        return listIn;
    }
}

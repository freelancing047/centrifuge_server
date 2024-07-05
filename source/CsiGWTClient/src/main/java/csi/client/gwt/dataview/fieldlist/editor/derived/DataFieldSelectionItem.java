package csi.client.gwt.dataview.fieldlist.editor.derived;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.model.FieldDef;
import csi.server.common.util.DisplayableObject;

/**
 * Created by centrifuge on 3/16/2015.
 */
public class DataFieldSelectionItem extends DisplayableObject implements ExtendedDisplayInfo {

    private FieldDef _dataField;

    public DataFieldSelectionItem() {

        super(DisplayMode.NORMAL);
    }

    public DataFieldSelectionItem(FieldDef dataFieldIn) {

        super(DisplayMode.NORMAL);
        _dataField = dataFieldIn;
    }

    public static List<DataFieldSelectionItem> getResetList(FieldListAccess dataModelIn, CsiDataType dataTypeIn) {

        List<DataFieldSelectionItem> myListOut = new ArrayList<DataFieldSelectionItem>();

        if (null != dataTypeIn) {

//            Collection<FieldDef> myListIn = dataModelIn.getOrderedNonDependentFieldDefs(dataTypeIn);
            Collection<FieldDef> myListIn = dataModelIn.getAlphaOrderedFieldSet(dataTypeIn);

            if ((null != myListIn) && (0 < myListIn.size())) {

                for (FieldDef myDataField : myListIn) {

                    myListOut.add(new DataFieldSelectionItem(myDataField));
                }
            }
        }

        return myListOut;
    }

    public void setDataField(FieldDef dataFieldIn) {

        _dataField = dataFieldIn;
    }

    public FieldDef getDataField() {

        return _dataField;
    }

    public void resetFlags() {

        setDisplayMode(DisplayMode.NORMAL);
    }

    @Override
    public String getKey() {

        return _dataField.getLocalId();
    }

    @Override
    public String getParentString() {

        return null;
    }

    @Override
    public String getDisplayString() {

        return _dataField.getFieldName();
    }

    @Override
    public String getTitleString() {

        return null;
    }

    @Override
    public String getDescriptionString() {

        return null;
    }

    private static List<DataFieldSelectionItem> resetList(List<DataFieldSelectionItem> listIn) {

        for (DataFieldSelectionItem myItem : listIn) {

            myItem.resetFlags();
        }
        return listIn;
    }
}

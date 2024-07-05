package csi.client.gwt.mapper;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;

import java.util.List;

/**
 * Created by centrifuge on 2/20/2019.
 */
public class TypedDataMapper extends VerticalMappingEditorLayout {
    @Override
    protected List<? extends SelectionPair> createPreSelectionList() {
        return null;
    }

    @Override
    protected SelectionPair createMappingItem(String idIn, SelectionDataAccess leftSelectionIn, SelectionDataAccess rightSelectionIn, CsiDataType castToTypeIn, ComparingToken comparingTokenIn) {
        return null;
    }
}

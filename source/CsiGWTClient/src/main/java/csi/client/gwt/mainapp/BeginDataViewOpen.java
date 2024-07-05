package csi.client.gwt.mainapp;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.model.dataview.DataView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by centrifuge on 1/18/2019.
 */
public class BeginDataViewOpen {

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String _choiceDialogNewButtonString = _constants.openDataviewDialog_Choice_NewButtonString();
    private static String _choiceDialogCurrentButtonString = _constants.openDataviewDialog_Choice_CurrentButtonString();
    private static String _choiceDialogTitle = _constants.openDataviewDialog_Choice_DialogTitle();
    private static String _choiceInfoString = _constants.openDataviewDialog_Choice_InfoString(_choiceDialogNewButtonString, _choiceDialogCurrentButtonString);

    private static List<ButtonDef> myButtonList = Arrays.asList(

            new ButtonDef(_choiceDialogCurrentButtonString),
            new ButtonDef(_choiceDialogNewButtonString)
    );

    DataView _dataView = null;
    ResourceBasics _dataViewInfo = null;
    String _uuidIn = null;

    public BeginDataViewOpen(DataView dataViewIn) {

    }

    public BeginDataViewOpen(ResourceBasics dataViewInfoIn) {

    }

    public BeginDataViewOpen(String uuidInIn) {

    }
}

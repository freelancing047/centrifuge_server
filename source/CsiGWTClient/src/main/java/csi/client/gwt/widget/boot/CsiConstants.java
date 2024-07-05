package csi.client.gwt.widget.boot;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;


public class CsiConstants {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    
    public static final String txtStateReady = _constants.dataSourceEditor_StateReady(Dialog.txtSaveButton, "DataView");
    public static final String txtStateNoAppendMappings = _constants.dataSourceEditor_StateNoAppendMappings();
    public static final String txtStateUnusedSource = _constants.dataSourceEditor_StateUnusedSource();
    public static final String txtStateNoJoinMappings = _constants.dataSourceEditor_StateNoJoinMappings();
    public static final String txtStateUnsupportedFields = _constants.dataSourceEditor_StateUnsupportedFields();
    public static final String txtStateNoComponents = _constants.dataSourceEditor_StateNoComponents();
    public static final String txtStateDanglingComponents = _constants.dataSourceEditor_StateDanglingComponents();
    public static final String txtStateBrokenTree = _constants.dataSourceEditor_StateBrokenTree();
    public static final String txtStateEmptyTable = _constants.dataSourceEditor_StateEmptyTable();
    public static final String txtStateUnsupportedTypes = _constants.dataSourceEditor_StateUnsupportedTypes();
    public static final String txtStateNoMappedFields = _constants.dataSourceEditor_StateNoMappedFields(Dialog.txtMapButton);
}

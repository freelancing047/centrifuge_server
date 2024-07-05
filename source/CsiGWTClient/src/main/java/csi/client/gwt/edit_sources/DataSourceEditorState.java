package csi.client.gwt.edit_sources;

import csi.client.gwt.widget.boot.CsiConstants;
import csi.client.gwt.widget.boot.Dialog;


public enum DataSourceEditorState {

    READY(CsiConstants.txtStateReady, Dialog.txtSuccessColor, 0),
    XTRASOURCE(CsiConstants.txtStateUnusedSource, Dialog.txtSuccessColor, 1),
    NOAMAP(CsiConstants.txtStateNoAppendMappings, Dialog.txtWarningColor, 2),
    BADTREE(CsiConstants.txtStateBrokenTree, Dialog.txtErrorColor, 3),
    NOTREE(CsiConstants.txtStateNoComponents, Dialog.txtErrorColor, 4),
    XTRATREE(CsiConstants.txtStateDanglingComponents, Dialog.txtErrorColor, 5),
    NOJMAP(CsiConstants.txtStateNoJoinMappings, Dialog.txtErrorColor, 6),
    BADFIELD(CsiConstants.txtStateUnsupportedFields, Dialog.txtErrorColor, 7),
    EMPTYTABLE(CsiConstants.txtStateEmptyTable, Dialog.txtErrorColor, 8),
    BADTYPE(CsiConstants.txtStateUnsupportedTypes, Dialog.txtErrorColor, 9),
    NOFIELDS(CsiConstants.txtStateNoMappedFields, Dialog.txtErrorColor, 10);

    private String _label;
    private String _color;
    private int _ordinal;

    private DataSourceEditorState(String labelIn, String colorIn, int ordinalIn) {
        _label = labelIn;
        _color = colorIn;
        _ordinal = ordinalIn;
    }

    public String getColor() {
        
        return _color;
    }

    public String getLabel() {
        
        return _label;
    }

    public int getOrdinal() {
        
        return _ordinal;
    }
}

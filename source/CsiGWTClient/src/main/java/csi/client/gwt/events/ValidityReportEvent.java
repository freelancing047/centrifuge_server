package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class ValidityReportEvent extends BaseCsiEvent<ValidityReportEventHandler> {

    private Boolean _isValid = false;
    private Boolean _atReset = false;
    private Object _selection = null;

    public static final GwtEvent.Type<ValidityReportEventHandler> type = new GwtEvent.Type<ValidityReportEventHandler>();

    public ValidityReportEvent(boolean isValidIn) {

        _isValid = isValidIn;
    }

    public ValidityReportEvent(boolean isValidIn, boolean atResetIn) {

        _isValid = isValidIn;
        _atReset = atResetIn;
    }

    public ValidityReportEvent(boolean isValidIn, Object selectionIn) {
        
        _isValid = isValidIn;
        _selection = selectionIn;
        _atReset = (null == selectionIn);
    }

    public boolean getValidFlag() {

        return _isValid;
    }

    public boolean isValid() {

        return _isValid;
    }

    public boolean atReset() {

        return _atReset;
    }

    public Object getSelection() {
        
        return _selection;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ValidityReportEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ValidityReportEventHandler handler) {
        handler.onValidityReport(this);
    }
}

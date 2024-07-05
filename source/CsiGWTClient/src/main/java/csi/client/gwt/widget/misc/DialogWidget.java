package csi.client.gwt.widget.misc;

/**
 * 
 * Used when a widget needs to have open and close action
 * @ T : 
 */
public interface DialogWidget {
    
    /**
     * call when the dialog action button is pressed
     * @param callback
     */
    void onDialogClose(WidgetCallback callback);
    
    /**
     * call when the dialog cancel button is pressed
     * @param callback
     */
    void onDialogCancel(WidgetCallback callback);
    
    

}

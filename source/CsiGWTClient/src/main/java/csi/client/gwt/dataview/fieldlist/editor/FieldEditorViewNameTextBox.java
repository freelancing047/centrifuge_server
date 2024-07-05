package csi.client.gwt.dataview.fieldlist.editor;

import csi.client.gwt.widget.input_boxes.FilteredTextBox;

public class FieldEditorViewNameTextBox extends FilteredTextBox {

    /*****
     * This class was created to prevent empty string values from being accepted as a name for a new field. Changing the parent class "FilteredTextBox" will
     * affect many other classes. This is meant to contain the change.
     */


    @Override
    protected boolean checkValue(String stringIn) {
        return (((null == _rejectionMap)
                || ((!_ignoreException) && (null != _exception) && (_exception.equals(stringIn)))
                || (!_rejectionMap.containsKey(stringIn)))
                && !stringIn.trim().isEmpty());
    }

}

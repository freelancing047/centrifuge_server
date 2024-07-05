package csi.client.gwt.widget.display_list_widgets;

import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 3/2/2015.
 */
public class ObjectComponentLabel extends ComponentLabel {

    private final static String _defaultColor = Dialog.txtInfoColor;
    private final static String _disabledColor = Dialog.txtDisabledColor;
    private final static String _selectedColor = Dialog.txtInfoColor;
    private final static String _defaultBackground = Dialog.txtDefaultBackground;
    private final static String _disabledBackground = Dialog.txtDefaultBackground;
    private final static String _selectedBackground = Dialog.txtSelectedBackground;

    public ObjectComponentLabel(String textIn) {

        super(textIn);
    }

    public ObjectComponentLabel(String textIn, boolean selectedIn) {

        super(textIn, selectedIn);
    }

    public ObjectComponentLabel(String textIn, boolean enabledIn, boolean selectedIn) {

        super(textIn, enabledIn, selectedIn);
    }

    public void setColor() {

        if (_enabled) {

            if (_selected) {

                getElement().getStyle().setColor(_selectedColor);
                getElement().getStyle().setBackgroundColor(_selectedBackground);

            } else {

                getElement().getStyle().setColor(_defaultColor);
                getElement().getStyle().setBackgroundColor(_defaultBackground);
            }

        } else {

            getElement().getStyle().setColor(_disabledColor);
            getElement().getStyle().setBackgroundColor(_disabledBackground);
        }
    }
}

package csi.client.gwt.widget.display_list_widgets;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 3/2/2015.
 */
public class PromptComponentLabel extends ComponentLabel {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final static String _defaultColor = Dialog.txtErrorColor;
    private final static String _disabledColor = Dialog.txtPatternColor;
    private final static String _selectedColor = Dialog.txtErrorColor;
    private final static String _defaultBackground = Dialog.txtDefaultBackground;
    private final static String _disabledBackground = Dialog.txtDefaultBackground;
    private final static String _selectedBackground = Dialog.txtSelectedBackground;
    private final static String _text = _constants.sqlTokenTreeLabel_MissingValue();

    public PromptComponentLabel() {

        super(_text);
    }

    public PromptComponentLabel(boolean selectedIn) {

        super(_text, selectedIn);
    }

    public PromptComponentLabel(boolean enabledIn, boolean selectedIn) {

        super(_text, enabledIn, selectedIn);
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

    @Override
    public boolean isValid() {

        return false;
    }
}

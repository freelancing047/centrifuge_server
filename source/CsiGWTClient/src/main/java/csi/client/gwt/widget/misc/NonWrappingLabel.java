package csi.client.gwt.widget.misc;

import com.google.gwt.user.client.ui.HTML;

import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.display_list_widgets.CanBeSelected;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 7/20/2015.
 */
public class NonWrappingLabel extends HTML implements CanBeSelected {

    public final static int DEFAULT_COLOR = 0;
    public final static int DISABLED_COLOR = 1;
    public final static int SELECTED_COLOR = 2;
    public final static int DEFAULT_BACKGROUND = 3;
    public final static int DISABLED_BACKGROUND = 4;
    public final static int SELECTED_BACKGROUND = 5;

    private final static String _defaultColor = Dialog.txtLabelColor;
    private final static String _disabledColor = Dialog.txtDisabledColor;
    private final static String _selectedColor = Dialog.txtInfoColor;
    private final static String _defaultBackground = Dialog.txtDefaultBackground;
    private final static String _disabledBackground = Dialog.txtDefaultBackground;
    private final static String _selectedBackground = Dialog.txtSelectedBackground;

    private String[] _colorValues = {_defaultColor, _disabledColor, _selectedColor,
                                        _defaultBackground, _disabledBackground, _selectedBackground};

    boolean _enabled = true;
    boolean _selected = false;
    boolean _selectable = false;

    public NonWrappingLabel() {

        setText("");
    }

    public NonWrappingLabel(String textIn) {

        setText(textIn);
    }

    public void setText(String textIn) {

        setHTML(encode(textIn));
        setColor();
    }

    public void setColor(int indexIn, String colorIn) {

        if (_colorValues.length > indexIn) {

            _colorValues[indexIn] = colorIn;
        }
    }

    public void setColors(String[] colorsIn) {

        for (int i = 0; Math.min(colorsIn.length, _colorValues.length) > i; i++) {

            if (null != colorsIn[i]) {

                _colorValues[i] = colorsIn[i];
            }
        }
        setColor();
    }

    @Override
    public boolean isEnabled() {

        return _enabled;
    }

    @Override
    public boolean isSelected() {

        return _selected;
    }

    public boolean isSelectable() {

        return _selectable;
    }

    @Override
    public void setEnabled(boolean enabledIn) {

        _enabled = enabledIn;
    }

    @Override
    public void setSelected(boolean selectedIn) {

        _selected = selectedIn;
    }

    public void setSelectable(boolean selectableIn) {

        _selectable = selectableIn;
    }

    @Override
    public boolean isValid() {

        return true;
    }

    private String encode(String textIn) {

        StringBuilder myBuffer = new StringBuilder();

        for (int i = 0; textIn.length() > i; i++ ) {

            char myCharacter = textIn.charAt(i);

            if ('\t' == myCharacter) {

                myBuffer.append("&nbsp;&nbsp;&nbsp;");

            } else if (' ' >= myCharacter) {

                myBuffer.append("&nbsp;");

            } else {

                switch (myCharacter) {

                case '\"' :

                    myBuffer.append("&#34;");
                    break;

                case '&' :

                    myBuffer.append("&amp;");
                    break;

                case '\'' :

                    myBuffer.append("&#39;");
                    break;

                case '-' :

                    myBuffer.append("&#8209;");
                    break;

                case '<' :

                    myBuffer.append("&lt;");
                    break;

                case '>' :

                    myBuffer.append("&gt;");
                    break;

                default :

                    myBuffer.append(myCharacter);
                    break;
                }
            }
        }

        return myBuffer.toString();
    }

    private void setColor() {

        if (_selectable) {

            if (_enabled) {

                if (_selected) {

                    getElement().getStyle().setColor(_colorValues[SELECTED_COLOR]);
                    getElement().getStyle().setBackgroundColor(_colorValues[SELECTED_BACKGROUND]);

                } else {

                    getElement().getStyle().setColor(_colorValues[DEFAULT_COLOR]);
                    getElement().getStyle().setBackgroundColor(_colorValues[DEFAULT_BACKGROUND]);
                }

            } else {

                getElement().getStyle().setColor(_colorValues[DISABLED_COLOR]);
                getElement().getStyle().setBackgroundColor(_colorValues[DISABLED_BACKGROUND]);
            }

        } else {

            getElement().getStyle().setColor(_colorValues[DEFAULT_COLOR]);
            getElement().getStyle().setBackgroundColor(_colorValues[DEFAULT_BACKGROUND]);
        }
    }
}

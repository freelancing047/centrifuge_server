package csi.client.gwt.widget.buttons;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;


public class ButtonDef {

    private String _buttonText;
    private ButtonType _buttonType = null;
    private IconType _buttonIcon = null;
    private boolean _enabled = true;

    public ButtonDef(String textIn) {

        _buttonText = textIn;
    }

    public ButtonDef(String textIn, ButtonType typeIn) {

        _buttonText = textIn;
        _buttonType = typeIn;
    }

    public ButtonDef(String textIn, IconType iconIn) {

        _buttonText = textIn;
        _buttonIcon = iconIn;
    }

    public ButtonDef(String textIn, IconType iconIn, ButtonType typeIn) {

        _buttonText = textIn;
        _buttonIcon = iconIn;
        _buttonType = typeIn;
    }

    public ButtonDef(String textIn, boolean enabledIn) {

        _buttonText = textIn;
        _enabled = enabledIn;
    }

    public ButtonDef(String textIn, ButtonType typeIn, boolean enabledIn) {

        _buttonText = textIn;
        _buttonType = typeIn;
        _enabled = enabledIn;
    }

    public ButtonDef(String textIn, IconType iconIn, boolean enabledIn) {

        _buttonText = textIn;
        _buttonIcon = iconIn;
        _enabled = enabledIn;
    }

    public ButtonDef(String textIn, IconType iconIn, ButtonType typeIn, boolean enabledIn) {

        _buttonText = textIn;
        _buttonIcon = iconIn;
        _buttonType = typeIn;
        _enabled = enabledIn;
    }

    public String getText() {
        
        return (null != _buttonText) ? _buttonText : "";
    }
    
    public boolean hasType() {
        
        return (null != _buttonType);
    }
    
    public ButtonType getType() {
        
        return _buttonType;
    }
    
    public boolean hasIcon() {
        
        return (null != _buttonIcon);
    }
    
    public IconType getIcon() {
        
        return _buttonIcon;
    }

    public boolean isEnabled() {

        return _enabled;
    }
}

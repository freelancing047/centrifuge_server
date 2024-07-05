package csi.client.gwt.widget.buttons;

import java.util.HashMap;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.events.HoverEvent;
import csi.client.gwt.events.HoverEventHandler;


public class Button extends com.github.gwtbootstrap.client.ui.Button {

    private final ButtonType _baseType = ButtonType.DEFAULT;
    private ButtonType _type = ButtonType.DEFAULT;
    
    private static final Map<String, ButtonType> _buttonTypes;

    static {
        _buttonTypes = new HashMap<String, ButtonType>();
        _buttonTypes.put("primary", ButtonType.PRIMARY);
        _buttonTypes.put("info", ButtonType.INFO);
        _buttonTypes.put("default", ButtonType.DEFAULT);
        _buttonTypes.put("success", ButtonType.SUCCESS);
        _buttonTypes.put("warning", ButtonType.WARNING);
        _buttonTypes.put("danger", ButtonType.DANGER);
        _buttonTypes.put("inverse", ButtonType.INVERSE);
        _buttonTypes.put("link", ButtonType.LINK);
    }
    
    private HoverEventHandler _hoverEventHandler;
    private String _displayMessage;
    private boolean _isOver = false;
    
    Widget _self;
    
    public Button() {
        
        super();
    }

    public Button(String labelIn) {
        
        super(labelIn);
    }

    public Button(ClickHandler clickIn) {
        
        super(clickIn);
    }

    public Button(String labelIn, ClickHandler clickIn) {
        
        super(labelIn, clickIn);
    }

    public Button(String labelIn, IconType iconIn) {
        
        super(labelIn, iconIn);
    }

    public Button(String labelIn, IconType iconIn, ClickHandler clickIn) {
        
        super(labelIn, iconIn, clickIn);
    }
    
    public Button(HoverEventHandler hoverIn, String displayMessageIn) {
        
        super();
        addHoverEventHandler(hoverIn, displayMessageIn);
    }

    public Button(String labelIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn);
        addHoverEventHandler(hoverIn, displayMessageIn);
    }

    public Button(ClickHandler clickIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(clickIn);
        addHoverEventHandler(hoverIn, displayMessageIn);
    }

    public Button(String labelIn, ClickHandler clickIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, clickIn);
        addHoverEventHandler(hoverIn, displayMessageIn);
    }

    public Button(String labelIn, IconType iconIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn);
        addHoverEventHandler(hoverIn, displayMessageIn);
    }

    public Button(String labelIn, IconType iconIn, ClickHandler clickIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn, clickIn);
        addHoverEventHandler(hoverIn, displayMessageIn);
    }

    public void addHoverEventHandler(HoverEventHandler hoverEventHandlerIn, String displayMessageIn) {
        
        if (null != hoverEventHandlerIn) {
            
            _hoverEventHandler = hoverEventHandlerIn;
            _displayMessage = displayMessageIn;
            
            _self  = this;
            
            addHandler(handleMouseOver, MouseOverEvent.getType());
            addHandler(handleMouseOut, MouseOutEvent.getType());
            addDomHandler(handleMouseOver, MouseOverEvent.getType());
            addDomHandler(handleMouseOut, MouseOutEvent.getType());
        }
    }
    
    private MouseOverHandler handleMouseOver = new MouseOverHandler() {
 
        @Override
        public void onMouseOver(MouseOverEvent event) {

            if (!_isOver) {
                
                _isOver = true;
                _hoverEventHandler.onHoverChange(new HoverEvent(_self, _displayMessage, _isOver));
            }
        }
    };
    
    private MouseOutHandler handleMouseOut = new MouseOutHandler() {

        @Override
        public void onMouseOut(MouseOutEvent event) {

            if (_isOver) {
                
                _isOver = false;
                _hoverEventHandler.onHoverChange(new HoverEvent(_self, _displayMessage, _isOver));
            }
        }
    };

    @Override
    public void setType(ButtonType typeIn) {
        
        _type = typeIn;
        
        super.setType(_type);
    }

    public void setType(String typeIn) {
        
        ButtonType myType = _buttonTypes.get(typeIn.toLowerCase());
        
        _type = (null != myType) ? myType : ButtonType.DEFAULT;
        
        super.setType(_type);
    }
    
    @Override
    public void setEnabled(boolean enabledIn) {
        
        if (enabledIn) {
            
            super.setType(_type);
            
        } else {
            
            super.setType(_baseType);
        }
        super.setEnabled(enabledIn);
    }

    public void setTextSize(int pixelsIn) {

        super.getElement().getStyle().setFontSize(pixelsIn, Style.Unit.PX);
    }

    protected void makeMini() {

        super.setSize(ButtonSize.MINI);
        super.getElement().getStyle().setFontSize(10, Style.Unit.PX);
    }
}

package csi.client.gwt.widget.buttons;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.events.HoverEventHandler;


public class GreenButton extends csi.client.gwt.widget.buttons.Button  {

    private final ButtonType _type = ButtonType.SUCCESS;
    
    public GreenButton() {
        
        super();
        super.setType(_type);
    }

    public GreenButton(String labelIn) {
        
        super(labelIn);
        super.setType(_type);
    }

    public GreenButton(ClickHandler handlerIn) {
        
        super(handlerIn);
        super.setType(_type);
    }

    public GreenButton(String labelIn, ClickHandler handlerIn) {
        
        super(labelIn, handlerIn);
        super.setType(_type);
    }

    public GreenButton(String labelIn, IconType iconIn) {
        
        super(labelIn, iconIn);
        super.setType(_type);
    }

    public GreenButton(String labelIn, IconType iconIn, ClickHandler handlerIn) {
        
        super(labelIn, iconIn, handlerIn);
        super.setType(_type);
    }
    
    public GreenButton(HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public GreenButton(String labelIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public GreenButton(ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(handlerIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public GreenButton(String labelIn, ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, handlerIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public GreenButton(String labelIn, IconType iconIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public GreenButton(String labelIn, IconType iconIn, ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn, handlerIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }
}

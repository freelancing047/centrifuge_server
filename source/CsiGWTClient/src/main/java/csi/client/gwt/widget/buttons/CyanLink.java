package csi.client.gwt.widget.buttons;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.events.HoverEventHandler;


public class CyanLink extends csi.client.gwt.widget.buttons.Button {

    private final ButtonType _type = ButtonType.LINK;
    
    public CyanLink() {
        
        super();
        super.setType(_type);
    }

    public CyanLink(String labelIn) {
        
        super(labelIn);
        super.setType(_type);
    }

    public CyanLink(ClickHandler handlerIn) {
        
        super(handlerIn);
        super.setType(_type);
    }

    public CyanLink(String labelIn, ClickHandler handlerIn) {
        
        super(labelIn, handlerIn);
        super.setType(_type);
    }
    
    public CyanLink(HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public CyanLink(String labelIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public CyanLink(ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(handlerIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }

    public CyanLink(String labelIn, ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, handlerIn, hoverIn, displayMessageIn);
        super.setType(_type);
    }
}

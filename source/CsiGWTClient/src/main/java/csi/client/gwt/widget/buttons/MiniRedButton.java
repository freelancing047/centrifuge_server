package csi.client.gwt.widget.buttons;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.events.HoverEventHandler;


public class MiniRedButton extends RedButton {
    
    public MiniRedButton() {
        
        super();
        makeMini();
    }

    public MiniRedButton(String labelIn) {
        
        super(labelIn);
        makeMini();
    }

    public MiniRedButton(ClickHandler handlerIn) {
        
        super(handlerIn);
        makeMini();
    }

    public MiniRedButton(String labelIn, ClickHandler handlerIn) {
        
        super(labelIn, handlerIn);
        makeMini();
    }

    public MiniRedButton(String labelIn, IconType iconIn) {
        
        super(labelIn, iconIn);
        makeMini();
    }

    public MiniRedButton(String labelIn, IconType iconIn, ClickHandler handlerIn) {
        
        super(labelIn, iconIn, handlerIn);
        makeMini();
    }
    
    public MiniRedButton(HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniRedButton(String labelIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniRedButton(ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(handlerIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniRedButton(String labelIn, ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, handlerIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniRedButton(String labelIn, IconType iconIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniRedButton(String labelIn, IconType iconIn, ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn, handlerIn, hoverIn, displayMessageIn);
        makeMini();
    }
}

package csi.client.gwt.widget.buttons;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.events.HoverEventHandler;


public class MiniCyanButton extends CyanButton {
    
    public MiniCyanButton() {
        
        super();
        makeMini();
    }

    public MiniCyanButton(String labelIn) {
        
        super(labelIn);
        makeMini();
    }

    public MiniCyanButton(ClickHandler handlerIn) {
        
        super(handlerIn);
        makeMini();
    }

    public MiniCyanButton(String labelIn, ClickHandler handlerIn) {
        
        super(labelIn, handlerIn);
        makeMini();
    }

    public MiniCyanButton(String labelIn, IconType iconIn) {
        
        super(labelIn, iconIn);
        makeMini();
    }

    public MiniCyanButton(String labelIn, IconType iconIn, ClickHandler handlerIn) {
        
        super(labelIn, iconIn, handlerIn);
        makeMini();
    }
    
    public MiniCyanButton(HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniCyanButton(String labelIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniCyanButton(ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(handlerIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniCyanButton(String labelIn, ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, handlerIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniCyanButton(String labelIn, IconType iconIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn, hoverIn, displayMessageIn);
        makeMini();
    }

    public MiniCyanButton(String labelIn, IconType iconIn, ClickHandler handlerIn, HoverEventHandler hoverIn, String displayMessageIn) {
        
        super(labelIn, iconIn, handlerIn, hoverIn, displayMessageIn);
        makeMini();
    }
}

package csi.client.gwt.csiwizard;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.boot.CsiModal;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;

/**
 * Created by centrifuge on 8/19/2015.
 */
public class InstructionOverlay extends CsiModal {

    FullSizeLayoutPanel _clearPanel = null;
    VerticalPanel _instructionPanel = null;

    //
    // Monitor all typing to check for a carriage return
    //
    private KeyDownHandler handlePanelKeyDown
            = new KeyDownHandler() {

        @Override
        public void onKeyDown(KeyDownEvent eventIn) {

            if (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode()) {

                eventIn.stopPropagation();
            }
        }
    };

    //
    // Monitor all typing to check for a carriage return
    //
    private KeyUpHandler handlePanelKeyUp
            = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent eventIn) {

            if (KeyCodes.KEY_ESCAPE == eventIn.getNativeKeyCode()) {

                eventIn.stopPropagation();
            }
        }
    };

    //
    // Monitor all typing to check for a carriage return
    //
    private KeyPressHandler handlePanelKeyPress
            = new KeyPressHandler() {

        @Override
        public void onKeyPress(KeyPressEvent eventIn) {

            if (KeyCodes.KEY_ESCAPE == eventIn.getCharCode()) {

                eventIn.stopPropagation();
            }
        }
    };

    public InstructionOverlay(Widget widgetIn, HasVerticalAlignment.VerticalAlignmentConstant alignmentIn) {

        super();

        _instructionPanel = new VerticalPanel();

        _instructionPanel.setWidth("100%");
        _instructionPanel.setHeight("100%");
        _instructionPanel.getElement().getStyle().setBackgroundColor("transparent");

        _instructionPanel.setVerticalAlignment(alignmentIn);
        _instructionPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        _instructionPanel.add(widgetIn);

        _clearPanel = new FullSizeLayoutPanel();
        _clearPanel.getElement().getStyle().setMargin(0, Style.Unit.PX);
        _clearPanel.getElement().getStyle().setBackgroundColor("transparent");
        _clearPanel.setPixelSize(800, 434);

        _clearPanel.add(_instructionPanel);
        _clearPanel.setWidgetTopBottom(_instructionPanel, 60, Style.Unit.PX, 60, Style.Unit.PX);
        _clearPanel.setWidgetLeftRight(_instructionPanel, 480, Style.Unit.PX, 0, Style.Unit.PX);

        setPixelSize(820, 454);
        getElement().getStyle().setBackgroundColor("transparent");
        add(_clearPanel);
    }
}

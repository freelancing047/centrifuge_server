package csi.client.gwt.viz.shared.chrome.panel;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

// FIXME: when change this from modifying bottom to height when the 
// widget's behavior is ironed out
public class ToggleTabDrawerClickHandler extends Animation implements ClickHandler {

    private static final int ANIMATION_DURATION = 500;
    private static final int BOTTOM_POSITION = -180;

    private int endSize;
    private int startSize;
    private Element tabDrawerElement;
    private Button toggleButton;


    public ToggleTabDrawerClickHandler(Element element, Button toggleButton) {
        tabDrawerElement = element;
        this.toggleButton = toggleButton;
    }


    @Override
    protected void onComplete() {
        tabDrawerElement.getStyle().setBottom(endSize, Unit.PX);
    }


    private boolean isHidden() {
        String collapsedPosition = BOTTOM_POSITION + "px";
        return collapsedPosition.equals(tabDrawerElement.getStyle().getBottom());
    }


    @Override
    protected void onUpdate(double progress) {
        double delta = (endSize - startSize) * progress;
        double newSize = startSize + delta;
        tabDrawerElement.getStyle().setBottom(newSize, Unit.PX);
    }


    void animateAction(int startSize, int endSize, int duration) {
        this.startSize = startSize;
        this.endSize = endSize;
        if (duration == 0) {
            onComplete();
            return;
        }
        run(duration);
    }


    @Override
    public void onClick(ClickEvent event) {
        if (!isHidden()) {
            animateAction(0, BOTTOM_POSITION, ANIMATION_DURATION);
            toggleButton.setIcon(IconType.ARROW_UP);
        } else {
            tabDrawerElement.getStyle().setDisplay(Display.BLOCK);
            animateAction(BOTTOM_POSITION, 0, ANIMATION_DURATION);
            toggleButton.setIcon(IconType.ARROW_DOWN);
        }

    }

}

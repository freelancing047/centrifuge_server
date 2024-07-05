package csi.client.gwt.viz.viewer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.SimplePanel;
import csi.client.gwt.widget.ui.SlidingAccessPanel;

public class ViewerPanel extends SimplePanel implements ViewerContainer {
    private SlidingAccessPanel viewerSlidingAccessPanel;

    public ViewerPanel(SlidingAccessPanel viewerSlidingAccessPanel) {

        this.viewerSlidingAccessPanel = viewerSlidingAccessPanel;
    }

    @Override
    public void show() {
        viewerSlidingAccessPanel.setPinned(true);
        viewerSlidingAccessPanel.setState(SlidingAccessPanel.State.TRANSITION);
        viewerSlidingAccessPanel.getElement().getStyle().setRight(0, Style.Unit.PX);
        viewerSlidingAccessPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
    }

    @Override
    public void hide() {
        viewerSlidingAccessPanel.setPinned(true);
        viewerSlidingAccessPanel.setState(SlidingAccessPanel.State.TRANSITION);
        viewerSlidingAccessPanel.getElement().getStyle().setRight(-getElement().getOffsetWidth(), Style.Unit.PX);
        viewerSlidingAccessPanel.getElement().getStyle().setDisplay(Style.Display.NONE);


    }

}

package csi.client.gwt.widget;

import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by centrifuge on 10/29/2015.
 */
public class SelectionWidget extends FocusPanel {

    private Boolean _enabled;
    private Boolean _selected;
    private ClickHandler _handler;

    public SelectionWidget(Widget widgetIn, String labelIn, String descriptionIn, String colorIn,
                           Boolean visibleIn, Boolean enabledIn, Boolean selectedIn, ClickHandler handlerIn) {

        initialize(widgetIn, labelIn, descriptionIn, colorIn, visibleIn, enabledIn, selectedIn, handlerIn);
    }

    public SelectionWidget(Widget widgetIn, String labelIn, String descriptionIn, String colorIn,
                           Boolean visibleIn, Boolean enabledIn, Boolean selectedIn) {

        initialize(widgetIn, labelIn, descriptionIn, colorIn, visibleIn, enabledIn, selectedIn, null);
    }

    public SelectionWidget(Widget widgetIn, String labelIn, String descriptionIn, String colorIn, ClickHandler handlerIn) {

        initialize(widgetIn, labelIn, descriptionIn, colorIn, true, true, false, handlerIn);
    }

    public SelectionWidget(Widget widgetIn, String labelIn, String descriptionIn, String colorIn) {

        initialize(widgetIn, labelIn, descriptionIn, colorIn, true, true, false, null);
    }

    public SelectionWidget(Widget widgetIn, String labelIn, String descriptionIn, ClickHandler handlerIn) {

        initialize(widgetIn, labelIn, descriptionIn, "black", true, true, false, handlerIn);
    }

    public SelectionWidget(Widget widgetIn, String labelIn, String descriptionIn) {

        initialize(widgetIn, labelIn, descriptionIn, "black", true, true, false, null);
    }

    public void setEnabled(boolean enabledIn) {

        _enabled = enabledIn;
    }

    public boolean isEnabled() {

        return _enabled;
    }

    public void setSelected(boolean selectedIn) {

        _selected = selectedIn;
    }

    public boolean isSelected() {

        return _selected;
    }

    public void setHandler(ClickHandler handlerIn) {

        _handler = handlerIn;
    }

    public ClickHandler getHandler() {

        return _handler;
    }

    public boolean isValid() {

        return isEnabled() || (!isSelected());
    }

    protected SelectionWidget() {

    }

    protected void initialize(Widget widgetIn, String labelIn, String descriptionIn, String colorIn,
                                Boolean visibleIn, Boolean enabledIn, Boolean selectedIn, ClickHandler handlerIn) {

        setVisible(visibleIn);
        setEnabled(enabledIn);
        setSelected(selectedIn);
        setHandler(handlerIn);

        buildObject(widgetIn, labelIn, descriptionIn);
    }

    protected void processClickEvent(ClickEvent eventIn) {


        if (null != _handler) {

            _handler.onClick(eventIn);
        }
    }

    private ClickHandler handleClickEvent = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            processClickEvent(eventIn);
        }
    };

    private void buildObject(Widget widgetIn, String labelIn, String descriptionIn) {

        add(widgetIn);
        add(new InlineLabel(labelIn));
        addClickHandler(handleClickEvent);
    }
}

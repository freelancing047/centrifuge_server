package csi.client.gwt.widget.boot;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.buttons.Button;

/**
 * Created by centrifuge on 5/1/2015.
 */
public class DialogPrototype extends SizeProvidingModal {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private boolean dialogContainerAdded;
    private DivWidget dialogContainer;

    private List<Button> _buttonList;

    @UiField
    Button buttonAction;
    @UiField
    Button buttonCancel;
    @UiField
    DivWidget leftControlContainer, rightControlContainer;

    interface SpecificUiBinder extends UiBinder<Widget, DialogPrototype> {
    }

    public DialogPrototype(String actionButtonTextIn, String cancelButtonTextIn, ClickHandler handlerIn) {
        init(actionButtonTextIn, cancelButtonTextIn);
        buttonCancel.addClickHandler(handlerIn);
    }

    public DialogPrototype(String actionButtonTextIn, String cancelButtonTextIn, CanBeShownParent parentIn) {
        super(parentIn);
        init(actionButtonTextIn, cancelButtonTextIn);
    }

    public DialogPrototype(String actionButtonTextIn, String cancelButtonTextIn) {
        super();
        init(actionButtonTextIn, cancelButtonTextIn);
    }

    @Override
    public void add(Widget w) {
        if (!dialogContainerAdded) {
            super.add(w);
        } else {
            dialogContainer.add(w);
        }
    }

    @Override
    public void onHeaderCloseClick() {
        // Simulate a cancel click
        buttonCancel.click();
        // Continue with normal dialog cancellation
        super.onHeaderCloseClick();
    }

    public Button getActionButton() {
        return buttonAction;
    }

    public Button getCancelButton() {
        return buttonCancel;
    }

    @UiChild(tagname = "leftControl")
    public Widget addLeftControl(Widget widget) {
        leftControlContainer.add(widget);
        if (widget instanceof Button) {

            _buttonList.add((Button)widget);
        }
        return widget;
    }

    @UiChild(tagname = "rightControl")
    public Widget addRightControl(Widget widget) {
        rightControlContainer.insert(widget, 0);
        if (widget instanceof Button) {

            _buttonList.add((Button)widget);
        }
        return widget;
    }

    public void hideOnCancel() {
        getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent eventIn) {
                hide();
            }
        });
    }

    public void hideOnAction() {
        getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent eventIn) {
                hide();
            }
        });
    }

    public void destroy() {
        hide();
        clear();
    }

    public void setFocus(final Widget widgetIn) {

        Scheduler.get().scheduleDeferred(new ScheduledCommand(){

            @Override
            public void execute() {
                widgetIn.getElement().focus();
            }});
    }

    public void show(Integer widthIn) {

        setButtonWidth(widthIn);

        super.show();
    }

    public DialogPrototype setButtonWidth(Integer widthIn) {

        if (null != widthIn) {

            for (Button myButton : _buttonList) {

                myButton.setWidth(widthIn.toString() + "px"); //$NON-NLS-1$
            }
        }
        return this;
    }

    private void init(String actionButtonTextIn, String cancelButtonTextIn) {
        // Adds the footer.
        add(uiBinder.createAndBindUi(this));
        dialogContainer = new DivWidget();
        add(dialogContainer);
        dialogContainerAdded = true;
        _buttonList = new ArrayList<Button>();
        _buttonList.add(buttonCancel);
        _buttonList.add(buttonAction);
        buttonAction.setText(actionButtonTextIn);
        buttonCancel.setText(cancelButtonTextIn);
    }
}

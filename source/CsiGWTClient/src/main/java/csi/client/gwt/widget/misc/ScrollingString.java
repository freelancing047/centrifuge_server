package csi.client.gwt.widget.misc;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 3/10/2015.
 */
public class ScrollingString implements IsWidget {

    @Override
    public Widget asWidget() {
        return topPanel;
    }

    interface SpecificUiBinder extends UiBinder<Widget, ScrollingString> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    protected VerticalPanel topPanel;
    @UiField
    protected ScrollPanel scrollingPanel;
    @UiField
    protected HorizontalPanel stringPanel;
    @UiField
    protected InlineLabel baseLabel;
    @UiField
    protected InlineLabel deltaLabel;
    @UiField
    protected NonWrappingLabel stringValue;
    @UiField
    protected HorizontalPanel usagePrompt;
    @UiField
    protected RadioButton asColumnNames;
    @UiField
    protected RadioButton asDataValues;

    private HandlerRegistration _handler = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ScrollingString() {

        createDisplay(null, null, null);
    }

    public ScrollingString(String textIn) {

        this();
        setText(textIn);
    }

    public ScrollingString(String textIn, String labelIn) {

        createDisplay(labelIn, textIn, null);
    }

    public ScrollingString(ClickHandler handlerIn) {

        createDisplay(null, null, handlerIn);
    }

    public ScrollingString(ClickHandler handlerIn, String labelIn) {

        createDisplay(labelIn, null, handlerIn);
    }

    public ScrollingString(String textIn, ClickHandler handlerIn, String labelIn) {

        createDisplay(labelIn, textIn, handlerIn);
    }

    public void setColor(int indexIn, String colorIn) {

        stringValue.setColor(indexIn, colorIn);
    }

    public void setColors(String[] colorsIn) {

        stringValue.setColors(colorsIn);
    }

    public void setWidth(String widthIn) {

        topPanel.setWidth(widthIn);
        scrollingPanel.setWidth(widthIn);
    }

    public void setText(String textIn) {

        if (null != textIn) {

            stringValue.setText(textIn);

        } else {

            stringValue.setText(""); //$NON-NLS-1$
        }
    }

    public void setText(String textIn, String colorIn) {

        setText(textIn);
        stringValue.getElement().getStyle().setColor(colorIn);
    }

    public void setText(String textIn, String colorIn, String backgroundIn) {

        setText(textIn);
        stringValue.getElement().getStyle().setColor(colorIn);
        stringValue.getElement().getStyle().setBackgroundColor(backgroundIn);
    }

    public void setBaseLabel(String textIn) {

        if (null != textIn) {

            baseLabel.setText(textIn);
            baseLabel.setVisible(true);

        } else {

            baseLabel.setText(""); //$NON-NLS-1$
            baseLabel.setVisible(false);
        }
    }

    public void setBaseLabel(String textIn, String colorIn) {

        setBaseLabel(textIn);
        baseLabel.getElement().getStyle().setColor(colorIn);
    }

    public void setBaseLabel(String textIn, String colorIn, String backgroundIn) {

        setBaseLabel(textIn);
        baseLabel.getElement().getStyle().setColor(colorIn);
        baseLabel.getElement().getStyle().setBackgroundColor(backgroundIn);
    }

    public void setDeltaLabel(String textIn) {

        if (null != textIn) {

            deltaLabel.setText(textIn);
            deltaLabel.setVisible(true);

        } else {

            deltaLabel.setText(""); //$NON-NLS-1$
            deltaLabel.setVisible(false);
        }
    }

    public void setDeltaLabel(String textIn, String colorIn) {

        setDeltaLabel(textIn);
        deltaLabel.getElement().getStyle().setColor(colorIn);
    }

    public void setDeltaLabel(String textIn, String colorIn, String backgroundIn) {

        setDeltaLabel(textIn);
        deltaLabel.getElement().getStyle().setColor(colorIn);
        deltaLabel.getElement().getStyle().setBackgroundColor(backgroundIn);
    }

    public void displayUsage() {

        usagePrompt.setVisible(true);
        asColumnNames.setValue(true);
    }

    public void hideUsage() {

        usagePrompt.setVisible(false);
    }

    public boolean hasColumnNames() {

        return (null != asColumnNames) ? asColumnNames.getValue() : false;
    }

    private void createDisplay(String labelIn, String valueIn, ClickHandler handlerIn) {

        uiBinder.createAndBindUi(this);

        scrollingPanel.setHeight(Integer.toString(2 * Dialog.intLabelHeight) + "px"); //$NON-NLS-1$

        setBaseLabel(labelIn);
        setText(valueIn);

        if (null != handlerIn) {

            if (null != _handler) {

                _handler.removeHandler();
            }

            _handler = stringValue.addClickHandler(handlerIn);
            stringValue.getElement().getStyle().setCursor(com.google.gwt.dom.client.Style.Cursor.POINTER);
            stringValue.setSelectable(true);

        } else {

            stringValue.setSelectable(false);
        }
    }
}

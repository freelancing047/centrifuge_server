package csi.client.gwt.widget.boot;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Icon;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;

public class MultiNotificationPopup extends Dialog {

    public MultiNotificationPopup() {
        super();
    }

    public MultiNotificationPopup(String titleIn, String firstLine, List<String> messages) {
        super();
        init(titleIn, firstLine, messages, null);
    }

    public MultiNotificationPopup(String titleIn, String firstLine, List<String> messages, Icon iconIn) {
        super();
    }

    protected void init(String titleIn, String firstLine, List<String> messages, Icon iconIn) {

        hideTitleCloseButton();

        if (null != iconIn) {
            addToHeader(iconIn);
        }

        if (null != titleIn) {
            CsiHeading myHeading = createHeading(titleIn);
            myHeading.getElement().getStyle().setDisplay(Display.INLINE);
            addToHeader(myHeading);
        }

        firstLine = firstLine == null ? "" : firstLine;
        messages = messages == null ? new ArrayList<String>() : messages;

        FluidRow myRow = new FluidRow();
        
        Column col0 = new Column(1);
        myRow.add(col0);
        
        Column myColumn = new Column(12);
        myColumn.getElement().getStyle().setTextAlign(TextAlign.LEFT);
        HtmlList myMessage = new HtmlList(firstLine, messages);

        myMessage.getElement().getStyle().setFontSize(12, Unit.PX);
        myColumn.add(myMessage);
        myRow.add(myColumn);
        add(myRow);

        setBodyWidth("400px");
        setBodyHeight("40px");

        getActionButton().setVisible(false);
        getCancelButton().setText(txtCloseButton);
        hideOnCancel();
    }
}

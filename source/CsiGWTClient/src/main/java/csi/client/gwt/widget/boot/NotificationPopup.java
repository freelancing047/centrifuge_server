package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;

import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.csiwizard.widgets.TextInputWidget;


public class NotificationPopup extends Dialog {

    private TextInputWidget _textInput = null;

    public NotificationPopup(ClickHandler handlerIn) {
        super(handlerIn);
        identifyAsAlert();
    }

    public NotificationPopup(CanBeShownParent parentIn) {
        super(parentIn);
        identifyAsAlert();
    }

    public NotificationPopup(String titleIn, String messageTextIn, String defaultIn, CanBeShownParent parentIn) {
        this(parentIn);
        init(titleIn, messageTextIn, null, defaultIn);
    }

    public NotificationPopup(String titleIn, String messageTextIn, CanBeShownParent parentIn) {
        this(parentIn);
        init(titleIn, messageTextIn, null, null);
    }

    public NotificationPopup(String titleIn, String messageTextIn, Icon iconIn, String defaultIn, CanBeShownParent parentIn) {
        this(parentIn);
        init(titleIn, messageTextIn, iconIn, defaultIn);
    }

    public NotificationPopup(String titleIn, String messageTextIn, Icon iconIn, CanBeShownParent parentIn) {
        this(parentIn);
        init(titleIn, messageTextIn, iconIn, null);
    }

    public NotificationPopup() {
        super();
        identifyAsAlert();
    }

    public NotificationPopup(String titleIn, String messageTextIn, String defaultIn) {
        this();
        init(titleIn, messageTextIn, null, defaultIn);
    }

    public NotificationPopup(String titleIn, String messageTextIn) {
        this();
        init(titleIn, messageTextIn, null, null);
    }

    public NotificationPopup(String titleIn, String messageTextIn, Icon iconIn, String defaultIn) {
        this();
        init(titleIn, messageTextIn, iconIn, defaultIn);
    }

    public NotificationPopup(String titleIn, String messageTextIn, Icon iconIn) {
        this();
        init(titleIn, messageTextIn, iconIn, null);
    }

    public String getResult() {

        try {

            return _textInput.getText();

        } catch (Exception myException) {

            return null;
        }
    }

    protected void init(String titleIn, String messageTextIn, Icon iconIn) {

        init(titleIn, messageTextIn, iconIn, null);
    }

    protected void init(String titleIn, String messageTextIn, Icon iconIn, String defaultIn) {

        String[] myMessage = messageTextIn.split("\n");

        identifyAsAlert();
        hideTitleCloseButton();
        
        if (null != iconIn) {

            addToHeader(iconIn);
        }
        if (null != titleIn) {

            CsiHeading myHeading = createHeading(titleIn);
            myHeading.getElement().getStyle().setDisplay(Display.INLINE);
            addToHeader(myHeading);
        }
        if (null != defaultIn) {

            TextInputWidget _textInput = new TextInputWidget(messageTextIn, defaultIn, true);

            add(_textInput);

        } else if (null != messageTextIn) {

            FluidRow myRow = new FluidRow();
            Column myColumn = new Column(12);
            myColumn.getElement().getStyle().setTextAlign(TextAlign.CENTER);
            for (String myLine : myMessage) {
                
                Paragraph myParagraph = new Paragraph(); 
                myParagraph.setText(myLine);
                myParagraph.getElement().getStyle().setFontSize(12, Unit.PX);
                myColumn.add(myParagraph);
            }
            myRow.add(myColumn);
            add(myRow);
        }
        setBodyWidth("400px");
        setBodyHeight("40px");
        
        getActionButton().setVisible(false);
        getCancelButton().setText(txtCloseButton);
        hideOnCancel();
    }
}

package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.server.common.util.StringUtil;

public class ContinueDialog extends Dialog {

    boolean _hideCancel = false;

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn) {
        
        super();

        initialize(titleIn, messageIn, null);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn) {

        super();

        initialize(titleIn, messageIn, null);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, boolean hideCancelIn) {

        super();

        _hideCancel = hideCancelIn;
        initialize(titleIn, messageIn, null);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn,
                          String actionTextIn, String cancelTextIn) {

        super();

        _hideCancel = false;
        initialize(titleIn, messageIn, null);
        getActionButton().setText(actionTextIn);
        getCancelButton().setText(cancelTextIn);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, String buttonIn) {
        
        super();

        initialize(titleIn, messageIn, buttonIn);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, String buttonIn) {
        
        super();

        initialize(titleIn, messageIn, buttonIn);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn) {
        
        super();

        initialize(null, messageIn, null);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn) {

        super();

        initialize(null, messageIn, null);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, boolean hideCancelIn) {

        super();

        _hideCancel = hideCancelIn;
        initialize(null, messageIn, null);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, String buttonIn) {
        
        super();

        initialize(null, messageIn, buttonIn);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, String buttonIn) {
        
        super();

        initialize(null, messageIn, buttonIn);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageIn, null);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageIn, null);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, String buttonIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageIn, buttonIn);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String titleIn, String messageIn, ClickHandler onContinueIn, String buttonIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageIn, buttonIn);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageIn, null);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageIn, null);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, String buttonIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageIn, buttonIn);
        addClickHandler(onContinueIn);
        addCancelHandler(onCancelIn);
    }

    public ContinueDialog(String messageIn, ClickHandler onContinueIn, String buttonIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageIn, buttonIn);
        addClickHandler(onContinueIn);
    }

    public ContinueDialog setButtonWidth(Integer widthIn, CanBeShownParent parentIn) {

        if (null != widthIn) {

            buttonAction.setWidth(widthIn.toString() + "px");
            buttonCancel.setWidth(widthIn.toString() + "px");
        }
        return this;
    }

    protected void initialize(String titleIn, String messageIn, String buttonIn) {
        
        String myTitle = (null != titleIn) ? titleIn : Dialog.txtWarningTitle;
        Icon myIcon = new Icon(IconType.WARNING_SIGN);
        CsiHeading myHeading = createHeading(myTitle);
        String[] myMessage = {""};
        try{
            myMessage = StringUtil.split(messageIn, '\n');
        }catch (Exception e){

        }

        identifyAsAlert();
        myIcon.getElement().getStyle().setFontSize(20, Unit.PX);
        myIcon.getElement().getStyle().setColor(txtWarningColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);

        myHeading.getElement().getStyle().setDisplay(Display.INLINE);
        
        hideTitleCloseButton();
        addToHeader(myIcon);
        addToHeader(myHeading);
        
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
        
        setBodyWidth("400px");
        
        hideOnCancel();
        hideOnAction();
        buttonAction.setType(ButtonType.WARNING);
        if (null != buttonIn) {
            buttonAction.setText(buttonIn);
        } else {
            buttonAction.setText(txtContinueButton);
        }
        if (_hideCancel) {

            buttonCancel.setVisible(false);

        } else {

            buttonCancel.setVisible(true);
            buttonCancel.setText(txtCancelButton);
        }
    }

    public void addClickHandler(ClickHandler handlerIn) {
        
        getActionButton().addClickHandler(handlerIn);
    }

    public void addCancelHandler(ClickHandler handlerIn) {
        
        getCancelButton().addClickHandler(handlerIn);
    }

    public void hideCancel() {
        
        buttonCancel.setVisible(false);
    }
}

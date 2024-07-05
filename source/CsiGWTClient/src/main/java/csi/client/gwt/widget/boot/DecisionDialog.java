package csi.client.gwt.widget.boot;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.server.common.util.StringUtil;

public class DecisionDialog extends Dialog {

    private HandlerManager _handlerManager;
    
    private List<Button> _buttonList = null;
    private Column _messageColumn = null;
    private Object _dataObject = null;

    public DecisionDialog(String titleIn, String messageIn, List<ButtonDef> buttonsIn, ChoiceMadeEventHandler handlerIn, Integer widthIn, CanBeShownParent parentIn) {

        super(parentIn);

        _handlerManager = new HandlerManager(this);

        initialize(titleIn, messageIn);
        placeButtons(buttonsIn, widthIn);
        addChoiceMadeEventHandler(handlerIn);
    }

    public DecisionDialog(String titleIn, String messageIn, List<ButtonDef> buttonsIn, ChoiceMadeEventHandler handlerIn, CanBeShownParent parentIn) {

        this(titleIn, messageIn, buttonsIn, handlerIn, null, parentIn);
    }

    public DecisionDialog(String messageIn, List<ButtonDef> buttonsIn, ChoiceMadeEventHandler handlerIn, Integer widthIn, CanBeShownParent parentIn) {

        this(null, messageIn, buttonsIn, handlerIn, widthIn, parentIn);
    }

    public DecisionDialog(String messageIn, List<ButtonDef> buttonsIn, ChoiceMadeEventHandler handlerIn, CanBeShownParent parentIn) {

        this(null, messageIn, buttonsIn, handlerIn, null, parentIn);
    }

    public DecisionDialog(String titleIn, String messageIn, List<ButtonDef> buttonsIn,
                          ChoiceMadeEventHandler handlerIn, Integer widthIn) {

        super();

        _handlerManager = new HandlerManager(this);

        initialize(titleIn, messageIn);
        placeButtons(buttonsIn, widthIn);
        addChoiceMadeEventHandler(handlerIn);
    }

    public DecisionDialog(String titleIn, String messageIn, List<ButtonDef> buttonsIn,
                          ChoiceMadeEventHandler handlerIn, Object dataObjectIn, Integer widthIn) {

        this(titleIn, messageIn, buttonsIn, handlerIn, widthIn);

        _dataObject = dataObjectIn;
    }

    public DecisionDialog(String titleIn, String messageIn, List<ButtonDef> buttonsIn, ChoiceMadeEventHandler handlerIn) {

        this(titleIn, messageIn, buttonsIn, handlerIn, null, (Integer)null);
    }

    public DecisionDialog(String messageIn, List<ButtonDef> buttonsIn, ChoiceMadeEventHandler handlerIn, Integer widthIn) {

        this(null, messageIn, buttonsIn, handlerIn, null, widthIn);
    }

    public DecisionDialog(String messageIn, List<ButtonDef> buttonsIn, ChoiceMadeEventHandler handlerIn) {

        this(null, messageIn, buttonsIn, handlerIn, null, (Integer)null);
    }

    @Override
    public void fireEvent(GwtEvent<?> eventIn) {
        
        _handlerManager.fireEvent(eventIn);
    }

    public HandlerRegistration addChoiceMadeEventHandler(
            ChoiceMadeEventHandler handler) {
        return _handlerManager.addHandler(ChoiceMadeEvent.type, handler);
    }
    
    public void addWidget(Widget widgetIn) {
        
        add(widgetIn);
    }
    
    public void enableButton(int buttonIndexIn, boolean valueIn) {
        
        if (_buttonList.size() >= buttonIndexIn) {
            
            _buttonList.get(buttonIndexIn - 1).setEnabled(valueIn);
        }
    }
    
    public void enableButton(int buttonIndexIn) {
        
        if (_buttonList.size() >= buttonIndexIn) {
            
            _buttonList.get(buttonIndexIn - 1).setEnabled(true);
        }
    }

    public void disableButton(int buttonIndexIn) {

        if (_buttonList.size() >= buttonIndexIn) {

            _buttonList.get(buttonIndexIn - 1).setEnabled(false);
        }
    }

    public void addInfo(String messageIn) {

        String[] myMessage = StringUtil.split(messageIn, '\n');

        for (String myLine : myMessage) {

            Paragraph myParagraph = new Paragraph();
            myParagraph.setText(myLine);
            myParagraph.getElement().getStyle().setFontSize(12, Unit.PX);
            _messageColumn.add(myParagraph);
        }
    }

    public void addWarning(String messageIn) {

        String[] myMessage = StringUtil.split(messageIn, '\n');

        for (String myLine : myMessage) {

            Paragraph myParagraph = new Paragraph();
            myParagraph.setText(myLine);
            myParagraph.getElement().getStyle().setFontSize(12, Unit.PX);
            myParagraph.getElement().getStyle().setColor(Dialog.txtWarningColor);
            _messageColumn.add(myParagraph);
        }
    }

    protected void initialize(String titleIn, String messageIn) {
        
        String myTitle = (null != titleIn) ? titleIn : Dialog.txtDecisionTitle;
        Icon myIcon = new Icon(IconType.QUESTION_SIGN);
        CsiHeading myHeading = createHeading(myTitle);

        identifyAsAlert();
        _buttonList = new ArrayList<Button>();

        myIcon.getElement().getStyle().setFontSize(20, Unit.PX);
        myIcon.getElement().getStyle().setColor(txtDecisionColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);

        myHeading.getElement().getStyle().setDisplay(Display.INLINE);
        
        hideTitleCloseButton();
        addToHeader(myIcon);
        addToHeader(myHeading);
        
        FluidRow myRow = new FluidRow();
        _messageColumn = new Column(12);
        _messageColumn.getElement().getStyle().setTextAlign(TextAlign.CENTER);
        addInfo(messageIn);
        myRow.add(_messageColumn);
        add(myRow);
        
        setBodyWidth("400px");
        
        getActionButton().setVisible(false);
        getCancelButton().setText(txtCancelButton);
    }
    
    private void placeButtons(List<ButtonDef> buttonsIn, Integer widthIn) {
        
        String myWidth = (null != widthIn) ? widthIn.toString() + "px" : null;
        
        if (null != buttonsIn) {
            
            for (int i = 0;  buttonsIn.size() > i; i++) {
                
                final int j = i + 1;
                ButtonDef myDef = buttonsIn.get(i);
                Button myButton = new Button(myDef.getText(), new ClickHandler() {
                    int buttonIndex = j;
                    public void onClick(ClickEvent eventIn) {
                        
                        fireEvent(new ChoiceMadeEvent(buttonIndex, _dataObject));
                        hide();
                    }
                });
                if (myDef.hasType()) {
                    
                    myButton.setType(myDef.getType());
                }
                if (myDef.hasIcon()) {
                    
                    myButton.setIcon(myDef.getIcon());
                }
                addRightControl(myButton);
                
                if (null != myWidth) {
                    
                    myButton.setWidth(myWidth);
                }
                myButton.setEnabled(myDef.isEnabled());
                
                _buttonList.add(myButton);
            }
            
            if (null != myWidth) {
                
                buttonCancel.setWidth(myWidth);
            }

            buttonCancel.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent eventIn) {
                        
                        fireEvent(new ChoiceMadeEvent(0, _dataObject));
                        hide();
                    }
                });
        }
    }
}

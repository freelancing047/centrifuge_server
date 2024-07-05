package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.buttons.Button;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ExceptionDialog extends NotificationPopup {

    FluidRow _traceRow = null;
    TextArea _textArea = null;
    Button _traceButton = null;

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    ClickHandler myClickHandler = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            
            boolean myTraceVisible = _traceRow.isVisible();
            
            _traceButton.setText(myTraceVisible ? txtShowTraceButton : txtHideTraceButton);
            _traceRow.setVisible(!myTraceVisible);
        }
    };
    
    public ExceptionDialog(String titleIn, Throwable exceptionIn, String messageIn) {
        super();
        Initialize(titleIn, messageIn, exceptionIn);
    };
    
    public ExceptionDialog(String titleIn, Throwable exceptionIn) {
        super();
        Initialize(titleIn, null, exceptionIn);
    };
    
    public ExceptionDialog(Throwable exceptionIn, String messageIn) {
        super();
        Initialize(null, messageIn, exceptionIn);
    };
    
    public ExceptionDialog(Throwable exceptionIn) {
        super();
        Initialize(null, null, exceptionIn);
    };
    
    protected void Initialize(String titleIn, String messageTextIn, Throwable exceptionIn) {
        String myTitle = (null != titleIn) ? titleIn : Dialog.txtExceptionTitle;
        String myMessage = (null != messageTextIn)
                            ? messageTextIn
                            : ((null != exceptionIn) && (null != exceptionIn.getMessage()))
                                    ? exceptionIn.getMessage()
                                    : i18n.exceptionDialogNoMessage() ; //$NON-NLS-1$
        Icon myIcon = new Icon(IconType.REMOVE_SIGN);
        myIcon.setSize(IconSize.TWO_TIMES);
        myIcon.getElement().getStyle().setColor(txtErrorColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);
        init(myTitle, myMessage, myIcon);
        if (null != exceptionIn) {
            
            String myStackTrace = buildStackTrace(exceptionIn);
            
            if ((null != myStackTrace) && (0 < myStackTrace.length())) {
                _traceRow = new FluidRow();
                Column myColumn = new Column(12);
                _textArea = new TextArea();
                _textArea.getElement().setAttribute("wrap", "off"); //$NON-NLS-1$ //$NON-NLS-2$
                _textArea.setSize("400px", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
                _textArea.setText(myStackTrace.toString());
                myColumn.getElement().getStyle().setTextAlign(TextAlign.LEFT);
                myColumn.add(_textArea);
                _traceRow.add(myColumn);
                _traceRow.setVisible(false);
                add(_traceRow);
                _traceButton = new Button(txtShowTraceButton);
                _traceButton.setEnabled(true);
                _traceButton.setVisible(true);
                _traceButton.addClickHandler(myClickHandler);
                addRightControl(_traceButton);
            }
        }
    }
    
    private String buildStackTrace(Throwable exceptionIn) {
        
        StringBuilder myStackTrace = new StringBuilder();
        
        for (Throwable myActiveException = exceptionIn; null != myActiveException; myActiveException = myActiveException.getCause()) {
            
            String myCause = myActiveException.getMessage();
            StackTraceElement[] myTrace = myActiveException.getStackTrace();
            
            myStackTrace.append(i18n.exceptionDialogCausedBy()); //$NON-NLS-1$
            myStackTrace.append((null != myCause) ? myCause : i18n.exceptionDialogUnknown()); //$NON-NLS-1$
            myStackTrace.append('\n');
            
            for (int i = 0; myTrace.length > i; i++) {
                
                StackTraceElement myElement = myTrace[i];
                myStackTrace.append("           "); //$NON-NLS-1$
                myStackTrace.append(myElement.toString());
                myStackTrace.append('\n');
            }
        }
        return myStackTrace.toString();
    }
}

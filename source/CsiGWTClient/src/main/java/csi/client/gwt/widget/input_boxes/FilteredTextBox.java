package csi.client.gwt.widget.input_boxes;

import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.boot.Dialog;

public class FilteredTextBox extends TextBoxBase implements ValidityCheckCapable {

    Widget colorChangingLabel  = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int _deltaWidth = 22;
    private static final int _boxHeight = 22;

    protected static final boolean[] alphaNumMap = {

            false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,
            false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,
            false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,
            true, true, true, true, true, true, true, true, true, true, false,false,false,false,false,false,
            false,true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true, true, true, true, true, false,false,false,false,false,
            false,true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, true, true, true, true, true, true, false,false,false,false,false
    };

    protected boolean _isRequired = false;
    protected Map<String, ? extends Object> _rejectionMap = null;
    protected String _exception = null;
    protected boolean _ignoreException = false;

    protected ValidityCheckCapable.Mode _mode = Mode.EXACT;

    protected String _okColor = Dialog.txtLabelColor;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public FilteredTextBox() {

        this(Document.get().createTextInputElement(), "gwt-TextBox", null, false);
    }

    public FilteredTextBox(Map<String, ? extends Object> rejectionMapIn) {

        this(Document.get().createTextInputElement(), "gwt-TextBox", rejectionMapIn, false);
    }

    public FilteredTextBox(Map<String, ? extends Object> rejectionMapIn, boolean ignoreExceptionIn) {

        this(Document.get().createTextInputElement(), "gwt-TextBox", rejectionMapIn, ignoreExceptionIn);
    }

    public FilteredTextBox(Element elementIn, String styleNameIn) {

        this(elementIn, styleNameIn, null, false);
    }

    public FilteredTextBox(Element elementIn, String styleNameIn, Map<String, ? extends Object> rejectionMapIn, boolean ignoreExceptionIn) {

        super(elementIn);
        setStyleName(styleNameIn);
        _rejectionMap = rejectionMapIn;
        _ignoreException = ignoreExceptionIn;
    }

    public void setRejectionMap(Map<String, ? extends Object> rejectionMapIn) {

        _rejectionMap = rejectionMapIn;
    }

    public void setRejectionMap(Map<String, ? extends Object> rejectionMapIn, boolean ignoreExceptionIn) {

        _rejectionMap = rejectionMapIn;
        _ignoreException = ignoreExceptionIn;
    }

    public void setColorChangingLabel(Widget labelIn) {

        colorChangingLabel = labelIn;
    }

    public void setColorChangingLabel(Widget labelIn, String okColorIn) {

        colorChangingLabel = labelIn;
        _okColor = okColorIn;
    }

    public void setInitialValue(String valueIn) {

        if ((null != valueIn) && (0 < valueIn.length())) {

            if (Mode.LOWERCASE.equals(_mode)) {

                _exception = valueIn.toLowerCase();

            } else if (Mode.UPPERCASE.equals(_mode)) {

                _exception = valueIn.toUpperCase();

            } else {

                _exception = valueIn;
            }
        }
        setValue(valueIn);
    }
    
    public boolean isConditionallyValid() {
        
        if (isEnabled() && isVisible()) {
            
            return isValid();

        } else {

            if (null != colorChangingLabel) {

                colorChangingLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
            }
            getElement().getStyle().setColor(Dialog.txtLabelColor);
            return true;
        }
    }

    public boolean isValid() {

        boolean myValidFlag = true;
        String myTest = getText();
            
        if ((null != myTest) && (0 < myTest.length())) {
                
            if (Mode.LOWERCASE.equals(_mode)) {
                
               myTest = myTest.toLowerCase();
                    
            } else if (Mode.UPPERCASE.equals(_mode)) {
                
                myTest = myTest.toUpperCase();
            }
            myValidFlag = checkValue(myTest);
            
        } else if (_isRequired) {

            myValidFlag = false;
            
        }

        if (myValidFlag) {

            if (null != colorChangingLabel) {

                colorChangingLabel.getElement().getStyle().setColor(_okColor);
            }
            getElement().getStyle().setColor(Dialog.txtLabelColor);

        } else {

            if (null != colorChangingLabel) {

                colorChangingLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
            }
            getElement().getStyle().setColor(Dialog.txtErrorColor);
        }
        return myValidFlag;
    }

    public void restrictValue() {

        String myTest = getText();

        if ((null != myTest) && (0 < myTest.length())) {

            if (Mode.LOWERCASE.equals(_mode)) {

                myTest = myTest.toLowerCase();

            } else if (Mode.UPPERCASE.equals(_mode)) {

                myTest = myTest.toUpperCase();
            }
           restrictValue(myTest);
        }
    }
    
    public boolean isRequired() {
        
        return _isRequired;
    }
    
    public void setMode(Mode modeIn) {

        _mode = modeIn;

        if (null != _exception) {

            String myValue = getValue();

            setInitialValue(_exception);
            setValue(myValue);
        }
    }
    
    public void setRequired(boolean isRequiredIn) {

        _isRequired = isRequiredIn;
    }

    public void formatBox(int boxWidthIn, int borderWidthIn) {

        int myWidth = boxWidthIn - (2 * borderWidthIn) - _deltaWidth;
        int myHeight = _boxHeight - (2 * borderWidthIn);

        getElement().getStyle().setBorderWidth((double)borderWidthIn, Style.Unit.PX);
        getElement().getStyle().setBorderColor("#7f7fff");

        super.setWidth(myWidth + "px");
        super.setHeight(myHeight + "px");
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected boolean checkValue(String stringIn) {

        return ((null == _rejectionMap)
                || ((!_ignoreException) && (null != _exception) && (_exception.equals(stringIn)))
                || (!_rejectionMap.containsKey(stringIn)));
    }

    protected void restrictValue(String stringIn) {

        if (Mode.LOWERCASE.equals(_mode) || Mode.UPPERCASE.equals(_mode)) {

            setText(stringIn);
        }
    }
}

package csi.client.gwt.widget.boot;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;


public class ValidatingDialog extends Dialog {

    class ValidationPair {
        
        private ValidityCheckCapable _object;
        private boolean _conditional;
        
        public ValidationPair(ValidityCheckCapable objectIn, boolean conditionalIn) {
            
            _object = objectIn;
            _conditional = conditionalIn;
        }
        
        boolean isValid() {
            
            if (_conditional) {
                
                return _object.isConditionallyValid();
                
            } else {
                
                return _object.isValid();
            }
        }
        
        boolean equals(ValidityCheckCapable objectIn) {
            
            return _object.equals(objectIn);
        }
    }

    private boolean _monitoring = false;
    private ValidityCheck _callBack;
    private List<AbstractInputWidget> _callBackList;
    private List<ValidationPair> _list = null;

    public ValidatingDialog() {

        super();
        _list = new ArrayList<ValidationPair>();
        hideTitleCloseButton();
    }

    public ValidatingDialog(ValidityCheck callBackIn) {

        this();
        _callBack = callBackIn;
    }

    public ValidatingDialog(List<AbstractInputWidget> callBackListIn) {

        this();
        _callBackList = callBackListIn;
    }

    public ValidatingDialog(CanBeShownParent parentIn) {

        super(parentIn);
        _list = new ArrayList<ValidationPair>();
        hideTitleCloseButton();
    }

    public ValidatingDialog(ValidityCheck callBackIn, CanBeShownParent parentIn) {

        this(parentIn);
        _callBack = callBackIn;
    }

    public ValidatingDialog(List<AbstractInputWidget> callBackListIn, CanBeShownParent parentIn) {

        this(parentIn);
        _callBackList = callBackListIn;
    }

    public void setCallBack(ValidityCheck callBackIn) {

        _callBack = callBackIn;
    }

    public void addObject(ValidityCheckCapable objectIn, boolean isConditionalIn) {

        _list.add(new ValidationPair(objectIn, isConditionalIn));
    }

    public void removeObject(ValidityCheckCapable objectIn) {
        
        ValidationPair myObject = null;
        
        for (ValidationPair myTest : _list) {
            
            if (!myTest.equals(objectIn)) {
                
                myObject = myTest;
                break;
            }
        }
        
        if (null != myObject) {
            
            _list.remove(myObject);
        }
    }

    @Override
    public void show() {
        
        beginMonitoring();
        super.show();
    }
    
    @Override
    public void show(Integer widthIn) {
        
        beginMonitoring();
        super.show(widthIn);
    }
 
    @Override
    public void hide() {
        
        suspendMonitoring();
        super.hide();
    }

    public void suspendMonitoring() {

        _monitoring = false;
    }

    public void beginMonitoring() {
        
        if (! _monitoring) {
            
            _monitoring = true;
            checkValidity();
        }
    }

    protected boolean isMonitoring() {

        return _monitoring;
    }

    protected void checkValidity() {

        if (null != _callBack) {

            _callBack.checkValidity();

        } else if (null != _callBackList) {

            boolean myValidFlag = true;

            for (AbstractInputWidget myWidget : _callBackList)  {

                if (!myWidget.isValid()) {

                    myValidFlag = false;
                    break;
                }
            }
            getActionButton().setEnabled(myValidFlag);

        } else {

            boolean myValidFlag = true;

            if (null != _list) {

                for (ValidationPair myObject : _list) {

                    if (!myObject.isValid()) {

                        myValidFlag = false;
                        break;
                    }
                }
            }
            getActionButton().setEnabled(myValidFlag);
        }

        if (_monitoring ) {
            
            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }
}

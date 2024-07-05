package csi.client.gwt.csiwizard.widgets;

import com.google.gwt.dom.client.Style.Unit;

import csi.client.gwt.events.CarriageReturnEvent;
import csi.client.gwt.events.CarriageReturnEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.server.common.exception.CentrifugeException;


public class StackedInputWidget extends AbstractInputWidget {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public static class WidgetLabelPair {
        
        private AbstractInputWidget _widget;
        private String _label;
        private boolean _required;
        
        public WidgetLabelPair(AbstractInputWidget widgetIn) {
            
            _widget = widgetIn;
            _label = null;
            _required = true;
        }
        
        public WidgetLabelPair(AbstractInputWidget widgetIn, boolean requiredIn) {
            
            _widget = widgetIn;
            _label = null;
            _required = requiredIn;
        }
        
        public WidgetLabelPair(String labelIn, AbstractInputWidget widgetIn) {
            
            _widget = widgetIn;
            _label = labelIn;
            _required = true;
        }
        
        public WidgetLabelPair(String labelIn, AbstractInputWidget widgetIn, boolean requiredIn) {
            
            _widget = widgetIn;
            _label = labelIn;
            _required = requiredIn;
        }
        
        public AbstractInputWidget getWidget() {
            
            return _widget;
        }
        
        public String getLabel() {
            
            return _label;
        }
        
        public boolean isRequired() {
            
            return _required;
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    AbstractInputWidget[] widgetArray = null;
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private String[] _labelArray = null;
    private boolean[] _requiredArray = null;
    private int _arraySize = 0;
    private String _delimeter = ",";
    private boolean _required;
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ValidityReportEventHandler handleValidityReportEvent
    = new ValidityReportEventHandler() {

        @Override
        public void onValidityReport(ValidityReportEvent eventIn) {
        
            if (_required) {
                
                fireEvent(new ValidityReportEvent(isValid()));
                
            } else {
                
                fireEvent(new ValidityReportEvent(isReady()));
            }
       }
    };

    private CarriageReturnEventHandler handleCarriageReturnEvent
    = new CarriageReturnEventHandler() {

        @Override
        public void onCarriageReturn(CarriageReturnEvent eventIn) {
        
            fireEvent(new CarriageReturnEvent(isValid()));
       }
    };

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public StackedInputWidget(WidgetLabelPair[] widgetsIn) {
        
        initializeValues(widgetsIn);
    }
    
    public StackedInputWidget(WidgetLabelPair[] widgetsIn, String delimeterIn) {
        
        initializeValues(widgetsIn);
        _delimeter = delimeterIn;
    }
    
    public StackedInputWidget(WidgetLabelPair[] widgetsIn, boolean requiredIn) {
        
        initializeValues(widgetsIn);
    }
    
    public StackedInputWidget(WidgetLabelPair[] widgetsIn, String delimeterIn, boolean requiredIn) {
        
        initializeValues(widgetsIn);
        _delimeter = delimeterIn;
    }

    public String getText() throws CentrifugeException {
        
        StringBuilder myBuffer = new StringBuilder();
        
        for (int i = 0; _arraySize > i; i++) {

            if (0 < i) {

                myBuffer.append(_delimeter);
            }

            if (widgetArray[i].isValid()) {
                
                String myLabel = _labelArray[i];
                String myValue = widgetArray[i].getText();

                if (null != myLabel) {
                    
                    myBuffer.append("(");
                    myBuffer.append(myLabel);
                    myBuffer.append(" :: ");
                    myBuffer.append((null != myValue) ? myValue : "");
                    myBuffer.append(")");
                    
                } else {
                    
                    myBuffer.append((null != myValue) ? myValue : "");
                }
            }
        }
        return myBuffer.toString();
    }
    
    public void resetValue() {
        
        for (int i = 0; _arraySize > i; i++) {
            
            widgetArray[i].resetValue();
        }
        
    }
    
    public boolean isValid() {
        
        boolean myValidFlag = true;

        for (int i = 0; _arraySize > i; i++) {
            
            if (_requiredArray[i] && (!widgetArray[i].isValid())) {
                
                myValidFlag = false;
                break;
            }
        }
        return myValidFlag;
    }
    
    public boolean isReady() {
        
        boolean myValidFlag = isValid();

        if ((!myValidFlag) && (!_required)) {
            
            myValidFlag = true;
            
            for (int i = 0; _arraySize > i; i++) {
                
                if (!widgetArray[i].atReset()) {
                    
                    myValidFlag = false;
                    break;
                }
            }
        }
        return myValidFlag;
    }
    
    public void grabFocus() {
        
        widgetArray[0].grabFocus();
    }
    
    public int getRequiredHeight() {
        
        int myRequiredHeight = 0;
        
        for (AbstractInputWidget myWidget : widgetArray) {
            
            myRequiredHeight += myWidget.getRequiredHeight();
        }
        return myRequiredHeight;
    }

    public boolean atReset() {
        
        boolean myAtReset = true;
        
        for (int i = 0; _arraySize > i; i++) {
            
            if (!widgetArray[i].atReset()) {
                
                myAtReset = false;
                break;
            }
        }
        
        return myAtReset;
    }
    
    @Override
    public void suspendMonitoring() {

        for (int i = 0; _arraySize > i; i++) {
            
            widgetArray[i].suspendMonitoring();
        }
    }
    
    @Override
    public void beginMonitoring() {

        for (int i = 0; _arraySize > i; i++) {
            
            widgetArray[i].beginMonitoring();
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void layoutDisplay() {
        
        int mySubHeight = getHeight() / _arraySize;
        int myWidth = getWidth();
        
        for (int i = 0; _arraySize > i; i++) {
            
            widgetArray[i].setPixelSize(myWidth, mySubHeight);;
            setWidgetTopHeight(widgetArray[i], (i  * mySubHeight), Unit.PX, mySubHeight, Unit.PX);
            setWidgetLeftWidth(widgetArray[i], 0, Unit.PX, myWidth, Unit.PX);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeValues(WidgetLabelPair[] widgetsIn) {
        
        _arraySize = widgetsIn.length;
        widgetArray = new AbstractInputWidget[_arraySize];
        _labelArray = new String[_arraySize];
        _requiredArray = new boolean[_arraySize];
        
        for (int i = 0; _arraySize > i; i++) {
            
            WidgetLabelPair myPair = widgetsIn[i];
            
            widgetArray[i] = myPair.getWidget();
            _labelArray[i] = myPair.getLabel();
            _requiredArray[i] = myPair.isRequired();

            add(widgetArray[i]);
        }
        
        for (int i = 0; _arraySize > i; i++) {
            widgetArray[i].addValidityReportEventHandler(handleValidityReportEvent);
        }
    }
}

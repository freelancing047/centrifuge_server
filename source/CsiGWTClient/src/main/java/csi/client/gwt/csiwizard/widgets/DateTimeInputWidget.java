package csi.client.gwt.csiwizard.widgets;

import java.util.Date;

import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.input_boxes.FilteredDateTimeBox;
import csi.server.common.exception.CentrifugeException;


public class DateTimeInputWidget extends AbstractInputWidget {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    FilteredDateTimeBox datePicker = null;
    Label parameterFormat = null;
    HorizontalPanel labelPanel = null;
    VerticalPanel formatPanel = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    //    protected Date _defaultDate = null;
    protected String _defaultDate = null;

    protected int _formatWidth = 0;
    protected boolean _initialized = false;
    protected boolean _needDate = true;
    protected boolean _needTime = true;
    protected HasViewMode.ViewMode _startView = null;
    protected HasViewMode.ViewMode _minView = null;
    protected HasViewMode.ViewMode _maxView = null;

    private boolean _monitoring = false;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DateTimeInputWidget() throws CentrifugeException {

        this(true, true);
    }

    public DateTimeInputWidget(boolean needDateIn, boolean needTimeIn) throws CentrifugeException {

        super();

        try {

            _needDate = needDateIn;
            _needTime = needTimeIn;

            initializeObject(_needDate ? _needTime ? "Select date and time" : "Select date" : _needTime ? "Select time" : "? ? ?");

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public DateTimeInputWidget(String promptIn, String defaultIn, boolean needDateIn, boolean needTimeIn) throws CentrifugeException {

        this(promptIn, defaultIn, needDateIn, needTimeIn, true);
    }

    public DateTimeInputWidget(String promptIn, String defaultIn, boolean needDateIn, boolean needTimeIn, boolean requiredIn) throws CentrifugeException {

        super(requiredIn);

        try {

            _needDate = needDateIn;
            _needTime = needTimeIn;

            //
            // Initialize the display objects
            //
            if (_needDate) {

                if (_needTime) {

                    configureDateTimeMode(promptIn);

                } else {

                    configureDateMode(promptIn);
                }

            } else {

                if (_needTime) {

                    configureTimeMode(promptIn);

                } else {

                    initializeObject(promptIn);
                }
            }
//        _defaultDate = (null != datePicker) ? datePicker.getValue(defaultIn) : null;
//        if (null != datePicker) datePicker.setValue(_defaultDate);
            _defaultDate = defaultIn;
            if (null != datePicker) {

                datePicker.setText(_defaultDate);
            }

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public DateTimeInputWidget(String promptIn, String defaultIn) {

        this(promptIn, defaultIn, true);
    }

    public DateTimeInputWidget(String promptIn, String defaultIn, boolean requiredIn) {

        super(requiredIn);

        try {

            //
            // Initialize the display objects
            //
            initializeObject(promptIn);
//        _defaultDate = (null != datePicker) ? datePicker.getValue(defaultIn) : null;
//        if (null != datePicker) datePicker.setValue(_defaultDate);
            _defaultDate = defaultIn;
            if (null != datePicker) datePicker.setText(_defaultDate);

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public void configureDateTimeMode(String promptIn) {

        try {

            _startView = null;
            _minView = null;
            _maxView = null;
            initializeObject(promptIn);
            layoutDisplay();

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public void configureDateMode(String promptIn) {

        try {

            _startView = HasViewMode.ViewMode.MONTH;
            _minView = HasViewMode.ViewMode.MONTH;
            _maxView = null;
            initializeObject(promptIn);
            layoutDisplay();

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public void configureTimeMode(String promptIn) {

        try {

            _startView = HasViewMode.ViewMode.DAY;
            _minView = null;
            _maxView = HasViewMode.ViewMode.DAY;
            initializeObject(promptIn);
            layoutDisplay();

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public void setFormatWidth(String formatWidthIn) {

        try {

            _formatWidth = decode(formatWidthIn);

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public Date getValue() {

        return (null != datePicker) ? datePicker.getValue() : null;
    }

    public void setCurrent() {

        setValue(new Date());
    }

    public void setValue(Date valueIn) {

        try {

            setValue(formatValue(valueIn));

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    @Override
    public void setRequired(boolean requiredIn) {

        super.setRequired(requiredIn);
        if (null != datePicker) datePicker.setRequired(requiredIn);
    }

    @Override
    public String getText() {

        String myResult = (null != datePicker) ? datePicker.getText() : null;

        return ((null == myResult) || (0 == myResult.length())) ? null : myResult;
    }

    @Override
    public void setValue(String valueIn) {

//        if (null != datePicker) datePicker.setValue(_defaultDate);
        if (null != datePicker) datePicker.setText(valueIn);
    }

    @Override
    public void resetValue() {

//        if (null != datePicker) datePicker.setValue(_defaultDate);
        if (null != datePicker) datePicker.setText(_defaultDate);
    }

    public void setDefault(String defaultDateIn) {

        _defaultDate = defaultDateIn;

        if (_initialized) {

            if (null != datePicker) datePicker.setText(_defaultDate);
        }
    }

    public void setPrompt(String promptIn) {

        try {

            if (_initialized) {

                parameterPrompt.setText(promptIn);

            } else if (null != parameterPrompt) {

                initializeObject(promptIn);
            }

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    public boolean isValid() {
        
        return (null != datePicker) ? datePicker.isValid() : false;
    }
    
    public void grabFocus() {
        
        DeferredCommand.add(new Command() {
            public void execute() {
                if (null != datePicker) datePicker.grabFocus();
            }
        });
    }
    
    public int getRequiredHeight() {
        
        int myRequiredHeight = Dialog.intTextBoxHeight;
        
        if (null != parameterPrompt) {

           myRequiredHeight += Dialog.intLabelHeight;
        }
        
        return myRequiredHeight;
    }
    
    public int getRequestedHeight() {

        try {

            return super.getRequestedHeight() + (2 * Dialog.intTextBoxHeight);

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
        return 0;
    }

    public String formatValue(Date valueIn) {

        try {

            return (null != datePicker) ? datePicker.formatValue(valueIn) : null;

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
        return null;
    }

    @Override
    public boolean atReset() {

        try {

            return (null != datePicker) ? datePicker.atReset() : true;

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
        return true;
    }
    
    @Override
    public void suspendMonitoring() {

        _monitoring = false;
    }
    
    @Override
    public void beginMonitoring() {

        try {

            if (! _monitoring) {

                _monitoring = true;
                checkValidity();
            }

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    @Override
    public void destroy() {

        try {

            suspendMonitoring();

            if (null != datePicker) {
                datePicker.destroy();
                datePicker = null;
            }

        } catch (Exception myException) {

            Dialog.showException("DateTimeInputWidget", myException);
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected void initializeObject(String promptIn) {
        
        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(promptIn);
        
        //
        // Wire in the handlers
        //
        wireInHandlers();

        _initialized = true;
    }
    
    protected void wireInHandlers() {
        
    }
    
    protected void layoutDisplay() {

        int myWidth = getWidth();
        int  myRightMargin = getRightMargin();
        int myFormatWidth = (0 < _formatWidth) ? _formatWidth : (myWidth - myRightMargin) / 2;
        int myInputTop = ((null != parameterPrompt) || (null != parameterFormat))
                ? Dialog.intLabelHeight : 0;

        if (null != parameterPrompt) {
            setWidgetTopHeight(parameterPrompt, 0, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetLeftWidth(parameterPrompt, 0, Unit.PX, myWidth, Unit.PX);
        }

        if (null != parameterFormat) {
            setWidgetTopHeight(parameterFormat, 0, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetRightWidth(parameterFormat, myRightMargin, Unit.PX, myFormatWidth, Unit.PX);
        }

        if (null != datePicker) {
            setWidgetTopHeight(datePicker, _buttonAlignmentTop, Unit.PX, Dialog.intTextBoxHeight, Unit.PX);
            setWidgetLeftRight(datePicker, 0, Unit.PX, myRightMargin, Unit.PX);
        }
    }

    @Override
    protected void centerAddButton() {
        
        if (null != addButton) {

            super.centerAddButton();
            setWidgetTopHeight(addButton, _buttonAlignmentTop, Unit.PX, getButtonHeight(), Unit.PX);
        }
    }

    public void setBoxWidth(String stringIn) {

        datePicker.setBoxWidth(stringIn);
    }

    protected HasViewMode.ViewMode getStartView() {
        return _startView;
    }

    protected HasViewMode.ViewMode getMinimumView() {
        return _minView;
    }
    
    protected HasViewMode.ViewMode getMaximumView() {
        return _maxView;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private void createWidgets(String promptIn) {

        try {

            HasViewMode.ViewMode myStartView = getStartView();
            HasViewMode.ViewMode myMinView = getMinimumView();
            HasViewMode.ViewMode myMaxView = getMaximumView();

            clear();

            datePicker = new FilteredDateTimeBox(_needDate, _needTime, _required);
            datePicker.setAutoClose(true);
            //datePicker.setReadOnly(true);

            if (null != myStartView) {
                
                datePicker.setStartView(myStartView);
            }

            if (null != myMinView) {
                
                datePicker.setMinView(myMinView);
            }

            if (null != myMaxView) {
                
                datePicker.setMaxView(myMaxView);
            }
            
            datePicker.setText(_defaultDate);
            add(datePicker);

            parameterPrompt = new Label();
            parameterPrompt.setText(promptIn);
            add(parameterPrompt);

            parameterFormat = new Label();
            parameterFormat.asWidget().getElement().getStyle().setColor(Dialog.txtPatternColor);
            parameterFormat.setText(datePicker.getFormat());
            add(parameterFormat);

        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }
    
    private void checkValidity() {

        if (null != datePicker) {

            reportValidity(datePicker.isValid(), datePicker.atReset());
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

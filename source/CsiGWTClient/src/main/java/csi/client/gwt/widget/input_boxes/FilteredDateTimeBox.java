package csi.client.gwt.widget.input_boxes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;
import com.github.gwtbootstrap.datetimepicker.client.ui.DateTimeBox;
import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;
import csi.shared.core.Constants;

public class FilteredDateTimeBox extends DateTimeBox implements ValidityCheckCapable {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    public static final long _oneMinute = 60000;
    class Calendar {

        public static final int YEAR = 0;
        public static final int MONTH = 1;
        public static final int DAY_OF_MONTH = 2;
        public static final int HOUR = 3;
        public static final int MINUTE = 4;
        public static final int SECOND = 5;


        private int[] _value = {0, 0, 0, 0, 0, 0};

        public Calendar() {

        }

        public Calendar(Date dateTimeIn, boolean clearDateIn, boolean clearTimeIn) {

            if (clearDateIn) {

                _value[0] = 0;
                _value[1] = 0;
                _value[2] = 0;

            } else {

                _value[0] = dateTimeIn.getYear();
                _value[1] = dateTimeIn.getMonth();
                _value[2] = dateTimeIn.getDate();
            }

            if (clearTimeIn) {

                _value[3] = 0;
                _value[4] = 0;
                _value[5] = 0;

            } else {

                _value[3] = dateTimeIn.getHours();
                _value[4] = dateTimeIn.getMinutes();
                _value[5] = dateTimeIn.getSeconds();
            }
        }

        public void set(int componentIn, int valueIn) {

            if ((0 <= componentIn) && (6 > componentIn)) {

                _value[componentIn] = valueIn;
            }
        }

        public int getValue(int componentIn) {

            int myValue = 0;

            if ((0 <= componentIn) && (6 > componentIn)) {

                myValue = _value[componentIn];
            }
            return myValue;
        }

        public Date getTime() {

            return new Date(_value[YEAR],
                            _value[MONTH],
                            _value[DAY_OF_MONTH],
                            _value[HOUR],
                            _value[MINUTE],
                            _value[SECOND]) ;
        }
    }

    class DtFormatItem {

        private int _begin;
        private int _end;
        private int _component;
        private int _adjustment;
        private int  _value;

        public DtFormatItem(int beginIn, int endIn, int componentIn, int adjustmentIn) {

            _begin = beginIn;
            _end = endIn;
            _component = componentIn;
            _adjustment = adjustmentIn;
        }

        public Calendar extractValue(Calendar calendarIn, String stringIn, Integer minIn, Integer maxIn) {

            Calendar myCalendar = calendarIn;

            _value = minIn;

            if (_begin < stringIn.length()) {

                if (_end <= stringIn.length()) {

                    _value = 0;

                    for (int i = _begin; _end > i; i++) {

                        char myCharacter = stringIn.charAt(i);

                        if (('0' <= myCharacter) && ('9' >= myCharacter)) {

                            _value = (_value * 10) + (int)myCharacter - (int)'0';

                        } else {

                            myCalendar = null;
                            break;
                        }
                    }

                    if (null != myCalendar) {

                        if (((null != minIn) && (minIn > _value))
                                || ((null != maxIn) && (maxIn < _value))) {

                            myCalendar = null;
                        }
                    }

                } else {

                    myCalendar = null;
                }

            }

            if (null != myCalendar) {

                _value += _adjustment;

                myCalendar.set(_component, _value);
            }

            return myCalendar;
        }

        public int getValue() {

            return _value;
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int BASE_YEAR = 1900;
    private static final int BASE_MONTH = 1;
    
    private static final int[] _daysInMonth
    = {
        31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31,
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31,
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31,
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        };
    
    private static final int[] _sizeList = {4, 2, 2, 2, 2, 2};
    
    private static final Integer[] _minList = {0, 1, 1, 0, 0, 0};
    
    private static final Integer[] _maxList = {null, 12, null, 23, 59, 59};
    
    private static final int[] _adjustmentList = {BASE_YEAR, BASE_MONTH, 0, 0, 0, 0};
    
    private static final String[] _componentName
    = {"year", "month", "day", "hour", "minute", "second"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    
    private static final int[] _componentList
    = {Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
        Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND};
    
    private static final Map<Character, Integer> _componentMap;
    
    private static final Integer _componentMax = 6;
    private static final Integer _yearComponent = 0;
    private static final Integer _monthComponent = 1;
    private static final Integer _dayComponent = 2;
    private static final Integer _hourComponent = 3;
    private static final Integer _minuteComponent = 4;
    private static final Integer _secondComponent = 5;

    static {
        _componentMap = new HashMap<Character, Integer>();
        _componentMap.put(new Character('y'), _yearComponent);
        _componentMap.put(new Character('m'), _monthComponent);
        _componentMap.put(new Character('d'), _dayComponent);
        _componentMap.put(new Character('h'), _hourComponent);
        _componentMap.put(new Character('i'), _minuteComponent);
        _componentMap.put(new Character('s'), _secondComponent);
    }

    private static final long _millisecondsPerDay = 24L * 60L * 60L * 1000L;

    private int[] _offsetList = null;
    private List<Integer> _offsets = null;
    private List<String> _constants = null;
    private DtFormatItem[] _formatItem = null;
    private int _minimumLength = 0;
    private String _format = null;

    protected boolean _isRequired = true;
    protected Date _minValue = null;
    protected Date _maxValue = null;
    protected boolean _needDate = true;
    protected boolean _needTime = true;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Static Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static FilteredDateTimeBox createDateSelector() throws CentrifugeException {

        try {

            FilteredDateTimeBox myWidget = new FilteredDateTimeBox(true, false, true);
            myWidget.setAutoClose(true);
            myWidget.setStartView(HasViewMode.ViewMode.MONTH);
            myWidget.setMinView(HasViewMode.ViewMode.MONTH);
            return myWidget;

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }

    public static FilteredDateTimeBox createTimeSelector() throws CentrifugeException {

        try {

            FilteredDateTimeBox myWidget = new FilteredDateTimeBox(false, true, true);
            myWidget.setAutoClose(true);
            myWidget.setStartView(HasViewMode.ViewMode.DAY);
            myWidget.setMaxView(HasViewMode.ViewMode.DAY);
            myWidget.setMinuteStep(1);
            return myWidget;

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }

    public static FilteredDateTimeBox createDateTimeSelector() throws CentrifugeException {

        try {

            FilteredDateTimeBox myWidget = new FilteredDateTimeBox(true, true, true);
            myWidget.setAutoClose(true);
            myWidget.setStartView(HasViewMode.ViewMode.MONTH);
            myWidget.setMinuteStep(1);
            return myWidget;

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }

    private HideHandler handleHide = new HideHandler() {
        @Override
        public void onHide(HideEvent eventIn) {

            setValue(normalize(getSelectedValue()));
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FilteredDateTimeBox() {

        this(true);
    }

    public FilteredDateTimeBox(boolean isRequiredIn) {

        super();

        try {

            _isRequired = isRequiredIn;

            super.setValue(super.getValue());

            super.addHideHandler(handleHide);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public FilteredDateTimeBox(boolean needDateIn, boolean needTimeIn)throws CentrifugeException {

        this(null, null, needDateIn, needTimeIn);
    }

    public FilteredDateTimeBox(boolean needDateIn, boolean needTimeIn, boolean isRequiredIn) throws CentrifugeException {

        this(null, null, needDateIn, needTimeIn, isRequiredIn);
    }

    public FilteredDateTimeBox(Date minValueIn) throws CentrifugeException {
        
        this(minValueIn, null);
    }

    public FilteredDateTimeBox(Date minValueIn, Date maxValueIn) throws CentrifugeException {

        this(minValueIn, maxValueIn, true, true);
    }

    public FilteredDateTimeBox(Date minValueIn, Date maxValueIn, boolean isRequiredIn) throws CentrifugeException {

        this(minValueIn, maxValueIn, true, true, isRequiredIn);
    }

    public FilteredDateTimeBox(Date minValueIn, Date maxValueIn, boolean needDateIn, boolean needTimeIn) throws CentrifugeException {

        this(minValueIn, maxValueIn, needDateIn, needTimeIn, true);
    }

    public FilteredDateTimeBox(Date minValueIn, Date maxValueIn, boolean needDateIn, boolean needTimeIn, boolean isRequiredIn) throws CentrifugeException {

        this(isRequiredIn);

        try {

            _minValue = minValueIn;
            _maxValue = maxValueIn;
            _needDate = needDateIn;
            _needTime = needTimeIn;

            if (_needDate) {

                if (_needTime) {

                    setFormat(Constants.DataConstants.FORMAT_PICKER_DATE_TIME);

                } else {

                    setFormat(Constants.DataConstants.FORMAT_PICKER_DATE);
                }

            } else {

                if (_needTime) {

                    setFormat(Constants.DataConstants.FORMAT_PICKER_TIME);

                } else {

                    throw new CentrifugeException(i18n.dateTimeInputRequireDateException()); //$NON-NLS-1$
                }
                setText(formatResult(new Calendar(currentDateTime(), false, true)));
            }

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public void destroy() {

        try {

            hide();
            removeFromParent();

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public void setMinimum(Date minValueIn) {

        try {

            _minValue = (minValueIn);
            setStartDate_(_minValue);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }
    
    public void setMaximum(Date maxValueIn) {

        try {

            _maxValue = maxValueIn;
            setEndDate_(_maxValue);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public String getFormat() {

        return _format;
    }
    
    public boolean isConditionallyValid() {

        try {

            if (isEnabled() && isVisible()) {

                return isValid();

            } else {

                getBox().getElement().getStyle().setColor(Dialog.txtLabelColor);
                return true;
            }

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return false;
    }

    public boolean atReset() {

        try {

            String myInput = getBox().getValue();

            return ((null == myInput) || (0 == myInput.length()));

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return false;
    }

    public boolean isValid() {

        try {

            boolean myValidFlag = false;

            getBox().getElement().getStyle().setColor(Dialog.txtErrorColor);

            if (hasText()) {

                Date myDate = getValue();

                if (null != myDate) {

                    if (((null == _minValue) || myDate.equals(_minValue) || myDate.after(_minValue))
                            && ((null == _maxValue) || myDate.equals(_maxValue) || myDate.before(_maxValue))) {

                        getBox().getElement().getStyle().setColor(Dialog.txtLabelColor);
                        return true;

                    }
                }

            } else {

                myValidFlag = !_isRequired;

            }
            return myValidFlag;

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return false;
    }
    
    public boolean isRequired() {
        
        return _isRequired;
    }
    
    public void setRequired(boolean isRequiredIn) {

        _isRequired = isRequiredIn;
    }
    
    public String getText() {

        try {

            Calendar myCalendar = parseText(getBox().getValue());
            String myResult = ""; //$NON-NLS-1$

            if (null != myCalendar) {

                myResult = formatResult(myCalendar);
            }
            return myResult;

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }
    
    public void setText(String stringIn) {

        try {

            getBox().setValue(stringIn);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    @Override
    public boolean isEnabled() {

        try {

            return getBox().isEnabled();

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return false;
    }

    public Date getSelectedValue() {

        try {

            return super.getValue();

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }

    @Override
    public void setValue(Date valueIn) {

        try {

            setText(formatValue(valueIn));

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public void setCurrent() {

        try {

            setValue(currentDateTime());

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public Date currentDateTime() {

        try {

            return new Date((new Date().getTime() / 1000) * 1000);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }

    @Override
    public Date getValue() {

        try {

            return getValue(getBox().getValue());

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }

    public Date getValue(String stringIn) {

        Date myValue = null;

        try {

            Calendar myCalendar = parseText(stringIn);

            if (null != myCalendar) {

                myValue = myCalendar.getTime();
            }

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return myValue;
    }

    public String formatValue(Date valueIn) {

        try {

            return (null != valueIn) ? formatResult(new Calendar(valueIn, !_needDate, !_needTime)) : null;

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
        return null;
    }
    
    @Override
    public void setFormat(String formatIn) {

        try {

            processFormat(formatIn);
            _format = formatIn;
            getBox().setTitle(_format);
            super.setFormat(formatIn);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public void setBoxWidth(String stringIn) {

        try {

            getBox().setWidth(stringIn);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }

    public void grabFocus() {

        try {

            getBox().setFocus(true);

        } catch (Exception myException) {

            Dialog.showException("FilteredDateTimeBox", myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private Date normalize(Date valueIn) {

        if (null != valueIn) {

            valueIn.setTime((valueIn.getTime()/_oneMinute) * _oneMinute);
        }
        return valueIn;
    }

    private boolean hasText() {
        
        return ((null != getBox().getValue()) && (0 < getBox().getValue().length()));
    }

    private Calendar parseText(String stringIn) {
        
        Calendar myCalendar = null;
        int myBase = (_needDate) ? _yearComponent :_hourComponent;
        int myTop = (_needTime) ? _secondComponent :_dayComponent;

        if (null != stringIn) {
            
            int myLimit = stringIn.length();
            
            if (myLimit >= _minimumLength) {
                
                myCalendar = new Calendar();
            
                // Verify format of the string encompassing the values to be extracted.
                for (int m = 0; _constants.size() > m; m++) {
                    
                    String myConstant = _constants.get(m);
                    int myOffset = _offsets.get(m);
                    int mySize = myConstant.length();
                    
                    for (int i = myOffset, j = 0; (myLimit > i) && (mySize > j); i++, j++) {
                        
                        if (stringIn.charAt(i) != myConstant.charAt(j)) {
                            
                            myCalendar = null;
                            break;
                        }
                    }
                }

                for (int i = myBase; (null != myCalendar) && (myTop >= i) && (null != _formatItem[i]); i++) {
                    
                    myCalendar = getComponent(myCalendar, stringIn, i);
                }
            }
        }
        return myCalendar;
    }

    private Calendar getComponent(Calendar calendarIn, String inputIn, int indexIn) {
        
        Calendar myCalendar = calendarIn;
        Integer myMax = _maxList[indexIn];
        
        if (_dayComponent == indexIn) {
            
            int myMultiplier = _formatItem[_yearComponent].getValue() % 4;
            int myOffset = _formatItem[_monthComponent].getValue();
            
            myMax = _daysInMonth[(12 * myMultiplier) + myOffset];
        }
        
        myCalendar = _formatItem[indexIn].extractValue(calendarIn, inputIn, _minList[indexIn], myMax);
        
        return myCalendar;
    }
    
    private void processFormat(String formatIn) throws CentrifugeException {
        
        StringBuilder myConstant = null;
        
        initializeTables();
        
        for (int i = 0; formatIn.length() > i; ) {
            
            char myCharacter = formatIn.charAt(i);
            
            Integer myComponentIndex = _componentMap.get(myCharacter);
            
            if (null != myComponentIndex) {
                
                if (null != myConstant) {
                    
                    _constants.add(myConstant.toString());
                    myConstant = null;
                }
                
                i = createComponent(myComponentIndex, formatIn, i);
                
                if ((formatIn.length() < _minimumLength)
                        && ((!_needDate) || (null != _formatItem[_yearComponent])
                        && (null != _formatItem[_monthComponent]) && (null != _formatItem[_dayComponent])))  {
                    
                    _minimumLength = i;
                }
                
            } else {
                
                if (null == myConstant) {
                    
                    myConstant = new StringBuilder();
                    _offsets.add(i);
                }
                
                myConstant.append(myCharacter);
                i++;
            }
        }
        if (null != myConstant) {
            
            _constants.add(myConstant.toString());
        }
    }
    
    private int createComponent(Integer indexIn, String formatIn, int offsetIn) throws CentrifugeException {
        
        if (null == _formatItem[indexIn]) {
            
            char myMatch = formatIn.charAt(offsetIn);
            int myOffset;
            
            for (myOffset = offsetIn; (offsetIn + _sizeList[indexIn]) > myOffset; myOffset++) {
                
                if (formatIn.charAt(myOffset) != myMatch) {
                    
                    throw new CentrifugeException(i18n.filteredDateTimeBoxLengthMinError(_componentName[indexIn], Integer.toString(_sizeList[indexIn]))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            
            
            _formatItem[indexIn] = new DtFormatItem(offsetIn, myOffset, _componentList[indexIn], -_adjustmentList[indexIn]);
            _offsetList[indexIn] = offsetIn;
            
            return myOffset;
            
        } else {
            
            if (_componentMap.get(formatIn.charAt(offsetIn - 1)) == indexIn) {
                
                throw new CentrifugeException(i18n.filteredDateTimeBoxLengthMaxError(_componentName[indexIn], Integer.toString(_sizeList[indexIn])));
                
            } else {
                
                throw new CentrifugeException(i18n.filteredDateTimeBoxDuplicateError(_componentName[indexIn])); //$NON-NLS-1$
            }
        }
    }
    
    private String formatResult(Calendar calendarIn) {
        
        char[] myBuffer = _format.toCharArray();
        int myBase = (_needDate) ? _yearComponent :_hourComponent;
        int myTop = (_needTime) ? _secondComponent :_dayComponent;

        for (int i = myBase; myTop >= i; i++) {
            
            if (null != _formatItem[i]) {
                
                int myStart = _offsetList[i] + _sizeList[i] - 1;
                int myEnd = _offsetList[i] - 1;
                int myValue = calendarIn.getValue(_componentList[i]) + _adjustmentList[i];
                
                for (int j = myStart; myEnd < j; j--) {
                    
                    myBuffer[j] = (char)((int)'0' + (myValue % 10));
                    myValue = myValue / 10;
                }
                
            } else {
                
                break;
            }
        }
        return new String(myBuffer);
    }
    
    private void initializeTables() {

        _offsetList = new int[_componentMax];
        _offsets = new ArrayList<Integer>();
        _constants = new ArrayList<String>();
        _formatItem = new DtFormatItem[_componentMax];
        _minimumLength = 65535;
    }
}

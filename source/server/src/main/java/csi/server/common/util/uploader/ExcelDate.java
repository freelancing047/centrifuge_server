package csi.server.common.util.uploader;

import java.sql.Timestamp;
import java.util.Date;

import csi.server.common.util.ByteBuffer;
import csi.server.common.util.EncodingByteValues;

/**
 * Created by centrifuge on 10/16/2015.
 */

class CsiExcelCalendar {

    private static final int _daysPerYear = 365;
    private static final int _daysPerLeapYear = 366;
    private static final int _daysPerQuadYear = (3 * _daysPerYear) + _daysPerLeapYear;

    private static final int[] _normalDaysPerMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int[] _leapYearDaysPerMonth = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static final int[][] _calendarChoice = {_leapYearDaysPerMonth, _normalDaysPerMonth,
                                                    _normalDaysPerMonth, _normalDaysPerMonth};

    private int _year;
    private int _month;
    private int _day;

    CsiExcelCalendar(Integer excelDaysIn) {

        int myWorkingDays = excelDaysIn - 1;
        int myQuads = (int)(myWorkingDays / (long)_daysPerQuadYear);
        int myExtraDays = (int)(myWorkingDays % (long)_daysPerQuadYear);
        int myExtraYears = myExtraDays / _daysPerYear;
        int[] myDaysPerMonth;

        myExtraDays -= (myExtraYears * _daysPerYear);
        if (0 < myExtraYears) {

            if (0 < myExtraDays) {

                myExtraDays--;

            } else {

                myExtraDays = _daysPerYear;
                myExtraYears--;
            }
        }
        myDaysPerMonth = _calendarChoice[myExtraYears];

        for (int i = 0; myDaysPerMonth.length > i; i++) {

            if (myDaysPerMonth[i] > myExtraDays) {

                _month = i;
                _day = myExtraDays;
                break;
            }
            myExtraDays -= myDaysPerMonth[i];
        }
        _year = (4 * myQuads) + myExtraYears;
    }

    public int getYear() {

        return _year + 1900;
    }

    public int getMonth() {

        return _month + 1;
    }

    public int getDay() {

        return _day + 1;
    }
}
/* Currently do not support scanning string values to acquire date/time values
   which would be necessary to support dates prior to Jan 1, 1900.
 */
public class ExcelDate {

    private static final int _millisecondsPerSecond = 1000;
    private static final int _secondsPerMinute = 60;
    private static final int _minutesPerHour = 60;
    private static final int _hoursPerDay = 24;
    private static final int _secondsPerHour = _secondsPerMinute * _minutesPerHour;
    private static final int _secondsPerDay = _secondsPerHour * _hoursPerDay;
    private static final int _millisecondsPerMinute = _secondsPerMinute * _millisecondsPerSecond;
    private static final int _millisecondsPerHour = _secondsPerHour * _millisecondsPerSecond;
    private static final int _millisecondsPerDay = _secondsPerDay * _millisecondsPerSecond;
    private static final int _converterDelta = 25569;

    private static final byte[] _noDate = { EncodingByteValues.asciiOne, EncodingByteValues.asciiNine,
                                            EncodingByteValues.asciiZero, EncodingByteValues.asciiZero,
                                            EncodingByteValues.asciiMinus, EncodingByteValues.asciiZero,
                                            EncodingByteValues.asciiOne, EncodingByteValues.asciiMinus,
                                            EncodingByteValues.asciiZero, EncodingByteValues.asciiOne };
    private static final byte[] _noTime = { EncodingByteValues.asciiZero, EncodingByteValues.asciiZero,
                                            EncodingByteValues.asciiColon, EncodingByteValues.asciiZero,
                                            EncodingByteValues.asciiZero, EncodingByteValues.asciiColon,
                                            EncodingByteValues.asciiZero, EncodingByteValues.asciiZero,
                                            EncodingByteValues.asciiDot, EncodingByteValues.asciiZero,
                                            EncodingByteValues.asciiZero, EncodingByteValues.asciiZero };

    private CsiExcelCalendar _calendar = null;

    private byte[] _byteBuffer = null;
    private Double _fractionalDaySinceMidnight = null;
    private Integer _excelDaysSince1900 = null;
    private Integer _actualDaysSince1900 = null;
    private Integer _totalDaysSince1970 = null;
    private Integer _millisecondsSinceMidnight = null;

    private boolean _hasDate = false;
    private boolean _hasTime = false;

    public ExcelDate(byte[] bufferIn){

        _byteBuffer = bufferIn;
        extractExcelTimeStamp();
    }

    public ExcelDate(String stringIn){

        this(stringIn.getBytes());
    }

    public Timestamp getSqlTimestamp() {

        return (_hasDate || _hasTime)
                ? new Timestamp((_totalDaysSince1970 * _millisecondsPerDay)
                + _millisecondsSinceMidnight)
                : null;
    }

    public Date getJavaDateTime() {

        return (_hasDate || _hasTime)
                ? new Date((_totalDaysSince1970 * _millisecondsPerDay)
                + _millisecondsSinceMidnight)
                : null;
    }

    public Date getJavaDate() {

        return (_hasDate)
                ? new Date(_totalDaysSince1970 * _millisecondsPerDay)
                : null;
    }

    public Date getJavaTime() {

        return (null != _actualDaysSince1900)
                ? new Date(_millisecondsSinceMidnight)
                : null;
    }

    public Long getUnixDateTime() {

        return (_hasDate || _hasTime)
                ? (_totalDaysSince1970 * _secondsPerDay) + ((_millisecondsSinceMidnight + 500L) / 1000L)
                : null;
    }

    public boolean isDateTime() {

        return (_hasDate && _hasTime);
    }

    public boolean isDateOnly() {

        return (_hasDate && (!_hasTime));
    }

    public boolean isTimeOnly() {

        return ((!_hasDate) && _hasTime);
    }

    public String formatSqlDateTime() {

        ByteBuffer myBuffer = formatTime(formatDate(new ByteBuffer()).append(EncodingByteValues.asciiBlank));

        return (1 < myBuffer.length()) ? myBuffer.toString() : null;
    }

    public String formatSqlDate() {

        ByteBuffer myBuffer = formatDate(null);

        return (null != myBuffer) ? myBuffer.toString() : null;
    }

    public String formatSqlTime() {

        ByteBuffer myBuffer = formatTime(null);

        return (null != myBuffer) ? myBuffer.toString() : null;
    }

    public ByteBuffer formatSqlDateTime(ByteBuffer bufferIn, byte[] nullIndicatorIn) {

        int myCount1 = bufferIn.append(EncodingByteValues.asciiQuote).length();
        int myCount2 = myCount1;

        if (formatDate(bufferIn).length() > myCount1) {

            myCount2 = bufferIn.append(EncodingByteValues.asciiBlank).length();
        }

        if (formatTime(bufferIn).length() > myCount2) {

            bufferIn.append(EncodingByteValues.asciiQuote);

        } else if (myCount2 > myCount1) {

            bufferIn.truncate(myCount2).append(EncodingByteValues.asciiQuote);

        } else {

            bufferIn.truncate(myCount1).append(nullIndicatorIn);
        }

        return bufferIn;
    }

    public ByteBuffer formatSqlDate(ByteBuffer bufferIn, byte[] nullIndicatorIn) {

        int myCount = bufferIn.append(EncodingByteValues.asciiQuote).length();

        if (formatDate(bufferIn).length() > myCount) {

            bufferIn.append(EncodingByteValues.asciiQuote);

        } else {

            bufferIn.truncate(myCount).append(nullIndicatorIn);
        }

        return bufferIn;
    }

    public ByteBuffer formatSqlTime(ByteBuffer bufferIn, byte[] nullIndicatorIn) {

        int myCount = bufferIn.append(EncodingByteValues.asciiQuote).length();

        if (formatTime(bufferIn).length() > myCount) {

            bufferIn.append(EncodingByteValues.asciiQuote);

        } else {

            bufferIn.truncate(myCount).append(nullIndicatorIn);
        }

        return bufferIn;
    }

    public ByteBuffer formatDate(ByteBuffer bufferIn) {

        if (_hasDate) {

            if (null == bufferIn) {

                bufferIn = new ByteBuffer();
            }

            CsiExcelCalendar myCalendar = getCalendar();
            int myYear = myCalendar.getYear();
            int myMonth = myCalendar.getMonth();
            int myDay = myCalendar.getDay();

            bufferIn.append(myYear, 4);
            bufferIn.append(EncodingByteValues.asciiMinus);
            bufferIn.append(myMonth, 2);
            bufferIn.append(EncodingByteValues.asciiMinus);
            bufferIn.append(myDay, 2);

        } else {

            bufferIn.append(_noDate);
        }
        return bufferIn;
    }

    public ByteBuffer formatTime(ByteBuffer bufferIn) {

        if (_hasTime) {

            int myMilliseconds = _millisecondsSinceMidnight;
            int myHour;
            int myMinute;
            int mySecond;

            if (null == bufferIn) {

                bufferIn = new ByteBuffer();
            }

            myHour = myMilliseconds / _millisecondsPerHour;
            myMilliseconds = myMilliseconds % _millisecondsPerHour;
            myMinute = myMilliseconds / _millisecondsPerMinute;
            myMilliseconds = myMilliseconds % _millisecondsPerMinute;
            mySecond = myMilliseconds / _millisecondsPerSecond;
            myMilliseconds = myMilliseconds % _millisecondsPerSecond;

            bufferIn.append(myHour, 2);
            bufferIn.append(EncodingByteValues.asciiColon);
            bufferIn.append(myMinute, 2);
            bufferIn.append(EncodingByteValues.asciiColon);
            bufferIn.append(mySecond, 2);
            bufferIn.append(EncodingByteValues.asciiDot);
            bufferIn.append(myMilliseconds, 3);

        } else {

            bufferIn.append(_noTime);
        }
        return bufferIn;
    }

    private CsiExcelCalendar getCalendar() {

        if (null == _calendar) {

            _calendar = new CsiExcelCalendar(_excelDaysSince1900);
        }
        return _calendar;
    }

    private void extractExcelTimeStamp() {

        if ((null != _byteBuffer) && (0 < _byteBuffer.length)) {

            int myCount = 0;
            int myWholeValue = 0;
            long myDividend = 0L;
            long myDivisor = 1L;
            int myPowerSign = 0;
            int myPower = 0;
            double myTotal;
            double myFraction;

            for (; _byteBuffer.length > myCount; myCount++) {

                if (('0' > _byteBuffer[myCount]) || ('9' < _byteBuffer[myCount])) {

                    break;
                }
                myWholeValue = ((myWholeValue * 10) + _byteBuffer[myCount]) - '0';
            }

            if ((_byteBuffer.length > myCount) && ('.' == _byteBuffer[myCount++])) {

                for (; _byteBuffer.length > myCount; myCount++) {

                    if (('0' > _byteBuffer[myCount]) || ('9' < _byteBuffer[myCount])) {

                        break;
                    }
                    myDividend = ((myDividend * 10L) + _byteBuffer[myCount]) - '0';
                    myDivisor *= 10L;
                }
                _hasTime = (0L < myDividend);
            }

            myFraction = (double)myDividend / (double)myDivisor;
            myTotal = myWholeValue + myFraction;

            if ((_byteBuffer.length > myCount) && (EncodingByteValues.asciiCapsE == _byteBuffer[myCount])) {

                myPowerSign = _byteBuffer[++myCount];

                if ((EncodingByteValues.asciiPlus == myPowerSign)
                        || (EncodingByteValues.asciiMinus == myPowerSign)) {

                    for (myCount++; _byteBuffer.length > myCount; myCount++) {

                        if (('0' > _byteBuffer[myCount]) || ('9' < _byteBuffer[myCount])) {

                            break;
                        }
                        myPower = ((myPower * 10) + _byteBuffer[myCount]) - '0';
                    }

                    if (EncodingByteValues.asciiPlus == myPowerSign) {

                        myTotal *= Math.pow(10, myPower);

                    } else {

                        myTotal /= Math.pow(10, myPower);
                    }
                }
                myWholeValue = (int)myTotal;
                myFraction = myTotal - myWholeValue;
            }

            if (_byteBuffer.length == myCount) {

                _excelDaysSince1900 = myWholeValue;

                // Add 1 to account for Excel thinking
                // there was a February 29th 1900 !!
                _actualDaysSince1900 = (61L > _excelDaysSince1900) ? _excelDaysSince1900 + 1 : _excelDaysSince1900;

                _totalDaysSince1970 = _actualDaysSince1900 - _converterDelta;

                _fractionalDaySinceMidnight = myFraction;

                _millisecondsSinceMidnight = (int)((_fractionalDaySinceMidnight * _millisecondsPerDay) + 0.5D);

                _hasDate = (0 < _excelDaysSince1900);
            }
        }
    }
}

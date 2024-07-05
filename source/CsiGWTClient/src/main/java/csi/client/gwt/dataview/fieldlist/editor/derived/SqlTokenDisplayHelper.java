package csi.client.gwt.dataview.fieldlist.editor.derived;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.display_list_widgets.ComponentLabel;
import csi.client.gwt.widget.display_list_widgets.DisplayList;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.SqlToken;
import csi.server.common.enumerations.SqlTokenType;

/**
 * Created by centrifuge on 3/16/2015.
 */
public class SqlTokenDisplayHelper {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final Map<SqlToken, String> _sqlTokenTreeLabelMap = new HashMap<SqlToken, String>();
    private static final Map<SqlToken, String> _sqlTokenLabelMap = new HashMap<SqlToken, String>();
    private static final Map<SqlToken, String> _sqlTokenInfoMap = new HashMap<SqlToken, String>();
    private static final Map<SqlToken, String> _sqlTokenHelpMap = new HashMap<SqlToken, String>();

    private static List<SqlToken> _sortedStringFunctionList = new ArrayList<SqlToken>();
    private static List<SqlToken> _sortedBooleanFunctionList = new ArrayList<SqlToken>();
    private static List<SqlToken> _sortedIntegerFunctionList = new ArrayList<SqlToken>();
    private static List<SqlToken> _sortedNumberFunctionList = new ArrayList<SqlToken>();
    private static List<SqlToken> _sortedDateTimeFunctionList = new ArrayList<SqlToken>();
    private static List<SqlToken> _sortedDateFunctionList = new ArrayList<SqlToken>();
    private static List<SqlToken> _sortedTimeFunctionList = new ArrayList<SqlToken>();

    private static Map<CsiDataType, List<SqlToken>> _functionListMap = new HashMap<CsiDataType, List<SqlToken>>();

    static {

        _sqlTokenTreeLabelMap.put(SqlToken.StringConditional, _constants.sqlTokenTreeLabel_Conditional());
        _sqlTokenTreeLabelMap.put(SqlToken.BooleanConditional, _constants.sqlTokenTreeLabel_Conditional());
        _sqlTokenTreeLabelMap.put(SqlToken.IntegerConditional, _constants.sqlTokenTreeLabel_Conditional());
        _sqlTokenTreeLabelMap.put(SqlToken.NumberConditional, _constants.sqlTokenTreeLabel_Conditional());
        _sqlTokenTreeLabelMap.put(SqlToken.DateTimeConditional, _constants.sqlTokenTreeLabel_Conditional());
        _sqlTokenTreeLabelMap.put(SqlToken.DateConditional, _constants.sqlTokenTreeLabel_Conditional());
        _sqlTokenTreeLabelMap.put(SqlToken.TimeConditional, _constants.sqlTokenTreeLabel_Conditional());

        _sqlTokenTreeLabelMap.put(SqlToken.StringConditionalComponent1, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.BooleanConditionalComponent1, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.IntegerConditionalComponent1, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.NumberConditionalComponent1, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.DateTimeConditionalComponent1, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.DateConditionalComponent1, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.TimeConditionalComponent1, _constants.sqlTokenTreeLabel_ConditionalComponent());

        _sqlTokenTreeLabelMap.put(SqlToken.StringConditionalComponent2, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.BooleanConditionalComponent2, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.IntegerConditionalComponent2, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.NumberConditionalComponent2, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.DateTimeConditionalComponent2, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.DateConditionalComponent2, _constants.sqlTokenTreeLabel_ConditionalComponent());
        _sqlTokenTreeLabelMap.put(SqlToken.TimeConditionalComponent2, _constants.sqlTokenTreeLabel_ConditionalComponent());

        _sqlTokenTreeLabelMap.put(SqlToken.StringConditionalDefault, _constants.sqlTokenTreeLabel_ConditionalDefault());
        _sqlTokenTreeLabelMap.put(SqlToken.BooleanConditionalDefault, _constants.sqlTokenTreeLabel_ConditionalDefault());
        _sqlTokenTreeLabelMap.put(SqlToken.IntegerConditionalDefault, _constants.sqlTokenTreeLabel_ConditionalDefault());
        _sqlTokenTreeLabelMap.put(SqlToken.NumberConditionalDefault, _constants.sqlTokenTreeLabel_ConditionalDefault());
        _sqlTokenTreeLabelMap.put(SqlToken.DateTimeConditionalDefault, _constants.sqlTokenTreeLabel_ConditionalDefault());
        _sqlTokenTreeLabelMap.put(SqlToken.DateConditionalDefault, _constants.sqlTokenTreeLabel_ConditionalDefault());
        _sqlTokenTreeLabelMap.put(SqlToken.TimeConditionalDefault, _constants.sqlTokenTreeLabel_ConditionalDefault());



        _sqlTokenLabelMap.put(SqlToken.BooleanAnd, _constants.sqlTokenLabel_BooleanAnd());
        _sqlTokenLabelMap.put(SqlToken.BooleanOR, _constants.sqlTokenLabel_BooleanOR());
        _sqlTokenLabelMap.put(SqlToken.BooleanNOT, _constants.sqlTokenLabel_BooleanNOT());
        _sqlTokenLabelMap.put(SqlToken.IsTrue, _constants.sqlTokenLabel_IsTrue());
        _sqlTokenLabelMap.put(SqlToken.IsNotTrue, _constants.sqlTokenLabel_IsNotTrue());
        _sqlTokenLabelMap.put(SqlToken.IsFalse, _constants.sqlTokenLabel_IsFalse());
        _sqlTokenLabelMap.put(SqlToken.IsNotFalse, _constants.sqlTokenLabel_IsNotFalse());
        _sqlTokenLabelMap.put(SqlToken.IsUnknown, _constants.sqlTokenLabel_IsUnknown());
        _sqlTokenLabelMap.put(SqlToken.IsNotUnknown, _constants.sqlTokenLabel_IsNotUnknown());
        _sqlTokenLabelMap.put(SqlToken.Equal, _constants.sqlTokenLabel_Equal());
        _sqlTokenLabelMap.put(SqlToken.NotEqual, _constants.sqlTokenLabel_NotEqual());
        _sqlTokenLabelMap.put(SqlToken.IsDistinct, _constants.sqlTokenLabel_IsDistinct());
        _sqlTokenLabelMap.put(SqlToken.IsNotDistinct, _constants.sqlTokenLabel_IsNotDistinct());
        _sqlTokenLabelMap.put(SqlToken.IsNull, _constants.sqlTokenLabel_IsNull());
        _sqlTokenLabelMap.put(SqlToken.IsNotNull, _constants.sqlTokenLabel_IsNotNull());
        _sqlTokenLabelMap.put(SqlToken.LessThan, _constants.sqlTokenLabel_LessThan());
        _sqlTokenLabelMap.put(SqlToken.GreaterThan, _constants.sqlTokenLabel_GreaterThan());
        _sqlTokenLabelMap.put(SqlToken.LessThanOrEqual, _constants.sqlTokenLabel_LessThanOrEqual());
        _sqlTokenLabelMap.put(SqlToken.GreaterThanOrEqual, _constants.sqlTokenLabel_GreaterThanOrEqual());
        _sqlTokenLabelMap.put(SqlToken.Between, _constants.sqlTokenLabel_Between());
        _sqlTokenLabelMap.put(SqlToken.NotBetween, _constants.sqlTokenLabel_NotBetween());
        _sqlTokenLabelMap.put(SqlToken.BetweenSymetric, _constants.sqlTokenLabel_BetweenSymetric());
        _sqlTokenLabelMap.put(SqlToken.NotBetweenSymetric, _constants.sqlTokenLabel_NotBetweenSymetric());
        _sqlTokenLabelMap.put(SqlToken.RegularExpression, _constants.sqlTokenLabel_RegularExpression());
        _sqlTokenLabelMap.put(SqlToken.CaselessRegularExpression, _constants.sqlTokenLabel_CaselessRegularExpression());
        _sqlTokenLabelMap.put(SqlToken.BitwiseAnd, _constants.sqlTokenLabel_BitwiseAnd());
        _sqlTokenLabelMap.put(SqlToken.BitwiseOR, _constants.sqlTokenLabel_BitwiseOR());
        _sqlTokenLabelMap.put(SqlToken.BitwiseXOR, _constants.sqlTokenLabel_BitwiseXOR());
        _sqlTokenLabelMap.put(SqlToken.BitwiseNOT, _constants.sqlTokenLabel_BitwiseNOT());
        _sqlTokenLabelMap.put(SqlToken.ShiftLeft, _constants.sqlTokenLabel_ShiftLeft());
        _sqlTokenLabelMap.put(SqlToken.ShiftRight, _constants.sqlTokenLabel_ShiftRight());
        _sqlTokenLabelMap.put(SqlToken.IntegerAddition, _constants.sqlTokenLabel_IntegerAddition());
        _sqlTokenLabelMap.put(SqlToken.IntegerSubtraction, _constants.sqlTokenLabel_IntegerSubtraction());
        _sqlTokenLabelMap.put(SqlToken.IntegerMultiplication, _constants.sqlTokenLabel_IntegerMultiplication());
        _sqlTokenLabelMap.put(SqlToken.IntegerDivision, _constants.sqlTokenLabel_IntegerDivision());
        _sqlTokenLabelMap.put(SqlToken.ModuloDivision, _constants.sqlTokenLabel_ModuloDivision());
        _sqlTokenLabelMap.put(SqlToken.IntegerFactorial, _constants.sqlTokenLabel_IntegerFactorial());
        _sqlTokenLabelMap.put(SqlToken.IntegerExponentiation, _constants.sqlTokenLabel_IntegerExponentiation());
        _sqlTokenLabelMap.put(SqlToken.DecimalAddition, _constants.sqlTokenLabel_DecimalAddition());
        _sqlTokenLabelMap.put(SqlToken.DecimalSubtraction, _constants.sqlTokenLabel_DecimalSubtraction());
        _sqlTokenLabelMap.put(SqlToken.DecimalMultiplication, _constants.sqlTokenLabel_DecimalMultiplication());
        _sqlTokenLabelMap.put(SqlToken.DecimalDivision, _constants.sqlTokenLabel_DecimalDivision());
        _sqlTokenLabelMap.put(SqlToken.DecimalExponentiation, _constants.sqlTokenLabel_DecimalExponentiation());
        _sqlTokenLabelMap.put(SqlToken.CastInteger, _constants.sqlTokenLabel_CastInteger());
        _sqlTokenLabelMap.put(SqlToken.CastDecimal, _constants.sqlTokenLabel_CastDecimal());
        _sqlTokenLabelMap.put(SqlToken.CastDate, _constants.sqlTokenLabel_CastDate());
        _sqlTokenLabelMap.put(SqlToken.CastTime, _constants.sqlTokenLabel_CastTime());
        _sqlTokenLabelMap.put(SqlToken.CastDateTime, _constants.sqlTokenLabel_CastDateTime());
        _sqlTokenLabelMap.put(SqlToken.CastBoolean, _constants.sqlTokenLabel_CastBoolean());
        _sqlTokenLabelMap.put(SqlToken.TruncateToInteger, _constants.sqlTokenLabel_TruncateToInteger());
        _sqlTokenLabelMap.put(SqlToken.TruncateDecimal, _constants.sqlTokenLabel_TruncateDecimal());
        _sqlTokenLabelMap.put(SqlToken.RoundToInteger, _constants.sqlTokenLabel_RoundToInteger());
        _sqlTokenLabelMap.put(SqlToken.RoundDecimal, _constants.sqlTokenLabel_RoundDecimal());
        _sqlTokenLabelMap.put(SqlToken.AbsoluteInteger, _constants.sqlTokenLabel_AbsoluteInteger());
        _sqlTokenLabelMap.put(SqlToken.AbsoluteDecimal, _constants.sqlTokenLabel_AbsoluteDecimal());
        _sqlTokenLabelMap.put(SqlToken.SquareRoot, _constants.sqlTokenLabel_SquareRoot());
        _sqlTokenLabelMap.put(SqlToken.CubeRoot, _constants.sqlTokenLabel_CubeRoot());
        _sqlTokenLabelMap.put(SqlToken.NaturalLog, _constants.sqlTokenLabel_NaturalLog());
        _sqlTokenLabelMap.put(SqlToken.LogBase10, _constants.sqlTokenLabel_LogBase10());
        _sqlTokenLabelMap.put(SqlToken.Sine, _constants.sqlTokenLabel_Sine());
        _sqlTokenLabelMap.put(SqlToken.Cosine, _constants.sqlTokenLabel_Cosine());
        _sqlTokenLabelMap.put(SqlToken.Tangent, _constants.sqlTokenLabel_Tangent());
        _sqlTokenLabelMap.put(SqlToken.Cotangent, _constants.sqlTokenLabel_Cotangent());
        _sqlTokenLabelMap.put(SqlToken.InverseSine, _constants.sqlTokenLabel_InverseSine());
        _sqlTokenLabelMap.put(SqlToken.InverseCosine, _constants.sqlTokenLabel_InverseCosine());
        _sqlTokenLabelMap.put(SqlToken.InverseTangent, _constants.sqlTokenLabel_InverseTangent());
        _sqlTokenLabelMap.put(SqlToken.InverseCotangent, _constants.sqlTokenLabel_InverseCotangent());
        _sqlTokenLabelMap.put(SqlToken.Concatenation, _constants.sqlTokenLabel_Concatenation());
        _sqlTokenLabelMap.put(SqlToken.ToString, _constants.sqlTokenLabel_ToString());
        _sqlTokenLabelMap.put(SqlToken.CharacterLength, _constants.sqlTokenLabel_CharacterLength());
        _sqlTokenLabelMap.put(SqlToken.ByteCount, _constants.sqlTokenLabel_ByteCount());
        _sqlTokenLabelMap.put(SqlToken.BitCount, _constants.sqlTokenLabel_BitCount());
        _sqlTokenLabelMap.put(SqlToken.Capitalize, _constants.sqlTokenLabel_Capitalize());
        _sqlTokenLabelMap.put(SqlToken.UpperCase, _constants.sqlTokenLabel_UpperCase());
        _sqlTokenLabelMap.put(SqlToken.LowerCase, _constants.sqlTokenLabel_LowerCase());
        _sqlTokenLabelMap.put(SqlToken.LeftTrim1, _constants.sqlTokenLabel_LeftTrim1());
        _sqlTokenLabelMap.put(SqlToken.LeftTrim2, _constants.sqlTokenLabel_LeftTrim2());
        _sqlTokenLabelMap.put(SqlToken.RightTrim1, _constants.sqlTokenLabel_RightTrim1());
        _sqlTokenLabelMap.put(SqlToken.RightTrim2, _constants.sqlTokenLabel_RightTrim2());
        _sqlTokenLabelMap.put(SqlToken.LeftPad1, _constants.sqlTokenLabel_LeftPad1());
        _sqlTokenLabelMap.put(SqlToken.LeftPad2, _constants.sqlTokenLabel_LeftPad2());
        _sqlTokenLabelMap.put(SqlToken.RightPad1, _constants.sqlTokenLabel_RightPad1());
        _sqlTokenLabelMap.put(SqlToken.RightPad2, _constants.sqlTokenLabel_RightPad2());
        _sqlTokenLabelMap.put(SqlToken.Substring1, _constants.sqlTokenLabel_Substring1());
        _sqlTokenLabelMap.put(SqlToken.Substring2, _constants.sqlTokenLabel_Substring2());
        _sqlTokenLabelMap.put(SqlToken.LocateSubstring, _constants.sqlTokenLabel_LocateSubstring());

        _sqlTokenLabelMap.put(SqlToken.TimestampPlusTime, _constants.sqlTokenLabel_TimestampPlusTime());
        _sqlTokenLabelMap.put(SqlToken.TimestampMinusTimeStamp, _constants.sqlTokenLabel_TimestampMinusTimeStamp());
        _sqlTokenLabelMap.put(SqlToken.TimestampMinusDate, _constants.sqlTokenLabel_TimestampMinusDate());
        _sqlTokenLabelMap.put(SqlToken.TimestampMinusTime, _constants.sqlTokenLabel_TimestampMinusTime());

        _sqlTokenLabelMap.put(SqlToken.DatePlusDays, _constants.sqlTokenLabel_DatePlusDays());
        _sqlTokenLabelMap.put(SqlToken.DatePlusTime, _constants.sqlTokenLabel_DatePlusTime());
        _sqlTokenLabelMap.put(SqlToken.DateMinusDate, _constants.sqlTokenLabel_DateMinusDate());
        _sqlTokenLabelMap.put(SqlToken.DateMinusDays, _constants.sqlTokenLabel_DateMinusDays());
        _sqlTokenLabelMap.put(SqlToken.DateMinusTime, _constants.sqlTokenLabel_DateMinusTime());

        _sqlTokenLabelMap.put(SqlToken.TimePlusTime, _constants.sqlTokenLabel_TimePlusTime());
        _sqlTokenLabelMap.put(SqlToken.TimeMinusTime, _constants.sqlTokenLabel_TimeMinusTime());
        _sqlTokenLabelMap.put(SqlToken.TimeMultiplied, _constants.sqlTokenLabel_TimeMultiplied());
        _sqlTokenLabelMap.put(SqlToken.TimeDivided, _constants.sqlTokenLabel_TimeDivided());

        _sqlTokenLabelMap.put(SqlToken.TimeAsTimeOfDay, _constants.sqlTokenLabel_TimeAsTimeOfDay());
        _sqlTokenLabelMap.put(SqlToken.TimestampAsDate, _constants.sqlTokenLabel_TimestampAsDate());
        _sqlTokenLabelMap.put(SqlToken.AsNumberOfDays, _constants.sqlTokenLabel_AsNumberOfDays());
        _sqlTokenLabelMap.put(SqlToken.AsNumberOfHours, _constants.sqlTokenLabel_AsNumberOfHours());
        _sqlTokenLabelMap.put(SqlToken.AsNumberOfMinutes, _constants.sqlTokenLabel_AsNumberOfMinutes());
        _sqlTokenLabelMap.put(SqlToken.AsNumberOfSeconds, _constants.sqlTokenLabel_AsNumberOfSeconds());
        _sqlTokenLabelMap.put(SqlToken.AsNumberOfMilliseconds, _constants.sqlTokenLabel_AsNumberOfMilliseconds());
        _sqlTokenLabelMap.put(SqlToken.AsNumberOfMicroseconds, _constants.sqlTokenLabel_AsNumberOfMicroseconds());

        _sqlTokenLabelMap.put(SqlToken.CurrentTimestamp, _constants.sqlTokenLabel_CurrentTimestamp());
        _sqlTokenLabelMap.put(SqlToken.CurrentDate, _constants.sqlTokenLabel_CurrentDate());
        _sqlTokenLabelMap.put(SqlToken.CurrentTime, _constants.sqlTokenLabel_CurrentTime());
        _sqlTokenLabelMap.put(SqlToken.DateDateExtract, _constants.sqlTokenLabel_DateDateExtract());
        _sqlTokenLabelMap.put(SqlToken.DateTimeExtract, _constants.sqlTokenLabel_DateTimeExtract());
        _sqlTokenLabelMap.put(SqlToken.DateEpochExtract, _constants.sqlTokenLabel_DateEpochExtract());
        _sqlTokenLabelMap.put(SqlToken.DateMillenniumExtract, _constants.sqlTokenLabel_DateMillenniumExtract());
        _sqlTokenLabelMap.put(SqlToken.DateCenturyExtract, _constants.sqlTokenLabel_DateCenturyExtract());
        _sqlTokenLabelMap.put(SqlToken.DateDecadeExtract, _constants.sqlTokenLabel_DateDecadeExtract());
        _sqlTokenLabelMap.put(SqlToken.DateYearExtract, _constants.sqlTokenLabel_DateYearExtract());
        _sqlTokenLabelMap.put(SqlToken.DateQuarterExtract, _constants.sqlTokenLabel_DateQuarterExtract());
        _sqlTokenLabelMap.put(SqlToken.DateMonthExtract, _constants.sqlTokenLabel_DateMonthExtract());
        _sqlTokenLabelMap.put(SqlToken.DateWeekExtract, _constants.sqlTokenLabel_DateWeekExtract());
        _sqlTokenLabelMap.put(SqlToken.DateYearDayExtract, _constants.sqlTokenLabel_DateYearDayExtract());
        _sqlTokenLabelMap.put(SqlToken.DateMonthDayExtract, _constants.sqlTokenLabel_DateMonthDayExtract());
        _sqlTokenLabelMap.put(SqlToken.DateWeekDayExtract, _constants.sqlTokenLabel_DateWeekDayExtract());
        _sqlTokenLabelMap.put(SqlToken.DateHourExtract, _constants.sqlTokenLabel_DateHourExtract());
        _sqlTokenLabelMap.put(SqlToken.DateMinuteExtract, _constants.sqlTokenLabel_DateMinuteExtract());
        _sqlTokenLabelMap.put(SqlToken.DateSecondExtract, _constants.sqlTokenLabel_DateSecondExtract());
        _sqlTokenLabelMap.put(SqlToken.DateMillisecondsExtract, _constants.sqlTokenLabel_DateMillisecondsExtract());
        _sqlTokenLabelMap.put(SqlToken.DateMicrosecondsExtract, _constants.sqlTokenLabel_DateMicrosecondsExtract());
        _sqlTokenLabelMap.put(SqlToken.DateTimezoneExtract, _constants.sqlTokenLabel_DateTimezoneExtract());

        _sqlTokenLabelMap.put(SqlToken.DayPartDifference, _constants.sqlTokenLabel_DayPartDifference());
        _sqlTokenLabelMap.put(SqlToken.HourPartDifference, _constants.sqlTokenLabel_HourPartDifference());
        _sqlTokenLabelMap.put(SqlToken.MinutePartDifference, _constants.sqlTokenLabel_MinutePartDifference());
        _sqlTokenLabelMap.put(SqlToken.SecondPartDifference, _constants.sqlTokenLabel_SecondPartDifference());
        _sqlTokenLabelMap.put(SqlToken.MillisecondPartDifference, _constants.sqlTokenLabel_MillisecondPartDifference());

        _sqlTokenLabelMap.put(SqlToken.DateMillenniumTruncate, _constants.sqlTokenLabel_DateMillenniumTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateCenturyTruncate, _constants.sqlTokenLabel_DateCenturyTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateDecadeTruncate, _constants.sqlTokenLabel_DateDecadeTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateYearTruncate, _constants.sqlTokenLabel_DateYearTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateQuarterTruncate, _constants.sqlTokenLabel_DateQuarterTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateMonthTruncate, _constants.sqlTokenLabel_DateMonthTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateWeekTruncate, _constants.sqlTokenLabel_DateWeekTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateMonthDayTruncate, _constants.sqlTokenLabel_DateMonthDayTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateHourTruncate, _constants.sqlTokenLabel_DateHourTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateMinuteTruncate, _constants.sqlTokenLabel_DateMinuteTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateSecondTruncate, _constants.sqlTokenLabel_DateSecondTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateMillisecondsTruncate, _constants.sqlTokenLabel_DateMillisecondsTruncate());
        _sqlTokenLabelMap.put(SqlToken.DateMicrosecondsTruncate, _constants.sqlTokenLabel_DateMicrosecondsTruncate());
        _sqlTokenLabelMap.put(SqlToken.StringField, _constants.sqlTokenLabel_StringField());
        _sqlTokenLabelMap.put(SqlToken.BooleanField, _constants.sqlTokenLabel_BooleanField());
        _sqlTokenLabelMap.put(SqlToken.IntegerField, _constants.sqlTokenLabel_IntegerField());
        _sqlTokenLabelMap.put(SqlToken.NumberField, _constants.sqlTokenLabel_NumberField());
        _sqlTokenLabelMap.put(SqlToken.DateTimeField, _constants.sqlTokenLabel_DateTimeField());
        _sqlTokenLabelMap.put(SqlToken.DateField, _constants.sqlTokenLabel_DateField());
        _sqlTokenLabelMap.put(SqlToken.TimeField, _constants.sqlTokenLabel_TimeField());
        _sqlTokenLabelMap.put(SqlToken.StringParameter, _constants.sqlTokenLabel_StringParameter());
        _sqlTokenLabelMap.put(SqlToken.BooleanParameter, _constants.sqlTokenLabel_BooleanParameter());
        _sqlTokenLabelMap.put(SqlToken.IntegerParameter, _constants.sqlTokenLabel_IntegerParameter());
        _sqlTokenLabelMap.put(SqlToken.NumberParameter, _constants.sqlTokenLabel_NumberParameter());
        _sqlTokenLabelMap.put(SqlToken.DateTimeParameter, _constants.sqlTokenLabel_DateTimeParameter());
        _sqlTokenLabelMap.put(SqlToken.DateParameter, _constants.sqlTokenLabel_DateParameter());
        _sqlTokenLabelMap.put(SqlToken.TimeParameter, _constants.sqlTokenLabel_TimeParameter());
        _sqlTokenLabelMap.put(SqlToken.StringValue, _constants.sqlTokenLabel_StringValue());
        _sqlTokenLabelMap.put(SqlToken.BooleanValue, _constants.sqlTokenLabel_BooleanValue());
        _sqlTokenLabelMap.put(SqlToken.IntegerValue, _constants.sqlTokenLabel_IntegerValue());
        _sqlTokenLabelMap.put(SqlToken.NumberValue, _constants.sqlTokenLabel_NumberValue());
        _sqlTokenLabelMap.put(SqlToken.DateTimeValue, _constants.sqlTokenLabel_DateTimeValue());
        _sqlTokenLabelMap.put(SqlToken.DateValue, _constants.sqlTokenLabel_DateValue());
        _sqlTokenLabelMap.put(SqlToken.TimeValue, _constants.sqlTokenLabel_TimeValue());

        _sqlTokenLabelMap.put(SqlToken.FormatValue, _constants.sqlTokenLabel_FormatValue());
        _sqlTokenLabelMap.put(SqlToken.InputUnixEpoch, _constants.sqlTokenLabel_EpochToDateTime());
        _sqlTokenLabelMap.put(SqlToken.ScanDateTime, _constants.sqlTokenLabel_ScanDateTime());
        _sqlTokenLabelMap.put(SqlToken.ScanDate, _constants.sqlTokenLabel_ScanDate());
        _sqlTokenLabelMap.put(SqlToken.ScanInteger, _constants.sqlTokenLabel_ScanInteger());
        _sqlTokenLabelMap.put(SqlToken.ScanDecimal, _constants.sqlTokenLabel_ScanDecimal());
        _sqlTokenLabelMap.put(SqlToken.NumToken, _constants.sqlTokenLabel_NumToken());
        _sqlTokenLabelMap.put(SqlToken.GetSingleToken, _constants.sqlTokenLabel_GetSingleToken());
        _sqlTokenLabelMap.put(SqlToken.GetLastToken, _constants.sqlTokenLabel_GetLastToken());

        _sqlTokenLabelMap.put(SqlToken.StringConditional, _constants.sqlTokenLabel_Conditional());
        _sqlTokenLabelMap.put(SqlToken.BooleanConditional, _constants.sqlTokenLabel_Conditional());
        _sqlTokenLabelMap.put(SqlToken.IntegerConditional, _constants.sqlTokenLabel_Conditional());
        _sqlTokenLabelMap.put(SqlToken.NumberConditional, _constants.sqlTokenLabel_Conditional());
        _sqlTokenLabelMap.put(SqlToken.DateTimeConditional, _constants.sqlTokenLabel_Conditional());
        _sqlTokenLabelMap.put(SqlToken.DateConditional, _constants.sqlTokenLabel_Conditional());
        _sqlTokenLabelMap.put(SqlToken.TimeConditional, _constants.sqlTokenLabel_Conditional());

        _sqlTokenLabelMap.put(SqlToken.StringConditionalComponent1, _constants.sqlTokenLabel_ConditionalComponent1());
        _sqlTokenLabelMap.put(SqlToken.BooleanConditionalComponent1, _constants.sqlTokenLabel_ConditionalComponent1());
        _sqlTokenLabelMap.put(SqlToken.IntegerConditionalComponent1, _constants.sqlTokenLabel_ConditionalComponent1());
        _sqlTokenLabelMap.put(SqlToken.NumberConditionalComponent1, _constants.sqlTokenLabel_ConditionalComponent1());
        _sqlTokenLabelMap.put(SqlToken.DateTimeConditionalComponent1, _constants.sqlTokenLabel_ConditionalComponent1());
        _sqlTokenLabelMap.put(SqlToken.DateConditionalComponent1, _constants.sqlTokenLabel_ConditionalComponent1());
        _sqlTokenLabelMap.put(SqlToken.TimeConditionalComponent1, _constants.sqlTokenLabel_ConditionalComponent1());

        _sqlTokenLabelMap.put(SqlToken.StringConditionalComponent2, _constants.sqlTokenLabel_ConditionalComponent2());
        _sqlTokenLabelMap.put(SqlToken.BooleanConditionalComponent2, _constants.sqlTokenLabel_ConditionalComponent2());
        _sqlTokenLabelMap.put(SqlToken.IntegerConditionalComponent2, _constants.sqlTokenLabel_ConditionalComponent2());
        _sqlTokenLabelMap.put(SqlToken.NumberConditionalComponent2, _constants.sqlTokenLabel_ConditionalComponent2());
        _sqlTokenLabelMap.put(SqlToken.DateTimeConditionalComponent2, _constants.sqlTokenLabel_ConditionalComponent2());
        _sqlTokenLabelMap.put(SqlToken.DateConditionalComponent2, _constants.sqlTokenLabel_ConditionalComponent2());
        _sqlTokenLabelMap.put(SqlToken.TimeConditionalComponent2, _constants.sqlTokenLabel_ConditionalComponent2());

        _sqlTokenLabelMap.put(SqlToken.StringConditionalDefault, _constants.sqlTokenLabel_ConditionalDefault());
        _sqlTokenLabelMap.put(SqlToken.BooleanConditionalDefault, _constants.sqlTokenLabel_ConditionalDefault());
        _sqlTokenLabelMap.put(SqlToken.IntegerConditionalDefault, _constants.sqlTokenLabel_ConditionalDefault());
        _sqlTokenLabelMap.put(SqlToken.NumberConditionalDefault, _constants.sqlTokenLabel_ConditionalDefault());
        _sqlTokenLabelMap.put(SqlToken.DateTimeConditionalDefault, _constants.sqlTokenLabel_ConditionalDefault());
        _sqlTokenLabelMap.put(SqlToken.DateConditionalDefault, _constants.sqlTokenLabel_ConditionalDefault());
        _sqlTokenLabelMap.put(SqlToken.TimeConditionalDefault, _constants.sqlTokenLabel_ConditionalDefault());

        _sqlTokenLabelMap.put(SqlToken.Extract1stValue, _constants.sqlTokenLabel_Extract1stValue());
        _sqlTokenLabelMap.put(SqlToken.Extract2ndValue, _constants.sqlTokenLabel_Extract2ndValue());
        _sqlTokenLabelMap.put(SqlToken.ExtractNthValue, _constants.sqlTokenLabel_ExtractNthValue());
        _sqlTokenLabelMap.put(SqlToken.ExtractLastValue, _constants.sqlTokenLabel_ExtractLastValue());
        _sqlTokenLabelMap.put(SqlToken.ExtractLastValue, _constants.sqlTokenLabel_ExtractLastValue());
        _sqlTokenLabelMap.put(SqlToken.ExtractMatchingString, _constants.sqlTokenLabel_ExtractMatchingString());

        _sqlTokenLabelMap.put(SqlToken.NullNumber, _constants.sqlTokenLabel_NullValue());
        _sqlTokenLabelMap.put(SqlToken.NullInteger, _constants.sqlTokenLabel_NullValue());
        _sqlTokenLabelMap.put(SqlToken.NullBoolean, _constants.sqlTokenLabel_NullValue());
        _sqlTokenLabelMap.put(SqlToken.NullDate, _constants.sqlTokenLabel_NullValue());
        _sqlTokenLabelMap.put(SqlToken.NullTime, _constants.sqlTokenLabel_NullValue());
        _sqlTokenLabelMap.put(SqlToken.NullDateTime, _constants.sqlTokenLabel_NullValue());
        _sqlTokenLabelMap.put(SqlToken.NullString, _constants.sqlTokenLabel_NullValue());

        {
            for (SqlToken myToken : SqlToken.getUserFunctions()) {

                _sqlTokenLabelMap.put(myToken, myToken.getDisplay());
            }
        }

        _sqlTokenInfoMap.put(SqlToken.BooleanAnd, _constants.sqlTokenDescription_BooleanAnd());
        _sqlTokenInfoMap.put(SqlToken.BooleanOR, _constants.sqlTokenDescription_BooleanOR());
        _sqlTokenInfoMap.put(SqlToken.BooleanNOT, _constants.sqlTokenDescription_BooleanNOT());
        _sqlTokenInfoMap.put(SqlToken.IsTrue, _constants.sqlTokenDescription_IsTrue());
        _sqlTokenInfoMap.put(SqlToken.IsNotTrue, _constants.sqlTokenDescription_IsNotTrue());
        _sqlTokenInfoMap.put(SqlToken.IsFalse, _constants.sqlTokenDescription_IsFalse());
        _sqlTokenInfoMap.put(SqlToken.IsNotFalse, _constants.sqlTokenDescription_IsNotFalse());
        _sqlTokenInfoMap.put(SqlToken.IsUnknown, _constants.sqlTokenDescription_IsUnknown());
        _sqlTokenInfoMap.put(SqlToken.IsNotUnknown, _constants.sqlTokenDescription_IsNotUnknown());
        _sqlTokenInfoMap.put(SqlToken.Equal, _constants.sqlTokenDescription_Equal());
        _sqlTokenInfoMap.put(SqlToken.NotEqual, _constants.sqlTokenDescription_NotEqual());
        _sqlTokenInfoMap.put(SqlToken.IsDistinct, _constants.sqlTokenDescription_IsDistinct());
        _sqlTokenInfoMap.put(SqlToken.IsNotDistinct, _constants.sqlTokenDescription_IsNotDistinct());
        _sqlTokenInfoMap.put(SqlToken.IsNull, _constants.sqlTokenDescription_IsNull());
        _sqlTokenInfoMap.put(SqlToken.IsNotNull, _constants.sqlTokenDescription_IsNotNull());
        _sqlTokenInfoMap.put(SqlToken.LessThan, _constants.sqlTokenDescription_LessThan());
        _sqlTokenInfoMap.put(SqlToken.GreaterThan, _constants.sqlTokenDescription_GreaterThan());
        _sqlTokenInfoMap.put(SqlToken.LessThanOrEqual, _constants.sqlTokenDescription_LessThanOrEqual());
        _sqlTokenInfoMap.put(SqlToken.GreaterThanOrEqual, _constants.sqlTokenDescription_GreaterThanOrEqual());
        _sqlTokenInfoMap.put(SqlToken.Between, _constants.sqlTokenDescription_Between());
        _sqlTokenInfoMap.put(SqlToken.NotBetween, _constants.sqlTokenDescription_NotBetween());
        _sqlTokenInfoMap.put(SqlToken.BetweenSymetric, _constants.sqlTokenDescription_BetweenSymetric());
        _sqlTokenInfoMap.put(SqlToken.NotBetweenSymetric, _constants.sqlTokenDescription_NotBetweenSymetric());
        _sqlTokenInfoMap.put(SqlToken.RegularExpression, _constants.sqlTokenDescription_RegularExpression());
        _sqlTokenInfoMap.put(SqlToken.CaselessRegularExpression, _constants.sqlTokenDescription_CaselessRegularExpression());
        _sqlTokenInfoMap.put(SqlToken.BitwiseAnd, _constants.sqlTokenDescription_BitwiseAnd());
        _sqlTokenInfoMap.put(SqlToken.BitwiseOR, _constants.sqlTokenDescription_BitwiseOR());
        _sqlTokenInfoMap.put(SqlToken.BitwiseXOR, _constants.sqlTokenDescription_BitwiseXOR());
        _sqlTokenInfoMap.put(SqlToken.BitwiseNOT, _constants.sqlTokenDescription_BitwiseNOT());
        _sqlTokenInfoMap.put(SqlToken.ShiftLeft, _constants.sqlTokenDescription_ShiftLeft());
        _sqlTokenInfoMap.put(SqlToken.ShiftRight, _constants.sqlTokenDescription_ShiftRight());
        _sqlTokenInfoMap.put(SqlToken.IntegerAddition, _constants.sqlTokenDescription_IntegerAddition());
        _sqlTokenInfoMap.put(SqlToken.IntegerSubtraction, _constants.sqlTokenDescription_IntegerSubtraction());
        _sqlTokenInfoMap.put(SqlToken.IntegerMultiplication, _constants.sqlTokenDescription_IntegerMultiplication());
        _sqlTokenInfoMap.put(SqlToken.IntegerDivision, _constants.sqlTokenDescription_IntegerDivision());
        _sqlTokenInfoMap.put(SqlToken.ModuloDivision, _constants.sqlTokenDescription_ModuloDivision());
        _sqlTokenInfoMap.put(SqlToken.IntegerFactorial, _constants.sqlTokenDescription_IntegerFactorial());
        _sqlTokenInfoMap.put(SqlToken.IntegerExponentiation, _constants.sqlTokenDescription_IntegerExponentiation());
        _sqlTokenInfoMap.put(SqlToken.DecimalAddition, _constants.sqlTokenDescription_DecimalAddition());
        _sqlTokenInfoMap.put(SqlToken.DecimalSubtraction, _constants.sqlTokenDescription_DecimalSubtraction());
        _sqlTokenInfoMap.put(SqlToken.DecimalMultiplication, _constants.sqlTokenDescription_DecimalMultiplication());
        _sqlTokenInfoMap.put(SqlToken.DecimalDivision, _constants.sqlTokenDescription_DecimalDivision());
        _sqlTokenInfoMap.put(SqlToken.DecimalExponentiation, _constants.sqlTokenDescription_DecimalExponentiation());
        _sqlTokenInfoMap.put(SqlToken.CastInteger, _constants.sqlTokenDescription_CastInteger());
        _sqlTokenInfoMap.put(SqlToken.CastDecimal, _constants.sqlTokenDescription_CastDecimal());
        _sqlTokenInfoMap.put(SqlToken.CastDate, _constants.sqlTokenDescription_CastDate());
        _sqlTokenInfoMap.put(SqlToken.CastTime, _constants.sqlTokenDescription_CastTime());
        _sqlTokenInfoMap.put(SqlToken.CastDateTime, _constants.sqlTokenDescription_CastDateTime());
        _sqlTokenInfoMap.put(SqlToken.CastBoolean, _constants.sqlTokenDescription_CastBoolean());
        _sqlTokenInfoMap.put(SqlToken.TruncateToInteger, _constants.sqlTokenDescription_TruncateToInteger());
        _sqlTokenInfoMap.put(SqlToken.TruncateDecimal, _constants.sqlTokenDescription_TruncateDecimal());
        _sqlTokenInfoMap.put(SqlToken.RoundToInteger, _constants.sqlTokenDescription_RoundToInteger());
        _sqlTokenInfoMap.put(SqlToken.RoundDecimal, _constants.sqlTokenDescription_RoundDecimal());
        _sqlTokenInfoMap.put(SqlToken.AbsoluteInteger, _constants.sqlTokenDescription_AbsoluteInteger());
        _sqlTokenInfoMap.put(SqlToken.AbsoluteDecimal, _constants.sqlTokenDescription_AbsoluteDecimal());
        _sqlTokenInfoMap.put(SqlToken.SquareRoot, _constants.sqlTokenDescription_SquareRoot());
        _sqlTokenInfoMap.put(SqlToken.CubeRoot, _constants.sqlTokenDescription_CubeRoot());
        _sqlTokenInfoMap.put(SqlToken.NaturalLog, _constants.sqlTokenDescription_NaturalLog());
        _sqlTokenInfoMap.put(SqlToken.LogBase10, _constants.sqlTokenDescription_LogBase10());
        _sqlTokenInfoMap.put(SqlToken.Sine, _constants.sqlTokenDescription_Sine());
        _sqlTokenInfoMap.put(SqlToken.Cosine, _constants.sqlTokenDescription_Cosine());
        _sqlTokenInfoMap.put(SqlToken.Tangent, _constants.sqlTokenDescription_Tangent());
        _sqlTokenInfoMap.put(SqlToken.Cotangent, _constants.sqlTokenDescription_Cotangent());
        _sqlTokenInfoMap.put(SqlToken.InverseSine, _constants.sqlTokenDescription_InverseSine());
        _sqlTokenInfoMap.put(SqlToken.InverseCosine, _constants.sqlTokenDescription_InverseCosine());
        _sqlTokenInfoMap.put(SqlToken.InverseTangent, _constants.sqlTokenDescription_InverseTangent());
        _sqlTokenInfoMap.put(SqlToken.InverseCotangent, _constants.sqlTokenDescription_InverseCotangent());
        _sqlTokenInfoMap.put(SqlToken.Concatenation, _constants.sqlTokenDescription_Concatenation());
        _sqlTokenInfoMap.put(SqlToken.ToString, _constants.sqlTokenDescription_ToString());
        _sqlTokenInfoMap.put(SqlToken.CharacterLength, _constants.sqlTokenDescription_CharacterLength());
        _sqlTokenInfoMap.put(SqlToken.ByteCount, _constants.sqlTokenDescription_ByteCount());
        _sqlTokenInfoMap.put(SqlToken.BitCount, _constants.sqlTokenDescription_BitCount());
        _sqlTokenInfoMap.put(SqlToken.Capitalize, _constants.sqlTokenDescription_Capitalize());
        _sqlTokenInfoMap.put(SqlToken.UpperCase, _constants.sqlTokenDescription_UpperCase());
        _sqlTokenInfoMap.put(SqlToken.LowerCase, _constants.sqlTokenDescription_LowerCase());
        _sqlTokenInfoMap.put(SqlToken.LeftTrim1, _constants.sqlTokenDescription_LeftTrim1());
        _sqlTokenInfoMap.put(SqlToken.LeftTrim2, _constants.sqlTokenDescription_LeftTrim2());
        _sqlTokenInfoMap.put(SqlToken.RightTrim1, _constants.sqlTokenDescription_RightTrim1());
        _sqlTokenInfoMap.put(SqlToken.RightTrim2, _constants.sqlTokenDescription_RightTrim2());
        _sqlTokenInfoMap.put(SqlToken.LeftPad1, _constants.sqlTokenDescription_LeftPad1());
        _sqlTokenInfoMap.put(SqlToken.LeftPad2, _constants.sqlTokenDescription_LeftPad2());
        _sqlTokenInfoMap.put(SqlToken.RightPad1, _constants.sqlTokenDescription_RightPad1());
        _sqlTokenInfoMap.put(SqlToken.RightPad2, _constants.sqlTokenDescription_RightPad2());
        _sqlTokenInfoMap.put(SqlToken.Substring1, _constants.sqlTokenDescription_Substring1());
        _sqlTokenInfoMap.put(SqlToken.Substring2, _constants.sqlTokenDescription_Substring2());
        _sqlTokenInfoMap.put(SqlToken.LocateSubstring, _constants.sqlTokenDescription_LocateSubstring());

        _sqlTokenInfoMap.put(SqlToken.TimestampPlusTime, _constants.sqlTokenDescription_TimestampPlusTime());
        _sqlTokenInfoMap.put(SqlToken.TimestampMinusTimeStamp, _constants.sqlTokenDescription_TimestampMinusTimeStamp());
        _sqlTokenInfoMap.put(SqlToken.TimestampMinusDate, _constants.sqlTokenDescription_TimestampMinusDate());
        _sqlTokenInfoMap.put(SqlToken.TimestampMinusTime, _constants.sqlTokenDescription_TimestampMinusTime());

        _sqlTokenInfoMap.put(SqlToken.DatePlusDays, _constants.sqlTokenDescription_DatePlusDays());
        _sqlTokenInfoMap.put(SqlToken.DatePlusTime, _constants.sqlTokenDescription_DatePlusTime());
        _sqlTokenInfoMap.put(SqlToken.DateMinusDate, _constants.sqlTokenDescription_DateMinusDate());
        _sqlTokenInfoMap.put(SqlToken.DateMinusDays, _constants.sqlTokenDescription_DateMinusDays());
        _sqlTokenInfoMap.put(SqlToken.DateMinusTime, _constants.sqlTokenDescription_DateMinusTime());

        _sqlTokenInfoMap.put(SqlToken.TimePlusTime, _constants.sqlTokenDescription_TimePlusTime());
        _sqlTokenInfoMap.put(SqlToken.TimeMinusTime, _constants.sqlTokenDescription_TimeMinusTime());
        _sqlTokenInfoMap.put(SqlToken.TimeMultiplied, _constants.sqlTokenDescription_TimeMultiplied());
        _sqlTokenInfoMap.put(SqlToken.TimeDivided, _constants.sqlTokenDescription_TimeDivided());

        _sqlTokenInfoMap.put(SqlToken.TimeAsTimeOfDay, _constants.sqlTokenDescription_TimeAsTimeOfDay());
        _sqlTokenInfoMap.put(SqlToken.TimestampAsDate, _constants.sqlTokenDescription_TimestampAsDate());
        _sqlTokenInfoMap.put(SqlToken.AsNumberOfDays, _constants.sqlTokenDescription_AsNumberOfDays());
        _sqlTokenInfoMap.put(SqlToken.AsNumberOfHours, _constants.sqlTokenDescription_AsNumberOfHours());
        _sqlTokenInfoMap.put(SqlToken.AsNumberOfMinutes, _constants.sqlTokenDescription_AsNumberOfMinutes());
        _sqlTokenInfoMap.put(SqlToken.AsNumberOfSeconds, _constants.sqlTokenDescription_AsNumberOfSeconds());
        _sqlTokenInfoMap.put(SqlToken.AsNumberOfMilliseconds, _constants.sqlTokenDescription_AsNumberOfMilliseconds());
        _sqlTokenInfoMap.put(SqlToken.AsNumberOfMicroseconds, _constants.sqlTokenDescription_AsNumberOfMicroseconds());

        _sqlTokenInfoMap.put(SqlToken.CurrentTimestamp, _constants.sqlTokenDescription_CurrentTimestamp());
        _sqlTokenInfoMap.put(SqlToken.CurrentDate, _constants.sqlTokenDescription_CurrentDate());
        _sqlTokenInfoMap.put(SqlToken.CurrentTime, _constants.sqlTokenDescription_CurrentTime());
        _sqlTokenInfoMap.put(SqlToken.DateDateExtract, _constants.sqlTokenDescription_DateDateExtract());
        _sqlTokenInfoMap.put(SqlToken.DateTimeExtract, _constants.sqlTokenDescription_DateTimeExtract());
        _sqlTokenInfoMap.put(SqlToken.DateEpochExtract, _constants.sqlTokenDescription_DateEpochExtract());
        _sqlTokenInfoMap.put(SqlToken.DateMillenniumExtract, _constants.sqlTokenDescription_DateMillenniumExtract());
        _sqlTokenInfoMap.put(SqlToken.DateCenturyExtract, _constants.sqlTokenDescription_DateCenturyExtract());
        _sqlTokenInfoMap.put(SqlToken.DateDecadeExtract, _constants.sqlTokenDescription_DateDecadeExtract());
        _sqlTokenInfoMap.put(SqlToken.DateYearExtract, _constants.sqlTokenDescription_DateYearExtract());
        _sqlTokenInfoMap.put(SqlToken.DateQuarterExtract, _constants.sqlTokenDescription_DateQuarterExtract());
        _sqlTokenInfoMap.put(SqlToken.DateMonthExtract, _constants.sqlTokenDescription_DateMonthExtract());
        _sqlTokenInfoMap.put(SqlToken.DateWeekExtract, _constants.sqlTokenDescription_DateWeekExtract());
        _sqlTokenInfoMap.put(SqlToken.DateYearDayExtract, _constants.sqlTokenDescription_DateYearDayExtract());
        _sqlTokenInfoMap.put(SqlToken.DateMonthDayExtract, _constants.sqlTokenDescription_DateMonthDayExtract());
        _sqlTokenInfoMap.put(SqlToken.DateWeekDayExtract, _constants.sqlTokenDescription_DateWeekDayExtract());
        _sqlTokenInfoMap.put(SqlToken.DateHourExtract, _constants.sqlTokenDescription_DateHourExtract());
        _sqlTokenInfoMap.put(SqlToken.DateMinuteExtract, _constants.sqlTokenDescription_DateMinuteExtract());
        _sqlTokenInfoMap.put(SqlToken.DateSecondExtract, _constants.sqlTokenDescription_DateSecondExtract());
        _sqlTokenInfoMap.put(SqlToken.DateMillisecondsExtract, _constants.sqlTokenDescription_DateMillisecondsExtract());
        _sqlTokenInfoMap.put(SqlToken.DateMicrosecondsExtract, _constants.sqlTokenDescription_DateMicrosecondsExtract());
        _sqlTokenInfoMap.put(SqlToken.DateTimezoneExtract, _constants.sqlTokenDescription_DateTimezoneExtract());

        _sqlTokenInfoMap.put(SqlToken.DayPartDifference, _constants.sqlTokenDescription_DayPartDifference());
        _sqlTokenInfoMap.put(SqlToken.HourPartDifference, _constants.sqlTokenDescription_HourPartDifference());
        _sqlTokenInfoMap.put(SqlToken.MinutePartDifference, _constants.sqlTokenDescription_MinutePartDifference());
        _sqlTokenInfoMap.put(SqlToken.SecondPartDifference, _constants.sqlTokenDescription_SecondPartDifference());
        _sqlTokenInfoMap.put(SqlToken.MillisecondPartDifference, _constants.sqlTokenDescription_MillisecondPartDifference());

        _sqlTokenInfoMap.put(SqlToken.DateMillenniumTruncate, _constants.sqlTokenDescription_DateMillenniumTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateCenturyTruncate, _constants.sqlTokenDescription_DateCenturyTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateDecadeTruncate, _constants.sqlTokenDescription_DateDecadeTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateYearTruncate, _constants.sqlTokenDescription_DateYearTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateQuarterTruncate, _constants.sqlTokenDescription_DateQuarterTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateMonthTruncate, _constants.sqlTokenDescription_DateMonthTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateWeekTruncate, _constants.sqlTokenDescription_DateWeekTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateMonthDayTruncate, _constants.sqlTokenDescription_DateMonthDayTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateHourTruncate, _constants.sqlTokenDescription_DateHourTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateMinuteTruncate, _constants.sqlTokenDescription_DateMinuteTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateSecondTruncate, _constants.sqlTokenDescription_DateSecondTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateMillisecondsTruncate, _constants.sqlTokenDescription_DateMillisecondsTruncate());
        _sqlTokenInfoMap.put(SqlToken.DateMicrosecondsTruncate, _constants.sqlTokenDescription_DateMicrosecondsTruncate());
        _sqlTokenInfoMap.put(SqlToken.StringField, _constants.sqlTokenDescription_StringField());
        _sqlTokenInfoMap.put(SqlToken.BooleanField, _constants.sqlTokenDescription_BooleanField());
        _sqlTokenInfoMap.put(SqlToken.IntegerField, _constants.sqlTokenDescription_IntegerField());
        _sqlTokenInfoMap.put(SqlToken.NumberField, _constants.sqlTokenDescription_NumberField());
        _sqlTokenInfoMap.put(SqlToken.DateTimeField, _constants.sqlTokenDescription_DateTimeField());
        _sqlTokenInfoMap.put(SqlToken.DateField, _constants.sqlTokenDescription_DateField());
        _sqlTokenInfoMap.put(SqlToken.TimeField, _constants.sqlTokenDescription_TimeField());
        _sqlTokenInfoMap.put(SqlToken.StringParameter, _constants.sqlTokenDescription_StringParameter());
        _sqlTokenInfoMap.put(SqlToken.BooleanParameter, _constants.sqlTokenDescription_BooleanParameter());
        _sqlTokenInfoMap.put(SqlToken.IntegerParameter, _constants.sqlTokenDescription_IntegerParameter());
        _sqlTokenInfoMap.put(SqlToken.NumberParameter, _constants.sqlTokenDescription_NumberParameter());
        _sqlTokenInfoMap.put(SqlToken.DateTimeParameter, _constants.sqlTokenDescription_DateTimeParameter());
        _sqlTokenInfoMap.put(SqlToken.DateParameter, _constants.sqlTokenDescription_DateParameter());
        _sqlTokenInfoMap.put(SqlToken.TimeParameter, _constants.sqlTokenDescription_TimeParameter());
        _sqlTokenInfoMap.put(SqlToken.StringValue, _constants.sqlTokenDescription_StringValue());
        _sqlTokenInfoMap.put(SqlToken.BooleanValue, _constants.sqlTokenDescription_BooleanValue());
        _sqlTokenInfoMap.put(SqlToken.IntegerValue, _constants.sqlTokenDescription_IntegerValue());
        _sqlTokenInfoMap.put(SqlToken.NumberValue, _constants.sqlTokenDescription_NumberValue());
        _sqlTokenInfoMap.put(SqlToken.DateTimeValue, _constants.sqlTokenDescription_DateTimeValue());
        _sqlTokenInfoMap.put(SqlToken.DateValue, _constants.sqlTokenDescription_DateValue());
        _sqlTokenInfoMap.put(SqlToken.TimeValue, _constants.sqlTokenDescription_TimeValue());

        _sqlTokenInfoMap.put(SqlToken.FormatValue, _constants.sqlTokenDescription_FormatValue());
        _sqlTokenInfoMap.put(SqlToken.InputUnixEpoch, _constants.sqlTokenDescription_FormatValue());
        _sqlTokenInfoMap.put(SqlToken.InputUnixEpoch, _constants.sqlTokenDescription_EpochToDateTime());
        _sqlTokenInfoMap.put(SqlToken.ScanDateTime, _constants.sqlTokenDescription_ScanDateTime());
        _sqlTokenInfoMap.put(SqlToken.ScanDate, _constants.sqlTokenDescription_ScanDate());
        _sqlTokenInfoMap.put(SqlToken.ScanInteger, _constants.sqlTokenDescription_ScanInteger());
        _sqlTokenInfoMap.put(SqlToken.ScanDecimal, _constants.sqlTokenDescription_ScanDecimal());
        _sqlTokenInfoMap.put(SqlToken.NumToken, _constants.sqlTokenDescription_NumToken());
        _sqlTokenInfoMap.put(SqlToken.GetSingleToken, _constants.sqlTokenDescription_GetSingleToken());
        _sqlTokenInfoMap.put(SqlToken.GetLastToken, _constants.sqlTokenDescription_GetLastToken());

        _sqlTokenInfoMap.put(SqlToken.StringConditional, _constants.sqlTokenDescription_Conditional());
        _sqlTokenInfoMap.put(SqlToken.BooleanConditional, _constants.sqlTokenDescription_Conditional());
        _sqlTokenInfoMap.put(SqlToken.IntegerConditional, _constants.sqlTokenDescription_Conditional());
        _sqlTokenInfoMap.put(SqlToken.NumberConditional, _constants.sqlTokenDescription_Conditional());
        _sqlTokenInfoMap.put(SqlToken.DateTimeConditional, _constants.sqlTokenDescription_Conditional());
        _sqlTokenInfoMap.put(SqlToken.DateConditional, _constants.sqlTokenDescription_Conditional());
        _sqlTokenInfoMap.put(SqlToken.TimeConditional, _constants.sqlTokenDescription_Conditional());

        _sqlTokenInfoMap.put(SqlToken.StringConditionalComponent1, _constants.sqlTokenDescription_ConditionalComponent1());
        _sqlTokenInfoMap.put(SqlToken.BooleanConditionalComponent1, _constants.sqlTokenDescription_ConditionalComponent1());
        _sqlTokenInfoMap.put(SqlToken.IntegerConditionalComponent1, _constants.sqlTokenDescription_ConditionalComponent1());
        _sqlTokenInfoMap.put(SqlToken.NumberConditionalComponent1, _constants.sqlTokenDescription_ConditionalComponent1());
        _sqlTokenInfoMap.put(SqlToken.DateTimeConditionalComponent1, _constants.sqlTokenDescription_ConditionalComponent1());
        _sqlTokenInfoMap.put(SqlToken.DateConditionalComponent1, _constants.sqlTokenDescription_ConditionalComponent1());
        _sqlTokenInfoMap.put(SqlToken.TimeConditionalComponent1, _constants.sqlTokenDescription_ConditionalComponent1());

        _sqlTokenInfoMap.put(SqlToken.StringConditionalComponent2, _constants.sqlTokenDescription_ConditionalComponent2());
        _sqlTokenInfoMap.put(SqlToken.BooleanConditionalComponent2, _constants.sqlTokenDescription_ConditionalComponent2());
        _sqlTokenInfoMap.put(SqlToken.IntegerConditionalComponent2, _constants.sqlTokenDescription_ConditionalComponent2());
        _sqlTokenInfoMap.put(SqlToken.NumberConditionalComponent2, _constants.sqlTokenDescription_ConditionalComponent2());
        _sqlTokenInfoMap.put(SqlToken.DateTimeConditionalComponent2, _constants.sqlTokenDescription_ConditionalComponent2());
        _sqlTokenInfoMap.put(SqlToken.DateConditionalComponent2, _constants.sqlTokenDescription_ConditionalComponent2());
        _sqlTokenInfoMap.put(SqlToken.TimeConditionalComponent2, _constants.sqlTokenDescription_ConditionalComponent2());

        _sqlTokenInfoMap.put(SqlToken.StringConditionalDefault, _constants.sqlTokenDescription_ConditionalDefault());
        _sqlTokenInfoMap.put(SqlToken.BooleanConditionalDefault, _constants.sqlTokenDescription_ConditionalDefault());
        _sqlTokenInfoMap.put(SqlToken.IntegerConditionalDefault, _constants.sqlTokenDescription_ConditionalDefault());
        _sqlTokenInfoMap.put(SqlToken.NumberConditionalDefault, _constants.sqlTokenDescription_ConditionalDefault());
        _sqlTokenInfoMap.put(SqlToken.DateTimeConditionalDefault, _constants.sqlTokenDescription_ConditionalDefault());
        _sqlTokenInfoMap.put(SqlToken.DateConditionalDefault, _constants.sqlTokenDescription_ConditionalDefault());
        _sqlTokenInfoMap.put(SqlToken.TimeConditionalDefault, _constants.sqlTokenDescription_ConditionalDefault());

        _sqlTokenInfoMap.put(SqlToken.Extract1stValue, _constants.sqlTokenDescription_Extract1stValue());
        _sqlTokenInfoMap.put(SqlToken.Extract2ndValue, _constants.sqlTokenDescription_Extract2ndValue());
        _sqlTokenInfoMap.put(SqlToken.ExtractNthValue, _constants.sqlTokenDescription_ExtractNthValue());
        _sqlTokenInfoMap.put(SqlToken.ExtractLastValue, _constants.sqlTokenDescription_ExtractLastValue());
        _sqlTokenInfoMap.put(SqlToken.ExtractLastValue, _constants.sqlTokenDescription_ExtractLastValue());
        _sqlTokenInfoMap.put(SqlToken.ExtractMatchingString, _constants.sqlTokenDescription_ExtractMatchingString());

        _sqlTokenInfoMap.put(SqlToken.NullNumber, _constants.sqlTokenDescription_NullValue());
        _sqlTokenInfoMap.put(SqlToken.NullInteger, _constants.sqlTokenDescription_NullValue());
        _sqlTokenInfoMap.put(SqlToken.NullBoolean, _constants.sqlTokenDescription_NullValue());
        _sqlTokenInfoMap.put(SqlToken.NullDate, _constants.sqlTokenDescription_NullValue());
        _sqlTokenInfoMap.put(SqlToken.NullTime, _constants.sqlTokenDescription_NullValue());
        _sqlTokenInfoMap.put(SqlToken.NullDateTime, _constants.sqlTokenDescription_NullValue());
        _sqlTokenInfoMap.put(SqlToken.NullString, _constants.sqlTokenDescription_NullValue());



        _sqlTokenHelpMap.put(SqlToken.BooleanAnd, _constants.sqlTokenHelp_BooleanAnd());
        _sqlTokenHelpMap.put(SqlToken.BooleanOR, _constants.sqlTokenHelp_BooleanOR());
        _sqlTokenHelpMap.put(SqlToken.BooleanNOT, _constants.sqlTokenHelp_BooleanNOT());
        _sqlTokenHelpMap.put(SqlToken.IsTrue, _constants.sqlTokenHelp_IsTrue());
        _sqlTokenHelpMap.put(SqlToken.IsNotTrue, _constants.sqlTokenHelp_IsNotTrue());
        _sqlTokenHelpMap.put(SqlToken.IsFalse, _constants.sqlTokenHelp_IsFalse());
        _sqlTokenHelpMap.put(SqlToken.IsNotFalse, _constants.sqlTokenHelp_IsNotFalse());
        _sqlTokenHelpMap.put(SqlToken.IsUnknown, _constants.sqlTokenHelp_IsUnknown());
        _sqlTokenHelpMap.put(SqlToken.IsNotUnknown, _constants.sqlTokenHelp_IsNotUnknown());
        _sqlTokenHelpMap.put(SqlToken.Equal, _constants.sqlTokenHelp_Equal());
        _sqlTokenHelpMap.put(SqlToken.NotEqual, _constants.sqlTokenHelp_NotEqual());
        _sqlTokenHelpMap.put(SqlToken.IsDistinct, _constants.sqlTokenHelp_IsDistinct());
        _sqlTokenHelpMap.put(SqlToken.IsNotDistinct, _constants.sqlTokenHelp_IsNotDistinct());
        _sqlTokenHelpMap.put(SqlToken.IsNull, _constants.sqlTokenHelp_IsNull());
        _sqlTokenHelpMap.put(SqlToken.IsNotNull, _constants.sqlTokenHelp_IsNotNull());
        _sqlTokenHelpMap.put(SqlToken.LessThan, _constants.sqlTokenHelp_LessThan());
        _sqlTokenHelpMap.put(SqlToken.GreaterThan, _constants.sqlTokenHelp_GreaterThan());
        _sqlTokenHelpMap.put(SqlToken.LessThanOrEqual, _constants.sqlTokenHelp_LessThanOrEqual());
        _sqlTokenHelpMap.put(SqlToken.GreaterThanOrEqual, _constants.sqlTokenHelp_GreaterThanOrEqual());
        _sqlTokenHelpMap.put(SqlToken.Between, _constants.sqlTokenHelp_Between());
        _sqlTokenHelpMap.put(SqlToken.NotBetween, _constants.sqlTokenHelp_NotBetween());
        _sqlTokenHelpMap.put(SqlToken.BetweenSymetric, _constants.sqlTokenHelp_BetweenSymetric());
        _sqlTokenHelpMap.put(SqlToken.NotBetweenSymetric, _constants.sqlTokenHelp_NotBetweenSymetric());
        _sqlTokenHelpMap.put(SqlToken.RegularExpression, _constants.sqlTokenHelp_RegularExpression());
        _sqlTokenHelpMap.put(SqlToken.CaselessRegularExpression, _constants.sqlTokenHelp_CaselessRegularExpression());
        _sqlTokenHelpMap.put(SqlToken.BitwiseAnd, _constants.sqlTokenHelp_BitwiseAnd());
        _sqlTokenHelpMap.put(SqlToken.BitwiseOR, _constants.sqlTokenHelp_BitwiseOR());
        _sqlTokenHelpMap.put(SqlToken.BitwiseXOR, _constants.sqlTokenHelp_BitwiseXOR());
        _sqlTokenHelpMap.put(SqlToken.BitwiseNOT, _constants.sqlTokenHelp_BitwiseNOT());
        _sqlTokenHelpMap.put(SqlToken.ShiftLeft, _constants.sqlTokenHelp_ShiftLeft());
        _sqlTokenHelpMap.put(SqlToken.ShiftRight, _constants.sqlTokenHelp_ShiftRight());
        _sqlTokenHelpMap.put(SqlToken.IntegerAddition, _constants.sqlTokenHelp_IntegerAddition());
        _sqlTokenHelpMap.put(SqlToken.IntegerSubtraction, _constants.sqlTokenHelp_IntegerSubtraction());
        _sqlTokenHelpMap.put(SqlToken.IntegerMultiplication, _constants.sqlTokenHelp_IntegerMultiplication());
        _sqlTokenHelpMap.put(SqlToken.IntegerDivision, _constants.sqlTokenHelp_IntegerDivision());
        _sqlTokenHelpMap.put(SqlToken.ModuloDivision, _constants.sqlTokenHelp_ModuloDivision());
        _sqlTokenHelpMap.put(SqlToken.IntegerFactorial, _constants.sqlTokenHelp_IntegerFactorial());
        _sqlTokenHelpMap.put(SqlToken.IntegerExponentiation, _constants.sqlTokenHelp_IntegerExponentiation());
        _sqlTokenHelpMap.put(SqlToken.DecimalAddition, _constants.sqlTokenHelp_DecimalAddition());
        _sqlTokenHelpMap.put(SqlToken.DecimalSubtraction, _constants.sqlTokenHelp_DecimalSubtraction());
        _sqlTokenHelpMap.put(SqlToken.DecimalMultiplication, _constants.sqlTokenHelp_DecimalMultiplication());
        _sqlTokenHelpMap.put(SqlToken.DecimalDivision, _constants.sqlTokenHelp_DecimalDivision());
        _sqlTokenHelpMap.put(SqlToken.DecimalExponentiation, _constants.sqlTokenHelp_DecimalExponentiation());
        _sqlTokenHelpMap.put(SqlToken.CastInteger, _constants.sqlTokenHelp_CastInteger());
        _sqlTokenHelpMap.put(SqlToken.CastDecimal, _constants.sqlTokenHelp_CastDecimal());
        _sqlTokenHelpMap.put(SqlToken.CastDate, _constants.sqlTokenHelp_CastDate());
        _sqlTokenHelpMap.put(SqlToken.CastTime, _constants.sqlTokenHelp_CastTime());
        _sqlTokenHelpMap.put(SqlToken.CastDateTime, _constants.sqlTokenHelp_CastDateTime());
        _sqlTokenHelpMap.put(SqlToken.CastBoolean, _constants.sqlTokenHelp_CastBoolean());
        _sqlTokenHelpMap.put(SqlToken.TruncateToInteger, _constants.sqlTokenHelp_TruncateToInteger());
        _sqlTokenHelpMap.put(SqlToken.TruncateDecimal, _constants.sqlTokenHelp_TruncateDecimal());
        _sqlTokenHelpMap.put(SqlToken.RoundToInteger, _constants.sqlTokenHelp_RoundToInteger());
        _sqlTokenHelpMap.put(SqlToken.RoundDecimal, _constants.sqlTokenHelp_RoundDecimal());
        _sqlTokenHelpMap.put(SqlToken.AbsoluteInteger, _constants.sqlTokenHelp_AbsoluteInteger());
        _sqlTokenHelpMap.put(SqlToken.AbsoluteDecimal, _constants.sqlTokenHelp_AbsoluteDecimal());
        _sqlTokenHelpMap.put(SqlToken.SquareRoot, _constants.sqlTokenHelp_SquareRoot());
        _sqlTokenHelpMap.put(SqlToken.CubeRoot, _constants.sqlTokenHelp_CubeRoot());
        _sqlTokenHelpMap.put(SqlToken.NaturalLog, _constants.sqlTokenHelp_NaturalLog());
        _sqlTokenHelpMap.put(SqlToken.LogBase10, _constants.sqlTokenHelp_LogBase10());
        _sqlTokenHelpMap.put(SqlToken.Sine, _constants.sqlTokenHelp_Sine());
        _sqlTokenHelpMap.put(SqlToken.Cosine, _constants.sqlTokenHelp_Cosine());
        _sqlTokenHelpMap.put(SqlToken.Tangent, _constants.sqlTokenHelp_Tangent());
        _sqlTokenHelpMap.put(SqlToken.Cotangent, _constants.sqlTokenHelp_Cotangent());
        _sqlTokenHelpMap.put(SqlToken.InverseSine, _constants.sqlTokenHelp_InverseSine());
        _sqlTokenHelpMap.put(SqlToken.InverseCosine, _constants.sqlTokenHelp_InverseCosine());
        _sqlTokenHelpMap.put(SqlToken.InverseTangent, _constants.sqlTokenHelp_InverseTangent());
        _sqlTokenHelpMap.put(SqlToken.InverseCotangent, _constants.sqlTokenHelp_InverseCotangent());
        _sqlTokenHelpMap.put(SqlToken.Concatenation, _constants.sqlTokenHelp_Concatenation());
        _sqlTokenHelpMap.put(SqlToken.ToString, _constants.sqlTokenHelp_ToString());
        _sqlTokenHelpMap.put(SqlToken.CharacterLength, _constants.sqlTokenHelp_CharacterLength());
        _sqlTokenHelpMap.put(SqlToken.ByteCount, _constants.sqlTokenHelp_ByteCount());
        _sqlTokenHelpMap.put(SqlToken.BitCount, _constants.sqlTokenHelp_BitCount());
        _sqlTokenHelpMap.put(SqlToken.Capitalize, _constants.sqlTokenHelp_Capitalize());
        _sqlTokenHelpMap.put(SqlToken.UpperCase, _constants.sqlTokenHelp_UpperCase());
        _sqlTokenHelpMap.put(SqlToken.LowerCase, _constants.sqlTokenHelp_LowerCase());
        _sqlTokenHelpMap.put(SqlToken.LeftTrim1, _constants.sqlTokenHelp_LeftTrim1());
        _sqlTokenHelpMap.put(SqlToken.LeftTrim2, _constants.sqlTokenHelp_LeftTrim2());
        _sqlTokenHelpMap.put(SqlToken.RightTrim1, _constants.sqlTokenHelp_RightTrim1());
        _sqlTokenHelpMap.put(SqlToken.RightTrim2, _constants.sqlTokenHelp_RightTrim2());
        _sqlTokenHelpMap.put(SqlToken.LeftPad1, _constants.sqlTokenHelp_LeftPad1());
        _sqlTokenHelpMap.put(SqlToken.LeftPad2, _constants.sqlTokenHelp_LeftPad2());
        _sqlTokenHelpMap.put(SqlToken.RightPad1, _constants.sqlTokenHelp_RightPad1());
        _sqlTokenHelpMap.put(SqlToken.RightPad2, _constants.sqlTokenHelp_RightPad2());
        _sqlTokenHelpMap.put(SqlToken.Substring1, _constants.sqlTokenHelp_Substring1());
        _sqlTokenHelpMap.put(SqlToken.Substring2, _constants.sqlTokenHelp_Substring2());
        _sqlTokenHelpMap.put(SqlToken.LocateSubstring, _constants.sqlTokenHelp_LocateSubstring());

        _sqlTokenHelpMap.put(SqlToken.TimestampPlusTime, _constants.sqlTokenHelp_TimestampPlusTime());
        _sqlTokenHelpMap.put(SqlToken.TimestampMinusTimeStamp, _constants.sqlTokenHelp_TimestampMinusTimeStamp());
        _sqlTokenHelpMap.put(SqlToken.TimestampMinusDate, _constants.sqlTokenHelp_TimestampMinusDate());
        _sqlTokenHelpMap.put(SqlToken.TimestampMinusTime, _constants.sqlTokenHelp_TimestampMinusTime());

        _sqlTokenHelpMap.put(SqlToken.DatePlusDays, _constants.sqlTokenHelp_DatePlusDays());
        _sqlTokenHelpMap.put(SqlToken.DatePlusTime, _constants.sqlTokenHelp_DatePlusTime());
        _sqlTokenHelpMap.put(SqlToken.DateMinusDate, _constants.sqlTokenHelp_DateMinusDate());
        _sqlTokenHelpMap.put(SqlToken.DateMinusDays, _constants.sqlTokenHelp_DateMinusDays());
        _sqlTokenHelpMap.put(SqlToken.DateMinusTime, _constants.sqlTokenHelp_DateMinusTime());

        _sqlTokenHelpMap.put(SqlToken.TimePlusTime, _constants.sqlTokenHelp_TimePlusTime());
        _sqlTokenHelpMap.put(SqlToken.TimeMinusTime, _constants.sqlTokenHelp_TimeMinusTime());
        _sqlTokenHelpMap.put(SqlToken.TimeMultiplied, _constants.sqlTokenHelp_TimeMultiplied());
        _sqlTokenHelpMap.put(SqlToken.TimeDivided, _constants.sqlTokenHelp_TimeDivided());

        _sqlTokenHelpMap.put(SqlToken.TimeAsTimeOfDay, _constants.sqlTokenHelp_TimeAsTimeOfDay());
        _sqlTokenHelpMap.put(SqlToken.TimestampAsDate, _constants.sqlTokenHelp_TimestampAsDate());
        _sqlTokenHelpMap.put(SqlToken.AsNumberOfDays, _constants.sqlTokenHelp_AsNumberOfDays());
        _sqlTokenHelpMap.put(SqlToken.AsNumberOfHours, _constants.sqlTokenHelp_AsNumberOfHours());
        _sqlTokenHelpMap.put(SqlToken.AsNumberOfMinutes, _constants.sqlTokenHelp_AsNumberOfMinutes());
        _sqlTokenHelpMap.put(SqlToken.AsNumberOfSeconds, _constants.sqlTokenHelp_AsNumberOfSeconds());
        _sqlTokenHelpMap.put(SqlToken.AsNumberOfMilliseconds, _constants.sqlTokenHelp_AsNumberOfMilliseconds());
        _sqlTokenHelpMap.put(SqlToken.AsNumberOfMicroseconds, _constants.sqlTokenHelp_AsNumberOfMicroseconds());

        _sqlTokenHelpMap.put(SqlToken.CurrentTimestamp, _constants.sqlTokenHelp_CurrentTimestamp());
        _sqlTokenHelpMap.put(SqlToken.CurrentDate, _constants.sqlTokenHelp_CurrentDate());
        _sqlTokenHelpMap.put(SqlToken.CurrentTime, _constants.sqlTokenHelp_CurrentTime());
        _sqlTokenHelpMap.put(SqlToken.DateDateExtract, _constants.sqlTokenHelp_DateDateExtract());
        _sqlTokenHelpMap.put(SqlToken.DateTimeExtract, _constants.sqlTokenHelp_DateTimeExtract());
        _sqlTokenHelpMap.put(SqlToken.DateEpochExtract, _constants.sqlTokenHelp_DateEpochExtract());
        _sqlTokenHelpMap.put(SqlToken.DateMillenniumExtract, _constants.sqlTokenHelp_DateMillenniumExtract());
        _sqlTokenHelpMap.put(SqlToken.DateCenturyExtract, _constants.sqlTokenHelp_DateCenturyExtract());
        _sqlTokenHelpMap.put(SqlToken.DateDecadeExtract, _constants.sqlTokenHelp_DateDecadeExtract());
        _sqlTokenHelpMap.put(SqlToken.DateYearExtract, _constants.sqlTokenHelp_DateYearExtract());
        _sqlTokenHelpMap.put(SqlToken.DateQuarterExtract, _constants.sqlTokenHelp_DateQuarterExtract());
        _sqlTokenHelpMap.put(SqlToken.DateMonthExtract, _constants.sqlTokenHelp_DateMonthExtract());
        _sqlTokenHelpMap.put(SqlToken.DateWeekExtract, _constants.sqlTokenHelp_DateWeekExtract());
        _sqlTokenHelpMap.put(SqlToken.DateYearDayExtract, _constants.sqlTokenHelp_DateYearDayExtract());
        _sqlTokenHelpMap.put(SqlToken.DateMonthDayExtract, _constants.sqlTokenHelp_DateMonthDayExtract());
        _sqlTokenHelpMap.put(SqlToken.DateWeekDayExtract, _constants.sqlTokenHelp_DateWeekDayExtract());
        _sqlTokenHelpMap.put(SqlToken.DateHourExtract, _constants.sqlTokenHelp_DateHourExtract());
        _sqlTokenHelpMap.put(SqlToken.DateMinuteExtract, _constants.sqlTokenHelp_DateMinuteExtract());
        _sqlTokenHelpMap.put(SqlToken.DateSecondExtract, _constants.sqlTokenHelp_DateSecondExtract());
        _sqlTokenHelpMap.put(SqlToken.DateMillisecondsExtract, _constants.sqlTokenHelp_DateMillisecondsExtract());
        _sqlTokenHelpMap.put(SqlToken.DateMicrosecondsExtract, _constants.sqlTokenHelp_DateMicrosecondsExtract());
        _sqlTokenHelpMap.put(SqlToken.DateTimezoneExtract, _constants.sqlTokenHelp_DateTimezoneExtract());

        _sqlTokenHelpMap.put(SqlToken.DayPartDifference, _constants.sqlTokenHelp_DayPartDifference());
        _sqlTokenHelpMap.put(SqlToken.HourPartDifference, _constants.sqlTokenHelp_HourPartDifference());
        _sqlTokenHelpMap.put(SqlToken.MinutePartDifference, _constants.sqlTokenHelp_MinutePartDifference());
        _sqlTokenHelpMap.put(SqlToken.SecondPartDifference, _constants.sqlTokenHelp_SecondPartDifference());
        _sqlTokenHelpMap.put(SqlToken.MillisecondPartDifference, _constants.sqlTokenHelp_MillisecondPartDifference());

        _sqlTokenHelpMap.put(SqlToken.DateMillenniumTruncate, _constants.sqlTokenHelp_DateMillenniumTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateCenturyTruncate, _constants.sqlTokenHelp_DateCenturyTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateDecadeTruncate, _constants.sqlTokenHelp_DateDecadeTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateYearTruncate, _constants.sqlTokenHelp_DateYearTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateQuarterTruncate, _constants.sqlTokenHelp_DateQuarterTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateMonthTruncate, _constants.sqlTokenHelp_DateMonthTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateWeekTruncate, _constants.sqlTokenHelp_DateWeekTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateMonthDayTruncate, _constants.sqlTokenHelp_DateMonthDayTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateHourTruncate, _constants.sqlTokenHelp_DateHourTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateMinuteTruncate, _constants.sqlTokenHelp_DateMinuteTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateSecondTruncate, _constants.sqlTokenHelp_DateSecondTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateMillisecondsTruncate, _constants.sqlTokenHelp_DateMillisecondsTruncate());
        _sqlTokenHelpMap.put(SqlToken.DateMicrosecondsTruncate, _constants.sqlTokenHelp_DateMicrosecondsTruncate());
        _sqlTokenHelpMap.put(SqlToken.StringField, _constants.sqlTokenHelp_StringField());
        _sqlTokenHelpMap.put(SqlToken.BooleanField, _constants.sqlTokenHelp_BooleanField());
        _sqlTokenHelpMap.put(SqlToken.IntegerField, _constants.sqlTokenHelp_IntegerField());
        _sqlTokenHelpMap.put(SqlToken.NumberField, _constants.sqlTokenHelp_NumberField());
        _sqlTokenHelpMap.put(SqlToken.DateTimeField, _constants.sqlTokenHelp_DateTimeField());
        _sqlTokenHelpMap.put(SqlToken.DateField, _constants.sqlTokenHelp_DateField());
        _sqlTokenHelpMap.put(SqlToken.TimeField, _constants.sqlTokenHelp_TimeField());
        _sqlTokenHelpMap.put(SqlToken.StringParameter, _constants.sqlTokenHelp_StringParameter());
        _sqlTokenHelpMap.put(SqlToken.BooleanParameter, _constants.sqlTokenHelp_BooleanParameter());
        _sqlTokenHelpMap.put(SqlToken.IntegerParameter, _constants.sqlTokenHelp_IntegerParameter());
        _sqlTokenHelpMap.put(SqlToken.NumberParameter, _constants.sqlTokenHelp_NumberParameter());
        _sqlTokenHelpMap.put(SqlToken.DateTimeParameter, _constants.sqlTokenHelp_DateTimeParameter());
        _sqlTokenHelpMap.put(SqlToken.DateParameter, _constants.sqlTokenHelp_DateParameter());
        _sqlTokenHelpMap.put(SqlToken.TimeParameter, _constants.sqlTokenHelp_TimeParameter());
        _sqlTokenHelpMap.put(SqlToken.StringValue, _constants.sqlTokenHelp_StringValue());
        _sqlTokenHelpMap.put(SqlToken.BooleanValue, _constants.sqlTokenHelp_BooleanValue());
        _sqlTokenHelpMap.put(SqlToken.IntegerValue, _constants.sqlTokenHelp_IntegerValue());
        _sqlTokenHelpMap.put(SqlToken.NumberValue, _constants.sqlTokenHelp_NumberValue());
        _sqlTokenHelpMap.put(SqlToken.DateTimeValue, _constants.sqlTokenHelp_DateTimeValue());
        _sqlTokenHelpMap.put(SqlToken.DateValue, _constants.sqlTokenHelp_DateValue());
        _sqlTokenHelpMap.put(SqlToken.TimeValue, _constants.sqlTokenHelp_TimeValue());

        _sqlTokenHelpMap.put(SqlToken.FormatValue, _constants.sqlTokenHelp_FormatValue());
        _sqlTokenHelpMap.put(SqlToken.InputUnixEpoch, _constants.sqlTokenHelp_FormatValue());
        _sqlTokenHelpMap.put(SqlToken.InputUnixEpoch, _constants.sqlTokenHelp_EpochToDateTime());
        _sqlTokenHelpMap.put(SqlToken.ScanDateTime, _constants.sqlTokenHelp_ScanDateTime());
        _sqlTokenHelpMap.put(SqlToken.ScanDate, _constants.sqlTokenHelp_ScanDate());
        _sqlTokenHelpMap.put(SqlToken.ScanInteger, _constants.sqlTokenHelp_ScanInteger());
        _sqlTokenHelpMap.put(SqlToken.ScanDecimal, _constants.sqlTokenHelp_ScanDecimal());
        _sqlTokenHelpMap.put(SqlToken.NumToken, _constants.sqlTokenHelp_NumToken());
        _sqlTokenHelpMap.put(SqlToken.GetSingleToken, _constants.sqlTokenHelp_GetSingleToken());
        _sqlTokenHelpMap.put(SqlToken.GetLastToken, _constants.sqlTokenHelp_GetLastToken());

        _sqlTokenHelpMap.put(SqlToken.StringConditional, _constants.sqlTokenHelp_Conditional());
        _sqlTokenHelpMap.put(SqlToken.BooleanConditional, _constants.sqlTokenHelp_Conditional());
        _sqlTokenHelpMap.put(SqlToken.IntegerConditional, _constants.sqlTokenHelp_Conditional());
        _sqlTokenHelpMap.put(SqlToken.NumberConditional, _constants.sqlTokenHelp_Conditional());
        _sqlTokenHelpMap.put(SqlToken.DateTimeConditional, _constants.sqlTokenHelp_Conditional());
        _sqlTokenHelpMap.put(SqlToken.DateConditional, _constants.sqlTokenHelp_Conditional());
        _sqlTokenHelpMap.put(SqlToken.TimeConditional, _constants.sqlTokenHelp_Conditional());

        _sqlTokenHelpMap.put(SqlToken.StringConditionalComponent1, _constants.sqlTokenHelp_ConditionalComponent1());
        _sqlTokenHelpMap.put(SqlToken.BooleanConditionalComponent1, _constants.sqlTokenHelp_ConditionalComponent1());
        _sqlTokenHelpMap.put(SqlToken.IntegerConditionalComponent1, _constants.sqlTokenHelp_ConditionalComponent1());
        _sqlTokenHelpMap.put(SqlToken.NumberConditionalComponent1, _constants.sqlTokenHelp_ConditionalComponent1());
        _sqlTokenHelpMap.put(SqlToken.DateTimeConditionalComponent1, _constants.sqlTokenHelp_ConditionalComponent1());
        _sqlTokenHelpMap.put(SqlToken.DateConditionalComponent1, _constants.sqlTokenHelp_ConditionalComponent1());
        _sqlTokenHelpMap.put(SqlToken.TimeConditionalComponent1, _constants.sqlTokenHelp_ConditionalComponent1());

        _sqlTokenHelpMap.put(SqlToken.StringConditionalComponent2, _constants.sqlTokenHelp_ConditionalComponent2());
        _sqlTokenHelpMap.put(SqlToken.BooleanConditionalComponent2, _constants.sqlTokenHelp_ConditionalComponent2());
        _sqlTokenHelpMap.put(SqlToken.IntegerConditionalComponent2, _constants.sqlTokenHelp_ConditionalComponent2());
        _sqlTokenHelpMap.put(SqlToken.NumberConditionalComponent2, _constants.sqlTokenHelp_ConditionalComponent2());
        _sqlTokenHelpMap.put(SqlToken.DateTimeConditionalComponent2, _constants.sqlTokenHelp_ConditionalComponent2());
        _sqlTokenHelpMap.put(SqlToken.DateConditionalComponent2, _constants.sqlTokenHelp_ConditionalComponent2());
        _sqlTokenHelpMap.put(SqlToken.TimeConditionalComponent2, _constants.sqlTokenHelp_ConditionalComponent2());

        _sqlTokenHelpMap.put(SqlToken.StringConditionalDefault, _constants.sqlTokenHelp_ConditionalDefault());
        _sqlTokenHelpMap.put(SqlToken.BooleanConditionalDefault, _constants.sqlTokenHelp_ConditionalDefault());
        _sqlTokenHelpMap.put(SqlToken.IntegerConditionalDefault, _constants.sqlTokenHelp_ConditionalDefault());
        _sqlTokenHelpMap.put(SqlToken.NumberConditionalDefault, _constants.sqlTokenHelp_ConditionalDefault());
        _sqlTokenHelpMap.put(SqlToken.DateTimeConditionalDefault, _constants.sqlTokenHelp_ConditionalDefault());
        _sqlTokenHelpMap.put(SqlToken.DateConditionalDefault, _constants.sqlTokenHelp_ConditionalDefault());
        _sqlTokenHelpMap.put(SqlToken.TimeConditionalDefault, _constants.sqlTokenHelp_ConditionalDefault());

        _sqlTokenHelpMap.put(SqlToken.Extract1stValue, _constants.sqlTokenHelp_Extract1stValue());
        _sqlTokenHelpMap.put(SqlToken.Extract2ndValue, _constants.sqlTokenHelp_Extract2ndValue());
        _sqlTokenHelpMap.put(SqlToken.ExtractNthValue, _constants.sqlTokenHelp_ExtractNthValue());
        _sqlTokenHelpMap.put(SqlToken.ExtractLastValue, _constants.sqlTokenHelp_ExtractLastValue());
        _sqlTokenHelpMap.put(SqlToken.ExtractLastValue, _constants.sqlTokenHelp_ExtractLastValue());
        _sqlTokenHelpMap.put(SqlToken.ExtractMatchingString, _constants.sqlTokenHelp_ExtractMatchingString());

        _sqlTokenHelpMap.put(SqlToken.NullNumber, _constants.sqlTokenHelp_NullValue());
        _sqlTokenHelpMap.put(SqlToken.NullInteger, _constants.sqlTokenHelp_NullValue());
        _sqlTokenHelpMap.put(SqlToken.NullBoolean, _constants.sqlTokenHelp_NullValue());
        _sqlTokenHelpMap.put(SqlToken.NullDate, _constants.sqlTokenHelp_NullValue());
        _sqlTokenHelpMap.put(SqlToken.NullTime, _constants.sqlTokenHelp_NullValue());
        _sqlTokenHelpMap.put(SqlToken.NullDateTime, _constants.sqlTokenHelp_NullValue());
        _sqlTokenHelpMap.put(SqlToken.NullString, _constants.sqlTokenHelp_NullValue());





        Comparator<SqlToken> myComparator = new Comparator<SqlToken>() {

            @Override
            public int compare(SqlToken o1, SqlToken o2) {
                if (o1.getGroup() < o2.getGroup()) {

                    return -1;

                } else if (o1.getGroup() > o2.getGroup()) {

                    return 1;

                } else {

                    return _sqlTokenLabelMap.get(o1).compareTo(_sqlTokenLabelMap.get(o2));
                }
            }
        };
        categorizeEntries(SqlToken.values());
        categorizeEntries(SqlToken.getUserFunctions());

        Collections.sort(_sortedStringFunctionList, myComparator);
        Collections.sort(_sortedBooleanFunctionList, myComparator);
        Collections.sort(_sortedIntegerFunctionList, myComparator);
        Collections.sort(_sortedNumberFunctionList, myComparator);
        Collections.sort(_sortedDateTimeFunctionList, myComparator);
        Collections.sort(_sortedDateFunctionList, myComparator);
        Collections.sort(_sortedTimeFunctionList, myComparator);

        _functionListMap.put(CsiDataType.String, _sortedStringFunctionList);
        _functionListMap.put(CsiDataType.Boolean, _sortedBooleanFunctionList);
        _functionListMap.put(CsiDataType.Integer, _sortedIntegerFunctionList);
        _functionListMap.put(CsiDataType.Number, _sortedNumberFunctionList);
        _functionListMap.put(CsiDataType.DateTime, _sortedDateTimeFunctionList);
        _functionListMap.put(CsiDataType.Date, _sortedDateFunctionList);
        _functionListMap.put(CsiDataType.Time, _sortedTimeFunctionList);
    }

    public static List<SqlToken> getList(CsiDataType dataTypeIn) {

        return (null != dataTypeIn) ? _functionListMap.get(dataTypeIn) : new ArrayList<SqlToken>();
    }

    public static String getTreeLabel(SqlToken tokenIn) {

        String myLabel = null;

        if (null != tokenIn) {

            myLabel = _sqlTokenTreeLabelMap.get(tokenIn);

            if (null == myLabel) {

                myLabel = _sqlTokenLabelMap.get(tokenIn);
            }
        }
        return myLabel;
    }

    public static String getLabel(SqlToken tokenIn) {

        String myString = _sqlTokenLabelMap.get(tokenIn);

        return ((null != myString) && (0 < myString.length())) ? myString : null;
    }

    public static String getTitle(SqlToken tokenIn) {

        String myString = tokenIn.getFormatString();

        return ((null != myString) && (0 < myString.length())) ? myString : null;
    }

    public static String getDescription(SqlToken tokenIn) {

        String myString = _sqlTokenInfoMap.get(tokenIn);

        return ((null != myString) && (0 < myString.length())) ? myString : null;
    }

    public static String getHelp(SqlToken tokenIn) {

        String myString = _sqlTokenHelpMap.get(tokenIn);

        return ((null != myString) && (0 < myString.length())) ? myString : null;
    }

    public static Map<SqlToken, String> getTokenToLabelMap() {

        return _sqlTokenLabelMap;
    }

    public static boolean isConditional(DisplayList<ComponentLabel, SqlToken> displayIn) {

        SqlToken myToken = (null != displayIn) ? displayIn.getSelectedParentObject() : null;

        return (null != myToken) ? SqlTokenType.CONDITIONAL.equals(myToken.getTokenType()) : false;
    }

    public static boolean hasConditionalComponent(DisplayList<ComponentLabel, SqlToken> displayIn) {

        boolean myResponse = false;

        List<SqlToken> myList = (null != displayIn) ? displayIn.getSelectedSiblingObjects() : null;

        if (null != myList) {

            for (SqlToken myToken : myList) {

                if (SqlTokenType.CONDITIONAL_COMPONENT_1.equals(myToken.getTokenType())
                        || SqlTokenType.CONDITIONAL_COMPONENT_2.equals(myToken.getTokenType())) {

                    myResponse = true;
                    break;
                }
            }
        }
        return myResponse;
    }

    public static boolean hasConditionalDefault(DisplayList<ComponentLabel, SqlToken> displayIn) {

        boolean myResponse = false;

        SqlToken mySelection = (null != displayIn) ? displayIn.getSelectedObject() : null;

        if ((null != mySelection) && SqlTokenType.CONDITIONAL_DEFAULT.equals(mySelection.getTokenType())) {

            myResponse = true;

        } else {

            List<SqlToken> myList = (null != displayIn) ? displayIn.getSelectedSiblingObjects() : null;

            if (null != myList) {

                for (SqlToken myToken : myList) {

                    if (SqlTokenType.CONDITIONAL_DEFAULT.equals(myToken.getTokenType())) {

                        myResponse = true;
                        break;
                    }
                }
            }
        }
        return myResponse;
    }

    public static CsiDataType[] getArgumentDataTypes(DisplayList<ComponentLabel, SqlToken> displayIn, Integer parentKeyIn, Integer slotIn) {

        CsiDataType[] myDataTypes = null;
        SqlToken mySelection = displayIn.getDataObject(parentKeyIn);

        if (null != mySelection) {

            if (SqlTokenType.CONDITIONAL.equals(mySelection.getTokenType())) {

                return new CsiDataType[]{mySelection.getType()};

            } else if (mySelection.isSymetric()) {

                SqlToken mySibling = displayIn.getFirstSelectedSiblingObject();

                if (null != mySibling) {

                    myDataTypes = new CsiDataType[] {mySibling.getType()};
                }
            }
            if (null == myDataTypes) {

                myDataTypes = mySelection.getArgumentDataTypes(slotIn);
            }
        }
        return myDataTypes;
    }

    public static boolean isAddOn(DisplayList<ComponentLabel, SqlToken> displayIn) {

        boolean myResult = false;

        SqlToken mySelection = (null != displayIn) ? displayIn.getSelectedObject() : null;

        if (null != mySelection) {

            myResult = SqlTokenType.CONDITIONAL_COMPONENT_1.equals(mySelection.getTokenType())
                        || SqlTokenType.CONDITIONAL_COMPONENT_2.equals(mySelection.getTokenType())
                        || SqlTokenType.CONDITIONAL_DEFAULT.equals(mySelection.getTokenType());
        }
        return myResult;
    }

    public static boolean canDelete(DisplayList<ComponentLabel, SqlToken> displayIn) {

        boolean myOk = false;

        if (displayIn.canDelete()) {

            SqlToken mySelection = (null != displayIn) ? displayIn.getSelectedObject() : null;

            if (null != mySelection) {

                if (SqlTokenType.CONDITIONAL_COMPONENT_1.equals(mySelection.getTokenType())
                        || SqlTokenType.CONDITIONAL_COMPONENT_2.equals(mySelection.getTokenType())) {

                    myOk = hasConditionalComponent(displayIn);

                } else {

                    myOk = true;
                }
            }
        }

        return myOk;
    }

    private static void categorizeEntries(SqlToken[] tokenArrayIn) {

        for (SqlToken myEnum : tokenArrayIn) {

            if (myEnum.isSelectable()) {

                switch (myEnum.getType()) {

                    case String :

                        _sortedStringFunctionList.add(myEnum);
                        break;

                    case Boolean:

                        _sortedBooleanFunctionList.add(myEnum);
                        break;

                    case Integer:

                        _sortedIntegerFunctionList.add(myEnum);
                        break;

                    case Number:

                        _sortedNumberFunctionList.add(myEnum);
                        break;

                    case DateTime:

                        _sortedDateTimeFunctionList.add(myEnum);
                        break;

                    case Date:

                        _sortedDateFunctionList.add(myEnum);
                        break;

                    case Time:

                        _sortedTimeFunctionList.add(myEnum);
                        break;

                    case Unsupported:

                        break;
                }
            }
        }
    }
}

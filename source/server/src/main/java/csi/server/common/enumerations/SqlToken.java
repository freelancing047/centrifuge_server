/**
 *
 */
package csi.server.common.enumerations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.server.common.dto.system.UserFunction;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DisplayListBuilderCallbacks;
import csi.server.common.interfaces.SqlTokenValueCallback;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SqlTokenTreeItem;
import csi.server.common.model.SqlTokenTreeItemList;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;

public enum SqlToken {

    // Expressions -- Boolean
    BooleanAnd(SqlTokenType.EXPRESSION, new String[] {"(", " AND ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    BooleanOR(SqlTokenType.EXPRESSION, new String[] {"(", " OR ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    BooleanNOT(SqlTokenType.EXPRESSION, new String[] {"(NOT ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),

    IsTrue(SqlTokenType.EXPRESSION, new String[] {"(", " IS TRUE)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IsNotTrue(SqlTokenType.EXPRESSION, new String[] {"(", " IS NOT TRUE)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IsFalse(SqlTokenType.EXPRESSION, new String[] {"(", " IS FALSE)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IsNotFalse(SqlTokenType.EXPRESSION, new String[] {"(", " IS NOT FALSE)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IsUnknown(SqlTokenType.EXPRESSION, new String[] {"(", " IS UNKNOWN)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IsNotUnknown(SqlTokenType.EXPRESSION, new String[] {"(", " IS NOT UNKNOWN)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),

    Equal(SqlTokenType.EXPRESSION, new String[] {"( ", " = ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 4),
    NotEqual(SqlTokenType.EXPRESSION, new String[] {"( ", " <> ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 4),
    IsDistinct(SqlTokenType.EXPRESSION, new String[] {"(", " IS DISTINCT FROM ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 4),
    IsNotDistinct(SqlTokenType.EXPRESSION, new String[] {"(", " IS NOT DISTINCT FROM ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 4),

    IsNull(SqlTokenType.EXPRESSION, new String[] {"(", " IS NULL)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.Boolean, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, false, 4),
    IsNotNull(SqlTokenType.EXPRESSION, new String[] {"(", " IS NOT NULL)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.Boolean, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, false, 4),

    LessThan(SqlTokenType.EXPRESSION, new String[] {"(", " < ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),
    GreaterThan(SqlTokenType.EXPRESSION, new String[] {"( ", " > ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),
    LessThanOrEqual(SqlTokenType.EXPRESSION, new String[] {"( ", " <= ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),
    GreaterThanOrEqual(SqlTokenType.EXPRESSION, new String[] {"( ", " >= ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),
    Between(SqlTokenType.EXPRESSION, new String[] {"( ", " BETWEEN ", " AND ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),
    NotBetween(SqlTokenType.EXPRESSION, new String[] {"( ", " NOT BETWEEN ", " AND ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),
    BetweenSymetric(SqlTokenType.EXPRESSION, new String[] {"( ", " BETWEEN SYMMETRIC ", " AND ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),
    NotBetweenSymetric(SqlTokenType.EXPRESSION, new String[] {"( ", " NOT BETWEEN SYMMETRIC ", " AND ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.Boolean, true, 5),

    RegularExpression(SqlTokenType.EXPRESSION, new String[] {"(", " ~ ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.Boolean, false, 6),
    CaselessRegularExpression(SqlTokenType.EXPRESSION, new String[] {"(", " ~* ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.Boolean, false, 6),

    // Expressions -- Bitwise
    BitwiseAnd(SqlTokenType.EXPRESSION, new String[] {"(", " & ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    BitwiseOR(SqlTokenType.EXPRESSION, new String[] {"(", " | ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    BitwiseXOR(SqlTokenType.EXPRESSION, new String[] {"(", " # ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    BitwiseNOT(SqlTokenType.EXPRESSION, new String[] {"(~", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    ShiftLeft(SqlTokenType.EXPRESSION, new String[] {"(", " << ", "::int4)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    ShiftRight(SqlTokenType.EXPRESSION, new String[] {"(", " >> ", "::int4)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),

    // Expressions -- Integer
    IntegerAddition(SqlTokenType.EXPRESSION, new String[] {"(", " + ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    IntegerSubtraction(SqlTokenType.EXPRESSION, new String[] {"(", " - ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    IntegerMultiplication(SqlTokenType.EXPRESSION, new String[] {"(", " * ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    IntegerDivision(SqlTokenType.EXPRESSION, new String[] {"(", " / NULLIF(", ", 0))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    ModuloDivision(SqlTokenType.EXPRESSION, new String[] {"(", " % NULLIF(", ", 0))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    IntegerFactorial(SqlTokenType.EXPRESSION, new String[] {"safe_factorial(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    IntegerExponentiation(SqlTokenType.EXPRESSION, new String[] {"((", " ^ ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),

    // Expressions -- Floating Point
    DecimalAddition(SqlTokenType.EXPRESSION, new String[] {"((", "::float8 + ", "::float8)::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 3),
    DecimalSubtraction(SqlTokenType.EXPRESSION, new String[] {"((", "::float8 - ", "::float8)::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 3),
    DecimalMultiplication(SqlTokenType.EXPRESSION, new String[] {"((", "::float8 * ", "::float8)::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 3),
    DecimalDivision(SqlTokenType.EXPRESSION, new String[] {"((", "::float8 / NULLIF(", "::float8, 0.0))::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 3),
    DecimalExponentiation(SqlTokenType.EXPRESSION, new String[] {"(", "::float8 ^ ", "::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 3),

    // Casting, truncation, rounding, and absolute value
    CastInteger(SqlTokenType.EXPRESSION, new String[] {"cast_integer(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.String}}, CsiDataType.Integer, false, 4),
    CastDecimal(SqlTokenType.EXPRESSION, new String[] {"cast_double(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer, CsiDataType.String}}, CsiDataType.Number, false, 4),
    TruncateToInteger(SqlTokenType.FUNCTION, new String[] {"(trunc(", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Integer, false, 4),
    TruncateDecimal(SqlTokenType.FUNCTION, new String[] {"(trunc(" , ", ", "::int)::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Number, false, 4),
    RoundToInteger(SqlTokenType.FUNCTION, new String[] {"(round(", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Integer, false, 4),
    RoundDecimal(SqlTokenType.FUNCTION, new String[] {"(round(" , ", ", "::int)::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Number, false, 4),
    AbsoluteInteger(SqlTokenType.FUNCTION, new String[] {"abs(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 4),
    AbsoluteDecimal(SqlTokenType.FUNCTION, new String[] {"abs(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 4),

    // Functions -- Integer
    ScanInteger(SqlTokenType.FUNCTION, new String[] {"(to_number(" , ", ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.Integer, false, 6),
    ScanDecimal(SqlTokenType.FUNCTION, new String[] {"(to_number(" , ", ", ")::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.Number, false, 4),

    // Functions -- Floating Point
    SquareRoot(SqlTokenType.FUNCTION, new String[] {"sqrt(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    CubeRoot(SqlTokenType.FUNCTION, new String[] {"cbrt(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    NaturalLog(SqlTokenType.FUNCTION, new String[] {"ln(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    LogBase10(SqlTokenType.FUNCTION, new String[] {"log(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),

    // Trig Functions -- Floating Point
    Sine(SqlTokenType.FUNCTION, new String[] {"sin(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    Cosine(SqlTokenType.FUNCTION, new String[] {"cos(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    Tangent(SqlTokenType.FUNCTION, new String[] {"tan(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    Cotangent(SqlTokenType.FUNCTION, new String[] {"cot(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    InverseSine(SqlTokenType.FUNCTION, new String[] {"csi_asin(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    InverseCosine(SqlTokenType.FUNCTION, new String[] {"csi_acos(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    InverseTangent(SqlTokenType.FUNCTION, new String[] {"csi_atan(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),
    InverseCotangent(SqlTokenType.FUNCTION, new String[] {"csi_acot(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer}}, CsiDataType.Number, false, 5),

    // Operations -- String
    Concatenation(SqlTokenType.EXPRESSION, new String[] {"(", " || ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String, CsiDataType.Number, CsiDataType.Integer, CsiDataType.Boolean, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.String, false, 3),

    // Functions -- String
    ToString(SqlTokenType.EXPRESSION, new String[] {"('' || ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer, CsiDataType.Boolean, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}}, CsiDataType.String, false, 6),
    FormatValue(SqlTokenType.FUNCTION, new String[] {"to_char(", ", ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number, CsiDataType.Integer, CsiDataType.DateTime, CsiDataType.Date, CsiDataType.Time}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 6),
    CharacterLength(SqlTokenType.FUNCTION, new String[] {"char_length(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.Integer, false, 5),
    ByteCount(SqlTokenType.FUNCTION, new String[] {"octet_length(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.Integer, false, 5),
    BitCount(SqlTokenType.FUNCTION, new String[] {"bit_length(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.Integer, false, 5),
    Capitalize(SqlTokenType.FUNCTION, new String[] {"initcap(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 4),
    UpperCase(SqlTokenType.FUNCTION, new String[] {"upper(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 4),
    LowerCase(SqlTokenType.FUNCTION, new String[] {"lower(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 4),
    LeftTrim1(SqlTokenType.FUNCTION, new String[] {"ltrim(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 5),
    LeftTrim2(SqlTokenType.FUNCTION, new String[] {"ltrim(", ", ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 5),
    RightTrim1(SqlTokenType.FUNCTION, new String[] {"rtrim(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 5),
    RightTrim2(SqlTokenType.FUNCTION, new String[] {"rtrim(", ", ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 5),
    LeftPad1(SqlTokenType.FUNCTION, new String[] {"lpad(", ", (", "::integer))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.String, false, 5),
    LeftPad2(SqlTokenType.FUNCTION, new String[] {"lpad(", ", (", "::integer), ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 5),
    RightPad1(SqlTokenType.FUNCTION, new String[] {"rpad(", ", (", "::integer))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.String, false, 5),
    RightPad2(SqlTokenType.FUNCTION, new String[] {"rpad(", ", (", "::integer), ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 5),
    Substring1(SqlTokenType.FUNCTION, new String[] {"substr(", ", (", "::integer))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.String, false, 3),
    Substring2(SqlTokenType.FUNCTION, new String[] {"substr(", ", (", "::integer), (", "::integer))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.String, false, 3),
    LocateSubstring(SqlTokenType.FUNCTION, new String[] {"strpos(" , ", ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.Integer, false, 5),
    GetSingleToken(SqlTokenType.FUNCTION, new String[] {"get_nth_token(" , ", " , ", (", "::integer))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.String, false, 3),

    NumToken(SqlTokenType.FUNCTION, new String[] {"num_token((" , "::char varying), (" , "::char varying))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.Integer, false, 5),

    // Expressions -- DateTime
    TimestampPlusTime(SqlTokenType.EXPRESSION, new String[] {"(", " + ", "::interval)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.Time, CsiDataType.Time}}, CsiDataType.DateTime, false, 3),
    TimestampMinusTimeStamp(SqlTokenType.EXPRESSION, new String[] {"(", " - ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Time, false, 3),
    TimestampMinusDate(SqlTokenType.EXPRESSION, new String[] {"(", " - (", " + time '00:00:00'))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Time, false, 3),
    TimestampMinusTime(SqlTokenType.EXPRESSION, new String[] {"(", " - ", "::interval)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.Time}}, CsiDataType.DateTime, false, 3),

    // Expressions -- Date
    DatePlusDays(SqlTokenType.EXPRESSION, new String[] {"(", " + ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Date, false, 3),
    DatePlusTime(SqlTokenType.EXPRESSION, new String[] {"(", " + ", "::interval)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}, new CsiDataType[] {CsiDataType.Time}}, CsiDataType.DateTime, false, 3),
    DateMinusDate(SqlTokenType.EXPRESSION, new String[] {"(", " - ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}, new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Integer, false, 3),
    DateMinusDays(SqlTokenType.EXPRESSION, new String[] {"(", " - ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Date, false, 3),
    DateMinusTime(SqlTokenType.EXPRESSION, new String[] {"(", " - ", "::interval)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}, new CsiDataType[] {CsiDataType.Time}}, CsiDataType.DateTime, false, 3),

    // Expressions -- Time
    TimePlusTime(SqlTokenType.EXPRESSION, new String[] {"(", " + ", "::interval)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}, new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 3),
    TimeMinusTime(SqlTokenType.EXPRESSION, new String[] {"(", " - ", "::interval)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}, new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 3),
    TimeMultiplied(SqlTokenType.EXPRESSION, new String[] {"(", " * ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}, new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),
    TimeDivided(SqlTokenType.EXPRESSION, new String[] {"(", " / NULLIF(", ", 0))"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}, new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),

    // Casting and truncation -- Time
    TimeAsTimeOfDay(SqlTokenType.EXPRESSION, new String[] {"cast_time(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 3),
    TimestampAsDate(SqlTokenType.EXPRESSION, new String[] {"cast_date(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Date, false, 3),
    AsNumberOfDays(SqlTokenType.EXPRESSION, new String[] {"(Interval '1 day' * ", ")::interval"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),
    AsNumberOfHours(SqlTokenType.EXPRESSION, new String[] {"(Interval '1 hour' * ", ")::interval"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),
    AsNumberOfMinutes(SqlTokenType.EXPRESSION, new String[] {"(Interval '1 minute' * ", ")::interval"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),
    AsNumberOfSeconds(SqlTokenType.EXPRESSION, new String[] {"(Interval '1 second' * ", ")::interval"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),
    AsNumberOfMilliseconds(SqlTokenType.EXPRESSION, new String[] {"(Interval '1 millisecond' * ", ")::interval"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),
    AsNumberOfMicroseconds(SqlTokenType.EXPRESSION, new String[] {"(Interval '1 microsecond' * ", ")::interval"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer, CsiDataType.Number}}, CsiDataType.Time, false, 3),

    // Functions -- DateTime -- System Values
    CurrentTimestamp(SqlTokenType.SYSTEM_VALUE, new String[] {"current_timestamp::timestamp"}, new CsiDataType[][] {}, CsiDataType.DateTime, false, 3),
    CurrentDate(SqlTokenType.SYSTEM_VALUE, new String[] {"current_date"}, new CsiDataType[][] {}, CsiDataType.Date, false, 3),
    CurrentTime(SqlTokenType.SYSTEM_VALUE, new String[] {"current_time::time"}, new CsiDataType[][] {}, CsiDataType.Time, false, 3),
    InputUnixEpoch(SqlTokenType.FUNCTION, new String[] {"to_timestamp(" ,  ")::timestamp"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.DateTime, false, 4),
    ScanDateTime(SqlTokenType.FUNCTION, new String[] {"to_timestamp(" , ", ", ")::timestamp"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.DateTime, false, 4),
    ScanDate(SqlTokenType.FUNCTION, new String[] {"to_date(" , ", ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.Date, false, 4),

    // Functions -- DateTime -- Exctraction
    DateDateExtract(SqlTokenType.FUNCTION, new String[] {"date_trunc('day', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Date, false, 4),
    DateTimeExtract(SqlTokenType.FUNCTION, new String[] {"time_from_timestamp(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Time, false, 4), // Special case -- use argument 2 as argument 1

    DateEpochExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('epoch', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateMillenniumExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('millennium', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateCenturyExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('century', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateDecadeExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('decade', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateYearExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('year', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateQuarterExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('quarter', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateMonthExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('month', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateWeekExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('week', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateYearDayExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('doy', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateMonthDayExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('day', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateWeekDayExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('dow', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),

    DateHourExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('hour', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateMinuteExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('minute', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateSecondExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('second', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateMillisecondsExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('milliseconds', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),
    DateMicrosecondsExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('microseconds', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 6),

    DateTimezoneExtract(SqlTokenType.FUNCTION, new String[] {"(date_part('timezone', ", ")::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, -1),

    DayPartDifference(SqlTokenType.FUNCTION, new String[] {"((EXTRACT(EPOCH FROM (", " - ", ")) / 86400)::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 3),
    HourPartDifference(SqlTokenType.FUNCTION, new String[] {"((EXTRACT(EPOCH FROM (", " - ", ")) / 3600)::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 3),
    MinutePartDifference(SqlTokenType.FUNCTION, new String[] {"((EXTRACT(EPOCH FROM (", " - ", ")) / 60)::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 3),
    SecondPartDifference(SqlTokenType.FUNCTION, new String[] {"(EXTRACT(EPOCH FROM (", " - ", "))::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 3),
    MillisecondPartDifference(SqlTokenType.FUNCTION, new String[] {"((EXTRACT(EPOCH FROM (", " - ", ")) * 1000)::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.Integer, false, 3),

    // Functions -- DateTime -- Truncation
    DateMillenniumTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('millennium', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateCenturyTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('century', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateDecadeTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('decade', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateYearTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('year', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateQuarterTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('quarter', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateMonthTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('month', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateWeekTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('week', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateMonthDayTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('day', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateHourTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('hour', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateMinuteTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('minute', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateSecondTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('second', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateMillisecondsTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('milliseconds', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),
    DateMicrosecondsTruncate(SqlTokenType.FUNCTION, new String[] {"date_trunc('microseconds', ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 5),

    // Data Fields
    StringField(SqlTokenType.FIELD_WRAPPER, new String[] {"\"", "\""}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 2),
    BooleanField(SqlTokenType.FIELD_WRAPPER, new String[] {"\"", "\""}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 2),
    IntegerField(SqlTokenType.FIELD_WRAPPER, new String[] {"\"", "\""}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 2),
    NumberField(SqlTokenType.FIELD_WRAPPER, new String[] {"\"", "\""}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 2),
    DateTimeField(SqlTokenType.FIELD_WRAPPER, new String[] {"\"", "\""}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 2),
    DateField(SqlTokenType.FIELD_WRAPPER, new String[] {"\"", "\""}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Date, false, 2),
    TimeField(SqlTokenType.FIELD_WRAPPER, new String[] {"\"", "\""}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 2),

    // Data Parameters
    StringParameter(SqlTokenType.PARAMETER_WRAPPER, new String[] {"{:", "}"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 1),
    BooleanParameter(SqlTokenType.PARAMETER_WRAPPER, new String[] {"{:", "}"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 1),
    IntegerParameter(SqlTokenType.PARAMETER_WRAPPER, new String[] {"{:", "}"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 1),
    NumberParameter(SqlTokenType.PARAMETER_WRAPPER, new String[] {"{:", "}"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 1),
    DateTimeParameter(SqlTokenType.PARAMETER_WRAPPER, new String[] {"{:", "}"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 1),
    DateParameter(SqlTokenType.PARAMETER_WRAPPER, new String[] {"{:", "}"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Date, false, 1),
    TimeParameter(SqlTokenType.PARAMETER_WRAPPER, new String[] {"{:", "}"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 1),

    // Actual Data Values
    StringValue(SqlTokenType.VALUE_WRAPPER, new String[] {"('", "'::text)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 0),
    BooleanValue(SqlTokenType.VALUE_WRAPPER, new String[] {"('", "'::boolean)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 0),
    IntegerValue(SqlTokenType.VALUE_WRAPPER, new String[] {"('", "'::int8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 0),
    NumberValue(SqlTokenType.VALUE_WRAPPER, new String[] {"('", "'::float8)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 0),
    DateTimeValue(SqlTokenType.VALUE_WRAPPER, new String[] {"('", "'::timestamp)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 0),
    DateValue(SqlTokenType.VALUE_WRAPPER, new String[] {"('", "'::date)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Date, false, 0),
    TimeValue(SqlTokenType.VALUE_WRAPPER, new String[] {"('", "'::time)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 0),

    // Missing Data Values
    MissingStringValue(SqlTokenType.VALUE_WRAPPER, new String[] {"???"}, new CsiDataType[][] {}, CsiDataType.String, false, 7),
    MissingBooleanValue(SqlTokenType.VALUE_WRAPPER, new String[] {"???"}, new CsiDataType[][] {}, CsiDataType.Boolean, false, 7),
    MissingIntegerValue(SqlTokenType.VALUE_WRAPPER, new String[] {"???"}, new CsiDataType[][] {}, CsiDataType.Integer, false, 7),
    MissingNumberValue(SqlTokenType.VALUE_WRAPPER, new String[] {"???"}, new CsiDataType[][] {}, CsiDataType.Number, false, 7),
    MissingDateTimeValue(SqlTokenType.VALUE_WRAPPER, new String[] {"???"}, new CsiDataType[][] {}, CsiDataType.DateTime, false, 7),
    MissingDateValue(SqlTokenType.VALUE_WRAPPER, new String[] {"???"}, new CsiDataType[][] {}, CsiDataType.Date, false, 7),
    MissingTimeValue(SqlTokenType.VALUE_WRAPPER, new String[] {"???"}, new CsiDataType[][] {}, CsiDataType.Time, false, 7),

    // Conditional Expressions
    StringConditional(SqlTokenType.CONDITIONAL, new String[] {"CASE ", " END"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 3),
    BooleanConditional(SqlTokenType.CONDITIONAL, new String[] {"CASE ", " END"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IntegerConditional(SqlTokenType.CONDITIONAL, new String[] {"CASE ", " END"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    NumberConditional(SqlTokenType.CONDITIONAL, new String[] {"CASE ", " END"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 3),
    DateTimeConditional(SqlTokenType.CONDITIONAL, new String[] {"CASE ", " END"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 3),
    DateConditional(SqlTokenType.CONDITIONAL, new String[] {"CASE ", " END"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Date, false, 3),
    TimeConditional(SqlTokenType.CONDITIONAL, new String[] {"CASE ", " END"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 3),

    StringConditionalComponent1(SqlTokenType.CONDITIONAL_COMPONENT_1, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 3),
    BooleanConditionalComponent1(SqlTokenType.CONDITIONAL_COMPONENT_1, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IntegerConditionalComponent1(SqlTokenType.CONDITIONAL_COMPONENT_1, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    NumberConditionalComponent1(SqlTokenType.CONDITIONAL_COMPONENT_1, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 3),
    DateTimeConditionalComponent1(SqlTokenType.CONDITIONAL_COMPONENT_1, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 3),
    DateConditionalComponent1(SqlTokenType.CONDITIONAL_COMPONENT_1, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Date, false, 3),
    TimeConditionalComponent1(SqlTokenType.CONDITIONAL_COMPONENT_1, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 3),

    StringConditionalComponent2(SqlTokenType.CONDITIONAL_COMPONENT_2, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 3),
    BooleanConditionalComponent2(SqlTokenType.CONDITIONAL_COMPONENT_2, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IntegerConditionalComponent2(SqlTokenType.CONDITIONAL_COMPONENT_2, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    NumberConditionalComponent2(SqlTokenType.CONDITIONAL_COMPONENT_2, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 3),
    DateTimeConditionalComponent2(SqlTokenType.CONDITIONAL_COMPONENT_2, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 3),
    DateConditionalComponent2(SqlTokenType.CONDITIONAL_COMPONENT_2, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Date, false, 3),
    TimeConditionalComponent2(SqlTokenType.CONDITIONAL_COMPONENT_2, new String[] {"WHEN ", " THEN ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}, new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 3),

    StringConditionalDefault(SqlTokenType.CONDITIONAL_DEFAULT, new String[] {"ELSE ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 3),
    BooleanConditionalDefault(SqlTokenType.CONDITIONAL_DEFAULT, new String[] {"ELSE ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Boolean}}, CsiDataType.Boolean, false, 3),
    IntegerConditionalDefault(SqlTokenType.CONDITIONAL_DEFAULT, new String[] {"ELSE ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Integer, false, 3),
    NumberConditionalDefault(SqlTokenType.CONDITIONAL_DEFAULT, new String[] {"ELSE ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Number}}, CsiDataType.Number, false, 3),
    DateTimeConditionalDefault(SqlTokenType.CONDITIONAL_DEFAULT, new String[] {"ELSE ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime}}, CsiDataType.DateTime, false, 3),
    DateConditionalDefault(SqlTokenType.CONDITIONAL_DEFAULT, new String[] {"ELSE ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date}}, CsiDataType.Date, false, 3),
    TimeConditionalDefault(SqlTokenType.CONDITIONAL_DEFAULT, new String[] {"ELSE ", " "}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Time}}, CsiDataType.Time, false, 3),

    Extract1stValue(SqlTokenType.FUNCTION, new String[] {"extract_nth_value(string_to_array(", ", null), 1)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.Number, false, 4),
    Extract2ndValue(SqlTokenType.FUNCTION, new String[] {"extract_nth_value(string_to_array(", ", null), 2)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.Number, false, 4),
    ExtractNthValue(SqlTokenType.FUNCTION, new String[] {"extract_nth_value(string_to_array(", ", null), ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.Integer}}, CsiDataType.Number, false, 4),
    ExtractLastValue(SqlTokenType.FUNCTION, new String[] {"extract_nth_value(string_to_array(", ", null), -1)"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.Number, false, 4),
    ExtractMatchingString(SqlTokenType.FUNCTION, new String[] {"substring(", " from ", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 3),
    GetLastToken(SqlTokenType.FUNCTION, new String[] {"get_last_token(" , ", " , ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}, new CsiDataType[] {CsiDataType.String}}, CsiDataType.String, false, 3),
    User050(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User051(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User052(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User053(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User054(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User055(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User056(SqlTokenType.FUNCTION, null, null, null, false, -1),
    NullNumber(SqlTokenType.SYSTEM_VALUE, new String[] {"null"}, new CsiDataType[][] {}, CsiDataType.Number, false, 3),
    NullInteger(SqlTokenType.SYSTEM_VALUE, new String[] {"null"}, new CsiDataType[][] {}, CsiDataType.Integer, false, 3),
    NullBoolean(SqlTokenType.SYSTEM_VALUE, new String[] {"null"}, new CsiDataType[][] {}, CsiDataType.Boolean, false, 3),
    NullDate(SqlTokenType.SYSTEM_VALUE, new String[] {"null"}, new CsiDataType[][] {}, CsiDataType.Date, false, 3),
    NullTime(SqlTokenType.SYSTEM_VALUE, new String[] {"null"}, new CsiDataType[][] {}, CsiDataType.Time, false, 3),
    NullDateTime(SqlTokenType.SYSTEM_VALUE, new String[] {"null"}, new CsiDataType[][] {}, CsiDataType.DateTime, false, 3),
    NullString(SqlTokenType.SYSTEM_VALUE, new String[] {"null"}, new CsiDataType[][] {}, CsiDataType.String, false, 3),
    // Casting to date and/or time
    CastDate(SqlTokenType.EXPRESSION, new String[] {"cast_date(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime, CsiDataType.String}}, CsiDataType.Date, false, 4),
    CastTime(SqlTokenType.EXPRESSION, new String[] {"cast_time(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.DateTime, CsiDataType.String}}, CsiDataType.Time, false, 4),
    CastDateTime(SqlTokenType.EXPRESSION, new String[] {"cast_datetime(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.Date, CsiDataType.String}}, CsiDataType.DateTime, false, 4),
    CastBoolean(SqlTokenType.EXPRESSION, new String[] {"cast_boolean(", ")"}, new CsiDataType[][] {new CsiDataType[] {CsiDataType.String}}, CsiDataType.Boolean, false, 4),
    // User defined functions
    User000(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User001(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User002(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User003(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User004(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User005(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User006(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User007(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User008(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User009(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User010(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User011(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User012(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User013(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User014(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User015(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User016(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User017(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User018(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User019(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User020(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User021(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User022(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User023(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User024(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User025(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User026(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User027(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User028(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User029(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User030(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User031(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User032(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User033(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User034(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User035(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User036(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User037(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User038(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User039(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User040(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User041(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User042(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User043(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User044(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User045(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User046(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User047(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User048(SqlTokenType.FUNCTION, null, null, null, false, -1),
    User049(SqlTokenType.FUNCTION, null, null, null, false, -1);

    private static final int _baseGroup = 3;
    private static final int _topGroup = 6;

    private static SqlToken[] _userFunctions = null;

    private static Map<CsiDataType, SqlToken> _dataFieldMap = new HashMap<CsiDataType, SqlToken>();
    private static Map<CsiDataType, SqlToken> _parameterMap = new HashMap<CsiDataType, SqlToken>();
    private static Map<CsiDataType, SqlToken> _dataValueMap = new HashMap<CsiDataType, SqlToken>();
    private static Map<CsiDataType, SqlToken> _conditionalMap = new HashMap<CsiDataType, SqlToken>();

    private SqlTokenType _tokenType;
    private String[] _operators;
    private CsiDataType[][] _arguments;
    private CsiDataType _result;
    private int _group;
    private String _display;
    private String _description;
    private boolean _symetric;

    static {

        _dataFieldMap.put(CsiDataType.String, StringField);
        _dataFieldMap.put(CsiDataType.Boolean, BooleanField);
        _dataFieldMap.put(CsiDataType.Integer, IntegerField);
        _dataFieldMap.put(CsiDataType.Number, NumberField);
        _dataFieldMap.put(CsiDataType.DateTime, DateTimeField);
        _dataFieldMap.put(CsiDataType.Date, DateField);
        _dataFieldMap.put(CsiDataType.Time, TimeField);

        _parameterMap.put(CsiDataType.String, StringParameter);
        _parameterMap.put(CsiDataType.Boolean, BooleanParameter);
        _parameterMap.put(CsiDataType.Integer, IntegerParameter);
        _parameterMap.put(CsiDataType.Number, NumberParameter);
        _parameterMap.put(CsiDataType.DateTime, DateTimeParameter);
        _parameterMap.put(CsiDataType.Date, DateParameter);
        _parameterMap.put(CsiDataType.Time, TimeParameter);

        _dataValueMap.put(CsiDataType.String, StringValue);
        _dataValueMap.put(CsiDataType.Boolean, BooleanValue);
        _dataValueMap.put(CsiDataType.Integer, IntegerValue);
        _dataValueMap.put(CsiDataType.Number, NumberValue);
        _dataValueMap.put(CsiDataType.DateTime, DateTimeValue);
        _dataValueMap.put(CsiDataType.Date, DateValue);
        _dataValueMap.put(CsiDataType.Time, TimeValue);

        _conditionalMap.put(CsiDataType.String, StringConditionalComponent1);
        _conditionalMap.put(CsiDataType.Boolean, BooleanConditionalComponent1);
        _conditionalMap.put(CsiDataType.Integer, IntegerConditionalComponent1);
        _conditionalMap.put(CsiDataType.Number, NumberConditionalComponent1);
        _conditionalMap.put(CsiDataType.DateTime, DateTimeConditionalComponent1);
        _conditionalMap.put(CsiDataType.Date, DateConditionalComponent1);
        _conditionalMap.put(CsiDataType.Time, TimeConditionalComponent1);
    }

    private SqlToken(SqlTokenType tokenTypeIn, String[] operatorsIn, CsiDataType[][] argumentsIn,
                     CsiDataType resultIn, boolean symetricIn, int groupIn) {

        _tokenType = tokenTypeIn;
        _operators = operatorsIn;
        _arguments = argumentsIn;
        _result = resultIn;
        _symetric = symetricIn;
        _group = groupIn;
        _display = null;
        _description = null;
    }

    public String getDescription() {

        return _description;
    }

    public void setDescription(String descriptionIn) {

        _description = descriptionIn;
    }

    public static boolean defineFunction(UserFunction definitionIn) {

        boolean mySuccess = false;
        int myId = User000.ordinal() + (int)definitionIn.getId();

        if ((User000.ordinal() <= myId) && (values().length > myId)) {

            if (replaceFunction(myId, definitionIn.getGroup(),

                    definitionIn.getFunctionName(), definitionIn.getResult(), definitionIn.getArguments())) {

                mySuccess = values()[myId].setDisplay(definitionIn.getDisplayName());
            }
        }
        return mySuccess;
    }

    public static boolean replaceFunction(int idIn, int groupIn, String nameIn, int resultIn, String argumentsIn) {

        boolean mySuccess = false;
        int myTypeLimit = CsiDataType.Unsupported.ordinal();

        if ((0 <= idIn) && (values().length > idIn) && (0 <= groupIn) && (8 > groupIn)
                && (0 <= resultIn) && (myTypeLimit > resultIn)) {

            SqlToken myToken = values()[idIn];

            if (SqlTokenType.FUNCTION == myToken.getTokenType()) {

                String[] myArgumentTypes = (null != argumentsIn) ? StringUtil.split(argumentsIn) : new String[0];
                String[] myFormat = new String[myArgumentTypes.length + 1];

                if (1 < myFormat.length) {

                    myFormat[0] = nameIn + "(";
                    for (int i = 1; (myFormat.length - 1) > i; i++) {

                        myFormat[i] = ", ";
                    }
                    myFormat[myFormat.length - 1] = ")";

                } else {

                    myFormat[0] = nameIn + "()";
                }
                mySuccess = replaceExpression(idIn, groupIn, myFormat, resultIn, argumentsIn);
            }
        }
        return mySuccess;
    }

    public static boolean replaceExpression(int idIn, int groupIn, String[] formatIn, int resultIn, String argumentsIn) {

        boolean mySuccess = false;
        int myTypeLimit = CsiDataType.Unsupported.ordinal();

        if ((0 <= idIn) && (values().length > idIn) && (0 <= groupIn) && (8 > groupIn)
                && (0 <= resultIn) && (myTypeLimit > resultIn)) {

            SqlToken myToken = values()[idIn];
            String[] myArgumentTypes = (null != argumentsIn) ? StringUtil.split(argumentsIn) : new String[0];

            if ((myArgumentTypes.length + 1) == formatIn.length) {

                CsiDataType[][] myArguments = null;
                CsiDataType myResult = CsiDataType.values()[resultIn];

                mySuccess = true;

                if (0 < myArgumentTypes.length) {

                    myArguments = new CsiDataType[myArgumentTypes.length][];
                    for (int i = 0; mySuccess && (myArgumentTypes.length > i); i++) {

                        byte[] myTypeIds = myArgumentTypes[i].getBytes();

                        if (0 < myTypeIds.length) {

                            myArguments[i] = new CsiDataType[myTypeIds.length];
                            for (int j = 0; myTypeIds.length > j; j++) {

                                int myOrdinal = myTypeIds[j] - '0';

                                if ((0 <= myOrdinal) && (myTypeLimit > myOrdinal)) {

                                    myArguments[i][j] = CsiDataType.values()[myOrdinal];

                                } else {

                                    mySuccess = false;
                                    break;
                                }
                            }

                        } else {

                            mySuccess = false;
                        }
                    }
                }
                if (mySuccess) {

                    mySuccess = myToken.replaceDefinition(groupIn, myResult, formatIn, myArguments);
                }
            }
        }
        return mySuccess;
    }

    public static SqlToken[] getUserFunctions() {

        if (null == _userFunctions) {

            List<SqlToken> myActiveFunctions = new ArrayList<>();

            for (int myId = User000.ordinal(); User049.ordinal() >= myId; myId++) {

                SqlToken myToken = values()[myId];

                if (0 <= myToken.getGroup()) {

                    myActiveFunctions.add(myToken);
                }
            }
            _userFunctions = myActiveFunctions.toArray(new SqlToken[0]);
        }
        return _userFunctions;
    }

    private boolean replaceDefinition(int groupIn, CsiDataType resultIn, String[] formatIn, CsiDataType[][] argumentsIn) {

        _group = groupIn;
        _result = resultIn;
        _operators = formatIn;
        _arguments = argumentsIn;

        return true;
    }

    public boolean setDisplay(String displayIn) {

        if (User000.ordinal() <= ordinal()) {

            _display = displayIn;
            return true;
        }
        return false;
    }

    public boolean isSymetric() {

        return _symetric;
    }

    public int getGroup() {

        return _group;
    }

    public boolean isFinal() {

        return (_baseGroup > _group);
    }

    public boolean isSelectable() {

        return ((_baseGroup <= _group) && (_topGroup >= _group));
    }

    public boolean isSystemValue() {

        return (SqlTokenType.SYSTEM_VALUE == _tokenType);
    }

    public boolean isFieldDefToken() {

        return (2 == _group);
    }

    public boolean isParameterToken() {

        return (1 == _group);
    }

    public boolean isDataValueToken() {

        return (0 == _group);
    }

    public static int getBaseGroup() {

        return _baseGroup;
    }

    public static int getTopGroup() {

        return _topGroup;
    }

    public SqlTokenType getTokenType() {

        return _tokenType;
    }

    public String getDisplay() {

        return _display;
    }

    public String getFormatString() {

        StringBuilder myBuffer = new StringBuilder();

        myBuffer.append(_operators[0]);

        for (int i = 0; _arguments.length > i; i++) {

            myBuffer.append("???");
            myBuffer.append(_operators[i + 1]);
        }

        return myBuffer.toString();
    }

    public String removeFormat(DataViewDef metaDataIn, String valueIn) {

        String myResult = null;

        if ((isFinal()) && (null != valueIn)
                && (valueIn.length() >= (_operators[0].length() + _operators[1].length()))) {

            int myStart = _operators[0].length();
            int myEnd = valueIn.length() - _operators[1].length();

            myResult = valueIn.substring(myStart, myEnd);

            switch (_group) {

                // data value
                case 0:

                    break;

                // parameter
                case 1:

                    myResult = metaDataIn.getParameterByName(myResult).getLocalId();
                    break;

                // data field
                case 2:

                    FieldDef myField = metaDataIn.getModelDef().getFieldListAccess().getFieldDefByName(myResult);

                    myResult = (null != myField) ? myField.getLocalId() : null;
                    break;
            }
        }
        return myResult;
    }

    public String format(String valueIn, boolean forExecutionIn) throws CentrifugeException {

        if (isSystemValue()) {

            return _operators[0];

        } else if (isFinal()) {

            if (forExecutionIn) {

                if (null != valueIn) {

                    StringBuilder myBuffer = new StringBuilder();

                    if (1 == _group) {

                        myBuffer.append('\'');
                        myBuffer.append(valueIn);
                        myBuffer.append('\'');

                    } else {

                        myBuffer.append(_operators[0]);
                        myBuffer.append(valueIn);
                        myBuffer.append(_operators[1]);
                    }

                    return myBuffer.toString();

                } else {

                    return "null";
                }

            } else {

                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append(_operators[0]);
                myBuffer.append((null != valueIn) ? valueIn : "???");
                myBuffer.append(_operators[1]);

                return myBuffer.toString();
            }

        } else {

            throw new CentrifugeException("Unexpected String value passed as argument.");
        }
    }

    public String format(SqlTokenValueCallback valueCallbackIn, SqlTokenTreeItemList argumentsIn, boolean forExecutionIn) throws CentrifugeException {

        if (isSystemValue()) {

            return _operators[0];

        } else if (!isFinal()) {

            if (SqlTokenType.CONDITIONAL == getTokenType()) {

                if (argumentsIn.size() >= _arguments.length) {

                    StringBuilder myBuffer = new StringBuilder();

                    myBuffer.append(_operators[0]);
                    int howMany = argumentsIn.size();

                    for (int i = 0; i < howMany; i++) {

                        String myExpression = null;
                        SqlTokenTreeItem myArgument = argumentsIn.get(i);

                        if (null != argumentsIn.get(i)) {

                            validateArgument(0, myArgument.getToken());
                            myExpression = myArgument.format(valueCallbackIn, forExecutionIn);
                        }

                        if ((!forExecutionIn) && (null == myExpression)) {

                            myExpression =  "???";
                        }
                        myBuffer.append(myExpression);
                    }
                    myBuffer.append(_operators[1]);

                    return myBuffer.toString();

                } else {

                    throw new CentrifugeException("Encountered argument list of wrong size.");
                }

            } else if (argumentsIn.size() == _arguments.length) {

                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append(_operators[0]);

                for (int i = 0; _arguments.length > i; i++) {

                    String myExpression = null;
                    SqlTokenTreeItem myArgument = argumentsIn.get(i);

                    if (null != myArgument) {
                        validateArgument(i, myArgument.getToken());
                        myExpression = myArgument.format(valueCallbackIn, forExecutionIn);
                    }

                    if ((!forExecutionIn) && (null == myExpression)) {

                        myExpression =  "???";
                    }
                    myBuffer.append(myExpression);
                    myBuffer.append(_operators[i + 1]);
                }

                return myBuffer.toString();

            } else {

                throw new CentrifugeException("Encountered argument list of wrong size.");
            }

        } else {

            throw new CentrifugeException("Unexpected argument list passed as argument.");
        }
    }

    public void buildDisplay(SqlTokenValueCallback valueCallbackIn, DisplayListBuilderCallbacks<SqlToken, String> builderIn, Map<SqlToken, String> labelMapIn, SqlTokenTreeItemList argumentsIn) throws CentrifugeException {

        String myLabel = (null != labelMapIn) ? labelMapIn.get(this) : null;

        if (isSystemValue()) {

            buildSystemValueDisplay(builderIn, myLabel);

        } else if (!isFinal()) {

            if (SqlTokenType.CONDITIONAL == getTokenType()) {

                if (argumentsIn.size() >= _arguments.length) {

                    builderIn.beginItem(this, myLabel);

                    builderIn.addSegment(_operators[0]);
                    int howMany = argumentsIn.size();

                    for (int i = 0; i < howMany; i++) {

                        validateArgument(0, argumentsIn.get(i).getToken());

                        if (null != argumentsIn.get(i)) {

                            argumentsIn.get(i).buildDisplay(valueCallbackIn, builderIn, labelMapIn);

                        } else {

                            builderIn.addEmptyValue(Integer.valueOf(i));
                        }
                    }

                    builderIn.addSegment(_operators[1]);

                    builderIn.endItem(this, myLabel);

                } else {

                    throw new CentrifugeException("Encountered argument list of wrong size.");
                }

            } else if (argumentsIn.size() == _arguments.length) {

                builderIn.beginItem(this, myLabel);

                builderIn.addSegment(_operators[0]);

                for (int i = 0; _arguments.length > i; i++) {

                    validateArgument(i, argumentsIn.get(i).getToken());

                    if (null != argumentsIn.get(i)) {

                        argumentsIn.get(i).buildDisplay(valueCallbackIn, builderIn, labelMapIn);

                    } else {

                        builderIn.addEmptyValue(Integer.valueOf(i));
                    }

                    builderIn.addSegment(_operators[i + 1]);
                }

                builderIn.endItem(this, (labelMapIn == null) ? null : labelMapIn.get(this));

            } else {

                throw new CentrifugeException("Encountered argument list of wrong size.");
            }

        } else {

            throw new CentrifugeException("Attempted to build a branch display of terminating item.");
        }
    }

    public void buildFinalDisplay(DisplayListBuilderCallbacks<SqlToken, String> builderIn, String valueIn, Map<SqlToken, String> labelMapIn) throws CentrifugeException {

        String myLabel = (null != labelMapIn) ? labelMapIn.get(this) : null;

        if (isSystemValue()) {

            buildSystemValueDisplay(builderIn, myLabel);

        } else if (isFinal()) {

            builderIn.addValue(this, this.format(valueIn, false));

        } else {

            throw new CentrifugeException("Attempted to build a terminating display of branch item.");
        }
    }

    public void buildEmptyDisplay(DisplayListBuilderCallbacks<SqlToken, String> builderIn, Map<SqlToken, String> labelMapIn) throws CentrifugeException {

        String myLabel = (null != labelMapIn) ? labelMapIn.get(this) : null;

        if (isSystemValue()) {

            buildSystemValueDisplay(builderIn, myLabel);

        } else if (!isFinal()) {

            builderIn.beginItem(this, myLabel);

            builderIn.addSegment(_operators[0]);

            for (int i = 1; _operators.length > i; i++) {

                builderIn.addEmptyValue(Integer.valueOf(i));
                builderIn.addSegment(_operators[i]);
            }

            builderIn.endItem(this, myLabel);

        } else {

            throw new CentrifugeException("Attempted to build a branch display of terminating item.");
        }
    }

    public void buildSystemValueDisplay(DisplayListBuilderCallbacks<SqlToken, String> builderIn, String labelIn) throws CentrifugeException {

        builderIn.beginItem(this, labelIn);
        builderIn.addSegment(_operators[0]);
        builderIn.endItem(this, labelIn);
    }

    public SqlToken getConditionalComponent() {

        return _conditionalMap.get(_result);
    }

    public int getArgumentCount() {

        return _arguments.length;
    }

    public CsiDataType getType() {

        return _result;
    }

    public CsiDataType[] getArgumentTypeArray(int indexIn) {

        CsiDataType[] myTypeArray = null;

        if (_arguments.length > indexIn) {

            myTypeArray = _arguments[indexIn];
        }
        return myTypeArray;
    }

    public int getArgumentTypeMask(int indexIn) {

        int myMask = 0;

        CsiDataType[] myTypeArray = getArgumentTypeArray(indexIn);

        if (null != myTypeArray) {

            for (CsiDataType myType : myTypeArray) {

                myMask |= (1 << myType.ordinal());
            }
        }
        return myMask;
    }

    public String getTypeLabel() {

        return _result.getLabel();
    }

    public void validateArgument(int indexIn, SqlToken tokenIn) throws CentrifugeException {

        if (_arguments.length > indexIn) {

            CsiDataType[] myAcceptableTypes = _arguments[indexIn];

            for (int i = 0; myAcceptableTypes.length > i; i++) {

                if (tokenIn.getType() == myAcceptableTypes[i]) {

                    return;
                }
            }
            throw new CentrifugeException("Argument type " + Format.value(tokenIn.name())
                    + " not accepted for argument " + Integer.toString(indexIn) + ".");

        } else {

            throw new CentrifugeException("Argument slot " + Integer.toString(indexIn) + " does not exist.");
        }
    }

    public final CsiDataType[] getArgumentDataTypes(Integer slotIn) {

        CsiDataType[] myDataTypes = null;

        if (null != slotIn) {

            if (SqlTokenType.CONDITIONAL == _tokenType) {

                int mySlot = slotIn - 1;

                if (_arguments.length > mySlot) {

                    myDataTypes = _arguments[mySlot];
                }

            } else if (1 == (slotIn % 2)) {

                int mySlot = slotIn / 2;

                if (_arguments.length > mySlot) {

                    myDataTypes = _arguments[mySlot];
                }
            }
        }

        return myDataTypes;
    }

    public int getArgumentDataTypeMask(Integer slotIn) {

        return CsiDataType.getDataTypeMask(getArgumentDataTypes(slotIn));
    }

    public static SqlToken getDataFieldToken(CsiDataType dataTypeIn) {

        return _dataFieldMap.get(dataTypeIn);
    }

    public static SqlToken getParameterToken(CsiDataType dataTypeIn) {

        return _parameterMap.get(dataTypeIn);
    }

    public static SqlToken getDataValueToken(CsiDataType dataTypeIn) {

        return _dataValueMap.get(dataTypeIn);
    }
}
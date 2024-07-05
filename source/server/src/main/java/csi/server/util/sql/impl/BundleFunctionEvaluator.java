/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.util.sql.impl;

import java.text.MessageFormat;
import java.util.List;

import com.google.common.base.Preconditions;

import csi.server.util.sql.api.BundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class BundleFunctionEvaluator {

    /**
     * @param function
     * @param fieldName The database column
     * @param paramValues Parameters for the function.
     * @return SQL expression for the bundle function.
     */
    public static String getFieldExpression(BundleFunction function, String fieldName, List<String> paramValues) {
        Preconditions.checkNotNull(paramValues);

        switch (function) {
            case NONE:
                return fieldName;
            case LEFT:
                return MessageFormat.format("substring( {0} for {1} )", fieldName, paramValues.get(0));
            case RIGHT:
                return MessageFormat.format("substring( {0} from length( {0} ) - {1} + 1 ) ", fieldName,
                        paramValues.get(0));
            case LENGTH:
                return "length( trim( both from " + fieldName + " ) )";
            case SUBSTRING:
                StringBuilder builder = new StringBuilder();
                builder.append("substring( ").append(fieldName);

                if (paramValues.get(0) != null && paramValues.get(0).length() != 0) {
                    builder.append(" from ").append(paramValues.get(0));
                }

                if (paramValues.get(1) != null && paramValues.get(1).length() > 0) {
                    builder.append(" for ").append(paramValues.get(1));
                }

                builder.append(" )");
                return builder.toString();
            case TRIM:
                return "trim(both from " + fieldName + ")";
            case REGEX_REPLACE:
                return "regexp_replace(" + fieldName + ", '" + paramValues.get(0) + "', '" + paramValues.get(1)
                        + "', '" + paramValues.get(2) + "')";
            case SPLIT:
                return "split_part(" + fieldName + ", '" + paramValues.get(0) + "', " + paramValues.get(1) + ")";
            case COUNT_TOKEN:
                return "num_token(" + fieldName + ", '" + paramValues.get(0) + "')";
            case SINGLE_TOKEN:
                return "get_nth_token(" + fieldName + ", '" + paramValues.get(0) + "', " + paramValues.get(1) + ")";

            case YEAR:
                return "cast(date_part( 'year', " + fieldName + " ) as integer)";
            case MONTH:
                return "cast(date_part( 'month', " + fieldName + " ) as integer)";
            case DAY_OF_MONTH:
                return "cast(date_part( 'day', " + fieldName + " ) as integer)";
            case HOUR:
                return "cast(date_part( 'hour', " + fieldName + " ) as integer)";
            case DATE:
                return "date( " + fieldName + " )";
            case MINUTE:
                return "cast(date_part( 'minute', " + fieldName + " ) as integer)";
            case SECOND:
                return "cast(date_part( 'second', " + fieldName + " ) as integer)";
            case MILLISECOND:
                return "cast(date_part( 'milliseconds', " + fieldName + " ) as integer)";
            case MICROSECOND:
                return "cast(date_part( 'microseconds', " + fieldName + " ) as integer)";
            case YEAR_MONTH:
                return "to_char( " + fieldName + ", 'YYYY-MM' )";
            case QUARTER:
                return "cast(date_part('quarter', " + fieldName + ") as integer)";
            case DAY_OF_WEEK:
                return "cast(date_part('dow', " + fieldName + ") as integer)";
            case DAY_OF_YEAR:
                return "cast(date_part('doy', " + fieldName + ") as integer)";
            case WEEK:
                return "cast(date_part('week', " + fieldName + ") as integer)";
            case DAY_TYPE:
                return "(case cast(date_part('dow'," + fieldName
                        + ") as integer)when 0 then 'Weekend' when 6 then 'Weekend' else 'Weekday' end)";

            case CEILING:
                return "ceiling( " + fieldName + " )";
            case FLOOR:
                return "floor( " + fieldName + " )";
            case ROUND:
                return "round(cast( " + fieldName + " as numeric) , " + paramValues.get(0) + ")";
            case ABSOLUTE:
                return "abs(" + fieldName + ")";
            case MOD:
                return "mod(" + fieldName + ", " + paramValues.get(0) + ")";
            case SIGN:
                return "sign(" + fieldName + ")";
            default:
                throw new RuntimeException("Unknown bundle function encountered: " + function.getLabel());
        }
    }
    // public static String getFieldValue(BundleFunction bundleFunction, FieldDef field, String value) {
    // CsiDataType type = bundleFunction.getReturnType(field);
    // switch (type) {
    // case Boolean:
    // case Date:
    // case DateTime:
    // case String:
    // case Time:
    // return SqlUtil.singleQuoteWithEscape(value);
    // case Integer:
    // case Number:
    // return value;
    // default:
    // throw new RuntimeException("Unknown type found: " + type);
    // }
    // }
    //
    // public static List<String> getFieldValue(BundleFunction bundleFunction, FieldDef field, List<String> valueList) {
    // List<String> list = new ArrayList<String>();
    // for (String value : valueList) {
    // list.add(getFieldValue(bundleFunction, field, value));
    // }
    // return list;
    // }
    //
    // public static String getFieldEqualityExpression(String fieldExpression, BundleFunction bundleFunction,
    // FieldDef field, String value) {
    // CsiDataType type = bundleFunction.getReturnType(field);
    // switch (type) {
    // case Boolean:
    // case Date:
    // case DateTime:
    // case String:
    // case Time:
    // if ("null".equals(value)) {
    // return fieldExpression + " is null ";
    // } else {
    // return fieldExpression + " = " + getFieldValue(bundleFunction, field, value);
    // }
    // case Integer:
    // case Number:
    // if ("null".equals(value)) {
    // return fieldExpression + " is null ";
    // } else {
    // return fieldExpression + " = " + getFieldValue(bundleFunction, field, value);
    // }
    // default:
    // throw new RuntimeException("Unknown type found: " + type);
    // }
    // }
}

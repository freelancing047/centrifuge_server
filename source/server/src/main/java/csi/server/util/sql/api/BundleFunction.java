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
package csi.server.util.sql.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.util.sql.api.BundleParameterInfo.BundleParameterName;
import csi.shared.core.util.HasLabel;

/**
 * NOTE: If you add a bundle function here, implement the appropriate methods in BundleFunctionEvaluator.
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public enum BundleFunction implements HasLabel {

    NONE("None", CsiDataType.String, 0) {

        @Override
        public CsiDataType getReturnType(FieldDef fieldDef) {
            return fieldDef.getValueType();
        }

        @Override
        public String getLabel(FieldDef field, List<String> parameters) {
            return field.getFieldName();
        }
    }, //
    LEFT("Left", CsiDataType.String, 1, CsiDataType.String) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.LENGTH, CsiDataType.Integer, 1)));
        }
    }, //
    RIGHT("Right", CsiDataType.String, 1, CsiDataType.String) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.LENGTH, CsiDataType.Integer, 1)));
        }
    }, //
    SUBSTRING("Substring", CsiDataType.String, 2, CsiDataType.String) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.START, CsiDataType.Integer, 1),
                                                                    new BundleParameterInfo(BundleParameterName.LENGTH, CsiDataType.Integer, 1)));
        }
    }, //
    LENGTH("Length", CsiDataType.String, 0, CsiDataType.String), //
    TRIM("Trim", CsiDataType.String, 0, CsiDataType.String), //
    REGEX_REPLACE("Regex Replace", CsiDataType.String, 3, CsiDataType.String) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.REGEX, CsiDataType.String, 2),
                                                                    new BundleParameterInfo(BundleParameterName.REPLACEMENT, CsiDataType.String, 2),
                                                                    new BundleParameterInfo(BundleParameterName.FLAGS, CsiDataType.String, 1)));
        }
    }, //
    SPLIT("Split", CsiDataType.String, 2, CsiDataType.String) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.DELIMITER, CsiDataType.String, 2),
                                                                    new BundleParameterInfo(BundleParameterName.INDEX, CsiDataType.Integer, 1)));
        }
    }, //
    COUNT_TOKEN("Count Tokens", CsiDataType.Integer, 1, CsiDataType.String) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.DELIMITER, CsiDataType.String, 2)));
        }
    }, //
    SINGLE_TOKEN("Token", CsiDataType.String, 2, CsiDataType.String) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.DELIMITER, CsiDataType.String, 2),
                                                                    new BundleParameterInfo(BundleParameterName.INDEX, CsiDataType.Integer, 1)));
        }
    }, //

    DATE("Date", CsiDataType.String, 0, CsiDataType.Date, CsiDataType.DateTime), //
    QUARTER("Quarter", CsiDataType.Integer, 0, CsiDataType.Date, CsiDataType.DateTime), //
    YEAR_MONTH("Year and Month", CsiDataType.String, 0, CsiDataType.Date, CsiDataType.DateTime), //
    YEAR("Year", CsiDataType.Integer, 0, CsiDataType.Date, CsiDataType.DateTime), //
    MONTH("Month", CsiDataType.Integer, 0, CsiDataType.Date, CsiDataType.DateTime), //
    DAY_OF_MONTH("Day of Month", CsiDataType.Integer, 0, CsiDataType.Date, CsiDataType.DateTime), //
    HOUR("Hour", CsiDataType.Integer, 0, CsiDataType.DateTime), //
    MINUTE("Minute", CsiDataType.Integer, 0, CsiDataType.DateTime), //
    SECOND("Second", CsiDataType.Integer, 0, CsiDataType.DateTime),
    MILLISECOND("Millisecond", CsiDataType.Integer, 0, CsiDataType.DateTime),
    MICROSECOND("Microsecond", CsiDataType.Integer, 0, CsiDataType.DateTime),
    DAY_OF_WEEK("Day of Week", CsiDataType.Integer, 0, CsiDataType.Date, CsiDataType.DateTime), //
    DAY_OF_YEAR("Day of Year", CsiDataType.Integer, 0, CsiDataType.Date, CsiDataType.DateTime), //
    WEEK("Week", CsiDataType.Integer, 0, CsiDataType.Date, CsiDataType.DateTime), //
    DAY_TYPE("Weekday/Weekend", CsiDataType.String, 0, CsiDataType.Date, CsiDataType.DateTime), //

    CEILING("Ceiling", CsiDataType.Number, 0, CsiDataType.Number), //
    FLOOR("Floor", CsiDataType.Number, 0, CsiDataType.Number), //
    ROUND("Round", CsiDataType.Number, 1, CsiDataType.Number, CsiDataType.Integer) {
        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.DECIMAL_PLACES, CsiDataType.Integer, 1)));
        }
    },
    ABSOLUTE("Absolute", CsiDataType.Number, 0, CsiDataType.Number, CsiDataType.Integer), //
    MOD("Modulo", CsiDataType.Number, 1, CsiDataType.Integer) {

        @Override
        public List<BundleParameterInfo> getParameterInfo() {
            return new ArrayList<BundleParameterInfo>(Arrays.asList(new BundleParameterInfo(BundleParameterName.DIVISOR, CsiDataType.Number, 2)));
        }
    }, //
    SIGN("Sign", CsiDataType.Integer, 0, CsiDataType.Number, CsiDataType.Integer), //
    ;

    private String label;
    private CsiDataType returnType;
    // Data types this bundling function applies to.
    private List<CsiDataType> dataTypes;
    private int paramCount;

   private BundleFunction(String label, CsiDataType returnType, int paramCount, CsiDataType... dataTypes) {
      this.label = label;
      this.returnType = returnType;
      this.dataTypes = new ArrayList<CsiDataType>(Arrays.asList((dataTypes == null) ? CsiDataType.values() : dataTypes));
      this.paramCount = paramCount;
   }

    public int getParamCount() {
        return paramCount;
    }

    public List<BundleParameterInfo> getParameterInfo() {
        return new ArrayList<BundleParameterInfo>();
    }

    public String getLabel() {
        return label;
    }

    public CsiDataType getReturnType(FieldDef fieldDef) {
        return returnType;
    }

    public boolean isApplicableFor(CsiDataType dataType) {
        return dataTypes.contains(dataType);
    }

    public static List<BundleFunction> forType(CsiDataType dataType) {
        List<BundleFunction> list = new ArrayList<BundleFunction>();
        for (BundleFunction bf : BundleFunction.values()) {
            if (bf.dataTypes.contains(dataType)) {
                list.add(bf);
            }
        }
        return list;
    }

   public String getLabel(FieldDef field, List<String> parameters) {
      return parameters.isEmpty()
                ? new StringBuilder(getLabel())
                            .append("(")
                            .append(field.getFieldName())
                            .append(")")
                            .toString()
                : new StringBuilder(getLabel())
                            .append(parameters.stream().collect(Collectors.joining(", ", "(", ")")))
                            .toString();
   }
}

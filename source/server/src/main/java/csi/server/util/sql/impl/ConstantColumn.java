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

import java.util.Date;

import csi.server.common.enumerations.CsiDataType;
import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ConstantColumn<T> extends AbstractColumn {

    private T constantValue;
    private CsiDataType csiDataType;

    public ConstantColumn(TableSourceSpi tableSource, T constantValue, CsiDataType dataType) {
        super(tableSource);
        this.constantValue = constantValue;
        csiDataType = dataType;
    }

    @Override
    public String getSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append(getAggregatedBundledColumnExpression(getConstantValue()));
        return builder.toString();
    }

    @Override
    public String getSQLWithoutTableAlias() {
        return getSQL();
    }

    private String getConstantValue() {
        switch (csiDataType) {
            case DateTime:
                if (null == constantValue) {
                } else if (constantValue instanceof String) {
                    return "'" + constantValue + "'";
                } else if (constantValue instanceof Date) {

                    return String.format("%Y-%1m-%1d %1H:%1M:%1S", constantValue);
                }
            case Date:
                if (null == constantValue) {
                } else if (constantValue instanceof String) {
                    return "'" + constantValue + "'";
                } else if (constantValue instanceof Date) {

                    return String.format("%Y-%1m-%1d", constantValue);
                }
            case Time:
                if (null == constantValue) {
                } else if (constantValue instanceof String) {
                    return "'" + constantValue + "'";
                } else if (constantValue instanceof Date) {

                    return String.format("%1H:%1M:%1S", constantValue);
                }
            case Number:
            case Boolean:
            case Integer:
                return "null".equals(constantValue) || constantValue == null ? "'null'" : constantValue.toString();
            case String:
                return constantValue == null ? "'null'" : "'" + constantValue + "'";
            default:
                throw new RuntimeException("Don't know how to handle " + csiDataType);
        }
    }
}

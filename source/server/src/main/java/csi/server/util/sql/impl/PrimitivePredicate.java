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
import csi.server.util.SqlUtil;
import csi.server.util.sql.impl.spi.PredicateFragmentSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PrimitivePredicate extends AbstractSimplePredicate {

    private CsiDataType valueType;
    private Object value;

    public <T> PrimitivePredicate(PredicateFragmentSpi fragment, T value) {
        super(fragment);
        if (value instanceof String) {
            valueType = CsiDataType.String;
            this.value = value.toString();
        } else if (value instanceof Boolean) {
            valueType = CsiDataType.Boolean;
            this.value = ((Boolean) value).toString();
        } else if (value instanceof Number) {
            valueType = CsiDataType.Number;
            this.value = value.toString();
        } else if (value instanceof Date) {
            valueType = CsiDataType.DateTime;
            // FIXME
            this.value = value.toString();
        } else if (value == null){
            // Null Predicate is handled later
            this.value = null;
        }else {
            throw new RuntimeException("Someone tell me what to do with " + value.getClass().getName());
        }
    }

    public PrimitivePredicate(PredicateFragmentImpl fragment, Object value, CsiDataType dataType) {
        super(fragment);
        this.value = value;
        this.valueType = dataType;
    }

    @Override
    public String getSQL() {
        return getPredicateFragment().getSQLFragment() + getValue();
    }

    @Override
    public String getAliasedSQL() {
        return getPredicateFragment().getAliasedSQLFragment() + getValue();
    }

    protected String getValue() {
        switch (valueType) {
            case Boolean:
            case Integer:
            case Number:
                return value.toString();
            case DateTime:
                if (null == value) {
                } else if (value instanceof String) {
                    return "'" + value + "'";
                } else if (value instanceof Date) {

                    return String.format("%Y-%1m-%1d %1H:%1M:%1S", value);
                }
            case Date:
                if (null == value) {
                } else if (value instanceof String) {
                    return "'" + value + "'";
                } else if (value instanceof Date) {

                    return String.format("%Y-%1m-%1d", value);
                }
            case Time:
                if (null == value) {
                } else if (value instanceof String) {
                    return "'" + value + "'";
                } else if (value instanceof Date) {

                    return String.format("%1H:%1M:%1S", value);
                }
            case String:
                return SqlUtil.singleQuoteWithEscape((value == null) ? "" : value.toString());
            default:
                throw new RuntimeException("Don't know how to handle " + valueType);
        }
    }
}

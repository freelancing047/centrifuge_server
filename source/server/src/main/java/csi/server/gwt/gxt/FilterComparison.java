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
package csi.server.gwt.gxt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum FilterComparison {

    LESS_THAN("lt"), //
    EQUALS("eq"), //
    GREATER_THAN("gt"), //
    CONTAINS("contains"), //
    BEFORE("before"), //
    ON("on"), //
    AFTER("after");

    private String code;

    private FilterComparison(String comparisonCode) {
        this.code = comparisonCode;
    }

    private static Map<String, FilterComparison> _codeToEnumMapping = new HashMap<String, FilterComparison>();

    static {
        for (FilterComparison fc : values()) {
            _codeToEnumMapping.put(fc.code, fc);
        }
    }

    public static FilterComparison forCode(String value) {
        return _codeToEnumMapping.get(value);
    }
}

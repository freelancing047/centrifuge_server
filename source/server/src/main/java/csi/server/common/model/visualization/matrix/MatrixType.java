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
package csi.server.common.model.visualization.matrix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum MatrixType implements Serializable, HasLabel {

    BUBBLE("Bubble"),
    HEAT_MAP("Heat map"),
    CO_OCCURRENCE("Co-occurrence"),
    CO_OCCURRENCE_DIR("Co-occurrence (Directed)");

    private String label;

    private MatrixType(String label) {
        this.label = label;
    }

    private static Map<String, MatrixType> _codeToEnumMapping = new HashMap<String, MatrixType>();

    static {
        for (MatrixType fc : values()) {
            _codeToEnumMapping.put(fc.label, fc);
        }
    }

    public static MatrixType forCode(String value) {
        return _codeToEnumMapping.get(value);
    }

    @Override
    public String getLabel() {
        return label;
    }


}

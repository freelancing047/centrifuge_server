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

import java.util.List;
import java.util.Map;

import csi.server.util.sql.SelectResultRow;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SelectResultRowImpl implements SelectResultRow {

    private Map<String, Integer> columnIndicesByName;
    private List<Object> values;

    public SelectResultRowImpl(Map<String, Integer> columnIndicesByName, List<Object> values) {
        super();
        this.columnIndicesByName = columnIndicesByName;
        this.values = values;
    }

    private int getIndex(String columnName) {
        return columnIndicesByName.get(columnName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(String columnName) {
        return (T) getValue(getIndex(columnName));
    }

    /**
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(int index) {
        return (T) values.get(index);
    }

}

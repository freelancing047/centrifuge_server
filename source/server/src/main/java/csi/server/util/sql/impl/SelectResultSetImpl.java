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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import csi.server.util.sql.SelectResultRow;
import csi.server.util.sql.SelectResultSet;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SelectResultSetImpl implements SelectResultSet {

    private int columnCount;

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    private List<SelectResultRow> rows = new ArrayList<SelectResultRow>();

    @Override
    public Iterator<SelectResultRow> iterator() {
        return rows.iterator();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    public void add(SelectResultRowImpl row) {
        rows.add(row);
    }

}

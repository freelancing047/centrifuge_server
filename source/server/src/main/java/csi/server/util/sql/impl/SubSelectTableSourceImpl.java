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

import csi.server.util.sql.Column;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.impl.spi.SelectSQLSpi;
import csi.server.util.sql.impl.spi.SubSelectTableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SubSelectTableSourceImpl extends AbstractTableSource implements SubSelectTableSourceSpi {

    private SelectSQLSpi innerSelect;

    public SubSelectTableSourceImpl(SelectSQL innerSelect) {
        this.innerSelect = (SelectSQLSpi) innerSelect;
    }

    @Override
    public List<? extends Column> getSubSelectColumns() {
        return innerSelect.getSelectColumns();
    }

    @Override
    public Column getColumn(Column subSelectColumn) {
        return new ColumnReferencingColumn(this, subSelectColumn);
    }

    @Override
    public String getSQL() {
        return "(" + innerSelect.getSQL() + ")";
    }

}

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

import csi.server.util.sql.Column;
import csi.server.util.sql.impl.spi.ColumnSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnReferencingColumn extends AbstractColumn {

    private ColumnSpi secondColumn;

    public ColumnReferencingColumn(SubSelectTableSourceImpl subSelectTableSource, Column subSelectColumn) {
        super(subSelectTableSource);
        this.secondColumn = (ColumnSpi) subSelectColumn;
    }

    @Override
    public String getSQL() {
        return getAggregatedBundledColumnExpression(getTableSource().getAlias() + "." + secondColumn.getAlias());
    }

    @Override
    public String getSQLWithoutTableAlias() {
        return getAggregatedBundledColumnExpression(secondColumn.getAlias());
    }

}

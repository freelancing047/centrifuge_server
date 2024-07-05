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
import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DistinctColumn extends AbstractCompositeColumn {

    public DistinctColumn(TableSourceSpi tableSource, Column... columns) {
        super(tableSource, columns);
    }

    @Override
    public String getSQL() {
        return getAggregatedBundledColumnExpression("distinct(" + super.getSQL() + ")");
    }

    @Override
    public String getSQLWithoutTableAlias() {
        return getAggregatedBundledColumnExpression("distinct(" + super.getSQLWithoutTableAlias() + ")");
    }

}

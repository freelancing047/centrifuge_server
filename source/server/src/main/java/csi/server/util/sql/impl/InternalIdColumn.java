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

import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class InternalIdColumn extends AbstractColumn {

    public InternalIdColumn(TableSourceSpi tableSource) {
        super(tableSource);
    }

    @Override
    public String getSQLWithoutTableAlias() {
        return "internal_id";
    }

    @Override
    public String getSQL() {
        return getTableSource().getAlias() + "." + getSQLWithoutTableAlias();
    }

}

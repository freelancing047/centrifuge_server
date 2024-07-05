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

import csi.server.common.model.dataview.DataView;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.SubSelectTableSource;
import csi.server.util.sql.TableSource;
import csi.server.util.sql.TableSourceFactory;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TableSourceFactoryImpl implements TableSourceFactory {

    @Override
    public CacheTableSource create(DataView dataView) {
        return new CacheTableSourceImpl(dataView);
    }

    @Override
    public TableSource create(String tableName) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SubSelectTableSource create(SelectSQL select) {
        return new SubSelectTableSourceImpl(select);
    }

}
